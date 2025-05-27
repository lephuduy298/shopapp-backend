package com.project.shopapp.components;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;


import java.time.Instant;
import java.time.temporal.ChronoUnit;


@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private final JwtEncoder jwtEncoder;

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.secretKey}")
    private String secretKey;

    public String createToken(Authentication authentication) {

        Instant now = Instant.now();
        Instant validity = validity = now.plus(this.expiration, ChronoUnit.SECONDS);;


        // @formatter:off
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(authentication.getName())
                .claim("phoneNumber", authentication.getName());


        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        String token = this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, builder.build())).getTokenValue();
        return token;
    }


}
