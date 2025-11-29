package  com.emp.indexer;


import java.util.ArrayList;
import java.util.List;

import org.tartarus.snowball.ext.FrenchStemmer;

import com.emp.indexer.snowball.ext.porterStemmer;
import com.emp.language_detector.Detector;
import com.emp.web_indexer.models.HtmlToken;

public class Stemmer {

    private final porterStemmer porter = new porterStemmer();
    private final FrenchStemmer frenchStemmer = new FrenchStemmer();
    private final Detector languageDetector = new Detector();


    public String stem(String token) {
        if (token == null || token.isEmpty()) return token;
        String lang = languageDetector.detect(token);
        if (null == lang) {
            return token;
        }else switch (lang) {
            case "en" -> {
                porter.setCurrent(token);
                porter.stem();
                return porter.getCurrent();
            }
            // case "fr" -> {
            //     frenchStemmer.setCurrent(token);
            //     frenchStemmer.stem();
            //     return frenchStemmer.getCurrent();
            // }
            default -> {
                return token;
            }
        }
    }

     public List<String> stemAll(List<String> tokens) {
        List<String> stems = new ArrayList<>(tokens.size());
        for (String token : tokens) {
            stems.add(stem(token));
        }
        return stems;
    }


    public List<HtmlToken> stemAllHtml(List<HtmlToken> tokens)
    {
        List<HtmlToken> stems = new ArrayList<>(tokens.size());
        for (HtmlToken token : tokens) {
            token.setTerm(stem(token.term));
            stems.add(token);
        }
        return stems;
    }


}
