import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { getAvailability } from '../api/customerApi'
import { moveTypeOptions } from '../reservationData'
import type { MoveType, ReservationForm, ServiceMode } from '../types'
import './ReservationCreateForm.css'

type FormStep = 'customer' | 'schedule' | 'address' | 'memo' | 'confirm'

type CalendarDay = {
  dateValue: string
  day: number
  disabled: boolean
}

type Props = {
  form: ReservationForm
  today: string
  errorMessage: string
  isSubmitting: boolean
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: <K extends keyof ReservationForm>(key: K, value: ReservationForm[K]) => void
  serviceMode: ServiceMode
}

const weekdayLabels = ['일', '월', '화', '수', '목', '금', '토']

const parseDateValue = (dateValue: string) => {
  const [year, month, day] = dateValue.split('-').map(Number)
  return new Date(year, month - 1, day)
}

const toDateValue = (date: Date) =>
  [
    date.getFullYear(),
    String(date.getMonth() + 1).padStart(2, '0'),
    String(date.getDate()).padStart(2, '0'),
  ].join('-')

const startOfMonth = (date: Date) => new Date(date.getFullYear(), date.getMonth(), 1)

const addMonths = (date: Date, amount: number) => new Date(date.getFullYear(), date.getMonth() + amount, 1)

const formatCalendarMonth = (date: Date) => `${date.getFullYear()}년 ${date.getMonth() + 1}월`

