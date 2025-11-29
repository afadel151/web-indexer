package com.emp.crawler;

import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws  IOException {
        Crawler c = new Crawler(0, 3, 1, 1000);
        c.startCrawling();
    }
}
