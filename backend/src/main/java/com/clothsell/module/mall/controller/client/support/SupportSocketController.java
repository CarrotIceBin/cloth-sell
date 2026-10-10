package com.clothsell.module.mall.controller.client.support;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.module.mall.websocket.SupportTickets;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class SupportSocketController {
    private final SupportTickets tickets;
    public SupportSocketController(SupportTickets tickets) { this.tickets = tickets; }
    @PostMapping("/mall/support/ws-ticket")
    public CommonResult<Map<String, String>> issue(@RequestHeader("Authorization") String authorization) {
        String raw = authorization.startsWith("Bearer ") ? authorization.substring(7) : "";
        return CommonResult.success(Map.of("ticket", tickets.issue(raw)));
    }
}
