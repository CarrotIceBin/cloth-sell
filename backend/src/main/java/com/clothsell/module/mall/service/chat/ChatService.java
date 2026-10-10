package com.clothsell.module.mall.service.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clothsell.framework.common.exception.ServiceException;
import com.clothsell.framework.security.core.util.SecurityFrameworkUtils.LoginUser;
import com.clothsell.module.mall.dal.dataobject.chat.*;
import com.clothsell.module.mall.dal.mysql.chat.*;
import com.clothsell.module.mall.dal.mysql.auth.MallUserMapper;
import com.clothsell.module.mall.dal.mysql.auth.MallAdminMapper;
import com.clothsell.module.mall.websocket.SupportSocketHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {
    public record Identity(@NotNull @Positive Long id, @NotBlank @Pattern(regexp = "USER|ADMIN") String role) {
        public String key() { return role + ":" + id; }
    }
    public record CreateRequest(@NotBlank @Pattern(regexp = "DIRECT|GROUP|BROADCAST|INBOX") String type,
            @Size(max = 80) String title, @NotEmpty @Size(max = 49) List<@Valid Identity> members) {}
    public record SendRequest(@NotBlank @Pattern(regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}") String clientId,
            @NotBlank @Size(max = 2000) String content) {}
    public record Member(String id, String role, String name) {}
    public record Room(String id, String type, String title, boolean canSend, String lastContent,
                       String lastMessageId, List<Member> members) {}
    public record Message(String id, String roomId, String senderRole, String senderId, String senderName,
                          String clientId, String content, LocalDateTime createTime) {
        static Message from(ChatMessageDO row) {
            return new Message(row.getId().toString(), row.getRoomId().toString(), row.getSenderRole(),
                    row.getSenderId().toString(), row.getSenderName(), row.getClientId(), row.getContent(), row.getCreateTime());
        }
    }

    private final ChatRoomMapper rooms;
    private final ChatMemberMapper members;
    private final ChatMessageMapper messages;
    private final MallUserMapper users;
    private final MallAdminMapper admins;
    private final TransactionTemplate transactions;
    private final SupportSocketHandler sockets;

    public ChatService(ChatRoomMapper rooms, ChatMemberMapper members, ChatMessageMapper messages,
            MallUserMapper users, MallAdminMapper admins, TransactionTemplate transactions, SupportSocketHandler sockets) {
        this.rooms = rooms; this.members = members; this.messages = messages;
        this.users = users; this.admins = admins; this.transactions = transactions; this.sockets = sockets;
    }

    private Identity identity(LoginUser user) {
        if (user == null || !("ADMIN".equals(user.role()) || "USER".equals(user.role()))) throw new AccessDeniedException("请先登录");
        return new Identity(user.id(), user.role());
    }

    private ChatRoomDO requireMember(LoginUser user, long roomId) {
        Identity self = identity(user);
        var member = members.selectOne(new LambdaQueryWrapper<ChatMemberDO>().eq(ChatMemberDO::getRoomId, roomId)
                .eq(ChatMemberDO::getRole, self.role()).eq(ChatMemberDO::getAccountId, self.id()));
        if (member == null) throw new AccessDeniedException("你不是此会话成员");
        ChatRoomDO room = rooms.selectById(roomId);
        if (room == null) throw new ServiceException(404, "会话不存在");
        return room;
    }

    public static boolean canSend(ChatRoomDO room, LoginUser user) {
        return !"BROADCAST".equals(room.getType()) || (room.getOwnerRole().equals(user.role()) && room.getOwnerId().equals(user.id()));
    }

    private List<ChatMemberDO> roomMembers(long roomId) {
        return members.selectList(new LambdaQueryWrapper<ChatMemberDO>().eq(ChatMemberDO::getRoomId, roomId).orderByAsc(ChatMemberDO::getId));
    }
    private Room view(ChatRoomDO room, LoginUser user) {
        var list = roomMembers(room.getId()).stream().map(m -> new Member(m.getAccountId().toString(), m.getRole(), m.getName())).toList();
        String title = room.getTitle();
        if ("DIRECT".equals(room.getType())) title = list.stream()
                .filter(m -> !(m.id().equals(user.id().toString()) && m.role().equals(user.role())))
                .map(Member::name).findFirst().orElse(title);
        return new Room(room.getId().toString(), room.getType(), title, canSend(room, user), room.getLastContent(),
                room.getLastMessageId().toString(), list);
    }

    public List<Room> list(LoginUser user, int page) {
        Identity self = identity(user);
        if (page < 1 || page > 10000) throw new ServiceException(400, "页码无效");
        return rooms.rooms(self.role(), self.id(), (page - 1) * 50).stream().map(room -> view(room, user)).toList();
    }

    public Room get(LoginUser user, long roomId) { return view(requireMember(user, roomId), user); }

    private String accountName(Identity member) {
        if ("ADMIN".equals(member.role())) {
            var admin = admins.selectById(member.id());
            if (admin != null) return admin.getUsername();
        } else {
            var buyer = users.selectById(member.id());
            if (buyer != null) return "买家 #" + buyer.getId();
        }
        throw new ServiceException(400, "成员账号不存在");
    }

    // ponytail: 单实例串行创建避免重复私聊；多实例部署需处理数据库唯一键冲突。
    public synchronized List<Room> create(LoginUser user, CreateRequest request) {
        Identity self = identity(user);
        var selected = new LinkedHashSet<>(request.members());
        selected.remove(self);
        if (selected.isEmpty()) throw new ServiceException(400, "请选择其他成员");
        if ("DIRECT".equals(request.type()) && selected.size() != 1) throw new ServiceException(400, "私聊只能选择一个成员");
        if ("INBOX".equals(request.type()) && !"ADMIN".equals(user.role())) throw new AccessDeniedException("只有客服可以建立接待会话");
        String title = request.title() == null ? "" : request.title().trim();
        if (Set.of("GROUP", "BROADCAST").contains(request.type()) && title.isEmpty()) throw new ServiceException(400, "请填写会话名称");
        var names = new LinkedHashMap<Identity, String>();
        names.put(self, accountName(self));
        selected.forEach(member -> names.put(member, accountName(member)));
        var created = transactions.execute(status -> {
            var result = new ArrayList<ChatRoomDO>();
            if ("INBOX".equals(request.type())) {
                for (Identity recipient : selected) {
                    var pair = new LinkedHashMap<Identity, String>();
                    pair.put(self, names.get(self)); pair.put(recipient, names.get(recipient));
                    result.add(createRoom(self, "DIRECT", "", pair));
                }
            } else result.add(createRoom(self, request.type(), title, names));
            return result;
        });
        for (ChatRoomDO room : created) {
            sockets.publishRoom(recipients(room.getId()), Map.of("roomId", room.getId().toString(), "created", true));
        }
        return created.stream().map(room -> view(room, user)).toList();
    }

    private ChatRoomDO createRoom(Identity self, String type, String title, Map<Identity, String> participants) {
        String directKey = "DIRECT".equals(type) ? participants.keySet().stream().map(Identity::key).sorted().collect(Collectors.joining("|")) : null;
        if (directKey != null) {
            var previous = rooms.selectOne(new LambdaQueryWrapper<ChatRoomDO>().eq(ChatRoomDO::getDirectKey, directKey));
            if (previous != null) return previous;
        }
        var room = new ChatRoomDO();
        room.setType(type); room.setTitle(title); room.setOwnerRole(self.role()); room.setOwnerId(self.id());
        room.setDirectKey(directKey); room.setLastMessageId(0L); room.setLastContent("");
        rooms.insert(room);
        participants.forEach((participant, name) -> {
            var member = new ChatMemberDO();
            member.setRoomId(room.getId()); member.setAccountId(participant.id()); member.setRole(participant.role()); member.setName(name);
            members.insert(member);
        });
        return room;
    }

    private Set<String> recipients(long roomId) {
        return roomMembers(roomId).stream().map(member -> member.getRole() + ":" + member.getAccountId()).collect(Collectors.toSet());
    }

    public List<Message> history(LoginUser user, long roomId, Long before, Long after) {
        requireMember(user, roomId);
        if (before != null && after != null) throw new ServiceException(400, "不能同时指定两个游标");
        var query = new LambdaQueryWrapper<ChatMessageDO>().eq(ChatMessageDO::getRoomId, roomId)
                .lt(before != null, ChatMessageDO::getId, before).gt(after != null, ChatMessageDO::getId, after);
        if (after == null) query.orderByDesc(ChatMessageDO::getId); else query.orderByAsc(ChatMessageDO::getId);
        var list = messages.selectList(query.last("LIMIT 50"));
        if (after == null) Collections.reverse(list);
        return list.stream().map(Message::from).toList();
    }

    // ponytail: 单实例串行提交保持消息游标顺序；多实例需共享提交顺序与推送。
    public synchronized Message send(LoginUser user, long roomId, SendRequest request) {
        ChatRoomDO room = requireMember(user, roomId);
        if (!canSend(room, user)) throw new AccessDeniedException("群发会话只有创建者可以发送");
        var duplicate = messages.selectOne(new LambdaQueryWrapper<ChatMessageDO>().eq(ChatMessageDO::getRoomId, roomId)
                .eq(ChatMessageDO::getSenderRole, user.role()).eq(ChatMessageDO::getSenderId, user.id())
                .eq(ChatMessageDO::getClientId, request.clientId()));
        if (duplicate != null) {
            if (!duplicate.getContent().equals(request.content().trim())) throw new ServiceException(409, "消息编号已使用");
            return Message.from(duplicate);
        }
        ChatMessageDO saved = transactions.execute(status -> {
            var message = new ChatMessageDO();
            message.setRoomId(roomId); message.setSenderId(user.id()); message.setSenderRole(user.role());
            message.setSenderName("ADMIN".equals(user.role()) ? user.name() : "买家 #" + user.id());
            message.setClientId(request.clientId()); message.setContent(request.content().trim());
            messages.insert(message);
            room.setLastMessageId(message.getId()); room.setLastContent(message.getContent()); rooms.updateById(room);
            return message;
        });
        Message result = Message.from(saved);
        sockets.publishRoom(recipients(roomId), result);
        return result;
    }
}
