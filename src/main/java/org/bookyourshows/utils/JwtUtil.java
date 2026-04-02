package org.bookyourshows.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

public class JwtUtil {

    private static final String SECRET = "abcABCDedefEFGHghiIJKLMONPjklmnoQRSWXZYpqrstuwbookmyshowillabookyourshowxyz123456";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    private static final long EXPIRATION = 1000 * 60 * 60 * 10; // 10 hour

    public static String generateToken(Integer userId, String role) {

        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setId(jti)
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    public static Claims validateToken(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}