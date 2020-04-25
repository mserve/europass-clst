package de.mserve.europass.clst;


// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import de.mserve.europass.tasks.ListCertificates;
import de.mserve.europass.tasks.SignXML;
import de.mserve.europass.tasks.ValidateXML;
import de.mserve.europass.tasks.VerifyXML;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Hello world!
 *
 */
@Command(name = "europass-clst", mixinStandardHelpOptions = true, version = "europass-clst 0.1",
        description = "europass-clst allows to sign Europass Digital Credential XML files via command line.",
        commandListHeading = "%nCommands:%n%nThe most commonly used commands are:%n",
        footer = "%nSee 'europass-clst help <command>' to read about a specific subcommand or concept.",
        subcommands = {
                ListCertificates.class,
                SignXML.class,
                ValidateXML.class,
                VerifyXML.class,
                CommandLine.HelpCommand.class
        })
public class App implements Runnable {

    // private static final Logger LOG = LoggerFactory.getLogger(App.class);

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        // if the command was invoked without subcommand, show the usage help
        spec.commandLine().usage(System.err);
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new App()).execute(args));
    }
}
