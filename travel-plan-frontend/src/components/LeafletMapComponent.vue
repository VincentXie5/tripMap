<template>
  <div class="map-wrapper">
    <div ref="mapContainer" class="map-container"></div>
    <div v-if="loading" class="map-loading">
      <span>地图加载中...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, defineProps, defineEmits } from 'vue'
import L from 'leaflet'

interface DailyPlan {
  id: number
  time: string
  location: string
  planDate: string
}

const props = defineProps<{
  dailyPlans: DailyPlan[]
  highlightedId?: number | null
}>()

const emit = defineEmits(['marker-click', 'map-click'])

const mapContainer = ref<HTMLElement | null>(null)
const loading = ref(true)
let map: L.Map | null = null
let markers: L.Marker[] = []

// 初始化地图
const initMap = () => {
  if (!mapContainer.value) return

  map = L.map(mapContainer.value).setView([39.90923, 116.397428], 5) // 默认显示中国

  // 添加 OpenStreetMap 图层
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19
  }).addTo(map)

  // 添加地图点击事件
  map.on('click', () => {
    emit('map-click')
  })

  loading.value = false
}

// 清除所有标记
const clearMarkers = () => {
  if (map) {
    markers.forEach(marker => marker.remove())
    markers = []
  }
}

// 添加标记点
const addMarker = (lat: number, lng: number, location: string, time: string, planId: number) => {
  if (!map) return

  const marker = L.marker([lat, lng])
    .addTo(map)
    .bindPopup(`
      <div class="marker-popup">
        <strong>${time ? time + ' ' : ''}${location}</strong>
      </div>
    `)

  marker.on('click', () => {
    emit('marker-click', planId)
  })

  // 存储planId到marker
  ;(marker as any).planId = planId
  markers.push(marker)
}

// 地理编码：将地点名称转换为经纬度
const geocodeLocation = async (location: string): Promise<{ lat: number; lng: number } | null> => {
  try {
    // 使用 Nominatim API 进行地理编码（免费）
    const response = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(location)}&limit=1`
    )
    const data = await response.json()
    
    if (data && data.length > 0) {
      const { lat, lon } = data[0]
      return { lat: parseFloat(lat), lng: parseFloat(lon) }
    }
    return null
  } catch (error) {
    console.warn(`地理编码失败: ${location}`, error)
    return null
  }
}

// 更新地图标记
const updateMarkers = async () => {
  clearMarkers()

  if (!props.dailyPlans || props.dailyPlans.length === 0) {
    // 如果没有行程，显示中国全图
    if (map) {
      map.setView([39.90923, 116.397428], 5)
    }
    return
  }

  const validPositions: [number, number][] = []

  // 并行处理所有地点的地理编码
  const promises = props.dailyPlans.map(async (plan) => {
    if (!plan.location) return

    const coords = await geocodeLocation(plan.location)
    if (coords) {
      addMarker(coords.lat, coords.lng, plan.location, plan.time, plan.id)
      validPositions.push([coords.lat, coords.lng])
    }
  })

  await Promise.all(promises)

  // 自适应显示所有标记点
  if (validPositions.length > 0 && map) {
    if (validPositions.length === 1) {
      // 只有一个点时，设置合适的缩放级别
      map.setView(validPositions[0], 12)
    } else {
      // 多个点时，使用 fitBounds 自适应显示
      const bounds = L.latLngBounds(validPositions)
      map.fitBounds(bounds, { padding: [50, 50] })
    }
  }
}

// 高亮标记点
const highlightMarker = (planId: number | null) => {
  markers.forEach(marker => {
    const markerPlanId = (marker as any).planId
    if (markerPlanId === planId) {
      // 高亮样式：改变图标颜色为红色
      marker.setIcon(L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
      }))
      // 居中地图到该点位
      if (map) {
        map.setView(marker.getLatLng(), 12)
      }
    } else {
      // 恢复默认图标为蓝色
      marker.setIcon(L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
      }))
    }
  })
}

// 监听 dailyPlans 变化
watch(() => props.dailyPlans, () => {
  if (map) {
    updateMarkers()
  }
}, { deep: true })

// 监听 highlightedId 变化
watch(() => props.highlightedId, (newId) => {
  if (map) {
    highlightMarker(newId)
  }
})

onMounted(() => {
  initMap()
  updateMarkers()
})

onUnmounted(() => {
  // 销毁地图
  if (map) {
    map.remove()
    map = null
  }
})
</script>

<style scoped>
.map-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.map-container {
  width: 100%;
  height: 100%;
}

.map-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.8);
  font-size: 16px;
  color: #666;
}

:deep(.marker-popup) {
  font-size: 14px;
  line-height: 1.5;
}

:deep(.leaflet-popup-content-wrapper) {
  border-radius: 8px;
}

:deep(.leaflet-popup-content) {
  margin: 10px 12px;
}
</style>