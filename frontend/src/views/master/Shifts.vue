<template>
  <div>
    <PageHeader :eyebrow="t('master.shifts.eyebrow')" :title="t('master.shifts.pageTitle')" :subtitle="t('master.shifts.pageSubtitle')">
      <template #actions>
        <el-button type="primary" @click="openDialog()"><el-icon class="mr-1"><Plus /></el-icon> {{ t('master.shifts.add') }}</el-button>
      </template>
    </PageHeader>

    <div class="page-card overflow-hidden">
      <el-table :data="paginatedShifts" stripe style="width: 100%" size="large" v-loading="loading">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="shiftName" :label="t('master.shifts.table.shiftName')" min-width="240">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <Clock class="h-4 w-4 text-sky-500" />
              <span class="font-bold">{{ row.shiftName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="standardTimeMinutes" :label="t('master.shifts.table.standardTime')" width="180" align="center">
          <template #default="{ row }"><el-tag type="warning" effect="plain" size="large">{{ row.standardTimeMinutes }} {{ t('common.units.minutes') }}</el-tag></template>
        </el-table-column>
        <el-table-column :label="t('master.shifts.table.converted')" width="120" align="center">
          <template #default="{ row }">{{ (row.standardTimeMinutes / 60).toFixed(1) }} {{ t('common.units.hours') }}</template>
        </el-table-column>
        <el-table-column :label="t('master.shifts.table.actions')" width="150" align="center">
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
          :total="shifts.length"
          layout="prev, pager, next"
          background
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? t('master.shifts.dialog.titleEdit') : t('master.shifts.dialog.titleCreate')" width="480px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item :label="t('master.shifts.dialog.shiftName')" required><el-input v-model="form.shiftName" :placeholder="t('master.shifts.placeholders.shiftName')" /></el-form-item>
        <el-form-item :label="t('master.shifts.dialog.standardTime')" required><el-input-number v-model="form.standardTimeMinutes" :min="60" :max="720" :step="60" class="!w-full" /></el-form-item>
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
import { Clock } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()
const shifts = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const loading = ref(false)
const form = ref({ shiftName: '', standardTimeMinutes: 480 })
const currentPage = ref(1)
const pageSize = ref(10)

const paginatedShifts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return shifts.value.slice(start, end)
})

function normalizeShift(item) {
  return {
    id: item.id,
    shiftName: item.name ?? item.shiftName,
    standardTimeMinutes: item.standardTimeMinutes,
  }
}

async function loadShifts() {
  loading.value = true
  try {
    const data = await masterApi.getShifts()
    shifts.value = data.map(normalizeShift)
  } catch (error) {
    ElMessage.error(`${t('master.shifts.messages.loadFailed')}: ${error.message}`)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editId.value = row?.id || null
  form.value = row ? { ...row } : { shiftName: '', standardTimeMinutes: 480 }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.shiftName) { ElMessage.warning(t('master.shifts.messages.missing')); return }
  try {
    const payload = {
      shiftName: form.value.shiftName.trim(),
      standardTimeMinutes: Number(form.value.standardTimeMinutes),
    }
    if (editId.value) {
      await masterApi.updateShift(editId.value, payload)
    } else {
      await masterApi.createShift(payload)
    }
    dialogVisible.value = false
    ElMessage.success(t('master.shifts.messages.saveSuccess'))
    await loadShifts()
  } catch (error) {
    ElMessage.error(`${t('master.shifts.messages.saveFailed')}: ${error.message}`)
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`${t('common.delete')} ${t('master.shifts.table.shiftName')} ${row.shiftName}?`, t('common.confirm'), { type: 'warning' })
  try {
    await masterApi.deleteShift(row.id)
    ElMessage.success(t('master.shifts.messages.deleteSuccess'))
    await loadShifts()
  } catch (error) {
    ElMessage.error(`${t('master.shifts.messages.deleteFailed')}: ${error.message}`)
  }
}

onMounted(loadShifts)
</script>
