package de.mserve.europass.tasks;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mserve.europass.model.CertificateEntry;
import de.mserve.europass.service.CertificateLoader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

enum KeyStore {
    mscapi, pkcs12
};

@Command(name = "list", header = "List all available certificates.")
public class ListCertificates implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateLoader.class);

    @Spec
    CommandSpec spec;

    @Option(names = { "-k", "--key-store" }, required = true, description = "key store to use")
    private KeyStore keyStore = null;

    @Option(names = { "-f", "--pkcs12-file" }, description = "PKCS#12 key store file", paramLabel = "FILE")
    File pkcs12File;

    @Option(names = { "-p",
            "--pkcs12-password" }, arity = "0..1", description = "Passphrase for PKCS#12 file", interactive = true)
    char[] pkcs12Password;

    @Override
    public void run() {

        CertificateLoader cl;

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
        // Enumerate certificates
        final List<CertificateEntry> certs = cl.enumerateKeys();
        int count = 0;
        for (CertificateEntry c : certs) {
            System.out.format("[%d] %s%n", count, c.getLabel());
            count++;
        }

    }
}