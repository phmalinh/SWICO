<template>
  <div class="min-h-screen bg-slate-100 font-sans">
    <div v-if="isWorkshopMode" class="flex h-screen flex-col">
      <header class="border-b border-slate-800 bg-slate-950 px-4 py-3 text-white shadow-lg sm:px-6">
        <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-lg bg-sky-500 text-lg font-black text-white shadow-sm">SW</div>
            <div>
              <h1 class="text-lg font-extrabold tracking-tight sm:text-xl">{{ t('layout.appTitle') }}</h1>
              <p class="text-sm text-slate-300">{{ t('layout.appSubtitle') }}</p>
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-3">
            <div class="rounded-lg border border-slate-700 bg-slate-900 px-4 py-2">
              <span class="block text-[11px] font-bold uppercase text-slate-400">{{ t('layout.currentShift') }}</span>
              <span class="text-sm font-bold text-amber-300">全天1 06:00-18:00</span>
            </div>
            <div class="rounded-lg border border-slate-700 bg-slate-900 px-4 py-2 text-sm font-bold text-slate-200">
              {{ displayUsername }} • {{ roleLabels[userRole]?.label }}
            </div>
            <button @click="doLogout" class="rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm font-bold text-slate-200 hover:bg-slate-700">
              {{ t('common.logout') }}
            </button>
          </div>
        </div>
      </header>

      <nav v-if="workshopNavItems.length > 1" class="grid grid-cols-2 gap-2 border-b border-slate-200 bg-white p-3 sm:flex">
        <router-link
          v-for="item in workshopNavItems"
          :key="item.path"
          :to="item.path"
          class="flex min-h-14 items-center justify-center gap-2 rounded-lg border border-slate-200 px-4 text-center text-base font-extrabold text-slate-600 transition hover:border-sky-300 hover:bg-sky-50 hover:text-sky-700"
          active-class="!border-sky-500 !bg-sky-600 !text-white"
        >
          <component :is="item.icon" class="h-5 w-5" />
          {{ item.title }}
        </router-link>
      </nav>

      <main class="flex-1 overflow-y-auto bg-slate-100 p-4 md:p-6">
        <router-view />
      </main>
    </div>

    <div v-else class="flex h-screen overflow-hidden">
      <aside
        class="z-20 flex flex-col border-r border-slate-800 bg-slate-950 text-slate-300 shadow-2xl transition-all duration-300"
        :class="isCollapsed ? 'w-[76px]' : 'w-80'"
      >
        <div class="flex h-16 items-center justify-between border-b border-slate-800 px-4">
          <div v-if="!isCollapsed" class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-sky-500 text-sm font-black text-white">SW</div>
            <div>
              <h1 class="text-sm font-extrabold tracking-wide text-white">{{ t('layout.appName') }}</h1>
              <p class="text-[11px] text-slate-500">{{ t('layout.appDescription') }}</p>
            </div>
          </div>
          <button @click="isCollapsed = !isCollapsed" class="mx-auto rounded-lg p-2 text-slate-400 transition hover:bg-slate-800 hover:text-white">
            <component :is="isCollapsed ? MenuIcon : ChevronLeftIcon" class="h-5 w-5" />
          </button>
        </div>

        <div v-if="!isCollapsed" class="border-b border-slate-800 bg-slate-900 px-4 py-3">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">{{ t('layout.profile') }}</span>
            <span class="rounded-md px-2 py-1 text-[11px] font-bold" :class="roleBadgeClass">{{ roleLabels[userRole]?.label }}</span>
          </div>
        </div>

        <nav class="custom-scrollbar flex-1 space-y-2 overflow-y-auto p-3">
          <section v-for="group in visibleMenuItems" :key="group.id">
            <button
              class="flex w-full items-center justify-between rounded-lg px-3 py-2.5 text-left text-slate-300 transition hover:bg-slate-900 hover:text-white"
              :class="activeGroupId === group.id ? 'bg-slate-900 text-white' : ''"
              @click="toggleGroup(group.id)"
            >
              <span class="flex items-center gap-3">
                <component :is="group.icon" class="h-5 w-5 shrink-0 text-sky-400" />
                <span v-if="!isCollapsed" class="text-sm font-bold leading-tight">{{ group.title }}</span>
              </span>
              <component
                v-if="!isCollapsed && group.children?.length"
                :is="expandedGroups.includes(group.id) ? ChevronDownIcon : ChevronRightIcon"
                class="h-4 w-4 text-slate-500"
              />
            </button>

            <div
              v-if="(!isCollapsed && expandedGroups.includes(group.id)) || (isCollapsed && group.children?.length)"
              class="mt-1 space-y-1"
              :class="!isCollapsed ? 'ml-5 border-l border-slate-800 pl-3' : ''"
            >
              <router-link
                v-for="sub in group.children"
                :key="sub.id"
                :to="sub.path"
                class="group flex items-center gap-2.5 rounded-lg px-3 py-2 text-xs font-semibold text-slate-400 transition hover:bg-sky-500/10 hover:text-white"
                active-class="!bg-sky-500 !text-white shadow-sm"
              >
                <component :is="sub.icon" class="h-4 w-4 shrink-0 opacity-70 group-hover:opacity-100" />
                <span v-if="!isCollapsed" class="truncate">{{ sub.title }}</span>
              </router-link>
            </div>
          </section>
        </nav>

        <div class="space-y-2 border-t border-slate-800 bg-slate-950 p-3">
          <button
            class="h-10 w-full rounded-lg bg-slate-800 text-xs font-bold text-slate-200 transition hover:bg-slate-700"
            @click="doLogout"
          >
            <span v-if="!isCollapsed">{{ t('common.logout') }}</span>
            <span v-else>O</span>
          </button>
        </div>
      </aside>

      <div class="flex h-screen flex-1 flex-col overflow-hidden">
        <header class="z-10 flex h-16 items-center justify-between border-b border-slate-200 bg-white px-6 shadow-sm">
          <div>
            <h2 class="text-base font-extrabold text-slate-900">{{ currentPageTitle }}</h2>
            <p class="text-xs text-slate-500">{{ t('layout.management') }}</p>
          </div>
          <div class="flex items-center gap-3">
            <el-select v-model="locale" size="small" style="width: 110px" @change="value => setLocale(value)">
              <el-option label="Tiếng Việt" value="vi" />
              <el-option label="繁體中文" value="zh-Hant" />
            </el-select>
            <span class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs font-bold text-slate-500">{{ currentDate }}</span>
            <el-dropdown @command="handleProfileAction" placement="bottom-end" trigger="click">
              <span class="flex cursor-pointer items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-medium text-slate-700 shadow-sm">
                <div class="flex h-9 w-9 items-center justify-center rounded-full bg-slate-900 text-xs font-black text-white">{{ roleShortName }}</div>
                <span class="hidden sm:inline">{{ displayUsername }}</span>
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-slate-500" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 10.94l3.71-3.71a.75.75 0 111.06 1.06l-4.24 4.24a.75.75 0 01-1.06 0L5.21 8.29a.75.75 0 01.02-1.08z" clip-rule="evenodd" />
                </svg>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">{{ t('layout.profileLabel') }}</el-dropdown-item>
                  <el-dropdown-item command="logout">{{ t('common.logout') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </header>

        <main class="flex-1 overflow-y-auto bg-slate-50 p-5 md:p-6">
          <router-view />
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, provide, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Menu as MenuIcon,
  ChevronLeft as ChevronLeftIcon,
  ChevronDown as ChevronDownIcon,
  ChevronRight as ChevronRightIcon,
  ClipboardList, BarChart3, Database, ShieldCheck,
  FileEdit, History, LayoutDashboard, Search, FileSpreadsheet,
  Package, GitFork, Clock, Cpu, Users, FileText,
} from 'lucide-vue-next'
import { getSession, logout } from '@/services/auth'
import { roleMenus, roleLabels as roleLabelsMap } from '@/data/mockData'
import { useI18n } from '@/i18n'

const route = useRoute()
const router = useRouter()
const { t, locale, setLocale } = useI18n()

const session = ref(getSession() || {})
const userRole = computed(() => session.value?.role || 'ROLE_MANAGER')
const isCollapsed = ref(false)
const expandedGroups = ref(['1', '2', '3'])
const activeGroupId = ref('1')

function refreshSession() {
  session.value = getSession() || {}
}

window.addEventListener('swico-session-changed', refreshSession)
onBeforeUnmount(() => {
  window.removeEventListener('swico-session-changed', refreshSession)
})

const roleLabels = roleLabelsMap
const profileAction = ref('')

const displayUsername = computed(() => session.value?.fullName || session.value?.username || 'Người dùng')

function handleProfileAction(action) {
  if (!action) return
  if (action === 'profile') {
    router.push({ name: 'Profile' })
  } else if (action === 'logout') {
    doLogout()
  }
  profileAction.value = ''
}

const allMenuItems = computed(() => [
  {
    id: '1', title: t('menu.production'), icon: ClipboardList,
    children: [
      { id: '1.1', title: t('menu.entry'), icon: FileEdit, path: '/production/entry' },
      { id: '1.2', title: t('menu.history'), icon: History, path: '/production/history' },
    ],
  },
  {
    id: '2', title: t('menu.monitoring'), icon: BarChart3,
    children: [
      { id: '2.1', title: t('menu.dashboard'), icon: LayoutDashboard, path: '/reports/oee-dashboard' },
      { id: '2.2', title: t('menu.search'), icon: Search, path: '/reports/search' },
    ],
  },
  {
    id: '3', title: t('menu.master'), icon: Database,
    children: [
      { id: '3.1', title: t('menu.products'), icon: Package, path: '/master/products' },
      { id: '3.2', title: t('menu.lines'), icon: GitFork, path: '/master/lines' },
      { id: '3.3', title: t('menu.shifts'), icon: Clock, path: '/master/shifts' },
      { id: '3.4', title: t('menu.machines'), icon: Cpu, path: '/master/machines' },
    ],
  },
  {
    id: '4', title: t('menu.system'), icon: ShieldCheck,
    children: [
      { id: '4.1', title: t('menu.users'), icon: Users, path: '/system/users' },
      { id: '4.2', title: t('menu.logs'), icon: FileText, path: '/system/logs' },
    ],
  },
])

const currentDate = computed(() => new Date().toLocaleDateString(locale.value === 'zh-Hant' ? 'zh-Hant' : 'vi-VN', { weekday: 'short', year: 'numeric', month: '2-digit', day: '2-digit' }))
const isWorkshopMode = computed(() => ['ROLE_OPERATOR', 'ROLE_LEADER'].includes(userRole.value))
const roleShortName = computed(() => roleLabels[userRole.value]?.label?.slice(0, 2).toUpperCase() || 'SW')
const currentUser = computed(() => ({
  role: userRole.value,
  name: roleLabels[userRole.value]?.label || 'Người đăng nhập',
  fullName: session.value?.fullName || session.value?.username || 'Người dùng',
}))

provide('currentUser', currentUser)

const visibleMenuItems = computed(() => {
  const allowed = roleMenus[userRole.value] || []
  return allMenuItems.value
    .map(group => ({
      ...group,
      children: group.children.filter(c => allowed.includes(c.id)),
    }))
    .filter(group => group.children.length > 0)
})

const workshopNavItems = computed(() => {
  const allowed = roleMenus[userRole.value] || []
  return allMenuItems.value
    .flatMap(g => g.children)
    .filter(c => allowed.includes(c.id) && c.path.startsWith('/production'))
})

const roleBadgeClass = computed(() => {
  const colors = {
    ROLE_OPERATOR: 'bg-sky-900 text-sky-200',
    ROLE_LEADER: 'bg-emerald-900 text-emerald-200',
    ROLE_MANAGER: 'bg-amber-900 text-amber-200',
    ROLE_ADMIN: 'bg-rose-900 text-rose-200',
  }
  return colors[userRole.value] || ''
})

const currentPageTitle = computed(() => {
  const title = route.meta?.title
  if (!title) return 'SWICO MES'
  return title.startsWith('routes.') ? t(title) : title
})

function toggleGroup(id) {
  if (isCollapsed.value) isCollapsed.value = false
  activeGroupId.value = id
  expandedGroups.value = expandedGroups.value.includes(id)
    ? expandedGroups.value.filter(i => i !== id)
    : [...expandedGroups.value, id]
}

async function doLogout() {
  try {
    await ElMessageBox.confirm(t('layout.logoutConfirm'), t('layout.logoutTitle'), {
      type: 'warning',
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
    })
    logout()
    router.push({ name: 'Login' })
  } catch (error) {
    // Cancelled logout
  }
}

watch(userRole, () => {
  if (isWorkshopMode.value) router.push('/production/entry')
})
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #334155; border-radius: 999px; }
</style>
