package com.clothsell.module.mall.websocket;

import com.clothsell.framework.security.core.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayDeque;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SupportSocketHandler extends TextWebSocketHandler {
    private static final int MAX_PENDING_BYTES = 64 * 1024;
    private static final int MAX_PENDING_MESSAGES = 128;
    private static final long SEND_TIMEOUT_MS = 5000;
    private static class Connection {
        final WebSocketSession socket;
        final SupportTickets.Ticket ticket;
        volatile long lastPing = System.currentTimeMillis();
        final ArrayDeque<String> pending = new ArrayDeque<>();
        int pendingBytes;
        boolean sending;
        boolean closed;
        volatile long sendStarted;
        Connection(WebSocketSession socket, SupportTickets.Ticket ticket) {
            this.socket = socket;
            this.ticket = ticket;
            if (socket instanceof StandardWebSocketSession standard) {
                standard.getNativeSession().getUserProperties().put("org.apache.tomcat.websocket.BLOCKING_SEND_TIMEOUT", SEND_TIMEOUT_MS);
                standard.getNativeSession().getUserProperties().put("org.apache.tomcat.websocket.SESSION_CLOSE_TIMEOUT", 1000L);
            }
        }
        boolean admin() { return "ADMIN".equals(ticket.user().role()); }
    }
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();
    private final ObjectMapper json;
    private final TokenService tokens;
    private final ThreadPoolExecutor writers = new ThreadPoolExecutor(32, 32, 0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(10000), r -> daemon(r, "support-writer"));
    private final ThreadPoolExecutor closers = new ThreadPoolExecutor(2, 2, 0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(10000), r -> daemon(r, "support-close"));
    private static Thread daemon(Runnable task, String name) {
        Thread thread = new Thread(task, name); thread.setDaemon(true); return thread;
    }
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "support-heartbeat");
        thread.setDaemon(true);
        return thread;
    });

    public SupportSocketHandler(ObjectMapper json, TokenService tokens) {
        this.json = json;
        this.tokens = tokens;
        timer.scheduleWithFixedDelay(this::expire, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.setTextMessageSizeLimit(64);
        var ticket = (SupportTickets.Ticket) session.getAttributes().get("supportTicket");
        if (ticket == null || tokens.parse(ticket.token()) == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        long count = connections.values().stream().filter(c -> c.ticket.user().id().equals(ticket.user().id())
                && c.ticket.user().role().equals(ticket.user().role())).count();
        if (count >= 5 || connections.size() >= 10000) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        Connection connection = new Connection(session, ticket);
        connections.put(session.getId(), connection);
        send(connection, event("presence", online()));
        if (connection.admin()) presence();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Connection connection = connections.get(session.getId());
        if (connection == null) return;
        if (tokens.parse(connection.ticket.token()) == null) { close(connection); return; }
        if (!"ping".equals(message.getPayload())) { close(connection); return; }
        connection.lastPing = System.currentTimeMillis();
        send(connection, "pong");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Connection removed = connections.remove(session.getId());
        if (removed != null) discard(removed);
        if (removed != null && removed.admin()) presence();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Connection connection = connections.get(session.getId());
        if (connection != null) close(connection);
    }

    public void publish(long userId, Object message) {
        String payload = event("message", message);
        connections.values().forEach(connection -> {
            if (connection.admin() || connection.ticket.user().id().equals(userId)) send(connection, payload);
        });
    }

    public void publishRoom(java.util.Set<String> recipients, Object message) {
        String payload = event("roomMessage", message);
        connections.values().forEach(connection -> {
            String identity = connection.ticket.user().role() + ":" + connection.ticket.user().id();
            if (recipients.contains(identity)) send(connection, payload);
        });
    }

    private boolean online() {
        return connections.values().stream().anyMatch(c -> c.admin() && c.socket.isOpen()
                && tokens.parse(c.ticket.token()) != null);
    }

    private void presence() {
        String payload = event("presence", online());
        connections.values().forEach(connection -> send(connection, payload));
    }

    private String event(String type, Object data) {
        try { return json.writeValueAsString(Map.of("type", type, "data", data)); }
        catch (IOException e) { throw new IllegalStateException("无法编码客服消息", e); }
    }

    private void send(Connection connection, String payload) {
        boolean overflow = false;
        boolean start = false;
        synchronized (connection) {
            if (connection.closed) return;
            int bytes = payload.getBytes(StandardCharsets.UTF_8).length;
            if (connection.pendingBytes + bytes > MAX_PENDING_BYTES
                    || connection.pending.size() + (connection.sending ? 1 : 0) >= MAX_PENDING_MESSAGES) {
                overflow = true;
            } else {
                connection.pending.addLast(payload);
                connection.pendingBytes += bytes;
                if (!connection.sending) { connection.sending = true; start = true; }
            }
        }
        if (overflow) close(connection);
        else if (start) schedule(connection);
    }

    private void schedule(Connection connection) {
        try { writers.execute(() -> writeNext(connection)); }
        catch (java.util.concurrent.RejectedExecutionException rejected) { close(connection); }
    }

    private void writeNext(Connection connection) {
        String payload;
        synchronized (connection) {
            if (connection.closed) return;
            payload = connection.pending.pollFirst();
            if (payload == null) { connection.sending = false; return; }
            connection.sendStarted = System.nanoTime();
        }
        try {
            if (tokens.parse(connection.ticket.token()) != null && connection.socket.isOpen()) {
                connection.socket.sendMessage(new TextMessage(payload));
            }
            else close(connection);
        } catch (IOException | RuntimeException e) { close(connection); }
        finally {
            boolean next;
            synchronized (connection) {
                connection.sendStarted = 0;
                if (!connection.closed) connection.pendingBytes -= payload.getBytes(StandardCharsets.UTF_8).length;
                next = !connection.closed && !connection.pending.isEmpty();
                if (!next) connection.sending = false;
            }
            if (next) schedule(connection);
        }
    }

    private void discard(Connection connection) {
        synchronized (connection) {
            connection.closed = true;
            connection.pending.clear();
            connection.pendingBytes = 0;
        }
    }

    private void close(Connection connection) {
        boolean removed = connections.remove(connection.socket.getId(), connection);
        if (!removed) return;
        discard(connection);
        try {
            closers.execute(() -> {
                try { connection.socket.close(CloseStatus.POLICY_VIOLATION); }
                catch (IOException | RuntimeException ignored) { }
            });
        } catch (java.util.concurrent.RejectedExecutionException ignored) { }
        if (removed && connection.admin()) presence();
    }

    private void expire() {
        connections.values().forEach(connection -> {
            if (System.currentTimeMillis() - connection.lastPing > 65000
                    || (connection.sendStarted != 0 && System.nanoTime() - connection.sendStarted > TimeUnit.MILLISECONDS.toNanos(SEND_TIMEOUT_MS))
                    || tokens.parse(connection.ticket.token()) == null) close(connection);
        });
    }

    @PreDestroy
    public void shutdown() {
        timer.shutdownNow();
        connections.values().forEach(this::close);
        writers.shutdownNow();
        closers.shutdown();
    }
}
