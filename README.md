# cloth-sell

服装商城。顾客浏览商品、加购物车、填写地址下单并付款；管理员维护商品规格与封面，并推进订单状态。

线上地址：[https://shop.mylyx.store/](https://shop.mylyx.store/)。站点已上线，目前仍在慢慢维护。

## 技术栈

- 后端：Java 17、Spring Boot 3.3、Spring Security、MyBatis-Plus、MySQL。数据源换成 PostgreSQL 时，分页会跟着切换。默认用 Redis 缓存商品，`REDIS_ENABLED=false` 时关闭。
- 前端：Vue 3、Vite、Vue Router、Element Plus、Axios

## 功能

**顾客端**

- 注册、登录。访问令牌过期后用刷新令牌续期。
- 已上架商品列表与详情，规格为颜色和尺码。
- 购物车增删改。
- 结算：收货人、手机号、省市区和详细地址。运费固定为 0。
- 下单扣减库存并清空购物车；在「我的订单」对待付款订单标记已付款。

**管理端**

- `/admin/login` 管理员登录。
- 商品：创建、修改、删除、上下架；每个商品有多个 SKU（颜色、尺码、价格、库存）。
- 封面图：配置了七牛云则上传到七牛；未配置则存到本地 `uploads`，通过 `/files/**` 访问。
- 订单：待付款 → 已付款 → 已发货 → 已完成。待付款、已付款可以取消，取消时把库存加回去。

## 目录

```
backend/     Spring Boot，控制器仍是 /mall
frontend/    Vue 商城与管理后台
```

浏览器里的页面是 `/`、`/admin`，请求仍发到 `/mall`、`/files`。开发服务器转发给后端之前，管理接口加上 `/admin-api`，顾客接口加上 `/client-api`。后端去掉前缀后再进入原来的接口。两条前缀不能互串。变更记录在 `CHANGELOG.md`。

接口文档由 springdoc 提供，后端起来后打开 `/swagger-ui.html`。

## 本地运行

需要 JDK 17、Maven、Node.js 和 MySQL。

### 1. 数据库

创建库 `cloth_sell`（`utf8` / `utf8mb4` 均可）。表：

| 表 | 用途 |
| --- | --- |
| `mall_admin` | 管理员，密码为 BCrypt |
| `mall_user` | 顾客 |
| `mall_product` / `mall_sku` | 商品与规格 |
| `mall_cart` | 购物车 |
| `mall_order` / `mall_order_line` | 订单与明细 |
| `mall_login_log` | 登录日志 |
| `mall_refresh_token` | 刷新令牌 |

完整库结构在本地文件 `backend/sql/cloth_sell.sql`。该文件被 `.gitignore` 排除，仓库里没有这份导出，也没有增量脚本和演示数据。已有这份文件时先导入它。

管理端登录页的账号名是 `admin`。库里还没有这个账号时，设置至少 8 位的 `app.admin-password`（或环境变量 `APP_ADMIN_PASSWORD`），后端启动时才会创建。已有账号不会被覆盖。仓库不提供默认口令。

### 2. 后端配置

```bash
cd backend
copy src\main\resources\application.yml.example src\main\resources\application.yml
```

`application.yml` 不进入仓库。数据库口令、令牌密钥和七牛配置可以写在文件里，也可以用环境变量：

| 变量 | 作用 |
| --- | --- |
| `MYSQL_PASSWORD` | MySQL 密码，对应示例里的 `password` |
| `APP_TOKEN_SECRET` | 访问令牌密钥，至少 32 位。示例里的 `change-me` 不能用来启动 |
| `APP_ADMIN_PASSWORD` | 首次创建管理员的口令，至少 8 位。已有 `admin` 时不会改密码 |
| `REDIS_ENABLED` | 默认 `true`，连接 Redis 缓存商品。设为 `false` 则关闭。地址用 `REDIS_HOST`、`REDIS_PORT`，默认 `127.0.0.1:6379` |
| `QINIU_ACCESS_KEY` / `QINIU_SECRET_KEY` / `QINIU_BUCKET` / `QINIU_DOMAIN` | 七牛上传；Access Key 为空时走本地目录 |

服务端口 `8080`。

```bash
mvn spring-boot:run
```

### 3. 前端

```bash
cd frontend
npm install
npm run dev
```

开发服务器在 `http://localhost:5173`。页面请求 `/mall` 和 `/files`；转发到 `http://127.0.0.1:8080` 时，`/mall` 会被改写成 `/admin-api` 或 `/client-api`。

| 路径 | 页面 |
| --- | --- |
| `/` | 商品列表 |
| `/product/:id` | 商品详情 |
| `/cart`、`/checkout`、`/orders` | 购物车、结算、我的订单（需顾客登录） |
| `/login` | 顾客登录 |
| `/admin/login` | 管理员登录 |
| `/admin/product`、`/admin/order` | 商品、订单管理 |
