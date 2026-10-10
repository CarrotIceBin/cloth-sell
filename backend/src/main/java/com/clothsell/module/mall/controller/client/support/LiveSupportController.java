package com.clothsell.module.mall.controller.client.support;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.module.mall.service.support.LiveSupportService;
import com.clothsell.module.mall.service.support.LiveSupportService.Message;
import com.clothsell.module.mall.service.support.LiveSupportService.SendRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static com.clothsell.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/mall/client/support/messages")
public class LiveSupportController {
    private final LiveSupportService service;
    public LiveSupportController(LiveSupportService service) { this.service = service; }
    @GetMapping
    public CommonResult<List<Message>> history(@RequestParam(required = false) Long before,
                                               @RequestParam(required = false) Long after) {
        return success(service.history(false, null, before, after));
    }
    @PostMapping
    public CommonResult<Message> send(@Valid @RequestBody SendRequest request) {
        return success(service.send(false, request));
    }
}
