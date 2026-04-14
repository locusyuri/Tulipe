<template>
  <div class="h-full flex flex-col bg-background">
    <!-- Header -->
    <header class="h-14 border-b border-border flex items-center px-6 gap-4">
      <button @click="router.back()" class="p-2 hover:bg-muted rounded-full transition-colors">
        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="19" y1="12" x2="5" y2="12"></line><polyline points="12 19 5 12 12 5"></polyline>
        </svg>
      </button>
      <h1 class="text-xl font-bold">设置</h1>
    </header>

    <!-- Content -->
    <main class="flex-1 flex overflow-hidden">
      <!-- Sidebar Navigation -->
      <aside class="w-64 border-r border-border p-4 space-y-1">
        <button 
          v-for="item in navItems" 
          :key="item.id"
          @click="activeSection = item.id"
          class="w-full flex items-center gap-3 px-3 py-2 text-sm rounded-lg transition-colors"
          :class="activeSection === item.id ? 'bg-primary text-primary-foreground font-medium shadow-sm' : 'text-muted-foreground hover:bg-muted hover:text-foreground'"
        >
          <component :is="item.icon" class="w-4 h-4" />
          {{ item.label }}
        </button>
      </aside>

      <!-- Section Content -->
      <section class="flex-1 overflow-y-auto p-8 max-w-4xl mx-auto space-y-12">
        <div v-if="activeSection === 'general'" class="space-y-6">
          <h2 class="text-2xl font-bold tracking-tight">通用设置</h2>
          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 bg-muted/30 rounded-xl border border-border">
              <div>
                <div class="font-medium">语言</div>
                <div class="text-sm text-muted-foreground">选择应用程序的显示语言</div>
              </div>
              <select class="bg-background border border-border rounded-md px-3 py-1 text-sm outline-none focus:ring-1 focus:ring-primary">
                <option value="zh">简体中文 (Recommended)</option>
                <option value="en">English</option>
              </select>
            </div>

            <div class="flex items-center justify-between p-4 bg-muted/30 rounded-xl border border-border">
              <div>
                <div class="font-medium">外观主题</div>
                <div class="text-sm text-muted-foreground">切换深色模式或跟随系统设置</div>
              </div>
              <div class="flex p-1 bg-muted rounded-lg border border-border">
                <button class="px-3 py-1.5 text-xs font-medium rounded-md bg-background shadow-sm border border-border">System</button>
                <button class="px-3 py-1.5 text-xs font-medium rounded-md text-muted-foreground hover:text-foreground">Light</button>
                <button class="px-3 py-1.5 text-xs font-medium rounded-md text-muted-foreground hover:text-foreground">Dark</button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="activeSection === 'ai'" class="space-y-6">
          <h2 class="text-2xl font-bold tracking-tight">AI 助手配置</h2>
          <div class="space-y-4">
            <div class="p-4 bg-primary/5 rounded-xl border border-primary/10 space-y-4">
              <div class="flex items-center gap-3 text-primary font-medium">
                <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"></path>
                </svg>
                模型服务提供商
              </div>
              <div class="space-y-3">
                <div class="space-y-1.5">
                  <label class="text-xs font-bold text-muted-foreground uppercase">API Host</label>
                  <input type="text" placeholder="https://api.openai.com/v1" class="w-full bg-background border border-border rounded-md px-3 py-2 text-sm outline-none focus:ring-1 focus:ring-primary" />
                </div>
                <div class="space-y-1.5">
                  <label class="text-xs font-bold text-muted-foreground uppercase">API Key</label>
                  <input type="password" placeholder="sk-..." class="w-full bg-background border border-border rounded-md px-3 py-2 text-sm outline-none focus:ring-1 focus:ring-primary" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, defineComponent, h } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeSection = ref('general')

const IconGeneral = defineComponent({ render: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', strokeWidth: '2' }, [h('path', { d: 'M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z' }), h('circle', { cx: '12', cy: '12', r: '3' })]) })
const IconAI = defineComponent({ render: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', strokeWidth: '2' }, [h('path', { d: 'm12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z' }), h('path', { d: 'M5 3v4' }), h('path', { d: 'M3 5h4' }), h('path', { d: 'M21 17v4' }), h('path', { d: 'M19 19h4' })]) })
const IconShortcuts = defineComponent({ render: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', strokeWidth: '2' }, [h('rect', { x: '2', y: '4', width: '20', height: '16', rx: '2' }), h('path', { d: 'M6 8h.01' }), h('path', { d: 'M10 8h.01' }), h('path', { d: 'M14 8h.01' }), h('path', { d: 'M18 8h.01' }), h('path', { d: 'M8 12h.01' }), h('path', { d: 'M12 12h.01' }), h('path', { d: 'M16 12h.01' }), h('path', { d: 'M7 16h10' })]) })

const navItems = [
  { id: 'general', label: '通用设置', icon: IconGeneral },
  { id: 'ai', label: 'AI 助手', icon: IconAI },
  { id: 'shortcuts', label: '快捷键', icon: IconShortcuts },
]
</script>
