package CLI;

import picocli.CommandLine;

@CommandLine.Command(name = "echo", description = "Prints text to the console")
class EchoCommand implements Runnable {

    @CommandLine.Parameters(arity = "1..*", paramLabel = "<text>", description = "Text to display")
    private String[] text;

    @Override
    public void run() {
        System.out.println(String.join(" ", text));
    }
}