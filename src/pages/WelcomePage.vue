<template>
	<div class="h-full flex flex-col bg-background overflow-hidden">
		<!-- Top Section: Header + Connected Pool Cards -->
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
							@click="createNewConnection"
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

				<!-- Connected Pool Cards -->
				<div v-if="activeConnections.length > 0" class="space-y-2">
					<div class="flex items-center justify-between">
						<h3
							class="text-xs font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-2">
							<div
								class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
							已连接 ({{ activeConnections.length }})
						</h3>
					</div>
					<div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
						<div
							v-for="conn in activeConnections"
							:key="conn.id"
							@click="connect(conn)"
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
									<div class="font-medium text-sm truncate">
										{{ conn.name }}
									</div>
									<div class="text-xs text-muted-foreground truncate">
										{{ conn.host }}:{{ conn.port }}
									</div>
								</div>
								<div
									class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
									<button
										@click.stop="disconnectConnection(conn)"
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

		<!-- Bottom Section: Connection Library with Folders -->
		<div class="flex-1 flex overflow-hidden">
			<!-- Left: Folder Tree -->
			<aside class="w-56 border-r border-border flex flex-col bg-card/30">
				<div
					class="p-3 border-b border-border flex items-center justify-between">
					<h3
						class="text-xs font-bold text-muted-foreground uppercase tracking-wider">
						连接分类
					</h3>
					<button
						@click="createFolder"
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
					<!-- All Connections -->
					<button
						@click="selectedFolderId = null"
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

					<!-- Folder Items -->
					<div v-for="folder in folders" :key="folder.id" class="group/folder">
						<button
							@click="selectedFolderId = folder.id"
							@contextmenu.prevent="showFolderContextMenu($event, folder)"
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

					<!-- Empty folders hint -->
					<div
						v-if="folders.length === 0"
						class="py-6 text-center text-xs text-muted-foreground">
						<p>暂无分类文件夹</p>
						<p class="mt-1">点击上方按钮创建</p>
					</div>
				</div>
			</aside>

			<!-- Right: Connection List -->
			<section class="flex-1 flex flex-col overflow-hidden">
				<!-- Search Bar -->
				<div class="p-3 border-b border-border flex items-center gap-3">
					<div class="relative flex-1 max-w-sm">
						<input
							v-model="searchQuery"
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

				<!-- Connection Cards Grid -->
				<div class="flex-1 overflow-y-auto p-4">
					<div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
						<div
							v-for="conn in filteredConnections"
							:key="conn.id"
							@click="connect(conn)"
							@contextmenu.prevent="showConnectionContextMenu($event, conn)"
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
								<span
									v-if="conn.database"
									class="text-muted-foreground truncate">
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
										@click.stop="editConnection(conn)"
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
										@click.stop="deleteConnection(conn)"
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

					<!-- Empty State -->
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
							@click="createNewConnection"
							class="px-4 py-2 bg-primary text-primary-foreground rounded-lg font-medium shadow-sm hover:opacity-90 transition-opacity text-sm">
							创建新连接
						</button>
					</div>
				</div>
			</section>
		</div>

		<!-- Footer -->
		<div
			class="flex-shrink-0 border-t border-border px-6 py-2 flex items-center justify-between text-muted-foreground text-xs bg-muted/20">
			<div class="flex items-center gap-4">
				<a href="#" class="hover:text-foreground transition-colors">文档</a>
				<a href="#" class="hover:text-foreground transition-colors">Github</a>
				<a href="#" class="hover:text-foreground transition-colors">检查更新</a>
			</div>
			<div>v0.1.0 (Alpha)</div>
		</div>

		<!-- Folder Context Menu -->
		<div
			v-if="folderContextMenu.visible"
			class="fixed bg-card border border-border rounded-lg shadow-xl py-1 z-50 text-sm min-w-[160px]"
			:style="{
				left: folderContextMenu.x + 'px',
				top: folderContextMenu.y + 'px',
			}">
			<button
				@click="renameFolder(folderContextMenu.folder)"
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
				@click="deleteFolder(folderContextMenu.folder)"
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

		<!-- Connection Context Menu -->
		<div
			v-if="connectionContextMenu.visible"
			class="fixed bg-card border border-border rounded-lg shadow-xl py-1 z-50 text-sm min-w-[180px]"
			:style="{
				left: connectionContextMenu.x + 'px',
				top: connectionContextMenu.y + 'px',
			}">
			<button
				@click="connect(connectionContextMenu.conn)"
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
				@click="editConnection(connectionContextMenu.conn)"
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
				v-if="connectionContextMenu.conn.active"
				@click="disconnectConnection(connectionContextMenu.conn)"
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
				@click="moveToFolder(connectionContextMenu.conn, null)"
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
				@click="moveToFolder(connectionContextMenu.conn, folder.id)"
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
				@click="deleteConnection(connectionContextMenu.conn)"
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

		<!-- New Folder Dialog -->
		<div
			v-if="newFolderDialog.visible"
			class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
			<div
				class="bg-card border border-border rounded-xl shadow-2xl p-6 w-80 space-y-4">
				<h3 class="text-lg font-bold">新建分类文件夹</h3>
				<input
					v-model="newFolderDialog.name"
					type="text"
					placeholder="文件夹名称"
					class="w-full px-3 py-2 bg-muted rounded-lg border-none text-sm outline-none focus:ring-1 focus:ring-primary"
					@keyup.enter="confirmCreateFolder"
					ref="folderNameInput" />
				<div class="flex items-center justify-end gap-2">
					<button
						@click="newFolderDialog.visible = false"
						class="px-3 py-1.5 bg-muted rounded-lg text-sm font-medium hover:bg-accent transition-colors">
						取消
					</button>
					<button
						@click="confirmCreateFolder"
						class="px-3 py-1.5 bg-primary text-primary-foreground rounded-lg text-sm font-medium shadow-sm hover:opacity-90 transition-opacity">
						创建
					</button>
				</div>
			</div>
		</div>

		<!-- Rename Folder Dialog -->
		<div
			v-if="renameFolderDialog.visible"
			class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
			<div
				class="bg-card border border-border rounded-xl shadow-2xl p-6 w-80 space-y-4">
				<h3 class="text-lg font-bold">重命名文件夹</h3>
				<input
					v-model="renameFolderDialog.name"
					type="text"
					placeholder="文件夹名称"
					class="w-full px-3 py-2 bg-muted rounded-lg border-none text-sm outline-none focus:ring-1 focus:ring-primary"
					@keyup.enter="confirmRenameFolder" />
				<div class="flex items-center justify-end gap-2">
					<button
						@click="renameFolderDialog.visible = false"
						class="px-3 py-1.5 bg-muted rounded-lg text-sm font-medium hover:bg-accent transition-colors">
						取消
					</button>
					<button
						@click="confirmRenameFolder"
						class="px-3 py-1.5 bg-primary text-primary-foreground rounded-lg text-sm font-medium shadow-sm hover:opacity-90 transition-opacity">
						确认
					</button>
				</div>
			</div>
		</div>

		<!-- API Call Notification Toast -->
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
				<div class="font-medium text-sm">
					{{ testApiResponse }}
				</div>
				<button
					@click="testApiResponse = null"
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
	</div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
	transition:
		opacity 0.3s ease,
		transform 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
	opacity: 0;
	transform: translateY(10px);
}
</style>

