<template>
  <div>
    <PageHeader :eyebrow="l('eyebrow')" :title="l('title')" />
    <div class="border-b border-slate-200 pb-4">
      <div class="toolbar-row">
        <el-input v-model="filters.keyword" clearable class="toolbar-keyword" :placeholder="l('searchPlaceholder')" />
        <el-select v-model="filters.team" clearable filterable class="toolbar-team" :placeholder="l('team')">
          <el-option v-for="team in teamOptions" :key="team" :label="team" :value="team" />
        </el-select>
        <el-button type="info" @click="resetFilters">{{ l('clearFilters') }}</el-button>
        <el-button type="primary" @click="openDialog()">{{ l('addSkill') }}</el-button>
        <el-button type="warning" :disabled="selectedRows.length === 0" @click="deleteSelected">{{ l('deleteSelected') }}</el-button>
        <el-button type="danger" :disabled="rows.length === 0" @click="deleteAll">{{ l('deleteAll') }}</el-button>
        <el-button type="success" :loading="importing" @click="triggerImport">{{ l('importExcel') }}</el-button>
        <input ref="fileInput" type="file" accept=".xlsx,.xls" class="hidden" @change="handleImportFile" />
        <el-button class="export-button" @click="exportCsv">{{ l('exportCsv') }}</el-button>
        <!-- <div class="summary-tile">
          <span>{{ l('employees') }}</span>
          <strong>{{ employeeCount }}</strong>
        </div> -->
      </div>
    </div>
    <div class="page-card space-y-4">
      <el-tabs v-model="viewMode" class="skill-tabs">
        <el-tab-pane :label="l('detailView')" name="detail">
          <el-table :data="paginatedRows" stripe border class="management-table" style="width:100%" size="large" v-loading="loading" @selection-change="onSelectionChange">
            <el-table-column type="selection" width="48" align="center" />
            <el-table-column type="index" label="#" width="60" :index="pageIndex" />
            <el-table-column prop="employeeCode" :label="l('employeeCode')" min-width="130" show-overflow-tooltip />
            <el-table-column prop="employeeName" :label="l('employeeName')" min-width="150" show-overflow-tooltip />
            <el-table-column prop="jobTitle" :label="l('jobTitle')" min-width="120" show-overflow-tooltip />
            <el-table-column prop="team" :label="l('team')" min-width="110" show-overflow-tooltip />
            <el-table-column prop="hireDate" :label="l('hireDate')" width="135" align="center" />
            <el-table-column prop="partName" :label="l('partName')" min-width="150" show-overflow-tooltip />
            <el-table-column prop="partNumber" :label="l('partNumber')" min-width="140" show-overflow-tooltip />
            <el-table-column prop="process" :label="l('process')" min-width="140" show-overflow-tooltip />
            <el-table-column prop="skill" :label="l('skill')" min-width="130" show-overflow-tooltip />
            <el-table-column :label="l('actions')" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <el-dropdown trigger="click">
                  <el-button link type="primary">{{ l('actions') }}</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="openDialog(row)">{{ l('edit') }}</el-dropdown-item>
                      <el-dropdown-item divided class="danger-item" @click="remove(row)">{{ l('delete') }}</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane :label="l('matrixView')" name="matrix">
          <el-table :data="matrixRows" stripe border class="management-table" style="width:100%" size="large" v-loading="loading">
            <el-table-column prop="partName" :label="l('partName')" min-width="180" fixed show-overflow-tooltip />
            <el-table-column prop="partNumber" :label="l('partNumber')" min-width="150" fixed show-overflow-tooltip />
            <el-table-column
              v-for="employee in matrixEmployees"
              :key="employee.employeeCode"
              min-width="140"
              show-overflow-tooltip
            >
              <template #header>
                <HeaderCell :main="employee.employeeName || employee.employeeCode" :sub="employee.employeeCode" />
              </template>
              <template #default="{ row }">
                {{ row.skills[employee.employeeCode] || '' }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <div v-if="viewMode === 'detail'" class="flex flex-col gap-3 border-t border-slate-200 bg-slate-50 px-4 py-3 md:flex-row md:items-center md:justify-between">
        <span class="text-sm font-semibold text-slate-500">{{ l('total') }}: {{ detailRows.length }}</span>
        <div class="flex items-center gap-3">
          <span class="text-sm text-slate-500">{{ l('rowsPerPage') }}</span>
          <el-select v-model="pageSize" size="small" style="width: 96px">
            <el-option v-for="size in pageSizeOptions" :key="size" :label="String(size)" :value="size" />
          </el-select>
          <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="detailRows.length" layout="prev, pager, next" background />
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <div class="grid gap-3 md:grid-cols-2">
          <el-form-item :label="l('employeeCode')" required>
            <el-select v-model="form.userId" filterable clearable class="!w-full" :placeholder="l('selectEmployee')" @change="onUserChange">
              <el-option
                v-for="user in userOptions"
                :key="user.id"
                :label="formatUserOption(user)"
                :value="user.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="l('employeeName')"><el-input v-model="form.employeeName" disabled /></el-form-item>
          <el-form-item :label="l('jobTitle')"><el-input v-model="form.jobTitle" disabled /></el-form-item>
          <el-form-item :label="l('team')"><el-input v-model="form.team" disabled /></el-form-item>
          <el-form-item :label="l('hireDate')"><el-date-picker v-model="form.hireDate" type="date" value-format="YYYY-MM-DD" class="!w-full" disabled /></el-form-item>
          <el-form-item :label="l('partNumber')">
            <el-select v-model="form.productId" filterable clearable class="!w-full" :placeholder="l('selectProduct')" @change="onProductChange">
              <el-option
                v-for="product in productOptions"
                :key="product.id"
                :label="formatProductOption(product)"
                :value="product.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="l('partName')"><el-input v-model="form.partName" disabled /></el-form-item>
          <el-form-item :label="l('process')">
            <el-select v-model="form.processIds" multiple filterable clearable class="!w-full" :placeholder="l('selectProcess')" @change="onProcessChange">
              <el-option
                v-for="process in processOptions"
                :key="process.id"
                :label="formatProcessOption(process)"
                :value="process.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="l('skill')" class="md:col-span-2"><el-input v-model="form.skill" /></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ l('cancel') }}</el-button>
        <el-button type="primary" @click="save">{{ l('save') }}</el-button>
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
const loading = ref(false)
const importing = ref(false)
const rows = ref([])
const userOptions = ref([])
const productOptions = ref([])
const processOptions = ref([])
const selectedRows = ref([])
const filters = ref({ keyword: '', team: '' })
const viewMode = ref('detail')
const dialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const pageSizeOptions = [10, 20, 50, 100]
const fileInput = ref(null)
const form = ref(emptyForm())

const text = {
  vi: {
    eyebrow: '3.7 Dữ liệu chính',
    title: 'Theo dõi năng lực nhân viên',
    addSkill: 'Thêm năng lực',
    deleteSelected: 'Xóa đã chọn',
    deleteAll: 'Xóa tất cả',
    importExcel: 'Nhập Excel',
    exportCsv: 'Xuất CSV',
    clearFilters: 'Xóa lọc',
    detailView: 'Danh sách',
    matrixView: 'Ma trận',
    searchPlaceholder: 'Tìm tài khoản, họ tên, mã hàng, công đoạn',
    selectEmployee: 'Chọn tài khoản',
    selectProduct: 'Chọn mã hàng',
    selectProcess: 'Chọn công đoạn',
    employeeCode: 'Tài khoản',
    employeeName: 'Họ tên',
    jobTitle: 'Chức vụ',
    team: 'Tổ',
    skill: 'Năng lực',
    hireDate: 'Ngày vào làm',
    partName: 'Tên hàng',
    partNumber: 'Mã hàng',
    process: 'Công đoạn',
    actions: 'Thao tác',
    edit: 'Sửa',
    delete: 'Xóa',
    createTitle: 'Thêm năng lực',
    editTitle: 'Sửa năng lực',
    cancel: 'Hủy',
    save: 'Lưu',
    missingEmployeeCode: 'Vui lòng nhập tài khoản',
    confirmTitle: 'Xác nhận',
    deleteConfirm: 'Xóa',
    deleteSelectedConfirm: 'Xóa các dòng năng lực đã chọn?',
    deleteAllConfirm: 'Xóa toàn bộ bảng theo dõi năng lực nhân viên?',
    saved: 'Đã lưu',
    deleted: 'Đã xóa',
    deletedAll: 'Da xoa tat ca',
    imported: 'Đã import {skills} dòng năng lực của {employees} nhân viên',
    importFailed: 'Import không thành công',
    total: 'Tổng',
    rowsPerPage: 'Dòng/trang',
    employees: 'Nhân viên',
    skills: 'Năng lực',
  },
  'zh-Hant': {
    eyebrow: '3.7 主資料',
    title: '人員能力追蹤表',
    addSkill: '新增能力',
    deleteSelected: '刪除已選',
    deleteAll: '全部刪除',
    importExcel: '匯入 Excel',
    exportCsv: '匯出 CSV',
    clearFilters: '清除篩選',
    detailView: '清單',
    matrixView: '矩陣',
    searchPlaceholder: '搜尋工號、姓名、料號、工序',
    selectEmployee: '選擇工號',
    selectProduct: '選擇料號',
    selectProcess: '選擇工序',
    employeeCode: '工號',
    employeeName: '姓名',
    jobTitle: '職稱',
    team: '組',
    skill: '能力',
    hireDate: '入職日期',
    partName: '品名',
    partNumber: '料號',
    process: '工序',
    actions: '操作',
    edit: '編輯',
    delete: '刪除',
    createTitle: '新增能力',
    editTitle: '編輯能力',
    cancel: '取消',
    save: '儲存',
    missingEmployeeCode: '請輸入工號',
    confirmTitle: '確認',
    deleteConfirm: '刪除',
    deleteSelectedConfirm: '刪除已選擇的人員能力資料？',
    deleteAllConfirm: '刪除全部人員能力追蹤資料？',
    saved: '已儲存',
    deleted: '已刪除',
    deletedAll: '已全部刪除',
    imported: '已匯入 {employees} 位人員，共 {skills} 筆能力資料',
    importFailed: '匯入失敗',
    total: '總筆數',
    rowsPerPage: '每頁筆數',
    employees: '人員',
    skills: '能力',
  },
}

const HeaderCell = {
  props: {
    main: { type: String, required: true },
    sub: { type: String, default: '' },
  },
  setup(props) {
    return () => h('div', { class: 'header-cell' }, [
      h('span', { class: 'header-main' }, props.main),
      props.sub ? h('span', { class: 'header-sub' }, props.sub) : null,
    ])
  },
}

const currentText = computed(() => text[locale.value] || text.vi)
const l = key => currentText.value[key] || key
const dialogTitle = computed(() => form.value.id ? l('editTitle') : l('createTitle'))

const teamOptions = computed(() => [...new Set(rows.value.map(row => row.team).filter(Boolean))])
const employeeCount = computed(() => new Set(filteredRows.value.map(row => row.employeeCode).filter(Boolean)).size)

const filteredRows = computed(() => {
  const term = filters.value.keyword.trim().toLowerCase()
  return rows.value.filter(row => {
    const matchesTeam = !filters.value.team || row.team === filters.value.team
    const matchesKeyword = !term || [
      row.employeeCode,
      row.employeeName,
      row.jobTitle,
      row.team,
      row.skill,
      row.partName,
      row.partNumber,
      row.process,
    ].some(value => String(value || '').toLowerCase().includes(term))
    return matchesTeam && matchesKeyword
  })
})

const paginatedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return detailRows.value.slice(start, start + pageSize.value)
})

