package de.mserve.europass.tasks;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mserve.europass.model.CertificateEntry;

import picocli.CommandLine.Command;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(name = "list", header = "List all available certificates.")
public class ListCertificates extends TaskWithCertificates {

    private static final Logger LOG = LoggerFactory.getLogger(ListCertificates.class);

    @Spec
    CommandSpec spec;

    @Override
    public void run() {

        this.loadKeyStore();

        LOG.info("Enumerating certificates of key store '{}'", this.getCertificateLoader().toString());

        // Enumerate certificates
        final List<CertificateEntry> certs = this.getCertificateLoader().enumerateKeys();
        int count = 0;
        for (CertificateEntry c : certs) {
            System.out.format("[%d] %s%n", count, c.getLabel());
            count++;
        }

    }
}