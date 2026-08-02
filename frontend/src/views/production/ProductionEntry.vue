<template>
  <div class="mx-auto w-full px-3 py-2 max-h-screen overflow-hidden">
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-3 items-start">
      <section class="lg:col-span-9 rounded-lg border border-slate-200 bg-white shadow-sm overflow-hidden">
        <!-- Header rút gọn -->
        <div class="border-b border-slate-200 bg-slate-50 px-4 py-2.5 flex items-center justify-between">
          <div>
            <span class="text-[10px] font-black uppercase tracking-wider text-sky-600 block leading-tight">{{ t('productionEntry.eyebrow') }}</span>
            <h2 class="text-lg font-black text-slate-900 leading-tight">{{ t('productionEntry.pageTitle') }}</h2>
          </div>
          <div class="rounded bg-amber-100 px-2.5 py-1 text-xs font-bold text-amber-800 border border-amber-300">
            {{ t('productionEntry.touchMode') }}
          </div>
        </div>
        <el-form :model="form" label-position="top" class="p-3 space-y-2.5">
          <!-- Dòng 1: Cấu hình chung (Chuyền, Máy, Ca, Công ty) -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-2.5">
            <el-form-item :label="t('productionEntry.line')" class="!mb-0">
              <el-select v-model="form.lineCode" size="default" class="w-full" :placeholder="t('productionEntry.selectLine')" @change="onLineChange">
                <el-option v-for="l in lines" :key="l.lineCode" :label="`${l.lineCode} - ${l.description}`" :value="l.lineCode" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('productionEntry.machine')" class="!mb-0">
              <el-select v-model="form.machineCode" size="default" class="w-full" :placeholder="t('productionEntry.selectMachine')">
                <el-option v-for="m in filteredMachines" :key="m.machineCode" :label="`${m.machineCode} - ${m.description}`" :value="m.machineCode" />
              </el-select>
            </el-form-item>

            <el-form-item :label="t('productionEntry.shift')" class="!mb-0">
              <el-select v-model="form.shiftName" size="default" class="w-full" :placeholder="t('productionEntry.selectShift')">
                <el-option v-for="s in shifts" :key="s.shiftName" :label="s.shiftName" :value="s.shiftName" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('productionEntry.company')" class="!mb-0">
              <el-input v-model="form.company" size="default" :placeholder="t('productionEntry.enterCompany')" />
            </el-form-item>
          </div>
          <div class="rounded-lg border border-slate-200 bg-slate-50 p-2.5">
            <div class="grid grid-cols-1 md:grid-cols-12 gap-2.5 items-end">
              <el-form-item :label="t('productionEntry.partNumber')" class="!mb-0 md:col-span-5">
                <el-select
                  v-model="form.partNumber"
                  size="default"
                  class="w-full"
                  filterable
                  :placeholder="t('productionEntry.scanBarcode')"
                  @change="onProductChange"
                >
                  <el-option v-for="p in products" :key="p.partNumber" :label="p.partNumber" :value="p.partNumber">
                    <span class="font-bold">{{ p.partNumber }}</span>
                    <span class="ml-2 text-xs text-slate-400">{{ p.partName }} ({{ p.cycleTimeSeconds }}s)</span>
                  </el-option>
                </el-select>
              </el-form-item>

              <el-form-item :label="t('productionEntry.partName')" class="!mb-0 md:col-span-4">
                <el-input v-model="form.partName" size="default" readonly placeholder="-" />
              </el-form-item>

              <el-form-item :label="t('productionEntry.cycleTime')" class="!mb-0 md:col-span-3">
                <el-input v-model="form.cycleTime" size="default" readonly placeholder="-" />
              </el-form-item>
            </div>
          </div>

          <!-- Dòng 3: Số liệu sản xuất thực tế -->
          <!-- Dòng 3: Thông số thời gian & Mục tiêu ca -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-2.5">
            <el-form-item :label="t('productionEntry.shiftTime')" class="!mb-0">
              <el-input 
                :model-value="formatNumber(selectedShiftMinutes, 0)" 
                size="default" 
                readonly 
                class="font-bold text-sky-700" 
              />
            </el-form-item>

            <el-form-item :label="t('productionEntry.dailyTarget')" class="!mb-0">
              <el-input 
                :model-value="formatNumber(dailyTargetPreview, 0)" 
                size="default" 
                readonly 
                class="font-bold text-indigo-700" 
              />
            </el-form-item>

            <el-form-item :label="t('productionEntry.actualMinutes')" class="!mb-0">
              <el-input 
                :model-value="formatNumber(actualOperatingMinutes, 0)" 
                size="default" 
                readonly 
                class="font-bold text-emerald-700" 
              />
            </el-form-item>

            <el-form-item :label="t('productionEntry.downtimeReason')" class="!mb-0">
              <el-select v-model="form.downtimeReason" size="default" class="w-full" :placeholder="t('productionEntry.downtimeReason')" filterable>
                <el-option v-for="r in downtimeReasons" :key="r" :label="r" :value="r" />
              </el-select>
            </el-form-item>
          </div>

          <!-- Dòng 4: Số lượng nhập & kết quả sản xuất -->
          <div class="mt-2.5 grid grid-cols-2 md:grid-cols-4 gap-2.5">
            <el-form-item :label="t('productionEntry.downtimeMinutes')" class="!mb-0">
              <el-input-number v-model="form.downtimeMinutes" :min="0" :max="1440" size="default" class="!w-full" controls-position="right" />
            </el-form-item>

            <el-form-item :label="t('productionEntry.inputQuantity')" class="!mb-0">
              <el-input-number v-model="form.inputQuantity" :min="0" :max="999999" size="default" class="!w-full" controls-position="right" />
            </el-form-item>

            <el-form-item :label="t('productionEntry.goodQuantity')" class="!mb-0">
              <el-input-number v-model="form.goodQuantity" :min="0" :max="999999" size="default" class="!w-full" controls-position="right" />
            </el-form-item>

            <el-form-item :label="t('productionEntry.defectQuantity')" class="!mb-0">
              <el-input :model-value="formatNumber(calculatedDefectQuantity, 0)" size="default" readonly class="font-bold text-rose-600" />
            </el-form-item>
          </div>

          <!-- Nút bấm Action -->
            <div class="pt-2 flex flex-wrap items-center gap-3">
              <el-button type="primary" size="large" class="flex-1 !h-10 text-base font-bold" :loading="saving" @click="saveReport">
                <Save class="mr-2 h-4 w-4" />
                {{ t('productionEntry.save') }}
              </el-button>
              <el-button type="danger" size="large" class="!h-10 text-base" :disabled="selectedReports.length === 0" @click="deleteSelectedReports">
                {{ t('productionEntry.deleteSelected') }}
              </el-button>
              <el-button type="info" size="large" class="!h-10 text-base" @click="searchMyReports">
                {{ t('productionEntry.search') }}
              </el-button>
            <el-button size="large" class="!h-10 text-base" @click="resetForm">
              <RotateCcw class="mr-2 h-4 w-4" />
              {{ t('productionEntry.reset') }}
            </el-button>
          </div>

          <div v-if="isOperator" class="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-4">
            <div class="mb-3 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h3 class="text-sm font-black text-slate-900">{{ t('productionEntry.myReportsTitle') }}</h3>
              </div>
              <el-button link type="primary" size="small" @click="loadMyReports">{{ t('productionEntry.refresh') }}</el-button>
            </div>
            <el-table
              type="selection"
              @selection-change="handleSelectionChange"
              @row-click="handleRowClick"
              :data="paginatedReports"
              stripe
              highlight-current-row
              row-key="id"
              style="width: 100%"
              size="small"
              v-loading="myReportsLoading"
            >
              <el-table-column prop="reportDate" :label="t('productionEntry.table.reportDate')" width="90" align="center" />
              <el-table-column prop="lineCode" :label="t('productionEntry.table.lineCode')" width="70" align="center" />
              <el-table-column prop="shiftName" :label="t('productionEntry.table.shiftName')" width="90" align="center" />
              <el-table-column prop="machineCode" :label="t('productionEntry.table.machineCode')" width="70" align="center" />
              <el-table-column prop="partNumber" :label="t('productionEntry.table.partNumber')" width="80" />
              <el-table-column prop="partName" :label="t('productionEntry.table.partName')" min-width="80" show-overflow-tooltip />
              <el-table-column prop="company" :label="t('productionEntry.table.company')" min-width="80" show-overflow-tooltip />
              <el-table-column prop="downtimeReason" :label="t('productionEntry.table.downtimeReason')" min-width="160" show-overflow-tooltip />
              <el-table-column prop="totalOperatingMinutes" :label="t('productionEntry.table.totalOperatingMinutes')" width="70" align="center" />
              <el-table-column prop="downtimeMinutes" :label="t('productionEntry.table.downtimeMinutes')" width="70" align="center" />
              <el-table-column prop="inputQuantity" :label="t('productionEntry.table.inputQuantity')" width="70" align="center" />
              <el-table-column prop="goodQuantity" :label="t('productionEntry.table.goodQuantity')" width="70" align="center" />
              <el-table-column prop="defectQuantity" :label="t('productionEntry.table.defectQuantity')" width="70" align="center" />
              <el-table-column prop="shiftStandardTimeMinutes" :label="t('productionEntry.table.shiftStandardTimeMinutes')" width="70" align="center" />
              <el-table-column prop="dailyTargetQuantity" :label="t('productionEntry.table.dailyTargetQuantity')" width="70" align="center" />
              <el-table-column prop="productionEfficiency" :label="t('productionEntry.table.productionEfficiency')" width="70" align="center" />
              <el-table-column prop="availabilityRate" :label="t('productionEntry.table.availabilityRate')" width="70" align="center" />
              <el-table-column prop="performanceRate" :label="t('productionEntry.table.performanceRate')" width="70" align="center" />
              <el-table-column prop="qualityRate" :label="t('productionEntry.table.qualityRate')" width="70" align="center" />
              <el-table-column prop="oee" :label="t('productionEntry.table.oee')" width="90" align="center" />
              <el-table-column prop="evaluationLabel" :label="t('productionEntry.table.evaluationLabel')" width="70" align="center" />
              <el-table-column prop="createdBy" :label="t('productionEntry.table.createdBy')" width="70" align="center" />
            </el-table>
            <div class="flex justify-end px-4 py-3 border-t border-slate-200 bg-slate-50">
              <el-pagination
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="myReports.length"
                layout="prev, pager, next"
                background
              />
            </div>
          </div>
        </el-form>
      </section>

      <!-- KHỐI KẾT QUẢ/OEE (RIGHT COLUMN - 3 Cols) -->
      <aside class="lg:col-span-3">
        <div class="rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <h3 class="text-xs font-black uppercase tracking-wider text-slate-500 border-b pb-1.5 mb-2">{{ t('productionEntry.results') }}</h3>
          
          <div v-if="showResult" class="space-y-1.5">
            <div v-for="item in oeeCards" :key="item.key" class="rounded border px-2.5 py-1.5 flex items-center justify-between" :class="item.cardClass">
              <span class="text-[11px] font-bold text-slate-600 uppercase">{{ item.label }}</span>
              <span class="text-base font-black" :class="item.textClass">{{ item.value }}</span>
            </div>
            
            <div class="mt-2 rounded py-1.5 text-center text-xs font-black uppercase tracking-wide" :class="resultBadgeClass">
              {{ resultLabel }}
            </div>
          </div>

          <div v-else class="py-12 border-2 border-dashed border-slate-200 rounded-lg text-center text-xs font-semibold text-slate-400">
            {{ t('productionEntry.noResult') }}
          </div>

          <div class="mt-4 rounded-lg border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
            <h4 class="font-bold mb-2 text-slate-900">{{ t('productionEntry.instructions.title') }}</h4>
            <ul class="list-disc list-inside space-y-1">
              <li>{{ t('productionEntry.instructions.edit') }}</li>
              <li>{{ t('productionEntry.instructions.delete') }}</li>
              <li>{{ t('productionEntry.instructions.search') }}</li>
            </ul>
          </div>
        </div>
      </aside>

    </div>
  </div>
