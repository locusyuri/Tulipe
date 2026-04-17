<template>
	<div class="h-full flex flex-col bg-background">
		<!-- 页面头部：返回按钮与标题 -->
		<Header @back="router.back()" />

		<!-- 页面主体：左侧导航 + 右侧内容 -->
		<main class="flex-1 flex overflow-hidden">
			<Sidebar v-model:activeSection="activeSection" />
			<Content :activeSection="activeSection" />
		</main>
	</div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";

import Header from "./components/Header.vue";
import Sidebar from "./components/Sidebar.vue";
import Content from "./components/Content.vue";

// 路由实例，用于返回上一页
const router = useRouter();
// 当前激活的设置分区，驱动右侧内容切换
const activeSection = ref<"general" | "ai" | "shortcuts">("general");
</script>
