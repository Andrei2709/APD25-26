import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class FileGenerator {

    public static void main(String[] args) {
        String filePath = "fisier.txt";

        // un dictionar de cuvinte de baza pentru a genera textul
        String[] words = {
                "proiect", "java", "procesare", "paralela", "thread", "secvential", "algoritm",
                "performanta", "sincronizare", "viteza", "date", "memorie", "cpu", "nuclee",
                "test", "rezultat", "frecventa", "cuvinte", "sistem", "fisiere", "programare",
                "cod", "dezvoltare", "software", "hardware", "retea", "baza", "server", "client",
                "interfata", "clasa", "obiect", "metoda", "variabila", "compilator", "eroare",
                "concurenta", "multithreading", "blocaj", "deadlock", "atomic", "volatile",
                "monitor", "semafor", "mutex", "task", "executor", "pool", "analiza", "optimizare",
                "structura", "graf", "arbore", "lista", "map", "set", "iteratie", "bucla",
                "conditie", "exceptie", "pachet", "import", "public", "privat", "protejat",
                "static", "final", "abstract", "implementare", "parametru", "argument", "return",
                "si", "de", "la", "cu", "un", "o", "sa", "pe", "pentru", "ca", "este", "sunt",
                "din", "care", "in", "mai", "sau", "dar", "acest", "aceasta", "tot", "toate",
                "prin", "peste", "sub", "fara", "intre", "despre", "foarte", "putin", "mult",
                "bine", "rau", "acum", "atunci", "aici", "acolo", "unde", "cand", "cum", "ce",
                "cine", "nimic", "ceva", "totul", "nimeni", "oricine", "oricum", "oricand"
        };

        Random random = new Random();
        // nr de linii generate
        int linesToGenerate = 1_000_000;

        System.out.println("Începem generarea fișierului '" + filePath + "'...");
        long startTime = System.currentTimeMillis();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < linesToGenerate; i++) {
                int wordsPerLine = 10 + random.nextInt(11);
                StringBuilder line = new StringBuilder();

                for (int j = 0; j < wordsPerLine; j++) {
                    String randomWord = words[random.nextInt(words.length)];
                    line.append(randomWord).append(" ");
                }

                writer.write(line.toString().trim());
                writer.newLine();

                // afisam progresul la fiecare 200.000 de linii
                if ((i + 1) % 200_000 == 0) {
                    System.out.println("S-au generat " + (i + 1) + " de linii...");
                }
            }
        } catch (IOException e) {
            System.err.println("Eroare la scrierea fișierului: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Generare completă în " + (endTime - startTime) + " ms.");
    }
}