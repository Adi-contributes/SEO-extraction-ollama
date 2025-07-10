package com.seotag.SEO.tag.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String getRootInfo() {
        return """
            SEO Extraction and Bias Detection API
            
            Welcome! This is a REST API for SEO extraction and bias detection using Ollama.
            
            Available endpoints:
            
            GET  /api/ - API documentation and usage examples
            GET  /api/ollama - Get default HTML content
            POST /api/ollama - Analyze with prompt (uses default HTML)
            POST /api/analyze - Analyze content with optional file upload
            POST /api/upload-rest - Legacy endpoint for file upload
            
            Quick start:
            
            Visit /api/ for detailed API documentation and usage examples.
            
            Example usage:
            curl -X POST "http://localhost:8080/api/analyze" \\
                 -F "prompt=Extract SEO tags from the provided HTML"
            """;
    }
} 