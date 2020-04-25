package de.mserve.europass.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mserve.europass.model.CertificateEntry;
import de.mserve.europass.service.XmlSigner;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(name = "sign", header = "Signs a single XML file or all XML files in a directory.")
public class SignXML extends TaskWithCertificates {

    private static final Logger LOG = LoggerFactory.getLogger(SignXML.class);

    @Spec
    CommandSpec spec;

    @ArgGroup(exclusive = true, multiplicity = "1")
    ExclusiveIn exclusiveIn;

    static class ExclusiveIn {
        @Option(names = { "-xf", "--xml-file" }, description = "XML file to sign", paramLabel = "FILE")
        private File xmlFile;

        @Option(names = { "-xd",
                "--xml-dir" }, description = "Directory containing XML files to sign", paramLabel = "FOLDER")
        private Path xmlFolder;
    }

    @Option(names = { "-xo",
            "--output-dir" }, description = "Directory containing signed data", paramLabel = "OUTFOLDER")
    private Path outFolder;

    @Option(names = { "-ki", "--key-index" }, description = "Index of key to use", paramLabel = "INDEX")
    private int keyIndex;

    @Override
    public void run() {
        // Load key store
        this.loadKeyStore();

        // Get Certificate
        CertificateEntry cert = this.getCertificateLoader().enumerateKeys().get(this.keyIndex);

        // Load XmlSigner
        XmlSigner xs = XmlSigner.build(cert);

        ArrayList<File> filesToSign = new ArrayList<File>();
        if (this.exclusiveIn.xmlFile != null) {
            filesToSign.add(this.exclusiveIn.xmlFile);
        } else if (this.exclusiveIn.xmlFolder != null) {
            LOG.info("Processing XML files in path '{}'", this.exclusiveIn.xmlFolder.toAbsolutePath().toString());
            try (Stream<Path> walk = Files.walk(this.exclusiveIn.xmlFolder)) {
                filesToSign.addAll(walk.filter(f -> f.toString().endsWith(".xml")).map(f -> f.toFile())
                .collect(Collectors.toList()));
                LOG.info("Found {} XML files in path '{}'", filesToSign.size(), this.exclusiveIn.xmlFolder.toAbsolutePath().toString());
            } catch (IOException e) {
                LOG.error("Could not list files in folder '{}'", this.exclusiveIn.xmlFolder.toString());
                return;
            }
        }

        for (final File f : filesToSign) {

            // Sign file
            if (this.outFolder != null) {
                String outPath = this.outFolder.toAbsolutePath().toString();
                LOG.info("Using output path '{}'", outPath);
                xs.sign(f, outPath);
            } else {
                xs.sign(f);

            }
        }

    }
}