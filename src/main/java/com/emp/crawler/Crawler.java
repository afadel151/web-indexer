package com.emp.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {

    private String rootUrl;

    private LinkedHashSet<String> urlToDiscover = new LinkedHashSet<>();

    private Set<String> urlCrawled = new HashSet<>();

    private int stopCondition;
    private int depth;
    private int seconds;
    private int pagesNumber;
    private final File pagesDir = new File("crawler_data/pages");

    public Crawler(int stopCondition, int depth, int seconds, int pagesNumber) {
        this.stopCondition = stopCondition;
        this.seconds = seconds;
        this.depth = depth;
        this.pagesNumber = pagesNumber;
    }

    public boolean isHtmlPage(String url) {
        if (url == null || url.isEmpty() || url.startsWith("mailto:")) {
            return false;
        }
        String lower = url.toLowerCase();
        int lastDot = lower.lastIndexOf('.');
        if (!lower.startsWith("http") || !lower.startsWith("https")) {
            return false;
        }

        if (lastDot == -1) {
            return true;
        }
        String ext = lower.substring(lastDot);
        Set<String> allowed = Set.of(".html", ".htm", ".xhtml", ".php", ".asp", ".aspx", ".jsp", ".jspx");
        return allowed.contains(ext);
    }

    public void crawlSite(String url) {

        System.out.println("crawling");
        try {
            System.out.println("-> CRAWLING: " + url);

            Document doc = Jsoup.connect(url)
                    .timeout(10000)
                    .userAgent("Mozilla/5.0 (compatible; SimpleJavaCrawler/1.0)")
                    .get();
            Set<String> newLinks = getDocLinks(doc);
            System.out.println(newLinks);
            int linksAdded = 0;
            for (String link : newLinks) {
                if (!urlCrawled.contains(link) && !urlToDiscover.contains(link)) {
                    urlToDiscover.add(link);
                    addToDiscover(link);
                    linksAdded++;
                }
            }
            System.out.println("<- SUCCESS: " + url + " | New links: " + linksAdded);
            savePage(url, doc);
        } catch (IOException e) {
            System.err.println("FAILURE: Could not crawl " + url + " -> " + e.getMessage());
        }
    }

    public void startCrawling() {
        initCrawling();
        if (!rootUrl.isEmpty() && !urlCrawled.contains(rootUrl)) {
            urlToDiscover.add(rootUrl);
        }
        crawlSite(rootUrl);
        int crawledPages = urlCrawled.size();
        System.out.println("\n--- Starting Crawling Process ---");
        System.out.println("Max pages: " + this.pagesNumber);
        System.out.println("Initial Discover Size: " + urlToDiscover.size());
        while (!urlToDiscover.isEmpty() && crawledPages < this.pagesNumber) {
            System.out.println("loop");
            Iterator<String> iterator = urlToDiscover.iterator();
            String currentUrl = iterator.next();
            iterator.remove();
            if (urlCrawled.contains(currentUrl)) {
                continue;
            }
            if (!isHtmlPage(currentUrl)) {
                System.out.println("Skipping non-HTML/invalid URL: " + currentUrl);
                continue;
            }
            crawlSite(currentUrl);
            urlCrawled.add(currentUrl);
            registerCrawled(currentUrl);
            crawledPages++;
            if (this.seconds > 0 && crawledPages < this.pagesNumber) {
                try {
                    System.out.println("Waiting " + this.seconds + " seconds...");
                    Thread.sleep(this.seconds * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        System.out.println("\n--- Crawling finished. Crawled "
                + crawledPages + "/" + this.pagesNumber
                + ". Remaining queue: " + urlToDiscover.size() + " ---");
    }

    public void initCrawling() {
        try {
            this.rootUrl = readFile("root_url.txt").readLine().trim();
            this.urlCrawled = readFile("url_crawled.txt")
                    .lines().map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());

            this.urlToDiscover = readFile("url_to_discover.txt")
                    .lines().map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            System.out.println("State initialized successfully.");
        } catch (IOException e) {
            System.out.println("Error loading state: " + e.getMessage());
        }
    }

    public Set<String> getDocLinks(Document doc) {
        Elements links = doc.select("a[href]");
        Set<String> extractedUrls = new HashSet<>();
        for (Element link : links) {
            String url = link.attr("abs:href");
            if (!url.isEmpty()) {
                extractedUrls.add(url);
            }
        }
        return extractedUrls;
    }

    public BufferedReader readFile(String file) throws IOException {
        Path p = Paths.get("crawler_data", file);
        return new BufferedReader(new InputStreamReader(Files.newInputStream(p), StandardCharsets.UTF_8));
    }

    public static void registerCrawled(String url) {
        Path filePath = Paths.get("crawler_data", "url_crawled.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), true))) {
            writer.newLine();
            writer.write(url);
        } catch (IOException e) {
            System.out.println("Error writing crawled: " + e.getMessage());
        }
    }

    public static void addToDiscover(String url) {
        Path filePath = Paths.get("crawler_data", "url_to_discover.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile(), true))) {
            writer.newLine();
            writer.write(url);
        } catch (IOException e) {
            System.out.println("Error writing discover: " + e.getMessage());
        }
    }

    private void savePage(String url, Document doc) {
        try {
            String safeName = url
                    .replace("https://", "")
                    .replace("http://", "")
                    .replaceAll("[^a-zA-Z0-9.-]", "_");

            if (!safeName.endsWith(".html")) {
                safeName += ".html";
            }

            File out = new File(pagesDir, safeName);

            try (PrintWriter pw = new PrintWriter(out, StandardCharsets.UTF_8)) {
                pw.write(doc.outerHtml());
            }

            System.out.println("Saved: " + out.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Failed to save page: " + url + " -> " + e.getMessage());
        }
    }

}
