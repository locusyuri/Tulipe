# Tulipe 前端架构与设计手册 (AI-Ready)

本文档旨在为开发人员及 AI 助手提供 Tulipe 前端的深度设计说明，确保能够快速接手并持续扩展本项目。

---

## 1. 设计哲学与核心理念

Tulipe 是一款面向现代开发者的数据库管理工具。其前端设计遵循：

- **极简主义**: 移除所有干扰项，专注于数据和查询。
- **极致响应**: 利用 Tauri 的原生能力和 Vue 3 的高效渲染，确保百万级数据不卡顿。
- **沉浸式工作区**: 采用三栏式可调布局，模拟现代 IDE（如 VS Code）的操作逻辑。

---

## 2. 技术栈说明

| 组件     | 选型                   | 作用                                                         |
| :------- | :--------------------- | :----------------------------------------------------------- |
| **容器** | Tauri 2.x              | 跨平台桌面外壳，提供系统级权限和 Sidecar 进程管理。          |
| **框架** | Vue 3 (Script Setup)   | 核心 UI 逻辑，响应式数据驱动。                               |
| **样式** | Tailwind CSS           | 快速构建现代 UI，配合 Shadcn-vue 规范组件。                  |
| **表格** | AG Grid (Community)    | 核心数据展示，支持无限滚动和单元格实时编辑。                 |
| **状态** | Pinia                  | 跨页面连接池管理、UI 布局状态持久化。                        |
| **通信** | Tauri Invoke / Reqwest | 前端通过 Tauri Command 调用 Rust 层，Rust 作为代理访问后端。 |

---

## 3. 界面布局架构

### 3.1 欢迎页 (WelcomePage.vue)

**布局逻辑**:

- **顶部连接池**: 显示当前活跃（已连接）的数据库卡片，支持一键断开或快速进入。
- **左侧分类树**: 用户可自定义文件夹（如“生产”、“开发”），支持连接的拖拽分组。
- **右侧库视图**: 展示所有保存的连接卡片，通过图标颜色区分数据库类型（MySQL, PG, Redis, etc.）。

### 3.2 主工作区 (MainWorkspace.vue)

**三栏式可调布局 (Resizable Layout)**:

1. **固定侧边栏 (72px)**: 仅显示数据库图标方块，最省空间的展示方式。
2. **对象导航栏 (Resizable)**:
   - 顶部标签页：水平平滑滚动，包含 `Tables`, `Views`, `Functions`, `Queries`, `Backups` 等。
   - 内容区：树状或列表展示选定分类下的数据库对象。
3. **内容工作区 (Flexible)**:
   - 顶部多标签页：管理打开的表数据、SQL 查询窗口。
   - 主体：集成 AG Grid 或 CodeMirror 6 编辑器。
4. **详情/AI 面板 (Resizable)**: 右侧侧边栏，提供对象属性查看及 AI 助手交互。

### 3.3 顶部状态栏 (Status Bar)

- **居中设计**: 显眼展示当前连接的数据库名称及主机地址。
- **状态灯**: 绿色呼吸灯表示后端连接正常。
- **快捷操作**: 内置“编辑连接”和“快速切换”按钮。切换按钮弹出层级菜单，无需返回欢迎页即可跳转。

---

## 4. 多数据库适配器设计

Tulipe 支持多种数据库类型（关系型、KV、图数据库、时序数据库等），每种数据库的数据模型和展示方式完全不同。因此采用 **"统一壳子 + 可变适配器"** 的架构。

### 4.1 不变与可变分析

| 层级 | 不变（壳子） | 可变（内容） |
|:-----|:------------|:------------|
| **整体结构** | 连接信息栏 + 主内容区 + 详情面板 | 各栏的具体组件 |
| **布局框架** | 四栏可拖拽调整布局 | 栏的数量和位置 |
| **交互逻辑** | Tab 管理、选中状态、拖拽 | 标签类型、列表内容 |

**不同数据库的差异示例**：

