import { API_BASE_URL } from '../reservationData'
import type {
  AdminReservationDetailResponse,
  AdminReservationListQuery,
  AdminReservationPageResponse,
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
    throw new Error('관리자 예약 목록을 불러오지 못했습니다.')
  }

  return response.json() as Promise<AdminReservationPageResponse>
}

export async function getAdminReservation(reservationId: number) {
  const response = await fetch(`${API_BASE_URL}/api/admin/reservations/${reservationId}`, {
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('관리자 예약 상세를 불러오지 못했습니다.')
  }

  return response.json() as Promise<AdminReservationDetailResponse>
}
