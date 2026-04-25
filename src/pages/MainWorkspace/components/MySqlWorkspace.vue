<!--
  Workspace.vue - 主工作区组件

  功能说明：
  - 实现三栏可拖拽调整的布局
  - 左侧栏：数据库图标列表（72px 固定宽度）
  - 中间栏：对象导航面板（可拖拽调整宽度 150-400px）
  - 右侧栏：数据表格展示区 + 详情面板（可拖拽调整宽度 150-600px）

  组件结构：
  - DatabaseBar: 左侧数据库图标栏
  - ObjectNavigator: 中间对象导航（包含标签切换和对象列表）
  - DataTable: 右侧数据表格展示
  - DetailsPanel: 右侧详情/AI面板
-->
<template>
	<!--
    主工作区容器
    - flex-1: 占据除 Header 外的全部垂直空间
    - overflow-hidden: 隐藏溢出内容
    - select-none: 拖拽时禁用文本选择
  -->
	<main
		class="flex-1 flex overflow-hidden relative"
		:class="{ 'select-none': !!resizingSide }">

		<!-- ==================== 左侧栏：数据库图标列表 ==================== -->
		<!-- 固定宽度 72px，不可调整 -->
		<aside
			class="w-[72px] border-r border-border flex flex-col bg-card/20 flex-shrink-0">
			<!-- 标题区域 -->
			<div
				class="p-2 border-b border-border flex items-center justify-center">
				<span
					class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
					DB
				</span>
			</div>

			<!-- 数据库列表（可滚动） -->
			<div class="flex-1 overflow-y-auto p-2 space-y-1.5 custom-scrollbar">
				<!--
          遍历显示所有数据库
          每个数据库显示数据库图标和缩写名称
        -->
				<div
					v-for="db in databases"
					:key="db"
					:title="db"
					class="flex flex-col items-center justify-center py-2 px-1 rounded-lg hover:bg-accent group cursor-pointer transition-colors">
					<!-- 数据库图标 SVG -->
					<svg
						class="w-5 h-5 text-muted-foreground group-hover:text-primary transition-colors"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2">
						<!-- 圆柱体顶部椭圆 -->
						<ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
						<!-- 中间连接线 -->
						<path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
						<!-- 圆柱体侧面 -->
						<path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
					</svg>
					<!-- 数据库名称（超过5个字符截断并加..） -->
					<span
						class="text-[10px] text-muted-foreground group-hover:text-foreground transition-colors truncate w-full text-center mt-1">
						{{ db.length > 5 ? db.substring(0, 5) + ".." : db }}
					</span>
				</div>
			</div>
		</aside>

		<!-- ==================== 中间栏：对象导航面板 ==================== -->
		<!-- 宽度可拖拽调整 -->
		<section
			class="border-r border-border flex flex-col bg-background flex-shrink-0"
			:style="{ width: objectNavWidth + 'px' }">
			<!-- 标签切换区域 -->
			<div
				class="h-10 border-b border-border flex items-center bg-muted/20 relative group/tabs">
				<div class="flex-1 flex items-center px-1 gap-1 h-full">
					<!--
            可见标签按钮（从所有标签中取前3个）
            当前选中的标签高亮显示
          -->
					<button
						v-for="tab in visibleNavTabs"
						:key="tab"
						@click="$emit('select-nav-tab', tab)"
						class="flex-1 min-w-0 whitespace-nowrap px-2 py-1 text-[10px] font-bold rounded-md transition-all duration-200 truncate"
						:class="
							activeNavTab === tab
								? 'bg-primary text-primary-foreground shadow-sm'
								: 'text-muted-foreground hover:bg-muted hover:text-foreground'
						"
						:title="tab">
						{{ tab }}
					</button>

					<!-- "更多"按钮：展开更多标签选项 -->
					<div class="relative flex-shrink-0">
						<button
							@click.stop="$emit('toggle-more-tabs')"
							class="p-1.5 hover:bg-muted rounded-md text-muted-foreground transition-all group/more"
							:class="{
								'bg-accent text-foreground':
									isMoreNavTabsOpen || !visibleNavTabs.includes(activeNavTab),
							}">
							<!-- 三个点图标 -->
							<svg
								class="w-3.5 h-3.5"
								viewBox="0 0 24 24"
								fill="none"
								stroke="currentColor"
								stroke-width="2.5">
								<circle cx="12" cy="12" r="1"></circle>
								<circle cx="19" cy="12" r="1"></circle>
								<circle cx="5" cy="12" r="1"></circle>
							</svg>
						</button>

						<!-- 更多标签下拉菜单 -->
						<!-- 显示所有标签，支持排序和选择 -->
						<div
							v-if="isMoreNavTabsOpen"
							class="absolute top-full right-0 mt-1 w-44 bg-card border border-border rounded-lg shadow-xl py-1 z-50 animate-in fade-in zoom-in-95 duration-100">
							<!-- 菜单标题 -->
							<div
								class="px-2 py-1 mb-1 border-b border-border/50 flex items-center justify-between">
								<span
									class="text-[9px] font-bold text-muted-foreground uppercase tracking-widest">
									所有项目 (点击切换 / 箭头排序)
								</span>
							</div>

							<!-- 标签列表（可滚动，最大高度 240px） -->
							<div class="max-h-60 overflow-y-auto custom-scrollbar">
								<!--
                  遍历所有标签
                  - 前3个标签显示"栏内"标签
                  - 当前选中标签显示高亮和圆点指示
                  - 每个标签支持上下移动排序
                -->
								<div
									v-for="(tab, index) in allNavTabs"
									:key="tab"
									@click="$emit('select-nav-tab', tab)"
									class="px-3 py-2 text-[10px] font-medium hover:bg-accent transition-all flex items-center gap-2 group/item border-l-2 border-transparent"
									:class="{
										'text-primary border-primary bg-primary/5':
											activeNavTab === tab,
										'bg-muted/40 font-bold': index < 3,
									}">
									<!-- 上移按钮 -->
									<button
										@click.stop="$emit('move-tab', index, 'up')"
										:disabled="index === 0"
										class="p-0.5 hover:bg-muted rounded text-muted-foreground hover:text-foreground disabled:opacity-20 disabled:cursor-not-allowed transition-colors">
										<svg
											class="w-3 h-3"
											viewBox="0 0 24 24"
											fill="none"
											stroke="currentColor"
											stroke-width="2.5">
											<path d="m18 15-6-6-6 6" />
										</svg>
									</button>

									<!-- 下移按钮 -->
									<button
										@click.stop="$emit('move-tab', index, 'down')"
										:disabled="index === allNavTabs.length - 1"
										class="p-0.5 hover:bg-muted rounded text-muted-foreground hover:text-foreground disabled:opacity-20 disabled:cursor-not-allowed transition-colors">
										<svg
											class="w-3 h-3"
											viewBox="0 0 24 24"
											fill="none"
											stroke="currentColor"
											stroke-width="2.5">
											<path d="m6 9 6 6 6-6" />
										</svg>
									</button>

									<!-- 标签名称 -->
									<span class="truncate flex-1">{{ tab }}</span>

									<!-- "栏内"标签（前3个显示） -->
									<div
										v-if="index < 3"
										class="text-[8px] bg-primary/20 text-primary px-1 rounded flex-shrink-0">
										栏内
									</div>

									<!-- 当前选中指示点 -->
									<div
										v-if="activeNavTab === tab"
										class="w-1.5 h-1.5 rounded-full bg-primary flex-shrink-0"></div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>

			<!-- 对象列表区域（根据选中的标签显示对应内容） -->
			<div class="flex-1 overflow-y-auto p-2 space-y-1 custom-scrollbar">
				<!--
          遍历显示对象列表（这里是模拟的表列表）
          实际应用中应根据 activeNavTab 加载对应类型的数据
        -->
				<div
					v-for="table in tables"
					:key="table"
					class="flex items-center gap-2 px-3 py-2 text-sm rounded-md hover:bg-accent group cursor-pointer">
					<!-- 表图标 -->
					<svg
						class="w-4 h-4 text-muted-foreground group-hover:text-primary transition-colors"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2">
						<!-- 矩形边框 -->
						<rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
						<!-- 表头分隔线 -->
						<line x1="3" y1="9" x2="21" y2="9"></line>
						<!-- 列分隔线 -->
						<line x1="9" y1="21" x2="9" y2="9"></line>
					</svg>
					<span class="truncate">{{ table }}</span>
				</div>
			</div>
		</section>

		<!-- ==================== 左侧拖拽调整条 ==================== -->
		<!-- 拖拽此区域可调整中间对象导航面板的宽度 -->
		<div
			class="w-1 hover:w-1.5 bg-border hover:bg-primary/40 cursor-col-resize transition-all z-10 active:bg-primary"
			@mousedown="$emit('start-resizing', 'left')"></div>

		<!-- ==================== 右侧栏：数据表格区域 ==================== -->
		<section
			class="flex-1 flex flex-col bg-muted/10 overflow-hidden min-w-[300px]">
			<!-- 打开的标签页列表 -->
			<div
				class="h-10 border-b border-border flex items-center px-2 bg-card/30">
				<!-- 标签列表（可横向滚动） -->
				<div
					class="flex h-full items-center gap-1 overflow-x-auto scrollbar-hide">
					<!--
              遍历显示打开的标签
              当前活动标签高亮显示
            -->
					<div
						v-for="tab in openTabs"
						:key="tab.id"
						class="flex items-center gap-2 px-3 h-8 text-xs rounded-t-md border border-b-0 border-border group"
						:class="
							tab.active
								? 'bg-background font-bold text-foreground'
								: 'text-muted-foreground bg-muted/50 hover:bg-background/80 cursor-pointer'
						">
						<span class="truncate max-w-[120px]">{{ tab.name }}</span>
						<!-- 关闭按钮（悬停时显示） -->
						<button
							class="opacity-0 group-hover:opacity-100 p-0.5 hover:bg-muted-foreground/20 rounded-full transition-opacity">
							<svg
								class="w-3 h-3"
								viewBox="0 0 24 24"
								fill="none"
								stroke="currentColor"
								stroke-width="2">
								<line x1="18" y1="6" x2="6" y2="18"></line>
								<line x1="6" y1="6" x2="18" y2="18"></line>
							</svg>
						</button>
					</div>
				</div>

				<!-- 新建标签按钮 -->
				<button
					class="ml-2 p-1.5 hover:bg-muted rounded text-muted-foreground transition-colors">
					<svg
						class="w-4 h-4"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2">
						<line x1="12" y1="5" x2="12" y2="19"></line>
						<line x1="5" y1="12" x2="19" y2="12"></line>
					</svg>
				</button>
			</div>

			<!-- 数据表格区域 -->
			<div class="flex-1 p-4 overflow-hidden flex flex-col">
				<!-- 表格标题栏：表名和操作按钮 -->
				<div class="flex items-center justify-between mb-4 flex-wrap gap-4">
					<div class="flex items-center gap-4">
						<h2 class="text-xl font-bold">users</h2>
						<!-- 行数统计 -->
						<div
							class="flex items-center gap-2 px-2 py-1 bg-muted rounded text-xs font-mono">
							<span class="text-muted-foreground">Rows:</span>
							<span>1,248</span>
						</div>
					</div>

					<!-- 操作按钮 -->
					<div class="flex items-center gap-2">
						<button
							class="px-3 py-1.5 bg-primary text-primary-foreground text-xs rounded font-medium shadow-sm hover:opacity-90">
							Save Changes
						</button>
						<button
							class="px-3 py-1.5 bg-muted text-foreground text-xs rounded font-medium hover:bg-accent transition-colors">
							Refresh
						</button>
					</div>
				</div>

				<!-- 数据表格（模拟 AG Grid） -->
				<div
					class="flex-1 bg-card border border-border rounded-lg shadow-inner overflow-hidden flex flex-col">
					<!-- 表头 -->
					<div
						class="grid grid-cols-5 border-b border-border bg-muted/30 text-xs font-bold text-muted-foreground">
						<!--
                遍历显示列信息
                列：id, username, email, created_at, status
                悬停时显示排序箭头
              -->
						<div
							v-for="col in [
								'id',
								'username',
								'email',
								'created_at',
								'status',
							]"
							:key="col"
							class="px-4 py-2 border-r border-border last:border-0 flex items-center justify-between group">
							{{ col.toUpperCase() }}
							<svg
								class="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity"
								viewBox="0 0 24 24"
								fill="none"
								stroke="currentColor"
								stroke-width="2">
								<path d="m6 9 6 6 6-6"></path>
							</svg>
						</div>
					</div>

					<!-- 表格数据（模拟数据，仅显示前20行） -->
					<div
						class="flex-1 overflow-y-auto bg-background divide-y divide-border custom-scrollbar">
						<div
							v-for="i in 20"
							:key="i"
							class="grid grid-cols-5 text-xs hover:bg-accent/30 transition-colors">
							<!-- ID 列 -->
							<div
								class="px-4 py-2 border-r border-border last:border-0 font-mono text-muted-foreground">
								{{ i }}
							</div>
							<!-- 用户名列 -->
							<div
								class="px-4 py-2 border-r border-border last:border-0 font-medium">
								user_{{ i }}
							</div>
							<!-- 邮箱列（可点击链接样式） -->
							<div
								class="px-4 py-2 border-r border-border last:border-0 text-blue-500 underline underline-offset-2">
								user_{{ i }}@example.com
							</div>
							<!-- 创建时间列 -->
							<div
								class="px-4 py-2 border-r border-border last:border-0 text-muted-foreground">
								2024-04-14 12:00:00
							</div>
							<!-- 状态列 -->
							<div
								class="px-4 py-2 border-r border-border last:border-0 flex items-center gap-2">
								<div class="w-1.5 h-1.5 rounded-full bg-green-500"></div>
								Active
							</div>
						</div>
					</div>
				</div>
			</div>
		</section>

		<!-- ==================== 右侧拖拽调整条 ==================== -->
		<!-- 拖拽此区域可调整右侧详情面板的宽度 -->
		<div
			class="w-1 hover:w-1.5 bg-border hover:bg-primary/40 cursor-col-resize transition-all z-10 active:bg-primary"
			@mousedown="$emit('start-resizing', 'right')"></div>

		<!-- ==================== 右侧栏：详情/AI面板 ==================== -->
		<!-- 显示当前选中对象的详细信息和 AI 建议 -->
		<aside
			class="border-l border-border bg-card/20 flex flex-col overflow-hidden flex-shrink-0"
			:style="{ width: detailsPanelWidth + 'px' }">
			<!-- 面板标题栏 -->
			<div
				class="h-10 border-b border-border flex items-center px-4 justify-between">
				<span
					class="text-xs font-bold uppercase tracking-widest text-muted-foreground">
					Details
				</span>
				<!-- 关闭按钮 -->
				<button
					class="p-1 hover:bg-muted rounded transition-colors text-muted-foreground">
					<svg
						class="w-4 h-4"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2">
						<line x1="18" y1="6" x2="6" y2="18"></line>
						<line x1="6" y1="6" x2="18" y2="18"></line>
					</svg>
				</button>
			</div>

			<!-- 面板内容区域 -->
			<div class="flex-1 p-4 space-y-6 overflow-y-auto custom-scrollbar">
				<!-- 信息区域：显示对象元数据 -->
				<div class="space-y-2">
					<h4
						class="text-[11px] font-bold text-muted-foreground uppercase tracking-wider">
						Information
					</h4>
					<div class="space-y-1">
						<!-- 类型信息 -->
						<div
							class="flex justify-between text-xs py-1 border-b border-border/50">
							<span class="text-muted-foreground">Type</span>
							<span class="font-medium">BASE TABLE</span>
						</div>
						<!-- 存储引擎 -->
						<div
							class="flex justify-between text-xs py-1 border-b border-border/50">
							<span class="text-muted-foreground">Engine</span>
							<span class="font-medium">InnoDB</span>
						</div>
						<!-- 字符集/排序规则 -->
						<div
							class="flex justify-between text-xs py-1 border-b border-border/50">
							<span class="text-muted-foreground">Collation</span>
							<span class="font-medium">utf8mb4_unicode_ci</span>
						</div>
					</div>
				</div>

				<!-- AI 洞察区域：显示 AI 生成的建议 -->
				<div class="space-y-2">
					<h4
						class="text-[11px] font-bold text-muted-foreground uppercase tracking-wider">
						AI Insight
					</h4>
					<div
						class="p-3 bg-primary/5 rounded-lg border border-primary/10 text-xs leading-relaxed space-y-2">
						<!-- AI 分析结果 -->
						<p>这个表看起来存储了用户信息。索引优化建议：</p>
						<!-- 建议列表 -->
						<ul class="list-disc list-inside space-y-1 text-muted-foreground">
							<li>
								为
								<code class="bg-muted px-1 rounded">email</code>
								字段添加唯一索引
							</li>
							<li>
								考虑对
								<code class="bg-muted px-1 rounded">created_at</code>
								进行分区优化
							</li>
						</ul>
						<!-- 询问更多按钮 -->
						<button
							class="w-full mt-2 py-1.5 bg-primary/10 text-primary hover:bg-primary/20 rounded font-medium transition-colors">
							问问 AI 更多...
						</button>
					</div>
				</div>
			</div>
		</aside>
	</main>