| 数据库 | 栏① | 栏②（对象导航） | 栏③（数据） | 栏④（详情） |
|:------|:----|:----------------|:------------|:------------|
| MySQL | ✅ 多个DB | ✅ Tables/Functions | ✅ 表数据表格 | ✅ 元信息 |
| Redis | ✅ 多个DB | ❌ 不需要 | ✅ Key列表 | ✅ Key属性 |
| Neo4j | ✅ 多个DB | ❌ 不需要 | ✅ 图可视化 | ✅ 节点属性 |
| InfluxDB | ✅ 多个DB | ❌ 不需要 | ✅ 时序图表 | ✅ 指标信息 |

### 4.2 核心接口设计

每个数据库类型实现一个 `DatabaseAdapter`，返回对应的组件：

```typescript
// src/types/DatabaseAdapter.ts

import type { Component } from 'vue';

/**
 * 数据库适配器接口
 * 每个数据库类型实现此接口，提供自己的 UI 组件
 */
export interface DatabaseAdapter {
  /** 适配器唯一标识 */
  readonly id: string;
  
  /** 数据库类型 */
  readonly dbType: string;
  
  /**
   * 获取对象导航组件（可选，返回 null 则不显示左侧栏）
   * 例如：MySQL 返回 Tables/Functions 导航面板
   *      Redis 返回 null（不需要对象导航）
   */
  getNavigator(): Component | null;
  
  /**
   * 获取主内容区组件（必选）
   * 这是数据展示的核心区域
   * 例如：MySQL 返回表格组件，Redis 返回 Key-Value 组件，Neo4j 返回图可视化
   */
  getMainPanel(): Component;
  
  /**
   * 获取详情面板组件（可选，返回 null 则不显示右侧栏）
   * 例如：MySQL 返回表结构详情，Redis 返回 Key 属性详情
   */
  getDetailsPanel(): Component | null;
}
```

### 4.3 适配器注册与使用

```typescript
// src/components/DatabaseAdapterRegistry.ts

import type { DatabaseAdapter } from '@/types/DatabaseAdapter';

class AdapterRegistry {
  private adapters = new Map<string, DatabaseAdapter>();
  
  register(adapter: DatabaseAdapter): void {
    this.adapters.set(adapter.dbType, adapter);
  }
  
  get(dbType: string): DatabaseAdapter | undefined {
    return this.adapters.get(dbType);
  }
  
  /** 获取或回退到默认适配器 */
  getOrDefault(dbType: string): DatabaseAdapter {
    return this.adapters.get(dbType) ?? this.adapters.get('mysql')!;
  }
}

export const adapterRegistry = new AdapterRegistry();
```

### 4.4 组件使用方式

```vue
<!-- MainWorkspace.vue -->
<template>
  <div class="workspace">
    <!-- 连接信息栏（始终显示） -->
    <ConnectionHeader :connection="connection" />
    
    <!-- 对象导航（可选） -->
    <component 
      v-if="adapter.getNavigator()"
      :is="adapter.getNavigator()"
      :connection="connection"
      @select="handleSelect"
    />
    
    <!-- 主内容区（必选） -->
    <component 
      :is="adapter.getMainPanel()"
      :connection="connection"
      :selected-item="selectedItem"
    />
    
    <!-- 详情面板（可选） -->
    <component 
      v-if="adapter.getDetailsPanel()"
      :is="adapter.getDetailsPanel()"
      :item="selectedItem"
    />
  </div>
</template>
```

### 4.5 各数据库适配器实现

| 数据库 | Navigator | MainPanel | DetailsPanel |
|:------|:----------|:----------|:-------------|
| **MySQL** | NavTree (Tables/Functions) | TableGrid | TableSchema |
| **PostgreSQL** | NavTree (Tables/Functions/Procedures) | TableGrid | TableSchema |
| **Redis** | `null` | KeyValueList | KeyInspector |
| **Neo4j** | `null` | GraphVisualizer | NodeInspector |
| **InfluxDB** | `null` | TimeSeriesChart | MetricDetails |
| **MongoDB** | CollectionTree | DocumentViewer | DocumentInspector |

### 4.6 文件结构建议

