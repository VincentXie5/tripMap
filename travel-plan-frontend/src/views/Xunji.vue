<template>
  <div class="xunji-container">
    <div class="xunji-header">
      <h2 class="xunji-title">寻迹</h2>
      <p class="xunji-subtitle">发现旅行者们的足迹，寻找你的下一个目的地</p>
    </div>

    <div class="xunji-filters">
      <el-input
        v-model="keyword"
        placeholder="搜索路线、地点..."
        clearable
        :prefix-icon="Search"
        class="search-input"
        @input="onSearchInput"
        @clear="onSearchClear"
      />
      <div class="tag-filters">
        <el-button
          v-for="tag in tagOptions"
          :key="tag.value"
          :type="selectedTag === tag.value ? 'primary' : 'default'"
          size="small"
          round
          @click="toggleTag(tag.value)"
        >
          {{ tag.label }}
        </el-button>
      </div>
      <div v-if="creatorFilter" class="creator-filter-bar">
        <span>查看 <strong>{{ creatorFilter.nickname }}</strong> 的公开计划</span>
        <el-button size="small" text @click="clearCreatorFilter">清除</el-button>
      </div>
    </div>

    <div v-if="cards.length === 0 && !loading" class="empty-state">
      <div class="empty-icon">🗺️</div>
      <p class="empty-text">还没有人分享旅行计划</p>
      <p class="empty-sub">来做第一个吧！在你的计划列表中开启"公开"</p>
    </div>

    <div v-else class="card-grid">
      <PublicPlanCard
        v-for="card in cards"
        :key="card.id"
        :plan="card"
        @click="goToDetail(card.id)"
        @creator-click="filterByCreator(card)"
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
import { getPublicPlans, getPublicPlansByUser } from '../api/travelApi'
import type { PublicPlanCard as PublicPlanCardType } from '../types/api'
import PublicPlanCard from '../components/PublicPlanCard.vue'

const router = useRouter()

const cards = ref<PublicPlanCardType[]>([])
const keyword = ref('')
const selectedTag = ref<number | null>(null)
const creatorFilter = ref<{ userId: number; nickname: string } | null>(null)
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(0)
const totalPages = ref(0)
const hasMore = ref(false)

const tagOptions = [
  { label: '🎯 景点', value: 1 },
  { label: '🍽️ 美食', value: 2 },
  { label: '🏨 住宿', value: 3 },
  { label: '🚗 交通', value: 4 },
  { label: '🛍️ 购物', value: 5 },
]

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
    if (selectedTag.value) {
      params.tag = selectedTag.value
    }
    const res: any = creatorFilter.value
      ? await getPublicPlansByUser(creatorFilter.value.userId, { page: page.value, size: 12 })
      : await getPublicPlans(params)
    const data = res
    if (append) {
      cards.value.push(...data.content)
    } else {
      cards.value = data.content
    }
    totalPages.value = data.totalPages
    hasMore.value = !data.last
  } catch (error) {
    console.error('Failed to fetch public plans:', error)
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

const toggleTag = (tag: number) => {
  if (selectedTag.value === tag) {
    selectedTag.value = null
  } else {
    selectedTag.value = tag
  }
  fetchPlans(false)
}

const loadMore = () => {
  page.value++
  fetchPlans(true)
}

const goToDetail = (planId: number) => {
  router.push(`/plan/${planId}`)
}

const filterByCreator = (card: PublicPlanCardType) => {
  creatorFilter.value = {
    userId: card.creatorUserId,
    nickname: card.creatorNickname,
  }
  fetchPlans(false)
}

const clearCreatorFilter = () => {
  creatorFilter.value = null
  fetchPlans(false)
}

onMounted(() => {
  fetchPlans(false)
})
</script>

<style scoped>
.xunji-container {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 24px 32px;
  background: #f0f2f5;
}

.xunji-header {
  margin-bottom: 20px;
  text-align: center;
}

.xunji-title {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px;
}

.xunji-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.xunji-filters {
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.search-input {
  max-width: 400px;
}

.tag-filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.creator-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #ecf5ff;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  color: #409eff;
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
