package com.example.SprintJwt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    @GetMapping("/demo")
    public ResponseEntity<String> demo(){
        return ResponseEntity.ok("Hellooooooo im authorized");
    }

    @GetMapping("/admin_only")
    public ResponseEntity<String> admin_only(){
        return ResponseEntity.ok("Hellooooooo im authorized admin");
    }
}
