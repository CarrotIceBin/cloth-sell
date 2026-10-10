package com.clothsell.module.mall.websocket;

import com.clothsell.framework.security.core.TokenService;
import com.clothsell.framework.security.core.util.SecurityFrameworkUtils.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupportSocketHandlerTest {
    TokenService tokens() {
        var tokens = mock(TokenService.class);
        when(tokens.parse("token")).thenReturn(new LoginUser(7L, "USER", "buyer"));
        return tokens;
    }
    WebSocketSession socket(String id) {
        var socket = mock(WebSocketSession.class);
        when(socket.getId()).thenReturn(id);
        when(socket.isOpen()).thenReturn(true);
        when(socket.getAttributes()).thenReturn(Map.of("supportTicket", new SupportTickets.Ticket(
                new LoginUser(7L, "USER", "buyer"), "token", Instant.now().plusSeconds(60))));
        return socket;
    }

    @Test void blockedConnectionDoesNotDelayOtherConnectionsAndKeepsItsOwnOrder() throws Exception {
        var handler = new SupportSocketHandler(new ObjectMapper(), tokens());
        var slow = socket("slow"); var healthy = socket("healthy");
        var entered = new CountDownLatch(1); var release = new CountDownLatch(1);
        var received = new CopyOnWriteArrayList<Integer>();
        doAnswer(invocation -> {
            String payload = ((TextMessage) invocation.getArgument(0)).getPayload();
            if (payload.contains("roomMessage")) {
                entered.countDown(); release.await(3, TimeUnit.SECONDS);
                received.add(new ObjectMapper().readTree(payload).path("data").path("sequence").asInt());
            }
            return null;
        }).when(slow).sendMessage(any());
        try {
            handler.afterConnectionEstablished(slow); handler.afterConnectionEstablished(healthy);
            handler.publishRoom(Set.of("USER:7"), Map.of("sequence", 1));
            assertTrue(entered.await(1, TimeUnit.SECONDS));
            handler.publishRoom(Set.of("USER:7"), Map.of("sequence", 2));
            verify(healthy, timeout(1000)).sendMessage(argThat(message -> message.getPayload().toString().contains("\"sequence\":2")));
            assertTrue(received.isEmpty(), "Slow socket must still be blocked");
            release.countDown();
            verify(slow, timeout(1000)).sendMessage(argThat(message -> message.getPayload().toString().contains("\"sequence\":2")));
            long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
            while (received.size() < 2 && System.nanoTime() < end) Thread.sleep(5);
            assertEquals(List.of(1, 2), received);
        } finally { release.countDown(); handler.shutdown(); }
    }

    @Test void queueOverflowClosesOnlySlowSocketAndReleasesQueuedMessages() throws Exception {
        var handler = new SupportSocketHandler(new ObjectMapper(), tokens());
        var slow = socket("overflow"); var entered = new CountDownLatch(1); var release = new CountDownLatch(1);
        doAnswer(invocation -> {
            if (((TextMessage)invocation.getArgument(0)).getPayload().contains("roomMessage")) {
                entered.countDown(); release.await(3, TimeUnit.SECONDS);
            }
            return null;
        }).when(slow).sendMessage(any());
        try {
            handler.afterConnectionEstablished(slow);
            handler.publishRoom(Set.of("USER:7"), Map.of("content", "start"));
            assertTrue(entered.await(1, TimeUnit.SECONDS));
            for (int i = 0; i < 140; i++) handler.publishRoom(Set.of("USER:7"), Map.of("content", "queued"));
            verify(slow, timeout(1000)).close(CloseStatus.POLICY_VIOLATION);
            release.countDown();
            verify(slow, after(100).never()).sendMessage(argThat(message -> message.getPayload().toString().contains("queued")));
        } finally { release.countDown(); handler.shutdown(); }
    }

    @Test void byteLimitAndSendDeadlineCloseSocket() throws Exception {
        var handler = new SupportSocketHandler(new ObjectMapper(), tokens());
        var large = socket("bytes"); var stalled = socket("deadline");
        var release = new CountDownLatch(1); var entered = new CountDownLatch(1);
        doAnswer(invocation -> { entered.countDown(); release.await(3, TimeUnit.SECONDS); return null; }).when(stalled).sendMessage(any());
        try {
            handler.afterConnectionEstablished(large);
            handler.publishRoom(Set.of("USER:7"), Map.of("content", "x".repeat(65536)));
            verify(large, timeout(1000)).close(CloseStatus.POLICY_VIOLATION);
            handler.afterConnectionEstablished(stalled);
            assertTrue(entered.await(1, TimeUnit.SECONDS));
            @SuppressWarnings("unchecked")
            var connections = (Map<String, Object>) org.springframework.test.util.ReflectionTestUtils.getField(handler, "connections");
            org.springframework.test.util.ReflectionTestUtils.setField(connections.get("deadline"), "sendStarted",
                    System.nanoTime() - TimeUnit.SECONDS.toNanos(6));
            org.springframework.test.util.ReflectionTestUtils.invokeMethod(handler, "expire");
            verify(stalled, timeout(1000)).close(CloseStatus.POLICY_VIOLATION);
        } finally { release.countDown(); handler.shutdown(); }
    }
}
