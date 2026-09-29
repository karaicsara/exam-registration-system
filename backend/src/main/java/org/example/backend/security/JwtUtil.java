package org.example.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(Long korisnikId, String username, String ime, String prezime, String tipKorisnika) {
        Date sada = new Date();
        Date istice = new Date(sada.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("korisnikId", korisnikId)
                .claim("ime", ime)
                .claim("prezime", prezime)
                .claim("tipKorisnika", tipKorisnika)
                .issuedAt(sada)
                .expiration(istice)
                .signWith(signingKey())
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nevažeći ili istekao token");
        }
    }

    public Long requireUserId(String authHeader, String ocekivaniTipKorisnika) {
        Claims claims = parseToken(extractToken(authHeader));

        if (!ocekivaniTipKorisnika.equals(claims.get("tipKorisnika", String.class))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Akcija nije dozvoljena za ovaj tip korisnika");
        }

        return Long.valueOf(claims.get("korisnikId").toString());
    }

    public Long tryGetUserId(String authHeader, String ocekivaniTipKorisnika) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            Claims claims = parseToken(authHeader.substring("Bearer ".length()));
            if (!ocekivaniTipKorisnika.equals(claims.get("tipKorisnika", String.class))) {
                return null;
            }
            return Long.valueOf(claims.get("korisnikId").toString());
        } catch (ResponseStatusException exception) {
            return null;
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nedostaje autorizacioni token");
        }
        return authHeader.substring("Bearer ".length());
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
