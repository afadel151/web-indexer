package com.emp.web_indexer.models;

import java.util.List;
import java.util.Map;

import com.emp.indexer.DocumentMeta;

public class WebIndex {
     public Map<String, Integer> lexicon;
    public Map<Integer,List<Posting>> postings;
    public Map<Integer, DocumentMeta> documents;
}
