package de.mserve.europass.tasks;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mserve.europass.service.CertificateLoader;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

enum KeyStore {
    mscapi, pkcs12
};

public abstract class TaskWithCertificates implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ListCertificates.class);

    @Spec
    CommandSpec spec;

    @Option(names = { "-k", "--key-store" }, required = true, description = "key store to use")
    private KeyStore keyStore = null;


    @Option(names = { "-pf", "--pkcs12-file" }, description = "PKCS#12 key store file", paramLabel = "PKCS12FILE")
    File pkcs12File;

    @Option(names = { "-pp",
            "--pkcs12-password" }, arity = "0..1", description = "Passphrase for PKCS#12 file", interactive = true)
    char[] pkcs12Password;

    private CertificateLoader cl;

    protected CertificateLoader getCertificateLoader() {
        return cl;
    }

    protected void loadKeyStore() {

        switch (this.keyStore) {
            case mscapi:
                cl = CertificateLoader.MSCAPI();
                break;
            case pkcs12:
                if (this.pkcs12File == null) {
                    throw new CommandLine.ParameterException(spec.commandLine(), "Missing PKCS#12 file for key store");
                }
                if (!this.pkcs12File.exists()) {
                    throw new CommandLine.ParameterException(spec.commandLine(),
                            "PKCS#12 file for key store not found");
                }
                cl = CertificateLoader.PKCS12(this.pkcs12File, this.pkcs12Password);
                break;
            default:
                return;
        }

        // Handle errors
        if (cl == null) {
            switch (this.keyStore) {
                case mscapi:
                    LOG.error("Could not open key store. Hint: are you trying to use MSCAPI on a non-Windows system?");
                    break;
                case pkcs12:
                    LOG.error("Could not open key store. Hint: is your PKCS#2 password correct?");
                    break;
                default:
                    LOG.error("Could not open key store. Please check out setting!");
            }
            return;
        }

    }
}