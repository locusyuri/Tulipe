<template>
  <div class="h-full flex flex-col bg-background">
    <!-- Status Bar -->
    <header class="h-10 border-b border-border bg-card/50 flex items-center justify-between px-4 text-xs font-medium">
      <div class="flex items-center gap-4">
        <button @click="router.push('/')" class="p-1 hover:bg-muted rounded text-muted-foreground hover:text-foreground">
          <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline>
          </svg>
        </button>
        <div class="flex items-center gap-2">
          <div class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
          <span class="text-foreground">Connected to: <span class="font-bold">Production MySQL</span></span>
          <span class="text-muted-foreground">(192.168.1.10:3306)</span>
        </div>
      </div>
      <div class="flex items-center gap-4">
        <button class="flex items-center gap-1.5 px-2 py-1 hover:bg-muted rounded text-muted-foreground hover:text-foreground transition-colors">
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
          </svg>
          AI Assistant
        </button>
        <button @click="router.push('/settings')" class="p-1 hover:bg-muted rounded text-muted-foreground hover:text-foreground">
          <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
          </svg>
        </button>
      </div>
    </header>

    <!-- Main Workspace Area (Three-Column Layout) -->
    <main class="flex-1 flex overflow-hidden">
      <!-- Left Column: Database Icon Bar -->
      <aside class="w-[72px] border-r border-border flex flex-col bg-card/20">
        <div class="p-2 border-b border-border flex items-center justify-center">
          <span class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">DB</span>
        </div>
        <div class="flex-1 overflow-y-auto p-2 space-y-1.5">
          <div 
            v-for="db in databases" 
            :key="db" 
            :title="db"
            class="flex flex-col items-center justify-center py-2 px-1 rounded-lg hover:bg-accent group cursor-pointer transition-colors"
          >
            <svg class="w-5 h-5 text-muted-foreground group-hover:text-primary transition-colors" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <ellipse cx="12" cy="5" rx="9" ry="3"></ellipse><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
            </svg>
            <span class="text-[10px] text-muted-foreground group-hover:text-foreground transition-colors truncate w-full text-center mt-1">{{ db.length > 5 ? db.substring(0, 5) + '..' : db }}</span>
          </div>
        </div>
      </aside>

      <!-- Middle Column: Object Navigator -->
      <section class="w-80 border-r border-border flex flex-col">
        <div class="h-10 border-b border-border flex items-center px-1">
          <button v-for="tab in ['Tables', 'Views', 'Functions']" :key="tab" class="flex-1 text-[11px] font-bold py-2 rounded-md transition-colors" :class="tab === 'Tables' ? 'bg-primary text-primary-foreground shadow-sm' : 'text-muted-foreground hover:bg-muted hover:text-foreground'">
            {{ tab }}
          </button>
        </div>
        <div class="flex-1 overflow-y-auto p-2 space-y-1">
          <div v-for="table in tables" :key="table" class="flex items-center gap-2 px-3 py-2 text-sm rounded-md hover:bg-accent group cursor-pointer">
            <svg class="w-4 h-4 text-muted-foreground group-hover:text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="3" y1="9" x2="21" y2="9"></line><line x1="9" y1="21" x2="9" y2="9"></line>
            </svg>
            <span class="truncate">{{ table }}</span>
          </div>
        </div>
      </section>

      <!-- Right Column: Workspace / Content Area -->
      <section class="flex-1 flex flex-col bg-muted/10 overflow-hidden">
        <!-- Tabs for open tables/queries -->
        <div class="h-10 border-b border-border flex items-center px-2 bg-card/30">
          <div class="flex h-full items-center gap-1 overflow-x-auto scrollbar-hide">
            <div v-for="tab in openTabs" :key="tab.id" class="flex items-center gap-2 px-3 h-8 text-xs rounded-t-md border border-b-0 border-border group" :class="tab.active ? 'bg-background font-bold text-foreground' : 'text-muted-foreground bg-muted/50 hover:bg-background/80 cursor-pointer'">
              <span class="truncate max-w-[120px]">{{ tab.name }}</span>
              <button class="opacity-0 group-hover:opacity-100 p-0.5 hover:bg-muted-foreground/20 rounded-full transition-opacity">
                <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>
          </div>
          <button class="ml-2 p-1.5 hover:bg-muted rounded text-muted-foreground transition-colors">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
          </button>
        </div>

        <!-- Table Content (AG Grid placeholder) -->
        <div class="flex-1 p-4 overflow-hidden flex flex-col">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-4">
              <h2 class="text-xl font-bold">users</h2>
              <div class="flex items-center gap-2 px-2 py-1 bg-muted rounded text-xs font-mono">
                <span class="text-muted-foreground">Rows:</span> <span>1,248</span>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <button class="px-3 py-1.5 bg-primary text-primary-foreground text-xs rounded font-medium shadow-sm hover:opacity-90">Save Changes</button>
              <button class="px-3 py-1.5 bg-muted text-foreground text-xs rounded font-medium hover:bg-accent transition-colors">Refresh</button>
            </div>
          </div>

          <!-- AG Grid Placeholder Mockup -->
          <div class="flex-1 bg-card border border-border rounded-lg shadow-inner overflow-hidden flex flex-col">
            <div class="grid grid-cols-5 border-b border-border bg-muted/30 text-xs font-bold text-muted-foreground">
              <div v-for="col in ['id', 'username', 'email', 'created_at', 'status']" :key="col" class="px-4 py-2 border-r border-border last:border-0 flex items-center justify-between group">
                {{ col.toUpperCase() }}
                <svg class="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="m6 9 6 6 6-6"></path>
                </svg>
              </div>
            </div>
            <div class="flex-1 overflow-y-auto bg-background divide-y divide-border">
              <div v-for="i in 20" :key="i" class="grid grid-cols-5 text-xs hover:bg-accent/30 transition-colors">
                <div class="px-4 py-2 border-r border-border last:border-0 font-mono text-muted-foreground">{{ i }}</div>
                <div class="px-4 py-2 border-r border-border last:border-0 font-medium">user_{{ i }}</div>
                <div class="px-4 py-2 border-r border-border last:border-0 text-blue-500 underline underline-offset-2">user_{{ i }}@example.com</div>
                <div class="px-4 py-2 border-r border-border last:border-0 text-muted-foreground">2024-04-14 12:00:00</div>
                <div class="px-4 py-2 border-r border-border last:border-0 flex items-center gap-2">
                  <div class="w-1.5 h-1.5 rounded-full bg-green-500"></div> Active
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Right Side: Details/AI Panel (Collapsible) -->
      <aside class="w-80 border-l border-border bg-card/20 flex flex-col overflow-hidden">
        <div class="h-10 border-b border-border flex items-center px-4 justify-between">
          <span class="text-xs font-bold uppercase tracking-widest text-muted-foreground">Details</span>
          <button class="p-1 hover:bg-muted rounded transition-colors text-muted-foreground">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="flex-1 p-4 space-y-6 overflow-y-auto">
          <div class="space-y-2">
            <h4 class="text-[11px] font-bold text-muted-foreground uppercase tracking-wider">Information</h4>
            <div class="space-y-1">
              <div class="flex justify-between text-xs py-1 border-b border-border/50">
                <span class="text-muted-foreground">Type</span>
                <span class="font-medium">BASE TABLE</span>
              </div>
              <div class="flex justify-between text-xs py-1 border-b border-border/50">
                <span class="text-muted-foreground">Engine</span>
                <span class="font-medium">InnoDB</span>
              </div>
              <div class="flex justify-between text-xs py-1 border-b border-border/50">
                <span class="text-muted-foreground">Collation</span>
                <span class="font-medium">utf8mb4_unicode_ci</span>
              </div>
            </div>
          </div>

          <div class="space-y-2">
            <h4 class="text-[11px] font-bold text-muted-foreground uppercase tracking-wider">AI Insight</h4>
            <div class="p-3 bg-primary/5 rounded-lg border border-primary/10 text-xs leading-relaxed space-y-2">
              <p>这个表看起来存储了用户信息。索引优化建议：</p>
              <ul class="list-disc list-inside space-y-1 text-muted-foreground">
                <li>为 <code class="bg-muted px-1 rounded">email</code> 字段添加唯一索引</li>
                <li>考虑对 <code class="bg-muted px-1 rounded">created_at</code> 进行分区优化</li>
              </ul>
              <button class="w-full mt-2 py-1.5 bg-primary/10 text-primary hover:bg-primary/20 rounded font-medium transition-colors">问问 AI 更多...</button>
            </div>
          </div>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const databases = ref(['information_schema', 'mysql', 'performance_schema', 'sys', 'tulipe_dev', 'users_db', 'order_system'])
const tables = ref(['users', 'roles', 'permissions', 'user_roles', 'profiles', 'login_history', 'audit_logs', 'settings'])
const openTabs = ref([
  { id: '1', name: 'users', active: true },
  { id: '2', name: 'SQL Query 1', active: false },
  { id: '3', name: 'roles', active: false },
])
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>
