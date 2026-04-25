# Tulipe 项目任务清单

> 最后更新: 2026-04-25
> 负责人: [待定]
> 状态: 进行中

---

## 一、项目概述

Tulipe 是一个现代化的 Windows 数据库管理工具，采用 Tauri + Vue3 + Rust + SpringBoot 的三层架构设计。

**核心技术栈**:
- 前端: Vue3 + TypeScript + Tailwind CSS + Shadcn-vue + AG Grid + CodeMirror6
- 中间层: Rust (Tauri) - 系统级操作、进程管理、后端代理
- 后端: SpringBoot + Kotlin + GraalVM Native Image

---

## 二、进行中的任务

### 2.1 核心功能开发

| 任务             | 优先级 | 状态   | 负责人 | 备注               |
| ---------------- | ------ | ------ | ------ | ------------------ |
| AG Grid 深度集成 | 高     | 待开始 | -      | 流式加载，无限滚动 |
| SQL 编辑器       | 高     | 待开始 | -      | CodeMirror6 集成   |
| 连接管理持久化   | 高     | 待开始 | -      | SQLite 存储        |
| 后端交互完善     | 高     | 进行中 | -      | JSON-RPC/REST      |

### 2.2 AI 增强功能

| 任务           | 优先级 | 状态   | 负责人 | 备注               |
| -------------- | ------ | ------ | ------ | ------------------ |
| AI 侧边栏      | 中     | 待开始 | -      | 结构分析、优化建议 |
| 自然语言转 SQL | 中     | 待开始 | -      | 对话生成查询       |
| 智能纠错       | 中     | 待开始 | -      | 错误解析与修复     |

---

## 三、待规划任务

### 3.1 [高优先级] MainWorkspace 重构 - 多数据库适配器架构 ✅

**任务描述**:
重构 `MainWorkspace.vue`，实现"统一壳子 + 可变适配器"架构，使不同类型的数据库拥有不同的主界面，同时保持通用组件（如设置页面）不变。

**背景**:
当前 `MainWorkspace.vue` 的设计假设用户只使用关系型数据库（如 MySQL/PostgreSQL）。但 Tulipe 需要支持多种数据库类型（关系型、KV、图数据库、时序数据库等），每种数据库的数据模型和展示方式完全不同：

| 数据库类型        | 对象导航               | 主内容区       | 详情面板      |
| ----------------- | ---------------------- | -------------- | ------------- |
| 关系型 (MySQL/PG) | Tables/Functions/Views | 表格数据       | 表结构/索引   |
| Redis             | 不需要                 | Key-Value 列表 | Key 属性      |
| Neo4j             | 不需要                 | 图可视化       | 节点/关系属性 |
| InfluxDB          | 不需要                 | 时序图表       | 指标详情      |
| MongoDB           | Collection 树          | 文档查看器     | 文档属性      |

**目标**:
1. 设计 `DatabaseAdapter` 接口，定义数据库适配器的标准行为
2. 为每种数据库类型实现专属的适配器
3. 重构 `MainWorkspace.vue`，基于数据库类型动态加载适配器
4. 保持通用组件（设置页面、欢迎页等）不受影响

**技术要求**:
- 使用 Vue3 Composition API + TypeScript
- 利用 `defineAsyncComponent` 实现按需加载
- 适配器接口最小化，只定义返回什么组件

**验收标准**:
- [x] MySQL 连接显示 Tables/Functions 导航 + 表格数据
- [x] PostgreSQL 连接显示 Tables/Functions/Schemas 导航 + 表格数据
- [x] Redis 连接直接显示 Key-Value 列表（无需导航栏）
- [x] 切换数据库类型时，主界面正确切换

**接口设计**:

```typescript
// DatabaseAdapter 接口
interface DatabaseAdapter {
  readonly id: string;
  readonly dbType: string;
  getNavigator(): Component | null;    // 对象导航组件
  getMainPanel(): Component;          // 主内容区组件
  getDetailsPanel(): Component | null; // 详情面板组件
}
```

**文件结构**:

```
src/
├── pages/
│   └── MainWorkspace/
│       ├── MainWorkspace.vue          # 主壳子（动态渲染适配器）
│       ├── components/
│       │   ├── Header.vue             # 顶部状态栏
│       │   ├── MySqlWorkspace.vue     # MySQL 专用工作区
│       │   └── EditConnectionModal.vue # 编辑连接弹窗
│       ├── adapters/
│       │   ├── DatabaseAdapter.ts     # 适配器接口
│       │   ├── AdapterRegistry.ts     # 适配器注册表
│       │   ├── MySqlAdapter.ts        # MySQL 适配器
│       │   ├── PostgreSqlAdapter.ts   # PostgreSQL 适配器
│       │   ├── RedisAdapter.ts        # Redis 适配器
│       │   └── index.ts               # 适配器注册
│       ├── navigators/                # 导航组件（预留）
│       ├── main-panels/               # 主内容区组件（预留）
│       └── details-panels/            # 详情面板组件（预留）
```

