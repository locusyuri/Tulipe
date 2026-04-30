# Tulipe 后端待办事项

- 项目：`src-backend`（Tauri 应用后端模块）
- 创建时间：`2026-04-18 00:00`
- 更新时间：`2026-04-25 00:00`
- 状态：进行中

## P0（最高优先级）

### 1) 创建用户连接实体用于 SQLite 持久化
- **目标**：创建用于存储用户连接信息的实体/表。
- **已完成字段**：
  - `id`（主键）
  - `profile_name`（连接名称）
  - `db_type`（数据库类型）
  - `host`
  - `port`
  - `database_name`（PostgreSQL 需要，MySQL 使用空字符串占位）
  - `username`
  - `password_ciphertext`（当前以密文形式存储）
  - `extra_json`（存储 group 等扩展信息）
  - `created_at`
  - `updated_at`
- **状态**：✅ 已完成
- **验收**：
  - ✅ 表已创建（`schema.sql`）
  - ✅ 实体已映射（`ConnectionProfile.kt`）
  - ✅ 可以插入和查询记录

### 2) 连接成功后保存连接信息到 SQLite
- **目标**：在 `/data-source/connect` 成功后，将连接元数据持久化到 SQLite。
- **状态**：✅ 已完成
- **验收**：
  - ✅ 成功连接后写入/更新一条记录
  - ⚠️ `last_connected_at` 尚未实现
  - ⚠️ 重复策略基于 profile_name 唯一约束（待确认）

## P1（高优先级）

### 3) 添加连接列表 API 供前端启动页使用
- **目标**：提供 API 供 Tauri 前端展示已保存的连接。
- **建议 API**：
  - `GET /data-source/list` - 获取连接列表
  - 支持按 `group` 过滤
  - 支持按 `created_at` 或其他字段排序
- **状态**：🔲 待开发

### 4) 添加更新/删除已保存连接的 API
- **目标**：完成用户管理的连接 CRUD 生命周期。
- **建议 API**：
  - `PUT /data-source/{id}` - 更新连接
  - `DELETE /data-source/{id}` - 删除连接
- **状态**：🔲 待开发

### 5) 标准化 dbType 和驱动/URL 构建策略
- **目标**：保持多数据库扩展的可维护性。
- **状态**：✅ 已完成
- **验收**：
  - ✅ MySQL/PostgreSQL 通过相同连接端点
  - ✅ 不支持的 dbType 抛出清晰的业务异常
  - ✅ 支持别名（postgres, pg, pgs）

## P2（安全/可靠性）

### 6) 安全凭证处理
- **目标**：避免明文凭证存储。
- **任务**：
  - ⚠️ 密码存储在 `password_ciphertext` 字段，但还不是真正的加密
  - ✅ 敏感信息未在日志中明文输出
  - ⚠️ API 响应中需确认是否脱敏
- **状态**：🔲 部分完成

### 7) 添加连接+持久化流程的集成测试
- **目标**：确保未来多数据库扩展的稳定性。
- **范围**：
  - 成功路径
  - 错误凭证路径
  - 不支持的 dbType 路径
- **状态**：🔲 待开发

---

## 里程碑建议

- ✅ M1：完成 P0
- 🔲 M2：完成 P1
- 🔲 M3：完成 P2