</template>

<script setup lang="ts">
/**
 * Workspace 组件：主工作区组件
 *
 * Props：接收父组件传来的状态数据
 * Emits：向父组件发送事件（如标签选择、拖拽开始等）
 */

// ==================== Props 定义 ====================
// 接收父组件传入的状态
defineProps<{
	objectNavWidth: number; // 中间对象导航面板宽度
	detailsPanelWidth: number; // 右侧详情面板宽度
	activeNavTab: string; // 当前选中的导航标签
	isMoreNavTabsOpen: boolean; // 更多标签下拉菜单是否打开
	allNavTabs: string[]; // 所有导航标签列表
	visibleNavTabs: string[]; // 可见的导航标签（前3个）
	databases: string[]; // 数据库列表
	tables: string[]; // 表列表
	openTabs: { id: string; name: string; active: boolean }[]; // 打开的标签页
	resizingSide: "left" | "right" | null; // 当前拖拽的边栏
}>();

// ==================== 事件定义 ====================
// 定义组件可触发的事件
defineEmits<{
	(e: "select-nav-tab", tab: string): void; // 选择导航标签
	(e: "toggle-more-tabs"): void; // 切换更多标签下拉菜单
	(e: "move-tab", index: number, direction: "up" | "down"): void; // 移动标签位置
	(e: "start-resizing", side: "left" | "right"): void; // 开始拖拽调整宽度
}>();
</script>

<style scoped>
/* 隐藏滚动条（用于标签页横向滚动） */
.scrollbar-hide::-webkit-scrollbar {
	display: none;
}

/* 自定义滚动条样式（可滚动区域使用） */

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
