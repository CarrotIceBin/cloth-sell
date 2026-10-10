package com.clothsell.module.mall.websocket;

import com.clothsell.module.mall.dal.dataobject.auth.MallUserDO;
import com.clothsell.module.mall.dal.mysql.auth.MallUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Opt-in, isolated H2 load test; no connections to the running business database. */
@EnabledIfSystemProperty(named = "chat.load", matches = "true")
class ChatLoadTest extends SupportIntegrationTest {
    @Autowired MallUserMapper users;
    @Autowired SupportSocketHandler handler;
    @Autowired org.springframework.jdbc.core.JdbcTemplate database;
    private final Map<String, Long> starts = new ConcurrentHashMap<>();
    private final Map<String, Long> deliveries = new ConcurrentHashMap<>();
    private final List<Map<String, Object>> results = new ArrayList<>();

    class LoadInbox extends TextWebSocketHandler {
        final String identity;
        LoadInbox(String identity) { this.identity = identity; }
        @Override protected void handleTextMessage(WebSocketSession socket, TextMessage message) throws Exception {
            if (!message.getPayload().startsWith("{")) return;
            var event = json.readTree(message.getPayload());
            if (!"roomMessage".equals(event.path("type").asText())) return;
            String uuid = event.path("data").path("clientId").asText();
            Long start = starts.get(uuid);
            if (start != null) deliveries.putIfAbsent(identity + ":" + uuid, System.nanoTime() - start);
        }
    }
    WebSocketSession open(String token, String identity) throws Exception {
        var headers = new WebSocketHttpHeaders(); headers.setOrigin("http://localhost:" + port);
        return new org.springframework.web.socket.client.standard.StandardWebSocketClient()
                .execute(new LoadInbox(identity), headers, java.net.URI.create("ws://localhost:" + port + "/mall/support/ws?ticket=" + ticket(token)))
                .get(10, TimeUnit.SECONDS);
    }
    long percentile(List<Long> values, double fraction) {
        if (values.isEmpty()) return -1;
        var sorted = new ArrayList<>(values); Collections.sort(sorted);
        return TimeUnit.NANOSECONDS.toMillis(sorted.get(Math.max(0, (int)Math.ceil(sorted.size() * fraction) - 1)));
    }

    Map<String, Object> run(String name, List<String> roomIds, List<String> senders, int count,
            int concurrency, int recipientsPerMessage) throws Exception {
        starts.clear(); deliveries.clear();
        long before = database.queryForObject("SELECT COUNT(*) FROM mall_chat_message", Long.class);
        var latencies = Collections.synchronizedList(new ArrayList<Long>());
        var perRoom = new ConcurrentHashMap<String, List<Long>>();
        var failures = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(concurrency);
        var gate = new CountDownLatch(1);
        long wallStart = System.nanoTime();
        var futures = new ArrayList<Future<?>>();
        try {
            for (int i = 0; i < count; i++) {
                final int index = i;
                futures.add(executor.submit(() -> {
                    try {
                        gate.await();
                        String uuid = UUID.randomUUID().toString();
                        long start = System.nanoTime(); starts.put(uuid, start);
                        String roomId = roomIds.get(index % roomIds.size());
                        var response = roomSend(senders.get(index % senders.size()), roomId, uuid,
                                "load-" + index + "-" + "x".repeat(180));
                        if (response.path("code").asInt(-1) != 0) failures.incrementAndGet();
                        long elapsed = System.nanoTime() - start;
                        latencies.add(elapsed);
                        perRoom.computeIfAbsent(roomId, key -> Collections.synchronizedList(new ArrayList<>())).add(elapsed);
                    } catch (Exception failure) { failures.incrementAndGet(); }
                }));
            }
            wallStart = System.nanoTime(); gate.countDown();
            for (var future : futures) future.get(120, TimeUnit.SECONDS);
        } finally { executor.shutdownNow(); }
        double seconds = (System.nanoTime() - wallStart) / 1_000_000_000.0;
        int expected = (count - failures.get()) * recipientsPerMessage;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (deliveries.size() < expected && System.nanoTime() < deadline) Thread.sleep(20);
        var row = new LinkedHashMap<String, Object>();
        row.put("scenario", name); row.put("requests", count); row.put("concurrent_senders", concurrency);
        row.put("seconds", Math.round(seconds * 100.0) / 100.0);
        row.put("requests_per_second", Math.round(count / seconds * 100.0) / 100.0);
        row.put("http_p50_ms", percentile(latencies, .50)); row.put("http_p95_ms", percentile(latencies, .95));
        row.put("http_p99_ms", percentile(latencies, .99)); row.put("http_max_ms", percentile(latencies, 1));
        row.put("errors", failures.get()); row.put("expected_pushes", expected); row.put("received_pushes", deliveries.size());
        long persisted = database.queryForObject("SELECT COUNT(*) FROM mall_chat_message", Long.class) - before;
        row.put("persisted_messages", persisted);
        row.put("push_p95_ms", percentile(new ArrayList<>(deliveries.values()), .95));
        if (roomIds.size() == 2) {
            var roomP95 = new LinkedHashMap<String, Long>();
            for (String roomId : roomIds) roomP95.put(roomId, percentile(perRoom.getOrDefault(roomId, List.of()), .95));
            row.put("room_p95_ms", roomP95);
        }
        results.add(row); System.out.println("CHAT_LOAD " + json.writeValueAsString(row));
        assertEquals(0, failures.get(), name + " HTTP errors");
        assertEquals(count - failures.get(), persisted, name + " missing database messages");
        assertEquals(expected, deliveries.size(), name + " missed WebSocket deliveries");
        return row;
    }