const detailRows = computed(() => {
  const map = new Map()
  filteredRows.value.forEach(row => {
    const key = `${row.employeeCode || row.userId || ''}::${row.partNumber || row.productId || ''}::${row.partName || ''}`
    if (!map.has(key)) {
      map.set(key, {
        ...row,
        ids: [],
        processIds: [],
        processValues: [],
      })
    }
    const item = map.get(key)
    item.ids.push(row.id)
    if (row.processId && !item.processIds.includes(row.processId)) item.processIds.push(row.processId)
    const process = processCodeOnly(row.process || row.skill || '')
    if (process && !item.processValues.includes(process)) item.processValues.push(process)
    item.id = item.ids[0]
    item.processId = item.processIds[0] || null
    item.process = item.processValues.join(' + ')
    item.skill = item.process
  })
  return [...map.values()]
})

const matrixEmployees = computed(() => {
  const map = new Map()
  filteredRows.value.forEach(row => {
    if (!row.employeeCode || map.has(row.employeeCode)) return
    map.set(row.employeeCode, { employeeCode: row.employeeCode, employeeName: row.employeeName })
  })
  return [...map.values()]
})

const matrixRows = computed(() => {
  const map = new Map()
  filteredRows.value.forEach(row => {
    const process = processCodeOnly(row.process || row.skill || '')
    const key = `${row.partNumber || ''}::${row.partName || ''}`
    if (!map.has(key)) {
      map.set(key, { partNumber: row.partNumber, partName: row.partName, skills: {} })
    }
    if (row.employeeCode && process) {
      const rowData = map.get(key)
      const current = rowData.skills[row.employeeCode]
      const values = current ? current.split(' + ').filter(Boolean) : []
      if (!values.includes(process)) values.push(process)
      rowData.skills[row.employeeCode] = values.join(' + ')
    }
  })
  return [...map.values()]
})

