<template>
  <div>
    <PageHeader :eyebrow="t('master.processes.eyebrow')" :title="t('master.processes.pageTitle')" />

    <div class="page-card">
      <div class="flex items-center gap-3 mb-4">
        <el-select v-model="selectedProductId" clearable filterable :placeholder="t('master.processes.selectProduct')" style="width:360px" @change="loadProcesses">
          <el-option v-for="p in products" :key="p.id" :label="`${p.code || p.partNumber} - ${p.name || p.partName}`" :value="p.id" />
        </el-select>
        <el-button type="primary" @click="openAdd()">{{ t('master.processes.add') }}</el-button>
        <el-button type="danger" :disabled="selectedRows.length === 0" @click="deleteSelected">{{ t('common.delete') }}</el-button>
      </div>

      <el-table :data="processes" stripe style="width:100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="56" />
        <el-table-column prop="id" label="#" width="60" />
        <el-table-column prop="processCode" :label="t('master.processes.table.processCode')" width="140" />
        <el-table-column prop="process" :label="t('master.processes.table.processRoute')" min-width="220" show-overflow-tooltip />
        <el-table-column prop="lineCode" :label="t('common.line')" min-width="160" show-overflow-tooltip />
        <el-table-column prop="machineCode" :label="t('common.machine')" min-width="180" show-overflow-tooltip />
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

    <el-dialog v-model="dialogVisible" :title="editId ? t('master.processes.dialog.edit') : t('master.processes.dialog.add')" width="560px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item :label="t('master.processes.dialog.processCode')"><el-input v-model="form.processCode" placeholder="10 / 20+30+40" /></el-form-item>
        <el-form-item :label="t('master.processes.dialog.processRoute')"><el-input v-model="form.process" /></el-form-item>
        <el-form-item :label="t('common.line')">
          <el-select v-model="form.lineCodes" multiple filterable allow-create default-first-option class="!w-full" placeholder="A1;A2;A3">
            <el-option v-for="line in lines" :key="line.lineCode" :label="`${line.lineCode} - ${line.description || ''}`" :value="line.lineCode" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.machine')">
          <el-select v-model="form.machineCodes" multiple filterable allow-create default-first-option class="!w-full" placeholder="TC28;TC29;Bang tay">
            <el-option v-for="machine in filteredMachines" :key="machine.machineCode" :label="`${machine.machineCode} - ${machine.description || ''}`" :value="machine.machineCode" />
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
const selectedRows = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const form = ref({ processCode: '', process: '', sequence: null, lineCodes: [], machineCodes: [], cycleTimeSeconds: null })

const filteredMachines = computed(() => {
  if (!form.value.lineCodes.length) return machines.value
  const selectedLines = form.value.lineCodes.map(line => String(line).toLowerCase())
  return machines.value.filter(machine => selectedLines.includes(String(machine.lineCode || '').toLowerCase()) || !machine.lineCode)
})

function splitCodes(value) {
  return String(value || '')
    .split(';')
    .map(item => item.trim())
    .filter(Boolean)
}

function joinCodes(value) {
  return Array.isArray(value) && value.length ? value.map(item => String(item).trim()).filter(Boolean).join(';') : null
}

async function loadProducts() {
  try {
    products.value = await masterApi.getProducts()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function loadMasterData() {
  try {
    const [linesRes, machinesRes] = await Promise.all([masterApi.getLines(), masterApi.getMachines()])
    lines.value = linesRes.map(item => ({ lineCode: item.code ?? item.lineCode, description: item.name ?? item.description }))
    machines.value = machinesRes.map(item => ({ machineCode: item.machineCode, description: item.description, lineCode: item.lineCode ?? '' }))
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function loadProcesses() {
  if (!selectedProductId.value) {
    processes.value = []
    selectedRows.value = []
    return
  }
  try {
    processes.value = await masterApi.getProductProcesses(selectedProductId.value)
    selectedRows.value = []
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function handleSelectionChange(selection) {
  selectedRows.value = selection || []
}

function openAdd() {
  if (!selectedProductId.value) {
    ElMessage.warning(t('master.processes.messages.selectProductFirst'))
    return
  }
  editId.value = null
  form.value = { processCode: '', process: '', sequence: null, lineCodes: [], machineCodes: [], cycleTimeSeconds: null }
  dialogVisible.value = true
}

function openEdit(row) {
  editId.value = row.id
  form.value = {
    processCode: row.processCode || '',
    process: row.process,
    sequence: row.sequence,
    lineCodes: splitCodes(row.lineCode),
    machineCodes: splitCodes(row.machineCode),
    cycleTimeSeconds: row.cycleTimeSeconds,
  }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.process) {
    ElMessage.warning(t('master.processes.messages.missing'))
    return
  }

  try {
    const payload = {
      processCode: form.value.processCode?.trim() || null,
      process: form.value.process.trim(),
      sequence: form.value.sequence != null ? Number(form.value.sequence) : null,
      lineCode: joinCodes(form.value.lineCodes),
      machineCode: joinCodes(form.value.machineCodes),
      cycleTimeSeconds: form.value.cycleTimeSeconds != null ? Number(form.value.cycleTimeSeconds) : null,
    }
    if (editId.value) {
      await masterApi.updateProcess(editId.value, payload)
    } else {
      await masterApi.addProductProcess(selectedProductId.value, payload)
    }
    dialogVisible.value = false
    await loadProcesses()
    ElMessage.success(t('master.processes.messages.saveSuccess'))
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`${t('common.delete')} ${row.process}?`, t('common.confirm'), { type: 'warning' })
  try {
    await masterApi.deleteProcess(row.id)
    ElMessage.success(t('master.processes.messages.deleteSuccess'))
    await loadProcesses()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function deleteSelected() {
  if (!selectedRows.value.length) return
  await ElMessageBox.confirm(`${t('common.delete')} ${selectedRows.value.length} ${t('master.processes.table.processRoute')}?`, t('common.confirm'), { type: 'warning' })
  try {
    await Promise.all(selectedRows.value.map(row => masterApi.deleteProcess(row.id)))
    selectedRows.value = []
    ElMessage.success(t('master.processes.messages.deleteSuccess'))
    await loadProcesses()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(async () => {
  await Promise.all([loadProducts(), loadMasterData()])
})
</script>
