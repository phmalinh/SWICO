<template>
  <div>
    <PageHeader
      :eyebrow="t('reports.search.eyebrow')"
      :title="t('reports.search.pageTitle')"
      :subtitle="t('reports.search.pageSubtitle')"
    />

    <div class="page-card mb-5 p-5">
      <el-form :model="filters" label-position="top">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-6">
          <el-form-item :label="t('reports.search.fields.dateFrom')">
            <el-date-picker v-model="filters.dateFrom" type="date" class="!w-full" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item :label="t('reports.search.fields.dateTo')">
            <el-date-picker v-model="filters.dateTo" type="date" class="!w-full" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item :label="t('reports.search.fields.line')">
            <el-select v-model="filters.lineCode" clearable :placeholder="t('reports.search.fields.all')" class="w-full">
              <el-option v-for="line in lines" :key="line.lineCode" :label="line.lineCode" :value="line.lineCode" />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('reports.search.fields.shift')">
            <el-select v-model="filters.shiftName" clearable :placeholder="t('reports.search.fields.all')" class="w-full">
              <el-option v-for="shift in shifts" :key="shift.shiftName" :label="shift.shiftName" :value="shift.shiftName" />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('reports.search.fields.partNumber')">
            <el-input v-model="filters.partNumber" clearable placeholder="PN-..." />
          </el-form-item>
        </div>
        <div class="mt-2 flex flex-col gap-2 md:flex-row">
          <el-button type="primary" :loading="loading" @click="search" class="w-full md:w-auto">
            <el-icon class="mr-1"><Search /></el-icon>
            {{ t('reports.search.buttons.search') }}
          </el-button>
          <el-button @click="resetFilters" class="w-full md:w-auto">{{ t('reports.search.buttons.reset') }}</el-button>
        </div>
      </el-form>
    </div>

    <div class="page-card overflow-hidden">
      <div class="px-4 py-3 border-b">
        <el-button type="danger" :disabled="selectedIds.length===0" @click="deleteSelected" class="mr-2">{{ t('reports.search.buttons.deleteSelected') }}</el-button>
        <el-button type="default" @click="selectAll">{{ t('reports.search.buttons.selectAll') }}</el-button>
      </div>
      <el-table :data="reports" stripe style="width: 100%" v-loading="loading">
        <el-table-column type="selection" width="56" />
        <el-table-column prop="reportDate" :label="t('reports.search.table.date')" width="112" />
        <el-table-column prop="lineCode" :label="t('reports.search.table.line')" width="82" align="center" />
        <el-table-column prop="shiftName" :label="t('reports.search.table.shift')" width="86" />
        <el-table-column prop="machineCode" :label="t('reports.search.table.machine')" width="92" />
        <el-table-column prop="partNumber" :label="t('reports.search.table.partNumber')" width="120" />
        <el-table-column prop="partName" :label="t('reports.search.table.partName')" min-width="150" show-overflow-tooltip />
        <el-table-column :label="t('reports.search.table.processIds')" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ formatProcessIds(row.processIds) }}</template>
        </el-table-column>
        <el-table-column :label="t('reports.search.table.oee')" width="88" align="center">
          <template #default="{ row }">
            <span class="font-black" :class="oeeTextClass(row.oee)">{{ formatPercent(row.oee) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="productionEfficiency" :label="t('reports.search.table.productionEfficiency')" width="90" align="center">
          <template #default="{ row }">
            {{ rate(row.productionEfficiency) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('reports.search.table.rates')" width="132" align="center">
          <template #default="{ row }">
            <span class="text-xs font-bold">{{ rate(row.availabilityRate) }}/{{ rate(row.performanceRate) }}/{{ rate(row.qualityRate) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('reports.search.table.inputGoodDefect')" width="140" align="center">
          <template #default="{ row }">{{ row.inputQuantity }}/{{ row.goodQuantity }}/{{ row.defectQuantity }}</template>
        </el-table-column>
        <el-table-column :label="t('reports.search.table.responsibility')" min-width="100" align="center">
          <template #default="{ row }">{{ formatPercent(row.responsibility) }}</template>
        </el-table-column>
        <el-table-column :label="t('reports.search.table.deductionPercent')" width="90" align="center">
          <template #default="{ row }">{{ formatPercent(row.deductionPercent) }}</template>
        </el-table-column>
        <el-table-column prop="internalDefectQuantity" :label="t('reports.search.table.internalDefectQuantity')" width="130" align="center" />
        <el-table-column prop="externalDefectQuantity" :label="t('reports.search.table.externalDefectQuantity')" width="130" align="center" />
      </el-table>
      <div class="border-t border-slate-100 px-4 py-3 text-sm font-semibold text-slate-500">
        {{ t('reports.search.results', { count: reports.length }) }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi, productionApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()

const filters = ref({
  dateFrom: new Date().toISOString().slice(0, 10),
  dateTo: new Date().toISOString().slice(0, 10),
  lineCode: '',
  shiftName: '',
  partNumber: '',
})

const reports = ref([])
const lines = ref([])
const shifts = ref([])
const processNameById = ref({})
const loading = ref(false)
const selectedIds = ref([])

async function loadOptions() {
  try {
    const [linesRes, shiftsRes, productsRes] = await Promise.all([masterApi.getLines(), masterApi.getShifts(), masterApi.getProducts()])
    lines.value = linesRes.map(item => ({ lineCode: item.code, description: item.name }))
    shifts.value = shiftsRes.map(item => ({ shiftName: item.name }))
    await loadProcessNamesForProducts(productsRes)
  } catch (error) {
    ElMessage.error(`${t('reports.search.messages.loadFailed')}: ${error.message}`)
  }
}

function mergeProcessNames(processes = []) {
  processNameById.value = {
    ...processNameById.value,
    ...processes.reduce((map, process) => {
      if (process?.id) map[process.id] = process.process
      return map
    }, {}),
  }
}

async function loadProcessNamesForProducts(products = []) {
  const processGroups = await Promise.all(
    products
      .filter(product => product.id)
      .map(product => masterApi.getProductProcesses(product.id).catch(() => []))
  )
  processGroups.forEach(mergeProcessNames)
}

function formatProcessIds(processIds) {
  if (!Array.isArray(processIds) || processIds.length === 0) return '-'
  return processIds.map(id => processNameById.value[id]).filter(Boolean).join('； ') || '-'
}

function formatPercent(value) {
  return `${(Number(value || 0) * 100).toFixed(1)}%`
}

function rate(value) {
  return `${(Number(value || 0) * 100).toFixed(0)}`
}

function oeeTextClass(oee) {
  if (oee >= 0.85) return 'text-emerald-600'
  if (oee >= 0.65) return 'text-amber-600'
  return 'text-rose-600'
}

async function search() {
  loading.value = true
  try {
    reports.value = await productionApi.search({
      from: filters.value.dateFrom,
      to: filters.value.dateTo,
      lineCode: filters.value.lineCode,
      shiftName: filters.value.shiftName,
      partNumber: filters.value.partNumber,
    })
    // clear selection on new search
    selectedIds.value = []
  } catch (error) {
    ElMessage.error(`${t('reports.search.messages.searchFailed')}: ${error.message}`)
  } finally {
    loading.value = false
  }
}

function selectAll() {
  if (!reports.value || !reports.value.length) return
  const allIds = reports.value.map(r => r.id)
  // toggle: if all selected then clear, else select all
  const allSelected = allIds.every(id => selectedIds.value.includes(id))
  selectedIds.value = allSelected ? [] : allIds
}

async function deleteSelected() {
  if (!selectedIds.value.length) return
  try {
    loading.value = true
    await productionApi.deleteReports(selectedIds.value)
    ElMessage.success(t('reports.search.messages.deleteSuccess'))
    await search()
  } catch (err) {
    ElMessage.error(`${t('reports.search.messages.deleteFailed')}: ${err.message}`)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    dateFrom: new Date().toISOString().slice(0, 10),
    dateTo: new Date().toISOString().slice(0, 10),
    lineCode: '',
    shiftName: '',
    partNumber: '',
  }
  search()
}

onMounted(async () => {
  await loadOptions()
  await search()
})
</script>