const formatSelectedDate = (dateValue: string) => {
  if (!dateValue) {
    return '날짜를 선택해 주세요'
  }

  const date = parseDateValue(dateValue)
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일 (${weekdayLabels[date.getDay()]})`
}

const formatOptionalText = (value: string, fallback = '없음') => value.trim() || fallback

const buildCalendarDays = (calendarMonth: Date, today: string): Array<CalendarDay | null> => {
  const firstDate = startOfMonth(calendarMonth)
  const daysInMonth = new Date(firstDate.getFullYear(), firstDate.getMonth() + 1, 0).getDate()
  const leadingBlankCount = firstDate.getDay()
  const days = Array.from({ length: daysInMonth }, (_, index) => {
    const date = new Date(firstDate.getFullYear(), firstDate.getMonth(), index + 1)
    const dateValue = toDateValue(date)

    return {
      dateValue,
      day: index + 1,
      disabled: dateValue < today,
    }
  })

  return [...Array<null>(leadingBlankCount).fill(null), ...days]
}

export function ReservationCreateForm({
  form,
  today,
  errorMessage,
  isSubmitting,
  onSubmit,
  onChange,
  serviceMode,
}: Props) {
  const isNonProfitMode = serviceMode === 'NON_PROFIT'
  const [availableTimes, setAvailableTimes] = useState<string[]>([])
  const [availabilityMessage, setAvailabilityMessage] = useState('날짜를 선택해 주세요.')
  const [isLoadingAvailability, setIsLoadingAvailability] = useState(Boolean(form.moveDate))
  const [currentStep, setCurrentStep] = useState<FormStep>('customer')
  const [stepMessage, setStepMessage] = useState('')
  const [calendarMonth, setCalendarMonth] = useState(() => startOfMonth(parseDateValue(form.moveDate || today)))

  useEffect(() => {
    if (!form.moveDate) {
      return
    }

    let active = true

    void getAvailability(form.moveDate)
      .then((availability) => {
        if (!active) return
        const times = availability.availableTimes.map((time) => time.slice(0, 5))
        setAvailableTimes(times)
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
        setAvailableTimes([])
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

  const displayedTimes = form.moveDate && !isLoadingAvailability ? availableTimes : []
  const calendarDays = buildCalendarDays(calendarMonth, today)
  const isPreviousMonthDisabled = startOfMonth(calendarMonth) <= startOfMonth(parseDateValue(today))
  const displayedMessage = !form.moveDate
    ? '날짜를 선택해 주세요.'
    : isLoadingAvailability
      ? '예약 가능 시간을 확인하고 있습니다.'
      : availabilityMessage
  const getCalendarDayStatus = (day: CalendarDay) => {
    if (day.disabled) {
      return '지난 날짜'
    }

    if (day.dateValue !== form.moveDate) {
      return '시간 확인'
    }

    if (isLoadingAvailability) {
      return '확인 중'
    }

    return availableTimes.length > 0 ? '예약 가능' : '마감'
  }
  const getCalendarDayClassName = (day: CalendarDay) =>
    [
      form.moveDate === day.dateValue ? 'selected' : '',
      form.moveDate === day.dateValue && !isLoadingAvailability && availableTimes.length === 0 ? 'unavailable' : '',
    ]
      .filter(Boolean)
      .join(' ')
  const selectedMoveTypeLabel = moveTypeOptions.find((option) => option.value === form.moveType)?.label ?? form.moveType
  const formattedMoveDate = formatSelectedDate(form.moveDate)
  const fromSiteSummary = [
    `${form.fromFloor}층`,
    form.fromElevator ? '엘리베이터 있음' : '엘리베이터 없음',
    form.fromLadderTruck ? '사다리차 요청' : '사다리차 미사용',
  ].join(' · ')
  const toSiteSummary = [
    `${form.toFloor}층`,
    form.toElevator ? '엘리베이터 있음' : '엘리베이터 없음',
    form.toLadderTruck ? '사다리차 요청' : '사다리차 미사용',
  ].join(' · ')
  const steps: { key: FormStep; label: string; description: string }[] = [
    { key: 'customer', label: isNonProfitMode ? '신청자 정보' : '고객 정보', description: '이름과 연락처를 입력합니다.' },
    { key: 'schedule', label: isNonProfitMode ? '도움 일정' : '이사 일정', description: '날짜와 가능한 시간을 선택합니다.' },
    { key: 'address', label: '이사 주소', description: '출발지와 도착지를 입력합니다.' },
    {
      key: 'memo',
      label: '추가 정보',
      description: isNonProfitMode ? '도움 요청사항을 확인합니다.' : '요청사항과 쿠폰을 확인합니다.',
    },
    { key: 'confirm', label: '최종 확인', description: '접수 전 내용을 확인합니다.' },
  ]
  const currentStepIndex = steps.findIndex((step) => step.key === currentStep)
  const isFirstStep = currentStepIndex === 0
  const isLastStep = currentStepIndex === steps.length - 1
  const primaryButtonText = isLastStep
    ? isSubmitting
      ? isNonProfitMode ? '도움 요청 접수 중' : '예약 접수 중'
      : isNonProfitMode ? '도움 요청 접수하기' : '예약 접수하기'
    : currentStep === 'memo'
      ? isNonProfitMode ? '요청 내용 확인' : '예약 내용 확인'
      : '다음'

  const validateCurrentStep = () => {
    if (currentStep === 'customer') {
      if (!form.customerName.trim() || !form.phone.trim()) {
        return '이름과 연락처를 입력해 주세요.'
      }
    }

    if (currentStep === 'schedule') {
      if (!form.moveDate) {
        return '이사 날짜를 선택해 주세요.'
      }

      if (isLoadingAvailability) {
        return '예약 가능 시간을 확인하고 있습니다.'
      }

      if (displayedTimes.length === 0) {
        return displayedMessage || '선택 가능한 시간이 없습니다.'
      }

      if (!form.moveTime) {
        return '예약 가능한 시간 중에서 희망 시간을 선택해 주세요.'
      }

      if (!displayedTimes.includes(form.moveTime)) {
        return '예약 가능한 시간 중에서 다시 선택해 주세요.'
      }
    }

    if (currentStep === 'address') {
      if (!form.fromAddress.trim() || !form.toAddress.trim()) {
        return '출발지와 도착지 주소를 입력해 주세요.'
      }

      if (form.fromFloor < 1 || form.toFloor < 1) {
        return '층수는 1층 이상으로 입력해 주세요.'
      }
    }

    if (currentStep === 'confirm') {
      if (!form.customerName.trim() || !form.phone.trim()) {
        return isNonProfitMode ? '신청자 정보를 다시 확인해 주세요.' : '고객 정보를 다시 확인해 주세요.'
      }

      if (!form.moveDate || !form.moveTime) {
        return '이사 일정을 다시 확인해 주세요.'
      }

      if (isLoadingAvailability) {
        return '예약 가능 시간을 확인하고 있습니다.'
      }

      if (!displayedTimes.includes(form.moveTime)) {
        return '예약 가능한 시간 중에서 다시 선택해 주세요.'
      }

      if (!form.fromAddress.trim() || !form.toAddress.trim()) {
        return '출발지와 도착지 주소를 다시 확인해 주세요.'
      }
    }

    return ''
  }

  const goToNextStep = () => {
    const message = validateCurrentStep()

    if (message) {
      setStepMessage(message)
      return
    }

    setStepMessage('')
    setCurrentStep(steps[Math.min(currentStepIndex + 1, steps.length - 1)].key)
  }

  const goToPreviousStep = () => {
    setStepMessage('')
    setCurrentStep(steps[Math.max(currentStepIndex - 1, 0)].key)
  }

  const selectMoveDate = (dateValue: string) => {
    setCalendarMonth(startOfMonth(parseDateValue(dateValue)))

    if (dateValue === form.moveDate) {
      return
    }

    setIsLoadingAvailability(true)
    onChange('moveDate', dateValue)
    onChange('moveTime', '')
  }

  const submitStepForm = (event: FormEvent<HTMLFormElement>) => {
    if (!isLastStep) {
      event.preventDefault()
      goToNextStep()
      return
    }

    const message = validateCurrentStep()

    if (message) {
      event.preventDefault()
      setStepMessage(message)
      return
    }

    onSubmit(event)
  }

  return (
    <form className="reservation-form" onSubmit={submitStepForm}>
      <div className="section-heading">
        <div>
          <h2>{isNonProfitMode ? '도움 요청 정보' : '예약 정보'}</h2>
          <p>
            {isNonProfitMode
              ? '단계별로 입력하면 도움 요청이 접수됩니다.'
              : '단계별로 입력하면 접수 상태로 예약됩니다.'}
          </p>
        </div>
        <span className="step-count">
          {currentStepIndex + 1} / {steps.length}
        </span>
      </div>

      <ol className="reservation-stepper" aria-label="예약 입력 단계">
        {steps.map((step, index) => (
          <li
            key={step.key}
            className={index === currentStepIndex ? 'current' : index < currentStepIndex ? 'done' : ''}
          >
            <span>{index + 1}</span>
            <div>
              <strong>{step.label}</strong>
              <small>{step.description}</small>
            </div>
          </li>
        ))}
      </ol>

      {currentStep === 'customer' && (
        <div className="step-panel">
          <div className="field-grid">
            <label>
              이름
              <input
                value={form.customerName}
                onChange={(event) => onChange('customerName', event.target.value)}
                placeholder="홍길동"
                required
              />
            </label>
            <label>
              연락처
              <input
                type="tel"
                inputMode="tel"
                value={form.phone}
                onChange={(event) => onChange('phone', event.target.value)}
                placeholder="010-1234-5678"
                required
              />
              <span className="field-message">숫자만 입력해도 자동으로 하이픈을 맞춥니다.</span>
            </label>
            <label>
              이메일
              <input
                type="email"
                value={form.email}
                onChange={(event) => onChange('email', event.target.value)}
                placeholder="customer@example.com"
              />
            </label>
            <label>
              이사 유형
              <select value={form.moveType} onChange={(event) => onChange('moveType', event.target.value as MoveType)}>
                {moveTypeOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </div>
      )}

      {currentStep === 'schedule' && (
        <div className="step-panel">
          <div className="reservation-schedule-layout">
            <section className="reservation-calendar-card" aria-label="이사 날짜 선택">
              <div className="reservation-calendar-header">
                <button
                  type="button"
                  aria-label="이전 달"
                  disabled={isPreviousMonthDisabled}
                  onClick={() => setCalendarMonth((current) => addMonths(current, -1))}
                >
                  ‹
                </button>
                <strong>{formatCalendarMonth(calendarMonth)}</strong>
                <button
                  type="button"
                  aria-label="다음 달"
                  onClick={() => setCalendarMonth((current) => addMonths(current, 1))}
                >
                  ›
                </button>
              </div>
              <div className="reservation-calendar-weekdays" aria-hidden="true">
                {weekdayLabels.map((label) => (
                  <span key={label}>{label}</span>
                ))}
              </div>
              <div className="reservation-calendar-grid">
                {calendarDays.map((day, index) =>
                  day ? (
                    <button
                      key={day.dateValue}
                      type="button"
                      className={getCalendarDayClassName(day)}
                      data-date={day.dateValue}
                      disabled={day.disabled}
                      aria-pressed={form.moveDate === day.dateValue}
                      onClick={() => selectMoveDate(day.dateValue)}
                    >
                      <strong>{day.day}</strong>
                      <span>{getCalendarDayStatus(day)}</span>
                    </button>
                  ) : (
                    <span key={`blank-${index}`} className="blank" />
                  ),
                )}
              </div>
            </section>

            <section className="reservation-time-card" aria-label="희망 시간 선택">
              <div className="reservation-time-heading">
                <span>선택한 날짜</span>
                <strong>{formatSelectedDate(form.moveDate)}</strong>
                {displayedMessage && <p>{displayedMessage}</p>}
              </div>
              <div
                className="time-slot-grid"
                role="group"
                aria-label="예약 가능한 희망 시간"
              >
                {!form.moveDate && <span className="time-slot-placeholder">날짜를 먼저 선택해 주세요.</span>}
                {form.moveDate && isLoadingAvailability && (
                  <span className="time-slot-placeholder">가능 시간을 확인하고 있습니다.</span>
                )}
                {form.moveDate && !isLoadingAvailability && displayedTimes.length === 0 && (
                  <span className="time-slot-placeholder">선택 가능한 시간이 없습니다.</span>
                )}
                {displayedTimes.map((time) => (
                  <button
                    key={time}
                    type="button"
                    className={`time-slot-button${form.moveTime === time ? ' selected' : ''}`}
                    aria-pressed={form.moveTime === time}
                    onClick={() => onChange('moveTime', time)}
                  >
                    {time}
                  </button>
                ))}
              </div>
            </section>
          </div>
        </div>
      )}

      {currentStep === 'address' && (
        <div className="step-panel address-grid">
          <fieldset>
            <legend>출발지</legend>
            <label>
              주소
              <input
                value={form.fromAddress}
                onChange={(event) => onChange('fromAddress', event.target.value)}
                placeholder="서울시 강남구 테헤란로 1"
                required
              />
            </label>
            <label>
              층수
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
            <div className="switch-row">
              <label>
                <input
                  type="checkbox"
                  checked={form.fromElevator}
                  onChange={(event) => onChange('fromElevator', event.target.checked)}
                />
                엘리베이터
              </label>
              <label>
                <input
                  type="checkbox"
                  checked={form.fromLadderTruck}
                  onChange={(event) => onChange('fromLadderTruck', event.target.checked)}
                />
                사다리차
              </label>
            </div>
          </fieldset>

          <fieldset>
            <legend>도착지</legend>
            <label>
              주소
              <input
                value={form.toAddress}
                onChange={(event) => onChange('toAddress', event.target.value)}
                placeholder="서울시 송파구 올림픽로 1"
                required
              />
            </label>
            <label>
              층수
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
            <div className="switch-row">
              <label>
                <input
                  type="checkbox"
                  checked={form.toElevator}
                  onChange={(event) => onChange('toElevator', event.target.checked)}
                />
                엘리베이터
              </label>
              <label>
                <input
                  type="checkbox"
                  checked={form.toLadderTruck}
                  onChange={(event) => onChange('toLadderTruck', event.target.checked)}
                />
                사다리차
              </label>
            </div>
          </fieldset>
        </div>
      )}

      {currentStep === 'memo' && (
        <div className="step-panel">
          <div className="field-grid">
            <label className="wide">
              요청사항
              <textarea
                value={form.memo}
                onChange={(event) => onChange('memo', event.target.value)}
                placeholder="깨지기 쉬운 짐, 주차 정보 등을 적어주세요."
              />
            </label>
            {!isNonProfitMode && (
              <label>
                쿠폰 코드
                <input
                  value={form.couponCode}
                  onChange={(event) => onChange('couponCode', event.target.value)}
                  placeholder="WELCOME10"
                />
              </label>
            )}
          </div>
        </div>
      )}

      {currentStep === 'confirm' && (
        <div className="step-panel reservation-confirm-panel">
          <div className="reservation-confirm-heading">
            <span>제출 전 확인</span>
            <h3>{isNonProfitMode ? '요청 내용을 한 번 더 확인해 주세요' : '예약 내용을 한 번 더 확인해 주세요'}</h3>
            <p>잘못 입력한 항목이 있으면 이전 버튼으로 돌아가 수정할 수 있습니다.</p>
          </div>

          <div className="reservation-confirm-grid">
            <section>
              <h4>{isNonProfitMode ? '신청자 정보' : '고객 정보'}</h4>
              <dl>
                <div>
                  <dt>이름</dt>
                  <dd>{form.customerName}</dd>
                </div>
                <div>
                  <dt>연락처</dt>
                  <dd>{form.phone}</dd>
                </div>
                <div>
                  <dt>이메일</dt>
                  <dd>{formatOptionalText(form.email, '입력 안 함')}</dd>
                </div>
                <div>
                  <dt>이사 유형</dt>
                  <dd>{selectedMoveTypeLabel}</dd>
                </div>
              </dl>
            </section>

            <section>
              <h4>이사 일정</h4>
              <dl>
                <div>
                  <dt>날짜</dt>
                  <dd>{formattedMoveDate}</dd>
                </div>
                <div>
                  <dt>희망 시간</dt>
                  <dd>{form.moveTime}</dd>
                </div>
              </dl>
            </section>

            <section className="wide">
              <h4>이사 주소</h4>
              <dl>
                <div>
                  <dt>출발지</dt>
                  <dd>
                    <strong>{form.fromAddress}</strong>
                    <span>{fromSiteSummary}</span>
                  </dd>
                </div>
                <div>
                  <dt>도착지</dt>
                  <dd>
                    <strong>{form.toAddress}</strong>
                    <span>{toSiteSummary}</span>
                  </dd>
                </div>
              </dl>
            </section>

            <section className="wide">
              <h4>추가 정보</h4>
              <dl>
                <div>
                  <dt>요청사항</dt>
                  <dd>{formatOptionalText(form.memo)}</dd>
                </div>
                {!isNonProfitMode && (
                  <div>
                    <dt>쿠폰 코드</dt>
                    <dd>{formatOptionalText(form.couponCode, '사용 안 함')}</dd>
                  </div>
                )}
              </dl>
            </section>
          </div>

          <p className="reservation-confirm-note">
            {isNonProfitMode
              ? '도움 요청 접수 후에는 조회번호가 발급됩니다. 조회번호와 연락처로 진행 상황을 확인할 수 있습니다.'
              : '예약 접수 후에는 예약번호가 발급됩니다. 예약번호와 연락처로 진행 상황을 조회할 수 있습니다.'}
          </p>
        </div>
      )}

      {stepMessage && <p className="message info">{stepMessage}</p>}
      {errorMessage && <p className="message error">{errorMessage}</p>}

      <div className="step-actions">
        <button className="submit-button secondary" type="button" onClick={goToPreviousStep} disabled={isFirstStep}>
          이전
        </button>
        <button className="submit-button" type="submit" disabled={isSubmitting}>
          {primaryButtonText}
        </button>
      </div>
    </form>
  )
}
