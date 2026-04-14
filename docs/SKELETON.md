# Tulipe 数据库管理工具项目骨架

## 项目概述

Tulipe 是一个现代化的 Windows 数据库管理工具，旨在解决传统数据库工具（如 DBeaver、DataGrip）存在的启动慢、内存占用高、大数据渲染卡顿等核心痛点。本项目采用创新的三层通信架构设计，在保证开发效率的同时，实现原生级性能体验。

## 技术栈

### 前端层 (UI Layer)

- **框架**: TypeScript 5.6.2 + Vue3 3.5.13 (Composition API)
- **UI 框架**: Shadcn-vue + Tailwind CSS
- **编辑器**: CodeMirror6 (SQL 编辑器)
- **容器**: Tauri 2.x (替代 Electron)
- **优势**:
  - 空窗口内存仅 40MB 左右
  - 启动速度 < 200ms (比 Electron 快 2-3 倍)
  - 可从原 Electron 方案无缝迁移，几乎无改造成本
  - Vue3 Composition API 提供更好的代码组织和状态管理
  - Shadcn-vue + Tailwind 提供现代化、可定制的 UI 组件

### 中间层 (Rust Layer)

- **语言**: Rust 1.75+
- **核心组件**:
  - `sqlparser-rs`: SQL 解析/格式化 (比 Java 的 JSqlParser 快 10 倍以上)
  - `Apache Arrow`: 列式结果集序列化 (比 JSON 传输体积减少 60%+，序列化/反序列化速度提升 5-10 倍)
  - `tokio`: 异步运行时，处理高并发 I/O
- **职责**:
  - 进程管理（启动/停止 SpringBoot 后端）
  - 文件系统操作
  - 系统级功能（托盘、窗口、剪贴板）
  - 安全凭证管理（系统密钥链）
  - 大数据处理（SQL 解析、Arrow 序列化）

### 后端层 (src-backend)

- **框架**: SpringBoot 3.2.4 + Kotlin 2.1.0
- **运行时**: GraalVM 原生编译
- **优势**:
  - 启动速度从 2-3s 压缩到 50-150ms
  - 内存占用从 200MB+ 降到 50MB 左右
  - 复用成熟的 JDBC 生态，覆盖所有主流/小众数据库官方驱动
  - 原有业务代码可几乎完全复用，迁移成本极低

## 三层通信架构设计

### 架构总览

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Tulipe 三层通信架构                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────────────┐                                                   │
│   │   前端 (Vue3 + TS)   │                                                   │
│   │   WebView 环境       │                                                   │
│   └─────────┬───────────┘                                                   │
│             │                                                               │
│     ┌───────┴───────┐                                                       │
│     │               │                                                       │
│     ▼               ▼                                                       │
│ ┌───────────┐  ┌─────────────────────────────────────────┐                 │
│ │   Rust    │  │           SpringBoot Backend            │                 │
│ │  (Tauri)  │  │         (GraalVM Native)                 │                 │
│ │           │  │                                         │                 │
│ │ 系统级操作 │  │  业务逻辑、数据库连接、数据处理          │                 │
│ │ 安全加密   │  │  JDBC 驱动、AI 服务调用                  │                 │
│ │ 进程管理   │  │                                         │                 │
│ └─────┬─────┘  └─────────────────────────────────────────┘                 │
│       │                           ▲                                         │
│       │                           │                                         │
│       └───────────────────────────┘                                         │
│              进程管理 / 特殊协作                                             │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 通信路径详解

#### 1. 前端 ↔ SpringBoot 通信

**通信方式**: HTTP REST / WebSocket / SSE

**主要职责**:

- 数据库连接管理（创建/编辑/删除数据源）
- SQL 执行与结果获取（中小数据集）
- 数据库元数据操作（表结构、索引、约束）
- 数据导入导出（小规模）
- 用户配置管理
- AI 功能调用（SQL 补全、优化建议）
- 历史记录与收藏

**实现示例**:

