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
        <h2>예약 조회</h2>
        <p>예약 번호와 연락처가 일치하면 상세 정보를 확인할 수 있습니다.</p>
      </div>

      <div className="field-grid">
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
