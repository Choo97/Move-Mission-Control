export type ReviewForm = {
  rating: number
  content: string
}

export type ReviewApiResponse = {
  id: number
  reservationId: number
  rating: number
  content: string
  createdAt: string
}

export type ReviewResponse = ReviewApiResponse

export type AdminReviewResponse = {
  id: number
  reservationId: number
  customerName: string
  phone: string
  email: string | null
  rating: number
  content: string
  published: boolean
  adminReply: string | null
  adminRepliedBy: string | null
  adminRepliedAt: string | null
  moveDate: string
  moveTime: string
  status: string
  statusLabel: string
  createdAt: string
}