<script setup lang="ts">
import { ref, computed, reactive, nextTick } from "vue";
import { useRouter } from "vue-router";
import { invoke } from "@tauri-apps/api/core";

const router = useRouter();
const searchQuery = ref("");
const selectedFolderId = ref<string | null>(null);
const testApiResponse = ref<string | null>(null);
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

const activeConnections = computed(() =>
	connections.value.filter((c) => c.active),
);

const allConnectionsCount = computed(() => connections.value.length);

function getFolderConnectionCount(folderId: string): number {
	return connections.value.filter((c) => c.folderId === folderId).length;
}

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

async function createNewConnection() {
	isLoadingTestApi.value = true;
	testApiResponse.value = null;
	try {
		// 先尝试启动后端 (如果没启动的话)
		await invoke("start_backend");
		// 调用测试接口
		const response = await invoke<string>("call_test_api");
		const data = JSON.parse(response);
		testApiResponse.value = data.message || response;
	} catch (error) {
		console.error("Failed to connect to backend:", error);
		testApiResponse.value = `连接失败: ${error}`;
	} finally {
		isLoadingTestApi.value = false;
		// 3秒后自动清除消息
		setTimeout(() => {
			testApiResponse.value = null;
		}, 3000);
	}
}

function connect(conn: Connection) {
	console.log("Connecting to", conn.name);
	router.push("/main");
}

function editConnection(conn: Connection) {
	console.log("Editing", conn.name);
}

function deleteConnection(conn: Connection) {
	connections.value = connections.value.filter((c) => c.id !== conn.id);
	hideAllMenus();
}

function disconnectConnection(conn: Connection) {
	const idx = connections.value.findIndex((c) => c.id === conn.id);
	if (idx !== -1) {
		connections.value[idx] = { ...connections.value[idx], active: false };
	}
	hideAllMenus();
}

// Folder management
const folderContextMenu = reactive({
	visible: false,
	x: 0,
	y: 0,
	folder: null as Folder | null,
});
const connectionContextMenu = reactive({
	visible: false,
	x: 0,
	y: 0,
	conn: null as Connection | null,
});
const newFolderDialog = reactive({ visible: false, name: "" });
const renameFolderDialog = reactive({ visible: false, name: "", folderId: "" });

function showFolderContextMenu(e: MouseEvent, folder: Folder) {
	hideAllMenus();
	folderContextMenu.visible = true;
	folderContextMenu.x = e.clientX;
	folderContextMenu.y = e.clientY;
	folderContextMenu.folder = folder;
}

function showConnectionContextMenu(e: MouseEvent, conn: Connection) {
	hideAllMenus();
	connectionContextMenu.visible = true;
	connectionContextMenu.x = e.clientX;
	connectionContextMenu.y = e.clientY;
	connectionContextMenu.conn = conn;
}

function hideAllMenus() {
	folderContextMenu.visible = false;
	connectionContextMenu.visible = false;
}

function createFolder() {
	newFolderDialog.visible = true;
	newFolderDialog.name = "";
}

function confirmCreateFolder() {
	if (!newFolderDialog.name.trim()) return;
	const id = "f" + Date.now();
	folders.value.push({ id, name: newFolderDialog.name.trim() });
	newFolderDialog.visible = false;
}

function renameFolder(folder: Folder | null) {
	if (!folder) return;
	renameFolderDialog.visible = true;
	renameFolderDialog.name = folder.name;
	renameFolderDialog.folderId = folder.id;
	hideAllMenus();
}

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

function moveToFolder(conn: Connection | null, folderId: string | null) {
	if (!conn) return;
	const idx = connections.value.findIndex((c) => c.id === conn.id);
	if (idx !== -1) {
		connections.value[idx] = { ...connections.value[idx], folderId };
	}
	hideAllMenus();
}

document.addEventListener("click", () => {
	hideAllMenus();
});
</script>
