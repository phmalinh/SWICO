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
          v-for="log in filteredLogs"
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
            <p class="mt-1 text-xs text-slate-400"><User class="mr-1 inline h-3 w-3" />{{ log.username }}</p>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { User } from 'lucide-vue-next'
import PageHeader from '@/components/PageHeader.vue'
import { systemApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()

const logs = ref([])
const filterAction = ref('')
const loading = ref(false)
let refreshTimer = null

const filteredLogs = computed(() =>
  filterAction.value ? logs.value.filter(l => l.action === filterAction.value) : logs.value
)

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

onMounted(() => {
  loadLogs()
  refreshTimer = window.setInterval(loadLogs, 20000)
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>
