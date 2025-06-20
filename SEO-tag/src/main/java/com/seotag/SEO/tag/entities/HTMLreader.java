package com.seotag.SEO.tag.entities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HTMLreader {
    public static String readHtmlFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public static void main(String[] args) {
        try {
            String htmlContent = readHtmlFile("./seo.html");
            System.out.println(htmlContent);
        } catch (IOException e) {
            System.err.println("Error reading HTML file: " + e.getMessage());
        }
    }
}
