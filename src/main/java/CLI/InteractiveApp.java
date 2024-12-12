package CLI;

import CLI.command.ExitCommand;
import CLI.command.GetExperimentCommand;
import CLI.command.GetExperimentsListCommand;
import CLI.command.RunExperimentCommand;
import CLI.experiment.ScheduledExperimentFetcher;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Scanner;

@Command(name = "", description = "Interactive CLI application", subcommands = {CommandLine.HelpCommand.class, ExitCommand.class})
public class InteractiveApp implements Runnable {
    public static void main(String[] args) {
        new InteractiveApp().run();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        CommandLine cmd = new CommandLine(this);

        cmd.addSubcommand("run", new RunExperimentCommand());
        cmd.addSubcommand("get", new GetExperimentCommand());
        cmd.addSubcommand("list", new GetExperimentsListCommand());

        ScheduledExperimentFetcher puller = new ScheduledExperimentFetcher();
        puller.startRequesting();

        System.out.println("Welcome to the interactive CLI application. Type 'help' to see available commands.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();

            if (line.trim().equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (line.isEmpty()) {
                cmd.usage(System.out);
                continue;
            }
            String[] arguments = line.split(" ");

            try {
                cmd.execute(arguments);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
