package com.adam.event_platform;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String sayHello() {
        return "Welcome to the Event Platform!";
    }

    @GetMapping("/smiley")
    public String getSmileyFace() {
        return "<div style='font-size: 48px;'></div><p>The platform is running smoothly!</p>";
    }
}