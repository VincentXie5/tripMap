import request from './auth'

export interface ProfileResponse {
  id: number
  email: string
  nickname: string
  avatarType: string
  isActive: boolean
  createdAt: string
  avatarUrl: string
}

export interface AvatarUpdateRequest {
  avatarType: string
}

export interface NicknameUpdateRequest {
  nickname: string
}

export interface PasswordChangeRequest {
  oldPassword: string
  newPassword: string
}

export interface EmailChangeRequest {
  email: string
  verifyCode: string
}

export const getProfile = () => {
  return request.get<{ data: ProfileResponse }>('/api/profile')
}

export const updateAvatar = (data: AvatarUpdateRequest) => {
  return request.put<{ data: ProfileResponse }>('/api/profile/avatar', data)
}

export const updateNickname = (data: NicknameUpdateRequest) => {
  return request.put<{ data: ProfileResponse }>('/api/profile/nickname', data)
}

export const changePassword = (data: PasswordChangeRequest) => {
  return request.put('/api/profile/password', data)
}

export const sendEmailCode = (email: string) => {
  return request.post('/api/profile/send-code', { email })
}

export const changeEmail = (data: EmailChangeRequest) => {
  return request.put<{ data: ProfileResponse }>('/api/profile/email', data)
}

export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<{ data: ProfileResponse }>('/api/profile/avatar/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
