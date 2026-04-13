<template>
  <div class="map-wrapper">
    <div ref="mapContainer" class="map-container"></div>
    <div v-if="loading" class="map-loading">
      <span>地图加载中...</span>
    </div>
    <button 
      class="fullscreen-btn" 
      @click="toggleFullscreen"
      :title="isFullscreen ? '退出全屏' : '全屏显示'"
    >
      <svg v-if="!isFullscreen" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="15 3 21 3 21 9"></polyline>
        <polyline points="9 21 3 21 3 15"></polyline>
        <line x1="21" y1="3" x2="14" y2="10"></line>
        <line x1="3" y1="21" x2="10" y2="14"></line>
      </svg>
      <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="4 14 10 14 10 20"></polyline>
        <polyline points="20 10 14 10 14 4"></polyline>
        <line x1="14" y1="10" x2="21" y2="3"></line>
        <line x1="10" y1="14" x2="3" y2="21"></line>
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, defineProps, defineEmits } from 'vue'
import L from 'leaflet'

// 标签颜色与标记图标映射表
const tagMarkerIcons: Record<number, string> = {
  0: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-grey.png',
  1: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png',
  2: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-orange.png',
  3: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-green.png',
  4: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-grey.png',
  5: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png'
}

interface DailyPlan {
  id: number
  time: string
  location: string
  planDate: string
  sortOrder?: number
  latitude?: number | null
  longitude?: number | null
  remark?: string
  tag?: number
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

const emit = defineEmits(['marker-click', 'map-click', 'route-click', 'edit-plan', 'delete-plan', 'locate-plan'])

const mapContainer = ref<HTMLElement | null>(null)
const loading = ref(true)
const isFullscreen = ref(false)
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
const addMarker = (plan: DailyPlan) => {
  if (!map || !plan.latitude || !plan.longitude) return

  const marker = L.marker([plan.latitude, plan.longitude], {
    icon: L.icon({
      iconUrl: tagMarkerIcons[plan.tag || 0],
      shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    })
  })
    .addTo(map)
    .bindPopup(L.popup({
      maxWidth: 300,
      minWidth: 240,
      className: 'custom-marker-popup',
      autoPan: true,
      autoPanPadding: [50, 50],
      closeOnClick: false
    }))

  // 标签名称映射
  const tagNames: Record<number, string> = {
    0: '无标签',
    1: '🏛️ 景点',
    2: '🍜 美食',
    3: '🏨 住宿',
    4: '🚗 交通',
    5: '🛒 购物'
  }

  // 构建弹窗内容
  const buildPopupContent = () => {
    const timeStr = plan.time ? plan.time.substring(0, 5) : ''
    let html = '<div class="popup-content">'
    
    if (timeStr) {
      html += `<div class="popup-time">${timeStr}</div>`
    }
    
    html += `<div class="popup-location">${plan.location}</div>`
    
    if (plan.tag && plan.tag > 0) {
      html += `<div class="popup-tag">${tagNames[plan.tag]}</div>`
    }
    
    if (plan.remark) {
      html += `<div class="popup-remark">${plan.remark}</div>`
    }
    
    html += `
      <div class="popup-actions">
        <button class="popup-btn popup-btn-edit" data-action="edit" data-id="${plan.id}">编辑</button>
        <button class="popup-btn popup-btn-delete" data-action="delete" data-id="${plan.id}">删除</button>
        <button class="popup-btn popup-btn-locate" data-action="locate" data-id="${plan.id}">行程</button>
      </div>
    </div>`
    
    return html
  }

  marker.setPopupContent(buildPopupContent())

  marker.on('click', () => {
    emit('marker-click', plan.id)
  })

  // 弹窗打开后绑定按钮事件
  marker.on('popupopen', () => {
    setTimeout(() => {
      document.querySelectorAll('.custom-marker-popup .popup-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
          const target = e.target as HTMLElement
          const action = target.dataset.action
          const id = Number(target.dataset.id)
          
          marker.closePopup()
          
          if (action === 'edit') {
            emit('edit-plan', id)
          } else if (action === 'delete') {
            emit('delete-plan', id)
          } else if (action === 'locate') {
            emit('locate-plan', id)
          }
          
          e.stopPropagation()
        })
      })
    }, 10)
  })

  // 存储planId到marker
  ;(marker as any).planId = plan.id
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
      addMarker(plan)
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
      if (route.coords.length > 0 && map) {
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

// 全屏切换
const toggleFullscreen = async () => {
  if (!mapContainer.value) return
  
  if (!document.fullscreenElement) {
    await mapContainer.value.requestFullscreen()
  } else {
    await document.exitFullscreen()
  }
}

// 全屏状态变化监听
const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
  
  // 地图重绘，避免尺寸变化后出现偏移
  if (map) {
    setTimeout(() => {
      map?.invalidateSize({ animate: false })
    }, 100)
  }
}

// ESC键关闭弹窗
const handleEscKey = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && map) {
    map.closePopup()
  }
}

onMounted(() => {
  initMap()
  updateMarkers()
  drawRoutes()
  document.addEventListener('keydown', handleEscKey)
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscKey)
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
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
  z-index: 1000;
}

.fullscreen-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 1001;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  transition: all 0.2s ease;
}

.fullscreen-btn:hover {
  background-color: #f5f7fa;
  color: #409eff;
  transform: scale(1.05);
}

:global(body:fullscreen) .map-wrapper {
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 9999;
}

:global(body:fullscreen) .map-container {
  width: 100vw;
  height: 100vh;
}

:deep(.marker-popup) {
  font-size: 14px;
  line-height: 1.5;
}

:deep(.leaflet-popup-content-wrapper) {
  border-radius: 8px;
}

:deep(.custom-marker-popup .leaflet-popup-content-wrapper) {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 0;
}

:deep(.custom-marker-popup .leaflet-popup-content) {
  margin: 0;
  min-width: 240px;
}

:deep(.custom-marker-popup .popup-content) {
  padding: 16px;
}

:deep(.custom-marker-popup .popup-time) {
  font-size: 13px;
  color: #409eff;
  font-weight: 600;
  margin-bottom: 4px;
}

:deep(.custom-marker-popup .popup-location) {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
}

:deep(.custom-marker-popup .popup-tag) {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  background-color: #e8f0fe;
  color: #1976d2;
  display: inline-block;
  margin-bottom: 8px;
}

:deep(.custom-marker-popup .popup-remark) {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  margin-bottom: 12px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}

:deep(.custom-marker-popup .popup-actions) {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

:deep(.custom-marker-popup .popup-btn) {
  flex: 1;
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

:deep(.custom-marker-popup .popup-btn-edit) {
  background-color: #409eff;
  color: white;
}

:deep(.custom-marker-popup .popup-btn-edit:hover) {
  background-color: #66b1ff;
}

:deep(.custom-marker-popup .popup-btn-delete) {
  background-color: #f56c6c;
  color: white;
}

:deep(.custom-marker-popup .popup-btn-delete:hover) {
  background-color: #f78989;
}

:deep(.custom-marker-popup .popup-btn-locate) {
  background-color: #67c23a;
  color: white;
}

:deep(.custom-marker-popup .popup-btn-locate:hover) {
  background-color: #85ce61;
}

:deep(.leaflet-popup-tip-container) {
  transition: transform 0.15s ease-out;
}
</style>