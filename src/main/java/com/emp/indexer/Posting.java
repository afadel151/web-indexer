package  com.emp.indexer;

public class Posting {
    int docId;
    int tf;

    public Posting(int docId, int termFrequency) {
        this.docId = docId;
        this.tf = termFrequency;
    }
}
