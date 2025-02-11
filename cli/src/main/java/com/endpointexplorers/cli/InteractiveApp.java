package com.endpointexplorers.cli;

import com.endpointexplorers.cli.command.*;
import com.endpointexplorers.cli.experiment.ScheduledExperimentFetcher;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.Scanner;

@Command(name = "", description = "Interactive CLI application", subcommands = {CommandLine.HelpCommand.class, ExitCommand.class})
public class InteractiveApp implements Runnable {
    public static void main(String[] args) {
        displayAsciiArtLogo();
        new InteractiveApp().run();
    }


    @Override
    public void run() {
        Injector injector = Guice.createInjector(new AppModule());
        FilesSaver filesSaver = injector.getInstance(FilesSaver.class);

        Scanner scanner = new Scanner(System.in);
        CommandLine cmd = new CommandLine(this);

        cmd.addSubcommand("run", new RunExperimentCommand());
        cmd.addSubcommand("get", new GetExperimentCommand());
        cmd.addSubcommand("list", new GetExperimentsListCommand());
        cmd.addSubcommand("getStats", new GetStatsCommand(filesSaver));
        cmd.addSubcommand("runManyDiff", new RunManyDifferentExperimentCommand());
        cmd.addSubcommand("setGroup", new SetExperimentsGroupCommand());
        cmd.addSubcommand("delete", new DeleteExperimentCommand());

        ScheduledExperimentFetcher puller = new ScheduledExperimentFetcher();
        puller.startRequesting();

        System.out.println("Welcome to the interactive CLI application. Type 'help' to see available commands.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();

            System.out.print("\033[K");
            System.out.print("> ");

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

    private static void displayAsciiArtLogo() {
        String blue = "\033[32m";
        String reset = "\033[0m";
        System.out.println(
                blue + """ 
                         ______             _                _         _     ______               _                             \s
                        |  ____|           | |              (_)       | |   |  ____|             | |                            \s
                        | |__    _ __    __| | _ __    ___   _  _ __  | |_  | |__   __  __ _ __  | |  ___   _ __  ___  _ __  ___\s
                        |  __|  | '_ \\  / _` || '_ \\  / _ \\ | || '_ \\ | __| |  __|  \\ \\/ /| '_ \\ | | / _ \\ | '__|/ _ \\| '__|/ __|
                        | |____ | | | || (_| || |_) || (_) || || | | || |_  | |____  >  < | |_) || || (_) || |  |  __/| |   \\__ \\
                        |______||_| |_| \\__,_|| .__/  \\___/ |_||_| |_| \\__| |______|/_/\\_\\| .__/ |_| \\___/ |_|   \\___||_|   |___/
                                              | |                                         | |                                   \s
                                              |_|                                         |_|                                   \s                                                                                          
                        """ + reset
        );
    }
}
