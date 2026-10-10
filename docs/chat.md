# 聊天中心

买家登录后打开 `/chat`，管理员打开 `/admin/chat`。页面显示自己的聊天账号类型和编号，新建会话时向对方索取编号，再选择买家 / 客服和填写编号。不会公开其他买家的手机号或提供全站用户目录。

## 四种聊天关系

| 方式 | 行为 |
| --- | --- |
| 一对一私聊 | 两个成员双方收发；同一对账号再次创建时复用原会话 |
| 一对多群发 | 创建者发送给选定成员，接收者只能阅读 |
| 多对一接待 | 管理员批量选择买家，各建立独立的一对一会话，买家彼此不可见 |
| 多对多群聊 | 创建者和选定成员共同收发 |

会话最多 50 人；一条消息最多 2000 字。成员在创建时确定，第一版不提供退出、邀请新成员、解散、撤回、图片或已读回执。旧 `/help` 人工客服与 `/admin/support` 的会话、消息和行为保持不变。

## 数据结构（PostgreSQL）

新增三张表，不修改原客服表。开发环境由本次接入创建；其他部署环境须执行：

```sql
CREATE TABLE IF NOT EXISTS mall_chat_room (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(16) NOT NULL,
    title VARCHAR(80) NOT NULL,
    owner_role VARCHAR(16) NOT NULL,
    owner_id BIGINT NOT NULL,
    direct_key VARCHAR(128) UNIQUE,
    last_message_id BIGINT NOT NULL DEFAULT 0,
    last_content VARCHAR(2000) NOT NULL DEFAULT '',
    creator VARCHAR(255) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(255) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS mall_chat_member (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES mall_chat_room(id),
    role VARCHAR(16) NOT NULL,
    account_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    creator VARCHAR(255) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(255) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    UNIQUE(room_id, role, account_id)
);
CREATE INDEX IF NOT EXISTS idx_chat_member_account ON mall_chat_member(role, account_id, room_id);
CREATE TABLE IF NOT EXISTS mall_chat_message (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES mall_chat_room(id),
    sender_role VARCHAR(16) NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_name VARCHAR(255) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    creator VARCHAR(255) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(255) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    UNIQUE(room_id, sender_role, sender_id, client_id)
);
CREATE INDEX IF NOT EXISTS idx_chat_message_room ON mall_chat_message(room_id, id);
```

MySQL 将三个 `BIGSERIAL` 主键换成 `BIGINT AUTO_INCREMENT`，去掉索引语句中的 `IF NOT EXISTS`，使用 `utf8mb4`。MySQL 外键使用显式的 `FOREIGN KEY (room_id) REFERENCES mall_chat_room(id)`，不要依赖列上的内联 REFERENCES。

## 权限与通信

接口均要求登录，身份来自现有 Bearer Token。管理员也必须是会话成员，无法通过管理员身份读取任意聊天。查询成员、历史和发送前均检查成员资格；群发额外检查创建者的角色与编号。账号编号在响应中使用字符串，角色与编号共同区分身份，避免买家与管理员相同数字编号混淆。

- `GET /mall/chat/me`：自己的角色和编号。
- `GET /mall/chat/rooms?page=1`：自己的会话，每页 50 个。
- `POST /mall/chat/rooms`：`{type,title,members:[{role,id}]}`；type 为 DIRECT、GROUP、BROADCAST 或 INBOX，INBOX 仅管理员可用且返回多个独立 DIRECT 会话。
- `GET /mall/chat/rooms/{roomId}`：会话及成员。
- `GET /mall/chat/rooms/{roomId}/messages?before=...`：历史，或使用 after 补取断线期间新消息，每页 50 条。
- `POST /mall/chat/rooms/{roomId}/messages`：`{clientId,content}`，clientId 为 UUID；重试原消息使用相同 UUID，冲突内容拒绝。

复用原客服的短期一次性票据、同源校验、心跳和重连 WebSocket 连接。新增 `roomMessage` 事件，消息仅推送给该会话成员；新建会话事件携带 `{roomId,created:true}`。HTTP 先事务提交消息与会话摘要，再推送；刷新和断线后从数据库补消息并按消息 ID 去重。

第一版只支持单个后端实例；群发采用一个持久化会话消息向多个成员推送，无需重复保存正文。上线多实例时需共享票据与跨实例广播。生产代理配置沿用 [人工客服部署说明](support-chat.md)。

## 验证

后端集成测试在独立 H2 测试库验证四种关系、私聊复用、群发只读、会话越权、相同数字编号的角色隔离、成员定向 WebSocket 推送和重复发送。运行 `.tools/apache-maven-3.9.9/bin/mvn.cmd -f backend/pom.xml test`。

前端运行 `node --test tests/support-socket.test.mjs`、`npm run build`。本地验证时使用两个已登录账号，各在聊天中心查看自己的编号，创建私聊 / 群聊后分别发消息，群发接收者应看不到输入框。
