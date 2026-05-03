package com.aelares.service;

import com.aelares.entity.AuthToken;
import com.aelares.entity.User;
import com.aelares.exception.TooManyRequestsException;
import com.aelares.exception.ResourceNotFoundException;
import com.aelares.exception.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@ApplicationScoped
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Inject
    EmailService emailService;

    @ConfigProperty(name = "app.auth.token-expiration-minutes", defaultValue = "10080")
    int tokenExpirationMinutes;

    @ConfigProperty(name = "app.auth.otp-expiration-minutes", defaultValue = "15")
    int otpExpirationMinutes;

    @ConfigProperty(name = "app.auth.jwt-secret", defaultValue = "your-secret-key-change-in-production")
    String jwtSecret;

    @ConfigProperty(name = "app.auth.otp-request-window-minutes", defaultValue = "10")
    int otpRequestWindowMinutes;

    @ConfigProperty(name = "app.auth.otp-request-max-attempts", defaultValue = "5")
    int otpRequestMaxAttempts;

    @ConfigProperty(name = "app.auth.otp-request-max-attempts-per-ip", defaultValue = "20")
    int otpRequestMaxAttemptsPerIp;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional
    public void requestCode(String email, String ipAddress) {
        enforceOtpRateLimit(email, ipAddress);

        AuthToken.delete("email = ?1 and (usedAt is null or expiresAt < CURRENT_TIMESTAMP)", email);

        String code = generateOtpCode();
        LocalDateTime now = LocalDateTime.now();

        AuthToken authToken = new AuthToken();
        authToken.id = UUID.randomUUID();
        authToken.email = email;
        authToken.code = code;
        authToken.createdAt = now;
        authToken.expiresAt = now.plusMinutes(otpExpirationMinutes);
        authToken.ipAddress = ipAddress;
        authToken.persist();

        emailService.sendOtpEmail(email, code);
    }

    private void enforceOtpRateLimit(String email, String ipAddress) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(otpRequestWindowMinutes);

        long requestsByEmail = AuthToken.count("email = ?1 and createdAt >= ?2", email, windowStart);
        if (requestsByEmail >= otpRequestMaxAttempts) {
            throw new TooManyRequestsException("Muitas tentativas para este email. Aguarde alguns minutos e tente novamente.");
        }

        if (ipAddress != null && !ipAddress.isBlank()) {
            long requestsByIp = AuthToken.count("ipAddress = ?1 and createdAt >= ?2", ipAddress, windowStart);
            if (requestsByIp >= otpRequestMaxAttemptsPerIp) {
                throw new TooManyRequestsException("Muitas tentativas deste IP. Aguarde alguns minutos e tente novamente.");
            }
        }
    }

    @Transactional
    public AuthResult verifyCode(String email, String code) {
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

        authToken.usedAt = LocalDateTime.now();
        authToken.persist();

        User user = User.findByEmail(email);
        LocalDateTime now = LocalDateTime.now();
        if (user == null) {
            user = new User();
            user.id = UUID.randomUUID();
            user.email = email;
            user.name = email.split("@")[0];
            user.createdAt = now;
            user.preferences = new HashMap<>();
        }
        user.lastLogin = now;
        user.persist();

        String token = generateJwtToken(user);
        return new AuthResult(token, user);
    }

    @Transactional
    public void sendOtpCode(String email) {
        requestCode(email, null);
    }

    @Transactional
    public String verifyOtpAndGetToken(String email, String code) {
        return verifyCode(email, code).token();
    }

    public String generateJwtToken(User user) {
        return Jwts.builder()
            .subject(user.id.toString())
            .claim("email", user.email)
            .claim("name", user.name)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + (tokenExpirationMinutes * 60 * 1000)))
            .signWith(getSigningKey())
            .compact();
    }

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

    private String generateOtpCode() {
        int code = SECURE_RANDOM.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

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

    public User getUserFromToken(String token) {
        UUID userId = extractUserIdFromToken(token);
        User user = User.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }
        return user;
    }

    public record AuthResult(String token, User user) {
    }
}

