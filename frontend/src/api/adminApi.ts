import { API_BASE_URL } from '../reservationData'
import type {
  ApiErrorResponse,
  AdminReservationDetailResponse,
  AdminReservationListQuery,
  AdminReservationPageResponse,
  ReservationStatus,
} from '../types'

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

async function throwApiError(response: Response, fallbackMessage: string): Promise<never> {
  const message = await response
    .json()
    .then((data: ApiErrorResponse) => data.message || fallbackMessage)
    .catch(() => fallbackMessage)

  throw new Error(message)
}
