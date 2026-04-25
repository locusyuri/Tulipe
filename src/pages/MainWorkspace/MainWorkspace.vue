<!--
  MainWorkspace.vue - 主工作区入口组件

  功能说明：
  - 作为主工作区页面的根组件，协调 Header、Workspace 和 EditConnectionModal 三个子组件
  - 管理全局状态，如模态框开关、面板宽度、导航标签等
  - 处理拖拽调整面板宽度、点击外部关闭下拉菜单等全局事件

  组件结构：
  - Header: 顶部状态栏（连接信息、切换连接、导航按钮）
  - Workspace: 中部三栏布局工作区（数据库列表、对象导航、数据表格）
  - EditConnectionModal: 编辑连接弹窗（条件渲染，isEditModalOpen 为 true 时显示）
-->
<template>
	<div class="h-full flex flex-col bg-background">
		<!-- Header 组件：顶部状态栏 -->
		<!-- 显示当前连接状态，支持快速切换连接和编辑连接 -->
		<Header
			:is-switch-dropdown-open="isSwitchDropdownOpen"
			@edit-connection="isEditModalOpen = true"
			@toggle-switch-dropdown="isSwitchDropdownOpen = !isSwitchDropdownOpen"
			@switch-connection="switchConnection" />

		<!-- 动态工作区组件：根据数据库类型渲染不同的工作区 -->
		<component
			:is="currentAdapter.getMainPanel()"
			:object-nav-width="objectNavWidth"
			:details-panel-width="detailsPanelWidth"
			:active-nav-tab="activeNavTab"
			:is-more-nav-tabs-open="isMoreNavTabsOpen"
			:all-nav-tabs="allNavTabs"
			:visible-nav-tabs="visibleNavTabs"
			:databases="databases"
			:tables="tables"
			:open-tabs="openTabs"
			:resizing-side="resizingSide"
			@select-nav-tab="selectNavTab"
			@toggle-more-tabs="isMoreNavTabsOpen = !isMoreNavTabsOpen"
			@move-tab="moveTab"
			@start-resizing="startResizing" />

		<!-- EditConnectionModal 组件：编辑连接弹窗 -->
		<!-- 仅当 isEditModalOpen 为 true 时渲染，用于编辑数据库连接信息 -->
		<EditConnectionModal
			v-if="isEditModalOpen"
			@close="isEditModalOpen = false" />
	</div>
</template>

<script setup lang="ts">
/**
 * 导入 Vue 组合式 API 和子组件
 * - ref: 创建响应式引用
 * - computed: 创建计算属性
 * - onMounted/onUnmounted: 生命周期钩子
 */
import { ref, computed, onMounted, onUnmounted } from "vue";
import Header from "./components/Header.vue"; // 顶部状态栏组件
import EditConnectionModal from "./components/EditConnectionModal.vue"; // 编辑连接弹窗组件
import { adapterRegistry } from "./adapters";

// ==================== 模态框状态 ====================
// isEditModalOpen: 控制编辑连接弹窗的显示/隐藏
const isEditModalOpen = ref(false);
// isSwitchDropdownOpen: 控制连接切换下拉菜单的显示/隐藏
const isSwitchDropdownOpen = ref(false);

// ==================== 数据库类型状态 ====================
// currentDbType: 当前连接的数据库类型，默认为 mysql
const currentDbType = ref("mysql");

// ==================== 计算属性 ====================
// currentAdapter: 根据当前数据库类型获取对应的适配器
const currentAdapter = computed(() => {
	return adapterRegistry.getOrDefault(currentDbType.value);
});

// ==================== 面板宽度状态（用于拖拽调整） ====================
// objectNavWidth: 中间对象导航面板的宽度（默认 280px）
const objectNavWidth = ref(280);
// detailsPanelWidth: 右侧详情面板的宽度（默认 300px）
const detailsPanelWidth = ref(300);
// resizingSide: 当前正在拖拽的边栏标识，null 表示未在拖拽
const resizingSide = ref<"left" | "right" | null>(null);

// ==================== 导航标签状态 ====================
// activeNavTab: 当前选中的导航标签
const activeNavTab = ref("Tables");
// isMoreNavTabsOpen: 控制"更多"标签下拉菜单的显示/隐藏
const isMoreNavTabsOpen = ref(false);

/**
 * allNavTabs: 所有可用的导航标签列表
 * 用于分类展示数据库对象：表、视图、函数、查询、备份、触发器、存储过程、事件
 */
const allNavTabs = ref([
	"Tables",
	"Views",
	"Functions",
	"Queries",
	"Backups",
	"Triggers",
	"Procedures",
	"Events",
]);

/**
 * visibleNavTabs: 计算属性，从 allNavTabs 中取前3个标签
 * 原因：界面只显示3个标签，更多标签需要点击"更多"按钮展开
 */
const visibleNavTabs = computed(() => {
	return allNavTabs.value.slice(0, 3);
});

/**
 * selectNavTab: 选择导航标签
 * @param tab - 要选择的标签名称
 * 功能：切换当前活动的导航标签，并关闭"更多"下拉菜单
 */
function selectNavTab(tab: string) {
	activeNavTab.value = tab;
	isMoreNavTabsOpen.value = false;
}

