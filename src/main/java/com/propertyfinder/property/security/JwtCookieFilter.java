package com.propertyfinder.property.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

@Component
public class JwtCookieFilter extends OncePerRequestFilter {
    private final SecretKey key;
    private final String cookieName;
    private final StringRedisTemplate redis;

    public JwtCookieFilter(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.cookie-name:JWT}") String cookieName, StringRedisTemplate redis) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32)
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.cookieName = cookieName;
        this.redis = redis;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String token = null;
        if (req.getCookies() != null) for (Cookie c : req.getCookies())
            if (cookieName.equals(c.getName())) {
                token = c.getValue();
                break;
            }
        if (token != null && !token.isBlank()) try {
            String blacklist = "propertyfinder:blacklist:" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
            if (!Boolean.TRUE.equals(redis.hasKey(blacklist))) {
                Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
                String role = claims.get("role", String.class);
                String email = claims.getSubject();
                if (role != null && email != null)
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email, token, List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))));
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(req, res);
    }
}
