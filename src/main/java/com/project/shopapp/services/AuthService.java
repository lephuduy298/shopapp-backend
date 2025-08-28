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

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String facebookClientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String facebookRedirectUri;



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
        else if ("facebook".equalsIgnoreCase(loginType)) {
            String baseUrl = "https://www.facebook.com/v15.0/dialog/oauth";
            String responseType = "code";
            String scope = "public_profile";
            String state = UUID.randomUUID().toString();

            return String.format(
                    "%s?client_id=%s&redirect_uri=%s&response_type=%s&scope=%s&state=%s",
                    baseUrl,
                    facebookClientId,
                    facebookRedirectUri,
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
                // Bước 1: Đổi code lấy access token
                String facebookTokenUri = "https://graph.facebook.com/v20.0/oauth/access_token"
                        + "?client_id=" + facebookClientId
                        + "&redirect_uri=" + facebookRedirectUri
                        + "&client_secret=" + facebookClientSecret
                        + "&code=" + code;

                try {
                    ResponseEntity<String> response = restTemplate.getForEntity(facebookTokenUri, String.class);
                    System.out.println("Facebook token response: " + response.getStatusCode() + " - " + response.getBody());
                    Map<String, Object> facebookTokenResponse = new ObjectMapper().readValue(
                            response.getBody(),
                            new TypeReference<Map<String, Object>>() {}
                    );
                    accessToken = (String) facebookTokenResponse.get("access_token");
                    String facebookUserInfoUri = "https://graph.facebook.com/me?fields=id,name,picture&access_token=" + accessToken;
                    return new ObjectMapper().readValue(
                            restTemplate.getForEntity(facebookUserInfoUri, String.class).getBody(),
                            new TypeReference<Map<String, Object>>() {}
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Bước 2: Gọi API lấy thông tin user từ Facebook (không dùng Authorization header)


            default:
                return new HashMap<>();
        }
    }

}
