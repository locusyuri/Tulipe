<template>
	<!-- 主内容区：左侧分类树 + 右侧连接列表 -->
	<div class="flex-1 flex overflow-hidden">
		<!-- 左侧文件夹与分类导航 -->
		<aside class="w-56 border-r border-border flex flex-col bg-card/30">
			<div class="p-3 border-b border-border flex items-center justify-between">
				<h3
					class="text-xs font-bold text-muted-foreground uppercase tracking-wider">
					连接分类
				</h3>
				<button
					@click="emit('create-folder')"
					class="p-1 hover:bg-muted rounded text-muted-foreground hover:text-foreground transition-colors"
					title="新建文件夹">
					<svg
						class="w-4 h-4"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2">
						<path
							d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
						<line x1="12" y1="11" x2="12" y2="17"></line>
						<line x1="9" y1="14" x2="15" y2="14"></line>
					</svg>
				</button>
			</div>
			<div class="flex-1 overflow-y-auto p-2 space-y-0.5">
				<button
					@click="emit('select-folder', null)"
					class="w-full flex items-center gap-2 px-3 py-2 text-sm rounded-lg transition-colors"
					:class="
						selectedFolderId === null
							? 'bg-primary/10 text-primary font-medium'
							: 'text-muted-foreground hover:bg-muted hover:text-foreground'
					">
					<svg
						class="w-4 h-4"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2">
						<ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
						<path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
						<path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
					</svg>
					全部连接
					<span class="ml-auto text-xs bg-muted px-1.5 py-0.5 rounded">
						{{ allConnectionsCount }}
					</span>
				</button>

				<div v-for="folder in folders" :key="folder.id" class="group/folder">
					<button
						@click="emit('select-folder', folder.id)"
						@contextmenu.prevent="
							emit('show-folder-context-menu', $event, folder)
						"
						class="w-full flex items-center gap-2 px-3 py-2 text-sm rounded-lg transition-colors"
						:class="
							selectedFolderId === folder.id
								? 'bg-primary/10 text-primary font-medium'
								: 'text-muted-foreground hover:bg-muted hover:text-foreground'
						">
						<svg
							class="w-4 h-4"
							viewBox="0 0 24 24"
							fill="none"
							stroke="currentColor"
							stroke-width="2">
							<path
								d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
						</svg>
						<span class="truncate">{{ folder.name }}</span>
						<span class="ml-auto text-xs bg-muted px-1.5 py-0.5 rounded">
							{{ getFolderConnectionCount(folder.id) }}
						</span>
					</button>
				</div>

				<div
					v-if="folders.length === 0"
					class="py-6 text-center text-xs text-muted-foreground">
					<p>暂无分类文件夹</p>
					<p class="mt-1">点击上方按钮创建</p>
				</div>
			</div>
		</aside>

		<!-- 右侧连接列表：搜索 + 卡片网格 + 空状态 -->
		<section class="flex-1 flex flex-col overflow-hidden">
			<div class="p-3 border-b border-border flex items-center gap-3">
				<div class="relative flex-1 max-w-sm">
					<input
						:value="searchQuery"
						@input="onSearchInput"
						type="text"
						placeholder="搜索连接..."
						class="w-full pl-8 pr-3 py-1.5 text-sm bg-muted rounded-lg border-none focus:ring-1 focus:ring-primary outline-none" />
					<svg
						class="w-3.5 h-3.5 absolute left-2.5 top-1/2 -translate-y-1/2 text-muted-foreground"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2">
						<circle cx="11" cy="11" r="8"></circle>
						<line x1="21" y1="21" x2="16.65" y2="16.65"></line>
					</svg>
				</div>
				<div class="text-xs text-muted-foreground">
					{{ filteredConnections.length }} 个连接
				</div>
			</div>

			<div class="flex-1 overflow-y-auto p-4">
				<div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
					<div
						v-for="conn in filteredConnections"
						:key="conn.id"
						@click="emit('connect', conn)"
						@contextmenu.prevent="
							emit('show-connection-context-menu', $event, conn)
						"
						class="group p-4 bg-card border border-border rounded-xl hover:border-primary/40 hover:shadow-md cursor-pointer transition-all duration-200 flex flex-col">
						<div class="flex items-center gap-3 mb-3">
							<div
								class="w-10 h-10 rounded-lg flex items-center justify-center text-lg font-bold transition-colors"
								:class="
									dbTypeColors[conn.type] || 'bg-muted text-muted-foreground'
								">
								{{
									dbTypeIcons[conn.type] || conn.type.charAt(0).toUpperCase()
								}}
							</div>
							<div class="flex-1 min-w-0">
								<div class="font-semibold truncate">{{ conn.name }}</div>
								<div class="text-xs text-muted-foreground truncate">
									{{ conn.host }}:{{ conn.port }}
								</div>
							</div>
						</div>

						<div class="flex items-center gap-2 text-xs mb-3">
							<span
								class="px-1.5 py-0.5 rounded text-[10px] uppercase font-bold"
								:class="
									dbTypeBadgeColors[conn.type] ||
									'bg-muted text-muted-foreground'
								">
								{{ conn.type }}
							</span>
							<span v-if="conn.database" class="text-muted-foreground truncate">
								{{ conn.database }}
							</span>
						</div>

						<div class="flex items-center justify-between mt-auto">
							<div class="flex items-center gap-1.5">
								<div
									class="w-1.5 h-1.5 rounded-full"
									:class="
										conn.active ? 'bg-green-500' : 'bg-muted-foreground/30'
									"></div>
								<span class="text-xs text-muted-foreground">
									{{ conn.active ? "已连接" : "未连接" }}
								</span>
							</div>
							<div
								class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
								<button
									@click.stop="emit('edit-connection', conn)"
									class="p-1 hover:bg-muted rounded text-muted-foreground hover:text-foreground transition-colors"
									title="编辑">
									<svg
										class="w-3.5 h-3.5"
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2">
										<path
											d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
										<path
											d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
									</svg>
								</button>
								<button
									@click.stop="emit('delete-connection', conn)"
									class="p-1 hover:bg-destructive/10 rounded text-muted-foreground hover:text-destructive transition-colors"
									title="删除">
									<svg
										class="w-3.5 h-3.5"
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2">
										<polyline points="3 6 5 6 21 6"></polyline>
										<path
											d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
									</svg>
								</button>
							</div>
						</div>
					</div>
				</div>

				<div
					v-if="filteredConnections.length === 0"
					class="h-full flex flex-col items-center justify-center text-muted-foreground space-y-3">
					<svg
						class="w-16 h-16 opacity-20"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="1">
						<ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
						<path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
						<path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
					</svg>
					<p class="text-sm">没有找到相关连接</p>
					<button
						@click="emit('create-new-connection')"
						class="px-4 py-2 bg-primary text-primary-foreground rounded-lg font-medium shadow-sm hover:opacity-90 transition-opacity text-sm">
						创建新连接
					</button>
				</div>
			</div>
		</section>
	</div>

	<!-- 文件夹右键菜单 -->
	<div
		v-if="folderContextMenu.visible"
		class="fixed bg-card border border-border rounded-lg shadow-xl py-1 z-50 text-sm min-w-[160px]"
		:style="{
			left: folderContextMenu.x + 'px',
			top: folderContextMenu.y + 'px',
		}">
		<button
			@click="emit('rename-folder', folderContextMenu.folder)"
			class="w-full px-3 py-1.5 text-left hover:bg-accent transition-colors flex items-center gap-2">
			<svg
				class="w-3.5 h-3.5"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2">
				<path
					d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
				<path
					d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
			</svg>
			重命名
		</button>
		<button
			@click="emit('delete-folder', folderContextMenu.folder)"
			class="w-full px-3 py-1.5 text-left hover:bg-destructive/10 text-destructive transition-colors flex items-center gap-2">
			<svg
				class="w-3.5 h-3.5"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2">
				<polyline points="3 6 5 6 21 6"></polyline>
				<path
					d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
			</svg>
			删除文件夹
		</button>
	</div>

	<!-- 连接右键菜单 -->
	<div
		v-if="connectionContextMenu.visible"
		class="fixed bg-card border border-border rounded-lg shadow-xl py-1 z-50 text-sm min-w-[180px]"
		:style="{
			left: connectionContextMenu.x + 'px',
			top: connectionContextMenu.y + 'px',
		}">
		<button
			@click="emit('connect', connectionContextMenu.conn)"
			class="w-full px-3 py-1.5 text-left hover:bg-accent transition-colors flex items-center gap-2">
			<svg
				class="w-3.5 h-3.5"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2">
				<path d="M5 12h14M12 5l7 7-7 7"></path>
			</svg>
			连接
		</button>
		<button
			@click="emit('edit-connection', connectionContextMenu.conn)"
			class="w-full px-3 py-1.5 text-left hover:bg-accent transition-colors flex items-center gap-2">
			<svg
				class="w-3.5 h-3.5"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2">
				<path
					d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
				<path
					d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
			</svg>
			编辑
		</button>
		<button
			v-if="connectionContextMenu.conn?.active"
			@click="emit('disconnect-connection', connectionContextMenu.conn)"
			class="w-full px-3 py-1.5 text-left hover:bg-accent transition-colors flex items-center gap-2">
			<svg
				class="w-3.5 h-3.5"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2">
				<path d="M18 6L6 18M6 6l12 12"></path>
			</svg>
			断开连接
		</button>
		<div class="border-t border-border my-1"></div>
		<div class="px-3 py-1 text-xs text-muted-foreground">移动到文件夹</div>
		<button
			@click="emit('move-to-folder', connectionContextMenu.conn, null)"
			class="w-full px-3 py-1.5 text-left hover:bg-accent transition-colors flex items-center gap-2">
			<svg
				class="w-3.5 h-3.5"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2">
				<ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
				<path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
				<path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
			</svg>
			全部连接（无分类）
		</button>
		<button
			v-for="folder in folders"
			:key="folder.id"
			@click="emit('move-to-folder', connectionContextMenu.conn, folder.id)"
			class="w-full px-3 py-1.5 text-left hover:bg-accent transition-colors flex items-center gap-2">
			<svg
				class="w-3.5 h-3.5"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2">
				<path
					d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
			</svg>
			{{ folder.name }}
		</button>
		<div class="border-t border-border my-1"></div>
		<button
			@click="emit('delete-connection', connectionContextMenu.conn)"
			class="w-full px-3 py-1.5 text-left hover:bg-destructive/10 text-destructive transition-colors flex items-center gap-2">
			<svg
				class="w-3.5 h-3.5"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2">
				<polyline points="3 6 5 6 21 6"></polyline>
				<path
					d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
			</svg>
			删除连接
		</button>
	</div>

	<!-- 新建文件夹弹窗 -->
	<div
		v-if="newFolderDialog.visible"
		class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
		<div
			class="bg-card border border-border rounded-xl shadow-2xl p-6 w-80 space-y-4">
			<h3 class="text-lg font-bold">新建分类文件夹</h3>
			<input
				:value="newFolderDialog.name"
				@input="onNewFolderNameInput"
				type="text"
				placeholder="文件夹名称"
				class="w-full px-3 py-2 bg-muted rounded-lg border-none text-sm outline-none focus:ring-1 focus:ring-primary"
				@keyup.enter="emit('confirm-create-folder')" />
			<div class="flex items-center justify-end gap-2">
				<button
					@click="emit('close-new-folder-dialog')"
					class="px-3 py-1.5 bg-muted rounded-lg text-sm font-medium hover:bg-accent transition-colors">
					取消
				</button>
				<button
					@click="emit('confirm-create-folder')"
					class="px-3 py-1.5 bg-primary text-primary-foreground rounded-lg text-sm font-medium shadow-sm hover:opacity-90 transition-opacity">
					创建
				</button>
			</div>
		</div>
	</div>

	<!-- 重命名文件夹弹窗 -->
	<div
		v-if="renameFolderDialog.visible"
		class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
		<div
			class="bg-card border border-border rounded-xl shadow-2xl p-6 w-80 space-y-4">
			<h3 class="text-lg font-bold">重命名文件夹</h3>
			<input
				:value="renameFolderDialog.name"
				@input="onRenameFolderNameInput"
				type="text"
				placeholder="文件夹名称"
				class="w-full px-3 py-2 bg-muted rounded-lg border-none text-sm outline-none focus:ring-1 focus:ring-primary"
				@keyup.enter="emit('confirm-rename-folder')" />
			<div class="flex items-center justify-end gap-2">
				<button
					@click="emit('close-rename-folder-dialog')"
					class="px-3 py-1.5 bg-muted rounded-lg text-sm font-medium hover:bg-accent transition-colors">
					取消
				</button>
				<button
					@click="emit('confirm-rename-folder')"
					class="px-3 py-1.5 bg-primary text-primary-foreground rounded-lg text-sm font-medium shadow-sm hover:opacity-90 transition-opacity">
					确认
				</button>
			</div>
		</div>
	</div>

	<!-- 接口调用结果提示 -->
	<Transition name="fade">
		<div
			v-if="testApiResponse"
			class="fixed bottom-12 right-12 z-50 flex items-center gap-3 px-6 py-4 rounded-2xl shadow-2xl border backdrop-blur-md transition-all duration-300"
			:class="
				testApiResponse.includes('失败')
					? 'bg-destructive/15 border-destructive/20 text-destructive'
					: 'bg-primary/10 border-primary/20 text-primary'
			">
			<div class="flex-shrink-0">
				<svg
					v-if="testApiResponse.includes('失败')"
					class="w-5 h-5"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2">
					<circle cx="12" cy="12" r="10"></circle>
					<line x1="12" y1="8" x2="12" y2="12"></line>
					<line x1="12" y1="16" x2="12.01" y2="16"></line>
				</svg>
				<svg
					v-else
					class="w-5 h-5"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2">
					<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
					<polyline points="22 4 12 14.01 9 11.01"></polyline>
				</svg>
			</div>
			<div class="font-medium text-sm">{{ testApiResponse }}</div>
			<button
				@click="emit('clear-test-api-response')"
				class="ml-2 p-1 hover:bg-black/5 rounded-full transition-colors">
				<svg
					class="w-3.5 h-3.5 opacity-60"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2">
					<line x1="18" y1="6" x2="6" y2="18"></line>
					<line x1="6" y1="6" x2="18" y2="18"></line>
				</svg>
			</button>
		</div>
	</Transition>
