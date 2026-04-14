# Tulipe 前端架构设计

## 目录

1. [架构概述](#架构概述)
2. [页面结构设计](#页面结构设计)
3. [AG Grid 集成方案](#ag-grid-集成方案)
4. [状态管理](#状态管理)
5. [特殊数据库界面](#特殊数据库界面)
6. [性能优化](#性能优化)
7. [实施路线图](#实施路线图)

---

## 架构概述

### 技术栈

- **框架**: Vue 3.5.13 + TypeScript 5.6.2
- **UI 组件**: Shadcn-vue + Tailwind CSS
- **表格组件**: AG Grid (社区版 MIT)
- **编辑器**: CodeMirror6
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **容器**: Tauri 2.x WebView

### 设计原则

1. **用户体验优先**: 清晰的层级结构，流畅的交互体验
2. **性能优化**: 虚拟化、懒加载、缓存策略
3. **可扩展性**: 模块化设计，支持插件化扩展
4. **状态持久化**: 连接状态保存/恢复，无缝切换

---

## 页面结构设计

### 总体布局

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

### 1. 欢迎页 (WelcomePage.vue)

**功能定位**:
- 数据库连接管理
- 连接历史记录
- 快速连接向导

**UI 组件**:
```vue
<template>
  <div class="welcome-container">
    <!-- 连接列表 -->
    <ConnectionList 
      :connections="connections" 
      @select="handleSelect"
      @edit="handleEdit"
      @delete="handleDelete"
    />
    
    <!-- 新建连接表单 -->
    <ConnectionForm 
      @create="handleCreate"
      @cancel="handleCancel"
    />
    
    <!-- 历史连接 -->
    <HistoryPanel 
      :history="history" 
      @restore="handleRestore"
    />
  </div>
</template>

<script setup>
const connectionStore = useConnectionStore();
const { connections, history } = storeToRefs(connectionStore);

function handleSelect(connection) {
  connectionStore.selectConnection(connection);
  router.push('/main');
}
</script>
```

**设计要点**:
- 卡片式布局，每个连接显示图标、名称、类型、主机信息
- 右键菜单：编辑、删除、复制连接
- 搜索过滤：按名称、类型、主机快速筛选
- 最近连接：显示最近使用的 5-10 个连接

### 2. 主界面 (MainWorkspace.vue)

**分层结构**:
```
┌─────────────────────────────────────────────────────────────────┐
│                    主界面布局 (三栏式)                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐       │
│  │ 数据库列表     │  │ 对象导航       │  │ 详情/工作区    │       │
│  │ (DatabaseList)│  │ (ObjectNav)    │  │ (DetailPanel) │       │
│  │               │  │               │  │               │       │
│  │ • 数据库1     │  │ 标签页:        │  │ 标签页:        │       │
│  │ • 数据库2     │  │   - 表         │  │   - 表结构     │       │
│  │ • ...         │  │   - 视图       │  │   - 数据       │       │
│  └───────────────┘  │   - 函数       │  │   - SQL 编辑器 │       │
│                     │   - 查询       │  │   - AI 对话    │       │
│                     │   - 备份       │  │               │       │
│                     └───────────────┘  └───────────────┘       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**关键组件**:
```vue
<template>
  <div class="main-workspace">
    <!-- 左侧: 数据库列表 -->
    <DatabaseList 
      :databases="currentConnection.databases" 
      @select-db="selectDatabase"
      @refresh="refreshDatabases"
    />
    
    <!-- 中间: 对象导航 (多标签页) -->
    <ObjectNavTabs 
      :current-db="selectedDatabase"
      :tabs="['表', '视图', '函数', '查询', '备份']"
    >
      <template #table>
        <TableList 
          :tables="selectedDatabase.tables" 
          @select-table="openTable"
          @context-menu="showTableMenu"
        />
      </template>
      <template #view>
        <ViewList :views="selectedDatabase.views" />
      </template>
      <template #function>
        <FunctionList :functions="selectedDatabase.functions" />
      </template>
      <template #query>
        <QueryHistory :queries="selectedDatabase.queries" />
      </template>
      <template #backup>
        <BackupPanel :backups="selectedDatabase.backups" />
      </template>
    </ObjectNavTabs>
    
    <!-- 右侧: 详情/工作区 (多标签页) -->
    <DetailPanelTabs :tabs="detailTabs">
      <template #structure>
        <TableStructure :table="selectedTable" />
      </template>
      <template #data>
        <AgGridTable :data="tableData" />
      </template>
      <template #sql>
        <SqlEditor />
      </template>
      <template #ai>
        <AIChatPanel />
      </template>
    </DetailPanelTabs>
  </div>
</template>
```

**状态栏设计**:
```vue
<template>
  <div class="status-bar">
    <!-- 当前连接指示器 -->
    <div class="connection-indicator" @click="showConnectionDropdown">
      <DatabaseIcon />
      <span>{{ currentConnection?.name || '未连接' }}</span>
      <ChevronDownIcon />
    </div>
    
    <!-- 快速切换连接下拉菜单 -->
    <DropdownMenu v-if="showDropdown">
      <DropdownItem 
        v-for="conn in connections" 
        :key="conn.id"
        @click="switchConnection(conn)"
      >
        {{ conn.name }} ({{ conn.type }})
      </DropdownItem>
    </DropdownMenu>
    
    <!-- Home 按钮 -->
    <Button variant="ghost" @click="goHome">
      <HomeIcon />
      <span>首页</span>
    </Button>
    
    <!-- 右侧工具 -->
    <div class="toolbar">
      <Button variant="ghost" @click="toggleAI">
        <SparklesIcon />
        <span>AI 助手</span>
      </Button>
      <Button variant="ghost" @click="openSettings">
        <SettingsIcon />
      </Button>
    </div>
  </div>
</template>

<script setup>
function switchConnection(conn) {
  // 保存当前状态
  connectionStore.saveState();
  
  // 切换连接
  connectionStore.selectConnection(conn);
  
  // 恢复新连接的状态
  connectionStore.restoreState(conn.id);
}

function goHome() {
  // 保存状态
  connectionStore.saveState();
  router.push('/welcome');
}
</script>
```

### 3. 设置页 (SettingsPage.vue)

**功能模块**:
```vue
<template>
  <div class="settings-page">
    <Tabs defaultValue="general">
      <TabsList>
        <TabsTrigger value="general">通用设置</TabsTrigger>
        <TabsTrigger value="editor">编辑器</TabsTrigger>
        <TabsTrigger value="ai">AI 配置</TabsTrigger>
        <TabsTrigger value="shortcuts">快捷键</TabsTrigger>
        <TabsTrigger value="about">关于</TabsTrigger>
      </TabsList>
      
      <TabsContent value="general">
        <GeneralSettings />
      </TabsContent>
      
      <TabsContent value="editor">
        <EditorSettings />
      </TabsContent>
      
      <TabsContent value="ai">
        <AIConfig 
          v-model:api-url="aiApiUrl"
          v-model:api-key="aiApiKey"
        />
      </TabsContent>
    </Tabs>
  </div>
</template>
```

---

## AG Grid 集成方案

### 为什么选择 AG Grid？

| 特性 | AG Grid 优势 | 说明 |
|------|--------------|------|
| **性能** | 虚拟滚动 | 支持百万行数据流畅滚动 |
| **编辑功能** | 单元格编辑 | Excel 级编辑体验，支持验证、下拉框等 |
| **扩展性** | 自定义组件 | 可完全自定义单元格渲染器和编辑器 |
| **功能丰富** | 分组/聚合/透视 | 内置高级数据分析功能 |
| **框架集成** | Vue 原生支持 | 官方提供 Vue 适配器 |
| **许可** | MIT 社区版 | 免费使用基本功能 |

### 基础集成

```vue
<template>
  <ag-grid-vue
    class="ag-theme-alpine"
    :columnDefs="columnDefs"
    :rowData="rowData"
    :defaultColDef="defaultColDef"
    :gridOptions="gridOptions"
    @cellValueChanged="onCellValueChanged"
    @gridReady="onGridReady"
  />
</template>

<script setup>
import { AgGridVue } from "ag-grid-vue3";
import "ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-theme-alpine.css";

const gridOptions = {
  // 启用 Excel 式编辑
  singleClickEdit: true,
  stopEditingWhenCellsLoseFocus: true,
  
  // 性能优化
  rowModelType: 'infinite',
  cacheBlockSize: 100,
  maxBlocksInCache: 10,
  
  // 功能扩展
  enableRangeSelection: true,
  enableFillHandle: true,
  enableCellChangeColour: true,
  
  // 上下文菜单
  enableCharts: true,
  popupParent: document.body,
};

const defaultColDef = {
  editable: true,
  sortable: true,
  filter: true,
  resizable: true,
  flex: 1,
  minWidth: 100,
};

const columnDefs = [
  { 
    headerName: "ID", 
    field: "id", 
    editable: false,
    checkboxSelection: true,
    headerCheckboxSelection: true,
    width: 50
  },
  { 
    headerName: "Name", 
    field: "name",
    // 自定义编辑器
    cellEditor: 'agTextCellEditor',
    cellEditorParams: {
      maxLength: 50
    }
  },
  { 
    headerName: "Type", 
    field: "type",
    // 下拉框编辑器
    cellEditor: 'agSelectCellEditor',
    cellEditorParams: {
      values: ['VARCHAR', 'INT', 'TEXT', 'DATE', 'BOOLEAN']
    }
  },
  { 
    headerName: "Nullable", 
    field: "nullable",
    // 布尔值编辑器
    cellEditor: 'agCheckboxCellEditor',
    cellRenderer: 'agCheckboxCellRenderer'
  },
  { 
    headerName: "Default", 
    field: "defaultValue"
  },
  { 
    headerName: "Actions", 
    cellRenderer: ActionRenderer,
    editable: false,
    width: 100
  }
];

function onCellValueChanged(event) {
  // 同步到后端
  saveCellChange(event.data);
}

function onGridReady(params) {
  gridApi.value = params.api;
  gridColumnApi.value = params.columnApi;
}
</script>

<style>
.ag-theme-alpine {
  height: 100%;
  width: 100%;
  --ag-font-size: 14px;
  --ag-font-family: 'Inter', sans-serif;
}
</style>
```

### 关键优化功能

#### 1. 异步数据加载 (大表分页)

```javascript
const dataSource = {
  type: 'infinite',
  getRows: async (params) => {
    const { startRow, endRow, filterModel, sortModel } = params;
    
    try {
      const response = await fetch(`/api/database/${selectedTable}/data`, {
        method: 'POST',
        body: JSON.stringify({
          start: startRow,
          end: endRow,
          filters: filterModel,
          sorts: sortModel
        })
      });
      
      const { rows, total } = await response.json();
      params.successCallback(rows, total);
    } catch (error) {
      params.failCallback();
    }
  }
};

gridOptions.api.setDatasource(dataSource);
```

#### 2. 实时协作编辑

```javascript
// WebSocket 监听后端变更
const ws = new WebSocket('wss://backend/table-updates');

ws.onmessage = (event) => {
  const update = JSON.parse(event.data);
  
  // 应用增量更新
  gridOptions.api.applyTransactionAsync({
    update: [update]
  });
  
  // 高亮变更的单元格
  const rowNode = gridOptions.api.getRowNode(update.id);
  if (rowNode) {
    rowNode.setDataValue(update.field, update.value);
    highlightCell(rowNode, update.field);
  }
};

function highlightCell(rowNode, field) {
  const eCell = rowNode.getRowElement().querySelector(`[col-id="${field}"]`);
  if (eCell) {
    eCell.classList.add('cell-highlight');
    setTimeout(() => eCell.classList.remove('cell-highlight'), 2000);
  }
}
```

#### 3. 自定义编辑器 (如 JSON 编辑器)

```vue
<script>
const JsonEditor = {
  template: `<div class="json-editor">
    <textarea v-model="value" @change="onChange" class="json-textarea"></textarea>
    <div v-if="error" class="error">{{ error }}</div>
  </div>`,
  data() {
    return { value: null, error: null };
  },
  methods: {
    getValue() {
      try {
        return JSON.parse(this.value);
      } catch (e) {
        this.error = 'Invalid JSON';
        return null;
      }
    },
    onChange() {
      this.error = null;
      this.params.api.stopEditing();
    }
  },
  mounted() {
    this.value = JSON.stringify(this.params.value, null, 2);
  }
};
</script>

<style>
.json-editor {
  position: relative;
}

.json-textarea {
  width: 100%;
  height: 200px;
  font-family: 'Fira Code', monospace;
  font-size: 12px;
}

.error {
  color: red;
  font-size: 11px;
  margin-top: 4px;
}
</style>
```

#### 4. 自定义渲染器 (如进度条、标签)

```vue
<script>
const ProgressRenderer = {
  template: `<div class="progress-cell">
    <div class="progress-bar" :style="{ width: progress + '%' }"></div>
    <span class="progress-text">{{ progress }}%</span>
  </div>`,
  props: ['value'],
  computed: {
    progress() {
      return this.value || 0;
    }
  }
};

const TagRenderer = {
  template: `<div class="tag-container">
    <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
  </div>`,
  props: ['value'],
  computed: {
    tags() {
      return this.value ? this.value.split(',') : [];
    }
  }
};
</script>

<style>
.progress-cell {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
}

.progress-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
  opacity: 0.3;
}

.progress-text {
  position: relative;
  z-index: 1;
  padding-left: 8px;
}

.tag-container {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.tag {
  background: #e0e0e0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}
</style>
```

#### 5. 批量操作

```javascript
// 批量删除
function batchDelete() {
  const selectedRows = gridOptions.api.getSelectedRows();
  if (selectedRows.length === 0) return;
  
  const ids = selectedRows.map(row => row.id);
  
  // 后端批量删除
  fetch('/api/database/batch-delete', {
    method: 'POST',
    body: JSON.stringify({ ids })
  }).then(() => {
    // 前端批量移除
    gridOptions.api.applyTransaction({
      remove: selectedRows
    });
  });
}

// 批量更新
function batchUpdate(field, value) {
  const selectedRows = gridOptions.api.getSelectedRows();
  
  const updates = selectedRows.map(row => ({
    ...row,
    [field]: value
  }));
  
  gridOptions.api.applyTransaction({
    update: updates
  });
  
  // 同步到后端
  saveBatchChanges(updates);
}
```

---

## 状态管理

### Pinia 状态设计

```typescript
// stores/connection.ts
import { defineStore } from 'pinia';

interface Connection {
  id: string;
  name: string;
  type: 'mysql' | 'postgresql' | 'sqlite' | 'mongodb';
  host: string;
  port: number;
  database: string;
  username: string;
}

interface ConnectionState {
  openTabs: Tab[];
  selectedDatabase: string | null;
  selectedTable: string | null;
  filters: Record<string, any>;
  sorts: Record<string, any>;
}

export const useConnectionStore = defineStore('connections', {
  state: () => ({
    currentConnection: null as Connection | null,
    connections: [] as Connection[],
    // 每个连接的状态缓存
    connectionStates: new Map<string, ConnectionState>()
  }),
  
  getters: {
    isConnected: (state) => state.currentConnection !== null,
    currentDatabase: (state) => {
      const connState = state.connectionStates.get(state.currentConnection?.id || '');
      return connState?.selectedDatabase || null;
    }
  },
  
  actions: {
    async loadConnections() {
      const connections = await invoke('get_connections');
      this.connections = connections;
    },
    
    selectConnection(conn: Connection) {
      // 保存当前状态
      this.saveState();
      
      this.currentConnection = conn;
      
      // 恢复状态
      if (this.connectionStates.has(conn.id)) {
        this.restoreState(conn.id);
      } else {
        this.connectionStates.set(conn.id, this.createInitialState());
      }
    },
    
    saveState() {
      if (!this.currentConnection) return;
      
      const state: ConnectionState = {
        openTabs: [...openTabs.value],
        selectedDatabase: selectedDatabase.value,
        selectedTable: selectedTable.value,
        filters: { ...filters.value },
        sorts: { ...sorts.value }
      };
      
      this.connectionStates.set(this.currentConnection.id, state);
    },
    
    restoreState(connId: string) {
      const state = this.connectionStates.get(connId);
      if (state) {
        openTabs.value = state.openTabs;
        selectedDatabase.value = state.selectedDatabase;
        selectedTable.value = state.selectedTable;
        filters.value = state.filters;
        sorts.value = state.sorts;
      }
    },
    
    createInitialState(): ConnectionState {
      return {
        openTabs: [],
        selectedDatabase: null,
        selectedTable: null,
        filters: {},
        sorts: {}
      };
    }
  }
});
```

### 状态持久化策略

```typescript
// plugins/persist.ts
import { Plugin } from 'pinia';

export const persistPlugin: Plugin = ({ store }) => {
  const savedState = localStorage.getItem(`tulipe-${store.$id}`);
  
  if (savedState) {
    store.$patch(JSON.parse(savedState));
  }
  
  store.$subscribe((mutation, state) => {
    localStorage.setItem(`tulipe-${store.$id}`, JSON.stringify(state));
  });
};
```

```typescript
// 监听路由变化
import { watch } from 'vue';
import { useRoute } from 'vue-router';
import { useConnectionStore } from '@/stores/connection';

const route = useRoute();
const connectionStore = useConnectionStore();

watch(() => route.path, (newPath) => {
  if (newPath === '/welcome') {
    // 保存当前状态
    connectionStore.saveState();
  }
});

// 连接变化时自动保存
watch(() => connectionStore.currentConnection, (newConn) => {
  if (newConn) {
    connectionStore.saveState();
  }
});

// 页面刷新前保存状态
window.addEventListener('beforeunload', () => {
  connectionStore.saveState();
});
```

### 标签页状态管理

```typescript
// stores/tabs.ts
export const useTabsStore = defineStore('tabs', {
  state: () => ({
    openTabs: [] as Tab[],
    activeTabId: null as string | null
  }),
  
  actions: {
    openTab(tab: Tab) {
      const existing = this.openTabs.find(t => t.id === tab.id);
      if (existing) {
        this.activeTabId = tab.id;
        return;
      }
      
      this.openTabs.push(tab);
      this.activeTabId = tab.id;
    },
    
    closeTab(tabId: string) {
      const index = this.openTabs.findIndex(t => t.id === tabId);
      if (index === -1) return;
      
      this.openTabs.splice(index, 1);
      
      if (this.activeTabId === tabId) {
        // 激活相邻标签页
        const nextTab = this.openTabs[index] || this.openTabs[index - 1];
        this.activeTabId = nextTab?.id || null;
      }
    },
    
    closeOtherTabs(tabId: string) {
      this.openTabs = this.openTabs.filter(t => t.id === tabId);
      this.activeTabId = tabId;
    },
    
    closeAllTabs() {
      this.openTabs = [];
      this.activeTabId = null;
    }
  }
});
```

---

## 特殊数据库界面

### 图数据库界面

```vue
<template>
  <div class="graph-db-view">
    <!-- 图可视化 -->
    <GraphVisualization 
      :nodes="nodes" 
      :edges="edges"
      :layout="layout"
      @node-click="selectNode"
      @edge-click="selectEdge"
    />
    
    <!-- Cypher 查询编辑器 -->
    <CypherQueryEditor 
      @execute="runCypherQuery"
      @save="saveQuery"
    />
    
    <!-- 图检查器 -->
    <GraphInspector 
      :selectedNode="selectedNode"
      :selectedEdge="selectedEdge"
    />
  </div>
</template>

<script setup>
import { ForceGraph2D } from 'vue-force-graph';

const nodes = ref([]);
const edges = ref([]);
const selectedNode = ref(null);
const selectedEdge = ref(null);

async function runCypherQuery(query) {
  const result = await invoke('execute_cypher', { query });
  nodes.value = result.nodes;
  edges.value = result.edges;
}
</script>

<style>
.graph-db-view {
  display: grid;
  grid-template-rows: 1fr auto 300px;
  height: 100%;
}

.graph-visualization {
  background: #1a1a2e;
}

.cypher-editor {
  border-top: 1px solid #333;
  padding: 16px;
}

.graph-inspector {
  border-top: 1px solid #333;
  overflow-y: auto;
}
</style>
```

### 时序数据库界面

```vue
<template>
  <div class="timeseries-view">
    <!-- 时间范围选择器 -->
    <TimeRangeSelector 
      v-model:start="startTime"
      v-model:end="endTime"
      @change="updateData"
    />
    
    <!-- 时序图表 -->
    <TimeSeriesChart 
      :data="chartData"
      :metrics="selectedMetrics"
      :aggregation="aggregation"
    />
    
    <!-- 异常检测面板 -->
    <AnomalyDetectionPanel 
      :anomalies="anomalies"
      :threshold="threshold"
    />
    
    <!-- 指标选择器 -->
    <MetricSelector 
      v-model:selected="selectedMetrics"
      :available-metrics="metrics"
    />
  </div>
</template>

<script setup>
import { LineChart } from 'vue-echarts';

const startTime = ref(Date.now() - 3600000);
const endTime = ref(Date.now());
const chartData = ref([]);
const selectedMetrics = ref(['cpu', 'memory']);
const anomalies = ref([]);

async function updateData() {
  chartData.value = await invoke('query_timeseries', {
    metrics: selectedMetrics.value,
    start: startTime.value,
    end: endTime.value
  });
  
  anomalies.value = await invoke('detect_anomalies', {
    data: chartData.value,
    threshold: 3
  });
}
</script>

<style>
.timeseries-view {
  display: grid;
  grid-template-rows: auto 1fr auto auto;
  gap: 16px;
  padding: 16px;
}

.time-series-chart {
  min-height: 400px;
}
</style>
```

---

## 性能优化

### 虚拟化技术

1. **AG Grid 虚拟滚动**:
   - 自动处理百万行数据
   - 只渲染可见区域的 DOM

2. **懒加载数据库对象**:
```javascript
// 按需加载表结构
async function loadTableSchema(tableName) {
  if (tableCache.has(tableName)) {
    return tableCache.get(tableName);
  }
  
  const schema = await invoke('get_table_schema', { tableName });
  tableCache.set(tableName, schema);
  return schema;
}
```

### 缓存策略

```typescript
// stores/cache.ts
export const useCacheStore = defineStore('cache', {
  state: () => ({
    tableSchemas: new Map<string, TableSchema>(),
    queryResults: new Map<string, QueryResult>(),
    connectionMetadata: new Map<string, any>()
  }),
  
  actions: {
    setTableSchema(key: string, schema: TableSchema, ttl: number = 300000) {
      this.tableSchemas.set(key, {
        data: schema,
        timestamp: Date.now(),
        ttl
      });
    },
    
    getTableSchema(key: string): TableSchema | null {
      const cached = this.tableSchemas.get(key);
      if (!cached) return null;
      
      if (Date.now() - cached.timestamp > cached.ttl) {
        this.tableSchemas.delete(key);
        return null;
      }
      
      return cached.data;
    }
  }
});
```

### Web Workers

```javascript
// workers/data-processor.js
self.onmessage = function(e) {
  const { data, operation } = e.data;
  
  switch (operation) {
    case 'sort':
      const sorted = data.sort((a, b) => a.value - b.value);
      self.postMessage(sorted);
      break;
    case 'filter':
      const filtered = data.filter(item => item.active);
      self.postMessage(filtered);
      break;
    case 'aggregate':
      const aggregated = data.reduce((acc, item) => {
        acc[item.type] = (acc[item.type] || 0) + item.value;
        return acc;
      }, {});
      self.postMessage(aggregated);
      break;
  }
};

// 主线程使用
const worker = new Worker('/workers/data-processor.js');

worker.onmessage = (e) => {
  const result = e.data;
  gridOptions.api.setRowData(result);
};

worker.postMessage({
  data: largeDataset,
  operation: 'sort'
});
```

### 请求优化

```typescript
// utils/request.ts
const pendingRequests = new Map<string, AbortController>();

export async function fetchWithDedup(
  url: string,
  options: RequestInit = {}
): Promise<any> {
  // 取消相同 URL 的未完成请求
  if (pendingRequests.has(url)) {
    pendingRequests.get(url).abort();
  }
  
  const controller = new AbortController();
  pendingRequests.set(url, controller);
  
  try {
    const response = await fetch(url, {
      ...options,
      signal: controller.signal
    });
    
    return await response.json();
  } finally {
    pendingRequests.delete(url);
  }
}
```

---

## 实施路线图

### 阶段 1: 核心组件开发 (2 周)

**目标**: 搭建基础框架，实现核心 UI 组件

- [ ] 状态栏组件
- [ ] 三栏布局框架
- [ ] AG Grid 基础集成
- [ ] 欢迎页连接管理
- [ ] Pinia 状态管理基础

**交付物**:
- 可运行的基础应用
- 连接管理功能
- 基础表格展示

### 阶段 2: 主界面开发 (3 周)

**目标**: 完善主界面功能，实现多标签页系统

- [ ] 数据库对象导航
- [ ] 多标签页系统
- [ ] 表结构查看器
- [ ] AG Grid 高级功能集成
- [ ] SQL 编辑器集成

**交付物**:
- 完整的数据库浏览功能
- 表格数据编辑
- SQL 查询执行

### 阶段 3: 状态管理 (1 周)

**目标**: 实现状态持久化，优化连接切换体验

- [ ] Pinia 状态持久化
- [ ] 连接状态保存/恢复
- [ ] 标签页状态管理
- [ ] 路由守卫

**交付物**:
- 无缝连接切换
- 状态恢复功能

### 阶段 4: 扩展功能 (2 周)

**目标**: 实现高级功能，提升用户体验

- [ ] AI 集成 (SQL 补全、优化建议)
- [ ] 特殊数据库界面 (图数据库、时序数据库)
- [ ] 设置页面
- [ ] 快捷键系统

**交付物**:
- AI 辅助功能
- 多数据库类型支持
- 完整的设置系统

### 阶段 5: 优化与测试 (2 周)

**目标**: 性能优化，质量保证

- [ ] 性能优化 (虚拟化、缓存、Worker)
- [ ] 压力测试 (大数据集)
- [ ] UI 完善 (动画、过渡)
- [ ] 错误处理
- [ ] 文档编写

**交付物**:
- 高性能应用
- 稳定的质量
- 完整的文档

---

## 总结

Tulipe 前端架构设计遵循以下核心原则:

1. **用户体验优先**: 清晰的三栏布局，流畅的交互，状态持久化
2. **性能优化**: AG Grid 虚拟化，懒加载，缓存策略，Web Workers
3. **可扩展性**: 模块化设计，支持插件化扩展
4. **专业性**: Excel 级表格编辑，AI 辅助，特殊数据库支持

通过 AG Grid 的强大功能和 Vue 3 的现代化技术栈，Tulipe 将提供超越传统数据库工具的用户体验。
