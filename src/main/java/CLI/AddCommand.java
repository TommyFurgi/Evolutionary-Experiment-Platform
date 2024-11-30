package CLI;

import picocli.CommandLine;

@CommandLine.Command(name = "add", description = "Dodaje dwie liczby")
class AddCommand implements Runnable {

    @CommandLine.Parameters(index = "0", paramLabel = "liczba1", description = "Pierwsza liczba")
    private int liczba1;

    @CommandLine.Parameters(index = "1", paramLabel = "liczba2", description = "Druga liczba")
    private int liczba2;

    @Override
    public void run() {
        int wynik = liczba1 + liczba2;
        System.out.println("Wynik: " + wynik);
    }
}
