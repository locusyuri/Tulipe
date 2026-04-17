<template>
	<!-- 设置页右侧内容区：按 activeSection 条件渲染 -->
	<section class="flex-1 overflow-y-auto p-8 max-w-4xl mx-auto space-y-12">
		<!-- 通用设置 -->
		<div v-if="activeSection === 'general'" class="space-y-6">
			<h2 class="text-2xl font-bold tracking-tight">通用设置</h2>
			<div class="space-y-4">
				<div
					class="flex items-center justify-between p-4 bg-muted/30 rounded-xl border border-border">
					<div>
						<div class="font-medium">语言</div>
						<div class="text-sm text-muted-foreground">
							选择应用程序的显示语言
						</div>
					</div>
					<select
						class="bg-background border border-border rounded-md px-3 py-1 text-sm outline-none focus:ring-1 focus:ring-primary">
						<option value="zh">简体中文 (Recommended)</option>
						<option value="en">English</option>
					</select>
				</div>

				<div
					class="flex items-center justify-between p-4 bg-muted/30 rounded-xl border border-border">
					<div>
						<div class="font-medium">外观主题</div>
						<div class="text-sm text-muted-foreground">
							切换深色模式或跟随系统设置
						</div>
					</div>
					<div class="flex p-1 bg-muted rounded-lg border border-border">
						<button
							class="px-3 py-1.5 text-xs font-medium rounded-md bg-background shadow-sm border border-border">
							System
						</button>
						<button
							class="px-3 py-1.5 text-xs font-medium rounded-md text-muted-foreground hover:text-foreground">
							Light
						</button>
						<button
							class="px-3 py-1.5 text-xs font-medium rounded-md text-muted-foreground hover:text-foreground">
							Dark
						</button>
					</div>
				</div>
			</div>
		</div>

		<!-- AI 助手配置 -->
		<div v-if="activeSection === 'ai'" class="space-y-6">
			<h2 class="text-2xl font-bold tracking-tight">AI 助手配置</h2>
			<div class="space-y-4">
				<div
					class="p-4 bg-primary/5 rounded-xl border border-primary/10 space-y-4">
					<div class="flex items-center gap-3 text-primary font-medium">
						<svg
							class="w-5 h-5"
							viewBox="0 0 24 24"
							fill="none"
							stroke="currentColor"
							stroke-width="2">
							<path
								d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"></path>
						</svg>
						模型服务提供商
					</div>
					<div class="space-y-3">
						<div class="space-y-1.5">
							<label class="text-xs font-bold text-muted-foreground uppercase">
								API Host
							</label>
							<input
								type="text"
								placeholder="https://api.openai.com/v1"
								class="w-full bg-background border border-border rounded-md px-3 py-2 text-sm outline-none focus:ring-1 focus:ring-primary" />
						</div>
						<div class="space-y-1.5">
							<label class="text-xs font-bold text-muted-foreground uppercase">
								API Key
							</label>
							<input
								type="password"
								placeholder="sk-..."
								class="w-full bg-background border border-border rounded-md px-3 py-2 text-sm outline-none focus:ring-1 focus:ring-primary" />
						</div>
					</div>
				</div>
			</div>
		</div>
	</section>
</template>

<script setup lang="ts">
// 右侧内容区支持的分区标识
type SectionId = "general" | "ai" | "shortcuts";

// 从父组件接收当前分区
defineProps<{
	activeSection: SectionId;
}>();
</script>
