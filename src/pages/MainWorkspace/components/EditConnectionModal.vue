<!--
  EditConnectionModal.vue - 编辑连接弹窗组件

  功能说明：
  - 以模态框形式展示，允许用户编辑数据库连接的各项参数
  - 支持编辑连接名称、数据库类型、主机地址、端口、用户名和密码
  - 提供取消和保存功能，以及删除连接的选项

  使用方式：
  - 通过 v-if 条件渲染控制显示/隐藏
  - 点击关闭按钮或点击遮罩层外部会触发 close 事件
  - 父组件监听 close 事件来控制 isEditModalOpen 状态

  布局结构：
  - 遮罩层：半透明黑色背景，点击可关闭
  - 弹窗主体：居中显示的卡片
    - 头部：标题和关闭按钮
    - 表单区域：连接配置输入项
    - 底部操作栏：删除、取消、保存按钮
-->
<template>
	<!--
    遮罩层
    - fixed + inset-0: 固定定位，覆盖整个视口
    - bg-black/40: 半透明黑色背景
    - flex + items-center + justify-center: 内容居中
    - z-[100]: 确保在最上层
    - backdrop-blur-sm: 背景模糊效果
    - animate-in fade-in: 淡入动画
  -->
	<div
		class="fixed inset-0 bg-black/40 flex items-center justify-center z-[100] backdrop-blur-sm animate-in fade-in duration-200">
		<!--
      弹窗主体卡片
      - w-[500px]: 固定宽度 500px
      - bg-card: 卡片背景色
      - border + rounded-2xl: 圆角边框
      - shadow-2xl: 大阴影效果
      - overflow-hidden: 隐藏溢出内容
      - animate-in zoom-in-95: 缩放淡入动画
    -->
		<div
			class="bg-card border border-border rounded-2xl shadow-2xl w-[500px] overflow-hidden animate-in zoom-in-95 duration-200">

			<!-- ==================== 弹窗头部 ==================== -->
			<div
				class="px-6 py-4 border-b border-border flex items-center justify-between bg-muted/30">
				<!-- 标题区域 -->
				<div class="flex items-center gap-3">
					<!-- 数据库类型图标（M 表示 MySQL） -->
					<div
						class="p-2 bg-primary/10 rounded-lg text-primary text-sm font-bold">
						M
					</div>
					<!-- 弹窗标题 -->
					<h3 class="text-lg font-bold">编辑连接: Production MySQL</h3>
				</div>

				<!-- 关闭按钮 -->
				<button
					@click="$emit('close')"
					class="p-1 hover:bg-muted rounded-full transition-colors text-muted-foreground">
					<!-- X 图标 -->
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

			<!-- ==================== 表单区域 ==================== -->
			<div class="p-6 space-y-4">
				<!-- 第一行：连接名称 + 数据库类型 -->
				<div class="grid grid-cols-2 gap-4">
					<!-- 连接名称输入框 -->
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

					<!-- 数据库类型下拉框 -->
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

				<!-- 第二行：主机地址 + 端口 -->
				<div class="grid grid-cols-3 gap-4">
					<!-- 主机地址输入框（占2列） -->
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

					<!-- 端口输入框 -->
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

				<!-- 第三行：用户名 + 密码 -->
				<div class="grid grid-cols-2 gap-4">
					<!-- 用户名输入框 -->
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

					<!-- 密码输入框（类型为 password） -->
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

			<!-- ==================== 底部操作栏 ==================== -->
			<div
				class="px-6 py-4 bg-muted/30 border-t border-border flex items-center justify-between">
				<!-- 左侧：删除按钮（危险操作） -->
				<button
					class="px-4 py-2 text-sm font-medium text-destructive hover:bg-destructive/10 rounded-lg transition-colors">
					删除连接
				</button>

				<!-- 右侧：取消和保存按钮 -->
				<div class="flex items-center gap-3">
					<!-- 取消按钮 -->
					<button
						@click="$emit('close')"
						class="px-4 py-2 text-sm font-medium hover:bg-muted rounded-lg transition-colors">
						取消
					</button>

					<!-- 保存按钮（主要操作） -->
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
/**
 * EditConnectionModal 组件：编辑连接弹窗
 *
 * 功能：
 * - 展示连接编辑表单
 * - 触发 close 事件供父组件关闭弹窗
 */

// ==================== 事件定义 ====================
// 定义组件可触发的事件
defineEmits<{
	// close: 点击关闭/取消/保存按钮时触发，用于通知父组件关闭弹窗
	(e: "close"): void;
}>();
</script>
