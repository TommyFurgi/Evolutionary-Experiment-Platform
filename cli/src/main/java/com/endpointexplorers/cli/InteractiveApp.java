package com.endpointexplorers.cli;

import com.endpointexplorers.cli.command.*;
import com.endpointexplorers.cli.component.ScheduledExperimentFetcher;
import com.endpointexplorers.cli.config.ConfigModule;
import com.endpointexplorers.cli.config.PathModule;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.Scanner;

@Command(name = "", description = "Interactive CLI application", subcommands = {CommandLine.HelpCommand.class})
public class InteractiveApp implements Runnable {
    public static void main(String[] args) {
        displayAsciiArtLogo();
        new InteractiveApp().run();
    }


    @Override
    public void run() {
        Injector injector = Guice.createInjector(new PathModule(), new ConfigModule());

        Scanner scanner = new Scanner(System.in);
        CommandLine cmd = new CommandLine(this);

        cmd.addSubcommand("run", injector.getInstance(RunExperimentsCommand.class));
        cmd.addSubcommand("get", injector.getInstance(GetExperimentCommand.class));
        cmd.addSubcommand("list", injector.getInstance(GetExperimentsListCommand.class));
        cmd.addSubcommand("getStats", injector.getInstance(GetStatsCommand.class));
        cmd.addSubcommand("runMulti", injector.getInstance(RunMultipleExperimentsCommand.class));
        cmd.addSubcommand("setGroup", injector.getInstance(SetExperimentsGroupCommand.class));
        cmd.addSubcommand("delete", injector.getInstance(DeleteExperimentCommand.class));
        cmd.addSubcommand("exit", new ExitCommand());

        ScheduledExperimentFetcher puller = injector.getInstance(ScheduledExperimentFetcher.class);
        puller.startRequesting();

        System.out.println("Welcome to the interactive CLI application. Type 'help' to see available commands.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();

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
    }

    private static void displayAsciiArtLogo() {
        String blue = "\033[32m";
        String reset = "\033[0m";
        System.out.println(
                blue + """ 
                         _____           _       _   _                              _____                     _                      _  ______ _       _    __                    \s
                        |  ___|         | |     | | (_)                            |  ___|                   (_)                    | | | ___ | |     | |  / _|                   \s
                        | |____   _____ | |_   _| |_ _  ___  _ __   __ _ _ __ _   _| |____  ___ __   ___ _ __ _ _ __ ___   ___ _ __ | |_| |_/ | | __ _| |_| |_ ___  _ __ _ __ ___ \s
                        |  __\\ \\ / / _ \\| | | | | __| |/ _ \\| '_ \\ / _` | '__| | | |  __\\ \\/ | '_ \\ / _ | '__| | '_ ` _ \\ / _ | '_ \\| __|  __/| |/ _` | __|  _/ _ \\| '__| '_ ` _ \\\s
                        | |___\\ V | (_) | | |_| | |_| | (_) | | | | (_| | |  | |_| | |___>  <| |_) |  __| |  | | | | | | |  __| | | | |_| |   | | (_| | |_| || (_) | |  | | | | | |
                        \\____/ \\_/ \\___/|_|\\__,_|\\__|_|\\___/|_| |_|\\__,_|_|   \\__, \\____/_/\\_| .__/ \\___|_|  |_|_| |_| |_|\\___|_| |_|\\__\\_|   |_|\\__,_|\\__|_| \\___/|_|  |_| |_| |_|
                                                                               __/ |         | |                                                                                  \s
                                                                              |___/          |_|                                                                                  \s
                        """ + reset
        );
    }
}
