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
        <div class="grid grid-cols-2 gap-2 md:col-span-2">
          <el-button type="primary" class="!m-0 !w-full" :loading="loading" @click="loadReports">{{ t('productionHistory.filters.search') }}</el-button>
          <el-button class="!m-0 !w-full" @click="resetFilters">{{ t('productionHistory.filters.reset') }}</el-button>
        </div>
      </div>
      <el-table :data="paginatedReports" :empty-text="t('productionHistory.noData')" stripe style="width: 100%" size="large" v-loading="loading">
        <el-table-column prop="createdAt" :label="t('productionHistory.table.time')" width="90">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="lineCode" :label="t('productionHistory.table.line')" width="80" align="center" />
        <el-table-column prop="machineCode" :label="t('productionHistory.table.machine')" width="90" align="center" />
        <el-table-column :label="t('productionHistory.table.operatorName')" width="160" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ row.operatorName || row.createdBy || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('productionHistory.table.responsibleLeader')" width="160" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ row.responsibleLeader || '-' }}</template>
        </el-table-column>
        <el-table-column prop="partNumber" :label="t('productionHistory.table.partNumber')" width="120" />
        <el-table-column prop="partName" :label="t('productionHistory.table.partName')" min-width="170" show-overflow-tooltip />
        <el-table-column :label="t('productionHistory.table.processIds')" min-width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ formatProcessIds(row.processIds) }}</template>
        </el-table-column>
        <el-table-column :label="t('productionHistory.table.runDowntime')" width="110" align="center">
          <template #default="{ row }">
            <span class="font-bold text-emerald-600">{{ row.totalOperatingMinutes }}</span>
            <span class="mx-1 text-slate-300">/</span>
            <span class="font-bold text-rose-500">{{ row.downtimeMinutes }}</span>
          </template>
        </el-table-column>
            <el-table-column :label="t('productionHistory.table.dailyTargetQuantity')" width="100" align="center">
          <template #default="{ row }">{{ formatNumber(row.dailyTargetQuantity) }}</template>
        </el-table-column>
        <el-table-column :label="t('productionHistory.table.inputGoodDefect')" width="130" align="center">
          <template #default="{ row }">
            <div class="history-multiline-cell">
              <div v-for="(line, index) in lotDisplayRows(row)" :key="index" class="history-multiline-row">{{ line.inputGoodDefect }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('productionHistory.table.internalDefectQuantity')" width="120" align="center">
          <template #default="{ row }">
            <div class="history-multiline-cell">
              <div v-for="(line, index) in lotDisplayRows(row)" :key="index" class="history-multiline-row">{{ line.internalDefectQuantity }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('productionHistory.table.externalDefectQuantity')" width="120" align="center">
          <template #default="{ row }">
            <div class="history-multiline-cell">
              <div v-for="(line, index) in lotDisplayRows(row)" :key="index" class="history-multiline-row">{{ line.externalDefectQuantity }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('productionHistory.table.lotNo')" width="110" align="center">
          <template #default="{ row }">
            <div class="history-multiline-cell">
              <div v-for="(line, index) in lotDisplayRows(row)" :key="index" class="history-multiline-row">{{ line.lotNo }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('productionHistory.table.responsibility')" width="110" align="center">
          <template #default="{ row }">{{ formatPercent(row.responsibility) }}</template>
        </el-table-column>
        <el-table-column :label="t('productionHistory.table.deductionPercent')" width="90" align="center">
          <template #default="{ row }">{{ formatPercent(row.deductionPercent) }}</template>
        </el-table-column>
        <el-table-column :label="t('productionHistory.table.oee')" width="110" align="center">
          <template #default="{ row }">
            <span class="rounded-full px-2.5 py-1 text-sm font-black" :class="oeeClass(row.oee)">
              {{ formatPercent(row.oee) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="evaluationLabel" :label="t('productionHistory.table.evaluationLabel')" width="110" align="center" show-overflow-tooltip />
      </el-table>

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
import { Edit } from '@element-plus/icons-vue'
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

const paginatedReports = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return todayReports.value.slice(start, end)
})

const filteredMachineOptions = computed(() => {
  if (!filters.value.lineCode) return machines.value
  return machines.value.filter(machine => machine.lineCode === filters.value.lineCode)
})

function formatTime(iso) {
  if (!iso) return ''
  return iso.split('T')[1]?.substring(0, 5) || ''
}

function formatPercent(value) {
  const percent = Number(value || 0) * 100
  return `${Number(percent.toFixed(2))}%`
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString()
}

function splitLotNos(value) {
  return String(value || '')
    .split(/\s*[;；]\s*/)
    .map(item => item.trim())
    .filter(Boolean)
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
      internalDefectQuantity,
      externalDefectQuantity,
    }
  })
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
  await loadMasterData()
  await loadReports()
})

watch([todayReports, pageSize], () => {
  const maxPage = Math.max(1, Math.ceil(todayReports.value.length / pageSize.value))
  if (currentPage.value > maxPage) currentPage.value = maxPage
})
</script>

<style scoped>
.history-multiline-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-weight: 700;
  line-height: 1.4;
}

.history-multiline-row {
  min-height: 22px;
}
</style>
