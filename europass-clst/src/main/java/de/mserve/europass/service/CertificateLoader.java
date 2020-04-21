package de.mserve.europass.service;

import java.io.File;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mserve.europass.model.CertificateEntry;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.MSCAPISignatureToken;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.enumerations.SignatureTokenType;

public class CertificateLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateLoader.class);

    private List<DSSPrivateKeyEntry> keys;
    private SignatureTokenType tokenType;
    private File pkcs12File;
    private char[] pkcs12Password;

    private CertificateLoader() {
    }

    public static CertificateLoader MSCAPI() {
        CertificateLoader cl = new CertificateLoader();
        cl.tokenType = SignatureTokenType.MSCAPI;
        cl.loadCertificates();
        return cl;
    }

    public static CertificateLoader PKCS12(File file) {
        return CertificateLoader.PKCS12(file, null);
    }

    public static CertificateLoader PKCS12(File file, char[] password) {
        CertificateLoader cl = new CertificateLoader();
        cl.tokenType = SignatureTokenType.PKCS12;
        cl.pkcs12File = file;
        if (password == null || password.length == 0)
            cl.pkcs12Password = "".toCharArray();
        else
            cl.pkcs12Password = password;
        cl.loadCertificates();
        return cl;
    }

    private void loadCertificates() {
        switch (this.tokenType) {
            case MSCAPI:
                try (MSCAPISignatureToken token = new MSCAPISignatureToken()) {
                    this.keys = token.getKeys();
                }
                break;
            case PKCS12:
                try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(this.pkcs12File,
                        new PasswordProtection(this.pkcs12Password))) {
                    this.keys = token.getKeys();
                } catch (final Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                break;
            default:
                LOG.error("CertStoreNotSupported", "Certificate Store not supported");
        }
    }

    /* loads all keys within the chosen certification and enumerates them */
    public List<CertificateEntry> enumerateKeys() {
        final List<CertificateEntry> list = new ArrayList<CertificateEntry>();
        try {
            for (final DSSPrivateKeyEntry dssPrivateKeyEntry : keys) {
                CertificateEntry c = new CertificateEntry(dssPrivateKeyEntry);
                list.add(c);
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return list;
    }
}