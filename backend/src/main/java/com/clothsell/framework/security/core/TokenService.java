package com.clothsell.framework.security.core;

import com.clothsell.framework.security.core.util.SecurityFrameworkUtils.LoginUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
public class TokenService {
    private final String secret;

    public TokenService(@Value("${app.token-secret}") String secret) {
        this.secret = secret;
    }

    public String issueAccess(LoginUser user) {
        long exp = Instant.now().getEpochSecond() + 30 * 60;
        String body = "A|" + user.role() + "|" + user.id() + "|" + user.name() + "|" + exp;
        return b64(body) + "." + sign(body);
    }

    public LoginUser parse(String token) {
        try {
            if (token == null || !token.contains(".")) {
                return null;
            }
            String[] parts = token.split("\\.", 2);
            String body = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            if (!sign(body).equals(parts[1])) {
                return null;
            }
            String[] bits = body.split("\\|", 5);
            if (bits.length != 5 || !"A".equals(bits[0]) || Long.parseLong(bits[4]) < Instant.now().getEpochSecond()) {
                return null;
            }
            return new LoginUser(Long.parseLong(bits[2]), bits[1], bits[3]);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String sign(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String b64(String body) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(body.getBytes(StandardCharsets.UTF_8));
    }
}