function pageIndex(index) {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

function processCodeOnly(value) {
  const text = String(value || '').trim()
  if (!text) return ''
  return text.split(' - ')[0].trim()
}

function emptyForm() {
  return {
    id: null,
    userId: null,
    employeeCode: '',
    employeeName: '',
    jobTitle: '',
    team: '',
    skill: '',
    hireDate: '',
    productId: null,
    partName: '',
    partNumber: '',
    processId: null,
    processIds: [],
    process: '',
  }
}

async function loadData() {
  loading.value = true
  try {
    const [skills, users, products] = await Promise.all([
      masterApi.getEmployeeSkills(),
      masterApi.getEmployeeSkillUsers(),
      masterApi.getProducts(),
    ])
    rows.value = skills
    userOptions.value = users
    productOptions.value = products
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { keyword: '', team: '' }
}

function onSelectionChange(selection) {
  selectedRows.value = selection || []
}

function openDialog(row) {
  form.value = row ? { ...emptyForm(), ...row, processIds: row.processIds?.length ? [...row.processIds] : (row.processId ? [row.processId] : []) } : emptyForm()
  dialogVisible.value = true
  if (form.value.productId) {
    loadProductProcesses(form.value.productId)
  } else {
    processOptions.value = []
  }
}

function formatUserOption(user) {
  return `${user.username}${user.fullName ? ` - ${user.fullName}` : ''}`
}

function formatProductOption(product) {
  const code = product.code ?? product.partNumber ?? ''
  const name = product.name ?? product.partName ?? ''
  return `${code}${name ? ` - ${name}` : ''}`
}

function formatProcessOption(process) {
  return process.processCode || process.process || ''
}

function onUserChange(userId) {
  const user = userOptions.value.find(item => item.id === userId)
  form.value.employeeCode = user?.username || ''
  form.value.employeeName = user?.fullName || ''
  form.value.jobTitle = user?.jobTitle || ''
  form.value.team = user?.team || ''
  form.value.hireDate = user?.hireDate || ''
}

async function onProductChange(productId) {
  const product = productOptions.value.find(item => item.id === productId)
  form.value.partNumber = product?.code ?? product?.partNumber ?? ''
  form.value.partName = product?.name ?? product?.partName ?? ''
  form.value.processId = null
  form.value.processIds = []
  form.value.process = ''
  form.value.skill = ''
  await loadProductProcesses(productId)
}

function onProcessChange(processIds) {
  const selected = processOptions.value.filter(item => (processIds || []).includes(item.id))
  form.value.processId = selected[0]?.id || null
  form.value.process = selected.map(formatProcessOption).join('; ')
  form.value.skill = form.value.process
}

async function loadProductProcesses(productId) {
  if (!productId) {
    processOptions.value = []
    return
  }
  try {
    processOptions.value = await masterApi.getProductProcesses(productId)
  } catch (error) {
    processOptions.value = []
    ElMessage.error(error.message)
  }
}

function triggerImport() {
  fileInput.value?.click()
}

async function handleImportFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  importing.value = true
  try {
    const result = await masterApi.importEmployeeSkills(file)
    ElMessage.success(l('imported').replace('{employees}', result.employeesImported ?? 0).replace('{skills}', result.skillsImported ?? 0))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || l('importFailed'))
  } finally {
    importing.value = false
  }
}