```typescript
// 前端直接调用 SpringBoot API
const response = await fetch("http://localhost:8080/api/database/query", {
	method: "POST",
	body: JSON.stringify({ sql, connectionId }),
});
```

#### 2. 前端 ↔ Rust 通信

**通信方式**: Tauri IPC (JSON-RPC)

**主要职责**:

- 文件系统操作（读写 SQL 脚本、配置文件）
- 系统级操作（剪贴板、系统托盘、窗口控制）
- 安全敏感操作（密码/密钥存储、凭证加密）
- 性能敏感操作（大数据集处理、SQL 解析格式化）
- 进程管理（启动/停止 SpringBoot 后端）
- 原生对话框（文件选择、消息确认）

**实现示例**:

```typescript
// 前端调用 Rust
import { invoke } from "@tauri-apps/api/core";
const result = await invoke("read_file", { path: "/path/to/file.sql" });
```

#### 3. Rust ↔ SpringBoot 通信

**通信方式**: HTTP / gRPC / 命名管道

**主要职责**:

- 进程生命周期管理（启动、监控、重启、优雅关闭）
- 大数据流传输（10万+ 行结果集、Apache Arrow 序列化）
- 高性能计算协作（SQL 解析、数据转换）
- 安全协作（加密解密、凭证传递）
- 资源协调（端口分配、内存协调）

### 通信决策矩阵

| 功能类型           | 通信路径                 | 通信方式          | 原因                      |
| ------------------ | ------------------------ | ----------------- | ------------------------- |
| 数据库连接管理     | 前端 → SpringBoot        | HTTP              | 业务逻辑，无需系统权限    |
| SQL 执行（小数据） | 前端 → SpringBoot        | HTTP              | 简单直接，JDBC 在 Java 端 |
| SQL 执行（大数据） | 前端 → Rust → SpringBoot | gRPC 流式         | Rust 处理流式传输更高效   |
| SQL 解析/格式化    | 前端 → Rust              | Tauri IPC         | Rust 解析性能高 10x       |
| 文件读写           | 前端 → Rust              | Tauri IPC         | 需要系统权限              |
| 密码存储           | 前端 → Rust              | Tauri IPC         | 需要系统密钥链            |
| 系统托盘           | 前端 → Rust              | Tauri IPC         | 原生系统功能              |
| 剪贴板操作         | 前端 → Rust              | Tauri IPC         | 大数据需要原生处理        |
| 用户配置           | 前端 → SpringBoot        | HTTP              | 业务数据，可持久化到 DB   |
| AI 功能            | 前端 → SpringBoot        | HTTP              | 业务逻辑，API 调用        |
| 进程管理           | Rust → SpringBoot        | HTTP/标准输入输出 | Rust 负责生命周期         |
| 健康检查           | Rust → SpringBoot        | HTTP              | 监控后端状态              |
| 数据导入导出       | 前端 → Rust              | Tauri IPC         | 文件系统操作              |
| 快捷键             | 前端 → Rust              | Tauri IPC         | 全局快捷键需要原生        |
| 窗口控制           | 前端 → Rust              | Tauri IPC         | 原生窗口管理              |

### 按数据特征选择通信方式

| 数据特征        | 推荐方式      | 说明         |
| --------------- | ------------- | ------------ |
| 小数据 (< 1MB)  | HTTP REST     | 简单、易调试 |
| 中数据 (1-10MB) | HTTP + 分页   | 避免超时     |
| 大数据 (> 10MB) | gRPC 流式     | 高效、可中断 |
| 实时推送        | WebSocket/SSE | 双向/单向    |
| 高频交互        | Tauri IPC     | 延迟 < 1ms   |
| 敏感数据        | Rust 处理     | 安全加密     |
| 文件数据        | Rust 处理     | 系统权限     |

## 核心功能模块

### 1. 插件化驱动系统

