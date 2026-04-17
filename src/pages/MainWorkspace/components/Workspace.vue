<template>
	<main
		class="flex-1 flex overflow-hidden relative"
		:class="{ 'select-none': !!resizingSide }">
		<!-- Left Column: Database Icon Bar -->
		<aside
			class="w-[72px] border-r border-border flex flex-col bg-card/20 flex-shrink-0">
			<div
				class="p-2 border-b border-border flex items-center justify-center">
				<span
					class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
					DB
				</span>
			</div>
			<div class="flex-1 overflow-y-auto p-2 space-y-1.5 custom-scrollbar">
				<div
					v-for="db in databases"
					:key="db"
					:title="db"
					class="flex flex-col items-center justify-center py-2 px-1 rounded-lg hover:bg-accent group cursor-pointer transition-colors">
					<svg
						class="w-5 h-5 text-muted-foreground group-hover:text-primary transition-colors"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2">
						<ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
						<path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
						<path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
					</svg>
					<span
						class="text-[10px] text-muted-foreground group-hover:text-foreground transition-colors truncate w-full text-center mt-1">
						{{ db.length > 5 ? db.substring(0, 5) + ".." : db }}
					</span>
				</div>
			</div>
		</aside>

		<!-- Middle Column: Object Navigator -->
		<section
			class="border-r border-border flex flex-col bg-background flex-shrink-0"
			:style="{ width: objectNavWidth + 'px' }">
			<div
				class="h-10 border-b border-border flex items-center bg-muted/20 relative group/tabs">
				<div class="flex-1 flex items-center px-1 gap-1 h-full">
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

					<!-- More Button -->
					<div class="relative flex-shrink-0">
						<button
							@click.stop="$emit('toggle-more-tabs')"
							class="p-1.5 hover:bg-muted rounded-md text-muted-foreground transition-all group/more"
							:class="{
								'bg-accent text-foreground':
									isMoreNavTabsOpen || !visibleNavTabs.includes(activeNavTab),
							}">
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

						<!-- More Tabs Dropdown -->
						<div
							v-if="isMoreNavTabsOpen"
							class="absolute top-full right-0 mt-1 w-44 bg-card border border-border rounded-lg shadow-xl py-1 z-50 animate-in fade-in zoom-in-95 duration-100">
							<div
								class="px-2 py-1 mb-1 border-b border-border/50 flex items-center justify-between">
								<span
									class="text-[9px] font-bold text-muted-foreground uppercase tracking-widest">
									所有项目 (点击切换 / 箭头排序)
								</span>
							</div>
							<div class="max-h-60 overflow-y-auto custom-scrollbar">
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
									<span class="truncate flex-1">{{ tab }}</span>
									<div
										v-if="index < 3"
										class="text-[8px] bg-primary/20 text-primary px-1 rounded flex-shrink-0">
										栏内
									</div>
									<div
										v-if="activeNavTab === tab"
										class="w-1.5 h-1.5 rounded-full bg-primary flex-shrink-0"></div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="flex-1 overflow-y-auto p-2 space-y-1 custom-scrollbar">
				<div
					v-for="table in tables"
					:key="table"
					class="flex items-center gap-2 px-3 py-2 text-sm rounded-md hover:bg-accent group cursor-pointer">
					<svg
						class="w-4 h-4 text-muted-foreground group-hover:text-primary transition-colors"
						viewBox="0 0 24 24"
						fill="none"
						stroke="currentColor"
						stroke-width="2">
						<rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
						<line x1="3" y1="9" x2="21" y2="9"></line>
						<line x1="9" y1="21" x2="9" y2="9"></line>
					</svg>
					<span class="truncate">{{ table }}</span>
				</div>
			</div>
		</section>

		<!-- Left Resizer -->
		<div
			class="w-1 hover:w-1.5 bg-border hover:bg-primary/40 cursor-col-resize transition-all z-10 active:bg-primary"
			@mousedown="$emit('start-resizing', 'left')"></div>

		<!-- Right Column: Workspace / Content Area -->
		<section
			class="flex-1 flex flex-col bg-muted/10 overflow-hidden min-w-[300px]">
			<!-- Tabs for open tables/queries -->
			<div
				class="h-10 border-b border-border flex items-center px-2 bg-card/30">
				<div
					class="flex h-full items-center gap-1 overflow-x-auto scrollbar-hide">
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

			<!-- Table Content (AG Grid placeholder) -->
			<div class="flex-1 p-4 overflow-hidden flex flex-col">
				<div class="flex items-center justify-between mb-4 flex-wrap gap-4">
					<div class="flex items-center gap-4">
						<h2 class="text-xl font-bold">users</h2>
						<div
							class="flex items-center gap-2 px-2 py-1 bg-muted rounded text-xs font-mono">
							<span class="text-muted-foreground">Rows:</span>
							<span>1,248</span>
						</div>
					</div>
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

				<!-- AG Grid Placeholder Mockup -->
				<div
					class="flex-1 bg-card border border-border rounded-lg shadow-inner overflow-hidden flex flex-col">
					<div
						class="grid grid-cols-5 border-b border-border bg-muted/30 text-xs font-bold text-muted-foreground">
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
					<div
						class="flex-1 overflow-y-auto bg-background divide-y divide-border custom-scrollbar">
						<div
							v-for="i in 20"
							:key="i"
							class="grid grid-cols-5 text-xs hover:bg-accent/30 transition-colors">
							<div
								class="px-4 py-2 border-r border-border last:border-0 font-mono text-muted-foreground">
								{{ i }}
							</div>
							<div
								class="px-4 py-2 border-r border-border last:border-0 font-medium">
								user_{{ i }}
							</div>
							<div
								class="px-4 py-2 border-r border-border last:border-0 text-blue-500 underline underline-offset-2">
								user_{{ i }}@example.com
							</div>
							<div
								class="px-4 py-2 border-r border-border last:border-0 text-muted-foreground">
								2024-04-14 12:00:00
							</div>
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

		<!-- Right Resizer -->
		<div
			class="w-1 hover:w-1.5 bg-border hover:bg-primary/40 cursor-col-resize transition-all z-10 active:bg-primary"
			@mousedown="$emit('start-resizing', 'right')"></div>

		<!-- Right Side: Details/AI Panel (Collapsible) -->
		<aside
			class="border-l border-border bg-card/20 flex flex-col overflow-hidden flex-shrink-0"
			:style="{ width: detailsPanelWidth + 'px' }">
			<div
				class="h-10 border-b border-border flex items-center px-4 justify-between">
				<span
					class="text-xs font-bold uppercase tracking-widest text-muted-foreground">
					Details
				</span>
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
			<div class="flex-1 p-4 space-y-6 overflow-y-auto custom-scrollbar">
				<div class="space-y-2">
					<h4
						class="text-[11px] font-bold text-muted-foreground uppercase tracking-wider">
						Information
					</h4>
					<div class="space-y-1">
						<div
							class="flex justify-between text-xs py-1 border-b border-border/50">
							<span class="text-muted-foreground">Type</span>
							<span class="font-medium">BASE TABLE</span>
						</div>
						<div
							class="flex justify-between text-xs py-1 border-b border-border/50">
							<span class="text-muted-foreground">Engine</span>
							<span class="font-medium">InnoDB</span>
						</div>
						<div
							class="flex justify-between text-xs py-1 border-b border-border/50">
							<span class="text-muted-foreground">Collation</span>
							<span class="font-medium">utf8mb4_unicode_ci</span>
						</div>
					</div>
				</div>

				<div class="space-y-2">
					<h4
						class="text-[11px] font-bold text-muted-foreground uppercase tracking-wider">
						AI Insight
					</h4>
					<div
						class="p-3 bg-primary/5 rounded-lg border border-primary/10 text-xs leading-relaxed space-y-2">
						<p>这个表看起来存储了用户信息。索引优化建议：</p>
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
defineProps<{
	objectNavWidth: number;
	detailsPanelWidth: number;
	activeNavTab: string;
	isMoreNavTabsOpen: boolean;
	allNavTabs: string[];
	visibleNavTabs: string[];
	databases: string[];
	tables: string[];
	openTabs: { id: string; name: string; active: boolean }[];
	resizingSide: "left" | "right" | null;
}>();

defineEmits<{
	(e: "select-nav-tab", tab: string): void;
	(e: "toggle-more-tabs"): void;
	(e: "move-tab", index: number, direction: "up" | "down"): void;
	(e: "start-resizing", side: "left" | "right"): void;
}>();
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
	display: none;
}

.custom-scrollbar::-webkit-scrollbar {
	width: 5px;
	height: 5px;
}

.custom-scrollbar::-webkit-scrollbar-track {
	background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
	background: hsl(var(--muted-foreground) / 0.2);
	border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
	background: hsl(var(--muted-foreground) / 0.4);
}
</style>
