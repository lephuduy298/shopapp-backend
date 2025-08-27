package com.project.shopapp.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateAuthUrl(String loginType) {
        if ("google".equalsIgnoreCase(loginType)) {
            String baseUrl = "https://accounts.google.com/o/oauth2/v2/auth";
            String responseType = "code";
            String scope = "email profile"; // để nguyên
            String state = UUID.randomUUID().toString(); // sinh random mỗi lần

            return String.format(
                    "%s?client_id=%s&redirect_uri=%s&response_type=%s&scope=%s&state=%s",
                    baseUrl,
                    googleClientId,
                    googleRedirectUri,
                    responseType,
                    URLEncoder.encode(scope, StandardCharsets.UTF_8),
                    URLEncoder.encode(state, StandardCharsets.UTF_8)
            );
        }
        return null;
    }


    public Map<String, Object> authenticateAndFetchProfile(String code, String loginType) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        String accessToken;

        switch (loginType.toLowerCase()) {
            case "google":
                accessToken = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(),
                        new GsonFactory(),
                        googleClientId,
                        googleClientSecret,
                        code,
                        googleRedirectUri
                ).execute().getAccessToken();

                // Cấu hình RestTemplate để tự động gắn access token vào Authorization header
                String finalAccessToken = accessToken;
                restTemplate.getInterceptors().add((req, body, executionContext) -> {
                    req.getHeaders().set("Authorization", "Bearer " + finalAccessToken);
                    return executionContext.execute(req, body);
                });

                // Gọi API lấy thông tin user
                String googleUserInfoUri = "https://www.googleapis.com/oauth2/v3/userinfo";
                return new ObjectMapper().readValue(
                        restTemplate.getForEntity(googleUserInfoUri, String.class).getBody(),
                        new TypeReference<Map<String, Object>>() {}
                );
            case "facebook":
                // TODO: Implement Facebook login
                return new HashMap<>();
            default:
                return new HashMap<>();
        }
    }

}
