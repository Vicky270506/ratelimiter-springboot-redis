package com.vignesh.ratelimiter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/data")
    public String getData(){
        return "{\"message\": \"Success! Data returned.\"}";
    }

    @PostMapping("/submit")
    public String submitData(){
        return "{\"message\": \"Submitted successfully.\"}";

    }
}
