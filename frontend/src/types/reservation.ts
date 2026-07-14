export type MoveType = 'STUDIO' | 'TWO_ROOM' | 'FAMILY' | 'OFFICE' | 'STORAGE'

export type ServiceRequestType = 'GENERAL' | 'NON_PROFIT'

export type ReservationStatus =
  | 'RECEIVED'
  | 'CONSULTING'
  | 'ESTIMATE_SENT'
  | 'CONFIRMED'
  | 'COMPLETED'
  | 'CANCELED'

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

export type ReservationSearchForm = {
  reservationId: string
  phone: string
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

export type ReservationApiPhotoResponse = {
  id: number
  originalFilename: string
  fileUrl: string
  uploadedAt: string
}

export type ReservationApiEstimateLineResponse = {
  label: string
  amount: number
}

export type ReservationCustomerRequestResponse = {
  id: number
  requestType: 'UPDATE' | 'CANCEL'
  requestTypeLabel: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  statusLabel: string
  detail: string
  rejectionReason: string | null
  requestedBy: string
  requestedAt: string
  processedBy: string | null
  processedAt: string | null
}

export type ReservationApiResponse = {
  id: number
  customerName: string
  email: string
  moveDate: string
  moveTime: string
  fromAddress: string
  toAddress: string
  moveType: MoveType
  moveTypeLabel: string
  serviceType: ServiceRequestType
  serviceTypeLabel: string
  status: ReservationStatus
  statusLabel: string
  fromElevator: boolean
  toElevator: boolean
  fromFloor: number
  toFloor: number
  fromLadderTruck: boolean
  toLadderTruck: boolean
  distanceKm: number | null
  estimatedPrice: number | null
  baseEstimatedPrice: number | null
  discountAmount: number
  finalEstimatedPrice: number | null
  couponCode: string | null
  couponName: string | null
  editable: boolean
  cancelable: boolean
  estimateAcceptable: boolean
  estimateAccepted: boolean
  acceptedEstimatePrice: number | null
  estimateAcceptedAt: string | null
  photos: ReservationApiPhotoResponse[]
  estimateLines: ReservationApiEstimateLineResponse[]
  customerRequests: ReservationCustomerRequestResponse[]
}

export type ReservationPhotoResponse = ReservationApiPhotoResponse
export type ReservationResponse = ReservationApiResponse