- **挑战**: GraalVM 原生镜像不支持运行时从外部 JAR 加载未注册的类
- **解决方案**:
  - **方案A (推荐)**: 预编译所有主流驱动到原生镜像中
    - 优点: 稳定性高，性能最佳
    - 缺点: 初始包体积较大，新增驱动需重新编译
  - **方案B**: 混合模式 - 核心驱动预编译，非主流驱动使用 JVM 模式动态加载
    - 优点: 保持插件化灵活性
    - 缺点: 实现复杂度高，需要维护两套加载机制

### 2. 多数据库适配

- **支持数据库**: MySQL, PostgreSQL, Oracle, SQL Server, SQLite, MongoDB, Redis, Neo4j (图数据库), Pinecone/Lance (向量数据库), Elasticsearch (文档数据库) 等
- **适配策略**:
  - 利用 JDBC 生态的官方驱动
  - 针对商业数据库 (Oracle, SQL Server, 达梦等) 进行 GraalVM 兼容性优化
  - 手动编写反射/资源配置以解决兼容性问题
  - 为不同数据库类型设计专用的访问接口

### 3. AI 集成功能

- **AI 服务模式**: 远程 API 接口（不使用本地模型）
- **用户配置**: 用户自行填写远程 AI API 地址和密钥
- **功能模块**:
  - **SQL 智能补全**: 基于上下文的智能提示
  - **SQL 优化建议**: 自动分析并提供性能优化建议
  - **自然语言转 SQL**: 支持用自然语言描述查询需求
  - **数据洞察**: 分析查询结果，提供可视化洞察
- **通信方式**: 前端 → SpringBoot → AI API

### 4. 内部数据存储 (Rust rusqlite + 系统密钥链)

Tulipe 作为数据库管理工具，需要存储自身的用户数据、配置信息等。采用 **Rust rusqlite + 系统密钥链** 的混合存储方案，确保 GraalVM Native Image 完全兼容：

#### 存储架构

```
┌─────────────────────────────────────────────────────────────────┐
│                     Tulipe 内部数据存储                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Rust 层 (Tauri)                           ││
│  │                                                             ││
│  │  ┌─────────────────┐    ┌─────────────────────────────────┐ ││
│  │  │   SQLite 文件    │    │         系统密钥链              │ ││
│  │  │  (rusqlite)     │    │      (keyring crate)            │ ││
│  │  │                 │    │                                 │ ││
│  │  │ • 连接配置      │    │ • 数据库密码                    │ ││
│  │  │ • 查询历史      │    │ • API Key                      │ ││
│  │  │ • 用户设置      │    │ • 加密密钥                     │ ││
│  │  │ • 收藏 SQL      │    │                                 │ ││
│  │  └─────────────────┘    └─────────────────────────────────┘ ││
│  │                                                             ││
│  └─────────────────────────────────────────────────────────────┘│
│           ↑ Tauri IPC                                           │
│           │                                                     │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    前端 (Vue3)                               ││
│  │                                                             ││
│  │  • 通过 Tauri IPC 访问配置数据                              ││
│  │  • 通过 HTTP 访问 SpringBoot 业务逻辑                       ││
│  │                                                             ││
│  └─────────────────────────────────────────────────────────────┘│
│           ↓ HTTP                                                │
│           │                                                     │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    SpringBoot (Native Image)                ││
│  │                                                             ││
│  │  • 专注于业务逻辑                                           ││
│  │  • JDBC 连接目标数据库                                      ││
│  │  • AI 服务调用                                              ││
│  │  • 无需处理内部配置存储                                     ││
│  │                                                             ││
│  └─────────────────────────────────────────────────────────────┘│
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### 数据分类存储策略

| 数据类型           | 存储位置      | 技术实现             | 原因                   |
| ------------------ | ------------- | -------------------- | ---------------------- |
| 连接配置（除密码） | SQLite (Rust) | rusqlite crate       | 结构化数据，需要查询   |
| 数据库密码         | 系统密钥链    | keyring crate        | 安全要求最高           |
| 查询历史           | SQLite (Rust) | rusqlite crate       | 需要全文搜索、时间排序 |
| 收藏的 SQL         | SQLite (Rust) | rusqlite crate       | 需要分类、标签管理     |
| 用户设置           | SQLite (Rust) | rusqlite crate       | 结构化配置             |
| AI API Key         | 系统密钥链    | keyring crate        | 敏感信息               |
| 应用状态           | SQLite / 内存 | rusqlite / in-memory | 快速恢复               |

#### 技术实现

**Rust + rusqlite:**

```rust
// Cargo.toml
[dependencies]
rusqlite = { version = "0.31", features = ["bundled"] }
serde = { version = "1.0", features = ["derive"] }
serde_json = "1.0"

