import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class ParalelWordCounter {

    // dicționarul comun (partajat) în care toate firele își vor pune rezultatele
    private static final Map<String, Integer> sharedWordFrequencies = new HashMap<>();

    public static void main(String[] args) {
        String filePath = "fisier.txt";
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("Fișierul nu există. Rulează generatorul mai întâi!");
            return;
        }

        // setăm numărul de fire de execuție (thread-uri).
        int numThreads = Runtime.getRuntime().availableProcessors();
        System.out.println("Începem procesarea paralelă folosind " + numThreads + " fire de execuție...");

        long startTime = System.currentTimeMillis();

        // calculăm dimensiunea fiecărui segment
        long fileSize = file.length();
        long segmentSize = fileSize / numThreads;

        Thread[] threads = new Thread[numThreads];

        // creăm și pornim firele de execuție
        for (int i = 0; i < numThreads; i++) {
            long startByte = i * segmentSize;
            // ultimul thread ia tot ce a mai rămas până la capătul fișierului
            long endByte = (i == numThreads - 1) ? fileSize : startByte + segmentSize;

            threads[i] = new WorkerThread(file, startByte, endByte);
            threads[i].start();
        }

        // main thread-ul trebuie să aștepte ca toate WorkerThread-urile să termine
        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join(); // join() blochează main-ul până când thread-ul 'i' se termină
            } catch (InterruptedException e) {
                System.err.println("Thread-ul a fost întrerupt: " + e.getMessage());
            }
        }

        long endTime = System.currentTimeMillis();
        long durata = endTime - startTime;

        System.out.println("Procesare paralelă finalizată!");
        System.out.println("Timp de execuție paralel: " + durata + " milisecunde.");
        System.out.println("Număr de cuvinte unice găsite: " + sharedWordFrequencies.size());

        System.out.println("\nTop 10 cele mai frecvente cuvinte (Paralel):");
        sharedWordFrequencies.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .forEach(entry -> System.out.println(entry.getKey() + " : " + entry.getValue()));
    }

    // --- clasa internă care definește ce face fiecare fir de execuție ---
    static class WorkerThread extends Thread {
        private final File file;
        private final long startByte;
        private final long endByte;

        public WorkerThread(File file, long startByte, long endByte) {
            this.file = file;
            this.startByte = startByte;
            this.endByte = endByte;
        }

        @Override
        public void run() {
            // dicționar local pentru a procesa rapid segmentul, fără să blocăm alte thread-uri constant
            Map<String, Integer> localFrequencies = new HashMap<>();

            // folosim RandomAccessFile pentru a sări direct la byte-ul specificat
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(startByte);

                // dacă nu suntem la începutul fișierului, este posibil să fim în mijlocul unui cuvânt.
                // asa că citim prima linie și o ignorăm (va fi procesată de thread-ul anterior).
                if (startByte != 0) {
                    raf.readLine();
                }

                String line;
                // citim cât timp nu am depășit limita segmentului nostru
                while (raf.getFilePointer() < endByte) {
                    line = raf.readLine();
                    if (line == null) break;

                    String[] words = line.split("\\W+");
                    for (String word : words) {
                        if (!word.isEmpty()) {
                            word = word.toLowerCase();
                            localFrequencies.put(word, localFrequencies.getOrDefault(word, 0) + 1);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Eroare la procesarea segmentului: " + e.getMessage());
            }

            // aici intervine cerința esențială: combinarea în dicționarul comun folosind 'synchronized'
            // blocăm dicționarul partajat doar cât timp transferăm datele locale în el
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