```
src/
├── types/
│   └── DatabaseAdapter.ts           # 适配器接口定义
│
├── components/
│   ├── DatabaseWorkspace/
│   │   ├── DatabaseWorkspace.vue    # 主壳子组件
│   │   ├── ConnectionHeader.vue     # 连接信息栏
│   │   └── adapters/
│   │       ├── AdapterRegistry.ts   # 适配器注册表
│   │       ├── MySQLAdapter.ts      # MySQL 适配器
│   │       ├── RedisAdapter.ts      # Redis 适配器
│   │       ├── Neo4jAdapter.ts      # Neo4j 适配器
│   │       └── ...
│   │
│   ├── navigators/                  # 对象导航组件
│   │   ├── RelationalNav.vue        # 关系型数据库导航
│   │   └── CollectionNav.vue       # MongoDB 集合导航
│   │
│   ├── main-panels/                 # 主内容区组件
│   │   ├── TableGrid.vue           # 表格组件
│   │   ├── KeyValueList.vue        # Redis Key 列表
│   │   ├── GraphVisualizer.vue     # 图可视化
│   │   └── TimeSeriesChart.vue     # 时序图表
│   │
│   └── details-panels/              # 详情面板组件
│       ├── TableSchema.vue          # 表结构详情
│       ├── KeyInspector.vue         # Key 属性详情
│       └── NodeInspector.vue        # 节点详情
```

### 4.7 设计原则

1. **接口最小化**：适配器只定义返回什么组件，不限制组件内部实现
2. **壳子不变**：工作区布局逻辑保持不变，只根据适配器动态组合组件
3. **按需加载**：使用 `defineAsyncComponent` 延迟加载非当前数据库的组件
4. **向后兼容**：关系型数据库优先实现，适配器系统为后续扩展预留

---

## 5. 实施路线图 (Implementation Roadmap)

### 第一阶段：基础构建 (当前进度: 100%)

- [x] Tauri + Vue 3 项目骨架搭建
- [x] 三层架构（Tauri -> Rust Proxy -> SpringBoot）连通
- [x] 欢迎页重构（文件夹分类、连接池卡片）
- [x] 主界面可调宽度布局及状态栏设计
- [x] Rust 后端日志与测试接口连通
- [x] MainWorkspace 重构：多数据库适配器架构

### 第二阶段：核心功能 (进行中)

- [ ] **AG Grid 深度集成**: 实现从后端流式加载数据，支持无限滚动。
- [ ] **SQL 编辑器**: 集成 CodeMirror 6，支持语法高亮和自动补全。
- [ ] **连接管理持久化**: 将用户定义的文件夹和连接保存到本地配置文件。
- [ ] **后端交互**: 完善 Rust 层与 SpringBoot 之间的 JSON-RPC 或 REST 调用。

### 第三阶段：AI 增强 (待启动)

- [ ] **AI 侧边栏**: 实时分析选定表的结构，提供优化建议。
- [ ] **自然语言转 SQL**: 在工作区直接通过对话生成查询语句。
- [ ] **智能纠错**: 自动解析 SQL 执行错误并给出修复方案。

---

## 6. 给 AI 接手者的开发者指南

如果你是接手此项目的 AI，请注意以下关键点：

### 6.1 核心文件路径

- **页面**: `src/pages/` (核心逻辑：`WelcomePage.vue`, `MainWorkspace.vue`)
- **样式**: `src/assets/main.css` (全局 Tailwind 基础配置)
- **后端指令**: `src-tauri/src/lib.rs` (定义了所有暴露给前端的 `#[tauri::command]`)
- **配置文件**: `src-tauri/tauri.conf.json` (窗口尺寸、权限、Sidecar 配置)

### 6.2 命名规范

- **Vue 组件**: 大驼峰 (PascalCase)，如 `DatabaseList.vue`。
- **CSS 类**: Tailwind 优先，复杂逻辑使用 BEM 变体。
- **状态变量**: 小驼峰 (camelCase)，宽度变量以 `Width` 结尾，如 `objectNavWidth`。

### 6.3 交互逻辑约定

- 所有需要访问后端的操作必须通过 `src-tauri/src/lib.rs` 中的 Rust 函数转发。
- 耗时操作（如测试连接）必须在前端显示 `loading` 状态并提供显式的失败/成功反馈（使用已实现的 Toast 系统）。
- 窗口状态（如侧边栏宽度）应考虑在 `onMounted` 时从本地存储恢复。

---

_最后更新日期: 2026-04-16_