// src-tauri/src/db.rs
use rusqlite::{Connection, Result as SqliteResult};
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct DbConnection {
    pub id: i64,
    pub name: String,
    pub db_type: String,
    pub host: String,
    pub port: i32,
    pub database: String,
    pub username: String,
    // 密码不存储在这里，存系统密钥链
}

#[tauri::command]
pub fn get_connections() -> Result<Vec<DbConnection>, String> {
    let conn = Connection::open(get_db_path()).map_err(|e| e.to_string())?;

    let mut stmt = conn.prepare(
        "SELECT id, name, db_type, host, port, database, username FROM db_connections"
    ).map_err(|e| e.to_string())?;

    let connections = stmt.query_map([], |row| {
        Ok(DbConnection {
            id: row.get(0)?,
            name: row.get(1)?,
            db_type: row.get(2)?,
            host: row.get(3)?,
            port: row.get(4)?,
            database: row.get(5)?,
            username: row.get(6)?,
        })
    }).map_err(|e| e.to_string())?;

    connections.collect::<SqliteResult<Vec<_>>>().map_err(|e| e.to_string())
}

#[tauri::command]
pub fn save_connection(conn: DbConnection) -> Result<(), String> {
    let db = Connection::open(get_db_path()).map_err(|e| e.to_string())?;

    db.execute(
        "INSERT INTO db_connections (name, db_type, host, port, database, username)
         VALUES (?1, ?2, ?3, ?4, ?5, ?6)",
        [&conn.name, &conn.db_type, &conn.host, &conn.port.to_string(),
         &conn.database, &conn.username]
    ).map_err(|e| e.to_string())?;

    Ok(())
}

fn get_db_path() -> String {
    let home = std::env::var("USERPROFILE")
        .or_else(|_| std::env::var("HOME"))
        .unwrap_or_else(|_| ".".to_string());
    format!("{}/.tulipe/tulipe.db", home)
}
```

**Rust 密钥链操作:**

```rust
// src-tauri/src/credentials.rs
use keyring::Entry;

#[tauri::command]
pub fn save_db_password(connection_id: String, password: String) -> Result<(), String> {
    Entry::new("tulipe-db", &connection_id)
        .map_err(|e| e.to_string())?
        .set_password(&password)
        .map_err(|e| e.to_string())
}

#[tauri::command]
pub fn get_db_password(connection_id: String) -> Result<String, String> {
    Entry::new("tulipe-db", &connection_id)
        .map_err(|e| e.to_string())?
        .get_password()
        .map_err(|e| e.to_string())
}
```

**前端调用示例:**

```typescript
// src/stores/connections.ts
import { invoke } from "@tauri-apps/api/core";

