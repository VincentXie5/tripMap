<template>
  <div class="map-wrapper">
    <div ref="mapContainer" class="map-container"></div>
    <div v-if="loading" class="map-loading">
      <span>地图加载中...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, defineProps, defineEmits, computed } from 'vue'
import L from 'leaflet'

interface DailyPlan {
  id: number
  time: string
  location: string
  planDate: string
  sortOrder?: number
  latitude?: number | null
  longitude?: number | null
}

interface RouteLine {
  date: string
  polyline: L.Polyline
  coords: [number, number][]
  colorIndex: number
}

const props = defineProps<{
  dailyPlans: DailyPlan[]
  highlightedId?: number | null
  highlightedDate?: string | null
}>()

const emit = defineEmits(['marker-click', 'map-click', 'route-click'])

const mapContainer = ref<HTMLElement | null>(null)
const loading = ref(true)
let map: L.Map | null = null
let markers: L.Marker[] = []
let routeLines: RouteLine[] = []

// 路线样式配置
const routeStyles = {
  colors: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399'],
  weight: 3,
  opacity: 0.7,
  dashArray: '10, 5',
  highlightWeight: 5,
  highlightOpacity: 1
}

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


// 更新地图标记
const updateMarkers = () => {
  clearMarkers()

  if (!props.dailyPlans || props.dailyPlans.length === 0) {
    // 如果没有行程，显示中国全图
    if (map) {
      map.setView([39.90923, 116.397428], 5)
    }
    return
  }

  const validPositions: [number, number][] = []

  // 直接使用后端返回的经纬度
  props.dailyPlans.forEach((plan) => {
    if (plan.location && plan.latitude && plan.longitude) {
      addMarker(plan.latitude, plan.longitude, plan.location, plan.time, plan.id)
      validPositions.push([plan.latitude, plan.longitude])
    }
  })

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

// 清除所有路线
const clearRoutes = () => {
  routeLines.forEach(route => route.polyline.remove())
  routeLines = []
}

// 绘制路线
const drawRoutes = () => {
  clearRoutes()
  
  if (!props.dailyPlans || props.dailyPlans.length < 2) return
  
  // 按日期分组并排序
  const groupedByDate = props.dailyPlans.reduce((acc, plan) => {
    if (!acc[plan.planDate]) {
      acc[plan.planDate] = []
    }
    acc[plan.planDate].push(plan)
    return acc
  }, {} as Record<string, DailyPlan[]>)
  
  let colorIndex = 0
  
  // 为每个日期绘制路线
  for (const [date, plans] of Object.entries(groupedByDate)) {
    // 按sortOrder排序
    const sortedPlans = [...plans].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    
    // 获取所有点位坐标
    const coords: [number, number][] = []
    for (const plan of sortedPlans) {
      if (plan.location && plan.latitude && plan.longitude) {
        coords.push([plan.latitude, plan.longitude])
      }
    }
    
    // 至少需要2个点才能绘制路线
    if (coords.length >= 2) {
      const polyline = L.polyline(coords, {
        color: routeStyles.colors[colorIndex % routeStyles.colors.length],
        weight: routeStyles.weight,
        opacity: routeStyles.opacity,
        dashArray: routeStyles.dashArray
      }).addTo(map!)
      
      // 绑定点击事件
      polyline.on('click', () => {
        emit('route-click', date)
      })
      
      // 绑定hover效果
      polyline.on('mouseover', () => {
        polyline.setStyle({
          weight: routeStyles.highlightWeight,
          opacity: routeStyles.highlightOpacity
        })
      })
      
      polyline.on('mouseout', () => {
        // 如果不是当前高亮日期，恢复样式
        if (props.highlightedDate !== date) {
          polyline.setStyle({
            weight: routeStyles.weight,
            opacity: routeStyles.opacity
          })
        }
      })
      
      routeLines.push({
        date,
        polyline,
        coords,
        colorIndex: colorIndex % routeStyles.colors.length
      })
      
      colorIndex++
    }
  }
}

// 高亮指定日期的路线
const highlightRoute = (date: string | null) => {
  routeLines.forEach(route => {
    if (route.date === date) {
      route.polyline.setStyle({
        weight: routeStyles.highlightWeight,
        opacity: routeStyles.highlightOpacity
      })
      
      // 同时高亮该日期的所有点位
      const datePlanIds = props.dailyPlans
        .filter(p => p.planDate === date)
        .map(p => p.id)
      
      markers.forEach(marker => {
        const markerPlanId = (marker as any).planId
        if (datePlanIds.includes(markerPlanId)) {
          marker.setIcon(L.icon({
            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
          }))
        }
      })
      
      // 地图居中到路线中心点
      if (coords.length > 0 && map) {
        const bounds = L.latLngBounds(route.coords)
        map.fitBounds(bounds, { padding: [50, 50] })
      }
    } else {
      route.polyline.setStyle({
        color: routeStyles.colors[route.colorIndex],
        weight: routeStyles.weight,
        opacity: routeStyles.opacity
      })
    }
  })
}

// 监听 dailyPlans 变化
watch(() => props.dailyPlans, () => {
  if (map) {
    updateMarkers()
    drawRoutes()
  }
}, { deep: true })

// 监听 highlightedId 变化
watch(() => props.highlightedId, (newId) => {
  if (map) {
    highlightMarker(newId ?? null)
  }
})

// 监听 highlightedDate 变化
watch(() => props.highlightedDate, (newDate) => {
  if (map) {
    highlightRoute(newDate ?? null)
  }
})

onMounted(() => {
  initMap()
  updateMarkers()
  drawRoutes()
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