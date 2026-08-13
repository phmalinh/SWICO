<template>
  <div>
    <PageHeader :eyebrow="t('master.downtimeReasons.eyebrow')" :title="t('master.downtimeReasons.pageTitle')" :subtitle="t('master.downtimeReasons.pageSubtitle')">
      <template #actions>
        <el-button type="primary" @click="openDialog()"><el-icon class="mr-1"><Plus /></el-icon> {{ t('master.downtimeReasons.add') }}</el-button>
      </template>
    </PageHeader>

    <div class="page-card overflow-hidden">
      <el-table :data="paginatedReasons" stripe style="width: 100%" size="large" v-loading="loading">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="reasonCode" :label="t('master.downtimeReasons.table.reasonCode')" width="90" align="center">
          <template #default="{ row }">
            <el-tag effect="dark" type="info">{{ row.reasonCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reasonText" :label="t('master.downtimeReasons.table.reasonText')" min-width="420" show-overflow-tooltip />
        <el-table-column prop="sortOrder" :label="t('master.downtimeReasons.table.sortOrder')" width="110" align="center" />
        <el-table-column prop="active" :label="t('master.downtimeReasons.table.active')" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'info'" effect="plain">
              {{ row.active ? t('common.active') : t('common.locked') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('master.downtimeReasons.table.actions')" width="150" align="center">
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
          :total="reasons.length"
          layout="prev, pager, next"
          background
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? t('master.downtimeReasons.dialog.titleEdit') : t('master.downtimeReasons.dialog.titleCreate')" width="640px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <div class="grid gap-3 md:grid-cols-2">
          <el-form-item :label="t('master.downtimeReasons.dialog.reasonCode')" required>
            <el-input v-model="form.reasonCode" placeholder="A" maxlength="20" />
          </el-form-item>
          <el-form-item :label="t('master.downtimeReasons.dialog.sortOrder')" required>
            <el-input-number v-model="form.sortOrder" :min="1" :max="999" class="!w-full" controls-position="right" />
          </el-form-item>
        </div>
        <el-form-item :label="t('master.downtimeReasons.dialog.reasonText')" required>
          <el-input v-model="form.reasonText" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item :label="t('master.downtimeReasons.dialog.active')">
          <el-switch v-model="form.active" />
        </el-form-item>
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
const reasons = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const loading = ref(false)
const form = ref({ reasonCode: '', reasonText: '', sortOrder: 1, active: true })
const currentPage = ref(1)
const pageSize = ref(10)

const paginatedReasons = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return reasons.value.slice(start, start + pageSize.value)
})

function normalizeReason(item) {
  return {
    id: item.id,
    reasonCode: item.reasonCode || '',
    reasonText: item.reasonText || '',
    sortOrder: item.sortOrder || 0,
    active: item.active !== false,
  }
}

async function loadReasons() {
  loading.value = true
  try {
    const data = await masterApi.getDowntimeReasons()
    reasons.value = data.map(normalizeReason)
  } catch (error) {
    ElMessage.error(`${t('master.downtimeReasons.messages.loadFailed')}: ${error.message}`)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editId.value = row?.id || null
  form.value = row ? { ...row } : { reasonCode: '', reasonText: '', sortOrder: reasons.value.length + 1, active: true }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.reasonCode || !form.value.reasonText) {
    ElMessage.warning(t('master.downtimeReasons.messages.missing'))
    return
  }

  try {
    const payload = {
      reasonCode: form.value.reasonCode.trim(),
      reasonText: form.value.reasonText.trim(),
      sortOrder: Number(form.value.sortOrder || 0),
      active: form.value.active !== false,
    }
    if (editId.value) {
      await masterApi.updateDowntimeReason(editId.value, payload)
    } else {
      await masterApi.createDowntimeReason(payload)
    }
    dialogVisible.value = false
    ElMessage.success(t('master.downtimeReasons.messages.saveSuccess'))
    await loadReasons()
  } catch (error) {
    ElMessage.error(`${t('master.downtimeReasons.messages.saveFailed')}: ${error.message}`)
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`${t('common.delete')} ${row.reasonCode}?`, t('common.confirm'), { type: 'warning' })
  try {
    await masterApi.deleteDowntimeReason(row.id)
    ElMessage.success(t('master.downtimeReasons.messages.deleteSuccess'))
    await loadReasons()
  } catch (error) {
    ElMessage.error(`${t('master.downtimeReasons.messages.deleteFailed')}: ${error.message}`)
  }
}

onMounted(loadReasons)
</script>
