package com.clothsell.module.mall.websocket;

import com.clothsell.framework.security.core.TokenService;
import com.clothsell.framework.security.core.util.SecurityFrameworkUtils.LoginUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupportTicketsTest {
    @Test void expiredTicketCannotBeConsumed() {
        var tickets = new SupportTickets(mock(TokenService.class));
        @SuppressWarnings("unchecked")
        var stored = (java.util.Map<String, SupportTickets.Ticket>) org.springframework.test.util.ReflectionTestUtils.getField(tickets, "tickets");
        stored.put("expired", new SupportTickets.Ticket(new LoginUser(7L, "USER", "buyer"), "token",
                java.time.Instant.now().minusSeconds(1)));
        assertNull(tickets.consume("expired"));
    }
    @Test void ticketIsBoundToIdentityAndCanOnlyBeUsedOnce() {
        var tokens = mock(TokenService.class);
        var buyer = new LoginUser(7L, "USER", "buyer");
        when(tokens.parse("buyer-token")).thenReturn(buyer);
        var tickets = new SupportTickets(tokens);
        String key = tickets.issue("buyer-token");
        assertEquals(buyer, tickets.consume(key).user());
        assertNull(tickets.consume(key));
        assertNull(tickets.consume("unknown"));
        assertThrows(AccessDeniedException.class, () -> tickets.issue("invalid"));
    }
    @Test void expiredLoginInvalidatesTicketAndRolesRemainSeparate() {
        var tokens = mock(TokenService.class);
        when(tokens.parse("buyer")).thenReturn(new LoginUser(7L, "USER", "buyer"));
        when(tokens.parse("admin")).thenReturn(new LoginUser(7L, "ADMIN", "admin"));
        var tickets = new SupportTickets(tokens);
        String buyer = tickets.issue("buyer");
        String admin = tickets.issue("admin");
        assertEquals("ADMIN", tickets.consume(admin).user().role());
        when(tokens.parse("buyer")).thenReturn(null);
        assertNull(tickets.consume(buyer));
    }
    @Test void newTicketReplacesUnconsumedTicketForSameUser() {
        var tokens = mock(TokenService.class);
        when(tokens.parse("buyer")).thenReturn(new LoginUser(7L, "USER", "buyer"));
        var tickets = new SupportTickets(tokens);
        String old = tickets.issue("buyer");
        String current = tickets.issue("buyer");
        assertNull(tickets.consume(old));
        assertNotNull(tickets.consume(current));
    }
}
