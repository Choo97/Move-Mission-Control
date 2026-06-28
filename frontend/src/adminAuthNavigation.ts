const fallbackAdminPath = '/admin/reservations'

let isRedirectingToAdminLogin = false

const currentAdminPath = () => {
  const currentPath = `${window.location.pathname}${window.location.search}`

  if (currentPath.startsWith('/admin')) {
    return currentPath
  }

  return fallbackAdminPath
}

export const adminLoginHref = () => `/login?next=${encodeURIComponent(currentAdminPath())}`

export const adminExpiredLoginHref = () => `/login?expired&next=${encodeURIComponent(currentAdminPath())}`

export const redirectToAdminExpiredLogin = () => {
  if (isRedirectingToAdminLogin) {
    return
  }

  isRedirectingToAdminLogin = true
  window.location.assign(adminExpiredLoginHref())
}
