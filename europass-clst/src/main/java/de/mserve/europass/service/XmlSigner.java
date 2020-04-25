package de.mserve.europass.service;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mserve.europass.model.CertificateEntry;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;

public class XmlSigner {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateLoader.class);

    private CertificateEntry cert;
    private XAdESSignatureParameters parameters;

    private XmlSigner() {
    }

    public static XmlSigner build(CertificateEntry cert) {
        XmlSigner x = new XmlSigner();
        x.setCertificateEntry(cert);
        return x;
    }

    private void setCertificateEntry(CertificateEntry cert) {
        // Set token
        this.cert = cert;

        // Preparing parameters for the XAdES signature
        this.parameters = new XAdESSignatureParameters();
        // Default settings
        this.parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
        this.parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        this.parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        // Add key
        this.parameters.setSigningCertificate(this.cert.getEntry().getCertificate());
        this.parameters.setCertificateChain(this.cert.getEntry().getCertificateChain());

    }

    public void setParameters(SignatureLevel level, SignaturePackaging packaging, DigestAlgorithm da) {
        this.parameters.setSignatureLevel(level);
        this.parameters.setSignaturePackaging(packaging);
        this.parameters.setDigestAlgorithm(da);
    }

    public boolean sign(File xml) {
        return sign(xml, FilenameUtils.getFullPath(xml.getAbsolutePath()));
    }
    public boolean sign(File xml, String outDir) {
        
        // Prepare the service
        XAdESService service = new XAdESService(new CommonCertificateVerifier());
            // Prepare the document
        LOG.info("Loading XML file '{}''",  xml.getAbsolutePath());
        DSSDocument document = new FileDocument(xml);
        LOG.info("Using signature level {}, packaging {}, digest {}", this.parameters.getSignatureLevel().name(), 
        this.parameters.getSignaturePackaging().name(), this.parameters.getDigestAlgorithm().name());
        ToBeSigned dataToSign = service.getDataToSign(document, parameters);
        DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();
        if (this.cert == null) {
            LOG.error("No valid certifitate set");
            return false;
        }
        if (this.cert.getSignatureToken()  == null) {
            LOG.error("Certificate '{}' has no valid token", this.cert.getLabel());
            return false;
        }
        SignatureValue signatureValue = this.cert.getSignatureToken().sign(dataToSign, digestAlgorithm, this.cert.getEntry());
        LOG.info("Signature value: {} bytes", signatureValue.getValue().length);
        DSSDocument signedDocument = service.signDocument(document, parameters, signatureValue);
        LOG.info("XML filed successfully signed with key '{}''",  this.cert.getLabel());
        String outFilePath = FilenameUtils.getFullPath(outDir) + FilenameUtils.getBaseName(xml.getAbsolutePath()) + "-signed.xml";
        try {
            signedDocument.save(outFilePath);
        } catch (Exception e) {
            LOG.error("error writing to path '{}'",  outFilePath);
            return false;    
        }
        LOG.info("XML filed successfully written to path '{}'",  outFilePath);
        return true;
    }

}