</template>

<script setup lang="ts">
import { type PropType } from "vue";

// 连接数据结构
type Connection = {
	id: string;
	name: string;
	type: string;
	host: string;
	port: number;
	database?: string;
	folderId: string | null;
	active: boolean;
};

// 文件夹结构
type Folder = {
	id: string;
	name: string;
};

// 文件夹右键菜单状态结构
type FolderContextMenu = {
	visible: boolean;
	x: number;
	y: number;
	folder: Folder | null;
};

// 连接右键菜单状态结构
type ConnectionContextMenu = {
	visible: boolean;
	x: number;
	y: number;
	conn: Connection | null;
};

// 新建文件夹弹窗状态结构
type NewFolderDialog = {
	visible: boolean;
	name: string;
};

// 重命名文件夹弹窗状态结构
type RenameFolderDialog = {
	visible: boolean;
	name: string;
	folderId: string;
};

// 内容区对外事件：状态同步、菜单动作、弹窗动作
const emit = defineEmits<{
	(e: "update:searchQuery", value: string): void;
	(e: "update:new-folder-name", value: string): void;
	(e: "update:rename-folder-name", value: string): void;
	(e: "select-folder", folderId: string | null): void;
	(e: "show-folder-context-menu", event: MouseEvent, folder: Folder): void;
	(
		e: "show-connection-context-menu",
		event: MouseEvent,
		conn: Connection,
	): void;
	(e: "connect", conn: Connection | null): void;
	(e: "disconnect-connection", conn: Connection | null): void;
	(e: "edit-connection", conn: Connection | null): void;
	(e: "delete-connection", conn: Connection | null): void;
	(e: "create-new-connection"): void;
	(e: "create-folder"): void;
	(e: "confirm-create-folder"): void;
	(e: "close-new-folder-dialog"): void;
	(e: "rename-folder", folder: Folder | null): void;
	(e: "confirm-rename-folder"): void;
	(e: "close-rename-folder-dialog"): void;
	(e: "delete-folder", folder: Folder | null): void;
	(e: "move-to-folder", conn: Connection | null, folderId: string | null): void;
	(e: "clear-test-api-response"): void;
}>();

