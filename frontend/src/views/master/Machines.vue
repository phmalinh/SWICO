<template>
  <div>
    <PageHeader :eyebrow="t('master.machines.eyebrow')" :title="t('master.machines.pageTitle')" :subtitle="t('master.machines.pageSubtitle')">
      <template #actions>
        <el-button type="primary" @click="openDialog()"><el-icon class="mr-1"><Plus /></el-icon> {{ t('master.machines.add') }}</el-button>
      </template>
    </PageHeader>

    <div class="page-card overflow-hidden">
      <el-table :data="paginatedMachines" stripe style="width: 100%" size="large" v-loading="loading">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="lineCode" :label="t('master.machines.table.lineCode')" width="120" />
        <el-table-column prop="assetCode" :label="t('master.machines.table.assetCode')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="description" :label="t('master.machines.table.description')" min-width="220" />
        <el-table-column prop="machineCode" :label="t('master.machines.table.machineCode')" width="140">
          <template #default="{ row }"><span class="font-mono font-black text-slate-800">{{ row.machineCode }}</span></template>
        </el-table-column>
        <el-table-column prop="purchaseDate" :label="t('master.machines.table.purchaseDate')" width="130" align="center" />
        <el-table-column prop="custodyDepartment" :label="t('master.machines.table.custodyDepartment')" min-width="170" show-overflow-tooltip />
        
        <el-table-column :label="t('master.machines.table.actions')" width="150" align="center">
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
          :total="machines.length"
          layout="prev, pager, next"
          background
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? t('master.machines.dialog.titleEdit') : t('master.machines.dialog.titleCreate')" width="480px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item :label="t('master.machines.dialog.machineCode')" required><el-input v-model="form.machineCode" placeholder="TC-31" /></el-form-item>
        <el-form-item :label="t('master.machines.dialog.lineCode')">
          <el-select v-model="form.lineCode" clearable placeholder="A1" class="w-full">
            <el-option v-for="line in lineOptions" :key="line.value" :label="line.label" :value="line.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('master.machines.dialog.description')"><el-input v-model="form.description" /></el-form-item>
        <el-form-item :label="t('master.machines.dialog.assetCode')"><el-input v-model="form.assetCode" /></el-form-item>
        <el-form-item :label="t('master.machines.dialog.purchaseDate')">
          <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" class="!w-full" />
        </el-form-item>
        <el-form-item :label="t('master.machines.dialog.custodyDepartment')"><el-input v-model="form.custodyDepartment" /></el-form-item>
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
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()
const machines = ref([])
const lineOptions = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const loading = ref(false)
const form = ref({ machineCode: '', description: '', lineCode: '', assetCode: '', purchaseDate: '', custodyDepartment: '' })
const currentPage = ref(1)
const pageSize = ref(10)

const paginatedMachines = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return machines.value.slice(start, end)
})

function normalizeMachine(item) {
  return {
    id: item.id,
    machineCode: item.machineCode,
    description: item.description,
    lineCode: item.lineCode || '',
    assetCode: item.assetCode || '',
    purchaseDate: item.purchaseDate || '',
    custodyDepartment: item.custodyDepartment || '',
  }
}

async function loadMachines() {
  loading.value = true
  try {
    const [machinesRes, linesRes] = await Promise.all([
      masterApi.getMachines(),
      masterApi.getLines(),
    ])
    machines.value = machinesRes.map(normalizeMachine)
    lineOptions.value = (linesRes || []).map(item => ({
      value: item.code ?? item.lineCode ?? item.name,
      label: `${item.code ?? item.lineCode ?? item.name} - ${item.name ?? item.description ?? ''}`.trim(),
    }))
  } catch (error) {
    ElMessage.error(`${t('master.machines.messages.loadFailed')}: ${error.message}`)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editId.value = row?.id || null
  form.value = row ? { ...row } : { machineCode: '', description: '', lineCode: '', assetCode: '', purchaseDate: '', custodyDepartment: '' }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.machineCode) { ElMessage.warning(t('master.machines.messages.missing')); return }
  try {
    const payload = {
      machineCode: form.value.machineCode.trim(),
      description: form.value.description?.trim() || '',
      lineCode: form.value.lineCode?.trim() || '',
      assetCode: form.value.assetCode?.trim() || '',
      purchaseDate: form.value.purchaseDate || null,
      custodyDepartment: form.value.custodyDepartment?.trim() || '',
    }
    if (editId.value) {
      await masterApi.updateMachine(editId.value, payload)
    } else {
      await masterApi.createMachine(payload)
    }
    dialogVisible.value = false
    ElMessage.success(t('master.machines.messages.saveSuccess'))
    await loadMachines()
  } catch (error) {
    ElMessage.error(`${t('master.machines.messages.saveFailed')}: ${error.message}`)
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`${t('common.delete')} ${t('master.machines.table.machineCode')} ${row.machineCode}?`, t('common.confirm'), { type: 'warning' })
  try {
    await masterApi.deleteMachine(row.id)
    ElMessage.success(t('master.machines.messages.deleteSuccess'))
    await loadMachines()
  } catch (error) {
    ElMessage.error(`${t('master.machines.messages.deleteFailed')}: ${error.message}`)
  }
}

onMounted(loadMachines)
</script>
