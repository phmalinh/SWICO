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
      <div class="excel-table-wrap" v-loading="loading">
        <table class="excel-like-table">
          <thead>
            <tr>
              <th class="select-col">
                <input type="checkbox" :checked="allPageReportsSelected" @change="togglePageReportSelection" />
              </th>
              <th>{{ t('reports.search.table.date') }}</th>
              <th>{{ t('reports.search.table.line') }}</th>
              <th>{{ t('reports.search.table.shift') }}</th>
              <th>{{ t('reports.search.table.machine') }}</th>
              <th>{{ t('reports.search.table.company') }}</th>
              <th>{{ t('reports.search.table.operatorName') }}</th>
              <th>{{ t('reports.search.table.responsibleLeader') }}</th>
              <th>{{ t('reports.search.table.partNumber') }}</th>
              <th>{{ t('reports.search.table.partName') }}</th>
              <th>{{ t('reports.search.table.processIds') }}</th>
              <th>{{ t('reports.search.table.cycleTime') }}</th>
              <th>{{ t('reports.search.table.totalOperatingMinutes') }}</th>
              <th>{{ t('reports.search.table.downtimeMinutes') }}</th>
              <th class="reason-col">{{ t('reports.search.table.downtimeReason') }}</th>
              <th>{{ t('reports.search.table.downtimeTime') }}</th>
              <th>{{ t('reports.search.table.shiftStandardTimeMinutes') }}</th>
              <th>{{ t('reports.search.table.dailyTargetDayQuantity') }}</th>
              <th>{{ t('reports.search.table.dailyTargetQuantity') }}</th>
              <th>{{ t('reports.search.table.inputQuantity') }}</th>
              <th>{{ t('reports.search.table.goodQuantity') }}</th>
              <th>{{ t('reports.search.table.defectQuantity') }}</th>
              <th>{{ t('reports.search.table.internalDefectQuantity') }}</th>
              <th>{{ t('reports.search.table.externalDefectQuantity') }}</th>
              <th>{{ t('reports.search.table.lotNo') }}</th>
              <th>{{ t('reports.search.table.responsibility') }}</th>
              <th>{{ t('reports.search.table.deductionPercent') }}</th>
              <th>{{ t('reports.search.table.productionEfficiency') }}</th>
              <th>{{ t('reports.search.table.dailyTargetEfficiency') }}</th>
              <th>{{ t('reports.search.table.rates') }}</th>
              <th>{{ t('reports.search.table.oee') }}</th>
              <th>{{ t('reports.search.table.evaluationLabel') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!paginatedReportRows.length">
              <td class="empty-cell" colspan="32">-</td>
            </tr>
            <template v-for="line in paginatedReportRows" :key="line.key">
              <tr>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="center-cell">
                  <input type="checkbox" :checked="isReportSelected(line.report.id)" @change="toggleReportSelection(line.report.id, $event.target.checked)" />
                </td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan">{{ line.report.reportDate }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="center-cell">{{ line.report.lineCode }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan">{{ line.report.shiftName }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan">{{ line.report.machineCode }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan">{{ line.report.company }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan">{{ line.report.operatorName || line.report.createdBy || '-' }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan">{{ line.report.responsibleLeader || '-' }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan">{{ line.report.partNumber }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="text-cell">{{ line.report.partName }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="text-cell">{{ formatProcessIds(line.report.processIds) }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ line.report.cycleTimeSeconds }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ line.report.totalOperatingMinutes }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ line.report.downtimeMinutes }}</td>
                <td class="text-cell reason-col">{{ line.downtime.reason }}</td>
                <td class="number-cell">{{ line.downtime.minutes }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ line.report.shiftStandardTimeMinutes }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ formatNumber(line.report.dailyTargetDayQuantity) }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ formatNumber(line.report.dailyTargetQuantity) }}</td>
                <td v-if="line.showLot" :rowspan="line.lotRowspan" class="number-cell">{{ line.lot.inputQuantity }}</td>
                <td v-if="line.showLot" :rowspan="line.lotRowspan" class="number-cell">{{ line.lot.goodQuantity }}</td>
                <td v-if="line.showLot" :rowspan="line.lotRowspan" class="number-cell">{{ line.lot.defectQuantity }}</td>
                <td v-if="line.showLot" :rowspan="line.lotRowspan" class="number-cell">{{ line.lot.internalDefectQuantity }}</td>
                <td v-if="line.showLot" :rowspan="line.lotRowspan" class="number-cell">{{ line.lot.externalDefectQuantity }}</td>
                <td v-if="line.showLot" :rowspan="line.lotRowspan" class="center-cell">{{ line.lot.lotNo }}</td>
                <td v-if="!line.lot" class="number-cell">-</td>
                <td v-if="!line.lot" class="number-cell">-</td>
                <td v-if="!line.lot" class="number-cell">-</td>
                <td v-if="!line.lot" class="number-cell">-</td>
                <td v-if="!line.lot" class="number-cell">-</td>
                <td v-if="!line.lot" class="center-cell">-</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ formatPercent(line.report.responsibility) }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ formatPercent(line.report.deductionPercent) }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ formatPercent(line.report.productionEfficiency) }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ formatPercent(line.report.dailyTargetEfficiency) }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="center-cell">{{ rate(line.report.availabilityRate) }}/{{ rate(line.report.performanceRate) }}/{{ rate(line.report.qualityRate) }}</td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan" class="number-cell">
                  <span class="font-black" :class="oeeTextClass(line.report.oee)">{{ formatPercent(line.report.oee) }}</span>
                </td>
                <td v-if="line.showReport" :rowspan="line.reportRowspan">{{ line.report.evaluationLabel }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
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

const paginatedReportRows = computed(() => paginatedReports.value.flatMap(buildReportTableRows))

const pageReportIds = computed(() => paginatedReports.value.map(report => report.id).filter(id => id !== undefined && id !== null))

const allPageReportsSelected = computed(() => (
  pageReportIds.value.length > 0 && pageReportIds.value.every(id => selectedIds.value.includes(id))
))

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
  return `${formatPercentNumber(value)}%`
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString()
}

