import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SecventialWordCounter {

    public static void main(String[] args) {
        String filePath = "fisier.txt";

        // dicționarul in care vom stoca frecvența cuvintelor
        Map<String, Integer> wordFrequencies = new HashMap<>();

        System.out.println("Începem procesarea secvențială a fișierului...");

        // masura timpului de execuție
        long startTime = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            // citim fișierul linie cu linie
            while ((line = reader.readLine()) != null) {
                // impărțim linia în cuvinte folosind Regex:
                // \\W+ înseamnă orice caracter care nu este litera sau cifra (spatii, puncte, virgule)
                String[] words = line.split("\\W+");

                for (String word : words) {
                    // ignoram stringurile goale rezultate din split
                    if (!word.isEmpty()) {
                        // transf totul in litere mici
                        word = word.toLowerCase();

                        // adaugam in dictionar sau incrementam valoarea daca exista deja
                        wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Eroare la citirea fișierului: " + e.getMessage());
            return;
        }

        // oprim cronometrul
        long endTime = System.currentTimeMillis();
        long durata = endTime - startTime;

        // afisare rezultate
        System.out.println("Procesare finalizată!");
        System.out.println("Timp de execuție secvențial: " + durata + " milisecunde.");
        System.out.println("Număr de cuvinte unice găsite: " + wordFrequencies.size());

        // afișarea primelor 10 cuvinte
        System.out.println("\nTop 10 cele mai frecvente cuvinte:");
        wordFrequencies.entrySet().stream()
                // sortam descrescator dupa frecv
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .forEach(entry -> System.out.println(entry.getKey() + " : " + entry.getValue()));
    }
}