<template>
	<!-- 顶部工具栏：返回、AI Assistant、设置入口 -->
	<div
		class="h-10 border-b border-border bg-card/50 flex items-center justify-between px-4 text-xs font-medium relative z-40">
		<div class="flex items-center gap-4">
			<button
				@click="emit('back')"
				class="p-1 hover:bg-muted rounded text-muted-foreground hover:text-foreground transition-colors"
				title="返回欢迎页">
				<svg
					class="w-4 h-4"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2">
					<path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
					<polyline points="9 22 9 12 15 12 15 22"></polyline>
				</svg>
			</button>
		</div>

		<div class="flex items-center gap-4">
			<button
				class="flex items-center gap-1.5 px-2 py-1 hover:bg-muted rounded text-muted-foreground hover:text-foreground transition-colors">
				<svg
					class="w-3.5 h-3.5"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="2">
					<path
						d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
				</svg>
				AI Assistant
			</button>
			<button
				@click="emit('settings')"
				class="p-1 hover:bg-muted rounded text-muted-foreground hover:text-foreground transition-colors"
				title="系统设置">
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
	</div>

	<!-- 欢迎区头部：品牌信息 + 新建连接 + 活跃连接卡片 -->
	<div class="flex-shrink-0 border-b border-border bg-muted/30">
		<div class="px-8 pt-6 pb-4">
			<div class="flex items-center justify-between mb-4">
				<div class="flex items-center gap-3">
					<div class="p-2.5 bg-primary rounded-xl shadow-lg">
						<svg
							class="w-7 h-7 text-primary-foreground"
							viewBox="0 0 24 24"
							fill="none"
							stroke="currentColor"
							stroke-width="2">
							<path
								d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
						</svg>
					</div>
					<div>
						<h1 class="text-2xl font-bold tracking-tight text-foreground">
							Tulipe
						</h1>
						<p class="text-sm text-muted-foreground">现代化数据库管理工具</p>
					</div>
				</div>
				<div class="flex items-center gap-2">
					<button
						@click="emit('create-new-connection')"
						:disabled="isLoadingTestApi"
						class="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg font-medium shadow-sm hover:opacity-90 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed">
						<svg
							v-if="isLoadingTestApi"
							class="w-4 h-4 animate-spin"
							viewBox="0 0 24 24"
							fill="none"
							stroke="currentColor"
							stroke-width="2">
							<circle
								cx="12"
								cy="12"
								r="10"
								stroke-dasharray="31.4"
								stroke-dashoffset="10"></circle>
						</svg>
						<svg
							v-else
							class="w-4 h-4"
							viewBox="0 0 24 24"
							fill="none"
							stroke="currentColor"
							stroke-width="2">
							<line x1="12" y1="5" x2="12" y2="19"></line>
							<line x1="5" y1="12" x2="19" y2="12"></line>
						</svg>
						{{ isLoadingTestApi ? "正在连接..." : "新建连接" }}
					</button>
				</div>
			</div>

			<div v-if="activeConnections.length > 0" class="space-y-2">
				<div class="flex items-center justify-between">
					<h3
						class="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-2">
						<div class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
						已连接 ({{ activeConnections.length }})
					</h3>
				</div>
				<div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
					<div
						v-for="conn in activeConnections"
						:key="conn.id"
						@click="emit('connect', conn)"
						class="group p-3 bg-card border border-border rounded-xl hover:border-primary/40 hover:shadow-md cursor-pointer transition-all duration-200">
						<div class="flex items-center gap-3">
							<div
								class="w-9 h-9 rounded-lg flex items-center justify-center text-sm font-bold transition-colors"
								:class="
									dbTypeColors[conn.type] || 'bg-muted text-muted-foreground'
								">
								{{
									dbTypeIcons[conn.type] || conn.type.charAt(0).toUpperCase()
								}}
							</div>
							<div class="flex-1 min-w-0">
								<div class="font-medium text-sm truncate">{{ conn.name }}</div>
								<div class="text-xs text-muted-foreground truncate">
									{{ conn.host }}:{{ conn.port }}
								</div>
							</div>
							<div
								class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
								<button
									@click.stop="emit('disconnect', conn)"
									class="p-1 hover:bg-destructive/10 rounded text-muted-foreground hover:text-destructive transition-colors"
									title="断开连接">
									<svg
										class="w-3.5 h-3.5"
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2">
										<path d="M18 6L6 18M6 6l12 12"></path>
									</svg>
								</button>
							</div>
						</div>
						<div class="mt-2 flex items-center gap-2 text-xs">
							<span
								class="px-1.5 py-0.5 rounded text-[10px] uppercase font-bold"
								:class="
									dbTypeBadgeColors[conn.type] ||
									'bg-muted text-muted-foreground'
								">
								{{ conn.type }}
							</span>
							<span class="text-muted-foreground">
								{{ conn.database || "-" }}
							</span>
						</div>
					</div>
				</div>
			</div>
			<div v-else class="py-4 text-center text-sm text-muted-foreground">
				暂无活跃连接，点击「新建连接」开始使用
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { type PropType } from "vue";

// 顶部卡片所需连接结构
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

// 头部对外事件
const emit = defineEmits<{
	(e: "back"): void;
	(e: "create-new-connection"): void;
	(e: "connect", conn: Connection): void;
	(e: "disconnect", conn: Connection): void;
	(e: "settings"): void;
}>();

// 头部渲染依赖的状态与映射
defineProps({
	isLoadingTestApi: {
		type: Boolean,
		required: true,
	},
	activeConnections: {
		type: Array as PropType<Connection[]>,
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
</script>
