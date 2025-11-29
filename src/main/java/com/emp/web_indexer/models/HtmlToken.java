package com.emp.web_indexer.models;

public class HtmlToken {
    public  String term;
    public final String tag; 
    public final int position; 


    public HtmlToken(int position, String tag, String term) {
        this.position = position;
        this.tag = tag;
        this.term = term;
    }

    public void setTerm(String term)
    {
        this.term = term;
    }
}