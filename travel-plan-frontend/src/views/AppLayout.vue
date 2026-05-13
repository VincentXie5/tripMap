<template>
  <div class="app-layout">
    <header class="app-header">
      <h1 class="logo" @click="router.push('/')">TripMap</h1>
      <nav class="header-nav">
        <span
          class="nav-tab"
          :class="{ active: route.path === '/' }"
          @click="router.push('/')"
        >我的计划</span>
        <span
          class="nav-tab"
          :class="{ active: route.path.startsWith('/xunji') || route.path.startsWith('/plan/') }"
          @click="router.push('/xunji')"
        >寻迹</span>
      </nav>
      <div class="header-right">
        <el-avatar :size="36" :src="avatarUrl">
          {{ authStore.userInfo?.nickname?.charAt(0) }}
        </el-avatar>
        <span class="username" @click="router.push('/profile')">
          {{ authStore.userInfo?.nickname || authStore.userInfo?.email }}
        </span>
        <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
      </div>
    </header>
    <main class="app-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElAvatar, ElButton } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getProfile } from '@/api/profile'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const avatarUrl = ref<string>()

onMounted(async () => {
  try {
    const res: any = await getProfile()
    avatarUrl.value = res.data?.avatarUrl
  } catch {
    // use fallback avatar (first char of nickname)
  }
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  flex-shrink: 0;
}

.logo {
  margin: 0;
  font-size: 20px;
  cursor: pointer;
  user-select: none;
}

.header-nav {
  display: flex;
  gap: 4px;
  margin-left: 32px;
}

.nav-tab {
  padding: 4px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.75);
  transition: all 0.2s;
}

.nav-tab:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.15);
}

.nav-tab.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.25);
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  color: white;
  cursor: pointer;
  font-size: 14px;
}

.username:hover {
  text-decoration: underline;
}

.app-content {
  flex: 1;
  overflow: hidden;
}
</style>
