# 前端（Vue 3）

## 本地运行

```sh
npm ci
npm run serve
```

开发服务器默认监听 `http://localhost:8080`。

## API 地址

复制 `.env.example` 为 `.env.local` 后，可配置后端地址：

```sh
VUE_APP_API_BASE_URL=http://localhost:10202
```

所有前端 API 服务通过 `src/config/api.js` 读取此配置。不要在组件或服务文件中新增固定的主机地址。

## 校验

```sh
npm run build
npm run lint
```
