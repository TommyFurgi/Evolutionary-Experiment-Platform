package CLI;

import picocli.CommandLine;

@CommandLine.Command(name = "echo", description = "Wyświetla podany tekst")
class EchoCommand implements Runnable {

    @CommandLine.Parameters(arity = "1..*", paramLabel = "<tekst>", description = "Tekst do wyświetlenia")
    private String[] tekst;

    @Override
    public void run() {
        System.out.println(String.join(" ", tekst));
    }
}