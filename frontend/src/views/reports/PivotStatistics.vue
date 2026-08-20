<template>
  <div class="space-y-5">
    <PageHeader :eyebrow="l('eyebrow')" :title="l('title')" />

    <div class="page-card p-5">
      <el-form :model="filters" label-position="top">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-6">
          <el-form-item :label="l('dateFrom')">
            <el-date-picker v-model="filters.dateFrom" type="date" value-format="YYYY-MM-DD" class="!w-full" />
          </el-form-item>
          <el-form-item :label="l('dateTo')">
            <el-date-picker v-model="filters.dateTo" type="date" value-format="YYYY-MM-DD" class="!w-full" />
          </el-form-item>
          <el-form-item :label="l('line')">
            <el-select v-model="filters.lineCode" clearable :placeholder="l('all')" class="!w-full">
              <el-option v-for="line in lines" :key="line.lineCode" :label="line.lineCode" :value="line.lineCode" />
            </el-select>
          </el-form-item>
          <el-form-item :label="l('shift')">
            <el-select v-model="filters.shiftName" clearable :placeholder="l('all')" class="!w-full">
              <el-option v-for="shift in shifts" :key="shift.shiftName" :label="shift.shiftName" :value="shift.shiftName" />
            </el-select>
          </el-form-item>
          <el-form-item :label="l('partNumber')">
            <el-input v-model="filters.partNumber" clearable placeholder="PN-..." />
          </el-form-item>
          <el-form-item :label="l('operatorName')">
            <el-input v-model="filters.operatorName" clearable :placeholder="l('operatorPlaceholder')" />
          </el-form-item>
        </div>
        <div class="mt-2 flex flex-col gap-2 md:flex-row md:justify-end">
          <el-button @click="resetFilters">{{ l('reset') }}</el-button>
          <el-button type="primary" :loading="loading" @click="search">{{ l('search') }}</el-button>
        </div>
      </el-form>
    </div>

    <div class="page-card overflow-hidden">
      <el-tabs v-model="activeTab" class="pivot-tabs">
        <el-tab-pane :label="l('quantityTab')" name="quantity">
          <div class="table-scroll">
            <table class="pivot-table">
              <thead>
                <tr>
                  <th class="label-col">{{ l('rowLabels') }}</th>
                  <th>{{ l('completedQuantity') }}</th>
                  <th>{{ l('goodQuantity') }}</th>
                  <th>{{ l('internalDefectQuantity') }}</th>
                  <th>{{ l('externalDefectQuantity') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in quantityRows" :key="row.key" :class="rowClass(row)">
                  <td class="label-col" :style="{ paddingLeft: `${8 + row.level * 20}px` }">
                    <span v-if="row.level < 2" class="tree-box">−</span>
                    <span>{{ row.label }}</span>
                  </td>
                  <td>{{ row.showValues ? formatNumber(row.inputQuantity) : '' }}</td>
                  <td>{{ row.showValues ? formatNumber(row.goodQuantity) : '' }}</td>
                  <td>{{ row.showValues ? formatNumber(row.internalDefectQuantity) : '' }}</td>
                  <td>{{ row.showValues ? formatNumber(row.externalDefectQuantity) : '' }}</td>
                </tr>
                <tr class="grand-row">
                  <td class="label-col">{{ l('grandTotal') }}</td>
                  <td>{{ formatNumber(quantityGrandTotal.inputQuantity) }}</td>
                  <td>{{ formatNumber(quantityGrandTotal.goodQuantity) }}</td>
                  <td>{{ formatNumber(quantityGrandTotal.internalDefectQuantity) }}</td>
                  <td>{{ formatNumber(quantityGrandTotal.externalDefectQuantity) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="l('rateTab')" name="rates">
          <div class="table-scroll">
            <table class="pivot-table">
              <thead>
                <tr>
                  <th class="label-col">{{ l('rowLabels') }}</th>
                  <th>{{ l('avgProductionEfficiency') }}</th>
                  <th>{{ l('avgAvailabilityRate') }}</th>
                  <th>{{ l('avgPerformanceRate') }}</th>
                  <th>{{ l('avgQualityRate') }}</th>
                  <th>{{ l('avgOee') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in rateRows" :key="row.key" :class="rowClass(row)">
                  <td class="label-col" :style="{ paddingLeft: `${8 + row.level * 20}px` }">
                    <span v-if="row.level < 2" class="tree-box">−</span>
                    <span>{{ row.label }}</span>
                  </td>
                  <td>{{ formatPercent(row.productionEfficiency) }}</td>
                  <td>{{ formatPercent(row.availabilityRate) }}</td>
                  <td>{{ formatPercent(row.performanceRate) }}</td>
                  <td>{{ formatPercent(row.qualityRate) }}</td>
                  <td>{{ formatPercent(row.oee) }}</td>
                </tr>
                <tr class="grand-row">
                  <td class="label-col">{{ l('grandTotal') }}</td>
                  <td>{{ formatPercent(rateGrandTotal.productionEfficiency) }}</td>
                  <td>{{ formatPercent(rateGrandTotal.availabilityRate) }}</td>
                  <td>{{ formatPercent(rateGrandTotal.performanceRate) }}</td>
                  <td>{{ formatPercent(rateGrandTotal.qualityRate) }}</td>
                  <td>{{ formatPercent(rateGrandTotal.oee) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-tab-pane>
      </el-tabs>
      <div class="border-t border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-500">
        {{ l('reportCount') }}: {{ reports.length }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi, productionApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()

const l = (key, params) => t(`reports.pivotStatistics.${key}`, params)
const today = new Date().toISOString().slice(0, 10)
const firstDayOfMonth = today.slice(0, 8) + '01'

const activeTab = ref('quantity')
const loading = ref(false)
const reports = ref([])
const lines = ref([])
const shifts = ref([])
const processNameById = ref({})
const filters = ref({
  dateFrom: firstDayOfMonth,
  dateTo: today,
  lineCode: '',
  shiftName: '',
  partNumber: '',
  operatorName: '',
})

function makeTotals() {
  return {
    inputQuantity: 0,
    goodQuantity: 0,
    internalDefectQuantity: 0,
    externalDefectQuantity: 0,
    productionEfficiency: 0,
    availabilityRate: 0,
    performanceRate: 0,
    qualityRate: 0,
    oee: 0,
    count: 0,
  }
}

function addReport(totals, report) {
  totals.inputQuantity += Number(report.inputQuantity || 0)
  totals.goodQuantity += Number(report.goodQuantity || 0)
  totals.internalDefectQuantity += Number(report.internalDefectQuantity || 0)
  totals.externalDefectQuantity += Number(report.externalDefectQuantity || 0)
  totals.productionEfficiency += Number(report.productionEfficiency || 0)
  totals.availabilityRate += Number(report.availabilityRate || 0)
  totals.performanceRate += Number(report.performanceRate || 0)
  totals.qualityRate += Number(report.qualityRate || 0)
  totals.oee += Number(report.oee || 0)
  totals.count += 1
}

function averages(totals) {
  const divisor = totals.count || 1
  return {
    productionEfficiency: totals.productionEfficiency / divisor,
    availabilityRate: totals.availabilityRate / divisor,
    performanceRate: totals.performanceRate / divisor,
    qualityRate: totals.qualityRate / divisor,
    oee: totals.oee / divisor,
  }
}

function dateLabel(value) {
  if (!value) return '-'
  const date = new Date(`${value}T00:00:00`)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString('en-US', { day: 'numeric', month: 'short' }).replace(' ', '-')
}

function processLabel(report) {
  if (!Array.isArray(report.processIds) || report.processIds.length === 0) {
    return l('unknownProcess')
  }
  return report.processIds
    .map(id => processNameById.value[id])
    .filter(Boolean)
    .join(' + ') || l('unknownProcess')
}

function operatorLabel(report) {
  return report.operatorName || report.createdBy || l('unknownOperator')
}

function leaderLabel(report) {
  return report.responsibleLeader || l('unknownLeader')
}

function buildGroupedRows(items, firstKey, secondKey) {
  const firstMap = new Map()
  items.forEach(report => {
    const firstLabel = firstKey(report)
    const secondLabel = secondKey(report)
    const dayLabel = dateLabel(report.reportDate)
    if (!firstMap.has(firstLabel)) {
      firstMap.set(firstLabel, { label: firstLabel, totals: makeTotals(), children: new Map() })
    }
    const first = firstMap.get(firstLabel)
    addReport(first.totals, report)
    if (!first.children.has(secondLabel)) {
      first.children.set(secondLabel, { label: secondLabel, totals: makeTotals(), children: new Map() })
    }
    const second = first.children.get(secondLabel)
    addReport(second.totals, report)
    if (!second.children.has(dayLabel)) {
      second.children.set(dayLabel, { label: dayLabel, totals: makeTotals() })
    }
    addReport(second.children.get(dayLabel).totals, report)
  })

  return [...firstMap.values()].flatMap(first => [
    toQuantityRow(first, 0, `first-${first.label}`, false),
    ...[...first.children.values()].flatMap(second => [
      toQuantityRow(second, 1, `second-${first.label}-${second.label}`),
      ...[...second.children.values()].map(day => toQuantityRow(day, 2, `day-${first.label}-${second.label}-${day.label}`)),
    ]),
  ])
}

function toQuantityRow(node, level, key, showValues = true) {
  return {
    key,
    label: node.label,
    level,
    showValues,
    inputQuantity: node.totals.inputQuantity,
    goodQuantity: node.totals.goodQuantity,
    internalDefectQuantity: node.totals.internalDefectQuantity,
    externalDefectQuantity: node.totals.externalDefectQuantity,
  }
}

function toRateRow(node, level, key) {
  return {
    key,
    label: node.label,
    level,
    ...averages(node.totals),
  }
}

function buildRateRows(items) {
  const firstMap = new Map()
  items.forEach(report => {
    const firstLabel = leaderLabel(report)
    const secondLabel = operatorLabel(report)
    const dayLabel = dateLabel(report.reportDate)
    if (!firstMap.has(firstLabel)) {
      firstMap.set(firstLabel, { label: firstLabel, totals: makeTotals(), children: new Map() })
    }
    const first = firstMap.get(firstLabel)
    addReport(first.totals, report)
    if (!first.children.has(secondLabel)) {
      first.children.set(secondLabel, { label: secondLabel, totals: makeTotals(), children: new Map() })
    }
    const second = first.children.get(secondLabel)
    addReport(second.totals, report)
    if (!second.children.has(dayLabel)) {
      second.children.set(dayLabel, { label: dayLabel, totals: makeTotals() })
    }
    addReport(second.children.get(dayLabel).totals, report)
  })

  return [...firstMap.values()].flatMap(first => [
    toRateRow(first, 0, `first-${first.label}`),
    ...[...first.children.values()].flatMap(second => [
      toRateRow(second, 1, `second-${first.label}-${second.label}`),
      ...[...second.children.values()].map(day => toRateRow(day, 2, `day-${first.label}-${second.label}-${day.label}`)),
    ]),
  ])
}

const quantityRows = computed(() => buildGroupedRows(reports.value, report => report.partNumber || '-', processLabel))
const rateRows = computed(() => buildRateRows(reports.value))

const quantityGrandTotal = computed(() => {
  const totals = makeTotals()
  reports.value.forEach(report => addReport(totals, report))
  return totals
})

const rateGrandTotal = computed(() => averages(quantityGrandTotal.value))

function rowClass(row) {
  return {
    'group-row': row.level === 0,
    'subgroup-row': row.level === 1,
    'detail-row': row.level === 2,
  }
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString()
}

function formatPercent(value) {
  return `${(Number(value || 0) * 100).toFixed(2)}%`
}

function resetFilters() {
  filters.value = {
    dateFrom: firstDayOfMonth,
    dateTo: today,
    lineCode: '',
    shiftName: '',
    partNumber: '',
    operatorName: '',
  }
  search()
}

async function loadOptions() {
  const [lineRes, shiftRes, productsRes] = await Promise.all([
    masterApi.getLines(),
    masterApi.getShifts(),
    masterApi.getProducts(),
  ])
  lines.value = (lineRes || []).map(item => ({ lineCode: item.code ?? item.lineCode ?? '', description: item.name ?? item.description ?? '' }))
  shifts.value = (shiftRes || []).map(item => ({ shiftName: item.name ?? item.shiftName ?? '' }))
  const processGroups = await Promise.all(
    (productsRes || [])
      .filter(product => product.id)
      .map(product => masterApi.getProductProcesses(product.id).catch(() => []))
  )
  processNameById.value = processGroups.flat().reduce((map, process) => {
    if (process?.id) map[process.id] = process.processCode || process.process || String(process.id)
    return map
  }, {})
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
  } catch (error) {
    ElMessage.error(`${l('loadFailed')}: ${error.message}`)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await loadOptions()
    await search()
  } catch (error) {
    ElMessage.error(`${l('loadFailed')}: ${error.message}`)
    loading.value = false
  }
})
</script>

<style scoped>
.pivot-tabs {
  padding: 0 16px 12px;
}

.table-scroll {
  overflow-x: auto;
  border: 1px solid #9fb5cf;
}

.pivot-table {
  width: 100%;
  min-width: 980px;
  border-collapse: collapse;
  background: #fff;
  font-size: 14px;
}

.pivot-table th {
  position: sticky;
  top: 0;
  z-index: 1;
  border-bottom: 1px solid #9fb5cf;
  background: #dbe8f4;
  color: #020617;
  font-weight: 900;
  text-align: left;
  white-space: nowrap;
}

.pivot-table th,
.pivot-table td {
  padding: 4px 8px;
}

.pivot-table td:not(.label-col),
.pivot-table th:not(.label-col) {
  text-align: right;
}

.label-col {
  min-width: 300px;
  text-align: left;
}

.group-row,
.subgroup-row {
  font-weight: 900;
}

.group-row td {
  border-top: 1px solid #9fb5cf;
}

.detail-row td {
  font-weight: 500;
}

.grand-row {
  border-top: 1px solid #9fb5cf;
  background: #dbe8f4;
  font-weight: 900;
}

.tree-box {
  display: inline-flex;
  width: 12px;
  height: 12px;
  margin-right: 4px;
  align-items: center;
  justify-content: center;
  border: 1px solid #94a3b8;
  border-radius: 2px;
  color: #475569;
  font-size: 11px;
  line-height: 1;
}
</style>
