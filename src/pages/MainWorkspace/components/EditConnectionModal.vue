<template>
	<div
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
					@click="$emit('close')"
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
						@click="$emit('close')"
						class="px-4 py-2 text-sm font-medium hover:bg-muted rounded-lg transition-colors">
						取消
					</button>
					<button
						@click="$emit('close')"
						class="px-4 py-2 text-sm font-medium bg-primary text-primary-foreground rounded-lg shadow-sm hover:opacity-90 transition-opacity">
						保存修改
					</button>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
defineEmits<{
	(e: "close"): void;
}>();
</script>
