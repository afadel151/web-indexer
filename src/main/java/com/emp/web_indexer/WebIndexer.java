package com.emp.web_indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emp.indexer.Config;
import com.emp.indexer.DocumentMeta;
import com.emp.indexer.DocumentReader;
import com.emp.indexer.Stemmer;
import com.emp.indexer.StopWordsFilter;
import com.emp.web_indexer.models.HtmlToken;
import com.emp.web_indexer.models.PositionInfo;
import com.emp.web_indexer.models.Posting;
import com.emp.web_indexer.models.TagWeights;

public class WebIndexer {

    private final HtmlTokenizer tokenizer;
    private final StopWordsFilter stopFilter;
    private final Stemmer stemmer;

    private final Map<String, Integer> termToId = new HashMap<>();
    private final Map<Integer, String> idToTerm = new HashMap<>();
    private final Map<Integer, List<Posting>> invertedIndex = new HashMap<>();
    private final Map<Integer, Integer> termDf = new HashMap<>();

    private int nextTermId = 0;

    public WebIndexer(HtmlTokenizer tokenizer, StopWordsFilter stopFilter, Stemmer stemmer) {
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

    public Map<Integer, List<Posting>> getInvertedIndex() {
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

            List<HtmlToken> tokens = tokenizer.tokenize(content);
            if (Config.USE_STOPWORDS) {
                tokens = stopFilter.filterHtml(tokens);
            }
            if (Config.USE_STEMMING) {
                tokens = stemmer.stemAllHtml(tokens);
            }
            Map<String, List<PositionInfo>> termFreqs = computeTermFrequency(tokens);

            updateInvertedIndex(docId, termFreqs);
        }

        System.out.println("Index built with " + termToId.size() + " unique terms.");
    }

    private Map<String, List<PositionInfo>> computeTermFrequency(List<HtmlToken> tokens) {
        Map<String, List<PositionInfo>> termPositions = new HashMap<>();

        for (HtmlToken token : tokens) {
            double weight = TagWeights.weightFor(token.tag);

            termPositions
                    .computeIfAbsent(token.term, k -> new ArrayList<>())
                    .add(new PositionInfo(token.position, token.tag, weight));
        }

        return termPositions;
    }

    private void updateInvertedIndex(int docId,Map<String, List<PositionInfo>> termPositions) {

        for (var entry : termPositions.entrySet()) {
            String term = entry.getKey();
            List<PositionInfo> positions = entry.getValue();
            int tf = positions.size();

            int termId = termToId.computeIfAbsent(term, t -> {
                int id = nextTermId++;
                idToTerm.put(id, t);
                return id;
            });

            Posting posting = new Posting(docId, tf, positions);

            invertedIndex.computeIfAbsent(termId, k -> new ArrayList<>())
                    .add(posting);

            termDf.put(termId, termDf.getOrDefault(termId, 0) + 1);
        }
    }

}
