package com.paysecure.ai_report_tool_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class SignedUrlService {

    @Value("${app.signed-url.secret:super-secret-key}")
    private String secret;

    private static final long EXPIRATION_SECONDS = 300; // 5 minutes

    public String generateToken(String fileId, String userId) {
        long expiry = Instant.now().getEpochSecond() + EXPIRATION_SECONDS;

        String payload = fileId + ":" + userId + ":" + expiry;
        String signature = sign(payload);

        String token = payload + ":" + signature;

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(token),
                    StandardCharsets.UTF_8
            );

            String[] parts = decoded.split(":");
            if (parts.length != 4) return false;

            String fileId = parts[0];
            String userId = parts[1];
            long expiry = Long.parseLong(parts[2]);
            String signature = parts[3];

            if (Instant.now().getEpochSecond() > expiry) return false;

            String payload = fileId + ":" + userId + ":" + expiry;
            String expectedSignature = sign(payload);

            return expectedSignature.equals(signature);

        } catch (Exception e) {
            return false;
        }
    }

    public String extractFileId(String token) {
        String decoded = new String(
                Base64.getUrlDecoder().decode(token),
                StandardCharsets.UTF_8
        );
        return decoded.split(":")[0];
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("Signing failed");
        }
    }
}