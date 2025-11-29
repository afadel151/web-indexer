package  com.emp.indexer;

public class DocumentMeta {

    String path;
    int length;

    public DocumentMeta(String path, int length) {
        this.path = path;
        this.length = length;
    }

    public String getPath() {
        return path;
    }

    public int getLength() {
        return length;
    }
}
