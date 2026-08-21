<template>
  <div class="min-h-screen bg-slate-100 flex items-center justify-center px-4">
    <div class="w-full max-w-md rounded-3xl bg-white p-8 shadow-xl">
      <div class="flex justify-end mb-2">
        <el-select 
          v-model="locale" 
          size="small" 
          class="!w-28"
          @change="value => setLocale(value)"
        >
          <el-option :label="t('common.languages.vi')" value="vi" />
          <el-option :label="t('common.languages.zhHant')" value="zh-Hant" />
        </el-select>
      </div>
      <div class="mb-6 text-center">
        <h1 class="text-2xl font-extrabold text-slate-900">{{ t('login.title') }}</h1>
        <p class="mt-1 text-sm text-slate-500">{{ t('login.subtitle') }}</p>
      </div>
      <el-form 
        :model="form" 
        label-position="top" 
        class="space-y-4"
        @submit.prevent="doLogin"
      >
        <el-form-item :label="t('login.username')">
          <el-input 
            v-model="form.username" 
            autocomplete="username" 
            size="large"
          />
        </el-form-item>

        <el-form-item :label="t('login.password')">
          <el-input 
            v-model="form.password" 
            type="password" 
            show-password
            autocomplete="current-password" 
            size="large"
          />
        </el-form-item>

        <div class="pt-2">
          <el-button 
            type="primary" 
            native-type="submit" 
            size="large" 
            class="w-full"
            :loading="loading"
          >
            {{ t('common.login') }}
          </el-button>
        </div>
      </el-form>
      <div class="mt-6 text-center text-xs text-slate-400">
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSession, login } from '@/services/auth'
import { useI18n } from '@/i18n'

const router = useRouter()
const form = ref({ username: '', password: '' })
const loading = ref(false)
const { t, locale, setLocale } = useI18n()

async function doLogin() {
  if (!form.value.username || !form.value.password) return

  loading.value = true
  try {
    const auth = await login(form.value.username.trim(), form.value.password.trim())
    const session = getSession()
    ElMessage.success(t('login.success'))
    router.push(session.mustChangePassword || auth.mustChangePassword ? '/profile' : '/')
  } catch (error) {
    ElMessage.error(t('login.failed'))
  } finally {
    loading.value = false
  }
}
</script>
