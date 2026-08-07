// import { API_BASE_URL } from './api'

// function normalizeToken(token) {
//   if (!token) return null
//   const normalized = String(token).trim()
//   if (!normalized || normalized === 'null' || normalized === 'undefined') return null
//   return normalized
// }

// export async function login(username, password) {
//   const response = await fetch(`${API_BASE_URL}/auth/login`, {
//     method: 'POST',
//     headers: {
//       'Content-Type': 'application/json',
//     },
//     body: JSON.stringify({ username, password }),
//   })

//   if (!response.ok) {
//     const text = await response.text()
//     throw new Error(text || 'Login failed')
//   }

//   return response.json()
// }

// export function logout() {
//   localStorage.removeItem('swico_token')
//   localStorage.removeItem('swico_user')
//   localStorage.removeItem('swico_role')
// }

// export function setSession({ token, username, fullName, role }) {
//   if (normalizeToken(token)) {
//     localStorage.setItem('swico_token', token)
//   } else {
//     localStorage.removeItem('swico_token')
//   }
//   if (username) localStorage.setItem('swico_username', username)
//   localStorage.setItem('swico_user', fullName || username || '')
//   localStorage.setItem('swico_role', role)
//   window.dispatchEvent(new Event('swico-session-changed'))
// }

// export function getSession() {
//   return {
//     token: normalizeToken(localStorage.getItem('swico_token')),
//     username: localStorage.getItem('swico_username'),
//     fullName: localStorage.getItem('swico_user'),
//     role: localStorage.getItem('swico_role'),
//   }
// }
import { AUTH_PATH, buildApiUrl } from './api'

function normalizeToken(token) {
  if (!token) return null
  let normalized = String(token).trim()
  if (!normalized || normalized === 'null' || normalized === 'undefined') return null
  
  // Tự động loại bỏ tiền tố "Bearer " nếu Backend lỡ trả về kèm chữ Bearer
  if (normalized.toLowerCase().startsWith('bearer ')) {
    normalized = normalized.slice(7).trim()
  }
  
  return normalized
}

export async function login(username, password) {
  const response = await fetch(buildApiUrl(`${AUTH_PATH}/login`), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, password }),
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || 'Login failed')
  }

  const data = await response.json()
  
  // Hỗ trợ bắt token linh hoạt dù Backend trả về data.token, data.accessToken hay data.jwt
  const token = data.token || data.accessToken || data.jwt || data.data?.token
  const userRole = data.role || data.roles?.[0] || data.user?.role
  const mustChangePassword = Boolean(data.mustChangePassword ?? data.user?.mustChangePassword)
  
  if (token) {
    setSession({
      token: token,
      username: data.username || username,
      fullName: data.fullName || data.name || username,
      role: userRole,
      mustChangePassword,
    })
  }

  return data
}

export function logout() {
  localStorage.removeItem('swico_token')
  localStorage.removeItem('swico_user')
  localStorage.removeItem('swico_username')
  localStorage.removeItem('swico_role')
  localStorage.removeItem('swico_must_change_password')
  
  // Báo hiệu cho UI biết session đã bị xóa để redirect về /login
  window.dispatchEvent(new Event('swico-session-changed'))
}

export function setSession({ token, username, fullName, role, mustChangePassword }) {
  const cleanToken = normalizeToken(token)
  
  if (cleanToken) {
    localStorage.setItem('swico_token', cleanToken)
  } else {
    localStorage.removeItem('swico_token')
  }

  if (username) localStorage.setItem('swico_username', username)
  if (fullName) localStorage.setItem('swico_user', fullName)
  if (role) localStorage.setItem('swico_role', typeof role === 'object' ? JSON.stringify(role) : role)
  if (mustChangePassword !== undefined) {
    localStorage.setItem('swico_must_change_password', mustChangePassword ? 'true' : 'false')
  }

  window.dispatchEvent(new Event('swico-session-changed'))
}

export function getSession() {
  return {
    token: normalizeToken(localStorage.getItem('swico_token')),
    username: localStorage.getItem('swico_username'),
    fullName: localStorage.getItem('swico_user'),
    role: localStorage.getItem('swico_role'),
    mustChangePassword: localStorage.getItem('swico_must_change_password') === 'true',
  }
}
