<template>
  <div>
    <PageHeader :eyebrow="l('eyebrow')" :title="l('title')" />
    <div class="border-b border-slate-200 pb-4">
      <div class="toolbar-row">
        <el-input v-model="filters.keyword" clearable :placeholder="l('searchPlaceholder')" class="toolbar-keyword" />
        <el-select v-model="filters.customer" clearable filterable :placeholder="l('customer')" class="toolbar-customer">
          <el-option v-for="customer in customerOptions" :key="customer" :label="customer" :value="customer" />
        </el-select>
        <el-button type="info" @click="resetFilters">{{ l('clearFilters') }}</el-button>
        <el-button type="primary" @click="openProductDialog()">{{ l('addProduct') }}</el-button>
        <el-button type="success" :disabled="!products.length" @click="openProcessDialog()">{{ l('addProcess') }}</el-button>
        <el-button type="warning" :loading="importing" @click="triggerImport">{{ l('importExcel') }}</el-button>
        <el-button class="export-button" @click="exportCsv">{{ l('exportCsv') }}</el-button>
        <input ref="fileInput" type="file" accept=".xlsx,.xls" class="hidden" @change="handleImportFile" />
      </div>
    </div>
    <div class="page-card space-y-4">
      <el-table :data="paginatedRows" stripe border class="product-process-table rounded-md" style="width:100%" size="large" v-loading="loading">
        <el-table-column type="index" label="#" width="60" :index="pageIndex" />
        <el-table-column prop="customer" min-width="150" show-overflow-tooltip>
          <template #header><HeaderCell zh="客戶" vi="Khách Hàng" /></template>
        </el-table-column>
        <el-table-column prop="partNumber" min-width="150" show-overflow-tooltip>
          <template #header><HeaderCell zh="品號" vi="Mã Hàng" /></template>
        </el-table-column>
        <el-table-column prop="processCode" min-width="130" show-overflow-tooltip>
          <template #header><HeaderCell zh="工序" vi="Công Đoạn" /></template>
        </el-table-column>
        <el-table-column prop="process" min-width="180" show-overflow-tooltip>
          <template #header><HeaderCell zh="製程" vi="Lưu Trình" /></template>
        </el-table-column>
        <el-table-column prop="cycleTimeSeconds" width="140" align="center">
          <template #header><HeaderCell zh="工時(秒)" vi="Thời Gian Thao Tác (giây)" /></template>
        </el-table-column>
        <el-table-column prop="lineCode" min-width="170" show-overflow-tooltip>
          <template #header><HeaderCell zh="線別" vi="Dây Chuyền" /></template>
        </el-table-column>
        <el-table-column prop="machineCode" min-width="180" show-overflow-tooltip>
          <template #header><HeaderCell zh="機台編號" vi="Mã Số Máy" /></template>
        </el-table-column>
        <el-table-column prop="sequence" width="120" align="center">
          <template #header><HeaderCell zh="順序" vi="Thứ Tự" /></template>
        </el-table-column>
        <el-table-column :label="l('actions')" width="130" fixed="right" align="center">
          <template #header><HeaderCell zh="操作" :vi="l('actions')" /></template>
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="command => handleRowCommand(command, row)">
              <el-button size="small">{{ l('moreActions') }}</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="editProduct">{{ l('editProduct') }}</el-dropdown-item>
                  <el-dropdown-item command="editProcess">{{ l('editProcess') }}</el-dropdown-item>
                  <el-dropdown-item command="deleteProduct" divided>{{ l('deleteProduct') }}</el-dropdown-item>
                  <el-dropdown-item v-if="row.processId" command="deleteProcess" divided>{{ l('deleteProcess') }}</el-dropdown-item>
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

    <el-dialog v-model="productDialogVisible" :title="productDialogTitle" width="520px" destroy-on-close>
      <el-form :model="productForm" label-position="top">
        <el-form-item :label="l('customer')"><el-input v-model="productForm.customer" /></el-form-item>
        <el-form-item :label="l('partNumber')" required><el-input v-model="productForm.partNumber" /></el-form-item>
        <el-form-item :label="l('partName')" required><el-input v-model="productForm.partName" /></el-form-item>
        <el-form-item label="C/T"><el-input-number v-model="productForm.cycleTimeSeconds" :min="0" :precision="2" class="!w-full" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialogVisible = false">{{ l('cancel') }}</el-button>
        <el-button type="primary" @click="saveProduct">{{ l('save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="processDialogVisible" :title="processDialogTitle" width="620px" destroy-on-close>
      <el-form :model="processForm" label-position="top">
        <el-form-item :label="l('partNumber')" required>
          <el-select v-model="processForm.productId" filterable class="!w-full">
            <el-option v-for="product in products" :key="product.id" :label="`${product.code} - ${product.name}`" :value="product.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="l('process')" required><el-input v-model="processForm.process" /></el-form-item>
        <el-form-item :label="l('processCode')"><el-input v-model="processForm.processCode" /></el-form-item>
        <div class="grid gap-3 md:grid-cols-2">
          <el-form-item :label="l('lineCode')">
            <el-select v-model="processForm.lineCodes" multiple filterable allow-create default-first-option class="!w-full">
              <el-option v-for="line in lineOptions" :key="line" :label="line" :value="line" />
            </el-select>
          </el-form-item>
          <el-form-item :label="l('machineCode')">
            <el-select v-model="processForm.machineCodes" multiple filterable allow-create default-first-option class="!w-full">
              <el-option v-for="machine in machineOptions" :key="machine" :label="machine" :value="machine" />
            </el-select>
          </el-form-item>
        </div>
        <div class="grid gap-3 md:grid-cols-2">
          <el-form-item label="C/T"><el-input-number v-model="processForm.cycleTimeSeconds" :min="0" :precision="2" class="!w-full" /></el-form-item>
          <el-form-item :label="l('sequence')"><el-input-number v-model="processForm.sequence" :min="1" class="!w-full" /></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">{{ l('cancel') }}</el-button>
        <el-button type="primary" @click="saveProcess">{{ l('save') }}</el-button>
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
const importing = ref(false)
const products = ref([])
const rows = ref([])
const fileInput = ref(null)
const lineOptions = ref([])
const machineOptions = ref([])
const filters = ref({ keyword: '', customer: '' })
const productDialogVisible = ref(false)
const processDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const pageSizeOptions = [10, 20, 50, 100]
const productForm = ref({ id: null, customer: '', partNumber: '', partName: '', cycleTimeSeconds: null })
const processForm = ref({ id: null, productId: null, processCode: '', process: '', lineCodes: [], machineCodes: [], cycleTimeSeconds: null, sequence: null })
const text = {
  vi: {
    eyebrow: '3.1 Dữ liệu chính',
    title: 'Quản lý mã hàng & công đoạn',
    addProduct: 'Thêm mã hàng',
    addProcess: 'Thêm công đoạn',
    importExcel: 'Nhập Excel',
    exportCsv: 'Xuất CSV',
    searchPlaceholder: 'Tìm mã hàng, tên hàng, công đoạn',
    clearFilters: 'Xóa lọc',
    customer: 'Khách hàng',
    partNumber: 'Mã hàng',
    partName: 'Tên hàng',
    process: 'Công đoạn',
    processCode: 'Mã công đoạn',
    machineCode: 'Mã thiết bị',
    lineCode: 'Chuyền',
    sequence: 'Thứ tự',
    actions: 'Thao tác',
    editProduct: 'Sửa mã hàng',
    editProcess: 'Sửa công đoạn',
    deleteProduct: 'Xóa mã hàng',
    deleteProcess: 'Xóa công đoạn',
    productCreateTitle: 'Thêm mã hàng',
    productEditTitle: 'Sửa mã hàng',
    processCreateTitle: 'Thêm công đoạn',
    processEditTitle: 'Sửa công đoạn',
    cancel: 'Hủy',
    save: 'Lưu',
    importSuccess: 'Nhập thành công',
    importFailed: 'Nhập thất bại',
    missingProduct: 'Vui lòng nhập mã hàng và tên hàng',
    missingProcess: 'Vui lòng chọn mã hàng và nhập công đoạn',
    confirmTitle: 'Xác nhận',
    deleteProductConfirm: 'Xóa mã hàng',
    deleteProcessConfirm: 'Xóa công đoạn',
    saved: 'Đã lưu',
    deleted: 'Đã xóa',
    total: 'Tổng',
    rowsPerPage: 'Dòng/trang',
    productsCount: 'Mã hàng',
    processesCount: 'Công đoạn',
    filteredCount: 'Kết quả',
    moreActions: 'Thao tác',
  },
  'zh-Hant': {
    eyebrow: '3.1 主資料',
    title: '料號及工序管理',
    addProduct: '新增料號',
    addProcess: '新增工序',
    importExcel: '匯入 Excel',
    exportCsv: '匯出 CSV',
    searchPlaceholder: '搜尋料號、品名、工序',
    clearFilters: '清除篩選',
    customer: '客戶',
    partNumber: '料號',
    partName: '品名',
    process: '工序',
    processCode: '工序代碼',
    machineCode: '設備編號',
    lineCode: '產線',
    sequence: '順序',
    actions: '操作',
    editProduct: '編輯料號',
    editProcess: '編輯工序',
    deleteProduct: '刪除料號',
    deleteProcess: '刪除工序',
    productCreateTitle: '新增料號',
    productEditTitle: '編輯料號',
    processCreateTitle: '新增工序',
    processEditTitle: '編輯工序',
    cancel: '取消',
    save: '儲存',
    importSuccess: '匯入成功',
    importFailed: '匯入失敗',
    missingProduct: '請輸入料號與品名',
    missingProcess: '請選擇料號並輸入工序',
    confirmTitle: '確認',
    deleteProductConfirm: '刪除料號',
    deleteProcessConfirm: '刪除工序',
    saved: '已儲存',
    deleted: '已刪除',
    total: '總筆數',
    rowsPerPage: '每頁筆數',
    productsCount: '料號',
    processesCount: '工序',
    filteredCount: '結果',
    moreActions: '操作',
  },
}

const currentText = computed(() => text[locale.value] || text.vi)
const l = key => currentText.value[key] || key
const productDialogTitle = computed(() => productForm.value.id ? l('productEditTitle') : l('productCreateTitle'))
const processDialogTitle = computed(() => processForm.value.id ? l('processEditTitle') : l('processCreateTitle'))

const customerOptions = computed(() => [...new Set(products.value.map(item => item.customer).filter(Boolean))])
const processCount = computed(() => rows.value.filter(row => row.processId).length)

const filteredRows = computed(() => {
  const keyword = filters.value.keyword.trim().toLowerCase()
  return rows.value.filter(row => {
    const matchesKeyword = !keyword || [row.customer, row.partNumber, row.partName, row.process, row.machineCode, row.lineCode]
      .some(value => String(value || '').toLowerCase().includes(keyword))
    const matchesCustomer = !filters.value.customer || row.customer === filters.value.customer
    return matchesKeyword && matchesCustomer
  })
})
const paginatedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRows.value.slice(start, start + pageSize.value)
})

function pageIndex(index) {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

function splitCodes(value) {
  return String(value || '').split(';').map(item => item.trim()).filter(Boolean)
}

function joinCodes(value) {
  return Array.isArray(value) && value.length ? value.map(item => String(item).trim()).filter(Boolean).join(';') : null
}

function normalizeProduct(item) {
  return {
    id: item.id,
    code: item.code ?? item.partNumber ?? '',
    name: item.name ?? item.partName ?? '',
    customer: item.customer ?? '',
    cycleTimeSeconds: item.cycleTimeSeconds ?? null,
  }
}

async function loadData() {
  loading.value = true
  try {
    const [productsRes, linesRes, machinesRes] = await Promise.all([
      masterApi.getProducts(),
      masterApi.getLines(),
      masterApi.getMachines(),
    ])
    products.value = (productsRes || []).map(normalizeProduct)
    lineOptions.value = (linesRes || []).map(item => item.code ?? item.lineCode).filter(Boolean)
    machineOptions.value = (machinesRes || []).map(item => item.machineCode).filter(Boolean)

    const processLists = await Promise.all(products.value.map(product =>
      masterApi.getProductProcesses(product.id).catch(() => [])
    ))
    rows.value = products.value.flatMap((product, index) => {
      const processes = processLists[index] || []
      if (!processes.length) {
        return [{
          productId: product.id,
          customer: product.customer,
          partNumber: product.code,
          partName: product.name,
          cycleTimeSeconds: product.cycleTimeSeconds,
          process: '',
          processId: null,
        }]
      }
      return processes.map(process => ({
        productId: product.id,
        customer: product.customer,
        partNumber: product.code,
        partName: product.name,
        processId: process.id,
        processCode: process.processCode,
        process: process.process || process.processCode || '',
        lineCode: process.lineCode || '',
        machineCode: process.machineCode || '',
        cycleTimeSeconds: process.cycleTimeSeconds ?? product.cycleTimeSeconds,
        sequence: process.sequence,
      }))
    })
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { keyword: '', customer: '' }
  currentPage.value = 1
}

function triggerImport() {
  fileInput.value?.click()
}

async function handleImportFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file || importing.value) return
  importing.value = true
  try {
    await masterApi.importProductionInfo(file)
    ElMessage.success(l('importSuccess'))
    await loadData()
  } catch (error) {
    ElMessage.error(`${l('importFailed')}: ${error.message}`)
  } finally {
    importing.value = false
  }
}

function csvValue(value) {
  return `"${String(value ?? '').replaceAll('"', '""')}"`
}

function downloadCsv(filename, headers, dataRows) {
  const content = [
    headers.map(csvValue).join(','),
    ...dataRows.map(row => headers.map(header => csvValue(row[header])).join(',')),
  ].join('\r\n')
  const blob = new Blob([`\uFEFF${content}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

function exportCsv() {
  const headers = [l('customer'), l('partNumber'), l('partName'), l('process'), l('machineCode'), l('lineCode'), 'C/T', l('sequence')]
  const dataRows = filteredRows.value.map(row => ({
    [l('customer')]: row.customer,
    [l('partNumber')]: row.partNumber,
    [l('partName')]: row.partName,
    [l('process')]: row.process,
    [l('machineCode')]: row.machineCode,
    [l('lineCode')]: row.lineCode,
    'C/T': row.cycleTimeSeconds,
    [l('sequence')]: row.sequence,
  }))
  downloadCsv(`product-processes-${new Date().toISOString().slice(0, 10)}.csv`, headers, dataRows)
}

function openProductDialog(row) {
  const product = row ? products.value.find(item => item.id === row.productId) : null
  productForm.value = row
    ? { id: row.productId, customer: row.customer, partNumber: row.partNumber, partName: row.partName, cycleTimeSeconds: product?.cycleTimeSeconds ?? row.cycleTimeSeconds }
    : { id: null, customer: '', partNumber: '', partName: '', cycleTimeSeconds: null }
  productDialogVisible.value = true
}

function openProcessDialog(row) {
  processForm.value = row?.processId
    ? {
        id: row.processId,
        productId: row.productId,
        processCode: row.processCode || '',
        process: row.process || '',
        lineCodes: splitCodes(row.lineCode),
        machineCodes: splitCodes(row.machineCode),
        cycleTimeSeconds: row.cycleTimeSeconds,
        sequence: row.sequence,
      }
    : { id: null, productId: row?.productId || products.value[0]?.id || null, processCode: '', process: '', lineCodes: [], machineCodes: [], cycleTimeSeconds: null, sequence: null }
  processDialogVisible.value = true
}

async function saveProduct() {
  if (!productForm.value.partNumber || !productForm.value.partName) {
    ElMessage.warning(l('missingProduct'))
    return
  }
  const payload = {
    partNumber: productForm.value.partNumber.trim(),
    partName: productForm.value.partName.trim(),
    customer: productForm.value.customer?.trim() || '',
    cycleTimeSeconds: productForm.value.cycleTimeSeconds != null ? Number(productForm.value.cycleTimeSeconds) : 0,
  }
  try {
    if (productForm.value.id) {
      await masterApi.updateProduct(productForm.value.id, payload)
    } else {
      await masterApi.createProduct(payload)
    }
    productDialogVisible.value = false
    ElMessage.success(l('saved'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function saveProcess() {
  if (!processForm.value.productId || !processForm.value.process) {
    ElMessage.warning(l('missingProcess'))
    return
  }
  const payload = {
    processCode: processForm.value.processCode?.trim() || null,
    process: processForm.value.process.trim(),
    lineCode: joinCodes(processForm.value.lineCodes),
    machineCode: joinCodes(processForm.value.machineCodes),
    cycleTimeSeconds: processForm.value.cycleTimeSeconds != null ? Number(processForm.value.cycleTimeSeconds) : null,
    sequence: processForm.value.sequence != null ? Number(processForm.value.sequence) : null,
  }
  try {
    if (processForm.value.id) {
      await masterApi.updateProcess(processForm.value.id, payload)
    } else {
      await masterApi.addProductProcess(processForm.value.productId, payload)
    }
    processDialogVisible.value = false
    ElMessage.success(l('saved'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function deleteProcess(row) {
  await ElMessageBox.confirm(`${l('deleteProcessConfirm')}: ${row.process}?`, l('confirmTitle'), { type: 'warning' })
  try {
    await masterApi.deleteProcess(row.processId)
    ElMessage.success(l('deleted'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function deleteProduct(row) {
  await ElMessageBox.confirm(`${l('deleteProductConfirm')}: ${row.partNumber}?`, l('confirmTitle'), { type: 'warning' })
  try {
    await masterApi.deleteProduct(row.productId)
    ElMessage.success(l('deleted'))
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function handleRowCommand(command, row) {
  if (command === 'editProduct') {
    openProductDialog(row)
  } else if (command === 'editProcess') {
    openProcessDialog(row)
  } else if (command === 'deleteProduct') {
    deleteProduct(row)
  } else if (command === 'deleteProcess') {
    deleteProcess(row)
  }
}

onMounted(loadData)

watch([filteredRows, pageSize], () => {
  const maxPage = Math.max(1, Math.ceil(filteredRows.value.length / pageSize.value))
  if (currentPage.value > maxPage) currentPage.value = maxPage
})

watch(() => [filters.value.keyword, filters.value.customer], () => {
  currentPage.value = 1
})
</script>

<style scoped>
.toolbar-row {
  display: grid;
  grid-template-columns: minmax(280px, 1fr) 220px repeat(5, max-content);
  gap: 12px;
  align-items: center;
  overflow-x: auto;
  padding-bottom: 2px;
}

:deep(.toolbar-keyword .el-input__wrapper) {
  background: #eff6ff;
  border-color: #bfdbfe;
  box-shadow: 0 0 0 1px #bfdbfe inset;
}

:deep(.toolbar-customer .el-select__wrapper) {
  background: #f0fdf4;
  border-color: #bbf7d0;
  box-shadow: 0 0 0 1px #bbf7d0 inset;
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

@media (max-width: 1180px) {
  .toolbar-row {
    grid-template-columns: minmax(260px, 1fr) 220px repeat(5, max-content);
    min-width: 980px;
  }
}

.summary-tile {
  display: flex;
  min-height: 58px;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #f8fafc;
  padding: 12px 14px;
}

.summary-tile span {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.summary-tile strong {
  color: #0f172a;
  font-size: 20px;
  font-weight: 900;
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
  flex-direction: column;
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
