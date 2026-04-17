<template>
	<div class="h-full flex flex-col bg-background overflow-hidden">
		<!-- 欢迎页头部：工具栏 + 活跃连接卡片 -->
		<Header
			:is-loading-test-api="isLoadingTestApi"
			:active-connections="activeConnections"
			:db-type-icons="dbTypeIcons"
			:db-type-colors="dbTypeColors"
			:db-type-badge-colors="dbTypeBadgeColors"
			@back="router.push('/')"
			@create-new-connection="createNewConnection"
			@connect="connect"
			@disconnect="disconnectConnection"
			@settings="router.push('/settings')" />

		<!-- 欢迎页内容：分类列表、连接卡片、上下文菜单与弹窗 -->
		<Content
			:search-query="searchQuery"
			:filtered-connections="filteredConnections"
			:connections="connections"
			:folders="folders"
			:selected-folder-id="selectedFolderId"
			:all-connections-count="allConnectionsCount"
			:folder-context-menu="folderContextMenu"
			:connection-context-menu="connectionContextMenu"
			:new-folder-dialog="newFolderDialog"
			:rename-folder-dialog="renameFolderDialog"
			:test-api-response="testApiResponse"
			:db-type-icons="dbTypeIcons"
			:db-type-colors="dbTypeColors"
			:db-type-badge-colors="dbTypeBadgeColors"
			@update:searchQuery="searchQuery = $event"
			@update:new-folder-name="newFolderDialog.name = $event"
			@update:rename-folder-name="renameFolderDialog.name = $event"
			@select-folder="selectedFolderId = $event"
			@show-folder-context-menu="showFolderContextMenu"
			@show-connection-context-menu="showConnectionContextMenu"
			@connect="connect"
			@disconnect-connection="disconnectConnection"
			@edit-connection="editConnection"
			@delete-connection="deleteConnection"
			@create-new-connection="createNewConnection"
			@create-folder="createFolder"
			@confirm-create-folder="confirmCreateFolder"
			@close-new-folder-dialog="newFolderDialog.visible = false"
			@rename-folder="renameFolder"
			@confirm-rename-folder="confirmRenameFolder"
			@close-rename-folder-dialog="renameFolderDialog.visible = false"
			@delete-folder="deleteFolder"
			@move-to-folder="moveToFolder"
			@clear-test-api-response="testApiResponse = null" />
	</div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, nextTick } from "vue";
import { useRouter } from "vue-router";
import { invoke } from "@tauri-apps/api/core";

import Header from "./components/Header.vue";
import Content from "./components/Content.vue";

// 路由实例，用于页面跳转
const router = useRouter();
// 搜索关键字
const searchQuery = ref("");
// 当前选中的文件夹（null 表示全部连接）
const selectedFolderId = ref<string | null>(null);
// 右下角提示消息
const testApiResponse = ref<string | null>(null);
// 后端连接按钮加载态
const isLoadingTestApi = ref(false);

interface Connection {
	id: string;
	name: string;
	type: string;
	host: string;
	port: number;
	database?: string;
	folderId: string | null;
	active: boolean;
}

interface Folder {
	id: string;
	name: string;
}

// 连接数据（当前为本地模拟数据）
const connections = ref<Connection[]>([
	{
		id: "1",
		name: "Production MySQL",
		type: "mysql",
		host: "192.168.1.10",
		port: 3306,
		database: "tulipe_dev",
		folderId: "f1",
		active: true,
	},
	{
		id: "2",
		name: "Local PostgreSQL",
		type: "postgresql",
		host: "localhost",
		port: 5432,
		database: "myapp",
		folderId: "f2",
		active: true,
	},
	{
		id: "3",
		name: "Redis Cache",
		type: "redis",
		host: "localhost",
		port: 6379,
		folderId: null,
		active: false,
	},
	{
		id: "4",
		name: "Internal SQLite",
		type: "sqlite",
		host: "app.db",
		port: 0,
		folderId: null,
		active: false,
	},
	{
		id: "5",
		name: "Staging MySQL",
		type: "mysql",
		host: "staging.internal",
		port: 3306,
		database: "staging_db",
		folderId: "f1",
		active: false,
	},
	{
		id: "6",
		name: "Analytics PG",
		type: "postgresql",
		host: "analytics.db.internal",
		port: 5432,
		database: "analytics",
		folderId: "f2",
		active: false,
	},
]);

// 文件夹数据（当前为本地模拟数据）
const folders = ref<Folder[]>([
	{ id: "f1", name: "生产环境" },
	{ id: "f2", name: "开发环境" },
]);

const dbTypeIcons: Record<string, string> = {
	mysql: "M",
	postgresql: "P",
	redis: "R",
	sqlite: "S",
};

const dbTypeColors: Record<string, string> = {
	mysql: "bg-blue-500/10 text-blue-600",
	postgresql: "bg-indigo-500/10 text-indigo-600",
	redis: "bg-red-500/10 text-red-600",
	sqlite: "bg-emerald-500/10 text-emerald-600",
};

const dbTypeBadgeColors: Record<string, string> = {
	mysql: "bg-blue-500/10 text-blue-600",
	postgresql: "bg-indigo-500/10 text-indigo-600",
	redis: "bg-red-500/10 text-red-600",
	sqlite: "bg-emerald-500/10 text-emerald-600",
};

// 顶部已连接卡片的数据来源
const activeConnections = computed(() =>
	connections.value.filter((c) => c.active),
);
// 连接总数（用于左侧“全部连接”计数）
const allConnectionsCount = computed(() => connections.value.length);

