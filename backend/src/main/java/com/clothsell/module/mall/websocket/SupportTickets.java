package com.clothsell.module.mall.websocket;

import com.clothsell.framework.security.core.TokenService;
import com.clothsell.framework.security.core.util.SecurityFrameworkUtils.LoginUser;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class SupportTickets {
    public record Ticket(LoginUser user, String token, Instant expires) {}
    private final Map<String, Ticket> tickets = new HashMap<>();
    private final TokenService tokens;
    public SupportTickets(TokenService tokens) { this.tokens = tokens; }

    public synchronized String issue(String raw) {
        LoginUser user = tokens.parse(raw);
        if (user == null) throw new org.springframework.security.access.AccessDeniedException("请先登录");
        tickets.values().removeIf(ticket -> ticket.expires().isBefore(Instant.now())
                || (ticket.user().id().equals(user.id()) && ticket.user().role().equals(user.role())));
        if (tickets.size() >= 10000) throw new com.clothsell.framework.common.exception.ServiceException(503, "连接繁忙，请稍后重试");
        String key = UUID.randomUUID().toString();
        tickets.put(key, new Ticket(user, raw, Instant.now().plusSeconds(60)));
        return key;
    }

    public synchronized Ticket consume(String key) {
        Ticket ticket = tickets.remove(key);
        return ticket != null && ticket.expires().isAfter(Instant.now()) && tokens.parse(ticket.token()) != null
                ? ticket : null;
    }
}
