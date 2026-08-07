<template>
  <div>
    <PageHeader :eyebrow="t('master.products.eyebrow')" :title="t('master.products.pageTitle')" :subtitle="t('master.products.pageSubtitle')">
      <template #actions>
        <el-button type="primary" @click="openDialog()"><el-icon class="mr-1"><Plus /></el-icon> {{ t('master.products.add') }}</el-button>
        <el-button @click="ElMessage.info(t('master.products.importExcel'))"><el-icon class="mr-1"><Upload /></el-icon> {{ t('master.products.importExcel') }}</el-button>
      </template>
    </PageHeader>

    <div class="page-card overflow-hidden">
      <el-table :data="paginatedRows" stripe style="width: 100%" size="large" v-loading="loading">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="partNumber" :label="t('master.products.table.partNumber')" width="150">
          <template #default="{ row }"><span class="font-mono font-black text-sky-600">{{ row.partNumber }}</span></template>
        </el-table-column>
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
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="products.length"
          layout="prev, pager, next"
          background
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? t('master.products.dialog.titleEdit') : t('master.products.dialog.titleCreate')" width="560px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item :label="t('master.products.dialog.partNumber')" required><el-input v-model="form.partNumber" placeholder="PN-001" /></el-form-item>
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
import { Plus, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()
const products = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const loading = ref(false)
const form = ref({ partNumber: '', partName: '', cycleTimeSeconds: 10 })
const currentPage = ref(1)
const pageSize = ref(10)

const paginatedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return products.value.slice(start, end)
})

async function loadProducts() {
  loading.value = true
  try {
    const data = await masterApi.getProducts()
    products.value = data.map(item => ({
      id: item.id,
      partNumber: item.code ?? item.partNumber,
      partName: item.name ?? item.partName,
      cycleTimeSeconds: Number(item.cycleTimeSeconds ?? 10),
    }))
  } catch (error) {
    ElMessage.error(`${t('master.products.messages.loadFailed')}: ${error.message}`)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editId.value = row?.id || null
  form.value = row
    ? { ...row }
    : { partNumber: '', partName: '', cycleTimeSeconds: 10 }
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

onMounted(loadProducts)
</script>
