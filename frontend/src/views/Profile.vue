<template>
  <div class="page-card p-6">
    <PageHeader :eyebrow="t('profile.eyebrow')" :title="t('profile.pageTitle')" :subtitle="t('profile.pageSubtitle')" />

    <el-alert
      v-if="profileForm.mustChangePassword"
      :title="t('profile.mustChangePasswordTitle')"
      :description="t('profile.mustChangePasswordDescription')"
      type="warning"
      show-icon
      :closable="false"
      class="mb-5"
    />

    <div class="grid gap-6 lg:grid-cols-[1fr_360px]">
      <section class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 class="text-lg font-bold text-slate-900">{{ t('profile.personalInfo') }}</h2>
        <el-form :model="profileForm" label-position="top" class="mt-5">
          <el-form-item :label="t('profile.account')">
            <el-input v-model="profileForm.username" readonly />
          </el-form-item>
          <el-form-item :label="t('profile.fullName')" required>
            <el-input v-model="profileForm.fullName" />
          </el-form-item>
          <el-form-item :label="t('profile.line')">
            <el-select v-model="profileForm.lineCode" clearable>
              <el-option v-for="line in lines" :key="line.lineCode" :label="line.lineCode" :value="line.lineCode" />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('profile.role')">
            <el-input v-model="profileForm.role" readonly />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile" :loading="savingProfile">{{ t('profile.save') }}</el-button>
          </el-form-item>
        </el-form>
      </section>

      <section class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 class="text-lg font-bold text-slate-900">{{ t('profile.changePassword') }}</h2>
        <el-form :model="passwordForm" label-position="top" class="mt-5">
          <el-form-item :label="t('profile.currentPassword')" required>
            <el-input type="password" v-model="passwordForm.currentPassword" autocomplete="current-password" />
          </el-form-item>
          <el-form-item :label="t('profile.newPassword')" required>
            <el-input type="password" v-model="passwordForm.newPassword" autocomplete="new-password" />
          </el-form-item>
          <el-form-item :label="t('profile.confirmPassword')" required>
            <el-input type="password" v-model="passwordForm.confirmPassword" autocomplete="new-password" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="changePassword" :loading="savingPassword">{{ t('profile.changePasswordAction') }}</el-button>
          </el-form-item>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import { authApi, masterApi } from '@/services/api'
import { setSession } from '@/services/auth'
import { ElMessage } from 'element-plus'
import { useI18n } from '@/i18n'

const { t } = useI18n()
const router = useRouter()

const profileForm = ref({ username: '', fullName: '', role: '', lineCode: '', mustChangePassword: false })
const passwordForm = ref({ currentPassword: '', newPassword: '', confirmPassword: '' })
const lines = ref([])
const savingProfile = ref(false)
const savingPassword = ref(false)

async function loadProfile() {
  try {
    const profile = await authApi.me()
    profileForm.value = { ...profile }
  } catch (error) {
    ElMessage.error(error.message || t('profile.messages.loadProfileFailed'))
  }
}

async function loadLines() {
  try {
    const data = await masterApi.getLines()
    lines.value = data.map(item => ({
      lineCode: item.code ?? item.lineCode,
      description: item.name ?? item.description,
    }))
  } catch (error) {
    lines.value = []
  }
}

async function saveProfile() {
  if (!profileForm.value.fullName) {
    ElMessage.warning(t('profile.messages.missing'))
    return
  }
  savingProfile.value = true
  try {
    const updated = await authApi.updateProfile({ fullName: profileForm.value.fullName, lineCode: profileForm.value.lineCode })
    profileForm.value = { ...updated }
    setSession({
      token: localStorage.getItem('swico_token'),
      username: updated.username,
      fullName: updated.fullName,
      role: updated.role,
      mustChangePassword: updated.mustChangePassword,
    })
    ElMessage.success(t('profile.messages.saveSuccess'))
  } catch (error) {
    ElMessage.error(error.message || t('profile.messages.saveFailed'))
  } finally {
    savingProfile.value = false
  }
}

async function changePassword() {
  if (!passwordForm.value.currentPassword || !passwordForm.value.newPassword || !passwordForm.value.confirmPassword) {
    ElMessage.warning(t('profile.messages.passwordMissing'))
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning(t('profile.messages.passwordMismatch'))
    return
  }

  savingPassword.value = true
  try {
    await authApi.changePassword({ currentPassword: passwordForm.value.currentPassword, newPassword: passwordForm.value.newPassword })
    passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
    profileForm.value.mustChangePassword = false
    setSession({
      token: localStorage.getItem('swico_token'),
      username: profileForm.value.username,
      fullName: profileForm.value.fullName,
      role: profileForm.value.role,
      mustChangePassword: false,
    })
    ElMessage.success(t('profile.messages.passwordSuccess'))
    router.push('/')
  } catch (error) {
    ElMessage.error(error.message || t('profile.messages.passwordFailed'))
  } finally {
    savingPassword.value = false
  }
}

onMounted(() => {
  loadProfile()
  loadLines()
})
</script>

<style scoped>
.page-card { background: transparent; }
</style>
