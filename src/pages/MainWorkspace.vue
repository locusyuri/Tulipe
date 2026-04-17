<template>
	<div class="h-full flex flex-col bg-background">
		<!-- Status Bar -->
		<header
			class="h-10 border-b border-border bg-card/50 flex items-center justify-between px-4 text-xs font-medium relative z-40">
			<div class="flex items-center gap-4">
				<button
					@click="router.push('/')"
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

			<!-- Centered Status Bar -->
			<div
				class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 flex items-center gap-3 px-4 py-1 bg-muted/50 rounded-full border border-border shadow-sm">
				<div class="flex items-center gap-2 pr-3 border-r border-border/50">
					<div
						class="w-2 h-2 rounded-full bg-green-500 animate-pulse shadow-[0_0_8px_rgba(34,197,94,0.6)]"></div>
					<div class="flex flex-col leading-tight">
						<span class="text-foreground font-bold tracking-tight">
							Production MySQL
						</span>
						<span class="text-[9px] text-muted-foreground opacity-80">
							192.168.1.10:3306
						</span>
					</div>
				</div>

				<div class="flex items-center gap-1">
					<button
						@click="isEditModalOpen = true"
						class="p-1.5 hover:bg-muted rounded-md text-muted-foreground hover:text-primary transition-all group"
						title="编辑连接">
						<svg
							class="w-3.5 h-3.5 group-hover:scale-110 transition-transform"
							viewBox="0 0 24 24"
							fill="none"
							stroke="currentColor"
							stroke-width="2.5">
							<path
								d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
							<path
								d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
						</svg>
					</button>

					<div class="relative">
						<button
							@click.stop="isSwitchDropdownOpen = !isSwitchDropdownOpen"
							class="p-1.5 hover:bg-muted rounded-md text-muted-foreground hover:text-primary transition-all group"
							title="快速切换连接">
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

						<!-- Switch Connection Dropdown -->
						<div
							v-if="isSwitchDropdownOpen"
							class="absolute top-full left-0 mt-2 w-64 bg-card border border-border rounded-xl shadow-2xl py-2 z-50 animate-in fade-in slide-in-from-top-2 duration-200">
							<div class="px-3 py-1.5 mb-1 border-b border-border">
								<input
									type="text"
									placeholder="搜索连接..."
									class="w-full bg-muted/50 border-none rounded-md px-2 py-1 text-[10px] outline-none focus:ring-1 focus:ring-primary/30" />
							</div>
							<div class="max-h-80 overflow-y-auto custom-scrollbar">
								<div
									v-for="folder in mockFolders"
									:key="folder.id"
									class="mb-2 last:mb-0">
									<div
										class="px-3 py-1 text-[9px] font-bold text-muted-foreground uppercase tracking-widest flex items-center gap-1.5">
										<svg
											class="w-2.5 h-2.5"
											viewBox="0 0 24 24"
											fill="none"
											stroke="currentColor"
											stroke-width="2.5">
											<path
												d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
										</svg>
										{{ folder.name }}
									</div>
									<button
										v-for="conn in getConnectionsInFolder(folder.id)"
										:key="conn.id"
										@click="switchConnection(conn)"
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
										<div
											v-if="conn.id === '1'"
											class="w-1.5 h-1.5 rounded-full bg-green-500"></div>
									</button>
								</div>

								<!-- Uncategorized -->
								<div class="mt-2 pt-2 border-t border-border">
									<div
										class="px-3 py-1 text-[9px] font-bold text-muted-foreground uppercase tracking-widest flex items-center gap-1.5">
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
									<button
										v-for="conn in getConnectionsInFolder(null)"
										:key="conn.id"
										@click="switchConnection(conn)"
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
					@click="router.push('/settings')"
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
		</header>

		<!-- Main Workspace Area (Three-Column Layout) -->
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
							@click="selectNavTab(tab)"
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
								@click.stop="isMoreNavTabsOpen = !isMoreNavTabsOpen"
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
										@click="selectNavTab(tab)"
										class="px-3 py-2 text-[10px] font-medium hover:bg-accent transition-all flex items-center gap-2 group/item border-l-2 border-transparent"
										:class="{
											'text-primary border-primary bg-primary/5':
												activeNavTab === tab,
											'bg-muted/40 font-bold': index < 3,
										}">
										<button
											@click.stop="moveTab(index, 'up')"
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
											@click.stop="moveTab(index, 'down')"
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
				@mousedown="startResizing('left')"></div>

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
				@mousedown="startResizing('right')"></div>

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

		<!-- Edit Connection Modal -->
		<div
			v-if="isEditModalOpen"
			class="fixed inset-0 bg-black/40 flex items-center justify-center z-[100] backdrop-blur-sm animate-in fade-in duration-200">
			<div
				class="bg-card border border-border rounded-2xl shadow-2xl w-[500px] overflow-hidden animate-in zoom-in-95 duration-200">
				<div
					class="px-6 py-4 border-b border-border flex items-center justify-between bg-muted/30">
					<div class="flex items-center gap-3">
						<div
							class="p-2 bg-primary/10 rounded-lg text-primary text-sm font-bold">
							M
						</div>
						<h3 class="text-lg font-bold">编辑连接: Production MySQL</h3>
					</div>
					<button
						@click="isEditModalOpen = false"
						class="p-1 hover:bg-muted rounded-full transition-colors text-muted-foreground">
						<svg
							class="w-5 h-5"
							viewBox="0 0 24 24"
							fill="none"
							stroke="currentColor"
							stroke-width="2">
							<line x1="18" y1="6" x2="6" y2="18"></line>
							<line x1="6" y1="6" x2="18" y2="18"></line>
						</svg>
					</button>
				</div>
				<div class="p-6 space-y-4">
					<div class="grid grid-cols-2 gap-4">
						<div class="space-y-1.5">
							<label
								class="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">
								连接名称
							</label>
							<input
								type="text"
								value="Production MySQL"
								class="w-full bg-muted border-none rounded-lg px-3 py-2 text-sm outline-none focus:ring-1 focus:ring-primary" />
						</div>
						<div class="space-y-1.5">
							<label
								class="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">
								数据库类型
							</label>
							<select
								class="w-full bg-muted border-none rounded-lg px-3 py-2 text-sm outline-none focus:ring-1 focus:ring-primary">
								<option>MySQL</option>
								<option>PostgreSQL</option>
								<option>SQLite</option>
							</select>
						</div>
					</div>
					<div class="grid grid-cols-3 gap-4">
						<div class="col-span-2 space-y-1.5">
							<label
								class="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">
								主机地址
							</label>
							<input
								type="text"
								value="192.168.1.10"
								class="w-full bg-muted border-none rounded-lg px-3 py-2 text-sm outline-none focus:ring-1 focus:ring-primary" />
						</div>
						<div class="space-y-1.5">
							<label
								class="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">
								端口
							</label>
							<input
								type="number"
								value="3306"
								class="w-full bg-muted border-none rounded-lg px-3 py-2 text-sm outline-none focus:ring-1 focus:ring-primary" />
						</div>
					</div>
					<div class="grid grid-cols-2 gap-4">
						<div class="space-y-1.5">
							<label
								class="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">
								用户名
							</label>
							<input
								type="text"
								value="root"
								class="w-full bg-muted border-none rounded-lg px-3 py-2 text-sm outline-none focus:ring-1 focus:ring-primary" />
						</div>
						<div class="space-y-1.5">
							<label
								class="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">
								密码
							</label>
							<input
								type="password"
								value="********"
								class="w-full bg-muted border-none rounded-lg px-3 py-2 text-sm outline-none focus:ring-1 focus:ring-primary" />
						</div>
					</div>
				</div>
				<div
					class="px-6 py-4 bg-muted/30 border-t border-border flex items-center justify-between">
					<button
						class="px-4 py-2 text-sm font-medium text-destructive hover:bg-destructive/10 rounded-lg transition-colors">
						删除连接
					</button>
					<div class="flex items-center gap-3">
						<button
							@click="isEditModalOpen = false"
							class="px-4 py-2 text-sm font-medium hover:bg-muted rounded-lg transition-colors">
							取消
						</button>
						<button
							@click="isEditModalOpen = false"
							class="px-4 py-2 text-sm font-medium bg-primary text-primary-foreground rounded-lg shadow-sm hover:opacity-90 transition-opacity">
							保存修改
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const isEditModalOpen = ref(false);
const isSwitchDropdownOpen = ref(false);

