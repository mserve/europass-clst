package de.mserve.europass.model;

import java.util.ArrayList;
import java.util.List;

import eu.europa.esig.dss.enumerations.QCStatement;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSASN1Utils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;

public class CertificateEntry {
    private DSSPrivateKeyEntry entry;
    private SignatureTokenConnection signatureToken;
    private ArrayList<QCStatement> properties = new ArrayList<>();
    private String label;

    public CertificateEntry(SignatureTokenConnection _stc, DSSPrivateKeyEntry _entry) {
        if (_entry != null) {
            this._setEntry(_entry);
            if (_stc != null) {
                if (_stc.getKeys().contains(_entry))
                    this._setSignatureToken(_stc);
            }
        }
    }
    
    private void _setSignatureToken(SignatureTokenConnection stc) {
        this.signatureToken = stc;
    }
    
    private void _setEntry(DSSPrivateKeyEntry dssPrivateKeyEntry) {
        this.entry = dssPrivateKeyEntry;
        final CertificateToken certToken = dssPrivateKeyEntry.getCertificate();
        this.label = DSSASN1Utils.getHumanReadableName(certToken) + " (" + certToken.getSerialNumber() + ")";
        final List<String> qcStatements = DSSASN1Utils.getQCStatementsIdList(certToken);
        final List<String> qcTypes = DSSASN1Utils.getQCTypesIdList(certToken);
        if (qcStatements.contains(QCStatement.QC_COMPLIANCE.getOid())) {
            this.label = this.label + " [Qualified]";
            this.properties.add(QCStatement.QC_COMPLIANCE);
        }
        if (qcStatements.contains(QCStatement.QC_SSCD.getOid())) {
            this.label = this.label + " [Qualified Signature Creation Device]";
            this.properties.add(QCStatement.QC_SSCD);
        }
        if (qcTypes.contains(QCStatement.QCT_ESIGN.getOid())) {
            this.label = this.label + " [E-Sign]";
            this.properties.add(QCStatement.QCT_ESIGN);
        }
        if (qcTypes.contains(QCStatement.QCT_ESEAL.getOid())) {
            this.label = this.label + " [E-Seal]";
            this.properties.add(QCStatement.QCT_ESEAL);
        }
    }

    public DSSPrivateKeyEntry getEntry() {
        return entry;
    }

    public SignatureTokenConnection getSignatureToken() {
        return signatureToken;
    }

    public String getLabel() {
        return label;
    }

    public boolean hasProperty(QCStatement qcs) {
        return this.properties.contains(qcs);
    }

    public List<QCStatement> getProperties() {
        return this.properties;
    }

    public boolean equals(CertificateEntry e) {
        return this.entry.equals(e.entry);
    }
}