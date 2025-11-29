package com.emp.indexer;

import java.util.List;
import java.util.Map;


public class DataStructures {
    // Lexicon
    Map<Integer, String> termIdToString;
    Map<String, Integer> termStringToId;
    Map<Integer, Integer> termIdToDF;

    //Documents
    Map<Integer, DocumentMeta> documents;

    //Posting lists and inverted index
    Map<Integer, List<Posting>> postings; // termID -> (docID -> term frequency in doc)
    
}