const objectNavWidth = ref(280);
const detailsPanelWidth = ref(300);
const activeNavTab = ref("Tables");
const isMoreNavTabsOpen = ref(false);

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

const visibleNavTabs = computed(() => {
	return allNavTabs.value.slice(0, 3);
});

function selectNavTab(tab: string) {
	activeNavTab.value = tab;
	isMoreNavTabsOpen.value = false;
}

function moveTab(index: number, direction: "up" | "down") {
	const tabs = [...allNavTabs.value];
	const newIndex = direction === "up" ? index - 1 : index + 1;
	if (newIndex < 0 || newIndex >= tabs.length) return;

	const [tab] = tabs.splice(index, 1);
	tabs.splice(newIndex, 0, tab);
	allNavTabs.value = tabs;
}
// ---------------------------------------------------------------

const resizingSide = ref<"left" | "right" | null>(null);

function startResizing(side: "left" | "right") {
	resizingSide.value = side;
	document.body.style.cursor = "col-resize";
}

function stopResizing() {
	resizingSide.value = null;
	document.body.style.cursor = "";
}

function handleResizing(e: MouseEvent) {
	if (!resizingSide.value) return;

	if (resizingSide.value === "left") {
		// Object Nav is after the 72px sidebar
		const newWidth = e.clientX - 72;
		if (newWidth > 150 && newWidth < 400) {
			objectNavWidth.value = newWidth;
		}
	} else if (resizingSide.value === "right") {
		const newWidth = window.innerWidth - e.clientX;
		if (newWidth > 150 && newWidth < 600) {
			detailsPanelWidth.value = newWidth;
		}
	}
}

