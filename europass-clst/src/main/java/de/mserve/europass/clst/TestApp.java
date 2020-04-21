package de.mserve.europass.clst;

import java.io.File;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mserve.europass.model.CertificateEntry;
import de.mserve.europass.service.CertificateLoader;
import eu.europa.esig.dss.enumerations.QCStatement;

/**
 * Hello world!
 *
 */
public class TestApp {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        // Show OIDs
        System.out.println("E-Sign: " + QCStatement.QCT_ESIGN.getOid());
        System.out.println("E-Seal: " + QCStatement.QCT_ESEAL.getOid());
        System.out.println("E-Web: " + QCStatement.QCT_WEB.getOid());
        
        // Load certicicates
        final String OS = System.getProperty("os.name").toLowerCase();
        CertificateLoader cl;
        if (OS.indexOf("win") >= 0) {
            cl = CertificateLoader.MSCAPI();
        } else {
            cl = CertificateLoader.PKCS12(
                    new File("/workspace/europass-clst/europass-clst/src/test/java/de/mserve/europass/clst/e-seal.p12"),
                     "seal".toCharArray());
        }

        // Enumerate certificates
        final List<CertificateEntry> certs = cl.enumerateKeys();
        int count = 0;
        for (CertificateEntry c : certs) {
            System.out.format("[%d] %s%n", count, c.getLabel());
            count++;
        }
    }
}
