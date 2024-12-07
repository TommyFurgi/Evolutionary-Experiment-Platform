package CLI;

import picocli.CommandLine;

@CommandLine.Command(name = "help", description = "Prints help information")
class HelpCommand implements Runnable {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        CommandLine cmd = spec.commandLine();
        cmd.usage(System.out);
    }
}
