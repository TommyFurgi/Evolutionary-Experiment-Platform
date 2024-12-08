package CLI.command;

import picocli.CommandLine;

@CommandLine.Command(name = "add", description = "Adds two numbers together")
public class AddCommand implements Runnable {

    @CommandLine.Parameters(index = "0", paramLabel = "number1", description = "First number")
    private int number1;

    @CommandLine.Parameters(index = "1", paramLabel = "number2", description = "Second number")
    private int number2;

    @Override
    public void run() {
        System.out.println("Score: " + number1 + number2);
    }
}
