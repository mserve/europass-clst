package de.mserve.europass.tasks;

import java.security.KeyStore.PasswordProtection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mserve.europass.model.CertificateLoaderOptions;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSASN1Utils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.MSCAPISignatureToken;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.enumerations.QCStatement;

public class CertificateLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateLoader.class);

    private List<DSSPrivateKeyEntry> keys;
    private final CertificateLoaderOptions options;

    public CertificateLoader(final CertificateLoaderOptions _options) {
        this.options = _options;
        loadCertificates();
    }

    private void loadCertificates() {
        switch (this.options.TOKEN_TYPE) {
            case MSCAPI:
                try (MSCAPISignatureToken token = new MSCAPISignatureToken()) {
                    this.keys = token.getKeys();
                }
                break;
            case PKCS12:
                try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(this.options.FILE_NAME,
                        new PasswordProtection(options.PKCS12_PASSWORD.toCharArray()))) {
                    this.keys = token.getKeys();
                } catch (final Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                break;
            default:
                LOG.error("CertStoreNotSupported", "Certificate Store not supported");
        }
    }

    /* gets all keys within the chosen certification storage */
    public Map<String, DSSPrivateKeyEntry> enumerateKeys() {
        final Map<String, DSSPrivateKeyEntry> map = new HashMap<String, DSSPrivateKeyEntry>();
        try {
            for (final DSSPrivateKeyEntry dssPrivateKeyEntry : keys) {
                final CertificateToken certToken = dssPrivateKeyEntry.getCertificate();
                String text = DSSASN1Utils.getHumanReadableName(certToken) + " (" + certToken.getSerialNumber() + ")";
                final List<String> qcStatements = DSSASN1Utils.getQCStatementsIdList(certToken);
                if (qcStatements.contains(QCStatement.QC_COMPLIANCE.getOid())) {
                    text = text + " [Qualified]";
                }
                if (qcStatements.contains(QCStatement.QCT_ESIGN.getOid())) {
                    text = text + " [E-Sign]";
                }
                map.put(text, dssPrivateKeyEntry);
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return map;
    }
}