    @Test void measureLoadAndSlowConnectionImpact() throws Exception {
        when(users.selectById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            if (!Set.of(1L, 7L, 8L, 9L, 10L).contains(id) && (id < 1000 || id > 2048)) return null;
            var user = new MallUserDO(); user.setId(id); user.setPhone("load-test-only"); return user;
        });
        String admin = account(1, "ADMIN");
        for (int clients : new int[]{10, 50, 100}) {
            var sockets = new ArrayList<WebSocketSession>();
            var rooms = new ArrayList<String>(); var senders = new ArrayList<String>();
            try {
                sockets.add(open(admin, "admin"));
                for (int i = 0; i < clients; i++) {
                    long id = 1000 + i; String buyer = account(id, "USER");
                    rooms.add(createRoom(admin, "DIRECT", List.of(participant(id, "USER"))).get(0).path("id").asText());
                    senders.add(buyer); sockets.add(open(buyer, "buyer-" + id));
                }
                for (int i = 0; i < 20; i++) roomSend(senders.get(0), rooms.get(0), UUID.randomUUID().toString(), "warmup");
                run("direct_" + clients + "_buyers", rooms, senders, clients == 10 ? 200 : clients == 50 ? 500 : 1000,
                        clients == 10 ? 10 : clients == 50 ? 25 : 50, 2);
                if (clients == 100) run("direct_100_buyers_30000_messages", rooms, senders, 30000, 50, 2);
            } finally { for (var socket : sockets) socket.close(); }
        }
        var groupSockets = new ArrayList<WebSocketSession>();
        try {
            groupSockets.add(open(admin, "admin-group"));
            var participants = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < 49; i++) {
                long id = 2000 + i; participants.add(participant(id, "USER"));
                groupSockets.add(open(account(id, "USER"), "group-" + id));
            }
            String room = createRoom(admin, "BROADCAST", participants).get(0).path("id").asText();
            run("broadcast_50_members", List.of(room), List.of(admin), 300, 20, 50);
        } finally { for (var socket : groupSockets) socket.close(); }

        String slowRoom = createRoom(admin, "DIRECT", List.of(participant(1000, "USER"))).get(0).path("id").asText();
        String normalRoom = createRoom(admin, "DIRECT", List.of(participant(1001, "USER"))).get(0).path("id").asText();
        Map<String, Object> baseline = run("two_rooms_baseline", List.of(slowRoom, normalRoom), List.of(admin), 50, 10, 0);
        var slow = mock(WebSocketSession.class);
        String slowToken = account(1000, "USER");
        when(slow.getId()).thenReturn("synthetic-slow-connection"); when(slow.isOpen()).thenReturn(true);
        when(slow.getAttributes()).thenReturn(Map.of("supportTicket", new SupportTickets.Ticket(tokens.parse(slowToken), slowToken, Instant.now().plusSeconds(60))));
        doAnswer(invocation -> { Thread.sleep(250); return null; }).when(slow).sendMessage(any());
        handler.afterConnectionEstablished(slow);
        try {
            Map<String, Object> impact = run("two_rooms_with_250ms_slow_socket", List.of(slowRoom, normalRoom), List.of(admin), 50, 10, 0);
            impact.put("synthetic_send_delay_ms", 250);
            impact.put("baseline_p95_ms", baseline.get("http_p95_ms"));
        } finally { handler.afterConnectionClosed(slow, CloseStatus.NORMAL); }
        var report = new LinkedHashMap<String, Object>();
        report.put("database", "isolated in-memory H2 PostgreSQL mode");
        report.put("java", System.getProperty("java.version")); report.put("cpu_processors", Runtime.getRuntime().availableProcessors());
        report.put("max_heap_mb", Runtime.getRuntime().maxMemory() / 1024 / 1024);
        report.put("scope", "loopback HTTP + real WebSocket, same JVM client/server, mocked account lookup; slow socket uses synthetic 250ms blocking IO");
        report.put("results", results);
        Path output = Path.of("target/chat-load-results.json"); Files.createDirectories(output.getParent());
        Files.writeString(output, json.writerWithDefaultPrettyPrinter().writeValueAsString(report));
    }
}
