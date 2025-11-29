package  com.emp.indexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryProcessor {

    //List<Integer> indexQuery(String query)
    //output : list of termIDs present in lexicon.

    
    public List<Integer> processQuery(String query,
            Map<String, Integer> termStringToId,
            Tokenizer tokenizer,
            StopWordsFilter stopFilter,
            Stemmer stemmer) {

        List<Integer> queryTermIds = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            return queryTermIds;
        }

        // tokenize 
        List<String> tokens = tokenizer.tokenize(query);

        //    stop-word removal 
        if (Config.USE_STOPWORDS) {
            tokens = stopFilter.filter(tokens);
        }
        // stemming
        if (Config.USE_STEMMING) {
            tokens = stemmer.stemAll(tokens);
        }

        // convert to term IDs (only if term exists in index) 
        for (String term : tokens) {

            Integer termId = termStringToId.get(term);
            if (termId != null) {
                queryTermIds.add(termId);
            }
        }
        return queryTermIds;
    }
}
