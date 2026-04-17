<template>
	<!-- 设置页左侧导航：点击切换 activeSection -->
	<aside class="w-64 border-r border-border p-4 space-y-1">
		<button
			v-for="item in navItems"
			:key="item.id"
			@click="emit('update:activeSection', item.id)"
			class="w-full flex items-center gap-3 px-3 py-2 text-sm rounded-lg transition-colors"
			:class="
				activeSection === item.id
					? 'bg-primary text-primary-foreground font-medium shadow-sm'
					: 'text-muted-foreground hover:bg-muted hover:text-foreground'
			">
			<component :is="item.icon" class="w-4 h-4" />
			{{ item.label }}
		</button>
	</aside>
</template>

<script setup lang="ts">
import { defineComponent, h } from "vue";

// 可切换的设置分区标识
type SectionId = "general" | "ai" | "shortcuts";

// 将分区切换结果同步给父组件
const emit = defineEmits<{
	(e: "update:activeSection", value: SectionId): void;
}>();

// 当前激活分区（用于高亮菜单项）
defineProps<{
	activeSection: SectionId;
}>();

// 导航图标组件
const IconGeneral = defineComponent({
	render: () =>
		h(
			"svg",
			{
				viewBox: "0 0 24 24",
				fill: "none",
				stroke: "currentColor",
				strokeWidth: "2",
			},
			[
				h("path", {
					d: "M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z",
				}),
				h("circle", { cx: "12", cy: "12", r: "3" }),
			],
		),
});

const IconAI = defineComponent({
	render: () =>
		h(
			"svg",
			{
				viewBox: "0 0 24 24",
				fill: "none",
				stroke: "currentColor",
				strokeWidth: "2",
			},
			[
				h("path", {
					d: "m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z",
				}),
				h("path", { d: "M5 3v4" }),
				h("path", { d: "M3 5h4" }),
				h("path", { d: "M21 17v4" }),
				h("path", { d: "M19 19h4" }),
			],
		),
});

const IconShortcuts = defineComponent({
	render: () =>
		h(
			"svg",
			{
				viewBox: "0 0 24 24",
				fill: "none",
				stroke: "currentColor",
				strokeWidth: "2",
			},
			[
				h("rect", { x: "2", y: "4", width: "20", height: "16", rx: "2" }),
				h("path", { d: "M6 8h.01" }),
				h("path", { d: "M10 8h.01" }),
				h("path", { d: "M14 8h.01" }),
				h("path", { d: "M18 8h.01" }),
				h("path", { d: "M8 12h.01" }),
				h("path", { d: "M12 12h.01" }),
				h("path", { d: "M16 12h.01" }),
				h("path", { d: "M7 16h10" }),
			],
		),
});

// 左侧导航项配置
const navItems: Array<{
	id: SectionId;
	label: string;
	icon: typeof IconGeneral;
}> = [
	{ id: "general", label: "通用设置", icon: IconGeneral },
	{ id: "ai", label: "AI 助手", icon: IconAI },
	{ id: "shortcuts", label: "快捷键", icon: IconShortcuts },
];
</script>
