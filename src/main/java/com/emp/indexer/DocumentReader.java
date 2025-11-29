package  com.emp.indexer;

// iterate over corpus folder

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class DocumentReader {

    public Map<Integer, DocumentMeta> loadDocuments(String corpusPath){
        Map<Integer, DocumentMeta> documents = new HashMap<>();
        try (Stream<Path> paths = Files.walk(Paths.get(corpusPath))){
            int docId = 0;
            for (Path file : (Iterable<Path>) paths.filter(Files::isRegularFile)::iterator) {
                String content = readDocument(file.toString());
                int length = content.split("\\s+").length;
                documents.put(docId, new DocumentMeta(file.toString(), length));
                docId++;
            }
        }catch (IOException e) {
            System.err.println(e);
        }
         System.out.println("Loaded " + documents.size() + " documents from " + corpusPath);
        return documents;
    }

    public String readDocument(String filePath) throws IOException{
        return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
    }
}
