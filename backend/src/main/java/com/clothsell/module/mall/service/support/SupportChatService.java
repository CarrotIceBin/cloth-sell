package com.clothsell.module.mall.service.support;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public interface SupportChatService {
    Map<String, Object> getStatus();

    ChatReply chat(@Valid ChatRequest request);

    record ChatRequest(@NotEmpty @Size(max = 12) List<@Valid ChatMessage> messages) {}

    record ChatMessage(@NotBlank @Pattern(regexp = "user|assistant", message = "消息角色无效") String role,
                       @NotBlank @Size(max = 2000) String content) {}

    record ChatReply(String reply, boolean aiEnabled) {}
}
