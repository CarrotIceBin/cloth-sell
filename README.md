# cloth-sell

服装商城。顾客浏览商品、加购物车、填写地址下单并付款；管理员维护商品规格与封面，并推进订单状态。

线上地址：[https://shop.mylyx.store/](https://shop.mylyx.store/)。站点已上线，目前仍在慢慢维护。

## 技术栈

- 后端：Java 17、Spring Boot 3.3、Spring Security、MyBatis-Plus、PostgreSQL。数据源换成 MySQL 时，分页会跟着切换。默认用 Redis 缓存商品，`REDIS_ENABLED=false` 时关闭。
- 前端：Vue 3、Vite、Vue Router、Element Plus、Axios

## 功能

**顾客端**

- 注册、登录。访问令牌过期后用刷新令牌续期。
- 已上架商品列表与详情，规格为颜色和尺码。首页是营销版面，商品列表在 `/shop`。
- 期刊：文章列表按标签筛选，`/journal/:id` 是正文页。
- 评价：商品详情页显示平均分、星级分布与评价列表。登录后可以评分，每个账号对同一件商品只能评价一次。
- 购物车增删改。
- 结算：收货人、手机号、省市区和详细地址。运费固定为 0。
- 下单扣减库存并清空购物车；在「我的订单」对待付款订单标记已付款。

**管理端**

- `/admin/login` 管理员登录。
- 商品：创建、修改、删除、上下架；每个商品有多个 SKU（颜色、尺码、价格、库存）。
- 期刊：创建、修改、删除文章，可设为未发布，未发布的文章顾客端看不到。
- 评价：查看全部评价，可以隐藏或删除。
- 封面图：配置了七牛云则上传到七牛；未配置则存到本地 `uploads`，通过 `/files/**` 访问。
- 订单：待付款 → 已付款 → 已发货 → 已完成。待付款、已付款可以取消，取消时把库存加回去。

## 目录

```
backend/     Spring Boot，控制器仍是 /mall
frontend/    Vue 商城与管理后台
```

浏览器里的页面是 `/`、`/admin`，接口直接请求 `/mall` 和 `/files`。变更记录在 `CHANGELOG.md`。

接口文档由 springdoc 提供，后端起来后打开 `/swagger-ui.html`。

## 本地运行

需要 JDK 17、Maven、Node.js、.NET 10 和 PostgreSQL。

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
| `mall_journal` | 期刊文章 |
| `mall_review` | 商品评价 |

建表脚本和演示数据不进入仓库，只在本地保存或在已有库里执行。期刊和评价使用的三处结构是：

| 对象 | 说明 |
| --- | --- |
| `mall_journal` | 期刊文章，字段含标签、标题、摘要、正文、封面、是否发布 |
| `mall_review` | 商品评价，字段含商品、用户、展示名、评分、内容、是否显示 |
| `mall_sku.cover_url` | 规格（颜色）的图片地址，为空时回退到商品封面 |

管理端登录页的账号名是 `admin`。库里还没有这个账号时，设置至少 8 位的 `app.admin-password`（或环境变量 `APP_ADMIN_PASSWORD`），后端启动时才会创建。已有账号不会被覆盖。仓库不提供默认口令。

### 2. 后端配置

```bash
cd backend
copy src\main\resources\application.yml.example src\main\resources\application.yml
```

`application.yml` 不进入仓库。复制示例后数据库口令已经写在 `password: 1234`。令牌密钥、管理员初始口令和七牛配置可以继续用环境变量：

| 变量 | 作用 |
| --- | --- |
| `APP_TOKEN_SECRET` | 访问令牌密钥，至少 32 位。示例里的 `change-me` 不能用来启动 |
| `APP_ADMIN_PASSWORD` | 首次创建管理员的口令，至少 8 位。已有 `admin` 时不会改密码 |
| `REDIS_ENABLED` | 默认 `true`，连接 Redis 缓存商品。设为 `false` 则关闭。地址用 `REDIS_HOST`、`REDIS_PORT`，默认 `127.0.0.1:6379` |
| `QINIU_ACCESS_KEY` / `QINIU_SECRET_KEY` / `QINIU_BUCKET` / `QINIU_DOMAIN` | 七牛上传；Access Key 为空时走本地目录 |

服务端口 `8080`。

金额由两个 C# 项目处理。`ClothSell.Database` 只负责打开数据库，`ClothSell.Money` 用它读写购物车金额、商品最低价，以及订单的单价、运费和应付。不接收 Java 传来的金额。先启动金额服务，再启动 Java：

```bash
cd money
dotnet run --project ClothSell.Money
```

监听 `http://127.0.0.1:5088`。后端数据库口令写在 `application.yml` 的 `password`。可用 `MONEY_URL` 改 Java 访问金额服务的地址。金额服务没启动时，商品列表、购物车和下单会提示金额计算服务不可用。

```bash
mvn spring-boot:run
```

### 3. 前端

```bash
cd frontend
npm install
npm run dev
```

开发服务器在 `http://localhost:5173`，并把 `/mall`、`/files` 代理到 `http://127.0.0.1:8080`。

| 路径 | 页面 |
| --- | --- |
| `/` | 商品列表 |
| `/product/:id` | 商品详情 |
| `/cart`、`/checkout`、`/orders` | 购物车、结算、我的订单（需顾客登录） |
| `/login` | 顾客登录 |
| `/admin/login` | 管理员登录 |
| `/admin/product`、`/admin/order`、`/admin/journal`、`/admin/review` | 商品、订单、期刊、评价管理 |

期刊的文章在管理后台「期刊」页维护：可以按标题、标签和发布状态筛选，文章正文空行分段，未发布的文章顾客端看不到。评价在「评价」页维护，可以按商品、星级和显示状态筛选，支持隐藏或删除。封面沿用商品上传接口，配置七牛时存到七牛，否则存到本地 `uploads`。
