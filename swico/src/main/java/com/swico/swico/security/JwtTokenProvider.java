// package com.swico.swico.security;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.security.Keys;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.stereotype.Component;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import javax.crypto.SecretKey;
// import java.util.Date;
// import java.util.stream.Collectors;

// @Component
// public class JwtTokenProvider {

//     private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

//     private final SecretKey jwtSecret;
//     private final long jwtExpirationMs;

//     public JwtTokenProvider(
//             @Value("${app.jwt.secret}") String secret,
//             @Value("${app.jwt.expiration-ms}") long expirationMs
//     ) {
//         this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes());
//         this.jwtExpirationMs = expirationMs;
//     }

//     public String generateToken(Authentication authentication) {
//         String username = authentication.getName();
//         String roles = authentication.getAuthorities().stream()
//                 .map(GrantedAuthority::getAuthority)
//                 .collect(Collectors.joining(","));

//         Date now = new Date();
//         Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

//         return Jwts.builder()
//                 .subject(username)
//                 .claim("roles", roles)
//                 .issuedAt(now)
//                 .expiration(expiryDate)
//                 .signWith(jwtSecret)
//                 .compact();
//     }

//     public String getUsernameFromToken(String token) {
//         return getClaims(token).getSubject();
//     }

//     public boolean validateToken(String authToken) {
//         try {
//             getClaims(authToken);
//             return true;
//         } catch (Exception ex) {
//             logger.debug("JWT validation failed: {}", ex.getMessage(), ex);
//             return false;
//         }
//     }

//     private Claims getClaims(String token) {
//         return Jwts.parser()
//                 .verifyWith(jwtSecret)
//                 .build()
//                 .parseSignedClaims(token)
//                 .getPayload();
//     }
// }

package com.swico.swico.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey jwtSecret;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = expirationMs;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            getClaims(authToken);
            return true;
        } catch (Exception ex) {
            //  Đổi sang logger.error để in rõ lý do vì sao Token không hợp lệ ra Console
            logger.error("JWT Validation Failed: {}", ex.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
