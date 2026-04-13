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
