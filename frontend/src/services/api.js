// const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8081/api/v1'

// function normalizeToken(token) {
//   if (!token) return null
//   const trimmed = String(token).trim()
//   if (trimmed === 'null' || trimmed === 'undefined' || trimmed === '') return null
//   return trimmed
// }

// function buildQuery(params = {}) {
//   const search = new URLSearchParams()
//   Object.entries(params).forEach(([key, value]) => {
//     if (value === undefined || value === null || value === '') return
//     search.set(key, String(value))
//   })
//   const qs = search.toString()
//   return qs ? `?${qs}` : ''
// }

// function handleAuthFailure() {
//   localStorage.removeItem('swico_token')
//   localStorage.removeItem('swico_username')
//   localStorage.removeItem('swico_user')
//   localStorage.removeItem('swico_role')
//   if (window.location.pathname !== '/login') {
//     window.location.href = '/login'
//   }
// }

// async function request(path, options = {}, config = {}) {
//   const token = normalizeToken(localStorage.getItem('swico_token'))
//   const method = (options.method || 'GET').toUpperCase()

//   const headers = {
//     ...(options.headers || {}),
//   }

//   if (options.body != null) {
//     headers['Content-Type'] = 'application/json'
//   }

//   if (token) {
//     headers.Authorization = `Bearer ${token}`
//   } else if (!path.startsWith('/master-data/') || method !== 'GET') {
//     console.warn('[api] protected request without auth token', method, path)
//   }

//   const response = await fetch(`${API_BASE_URL}${path}`, {
//     ...options,
//     headers,
//   })

//   if (!response.ok) {
//     const text = await response.text()
//     if (response.status === 401) {
//       if (config.handleAuthFailure !== false) {
//         handleAuthFailure()
//       }
//       throw new Error(text || 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
//     }
//     if (response.status === 403) {
//       throw new Error('Không có quyền thực hiện thao tác này. Hãy đăng nhập bằng tài khoản manager01 hoặc admin.')
//     }
//     throw new Error(text || `Request failed with status ${response.status}`)
//   }

//   if (response.status === 204) {
//     return null
//   }

//   const contentType = response.headers.get('content-type') || ''
//   if (contentType.includes('application/json')) {
//     return response.json()
//   }

//   return response.text()
// }

// export const masterApi = {
//   getProducts: () => request('/master-data/products'),
//   createProduct: payload => request('/master-data/products', { method: 'POST', body: JSON.stringify(payload) }),
//   updateProduct: (id, payload) => request(`/master-data/products/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
//   deleteProduct: id => request(`/master-data/products/${id}`, { method: 'DELETE' }),

//   getLines: () => request('/master-data/lines'),
//   createLine: payload => request('/master-data/lines', { method: 'POST', body: JSON.stringify(payload) }),
//   updateLine: (id, payload) => request(`/master-data/lines/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
//   deleteLine: id => request(`/master-data/lines/${id}`, { method: 'DELETE' }),

//   getShifts: () => request('/master-data/shifts'),
//   createShift: payload => request('/master-data/shifts', { method: 'POST', body: JSON.stringify(payload) }),
//   updateShift: (id, payload) => request(`/master-data/shifts/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
//   deleteShift: id => request(`/master-data/shifts/${id}`, { method: 'DELETE' }),

//   getMachines: () => request('/master-data/machines'),
//   createMachine: payload => request('/master-data/machines', { method: 'POST', body: JSON.stringify(payload) }),
//   updateMachine: (id, payload) => request(`/master-data/machines/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
//   deleteMachine: id => request(`/master-data/machines/${id}`, { method: 'DELETE' }),
// }

// export const productionApi = {
//   calculate: payload => request('/production-reports/calculate', { method: 'POST', body: JSON.stringify(payload) }),
//   create: payload => request('/production-reports', { method: 'POST', body: JSON.stringify(payload) }),
//   update: (id, payload) => request(`/production-reports/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
//   deleteReports: ids => request('/production-reports', { method: 'DELETE', body: JSON.stringify(ids) }),
//   today: params => request(`/production-reports/today${buildQuery(params)}`),
//   search: params => request(`/production-reports${buildQuery(params)}`),
//   dashboard: params => request(`/production-reports/dashboard${buildQuery(params)}`),
//   exportV9: async params => {
//     const token = normalizeToken(localStorage.getItem('swico_token'))
//     const headers = {}
//     if (token) {
//       headers.Authorization = `Bearer ${token}`
//     }

