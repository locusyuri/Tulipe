<!--
  Header.vue - 顶部状态栏组件

  功能说明：
  - 显示当前数据库连接状态（名称、地址、连接状态指示灯）
  - 提供快速切换连接的入口
  - 提供编辑连接、系统设置等操作入口
  - 提供 AI 助手入口

  布局结构：
  - 左侧：返回欢迎页按钮
  - 中间：居中的连接状态卡片（核心区域）
  - 右侧：AI 助手按钮、设置按钮
-->
<template>
	<!--
    顶部状态栏容器
    - 固定高度 40px
    - 底部边框分隔
    - 半透明背景
    - 相对定位，z-index 确保在最上层
  -->
	<header
		class="h-10 border-b border-border bg-card/50 flex items-center justify-between px-4 text-xs font-medium relative z-40">

		<!-- 左侧区域：返回按钮 -->
		<div class="flex items-center gap-4">
			<button
				@click="router.push('/')"
				class="p-1 hover:bg-muted rounded text-muted-foreground hover:text-foreground transition-colors"
				title="返回欢迎页">
				<!-- 房屋图标 SVG -->
				<svg
					class="w-4 h-4"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2">
					<!-- 房屋屋顶和墙体 -->
					<path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
					<!-- 门 -->
					<polyline points="9 22 9 12 15 12 15 22"></polyline>
				</svg>
			</button>
		</div>

		<!-- 中间区域：连接状态卡片（居中定位） -->
		<!-- 圆角药丸形状，带阴影的浮动效果 -->
		<div
			class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 flex items-center gap-3 px-4 py-1 bg-muted/50 rounded-full border border-border shadow-sm">

			<!-- 连接信息区域 -->
			<div class="flex items-center gap-2 pr-3 border-r border-border/50">
				<!-- 连接状态指示灯：绿色闪烁动画表示已连接 -->
				<div
					class="w-2 h-2 rounded-full bg-green-500 animate-pulse shadow-[0_0_8px_rgba(34,197,94,0.6)]"></div>
				<!-- 连接名称和地址 -->
				<div class="flex flex-col leading-tight">
					<span class="text-foreground font-bold tracking-tight">
						Production MySQL
					</span>
					<span class="text-[9px] text-muted-foreground opacity-80">
						192.168.1.10:3306
					</span>
				</div>
			</div>

			<!-- 操作按钮区域 -->
			<div class="flex items-center gap-1">
				<!-- 编辑连接按钮：打开编辑连接弹窗 -->
				<button
					@click="$emit('edit-connection')"
					class="p-1.5 hover:bg-muted rounded-md text-muted-foreground hover:text-primary transition-all group"
					title="编辑连接">
					<!-- 铅笔图标 -->
					<svg
						class="w-3.5 h-3.5 group-hover:scale-110 transition-transform"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2.5">
						<path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
						<path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
					</svg>
				</button>

				<!-- 连接切换下拉菜单容器 -->
				<div class="relative">
					<!-- 切换连接按钮 -->
					<button
						@click.stop="$emit('toggle-switch-dropdown')"
						class="p-1.5 hover:bg-muted rounded-md text-muted-foreground hover:text-primary transition-all group"
						title="快速切换连接">
						<!-- 上下箭头图标 -->
						<svg
							class="w-3.5 h-3.5 group-hover:scale-110 transition-transform"
							viewBox="0 0 24 24"
							fill="none"
							stroke="currentColor"
							stroke-width="2.5">
							<path d="m7 15 5 5 5-5"></path>
							<path d="m7 9 5-5 5 5"></path>
						</svg>
					</button>

					<!-- 连接切换下拉菜单 -->
					<!-- v-if 控制显示/隐藏，带有淡入滑入动画 -->
					<div
						v-if="isSwitchDropdownOpen"
						class="absolute top-full left-0 mt-2 w-64 bg-card border border-border rounded-xl shadow-2xl py-2 z-50 animate-in fade-in slide-in-from-top-2 duration-200">
						<!-- 搜索框 -->
						<div class="px-3 py-1.5 mb-1 border-b border-border">
							<input
								type="text"
								placeholder="搜索连接..."
								class="w-full bg-muted/50 border-none rounded-md px-2 py-1 text-[10px] outline-none focus:ring-1 focus:ring-primary/30" />
						</div>

						<!-- 连接列表（可滚动） -->
						<div class="max-h-80 overflow-y-auto custom-scrollbar">
							<!-- 按文件夹分组显示连接 -->
							<div
								v-for="folder in mockFolders"
								:key="folder.id"
								class="mb-2 last:mb-0">
								<!-- 文件夹标题 -->
								<div
									class="px-3 py-1 text-[9px] font-bold text-muted-foreground uppercase tracking-widest flex items-center gap-1.5">
									<!-- 文件夹图标 -->
									<svg
										class="w-2.5 h-2.5"
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2.5">
										<path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
									</svg>
									{{ folder.name }}
								</div>
								<!-- 该文件夹下的连接列表 -->
								<button
									v-for="conn in getConnectionsInFolder(folder.id)"
									:key="conn.id"
									@click="$emit('switch-connection', conn)"
									class="w-full px-4 py-2 flex items-center gap-3 hover:bg-accent transition-colors group text-left">
									<!-- 数据库类型首字母图标 -->
									<div
										class="w-6 h-6 rounded bg-muted flex items-center justify-center text-[10px] font-bold text-muted-foreground group-hover:bg-primary/10 group-hover:text-primary transition-colors">
										{{ conn.type.charAt(0).toUpperCase() }}
									</div>
									<!-- 连接名称和主机 -->
									<div class="flex-1 min-w-0">
										<div class="text-[11px] font-semibold truncate">
											{{ conn.name }}
										</div>
										<div class="text-[9px] text-muted-foreground truncate">
											{{ conn.host }}
										</div>
									</div>
									<!-- 当前连接指示点 -->
									<div
										v-if="conn.id === '1'"
										class="w-1.5 h-1.5 rounded-full bg-green-500"></div>
								</button>
							</div>

							<!-- 未分类连接 -->
							<div class="mt-2 pt-2 border-t border-border">
								<!-- 未分类标题 -->
								<div
									class="px-3 py-1 text-[9px] font-bold text-muted-foreground uppercase tracking-widest flex items-center gap-1.5">
									<!-- 数据库图标 -->
									<svg
										class="w-2.5 h-2.5"
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2.5">
										<ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
										<path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
										<path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
									</svg>
									未分类
								</div>
								<!-- 未分类连接列表 -->
								<button
									v-for="conn in getConnectionsInFolder(null)"
									:key="conn.id"
									@click="$emit('switch-connection', conn)"
									class="w-full px-4 py-2 flex items-center gap-3 hover:bg-accent transition-colors group text-left">
									<div
										class="w-6 h-6 rounded bg-muted flex items-center justify-center text-[10px] font-bold text-muted-foreground group-hover:bg-primary/10 group-hover:text-primary transition-colors">
										{{ conn.type.charAt(0).toUpperCase() }}
									</div>
									<div class="flex-1 min-w-0">
										<div class="text-[11px] font-semibold truncate">
											{{ conn.name }}
										</div>
										<div class="text-[9px] text-muted-foreground truncate">
											{{ conn.host }}
										</div>
									</div>
								</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 右侧区域：功能按钮 -->
		<div class="flex items-center gap-4">
			<!-- AI 助手按钮 -->
			<button
				class="flex items-center gap-1.5 px-2 py-1 hover:bg-muted rounded text-muted-foreground hover:text-foreground transition-colors">
				<!-- 聊天图标 -->
				<svg
					class="w-3.5 h-3.5"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2">
					<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
				</svg>
				AI Assistant
			</button>

			<!-- 系统设置按钮 -->
			<button
				@click="router.push('/settings')"
				class="p-1 hover:bg-muted rounded text-muted-foreground hover:text-foreground transition-colors"
				title="系统设置">
				<!-- 齿轮图标 -->
				<svg
					class="w-4 h-4"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2">
					<circle cx="12" cy="12" r="3"></circle>
					<path
						d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
				</svg>
			</button>
		</div>
	</header>