function csvValue(value) {
  return `"${String(value ?? '').replaceAll('"', '""')}"`
}

function exportCsv() {
  const headers = [l('employeeCode'), l('employeeName'), l('jobTitle'), l('team'), l('hireDate'), l('partName'), l('partNumber'), l('process'), l('skill')]
  const dataRows = detailRows.value.map(row => ({
    [l('employeeCode')]: row.employeeCode,
    [l('employeeName')]: row.employeeName,
    [l('jobTitle')]: row.jobTitle,
    [l('team')]: row.team,
    [l('hireDate')]: row.hireDate,
    [l('partName')]: row.partName,
    [l('partNumber')]: row.partNumber,
    [l('process')]: row.process,
    [l('skill')]: row.skill,
  }))
  const content = [
    headers.map(csvValue).join(','),
    ...dataRows.map(row => headers.map(header => csvValue(row[header])).join(',')),
  ].join('\r\n')
  const blob = new Blob([`\uFEFF${content}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `employee-skills-${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

async function save() {
  if (!form.value.employeeCode) {
    ElMessage.warning(l('missingEmployeeCode'))
    return
  }
  const basePayload = {
    userId: form.value.userId || null,
    employeeCode: form.value.employeeCode.trim(),
    employeeName: form.value.employeeName?.trim() || '',
    jobTitle: form.value.jobTitle?.trim() || '',
    team: form.value.team?.trim() || '',
    skill: form.value.skill?.trim() || form.value.process?.trim() || '',
    hireDate: form.value.hireDate || null,
    productId: form.value.productId || null,
    partName: form.value.partName?.trim() || '',
    partNumber: form.value.partNumber?.trim() || '',
  }
  const selectedProcesses = processOptions.value.filter(item => (form.value.processIds || []).includes(item.id))
  const payloads = selectedProcesses.length
    ? selectedProcesses.map(process => {
        const label = formatProcessOption(process)
        return { ...basePayload, processId: process.id, process: label, skill: label }
      })
    : [{ ...basePayload, processId: form.value.processId || null, process: form.value.process?.trim() || '' }]
  try {
    if (form.value.id) {
      await masterApi.updateEmployeeSkill(form.value.id, payloads[0])
      const oldExtraIds = form.value.ids?.length ? form.value.ids.slice(1) : []
      await Promise.all(oldExtraIds.map(id => masterApi.deleteEmployeeSkill(id)))
      await Promise.all(payloads.slice(1).map(payload => masterApi.createEmployeeSkill(payload)))
    } else {
      await Promise.all(payloads.map(payload => masterApi.createEmployeeSkill(payload)))
    }
    dialogVisible.value = false
    ElMessage.success(l('saved'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`${l('deleteConfirm')}: ${row.employeeCode}?`, l('confirmTitle'), { type: 'warning' })
  try {
    const ids = row.ids?.length ? row.ids : [row.id]
    await Promise.all(ids.map(id => masterApi.deleteEmployeeSkill(id)))
    ElMessage.success(l('deleted'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function deleteSelected() {
  if (selectedRows.value.length === 0) return
  await ElMessageBox.confirm(l('deleteSelectedConfirm'), l('confirmTitle'), { type: 'warning' })
  try {
    const ids = selectedRows.value.flatMap(row => row.ids?.length ? row.ids : [row.id])
    await Promise.all(ids.map(id => masterApi.deleteEmployeeSkill(id)))
    selectedRows.value = []
    ElMessage.success(l('deleted'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function deleteAll() {
  await ElMessageBox.confirm(l('deleteAllConfirm'), l('confirmTitle'), { type: 'warning' })
  try {
    await masterApi.deleteAllEmployeeSkills()
    selectedRows.value = []
    ElMessage.success(l('deletedAll'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadData)

watch([detailRows, pageSize], () => {
  const maxPage = Math.max(1, Math.ceil(detailRows.value.length / pageSize.value))
  if (currentPage.value > maxPage) currentPage.value = maxPage
})
</script>

<style scoped>
.toolbar-row {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) minmax(180px, 0.65fr) repeat(6, max-content);
  gap: 10px;
  align-items: stretch;
  width: 100%;
}

.toolbar-keyword {
  min-width: 0;
}

.toolbar-team {
  min-width: 0;
}

:deep(.toolbar-keyword .el-input__wrapper) {
  background: #eff6ff;
  border-color: #bfdbfe;
  box-shadow: 0 0 0 1px #bfdbfe inset;
}

:deep(.toolbar-team .el-input__wrapper) {
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
  min-height: 40px;
  border: 1px solid #d8e3f0;
  background: #f8fbff;
  border-radius: 6px;
  padding: 5px 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.summary-tile span {
  font-size: 11px;
  font-weight: 700;
  color: #64748b;
}

.summary-tile strong {
  font-size: 16px;
  color: #0f172a;
  line-height: 1.1;
}

.management-table :deep(.el-table__header th) {
  background: #1f3f73 !important;
  color: #fff;
  border-color: #d8e3f0;
}

.management-table :deep(.el-table__cell) {
  border-color: #d8e3f0;
}

.header-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  line-height: 1.2;
  white-space: normal;
}

.header-main {
  font-weight: 800;
}

.header-sub {
  font-size: 12px;
  font-weight: 700;
  opacity: 0.9;
}

.danger-item {
  color: #ef4444;
}

@media (max-width: 1180px) {
  .toolbar-row {
    grid-template-columns: repeat(2, minmax(220px, 1fr));
  }
}

@media (max-width: 640px) {
  .toolbar-row {
    grid-template-columns: 1fr;
  }
}
</style>
