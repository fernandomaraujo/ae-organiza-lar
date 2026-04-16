package com.aelares.service;

import com.aelares.entity.AuthToken;
import com.aelares.entity.User;
import com.aelares.dto.response.UserResponse;
import com.aelares.exception.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AuthService {

    @Inject
    EmailService emailService;

    @ConfigProperty(name = "app.auth.token-expiration-minutes", defaultValue = "10080")
    int tokenExpirationMinutes;

    @ConfigProperty(name = "app.auth.otp-expiration-minutes", defaultValue = "15")
    int otpExpirationMinutes;

    @ConfigProperty(name = "app.auth.jwt-secret", defaultValue = "your-secret-key-change-in-production")
    String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Gera um código OTP de 6 dígitos e envia por email
     */
    @Transactional
    public void sendOtpCode(String email) {
        // Limpar códigos antigos expirados
        AuthToken.delete("email = ?1 and expires_at < CURRENT_TIMESTAMP", email);

        // Gerar novo código
        String code = generateOtpCode();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(otpExpirationMinutes);

        AuthToken authToken = new AuthToken();
        authToken.id = UUID.randomUUID();
        authToken.email = email;
        authToken.code = code;
        authToken.createdAt = now;
        authToken.expiresAt = expiresAt;
        authToken.ipAddress = "127.0.0.1"; // TODO: Get from request

        authToken.persist();

        // Enviar email
        emailService.sendOtpEmail(email, code);
    }

    /**
     * Verifica o código OTP e gera JWT token
     */
    @Transactional
    public String verifyOtpAndGetToken(String email, String code) {
        AuthToken authToken = AuthToken.findByCode(code);

        if (authToken == null) {
            throw new UnauthorizedException("Código inválido ou expirado");
        }

        if (!authToken.email.equals(email)) {
            throw new UnauthorizedException("Email não corresponde ao código");
        }

        if (authToken.isExpired()) {
            throw new UnauthorizedException("Código expirado");
        }

        if (authToken.isUsed()) {
            throw new UnauthorizedException("Código já foi utilizado");
        }

        // Marcar como usado
        authToken.usedAt = LocalDateTime.now();
        authToken.persist();

        // Buscar ou criar usuário
        User user = User.findByEmail(email);
        if (user == null) {
            user = new User();
            user.id = UUID.randomUUID();
            user.email = email;
            user.name = email.split("@")[0]; // Nome padrão
            user.createdAt = LocalDateTime.now();
            user.persist();
        } else {
            // Atualizar last_login
            user.lastLogin = LocalDateTime.now();
            user.persist();
        }

        // Gerar JWT token
        return generateJwtToken(user);
    }

    /**
     * Gera um JWT token válido
     */
    public String generateJwtToken(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(tokenExpirationMinutes);

        return Jwts.builder()
            .subject(user.id.toString())
            .claim("email", user.email)
            .claim("name", user.name)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + (tokenExpirationMinutes * 60 * 1000)))
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Extrai UUID do usuário do JWT token
     */
    public UUID extractUserIdFromToken(String token) {
        try {
            String userId = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

            return UUID.fromString(userId);
        } catch (Exception e) {
            throw new UnauthorizedException("Token inválido");
        }
    }

    /**
     * Valida se o token é válido
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gera um código OTP de 6 dígitos
     */
    private String generateOtpCode() {
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    /**
     * Extrai email do token JWT
     */
    public String extractEmailFromToken(String token) {
        try {
            return (String) Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email");
        } catch (Exception e) {
            throw new UnauthorizedException("Token inválido");
        }
    }
}

