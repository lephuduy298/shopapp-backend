package com.project.shopapp.components;

import com.project.shopapp.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    @Value("${jwt.accessTokenExpiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refreshTokenExpiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.secretKey}")
    private String secretKey;


    public String generateAccessToken(User userLogin) {

        //properties => claims
//        List<String> listAuthority = new ArrayList<>();
        Map<String, Object> claims = new HashMap<>();
        claims.put("phoneNumber", userLogin.getPhoneNumber());
        claims.put("email", userLogin.getEmail());
        claims.put("facebookAccountId", userLogin.getFacebookAccountId());
        claims.put("googleAccountId", userLogin.getGoogleAccountId());
//        claims.put("userId", userLogin.getId());

        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(String.valueOf(userLogin.getId()))
                    .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000L))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
            return token;
        } catch (Exception e) {
            throw new InvalidParameterException("Can't generate access jwt token, error: " + e.getMessage());
        }
    }

    public String generateRefreshToken(User userLogin) {

        //properties => claims
//        List<String> listAuthority = new ArrayList<>();
        Map<String, Object> claims = new HashMap<>();
        claims.put("phoneNumber", userLogin.getPhoneNumber());
        claims.put("email", userLogin.getEmail());
        claims.put("facebookAccountId", userLogin.getFacebookAccountId());
        claims.put("googleAccountId", userLogin.getGoogleAccountId());

        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(String.valueOf(userLogin.getId()))
                    .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration * 1000L))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
            return token;
        } catch (Exception e) {
            throw new InvalidParameterException("Can't generate refresh jwt token, error: " + e.getMessage());
        }
    }

    private Key getSignInKey(){
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenExpired (String token){
        Date expirationDate = this.extractClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    public String extractSubject(String token){
        return this.extractClaim(token, Claims::getSubject);
    }

    public String extractPhoneNumber(String token){
        return this.extractClaim(token, claims -> claims.get("phoneNumber", String.class));
    }

    public String extractEmail(String token){
        return this.extractClaim(token, claims -> claims.get("email", String.class));
    }

    /// ////////////////////////////////////////////
    public boolean isValidToken(String token, UserDetails userDetails){
        String subject = this.extractSubject(token);

        return (subject.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public Claims checkValidRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new InvalidParameterException("Refresh token is missing or empty");
        }

        try {
            Claims claims = extractAllClaims(refreshToken);

            // Kiểm tra hạn sử dụng
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                throw new InvalidParameterException("Refresh token has expired");
            }

            // Kiểm tra subject (phoneNumber)
            String phoneNumber = claims.getSubject();
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                throw new InvalidParameterException("Invalid refresh token: phone number is missing");
            }

            return claims;

            // Bạn có thể thêm các kiểm tra khác ở đây, ví dụ: kiểm tra userId tồn tại, token có trong DB, v.v.

        } catch (Exception e) {
            throw new InvalidParameterException("Invalid refresh token: ");
        }
    }

}
