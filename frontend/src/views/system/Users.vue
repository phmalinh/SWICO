<template>
  <div>
    <PageHeader :eyebrow="'4.1 ' + t('layout.system')" :title="t('users.pageTitle')" :subtitle="t('users.pageSubtitle')">
      <!-- <template #actions>
        <el-button type="success" :loading="importing" @click="triggerImport"><el-icon class="mr-1"><Upload /></el-icon> {{ t('users.importExcel') }}</el-button>
        <el-button type="primary" @click="openDialog()"><el-icon class="mr-1"><Plus /></el-icon> {{ t('users.createAccount') }}</el-button>
        <input ref="fileInput" type="file" accept=".xlsx,.xls" class="hidden" @change="handleImportFile" />
      </template> -->
    </PageHeader>

    <div class="mb-5 grid grid-cols-2 gap-3 md:grid-cols-4">
      <div v-for="role in roleSummary" :key="role.key" class="stat-card !p-4">
        <div class="text-xs font-black uppercase text-slate-500">{{ role.label }}</div>
        <div class="text-2xl font-black" :class="role.color">{{ role.count }}</div>
      </div>
    </div>

    <div class="page-card overflow-hidden">
      <div class="toolbar-row">
        <el-input
          v-model="keyword"
          clearable
          :placeholder="t('users.searchPlaceholder')"
         
        />
        <el-button type="success" :loading="importing" @click="triggerImport"><el-icon class="mr-1"><Upload /></el-icon> {{ t('users.importExcel') }}</el-button>
        <el-button type="primary" @click="openDialog()"><el-icon class="mr-1"><Plus /></el-icon> {{ t('users.createAccount') }}</el-button>
        <input ref="fileInput" type="file" accept=".xlsx,.xls" class="hidden" @change="handleImportFile" />
        <el-button type="danger" plain :disabled="selectedUsers.length === 0" @click="deleteSelectedUsers">
          {{ t('users.deleteSelected') }}
        </el-button>
        
      </div>
      <el-table :data="paginatedUsers" stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="username" :label="t('users.table.account')" width="150">
          <template #default="{ row }"><span class="font-mono font-bold">{{ row.username }}</span></template>
        </el-table-column>
        <el-table-column prop="fullName" :label="t('users.table.fullName')" min-width="170" />
        <el-table-column prop="jobTitle" :label="t('users.table.jobTitle')" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ row.jobTitle || '-' }}</template>
        </el-table-column>
        <el-table-column prop="team" :label="t('users.table.team')" width="110" align="center">
          <template #default="{ row }">{{ row.team || '-' }}</template>
        </el-table-column>
        <el-table-column prop="hireDate" :label="t('users.table.hireDate')" width="130" align="center">
          <template #default="{ row }">{{ row.hireDate || '-' }}</template>
        </el-table-column>
        <el-table-column prop="role" :label="t('users.table.role')" width="150" align="center">
          <template #default="{ row }"><el-tag :type="roleTagType(row.role)" effect="dark" size="small">{{ row.role }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="lineCode" :label="t('users.table.line')" width="100" align="center">
          <template #default="{ row }">{{ row.lineCode || '-' }}</template>
        </el-table-column>
        <el-table-column prop="active" :label="t('users.table.status')" width="120" align="center">
          <template #default="{ row }"><el-tag :type="row.active ? 'success' : 'info'" size="small">{{ row.active ? t('users.table.active') : t('users.table.locked') }}</el-tag></template>
        </el-table-column>
        <el-table-column :label="t('users.table.actions')" width="210" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">{{ t('users.table.edit') }}</el-button>
            <el-button type="danger" link @click="toggleActive(row)">{{ row.active ? t('users.table.lock') : t('users.table.unlock') }}</el-button>
            <el-button type="danger" link @click="deleteUser(row)">{{ t('users.table.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="filteredUsers.length"
          layout="prev, pager, next"
          background
        />
    </div>

    <div class="page-card mt-5 p-5">
      <h3 class="section-title mb-3">{{ t('users.permissionMatrix.title') }}</h3>
      <el-table :data="permissionMatrix" size="small" border>
        <el-table-column prop="role" :label="t('users.permissionMatrix.role')" width="120" />
        <el-table-column prop="menu1" :label="t('users.permissionMatrix.menuProduction')" />
        <el-table-column prop="menu2" :label="t('users.permissionMatrix.menuMonitoring')" />
        <el-table-column prop="menu3" :label="t('users.permissionMatrix.menuMaster')" />
        <el-table-column prop="menu4" :label="t('users.permissionMatrix.menuSystem')" />
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? t('users.dialog.editTitle') : t('users.dialog.createTitle')" width="480px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item :label="t('users.table.account')" required><el-input v-model="form.username" /></el-form-item>
        <el-form-item :label="t('users.table.fullName')" required><el-input v-model="form.fullName" /></el-form-item>
        <el-form-item :label="t('users.dialog.role')" required>
          <el-select v-model="form.role" class="w-full">
            <el-option :label="t('users.roleOptions.operator')" value="ROLE_OPERATOR" />
            <el-option :label="t('users.roleOptions.leader')" value="ROLE_LEADER" />
            <el-option :label="t('users.roleOptions.manager')" value="ROLE_MANAGER" />
            <el-option :label="t('users.roleOptions.admin')" value="ROLE_ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('users.dialog.line')">
          <el-select v-model="form.lineCode" clearable class="w-full">
            <el-option v-for="l in lines" :key="l.lineCode" :label="l.lineCode" :value="l.lineCode" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('users.dialog.jobTitle')"><el-input v-model="form.jobTitle" /></el-form-item>
        <el-form-item :label="t('users.dialog.team')"><el-input v-model="form.team" /></el-form-item>
        <el-form-item :label="t('users.dialog.hireDate')">
          <el-date-picker v-model="form.hireDate" type="date" value-format="YYYY-MM-DD" class="w-full" />
        </el-form-item>
        <el-form-item :label="t('users.dialog.password')" :required="!editId">
          <el-input type="password" v-model="form.password" autocomplete="new-password" :placeholder="t('users.dialog.password')" />
          <p class="text-xs text-slate-500 mt-1">{{ t('users.dialog.passwordHint') }}</p>
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
import { ref, computed, onMounted, watch } from 'vue'
import { Plus, Upload } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { masterApi, userApi } from '@/services/api'
import { useI18n } from '@/i18n'

const { t } = useI18n()

const users = ref([])
const lines = ref([])
const importing = ref(false)
const fileInput = ref(null)
const keyword = ref('')
const selectedUsers = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const defaultForm = () => ({ username: '', fullName: '', password: '', role: 'ROLE_OPERATOR', lineCode: null, jobTitle: '', team: '', hireDate: '', active: true })
const form = ref(defaultForm())
const currentPage = ref(1)
const pageSize = ref(10)

const filteredUsers = computed(() => {
  const term = keyword.value.trim().toLowerCase()
  if (!term) return users.value
  return users.value.filter(user => [
    user.username,
    user.fullName,
    user.jobTitle,
    user.team,
    user.role,
    user.lineCode,
  ].some(value => String(value || '').toLowerCase().includes(term)))
})

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

watch(keyword, () => {
  currentPage.value = 1
})
const roleSummary = computed(() => [
  { key: 'ROLE_OPERATOR', label: t('users.summary.operator'), count: users.value.filter(u => u.role === 'ROLE_OPERATOR').length, color: 'text-sky-600' },
  { key: 'ROLE_LEADER', label: t('users.summary.leader'), count: users.value.filter(u => u.role === 'ROLE_LEADER').length, color: 'text-emerald-600' },
  { key: 'ROLE_MANAGER', label: t('users.summary.manager'), count: users.value.filter(u => u.role === 'ROLE_MANAGER').length, color: 'text-amber-600' },
  { key: 'ROLE_ADMIN', label: t('users.summary.admin'), count: users.value.filter(u => u.role === 'ROLE_ADMIN').length, color: 'text-rose-600' },
])

const permissionMatrix = computed(() => [
  { role: 'ROLE_OPERATOR', menu1: t('users.permissionMatrix.entry'), menu2: '-', menu3: '-', menu4: '-' },
  { role: 'ROLE_LEADER', menu1: t('users.permissionMatrix.entryHistory'), menu2: t('users.permissionMatrix.search'), menu3: '-', menu4: '-' },
  { role: 'ROLE_MANAGER', menu1: '-', menu2: '2.1 + 2.2 + 2.3', menu3: t('users.permissionMatrix.all'), menu4: '-' },
  { role: 'ROLE_ADMIN', menu1: t('users.permissionMatrix.all'), menu2: t('users.permissionMatrix.all'), menu3: t('users.permissionMatrix.all'), menu4: t('users.permissionMatrix.all') },
])

function roleTagType(role) {
  return { ROLE_OPERATOR: '', ROLE_LEADER: 'success', ROLE_MANAGER: 'warning', ROLE_ADMIN: 'danger' }[role] || 'info'
}

function onSelectionChange(selection) {
  selectedUsers.value = selection
}

async function loadUsers() {
  try {
    users.value = await userApi.list()
  } catch (error) {
    ElMessage.error(error.message || 'Không tải được danh sách người dùng')
  }
}

async function loadLines() {
  try {
    const data = await masterApi.getLines()
    lines.value = (data || []).map(item => ({
      id: item.id,
      lineCode: item.code ?? item.lineCode ?? item.name,
      description: item.name ?? item.description,
    }))
  } catch (error) {
    lines.value = []
  }
}

function triggerImport() {
  fileInput.value?.click()
}

async function handleImportFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return

  importing.value = true
  try {
    const result = await userApi.importUsers(file)
    await loadUsers()
    selectedUsers.value = []
    ElMessage.success(t('users.messages.importSuccess', {
      created: result.created ?? 0,
      updated: result.updated ?? 0,
      skipped: result.skipped ?? 0,
    }))
  } catch (error) {
    ElMessage.error(`${t('users.messages.importFailed')}: ${error.message}`)
  } finally {
    importing.value = false
  }
}

function openDialog(row) {
  editId.value = row?.id || null
  form.value = row
    ? { ...defaultForm(), ...row, password: '' }
    : defaultForm()
  dialogVisible.value = true
}

async function save() {
  if (!form.value.username || !form.value.fullName || (!editId.value && !form.value.password)) {
    ElMessage.warning(t('users.messages.fillAll'))
    return
  }

  const payload = {
    username: form.value.username,
    fullName: form.value.fullName,
    password: form.value.password || undefined,
    role: form.value.role,
    lineCode: form.value.lineCode,
    jobTitle: form.value.jobTitle || '',
    team: form.value.team || '',
    hireDate: form.value.hireDate || null,
    active: form.value.active,
  }

  try {
    if (editId.value) {
      const updated = await userApi.update(editId.value, payload)
      const idx = users.value.findIndex(u => u.id === editId.value)
      if (idx >= 0) users.value[idx] = updated
      ElMessage.success(t('users.messages.updated'))
    } else {
      const created = await userApi.create(payload)
      users.value.push(created)
      ElMessage.success(t('users.messages.created'))
    }
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || 'Lưu tài khoản thất bại')
  }
}

async function toggleActive(row) {
  try {
    await ElMessageBox.confirm(t('users.messages.toggleConfirm', { action: row.active ? t('users.table.lock') : t('users.table.unlock'), username: row.username }), t('common.confirm'), {
      type: 'warning',
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
    })
    const payload = {
      username: row.username,
      fullName: row.fullName,
      password: undefined,
      role: row.role,
      lineCode: row.lineCode,
      jobTitle: row.jobTitle || '',
      team: row.team || '',
      hireDate: row.hireDate || null,
      active: !row.active,
    }
    const updated = await userApi.update(row.id, payload)
    row.active = updated.active
    ElMessage.success(row.active ? t('users.messages.opened') : t('users.messages.locked'))
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || 'Không thể thay đổi trạng thái')
    }
  }
}

