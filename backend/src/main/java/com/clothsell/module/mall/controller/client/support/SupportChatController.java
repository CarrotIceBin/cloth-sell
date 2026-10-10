package com.clothsell.module.mall.controller.client.support;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.module.mall.service.support.SupportChatService;
import com.clothsell.module.mall.service.support.SupportChatService.ChatReply;
import com.clothsell.module.mall.service.support.SupportChatService.ChatRequest;
import jakarta.validation.Valid;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.clothsell.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/mall/client/support")
public class SupportChatController {
    @Resource
    private SupportChatService supportChatService;

    @GetMapping("/status")
    public CommonResult<java.util.Map<String, Object>> status() {
        return success(supportChatService.getStatus());
    }

    @PostMapping("/chat")
    public CommonResult<ChatReply> chat(@Valid @RequestBody ChatRequest request) {
        return success(supportChatService.chat(request));
    }
}