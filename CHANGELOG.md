# 更新日志

## 20260927

### 其他

- README 精简本地运行说明：去掉与正文重复的启动命令块、示例配置复制命令和七牛上传说明，技术栈一行不再提 `REDIS_ENABLED` 开关。
  文件：
  - `README.md`

## 20260926

### 顾客端

- 首页改为营销版面：横幅、品类、新品、品牌横幅、媒体墙与期刊。商品列表移到 `/shop`，支持按名称搜索和排序。
  文件：
  - `frontend/src/views/mall/client/home/index.vue`
  - `frontend/src/views/mall/client/shop/index.vue`
  - `frontend/src/views/mall/client/product/index.vue`
  - `frontend/src/components/ProductCard.vue`
  - `frontend/src/components/ShopBar.vue`（删除）
  - `frontend/src/components/SiteHeader.vue`
  - `frontend/src/components/SiteFooter.vue`
  - `frontend/src/styles.css`
  - `frontend/src/router.ts`
- 站内改名为「棉里 MIANLI」，登录页和后台侧栏的标识一起换掉。
  文件：
  - `frontend/index.html`
  - `frontend/src/views/mall/auth/login.vue`
  - `frontend/src/views/mall/AdminLayout.vue`
- 期刊：`/journal` 可按标签筛选，`/journal/:id` 是正文页，首页期刊板块改为读接口。
  文件：
  - `frontend/src/views/mall/client/journal/index.vue`
  - `frontend/src/views/mall/client/journal/detail.vue`
  - `frontend/src/api/mall/journal/index.ts`
  - `frontend/src/utils/cover.ts`
  - `frontend/src/router.ts`
  - `backend/src/main/java/com/clothsell/module/mall/service/journal/JournalService.java`
  - `backend/src/main/java/com/clothsell/module/mall/service/journal/JournalServiceImpl.java`
  - `backend/src/main/java/com/clothsell/module/mall/dal/dataobject/journal/JournalDO.java`
  - `backend/src/main/java/com/clothsell/module/mall/dal/mysql/journal/JournalMapper.java`
  - `backend/src/main/java/com/clothsell/module/mall/vo/journal/JournalPageReqVO.java`
  - `backend/src/main/java/com/clothsell/module/mall/vo/journal/JournalRespVO.java`
  - `backend/src/main/java/com/clothsell/module/mall/vo/journal/JournalSaveReqVO.java`
- 商品详情页按颜色切换主图，颜色缩略图取自规格图片，购物车也用同一张颜色图。
  文件：
  - `frontend/src/views/mall/client/product/detail.vue`
  - `frontend/src/api/mall/product/index.ts`
  - `frontend/src/utils/cover.ts`
  - `backend/src/main/java/com/clothsell/module/mall/dal/dataobject/product/SkuDO.java`
  - `backend/src/main/java/com/clothsell/module/mall/vo/product/SkuRespVO.java`
  - `backend/src/main/java/com/clothsell/module/mall/service/product/ProductServiceImpl.java`
  - `backend/src/main/java/com/clothsell/module/mall/service/cart/CartServiceImpl.java`
- 评价：显示平均分、星级分布与评价列表，登录后可提交，每个账号对同一件商品只能评价一次。
  文件：
  - `frontend/src/views/mall/client/product/detail.vue`
  - `frontend/src/api/mall/review/index.ts`
  - `backend/src/main/java/com/clothsell/module/mall/service/review/ReviewService.java`
  - `backend/src/main/java/com/clothsell/module/mall/service/review/ReviewServiceImpl.java`
  - `backend/src/main/java/com/clothsell/module/mall/dal/dataobject/review/ReviewDO.java`
  - `backend/src/main/java/com/clothsell/module/mall/dal/mysql/review/ReviewMapper.java`
  - `backend/src/main/java/com/clothsell/module/mall/vo/review/ReviewPageReqVO.java`
  - `backend/src/main/java/com/clothsell/module/mall/vo/review/ReviewRespVO.java`
  - `backend/src/main/java/com/clothsell/module/mall/vo/review/ReviewSaveReqVO.java`
  - `backend/src/main/java/com/clothsell/module/mall/vo/review/ReviewSummaryRespVO.java`
- 购物车、结算与订单页换成和首页同一套版式。
  文件：
  - `frontend/src/views/mall/client/cart/index.vue`
  - `frontend/src/views/mall/client/order/checkout.vue`
  - `frontend/src/views/mall/client/order/index.vue`
- 品牌页 `/about`：品牌故事、三个坚持、面料、版型与制作、包装与环保、服务承诺。首页横幅的「了解品牌」、页头「品牌」和页脚「品牌故事」都指向它。
  文件：
  - `frontend/src/views/mall/client/about/index.vue`
  - `frontend/src/components/SiteHeader.vue`
  - `frontend/src/components/SiteFooter.vue`
  - `frontend/src/views/mall/client/home/index.vue`
  - `frontend/src/router.ts`
  - `README.md`
- 帮助中心 `/help`：联系我们、常见问题、配送说明、退换货、尺码指南。内容写在前端，页脚和页头的客服入口指向对应分区锚点。
  文件：
  - `frontend/src/views/mall/client/help/index.vue`
  - `frontend/src/components/SiteHeader.vue`
  - `frontend/src/components/SiteFooter.vue`
  - `frontend/src/router.ts`
  - `README.md`
- 帮助中心左侧导航会随页面滚动高亮当前所在的分区。
  文件：
  - `frontend/src/views/mall/client/help/index.vue`