</template>

<script setup>
import { computed, inject, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { RotateCcw, Save } from 'lucide-vue-next'
import { masterApi, productionApi } from '@/services/api'
import { formatPercent, getOeeColor } from '@/composables/useOee'
import { useI18n } from '@/i18n'

const { t } = useI18n()

const products = ref([])
const lines = ref([])
const machines = ref([])
const shifts = ref([])
const saving = ref(false)
const showResult = ref(false)
const result = ref(null)
const currentUser = inject('currentUser', computed(() => ({ name: 'Người đăng nhập', fullName: 'Người dùng' })))
const myReports = ref([])
const myReportsLoading = ref(false)
const selectedReports = ref([])
const editedReportId = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)

const paginatedReports = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return myReports.value.slice(start, end)
})

function localTodayString() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const form = ref({
  reportDate: localTodayString(),
  lineCode: '',
  machineCode: '',
  shiftName: '',
  partNumber: '',
  partName: '',
  cycleTime: '',
  downtimeMinutes: 30,
  inputQuantity: 0,
  goodQuantity: 0,
  company: 'SWICO',
  downtimeReason: '',
})

const downtimeReasons = [
  '砂輪用盡 / Hết đá',
  '更換砂輪（針對磨床組） / Thay đá ( đối với tổ Mài)',
  '其他 / Khác',
]

