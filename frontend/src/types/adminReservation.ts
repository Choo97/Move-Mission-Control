import type { MoveType, ReservationStatus } from './reservation'

export type AdminReservationSort = 'PRIORITY' | 'MOVE_DATE' | 'CREATED_DESC'

export type AdminReservationListItemResponse = {
  id: number
  customerName: string
  phone: string
  moveDate: string
  moveTime: string
  fromAddress: string
  toAddress: string
  moveType: MoveType
  moveTypeLabel: string
  status: ReservationStatus
  statusLabel: string
  distanceKm: number | null
  finalEstimatedPrice: number
  createdAt: string
}

export type AdminReservationPageResponse = {
  content: AdminReservationListItemResponse[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export type AdminReservationStatusHistoryResponse = {
  id: number
  previousStatus: ReservationStatus
  previousStatusLabel: string
  changedStatus: ReservationStatus
  changedStatusLabel: string
  changedAt: string
  changedBy: string | null
}

export type AdminReservationCustomerActionHistoryResponse = {
  id: number
  actionType: string
  summary: string
  detail: string
  requestedBy: string
  createdAt: string
}

export type AdminNotificationHistoryResponse = {
  id: number
  type: string
  typeLabel: string
  channel: 'SMS' | 'EMAIL' | 'KAKAO_ALIMTALK'
  channelLabel: string
  status: 'READY' | 'SENT' | 'FAILED'
  statusLabel: string
  recipientContact: string
  message: string
  failureReason: string | null
  sentAt: string | null
  createdAt: string
}

export type AdminAuditLogResponse = {
  id: number
  action: string
  detail: string
  createdBy: string
  createdAt: string
}

export type AdminEmailSendResponse = {
  sentCount: number
  failedCount: number
  reservation: AdminReservationDetailResponse
}

export type AdminReservationDetailResponse = {
  id: number
  customerName: string
  phone: string
  email: string
  moveDate: string
  moveTime: string
  fromAddress: string
  toAddress: string
  moveType: MoveType
  moveTypeLabel: string
  status: ReservationStatus
  statusLabel: string
  fromElevator: boolean
  toElevator: boolean
  fromFloor: number
  toFloor: number
  fromLadderTruck: boolean
  toLadderTruck: boolean
  distanceKm: number | null
  estimatedPrice: number
  baseEstimatedPrice: number
  discountAmount: number
  finalEstimatedPrice: number
  couponCode: string | null
  couponName: string | null
  memo: string | null
  adminMemo: string | null
  adminMemoUpdatedBy: string | null
  editable: boolean
  cancelable: boolean
  estimateAcceptable: boolean
  estimateAccepted: boolean
  acceptedEstimatePrice: number | null
  estimateAcceptedAt: string | null
  createdAt: string
  updatedAt: string
  photos: Array<{
    id: number
    originalFilename: string
    fileUrl: string
    uploadedAt: string
  }>
  estimateLines: Array<{
    label: string
    amount: number
  }>
  statusHistories: AdminReservationStatusHistoryResponse[]
  customerActionHistories: AdminReservationCustomerActionHistoryResponse[]
  notifications: AdminNotificationHistoryResponse[]
  auditLogs: AdminAuditLogResponse[]
}

export type AdminReservationListQuery = {
  status?: ReservationStatus
  keyword?: string
  startDate?: string
  endDate?: string
  needsDistance?: boolean
  sort?: AdminReservationSort
  page?: number
  size?: number
}

export type AdminReservationConflictAttemptResponse = {
  id: number
  customerName: string
  phone: string
  moveDate: string
  moveTime: string
  attemptedAt: string
}
