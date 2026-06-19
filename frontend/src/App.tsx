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
  email: string
  moveDate: string
  moveTime: string
  fromAddress: string
  toAddress: string
  fromFloor: number
  toFloor: number
  fromLadderTruck: boolean
  toLadderTruck: boolean
  moveTypeLabel: string
  statusLabel: string
  distanceKm: number | null
  baseEstimatedPrice: number
  discountAmount: number
  finalEstimatedPrice: number
  couponCode: string | null
  couponName: string | null
  editable: boolean
  cancelable: boolean
  estimateAcceptable: boolean
  estimateAccepted: boolean
  acceptedEstimatePrice: number | null
  estimateLines: Array<{
    label: string
    amount: number
  }>
}

type ReservationSearchForm = {
  reservationId: string
  phone: string
}

type ReservationEditForm = {
  phone: string
  email: string
  moveDate: string
  moveTime: string
  fromAddress: string
  toAddress: string
  fromFloor: number
  toFloor: number
  fromLadderTruck: boolean
  toLadderTruck: boolean
  memo: string
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

const initialSearchForm: ReservationSearchForm = {
  reservationId: '',
  phone: '',
}

const toEditForm = (reservation: ReservationResponse, phone: string): ReservationEditForm => ({
  phone,
  email: reservation.email ?? '',
  moveDate: reservation.moveDate,
  moveTime: reservation.moveTime.slice(0, 5),
  fromAddress: reservation.fromAddress,
  toAddress: reservation.toAddress,
  fromFloor: reservation.fromFloor,
  toFloor: reservation.toFloor,
  fromLadderTruck: reservation.fromLadderTruck,
  toLadderTruck: reservation.toLadderTruck,
  memo: '',
})

function App() {
  const [activeView, setActiveView] = useState<'create' | 'search'>('create')
  const [form, setForm] = useState<ReservationForm>(initialForm)
  const [searchForm, setSearchForm] = useState<ReservationSearchForm>(initialSearchForm)
  const [editForm, setEditForm] = useState<ReservationEditForm | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isSearching, setIsSearching] = useState(false)
  const [isUpdating, setIsUpdating] = useState(false)
  const [isCanceling, setIsCanceling] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [searchErrorMessage, setSearchErrorMessage] = useState('')
  const [actionMessage, setActionMessage] = useState('')
  const [reservation, setReservation] = useState<ReservationResponse | null>(null)

  const today = useMemo(() => new Date().toISOString().slice(0, 10), [])

  const updateField = <K extends keyof ReservationForm>(
    key: K,
    value: ReservationForm[K],
  ) => {
    setForm((current) => ({ ...current, [key]: value }))
  }

  const updateEditField = <K extends keyof ReservationEditForm>(
    key: K,
    value: ReservationEditForm[K],
  ) => {
    setEditForm((current) => (current ? { ...current, [key]: value } : current))
  }

  const submitReservation = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSubmitting(true)
    setErrorMessage('')
    setActionMessage('')
    setReservation(null)
    setEditForm(null)
    const submittedPhone = form.phone

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

      const createdReservation = data as ReservationResponse
      setReservation(createdReservation)
      setSearchForm({
        reservationId: String(createdReservation.id),
        phone: submittedPhone,
      })
      setForm(initialForm)
      setActiveView('search')
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '예약 신청에 실패했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const searchReservation = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSearching(true)
    setSearchErrorMessage('')
    setActionMessage('')
    setReservation(null)
    setEditForm(null)

    try {
      const response = await fetch(`${API_BASE_URL}/api/reservations/search`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          reservationId: Number(searchForm.reservationId),
          phone: searchForm.phone,
        }),
      })

      const data = await response.json()

      if (!response.ok) {
        const error = data as ApiErrorResponse
        throw new Error(error.message || '예약 조회에 실패했습니다.')
      }

      setReservation(data as ReservationResponse)
    } catch (error) {
      setSearchErrorMessage(error instanceof Error ? error.message : '예약 조회에 실패했습니다.')
    } finally {
      setIsSearching(false)
    }
  }

  const startEdit = () => {
    if (!reservation) {
      return
    }

    setActionMessage('')
    setEditForm(toEditForm(reservation, searchForm.phone))
  }

  const submitReservationUpdate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!reservation || !editForm) {
      return
    }

    setIsUpdating(true)
    setActionMessage('')

    try {
      const response = await fetch(`${API_BASE_URL}/api/reservations/${reservation.id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(editForm),
      })

      const data = await response.json()

      if (!response.ok) {
        const error = data as ApiErrorResponse
        throw new Error(error.message || '예약 수정에 실패했습니다.')
      }

      const updatedReservation = data as ReservationResponse
      setReservation(updatedReservation)
      setSearchForm({
        reservationId: String(updatedReservation.id),
        phone: editForm.phone,
      })
      setEditForm(null)
      setActionMessage('예약 정보가 수정되었습니다.')
    } catch (error) {
      setActionMessage(error instanceof Error ? error.message : '예약 수정에 실패했습니다.')
    } finally {
      setIsUpdating(false)
    }
  }

  const cancelReservation = async () => {
    if (!reservation || !searchForm.phone) {
      setActionMessage('예약 조회에 사용한 연락처가 필요합니다.')
      return
    }

    const confirmed = window.confirm('예약을 취소하시겠습니까?')

    if (!confirmed) {
      return
    }

    setIsCanceling(true)
    setActionMessage('')

    try {
      const response = await fetch(`${API_BASE_URL}/api/reservations/${reservation.id}/cancel`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ phone: searchForm.phone }),
      })

      const data = await response.json()

      if (!response.ok) {
        const error = data as ApiErrorResponse
        throw new Error(error.message || '예약 취소에 실패했습니다.')
      }

      setReservation(data as ReservationResponse)
      setEditForm(null)
      setActionMessage('예약이 취소되었습니다.')
    } catch (error) {
      setActionMessage(error instanceof Error ? error.message : '예약 취소에 실패했습니다.')
    } finally {
      setIsCanceling(false)
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

      <nav className="view-tabs" aria-label="고객 예약 메뉴">
        <button
          type="button"
          className={activeView === 'create' ? 'active' : ''}
          onClick={() => {
            setActiveView('create')
            setErrorMessage('')
            setActionMessage('')
          }}
        >
          예약 신청
        </button>
        <button
          type="button"
          className={activeView === 'search' ? 'active' : ''}
          onClick={() => {
            setActiveView('search')
            setSearchErrorMessage('')
            setActionMessage('')
          }}
        >
          예약 조회
        </button>
      </nav>

      <section className="workspace">
        {activeView === 'create' ? (
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
        ) : (
          <form className="reservation-form search-form" onSubmit={searchReservation}>
            <div className="section-heading">
              <h2>예약 조회</h2>
              <p>예약 번호와 연락처가 일치하면 상세 정보를 확인할 수 있습니다.</p>
            </div>

            <div className="field-grid">
              <label>
                예약 번호
                <input
                  type="number"
                  min="1"
                  value={searchForm.reservationId}
                  onChange={(event) =>
                    setSearchForm((current) => ({
                      ...current,
                      reservationId: event.target.value,
                    }))
                  }
                  placeholder="1"
                  required
                />
              </label>
              <label>
                연락처
                <input
                  value={searchForm.phone}
                  onChange={(event) =>
                    setSearchForm((current) => ({ ...current, phone: event.target.value }))
                  }
                  placeholder="010-1234-5678"
                  required
                />
              </label>
            </div>

            {searchErrorMessage && <p className="message error">{searchErrorMessage}</p>}

            <button className="submit-button" type="submit" disabled={isSearching}>
              {isSearching ? '예약 조회 중' : '예약 조회'}
            </button>
          </form>
        )}

        {editForm && reservation && (
          <form className="reservation-form edit-form" onSubmit={submitReservationUpdate}>
            <div className="section-heading">
              <h2>예약 수정</h2>
              <p>조회에 사용한 연락처로 본인 확인 후 예약 정보를 수정합니다.</p>
            </div>

            <div className="field-grid">
              <label>
                연락처
                <input
                  value={editForm.phone}
                  onChange={(event) => updateEditField('phone', event.target.value)}
                  required
                />
              </label>
              <label>
                이메일
                <input
                  type="email"
                  value={editForm.email}
                  onChange={(event) => updateEditField('email', event.target.value)}
                />
              </label>
              <label>
                이사 날짜
                <input
                  type="date"
                  min={today}
                  value={editForm.moveDate}
                  onChange={(event) => updateEditField('moveDate', event.target.value)}
                  required
                />
              </label>
              <label>
                희망 시간
                <input
                  type="time"
                  value={editForm.moveTime}
                  onChange={(event) => updateEditField('moveTime', event.target.value)}
                  required
                />
              </label>
              <label>
                출발지 주소
                <input
                  value={editForm.fromAddress}
                  onChange={(event) => updateEditField('fromAddress', event.target.value)}
                  required
                />
              </label>
              <label>
                도착지 주소
                <input
                  value={editForm.toAddress}
                  onChange={(event) => updateEditField('toAddress', event.target.value)}
                  required
                />
              </label>
              <label>
                출발지 층수
                <input
                  type="number"
                  min="1"
                  max="50"
                  value={editForm.fromFloor}
                  onChange={(event) => updateEditField('fromFloor', Number(event.target.value))}
                  required
                />
              </label>
              <label>
                도착지 층수
                <input
                  type="number"
                  min="1"
                  max="50"
                  value={editForm.toFloor}
                  onChange={(event) => updateEditField('toFloor', Number(event.target.value))}
                  required
                />
              </label>
            </div>

            <div className="switch-row edit-switches">
              <label>
                <input
                  type="checkbox"
                  checked={editForm.fromLadderTruck}
                  onChange={(event) => updateEditField('fromLadderTruck', event.target.checked)}
                />
                출발지 사다리차
              </label>
              <label>
                <input
                  type="checkbox"
                  checked={editForm.toLadderTruck}
                  onChange={(event) => updateEditField('toLadderTruck', event.target.checked)}
                />
                도착지 사다리차
              </label>
            </div>

            <label className="wide edit-memo">
              요청사항
              <textarea
                value={editForm.memo}
                onChange={(event) => updateEditField('memo', event.target.value)}
              />
            </label>

            <div className="button-row">
              <button className="submit-button secondary" type="button" onClick={() => setEditForm(null)}>
                수정 취소
              </button>
              <button className="submit-button" type="submit" disabled={isUpdating}>
                {isUpdating ? '수정 저장 중' : '수정 저장'}
              </button>
            </div>
          </form>
        )}

        <aside className="status-panel">
          <h2>{activeView === 'create' ? '접수 결과' : '예약 상세'}</h2>
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
                  <dt>출발</dt>
                  <dd>{reservation.fromAddress}</dd>
                </div>
                <div>
                  <dt>도착</dt>
                  <dd>{reservation.toAddress}</dd>
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
                  <dt>거리</dt>
                  <dd>{reservation.distanceKm === null ? '확인 전' : `${reservation.distanceKm}km`}</dd>
                </div>
                <div>
                  <dt>할인</dt>
                  <dd>{reservation.discountAmount.toLocaleString()}원</dd>
                </div>
                <div>
                  <dt>예상금액</dt>
                  <dd>{reservation.finalEstimatedPrice.toLocaleString()}원</dd>
                </div>
              </dl>
              <div className="action-state">
                {reservation.editable && <span>수정 가능</span>}
                {reservation.cancelable && <span>취소 가능</span>}
                {reservation.estimateAcceptable && <span>견적 동의 가능</span>}
                {reservation.estimateAccepted && <span>견적 동의 완료</span>}
              </div>
              {reservation.estimateLines.length > 0 && (
                <div className="estimate-lines">
                  <h3>견적 내역</h3>
                  <dl>
                    {reservation.estimateLines.map((line) => (
                      <div key={line.label}>
                        <dt>{line.label}</dt>
                        <dd>{line.amount.toLocaleString()}원</dd>
                      </div>
                    ))}
                  </dl>
                </div>
              )}
              {(actionMessage || reservation.editable || reservation.cancelable) && (
                <div className="customer-actions">
                  {actionMessage && <p className="message info">{actionMessage}</p>}
                  <div className="button-row">
                    {reservation.editable && (
                      <button className="submit-button secondary" type="button" onClick={startEdit}>
                        예약 수정
                      </button>
                    )}
                    {reservation.cancelable && (
                      <button
                        className="submit-button danger"
                        type="button"
                        disabled={isCanceling}
                        onClick={cancelReservation}
                      >
                        {isCanceling ? '취소 처리 중' : '예약 취소'}
                      </button>
                    )}
                  </div>
                </div>
              )}
            </div>
          ) : (
            <p className="empty-state">
              {activeView === 'create'
                ? '예약 신청을 완료하면 접수 결과가 여기에 표시됩니다.'
                : '예약을 조회하면 상세 정보가 여기에 표시됩니다.'}
            </p>
          )}
        </aside>
      </section>
    </main>
  )
}

export default App