const filteredMachines = computed(() => machines.value)

const selectedShiftMinutes = computed(() => {
  const shift = shifts.value.find(s => s.shiftName === form.value.shiftName)
  return shift?.standardTimeMinutes ?? null
})

const actualOperatingMinutes = computed(() => {
  const shiftMinutes = Number(selectedShiftMinutes.value || 0)
  const downtime = Number(form.value.downtimeMinutes || 0)
  if (shiftMinutes <= 0) return 0
  return Math.max(shiftMinutes - downtime, 0)
})

const calculatedDefectQuantity = computed(() => {
  const input = Number(form.value.inputQuantity || 0)
  const good = Number(form.value.goodQuantity || 0)
  return Math.max(input - good, 0)
})

const dailyTargetPreview = computed(() => {
  const actualMinutes = Number(actualOperatingMinutes.value || 0)
  const cycleTime = Number(form.value.cycleTime || 0)
  if (actualMinutes <= 0 || cycleTime <= 0) return null
  return (actualMinutes * 60) / cycleTime
})

const isOperator = computed(() => currentUser.value?.role === 'ROLE_OPERATOR')

async function loadMyReports(params = {}) {
  if (!isOperator.value) {
    myReports.value = []
    return
  }

  myReportsLoading.value = true
  try {
    myReports.value = await productionApi.myReports(params)
  } catch (error) {
    console.warn('Failed to load operator reports', error)
    myReports.value = []
  } finally {
    myReportsLoading.value = false
  }
}

