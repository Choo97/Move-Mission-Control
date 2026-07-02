export type AvailabilityResponse = {
  date: string
  closed: boolean
  closureReason: string | null
  availableTimes: string[]
}

export type OperatingScheduleResponse = {
  id: number
  dayOfWeek: string
  dayLabel: string
  open: boolean
  startTime: string
  endTime: string
  slotMinutes: number
}

export type OperatingHolidayResponse = {
  id: number
  holidayDate: string
  reason: string
}

export type OperatingPolicyResponse = {
  minAdvanceDays: number
  maxAdvanceDays: number
  maxDailyReservations: number
}
