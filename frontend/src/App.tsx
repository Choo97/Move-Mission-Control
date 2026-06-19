import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8081'

type MoveType = 'STUDIO' | 'TWO_ROOM' | 'FAMILY' | 'OFFICE' | 'STORAGE'

type ReservationForm = {
  customerName: string
  phone: string
  email: string
  moveDate: string
  moveTime: string
  fromAddress: string
  toAddress: string
  moveType: MoveType
  fromElevator: boolean
  toElevator: boolean
  fromFloor: number
  toFloor: number
  fromLadderTruck: boolean
  toLadderTruck: boolean
  memo: string
  couponCode: string
}

type ApiErrorResponse = {
  code: string
  message: string
}

type ReservationResponse = {
  id: number
  customerName: string
  moveDate: string
  moveTime: string
  fromAddress: string
  toAddress: string
  moveTypeLabel: string
  statusLabel: string
  finalEstimatedPrice: number
}

const moveTypeOptions: Array<{ value: MoveType; label: string }> = [
  { value: 'STUDIO', label: '원룸' },
  { value: 'TWO_ROOM', label: '투룸' },
  { value: 'FAMILY', label: '가정집' },
  { value: 'OFFICE', label: '사무실' },
  { value: 'STORAGE', label: '보관 이사' },
]

const initialForm: ReservationForm = {
  customerName: '',
  phone: '',
  email: '',
  moveDate: '',
  moveTime: '10:00',
  fromAddress: '',
  toAddress: '',
  moveType: 'STUDIO',
  fromElevator: true,
  toElevator: true,
  fromFloor: 1,
  toFloor: 1,
  fromLadderTruck: false,
  toLadderTruck: false,
  memo: '',
  couponCode: '',
}

function App() {
  const [form, setForm] = useState<ReservationForm>(initialForm)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [reservation, setReservation] = useState<ReservationResponse | null>(null)

  const today = useMemo(() => new Date().toISOString().slice(0, 10), [])

  const updateField = <K extends keyof ReservationForm>(
    key: K,
    value: ReservationForm[K],
  ) => {
    setForm((current) => ({ ...current, [key]: value }))
  }

  const submitReservation = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSubmitting(true)
    setErrorMessage('')
    setReservation(null)

    try {
      const response = await fetch(`${API_BASE_URL}/api/reservations`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(form),
      })

      const data = await response.json()

      if (!response.ok) {
        const error = data as ApiErrorResponse
        throw new Error(error.message || '예약 신청에 실패했습니다.')
      }

      setReservation(data as ReservationResponse)
      setForm(initialForm)
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '예약 신청에 실패했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="app-shell">
      <header className="top-bar">
        <div>
          <p className="eyebrow">Move Mission Control</p>
          <h1>고객 이사 예약</h1>
        </div>
        <a className="admin-link" href={`${API_BASE_URL}/admin/reservations`}>
          관리자
        </a>
      </header>

      <section className="workspace">
        <form className="reservation-form" onSubmit={submitReservation}>
          <div className="section-heading">
            <h2>예약 정보</h2>
            <p>이사 일정과 주소를 입력하면 접수 상태로 예약됩니다.</p>
          </div>

          <div className="field-grid">
            <label>
              이름
              <input
                value={form.customerName}
                onChange={(event) => updateField('customerName', event.target.value)}
                placeholder="홍길동"
                required
              />
            </label>
            <label>
              연락처
              <input
                value={form.phone}
                onChange={(event) => updateField('phone', event.target.value)}
                placeholder="010-1234-5678"
                required
              />
            </label>
            <label>
              이메일
              <input
                type="email"
                value={form.email}
                onChange={(event) => updateField('email', event.target.value)}
                placeholder="customer@example.com"
              />
            </label>
            <label>
              이사 유형
              <select
                value={form.moveType}
                onChange={(event) => updateField('moveType', event.target.value as MoveType)}
              >
                {moveTypeOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>
            <label>
              이사 날짜
              <input
                type="date"
                min={today}
                value={form.moveDate}
                onChange={(event) => updateField('moveDate', event.target.value)}
                required
              />
            </label>
            <label>
              희망 시간
              <input
                type="time"
                value={form.moveTime}
                onChange={(event) => updateField('moveTime', event.target.value)}
                required
              />
            </label>
          </div>

          <div className="address-grid">
            <fieldset>
              <legend>출발지</legend>
              <label>
                주소
                <input
                  value={form.fromAddress}
                  onChange={(event) => updateField('fromAddress', event.target.value)}
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
                  onChange={(event) => updateField('fromFloor', Number(event.target.value))}
                  required
                />
              </label>
              <div className="switch-row">
                <label>
                  <input
                    type="checkbox"
                    checked={form.fromElevator}
                    onChange={(event) => updateField('fromElevator', event.target.checked)}
                  />
                  엘리베이터
                </label>
                <label>
                  <input
                    type="checkbox"
                    checked={form.fromLadderTruck}
                    onChange={(event) => updateField('fromLadderTruck', event.target.checked)}
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
                  onChange={(event) => updateField('toAddress', event.target.value)}
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
                  onChange={(event) => updateField('toFloor', Number(event.target.value))}
                  required
                />
              </label>
              <div className="switch-row">
                <label>
                  <input
                    type="checkbox"
                    checked={form.toElevator}
                    onChange={(event) => updateField('toElevator', event.target.checked)}
                  />
                  엘리베이터
                </label>
                <label>
                  <input
                    type="checkbox"
                    checked={form.toLadderTruck}
                    onChange={(event) => updateField('toLadderTruck', event.target.checked)}
                  />
                  사다리차
                </label>
              </div>
            </fieldset>
          </div>

          <div className="field-grid">
            <label className="wide">
              요청사항
              <textarea
                value={form.memo}
                onChange={(event) => updateField('memo', event.target.value)}
                placeholder="깨지기 쉬운 짐, 주차 정보 등을 적어주세요."
              />
            </label>
            <label>
              쿠폰 코드
              <input
                value={form.couponCode}
                onChange={(event) => updateField('couponCode', event.target.value)}
                placeholder="WELCOME10"
              />
            </label>
          </div>

          {errorMessage && <p className="message error">{errorMessage}</p>}

          <button className="submit-button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? '예약 접수 중' : '예약 신청'}
          </button>
        </form>

        <aside className="status-panel">
          <h2>접수 결과</h2>
          {reservation ? (
            <div className="result">
              <strong>예약 #{reservation.id}</strong>
              <dl>
                <div>
                  <dt>고객</dt>
                  <dd>{reservation.customerName}</dd>
                </div>
                <div>
                  <dt>일정</dt>
                  <dd>
                    {reservation.moveDate} {reservation.moveTime}
                  </dd>
                </div>
                <div>
                  <dt>유형</dt>
                  <dd>{reservation.moveTypeLabel}</dd>
                </div>
                <div>
                  <dt>상태</dt>
                  <dd>{reservation.statusLabel}</dd>
                </div>
                <div>
                  <dt>예상금액</dt>
                  <dd>{reservation.finalEstimatedPrice.toLocaleString()}원</dd>
                </div>
              </dl>
            </div>
          ) : (
            <p className="empty-state">예약 신청을 완료하면 접수 결과가 여기에 표시됩니다.</p>
          )}
        </aside>
      </section>
    </main>
  )
}

export default App
