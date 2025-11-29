package  com.emp.indexer;


//enable/disable tokenization stopwords  stemming  weighting   
// k1 and b for BM25 and paths to corpus and index folder
public class Config {

    public static final String CORPUS_PATH = "crawler_data/pages/";
    public static final String INDEX_PATH = "index_data/";
    public static final String ENGLISH_STOPWORDS_PATH = "stopwords-en.txt";
    public static final String FRENCH_STOPWORDS_PATH = "stopwords-fr.txt";
    public static final String HTML_STOPWORDS_PATH = "stopwords-html.txt";

    public static final boolean USE_STOPWORDS = true;
    public static final boolean USE_STEMMING = true;
    
    public static final double K1 = 1.2; 
    public static final double B = 0.75;
}
