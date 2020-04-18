package de.mserve.europass.model;

import java.util.ArrayList;
import java.util.List;

import eu.europa.esig.dss.enumerations.QCStatement;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSASN1Utils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

public class CertificateEntry {
    private DSSPrivateKeyEntry entry;
    private ArrayList<QCStatement> properties = new ArrayList<>();
    private String label;

    public CertificateEntry(DSSPrivateKeyEntry _entry) {
        if (_entry != null) {
            this._setEntry(_entry);
        }
    }

    private void _setEntry(DSSPrivateKeyEntry dssPrivateKeyEntry) {
        this.entry = dssPrivateKeyEntry;
        final CertificateToken certToken = dssPrivateKeyEntry.getCertificate();
        this.label = DSSASN1Utils.getHumanReadableName(certToken) + " (" + certToken.getSerialNumber() + ")";
        final List<String> qcStatements = DSSASN1Utils.getQCStatementsIdList(certToken);
        if (qcStatements.contains(QCStatement.QC_COMPLIANCE.getOid())) {
            this.label = this.label + " [Qualified]";
            this.properties.add(QCStatement.QC_COMPLIANCE);
        }
        if (qcStatements.contains(QCStatement.QCT_ESIGN.getOid())) {
            this.label = this.label + " [E-Sign]";
            this.properties.add(QCStatement.QCT_ESIGN);
        }
    }

    public DSSPrivateKeyEntry getEntry() {
        return entry;
    }
    public String getLabel() {
        return label;
    }
    public boolean hasProperty(QCStatement qcs) {
        return this.properties.contains(QCStatement.QCT_ESIGN);
    }

    public List<QCStatement> getProperties() {
        return this.properties;
    }
}