export const useConnectionStore = defineStore("connections", () => {
	const connections = ref<DbConnection[]>([]);

	async function loadConnections() {
		connections.value = await invoke("get_connections");
	}

	async function saveConnection(conn: DbConnection) {
		await invoke("save_connection", { conn });
		await loadConnections();
	}

	async function getPassword(connectionId: string): Promise<string> {
		return await invoke("get_db_password", { connectionId });
	}

	return { connections, loadConnections, saveConnection, getPassword };
});
```

#### 数据库初始化

```rust
// src-tauri/src/db.rs
pub fn init_db() -> Result<(), String> {
    let conn = Connection::open(get_db_path()).map_err(|e| e.to_string())?;

    // 创建连接配置表
    conn.execute(
        "CREATE TABLE IF NOT EXISTS db_connections (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            db_type TEXT NOT NULL,
            host TEXT NOT NULL,
            port INTEGER NOT NULL,
            database TEXT NOT NULL,
            username TEXT NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )",
        [],
    ).map_err(|e| e.to_string())?;

    // 创建查询历史表
    conn.execute(
        "CREATE TABLE IF NOT EXISTS query_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            connection_id INTEGER NOT NULL,
            sql TEXT NOT NULL,
            execution_time INTEGER,
            row_count INTEGER,
            executed_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )",
        [],
    ).map_err(|e| e.to_string())?;

    // 创建收藏 SQL 表
    conn.execute(
        "CREATE TABLE IF NOT EXISTS saved_queries (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            sql TEXT NOT NULL,
            tags TEXT,
            folder TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )",
        [],
    ).map_err(|e| e.to_string())?;

    // 创建用户设置表
    conn.execute(
        "CREATE TABLE IF NOT EXISTS user_settings (
            key TEXT PRIMARY KEY,
            value TEXT NOT NULL,
            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )",
        [],
    ).map_err(|e| e.to_string())?;

    Ok(())
}
```

#### 优势

- **GraalVM 兼容性**: Rust rusqlite 使用 bundled SQLite，无 JNI 问题，完全兼容 Native Image
- **高性能**: Rust 直接访问 SQLite，性能极高（比 Java JDBC 快 5-10 倍）
- **安全性**: 敏感数据存系统密钥链，普通数据存 SQLite
- **便携性**: 单文件数据库，易于备份和迁移
- **职责分离**: SpringBoot 专注于业务逻辑，Rust 处理系统级存储
- **开发效率**: Tauri IPC 调用简单，前端代码清晰

### 5. 前端架构设计

#### 页面结构

```
┌─────────────────────────────────────────────────────────────────┐
│                        Tulipe 前端架构                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌───────────────────────────┐                                  │
│  │       状态栏 (StatusBar)   │                                  │
│  │  • 当前连接指示器          │                                  │
│  │  • 快速切换连接下拉菜单     │                                  │
│  │  • Home 按钮               │                                  │
│  └───────────────────────────┘                                  │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ 页面容器                                                    ││
│  ├─────────────────────────────────────────────────────────────┤│
│  │                                                             ││
│  │ 1. 欢迎页                                                   ││
│  │ 2. 主界面                                                   ││
│  │ 3. 设置页                                                   ││
│  │                                                             ││
│  └─────────────────────────────────────────────────────────────┘│
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### 核心页面

**1. 欢迎页 (WelcomePage.vue)**

- 数据库连接管理
- 连接历史记录
- 快速连接向导

**2. 主界面 (MainWorkspace.vue) - 三栏式布局**

```
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│ 数据库列表     │  │ 对象导航       │  │ 详情/工作区    │
│ (DatabaseList)│  │ (ObjectNav)    │  │ (DetailPanel) │
│               │  │               │  │               │
│ • 数据库1     │  │ 标签页:        │  │ 标签页:        │
│ • 数据库2     │  │   - 表         │  │   - 表结构     │
│ • ...         │  │   - 视图       │  │   - 数据       │
└───────────────┘  │   - 函数       │  │   - SQL 编辑器 │
                   │   - 查询       │  │   - AI 对话    │
                   │   - 备份       │  │               │
                   └───────────────┘  └───────────────┘
```

**3. 设置页 (SettingsPage.vue)**

- 通用设置
- 编辑器配置
- AI 配置
- 快捷键
- 关于

#### AG Grid 集成

**为什么选择 AG Grid？**

