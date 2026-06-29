export type FaqResponse = {
  id: number
  question: string
  answer: string
}

export type AdminFaqResponse = {
  id: number
  question: string
  answer: string
  displayOrder: number
  active: boolean
  createdAt: string
}

export type AdminFaqSaveRequest = {
  question: string
  answer: string
  displayOrder: number
}
