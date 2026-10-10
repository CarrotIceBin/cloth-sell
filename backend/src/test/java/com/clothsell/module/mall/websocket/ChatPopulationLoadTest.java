package com.clothsell.module.mall.websocket;

import com.clothsell.module.mall.dal.dataobject.auth.MallUserDO;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/** 100,000 distinct HTTP senders; deliberately does not claim 100,000 open sockets. */
class ChatPopulationLoadTest extends ChatLoadTest {
    @Override @Test void measureLoadAndSlowConnectionImpact() throws Exception {
        int population = 100_000;
        when(users.selectById(anyLong())).thenAnswer(invocation -> {
            long id = invocation.getArgument(0);
            if (!Set.of(1L, 7L, 8L, 9L, 10L).contains(id) && (id < 100_000 || id >= 200_000)) return null;
            var user = new MallUserDO(); user.setId(id); user.setPhone("population-test-only"); return user;
        });
        var rooms = new ArrayList<String>();
        var senders = new ArrayList<String>();
        for (int offset = 0; offset < population; offset += 1000) {
            var roomRows = new ArrayList<Object[]>();
            var memberRows = new ArrayList<Object[]>();
            for (int i = offset; i < offset + 1000; i++) {
                long id = 100_000L + i;
                rooms.add(Long.toString(id)); senders.add(account(id, "USER"));
                roomRows.add(new Object[]{id, "population fixture", "ADMIN:1|USER:" + id});
                memberRows.add(new Object[]{id, "ADMIN", 1L, "support"});
                memberRows.add(new Object[]{id, "USER", id, "test-" + id});
            }
            database.batchUpdate("INSERT INTO mall_chat_room(id,type,title,owner_role,owner_id,direct_key) VALUES(?,'DIRECT',?,'ADMIN',1,?)", roomRows);
            database.batchUpdate("INSERT INTO mall_chat_member(room_id,role,account_id,name) VALUES(?,?,?,?)", memberRows);
        }
        System.out.println("CHAT_POPULATION fixtures ready: 100000 users, 100000 private rooms, 200 concurrent HTTP senders, no load sockets");
        var result = run("100000_distinct_users_private_messages", rooms, senders, population, 200, 0);
        var report = new LinkedHashMap<String, Object>();
        report.put("logical_users", population);
        report.put("load_websocket_connections", 0);
        report.put("application_websocket_connection_limit", 10000);
        report.put("database", "isolated in-memory H2; account lookup mocked; private-room fixtures seeded directly");
        report.put("scope", "closed-loop authenticated HTTP sends, 200 workers; latency excludes client executor queue; no WebSocket delivery measurement");
        report.put("result", result);
        Path output = Path.of("target/chat-population-results.json");
        Files.writeString(output, json.writerWithDefaultPrettyPrinter().writeValueAsString(report));
    }
}
