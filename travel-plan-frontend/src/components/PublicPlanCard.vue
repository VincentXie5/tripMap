<template>
  <div class="plan-card" :style="{ borderTopColor: themeColor }" @click="$emit('click')">
    <div class="card-header">
      <h3 class="card-title">{{ plan.title }}</h3>
    </div>
    <div class="card-route" v-if="plan.routePreview">
      <span class="route-text">{{ plan.routePreview }}</span>
    </div>
    <div class="card-tags">
      <el-tag
        v-for="tag in displayTags"
        :key="tag.value"
        :type="tag.type"
        size="small"
        effect="plain"
      >
        {{ tag.label }}
      </el-tag>
    </div>
    <div class="card-stats">
      <span>{{ plan.dayCount }}天</span>
      <span class="stat-divider">·</span>
      <span>{{ plan.locationCount }}个地点</span>
    </div>
    <div class="card-footer">
      <div class="creator-info" @click.stop="$emit('creator-click')">
        <el-avatar :size="28" :src="plan.creatorAvatarUrl">
          {{ plan.creatorNickname?.charAt(0) }}
        </el-avatar>
        <span class="creator-name">{{ plan.creatorNickname }}</span>
      </div>
      <span class="card-date">
        {{ plan.startDate }} ~ {{ plan.endDate }}
        <span v-if="plan.favoritedAt" class="favorited-at"> · 收藏于 {{ timeAgo(plan.favoritedAt) }}</span>
      </span>
    </div>
    <div class="card-actions">
      <span class="action-btn" :class="{ active: plan.isLiked }" @click.stop="$emit('like')">
        <span class="action-icon">{{ plan.isLiked ? '❤️' : '🤍' }}</span>
        <span class="action-count">{{ plan.likeCount }}</span>
      </span>
      <span class="action-btn" :class="{ active: plan.isFavorited }" @click.stop="$emit('favorite')">
        <span class="action-icon">{{ plan.isFavorited ? '⭐' : '☆' }}</span>
        <span class="action-count">{{ plan.favoriteCount }}</span>
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { PublicPlanCard as PublicPlanCardType } from '../types/api'

const props = defineProps<{
  plan: PublicPlanCardType
}>()

defineEmits<{
  click: []
  'creator-click': []
  like: []
  favorite: []
}>()

const TAG_CONFIG: Record<number, { label: string; type: string }> = {
  1: { label: '景点', type: '' },
  2: { label: '美食', type: 'warning' },
  3: { label: '住宿', type: 'success' },
  4: { label: '交通', type: 'info' },
  5: { label: '购物', type: 'danger' },
}

const TAG_COLORS: Record<number, string> = {
  1: '#409EFF',
  2: '#E6A23C',
  3: '#67C23A',
  4: '#909399',
  5: '#F56C6C',
  0: '#409EFF',
}

const timeAgo = (dateStr: string) => {
  const diff = Date.now() - new Date(dateStr).getTime()
  const days = Math.floor(diff / 86400000)
  if (days < 1) return '今天'
  if (days === 1) return '1 天前'
  if (days < 30) return `${days} 天前`
  const months = Math.floor(days / 30)
  if (months < 12) return `${months} 个月前`
  return `${Math.floor(months / 12)} 年前`
}

const themeColor = computed(() => TAG_COLORS[props.plan.dominantTag] || TAG_COLORS[0])

const displayTags = computed(() => {
  const tags = []
  if (props.plan.dominantTag && props.plan.dominantTag > 0) {
    const config = TAG_CONFIG[props.plan.dominantTag]
    if (config) tags.push({ label: config.label, type: config.type, value: props.plan.dominantTag })
  }
  if (props.plan.dayCount > 0) {
    tags.push({ label: props.plan.dayCount + '天', type: 'info', value: 'days' })
  }
  return tags
})
</script>

<style scoped>
.plan-card {
  background: #fff;
  border-radius: 10px;
  border-top: 4px solid #409eff;
  padding: 16px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.plan-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
}

.card-header {
  margin-bottom: 8px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-route {
  margin-bottom: 8px;
  min-height: 20px;
}

.route-text {
  font-size: 13px;
  color: #909399;
}

.card-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.card-stats {
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
}

.stat-divider {
  margin: 0 6px;
  color: #c0c4cc;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.creator-info:hover .creator-name {
  color: #409eff;
}

.creator-name {
  font-size: 13px;
  color: #606266;
  transition: color 0.2s;
}

.card-date {
  font-size: 12px;
  color: #c0c4cc;
}

.card-actions {
  display: flex;
  gap: 16px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #909399;
  transition: color 0.2s;
  user-select: none;
}

.action-btn:hover {
  color: #409eff;
}

.action-btn.active {
  color: #409eff;
}

.action-icon {
  font-size: 15px;
}

.action-count {
  font-size: 13px;
}
</style>
