import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'
import { getSession } from '@/services/auth'

const ExportV9View = () => import('@/views/reports/ExportV9.vue')

const routes = [
  {
    path: '/',
    component: AppLayout,
    redirect: '/production/entry',
    meta: { requiresAuth: true },
    children: [
      { path: 'production/entry', name: 'ProductionEntry', component: () => import('@/views/production/ProductionEntry.vue'), meta: { title: 'routes.productionEntry', roles: ['ROLE_OPERATOR','ROLE_LEADER','ROLE_MANAGER','ROLE_ADMIN'] } },
      { path: 'production/history', name: 'ProductionHistory', component: () => import('@/views/production/ProductionHistory.vue'), meta: { title: 'routes.productionHistory', roles: ['ROLE_LEADER','ROLE_MANAGER','ROLE_ADMIN'] } },
      { path: 'reports/oee-dashboard', name: 'OeeDashboard', component: () => import('@/views/reports/OeeDashboard.vue'), meta: { title: 'routes.oeeDashboard', roles: ['ROLE_LEADER','ROLE_MANAGER','ROLE_ADMIN'] } },
      { path: 'reports/search', name: 'ReportSearch', component: ExportV9View, meta: { title: 'routes.reportSearch', roles: ['ROLE_LEADER','ROLE_MANAGER','ROLE_ADMIN'] } },
      { path: 'reports/export-v9', name: 'ExportV9', component: ExportV9View, meta: { title: 'routes.exportV9', roles: ['ROLE_MANAGER','ROLE_ADMIN'] } },
      { path: 'master/products', name: 'Products', component: () => import('@/views/master/Products.vue'), meta: { title: 'routes.products', roles: ['ROLE_MANAGER','ROLE_ADMIN'] } },
      { path: 'master/processes', name: 'Processes', component: () => import('@/views/master/Processes.vue'), meta: { title: 'routes.processes', roles: ['ROLE_MANAGER','ROLE_ADMIN'] } },
      { path: 'master/lines', name: 'Lines', component: () => import('@/views/master/Lines.vue'), meta: { title: 'routes.lines', roles: ['ROLE_MANAGER','ROLE_ADMIN'] } },
      { path: 'master/shifts', name: 'Shifts', component: () => import('@/views/master/Shifts.vue'), meta: { title: 'routes.shifts', roles: ['ROLE_MANAGER','ROLE_ADMIN'] } },
      { path: 'master/machines', name: 'Machines', component: () => import('@/views/master/Machines.vue'), meta: { title: 'routes.machines', roles: ['ROLE_MANAGER','ROLE_ADMIN'] } },
      { path: 'master/downtime-reasons', name: 'DowntimeReasons', component: () => import('@/views/master/DowntimeReasons.vue'), meta: { title: 'routes.downtimeReasons', roles: ['ROLE_MANAGER','ROLE_ADMIN'] } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: 'routes.profile', roles: ['ROLE_OPERATOR','ROLE_LEADER','ROLE_MANAGER','ROLE_ADMIN'] } },
      { path: 'system/users', name: 'Users', component: () => import('@/views/system/Users.vue'), meta: { title: 'routes.users', roles: ['ROLE_ADMIN'] } },
      { path: 'system/logs', name: 'AuditLogs', component: () => import('@/views/system/AuditLogs.vue'), meta: { title: 'routes.auditLogs', roles: ['ROLE_ADMIN'] } },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: 'Đăng Nhập' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const session = getSession()
  const isAuthenticated = !!session.token

  if (to.name === 'Login' && isAuthenticated) {
    next({ path: '/' })
    return
  }

  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: 'Login' })
    return
  }

  if (isAuthenticated && session.mustChangePassword && to.name !== 'Profile') {
    next({ name: 'Profile' })
    return
  }

  const allowedRoles = to.meta.roles || []
  if (isAuthenticated && allowedRoles.length > 0 && !allowedRoles.includes(session.role)) {
    next({ path: '/' })
    return
  }

  next()
})

export default router
