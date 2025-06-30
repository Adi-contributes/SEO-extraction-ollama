package com.seotag.SEO.tag.controller;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.seotag.SEO.tag.entities.HTMLreader;


@RestController
@RequestMapping("/api")
public class OllamaController {

    
    @GetMapping("/ollama")
    public String getHTMLString() {
    
            String htmlContent = HTMLreader.extractArticleTag("seo.html");
            return htmlContent;

        
    }
    @PostMapping("/ollama")
    public String postHTMLString(@RequestParam("prompt") String prompt) {
        try {
            String htmlContent = HTMLreader.extractArticleTag("seo.html");
            String fullPrompt = htmlContent + "\n\n" + prompt;
            String ollamaRaw = callOllama(fullPrompt);
            return prettyPrintOllamaResponse(ollamaRaw);
        } catch (IOException e) {
            return "Error reading HTML file: " + e.getMessage();
        } catch (Exception e) {
            return "Error communicating with Ollama: " + e.getMessage();
        }
    }
    @PostMapping("/upload-rest")
    public String handleFileUploadRest(@RequestParam("file") MultipartFile file,
                                       @RequestParam("prompt") String prompt) {
        try {
            String htmlContent = HTMLreader.extractArticleTag("seo.html");
            String fullPrompt = htmlContent + "\n\n" + prompt;
            System.out.println("Full Prompt: " + fullPrompt); // Debugging line
            String ollamaRaw = callOllama(fullPrompt);
            return prettyPrintOllamaResponse(ollamaRaw);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String prettyPrintOllamaResponse(String raw) {
        StringBuilder sb = new StringBuilder();
       for (String line : raw.split("\\n")) {
           line = line.trim();
           if (line.isEmpty()) continue;
           try {
               com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(line);
               if (node.has("response")) {
                   String resp = node.get("response").asText();
                   if (resp != null && !resp.trim().isEmpty()) {
                       sb.append(resp);
                   }
               }
           } catch (Exception e) {
               
           }
       }
       String result = sb.toString();
       return result.isEmpty() ? "No meaningful response from Ollama." : result;
    
    }

    public String callOllama(String prompt) throws Exception {
        String ollamaUrl = "http://localhost:11434/api/generate";
        java.util.Map<String, String> requestObj = new java.util.HashMap<>();
        requestObj.put("model", "llama2:latest");
        requestObj.put("prompt", prompt);
        String requestBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestObj);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ollamaUrl))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }   
}