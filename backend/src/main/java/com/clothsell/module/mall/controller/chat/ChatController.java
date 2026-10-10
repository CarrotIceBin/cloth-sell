package com.clothsell.module.mall.controller.chat;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.framework.security.core.util.SecurityFrameworkUtils.LoginUser;
import com.clothsell.module.mall.service.chat.ChatService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import static com.clothsell.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/mall/chat")
public class ChatController {
    private final ChatService service;
    public ChatController(ChatService service) { this.service = service; }
    @GetMapping("/me")
    public CommonResult<Map<String, String>> me(@AuthenticationPrincipal LoginUser user) {
        return success(Map.of("id", user.id().toString(), "role", user.role()));
    }
    @GetMapping("/rooms")
    public CommonResult<List<ChatService.Room>> rooms(@AuthenticationPrincipal LoginUser user, @RequestParam(defaultValue = "1") int page) {
        return success(service.list(user, page));
    }
    @PostMapping("/rooms")
    public CommonResult<List<ChatService.Room>> create(@AuthenticationPrincipal LoginUser user, @Valid @RequestBody ChatService.CreateRequest request) {
        return success(service.create(user, request));
    }
    @GetMapping("/rooms/{roomId}")
    public CommonResult<ChatService.Room> room(@AuthenticationPrincipal LoginUser user, @PathVariable long roomId) {
        return success(service.get(user, roomId));
    }
    @GetMapping("/rooms/{roomId}/messages")
    public CommonResult<List<ChatService.Message>> history(@AuthenticationPrincipal LoginUser user, @PathVariable long roomId,
            @RequestParam(required = false) Long before, @RequestParam(required = false) Long after) {
        return success(service.history(user, roomId, before, after));
    }
    @PostMapping("/rooms/{roomId}/messages")
    public CommonResult<ChatService.Message> send(@AuthenticationPrincipal LoginUser user, @PathVariable long roomId,
            @Valid @RequestBody ChatService.SendRequest request) {
        return success(service.send(user, roomId, request));
    }
}
