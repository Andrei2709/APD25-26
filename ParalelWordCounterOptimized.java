import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParalelWordCounterOptimized {

    // dicționarul comun cerut
    private static final Map<String, Integer> sharedWordFrequencies = new HashMap<>();

    public static void main(String[] args) {
        String filePath = "fisier.txt";
        List<String> allLines = new ArrayList<>();

        System.out.println("Citim fișierul în memorie...");
        // citirea secvențială rapidă de pe disc
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Eroare la citire: " + e.getMessage());
            return;
        }

        int totalLines = allLines.size();
        System.out.println("S-au citit " + totalLines + " rânduri. Începem procesarea paralelă...");

        long startTime = System.currentTimeMillis();

        int numThreads = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[numThreads];

        // calculăm câte rânduri primește fiecare segment
        int linesPerSegment = totalLines / numThreads;

        // creăm și pornim firele de execuție
        for (int i = 0; i < numThreads; i++) {
            int startLine = i * linesPerSegment;
            // ultimul thread ia toate rândurile rămase
            int endLine = (i == numThreads - 1) ? totalLines : startLine + linesPerSegment;

            // creăm un sub-segment (o sub-listă) pentru acest thread
            List<String> segment = allLines.subList(startLine, endLine);

            threads[i] = new WorkerThread(segment);
            threads[i].start();
        }

        // asteptăm ca toate firele să termine procesarea
        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                System.err.println("Thread întrerupt: " + e.getMessage());
            }
        }

        long endTime = System.currentTimeMillis();
        long durata = endTime - startTime;

        System.out.println("Procesare paralelă finalizată!");
        // aici măsurăm doar partea de paralelizare CPU
        System.out.println("Timp de execuție CPU paralel: " + durata + " milisecunde.");
        System.out.println("Număr de cuvinte unice găsite: " + sharedWordFrequencies.size());

        System.out.println("\nTop 10 cele mai frecvente cuvinte (Paralel Optimizat):");
        sharedWordFrequencies.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .forEach(entry -> System.out.println(entry.getKey() + " : " + entry.getValue()));
    }

    // --- WorkerThread care lucrează strict în memorie ---
    static class WorkerThread extends Thread {
        private final List<String> linesSegment;

        public WorkerThread(List<String> linesSegment) {
            this.linesSegment = linesSegment;
        }

        @Override
        public void run() {
            // dicționar local pentru eficiență
            Map<String, Integer> localFrequencies = new HashMap<>();

            // procesăm segmentul din memorie (fără să atingem hard disk-ul)
            for (String line : linesSegment) {
                String[] words = line.split("\\W+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        word = word.toLowerCase();
                        localFrequencies.put(word, localFrequencies.getOrDefault(word, 0) + 1);
                    }
                }
            }

            // punem rezultatele în dicționarul comun folosind block-ul synchronized cerut
            synchronized (sharedWordFrequencies) {
                for (Map.Entry<String, Integer> entry : localFrequencies.entrySet()) {
                    String word = entry.getKey();
                    int count = entry.getValue();
                    sharedWordFrequencies.put(word, sharedWordFrequencies.getOrDefault(word, 0) + count);
                }
            }
        }
    }
}