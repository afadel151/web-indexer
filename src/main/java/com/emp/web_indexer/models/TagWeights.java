package com.emp.web_indexer.models;

import java.util.Map;



public class TagWeights {


    public static final Map<String, Double> WEIGHTS = Map.ofEntries(
        Map.entry("title", 5.0),
        Map.entry("h1", 3.0),
        Map.entry("h2", 2.5),
        Map.entry("strong", 2.0),
        Map.entry("em", 1.8),
        Map.entry("p", 1.0),
        Map.entry("li", 0.8),
        Map.entry("a", 0.5),
        Map.entry("span", 0.3),
        Map.entry("div", 0.2)
    );

    public static double weightFor(String tag) {
        return WEIGHTS.getOrDefault(tag.toLowerCase(), 0.1);
    }
}
