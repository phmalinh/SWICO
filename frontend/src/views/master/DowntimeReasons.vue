<template>
  <div>
    <PageHeader :eyebrow="t('master.downtimeReasons.eyebrow')" :title="t('master.downtimeReasons.pageTitle')" :subtitle="t('master.downtimeReasons.pageSubtitle')">
      <!-- <template #actions>
        <el-button type="warning" :loading="importing" @click="triggerImport">
          {{ t('master.downtimeReasons.importExcel') }}
        </el-button>
        <el-button v-if="activeTab === 'categories'" type="primary" @click="openCategoryDialog()">
          <el-icon class="mr-1"><Plus /></el-icon> {{ t('master.downtimeReasons.addCategory') }}
        </el-button>
        <el-button v-else type="primary" @click="openReasonDialog()">
          <el-icon class="mr-1"><Plus /></el-icon> {{ t('master.downtimeReasons.add') }}
        </el-button>
        <el-button v-if="activeTab === 'categories'" type="danger" :disabled="selectedCategories.length === 0" @click="deleteSelectedCategories">
          {{ t('master.downtimeReasons.deleteSelected') }}
        </el-button>
        <el-button v-else type="danger" :disabled="selectedReasons.length === 0" @click="deleteSelectedReasons">
          {{ t('master.downtimeReasons.deleteSelected') }}
        </el-button>
        <input ref="fileInput" type="file" accept=".xlsx,.xls" class="hidden" @change="handleImportFile" />
      </template> -->
    </PageHeader>

    <div class="page-card overflow-hidden">
      <el-tabs v-model="activeTab" class="downtime-tabs">
        <el-tab-pane :label="t('master.downtimeReasons.tabs.categories')" name="categories">
          <div class="toolbar-row">
            <el-input v-model="categoryKeyword" clearable :placeholder="t('master.downtimeReasons.searchCategory')" class="toolbar-search" />
            <el-button type="primary" @click="categoryPage = 1">{{ t('common.search') }}</el-button>
            <el-button type="primary" @click="openCategoryDialog()">
              <el-icon class="mr-1"><Plus /></el-icon> {{ t('master.downtimeReasons.addCategory') }}
            </el-button>
            <el-button  type="danger" :disabled="selectedCategories.length === 0" @click="deleteSelectedCategories">
              {{ t('master.downtimeReasons.deleteSelected') }}
            </el-button>
            <el-button type="warning" :loading="importing" @click="triggerImport">
              {{ t('master.downtimeReasons.importExcel') }}
            </el-button>
            <!-- <el-button type="info" @click="categoryKeyword = ''">{{ t('common.reset') }}</el-button> -->
          </div>
          <el-table :data="paginatedCategories" stripe style="width: 100%" size="large" v-loading="loading" @selection-change="handleCategorySelection">
            <el-table-column type="selection" width="48" />
            <el-table-column type="index" label="#" width="60" />
            <el-table-column prop="reasonCategoryCode" :label="t('master.downtimeReasons.table.categoryCode')" width="110" align="center">
              <template #default="{ row }">
                <el-tag effect="dark" type="info">{{ row.reasonCategoryCode }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reasonCategoryText" :label="t('master.downtimeReasons.table.categoryText')" min-width="360" show-overflow-tooltip />
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
                <el-button type="primary" link @click="openCategoryDialog(row)">{{ t('common.edit') }}</el-button>
                <el-button type="danger" link @click="removeCategory(row)">{{ t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="flex justify-end px-4 py-3 border-t border-slate-200 bg-slate-50">
            <el-pagination v-model:current-page="categoryPage" :page-size="pageSize" :total="filteredCategories.length" layout="prev, pager, next" background />
          </div>
        </el-tab-pane>

        <el-tab-pane :label="t('master.downtimeReasons.tabs.reasons')" name="reasons">
          <div class="toolbar-row">
            <el-input v-model="reasonKeyword" clearable :placeholder="t('master.downtimeReasons.searchReason')" class="toolbar-search" />
            <el-button type="primary" @click="reasonPage = 1">{{ t('common.search') }}</el-button>
            <el-button  type="primary" @click="openReasonDialog()">
              <el-icon class="mr-1"><Plus /></el-icon> {{ t('master.downtimeReasons.add') }}
            </el-button>
            <el-button type="danger" :disabled="selectedReasons.length === 0" @click="deleteSelectedReasons">
              {{ t('master.downtimeReasons.deleteSelected') }}
            </el-button>


            <!-- <el-button type="info" @click="reasonKeyword = ''">{{ t('common.reset') }}</el-button> -->
          </div>
          <el-table :data="paginatedReasons" stripe style="width: 100%" size="large" v-loading="loading" @selection-change="handleReasonSelection">
            <el-table-column type="selection" width="48" />
            <el-table-column type="index" label="#" width="60" />
            <el-table-column prop="reasonCategoryCode" :label="t('master.downtimeReasons.table.categoryCode')" width="110" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.reasonCategoryCode" effect="plain">{{ row.reasonCategoryCode }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reasonCode" :label="t('master.downtimeReasons.table.reasonCode')" width="100" align="center">
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
                <el-button type="primary" link @click="openReasonDialog(row)">{{ t('common.edit') }}</el-button>
                <el-button type="danger" link @click="removeReason(row)">{{ t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="flex justify-end px-4 py-3 border-t border-slate-200 bg-slate-50">
            <el-pagination v-model:current-page="reasonPage" :page-size="pageSize" :total="filteredReasons.length" layout="prev, pager, next" background />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="categoryDialogVisible" :title="categoryEditId ? t('master.downtimeReasons.dialog.categoryTitleEdit') : t('master.downtimeReasons.dialog.categoryTitleCreate')" width="560px" destroy-on-close>
      <el-form :model="categoryForm" label-position="top">
        <div class="grid gap-3 md:grid-cols-2">
          <el-form-item :label="t('master.downtimeReasons.dialog.categoryCode')" required>
            <el-input v-model="categoryForm.reasonCategoryCode" placeholder="1" maxlength="20" />
          </el-form-item>
          <el-form-item :label="t('master.downtimeReasons.dialog.sortOrder')" required>
            <el-input-number v-model="categoryForm.sortOrder" :min="1" :max="999" class="!w-full" controls-position="right" />
          </el-form-item>
        </div>
        <el-form-item :label="t('master.downtimeReasons.dialog.categoryText')" required>
          <el-input v-model="categoryForm.reasonCategoryText" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="t('master.downtimeReasons.dialog.active')">
          <el-switch v-model="categoryForm.active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveCategory">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reasonDialogVisible" :title="reasonEditId ? t('master.downtimeReasons.dialog.titleEdit') : t('master.downtimeReasons.dialog.titleCreate')" width="640px" destroy-on-close>
      <el-form :model="reasonForm" label-position="top">
        <div class="grid gap-3 md:grid-cols-3">
          <el-form-item :label="t('master.downtimeReasons.dialog.categoryCode')">
            <el-select v-model="reasonForm.reasonCategoryCode" clearable filterable class="!w-full">
              <el-option v-for="category in categories" :key="category.reasonCategoryCode" :label="categoryLabel(category)" :value="category.reasonCategoryCode" />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('master.downtimeReasons.dialog.reasonCode')" required>
            <el-input v-model="reasonForm.reasonCode" placeholder="1-1" maxlength="20" />
          </el-form-item>
          <el-form-item :label="t('master.downtimeReasons.dialog.sortOrder')" required>
            <el-input-number v-model="reasonForm.sortOrder" :min="1" :max="999" class="!w-full" controls-position="right" />
          </el-form-item>
        </div>
        <el-form-item :label="t('master.downtimeReasons.dialog.reasonText')" required>
          <el-input v-model="reasonForm.reasonText" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item :label="t('master.downtimeReasons.dialog.active')">
          <el-switch v-model="reasonForm.active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reasonDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveReason">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()
const activeTab = ref('categories')
const categories = ref([])
const reasons = ref([])
const categoryDialogVisible = ref(false)
const reasonDialogVisible = ref(false)
const categoryEditId = ref(null)
const reasonEditId = ref(null)
const loading = ref(false)
const importing = ref(false)
const fileInput = ref(null)
const categoryForm = ref({ reasonCategoryCode: '', reasonCategoryText: '', sortOrder: 1, active: true })
const reasonForm = ref({ reasonCategoryCode: '', reasonCode: '', reasonText: '', sortOrder: 1, active: true })
const categoryKeyword = ref('')
const reasonKeyword = ref('')
const selectedCategories = ref([])
const selectedReasons = ref([])
const categoryPage = ref(1)
const reasonPage = ref(1)
const pageSize = ref(10)

const filteredCategories = computed(() => {
  const keyword = categoryKeyword.value.trim().toLowerCase()
  if (!keyword) return categories.value
  return categories.value.filter(item => [item.reasonCategoryCode, item.reasonCategoryText]
    .some(value => String(value || '').toLowerCase().includes(keyword)))
})

const filteredReasons = computed(() => {
  const keyword = reasonKeyword.value.trim().toLowerCase()
  if (!keyword) return reasons.value
  return reasons.value.filter(item => [item.reasonCategoryCode, item.reasonCode, item.reasonText]
    .some(value => String(value || '').toLowerCase().includes(keyword)))
})

const paginatedCategories = computed(() => {
  const start = (categoryPage.value - 1) * pageSize.value
  return filteredCategories.value.slice(start, start + pageSize.value)
})

const paginatedReasons = computed(() => {
  const start = (reasonPage.value - 1) * pageSize.value
  return filteredReasons.value.slice(start, start + pageSize.value)
})

function normalizeCategory(item) {
  return {
    id: item.id,
    reasonCategoryCode: item.reasonCategoryCode || '',
    reasonCategoryText: item.reasonCategoryText || '',
    sortOrder: item.sortOrder || 0,
    active: item.active !== false,
  }
}

function normalizeReason(item) {
  return {
    id: item.id,
    reasonCategoryCode: item.reasonCategoryCode || '',
    reasonCode: item.reasonCode || '',
    reasonText: item.reasonText || '',
    sortOrder: item.sortOrder || 0,
    active: item.active !== false,
  }
}

function categoryLabel(category) {
  return `${category.reasonCategoryCode} - ${category.reasonCategoryText}`.trim()
}

async function loadData() {
  loading.value = true
  try {
    const [categoryData, reasonData] = await Promise.all([
      masterApi.getDowntimeReasonCategories(),
      masterApi.getDowntimeReasons(),
    ])
    categories.value = categoryData.map(normalizeCategory)
    reasons.value = reasonData.map(normalizeReason)
    selectedCategories.value = []
    selectedReasons.value = []
  } catch (error) {
    ElMessage.error(`${t('master.downtimeReasons.messages.loadFailed')}: ${error.message}`)
  } finally {
    loading.value = false
  }
}

function handleCategorySelection(selection) {
  selectedCategories.value = selection || []
}

function handleReasonSelection(selection) {
  selectedReasons.value = selection || []
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
    const result = await masterApi.importDowntimeReasons(file)
    ElMessage.success(t('master.downtimeReasons.messages.importSuccess', {
      categories: result?.categoriesImported ?? 0,
      reasons: result?.reasonsImported ?? 0,
    }))
    await loadData()
  } catch (error) {
    ElMessage.error(`${t('master.downtimeReasons.messages.importFailed')}: ${error.message}`)
  } finally {
    importing.value = false
  }
}

function openCategoryDialog(row) {
  categoryEditId.value = row?.id || null
  categoryForm.value = row ? { ...row } : { reasonCategoryCode: '', reasonCategoryText: '', sortOrder: categories.value.length + 1, active: true }
  categoryDialogVisible.value = true
}

function openReasonDialog(row) {
  reasonEditId.value = row?.id || null
  reasonForm.value = row
    ? { ...row }
    : { reasonCategoryCode: categories.value[0]?.reasonCategoryCode || '', reasonCode: '', reasonText: '', sortOrder: reasons.value.length + 1, active: true }
  reasonDialogVisible.value = true
}

async function saveCategory() {
  if (!categoryForm.value.reasonCategoryCode || !categoryForm.value.reasonCategoryText) {
    ElMessage.warning(t('master.downtimeReasons.messages.categoryMissing'))
    return
  }
  const payload = {
    reasonCategoryCode: categoryForm.value.reasonCategoryCode.trim(),
    reasonCategoryText: categoryForm.value.reasonCategoryText.trim(),
    sortOrder: Number(categoryForm.value.sortOrder || 0),
    active: categoryForm.value.active !== false,
  }
  try {
    if (categoryEditId.value) {
      await masterApi.updateDowntimeReasonCategory(categoryEditId.value, payload)
    } else {
      await masterApi.createDowntimeReasonCategory(payload)
    }
    categoryDialogVisible.value = false
    ElMessage.success(t('master.downtimeReasons.messages.saveSuccess'))
    await loadData()
  } catch (error) {
    ElMessage.error(`${t('master.downtimeReasons.messages.saveFailed')}: ${error.message}`)
  }
}

async function saveReason() {
  if (!reasonForm.value.reasonCode || !reasonForm.value.reasonText) {
    ElMessage.warning(t('master.downtimeReasons.messages.missing'))
    return
  }
  const payload = {
    reasonCategoryCode: reasonForm.value.reasonCategoryCode || null,
    reasonCode: reasonForm.value.reasonCode.trim(),
    reasonText: reasonForm.value.reasonText.trim(),
    sortOrder: Number(reasonForm.value.sortOrder || 0),
    active: reasonForm.value.active !== false,
  }
  try {
    if (reasonEditId.value) {
      await masterApi.updateDowntimeReason(reasonEditId.value, payload)
    } else {
      await masterApi.createDowntimeReason(payload)
    }
    reasonDialogVisible.value = false
    ElMessage.success(t('master.downtimeReasons.messages.saveSuccess'))
    await loadData()
  } catch (error) {
    ElMessage.error(`${t('master.downtimeReasons.messages.saveFailed')}: ${error.message}`)
  }
}

async function removeCategory(row) {
  await ElMessageBox.confirm(`${t('common.delete')} ${row.reasonCategoryCode}?`, t('common.confirm'), { type: 'warning' })
  try {
    await masterApi.deleteDowntimeReasonCategory(row.id)
    ElMessage.success(t('master.downtimeReasons.messages.categoryDeleteSuccess'))
    await loadData()
  } catch (error) {
    ElMessage.error(`${t('master.downtimeReasons.messages.deleteFailed')}: ${error.message}`)
  }
}

async function deleteSelectedCategories() {
  if (!selectedCategories.value.length) return
  await ElMessageBox.confirm(t('master.downtimeReasons.messages.deleteSelectedCategoriesConfirm', { count: selectedCategories.value.length }), t('common.confirm'), { type: 'warning' })
  try {
    await Promise.all(selectedCategories.value.map(row => masterApi.deleteDowntimeReasonCategory(row.id)))
    selectedCategories.value = []
    ElMessage.success(t('master.downtimeReasons.messages.categoryDeleteSuccess'))
    await loadData()
  } catch (error) {
    ElMessage.error(`${t('master.downtimeReasons.messages.deleteFailed')}: ${error.message}`)
  }
}

async function removeReason(row) {
  await ElMessageBox.confirm(`${t('common.delete')} ${row.reasonCode}?`, t('common.confirm'), { type: 'warning' })
  try {
    await masterApi.deleteDowntimeReason(row.id)
    ElMessage.success(t('master.downtimeReasons.messages.deleteSuccess'))
    await loadData()
  } catch (error) {
    ElMessage.error(`${t('master.downtimeReasons.messages.deleteFailed')}: ${error.message}`)
  }
}

async function deleteSelectedReasons() {
  if (!selectedReasons.value.length) return
  await ElMessageBox.confirm(t('master.downtimeReasons.messages.deleteSelectedReasonsConfirm', { count: selectedReasons.value.length }), t('common.confirm'), { type: 'warning' })
  try {
    await Promise.all(selectedReasons.value.map(row => masterApi.deleteDowntimeReason(row.id)))
    selectedReasons.value = []
    ElMessage.success(t('master.downtimeReasons.messages.deleteSuccess'))
    await loadData()
  } catch (error) {
    ElMessage.error(`${t('master.downtimeReasons.messages.deleteFailed')}: ${error.message}`)
  }
}

watch(categoryKeyword, () => {
  categoryPage.value = 1
})

watch(reasonKeyword, () => {
  reasonPage.value = 1
})

onMounted(loadData)
</script>

<style scoped>
:deep(.downtime-tabs .el-tabs__header) {
  margin: 0;
  padding: 0 16px;
  background: #f8fafc;
}

.toolbar-row {
  display: grid;
  /* grid-template-columns: minmax(260px, 1fr) max-content max-content; */
  grid-template-columns: minmax(280px, 1fr) 120px repeat(5, max-content);
  gap: 12px;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #e2e8f0;
  background: #fff;
}

:deep(.toolbar-search .el-input__wrapper) {
  background: #eff6ff;
  border-color: #bfdbfe;
  box-shadow: 0 0 0 1px #bfdbfe inset;
}

@media (max-width: 720px) {
  .toolbar-row {
    grid-template-columns: 1fr;
  }
}
</style>
