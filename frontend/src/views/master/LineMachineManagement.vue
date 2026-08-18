<template>
  <div>
    <PageHeader :eyebrow="l('eyebrow')" :title="l('title')" />
    <div class="border-b border-slate-200 pb-4">
      <div class="toolbar-row">
        <div class="toolbar-group toolbar-filters">
          <el-select
            v-model="filters.lineCode"
            clearable
            filterable
            allow-create
            default-first-option
            :placeholder="l('searchLine')"
            class="toolbar-line"
          >
            <el-option v-for="line in lineOptions" :key="line" :label="line" :value="line" />
          </el-select>
          <el-input v-model="filters.machineCode" clearable :placeholder="l('searchMachine')" class="toolbar-machine" />
          <el-button type="info" plain @click="resetFilters">{{ l('clearFilters') }}</el-button>
        </div>
        <div class="toolbar-group toolbar-primary-actions">
          <el-button type="primary" @click="openLineDialog()">{{ l('addLine') }}</el-button>
          <el-button type="success" @click="openMachineDialog()">{{ l('addMachine') }}</el-button>
          <el-button class="export-button" @click="exportCsv">{{ l('exportCsv') }}</el-button>
        </div>
        <div class="toolbar-group toolbar-danger-actions">
          <el-button type="danger" plain @click="deleteAllLines">{{ l('deleteAllLines') }}</el-button>
          <el-button type="danger" plain @click="deleteAllMachines">{{ l('deleteAllMachines') }}</el-button>
        </div>
      </div>
    </div>
    <div class="page-card space-y-4">
      <el-table :data="paginatedRows" stripe border class="line-machine-table rounded-md" style="width:100%" size="large" v-loading="loading">
        <el-table-column type="index" label="#" width="60" :index="pageIndex" />
        <el-table-column prop="lineCode" width="130">
          <template #header><HeaderCell zh="產線" vi="Chuyền" /></template>
        </el-table-column>
        <el-table-column prop="assetCode" min-width="150" show-overflow-tooltip>
          <template #header><HeaderCell zh="財產編號" vi="Mã Tài Sản" /></template>
        </el-table-column>
        <el-table-column prop="machineCode" min-width="150" show-overflow-tooltip>
          <template #header><HeaderCell zh="設備編號" vi="Mã Thiết Bị" /></template>
        </el-table-column>
        <el-table-column prop="description" min-width="200" show-overflow-tooltip>
          <template #header><HeaderCell zh="設備名稱" vi="Tên Thiết Bị" /></template>
        </el-table-column>
        <el-table-column prop="purchaseDate" width="150" align="center">
          <template #header><HeaderCell zh="購入日期" vi="Ngày Nhập" /></template>
        </el-table-column>
        <el-table-column prop="custodyDepartment" min-width="180" show-overflow-tooltip>
          <template #header><HeaderCell zh="保管單位" vi="Bộ Phận Bảo Quản" /></template>
        </el-table-column>
        <el-table-column :label="l('actions')" width="130" fixed="right" align="center">
          <template #header><HeaderCell zh="操作" :vi="l('actions')" /></template>
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="command => handleRowCommand(command, row)">
              <el-button size="small">{{ l('moreActions') }}</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="editLine">{{ l('editLine') }}</el-dropdown-item>
                  <el-dropdown-item command="deleteLine" divided>{{ l('deleteLine') }}</el-dropdown-item>
                  <el-dropdown-item v-if="row.machineId" command="editMachine">{{ l('editMachine') }}</el-dropdown-item>
                  <el-dropdown-item v-if="row.machineId" command="deleteMachine" divided>{{ l('deleteMachine') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex flex-col gap-3 border-t border-slate-200 bg-slate-50 px-4 py-3 md:flex-row md:items-center md:justify-between">
        <span class="text-sm font-semibold text-slate-500">{{ l('total') }}: {{ filteredRows.length }}</span>
        <div class="flex items-center gap-3">
          <span class="text-sm text-slate-500">{{ l('rowsPerPage') }}</span>
          <el-select v-model="pageSize" size="small" style="width: 96px">
            <el-option v-for="size in pageSizeOptions" :key="size" :label="String(size)" :value="size" />
          </el-select>
          <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="filteredRows.length" layout="prev, pager, next" background />
        </div>
      </div>
    </div>

    <el-dialog v-model="lineDialogVisible" :title="lineDialogTitle" width="460px" destroy-on-close>
      <el-form :model="lineForm" label-position="top">
        <el-form-item :label="l('lineCode')" required><el-input v-model="lineForm.lineCode" /></el-form-item>
        <el-form-item :label="l('description')"><el-input v-model="lineForm.description" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="lineDialogVisible = false">{{ l('cancel') }}</el-button>
        <el-button type="primary" @click="saveLine">{{ l('save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="machineDialogVisible" :title="machineDialogTitle" width="520px" destroy-on-close>
      <el-form :model="machineForm" label-position="top">
        <el-form-item :label="l('lineCode')">
          <el-select v-model="machineForm.lineCode" clearable filterable class="!w-full">
            <el-option v-for="line in lines" :key="line.code" :label="`${line.code} - ${line.name || ''}`" :value="line.code" />
          </el-select>
        </el-form-item>
        <el-form-item :label="l('assetCode')"><el-input v-model="machineForm.assetCode" /></el-form-item>
        <el-form-item :label="l('machineCode')" required><el-input v-model="machineForm.machineCode" /></el-form-item>
        <el-form-item :label="l('machineName')"><el-input v-model="machineForm.description" /></el-form-item>
        <el-form-item :label="l('purchaseDate')"><el-date-picker v-model="machineForm.purchaseDate" type="date" value-format="YYYY-MM-DD" class="!w-full" /></el-form-item>
        <el-form-item :label="l('custodyDepartment')"><el-input v-model="machineForm.custodyDepartment" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="machineDialogVisible = false">{{ l('cancel') }}</el-button>
        <el-button type="primary" @click="saveMachine">{{ l('save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, h, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { locale } = useI18n()
const HeaderCell = {
  props: {
    zh: { type: String, required: true },
    vi: { type: String, required: true },
  },
  setup(props) {
    return () => h('div', { class: 'excel-header-cell' }, [
      h('div', { class: 'excel-header-main' }, locale.value === 'zh-Hant' ? props.zh : props.vi),
    ])
  },
}
const loading = ref(false)
const lines = ref([])
const rows = ref([])
const filters = ref({ lineCode: '', machineCode: '' })
const lineDialogVisible = ref(false)
const machineDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const pageSizeOptions = [10, 20, 50, 100]
const lineForm = ref({ id: null, lineCode: '', description: '' })
const machineForm = ref({ id: null, lineCode: '', assetCode: '', machineCode: '', description: '', purchaseDate: '', custodyDepartment: '' })
const text = {
  vi: {
    eyebrow: '3.2 Dữ liệu chính',
    title: 'Quản lý chuyền & thiết bị',
    addLine: 'Thêm chuyền',
    addMachine: 'Thêm thiết bị',
    exportCsv: 'Xuất CSV',
    searchLine: 'Tìm chuyền',
    searchMachine: 'Tìm mã thiết bị',
    clearFilters: 'Xóa lọc',
    lineCode: 'Chuyền',
    assetCode: 'Mã tài sản',
    machineCode: 'Mã thiết bị',
    machineName: 'Tên thiết bị',
    purchaseDate: 'Ngày nhập',
    custodyDepartment: 'Bộ phận bảo quản',
    actions: 'Thao tác',
    editLine: 'Sửa chuyền',
    deleteLine: 'Xóa chuyền',
    editMachine: 'Sửa thiết bị',
    deleteMachine: 'Xóa thiết bị',
    lineCreateTitle: 'Thêm chuyền',
    lineEditTitle: 'Sửa chuyền',
    machineCreateTitle: 'Thêm thiết bị',
    machineEditTitle: 'Sửa thiết bị',
    description: 'Mô tả',
    cancel: 'Hủy',
    save: 'Lưu',
    missingLine: 'Vui lòng nhập chuyền',
    missingMachine: 'Vui lòng nhập mã thiết bị',
    confirmTitle: 'Xác nhận',
    deleteLineConfirm: 'Xóa chuyền',
    deleteMachineConfirm: 'Xóa thiết bị',
    deleteAllLines: 'Xóa toàn bộ chuyền',
    deleteAllMachines: 'Xóa toàn bộ thiết bị',
    deleteAllLinesConfirm: 'Xóa toàn bộ chuyền?',
    deleteAllMachinesConfirm: 'Xóa toàn bộ thiết bị?',
    saved: 'Đã lưu',
    deleted: 'Đã xóa',
    deletedAllLines: 'Đã xóa toàn bộ chuyền',
    deletedAllMachines: 'Đã xóa toàn bộ thiết bị',
    total: 'Tổng',
    rowsPerPage: 'Dòng/trang',
    lineCount: 'Chuyền',
    machineCount: 'Thiết bị',
    moreActions: 'Thao tác',
  },
  'zh-Hant': {
    eyebrow: '3.2 主資料',
    title: '產線與設備管理',
    addLine: '新增產線',
    addMachine: '新增設備',
    exportCsv: '匯出 CSV',
    searchLine: '搜尋產線',
    searchMachine: '搜尋設備編號',
    clearFilters: '清除篩選',
    lineCode: '產線',
    assetCode: '財產編號',
    machineCode: '設備編號',
    machineName: '設備名稱',
    purchaseDate: '購入日期',
    custodyDepartment: '保管單位',
    actions: '操作',
    editLine: '編輯產線',
    deleteLine: '刪除產線',
    editMachine: '編輯設備',
    deleteMachine: '刪除設備',
    lineCreateTitle: '新增產線',
    lineEditTitle: '編輯產線',
    machineCreateTitle: '新增設備',
    machineEditTitle: '編輯設備',
    description: '說明',
    cancel: '取消',
    save: '儲存',
    missingLine: '請輸入產線',
    missingMachine: '請輸入設備編號',
    confirmTitle: '確認',
    deleteLineConfirm: '刪除產線',
    deleteMachineConfirm: '刪除設備',
    deleteAllLines: '刪除所有產線',
    deleteAllMachines: '刪除所有設備',
    deleteAllLinesConfirm: '刪除所有產線？',
    deleteAllMachinesConfirm: '刪除所有設備？',
    saved: '已儲存',
    deleted: '已刪除',
    deletedAllLines: '已刪除所有產線',
    deletedAllMachines: '已刪除所有設備',
    total: '總筆數',
    rowsPerPage: '每頁筆數',
    lineCount: '產線',
    machineCount: '設備',
    moreActions: '操作',
  },
}

const currentText = computed(() => text[locale.value] || text.vi)
const l = key => currentText.value[key] || key
const lineDialogTitle = computed(() => lineForm.value.id ? l('lineEditTitle') : l('lineCreateTitle'))
const machineDialogTitle = computed(() => machineForm.value.id ? l('machineEditTitle') : l('machineCreateTitle'))
const machineCount = computed(() => rows.value.filter(row => row.machineId).length)
const lineOptions = computed(() => [...new Set(rows.value.map(row => row.lineCode).filter(Boolean))])
const filteredRows = computed(() => {
  const lineTerm = filters.value.lineCode.trim().toLowerCase()
  const machineTerm = filters.value.machineCode.trim().toLowerCase()
  return rows.value.filter(row => {
    const matchesLine = !lineTerm || String(row.lineCode || '').toLowerCase().includes(lineTerm)
    const matchesMachine = !machineTerm || String(row.machineCode || '').toLowerCase().includes(machineTerm)
    return matchesLine && matchesMachine
  })
})
const paginatedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRows.value.slice(start, start + pageSize.value)
})

function pageIndex(index) {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

async function loadData() {
  loading.value = true
  try {
    const [lineRes, machineRes] = await Promise.all([masterApi.getLines(), masterApi.getMachines()])
    lines.value = (lineRes || []).map(item => ({ id: item.id, code: item.code ?? item.lineCode ?? '', name: item.name ?? item.description ?? '' }))
    const machines = machineRes || []
    const lineRows = lines.value.map(line => {
      const related = machines.filter(machine => (machine.lineCode || '') === line.code)
      if (!related.length) return [{ lineId: line.id, lineCode: line.code, lineName: line.name }]
      return related.map(machine => ({ lineId: line.id, lineCode: line.code, lineName: line.name, machineId: machine.id, ...machine }))
    }).flat()
    const looseMachines = machines.filter(machine => !machine.lineCode).map(machine => ({ lineCode: '', machineId: machine.id, ...machine }))
    rows.value = [...lineRows, ...looseMachines]
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function openLineDialog(row) {
  lineForm.value = row ? { id: row.lineId, lineCode: row.lineCode, description: row.lineName || '' } : { id: null, lineCode: '', description: '' }
  lineDialogVisible.value = true
}

function openMachineDialog(row) {
  machineForm.value = row?.machineId
    ? { id: row.machineId, lineCode: row.lineCode || '', assetCode: row.assetCode || '', machineCode: row.machineCode || '', description: row.description || '', purchaseDate: row.purchaseDate || '', custodyDepartment: row.custodyDepartment || '' }
    : { id: null, lineCode: row?.lineCode || '', assetCode: '', machineCode: '', description: '', purchaseDate: '', custodyDepartment: '' }
  machineDialogVisible.value = true
}

function csvValue(value) {
  return `"${String(value ?? '').replaceAll('"', '""')}"`
}

function exportCsv() {
  const headers = [l('lineCode'), l('assetCode'), l('machineCode'), l('machineName'), l('purchaseDate'), l('custodyDepartment')]
  const dataRows = filteredRows.value.map(row => ({
    [l('lineCode')]: row.lineCode,
    [l('assetCode')]: row.assetCode,
    [l('machineCode')]: row.machineCode,
    [l('machineName')]: row.description,
    [l('purchaseDate')]: row.purchaseDate,
    [l('custodyDepartment')]: row.custodyDepartment,
  }))
  const content = [
    headers.map(csvValue).join(','),
    ...dataRows.map(row => headers.map(header => csvValue(row[header])).join(',')),
  ].join('\r\n')
  const blob = new Blob([`\uFEFF${content}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `line-machines-${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

function resetFilters() {
  filters.value = { lineCode: '', machineCode: '' }
  currentPage.value = 1
}


async function saveLine() {
  if (!lineForm.value.lineCode) {
    ElMessage.warning(l('missingLine'))
    return
  }
  const payload = { lineCode: lineForm.value.lineCode.trim(), description: lineForm.value.description?.trim() || '' }
  try {
    if (lineForm.value.id) await masterApi.updateLine(lineForm.value.id, payload)
    else await masterApi.createLine(payload)
    lineDialogVisible.value = false
    ElMessage.success(l('saved'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function saveMachine() {
  if (!machineForm.value.machineCode) {
    ElMessage.warning(l('missingMachine'))
    return
  }
  const payload = {
    machineCode: machineForm.value.machineCode.trim(),
    description: machineForm.value.description?.trim() || '',
    lineCode: machineForm.value.lineCode || '',
    assetCode: machineForm.value.assetCode?.trim() || '',
    purchaseDate: machineForm.value.purchaseDate || null,
    custodyDepartment: machineForm.value.custodyDepartment?.trim() || '',
  }
  try {
    if (machineForm.value.id) await masterApi.updateMachine(machineForm.value.id, payload)
    else await masterApi.createMachine(payload)
    machineDialogVisible.value = false
    ElMessage.success(l('saved'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function deleteMachine(row) {
  await ElMessageBox.confirm(`${l('deleteMachineConfirm')}: ${row.machineCode}?`, l('confirmTitle'), { type: 'warning' })
  try {
    await masterApi.deleteMachine(row.machineId)
    ElMessage.success(l('deleted'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function deleteLine(row) {
  await ElMessageBox.confirm(`${l('deleteLineConfirm')}: ${row.lineCode}?`, l('confirmTitle'), { type: 'warning' })
  try {
    await masterApi.deleteLine(row.lineId)
    ElMessage.success(l('deleted'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}
async function deleteAllLines() {
  await ElMessageBox.confirm(l('deleteAllLinesConfirm'), l('confirmTitle'), { type: 'warning' })
  try {
    await masterApi.deleteAllLines()
    ElMessage.success(l('deletedAllLines'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}
async function deleteAllMachines() {
  await ElMessageBox.confirm(l('deleteAllMachinesConfirm'), l('confirmTitle'), { type: 'warning' })
  try {
    await masterApi.deleteAllMachines()
    ElMessage.success(l('deletedAllMachines'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function handleRowCommand(command, row) {
  if (command === 'editLine') {
    openLineDialog(row)
  } else if (command === 'deleteLine') {
    deleteLine(row)
  } else if (command === 'editMachine') {
    openMachineDialog(row)
  } else if (command === 'deleteMachine') {
    deleteMachine(row)
  }
}

onMounted(loadData)

watch([filteredRows, pageSize], () => {
  const maxPage = Math.max(1, Math.ceil(filteredRows.value.length / pageSize.value))
  if (currentPage.value > maxPage) currentPage.value = maxPage
})

watch(() => [filters.value.lineCode, filters.value.machineCode], () => {
  currentPage.value = 1
})
</script>

<style scoped>
.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: stretch;
}

.toolbar-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: stretch;
}

.toolbar-filters {
  flex: 1 1 100%;
  min-width: 0;
}

.toolbar-primary-actions,
.toolbar-danger-actions {
  flex: 0 1 auto;
}

.toolbar-line {
  flex: 1 1 220px;
  min-width: 180px;
}

.toolbar-machine {
  flex: 1 1 220px;
  min-width: 180px;
}

.toolbar-row :deep(.el-button) {
  min-width: 112px;
  margin-left: 0;
}

:deep(.toolbar-line .el-input__wrapper) {
  background: #eff6ff;
  border-color: #bfdbfe;
  box-shadow: 0 0 0 1px #bfdbfe inset;
}

:deep(.toolbar-machine .el-input__wrapper) {
  background: #eff6ff;
  border-color: #bfdbfe;
  box-shadow: 0 0 0 1px #bfdbfe inset;
}

:deep(.export-button) {
  border-color: #c084fc;
  background: #9333ea;
  color: #fff;
}

:deep(.export-button:hover),
:deep(.export-button:focus) {
  border-color: #a855f7;
  background: #7e22ce;
  color: #fff;
}

.summary-tile {
  display: flex;
  min-height: 40px;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #f8fafc;
  padding: 8px 14px;
}

.summary-tile span {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.summary-tile strong {
  color: #0f172a;
  font-size: 18px;
  font-weight: 900;
}

@media (max-width: 1180px) {
  .toolbar-group,
  .toolbar-filters {
    flex: 1 1 100%;
  }

  .toolbar-row :deep(.el-button) {
    flex: 1 1 160px;
  }
}

@media (max-width: 640px) {
  .toolbar-group,
  .toolbar-line,
  .toolbar-machine,
  .toolbar-row :deep(.el-button) {
    flex: 1 1 100%;
    min-width: 0;
  }
}

:deep(.el-table__header-wrapper th),
:deep(.el-table__fixed-header-wrapper th) {
  background-color: #1f4e79 !important;
  color: #fff;
}

:deep(.el-table__header-wrapper .cell),
:deep(.el-table__fixed-header-wrapper .cell) {
  line-height: 1.2;
  white-space: normal;
}

:deep(.excel-header-cell) {
  display: flex;
  min-height: 42px;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-weight: 800;
}

:deep(.excel-header-main) {
  font-size: 12px;
  line-height: 1.25;
  white-space: normal;
}
</style>
