package com.clothsell.module.mall.controller.admin.support;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.module.mall.service.support.LiveSupportService;
import com.clothsell.module.mall.service.support.LiveSupportService.Message;
import com.clothsell.module.mall.service.support.LiveSupportService.SendRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static com.clothsell.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/mall/admin/support")
public class SupportController {
    private final LiveSupportService service;
    public SupportController(LiveSupportService service) { this.service = service; }
    @GetMapping("/conversations")
    public CommonResult<List<Message>> conversations(@RequestParam(defaultValue = "1") int page) {
        return success(service.conversations(page));
    }
    @GetMapping("/messages")
    public CommonResult<List<Message>> history(@RequestParam Long userId, @RequestParam(required = false) Long before,
                                               @RequestParam(required = false) Long after) {
        return success(service.history(true, userId, before, after));
    }
    @PostMapping("/messages")
    public CommonResult<Message> send(@Valid @RequestBody SendRequest request) {
        return success(service.send(true, request));
    }
}
