import { API_BASE_URL } from '../reservationData'
import type {
  ApiErrorResponse,
  AvailabilityResponse,
  CustomerGuideItem,
  FaqResponse,
  ReservationEditForm,
  ReservationForm,
  ReservationPhotoResponse,
  ReservationResponse,
  ReservationSearchForm,
  ServiceRequestType,
  PublicReviewResponse,
  PublicOperatingPolicyResponse,
  ReviewForm,
  ReviewResponse,
} from '../types'

async function readJson<T>(response: Response, fallbackMessage: string): Promise<T> {
  const data = await response.json()

  if (!response.ok) {
    const error = data as ApiErrorResponse
    throw new Error(error.message || fallbackMessage)
  }

  return data as T
}

export async function getAvailability(date: string) {
  const response = await fetch(`${API_BASE_URL}/api/availability?date=${encodeURIComponent(date)}`)
  return readJson<AvailabilityResponse>(response, '예약 가능 시간을 불러오지 못했습니다.')
}

export async function getServicePolicy() {
  const response = await fetch(`${API_BASE_URL}/api/service-policy`)
  return readJson<PublicOperatingPolicyResponse>(response, '서비스 운영 방식을 불러오지 못했습니다.')
}

export async function createReservation(form: ReservationForm, serviceType: ServiceRequestType = 'GENERAL') {
  const response = await fetch(`${API_BASE_URL}/api/reservations`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ...form, serviceType }),
  })

  return readJson<ReservationResponse>(response, '예약 신청에 실패했습니다.')
}

export async function searchReservation(form: ReservationSearchForm) {
  const response = await fetch(`${API_BASE_URL}/api/reservations/search`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      reservationId: Number(form.reservationId),
      phone: form.phone,
    }),
  })

  return readJson<ReservationResponse>(response, '예약 조회에 실패했습니다.')
}

export async function updateReservation(reservationId: number, form: ReservationEditForm) {
  const response = await fetch(`${API_BASE_URL}/api/reservations/${reservationId}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(form),
  })

  return readJson<ReservationResponse>(response, '예약 수정에 실패했습니다.')
}

export async function cancelReservation(reservationId: number, phone: string) {
  const response = await fetch(`${API_BASE_URL}/api/reservations/${reservationId}/cancel`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ phone }),
  })

  return readJson<ReservationResponse>(response, '예약 취소에 실패했습니다.')
}

export async function acceptEstimate(reservationId: number, phone: string) {
  const response = await fetch(`${API_BASE_URL}/api/reservations/${reservationId}/estimate/accept`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ phone }),
  })

  return readJson<ReservationResponse>(response, '견적 동의에 실패했습니다.')
}

export async function uploadReservationPhotos(reservationId: number, phone: string, files: File[]) {
  const formData = new FormData()
  formData.append('phone', phone)
  files.forEach((file) => formData.append('photos', file))

  const response = await fetch(`${API_BASE_URL}/api/reservations/${reservationId}/photos`, {
    method: 'POST',
    body: formData,
  })

  return readJson<ReservationPhotoResponse[]>(response, '짐 사진 업로드에 실패했습니다.')
}

export async function createReview(reservationId: number, phone: string, form: ReviewForm) {
  const response = await fetch(`${API_BASE_URL}/api/reviews`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      reservationId,
      phone,
      rating: form.rating,
      content: form.content,
    }),
  })

  return readJson<ReviewResponse>(response, '리뷰 작성에 실패했습니다.')
}

export async function getPublicReviews(limit = 6) {
  const response = await fetch(`${API_BASE_URL}/api/reviews/public?limit=${encodeURIComponent(String(limit))}`)
  return readJson<PublicReviewResponse[]>(response, '공개 리뷰를 불러오지 못했습니다.')
}

export async function getCustomerGuides(status: string) {
  const response = await fetch(`${API_BASE_URL}/api/customer-guides/${status}`)

  return readJson<CustomerGuideItem[]>(response, '고객 안내를 불러오지 못했습니다.')
}

export async function getFaqs() {
  const response = await fetch(`${API_BASE_URL}/api/faqs`)
  return readJson<FaqResponse[]>(response, '자주 묻는 질문을 불러오지 못했습니다.')
}