| 特性         | AG Grid 优势   | 说明                     |
| ------------ | -------------- | ------------------------ |
| **性能**     | 虚拟滚动       | 支持百万行数据流畅滚动   |
| **编辑功能** | 单元格编辑     | Excel 级编辑体验         |
| **扩展性**   | 自定义组件     | 完全自定义渲染器和编辑器 |
| **功能丰富** | 分组/聚合/透视 | 内置高级数据分析功能     |
| **框架集成** | Vue 原生支持   | 官方提供 Vue 适配器      |

**关键优化功能**:

- 异步数据加载（大表分页）
- 实时协作编辑（WebSocket）
- 自定义编辑器（JSON、下拉框等）
- 批量操作（删除、更新）

#### 状态管理

**Pinia 状态设计**:

- `connectionStore`: 连接管理、状态缓存
- `tabsStore`: 标签页管理
- `cacheStore`: 数据缓存

**状态持久化策略**:

- 连接切换时自动保存状态
- 页面刷新前保存状态
- LocalStorage 持久化

**关键特性**: 切换连接/返回欢迎页后，保留主界面连接的一切状态，包括打开的标签页等，除非用户手动断开此连接。

### 6. 性能优化特性

- **大数据渲染**: 10 万行结果集首屏渲染 < 0.3s (AG Grid 虚拟滚动)
- **内存管理**: 总内存占用 < 100MB
- **启动优化**: 总启动时间从 2.5s 降到 0.25s
- **多数据库支持**: 支持向量数据库、图数据库、文档数据库等新型数据库
- **Web Workers**: 大数据排序/过滤在 Worker 中执行
- **缓存策略**: SQL 查询结果缓存、表结构缓存

## 开发与部署策略

### 分阶段落地计划

#### 阶段1: MVP 验证 (Tauri + GraalVM SpringBoot)

- **目标**: 快速验证核心架构可行性
- **技术栈**: 仅使用 Tauri + GraalVM SpringBoot
- **预期收益**: 获得 80% 的性能提升
- **团队要求**: 无需 Rust 经验，原有 TS/Java 团队可无缝衔接

#### 阶段2: 性能优化 (引入 Rust 代理层)

- **目标**: 解决剩余 20% 的性能瓶颈
- **重点优化**: SQL 解析、大数据传输、高频交互
- **团队要求**: 需要 Rust 开发人员或学习投入

### 构建与发布流程

#### 开发环境搭建

```bash
# 前端依赖
npm install

# Rust 依赖
cd src-tauri && cargo build

# Java 后端 (需要 GraalVM 21)
cd src-backend
./gradlew build
```

#### 打包发布

##### 1. 环境准备

```bash
# 安装 GraalVM (推荐使用 SDKMAN)
sdk install java 21.0.2-graal
sdk use java 21.0.2-graal

# 安装 Native Image
gu install native-image

# Windows 需要安装 Visual Studio Build Tools
# 下载地址: https://visualstudio.microsoft.com/visual-cpp-build-tools/
# 勾选 "C++ build tools" 工作负载
```

##### 2. 构建 SpringBoot Native 可执行文件

```bash
cd src-backend

# 方式一: 使用 Gradle Native Compile 插件
./gradlew nativeCompile
# 输出: build/native/nativeCompile/src-backend.exe

# 方式二: 使用 GraalVM Native Image Maven 插件 (如果使用 Maven)
./mvnw -Pnative native:compile
# 输出: target/src-backend.exe
```

##### 3. 配置 Tauri 嵌入 Native 可执行文件

修改 `src-tauri/tauri.conf.json`:

```json
{
	"bundle": {
		"externalBin": ["binaries/src-backend"],
		"resources": ["resources/*"]
	}
}
```

创建 `src-tauri/binaries/` 目录结构:

```
src-tauri/
├── binaries/
│   └── x86_64-pc-windows-msvc/
│       └── src-backend.exe    # GraalVM 编译的 native 可执行文件
├── resources/
│   └── application.yaml       # SpringBoot 配置文件 (可选)
```

##### 4. 在 Rust 中启动嵌入式后端

