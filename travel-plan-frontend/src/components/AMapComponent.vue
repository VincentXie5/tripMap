<template>
  <div class="map-wrapper">
    <div ref="mapContainer" class="map-container"></div>
    <div v-if="loading" class="map-loading">
      <span>地图加载中...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, defineProps } from 'vue'

// 声明高德地图类型
declare global {
  interface Window {
    AMap: any
  }
}

interface DailyPlan {
  id: number
  time: string
  location: string
  planDate: string
}

const props = defineProps<{
  dailyPlans: DailyPlan[]
}>()

const mapContainer = ref<HTMLElement | null>(null)
const loading = ref(true)
let map: any = null
let markers: any[] = []
let geocoder: any = null

// 初始化地图
const initMap = () => {
  if (!mapContainer.value || !window.AMap) return

  map = new window.AMap.Map(mapContainer.value, {
    zoom: 5,
    center: [116.397428, 39.90923], // 默认显示中国
    viewMode: '2D'
  })

  // 初始化地理编码服务
  geocoder = new window.AMap.Geocoder({
    city: '全国',
    radius: 1000
  })

  map.on('complete', () => {
    loading.value = false
  })
}

// 清除所有标记
const clearMarkers = () => {
  if (map && markers.length > 0) {
    map.remove(markers)
    markers = []
  }
}

// 添加标记点
const addMarker = (lng: number, lat: number, location: string, time: string) => {
  if (!map) return

  const marker = new window.AMap.Marker({
    position: [lng, lat],
    title: location,
    label: {
      content: `<div class="marker-label">${time ? time + ' ' : ''}${location}</div>`,
      offset: new window.AMap.Pixel(0, -30)
    }
  })

  markers.push(marker)
  map.add(marker)
}

// 地理编码：将地点名称转换为经纬度
const geocodeLocation = (location: string): Promise<{ lng: number; lat: number } | null> => {
  return new Promise((resolve) => {
    if (!geocoder) {
      resolve(null)
      return
    }

    geocoder.getLocation(location, (status: string, result: any) => {
      if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
        const { lng, lat } = result.geocodes[0].location
        resolve({ lng, lat })
      } else {
        console.warn(`地理编码失败: ${location}`)
        resolve(null)
      }
    })
  })
}

// 更新地图标记
const updateMarkers = async () => {
  clearMarkers()

  if (!props.dailyPlans || props.dailyPlans.length === 0) {
    // 如果没有行程，显示中国全图
    if (map) {
      map.setZoomAndCenter(5, [116.397428, 39.90923])
    }
    return
  }

  const validPositions: [number, number][] = []

  // 并行处理所有地点的地理编码
  const promises = props.dailyPlans.map(async (plan) => {
    if (!plan.location) return

    const coords = await geocodeLocation(plan.location)
    if (coords) {
      addMarker(coords.lng, coords.lat, plan.location, plan.time)
      validPositions.push([coords.lng, coords.lat])
    }
  })

  await Promise.all(promises)

  // 自适应显示所有标记点
  if (validPositions.length > 0 && map) {
    if (validPositions.length === 1) {
      // 只有一个点时，设置合适的缩放级别
      map.setZoomAndCenter(12, validPositions[0])
    } else {
      // 多个点时，使用 setFitView 自适应显示
      map.setFitView(markers, false, [100, 100, 100, 100])
    }
  }
}

// 监听 dailyPlans 变化
watch(() => props.dailyPlans, () => {
  if (map) {
    updateMarkers()
  }
}, { deep: true })

let checkAMapInterval: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  // 确保高德地图API已加载
  if (window.AMap) {
    initMap()
    updateMarkers()
  } else {
    // 等待API加载，设置超时处理
    let waitTime = 0
    const maxWaitTime = 10000 // 最大等待10秒
    
    checkAMapInterval = setInterval(() => {
      waitTime += 100
      
      if (window.AMap) {
        if (checkAMapInterval) {
          clearInterval(checkAMapInterval)
          checkAMapInterval = null
        }
        initMap()
        updateMarkers()
      } else if (waitTime >= maxWaitTime) {
        // 超时处理
        if (checkAMapInterval) {
          clearInterval(checkAMapInterval)
          checkAMapInterval = null
        }
        loading.value = false
        console.error('高德地图 API 加载超时，请检查网络连接或 API 配置')
      }
    }, 100)
  }
})

onUnmounted(() => {
  // 清理定时器
  if (checkAMapInterval) {
    clearInterval(checkAMapInterval)
    checkAMapInterval = null
  }
  
  // 销毁地图
  if (map) {
    map.destroy()
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

:deep(.marker-label) {
  background-color: #409eff;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}
</style>