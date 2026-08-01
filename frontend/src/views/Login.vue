<template>
  <div class="min-h-screen bg-slate-100 flex items-center justify-center px-4">
    <div class="w-full max-w-md rounded-3xl bg-white p-8 shadow-xl">
      <div class="mb-4 flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-extrabold text-slate-900">{{ t('login.title') }}</h1>
          <p class="text-sm text-slate-500">{{ t('login.subtitle') }}</p>
        </div>
        <el-select v-model="locale" size="small" style="width: 120px" @change="value => setLocale(value)">
          <el-option label="Tiếng Việt" value="vi" />
          <el-option label="繁體中文" value="zh-Hant" />
        </el-select>
      </div>

      <el-form :model="form" label-position="top">
        <el-form-item :label="t('login.username')">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item :label="t('login.password')">
          <el-input type="password" v-model="form.password" autocomplete="current-password" />
        </el-form-item>
        <el-button type="primary" class="w-full" @click="doLogin">{{ t('common.login') }}</el-button>
      </el-form>

      <div class="mt-6 text-xs text-slate-400">{{ t('login.sample') }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, setSession } from '@/services/auth'
import { useI18n } from '@/i18n'

const router = useRouter()
const form = ref({ username: '', password: '' })
const { t, locale, setLocale } = useI18n()

async function doLogin() {
  try {
    const auth = await login(form.value.username, form.value.password)
    setSession(auth)
    ElMessage.success(t('login.success'))
    router.push('/')
  } catch (error) {
    ElMessage.error(t('login.failed'))
  }
}
</script>
