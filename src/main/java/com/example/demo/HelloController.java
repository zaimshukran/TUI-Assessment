package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping(value = "/api/hello", produces = "text/plain")
    public String hello(@RequestParam(defaultValue = "World")String name){
        return "Hello " + name + "!";
    }
}