function handleSelectionChange(selection) {
  selectedReports.value = selection || []
}

function handleRowClick(report) {
  if (!report) return
  selectedReports.value = [report]
  populateFormForEdit(report)
  editedReportId.value = report.id
  ElMessage.info(t('productionEntry.messages.selectedForEdit'))
}

function populateFormForEdit(report) {
  form.value.reportDate = report.reportDate || localTodayString()
  form.value.lineCode = report.lineCode || ''
  form.value.machineCode = report.machineCode || ''
  form.value.shiftName = report.shiftName || ''
  form.value.partNumber = report.partNumber || ''
  form.value.partName = report.partName || ''
  form.value.cycleTime = report.cycleTimeSeconds || ''
  form.value.downtimeMinutes = report.downtimeMinutes ?? 30
  form.value.inputQuantity = report.inputQuantity ?? 0
  form.value.goodQuantity = report.goodQuantity ?? 0
  form.value.company = report.company || 'SWICO'
  form.value.downtimeReason = report.downtimeReason || ''
}

async function editSelectedReport() {
  if (!selectedReports.value.length) {
    ElMessage.warning(t('productionEntry.messages.selectOneReportForEdit'))
    return
  }
  const report = selectedReports.value[0]
  populateFormForEdit(report)
  editedReportId.value = report.id
  ElMessage.info(t('productionEntry.messages.selectedForEdit'))
}

