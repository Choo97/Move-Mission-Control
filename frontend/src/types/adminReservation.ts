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