async function deleteUser(row) {
  try {
    await ElMessageBox.confirm(t('users.messages.deleteConfirm', { username: row.username }), t('common.confirm'), {
      type: 'warning',
      confirmButtonText: t('users.table.delete'),
      cancelButtonText: t('common.cancel'),
    })
    await userApi.delete(row.id)
    users.value = users.value.filter(user => user.id !== row.id)
    ElMessage.success(t('users.messages.deleted'))
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('users.messages.deleteFailed'))
    }
  }
}

async function deleteSelectedUsers() {
  if (!selectedUsers.value.length) return
  try {
    await ElMessageBox.confirm(
      t('users.messages.deleteSelectedConfirm', { count: selectedUsers.value.length }),
      t('common.confirm'),
      {
        type: 'warning',
        confirmButtonText: t('users.table.delete'),
        cancelButtonText: t('common.cancel'),
      }
    )
    const selectedIds = selectedUsers.value.map(user => user.id)
    for (const id of selectedIds) {
      await userApi.delete(id)
    }
    users.value = users.value.filter(user => !selectedIds.includes(user.id))
    selectedUsers.value = []
    ElMessage.success(t('users.messages.deleted'))
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('users.messages.deleteFailed'))
    }
  }
}

onMounted(() => {
  loadUsers()
  loadLines()
})
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
  grid-template-columns: minmax(350px, 1fr) 120px repeat(5, max-content);
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