async function deleteSelectedReports() {
  if (!selectedReports.value.length) {
    ElMessage.warning(t('productionEntry.messages.selectOneReportToDelete'))
    return
  }
  const confirmed = window.confirm(t('productionEntry.messages.deleteConfirm'))
  if (!confirmed) return

  try {
    const ids = selectedReports.value.map(item => item.id)
    await productionApi.deleteReports(ids)
    ElMessage.success(t('productionEntry.messages.deleteSuccess'))
    selectedReports.value = []
    await loadMyReports()
  } catch (error) {
    ElMessage.error(t('productionEntry.messages.deleteFailed', { error: error.message }))
  }
}

function searchMyReports() {
  loadMyReports({
    reportDate: form.value.reportDate,
    lineCode: form.value.lineCode,
    shiftName: form.value.shiftName,
    partNumber: form.value.partNumber,
  })
}

function onProductChange(partNumber) {
  const product = products.value.find(p => p.partNumber === partNumber)
  if (product) {
    form.value.partName = product.partName
    form.value.cycleTime = product.cycleTimeSeconds
  }
}

function onLineChange(lineCode) {
  if (machines.value.length) {
    form.value.machineCode = machines.value[0].machineCode
  }
}

function resetForm() {
  form.value.partNumber = ''
  form.value.partName = ''
  form.value.cycleTime = ''
  form.value.downtimeMinutes = 30
  form.value.inputQuantity = 0
  form.value.goodQuantity = 0
  showResult.value = false
  result.value = null
}

async function loadInitialData() {
  try {
    const [productsRes, linesRes, machinesRes, shiftsRes] = await Promise.all([
      masterApi.getProducts(),
      masterApi.getLines(),
      masterApi.getMachines(),
      masterApi.getShifts(),
    ])

    products.value = productsRes.map(item => ({
      id: item.id,
      partNumber: item.code,
      partName: item.name,
      cycleTimeSeconds: item.cycleTimeSeconds,
    }))
    lines.value = linesRes.map(item => ({ id: item.id, lineCode: item.code, description: item.name }))
    machines.value = machinesRes.map(item => ({ id: item.id, machineCode: item.machineCode, description: item.description }))
    shifts.value = shiftsRes.map(item => ({ id: item.id, shiftName: item.name, standardTimeMinutes: item.standardTimeMinutes }))

    if (!form.value.lineCode && lines.value.length) form.value.lineCode = lines.value[0].lineCode
    if (!form.value.machineCode && machines.value.length) {
      const machine = machines.value.find(m => m.lineCode === form.value.lineCode) || machines.value[0]
      form.value.machineCode = machine?.machineCode || ''
    }
    if (!form.value.shiftName && shifts.value.length) form.value.shiftName = shifts.value[0].shiftName
    await loadMyReports()
  } catch (error) {
    ElMessage.error(`${t('productionEntry.messages.loadFailed')}: ${error.message}`)
  }
}

async function saveReport() {
  if (!form.value.partNumber) {
    ElMessage.warning(t('productionEntry.messages.selectProduct'))
    return
  }
  saving.value = true
  try {
    const payload = {
      reportDate: form.value.reportDate,
      lineCode: form.value.lineCode,
      shiftName: form.value.shiftName,
      machineCode: form.value.machineCode,
      partNumber: form.value.partNumber,
      partName: form.value.partName,
      cycleTimeSeconds: Number(form.value.cycleTime),
      totalOperatingMinutes: Number(actualOperatingMinutes.value),
      downtimeMinutes: Number(form.value.downtimeMinutes),
      inputQuantity: Number(form.value.inputQuantity),
      goodQuantity: Number(form.value.goodQuantity),
      defectQuantity: Number(calculatedDefectQuantity.value),
      company: form.value.company,
      downtimeReason: form.value.downtimeReason,
    }

    const saved = editedReportId.value
      ? await productionApi.update(editedReportId.value, payload)
      : await productionApi.create(payload)

    result.value = saved
    showResult.value = true
    ElMessage.success(t('productionEntry.messages.saveSuccess'))
    editedReportId.value = null
    selectedReports.value = []
    await loadMyReports()
  } catch (error) {
    ElMessage.error(`${t('productionEntry.messages.saveFailed')}: ${error.message}`)
  } finally {
    saving.value = false
  }
}