//     const response = await fetch(`${API_BASE_URL}/production-reports/export-v9${buildQuery(params)}`, {
//       headers,
//     })
//     if (!response.ok) {
//       const text = await response.text()
//       throw new Error(text || `Request failed with status ${response.status}`)
//     }
//     return response.blob()
//   },
// //   importV9: async file => {
// //     const token = normalizeToken(localStorage.getItem('swico_token'))
// //     const headers = {}
// //     if (token) {
// //       headers.Authorization = `Bearer ${token}`
// //     }

// //     const formData = new FormData()
// //     formData.append('file', file)

// //     const response = await fetch(`${API_BASE_URL}/production-reports/import`, {
// //       method: 'POST',
// //       headers,
// //       body: formData,
// //     })
// //     if (!response.ok) {
// //       const text = await response.text()
// //       throw new Error(text || `Request failed with status ${response.status}`)
// //     }
// //     return response.json()
// //   },
// // }
// importV9: async file => {
//     const token = normalizeToken(localStorage.getItem('swico_token'))
//     const headers = {}
//     if (token) {
//       headers.Authorization = `Bearer ${token}`
//     }

//     const formData = new FormData()
//     formData.append('file', file)

//     const response = await fetch(`${API_BASE_URL}/production-reports/import`, {
//       method: 'POST',
//       headers,
//       body: formData,
//     })
//     if (!response.ok) {
//       const text = await response.text()
//       throw new Error(text || `Request failed with status ${response.status}`)
//     }
//     return response.json()
//   },
// }

// export const userApi = {
//   list: () => request('/system/users'),
//   create: payload => request('/system/users', { method: 'POST', body: JSON.stringify(payload) }),
//   update: (id, payload) => request(`/system/users/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
//   delete: id => request(`/system/users/${id}`, { method: 'DELETE' }),
// }

// export const systemApi = {
//   auditLogs: () => request('/system/audit-logs'),
// }

// export const authApi = {
//   me: () => request('/auth/me'),
//   updateProfile: payload => request('/auth/me', { method: 'PUT', body: JSON.stringify(payload) }),
//   changePassword: payload => request('/auth/change-password', { method: 'POST', body: JSON.stringify(payload) }, { handleAuthFailure: false }),
// }

// export { API_BASE_URL }
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8081/api/v1'
const AUTH_PATH = (import.meta.env.VITE_AUTH_PATH || '/auth').replace(/\/+$/, '')
const APP_ENV = import.meta.env.VITE_APP_ENV || 'development'
const API_TIMEOUT_MS = Number(import.meta.env.VITE_API_TIMEOUT_MS || 30000)
const ENABLE_API_LOGS = import.meta.env.VITE_ENABLE_API_LOGS === 'true' || import.meta.env.DEV

function normalizeToken(token) {
  if (!token) return null
  const trimmed = String(token).trim()
  if (trimmed === 'null' || trimmed === 'undefined' || trimmed === '') return null
  return trimmed
}

function buildQuery(params = {}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    search.set(key, String(value))
  })
  const qs = search.toString()
  return qs ? `?${qs}` : ''
}

