package de.mserve.europass.clst;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mserve.europass.model.CertificateEntry;
import de.mserve.europass.service.CertificateLoader;
import de.mserve.europass.service.XmlSigner;
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
            File f = new File(
                    "/workspace/europass-clst/europass-clst/src/test/resources/de/mserve/europass/clst/e-seal.p12");
            cl = CertificateLoader.PKCS12(f, "seal".toCharArray());
        }

        // Enumerate certificates
        final List<CertificateEntry> certs = cl.enumerateKeys();
        int count = 0;
        for (CertificateEntry c : certs) {
            System.out.format("[%d] %s%n", count, c.getLabel());
            count++;
        }

        // Sign document
        XmlSigner xs = XmlSigner.build(certs.get(0));
        xs.sign(new File(
                "/workspace/europass-clst/europass-clst/src/test/resources/de/mserve/europass/clst/hello.xml"));
    }
}
