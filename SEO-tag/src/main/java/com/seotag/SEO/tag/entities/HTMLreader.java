package com.seotag.SEO.tag.entities;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HTMLreader {
    // method to extract only the <article tag from HTML using jsoup
    public static String extractArticleTag(String filePath) {
        try {
            String html = Files.readString(Paths.get(filePath));
            Document doc = Jsoup.parse(html);
            Element article = doc.selectFirst("article");
            return article != null ? article.outerHtml() : "No <article> tag found.";
        } catch (Exception e) {
            return "Error extracting article: " + e.getMessage();
        }
    }
}
