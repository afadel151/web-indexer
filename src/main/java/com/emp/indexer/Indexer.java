package  com.emp.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Indexer {

    //iterate documents, build in-memory hashmaps
    //Data produced: Terms/Lexicon, Documents map, Posting lists.
    private final Tokenizer tokenizer;
    private final StopWordsFilter stopFilter;
    private final Stemmer stemmer;

    private final Map<String, Integer> termToId = new HashMap<>();
    private final Map<Integer, String> idToTerm = new HashMap<>();
    private final Map<Integer, List<int[]>> invertedIndex = new HashMap<>(); // termId -> list de [docId, tf]
    private final Map<Integer, Integer> termDf = new HashMap<>();            // termId -> DF count

    private int nextTermId = 0;

    public Indexer(Tokenizer tokenizer, StopWordsFilter stopFilter, Stemmer stemmer) {
        this.tokenizer = tokenizer;
        this.stopFilter = stopFilter;
        this.stemmer = stemmer;
    }

    public Map<String, Integer> getTermToId() {
        return termToId;
    }

    public Map<Integer, String> getIdToTerm() {
        return idToTerm;
    }

    public Map<Integer, List<int[]>> getInvertedIndex() {
        return invertedIndex;
    }

    public Map<Integer, Integer> getTermDf() {
        return termDf;
    }

    public void buildIndex(String corpusPath) throws IOException {
        DocumentReader reader = new DocumentReader();
        Map<Integer, DocumentMeta> documents = reader.loadDocuments(corpusPath);

        for (Map.Entry<Integer, DocumentMeta> entry : documents.entrySet()) {
            int docId = entry.getKey();
            String path = entry.getValue().getPath();

            String content = reader.readDocument(path);

            // text processing pipeline 
            List<String> tokens = tokenizer.tokenize(content);
            if (Config.USE_STOPWORDS) {
                tokens = stopFilter.filter(tokens);
            }
            if (Config.USE_STEMMING) {
                tokens = stemmer.stemAll(tokens);
            }

            // term Frequency
            Map<String, Integer> termFreqs = computeTermFrequency(tokens);

            //Update inverted index 
            updateInvertedIndex(docId, termFreqs);
        }

        System.out.println("Index built with " + termToId.size() + " unique terms.");
    }

    private Map<String, Integer> computeTermFrequency(List<String> tokens) {
        Map<String, Integer> tf = new HashMap<>();

        for (String token : tokens) {
            tf.put(token, tf.getOrDefault(token, 0) + 1);
        }

        return tf;
    }

    private void updateInvertedIndex(int docId, Map<String, Integer> termFreqs) {
        for (Map.Entry<String, Integer> entry : termFreqs.entrySet()) {
            String term = entry.getKey();
            int tf = entry.getValue();

            int termId = termToId.computeIfAbsent(term, t -> {
                int id = nextTermId++;
                idToTerm.put(id, t);
                return id;
            });

            invertedIndex.computeIfAbsent(termId, k -> new ArrayList<>())
                    .add(new int[]{docId, tf});

            termDf.put(termId, termDf.getOrDefault(termId, 0) + 1);
        }
    }

}
