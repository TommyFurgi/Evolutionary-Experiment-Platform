package CLI;

import CLI.command.*;
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

        cmd.addSubcommand("echo", new EchoCommand());
        cmd.addSubcommand("add", new AddCommand());
        cmd.addSubcommand("run", new RunExperimentCommand());
        cmd.addSubcommand("get", new GetExperimentCommand());

        System.out.println("Welcome to the interactive CLI application. Type 'help' to see available commands.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();

            if (line.trim().equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
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