const oeeCards = computed(() => {
  if (!result.value) return []
  const r = result.value
  const oeeColor = getOeeColor(Number(r.oee || 0))
  return [
    { key: 'oee', label: 'OEE', value: formatPercent(Number(r.oee || 0)), textClass: oeeColor.text, cardClass: 'border-slate-200 bg-slate-50' },
    { key: 'prod-eff', label: t('productionEntry.productionEfficiency'), value: formatPercent(Number(r.productionEfficiency || 0)), textClass: 'text-amber-600', cardClass: 'border-amber-100 bg-amber-50' },
    { key: 'a', label: t('reports.dashboard.sections.availability'), value: formatPercent(Number(r.availabilityRate || 0)), textClass: 'text-sky-600', cardClass: 'border-sky-100 bg-sky-50' },
    { key: 'p', label: t('reports.dashboard.sections.performance'), value: formatPercent(Number(r.performanceRate || 0)), textClass: 'text-indigo-600', cardClass: 'border-indigo-100 bg-indigo-50' },
    { key: 'q', label: t('reports.dashboard.sections.quality'), value: formatPercent(Number(r.qualityRate || 0)), textClass: 'text-emerald-600', cardClass: 'border-emerald-100 bg-emerald-50' },
    { key: 'signature', label: t('productionEntry.createdBy'), value: currentUser.value?.fullName || currentUser.value?.name || '-', textClass: 'text-slate-700', cardClass: 'border-slate-200 bg-white' },
  ]
})

function formatNumber(value, fractionDigits = 0) {
  if (value === null || value === undefined || value === '') return '-'
  return Number(value).toLocaleString('vi-VN', {
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  })
}

const resultLabel = computed(() => (result.value ? result.value.evaluationLabel : ''))
const resultBadgeClass = computed(() => {
  const oee = Number(result.value?.oee || 0)
  if (oee >= 0.85) return 'bg-emerald-100 text-emerald-800'
  if (oee >= 0.65) return 'bg-amber-100 text-amber-800'
  return 'bg-rose-100 text-rose-800'
})

watch(() => form.value.lineCode, lineCode => {
  if (!lineCode) return
  const machine = machines.value.find(m => m.lineCode === lineCode)
  if (machine) form.value.machineCode = machine.machineCode
})

watch([() => form.value.partNumber, products], ([partNumber]) => {
  if (!partNumber) return
  const product = products.value.find(p => p.partNumber === partNumber)
  if (!product) return
  form.value.partName = product.partName
  form.value.cycleTime = product.cycleTimeSeconds
}, { immediate: true })

onMounted(loadInitialData)
</script>

<style scoped>
/* Reset Element Plus compact height */
:deep(.el-form-item__label) {
  font-size: 0.8rem !important;
  font-weight: 700 !important;
  margin-bottom: 0.2rem !important;
  line-height: 1.2 !important;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  min-height: 2.125rem !important;
  height: 2.125rem !important;
  padding-left: 0.5rem !important;
  padding-right: 0.5rem !important;
}

/* tô xám toàn bộ các ô readonly (kể cả ô input wrapper và khung bên trong) */
:deep(.el-input.is-disabled .el-input__wrapper),
:deep(.el-input .el-input__inner[readonly]),
:deep(.el-input__wrapper:has(input[readonly])) {
  background-color: #e2e8f0 !important; /* Màu xám slate-200 rõ ràng */
  border-color: #cbd5e1 !important;
  cursor: not-allowed;
}

:deep(.el-input__inner) {
  font-size: 0.925rem !important;
  font-weight: 700 !important;
}

/* Đảm bảo chữ trong ô readonly vẫn đậm và dễ nhìn */
:deep(.el-input__inner[readonly]) {
  color: #334155 !important; /* Màu chữ xám đậm slate-700 */
}

:deep(.el-input-number) {
  width: 100% !important;
}
</style>