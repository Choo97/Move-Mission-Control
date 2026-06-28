package com.moving.reservation.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class LoginController {

    private final String frontendBaseUrl;

    public LoginController(@Value("${app.frontend.base-url}") String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @GetMapping("/login")
    public String login(@RequestParam MultiValueMap<String, String> queryParams) {
        String baseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        String loginUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/login")
                .queryParams(queryParams)
                .build()
                .toUriString();
        return "redirect:" + loginUrl;
    }
}
