import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { getAvailability } from '../api/customerApi'
import { moveTypeOptions } from '../reservationData'
import type { MoveType, ReservationForm } from '../types'

type FormStep = 'customer' | 'schedule' | 'address' | 'memo'

type Props = {
  form: ReservationForm
  today: string
  errorMessage: string
  isSubmitting: boolean
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: <K extends keyof ReservationForm>(key: K, value: ReservationForm[K]) => void
}

export function ReservationCreateForm({
  form,
  today,
  errorMessage,
  isSubmitting,
  onSubmit,
  onChange,
}: Props) {
  const [availableTimes, setAvailableTimes] = useState<string[]>([])
  const [availabilityMessage, setAvailabilityMessage] = useState('날짜를 선택해 주세요.')
  const [isLoadingAvailability, setIsLoadingAvailability] = useState(Boolean(form.moveDate))
  const [currentStep, setCurrentStep] = useState<FormStep>('customer')
  const [stepMessage, setStepMessage] = useState('')

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
        if (!times.includes(form.moveTime)) {
          onChange('moveTime', times[0] ?? '')
        }
      })
      .catch(() => {
        if (!active) return
        setAvailableTimes([])
        setAvailabilityMessage('예약 가능 시간을 불러오지 못했습니다.')
        onChange('moveTime', '')
      })
      .finally(() => {
        if (active) setIsLoadingAvailability(false)
      })

    return () => {
      active = false
    }
  }, [form.moveDate, form.moveTime, onChange])

  const displayedTimes = form.moveDate ? availableTimes : []
  const displayedMessage = form.moveDate ? availabilityMessage : '날짜를 선택해 주세요.'
  const steps: { key: FormStep; label: string; description: string }[] = [
    { key: 'customer', label: '고객 정보', description: '이름과 연락처를 입력합니다.' },
    { key: 'schedule', label: '이사 일정', description: '날짜와 가능한 시간을 선택합니다.' },
    { key: 'address', label: '이사 주소', description: '출발지와 도착지를 입력합니다.' },
    { key: 'memo', label: '추가 정보', description: '요청사항과 쿠폰을 확인합니다.' },
  ]
  const currentStepIndex = steps.findIndex((step) => step.key === currentStep)
  const isFirstStep = currentStepIndex === 0
  const isLastStep = currentStepIndex === steps.length - 1

  const validateCurrentStep = () => {
    if (currentStep === 'customer') {
      if (!form.customerName.trim() || !form.phone.trim()) {
        return '이름과 연락처를 입력해 주세요.'
      }
    }

    if (currentStep === 'schedule') {
      if (!form.moveDate || !form.moveTime) {
        return '이사 날짜와 희망 시간을 선택해 주세요.'
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
          <h2>예약 정보</h2>
          <p>단계별로 입력하면 접수 상태로 예약됩니다.</p>
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
          <div className="field-grid">
            <label>
              이사 날짜
              <input
                type="date"
                min={today}
                value={form.moveDate}
                onChange={(event) => {
                  setIsLoadingAvailability(Boolean(event.target.value))
                  onChange('moveDate', event.target.value)
                }}
                required
              />
            </label>
            <label>
              희망 시간
              <select
                value={form.moveTime}
                onChange={(event) => onChange('moveTime', event.target.value)}
                disabled={!form.moveDate || isLoadingAvailability || displayedTimes.length === 0}
                required
              >
                <option value="">{isLoadingAvailability ? '확인 중' : '시간 선택'}</option>
                {displayedTimes.map((time) => (
                  <option key={time} value={time}>{time}</option>
                ))}
              </select>
              {displayedMessage && <span className="field-message">{displayedMessage}</span>}
            </label>
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
            <label>
              쿠폰 코드
              <input
                value={form.couponCode}
                onChange={(event) => onChange('couponCode', event.target.value)}
                placeholder="WELCOME10"
              />
            </label>
          </div>
        </div>
      )}

      {stepMessage && <p className="message info">{stepMessage}</p>}
      {errorMessage && <p className="message error">{errorMessage}</p>}

      <div className="step-actions">
        <button className="submit-button secondary" type="button" onClick={goToPreviousStep} disabled={isFirstStep}>
          이전
        </button>
        <button className="submit-button" type="submit" disabled={isSubmitting}>
          {isLastStep ? (isSubmitting ? '예약 접수 중' : '예약 신청') : '다음'}
        </button>
      </div>
    </form>
  )
}
