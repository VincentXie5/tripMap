/**
 * 统一API响应类型定义
 * 与后端ApiResult<T>对应
 */

/**
 * 统一响应结构
 */
export interface ApiResponse<T = any> {
  code: number      // 状态码：200成功，400参数错误，401未认证，404未找到，500服务器错误
  message: string   // 消息：描述本次响应的文字信息
  data: T           // 数据：泛型，承载实际业务数据
  timestamp?: number // 时间戳（可选）
}

/**
 * 旅行计划实体（与后端 TravelPlan 对应）
 */
export interface Plan {
  id: number
  title: string
  startDate: string
  endDate: string
  isPublic?: boolean
}

/**
 * 公开计划卡片（与后端 PublicPlanCardDTO 对应）
 */
export interface PublicPlanCard {
  id: number
  title: string
  startDate: string
  endDate: string
  creatorNickname: string
  creatorAvatarUrl: string
  creatorUserId: number
  routePreview: string | null
  dominantTag: number
  dayCount: number
  locationCount: number
}

/**
 * 公开计划详情（与后端 PublicPlanDetailDTO 对应）
 */
export interface PublicPlanDetail {
  id: number
  title: string
  startDate: string
  endDate: string
  creatorNickname: string
  creatorAvatarUrl: string
  creatorUserId: number
  dailyPlans: PlanDailyPlan[]
}

export interface PlanDailyPlan {
  id: number
  planDate: string
  time: string
  location: string
  remark: string | null
  tag: number
  sortOrder: number
  latitude: number | null
  longitude: number | null
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
  empty: boolean
}

/**
 * 每日行程实体（与后端 DailyPlan 对应）
 */
export interface DailyPlan {
  id: number
  travelPlan: {
    id: number
    title?: string
    startDate?: string
    endDate?: string
  }
  time: string
  location: string
  planDate: string
  remark?: string
  tag?: number
  sortOrder?: number
  latitude?: number
  longitude?: number
}

/**
 * HTTP状态码枚举
 */
export const HttpCode = {
  OK: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500
} as const

export type HttpCode = (typeof HttpCode)[keyof typeof HttpCode]

/**
 * 业务状态码枚举（与后端ApiResult.code对应）
 */
export const BizCode = {
  SUCCESS: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500
} as const
export type BizCode = (typeof BizCode)[keyof typeof BizCode]
