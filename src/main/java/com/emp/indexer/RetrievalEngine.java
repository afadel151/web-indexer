package  com.emp.indexer;

import java.util.*;
public class RetrievalEngine {

    //Map<Integer, Double> scoreTfIdf(List<Integer> queryTermIDs)
    // Map<Integer, Double> scoreBM25(List<Integer> queryTermIDs)

    public Map<Integer, Double> scoreTfIdf(List<Integer> queryTerms,
                                           Map<Integer, List<int[]>> postings,
                                           Map<Integer, Integer> termIdToDf,
                                           int totalDocs) {

        Map<Integer, Double> scores = new HashMap<>();

        for (int termId : queryTerms) {
            List<int[]> plist = postings.get(termId);
            if (plist == null) continue;

            int df = termIdToDf.getOrDefault(termId, 1);
            double idf = Math.log((double) totalDocs / df);

            for (int[] pair : plist) {
                int docId = pair[0];
                int tf = pair[1];
                double score = tf * idf;
                scores.put(docId, scores.getOrDefault(docId, 0.0) + score);
            }
        }

        return scores;
    }

    public Map<Integer, Double> scoreBM25(List<Integer> queryTerms,
                                          Map<Integer, List<int[]>> postings,
                                          Map<Integer, Integer> termIdToDf,
                                          Map<Integer, DocumentMeta> documents,
                                          int totalDocs) {

        Map<Integer, Double> scores = new HashMap<>();

        // Compute average document length
        double avgdl = documents.values().stream()
                .mapToInt(DocumentMeta::getLength)
                .average()
                .orElse(0.0);

        double k1 = Config.K1;
        double b = Config.B;

        for (int termId : queryTerms) {
            List<int[]> plist = postings.get(termId);
            if (plist == null) continue;

            int df = termIdToDf.getOrDefault(termId, 1);
            double idf = Math.log((totalDocs - df + 0.5) / (df + 0.5));

            for (int[] pair : plist) {
                int docId = pair[0];
                int tf = pair[1];
                double docLen = documents.get(docId).getLength();

                double numerator = tf * (k1 + 1);
                double denominator = tf + k1 * (1 - b + b * (docLen / avgdl));
                double score = idf * (numerator / denominator);

                scores.put(docId, scores.getOrDefault(docId, 0.0) + score);
            }
        }

        return scores;
    }
}
