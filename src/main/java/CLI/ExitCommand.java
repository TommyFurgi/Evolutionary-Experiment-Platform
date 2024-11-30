package CLI;

import picocli.CommandLine;

@CommandLine.Command(name = "exit", description = "Kończy działanie aplikacji")
class ExitCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Do widzenia!");
        System.exit(0);
    }
}
