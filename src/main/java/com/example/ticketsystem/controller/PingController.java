package com.example.ticketsystem.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ping")
public class PingController {
    @GetMapping
    public Map<String, String> ping() {
        return Map.of("pong", "ok");
    }
}