function splitDowntimeReasons(value) {
  return String(value || '')
    .split(/\s*[;；]\s*|\r?\n/)
    .map(item => item.trim())
    .filter(Boolean)
}

function downtimeDisplayRows(row) {
  if (Array.isArray(row.downtimes) && row.downtimes.length) {
    return row.downtimes
      .map(item => formatDowntimeDisplay(item.reason, item.minutes))
      .filter(item => item.reason || item.minutes)
  }
  const reasons = splitDowntimeReasons(row.downtimeReason)
  return reasons.length ? reasons.map(item => parseDowntimeDisplay(item)) : [{ reason: '-', minutes: '-' }]
}

function formatDowntimeDisplay(reason, minutes) {
  const reasonText = String(reason || '').trim() || '-'
  const minuteValue = Number(minutes || 0)
  return {
    reason: reasonText,
    minutes: minuteValue > 0 ? formatNumber(minuteValue) : '-',
  }
}

function parseDowntimeDisplay(value) {
  const text = String(value || '').trim()
  const match = text.match(/^(.*?)(?:\s+-\s+(\d+))?$/)
  return {
    reason: match?.[1]?.trim() || '-',
    minutes: match?.[2] ? formatNumber(match[2]) : '-',
  }
}

function downtimeRowsForTable(row) {
  if (Array.isArray(row.downtimes) && row.downtimes.length) {
    return row.downtimes
      .map(item => ({
        ...formatDowntimeDisplay(item.reason, item.minutes),
        lotNo: normalizeLotNo(item.lotNo),
      }))
      .filter(item => item.reason || item.minutes)
  }
  return downtimeDisplayRows(row).map(item => ({ ...item, lotNo: '' }))
}

function buildReportTableRows(report) {
  const detailLines = detailLinesForReport(report)
  const rows = detailLines.map((line, index) => ({
    key: `${report.id || 'report'}-${index}`,
    report,
    reportRowspan: detailLines.length,
    showReport: index === 0,
    showLot: false,
    lotRowspan: 1,
    lot: line.lot,
    downtime: line.downtime,
  }))

  let index = 0
  while (index < rows.length) {
    const lot = rows[index].lot
    if (!lot) {
      index++
      continue
    }
    let groupEnd = index
    while (groupEnd + 1 < rows.length && sameLot(lot, rows[groupEnd + 1].lot)) {
      groupEnd++
    }
    rows[index].showLot = true
    rows[index].lotRowspan = groupEnd - index + 1
    index = groupEnd + 1
  }

  return rows
}

