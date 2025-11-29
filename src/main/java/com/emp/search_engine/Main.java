package com.emp.search_engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.emp.indexer.DocumentMeta;
import com.emp.indexer.DocumentReader;
import com.emp.indexer.Stemmer;
import com.emp.indexer.StopWordsFilter;
import com.emp.web_indexer.HtmlQueryProcessor;
import com.emp.web_indexer.HtmlTokenizer;
import com.emp.web_indexer.WebIndexDiskService;
import com.emp.web_indexer.WebIndexer;
import com.emp.web_indexer.WebSearchEngine;
import com.emp.web_indexer.models.Posting;
import com.emp.web_indexer.models.WebIndex;

public class Main {

    public static void main(String[] args) throws IOException {
        String basePath = System.getProperty("user.dir");                    // project root
        String corpusPath = basePath + "/crawler_data/pages"; // folder of .txt documents
        String englishStopword = basePath + "/index_data/stopwords-en.txt";
        String frenchStopword = basePath + "/index_data/stopwords-fr.txt";
        String htmlStopwords = basePath + "/index_data/stopwords-html.txt";

        String indexPath = basePath;               // where lexicon.txt, postings.txt, documents.txt live

        HtmlTokenizer tokenizer = new HtmlTokenizer();
        StopWordsFilter stopFilter = new StopWordsFilter(englishStopword, frenchStopword, htmlStopwords);
        Stemmer stemmer = new Stemmer();
        WebIndexDiskService io = new WebIndexDiskService();

        Map<String, Integer> lexicon;
        Map<Integer, List<Posting>> postings;
        Map<Integer, DocumentMeta> documents;
        Map<Integer, Integer> termDf = new HashMap<>();

        boolean needBuild = Files.size(Paths.get("./index_data/lexicon.txt")) == 0
                || Files.size(Paths.get("./index_data/docs.txt")) == 0
                || Files.size(Paths.get("./index_data/postings.txt")) == 0;

        if (needBuild) {
            System.out.println("No index found — building new index...");

            WebIndexer indexer = new WebIndexer(tokenizer, stopFilter, stemmer);
            indexer.buildIndex(corpusPath);

            // load document metadata again for saving
            DocumentReader reader = new DocumentReader();
            Map<Integer, DocumentMeta> docs = reader.loadDocuments(corpusPath);

            // save the index
            io.save_index_to_disk(indexer.getTermToId(), indexer.getInvertedIndex(), docs, indexPath);
            termDf = indexer.getTermDf();

            System.out.println("Index built and saved successfully!");
        }

        WebIndex data = io.read_index_from_disk(indexPath);
        lexicon = data.lexicon;
        postings = data.postings;
        documents = data.documents;
        for (var e : postings.entrySet()) {
            termDf.put(e.getKey(), e.getValue().size());
        }
        // loop
        WebSearchEngine engine = new WebSearchEngine();
        Scanner sc = new Scanner(System.in);
        System.out.println("\n=== Information Retrieval System ===");
        System.out.println("Type a query, or 'exit' to quit.");

        while (true) {
            System.out.print("\nQuery > ");
            String query = sc.nextLine().trim();
            if (query.equalsIgnoreCase("exit")) {
                break;
            }
            if (query.isEmpty()) {
                continue;
            }
            List<Integer> queryTerms = HtmlQueryProcessor.processQuery(query, lexicon, stopFilter, stemmer);

            if (queryTerms.isEmpty()) {
                System.out.println("No matching terms found in lexicon.");
                continue;
            }

            Map<Integer, Double> scores = engine.scoreBM25(
                    queryTerms, postings, termDf, documents, documents.size());
            
            scores.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                    .limit(10)
                    .forEach(e -> {
                        DocumentMeta doc = documents.get(e.getKey());
                        String url = extractUrl(doc.getPath());
                        System.out.printf("DocID %d | Score: %.4f | Url: %s%n",
                                e.getKey(), e.getValue(), url);
                    });
        }
        System.out.println("Goodbye!");
        sc.close();
    }

    private static String extractUrl(String path) {
        String fileName = Paths.get(path).getFileName().toString();
        String base = fileName.replace(".html", "");

        int firstUnderscore = base.indexOf("_");
        if (firstUnderscore == -1) {
            return fileName;
        }

        String domain = base.substring(0, firstUnderscore);
        String id = base.substring(firstUnderscore + 1).replace("_", "/");

        return "https://" +  domain + "/" + id;
    }

}
