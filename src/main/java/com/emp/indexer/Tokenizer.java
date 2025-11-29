package  com.emp.indexer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Tokenizer {

    //List<String> tokenize(String text)
    private final String  delimiters = " ,.’:;!?-_()[]{}\"\\/\n\r\t";
    public Tokenizer() {
    }
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return tokens;
        }
        text = text.toLowerCase();
        StringBuilder sb = new StringBuilder();
        sb.append(text);
        StringTokenizer tokenizer = new StringTokenizer(sb.toString(), this.delimiters);
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }
        return tokens;
    }
}