// 根据分类和关键字过滤后的连接列表
const filteredConnections = computed(() => {
	let result = connections.value;
	if (selectedFolderId.value !== null) {
		result = result.filter((c) => c.folderId === selectedFolderId.value);
	}
	if (searchQuery.value) {
		const q = searchQuery.value.toLowerCase();
		result = result.filter(
			(c) =>
				c.name.toLowerCase().includes(q) ||
				c.type.toLowerCase().includes(q) ||
				c.host.toLowerCase().includes(q) ||
				(c.database && c.database.toLowerCase().includes(q)),
		);
	}
	return result;
});

// 新建连接：启动后端并调用测试接口，成功/失败都反馈到提示消息
async function createNewConnection() {
	isLoadingTestApi.value = true;
	testApiResponse.value = null;
	try {
		await invoke("start_backend");
		const response = await invoke<string>("call_test_api");
		const data = JSON.parse(response);
		testApiResponse.value = data.message || response;
	} catch (error) {
		console.error("Failed to connect to backend:", error);
		testApiResponse.value = `连接失败: ${error}`;
	} finally {
		isLoadingTestApi.value = false;
		setTimeout(() => {
			testApiResponse.value = null;
		}, 3000);
	}
}

// 打开连接并进入主工作区
function connect(conn: Connection | null) {
	if (!conn) return;
	console.log("Connecting to", conn.name);
	router.push("/main");
}

// 编辑连接（当前仅保留占位逻辑）
function editConnection(conn: Connection | null) {
	if (!conn) return;
	console.log("Editing", conn.name);
}

// 删除连接并关闭菜单
function deleteConnection(conn: Connection | null) {
	if (!conn) return;
	connections.value = connections.value.filter((c) => c.id !== conn.id);
	hideAllMenus();
}

// 断开连接并关闭菜单
function disconnectConnection(conn: Connection | null) {
	if (!conn) return;
	const idx = connections.value.findIndex((c) => c.id === conn.id);
	if (idx !== -1) {
		connections.value[idx] = { ...connections.value[idx], active: false };
	}
	hideAllMenus();
}

// 右键菜单状态：文件夹菜单
const folderContextMenu = reactive({
	visible: false,
	x: 0,
	y: 0,
	folder: null as Folder | null,
});
// 右键菜单状态：连接菜单
const connectionContextMenu = reactive({
	visible: false,
	x: 0,
	y: 0,
	conn: null as Connection | null,
});
// 新建文件夹弹窗状态
const newFolderDialog = reactive({ visible: false, name: "" });
// 重命名文件夹弹窗状态
const renameFolderDialog = reactive({ visible: false, name: "", folderId: "" });

// 打开文件夹右键菜单，并先关闭其他菜单
function showFolderContextMenu(e: MouseEvent, folder: Folder) {
	hideAllMenus();
	folderContextMenu.visible = true;
	folderContextMenu.x = e.clientX;
	folderContextMenu.y = e.clientY;
	folderContextMenu.folder = folder;
}

// 打开连接右键菜单，并先关闭其他菜单
function showConnectionContextMenu(e: MouseEvent, conn: Connection) {
	hideAllMenus();
	connectionContextMenu.visible = true;
	connectionContextMenu.x = e.clientX;
	connectionContextMenu.y = e.clientY;
	connectionContextMenu.conn = conn;
}

// 统一关闭所有右键菜单
function hideAllMenus() {
	folderContextMenu.visible = false;
	connectionContextMenu.visible = false;
}

// 打开“新建文件夹”弹窗并重置输入
function createFolder() {
	newFolderDialog.visible = true;
	newFolderDialog.name = "";
}

// 确认创建文件夹
function confirmCreateFolder() {
	if (!newFolderDialog.name.trim()) return;
	const id = "f" + Date.now();
	folders.value.push({ id, name: newFolderDialog.name.trim() });
	newFolderDialog.visible = false;
}

// 打开“重命名文件夹”弹窗
function renameFolder(folder: Folder | null) {
	if (!folder) return;
	renameFolderDialog.visible = true;
	renameFolderDialog.name = folder.name;
	renameFolderDialog.folderId = folder.id;
	hideAllMenus();
}

// 确认重命名文件夹
function confirmRenameFolder() {
	if (!renameFolderDialog.name.trim()) return;
	const idx = folders.value.findIndex(
		(f) => f.id === renameFolderDialog.folderId,
	);
	if (idx !== -1) {
		folders.value[idx] = {
			...folders.value[idx],
			name: renameFolderDialog.name.trim(),
		};
	}
	renameFolderDialog.visible = false;
}

// 删除文件夹，并把其下连接移动到“未分类”
function deleteFolder(folder: Folder | null) {
	if (!folder) return;
	connections.value.forEach((c) => {
		if (c.folderId === folder.id) {
			c.folderId = null;
		}
	});
	folders.value = folders.value.filter((f) => f.id !== folder.id);
	if (selectedFolderId.value === folder.id) {
		selectedFolderId.value = null;
	}
	hideAllMenus();
}

// 将连接移动到指定文件夹（或未分类）
function moveToFolder(conn: Connection | null, folderId: string | null) {
	if (!conn) return;
	const idx = connections.value.findIndex((c) => c.id === conn.id);
	if (idx !== -1) {
		connections.value[idx] = { ...connections.value[idx], folderId };
	}
	hideAllMenus();
}

// 点击页面空白区域时关闭右键菜单
document.addEventListener("click", () => {
	hideAllMenus();
});
</script>
