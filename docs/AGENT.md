# Tulipe 项目构建指南 (AI 专用)

本指南旨在帮助 AI 助手理解 Tulipe 项目的三层通信架构及构建流程。

## 核心架构

Tulipe 采用创新的三层通信架构，每一层专注于自己最擅长的领域：

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Tulipe 三层通信架构                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────────────┐                                                   │
│   │   前端 (Vue3 + TS)   │  ← 业务逻辑、UI 渲染、状态管理                     │
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

### 各层职责

| 层级        | 技术栈                              | 核心职责                                 | 通信方式                                        |
| ----------- | ----------------------------------- | ---------------------------------------- | ----------------------------------------------- |
| **前端层**  | Vue3 + TypeScript + Tauri WebView   | UI 渲染、状态管理、业务逻辑编排          | HTTP/WebSocket → SpringBoot<br>Tauri IPC → Rust |
| **Rust 层** | Rust 1.75+ + Tauri                  | 进程管理、文件系统、安全凭证、大数据处理 | Tauri IPC ← 前端<br>HTTP/gRPC ↔ SpringBoot      |
| **后端层**  | SpringBoot 3.2.4 + Kotlin + GraalVM | 数据库连接、JDBC 驱动、AI 服务、业务逻辑 | HTTP/WebSocket ← 前端<br>HTTP/gRPC ← Rust       |

### 通信路径决策

| 功能类型           | 通信路径                 | 原因                      |
| ------------------ | ------------------------ | ------------------------- |
| 数据库连接管理     | 前端 → SpringBoot        | 业务逻辑，无需系统权限    |
| SQL 执行（小数据） | 前端 → SpringBoot        | 简单直接，JDBC 在 Java 端 |
| SQL 执行（大数据） | 前端 → Rust → SpringBoot | Rust 处理流式传输更高效   |
| SQL 解析/格式化    | 前端 → Rust              | Rust 解析性能高 10x       |
| 文件读写           | 前端 → Rust              | 需要系统权限              |
| 密码存储           | 前端 → Rust              | 需要系统密钥链            |
| 系统托盘/窗口      | 前端 → Rust              | 原生系统功能              |
| 进程管理           | Rust → SpringBoot        | Rust 负责生命周期         |

## 构建流程

### 1. 后端构建 (Native Image)

这是最耗时的步骤，将 Java 应用编译为本地可执行文件。

- **环境要求**:
  - GraalVM (推荐 JDK 21), Native Image 组件。
  - **Windows 必须**: Visual Studio Build Tools (C++ 工作负载) 以及正确的 x64 Native Tools 环境变量。
- **构建方式**:
  - 推荐使用提供的 [build_native.ps1](file:///c:/Programs/Tulipe/src-backend/build_native.ps1) 脚本，它会自动加载 MSVC 环境并执行构建。
  ```powershell
  cd src-backend
  .\build_native.ps1
  ```
- **输出位置**: `src-backend/build/native/nativeCompile/src-backend.exe`。

### 2. 资源分发

构建完成后，需要将生成的二进制文件放入 Tauri 能够识别的路径。

- **目标目录**: `src-tauri/binaries/x86_64-pc-windows-msvc/`。
- **重命名**: 生成的文件名通常与项目名一致，需确保与 `tauri.conf.json` 中的 `externalBin` 配置匹配。

### 3. Tauri 打包

最后一步是将前端资源和后端二进制文件打包进同一个安装包。

- **构建命令**:
  ```powershell
  npm run tauri build
  ```
- **输出位置**: `src-tauri/target/release/bundle/`。

## 完整构建脚本

```bash
#!/bin/bash
# scripts/build.sh

set -e

echo "=== Tulipe 构建脚本 ==="

# 1. 构建前端
echo "[1/4] 构建前端..."
npm run build

# 2. 构建后端 Native Image
echo "[2/4] 构建 SpringBoot Native Image..."
cd src-backend
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    .\build_native.ps1
else
    ./gradlew nativeCompile
fi
cd ..

# 3. 复制二进制文件
echo "[3/4] 复制二进制文件到 Tauri 资源目录..."
mkdir -p src-tauri/binaries/x86_64-pc-windows-msvc
cp src-backend/build/native/nativeCompile/src-backend.exe \
   src-tauri/binaries/x86_64-pc-windows-msvc/

# 4. 构建 Tauri 应用
echo "[4/4] 构建 Tauri 应用..."
cd src-tauri
cargo tauri build

echo "=== 构建完成 ==="
echo "输出目录: src-tauri/target/release/bundle/"
```

## 开发环境配置

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
{
	"security": {
		"csp": "default-src 'self'; connect-src 'self' http://localhost:* ws://localhost:* https://api.openai.com; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"
	}
}
```

### Tauri 权限配置

```json
{
	"identifier": "default",
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

## 应用启动流程

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

## 注意事项

1. **进程管理**: Rust 代码通过 `BackendState` 管理后端进程的生命周期。
2. **通信方式**:
   - 前端 ↔ Rust: Tauri IPC (延迟 < 1ms)。
   - 前端 ↔ 后端: HTTP/2 或 WebSocket (localhost)。
   - Rust ↔ 后端: HTTP/gRPC (进程管理、大数据流)。
3. **性能**: Native Image 启动时间应在 50-150ms 以内，内存占用约 30-50MB。
4. **端口管理**: 建议使用动态端口分配，避免端口冲突。
5. **CORS 配置**: 必须正确配置 SpringBoot CORS，允许 Tauri WebView 的 origin。

## 常见问题

### Q: 前端如何知道后端已经启动？

A: 前端应实现轮询或 WebSocket 连接，等待后端健康检查端点返回成功。

### Q: 大数据查询如何处理？

A: 小数据（< 1MB）直接走前端 → SpringBoot；大数据（> 10MB）走前端 → Rust → SpringBoot gRPC 流式传输。

### Q: 敏感信息如何存储？

A: 采用混合存储策略：

- **普通数据**（连接配置、查询历史等）→ SQLite（通过 Rust rusqlite）
- **敏感信息**（数据库密码、API Key）→ 系统密钥链（通过 Rust keyring）

### Q: 内部数据存储在哪里？

A: Tulipe 使用 SQLite 存储内部数据：

- **位置**: `${user.home}/.tulipe/tulipe.db`
- **技术**: Rust rusqlite crate（bundled SQLite）
- **数据**: 连接配置、查询历史、收藏的 SQL、用户设置
- **敏感数据**: 密码存储在系统密钥链，不在 SQLite 中

### Q: 为什么 SQLite 访问放在 Rust 层？

A: 因为 GraalVM Native Image 对 JNI 支持有限：

- **SQLite JDBC 问题**: 使用 JNI 调用 native C 库，需要复杂配置
- **Rust rusqlite 优势**: 使用 bundled SQLite，编译时静态链接，无 JNI 问题
- **性能更好**: Rust 直接访问比 Java JDBC 快 5-10 倍
- **职责分离**: SpringBoot 专注于业务逻辑，无需处理配置存储

### Q: 如何调试三层通信？

A:

- 前端 ↔ SpringBoot: 使用浏览器开发者工具 Network 面板
- 前端 ↔ Rust: 使用 Tauri 开发者工具 Console
- Rust ↔ SpringBoot: 查看 Rust 和后端的日志输出