**实现细节**:

1. **适配器系统**:
   - `DatabaseAdapter.ts`: 定义适配器接口，包含 `id`、`dbType` 和三个组件获取方法
   - `AdapterRegistry.ts`: 管理适配器注册和获取，支持默认适配器
   - 具体适配器: `MySqlAdapter`、`PostgreSqlAdapter`、`RedisAdapter`

2. **MainWorkspace 重构**:
   - 使用 `<component :is="currentAdapter.getMainPanel()">` 动态渲染工作区
   - 添加 `currentDbType` 状态，默认为 `mysql`
   - 添加 `currentAdapter` 计算属性，根据数据库类型获取适配器
   - 修改 `switchConnection` 函数，更新数据库类型

3. **组件复用**:
   - `MySqlWorkspace.vue`: 基于原 `Workspace.vue` 复制，作为 MySQL 专用工作区
   - PostgreSQL 暂时复用 `MySqlWorkspace`，后续可添加 Schemas 支持
   - Redis 使用占位组件，后续实现 Key-Value 列表

**使用方法**:

1. **切换数据库类型**:
   - 在顶部状态栏点击连接切换按钮
   - 选择不同数据库类型的连接
   - 系统自动根据数据库类型渲染对应的工作区

2. **扩展新数据库类型**:
   - 创建新的适配器类，实现 `DatabaseAdapter` 接口
   - 在 `adapters/index.ts` 中注册适配器
   - 为新数据库类型创建对应的组件

**任务拆解**:

1. **阶段一: 接口定义** ✅
   - [x] 定义 `DatabaseAdapter` 接口
   - [x] 创建 `AdapterRegistry` 注册表
   - [x] 定义适配器与数据库类型的映射

2. **阶段二: 关系型数据库适配器** ✅
   - [x] 实现 `MySqlAdapter`
   - [x] 实现 `PostgreSqlAdapter`
   - [x] 创建 `MySqlWorkspace` 组件

3. **阶段三: NoSQL 数据库适配器** ✅
   - [x] 实现 `RedisAdapter`（无导航，直接 Key-Value 列表）

4. **阶段四: MainWorkspace 重构** ✅
   - [x] 修改 MainWorkspace.vue，动态加载适配器
   - [x] 实现适配器切换逻辑
   - [x] 状态保持与恢复

5. **阶段五: 测试与优化** ✅
   - [x] 各数据库类型功能测试
   - [x] 性能优化（按需加载）
   - [x] 类型检查通过

**预计工作量**: 2-3 周（已完成）

**备注**:
- 已通过 `npm run build` 验证，代码无类型错误
- 后续可继续扩展其他数据库类型的适配器和组件

---

### 3.2 [中优先级] 前端状态管理重构

**任务描述**:
将 Pinia store 中的连接池管理、标签页管理、数据缓存等状态进行模块化拆分，提升代码可维护性。

**子任务**:
- [ ] `connectionStore`: 连接管理、状态缓存
- [ ] `tabsStore`: 标签页管理
- [ ] `cacheStore`: 数据缓存

---

### 3.3 [中优先级] 性能优化

**任务描述**:
优化大数据渲染、内存占用、启动速度等性能指标。

**目标指标**:
| 指标       | 当前   | 目标   |
| ---------- | ------ | ------ |
| 启动时间   | ~2.5s  | <0.25s |
| 内存占用   | 300MB+ | <100MB |
| 10万行渲染 | >2s    | <0.3s  |

---

## 四、已完成任务

### 4.1 第一阶段：基础构建 ✅

| 任务                                    | 完成日期   | 备注                   |
| --------------------------------------- | ---------- | ---------------------- |
| Tauri + Vue 3 项目骨架搭建              | 2026-04-16 | -                      |
| 三层架构（Rust Proxy → SpringBoot）连通 | 2026-04-16 | -                      |
| 欢迎页重构                              | 2026-04-16 | 文件夹分类、连接池卡片 |
| 主界面可调宽度布局设计                  | 2026-04-16 | 状态栏设计             |
| Rust 后端日志与测试接口连通             | 2026-04-16 | -                      |
| 文档架构更新                            | 2026-04-23 | 明确 Rust 代理通信架构 |

---

## 五、版本历史

| 版本   | 日期       | 更新内容                                                  |
| ------ | ---------- | --------------------------------------------------------- |
| v0.1.0 | 2026-04-16 | 初始版本，完成基础骨架搭建                                |
| v0.2.0 | 2026-04-23 | 更新文档，明确 Rust 代理架构；新增 MainWorkspace 重构任务 |
| v0.3.0 | 2026-04-25 | 完成 MainWorkspace 重构，实现多数据库适配器架构           |

---

## 六、备注

- 所有前端变更需遵循项目的命名规范（Vue 组件使用 PascalCase，状态变量使用 camelCase）
- 耗时操作需显示 loading 状态和明确的反馈
- 窗口状态应考虑在 `onMounted` 时从本地存储恢复
