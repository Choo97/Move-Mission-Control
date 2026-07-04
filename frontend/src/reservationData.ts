import type { MoveType, ReservationEditForm, ReservationForm, ReservationResponse, ReviewForm } from './types'

const rawApiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim()

export const API_BASE_URL = (rawApiBaseUrl || 'http://localhost:8081').replace(/\/+$/, '')

export const moveTypeOptions: Array<{ value: MoveType; label: string }> = [
  { value: 'STUDIO', label: '원룸' },
  { value: 'TWO_ROOM', label: '투룸' },
  { value: 'FAMILY', label: '가정집' },
  { value: 'OFFICE', label: '사무실' },
  { value: 'STORAGE', label: '보관 이사' },
]

export const initialForm: ReservationForm = {
  customerName: '',
  phone: '',
  email: '',
  moveDate: '',
  moveTime: '',
  fromAddress: '',
  toAddress: '',
  moveType: 'STUDIO',
  fromElevator: true,
  toElevator: true,
  fromFloor: 1,
  toFloor: 1,
  fromLadderTruck: false,
  toLadderTruck: false,
  memo: '',
  couponCode: '',
}

export const initialSearchForm = {
  reservationId: '',
  phone: '',
}

export const initialReviewForm: ReviewForm = {
  rating: 5,
  content: '',
}

export const toEditForm = (reservation: ReservationResponse, phone: string): ReservationEditForm => ({
  phone,
  email: reservation.email ?? '',
  moveDate: reservation.moveDate,
  moveTime: reservation.moveTime.slice(0, 5),
  fromAddress: reservation.fromAddress,
  toAddress: reservation.toAddress,
  fromFloor: reservation.fromFloor,
  toFloor: reservation.toFloor,
  fromLadderTruck: reservation.fromLadderTruck,
  toLadderTruck: reservation.toLadderTruck,
  memo: '',
})
