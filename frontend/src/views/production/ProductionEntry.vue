<template>
  <div class="mx-auto w-[100%] px-4">
    <div class="grid gap-3 lg:grid-cols-[1fr_300px]">
      <section class="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        <div class="border-b border-slate-200 bg-white px-5 py-4">
          <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p class="text-xs font-black uppercase tracking-wider text-sky-600">{{ t('productionEntry.eyebrow') }}</p>
              <h2 class="text-2xl font-black text-slate-900">{{ t('productionEntry.pageTitle') }}</h2>
              <p class="mt-1 text-sm text-slate-500">{{ t('productionEntry.pageSubtitle') }}</p>
            </div>
            <div class="rounded-lg border border-amber-200 bg-amber-50 px-4 py-2 text-sm font-bold text-amber-800">
              {{ t('productionEntry.touchMode') }}
            </div>
          </div>
        </div>

        <el-form :model="form" label-position="top" class="p-5">
          <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
            <el-form-item :label="t('productionEntry.line')" class="!mb-0">
              <el-select v-model="form.lineCode" size="large" class="w-full touch-input" :placeholder="t('productionEntry.selectLine')" @change="onLineChange">
                <el-option v-for="l in lines" :key="l.lineCode" :label="`${l.lineCode} - ${l.description}`" :value="l.lineCode" />
              </el-select>
            </el-form-item>

            <el-form-item :label="t('productionEntry.machine')" class="!mb-0">
              <el-select v-model="form.machineCode" size="large" class="w-full touch-input" :placeholder="t('productionEntry.selectMachine')">
                <el-option v-for="m in filteredMachines" :key="m.machineCode" :label="`${m.machineCode} - ${m.description}`" :value="m.machineCode" />
              </el-select>
            </el-form-item>

            <el-form-item :label="t('productionEntry.shift')" class="!mb-0">
              <el-select v-model="form.shiftName" size="large" class="w-full touch-input" :placeholder="t('productionEntry.selectShift')">
                <el-option v-for="s in shifts" :key="s.shiftName" :label="s.shiftName" :value="s.shiftName" />
              </el-select>
            </el-form-item>
          </div>

          <div class="mt-4 grid grid-cols-1 gap-4 md:grid-cols-3">
            <el-form-item :label="t('productionEntry.company')" class="!mb-0">
              <el-input v-model="form.company" size="medium" class="touch-input" :placeholder="t('productionEntry.enterCompany')" />
            </el-form-item>
            <el-form-item :label="t('productionEntry.shiftTime')" class="!mb-0">
              <el-input :model-value="formatNumber(selectedShiftMinutes, 0)" size="small" class="touch-input" readonly />
            </el-form-item>
            <el-form-item :label="t('productionEntry.dailyTarget')" class="!mb-0">
              <el-input :model-value="formatNumber(dailyTargetPreview, 2)" size="small" class="touch-input" readonly />
            </el-form-item>
          </div>

          <div class="mt-5 rounded-lg border border-slate-200 bg-slate-50 p-4">
            <el-form-item :label="t('productionEntry.partNumber')" class="!mb-0">
                <el-select
                v-model="form.partNumber"
                size="large"
                class="w-full touch-input"
                filterable
                :placeholder="t('productionEntry.scanBarcode')"
                @change="onProductChange"
              >
                <el-option v-for="p in products" :key="p.partNumber" :label="p.partNumber" :value="p.partNumber">
                  <span class="font-bold">{{ p.partNumber }}</span>
                  <span class="ml-2 text-slate-400">{{ p.partName }} - C/T {{ p.cycleTimeSeconds }}s</span>
                </el-option>
              </el-select>
            </el-form-item>

            <div class="mt-4 grid grid-cols-1 gap-2 md:grid-cols-12">
              <el-form-item :label="t('productionEntry.partName')" class="!mb-0 md:col-span-8">
                <el-input v-model="form.partName" size="small" class="touch-input" readonly placeholder="-" />
              </el-form-item>
              <el-form-item :label="t('productionEntry.cycleTime')" class="!mb-0 md:col-span-4">
                <el-input v-model="form.cycleTime" size="small" class="touch-input" readonly placeholder="-" />
              </el-form-item>
            </div>
          </div>

          <div class="mt-5">
            <h3 class="mb-3 text-base font-black text-slate-800">{{ t('productionEntry.rawData') }}</h3>

            <!-- Row 1: two inputs side by side -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <el-form-item :label="t('productionEntry.downtimeReason')" class="!mb-0">
                <el-select v-model="form.downtimeReason" size="large" class="w-full touch-input" placeholder="Chọn lý do dừng" filterable>
                  <el-option v-for="r in downtimeReasons" :key="r" :label="r" :value="r" />
                </el-select>
              </el-form-item>

                <el-form-item :label="t('productionEntry.downtimeMinutes')" class="!mb-0">
                  <el-input-number v-model="form.downtimeMinutes" :min="0" :max="1440" size="small" class="!w-full touch-input compact-number" controls-position="right" />
                </el-form-item>
            </div>

            <!-- Row 2: four inputs in one line -->
            <div class="mt-4 grid grid-cols-1 sm:grid-cols-4 gap-4">
              <el-form-item :label="t('productionEntry.inputQuantity')" class="!mb-0">
                <el-input-number v-model="form.inputQuantity" :min="0" :max="999999" size="large" class="!w-full touch-input" controls-position="right" />
              </el-form-item>

              <el-form-item :label="t('productionEntry.goodQuantity')" class="!mb-0">
                <el-input-number v-model="form.goodQuantity" :min="0" :max="999999" size="large" class="!w-full touch-input" controls-position="right" />
              </el-form-item>

              <el-form-item :label="t('productionEntry.actualMinutes')" class="!mb-0">
                <el-input :model-value="formatNumber(actualOperatingMinutes, 0)" size="large" class="touch-input" readonly />
              </el-form-item>

              <el-form-item :label="t('productionEntry.defectQuantity')" class="!mb-0">
                <el-input :model-value="formatNumber(calculatedDefectQuantity, 0)" size="large" class="touch-input" readonly />
              </el-form-item>
            </div>
          </div>

          <div class="mt-6 flex flex-col gap-3 sm:flex-row">
            <el-button type="primary" size="large" class="touch-btn flex-1" :loading="saving" @click="saveReport">
              <Save class="mr-2 h-5 w-5" />
              {{ t('productionEntry.save') }}
            </el-button>
            <el-button size="large" class="touch-btn" @click="resetForm">
              <RotateCcw class="mr-2 h-5 w-5" />
              {{ t('productionEntry.reset') }}
            </el-button>
          </div>
        </el-form>
      </section>

      <aside class="space-y-3">
        <div class="rounded-lg border border-slate-200 bg-white p-2 shadow-sm">
          <h3 class="text-sm font-black uppercase tracking-wider text-slate-500">{{ t('productionEntry.results') }}</h3>
          <div v-if="showResult" class="mt-4 space-y-3">
            <div v-for="item in oeeCards" :key="item.key" class="rounded-lg border p-2" :class="item.cardClass">
              <div class="text-[10px] font-black uppercase text-slate-500">{{ item.label }}</div>
              <div class="mt-1 text-2xl font-black" :class="item.textClass">{{ item.value }}</div>
            </div>
            <div class="rounded-lg px-3 py-2 text-center text-sm font-black" :class="resultBadgeClass">{{ resultLabel }}</div>
          </div>
          <div v-else class="mt-4 rounded-lg border border-dashed border-slate-300 p-6 text-center text-sm font-semibold text-slate-400">
            {{ t('productionEntry.noResult') }}
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
const currentSignature = computed(() => currentUser.value?.fullName || currentUser.value?.name || '-')

