import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { getAvailability } from '../api/customerApi'
import type { ReservationEditForm } from '../types'

type Props = {
  form: ReservationEditForm
  today: string
  currentMoveDate: string
  currentMoveTime: string
  isUpdating: boolean
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: <K extends keyof ReservationEditForm>(key: K, value: ReservationEditForm[K]) => void
  onCancel: () => void
}

export function ReservationEditFormView({
  form,
  today,
  currentMoveDate,
  currentMoveTime,
  isUpdating,
  onSubmit,
  onChange,
  onCancel,
}: Props) {
  const [baseAvailableTimes, setBaseAvailableTimes] = useState<string[]>([])
  const [availabilityMessage, setAvailabilityMessage] = useState('날짜를 선택해 주세요.')
  const [isLoadingAvailability, setIsLoadingAvailability] = useState(Boolean(form.moveDate))
  const [formMessage, setFormMessage] = useState('')
  const currentTime = currentMoveTime.slice(0, 5)
  const availableTimes = useMemo(() => {
    const times = new Set(baseAvailableTimes)

    if (form.moveDate === currentMoveDate && currentTime) {
      times.add(currentTime)
    }

    return [...times].sort()
  }, [baseAvailableTimes, currentMoveDate, currentTime, form.moveDate])

  useEffect(() => {
    if (!form.moveDate) {
      return
    }

    let active = true

    void getAvailability(form.moveDate)
      .then((availability) => {
        if (!active) return
        const times = availability.availableTimes.map((time) => time.slice(0, 5))
        setBaseAvailableTimes(times)
        setAvailabilityMessage(
          availability.closed
            ? availability.closureReason ?? '휴무일입니다.'
            : times.length === 0
              ? '선택한 날짜의 예약이 마감되었습니다.'
              : '',
        )
      })
      .catch(() => {
        if (!active) return
        setBaseAvailableTimes([])
        setAvailabilityMessage('예약 가능 시간을 불러오지 못했습니다.')
      })
      .finally(() => {
        if (active) setIsLoadingAvailability(false)
      })

    return () => {
      active = false
    }
  }, [form.moveDate])

  useEffect(() => {
    if (!form.moveDate || isLoadingAvailability) {
      return
    }

    if (availableTimes.length === 0 && form.moveTime) {
      onChange('moveTime', '')
      return
    }

    if (form.moveTime && !availableTimes.includes(form.moveTime)) {
      onChange('moveTime', '')
    }
  }, [availableTimes, form.moveDate, form.moveTime, isLoadingAvailability, onChange])

  const displayedAvailableTimes = isLoadingAvailability ? [] : availableTimes
  const displayedAvailabilityMessage = !form.moveDate
    ? '날짜를 선택해 주세요.'
    : isLoadingAvailability
      ? '예약 가능 시간을 확인하고 있습니다.'
      : availabilityMessage

  const submitEditForm = (event: FormEvent<HTMLFormElement>) => {
    if (!form.moveDate) {
      event.preventDefault()
      setFormMessage('이사 날짜를 선택해 주세요.')
      return
    }

    if (isLoadingAvailability) {
      event.preventDefault()
      setFormMessage('예약 가능 시간을 확인하고 있습니다.')
      return
    }

    if (displayedAvailableTimes.length === 0) {
      event.preventDefault()
      setFormMessage(displayedAvailabilityMessage || '선택 가능한 시간이 없습니다.')
      return
    }

    if (!form.moveTime) {
      event.preventDefault()
      setFormMessage('예약 가능한 시간 중에서 희망 시간을 선택해 주세요.')
      return
    }

    if (!displayedAvailableTimes.includes(form.moveTime)) {
      event.preventDefault()
      setFormMessage('예약 가능한 시간 중에서 다시 선택해 주세요.')
      return
    }

    setFormMessage('')
    onSubmit(event)
  }

  return (
    <form className="reservation-form edit-form" onSubmit={submitEditForm}>
      <div className="section-heading">
        <h2>예약 수정</h2>
        <p>조회에 사용한 연락처로 본인 확인 후 예약 정보를 수정합니다.</p>
      </div>

      <div className="field-grid">
        <label>
          연락처
          <input value={form.phone} onChange={(event) => onChange('phone', event.target.value)} required />
          <span className="field-message">예약 조회에 사용한 연락처와 일치해야 합니다.</span>
        </label>
        <label>
          이메일
          <input type="email" value={form.email} onChange={(event) => onChange('email', event.target.value)} />
        </label>
        <label>
          이사 날짜
          <input
            type="date"
            min={today}
            value={form.moveDate}
            onChange={(event) => {
              const nextDate = event.target.value
              setIsLoadingAvailability(Boolean(nextDate))
              onChange('moveDate', nextDate)
              onChange('moveTime', nextDate === currentMoveDate ? currentTime : '')
            }}
            required
          />
        </label>
        <label>
          희망 시간
          <div className="time-slot-grid" role="group" aria-label="예약 가능한 수정 희망 시간">
            {!form.moveDate && <span className="time-slot-placeholder">날짜를 먼저 선택해 주세요.</span>}
            {form.moveDate && isLoadingAvailability && (
              <span className="time-slot-placeholder">가능 시간을 확인하고 있습니다.</span>
            )}
            {form.moveDate && !isLoadingAvailability && displayedAvailableTimes.length === 0 && (
              <span className="time-slot-placeholder">선택 가능한 시간이 없습니다.</span>
            )}
            {displayedAvailableTimes.map((time) => (
              <button
                key={time}
                type="button"
                className={`time-slot-button${form.moveTime === time ? ' selected' : ''}${time === currentTime && form.moveDate === currentMoveDate ? ' current' : ''}`}
                aria-pressed={form.moveTime === time}
                onClick={() => onChange('moveTime', time)}
              >
                {time}
              </button>
            ))}
          </div>
          {displayedAvailabilityMessage && <span className="field-message">{displayedAvailabilityMessage}</span>}
        </label>
        <label>
          출발지 주소
          <input value={form.fromAddress} onChange={(event) => onChange('fromAddress', event.target.value)} required />
        </label>
        <label>
          도착지 주소
          <input value={form.toAddress} onChange={(event) => onChange('toAddress', event.target.value)} required />
        </label>
        <label>
          출발지 층수
          <input
            type="number"
            min="1"
            max="50"
            value={form.fromFloor}
            onChange={(event) => onChange('fromFloor', Number(event.target.value))}
            required
          />
          <span className="field-message">1층부터 50층까지 입력할 수 있습니다.</span>
        </label>
        <label>
          도착지 층수
          <input
            type="number"
            min="1"
            max="50"
            value={form.toFloor}
            onChange={(event) => onChange('toFloor', Number(event.target.value))}
            required
          />
          <span className="field-message">1층부터 50층까지 입력할 수 있습니다.</span>
        </label>
      </div>

      <div className="switch-row edit-switches">
        <label>
          <input
            type="checkbox"
            checked={form.fromLadderTruck}
            onChange={(event) => onChange('fromLadderTruck', event.target.checked)}
          />
          출발지 사다리차
        </label>
        <label>
          <input
            type="checkbox"
            checked={form.toLadderTruck}
            onChange={(event) => onChange('toLadderTruck', event.target.checked)}
          />
          도착지 사다리차
        </label>
      </div>

      <label className="wide edit-memo">
        요청사항
        <textarea value={form.memo} onChange={(event) => onChange('memo', event.target.value)} />
      </label>

      {formMessage && <p className="message info">{formMessage}</p>}

      <div className="button-row">
        <button className="submit-button secondary" type="button" onClick={onCancel}>
          수정 취소
        </button>
        <button className="submit-button" type="submit" disabled={isUpdating}>
          {isUpdating ? '수정 요청 중' : '수정 요청'}
        </button>
      </div>
    </form>
  )
}
