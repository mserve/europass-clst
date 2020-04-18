package de.mserve.europass.clst;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mserve.europass.model.CertificateLoaderOptions;
import de.mserve.europass.tasks.CertificateLoader;
import eu.europa.esig.dss.enumerations.SignatureTokenType;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

/**
 * Hello world!
 *
 */
public class App {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        // Load certicicates
        CertificateLoaderOptions clo = new CertificateLoaderOptions();
        final String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("win") >= 0) {
            clo.TOKEN_TYPE = SignatureTokenType.MSCAPI;
        } else {
            clo.TOKEN_TYPE = SignatureTokenType.PKCS12;
            clo.FILE_NAME = "/workspace/europass-clst/europass-clst/src/test/java/de/mserve/europass/clst/demo.p12";
            clo.PKCS12_PASSWORD = "";
        }

        final CertificateLoader cl = new CertificateLoader(clo);

        // Enumerate certificates
        final Map<String, DSSPrivateKeyEntry> certs = cl.enumerateKeys();
        Set<String> keys = certs.keySet();
        int count = 0;
        for (String key: keys) {
            System.out.format("[%d] %s%n", count, key);
            count ++;
        }
    }
}
