package com.seotag.SEO.tag.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seotag.SEO.tag.entities.HTMLreader;


@RestController
@RequestMapping("/api")
public class OllamaController {

    
    @GetMapping("/ollama")
    public String getHTMLString() {
        try {
            String htmlContent = HTMLreader.readHtmlFile("seo.html");
            return htmlContent;
        } catch (IOException e) {
            return "Error reading HTML file: " + e.getMessage();
        }
    }
    @PostMapping("/ollama")
    public String postHTMLString() {
        try {
            String htmlContent = HTMLreader.readHtmlFile("seo.html");
            String prompt = htmlContent + "\n\n Extract SEO's like you would for an HTML file.";

            // Prepare request to Ollama
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String ollamaUrl = "http://localhost:11434/api/generate";
            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("model", "llama3.2"); // Change model if needed
            requestBody.put("prompt", prompt);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/json");
            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(ollamaUrl, entity, String.class);
            return response;
        } catch (IOException e) {
            return "Error reading HTML file: " + e.getMessage();
        } catch (Exception e) {
            return "Error communicating with Ollama: " + e.getMessage();
        }
    }
}