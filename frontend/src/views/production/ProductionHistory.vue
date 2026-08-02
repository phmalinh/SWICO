<template>
  <div>
    <PageHeader
      :eyebrow="t('productionHistory.eyebrow')"
      :title="t('productionHistory.pageTitle')"
      :subtitle="t('productionHistory.pageSubtitle')"
    >
      <template #actions>
        <div class="flex items-center gap-3">
          <el-date-picker v-model="reportDate" type="date" value-format="YYYY-MM-DD" class="!w-44" @change="loadReports" />
          <el-button :loading="loading" @click="loadReports">{{ t('productionHistory.refresh') }}</el-button>
          <el-tag type="info" size="large" effect="plain">{{ t('productionHistory.reportCount', { count: todayReports.length }) }}</el-tag>
        </div>
      </template>
    </PageHeader>

    <div class="page-card overflow-hidden">
      <el-table :data="paginatedReports" stripe style="width: 100%" size="large" v-loading="loading">
        <el-table-column prop="createdAt" :label="t('productionHistory.table.time')" width="105">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="lineCode" :label="t('productionHistory.table.line')" width="86" align="center" />
        <el-table-column prop="machineCode" :label="t('productionHistory.table.machine')" width="96" align="center" />
        <el-table-column prop="partNumber" :label="t('productionHistory.table.partNumber')" width="120" />
        <el-table-column prop="partName" :label="t('productionHistory.table.partName')" min-width="150" show-overflow-tooltip />
        <el-table-column :label="t('productionHistory.table.runDowntime')" width="120" align="center">
          <template #default="{ row }">
            <span class="font-bold text-emerald-600">{{ row.totalOperatingMinutes }}</span>
            <span class="mx-1 text-slate-300">/</span>
            <span class="font-bold text-rose-500">{{ row.downtimeMinutes }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('productionHistory.table.inputGoodDefect')" width="150" align="center">
          <template #default="{ row }">{{ row.inputQuantity }} / {{ row.goodQuantity }} / {{ row.defectQuantity }}</template>
        </el-table-column>
        <el-table-column :label="t('productionHistory.table.oee')" width="95" align="center">
          <template #default="{ row }">
            <span class="rounded-full px-2.5 py-1 text-sm font-black" :class="oeeClass(row.oee)">
              {{ formatPercent(row.oee) }}
            </span>
          </template>
        </el-table-column>
        
      </el-table>

      <div class="flex justify-end px-4 py-3 border-t border-slate-200 bg-slate-50">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="todayReports.length"
          layout="prev, pager, next"
          background
        />
      </div>
    </div>

    <el-dialog v-model="editVisible" :title="t('productionHistory.dialog.title')" width="560px" destroy-on-close>
      <el-form v-if="editForm" :model="editForm" label-position="top">
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <el-form-item :label="t('productionHistory.dialog.operatingMinutes')">
            <el-input-number v-model="editForm.totalOperatingMinutes" :min="0" class="!w-full" />
          </el-form-item>
          <el-form-item :label="t('productionHistory.dialog.downtimeMinutes')">
            <el-input-number v-model="editForm.downtimeMinutes" :min="0" class="!w-full" />
          </el-form-item>
          <el-form-item :label="t('productionHistory.dialog.inputQuantity')">
            <el-input-number v-model="editForm.inputQuantity" :min="0" class="!w-full" />
          </el-form-item>
          <el-form-item :label="t('productionHistory.dialog.goodQuantity')">
            <el-input-number v-model="editForm.goodQuantity" :min="0" class="!w-full" />
          </el-form-item>
          <el-form-item :label="t('productionHistory.dialog.defectQuantity')" class="sm:col-span-2">
            <el-input-number v-model="editForm.defectQuantity" :min="0" class="!w-full" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">{{ t('productionHistory.dialog.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">{{ t('productionHistory.dialog.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { productionApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()

const todayReports = ref([])
const reportDate = ref(new Date().toISOString().slice(0, 10))
const loading = ref(false)
const saving = ref(false)
const editVisible = ref(false)
const editForm = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)

const paginatedReports = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return todayReports.value.slice(start, end)
})

function formatTime(iso) {
  if (!iso) return ''
  return iso.split('T')[1]?.substring(0, 5) || ''
}

function formatPercent(value) {
  const percent = Number(value || 0) * 100
  return `${percent.toFixed(1)}%`
}

function oeeClass(oee) {
  if (oee >= 0.85) return 'bg-emerald-100 text-emerald-700'
  if (oee >= 0.65) return 'bg-amber-100 text-amber-700'
  return 'bg-rose-100 text-rose-700'
}

async function loadReports() {
  loading.value = true
  try {
    todayReports.value = await productionApi.today({ reportDate: reportDate.value })
  } catch (error) {
    ElMessage.error(`${t('productionHistory.messages.loadFailed')}: ${error.message}`)
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  editForm.value = {
    id: row.id,
    reportDate: row.reportDate,
    lineCode: row.lineCode,
    shiftName: row.shiftName,
    machineCode: row.machineCode,
    partNumber: row.partNumber,
    partName: row.partName,
    cycleTimeSeconds: row.cycleTimeSeconds,
    totalOperatingMinutes: row.totalOperatingMinutes,
    downtimeMinutes: row.downtimeMinutes,
    inputQuantity: row.inputQuantity,
    goodQuantity: row.goodQuantity,
    defectQuantity: row.defectQuantity,
  }
  editVisible.value = true
}

async function saveEdit() {
  if (!editForm.value?.id) return
  saving.value = true
  try {
    const payload = {
      reportDate: editForm.value.reportDate,
      lineCode: editForm.value.lineCode,
      shiftName: editForm.value.shiftName,
      machineCode: editForm.value.machineCode,
      partNumber: editForm.value.partNumber,
      partName: editForm.value.partName,
      cycleTimeSeconds: Number(editForm.value.cycleTimeSeconds || 0),
      totalOperatingMinutes: Number(editForm.value.totalOperatingMinutes || 0),
      downtimeMinutes: Number(editForm.value.downtimeMinutes || 0),
      inputQuantity: Number(editForm.value.inputQuantity || 0),
      goodQuantity: Number(editForm.value.goodQuantity || 0),
      defectQuantity: Number(editForm.value.defectQuantity || 0),
    }
    await productionApi.update(editForm.value.id, payload)
    ElMessage.success(t('productionHistory.messages.saveSuccess'))
    editVisible.value = false
    await loadReports()
  } catch (error) {
    ElMessage.error(`${t('productionHistory.messages.saveFailed')}: ${error.message}`)
  } finally {
    saving.value = false
  }
}

onMounted(loadReports)
</script>