function buildApiUrl(path = '') {
  const base = (API_BASE_URL || '').replace(/\/+$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${base}${normalizedPath}`
}

function handleAuthFailure() {
  localStorage.removeItem('swico_token')
  localStorage.removeItem('swico_username')
  localStorage.removeItem('swico_user')
  localStorage.removeItem('swico_role')
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

async function request(path, options = {}, config = {}) {
  const token = normalizeToken(localStorage.getItem('swico_token'))
  const method = (options.method || 'GET').toUpperCase()

  const headers = new Headers(options.headers || {})
  headers.set('Accept', 'application/json')

  // Nếu body KHÔNG PHẢI là FormData thì mới gán Content-Type là application/json
  if (options.body != null && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  } else if (!path.startsWith('/master-data/') || method !== 'GET') {
    console.warn('[api] protected request without auth token', method, path)
  }

  if (ENABLE_API_LOGS) {
    try {
      console.debug('[api] request', {
        env: APP_ENV,
        method,
        path,
        url: buildApiUrl(path),
        tokenPresent: !!token,
        tokenSnippet: token ? `${String(token).slice(0, 8)}...` : null,
        headers: Object.fromEntries(headers.entries()),
      })
    } catch (e) {
      /* ignore */
    }
  }

  const controller = new AbortController()
  const timeoutId = window.setTimeout(() => controller.abort(), API_TIMEOUT_MS)

  try {
    const response = await fetch(buildApiUrl(path), {
      ...options,
      headers,
      signal: controller.signal,
    })

    if (!response.ok) {
      const text = await response.text()
      if (response.status === 401) {
        if (config.handleAuthFailure !== false) {
          handleAuthFailure()
        }
        throw new Error(text || 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
      }
      if (response.status === 403) {
        throw new Error('Không có quyền thực hiện thao tác này. Hãy đăng nhập bằng tài khoản manager01 hoặc admin.')
      }
      throw new Error(text || `Request failed with status ${response.status}`)
    }

    if (response.status === 204) {
      return null
    }

    // Nếu caller yêu cầu nhận Blob (dùng cho export file Excel)
    if (config.responseType === 'blob') {
      return response.blob()
    }

    const contentType = response.headers.get('content-type') || ''
    if (contentType.includes('application/json')) {
      return response.json()
    }

    return response.text()
  } finally {
    window.clearTimeout(timeoutId)
  }
}

export const masterApi = {
  getProducts: () => request('/master-data/products'),
  createProduct: payload => request('/master-data/products', { method: 'POST', body: JSON.stringify(payload) }),
  updateProduct: (id, payload) => request(`/master-data/products/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteProduct: id => request(`/master-data/products/${id}`, { method: 'DELETE' }),

  getLines: () => request('/master-data/lines'),
  createLine: payload => request('/master-data/lines', { method: 'POST', body: JSON.stringify(payload) }),
  updateLine: (id, payload) => request(`/master-data/lines/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteLine: id => request(`/master-data/lines/${id}`, { method: 'DELETE' }),

  getShifts: () => request('/master-data/shifts'),
  createShift: payload => request('/master-data/shifts', { method: 'POST', body: JSON.stringify(payload) }),
  updateShift: (id, payload) => request(`/master-data/shifts/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteShift: id => request(`/master-data/shifts/${id}`, { method: 'DELETE' }),

  getMachines: () => request('/master-data/machines'),
  createMachine: payload => request('/master-data/machines', { method: 'POST', body: JSON.stringify(payload) }),
  updateMachine: (id, payload) => request(`/master-data/machines/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteMachine: id => request(`/master-data/machines/${id}`, { method: 'DELETE' }),
}

export const productionApi = {
  calculate: payload => request('/production-reports/calculate', { method: 'POST', body: JSON.stringify(payload) }),
  create: payload => request('/production-reports', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id, payload) => request(`/production-reports/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteReports: ids => request('/production-reports', { method: 'DELETE', body: JSON.stringify(ids) }),
  today: params => request(`/production-reports/today${buildQuery(params)}`),
  search: params => request(`/production-reports${buildQuery(params)}`),
  dashboard: params => request(`/production-reports/dashboard${buildQuery(params)}`),

  // Đã tối ưu hàm exportV9 dùng chung request()
  exportV9: params => request(`/production-reports/export-v9${buildQuery(params)}`, {}, { responseType: 'blob' }),

  // Đã tối ưu hàm importV9 dùng chung request()
  importV9: async file => {
    const token = normalizeToken(localStorage.getItem('swico_token'))

    console.debug('[api] importV9 tokenPresent:', !!token, 'tokenSnippet:', token ? `${String(token).slice(0,8)}...` : null)

    const formData = new FormData()
    formData.append('file', file)

    const headers = new Headers()
    headers.set('Accept', 'application/json')
    if (token) {
      headers.set('Authorization', `Bearer ${token}`)
    }

    return fetch(buildApiUrl('/production-reports/import'), {
      method: 'POST',
      headers,
      body: formData,
    }).then(async response => {
      const text = await response.text()
      if (!response.ok) {
        throw new Error(text || `Request failed with status ${response.status}`)
      }
      try {
        return JSON.parse(text)
      } catch {
        return text
      }
    })
  },
}

export const userApi = {
  list: () => request('/system/users'),
  create: payload => request('/system/users', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id, payload) => request(`/system/users/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  delete: id => request(`/system/users/${id}`, { method: 'DELETE' }),
}

export const systemApi = {
  auditLogs: () => request('/system/audit-logs'),
}

export const authApi = {
  me: () => request(`${AUTH_PATH}/me`),
  updateProfile: payload => request(`${AUTH_PATH}/me`, { method: 'PUT', body: JSON.stringify(payload) }),
  changePassword: payload => request(`${AUTH_PATH}/change-password`, { method: 'POST', body: JSON.stringify(payload) }, { handleAuthFailure: false }),
}

export { API_BASE_URL, AUTH_PATH, APP_ENV, API_TIMEOUT_MS, ENABLE_API_LOGS, buildApiUrl }