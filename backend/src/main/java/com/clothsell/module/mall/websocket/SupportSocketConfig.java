package com.clothsell.module.mall.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

@Configuration
@EnableWebSocket
public class SupportSocketConfig implements WebSocketConfigurer {
    private final SupportSocketHandler handler;
    private final SupportTickets tickets;
    public SupportSocketConfig(SupportSocketHandler handler, SupportTickets tickets) {
        this.handler = handler;
        this.tickets = tickets;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/mall/support/ws").addInterceptors(new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                           WebSocketHandler wsHandler, Map<String, Object> attributes) {
                if (request.getHeaders().getOrigin() == null) {
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    return false;
                }
                String key = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("ticket");
                var ticket = tickets.consume(key);
                if (ticket == null) {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;
                }
                attributes.put("supportTicket", ticket);
                return true;
            }
            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Exception exception) { }
        });
    }
}
