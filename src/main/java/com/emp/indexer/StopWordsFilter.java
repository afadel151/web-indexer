package  com.emp.indexer;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emp.web_indexer.models.HtmlToken;

public class StopWordsFilter {
    private final Set<String> stopwords;
  

    public StopWordsFilter(String enStopwordsFile,String frStopwordsFile,String htmlStopwordsFile) throws IOException {
        stopwords = new HashSet<>();
        
        stopwords.addAll(stopWordsBuilder(frStopwordsFile));
        stopwords.addAll(stopWordsBuilder(enStopwordsFile));
        stopwords.addAll(stopWordsBuilder(htmlStopwordsFile));
        System.out.println("Loaded " + stopwords.size() + " stop words from " + enStopwordsFile);
    }

    public Set<String> stopWordsBuilder(String stopwordsFile) throws IOException 
    {
        Set<String> stopwords = new HashSet<>();
        List<String> lines = Files.readAllLines(Paths.get(stopwordsFile));
        for (String line : lines) {
            String word = line.trim().toLowerCase();
            if (!word.isEmpty()) {
                stopwords.add(word);
            }
        }
        return stopwords;
    }
    public List<String> filter(List<String> tokens) {
        List<String> filtered = new ArrayList<>();
        for (String token : tokens) {
            if (!stopwords.contains(token)) {
                filtered.add(token);
            }
        }
        return filtered;
    }

    public List<HtmlToken> filterHtml(List<HtmlToken> tokens) 
    {
        List<HtmlToken> filtered = new ArrayList<>();
        for (HtmlToken token : tokens) {
            if (!stopwords.contains(token.term)) {
                filtered.add(token);
            }
        }
        return filtered;
    }

}
