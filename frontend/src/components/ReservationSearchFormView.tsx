import type { FormEvent } from 'react'
import type { ReservationSearchForm } from '../types'

type Props = {
  form: ReservationSearchForm
  errorMessage: string
  isSearching: boolean
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: <K extends keyof ReservationSearchForm>(key: K, value: ReservationSearchForm[K]) => void
}

export function ReservationSearchFormView({ form, errorMessage, isSearching, onSubmit, onChange }: Props) {
  return (
    <form className="reservation-form search-form" onSubmit={onSubmit}>
      <div className="section-heading">
        <div>
          <p className="eyebrow">Reservation Lookup</p>
          <h2>예약 조회</h2>
          <p>예약번호와 연락처로 예약 상태, 견적, 사진 업로드 가능 여부를 확인합니다.</p>
        </div>
      </div>

      <div className="search-guide">
        <div>
          <strong>1</strong>
          <span>예약 완료 화면에 표시된 예약번호를 입력합니다.</span>
        </div>
        <div>
          <strong>2</strong>
          <span>예약 신청 때 입력한 연락처를 그대로 입력합니다.</span>
        </div>
      </div>

      <div className="lookup-grid">
        <label>
          예약 번호
          <input
            type="number"
            min="1"
            value={form.reservationId}
            onChange={(event) => onChange('reservationId', event.target.value)}
            placeholder="1"
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
        </label>
      </div>

      {errorMessage && <p className="message error">{errorMessage}</p>}

      <button className="submit-button" type="submit" disabled={isSearching}>
        {isSearching ? '예약 조회 중' : '예약 조회'}
      </button>
    </form>
  )
}
