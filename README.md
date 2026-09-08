# 知忆岛 · Zhiyi Island

基于 Vue 和 Spring Boot 的读书笔记与知识库项目。本仓库用于展示基于已有项目进行的学习、环境配置、AI 接入与服务器部署实践，并非全部原创开发。原项目许可证保留在 [LICENSE](LICENSE)。

## 项目内容

- 用户登录、注册及邮箱验证码流程（邮件发送需自行配置 SMTP）。
- 笔记编辑、分类、知识库管理和知识图谱页面。
- 文档导入、数据导出及 MinIO 图片存储。
- AI 摘要和知识提取接口，支持 DeepSeek 与 SiliconFlow；需要自行提供 API 密钥。
- PostgreSQL + pgvector 数据结构；向量搜索需要独立配置嵌入模型，DeepSeek 部署模板默认关闭此功能。

## 本仓库中的部署实践

- 使用环境变量或私有配置文件管理凭据，打包时排除本地配置。
- 接入 DeepSeek，并区分不同服务商的请求参数。
- 提供 Nginx 反向代理、MinIO 文件访问地址配置及 systemd 服务模板。
- 提供适合小内存服务器的资源限制和定时备份脚本。
- 补充 AI 请求参数和文件 URL 的后端测试。

## 技术栈

| 部分 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite、Pinia、Element Plus、Axios |
| 后端 | Java 17、Spring Boot、Spring Security、MyBatis、JWT |
| 数据与文件 | PostgreSQL、pgvector、Redis、MinIO |
| 部署 | Linux、Nginx、systemd |

## 目录与运行

- [后端源码](zhiyi-island-backend/)
- [前端源码](zhiyi-island-frontend/)
- [运行与部署指南](docs/部署指南.md)
- [服务器配置模板](deploy/ecs/)
- [代码与文档审查报告](代码与文档审查报告.md)

项目已完成本地及服务器私有预览部署验证。公开仓库不包含可用的演示账号、个人服务器连接信息、数据库内容、邮件授权码或 API 密钥。

**当前定位为学习和作品演示。** 审查报告记录了对象归属校验、富文本安全及图片更新等尚待处理的问题；投入公开多用户服务前，应先修复并验证。代码公开不代表网站已完成公开上线。

## 验证

前端构建：在前端目录运行 `npm ci`、`npm run build`。

后端测试：在后端目录运行 `./mvnw test`；Windows 使用 `mvnw.cmd test`。目前的测试覆盖部分 AI 请求参数与文件地址行为，不代表完整的业务或安全测试。

## 许可证与来源

许可条款见 [LICENSE](LICENSE)。本仓库保留原项目提供的许可证文本；使用和再分发时请保留相应声明。技术文档中部分内容来自原项目，实际实现及限制以当前代码和审查报告为准。
