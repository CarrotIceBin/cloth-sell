# 10 万逻辑用户压力测试

本次不是 10 万 WebSocket 同时在线测试。当前服务硬上限为 10000 个连接，无法支持该目标；本次没有修改连接上限或业务代码。

## 场景与实测

- 100000 个独立买家身份，100000 个私聊会话，每人通过真实 HTTP 发送一条约 200 字符消息。
- 200 个发送线程，闭环请求；总共 100000 条消息，无负载 WebSocket 连接。
- 耗时 24.77 秒，吞吐 4036.38 条/秒。
- HTTP P50：44ms，P95：110ms，P99：221ms，最大：448ms。
- 请求错误 0，数据库新增消息 100000 条，数量核对通过。
- 包含继承的接口与 WebSocket 功能测试，执行结果见 Surefire 报告。

## 复现

在项目根目录 PowerShell 执行：

```powershell
.tools/apache-maven-3.9.9/bin/mvn.cmd -f backend/pom.xml test '-Dtest=ChatPopulationLoadTest' '-Dchat.load=true' '-DargLine=-Xmx2g' -q
```

原始结果：`docs/chat-population-results.json`；运行输出：`backend/target/chat-population-run.log`。

## 结果边界

使用隔离的内存 H2、模拟账号查询、直接批量建立会话数据；请求仍经过认证、权限判断、消息保存的实际接口。客户端与服务器运行在同一 JVM，本次不连接业务数据库。

延迟从工作线程开始请求计算，不包含客户端任务队列等待。没有测量 WebSocket 消息投递、10 万连接握手、生产 PostgreSQL、远程网络或长时间稳定性。因此只能证明此场景中的 HTTP 消息处理结果，不能证明 10 万人同时实时聊天。

验证真正 10 万在线前，需要解决当前 1 万连接上限和单实例推送路由，再使用多个压测机和目标部署环境测连接、投递延迟、丢失率、CPU、内存及重连行为。
