package com.swico.swico.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping("/hello")
    public String hello() {
        return "Swico Backend Service is running smoothly!";
    }
}
