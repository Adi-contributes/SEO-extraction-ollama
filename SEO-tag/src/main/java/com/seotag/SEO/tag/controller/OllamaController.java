package com.seotag.SEO.tag.controller;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.seotag.SEO.tag.entities.HTMLreader;


@Controller
@RequestMapping("/api")
public class OllamaController {

    @GetMapping("/")
    @ResponseBody
    public String getApiInfo() {
        return """
            SEO Extraction and Bias Detection API
            
            Available endpoints:
            
            GET  /api/ollama - Get default HTML content
            POST /api/ollama - Analyze with prompt (uses default HTML)
            POST /api/analyze - Analyze content with optional file upload
            POST /api/upload-rest - Legacy endpoint for file upload
            
            Usage examples:
            
            curl -X POST "http://localhost:8080/api/analyze" \\
                 -F "prompt=Extract SEO tags from the provided HTML"
            
            curl -X POST "http://localhost:8080/api/analyze" \\
                 -F "file=@your-file.html" \\
                 -F "prompt=Detect bias in the content"
            
            curl -X POST "http://localhost:8080/api/ollama" \\
                 -F "prompt=Extract SEO tags from the provided HTML"
            """;
    }
    
    @GetMapping("/ollama")
    @ResponseBody
    public String getHTMLString() {
    
            String htmlContent = HTMLreader.extractArticleTag("seo.html");
            return htmlContent;

        
    }
    @PostMapping("/ollama")
    @ResponseBody
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
    @GetMapping("/analyze")
    public ModelAndView showAnalyzeForm() {
        return new ModelAndView("upload");
    }

    @PostMapping("/analyze")
    @ResponseBody
    public String analyzeContent(@RequestParam(value = "file", required = false) MultipartFile file,
                                 @RequestParam("prompt") String prompt) {
        try {
            String fullPrompt;
            
            if (file != null && !file.isEmpty()) {
                // Read the uploaded file content
                String uploadedContent = new String(file.getBytes());
                // Extract article tag from uploaded content
                org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(uploadedContent);
                org.jsoup.nodes.Element article = doc.selectFirst("article");
                String htmlContent = article != null ? article.outerHtml() : uploadedContent;
                fullPrompt = htmlContent + "\n\n" + prompt;
            } else {
                // No file uploaded, just use the prompt
                fullPrompt = prompt;
            }
            
            System.out.println("Full Prompt: " + fullPrompt);
            String ollamaRaw = callOllama(fullPrompt);
            return prettyPrintOllamaResponse(ollamaRaw);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/upload-rest")
    @ResponseBody
    public String handleFileUploadRest(@RequestParam("file") MultipartFile file,
                                       @RequestParam("prompt") String prompt) {
        try {
            String htmlContent = HTMLreader.extractArticleTag("seo.html");
            String fullPrompt = htmlContent + "\n\n" + prompt;
            System.out.println("Full Prompt: " + fullPrompt); // Debugging line
            String ollamaRaw = callOllama(fullPrompt);
            System.out.println(ollamaRaw); //Debuggy
            return prettyPrintOllamaResponse(ollamaRaw);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String prettyPrintOllamaResponse(String raw) {
        // Validate input
        if (raw == null || raw.trim().isEmpty()) {
            return "Error: Empty or null response from Ollama.";
        }
        
        try {
            // Parse the single JSON response from Ollama
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(raw);
            
            // Extract the "response" field which contains the complete generated text
            if (jsonNode.has("response")) {
                String response = jsonNode.get("response").asText();
                if (response != null && !response.isEmpty()) {
                    // The asText() method should preserve newlines, but let's ensure proper formatting
                    // Replace any escaped newlines with actual newlines if needed
                    String formattedResponse = response
                        .replace("\\n", "\n")  // Replace escaped newlines
                        .replace("\\r", "\r")  // Replace escaped carriage returns
                        .trim();               // Only trim leading/trailing whitespace
                    
                    // Debug: Print the response to see if newlines are preserved
                    System.out.println("Formatted response: [" + formattedResponse + "]");
                    
                    return formattedResponse;
                }
            }
            
            // If no response field found, return error message
            return "No response field found in Ollama response.";
            
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // Log the parsing error for debugging
            System.err.println("Failed to parse Ollama response as JSON: " + e.getMessage());
            System.err.println("Raw response: " + raw);
            return "Error parsing Ollama response: " + e.getMessage();
        } catch (Exception e) {
            // Handle any other unexpected errors
            System.err.println("Unexpected error parsing Ollama response: " + e.getMessage());
            return "Error processing Ollama response: " + e.getMessage();
        }
    }

    public String callOllama(String prompt) throws Exception {
        String ollamaUrl = "http://localhost:11434/api/generate";
        
        // Validate input
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }
        
        // Create request payload
        java.util.Map<String, Object> requestObj = new java.util.HashMap<>();
        requestObj.put("model", "llama2:latest");
        requestObj.put("prompt", prompt.trim());
        requestObj.put("stream", false); // Set to false for non-streaming response
        
        String requestBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestObj);

        // Create HTTP request with timeout
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ollamaUrl))
            .header("Content-Type", "application/json")
            .timeout(java.time.Duration.ofSeconds(60)) // 60 second timeout
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        // Send request and get response
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Check response status
        if (response.statusCode() != 200) {
            throw new RuntimeException("Ollama API returned status code: " + response.statusCode() + 
                                     " with body: " + response.body());
        }
        
        // Validate response body
        String responseBody = response.body();
        if (responseBody == null || responseBody.trim().isEmpty()) {
            throw new RuntimeException("Ollama API returned empty response");
        }
        
        // Debug: Print the raw response to see the format
        System.out.println("Raw Ollama response: [" + responseBody + "]");
        
        return responseBody;
    }   
}