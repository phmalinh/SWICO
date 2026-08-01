<template>
  <div>
    <PageHeader :eyebrow="t('master.lines.eyebrow')" :title="t('master.lines.pageTitle')" :subtitle="t('master.lines.pageSubtitle')">
      <template #actions>
        <el-button type="primary" @click="openDialog()"><el-icon class="mr-1"><Plus /></el-icon> {{ t('master.lines.add') }}</el-button>
      </template>
    </PageHeader>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
      <div v-for="line in lines" :key="line.id" class="page-card group p-5 transition-shadow hover:shadow-md">
        <div class="flex items-start justify-between">
          <div class="flex h-14 w-14 items-center justify-center rounded-lg bg-sky-100">
            <span class="text-xl font-black text-sky-700">{{ line.lineCode }}</span>
          </div>
          <div class="flex gap-1 opacity-0 transition-opacity group-hover:opacity-100">
            <el-button type="primary" link size="small" @click="openDialog(line)">{{ t('common.edit') }}</el-button>
            <el-button type="danger" link size="small" @click="remove(line)">{{ t('common.delete') }}</el-button>
          </div>
        </div>
        <h3 class="mt-3 font-bold text-slate-800">{{ line.description }}</h3>
        <p class="mt-1 text-xs text-slate-400">{{ t('master.lines.table.code') }}: {{ line.lineCode }}</p>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? t('master.lines.dialog.titleEdit') : t('master.lines.dialog.titleCreate')" width="420px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item :label="t('master.lines.dialog.lineCode')" required><el-input v-model="form.lineCode" placeholder="A1" /></el-form-item>
        <el-form-item :label="t('master.lines.dialog.description')" required><el-input v-model="form.description" :placeholder="t('master.lines.placeholders.description')" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="save">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()
const lines = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const loading = ref(false)
const form = ref({ lineCode: '', description: '' })

function normalizeLine(item) {
  return {
    id: item.id,
    lineCode: item.code ?? item.lineCode,
    description: item.name ?? item.description,
  }
}

async function loadLines() {
  loading.value = true
  try {
    const data = await masterApi.getLines()
    lines.value = data.map(normalizeLine)
  } catch (error) {
    ElMessage.error(`${t('master.lines.messages.loadFailed')}: ${error.message}`)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editId.value = row?.id || null
  form.value = row ? { ...row } : { lineCode: '', description: '' }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.lineCode) { ElMessage.warning(t('master.lines.messages.missing')); return }
  try {
    const payload = {
      lineCode: form.value.lineCode.trim(),
      description: form.value.description.trim(),
    }
    if (editId.value) {
      await masterApi.updateLine(editId.value, payload)
    } else {
      await masterApi.createLine(payload)
    }
    dialogVisible.value = false
    ElMessage.success(t('master.lines.messages.saveSuccess'))
    await loadLines()
  } catch (error) {
    ElMessage.error(`${t('master.lines.messages.saveFailed')}: ${error.message}`)
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`${t('common.delete')} ${t('master.lines.table.code')} ${row.lineCode}?`, t('common.confirm'), { type: 'warning' })
  try {
    await masterApi.deleteLine(row.id)
    ElMessage.success(t('master.lines.messages.deleteSuccess'))
    await loadLines()
  } catch (error) {
    ElMessage.error(`${t('master.lines.messages.deleteFailed')}: ${error.message}`)
  }
}

onMounted(loadLines)
</script>
