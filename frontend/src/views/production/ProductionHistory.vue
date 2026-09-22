<template>
  <div>
    <PageHeader
      :eyebrow="t('productionHistory.eyebrow')"
      :title="t('productionHistory.pageTitle')"
    >
      <!-- <template #actions>
        <div class="flex items-center gap-3">
          <el-button :loading="loading" @click="loadReports">{{ t('productionHistory.refresh') }}</el-button>
          <el-tag type="info" size="large" effect="plain">{{ t('productionHistory.reportCount', { count: todayReports.length }) }}</el-tag>
        </div>
      </template> -->
    </PageHeader>

    <div class="page-card overflow-hidden">
      <!-- Bộ lọc và Nút tùy chỉnh ẩn/hiện cột -->
      <div class="grid grid-cols-1 gap-2.5 border-b border-slate-200 bg-slate-50 p-3 md:grid-cols-12 md:items-center">
        <el-date-picker v-model="reportDate" type="date" value-format="YYYY-MM-DD" class="!w-full md:col-span-2" @change="loadReports" />
        <el-select v-model="filters.lineCode" clearable filterable class="md:col-span-2" :placeholder="t('productionHistory.filters.line')" @change="onLineFilterChange">
          <el-option v-for="line in lines" :key="line.lineCode" :label="`${line.lineCode} - ${line.description}`" :value="line.lineCode" />
        </el-select>
        <el-select v-model="filters.machineCode" clearable filterable class="md:col-span-2" :placeholder="t('productionHistory.filters.machine')" @change="loadReports">
          <el-option v-for="machine in filteredMachineOptions" :key="machine.machineCode" :label="`${machine.machineCode} - ${machine.description}`" :value="machine.machineCode" />
        </el-select>
        <el-select v-model="filters.partNumber" clearable filterable class="md:col-span-2" :placeholder="t('productionHistory.filters.partNumber')" @change="loadReports">
          <el-option v-for="product in products" :key="product.partNumber" :label="product.partNumber" :value="product.partNumber">
            <span class="font-bold">{{ product.partNumber }}</span>
            <span class="ml-2 text-xs text-slate-400">{{ product.partName }}</span>
          </el-option>
        </el-select>
        <el-input v-model="filters.operatorName" clearable class="md:col-span-2" :placeholder="t('productionHistory.filters.operatorName')" @keyup.enter="loadReports" @clear="loadReports" />
        
        <div class="flex items-center gap-2 md:col-span-2">
          <el-button type="primary" class="!m-0 flex-1" :loading="loading" @click="loadReports">{{ t('productionHistory.filters.search') }}</el-button>
          <el-button class="!m-0 flex-1" @click="resetFilters">{{ t('productionHistory.filters.reset') }}</el-button>
          
          <!-- Popover chọn cột hiển thị -->
          <el-popover placement="bottom-end" :width="240" trigger="click">
            <template #reference>
              <el-button class="!m-0 px-2.5" title="Tùy chỉnh cột">
                <el-icon><Operation /></el-icon>
              </el-button>
            </template>
            <div class="column-setting-popover">
              <div class="font-bold border-b pb-1.5 mb-2 flex justify-between items-center text-sm text-slate-700">
                <span>{{ t('productionHistory.selectColumns') }}</span>
                <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" @change="handleCheckAllChange">
                  ALL
                </el-checkbox>
              </div>
              <el-scrollbar height="260px">
                <div class="flex flex-col gap-1.5">
                  <el-checkbox 
                    v-for="col in columns" 
                    :key="col.key" 
                    v-model="columnVisibility[col.key]"
                    @change="handleColumnChange"
                  >
                    {{ col.label }}
                  </el-checkbox>
                </div>
              </el-scrollbar>
            </div>
          </el-popover>
        </div>
      </div>

      <!-- Bảng dữ liệu Excel-like -->
      <div class="history-excel-wrap" v-loading="loading">
        <table class="history-excel-table">
          <thead>
            <tr>
              <th v-if="isColVisible('time')" class="reason-col2">{{ t('productionHistory.table.time') }}</th>
              <th v-if="isColVisible('line')">{{ t('productionHistory.table.line') }}</th>
              <th v-if="isColVisible('machine')">{{ t('productionHistory.table.machine') }}</th>
              <th v-if="isColVisible('operatorName')">{{ t('productionHistory.table.operatorName') }}</th>
              <th v-if="isColVisible('responsibleLeader')">{{ t('productionHistory.table.responsibleLeader') }}</th>
              <th v-if="isColVisible('partNumber')">{{ t('productionHistory.table.partNumber') }}</th>
              <th v-if="isColVisible('partName')">{{ t('productionHistory.table.partName') }}</th>
              <th v-if="isColVisible('processIds')">{{ t('productionHistory.table.processIds') }}</th>
              <th v-if="isColVisible('runDowntime')">{{ t('productionHistory.table.runDowntime') }}</th>
              <th v-if="isColVisible('downtimeReason')" class="reason-col">{{ t('productionHistory.table.downtimeReason') }}</th>
              <th v-if="isColVisible('downtimeMinutes')">{{ t('productionHistory.table.downtimeMinutes') }}</th>
              <th v-if="isColVisible('dailyTargetDayQuantity')">{{ t('productionHistory.table.dailyTargetDayQuantity') }}</th>
              <th v-if="isColVisible('dailyTargetQuantity')">{{ t('productionHistory.table.dailyTargetQuantity') }}</th>
              <th v-if="isColVisible('inputGoodDefect')" class="reason-col1">{{ t('productionHistory.table.inputGoodDefect') }}</th>
              <th v-if="isColVisible('internalDefectQuantity')">{{ t('productionHistory.table.internalDefectQuantity') }}</th>
              <th v-if="isColVisible('externalDefectQuantity')">{{ t('productionHistory.table.externalDefectQuantity') }}</th>
              <th v-if="isColVisible('lotNo')">{{ t('productionHistory.table.lotNo') }}</th>
              <th v-if="isColVisible('responsibility')">{{ t('productionHistory.table.responsibility') }}</th>
              <th v-if="isColVisible('deductionPercent')">{{ t('productionHistory.table.deductionPercent') }}</th>
              <th v-if="isColVisible('productionEfficiency')" class="reason-col1">{{ t('productionHistory.table.productionEfficiency') }}</th>
              <th v-if="isColVisible('dailyTargetEfficiency')" class="reason-col1">{{ t('productionHistory.table.dailyTargetEfficiency') }}</th>
              <th v-if="isColVisible('rates')" class="reason-col1">{{ t('productionHistory.table.rates') }}</th>
              <th v-if="isColVisible('oee')" class="reason-col1">{{ t('productionHistory.table.oee') }}</th>
              <th v-if="isColVisible('evaluationLabel')" class="reason-col">{{ t('productionHistory.table.evaluationLabel') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!historyReportRows.length">
              <td class="empty-cell" :colspan="visibleColumnsCount">{{ t('productionHistory.noData') }}</td>
            </tr>
            <template v-for="line in historyReportRows" :key="line.key">
              <tr>
                <td v-if="isColVisible('time') && line.showReport" :rowspan="line.reportRowspan" class="center-cell">{{ formatTime(line.report.createdAt) }}</td>
                <td v-if="isColVisible('line') && line.showReport" :rowspan="line.reportRowspan" class="center-cell">{{ line.report.lineCode }}</td>
                <td v-if="isColVisible('machine') && line.showReport" :rowspan="line.reportRowspan" class="center-cell">{{ line.report.machineCode }}</td>
                <td v-if="isColVisible('operatorName') && line.showReport" :rowspan="line.reportRowspan">{{ line.report.operatorName || line.report.createdBy || '-' }}</td>
                <td v-if="isColVisible('responsibleLeader') && line.showReport" :rowspan="line.reportRowspan">{{ line.report.responsibleLeader || '-' }}</td>
                <td v-if="isColVisible('partNumber') && line.showReport" :rowspan="line.reportRowspan">{{ line.report.partNumber }}</td>
                <td v-if="isColVisible('partName') && line.showReport" :rowspan="line.reportRowspan" class="text-cell">{{ line.report.partName }}</td>
                <td v-if="isColVisible('processIds') && line.showReport" :rowspan="line.reportRowspan" class="text-cell">{{ formatProcessIds(line.report.processIds) }}</td>
                <td v-if="isColVisible('runDowntime') && line.showReport" :rowspan="line.reportRowspan" class="center-cell">
                  <span class="text-emerald-600">{{ line.report.totalOperatingMinutes }}</span>
                  <span class="text-slate-300"> / </span>
                  <span class="text-rose-500">{{ line.report.downtimeMinutes }}</span>
                </td>
                <td v-if="isColVisible('downtimeReason')" class="text-cell reason-col">{{ line.downtime.reason }}</td>
                <td v-if="isColVisible('downtimeMinutes')" class="number-cell">{{ line.downtime.minutes }}</td>
                <td v-if="isColVisible('dailyTargetDayQuantity') && line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ formatFloorNumber(line.report.dailyTargetDayQuantity) }}</td>
                <td v-if="isColVisible('dailyTargetQuantity') && line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ formatFloorNumber(line.report.dailyTargetQuantity) }}</td>
                <td v-if="isColVisible('inputGoodDefect') && line.showLot" :rowspan="line.lotRowspan" class="center-cell">{{ line.lot ? line.lot.inputGoodDefect : '-' }}</td>
                <td v-if="isColVisible('internalDefectQuantity') && line.showLot" :rowspan="line.lotRowspan" class="number-cell">{{ line.lot ? line.lot.internalDefectQuantity : '-' }}</td>
                <td v-if="isColVisible('externalDefectQuantity') && line.showLot" :rowspan="line.lotRowspan" class="number-cell">{{ line.lot ? line.lot.externalDefectQuantity : '-' }}</td>
                <td v-if="isColVisible('lotNo') && line.showLot" :rowspan="line.lotRowspan" class="center-cell">{{ line.lot ? line.lot.lotNo : '-' }}</td>
                <td v-if="isColVisible('responsibility') && line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ formatPercent(line.report.responsibility) }}</td>
                <td v-if="isColVisible('deductionPercent') && line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ formatPercent(line.report.deductionPercent) }}</td>
                <td v-if="isColVisible('productionEfficiency') && line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ formatPercent(line.report.productionEfficiency) }}</td>
                <td v-if="isColVisible('dailyTargetEfficiency') && line.showReport" :rowspan="line.reportRowspan" class="number-cell">{{ formatPercent(line.report.dailyTargetEfficiency) }}</td>
                <td v-if="isColVisible('rates') && line.showReport" :rowspan="line.reportRowspan" class="center-cell">{{ rate(line.report.availabilityRate) }}/{{ rate(line.report.performanceRate) }}/{{ rate(line.report.qualityRate) }}</td>
                <td v-if="isColVisible('oee') && line.showReport" :rowspan="line.reportRowspan" class="number-cell">
                  <span class="rounded-full px-2.5 py-1 text-sm font-black" :class="oeeClass(line.report.oee)">{{ formatPercent(line.report.oee) }}</span>
                </td>
                <td v-if="isColVisible('evaluationLabel') && line.showReport" :rowspan="line.reportRowspan" class="center-cell">{{ line.report.evaluationLabel }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
      <div class="flex flex-col gap-3 border-t border-slate-200 bg-slate-50 px-4 py-3 md:flex-row md:items-center md:justify-between">
        <span class="text-sm font-semibold text-slate-500">{{ t('productionHistory.total') }}: {{ todayReports.length }}</span>
        <div class="flex items-center gap-3">
          <span class="text-sm text-slate-500">{{ t('productionHistory.rowsPerPage') }}</span>
          <el-select v-model="pageSize" size="small" style="width: 96px">
            <el-option v-for="size in pageSizeOptions" :key="size" :label="String(size)" :value="size" />
          </el-select>
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="todayReports.length"
            layout="prev, pager, next"
            background
          />
        </div>
      </div>
    </div>

    <!-- Dialog chỉnh sửa -->
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
import { computed, onMounted, ref, watch } from 'vue'
import { Operation } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi, productionApi } from '@/services/api'
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
const pageSizeOptions = [10, 20, 50, 100]
const processNameById = ref({})
const lines = ref([])
const machines = ref([])
const products = ref([])
const filters = ref({
  lineCode: '',
  machineCode: '',
  partNumber: '',
  operatorName: '',
})

// --- Quản lý Trạng thái ẩn/hiện cột (Lưu trạng thái boolean đơn thuần) ---
const columnVisibility = ref({
  time: true,
  line: true,
  machine: true,
  operatorName: true,
  responsibleLeader: true,
  partNumber: true,
  partName: true,
  processIds: true,
  runDowntime: true,
  downtimeReason: true,
  downtimeMinutes: true,
  dailyTargetDayQuantity: true,
  dailyTargetQuantity: true,
  inputGoodDefect: true,
  internalDefectQuantity: true,
  externalDefectQuantity: true,
  lotNo: true,
  responsibility: true,
  deductionPercent: true,
  productionEfficiency: true,
  dailyTargetEfficiency: true,
  rates: true,
  oee: true,
  evaluationLabel: true,
})

const checkAll = ref(true)
const isIndeterminate = ref(false)

// --- Dùng computed để columns phản xạ tức thì mỗi khi t(...) đổi ngôn ngữ ---
const columns = computed(() => [
  { key: 'time', label: t('productionHistory.table.time'), visible: columnVisibility.value.time },
  { key: 'line', label: t('productionHistory.table.line'), visible: columnVisibility.value.line },
  { key: 'machine', label: t('productionHistory.table.machine'), visible: columnVisibility.value.machine },
  { key: 'operatorName', label: t('productionHistory.table.operatorName'), visible: columnVisibility.value.operatorName },
  { key: 'responsibleLeader', label: t('productionHistory.table.responsibleLeader'), visible: columnVisibility.value.responsibleLeader },
  { key: 'partNumber', label: t('productionHistory.table.partNumber'), visible: columnVisibility.value.partNumber },
  { key: 'partName', label: t('productionHistory.table.partName'), visible: columnVisibility.value.partName },
  { key: 'processIds', label: t('productionHistory.table.processIds'), visible: columnVisibility.value.processIds },
  { key: 'runDowntime', label: t('productionHistory.table.runDowntime'), visible: columnVisibility.value.runDowntime },
  { key: 'downtimeReason', label: t('productionHistory.table.downtimeReason'), visible: columnVisibility.value.downtimeReason },
  { key: 'downtimeMinutes', label: t('productionHistory.table.downtimeMinutes'), visible: columnVisibility.value.downtimeMinutes },
  { key: 'dailyTargetDayQuantity', label: t('productionHistory.table.dailyTargetDayQuantity'), visible: columnVisibility.value.dailyTargetDayQuantity },
  { key: 'dailyTargetQuantity', label: t('productionHistory.table.dailyTargetQuantity'), visible: columnVisibility.value.dailyTargetQuantity },
  { key: 'inputGoodDefect', label: t('productionHistory.table.inputGoodDefect'), visible: columnVisibility.value.inputGoodDefect },
  { key: 'internalDefectQuantity', label: t('productionHistory.table.internalDefectQuantity'), visible: columnVisibility.value.internalDefectQuantity },
  { key: 'externalDefectQuantity', label: t('productionHistory.table.externalDefectQuantity'), visible: columnVisibility.value.externalDefectQuantity },
  { key: 'lotNo', label: t('productionHistory.table.lotNo'), visible: columnVisibility.value.lotNo },
  { key: 'responsibility', label: t('productionHistory.table.responsibility'), visible: columnVisibility.value.responsibility },
  { key: 'deductionPercent', label: t('productionHistory.table.deductionPercent'), visible: columnVisibility.value.deductionPercent },
  { key: 'productionEfficiency', label: t('productionHistory.table.productionEfficiency'), visible: columnVisibility.value.productionEfficiency },
  { key: 'dailyTargetEfficiency', label: t('productionHistory.table.dailyTargetEfficiency'), visible: columnVisibility.value.dailyTargetEfficiency },
  { key: 'rates', label: t('productionHistory.table.rates'), visible: columnVisibility.value.rates },
  { key: 'oee', label: t('productionHistory.table.oee'), visible: columnVisibility.value.oee },
  { key: 'evaluationLabel', label: t('productionHistory.table.evaluationLabel'), visible: columnVisibility.value.evaluationLabel },
])

const isColVisible = (key) => columnVisibility.value[key] !== false

const visibleColumnsCount = computed(() => Object.values(columnVisibility.value).filter(Boolean).length || 1)

const handleCheckAllChange = (val) => {
  Object.keys(columnVisibility.value).forEach(key => {
    columnVisibility.value[key] = val
  })
  isIndeterminate.value = false
  saveColumnVisibility()
}

const handleColumnChange = () => {
  const values = Object.values(columnVisibility.value)
  const checkedCount = values.filter(Boolean).length
  checkAll.value = checkedCount === values.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < values.length
  saveColumnVisibility()
}

const saveColumnVisibility = () => {
  localStorage.setItem('prod_history_cols', JSON.stringify(columnVisibility.value))
}

const loadColumnVisibility = () => {
  const saved = localStorage.getItem('prod_history_cols')
  if (saved) {
    try {
      const parsed = JSON.parse(saved)
      Object.keys(parsed).forEach(key => {
        if (columnVisibility.value[key] !== undefined) {
          columnVisibility.value[key] = parsed[key]
        }
      })
      const values = Object.values(columnVisibility.value)
      const checkedCount = values.filter(Boolean).length
      checkAll.value = checkedCount === values.length
      isIndeterminate.value = checkedCount > 0 && checkedCount < values.length
    } catch (e) {
      console.error(e)
    }
  }
}
// --- Logic tính toán bảng ---
const paginatedReports = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return todayReports.value.slice(start, end)
})

