package com.clothsell.module.mall.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clothsell.framework.common.exception.ServiceException;
import com.clothsell.framework.security.core.util.SecurityFrameworkUtils;
import com.clothsell.framework.security.core.util.SecurityFrameworkUtils.LoginUser;
import com.clothsell.module.mall.dal.dataobject.support.SupportMessageDO;
import com.clothsell.module.mall.dal.mysql.support.SupportMessageMapper;
import com.clothsell.module.mall.websocket.SupportSocketHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class LiveSupportService {
    public record SendRequest(Long userId,
            @NotBlank @Pattern(regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}") String clientId,
            @NotBlank @Size(max = 2000) String content) {}
    public record Message(String id, String userId, String buyerName, String senderRole, String senderName,
                          String clientId, String content, LocalDateTime createTime) {
        public static Message from(SupportMessageDO row) {
            return new Message(row.getId().toString(), row.getUserId().toString(), row.getBuyerName(), row.getSenderRole(),
                    row.getSenderName(), row.getClientId(), row.getContent(), row.getCreateTime());
        }
    }
    private final SupportMessageMapper mapper;
    private final TransactionTemplate transactions;
    private final SupportSocketHandler sockets;

    public LiveSupportService(SupportMessageMapper mapper, TransactionTemplate transactions, SupportSocketHandler sockets) {
        this.mapper = mapper;
        this.transactions = transactions;
        this.sockets = sockets;
    }

    public static LoginUser current(boolean admin) {
        Long id = admin ? SecurityFrameworkUtils.getAuthId() : SecurityFrameworkUtils.getLoginUserId();
        if (id == null) throw new AccessDeniedException("没有权限");
        return new LoginUser(id, admin ? "ADMIN" : "USER", SecurityFrameworkUtils.getUserName());
    }

    public static long conversation(LoginUser user, Long requested) {
        if (!"ADMIN".equals(user.role())) return user.id();
        if (requested == null || requested < 1) throw new ServiceException(400, "请选择买家会话");
        return requested;
    }

    public List<Message> history(boolean admin, Long userId, Long before, Long after) {
        long owner = conversation(current(admin), userId);
        if (before != null && after != null) throw new ServiceException(400, "不能同时指定两个游标");
        var query = new LambdaQueryWrapper<SupportMessageDO>().eq(SupportMessageDO::getUserId, owner)
                .lt(before != null, SupportMessageDO::getId, before)
                .gt(after != null, SupportMessageDO::getId, after);
        if (after == null) query.orderByDesc(SupportMessageDO::getId);
        else query.orderByAsc(SupportMessageDO::getId);
        var rows = mapper.selectList(query.last("LIMIT 50"));
        if (after == null) Collections.reverse(rows);
        return rows.stream().map(Message::from).toList();
    }

    public List<Message> conversations(int page) {
        current(true);
        if (page < 1 || page > 10000) throw new ServiceException(400, "页码无效");
        return mapper.conversations((page - 1) * 50).stream().map(Message::from).toList();
    }

    // ponytail: 单实例串行提交保证游标顺序；多实例时改用共享消息分发和提交游标。
    public synchronized Message send(boolean admin, SendRequest request) {
        LoginUser user = current(admin);
        long owner = conversation(user, request.userId());
        var query = new LambdaQueryWrapper<SupportMessageDO>().eq(SupportMessageDO::getUserId, owner)
                .eq(SupportMessageDO::getSenderId, user.id()).eq(SupportMessageDO::getSenderRole, user.role())
                .eq(SupportMessageDO::getClientId, request.clientId());
        SupportMessageDO existing = mapper.selectOne(query);
        if (existing != null) {
            if (!existing.getContent().equals(request.content().trim())) throw new ServiceException(409, "消息编号已使用");
            return Message.from(existing);
        }
        if (admin && mapper.selectCount(new LambdaQueryWrapper<SupportMessageDO>()
                .eq(SupportMessageDO::getUserId, owner)) == 0) {
            throw new ServiceException(400, "买家尚未发起会话");
        }
        SupportMessageDO saved = transactions.execute(status -> {
            var row = new SupportMessageDO();
            row.setUserId(owner);
            row.setSenderId(user.id());
            row.setSenderRole(user.role());
            row.setSenderName(user.name());
            row.setClientId(request.clientId());
            row.setContent(request.content().trim());
            mapper.insert(row);
            return row;
        });
        Message message = Message.from(saved);
        sockets.publish(owner, message);
        return message;
    }
}
