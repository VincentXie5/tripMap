<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>

      <!-- 头像部分 -->
      <div class="section">
        <h3>头像</h3>
        <div class="avatar-section">
          <el-avatar :size="80" :src="profile?.avatarUrl">
            {{ profile?.nickname?.charAt(0) }}
          </el-avatar>
          <div class="avatar-options">
            <el-radio-group v-model="selectedAvatarType" @change="handleAvatarChange">
              <el-radio value="DEFAULT">默认头像</el-radio>
              <el-radio value="GRAVATAR">Gravatar</el-radio>
              <el-radio value="CUSTOM">自定义头像</el-radio>
            </el-radio-group>
            <div v-if="selectedAvatarType === 'CUSTOM'" class="upload-section">
              <el-upload
                :auto-upload="false"
                :show-file-list="false"
                accept="image/jpeg,image/png,image/gif,image/webp"
                @change="handleFileChange"
              >
                <el-button type="primary" :loading="avatarUploading">选择图片</el-button>
                <template #tip>
                  <div class="upload-tip">支持 JPG、PNG、GIF、WebP，不超过 2MB</div>
                </template>
              </el-upload>
            </div>
          </div>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="section">
        <h3>基本信息</h3>
        <el-form :model="profileForm" label-width="100px">
          <el-form-item label="昵称">
            <el-input v-model="nickname" placeholder="请输入昵称" style="width: 200px" />
            <el-button type="primary" @click="handleNicknameSave" :loading="nicknameLoading" style="margin-left: 10px">
              保存
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 邮箱部分 -->
      <div class="section">
        <h3>邮箱</h3>
        <div class="email-section">
          <div v-if="!emailChanging">
            <span>当前邮箱: {{ profile?.email }}</span>
            <el-tag :type="profile?.isActive ? 'success' : 'warning'" style="margin-left: 10px">
              {{ profile?.isActive ? '已验证' : '未验证' }}
            </el-tag>
            <el-button type="primary" text @click="emailChanging = true" style="margin-left: 20px">
              修改邮箱
            </el-button>
          </div>
          <div v-else class="email-change-form">
            <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef" label-width="80px">
              <el-form-item label="新邮箱" prop="email">
                <el-input v-model="emailForm.email" placeholder="请输入新邮箱" style="width: 200px" />
              </el-form-item>
              <el-form-item label="验证码" prop="code" v-if="emailForm.email && !codeSent">
                <el-input v-model="emailForm.code" placeholder="请输入验证码" style="width: 120px" />
                <el-button type="primary" @click="handleSendCode" :loading="sendCodeLoading" :disabled="!!countdown" style="margin-left: 10px">
                  {{ countdown ? `${countdown}s` : '发送验证码' }}
                </el-button>
              </el-form-item>
              <el-form-item v-if="codeSent">
                <el-button type="primary" @click="handleEmailChange" :loading="emailLoading">
                  确认修改
                </el-button>
                <el-button @click="emailChanging = false">取消</el-button>
              </el-form-item>
              <el-form-item v-if="emailForm.email && !codeSent">
                <el-button type="primary" @click="handleSendCode" :loading="sendCodeLoading" :disabled="!!countdown">
                  {{ countdown ? `${countdown}s` : '发送验证码' }}
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>

      <!-- 密码修改 -->
      <div class="section">
        <h3>修改密码</h3>
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password style="width: 200px" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码（至少8位）" show-password style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handlePasswordChange" :loading="passwordLoading">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getProfile, updateAvatar, updateNickname, changePassword, sendEmailCode, changeEmail, uploadAvatar } from '@/api/profile'
import type { ProfileResponse } from '@/api/profile'

const profile = ref<ProfileResponse | null>(null)
const selectedAvatarType = ref('DEFAULT')
const nickname = ref('')
const avatarUploading = ref(false)
const nicknameLoading = ref(false)

