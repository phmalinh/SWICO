<template>
  <div>
    <PageHeader :eyebrow="t('audit.eyebrow')" :title="t('audit.pageTitle')" :subtitle="t('audit.pageSubtitle')">
      <template #actions>
        <el-select v-model="filterAction" clearable :placeholder="t('audit.filterPlaceholder')" style="width: 170px">
          <el-option label="CREATE" value="CREATE" />
          <el-option label="UPDATE" value="UPDATE" />
          <el-option label="DELETE" value="DELETE" />
        </el-select>
      </template>
    </PageHeader>

    <div class="page-card overflow-hidden">
      <el-timeline class="p-6">
        <el-timeline-item
          v-for="log in paginatedLogs"
          :key="log.id"
          :timestamp="log.timestamp"
          placement="top"
          :type="actionType(log.action)"
          :hollow="log.action === 'UPDATE'"
        >
          <div class="rounded-lg border border-slate-100 bg-slate-50 p-4">
            <div class="mb-1 flex items-center gap-2">
              <el-tag :type="actionTagType(log.action)" size="small" effect="dark">{{ log.action }}</el-tag>
              <span class="text-xs text-slate-400">{{ log.entity }} #{{ log.entityId }}</span>
            </div>
            <p class="text-sm font-bold text-slate-700">{{ log.detail }}</p>
            <p class="mt-1 text-xs text-slate-400"><User class="mr-1 inline h-3 w-3" />{{ displayUserName(log.username) }}</p>
          </div>
        </el-timeline-item>
      </el-timeline>
      <div class="flex justify-end px-4 py-3 border-t border-slate-200 bg-slate-50">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="filteredLogs.length"
          layout="prev, pager, next"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { User } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import { systemApi, userApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()

const logs = ref([])
const users = ref([])
const filterAction = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
let refreshTimer = null

const usernameToFullName = computed(() => {
  return users.value.reduce((map, user) => {
    if (user.username) {
      map[user.username] = user.fullName || user.username
    }
    return map
  }, {})
})

function displayUserName(username) {
  const fullName = usernameToFullName.value[username]
  return fullName && fullName !== username ? `${fullName} (${username})` : username
}

const filteredLogs = computed(() =>
  filterAction.value ? logs.value.filter(l => l.action === filterAction.value) : logs.value
)

const paginatedLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredLogs.value.slice(start, start + pageSize.value)
})

watch(filterAction, () => {
  currentPage.value = 1
})

function actionType(action) {
  return { CREATE: 'success', UPDATE: 'warning', DELETE: 'danger' }[action] || 'primary'
}

function actionTagType(action) {
  return { CREATE: 'success', UPDATE: 'warning', DELETE: 'danger' }[action] || 'info'
}

async function loadLogs() {
  loading.value = true
  try {
    logs.value = await systemApi.auditLogs()
  } catch (error) {
    console.error('Could not load audit logs', error)
  } finally {
    loading.value = false
  }
}

async function loadUsers() {
  try {
    users.value = await userApi.list()
  } catch (error) {
    console.error('Could not load users', error)
  }
}

onMounted(() => {
  loadLogs()
  loadUsers()
  refreshTimer = window.setInterval(loadLogs, 20000)
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>
