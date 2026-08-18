<template>
  <div class="space-y-6">
    <PageHeader
      :eyebrow="t('reports.exportV9.eyebrow')"
      :title="t('reports.exportV9.pageTitle')"
    />


    <div class="page-card mb-5 p-5">
      <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 class="text-lg font-black text-slate-900">{{ t('reports.search.pageTitle') }}</h3>
          <p class="text-sm text-slate-500">{{ t('reports.search.pageSubtitle') }}</p>
        </div>
      </div>

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
          <el-form-item :label="t('reports.search.fields.operatorName')">
            <el-input v-model="filters.operatorName" clearable :placeholder="t('reports.search.fields.operatorPlaceholder')" />
          </el-form-item>
        </div>
        <div class="mt-2 flex flex-col gap-2 md:flex-row md:justify-end">
          <el-button type="primary" :loading="loading" @click="search" class="w-full md:w-auto">
            <el-icon class="mr-1"><Search /></el-icon>
            {{ t('reports.search.buttons.search') }}
          </el-button>
          <el-button type="warning" :loading="importing" @click="selectImportFile" class="w-full md:w-auto">
            <el-icon class="mr-1"><Upload /></el-icon>
            {{ t('reports.search.buttons.importExcel') }}
          </el-button>
          <el-button type="danger" :disabled="selectedIds.length === 0" @click="deleteSelected" class="w-full md:w-auto">{{ t('reports.search.buttons.deleteSelected') }}</el-button>
          <el-button type="success" :loading="exporting" @click="exportExcel({ from: filters.dateFrom, to: filters.dateTo, lineCode: filters.lineCode, shiftName: filters.shiftName, partNumber: filters.partNumber, operatorName: filters.operatorName })" class="w-full md:w-auto">
            <el-icon class="mr-1"><Download /></el-icon>
            {{ t('reports.exportV9.button') }}
          </el-button>
        </div>
        <input ref="fileInput" type="file" accept=".xlsx,.xls" class="hidden" @change="handleImportFile" />
      </el-form>
    </div>

    <!-- <div class="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-5">
      <div v-for="item in summaryItems" :key="item.key" class="rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm">
        <p class="text-xs font-bold uppercase text-slate-500">{{ item.label }}</p>
        <p class="mt-1 text-2xl font-black text-slate-900">{{ item.format === 'percent' ? formatPercent(item.value) : formatNumber(item.value) }}</p>
      </div>
    </div> -->

    <div class="page-card overflow-hidden">
      <el-table :data="paginatedReports" stripe style="width: 100%" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="56" />
        <el-table-column prop="reportDate" :label="t('reports.search.table.date')" width="100" />
        <el-table-column prop="lineCode" :label="t('reports.search.table.line')" width="60" align="center" />
        <el-table-column prop="shiftName" :label="t('reports.search.table.shift')" width="200" />
        <el-table-column prop="machineCode" :label="t('reports.search.table.machine')" width="80" />
        <el-table-column prop="company" :label="t('reports.search.table.company')" min-width="80" show-overflow-tooltip />
        <el-table-column :label="t('reports.search.table.operatorName')" width="150" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ row.operatorName || row.createdBy || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('reports.search.table.responsibleLeader')" width="160" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ row.responsibleLeader || '-' }}</template>
        </el-table-column>
        <el-table-column prop="partNumber" :label="t('reports.search.table.partNumber')" width="120" />
        <el-table-column prop="partName" :label="t('reports.search.table.partName')" min-width="80" show-overflow-tooltip />
        <el-table-column :label="t('reports.search.table.processIds')" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ formatProcessIds(row.processIds) }}</template>
        </el-table-column>
        <el-table-column prop="cycleTimeSeconds" :label="t('reports.search.table.cycleTime')" width="70" align="center" />
        <el-table-column prop="totalOperatingMinutes" :label="t('reports.search.table.totalOperatingMinutes')" width="70" align="center" />
        <el-table-column prop="downtimeMinutes" :label="t('reports.search.table.downtimeMinutes')" width="80" align="center" />
        <el-table-column prop="downtimeReason" :label="t('reports.search.table.downtimeReason')" min-width="80" show-overflow-tooltip />
        <el-table-column :label="t('reports.search.table.responsibility')" min-width="100" align="center">
          <template #default="{ row }">{{ formatPercent(row.responsibility) }}</template>
        </el-table-column>
        <el-table-column :label="t('reports.search.table.deductionPercent')" width="90" align="center">
          <template #default="{ row }">{{ formatPercent(row.deductionPercent) }}</template>
        </el-table-column>
        <el-table-column prop="shiftStandardTimeMinutes" :label="t('reports.search.table.shiftStandardTimeMinutes')" width="80" align="center" />
        <el-table-column prop="dailyTargetQuantity" :label="t('reports.search.table.dailyTargetQuantity')" width="80" align="center" />
        <el-table-column :label="t('reports.search.table.inputGoodDefect')" width="130" align="center">
          <template #default="{ row }">{{ row.inputQuantity }}/{{ row.goodQuantity }}/{{ row.defectQuantity }}</template>
        </el-table-column>
        <el-table-column prop="internalDefectQuantity" :label="t('reports.search.table.internalDefectQuantity')" width="130" align="center" />
        <el-table-column prop="externalDefectQuantity" :label="t('reports.search.table.externalDefectQuantity')" width="130" align="center" />
        <el-table-column prop="productionEfficiency" :label="t('reports.search.table.productionEfficiency')" width="80" align="center" />
        <el-table-column :label="t('reports.search.table.rates')" width="80" align="center">
          <template #default="{ row }">
            <span class="text-xs font-bold">{{ rate(row.availabilityRate) }}/{{ rate(row.performanceRate) }}/{{ rate(row.qualityRate) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('reports.search.table.oee')" width="88" align="center">
          <template #default="{ row }">
            <span class="font-black" :class="oeeTextClass(row.oee)">{{ formatPercent(row.oee) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="evaluationLabel" :label="t('reports.search.table.evaluationLabel')" min-width="120" />
      </el-table>
      <div class="flex flex-col gap-3 border-t border-slate-100 px-4 py-3 text-sm font-semibold text-slate-500 md:flex-row md:items-center md:justify-between">
        <span>{{ t('reports.search.results', { count: reports.length }) }}</span>
        <div class="flex items-center gap-3">
          <span>{{ t('common.units.rows') || 'Dòng/trang' }}</span>
          <el-select v-model="pageSize" size="small" style="width: 96px">
            <el-option v-for="size in pageSizeOptions" :key="size" :label="String(size)" :value="size" />
          </el-select>
          <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="reports.length" layout="prev, pager, next" background />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Download, Search, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi, productionApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()

const form = ref({
  dateFrom: new Date().toISOString().slice(0, 10),
  dateTo: new Date().toISOString().slice(0, 10),
  lineCode: '',
})

const filters = ref({
  dateFrom: new Date().toISOString().slice(0, 10),
  dateTo: new Date().toISOString().slice(0, 10),
  lineCode: '',
  shiftName: '',
  partNumber: '',
  operatorName: '',
})

const exporting = ref(false)
const importing = ref(false)
const loading = ref(false)
const lines = ref([])
const shifts = ref([])
const reports = ref([])
const processNameById = ref({})
const selectedIds = ref([])
const fileInput = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const pageSizeOptions = [10, 20, 50, 100]

const paginatedReports = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return reports.value.slice(start, start + pageSize.value)
})

const reportTotals = computed(() => reports.value.reduce((totals, report) => ({
  completedQuantity: totals.completedQuantity + Number(report.inputQuantity || 0),
  goodQuantity: totals.goodQuantity + Number(report.goodQuantity || 0),
  internalDefectQuantity: totals.internalDefectQuantity + Number(report.internalDefectQuantity || 0),
  externalDefectQuantity: totals.externalDefectQuantity + Number(report.externalDefectQuantity || 0),
  productionEfficiency: totals.productionEfficiency + Number(report.productionEfficiency || 0),
  availabilityRate: totals.availabilityRate + Number(report.availabilityRate || 0),
  performanceRate: totals.performanceRate + Number(report.performanceRate || 0),
  qualityRate: totals.qualityRate + Number(report.qualityRate || 0),
  oee: totals.oee + Number(report.oee || 0),
}), {
  completedQuantity: 0,
  goodQuantity: 0,
  internalDefectQuantity: 0,
  externalDefectQuantity: 0,
  productionEfficiency: 0,
  availabilityRate: 0,
  performanceRate: 0,
  qualityRate: 0,
  oee: 0,
}))

const reportCount = computed(() => reports.value.length)

function averageTotal(value) {
  return reportCount.value ? value / reportCount.value : 0
}

const summaryItems = computed(() => [
  {
    key: 'completedQuantity',
    label: t('reports.search.summary.completedQuantity'),
    value: reportTotals.value.completedQuantity,
  },
  {
    key: 'goodQuantity',
    label: t('reports.search.summary.goodQuantity'),
    value: reportTotals.value.goodQuantity,
  },
  {
    key: 'internalDefectQuantity',
    label: t('reports.search.summary.internalDefectQuantity'),
    value: reportTotals.value.internalDefectQuantity,
  },
  {
    key: 'externalDefectQuantity',
    label: t('reports.search.summary.externalDefectQuantity'),
    value: reportTotals.value.externalDefectQuantity,
  },
  {
    key: 'avgProductionEfficiency',
    label: t('reports.search.summary.avgProductionEfficiency'),
    value: averageTotal(reportTotals.value.productionEfficiency),
    format: 'percent',
  },
  {
    key: 'avgAvailabilityRate',
    label: t('reports.search.summary.avgAvailabilityRate'),
    value: averageTotal(reportTotals.value.availabilityRate),
    format: 'percent',
  },
  {
    key: 'avgPerformanceRate',
    label: t('reports.search.summary.avgPerformanceRate'),
    value: averageTotal(reportTotals.value.performanceRate),
    format: 'percent',
  },
  {
    key: 'avgQualityRate',
    label: t('reports.search.summary.avgQualityRate'),
    value: averageTotal(reportTotals.value.qualityRate),
    format: 'percent',
  },
  {
    key: 'avgOee',
    label: t('reports.search.summary.avgOee'),
    value: averageTotal(reportTotals.value.oee),
    format: 'percent',
  },
])

async function loadOptions() {
  try {
    const [linesRes, shiftsRes, productsRes] = await Promise.all([masterApi.getLines(), masterApi.getShifts(), masterApi.getProducts()])
    lines.value = linesRes.map(item => ({
      lineCode: item.code,
      description: item.name,
    }))
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
      if (process?.id) map[process.id] = process.processCode || process.process || ''
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
  return processIds.map(id => processNameById.value[id]).filter(Boolean).join(' + ') || '-'
}

async function exportExcel(params = null) {
  exporting.value = true
  try {
    const payload = params || {
      from: form.value.dateFrom,
      to: form.value.dateTo,
      lineCode: form.value.lineCode,
    }

    const blob = await productionApi.exportV9(payload)

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `OEE_V9_${payload.from}_${payload.to}.xlsx`
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)

    ElMessage.success(t('reports.exportV9.messages.success'))
  } catch (error) {
    ElMessage.error(`${t('reports.exportV9.messages.failed')}: ${error.message}`)
  } finally {
    exporting.value = false
  }
}

function selectImportFile() {
  fileInput.value?.click()
}

async function handleImportFile(event) {
  const file = event.target.files?.[0]
  if (!file) return
  event.target.value = null
  await importExcelFile(file)
}

async function importExcelFile(file) {
  importing.value = true
  try {
    await productionApi.importV9(file)
    ElMessage.success(t('reports.exportV9.messages.importSuccess'))
    await search()
  } catch (error) {
    ElMessage.error(`${t('reports.exportV9.messages.importFailed')}: ${error.message}`)
  } finally {
    importing.value = false
  }
}

function formatPercent(value) {
  return `${(Number(value || 0) * 100).toFixed(1)}%`
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString()
}

function rate(value) {
  return `${(Number(value || 0) * 100).toFixed(0)}`
}

function oeeTextClass(oee) {
  if (oee >= 0.85) return 'text-emerald-600'
  if (oee >= 0.65) return 'text-amber-600'
  return 'text-rose-600'
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
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
      operatorName: filters.value.operatorName,
    })
    selectedIds.value = []
    currentPage.value = 1
  } catch (error) {
    ElMessage.error(`${t('reports.search.messages.searchFailed')}: ${error.message}`)
  } finally {
    loading.value = false
  }
}

function selectAll() {
  if (!reports.value || !reports.value.length) return
  const allIds = reports.value.map(r => r.id)
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
  } catch (error) {
    ElMessage.error(`${t('reports.search.messages.deleteFailed')}: ${error.message}`)
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
    operatorName: '',
  }
  search()
}

onMounted(async () => {
  await loadOptions()
  await search()
})

watch([reports, pageSize], () => {
  const maxPage = Math.max(1, Math.ceil(reports.value.length / pageSize.value))
  if (currentPage.value > maxPage) currentPage.value = maxPage
})
</script>