// 内容区渲染所需状态
const props = defineProps({
	searchQuery: {
		type: String,
		required: true,
	},
	filteredConnections: {
		type: Array as PropType<Connection[]>,
		required: true,
	},
	connections: {
		type: Array as PropType<Connection[]>,
		required: true,
	},
	folders: {
		type: Array as PropType<Folder[]>,
		required: true,
	},
	allConnectionsCount: {
		type: Number,
		required: true,
	},
	selectedFolderId: {
		type: [String, null] as PropType<string | null>,
		required: true,
	},
	folderContextMenu: {
		type: Object as PropType<FolderContextMenu>,
		required: true,
	},
	connectionContextMenu: {
		type: Object as PropType<ConnectionContextMenu>,
		required: true,
	},
	newFolderDialog: {
		type: Object as PropType<NewFolderDialog>,
		required: true,
	},
	renameFolderDialog: {
		type: Object as PropType<RenameFolderDialog>,
		required: true,
	},
	testApiResponse: {
		type: [String, null] as PropType<string | null>,
		required: true,
	},
	dbTypeIcons: {
		type: Object as PropType<Record<string, string>>,
		required: true,
	},
	dbTypeColors: {
		type: Object as PropType<Record<string, string>>,
		required: true,
	},
	dbTypeBadgeColors: {
		type: Object as PropType<Record<string, string>>,
		required: true,
	},
});

// 搜索框输入同步到父组件
function onSearchInput(event: Event) {
	emit("update:searchQuery", (event.target as HTMLInputElement).value);
}

// 新建文件夹输入框同步到父组件
function onNewFolderNameInput(event: Event) {
	const target = event.target as HTMLInputElement;
	if (target) {
		emit("update:new-folder-name", target.value);
	}
}

// 重命名输入框同步到父组件
function onRenameFolderNameInput(event: Event) {
	const target = event.target as HTMLInputElement;
	if (target) {
		emit("update:rename-folder-name", target.value);
	}
}

// 计算指定文件夹下的连接数量
function getFolderConnectionCount(folderId: string): number {
	return props.connections.filter(
		(connection) => connection.folderId === folderId,
	).length;
}
</script>
