<template>
  <div>
    <PageHeader :eyebrow="t('master.products.eyebrow')" :title="t('master.products.pageTitle')" :subtitle="t('master.products.pageSubtitle')">

    </PageHeader>

    <div class="page-card mb-5 p-5">
      <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 class="text-lg font-black text-slate-900">{{ t('master.products.searchTitle') }}</h3>
          <p class="text-sm text-slate-500">{{ t('master.products.searchSubtitle') }}</p>
        </div>
      </div>

      <el-form :model="filters" label-position="top">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
          <el-form-item :label="t('master.products.table.partNumber')">
            <el-input v-model="filters.partNumber" clearable :placeholder="t('master.products.table.partNumber')" @keyup.enter="search" />
          </el-form-item>
          <el-form-item :label="t('master.products.table.partName')">
            <el-input v-model="filters.partName" clearable :placeholder="t('master.products.table.partName')" @keyup.enter="search" />
          </el-form-item>
          <el-form-item :label="t('master.products.table.customer')">
            <el-input v-model="filters.customer" clearable :placeholder="t('master.products.table.customer')" @keyup.enter="search" />
          </el-form-item>
        </div>
        <div class="mt-2 flex flex-col gap-2 md:flex-row md:justify-end">
          <el-button type="primary" :loading="loading" class="w-full md:w-auto" @click="search">
            <el-icon class="mr-1"><Search /></el-icon>
            {{ t('reports.search.buttons.search') }}
          </el-button>
          <el-button type="primary" @click="openDialog()"><el-icon class="mr-1"><Plus /></el-icon> {{ t('master.products.add') }}</el-button>
          <el-button :loading="importing" @click="triggerImport"><el-icon class="mr-1"><Upload /></el-icon> {{ t('master.products.importExcel') }}</el-button>
          <input ref="fileInput" type="file" accept=".xlsx,.xls" class="hidden" @change="handleImportFile" />
          <el-button class="w-full md:w-auto" @click="resetFilters">{{ t('reports.search.buttons.reset') }}</el-button>
          <el-button type="danger" :disabled="selectedRows.length === 0" class="w-full md:w-auto" @click="deleteSelected">
            {{ t('reports.search.buttons.deleteSelected') }}
          </el-button>
        </div>
      </el-form>
    </div>

    <div class="page-card overflow-hidden">
      <el-table :data="paginatedRows" stripe style="width: 100%" size="large" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="56" />
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="partNumber" :label="t('master.products.table.partNumber')" width="170">
          <template #default="{ row }"><span class="font-mono font-black text-sky-600">{{ row.partNumber }}</span></template>
        </el-table-column>
        <el-table-column prop="customer" :label="t('master.products.table.customer')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="partName" :label="t('master.products.table.partName')" min-width="220" />
        <el-table-column prop="cycleTimeSeconds" :label="t('master.products.table.cycleTime')" width="130" align="center">
          <template #default="{ row }"><el-tag type="info">{{ row.cycleTimeSeconds }} {{ t('common.units.seconds') }}</el-tag></template>
        </el-table-column>
        <el-table-column :label="t('master.products.table.actions')" width="150" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">{{ t('common.edit') }}</el-button>
            <el-button type="danger" link @click="remove(row)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end px-4 py-3 border-t border-slate-200 bg-slate-50">
        <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="filteredProducts.length" layout="prev, pager, next" background />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? t('master.products.dialog.titleEdit') : t('master.products.dialog.titleCreate')" width="560px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item :label="t('master.products.dialog.partNumber')" required><el-input v-model="form.partNumber" placeholder="PN-001" /></el-form-item>
        <el-form-item :label="t('master.products.dialog.customer')"><el-input v-model="form.customer" /></el-form-item>
        <el-form-item :label="t('master.products.dialog.partName')" required><el-input v-model="form.partName" :placeholder="t('master.products.placeholders.partName')" /></el-form-item>
        <el-form-item :label="t('master.products.dialog.cycleTime')" required><el-input-number v-model="form.cycleTimeSeconds" :min="0.1" :precision="1" :step="0.5" class="!w-full" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="save">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Plus, Search, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()
