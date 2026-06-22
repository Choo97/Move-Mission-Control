import { initialForm } from './reservationData'
import type { MoveType, ReservationForm } from './types'

const DRAFT_STORAGE_KEY = 'move-mission-control.reservation-draft.v1'
const DRAFT_RETENTION_MS = 7 * 24 * 60 * 60 * 1000
const moveTypes: MoveType[] = ['STUDIO', 'TWO_ROOM', 'FAMILY', 'OFFICE', 'STORAGE']

type StoredReservationDraft = {
  savedAt: number
  form: ReservationForm
}

const isReservationForm = (value: unknown): value is ReservationForm => {
  if (!value || typeof value !== 'object') {
    return false
  }

  const form = value as Record<string, unknown>
  return (
    typeof form.customerName === 'string' &&
    typeof form.phone === 'string' &&
    typeof form.email === 'string' &&
    typeof form.moveDate === 'string' &&
    typeof form.moveTime === 'string' &&
    typeof form.fromAddress === 'string' &&
    typeof form.toAddress === 'string' &&
    typeof form.moveType === 'string' &&
    moveTypes.includes(form.moveType as MoveType) &&
    typeof form.fromElevator === 'boolean' &&
    typeof form.toElevator === 'boolean' &&
    typeof form.fromFloor === 'number' &&
    typeof form.toFloor === 'number' &&
    typeof form.fromLadderTruck === 'boolean' &&
    typeof form.toLadderTruck === 'boolean' &&
    typeof form.memo === 'string' &&
    typeof form.couponCode === 'string'
  )
}

const hasCustomerInput = (form: ReservationForm) =>
  form.customerName.trim() !== '' ||
  form.phone.trim() !== '' ||
  form.email.trim() !== '' ||
  form.moveDate !== '' ||
  form.fromAddress.trim() !== '' ||
  form.toAddress.trim() !== '' ||
  form.memo.trim() !== '' ||
  form.couponCode.trim() !== ''

export const loadReservationDraft = (): ReservationForm => {
  try {
    const rawDraft = window.localStorage.getItem(DRAFT_STORAGE_KEY)
    if (!rawDraft) {
      return initialForm
    }

    const draft = JSON.parse(rawDraft) as Partial<StoredReservationDraft>
    if (
      typeof draft.savedAt !== 'number' ||
      Date.now() - draft.savedAt > DRAFT_RETENTION_MS ||
      !isReservationForm(draft.form)
    ) {
      window.localStorage.removeItem(DRAFT_STORAGE_KEY)
      return initialForm
    }

    return draft.form
  } catch {
    window.localStorage.removeItem(DRAFT_STORAGE_KEY)
    return initialForm
  }
}

export const saveReservationDraft = (form: ReservationForm) => {
  try {
    if (!hasCustomerInput(form)) {
      window.localStorage.removeItem(DRAFT_STORAGE_KEY)
      return
    }

    const draft: StoredReservationDraft = { savedAt: Date.now(), form }
    window.localStorage.setItem(DRAFT_STORAGE_KEY, JSON.stringify(draft))
  } catch {
    // Browsers can deny local storage in private or restricted contexts.
  }
}

export const clearReservationDraft = () => {
  try {
    window.localStorage.removeItem(DRAFT_STORAGE_KEY)
  } catch {
    // Clearing a draft should not block a completed reservation.
  }
}
