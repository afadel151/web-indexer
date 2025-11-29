package  com.emp.indexer;

import java.util.List;
import java.util.Map;

public class IndexData {
    public Map<String, Integer> lexicon;
    public Map<Integer, List<int[]>> postings;
    public Map<Integer, DocumentMeta> documents;
}
