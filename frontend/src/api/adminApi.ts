import { API_BASE_URL } from '../reservationData'
import type {
  ApiErrorResponse,
  AdminEmailSendResponse,
  AdminReservationConflictAttemptResponse,
  AdminReservationDetailResponse,
  AdminReservationListQuery,
  AdminReservationPageResponse,
  AdminSmsSendResponse,
  OperatingHolidayResponse,
  OperatingScheduleResponse,
  ReservationStatus,
} from '../types'

export class AdminAuthenticationRequiredError extends Error {
  constructor() {
    super('관리자 로그인이 필요하거나 로그인 세션이 만료되었습니다.')
    this.name = 'AdminAuthenticationRequiredError'
  }
}

export async function getOperatingSchedules() {
  const response = await fetch(`${API_BASE_URL}/api/admin/operating-schedules`, { credentials: 'include' })
  if (!response.ok) await throwApiError(response, '요일별 운영시간을 불러오지 못했습니다.')
  return response.json() as Promise<OperatingScheduleResponse[]>
}

export async function updateOperatingSchedule(
  scheduleId: number,
  schedule: Pick<OperatingScheduleResponse, 'open' | 'startTime' | 'endTime' | 'slotMinutes'>,
) {
  const response = await fetch(`${API_BASE_URL}/api/admin/operating-schedules/${scheduleId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(schedule),
  })
  if (!response.ok) await throwApiError(response, '요일별 운영시간 저장에 실패했습니다.')
  return response.json() as Promise<OperatingScheduleResponse>
}

export async function getOperatingHolidays() {
  const response = await fetch(`${API_BASE_URL}/api/admin/holidays`, { credentials: 'include' })
  if (!response.ok) await throwApiError(response, '휴무일을 불러오지 못했습니다.')
  return response.json() as Promise<OperatingHolidayResponse[]>
}

export async function addOperatingHoliday(holidayDate: string, reason: string) {
  const response = await fetch(`${API_BASE_URL}/api/admin/holidays`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ holidayDate, reason }),
  })
  if (!response.ok) await throwApiError(response, '휴무일 등록에 실패했습니다.')
  return response.json() as Promise<OperatingHolidayResponse>
}

export async function deleteOperatingHoliday(holidayId: number) {
  const response = await fetch(`${API_BASE_URL}/api/admin/holidays/${holidayId}`, {
    method: 'DELETE',
    credentials: 'include',
  })
  if (!response.ok) await throwApiError(response, '휴무일 삭제에 실패했습니다.')
}

const appendQueryParam = (params: URLSearchParams, key: string, value: string | number | boolean | undefined) => {
  if (value !== undefined && value !== '') {
    params.set(key, String(value))
  }
}

export async function getAdminReservations(query: AdminReservationListQuery = {}) {
  const params = new URLSearchParams()

  appendQueryParam(params, 'status', query.status)
  appendQueryParam(params, 'keyword', query.keyword)
  appendQueryParam(params, 'startDate', query.startDate)
  appendQueryParam(params, 'endDate', query.endDate)
  appendQueryParam(params, 'needsDistance', query.needsDistance)
  appendQueryParam(params, 'sort', query.sort)
  appendQueryParam(params, 'page', query.page)
  appendQueryParam(params, 'size', query.size)

  const queryString = params.toString()
  const response = await fetch(
    `${API_BASE_URL}/api/admin/reservations${queryString ? `?${queryString}` : ''}`,
    { credentials: 'include' },
  )

  if (!response.ok) {
    await throwApiError(response, '관리자 예약 목록을 불러오지 못했습니다.')
  }

  return response.json() as Promise<AdminReservationPageResponse>
}

export async function getAdminReservationConflictAttempts() {
  const response = await fetch(`${API_BASE_URL}/api/admin/reservation-conflict-attempts`, {
    credentials: 'include',
  })

  if (!response.ok) {
    await throwApiError(response, '중복 시간 예약 시도 이력을 불러오지 못했습니다.')
  }

  return response.json() as Promise<AdminReservationConflictAttemptResponse[]>
}

export async function getAdminReservation(reservationId: number) {
  const response = await fetch(`${API_BASE_URL}/api/admin/reservations/${reservationId}`, {
    credentials: 'include',
  })

  if (!response.ok) {
    await throwApiError(response, '관리자 예약 상세를 불러오지 못했습니다.')
  }

  return response.json() as Promise<AdminReservationDetailResponse>
}

export async function updateAdminReservationStatus(reservationId: number, status: ReservationStatus) {
  const response = await fetch(`${API_BASE_URL}/api/admin/reservations/${reservationId}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ status }),
  })

  if (!response.ok) {
    await throwApiError(response, '관리자 예약 상태 변경에 실패했습니다.')
  }

  return response.json() as Promise<AdminReservationDetailResponse>
}

