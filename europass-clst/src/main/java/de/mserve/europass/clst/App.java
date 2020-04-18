package de.mserve.europass.clst;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mserve.europass.model.CertificateEntry;
import de.mserve.europass.tasks.CertificateLoader;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

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
                    "/workspace/europass-clst/europass-clst/src/test/java/de/mserve/europass/clst/demo.p12", "");
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
