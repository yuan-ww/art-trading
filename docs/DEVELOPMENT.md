# 本地开发

## 配置文件

| 位置 | 用途 |
| --- | --- |
| `demo/.env` | MySQL、上传目录、后端端口、CORS 来源和对外资源地址 |
| `front/.env.local` | 浏览器访问的后端 API 根地址 |

两者都只用于本机，仓库只提交对应的 `.env.example` 模板。

`PUBLIC_BASE_URL` 会作为上传图片和头像保存到数据库时的访问前缀；本地默认值为 `http://localhost:10202`。

## 启动顺序

1. 启动可访问的 MySQL 并创建 `user_auth` 数据库。
2. 配置 `demo/.env`。
3. 运行 `./scripts/start-backend.sh`。
4. 配置前端（可选）并运行 `./scripts/start-frontend.sh`。

## 验证边界

- `./demo/mvnw -DskipTests package` 可验证后端编译，不要求数据库。
- `./demo/mvnw test` 会加载 Spring 上下文，需要可连接的 MySQL。
- `npm run build` 可验证前端生产构建。
- 端到端业务流仍需在数据库就绪后执行。
