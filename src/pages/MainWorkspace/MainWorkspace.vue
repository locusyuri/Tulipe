<template>
	<div class="h-full flex flex-col bg-background">
		<!-- Header Component -->
		<Header
			:is-switch-dropdown-open="isSwitchDropdownOpen"
			@edit-connection="isEditModalOpen = true"
			@toggle-switch-dropdown="isSwitchDropdownOpen = !isSwitchDropdownOpen"
			@switch-connection="switchConnection"
		/>

		<!-- Workspace Component -->
		<Workspace
			:object-nav-width="objectNavWidth"
			:details-panel-width="detailsPanelWidth"
			:active-nav-tab="activeNavTab"
			:is-more-nav-tabs-open="isMoreNavTabsOpen"
			:all-nav-tabs="allNavTabs"
			:visible-nav-tabs="visibleNavTabs"
			:databases="databases"
			:tables="tables"
			:open-tabs="openTabs"
			:resizing-side="resizingSide"
			@select-nav-tab="selectNavTab"
			@toggle-more-tabs="isMoreNavTabsOpen = !isMoreNavTabsOpen"
			@move-tab="moveTab"
			@start-resizing="startResizing"
		/>

		<!-- Edit Connection Modal -->
		<EditConnectionModal
			v-if="isEditModalOpen"
			@close="isEditModalOpen = false"
		/>
	</div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import Header from "./components/Header.vue";
import Workspace from "./components/Workspace.vue";
import EditConnectionModal from "./components/EditConnectionModal.vue";

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

function switchConnection(conn: any) {
	console.log("Switching to", conn.name);
	isSwitchDropdownOpen.value = false;
}

// Close dropdown when clicking outside
function handleClickOutside(e: MouseEvent) {
	const target = e.target as HTMLElement;
	if (target.closest(".relative > button")) return;

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
