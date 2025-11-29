package com.emp.web_indexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.emp.indexer.Config;
import com.emp.indexer.Stemmer;
import com.emp.indexer.StopWordsFilter;
import com.emp.indexer.Tokenizer;

public class HtmlQueryProcessor {

    public HtmlQueryProcessor(){}
    public static  List<Integer> processQuery(String query,Map<String, Integer> termStringToId,StopWordsFilter stopFilter,Stemmer stemmer) {


        List<Integer> queryTermIds = new ArrayList<>();
        Tokenizer tokenizer = new Tokenizer();
        if (query == null || query.isEmpty()) {
            System.out.println("empty query");
            return queryTermIds;
        }

        List<String> tokens = tokenizer.tokenize(query);

        if (Config.USE_STOPWORDS) {
            tokens = stopFilter.filter(tokens);
        }

        if (Config.USE_STEMMING) {
            tokens = stemmer.stemAll(tokens);
        }
        System.out.println("tokens " + tokens);
        for (String term : tokens) {

            Integer termId = termStringToId.get(term);
            if (termId != null) {
                queryTermIds.add(termId);
            }
        }
        return queryTermIds;

    }
}