</template>

<script setup lang="ts">
/**
 * 导入 Vue Router，用于页面导航
 */
import { useRouter } from "vue-router";

// ==================== Props 定义 ====================
// 父组件传入的连接切换下拉菜单状态
defineProps<{
	isSwitchDropdownOpen: boolean; // 是否显示连接切换下拉菜单
}>();

// ==================== 事件定义 ====================
// 定义组件可触发的事件，供父组件监听
defineEmits<{
	(e: "edit-connection"): void; // 点击编辑连接按钮
	(e: "toggle-switch-dropdown"): void; // 点击切换连接按钮
	(e: "switch-connection", conn: any): void; // 选择某个连接时触发
}>();

// ==================== 路由实例 ====================
const router = useRouter();

// ==================== 模拟数据 ====================

/**
 * mockFolders: 连接文件夹列表
 * 用于分组管理数据库连接
 */
const mockFolders = [
	{ id: "f1", name: "生产环境" },
	{ id: "f2", name: "开发环境" },
];

/**
 * mockConnections: 数据库连接列表（模拟数据）
 * 包含连接的完整信息：ID、名称、类型、主机地址、所属文件夹
 */
const mockConnections = [
	{
		id: "1",
		name: "Production MySQL",
		type: "mysql",
		host: "192.168.1.10",
		folderId: "f1", // 属于生产环境
	},
	{
		id: "5",
		name: "Staging MySQL",
		type: "mysql",
		host: "staging.internal",
		folderId: "f1",
	},
	{
		id: "2",
		name: "Local PostgreSQL",
		type: "postgresql",
		host: "localhost",
		folderId: "f2", // 属于开发环境
	},
	{
		id: "6",
		name: "Analytics PG",
		type: "postgresql",
		host: "analytics.db.internal",
		folderId: "f2",
	},
	{
		id: "3",
		name: "Redis Cache",
		type: "redis",
		host: "localhost",
		folderId: null, // 未分类
	},
	{
		id: "4",
		name: "Internal SQLite",
		type: "sqlite",
		host: "app.db",
		folderId: null,
	},
];

/**
 * getConnectionsInFolder: 获取指定文件夹下的所有连接
 * @param folderId - 文件夹 ID，null 表示获取未分类的连接
 * @returns 该文件夹下的连接数组
 */
function getConnectionsInFolder(folderId: string | null) {
	return mockConnections.filter((c) => c.folderId === folderId);
}
</script>

<style scoped>
/* 自定义滚动条样式 */

/* 滚动条基础样式 */
.custom-scrollbar::-webkit-scrollbar {
	width: 5px;
	height: 5px;
}

/* 滚动条轨道背景 */
.custom-scrollbar::-webkit-scrollbar-track {
	background: transparent;
}

/* 滚动条滑块样式 */
.custom-scrollbar::-webkit-scrollbar-thumb {
	background: hsl(var(--muted-foreground) / 0.2);
	border-radius: 10px;
}

/* 滚动条滑块悬停样式 */
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
	background: hsl(var(--muted-foreground) / 0.4);
}
</style>
