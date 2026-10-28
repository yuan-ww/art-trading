# ArtTrading

艺术品交易平台的前后端项目。有关目录边界请阅读 [架构说明](docs/ARCHITECTURE.md)。

## 当前功能

- 用户注册、登录、作品发布、审核、收藏、评价和举报。
- 直售订单、模拟钱包付款、超时取消、物流发货/确认收货及物流轨迹。
- 拍卖报名、保证金、出价、结算、流拍处理和驳回后重新提交。
- 售后申请、卖家处理、管理员退款核实，以及钱包资金流水。
- 站内通知、基础运营统计、普通账号禁用和管理端举报/售后处理。
- 公开作品、订单、用户、售后、钱包流水、通知、物流和举报等列表均提供分页接口；公开列表、订单列表使用轻量 DTO 查询。

支付仅使用项目内部的模拟钱包余额，不接入微信、支付宝、银行卡或任何真实扣款服务。

## 目录

```text
ArtTrading 2/
├── demo/       Spring Boot 后端（端口 10202）
├── front/      Vue 3 前端（开发服务器端口 8080）
├── scripts/    本地启动脚本
├── docs/       架构与开发说明
├── database/   数据库使用说明
└── README.md
```

## 数据库迁移

`database/migrations/` 为增量迁移脚本，按数字顺序执行。迁移会创建或补齐唯一约束、查询索引、通知/物流/举报表、账号禁用字段，以及交易安全相关的版本列和请求键。

MySQL DDL 会隐式提交。对已有数据库执行前请先备份，并在测试数据库验证；脚本尽量基于 `information_schema` 防止重复执行。第 7 份脚本仅用于清理旧 Hibernate 自动建表与迁移同时存在时产生的重复请求键索引。

## 运行要求

- JDK 17
- MySQL（建议 8.x；SQL 导出来自 MySQL 5.6）
- Node.js 与 npm

## 首次启动

1. 创建数据库。公开仓库不包含历史数据；请使用受控的本地 SQL 导出或自行准备脱敏 schema 与测试数据：

   ```sh
   mysql -u root -p -e 'CREATE DATABASE user_auth CHARACTER SET utf8mb4;'
   # 本地保留历史数据时才执行；该文件不在 Git 仓库内。
   mysql -u root -p user_auth < ../数据库/DataBase.sql
   ```

2. 配置后端本地环境文件：

   ```sh
   cp demo/.env.example demo/.env
   ```

   编辑 `demo/.env`，至少填入正确的 `DB_PASSWORD`。`.env` 已被 Git 忽略，勿提交。

   如需变更前端接口地址，可复制 `front/.env.example` 为 `front/.env.local` 并设置 `VUE_APP_API_BASE_URL`。

3. 在导入基础 schema 后，按顺序执行 `database/migrations/` 中的迁移脚本。生产或远程数据库建议先在备份或测试库中确认执行结果。

4. 在两个终端分别启动：

   ```sh
   ./scripts/start-backend.sh
   ./scripts/start-frontend.sh
   ```

前端默认访问 `http://localhost:8080`，后端默认运行在 `http://localhost:10202`。前端接口地址与后端跨域配置目前均以此本机端口组合为准。

## 验证与构建

```sh
# 前端生产构建
(cd front && npm run build)

# 后端测试
(cd demo && ./mvnw test)
```

当前后端测试包含数据库查询集成校验、交易状态校验与请求键重试保护；前端使用生产构建验证路由懒加载和组件编译。

## 运行边界

- 缓存为分类数据的本机短时缓存，不依赖 Redis；新上传图片生成缩略图，历史图片会回退原图。
- 物流轨迹由卖家手动维护，不连接第三方快递服务。
- 交易流程已使用数据库锁、版本列和请求键防止重复处理；部署前如有高并发要求，仍建议结合目标环境做压力测试。
- 历史余额和历史订单不会自动补生成资金流水。

## 交付说明

- `database/README.md` 记录公开仓库的数据库边界；历史 `.frm`/`.ibd` 文件不应作为跨机器导入方案。
- 前端和后端分别使用 `front/.env.example`、`demo/.env.example` 作为本地配置模板。
- 当前项目不包含容器化部署配置或脱敏数据库初始化脚本；部署和 CI 前应补充。
