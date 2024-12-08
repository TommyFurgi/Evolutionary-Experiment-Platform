package CLI.command;

import picocli.CommandLine;

@CommandLine.Command(name = "exit", description = "Exit the application")
public class ExitCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Goodbye! Have a nice day!");
        System.exit(0);
    }
}
