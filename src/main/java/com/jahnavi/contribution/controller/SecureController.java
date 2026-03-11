package com.jahnavi.contribution.controller;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SecureController {

    @GetMapping("/secure")
    public Map<String, String> secureEndpoint(Authentication authentication) {
        return Map.of(
                "message", "You have accessed a protected endpoint.",
                "username", authentication.getName()
        );
    }
}
