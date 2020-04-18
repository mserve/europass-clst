package de.mserve.europass.clst;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mserve.europass.model.CertificateEntry;
import de.mserve.europass.tasks.CertificateLoader;

/**
 * Hello world!
 *
 */
public class App {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        // Load certicicates
        final String OS = System.getProperty("os.name").toLowerCase();
        CertificateLoader cl;
        if (OS.indexOf("win") >= 0) {
            cl = CertificateLoader.MSCAPI();
        } else {
            cl = CertificateLoader.PKCS12(
                    "/workspace/europass-clst/europass-clst/src/test/java/de/mserve/europass/clst/e-seal.p12",
                     "seal");
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