const form = ref({
  reportDate: new Date().toISOString().slice(0, 10),
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

const numericFields = [
  { key: 'downtimeMinutes', label: 'Dừng (phút)', max: 1440 },
  { key: 'inputQuantity', label: 'Số lượng nhập', max: 999999 },
  { key: 'goodQuantity', label: 'Số lượng đạt', max: 999999 },
]

const numericFieldsFiltered = computed(() => numericFields.filter(f => f.key !== 'downtimeMinutes'))

const downtimeReasons = [
  '砂輪用盡 / Hết đá',
  '更換砂輪（針對磨床組） / Thay đá ( đối với tổ Mài)',
  '其他 / Khác',
]

const filteredMachines = computed(() =>
  machines.value.filter(m => !form.value.lineCode || m.lineCode === form.value.lineCode)
)

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

function normalizeMaster(item) {
  return {
    id: item.id,
    code: item.code,
    name: item.name,
    cycleTimeSeconds: item.cycleTimeSeconds,
    standardTimeMinutes: item.standardTimeMinutes,
  }
}

function onProductChange(partNumber) {
  const product = products.value.find(p => p.partNumber === partNumber)
  if (product) {
    form.value.partName = product.partName
    form.value.cycleTime = product.cycleTimeSeconds
  }
}

function onLineChange(lineCode) {
  const firstMachine = machines.value.find(m => m.lineCode === lineCode)
  if (firstMachine) {
    form.value.machineCode = firstMachine.machineCode
  }
}

function resetForm() {
  form.value.partNumber = ''
  form.value.partName = ''
  form.value.cycleTime = ''
  form.value.operatingMinutes = 420
  form.value.downtimeMinutes = 30
  form.value.inputQuantity = 0
  form.value.goodQuantity = 0
  form.value.defectQuantity = 0
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
    machines.value = machinesRes.map(item => ({ id: item.id, machineCode: item.machineCode, lineCode: item.lineCode, description: item.description }))
    shifts.value = shiftsRes.map(item => ({ id: item.id, shiftName: item.name, standardTimeMinutes: item.standardTimeMinutes }))

    if (!form.value.lineCode && lines.value.length) {
      form.value.lineCode = lines.value[0].lineCode
    }
    if (!form.value.machineCode && machines.value.length) {
      const machine = machines.value.find(m => m.lineCode === form.value.lineCode) || machines.value[0]
      form.value.machineCode = machine?.machineCode || ''
    }
    if (!form.value.shiftName && shifts.value.length) {
      form.value.shiftName = shifts.value[0].shiftName
    }
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
    const saved = await productionApi.create(payload)
    result.value = saved
    showResult.value = true
    ElMessage.success(t('productionEntry.messages.saveSuccess'))
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
    { key: 'prod-eff', label: '生產效率 / Hiệu Suất', value: formatPercent(Number(r.productionEfficiency || 0)), textClass: 'text-amber-600', cardClass: 'border-amber-100 bg-amber-50' },
    { key: 'a', label: t('reports.dashboard.sections.availability'), value: formatPercent(Number(r.availabilityRate || 0)), textClass: 'text-sky-600', cardClass: 'border-sky-100 bg-sky-50' },
    { key: 'p', label: t('reports.dashboard.sections.performance'), value: formatPercent(Number(r.performanceRate || 0)), textClass: 'text-indigo-600', cardClass: 'border-indigo-100 bg-indigo-50' },
    { key: 'q', label: t('reports.dashboard.sections.quality'), value: formatPercent(Number(r.qualityRate || 0)), textClass: 'text-emerald-600', cardClass: 'border-emerald-100 bg-emerald-50' },
    { key: 'signature', label: '簽名', value: currentUser.value?.fullName || currentUser.value?.name || '-', textClass: 'text-slate-700', cardClass: 'border-slate-200 bg-white' },
  ]
})

function formatNumber(value, fractionDigits = 2) {
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
:deep(.el-input-number) { width: 100%; }
:deep(.el-input-number .el-input__inner) { height: 3.5rem; text-align: left; font-size: 1.125rem; font-weight: 700 }
:deep(.el-input .el-input__inner) { height: 3.5rem; font-size: 1.125rem; font-weight: 700 }
:deep(.el-form-item__label) { font-size: 0.95rem; margin-bottom: 0.5rem }
:deep(.touch-btn) { padding-top: 0.75rem; padding-bottom: 0.75rem; font-size: 1.05rem }
:deep(.touch-input .el-input__inner) { padding-left: 1rem }

/* Compact override for the downtimeMinutes input to match select height */
:deep(.compact-number .el-input__inner) {
  height: 2.4rem !important;
  line-height: 2.4rem !important;
  font-size: 1rem !important;
  padding-top: 0.15rem !important;
  padding-bottom: 0.15rem !important;
}
:deep(.compact-number .el-input-number__decrease), :deep(.compact-number .el-input-number__increase) {
  top: 0.5rem !important;
}
</style>