// Email change state
const emailChanging = ref(false)
const emailFormRef = ref<FormInstance>()
const emailForm = reactive({
  email: '',
  code: ''
})
const sendCodeLoading = ref(false)
const emailLoading = ref(false)
const codeSent = ref(false)
const countdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

// Password change state
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: ''
})
const passwordLoading = ref(false)

// Dummy form for el-form :model
const profileForm = reactive({})

const emailRules: FormRules = {
  email: [
    { required: true, message: '请输入新邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码至少8位', trigger: 'blur' }
  ]
}

onMounted(async () => {
  await loadProfile()
})

const loadProfile = async () => {
  try {
    const res = await getProfile()
    profile.value = res.data
    selectedAvatarType.value = profile.value.avatarType || 'DEFAULT'
    nickname.value = profile.value.nickname || ''
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载失败')
  }
}

const handleAvatarChange = async () => {
  // CUSTOM 类型由上传组件处理，radio 切换仅处理 DEFAULT / GRAVATAR
  if (selectedAvatarType.value === 'CUSTOM') {
    return
  }
  try {
    const res = await updateAvatar({ avatarType: selectedAvatarType.value })
    profile.value = res.data
    ElMessage.success('头像更新成功')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '更新失败')
    selectedAvatarType.value = profile.value?.avatarType || 'DEFAULT'
  }
}

const handleFileChange = async (file: any) => {
  const rawFile = file.raw
  if (!rawFile) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WebP 格式')
    return
  }
  if (rawFile.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }

  avatarUploading.value = true
  try {
    const res: any = await uploadAvatar(rawFile)
    profile.value = res.data
    selectedAvatarType.value = 'CUSTOM'
    ElMessage.success('头像上传成功')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '上传失败')
    selectedAvatarType.value = profile.value?.avatarType || 'DEFAULT'
  } finally {
    avatarUploading.value = false
  }
}

const handleNicknameSave = async () => {
  if (!nickname.value.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  nicknameLoading.value = true
  try {
    const res = await updateNickname({ nickname: nickname.value })
    profile.value = res.data
    ElMessage.success('昵称更新成功')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '更新失败')
  } finally {
    nicknameLoading.value = false
  }
}

const handleSendCode = async () => {
  if (!emailFormRef.value) return
  await emailFormRef.value.validateField('email', async (error) => {
    if (!error) {
      sendCodeLoading.value = true
      try {
        await sendEmailCode(emailForm.email)
        ElMessage.success('验证码已发送')
        codeSent.value = true
        // Start countdown
        countdown.value = 60
        countdownTimer = setInterval(() => {
          countdown.value--
          if (countdown.value <= 0) {
            countdown.value = 0
            if (countdownTimer) clearInterval(countdownTimer)
          }
        }, 1000)
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '发送失败')
      } finally {
        sendCodeLoading.value = false
      }
    }
  })
}

const handleEmailChange = async () => {
  if (!emailFormRef.value) return
  await emailFormRef.value.validate(async (valid) => {
    if (valid) {
      emailLoading.value = true
      try {
        const res = await changeEmail({ email: emailForm.email, verifyCode: emailForm.code })
        profile.value = res.data
        ElMessage.success('邮箱修改成功')
        emailChanging.value = false
        emailForm.email = ''
        emailForm.code = ''
        codeSent.value = false
        if (countdownTimer) clearInterval(countdownTimer)
        countdown.value = 0
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '修改失败')
      } finally {
        emailLoading.value = false
      }
    }
  })
}

const handlePasswordChange = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功')
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '修改失败')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 700px;
  margin: 0 auto;
  height: 100%;
  overflow-y: auto;
}

.profile-card {
  width: 100%;
}

.card-header {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
}

.section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.section h3 {
  margin-bottom: 15px;
  color: #333;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.email-section {
  display: flex;
  flex-direction: column;
}

.upload-section {
  margin-top: 12px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}

.email-change-form {
  margin-top: 10px;
}
</style>
