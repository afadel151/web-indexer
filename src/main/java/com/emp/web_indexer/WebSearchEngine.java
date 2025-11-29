package com.emp.web_indexer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emp.indexer.Config;
import com.emp.indexer.DocumentMeta;
import com.emp.web_indexer.models.PositionInfo;
import com.emp.web_indexer.models.Posting;


public class WebSearchEngine {

    // tf : tag-weight + postition 
    public Map<Integer, Double> scoreTfIdf(
            List<Integer> queryTerms,
            Map<Integer, List<Posting>> postings,
            Map<Integer, Integer> termToDf,
            int totalDocs) {

        Map<Integer, Double> scores = new HashMap<>();

        for (int termId : queryTerms) {
            List<Posting> plist = postings.get(termId);
            if (plist == null) continue;

            int df = termToDf.getOrDefault(termId, 1);

            double idf = Math.log((double) totalDocs / df);

            for (Posting posting : plist) {
                int docId = posting.getDocId();

                double weightedTf = computeWeightedTf(posting);

                double score = weightedTf * idf;

                scores.put(docId, scores.getOrDefault(docId, 0.0) + score);
            }
        }

        return scores;
    }

    // BM25 using wheigted tf
    public Map<Integer, Double> scoreBM25(
            List<Integer> queryTerms,
            Map<Integer, List<Posting>> postings,
            Map<Integer, Integer> termToDf,
            Map<Integer, DocumentMeta> documents,
            int totalDocs) {

        Map<Integer, Double> scores = new HashMap<>();

        // average document length
        double avgdl = documents.values().stream()
                .mapToInt(DocumentMeta::getLength)
                .average()
                .orElse(0.0);

        double k1 = Config.K1;
        double b = Config.B;

        for (int termId : queryTerms) {
            List<Posting> plist = postings.get(termId);
            if (plist == null) continue;

            int df = termToDf.getOrDefault(termId, 1);
            double idf = Math.log((totalDocs - df + 0.5) / (df + 0.5));

            for (Posting posting : plist) {
                int docId = posting.getDocId();
                double docLen = documents.get(docId).getLength();
                double wtf = computeWeightedTf(posting);
                double numerator = wtf * (k1 + 1);
                double denominator = wtf + k1 * (1 - b + b * (docLen / avgdl));
                double score = idf * (numerator / denominator);

                scores.put(docId, scores.getOrDefault(docId, 0.0) + score);
            }
        }
        return scores;
    }

    private double computeWeightedTf(Posting posting) {
        double sum = 0.0;

        for (PositionInfo p : posting.getPositions()) {
            double tagWeight = p.getTagWeight();
            double positionWeight = 1.0;

            // first 200 more important
            if (p.getPosition() < 200) {
                positionWeight = 1.5 - (p.getPosition() / 200.0);
            }

            sum += (tagWeight * positionWeight);
        }

        return sum;
    }
}
