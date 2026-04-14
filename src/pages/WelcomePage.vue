<template>
  <div class="h-full flex flex-col items-center justify-center bg-muted/30 p-8 overflow-auto">
    <div class="max-w-4xl w-full space-y-8">
      <!-- Header Section -->
      <div class="text-center space-y-4">
        <div class="flex justify-center">
          <div class="p-4 bg-primary rounded-2xl shadow-lg">
            <svg class="w-12 h-12 text-primary-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
            </svg>
          </div>
        </div>
        <h1 class="text-4xl font-bold tracking-tight text-foreground">Tulipe</h1>
        <p class="text-muted-foreground text-lg max-w-lg mx-auto">
          现代化数据库管理工具，高性能、极简、跨平台。
        </p>
      </div>

      <!-- Main Action Section -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- New Connection Card -->
        <div 
          @click="createNewConnection"
          class="group p-8 bg-card border-2 border-dashed border-muted-foreground/25 hover:border-primary/50 hover:bg-accent/50 rounded-2xl cursor-pointer transition-all duration-300 flex flex-col items-center justify-center space-y-4"
        >
          <div class="p-3 bg-muted group-hover:bg-primary/10 rounded-full transition-colors">
            <svg class="w-8 h-8 text-muted-foreground group-hover:text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
          </div>
          <div class="text-center">
            <h3 class="text-xl font-semibold">新建连接</h3>
            <p class="text-sm text-muted-foreground mt-1">创建新的数据库连接配置</p>
          </div>
        </div>

        <!-- Connection List Section -->
        <div class="bg-card border border-border rounded-2xl shadow-sm flex flex-col h-[400px]">
          <div class="p-4 border-b border-border flex items-center justify-between">
            <h3 class="font-semibold text-foreground flex items-center gap-2">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 11a9 9 0 0 1 9 9" /><path d="M4 4a16 16 0 0 1 16 16" /><circle cx="5" cy="19" r="1" />
              </svg>
              最近连接
            </h3>
            <div class="relative w-48">
              <input 
                v-model="searchQuery"
                type="text" 
                placeholder="搜索连接..." 
                class="w-full pl-8 pr-3 py-1.5 text-sm bg-muted rounded-md border-none focus:ring-1 focus:ring-primary outline-none"
              />
              <svg class="w-3.5 h-3.5 absolute left-2.5 top-1/2 -translate-y-1/2 text-muted-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </div>
          </div>

          <div class="flex-1 overflow-y-auto p-2 space-y-1">
            <div 
              v-for="conn in filteredConnections" 
              :key="conn.id"
              @click="connect(conn)"
              class="flex items-center gap-3 p-3 rounded-xl hover:bg-accent group cursor-pointer transition-colors"
            >
              <div class="w-10 h-10 rounded-lg bg-muted flex items-center justify-center text-xl font-bold text-muted-foreground group-hover:bg-primary/10 group-hover:text-primary transition-colors">
                {{ conn.type.charAt(0).toUpperCase() }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between">
                  <span class="font-medium truncate">{{ conn.name }}</span>
                  <span class="text-[10px] px-1.5 py-0.5 bg-muted rounded uppercase text-muted-foreground">{{ conn.type }}</span>
                </div>
                <div class="text-xs text-muted-foreground truncate">{{ conn.host }}:{{ conn.port }}</div>
              </div>
              <div class="opacity-0 group-hover:opacity-100 transition-opacity flex items-center gap-1">
                <button @click.stop="editConnection(conn)" class="p-1.5 hover:bg-muted rounded-md text-muted-foreground">
                  <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                  </svg>
                </button>
              </div>
            </div>

            <!-- Empty State -->
            <div v-if="filteredConnections.length === 0" class="h-full flex flex-col items-center justify-center text-muted-foreground space-y-2 py-10">
              <svg class="w-10 h-10 opacity-20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
                <rect x="2" y="2" width="20" height="20" rx="5" ry="5"></rect><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path><line x1="17.5" y1="6.5" x2="17.51" y2="6.5"></line>
              </svg>
              <p class="text-sm">没有找到相关连接</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer Info -->
      <div class="flex items-center justify-between text-muted-foreground text-sm px-4">
        <div class="flex items-center gap-4">
          <a href="#" class="hover:text-foreground transition-colors">文档</a>
          <a href="#" class="hover:text-foreground transition-colors">Github</a>
          <a href="#" class="hover:text-foreground transition-colors">检查更新</a>
        </div>
        <div>v0.1.0 (Alpha)</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchQuery = ref('')

// Mock connections - these will eventually come from the Rust backend
const connections = ref([
  { id: '1', name: 'Production MySQL', type: 'mysql', host: '192.168.1.10', port: 3306 },
  { id: '2', name: 'Local PostgreSQL', type: 'postgresql', host: 'localhost', port: 5432 },
  { id: '3', name: 'Redis Cache', type: 'redis', host: 'localhost', port: 6379 },
  { id: '4', name: 'Internal SQLite', type: 'sqlite', host: 'app.db', port: 0 },
])

const filteredConnections = computed(() => {
  if (!searchQuery.value) return connections.value
  const q = searchQuery.value.toLowerCase()
  return connections.value.filter(c => 
    c.name.toLowerCase().includes(q) || 
    c.type.toLowerCase().includes(q) || 
    c.host.toLowerCase().includes(q)
  )
})

function createNewConnection() {
  console.log('Create new connection')
  // Open dialog or navigate to form
}

function connect(conn: any) {
  console.log('Connecting to', conn.name)
  router.push('/main')
}

function editConnection(conn: any) {
  console.log('Editing', conn.name)
}
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>