修改 `src-tauri/src/lib.rs`:

```rust
use tauri::Manager;
use std::process::{Command, Child};
use std::sync::Mutex;

pub struct Backend(Mutex<Option<Child>>);

#[tauri::command]
fn start_backend(app: tauri::AppHandle) -> Result<(), String> {
    let backend_path = app.path().resolve(
        "binaries/x86_64-pc-windows-msvc/src-backend.exe",
        tauri::path::BaseDirectory::Resource
    ).map_err(|e| e.to_string())?;

    let child = Command::new(backend_path)
        .spawn()
        .map_err(|e| format!("Failed to start backend: {}", e))?;

    let backend = app.state::<Backend>();
    *backend.0.lock().unwrap() = Some(child);

    Ok(())
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .manage(Backend(Mutex::new(None)))
        .invoke_handler(tauri::generate_handler![start_backend])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
```

##### 5. 完整打包流程

```bash
# 步骤 1: 构建前端
npm run build

# 步骤 2: 构建 SpringBoot Native 可执行文件
cd src-backend
./gradlew nativeCompile
cd ..

# 步骤 3: 复制 native 可执行文件到 Tauri 资源目录
mkdir -p src-tauri/binaries/x86_64-pc-windows-msvc
cp src-backend/build/native/nativeCompile/src-backend.exe \
   src-tauri/binaries/x86_64-pc-windows-msvc/

# 步骤 4: 构建 Tauri 应用 (包含嵌入式后端)
cd src-tauri
cargo tauri build --target x86_64-pc-windows-msvc
```

##### 6. 打包输出

```
src-tauri/target/release/
├── Tulipe.exe                    # 主应用程序
└── bundle/
    ├── msi/
    │   └── Tulipe_0.1.0_x64.msi  # Windows 安装包
    └── nsis/
        └── Tulipe_0.1.0_x64-setup.exe  # NSIS 安装包
```

##### 7. 应用启动流程

```
用户启动 Tulipe.exe
       ↓
Tauri 初始化 WebView
       ↓
Rust 启动嵌入式 src-backend.exe (子进程)
       ↓
前端通过 HTTP/WebSocket 连接 localhost:8080
       ↓
应用正常运行
       ↓
用户关闭应用 → Rust 终止后端子进程
```

##### 8. 注意事项

- **启动顺序**: 后端启动需要等待端口就绪，前端应实现重试机制
- **端口冲突**: 建议使用动态端口或配置文件指定端口
- **进程管理**: 应用退出时必须清理后端子进程，避免僵尸进程
- **文件大小**: Native Image 约 30-80MB，整体安装包约 100-150MB
- **内存占用**: Native Image 启动后约 20-50MB 基础内存

## 配置要点

### SpringBoot CORS 配置

```kotlin
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:1420",      // Tauri dev
                "http://localhost:5173",      // Vite dev
                "tauri://localhost",          // Tauri production
                "https://tauri.localhost"     // Tauri production (HTTPS)
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
```

### Tauri CSP 配置

```json
// src-tauri/tauri.conf.json
{
	"security": {
		"csp": "default-src 'self'; connect-src 'self' http://localhost:* ws://localhost:* https://api.openai.com; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"
	}
}
```

### Tauri 权限配置

```json
// src-tauri/capabilities/default.json
{
	"identifier": "default",
	"description": "Default capabilities for the app",
	"permissions": [
		"core:default",
		"shell:allow-spawn",
		"shell:allow-kill",
		"fs:allow-read",
		"fs:allow-write",
		"clipboard:allow-write",
		"clipboard:allow-read",
		"notification:default",
		"global-shortcut:allow-register"
	]
}
```

## 潜在风险与应对策略

### 1. GraalVM 兼容性风险

- **问题**: 商业数据库 JDBC 驱动对 GraalVM 兼容性差
- **应对**:
  - 提前进行驱动兼容性测试
  - 准备手动配置反射/资源注册方案
  - 考虑混合运行模式作为备选方案

