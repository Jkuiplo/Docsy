package com.google.docsy.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${application.security.jwt.expiration:2592000000}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserPrincipal userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())          // setSubject()
                .issuedAt(new Date(System.currentTimeMillis()))   // setIssuedAt()
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // setExpiration()
                .signWith(getSignInKey())                     // The algorithm is derived automatically from the key type.
                .compact();
    }

    public boolean isTokenValid(String token, UserPrincipal userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()                          // parserBuilder()
                .verifyWith(getSignInKey())           // setSigningKey()
                .build()
                .parseSignedClaims(token)            // parseClaimsJws()
                .getPayload();                       // getBody()
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}