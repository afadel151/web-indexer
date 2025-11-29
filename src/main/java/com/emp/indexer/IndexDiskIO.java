package  com.emp.indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexDiskIO {

    //save_index_to_disk and read_index_from_disk
    public void save_index_to_disk(Map<String, Integer> lexicon,
                          Map<Integer, List<int[]>> postings,
                          Map<Integer, DocumentMeta> documents,
                          String outputPath) throws IOException {

        Files.createDirectories(Paths.get(outputPath));

        // save lexicon
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath, "index_data/lexicon.txt"))) {
            for (Map.Entry<String, Integer> e : lexicon.entrySet()) {
                writer.write(e.getKey() + "\t" + e.getValue());
                writer.newLine();
            }
        }

        // save documents
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath, "index_data/docs.txt"))) {
            for (Map.Entry<Integer, DocumentMeta> e : documents.entrySet()) {
                writer.write(e.getKey() + "\t" +
                             e.getValue().getPath() + "\t" +
                             e.getValue().getLength());
                writer.newLine();
            }
        }

        // save postings
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath, "index_data/postings.txt"))) {
            for (Map.Entry<Integer, List<int[]>> e : postings.entrySet()) {
                int termId = e.getKey();
                List<int[]> plist = e.getValue();

                StringBuilder line = new StringBuilder();
                line.append(termId).append("\t");

                for (int i = 0; i < plist.size(); i++) {
                    int[] pair = plist.get(i);
                    line.append(pair[0]).append(":").append(pair[1]);
                    if (i < plist.size() - 1) line.append(",");
                }

                writer.write(line.toString());
                writer.newLine();
            }
        }
        System.out.println("Index saved to: " + outputPath);
    }


    public IndexData read_index_from_disk(String indexPath) throws IOException {
        IndexData data = new IndexData();
        data.lexicon = new HashMap<>();
        data.postings = new HashMap<>();
        data.documents = new HashMap<>();

        // read lexicon
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(indexPath, "index_data/lexicon.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length == 2) {
                    data.lexicon.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
        }

        // read documents
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(indexPath, "index_data/docs.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length == 3) {
                    int docId = Integer.parseInt(parts[0]);
                    String path = parts[1];
                    int length = Integer.parseInt(parts[2]);
                    data.documents.put(docId, new DocumentMeta(path, length));
                }
            }
        }

        // read postings
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(indexPath, "index_data/postings.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length == 2) {
                    int termId = Integer.parseInt(parts[0]);
                    List<int[]> plist = new ArrayList<>();

                    String[] pairs = parts[1].split(",");
                    for (String p : pairs) {
                        String[] values = p.split(":");
                        if (values.length == 2) {
                            int docId = Integer.parseInt(values[0]);
                            int tf = Integer.parseInt(values[1]);
                            plist.add(new int[]{docId, tf});
                        }
                    }
                    data.postings.put(termId, plist);
                }
            }
        }

        System.out.println("Index loaded from: " + indexPath);
        System.out.println("Terms: " + data.lexicon.size() +
                           ", Documents: " + data.documents.size() +
                           ", Postings: " + data.postings.size());

        return data;
    }
}