const databases = ref([
	"information_schema",
	"mysql",
	"performance_schema",
	"sys",
	"tulipe_dev",
	"users_db",
	"order_system",
]);
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
const openTabs = ref([
	{ id: "1", name: "users", active: true },
	{ id: "2", name: "SQL Query 1", active: false },
	{ id: "3", name: "roles", active: false },
]);

// Mock data for connection switcher
const mockFolders = [
	{ id: "f1", name: "生产环境" },
	{ id: "f2", name: "开发环境" },
];

const mockConnections = [
	{
		id: "1",
		name: "Production MySQL",
		type: "mysql",
		host: "192.168.1.10",
		folderId: "f1",
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
		folderId: "f2",
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
		folderId: null,
	},
	{
		id: "4",
		name: "Internal SQLite",
		type: "sqlite",
		host: "app.db",
		folderId: null,
	},
];

function getConnectionsInFolder(folderId: string | null) {
	return mockConnections.filter((c) => c.folderId === folderId);
}

function switchConnection(conn: any) {
	console.log("Switching to", conn.name);
	isSwitchDropdownOpen.value = false;
}

// Close dropdown when clicking outside
function handleClickOutside(e: MouseEvent) {
	const target = e.target as HTMLElement;
	// 如果点击的是按钮本身，由按钮的 @click 处理切换，这里直接返回
	if (target.closest(".relative > button")) return;

	// 否则点击外部时关闭所有下拉
	isSwitchDropdownOpen.value = false;
	isMoreNavTabsOpen.value = false;
}

onMounted(() => {
	document.addEventListener("click", handleClickOutside);
	window.addEventListener("mousemove", handleResizing);
	window.addEventListener("mouseup", stopResizing);
});

onUnmounted(() => {
	document.removeEventListener("click", handleClickOutside);
	window.removeEventListener("mousemove", handleResizing);
	window.removeEventListener("mouseup", stopResizing);
});
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
