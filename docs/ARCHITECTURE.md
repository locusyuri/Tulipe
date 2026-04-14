# Tulipe 三层通信架构详细设计

## 目录

1. [架构概述](#架构概述)
2. [通信路径详解](#通信路径详解)
3. [职责分层](#职责分层)
4. [通信协议选择](#通信协议选择)
5. [实现示例](#实现示例)
6. [配置指南](#配置指南)
7. [性能优化](#性能优化)
8. [安全设计](#安全设计)

---

## 架构概述

Tulipe 采用创新的三层通信架构，将系统功能按职责清晰划分到三个层级：

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

### 设计原则

1. **职责分离**: 每一层只处理自己最擅长的事情
2. **最小权限**: 前端不直接访问系统资源，通过 Rust 代理
3. **性能优先**: 大数据、高频操作走 Rust，普通业务走 SpringBoot
4. **开发效率**: 业务逻辑集中在 SpringBoot，避免 Rust 业务代码

---

## 通信路径详解

### 1. 前端 ↔ SpringBoot 通信

#### 通信方式

| 方式               | 协议     | 场景           | 特点                   |
| ------------------ | -------- | -------------- | ---------------------- |
| REST API           | HTTP/1.1 | 普通业务请求   | 简单、易调试、生态丰富 |
| Server-Sent Events | SSE      | 服务端推送通知 | 单向、轻量、自动重连   |
| WebSocket          | WS       | 实时双向通信   | 双向、低延迟、适合高频 |

#### 主要职责

```
┌─────────────────────────────────────────────────────────────────┐
│                    前端 → SpringBoot                           │
├─────────────────────────────────────────────────────────────────┤
│ ✅ 数据库连接管理                                                │
│    - 创建/编辑/删除数据源连接                                     │
│    - 连接测试、连接池管理                                         │
│                                                                 │
│ ✅ SQL 执行与结果获取                                             │
│    - 执行 SQL 查询                                               │
│    - 获取查询结果（中小数据集）                                    │
│    - 执行计划分析                                                 │
│                                                                 │
│ ✅ 数据库元数据操作                                               │
│    - 获取表结构、索引、约束                                       │
│    - 获取数据库版本信息                                           │
│    - 获取统计信息                                                 │
│                                                                 │
│ ✅ 数据导入导出（小规模）                                         │
│    - CSV/JSON 导入导出                                           │
│    - 数据迁移脚本生成                                             │
│                                                                 │
│ ✅ 用户配置管理                                                   │
│    - 用户偏好设置                                                 │
│    - 快捷键配置                                                   │
│    - 主题设置                                                    │
│                                                                 │
│ ✅ AI 功能调用                                                   │
│    - SQL 智能补全                                                │
│    - 自然语言转 SQL                                              │
│    - SQL 优化建议                                                │
│                                                                 │
│ ✅ 历史记录与收藏                                                │
│    - 查询历史                                                    │
│    - 收藏的 SQL                                                  │
│    - 执行日志                                                    │
└─────────────────────────────────────────────────────────────────┘
```

#### 实现示例

**SpringBoot Controller:**

```kotlin
@RestController
@RequestMapping("/api/database")
class DatabaseController(
    private val connectionService: ConnectionService,
    private val queryService: QueryService
) {

    @PostMapping("/connect")
    suspend fun connect(@RequestBody config: ConnectionConfig): Result<ConnectionInfo>

    @PostMapping("/query")
    suspend fun executeQuery(@RequestBody request: QueryRequest): Result<QueryResult>

    @GetMapping("/metadata/{connectionId}/tables")
    suspend fun getTables(@PathVariable connectionId: String): Result<List<TableInfo>>

    @GetMapping("/metadata/{connectionId}/tables/{tableName}/schema")
    suspend fun getTableSchema(
        @PathVariable connectionId: String,
        @PathVariable tableName: String
    ): Result<TableSchema>
}
```

**前端调用:**

```typescript
// src/services/database.ts
import axios from "axios";

const api = axios.create({
	baseURL: "http://localhost:8080/api",
	timeout: 30000,
});

export const databaseApi = {
	async connect(config: ConnectionConfig) {
		const { data } = await api.post("/database/connect", config);
		return data;
	},

	async executeQuery(sql: string, connectionId: string) {
		const { data } = await api.post("/database/query", { sql, connectionId });
		return data;
	},

	async getTables(connectionId: string) {
		const { data } = await api.get(`/database/metadata/${connectionId}/tables`);
		return data;
	},
};
```

---

### 2. 前端 ↔ Rust 通信

#### 通信方式

| 方式         | 协议        | 场景     | 特点                 |
| ------------ | ----------- | -------- | -------------------- |
| Tauri IPC    | JSON-RPC    | 命令调用 | 原生支持、延迟 < 1ms |
| Tauri Events | 事件总线    | 状态通知 | 双向、异步、解耦     |
| 二进制传输   | ArrayBuffer | 大数据   | 高效、零拷贝         |

#### 主要职责

```
┌─────────────────────────────────────────────────────────────────┐
│                      前端 → Rust                                │
├─────────────────────────────────────────────────────────────────┤
│ ✅ 文件系统操作                                                   │
│    - 读写本地文件（SQL 脚本、配置文件）                            │
│    - 选择文件/目录（导入导出路径选择）                             │
│    - 文件监控（自动重载配置）                                      │
│    - 临时文件管理                                                 │
│                                                                 │
│ ✅ 系统级操作                                                    │
│    - 剪贴板操作（复制粘贴大数据）                                  │
│    - 系统托盘管理                                                 │
│    - 窗口控制（无边框窗口、透明度）                                │
│    - 系统通知                                                    │
│    - 快捷键全局注册                                               │
│                                                                 │
│ ✅ 安全敏感操作                                                   │
│    - 密码/密钥存储（系统密钥链）                                   │
│    - 数据库连接凭证加密                                           │
│    - 敏感配置加密存储                                             │
│    - API Key 安全管理                                             │
│                                                                 │
│ ✅ 性能敏感操作                                                   │
│    - 大数据集处理（10万+ 行）                                     │
│    - SQL 解析与格式化（高频）                                      │
│    - 数据序列化/反序列化                                          │
│    - 本地缓存管理                                                 │
│                                                                 │
│ ✅ 进程管理                                                      │
│    - 启动/停止 SpringBoot 后端                                    │
│    - 监控后端进程状态                                             │
│    - 自动重启崩溃的后端                                           │
│    - 端口冲突检测与处理                                           │
│                                                                 │
│ ✅ 原生对话框                                                    │
│    - 文件选择对话框                                               │
│    - 消息对话框                                                  │
│    - 确认对话框                                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### 实现示例

**Rust Commands (src-tauri/src/lib.rs):**

```rust
use tauri::Manager;
use std::process::{Command, Child};
use std::sync::Mutex;
use serde::{Deserialize, Serialize};

pub struct AppState {
    backend: Mutex<Option<Child>>,
}

#[derive(Serialize)]
struct BackendStatus {
    running: bool,
    port: Option<u16>,
    pid: Option<u32>,
}

#[tauri::command]
async fn start_backend(
    app: tauri::AppHandle,
    state: tauri::State<'_, AppState>,
) -> Result<BackendStatus, String> {
    let backend_path = app.path()
        .resolve("binaries/src-backend.exe", tauri::path::BaseDirectory::Resource)
        .map_err(|e| e.to_string())?;

    let child = Command::new(&backend_path)
        .arg(format!("--server.port={}", 8080))
        .spawn()
        .map_err(|e| format!("启动后端失败: {}", e))?;

    let pid = child.id();
    *state.backend.lock().unwrap() = Some(child);

    Ok(BackendStatus {
        running: true,
        port: Some(8080),
        pid: Some(pid),
    })
}

#[tauri::command]
fn stop_backend(state: tauri::State<'_, AppState>) -> Result<(), String> {
    let mut backend = state.backend.lock().unwrap();
    if let Some(mut child) = backend.take() {
        child.kill().map_err(|e| e.to_string())?;
    }
    Ok(())
}

#[tauri::command]
async fn read_file(path: String) -> Result<String, String> {
    tokio::fs::read_to_string(&path)
        .await
        .map_err(|e| format!("读取文件失败: {}", e))
}

#[tauri::command]
async fn write_file(path: String, content: String) -> Result<(), String> {
    tokio::fs::write(&path, content)
        .await
        .map_err(|e| format!("写入文件失败: {}", e))
}

#[tauri::command]
fn save_credential(service: String, username: String, password: String) -> Result<(), String> {
    keyring::Entry::new(&service, &username)
        .set_password(&password)
        .map_err(|e| format!("保存凭证失败: {}", e))
}

#[tauri::command]
fn get_credential(service: String, username: String) -> Result<String, String> {
    keyring::Entry::new(&service, &username)
        .get_password()
        .map_err(|e| format!("获取凭证失败: {}", e))
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .manage(AppState {
            backend: Mutex::new(None),
        })
        .invoke_handler(tauri::generate_handler![
            start_backend,
            stop_backend,
            read_file,
            write_file,
            save_credential,
            get_credential,
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
```

**前端调用:**

```typescript
// src/services/tauri.ts
import { invoke } from "@tauri-apps/api/core";
import { listen } from "@tauri-apps/api/event";

export const tauriApi = {
	// 进程管理
	async startBackend() {
		return invoke<BackendStatus>("start_backend");
	},

	async stopBackend() {
		return invoke<void>("stop_backend");
	},

	// 文件操作
	async readFile(path: string) {
		return invoke<string>("read_file", { path });
	},

	async writeFile(path: string, content: string) {
		return invoke<void>("write_file", { path, content });
	},

	// 安全凭证
	async saveCredential(service: string, username: string, password: string) {
		return invoke<void>("save_credential", { service, username, password });
	},

	async getCredential(service: string, username: string) {
		return invoke<string>("get_credential", { service, username });
	},

	// 监听后端状态变化
	onBackendStatusChange(callback: (status: BackendStatus) => void) {
		return listen<BackendStatus>("backend-status", (event) => {
			callback(event.payload);
		});
	},
};
```

---

### 3. Rust ↔ SpringBoot 通信

#### 通信方式

| 方式         | 协议         | 场景           | 特点                   |
| ------------ | ------------ | -------------- | ---------------------- |
| HTTP         | HTTP/1.1     | 简单控制请求   | 简单、易实现           |
| gRPC         | HTTP/2       | 高频、类型安全 | 高性能、双向流         |
| 命名管道     | IPC          | 本地高速传输   | 最高性能、Windows 原生 |
| 标准输入输出 | stdin/stdout | 进程控制       | 最简单、适合启动参数   |

#### 主要职责

```
┌─────────────────────────────────────────────────────────────────┐
│                     Rust ↔ SpringBoot                           │
├─────────────────────────────────────────────────────────────────┤
│ ✅ 进程生命周期管理                                               │
│    - Rust 启动 SpringBoot 子进程                                 │
│    - 监控进程健康状态（心跳检测）                                  │
│    - 崩溃自动重启                                                │
│    - 优雅关闭（等待请求完成）                                      │
│                                                                 │
│ ✅ 大数据流传输                                                   │
│    - 10万+ 行结果集高速传输                                       │
│    - Apache Arrow 列式序列化                                     │
│    - 流式压缩/解压                                               │
│    - 断点续传                                                    │
│                                                                 │
│ ✅ 高性能计算协作                                                 │
│    - SQL 解析（Rust 比 Java 快 10x）                             │
│    - SQL 格式化                                                  │
│    - 语法高亮预处理                                               │
│    - 数据转换（Arrow ↔ JSON）                                    │
│                                                                 │
│ ✅ 安全协作                                                      │
│    - Rust 加密 → SpringBoot 解密                                 │
│    - 凭证安全传递                                                │
│    - 敏感数据脱敏                                                │
│                                                                 │
│ ✅ 资源协调                                                      │
│    - 端口动态分配                                                │
│    - 内存使用协调                                                │
│    - 连接池状态同步                                               │
└─────────────────────────────────────────────────────────────────┘
```

#### 实现示例

**方案 A: HTTP 简单协作（推荐起步）**

```rust
// Rust 端 - 进程管理 + 健康检查
use std::process::{Command, Child};
use std::sync::Mutex;
use reqwest::Client;

pub struct BackendManager {
    process: Mutex<Option<Child>>,
    client: Client,
    port: u16,
}

impl BackendManager {
    pub async fn start(&self) -> Result<(), String> {
        // 启动 SpringBoot
        let child = Command::new("./binaries/src-backend.exe")
            .arg(format!("--server.port={}", self.port))
            .spawn()
            .map_err(|e| e.to_string())?;

        *self.process.lock().unwrap() = Some(child);

        // 等待就绪
        self.wait_ready().await
    }

    async fn wait_ready(&self) -> Result<(), String> {
        for _ in 0..30 {
            if let Ok(resp) = self.client
                .get(format!("http://localhost:{}/actuator/health", self.port))
                .send()
                .await
            {
                if resp.status().is_success() {
                    return Ok(());
                }
            }
            tokio::time::sleep(tokio::time::Duration::from_millis(100)).await;
        }
        Err("Backend failed to start".to_string())
    }

    pub async fn health_check(&self) -> bool {
        self.client
            .get(format!("http://localhost:{}/actuator/health", self.port))
            .send()
            .await
            .map(|r| r.status().is_success())
            .unwrap_or(false)
    }
}
```

**方案 B: gRPC 高性能协作（大数据场景）**

```protobuf
// backend.proto
syntax = "proto3";

package tulipe;

service BackendService {
    // 大数据流传输
    rpc StreamQueryResult(QueryRequest) returns (stream DataRow);

    // SQL 解析协作
    rpc ParseSql(ParseRequest) returns (ParseResult);

    // 健康检查
    rpc HealthCheck(HealthRequest) returns (HealthResponse);
}

message QueryRequest {
    string connection_id = 1;
    string sql = 2;
    int32 batch_size = 3;
}

message DataRow {
    bytes arrow_data = 1;  // Apache Arrow 序列化
    bool is_last = 2;
}

message ParseRequest {
    string sql = 1;
}

message ParseResult {
    string ast_json = 1;
    repeated string tables = 2;
    repeated string columns = 3;
}
```

**SpringBoot gRPC 服务端:**

```kotlin
@GrpcService
class BackendGrpcService(
    private val queryService: QueryService,
    private val sqlParserService: SqlParserService
) : BackendServiceGrpcKt.BackendServiceCoroutineImplBase() {

    override fun streamQueryResult(request: QueryRequest): Flow<DataRow> = flow {
        val result = queryService.executeStreaming(
            connectionId = request.connectionId,
            sql = request.sql,
            batchSize = request.batchSize
        )

        result.collect { batch ->
            emit(DataRow.newBuilder()
                .setArrowData(batch.toArrowBytes())
                .setIsLast(batch.isLast)
                .build())
        }
    }

    override suspend fun parseSql(request: ParseRequest): ParseResult {
        val ast = sqlParserService.parse(request.sql)
        return ParseResult.newBuilder()
            .setAstJson(ast.toJson())
            .addAllTables(ast.tables)
            .addAllColumns(ast.columns)
            .build()
    }
}
```

---

## 职责分层

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              职责分层                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 前端层 (Vue3 + TypeScript)                                          │   │
│  │ ─────────────────────────────────────────────────────────────────── │   │
│  │ • UI 渲染与交互                                                     │   │
│  │ • 状态管理 (Pinia)                                                  │   │
│  │ • 路由控制                                                          │   │
│  │ • 调用 Rust (Tauri IPC) 或 SpringBoot (HTTP/WS)                    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                    ┌───────────────┴───────────────┐                       │
│                    ▼                               ▼                        │
│  ┌─────────────────────────────┐   ┌─────────────────────────────────────┐ │
│  │ Rust 层           │   │ SpringBoot 层                        │ │
│  │ ─────────────────────────── │   │ ─────────────────────────────────── │ │
│  │ • 文件系统操作              │   │ • 数据库连接管理 (JDBC)              │ │
│  │ • 系统级功能 (托盘/窗口)     │   │ • SQL 执行与结果处理                │ │
│  │ • 安全凭证 (密钥链)          │   │ • 业务逻辑处理                       │ │
│  │ • 大数据处理 (Arrow)         │   │ • AI 服务集成                       │ │
│  │ • SQL 解析/格式化            │   │ • 用户配置持久化                     │ │
│  │ • 进程管理 (启动后端)        │   │ • 数据库元数据管理                   │ │
│  │ • 剪贴板 (大数据)            │   │ • WebSocket 推送                    │ │
│  │ • 全局快捷键                 │   │                                     │ │
│  │ • 窗口控制                   │   │                                     │ │
│  └─────────────────────────────┘   └─────────────────────────────────────┘ │
│                    │                               ▲                        │
│                    └───────────────────────────────┘                        │
│                          进程管理 / gRPC 流式                                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 通信协议选择

### 按功能类型选择通信路径

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

---

## 实现示例

### 前端服务层设计

```typescript
// src/services/api.ts - 统一 API 入口
import { tauriApi } from "./tauri";
import { databaseApi } from "./database";
import { systemApi } from "./system";

export const api = {
	// 数据库相关 - 直接调用 SpringBoot
	database: databaseApi,

	// 系统相关 - 调用 Rust
	system: systemApi,

	// 自动选择通信路径
	async executeQuery(
		sql: string,
		connectionId: string,
		options?: QueryOptions,
	) {
		const estimatedSize = this.estimateResultSize(sql);

		if (estimatedSize > 100_000) {
			// 大数据：走 Rust 流式处理
			return tauriApi.streamQuery(sql, connectionId);
		} else {
			// 小数据：直接调用 SpringBoot
			return databaseApi.executeQuery(sql, connectionId);
		}
	},

	async parseSql(sql: string) {
		// SQL 解析：走 Rust（性能高 10x）
		return tauriApi.parseSql(sql);
	},

	async saveConnection(config: ConnectionConfig) {
		// 保存连接配置：业务逻辑走 SpringBoot
		const result = await databaseApi.saveConnection(config);

		// 密码加密：走 Rust 密钥链
		if (config.password) {
			await tauriApi.saveCredential(
				`db-${config.id}`,
				config.username,
				config.password,
			);
		}

		return result;
	},
};
```

---

## 配置指南

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

---

## 性能优化

### 通信性能对比

| 通信路径                 | 延迟   | 吞吐量 | 适用场景 |
| ------------------------ | ------ | ------ | -------- |
| 前端 → SpringBoot (HTTP) | 1-10ms | 中等   | 普通业务 |
| 前端 → Rust (IPC)        | < 1ms  | 高     | 高频交互 |
| Rust → SpringBoot (HTTP) | 1-5ms  | 高     | 进程管理 |
| Rust → SpringBoot (gRPC) | < 1ms  | 极高   | 大数据流 |

### 优化策略

1. **连接池**: HTTP 客户端使用连接池复用连接
2. **批量处理**: 小请求合并批量发送
3. **流式传输**: 大数据使用 gRPC 流式或 SSE
4. **缓存**: 频繁访问的数据在 Rust 层缓存
5. **压缩**: 大数据传输启用压缩

---

## 安全设计

### 安全层级

```
┌─────────────────────────────────────────────────────────────────┐
│                        安全层级                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Level 3: 系统密钥链 (Rust)                                      │
│  ─────────────────────────────────────────────────────────────  │
│  • 数据库密码                                                    │
│  • API Key                                                      │
│  • 加密密钥                                                      │
│                                                                 │
│  Level 2: 内存加密 (Rust)                                        │
│  ─────────────────────────────────────────────────────────────  │
│  • 运行时敏感数据                                                │
│  • 临时凭证                                                      │
│                                                                 │
│  Level 1: 传输加密 (HTTPS/gRPC TLS)                              │
│  ─────────────────────────────────────────────────────────────  │
│  • 前端 ↔ SpringBoot                                            │
│  • Rust ↔ SpringBoot                                            │
│                                                                 │
│  Level 0: 前端隔离                                               │
│  ─────────────────────────────────────────────────────────────  │
│  • WebView CSP                                                  │
│  • CORS 限制                                                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 安全最佳实践

1. **凭证管理**: 所有敏感凭证通过 Rust → 系统密钥链存储
2. **传输加密**: 生产环境使用 HTTPS/gRPC TLS
3. **输入验证**: 所有输入在 SpringBoot 层验证
4. **最小权限**: Rust 只暴露必要的系统功能
5. **审计日志**: 敏感操作记录审计日志

---

## 内部数据存储 (Rust rusqlite)

### 存储架构

Tulipe 作为数据库管理工具，需要存储自身的用户数据、配置信息等。采用 **Rust rusqlite + 系统密钥链** 的混合存储方案，确保 GraalVM Native Image 完全兼容：

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

### 数据分类存储策略

| 数据类型           | 存储位置      | 原因                   |
| ------------------ | ------------- | ---------------------- |
| 连接配置（除密码） | SQLite        | 结构化数据，需要查询   |
| 数据库密码         | 系统密钥链    | 安全要求最高           |
| 查询历史           | SQLite        | 需要全文搜索、时间排序 |
| 收藏的 SQL         | SQLite        | 需要分类、标签管理     |
| 用户设置           | SQLite        | 结构化配置             |
| AI API Key         | 系统密钥链    | 敏感信息               |
| 应用状态           | SQLite / 内存 | 快速恢复               |

### 为什么选择 SQLite？

| 优势             | 说明                                   |
| ---------------- | -------------------------------------- |
| **零配置**       | 无需安装、无需服务器进程               |
| **单文件**       | 整个数据库就是一个文件，易于备份和迁移 |
| **跨平台**       | Windows/macOS/Linux 都支持             |
| **轻量级**       | 体积小（< 1MB），启动快                |
| **GraalVM 兼容** | 可以编译到 Native Image 中             |
| **嵌入式**       | 适合桌面应用，无需网络                 |

### 技术实现

**SpringBoot + SQLite:**

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.4.0.Final")
}
```

```yaml
# application.yaml
spring:
  datasource:
    url: jdbc:sqlite:${user.home}/.tulipe/tulipe.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: update
```

**Rust 密钥链操作:**

```rust
// 保存密码到系统密钥链
#[tauri::command]
fn save_db_password(connection_id: String, password: String) -> Result<(), String> {
    keyring::Entry::new("tulipe-db", &connection_id)
        .set_password(&password)
        .map_err(|e| e.to_string())
}

// 从系统密钥链读取密码
#[tauri::command]
fn get_db_password(connection_id: String) -> Result<String, String> {
    keyring::Entry::new("tulipe-db", &connection_id)
        .get_password()
        .map_err(|e| e.to_string())
}
```

### 数据模型

```kotlin
// 数据库连接配置
@Entity
@Table(name = "db_connections")
data class DbConnection(
    @Id @GeneratedValue
    val id: Long = 0,
    val name: String,
    val dbType: String,  // mysql, postgresql, etc.
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    // 密码不存储在这里，存系统密钥链
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

// 查询历史
@Entity
@Table(name = "query_history")
data class QueryHistory(
    @Id @GeneratedValue
    val id: Long = 0,
    val connectionId: Long,
    val sql: String,
    val executionTime: Long,  // ms
    val rowCount: Int,
    val executedAt: LocalDateTime = LocalDateTime.now()
)

// 收藏的 SQL
@Entity
@Table(name = "saved_queries")
data class SavedQuery(
    @Id @GeneratedValue
    val id: Long = 0,
    val name: String,
    val sql: String,
    val tags: String,  // JSON array
    val folder: String?,  // 文件夹路径
    val createdAt: LocalDateTime = LocalDateTime.now()
)

// 用户设置
@Entity
@Table(name = "user_settings")
data class UserSettings(
    @Id
    val key: String,
    val value: String,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
```

### 数据操作流程

```
┌─────────────────────────────────────────────────────────────────┐
│                     数据操作流程                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. 保存数据库连接                                               │
│  ─────────────────────────────────────────────────────────────  │
│  前端 → SpringBoot: 连接配置（不含密码）                         │
│       → SQLite: 保存配置                                        │
│  前端 → Rust: 密码                                               │
│       → 系统密钥链: 保存密码                                     │
│                                                                 │
│  2. 获取连接列表                                                 │
│  ─────────────────────────────────────────────────────────────  │
│  前端 → SpringBoot: 请求连接列表                                 │
│       → SQLite: 查询配置                                        │
│       → 前端: 返回列表（不含密码）                               │
│                                                                 │
│  3. 连接数据库                                                   │
│  ─────────────────────────────────────────────────────────────  │
│  前端 → Rust: 请求密码                                           │
│       → 系统密钥链: 获取密码                                     │
│       → 前端: 返回密码                                           │
│  前端 → SpringBoot: 完整连接信息（含密码）                       │
│       → JDBC: 建立连接                                          │
│                                                                 │
│  4. 保存查询历史                                                 │
│  ─────────────────────────────────────────────────────────────  │
│  前端 → SpringBoot: 执行 SQL                                     │
│       → 目标数据库: 执行                                         │
│       → SQLite: 保存历史记录                                     │
│       → 前端: 返回结果                                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 备份与迁移

```kotlin
// 导出用户数据
@GetMapping("/export")
fun exportData(): ResponseEntity<ByteArray> {
    val connections = connectionRepository.findAll()
    val history = queryHistoryRepository.findAll()
    val savedQueries = savedQueryRepository.findAll()
    val settings = userSettingsRepository.findAll()

    val export = ExportData(
        connections = connections,
        history = history,
        savedQueries = savedQueries,
        settings = settings,
        exportTime = LocalDateTime.now()
    )

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tulipe-backup.json\"")
        .body(Json.encodeToString(export).toByteArray())
}

// 导入用户数据
@PostMapping("/import")
fun importData(@RequestBody data: ExportData) {
    connectionRepository.saveAll(data.connections)
    queryHistoryRepository.saveAll(data.history)
    savedQueryRepository.saveAll(data.savedQueries)
    userSettingsRepository.saveAll(data.settings)
}
```

---

## 总结

| 通信路径              | 主要职责                             | 通信方式       | 核心优势               |
| --------------------- | ------------------------------------ | -------------- | ---------------------- |
| **前端 → SpringBoot** | 业务逻辑、数据库操作、AI 功能        | HTTP/WebSocket | 简单直接、开发效率高   |
| **前端 → Rust**       | 文件系统、安全凭证、系统功能、大数据 | Tauri IPC      | 原生能力、高性能、安全 |
| **Rust → SpringBoot** | 进程管理、大数据流、高性能计算协作   | HTTP/gRPC      | 进程控制、流式传输     |

### 数据存储总结

| 存储类型     | 技术                | 数据                         | 优势                           |
| ------------ | ------------------- | ---------------------------- | ------------------------------ |
| **内部数据** | Rust rusqlite       | 连接配置、查询历史、用户设置 | GraalVM 兼容、高性能、职责分离 |
| **敏感数据** | 系统密钥链          | 数据库密码、API Key          | 最高安全性、系统集成           |
| **目标数据** | JDBC + 各数据库驱动 | 用户业务数据                 | 复用生态、全面支持             |

这种架构让每一层都专注于自己最擅长的事情，最大化开发效率和运行性能。
