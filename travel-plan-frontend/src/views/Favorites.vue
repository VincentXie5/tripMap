<template>
  <div class="favorites-container">
    <div class="favorites-header">
      <h2 class="favorites-title">星迹</h2>
      <p class="favorites-subtitle">珍藏的旅行足迹，随时回顾心动计划</p>
    </div>

    <div class="favorites-filters">
      <el-input
        v-model="keyword"
        placeholder="搜索已收藏的计划..."
        clearable
        :prefix-icon="Search"
        class="search-input"
        @input="onSearchInput"
        @clear="onSearchClear"
      />
    </div>

    <div v-if="cards.length === 0 && !loading" class="empty-state">
      <div class="empty-icon">⭐</div>
      <p class="empty-text">还没有收藏任何旅行计划</p>
      <p class="empty-sub">
        去<router-link to="/xunji" class="empty-link">寻迹</router-link>发现感兴趣的旅行计划吧
      </p>
    </div>

    <div v-else class="card-grid">
      <PublicPlanCard
        v-for="card in cards"
        :key="card.id"
        :plan="card"
        @click="goToDetail(card.id)"
        @creator-click="goToDetail(card.id)"
        @like="handleLike(card)"
        @favorite="handleFavorite(card)"
      />
    </div>

    <div v-if="hasMore && !loading" class="load-more">
      <el-button :loading="loadingMore" @click="loadMore">加载更多</el-button>
    </div>

    <div v-if="loading" class="loading-area">
      <el-icon class="is-loading"><Loading /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Loading } from '@element-plus/icons-vue'
import { getFavoritePlans, toggleLike, toggleFavorite } from '../api/travelApi'
import type { PublicPlanCard as PublicPlanCardType } from '../types/api'
import PublicPlanCard from '../components/PublicPlanCard.vue'

const router = useRouter()

const cards = ref<PublicPlanCardType[]>([])
const keyword = ref('')
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(0)
const hasMore = ref(false)

let searchTimer: ReturnType<typeof setTimeout> | null = null

const fetchPlans = async (append = false) => {
  if (!append) {
    loading.value = true
    page.value = 0
    cards.value = []
  } else {
    loadingMore.value = true
  }
  try {
    const params: Record<string, any> = {
      page: append ? page.value : 0,
      size: 12,
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    const res: any = await getFavoritePlans(params)
    const data = res
    if (append) {
      cards.value.push(...data.content)
    } else {
      cards.value = data.content
    }
    hasMore.value = !data.last
  } catch (error) {
    console.error('Failed to fetch favorite plans:', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const onSearchInput = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    fetchPlans(false)
  }, 400)
}

const onSearchClear = () => {
  fetchPlans(false)
}

const loadMore = () => {
  page.value++
  fetchPlans(true)
}

const goToDetail = (planId: number) => {
  router.push(`/plan/${planId}`)
}

const handleLike = async (card: PublicPlanCardType) => {
  try {
    const res: any = await toggleLike(card.id)
    card.isLiked = res.liked
    card.likeCount = res.likeCount
  } catch (_) { /* error handled by interceptor */ }
}

const handleFavorite = async (card: PublicPlanCardType) => {
  try {
    await toggleFavorite(card.id)
    cards.value = cards.value.filter(c => c.id !== card.id)
  } catch (_) { /* error handled by interceptor */ }
}

onMounted(() => {
  fetchPlans(false)
})
</script>

<style scoped>
.favorites-container {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 24px 32px;
  background: #f0f2f5;
}

.favorites-header {
  margin-bottom: 20px;
  text-align: center;
}

.favorites-title {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px;
}

.favorites-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.favorites-filters {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.search-input {
  max-width: 400px;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 18px;
  color: #303133;
  margin: 0 0 8px;
}

.empty-sub {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.empty-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.empty-link:hover {
  text-decoration: underline;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.load-more {
  text-align: center;
  margin-top: 24px;
}

.loading-area {
  text-align: center;
  padding: 40px;
  font-size: 28px;
  color: #409eff;
}
</style>