export async function updateAdminReservationEstimate(reservationId: number, estimatedPrice: number) {
  const response = await fetch(`${API_BASE_URL}/api/admin/reservations/${reservationId}/estimate`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ estimatedPrice }),
  })

  if (!response.ok) {
    await throwApiError(response, '관리자 예약 견적 저장에 실패했습니다.')
  }

  return response.json() as Promise<AdminReservationDetailResponse>
}

export async function updateAdminReservationDistance(reservationId: number, distanceKm: number | null) {
  const response = await fetch(`${API_BASE_URL}/api/admin/reservations/${reservationId}/distance`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ distanceKm }),
  })

  if (!response.ok) {
    await throwApiError(response, '관리자 예약 이동 거리 저장에 실패했습니다.')
  }

  return response.json() as Promise<AdminReservationDetailResponse>
}

export async function updateAdminReservationMemo(reservationId: number, adminMemo: string) {
  const response = await fetch(`${API_BASE_URL}/api/admin/reservations/${reservationId}/memo`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ adminMemo }),
  })

  if (!response.ok) {
    await throwApiError(response, '관리자 예약 메모 저장에 실패했습니다.')
  }

  return response.json() as Promise<AdminReservationDetailResponse>
}

export async function sendAdminReservationEmails(reservationId: number) {
  const response = await fetch(`${API_BASE_URL}/api/admin/reservations/${reservationId}/notifications/email/send`, {
    method: 'POST',
    credentials: 'include',
  })

  if (!response.ok) {
    await throwApiError(response, '관리자 예약 이메일 발송에 실패했습니다.')
  }

  return response.json() as Promise<AdminEmailSendResponse>
}

export async function resendAdminReservationFailedEmails(reservationId: number) {
  const response = await fetch(
    `${API_BASE_URL}/api/admin/reservations/${reservationId}/notifications/email/resend-failed`,
    { method: 'POST', credentials: 'include' },
  )

  if (!response.ok) {
    await throwApiError(response, '관리자 예약 실패 이메일 재발송에 실패했습니다.')
  }

  return response.json() as Promise<AdminEmailSendResponse>
}

export async function sendAdminReservationSms(reservationId: number) {
  const response = await fetch(`${API_BASE_URL}/api/admin/reservations/${reservationId}/notifications/sms/send`, {
    method: 'POST',
    credentials: 'include',
  })

  if (!response.ok) {
    await throwApiError(response, '관리자 예약 SMS 발송에 실패했습니다.')
  }

  return response.json() as Promise<AdminSmsSendResponse>
}

export async function resendAdminReservationFailedSms(reservationId: number) {
  const response = await fetch(
    `${API_BASE_URL}/api/admin/reservations/${reservationId}/notifications/sms/resend-failed`,
    { method: 'POST', credentials: 'include' },
  )

  if (!response.ok) {
    await throwApiError(response, '관리자 예약 실패 SMS 재발송에 실패했습니다.')
  }

  return response.json() as Promise<AdminSmsSendResponse>
}

export async function approveAdminCustomerRequest(requestId: number) {
  const response = await fetch(`${API_BASE_URL}/api/admin/reservations/customer-requests/${requestId}/approve`, {
    method: 'POST',
    credentials: 'include',
  })

  if (!response.ok) {
    await throwApiError(response, '고객 요청 승인에 실패했습니다.')
  }

  return response.json() as Promise<AdminReservationDetailResponse>
}

export async function rejectAdminCustomerRequest(requestId: number, rejectionReason: string) {
  const response = await fetch(`${API_BASE_URL}/api/admin/reservations/customer-requests/${requestId}/reject`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ rejectionReason }),
  })

  if (!response.ok) {
    await throwApiError(response, '고객 요청 반려에 실패했습니다.')
  }

  return response.json() as Promise<AdminReservationDetailResponse>
}

async function throwApiError(response: Response, fallbackMessage: string): Promise<never> {
  if (response.status === 401) {
    throw new AdminAuthenticationRequiredError()
  }

  if (response.status === 403) {
    throw new Error('관리자 권한이 필요한 기능입니다.')
  }

  const message = await response
    .json()
    .then((data: ApiErrorResponse) => data.message || fallbackMessage)
    .catch(() => fallbackMessage)

  throw new Error(message)
}
