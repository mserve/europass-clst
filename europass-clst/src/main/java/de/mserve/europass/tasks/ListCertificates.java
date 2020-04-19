package de.mserve.europass.tasks;

import java.io.File;
import java.util.List;

import de.mserve.europass.model.CertificateEntry;
import de.mserve.europass.service.CertificateLoader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

enum KeyStore {
    mscapi, pkcs12
};

@Command(name = "list", header = "List all available certificates.")
public class ListCertificates implements Runnable {

    @Option(names = { "-k", "--key-store" }, description = "key store to use")
    private KeyStore keyStore = null;

    @Option(names = { "-f", "--pkcs12-file" },   description = "PKCS#12 key store file",  paramLabel = "FILE")
    File pkcs12File;

    @Option(names = {"-p", "--pkcs12-password"}, description = "Passphrase for PKCS#12 file", interactive = true)
    char[] pkcs12Password;

    @Override
    public void run() {

        CertificateLoader cl;

        switch (this.keyStore) {
            case mscapi:
                cl = CertificateLoader.MSCAPI();
                break;
            case pkcs12:
                cl = CertificateLoader.PKCS12(this.pkcs12File, this.pkcs12Password);
                break;
            default:
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