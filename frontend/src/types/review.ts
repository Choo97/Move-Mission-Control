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
