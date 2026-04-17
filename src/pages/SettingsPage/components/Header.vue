<template>
	<!-- 设置页头部：返回与标题 -->
	<header class="h-14 border-b border-border flex items-center px-6 gap-4">
		<button
			@click="emit('back')"
			class="p-2 hover:bg-muted rounded-full transition-colors">
			<svg
				class="w-5 h-5"
				viewBox="0 0 24 24"
				fill="none"
				stroke="currentColor"
				stroke-width="2">
				<line x1="19" y1="12" x2="5" y2="12"></line>
				<polyline points="12 19 5 12 12 5"></polyline>
			</svg>
		</button>
		<h1 class="text-xl font-bold">设置</h1>
	</header>
</template>

<script setup lang="ts">
// 向父组件抛出返回事件
const emit = defineEmits<{
	(e: "back"): void;
}>();
</script>
