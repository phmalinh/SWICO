<template>
  <div>
    <PageHeader :eyebrow="t('master.processes.eyebrow')" :title="t('master.processes.pageTitle')" />

    <div class="page-card">
      <div class="flex items-center gap-3 mb-4">
        <el-select v-model="selectedProductId" clearable filterable placeholder="Chọn mã hàng" style="width:320px" @change="loadProcesses">
          <el-option v-for="p in products" :key="p.id" :label="`${p.code || p.partNumber} - ${p.name || p.partName}`" :value="p.id" />
        </el-select>
        <el-button type="primary" @click="openAdd()">{{ t('master.processes.add') }}</el-button>
      </div>

      <el-table :data="processes" stripe style="width:100%">
        <el-table-column prop="id" label="#" width="60" />
        <el-table-column prop="process" :label="t('master.processes.table.process')" />
        <el-table-column prop="lineCode" :label="t('common.line')" width="120" />
        <el-table-column prop="machineCode" :label="t('common.machine')" width="140" />
        <el-table-column prop="sequence" :label="t('master.processes.table.sequence')" width="120" align="center" />
        <el-table-column prop="cycleTimeSeconds" :label="t('master.processes.table.cycleTime')" width="160" align="center" />
        <el-table-column :label="t('master.products.table.actions')" width="160" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">{{ t('common.edit') }}</el-button>
            <el-button type="danger" link @click="remove(row)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? t('master.processes.dialog.edit') : t('master.processes.dialog.add')" width="520px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item :label="t('master.processes.dialog.process')"><el-input v-model="form.process" /></el-form-item>
        <el-form-item :label="t('common.line')">
          <el-select v-model="form.lineCode" clearable placeholder="Chọn line">
            <el-option v-for="line in lines" :key="line.lineCode" :label="`${line.lineCode} - ${line.description}`" :value="line.lineCode" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.machine')">
          <el-select v-model="form.machineCode" clearable placeholder="Chọn máy">
            <el-option v-for="machine in filteredMachines" :key="machine.machineCode" :label="`${machine.machineCode} - ${machine.description}`" :value="machine.machineCode" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('master.processes.dialog.sequence')"><el-input-number v-model="form.sequence" :min="1" class="!w-full" /></el-form-item>
        <el-form-item :label="t('master.processes.dialog.cycleTime')"><el-input-number v-model="form.cycleTimeSeconds" :min="0" :precision="1" class="!w-full" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="save">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi } from '@/services/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from '@/i18n'

const { t } = useI18n()
const products = ref([])
const lines = ref([])
const machines = ref([])
const processes = ref([])
const selectedProductId = ref(null)
const dialogVisible = ref(false)
const editId = ref(null)
const form = ref({ process: '', sequence: null, lineCode: '', machineCode: '', cycleTimeSeconds: null })

async function loadProducts() {
  try {
    const data = await masterApi.getProducts()
    products.value = data
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function loadProcesses() {
  if (!selectedProductId.value) {
    processes.value = []
    return
  }
  try {
    processes.value = await masterApi.getProductProcesses(selectedProductId.value)
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const filteredMachines = computed(() => {
  if (!form.value.lineCode) return machines.value
  const selectedLine = form.value.lineCode.toLowerCase()
  return machines.value.filter(machine => (machine.lineCode || '').toLowerCase() === selectedLine)
})

function openAdd() {
  if (!selectedProductId.value) {
    ElMessage.warning('Vui lòng chọn mã hàng trước')
    return
  }
  editId.value = null
  form.value = { process: '', sequence: null, lineCode: '', machineCode: '', cycleTimeSeconds: null }
  dialogVisible.value = true
}

function openEdit(row) {
  editId.value = row.id
  form.value = { process: row.process, sequence: row.sequence, lineCode: row.lineCode || '', machineCode: row.machineCode || '', cycleTimeSeconds: row.cycleTimeSeconds }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.process) {
    ElMessage.warning('Vui lòng nhập tên công đoạn')
    return
  }

  try {
    const payload = {
      process: form.value.process.trim(),
      sequence: form.value.sequence != null ? Number(form.value.sequence) : null,
      lineCode: form.value.lineCode?.trim() || null,
      machineCode: form.value.machineCode?.trim() || null,
      cycleTimeSeconds: form.value.cycleTimeSeconds != null ? Number(form.value.cycleTimeSeconds) : null,
    }
    if (editId.value) {
      await masterApi.updateProcess(editId.value, payload)
    } else {
      await masterApi.addProductProcess(selectedProductId.value, payload)
    }
    dialogVisible.value = false
    await loadProcesses()
    ElMessage.success(t('common.saveSuccess'))
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`${t('common.delete')} ${row.process}?`, t('common.confirm'), { type: 'warning' })
  try {
    await masterApi.deleteProcess(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    await loadProcesses()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(async () => {
  await Promise.all([loadProducts(), loadMasterData()])
})

async function loadMasterData() {
  try {
    const [linesRes, machinesRes] = await Promise.all([masterApi.getLines(), masterApi.getMachines()])
    lines.value = linesRes.map(item => ({ lineCode: item.code ?? item.lineCode, description: item.name ?? item.description }))
    machines.value = machinesRes.map(item => ({ machineCode: item.machineCode, description: item.description, lineCode: item.lineCode ?? '' }))
  } catch (e) {
    ElMessage.error(e.message)
  }
}
</script>
