# Tulipe 项目任务清单

> 最后更新: 2026-04-23
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

### 3.1 [高优先级] MainWorkspace 重构 - 多数据库适配器架构

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
- [ ] MySQL 连接显示 Tables/Functions 导航 + 表格数据
- [ ] PostgreSQL 连接显示 Tables/Functions/Schemas 导航 + 表格数据
- [ ] Redis 连接直接显示 Key-Value 列表（无需导航栏）
- [ ] Neo4j 连接显示图可视化界面
- [ ] 切换数据库类型时，主界面正确切换

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
│       ├── MainWorkspace.vue          # 主壳子（不变）
│       ├── adapters/
│       │   ├── AdapterRegistry.ts    # 适配器注册表
│       │   ├── RelationalAdapter.ts   # 关系型数据库适配器
│       │   ├── RedisAdapter.ts        # Redis 适配器
│       │   ├── Neo4jAdapter.ts        # Neo4j 适配器
│       │   └── InfluxDbAdapter.ts     # InfluxDB 适配器
│       ├── navigators/
│       │   ├── RelationalNav.vue     # 关系型导航
│       │   └── CollectionNav.vue     # MongoDB 导航
│       ├── main-panels/
│       │   ├── TableGrid.vue         # 表格组件
│       │   ├── KeyValueList.vue      # Redis Key 列表
│       │   ├── GraphVisualizer.vue   # 图可视化
│       │   └── TimeSeriesChart.vue   # 时序图表
│       └── details-panels/
│           ├── TableSchema.vue        # 表结构详情
│           ├── KeyInspector.vue       # Key 属性详情
│           └── NodeInspector.vue      # 节点详情
```

**任务拆解**:

1. **阶段一: 接口定义**
   - [ ] 定义 `DatabaseAdapter` 接口
   - [ ] 创建 `AdapterRegistry` 注册表
   - [ ] 定义适配器与数据库类型的映射

2. **阶段二: 关系型数据库适配器**
   - [ ] 实现 `RelationalAdapter`
   - [ ] 创建 `RelationalNav` 导航组件（支持 Tables/Functions/Views）
   - [ ] PostgreSQL 需额外支持 Schemas 导航

3. **阶段三: NoSQL/图数据库适配器**
   - [ ] 实现 `RedisAdapter`（无导航，直接 Key-Value 列表）
   - [ ] 实现 `Neo4jAdapter`（图可视化）
   - [ ] 实现 `InfluxDbAdapter`（时序图表）
   - [ ] 实现 `MongoDBAdapter`（Collection 树 + 文档查看器）

4. **阶段四: MainWorkspace 重构**
   - [ ] 修改 MainWorkspace.vue，动态加载适配器
   - [ ] 实现适配器切换逻辑
   - [ ] 状态保持与恢复

5. **阶段五: 测试与优化**
   - [ ] 各数据库类型功能测试
   - [ ] 性能优化（按需加载）
   - [ ] 边界情况处理

**预计工作量**: 2-3 周

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

---

## 六、备注

- 所有前端变更需遵循项目的命名规范（Vue 组件使用 PascalCase，状态变量使用 camelCase）
- 耗时操作需显示 loading 状态和明确的反馈
- 窗口状态应考虑在 `onMounted` 时从本地存储恢复
