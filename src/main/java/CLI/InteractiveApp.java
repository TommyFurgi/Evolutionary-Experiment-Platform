package CLI;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Scanner;

@Command(name = "", description = "Interaktywna aplikacja CLI", subcommands = {CommandLine.HelpCommand.class, ExitCommand.class})
public class InteractiveApp implements Runnable {
    public static void main(String[] args) {
        new InteractiveApp().run();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        CommandLine cmd = new CommandLine(this);

        // Dodaj swoje komendy jako subkomendy
        cmd.addSubcommand("echo", new EchoCommand());
        cmd.addSubcommand("add", new AddCommand());
        cmd.addSubcommand("run", new RunExperimentCommand());

        System.out.println("Witaj w interaktywnej aplikacji CLI. Wpisz 'help' aby zobaczyć dostępne komendy.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();

            // Sprawdź, czy użytkownik wpisał 'exit'
            if (line.trim().equalsIgnoreCase("exit")) {
                System.out.println("Do widzenia!");
                break;
            }

            // Podziel linię na argumenty
            String[] arguments = line.split(" ");

            try {
                cmd.execute(arguments);
            } catch (Exception e) {
                System.err.println("Błąd: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