const historyReportRows = computed(() => paginatedReports.value.flatMap(buildReportTableRows))

const filteredMachineOptions = computed(() => {
  if (!filters.value.lineCode) return machines.value
  return machines.value.filter(machine => machine.lineCode === filters.value.lineCode)
})

function formatTime(iso) {
  if (!iso) return ''
  return iso.split('T')[1]?.substring(0, 5) || ''
}

function formatPercent(value) {
  return `${rate(value)}%`
}

function rate(value) {
  const percent = Number(value || 0) * 100
  return Number(percent.toFixed(2)).toString()
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString()
}

function formatFloorNumber(value) {
  return Math.floor(Number(value || 0)).toLocaleString()
}

function splitLotNos(value) {
  return String(value || '')
    .split(/\s*[;；]\s*/)
    .map(item => item.trim())
    .filter(Boolean)
}

function splitDowntimeReasons(value) {
  return String(value || '')
    .split(/\s*[;；]\s*/)
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

function distributeQuantity(total, count, index) {
  const safeTotal = Number(total || 0)
  if (count <= 1) return safeTotal
  const base = Math.floor(safeTotal / count)
  const remainder = safeTotal % count
  return base + (index < remainder ? 1 : 0)
}

function lotDisplayRows(row) {
  if (Array.isArray(row.lots) && row.lots.length) {
    return row.lots.map(lot => {
      const inputQuantity = Number(lot.inputQuantity || 0)
      const internalDefectQuantity = Number(lot.internalDefectQuantity || 0)
      const externalDefectQuantity = Number(lot.externalDefectQuantity || 0)
      const defectQuantity = lot.defectQuantity ?? (internalDefectQuantity + externalDefectQuantity)
      const goodQuantity = lot.goodQuantity ?? Math.max(inputQuantity - Number(defectQuantity || 0), 0)

      return {
        lotNo: lot.lotNo || '-',
        inputGoodDefect: `${inputQuantity} / ${goodQuantity} / ${defectQuantity}`,
        inputQuantity,
        goodQuantity,
        defectQuantity,
        internalDefectQuantity,
        externalDefectQuantity,
      }
    })
  }

  const lotNos = splitLotNos(row.lotNo)
  const count = Math.max(lotNos.length, 1)
  return Array.from({ length: count }, (_, index) => {
    const inputQuantity = distributeQuantity(row.inputQuantity, count, index)
    const internalDefectQuantity = distributeQuantity(row.internalDefectQuantity, count, index)
    const externalDefectQuantity = distributeQuantity(row.externalDefectQuantity, count, index)
    const defectQuantity = internalDefectQuantity + externalDefectQuantity
    const goodQuantity = Math.max(inputQuantity - defectQuantity, 0)

    return {
      lotNo: lotNos[index] || '-',
      inputGoodDefect: `${inputQuantity} / ${goodQuantity} / ${defectQuantity}`,
      inputQuantity,
      goodQuantity,
      defectQuantity,
      internalDefectQuantity,
      externalDefectQuantity,
    }
  })
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

function oeeClass(oee) {
  if (oee >= 0.85) return 'bg-emerald-100 text-emerald-700'
  if (oee >= 0.65) return 'bg-amber-100 text-amber-700'
  return 'bg-rose-100 text-rose-700'
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

async function loadMasterData() {
  const [productsRes, linesRes, machinesRes] = await Promise.all([
    masterApi.getProducts(),
    masterApi.getLines(),
    masterApi.getMachines(),
  ])
  products.value = productsRes.map(item => ({
    id: item.id,
    partNumber: item.code,
    partName: item.name,
  }))
  lines.value = linesRes.map(item => ({
    id: item.id,
    lineCode: item.code,
    description: item.name,
  }))
  machines.value = machinesRes.map(item => ({
    id: item.id,
    machineCode: item.machineCode,
    description: item.description,
    lineCode: item.lineCode || '',
  }))

  const processGroups = await Promise.all(
    products.value
      .filter(product => product.id)
      .map(product => masterApi.getProductProcesses(product.id).catch(() => []))
  )
  processGroups.forEach(mergeProcessNames)
}

function formatProcessIds(processIds) {
  if (!Array.isArray(processIds) || processIds.length === 0) return '-'
  return processIds.map(id => processNameById.value[id]).filter(Boolean).join(' + ') || '-'
}

async function loadReports() {
  currentPage.value = 1
  loading.value = true
  try {
    todayReports.value = await productionApi.today({
      reportDate: reportDate.value,
      lineCode: filters.value.lineCode,
      machineCode: filters.value.machineCode,
      partNumber: filters.value.partNumber,
      operatorName: filters.value.operatorName,
    })
  } catch (error) {
    ElMessage.error(`${t('productionHistory.messages.loadFailed')}: ${error.message}`)
  } finally {
    loading.value = false
  }
}

function onLineFilterChange() {
  const machineStillValid = filteredMachineOptions.value.some(machine => machine.machineCode === filters.value.machineCode)
  if (!machineStillValid) filters.value.machineCode = ''
  loadReports()
}

function resetFilters() {
  filters.value = {
    lineCode: '',
    machineCode: '',
    partNumber: '',
    operatorName: '',
  }
  loadReports()
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
    lotNo: row.lotNo || '',
    cycleTimeSeconds: row.cycleTimeSeconds,
    processIds: row.processIds || [],
    totalOperatingMinutes: row.totalOperatingMinutes,
    downtimeMinutes: row.downtimeMinutes,
    inputQuantity: row.inputQuantity,
    goodQuantity: row.goodQuantity,
    defectQuantity: row.defectQuantity,
    internalDefectQuantity: row.internalDefectQuantity,
    externalDefectQuantity: row.externalDefectQuantity,
    company: row.company || '',
    responsibleLeader: row.responsibleLeader || '',
    downtimeReason: row.downtimeReason || '',
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
      lotNo: editForm.value.lotNo,
      cycleTimeSeconds: Number(editForm.value.cycleTimeSeconds || 0),
      processIds: editForm.value.processIds || [],
      totalOperatingMinutes: Number(editForm.value.totalOperatingMinutes || 0),
      downtimeMinutes: Number(editForm.value.downtimeMinutes || 0),
      inputQuantity: Number(editForm.value.inputQuantity || 0),
      goodQuantity: Number(editForm.value.goodQuantity || 0),
      defectQuantity: Number(editForm.value.defectQuantity || 0),
      internalDefectQuantity: Number(editForm.value.internalDefectQuantity || 0),
      externalDefectQuantity: Number(editForm.value.externalDefectQuantity || 0),
      company: editForm.value.company,
      responsibleLeader: editForm.value.responsibleLeader,
      downtimeReason: editForm.value.downtimeReason,
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

onMounted(async () => {
  loadColumnVisibility()
  await loadMasterData()
  await loadReports()
})

watch([todayReports, pageSize], () => {
  const maxPage = Math.max(1, Math.ceil(todayReports.value.length / pageSize.value))
  if (currentPage.value > maxPage) currentPage.value = maxPage
})
</script>

<style scoped>
.history-excel-wrap {
  min-height: 180px;
  overflow: auto;
}

.history-excel-table {
  min-width: 100%;
  width: max-content;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 14px;
}

/* Thêm tính năng kéo dãn chiều ngang (Resize) cho tiêu đề cột */
.history-excel-table th {
  position: sticky;
  top: 0;
  z-index: 2;
  background: #1f4e79;
  color: #fff;
  font-weight: 800;
  line-height: 1.3;
  text-align: center;
  resize: horizontal;
  overflow: auto;
  min-width: 90px;
}

.history-excel-table th,
.history-excel-table td {
  border: 1px solid #111827;
  padding: 8px 6px;
  vertical-align: middle;
}

.history-excel-table td {
  background: #fff;
  color: #1f2937;
  font-weight: 600;
}

.reason-col {
  width: 280px;
}
.reason-col1 {
  width: 120px;
}
.reason-col2 {
  width: 80px;
}
.reason-col3 {
  width: 30px;
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