function detailLinesForReport(report) {
  const lots = lotDisplayRows(report)
  const downtimes = downtimeRowsForTable(report)
  const hasDowntimeLotNo = downtimes.some(item => item.lotNo)

  if (!hasDowntimeLotNo) {
    const lineCount = Math.max(lots.length, downtimes.length, 1)
    return Array.from({ length: lineCount }, (_, index) => ({
      lot: lots[index] || null,
      downtime: downtimes[index] || { reason: '-', minutes: '-', lotNo: '' },
    }))
  }

  const lines = []
  lots.forEach(lot => {
    const lotDowntimes = downtimes.filter(downtime => sameLotNo(downtime.lotNo, lot.lotNo))
    if (!lotDowntimes.length) {
      lines.push({ lot, downtime: { reason: '-', minutes: '-', lotNo: lot.lotNo } })
      return
    }
    lotDowntimes.forEach(downtime => lines.push({ lot, downtime }))
  })
  downtimes
    .filter(downtime => !lots.some(lot => sameLotNo(downtime.lotNo, lot.lotNo)))
    .forEach(downtime => lines.push({ lot: null, downtime }))

  return lines.length ? lines : [{ lot: null, downtime: { reason: '-', minutes: '-', lotNo: '' } }]
}

function normalizeLotNo(value) {
  return String(value || '').trim()
}

function sameLotNo(left, right) {
  return normalizeLotNo(left) === normalizeLotNo(right)
}

function sameLot(left, right) {
  if (!left || !right) return false
  return sameLotNo(left.lotNo, right.lotNo)
    && Number(left.inputQuantity || 0) === Number(right.inputQuantity || 0)
    && Number(left.goodQuantity || 0) === Number(right.goodQuantity || 0)
    && Number(left.defectQuantity || 0) === Number(right.defectQuantity || 0)
    && Number(left.internalDefectQuantity || 0) === Number(right.internalDefectQuantity || 0)
    && Number(left.externalDefectQuantity || 0) === Number(right.externalDefectQuantity || 0)
}

function rate(value) {
  return formatPercentNumber(value)
}

function formatPercentNumber(value) {
  const percent = Number(value || 0) * 100
  return Number(percent.toFixed(2)).toString()
}

function lotDisplayRows(row) {
  const lots = Array.isArray(row.lots) && row.lots.length
    ? row.lots
    : [{
        lotNo: row.lotNo,
        inputQuantity: row.inputQuantity,
        goodQuantity: row.goodQuantity,
        defectQuantity: row.defectQuantity,
        internalDefectQuantity: row.internalDefectQuantity,
        externalDefectQuantity: row.externalDefectQuantity,
      }]

  return lots.map(lot => {
    const inputQuantity = Number(lot.inputQuantity || 0)
    const internalDefectQuantity = Number(lot.internalDefectQuantity || 0)
    const externalDefectQuantity = Number(lot.externalDefectQuantity || 0)
    const defectQuantity = lot.defectQuantity ?? (internalDefectQuantity + externalDefectQuantity)
    const goodQuantity = lot.goodQuantity ?? Math.max(inputQuantity - Number(defectQuantity || 0), 0)

    return {
      lotNo: lot.lotNo || '-',
      inputQuantity,
      goodQuantity,
      defectQuantity,
      internalDefectQuantity,
      externalDefectQuantity,
    }
  })
}

function oeeTextClass(oee) {
  if (oee >= 0.85) return 'text-emerald-600'
  if (oee >= 0.65) return 'text-amber-600'
  return 'text-rose-600'
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

function isReportSelected(id) {
  return selectedIds.value.includes(id)
}

function toggleReportSelection(id, checked) {
  if (id === undefined || id === null) return
  if (checked) {
    if (!selectedIds.value.includes(id)) selectedIds.value = [...selectedIds.value, id]
    return
  }
  selectedIds.value = selectedIds.value.filter(item => item !== id)
}

function togglePageReportSelection(event) {
  const checked = event.target.checked
  if (checked) {
    selectedIds.value = Array.from(new Set([...selectedIds.value, ...pageReportIds.value]))
    return
  }
  selectedIds.value = selectedIds.value.filter(id => !pageReportIds.value.includes(id))
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

<style scoped>
.excel-table-wrap {
  min-height: 180px;
  overflow: auto;
}

.excel-like-table {
  min-width: 2500px;
  /* width: 100%; */
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 14px;
}

.excel-like-table th {
  position: sticky;
  top: 0;
  z-index: 2;
  background: #1f4e79;
  color: #fff;
  font-weight: 800;
  line-height: 1.3;
  text-align: center;
}

.excel-like-table th,
.excel-like-table td {
  border: 1px solid #111827;
  padding: 8px 6px;
  vertical-align: middle;
}

.excel-like-table td {
  background: #fff;
  color: #1f2937;
  font-weight: 600;
}

.select-col {
  width: 42px;
}

.reason-col {
  width: 280px;
}

.center-cell {
  text-align: center;
}

.number-cell {
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.text-cell {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-cell {
  height: 80px;
  text-align: center;
  color: #94a3b8;
}

</style>