### 管理端

- 新增「期刊」页：按标题、标签、发布状态筛选，可新增、编辑、删除文章并上传封面。
  文件：
  - `frontend/src/views/mall/journal/index.vue`
  - `frontend/src/views/mall/journal/JournalForm.vue`
  - `frontend/src/views/mall/AdminLayout.vue`
  - `frontend/src/router.ts`
- 新增「评价」页：按商品、星级、显示状态筛选，可隐藏或删除评价。
  文件：
  - `frontend/src/views/mall/review/index.vue`
  - `frontend/src/views/mall/AdminLayout.vue`
  - `frontend/src/router.ts`
- 商品规格可为每个颜色上传图片，同一颜色自动共用。
  文件：
  - `frontend/src/views/mall/product/ProductForm.vue`
  - `backend/src/main/java/com/clothsell/module/mall/vo/product/SkuSaveReqVO.java`
- 管理员权限补上期刊与评价相关项，这两个新页面的按钮此前会被权限指令隐藏。
  文件：
  - `backend/src/main/java/com/clothsell/framework/security/config/TokenAuthFilter.java`
  - `backend/src/main/java/com/clothsell/module/mall/service/auth/AuthService.java`
  - `frontend/src/hooks/web.ts`
- 商品表单的价格与库存允许直接粘贴带 ¥ 或千分位的数字，提交前自动转成纯数字；仍填不对时会提示是第几行。
  文件：
  - `frontend/src/views/mall/product/ProductForm.vue`
  - `backend/src/main/java/com/clothsell/framework/web/GlobalExceptionHandler.java`

### 接口

- 新增顾客端 `/mall/client/journal/*`、`/mall/client/review/*`，GET 匿名可读；管理端新增 `/mall/journal/*`、`/mall/review/*`。
  文件：
  - `backend/src/main/java/com/clothsell/framework/security/config/SecurityConfig.java`
  - `backend/src/main/java/com/clothsell/module/mall/controller/client/journal/JournalController.java`
  - `backend/src/main/java/com/clothsell/module/mall/controller/client/review/ReviewController.java`
  - `backend/src/main/java/com/clothsell/module/mall/controller/admin/journal/JournalController.java`
  - `backend/src/main/java/com/clothsell/module/mall/controller/admin/review/ReviewController.java`
  - `backend/src/main/java/com/clothsell/module/mall/enums/ErrorCodeConstants.java`

### 其他

- 列表的每页条数选项改为 12、20、40。
  文件：
  - `frontend/src/components/Pagination/index.vue`
- 分页的每页条数选项始终包含当前值，选择其他条数后不会再显示一个下拉里没有的数字。
  文件：
  - `frontend/src/components/Pagination/index.vue`
- 建表脚本和演示数据不再进入仓库，只在本地维护。
  文件：
  - `README.md`
- 金额服务连不上时购物车与下单不再失败，金额改由本站按单价与数量计算；请求金额服务的超时缩短，不会长时间拖住页面。
  文件：
  - `backend/src/main/java/com/clothsell/module/mall/service/money/MoneyClient.java`
  - `backend/src/main/java/com/clothsell/module/mall/service/cart/CartServiceImpl.java`
  - `backend/src/main/java/com/clothsell/module/mall/service/order/OrderServiceImpl.java`

## 2026-09-25

### 安全

- 同一账号 10 分钟内连续登录失败 8 次后暂时拒绝。
- 账号不存在时也走密码校验，避免用响应快慢判断手机号是否已注册。
- 访问令牌签名改为固定时间比较。`app.token-secret` 至少 32 位，并拒绝示例里的默认值。
- 不再自动创建 `admin` / `admin123`。库里还没有管理员时，设置至少 8 位的 `app.admin-password` 才会创建。
- 刷新令牌用过即作废，并发刷新只有一次成功。
- 除登录注册、商品浏览、本地图片和接口文档外，其余接口必须登录。
- 封面上传按文件内容识别 jpg、png、gif、webp。封面地址只接受 `/files/` 或 `https://`。
- 收货人和地址限制长度。

### 接口

- 页面地址和浏览器里的请求仍是 `/`、`/admin` 和 `/mall`。
- 开发服务器直接把 `/mall`、`/files` 转到后端，不再使用 `/admin-api` 和 `/client-api`。

### 后台

- 订单状态筛选框加宽。未选择时显示「请选择状态」，选中后在框内显示对应状态。

### 其他

- 商品列表和详情默认写入 Redis，可用 `REDIS_ENABLED=false` 关闭。Redis 连不上时请求仍直接查库。
- 启动结束后在控制台按顺序打印端口、数据库、登录校验和商品缓存。
- 购物车金额、商品最低价，以及订单的单价、运费和应付，改由 C# 直接读写数据库。`ClothSell.Database` 负责连接，`ClothSell.Money` 负责金额接口。Java 只传用户或订单编号。
- 商品新增改用 PostgreSQL 自增主键，不再依赖不存在的 `mall_product_seq`。金额服务不可用时，商品最低价仍用已查出的规格价格。
- 分页插件按数据源选择 MySQL 或 PostgreSQL。当前默认连接本机 PostgreSQL 的 `cloth_sell`。
- 数据库口令直接写在 `application.yml`，金额服务不再读取 `POSTGRES_PASSWORD`。
- README 写明了接口前缀、首次创建管理员，以及令牌密钥和 Redis 的环境变量。库结构改由本地的 `backend/sql/cloth_sell.sql` 导入，仓库里不再列出已删除的 SQL 脚本。