const products = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const loading = ref(false)
const importing = ref(false)
const fileInput = ref(null)
const form = ref({ partNumber: '', partName: '', customer: '', cycleTimeSeconds: 10 })
const filters = ref({ partNumber: '', partName: '', customer: '' })
const selectedRows = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

function normalize(value) {
  return String(value || '').trim().toLowerCase()
}

const filteredProducts = computed(() => {
  const partNumber = normalize(filters.value.partNumber)
  const partName = normalize(filters.value.partName)
  const customer = normalize(filters.value.customer)

  return products.value.filter(product => {
    const matchesPartNumber = !partNumber || normalize(product.partNumber).includes(partNumber)
    const matchesPartName = !partName || normalize(product.partName).includes(partName)
    const matchesCustomer = !customer || normalize(product.customer).includes(customer)
    return matchesPartNumber && matchesPartName && matchesCustomer
  })
})

const paginatedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredProducts.value.slice(start, start + pageSize.value)
})

async function loadProducts() {
  loading.value = true
  try {
    const data = await masterApi.getProducts()
    products.value = data.map(item => ({
      id: item.id,
      partNumber: item.code ?? item.partNumber,
      partName: item.name ?? item.partName,
      customer: item.customer || '',
      cycleTimeSeconds: Number(item.cycleTimeSeconds ?? 10),
    }))
  } catch (error) {
    ElMessage.error(`${t('master.products.messages.loadFailed')}: ${error.message}`)
  } finally {
    loading.value = false
  }
}

function search() {
  currentPage.value = 1
}

function resetFilters() {
  filters.value = { partNumber: '', partName: '', customer: '' }
  selectedRows.value = []
  currentPage.value = 1
}

function handleSelectionChange(selection) {
  selectedRows.value = selection || []
}

function openDialog(row) {
  editId.value = row?.id || null
  form.value = row ? { ...row } : { partNumber: '', partName: '', customer: '', cycleTimeSeconds: 10 }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.partNumber || !form.value.partName) {
    ElMessage.warning(t('master.products.messages.missing'))
    return
  }

  try {
    const payload = {
      partNumber: form.value.partNumber.trim(),
      partName: form.value.partName.trim(),
      customer: form.value.customer?.trim() || '',
      cycleTimeSeconds: Number(form.value.cycleTimeSeconds),
    }
    if (editId.value) {
      await masterApi.updateProduct(editId.value, payload)
    } else {
      await masterApi.createProduct(payload)
    }
    dialogVisible.value = false
    ElMessage.success(t('master.products.messages.saveSuccess'))
    await loadProducts()
  } catch (error) {
    ElMessage.error(`${t('master.products.messages.saveFailed')}: ${error.message}`)
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`${t('common.delete')} ${t('master.products.table.partNumber')} ${row.partNumber}?`, t('common.confirm'), { type: 'warning' })
  try {
    await masterApi.deleteProduct(row.id)
    ElMessage.success(t('master.products.messages.deleteSuccess'))
    await loadProducts()
  } catch (error) {
    ElMessage.error(`${t('master.products.messages.deleteFailed')}: ${error.message}`)
  }
}

async function deleteSelected() {
  if (!selectedRows.value.length) return
  await ElMessageBox.confirm(
    t('master.products.messages.deleteSelectedConfirm', { count: selectedRows.value.length }),
    t('common.confirm'),
    { type: 'warning' }
  )
  try {
    await Promise.all(selectedRows.value.map(row => masterApi.deleteProduct(row.id)))
    ElMessage.success(t('master.products.messages.deleteSuccess'))
    selectedRows.value = []
    await loadProducts()
    currentPage.value = 1
  } catch (error) {
    ElMessage.error(`${t('master.products.messages.deleteFailed')}: ${error.message}`)
  }
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
    const result = await masterApi.importProductionInfo(file)
    ElMessage.success(t('master.products.messages.importSuccess', result || {}))
    await loadProducts()
  } catch (error) {
    ElMessage.error(`${t('master.products.messages.importFailed')}: ${error.message}`)
  } finally {
    importing.value = false
  }
}

onMounted(loadProducts)
</script>
