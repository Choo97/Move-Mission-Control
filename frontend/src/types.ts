export type MoveType = 'STUDIO' | 'TWO_ROOM' | 'FAMILY' | 'OFFICE' | 'STORAGE'

export type ReservationForm = {
  customerName: string
  phone: string
  email: string
  moveDate: string
  moveTime: string
  fromAddress: string
  toAddress: string
  moveType: MoveType
  fromElevator: boolean
  toElevator: boolean
  fromFloor: number
  toFloor: number
  fromLadderTruck: boolean
  toLadderTruck: boolean
  memo: string
  couponCode: string
}

export type ApiErrorResponse = {
  code: string
  message: string
}

export type ReservationResponse = {
  id: number
  customerName: string
  email: string
  moveDate: string
  moveTime: string
  fromAddress: string
  toAddress: string
  fromFloor: number
  toFloor: number
  fromLadderTruck: boolean
  toLadderTruck: boolean
  moveTypeLabel: string
  status: string
  statusLabel: string
  distanceKm: number | null
  baseEstimatedPrice: number
  discountAmount: number
  finalEstimatedPrice: number
  couponCode: string | null
  couponName: string | null
  editable: boolean
  cancelable: boolean
  estimateAcceptable: boolean
  estimateAccepted: boolean
  acceptedEstimatePrice: number | null
  estimateAcceptedAt: string | null
  photos: ReservationPhotoResponse[]
  estimateLines: Array<{
    label: string
    amount: number
  }>
}

export type ReservationPhotoResponse = {
  id: number
  originalFilename: string
  fileUrl: string
  uploadedAt: string
}

export type ReviewResponse = {
  id: number
  reservationId: number
  rating: number
  content: string
  createdAt: string
}

export type CustomerGuideItem = {
  title: string
  description: string
}

export type ReservationSearchForm = {
  reservationId: string
  phone: string
}

export type ReviewForm = {
  rating: number
  content: string
}

export type ReservationEditForm = {
  phone: string
  email: string
  moveDate: string
  moveTime: string
  fromAddress: string
  toAddress: string
  fromFloor: number
  toFloor: number
  fromLadderTruck: boolean
  toLadderTruck: boolean
  memo: string
}