/**
 * moveTab: 移动导航标签位置
 * @param index - 当前标签在数组中的索引
 * @param direction - 移动方向，'up' 上移，'down' 下移
 * 功能：通过上下箭头调整标签在"栏内"区域的显示顺序
 */
function moveTab(index: number, direction: "up" | "down") {
	const tabs = [...allNavTabs.value];
	const newIndex = direction === "up" ? index - 1 : index + 1;
	// 边界检查：不能移到数组范围外
	if (newIndex < 0 || newIndex >= tabs.length) return;

	// 从原位置移除标签，插入到新位置
	const [tab] = tabs.splice(index, 1);
	tabs.splice(newIndex, 0, tab);
	allNavTabs.value = tabs;
}

// ==================== 拖拽调整宽度功能 ====================

/**
 * startResizing: 开始拖拽调整面板宽度
 * @param side - 要调整的边栏：'left' 左侧导航面板，'right' 右侧详情面板
 * 功能：设置拖拽状态，修改鼠标指针样式
 */
function startResizing(side: "left" | "right") {
	resizingSide.value = side;
	document.body.style.cursor = "col-resize"; // 拖拽时显示列调整光标
}

/**
 * stopResizing: 停止拖拽
 * 功能：清除拖拽状态，恢复默认鼠标样式
 */
function stopResizing() {
	resizingSide.value = null;
	document.body.style.cursor = "";
}

/**
 * handleResizing: 处理拖拽过程中的宽度更新
 * @param e - 鼠标事件对象
 * 功能：根据鼠标位置计算并更新对应面板的宽度
 * 限制：左侧面板宽度范围 150-400px，右侧面板宽度范围 150-600px
 */
function handleResizing(e: MouseEvent) {
	if (!resizingSide.value) return;

	if (resizingSide.value === "left") {
		// 左侧面板宽度 = 鼠标X坐标 - 左侧数据库图标栏宽度(72px)
		const newWidth = e.clientX - 72;
		if (newWidth > 150 && newWidth < 400) {
			objectNavWidth.value = newWidth;
		}
	} else if (resizingSide.value === "right") {
		// 右侧面板宽度 = 窗口宽度 - 鼠标X坐标
		const newWidth = window.innerWidth - e.clientX;
		if (newWidth > 150 && newWidth < 600) {
			detailsPanelWidth.value = newWidth;
		}
	}
}

// ==================== 数据列表（模拟数据） ====================

/**
 * databases: 数据库列表
 * 显示当前连接下的所有数据库
 */
const databases = ref([
	"information_schema",
	"mysql",
	"performance_schema",
	"sys",
	"tulipe_dev",
	"users_db",
	"order_system",
]);

/**
 * tables: 数据表列表
 * 显示当前选中的数据库中的所有表
 */
const tables = ref([
	"users",
	"roles",
	"permissions",
	"user_roles",
	"profiles",
	"login_history",
	"audit_logs",
	"settings",
]);

/**
 * openTabs: 当前打开的标签页列表
 * 用于显示已打开的表或查询
 */
const openTabs = ref([
	{ id: "1", name: "users", active: true },
	{ id: "2", name: "SQL Query 1", active: false },
	{ id: "3", name: "roles", active: false },
]);

/**
 * switchConnection: 切换数据库连接
 * @param conn - 要切换到的连接对象
 * 功能：输出切换日志，关闭连接切换下拉菜单，更新数据库类型
 */
function switchConnection(conn: any) {
	console.log("Switching to", conn.name);
	// 更新当前数据库类型，默认为 mysql
	currentDbType.value = conn.dbType || "mysql";
	isSwitchDropdownOpen.value = false;
}

// ==================== 点击外部关闭下拉菜单 ====================

/**
 * handleClickOutside: 处理点击空白区域关闭下拉菜单
 * @param e - 鼠标点击事件
 * 功能：点击非按钮区域时，自动关闭所有下拉菜单
 */
function handleClickOutside(e: MouseEvent) {
	const target = e.target as HTMLElement;
	// 如果点击的是下拉菜单内的按钮，不处理（由按钮自身处理）
	if (target.closest(".relative > button")) return;

	// 关闭连接切换下拉菜单和导航标签更多下拉菜单
	isSwitchDropdownOpen.value = false;
	isMoreNavTabsOpen.value = false;
}

// ==================== 生命周期钩子 ====================

/**
 * onMounted: 组件挂载后
 * 功能：注册全局事件监听器
 * - click: 用于点击外部关闭下拉菜单
 * - mousemove: 用于拖拽调整面板宽度
 * - mouseup: 用于停止拖拽
 */
onMounted(() => {
	document.addEventListener("click", handleClickOutside);
	window.addEventListener("mousemove", handleResizing);
	window.addEventListener("mouseup", stopResizing);
});

/**
 * onUnmounted: 组件卸载前
 * 功能：移除所有注册的事件监听器，防止内存泄漏
 */
onUnmounted(() => {
	document.removeEventListener("click", handleClickOutside);
	window.removeEventListener("mousemove", handleResizing);
	window.removeEventListener("mouseup", stopResizing);
});
</script>
