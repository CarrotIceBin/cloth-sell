package com.clothsell.module.mall.service.auth;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.LOGIN_LOCKED;

@Component
public class LoginGuard {
    private static final int MAX_FAILURES = 8;
    private static final Duration WINDOW = Duration.ofMinutes(10);
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void check(String account) {
        Attempt attempt = attempts.get(key(account));
        if (attempt != null && attempt.count >= MAX_FAILURES && attempt.start.plus(WINDOW).isAfter(Instant.now())) {
            throw exception(LOGIN_LOCKED);
        }
    }

    public void fail(String account) {
        if (attempts.size() > 10_000) {
            attempts.clear();
        }
        Instant now = Instant.now();
        attempts.compute(key(account), (ignored, current) -> {
            if (current == null || current.start.plus(WINDOW).isBefore(now)) {
                return new Attempt(now, 1);
            }
            return new Attempt(current.start, current.count + 1);
        });
    }

    public void ok(String account) {
        attempts.remove(key(account));
    }

    private String key(String account) {
        return account == null ? "" : account.trim();
    }

    private static final class Attempt {
        private final Instant start;
        private final int count;

        private Attempt(Instant start, int count) {
            this.start = start;
            this.count = count;
        }
    }
}
