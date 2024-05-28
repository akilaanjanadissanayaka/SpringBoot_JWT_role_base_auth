package com.example.SprintJwt.service;

import com.example.SprintJwt.model.User;
import com.example.SprintJwt.repository.TokenRipository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    private final String SECRET_KEY="8b1d35c845a6097d44f32153d19c0e54d4b8f26adcec66d19349ada5c13dad2f";
    private final TokenRipository tokenRipository;

    public JwtService(TokenRipository tokenRipository) {
        this.tokenRipository = tokenRipository;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public boolean isValid(String token, UserDetails user) {
        String username = extractUsername(token);
        boolean isValidToken=tokenRipository.findByToken(token)
                .map(t->!t.isLoggedOut()).orElse(false);
        return (username.equals(user.getUsername()))&& !isTokenExpired(token)  && isValidToken;
    }
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(User user) {
        String token = Jwts
                .builder()
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 24*60*60*1000 ))
                .signWith(getSigninKey())
                .compact();

        return token;
    }

    private SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