### 2. 动态特性缺失

- **问题**: GraalVM 原生镜像缺乏动态数据源切换、驱动热加载等特性
- **应对**:
  - 重构为静态配置模式
  - 在 Rust 层实现部分动态逻辑
  - 接受功能限制，优先保证稳定性

### 3. 团队技术栈适应

- **问题**: Rust 学习曲线陡峭
- **应对**:
  - 采用分阶段实施策略
  - 优先完成 MVP 验证
  - 逐步引入 Rust 优化

### 4. AI 服务集成风险

- **问题**: 远程 AI 服务不可靠、响应慢、费用高昂
- **应对**:
  - 提供备用方案（本地缓存、降级模式）
  - 实现重试机制和超时控制
  - 用户可选择是否启用 AI 功能

## 性能指标目标

| 指标        | 传统方案 | Tulipe 目标 | 提升倍数 |
| ----------- | -------- | ----------- | -------- |
| 启动时间    | 2.5s     | 0.25s       | 10x      |
| 内存占用    | 300MB+   | <100MB      | 3x+      |
| 10万行渲染  | >2s      | <0.3s       | 6x+      |
| SQL解析速度 | 基准     | 10x+        | 10x+     |
| AI 响应时间 | 2s       | <1s         | 2x+      |

## 项目目录结构

```
tulipe/
├── src/                # Tauri 前端应用 (TS+Vue3 + Composition API)
│   ├── assets/         # 静态资源
│   ├── App.vue         # 根组件
│   ├── main.ts         # 应用入口
│   └── vite-env.d.ts   # Vite 类型声明
├── src-tauri/          # Rust Tauri 应用层
│   ├── src/
│   │   ├── main.rs     # 主入口
│   │   └── lib.rs      # 库模块
│   ├── Cargo.toml      # Rust 依赖配置
│   ├── build.rs        # 构建脚本
│   └── capabilities/   # Tauri 权限配置
│       └── default.json
├── src-backend/        # SpringBoot + Kotlin 后端 (sec-backend)
│   ├── src/main/kotlin/org/fleur/srcbackend/
│   │   └── SrcBackendApplication.kt    # 启动类
│   ├── src/main/resources/
│   │   └── application.yaml            # 应用配置
│   ├── build.gradle.kts                # Gradle 构建配置 (SpringBoot 3.2.4)
│   └── settings.gradle.kts             # Gradle 项目设置
├── public/             # 公共静态资源
│   ├── tauri.svg
│   └── vite.svg
├── .vscode/            # VS Code 配置
│   └── extensions.json
├── .idea/              # IntelliJ IDEA 配置
├── docs/               # 项目文档
├── scripts/            # 构建和部署脚本
├── index.html          # HTML 入口
├── package.json        # 前端依赖配置
├── package-lock.json   # 前端依赖锁定
└── README.md           # 项目说明
```

## 开发规范

### 代码风格

- **TypeScript**: 使用 ESLint + Prettier
- **Rust**: 使用 rustfmt + clippy
- **Kotlin**: 使用 ktlint
- **Vue3**: 使用 Composition API，遵循 Vue 官方最佳实践

### 测试策略

- **单元测试**: 各层独立测试
- **集成测试**: 跨层通信测试
- **性能测试**: 关键路径基准测试
- **AI 服务测试**: 远程 API 调用模拟测试

### 版本管理

- **语义化版本**: MAJOR.MINOR.PATCH
- **分支策略**: main (稳定) + develop (开发) + feature/\* (特性)

## 贡献指南

1. Fork 项目仓库
2. 创建特性分支 (`git checkout -b feature/your-feature`)
3. 提交更改 (`git commit -am 'Add some feature'`)
4. 推送到分支 (`git push origin feature/your-feature`)
5. 创建 Pull Request

## 许可证

[待确定具体许可证类型]

## 联系方式

- 项目维护者: [待填写]
- 问题反馈: [待填写]
- 文档地址: [待填写]
