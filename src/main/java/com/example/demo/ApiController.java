package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    
    @GetMapping("/health")
    public String healthCheck() {
        logger.info("Health check endpoint called");
        return "OK";
    }

}
