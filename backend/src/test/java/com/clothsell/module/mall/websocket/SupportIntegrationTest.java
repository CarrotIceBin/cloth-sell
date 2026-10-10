package com.clothsell.module.mall.websocket;

import com.clothsell.framework.mybatis.config.AuditFillHandler;
import com.clothsell.framework.security.config.SecurityConfig;
import com.clothsell.framework.security.config.TokenAuthFilter;
import com.clothsell.framework.security.core.TokenService;
import com.clothsell.framework.security.core.util.SecurityFrameworkUtils.LoginUser;
import com.clothsell.framework.web.GlobalExceptionHandler;
import com.clothsell.module.mall.controller.admin.support.SupportController;
import com.clothsell.module.mall.controller.client.support.LiveSupportController;
import com.clothsell.module.mall.controller.client.support.SupportSocketController;
import com.clothsell.module.mall.service.support.LiveSupportService;
import com.clothsell.module.mall.service.chat.ChatService;
import com.clothsell.module.mall.controller.chat.ChatController;
import com.clothsell.module.mall.dal.mysql.auth.MallUserMapper;
import com.clothsell.module.mall.dal.mysql.auth.MallAdminMapper;
import com.clothsell.module.mall.dal.dataobject.auth.MallUserDO;
import com.clothsell.module.mall.dal.dataobject.auth.MallAdminDO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SupportIntegrationTest.TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = { "spring.datasource.url=jdbc:h2:mem:support;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver", "spring.sql.init.mode=always",
                "spring.sql.init.schema-locations=classpath:support-schema.sql" })
class SupportIntegrationTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @MapperScan({"com.clothsell.module.mall.dal.mysql.support", "com.clothsell.module.mall.dal.mysql.chat"})
    @Import({LiveSupportService.class, LiveSupportController.class, SupportController.class, SupportSocketController.class,
            SupportTickets.class, SupportSocketHandler.class, SupportSocketConfig.class, SecurityConfig.class,
            TokenAuthFilter.class, AuditFillHandler.class, GlobalExceptionHandler.class, ChatService.class, ChatController.class})
    static class TestApp {
        @Bean TokenService tokens() { return new TokenService("integration-test-only-secret-at-least-32-characters"); }
        @Bean MallUserMapper users() {
            var mapper = org.mockito.Mockito.mock(MallUserMapper.class);
            for (long id : new long[]{1, 7, 8, 9, 10}) {
                var user = new MallUserDO(); user.setId(id); user.setPhone("test-only");
                org.mockito.Mockito.when(mapper.selectById(id)).thenReturn(user);
            }
            return mapper;
        }
        @Bean MallAdminMapper admins() {
            var mapper = org.mockito.Mockito.mock(MallAdminMapper.class);
            for (long id : new long[]{1, 2}) {
                var admin = new MallAdminDO(); admin.setId(id); admin.setUsername("support-" + id);
                org.mockito.Mockito.when(mapper.selectById(id)).thenReturn(admin);
            }
            return mapper;
        }
    }
    @Autowired TestRestTemplate http;
    @Autowired TokenService tokens;
    @Autowired ObjectMapper json;
    @LocalServerPort int port;

    ResponseEntity<JsonNode> call(HttpMethod method, String path, String token, Object body) {
        var headers = new HttpHeaders();
        if (token != null) headers.setBearerAuth(token);
        return http.exchange(path, method, new HttpEntity<>(body, headers), JsonNode.class);
    }
    String ticket(String token) {
        var response = call(HttpMethod.POST, "/mall/support/ws-ticket", token, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody().path("data").path("ticket").asText();
    }
    static class Inbox extends TextWebSocketHandler {
        final LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
        @Override protected void handleTextMessage(WebSocketSession session, TextMessage message) { messages.add(message.getPayload()); }
    }
    WebSocketSession connect(String key, Inbox inbox) throws Exception {
        return connect(key, inbox, "http://localhost:" + port);
    }
    WebSocketSession connect(String key, Inbox inbox, String origin) throws Exception {
        var headers = new WebSocketHttpHeaders();
        headers.setOrigin(origin);
        return new StandardWebSocketClient().execute(inbox, headers,
                URI.create("ws://localhost:" + port + "/mall/support/ws?ticket=" + key)).get(5, TimeUnit.SECONDS);
    }
    JsonNode nextMessage(Inbox inbox) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            String payload = inbox.messages.poll(100, TimeUnit.MILLISECONDS);
            if (payload != null && payload.startsWith("{")) {
                var event = json.readTree(payload);
                if ("message".equals(event.path("type").asText())) return event.path("data");
            }
        }
        fail("No real-time message received");
        return null;
    }

    String account(long id, String role) { return tokens.issueAccess(new LoginUser(id, role, "test-" + id)); }
    JsonNode createRoom(String token, String type, Object participants) {
        var response = call(HttpMethod.POST, "/mall/chat/rooms", token,
                Map.of("type", type, "title", "test room", "members", participants));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().path("code").asInt(), response.getBody().toString());
        return response.getBody().path("data");
    }
    Map<String, Object> participant(long id, String role) { return Map.of("id", id, "role", role); }
    JsonNode roomSend(String token, String roomId, String uuid, String content) {
        return call(HttpMethod.POST, "/mall/chat/rooms/" + roomId + "/messages", token,
                Map.of("clientId", uuid, "content", content)).getBody();
    }

    @Test void directChatReusesPairAndIsolatesUnrelatedBuyersAndAdmins() {
        String buyer = account(7, "USER"), admin = account(2, "ADMIN"), unrelated = account(9, "USER");
        String room = createRoom(buyer, "DIRECT", java.util.List.of(participant(2, "ADMIN"))).get(0).path("id").asText();
        assertEquals(room, createRoom(admin, "DIRECT", java.util.List.of(participant(7, "USER"))).get(0).path("id").asText());
        String uuid = UUID.randomUUID().toString();
        var sent = roomSend(buyer, room, uuid, "private question");
        assertEquals(0, sent.path("code").asInt());
        assertEquals(sent.path("data").path("id"), roomSend(buyer, room, uuid, "private question").path("data").path("id"));
        assertEquals(409, roomSend(buyer, room, uuid, "different question").path("code").asInt());
        assertEquals(0, roomSend(admin, room, UUID.randomUUID().toString(), "private reply").path("code").asInt());
        assertEquals(HttpStatus.FORBIDDEN, call(HttpMethod.GET, "/mall/chat/rooms/" + room + "/messages", unrelated, null).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, call(HttpMethod.GET, "/mall/chat/rooms/" + room, account(1, "ADMIN"), null).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, call(HttpMethod.POST, "/mall/chat/rooms/" + room + "/messages", unrelated,
                Map.of("clientId", UUID.randomUUID().toString(), "content", "unauthorized")).getStatusCode());
        assertEquals(2, call(HttpMethod.GET, "/mall/chat/rooms/" + room + "/messages", buyer, null).getBody().path("data").size());
    }

    @Test void broadcastAllowsOnlyCreatorAndManyToOneCreatesSeparatePrivateRooms() {
        String admin = account(1, "ADMIN"), buyer = account(8, "USER");
        var participants = java.util.List.of(participant(8, "USER"), participant(10, "USER"));
        String broadcast = createRoom(admin, "BROADCAST", participants).get(0).path("id").asText();
        assertEquals(0, roomSend(admin, broadcast, UUID.randomUUID().toString(), "announcement").path("code").asInt());
        assertEquals(HttpStatus.FORBIDDEN, call(HttpMethod.POST, "/mall/chat/rooms/" + broadcast + "/messages", buyer,
                Map.of("clientId", UUID.randomUUID().toString(), "content", "reply denied")).getStatusCode());
        assertFalse(call(HttpMethod.GET, "/mall/chat/rooms/" + broadcast, buyer, null).getBody().path("data").path("canSend").asBoolean());
        var inbox = createRoom(admin, "INBOX", participants);
        assertEquals(2, inbox.size());
        assertNotEquals(inbox.get(0).path("id"), inbox.get(1).path("id"));
        for (var room : inbox) assertEquals(2, room.path("members").size());
        assertEquals(HttpStatus.FORBIDDEN, call(HttpMethod.GET, "/mall/chat/rooms/" + inbox.get(1).path("id").asText() + "/messages", buyer, null).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, call(HttpMethod.POST, "/mall/chat/rooms", buyer,
                Map.of("type", "INBOX", "title", "", "members", participants)).getStatusCode());
    }

    @Test void groupChatAllowsAllMembersAndRejectsInvalidMembership() {
        String buyer = account(7, "USER"), other = account(8, "USER"), admin = account(1, "ADMIN");
        String room = createRoom(buyer, "GROUP", java.util.List.of(participant(8, "USER"), participant(1, "ADMIN")))
                .get(0).path("id").asText();
        for (String token : new String[]{buyer, other, admin}) {
            assertEquals(0, roomSend(token, room, UUID.randomUUID().toString(), "group reply").path("code").asInt());
        }
        assertEquals(3, call(HttpMethod.GET, "/mall/chat/rooms/" + room + "/messages", other, null).getBody().path("data").size());
        assertEquals(400, call(HttpMethod.POST, "/mall/chat/rooms", buyer,
                Map.of("type", "DIRECT", "members", java.util.List.of(participant(999999, "USER")))).getBody().path("code").asInt());
        assertEquals(400, call(HttpMethod.POST, "/mall/chat/rooms", buyer,
                Map.of("type", "DIRECT", "members", java.util.List.of(participant(7, "USER")))).getBody().path("code").asInt());
        assertEquals(400, call(HttpMethod.POST, "/mall/chat/rooms/" + room + "/messages", buyer,
                Map.of("clientId", "invalid", "content", " ")).getBody().path("code").asInt());
    }

    @Test void groupPushReachesMembersOnlyEvenWhenAccountIdsOverlapAcrossRoles() throws Exception {
        String owner = account(1, "ADMIN"), buyer = account(1, "USER"), member = account(10, "USER"), outsider = account(9, "USER");
        String room = createRoom(owner, "GROUP", java.util.List.of(participant(1, "USER"), participant(10, "USER"))).get(0).path("id").asText();
        var inbox1 = new Inbox(); var inbox2 = new Inbox(); var inbox3 = new Inbox();
        var socket1 = connect(ticket(buyer), inbox1); var socket2 = connect(ticket(member), inbox2); var socket3 = connect(ticket(outsider), inbox3);
        try {
            var saved = roomSend(owner, room, UUID.randomUUID().toString(), "members only");
            assertEquals(0, saved.path("code").asInt());
            for (Inbox inbox : new Inbox[]{inbox1, inbox2}) {
                boolean found = false;
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (System.nanoTime() < deadline && !found) {
                    String payload = inbox.messages.poll(100, TimeUnit.MILLISECONDS);
                    if (payload != null && payload.startsWith("{")) {
                        var event = json.readTree(payload);
                        found = "roomMessage".equals(event.path("type").asText()) && room.equals(event.path("data").path("roomId").asText());
                    }
                }
                assertTrue(found, "Member did not receive group message");
            }
            Thread.sleep(100);
            for (String payload : inbox3.messages) assertFalse(payload.contains("members only"));
        } finally { socket1.close(); socket2.close(); socket3.close(); }
    }

    @Test void chatPersistsPushesBothWaysAndEnforcesBuyerIsolation() throws Exception {
        String buyer = tokens.issueAccess(new LoginUser(7L, "USER", "buyer"));
        String other = tokens.issueAccess(new LoginUser(8L, "USER", "other"));
        String admin = tokens.issueAccess(new LoginUser(1L, "ADMIN", "support"));
        assertEquals(HttpStatus.UNAUTHORIZED, call(HttpMethod.GET, "/mall/client/support/messages", null, null).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, call(HttpMethod.GET, "/mall/admin/support/conversations", buyer, null).getStatusCode());
        assertThrows(Exception.class, () -> connect("invalid", new Inbox()));
        assertThrows(Exception.class, () -> connect(ticket(buyer), new Inbox(), "https://untrusted.example"));
        var buyerInbox = new Inbox();
        var adminInbox = new Inbox();
        String buyerTicket = ticket(buyer);
        var buyerSocket = connect(buyerTicket, buyerInbox);
        var adminSocket = connect(ticket(admin), adminInbox);
        try {
            assertThrows(Exception.class, () -> connect(buyerTicket, new Inbox()));
            var request = Map.of("clientId", UUID.randomUUID().toString(), "content", "hello", "userId", 8);
            var saved = call(HttpMethod.POST, "/mall/client/support/messages", buyer, request).getBody();
            assertEquals(0, saved.path("code").asInt());
            String id = saved.path("data").path("id").asText();
            assertEquals("7", saved.path("data").path("userId").asText());
            assertEquals(id, nextMessage(buyerInbox).path("id").asText());
            assertEquals(id, nextMessage(adminInbox).path("id").asText());
            assertEquals(id, call(HttpMethod.POST, "/mall/client/support/messages", buyer, request)
                    .getBody().path("data").path("id").asText());
            assertEquals(0, call(HttpMethod.GET, "/mall/client/support/messages?userId=7", other, null).getBody().path("data").size());
            var reply = call(HttpMethod.POST, "/mall/admin/support/messages", admin,
                    Map.of("clientId", UUID.randomUUID().toString(), "content", "how can I help", "userId", 7)).getBody();
            assertEquals(0, reply.path("code").asInt());
            assertEquals("ADMIN", nextMessage(buyerInbox).path("senderRole").asText());
            assertEquals(2, call(HttpMethod.GET, "/mall/client/support/messages", buyer, null).getBody().path("data").size());
            assertEquals(1, call(HttpMethod.GET, "/mall/client/support/messages?after=" + id, buyer, null).getBody().path("data").size());
            assertEquals(1, call(HttpMethod.GET, "/mall/admin/support/conversations", admin, null).getBody().path("data").size());
            buyerSocket.close();
            call(HttpMethod.POST, "/mall/admin/support/messages", admin,
                    Map.of("clientId", UUID.randomUUID().toString(), "content", "offline reply", "userId", 7));
            assertEquals(3, call(HttpMethod.GET, "/mall/client/support/messages", buyer, null).getBody().path("data").size());
        } finally { buyerSocket.close(); adminSocket.close(); }
    }
}
