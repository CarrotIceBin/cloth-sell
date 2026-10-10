# 人工客服接入与部署

买家登录后在 `/help` 切换「人工客服」，后台管理员在 `/admin/support` 查看会话并回复。AI / FAQ 模式仍可单独使用。当前所有管理员均可查看所有人工客服会话。

## 上线前建表

遵循本项目建表脚本不入仓库的约定，在目标数据库手动执行以下结构（不要自动对生产库执行）。PostgreSQL：

```sql
CREATE TABLE mall_support_message (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_role VARCHAR(16) NOT NULL,
    sender_name VARCHAR(255) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    creator VARCHAR(255) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(255) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_support_message_client UNIQUE (user_id, sender_role, sender_id, client_id)
);
CREATE INDEX idx_support_message_user_id ON mall_support_message (user_id, id);
```

MySQL 使用同一结构，将 `id BIGSERIAL PRIMARY KEY` 换为 `id BIGINT AUTO_INCREMENT PRIMARY KEY`，表使用 `utf8mb4` 字符集。保留唯一约束与索引。新增内容不要求更改原有表，不会自动创建或删除数据。

## 连接及接口

- 买家消息：`GET/POST /mall/client/support/messages`。
- 管理员会话：`GET /mall/admin/support/conversations?page=1`，每页 50 个。
- 管理员消息：`GET/POST /mall/admin/support/messages`，查询带 `userId`，发送正文带 `userId`。
- 发送正文：`{ "clientId": "UUID", "content": "文字" }`，最多 2000 字。相同消息重试使用原 UUID。
- 消息查询：可选 `before` 或 `after` 消息 ID，不能同时使用，每页 50 条。
- 获取票据：`POST /mall/support/ws-ticket`，使用原有 Bearer 登录鉴权。
- 升级连接：`/mall/support/ws?ticket=...`，一次性票据 60 秒过期，不在 URL 暴露访问令牌。
- WebSocket 推送 JSON：`{ "type": "message", "data": { ... } }` 或 `{ "type": "presence", "data": true }`；客户端发送 `ping`，服务端回复文本 `pong`。

服务端已保存消息后才推送；客服离线也能留言。发送失败保留输入，重试去重。消息 ID 用字符串返回，避免 JavaScript 大整数精度丢失。客户端与服务器均限制连接与心跳生命周期，访问令牌过期后重连通过原 HTTP 刷新机制重新认证。

推送在后台执行；每连接待发送消息上限为 128 条 / 64KB，发送超时为 5 秒（每秒巡检），达到限额或超时后断开并由重连补取已保存消息。HTTP 成功表示消息已入库，不表示对方已收到。慢连接修复前后对照见 [压测报告](chat-load-report.md)。

## 反向代理

本地 Vite `/mall` 代理已开启 `ws: true`。生产 HTTPS 页面使用 `wss`，反向代理须保留同源 Host / Origin，并转发 WebSocket Upgrade。Nginx 对该路径可使用：

```nginx
location = /mall/support/ws {
    proxy_pass http://127.0.0.1:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_read_timeout 90s;
}
```

HTTPS 在代理终止时，后端需通过 Spring Boot `server.forward-headers-strategy=framework`（环境变量 `SERVER_FORWARD_HEADERS_STRATEGY=framework`）识别原始协议，以便同源校验。只有可信代理可以向后端提供转发头，后端端口应限制为内部访问。参考 [Spring WebSocket 文档](https://docs.spring.io/spring-framework/reference/web/websocket/server.html)。

第一版只支持单后端实例：票据和在线连接位于进程内，数据库保存消息。单身份最多 5 个连接，总连接 / 未消费票据上限 10000。多实例需共享票据并增加跨实例消息广播；当前不引入消息代理。

## 验证

后端：`.tools/apache-maven-3.9.9/bin/mvn.cmd -f backend/pom.xml test`；前端：在 `frontend` 执行 `node --test tests/support-socket.test.mjs` 和 `npm run build`。后端集成测试用独立 H2 测试库启动真实 HTTP / WebSocket 服务，不连接业务数据库。

建表并启动后，用买家与管理员两个浏览器会话测试：买家留言、管理员回复、关闭后台后离线留言、重新进入后台、刷新买家页面、切断网络后恢复。测试第二个买家不能读取第一个买家的历史，买家不能进入管理员接口。生产上线前检查反向代理的 WebSocket 升级是否返回 101。
