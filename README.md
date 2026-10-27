# ArtTrading

艺术品交易平台的前后端项目。主项目目录为本文件所在位置；数据库导入文件位于相邻目录 `../数据库/DataBase.sql`。

## 目录

```text
ArtTrading 2/
├── demo/       Spring Boot 后端（端口 10202）
├── front/      Vue 3 前端（开发服务器端口 8080）
├── scripts/    本地启动脚本
└── README.md
```

## 运行要求

- JDK 17
- MySQL（建议 8.x；SQL 导出来自 MySQL 5.6）
- Node.js 与 npm

## 首次启动

1. 创建数据库并导入初始数据：

   ```sh
   mysql -u root -p -e 'CREATE DATABASE user_auth CHARACTER SET utf8mb4;'
   mysql -u root -p user_auth < ../数据库/DataBase.sql
   ```

2. 配置后端本地环境文件：

   ```sh
   cp demo/.env.example demo/.env
   ```

   编辑 `demo/.env`，至少填入正确的 `DB_PASSWORD`。`.env` 已被 Git 忽略，勿提交。

3. 在两个终端分别启动：

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

## 交付说明

- 数据库恢复请使用 `../数据库/DataBase.sql`；`数据库/user_auth` 中的 `.frm`/`.ibd` 文件为旧数据库数据目录，不应作为跨机器导入方案。
- 根目录的 `ArtTrading.zip` 是可用备份；`ArtTrading/` 是不完整的历史解压目录，不能替代本项目。
- 当前项目不包含 Git 历史、Docker 部署配置或生产环境配置。提交或部署前应另行补充。
