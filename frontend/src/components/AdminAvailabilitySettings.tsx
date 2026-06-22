import { useEffect, useState } from 'react'
import {
  addOperatingHoliday,
  deleteOperatingHoliday,
  getOperatingHolidays,
  getOperatingSchedules,
  updateOperatingSchedule,
} from '../api/adminApi'
import { getErrorMessage } from '../api/apiError'
import type { OperatingHolidayResponse, OperatingScheduleResponse } from '../types'

export function AdminAvailabilitySettings() {
  const [schedules, setSchedules] = useState<OperatingScheduleResponse[]>([])
  const [holidays, setHolidays] = useState<OperatingHolidayResponse[]>([])
  const [holidayDate, setHolidayDate] = useState('')
  const [holidayReason, setHolidayReason] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    void Promise.all([getOperatingSchedules(), getOperatingHolidays()])
      .then(([nextSchedules, nextHolidays]) => {
        setSchedules(nextSchedules)
        setHolidays(nextHolidays)
      })
      .catch((error) => setMessage(getErrorMessage(error, '운영 일정을 불러오지 못했습니다.')))
  }, [])

  const changeSchedule = <K extends keyof OperatingScheduleResponse>(
    id: number,
    key: K,
    value: OperatingScheduleResponse[K],
  ) => setSchedules((current) => current.map((item) => (item.id === id ? { ...item, [key]: value } : item)))

  const saveSchedule = async (schedule: OperatingScheduleResponse) => {
    setMessage('')
    try {
      const updated = await updateOperatingSchedule(schedule.id, schedule)
      setSchedules((current) => current.map((item) => (item.id === updated.id ? updated : item)))
      setMessage(`${updated.dayLabel} 운영시간을 저장했습니다.`)
    } catch (error) {
      setMessage(getErrorMessage(error, '운영시간 저장에 실패했습니다.'))
    }
  }

  const addHoliday = async () => {
    if (!holidayDate) return
    setMessage('')
    try {
      const added = await addOperatingHoliday(holidayDate, holidayReason)
      setHolidays((current) => [...current, added].sort((a, b) => a.holidayDate.localeCompare(b.holidayDate)))
      setHolidayDate('')
      setHolidayReason('')
      setMessage('휴무일을 등록했습니다.')
    } catch (error) {
      setMessage(getErrorMessage(error, '휴무일 등록에 실패했습니다.'))
    }
  }

  const removeHoliday = async (holidayId: number) => {
    setMessage('')
    try {
      await deleteOperatingHoliday(holidayId)
      setHolidays((current) => current.filter((holiday) => holiday.id !== holidayId))
      setMessage('휴무일을 삭제했습니다.')
    } catch (error) {
      setMessage(getErrorMessage(error, '휴무일 삭제에 실패했습니다.'))
    }
  }

  return (
    <section className="admin-availability-settings">
      <div className="admin-section-heading">
        <div><p className="eyebrow">Availability</p><h3>예약 가능 시간표</h3></div>
      </div>
      <div className="operating-schedule-list">
        {schedules.map((schedule) => (
          <div className="operating-schedule-row" key={schedule.id}>
            <strong>{schedule.dayLabel}</strong>
            <label className="inline-check">
              <input type="checkbox" checked={schedule.open}
                onChange={(event) => changeSchedule(schedule.id, 'open', event.target.checked)} />영업
            </label>
            <input type="time" value={schedule.startTime.slice(0, 5)} disabled={!schedule.open}
              onChange={(event) => changeSchedule(schedule.id, 'startTime', event.target.value)} />
            <span>~</span>
            <input type="time" value={schedule.endTime.slice(0, 5)} disabled={!schedule.open}
              onChange={(event) => changeSchedule(schedule.id, 'endTime', event.target.value)} />
            <select value={schedule.slotMinutes} disabled={!schedule.open}
              onChange={(event) => changeSchedule(schedule.id, 'slotMinutes', Number(event.target.value))}>
              <option value={30}>30분</option><option value={60}>1시간</option><option value={120}>2시간</option>
            </select>
            <button type="button" onClick={() => void saveSchedule(schedule)}>저장</button>
          </div>
        ))}
      </div>
      <div className="holiday-editor">
        <h4>지정 휴무일</h4>
        <div className="holiday-form">
          <input type="date" value={holidayDate} min={new Date().toISOString().slice(0, 10)}
            onChange={(event) => setHolidayDate(event.target.value)} />
          <input value={holidayReason} maxLength={100} placeholder="휴무 사유"
            onChange={(event) => setHolidayReason(event.target.value)} />
          <button type="button" disabled={!holidayDate} onClick={() => void addHoliday()}>추가</button>
        </div>
        <ul>
          {holidays.map((holiday) => (
            <li key={holiday.id}>
              <strong>{holiday.holidayDate}</strong><span>{holiday.reason}</span>
              <button type="button" onClick={() => void removeHoliday(holiday.id)}>삭제</button>
            </li>
          ))}
        </ul>
      </div>
      {message && <p className="message">{message}</p>}
    </section>
  )
}
