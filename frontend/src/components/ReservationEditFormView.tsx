import type { FormEvent } from 'react'
import type { ReservationEditForm } from '../types'

type Props = {
  form: ReservationEditForm
  today: string
  isUpdating: boolean
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: <K extends keyof ReservationEditForm>(key: K, value: ReservationEditForm[K]) => void
  onCancel: () => void
}

export function ReservationEditFormView({ form, today, isUpdating, onSubmit, onChange, onCancel }: Props) {
  return (
    <form className="reservation-form edit-form" onSubmit={onSubmit}>
      <div className="section-heading">
        <h2>예약 수정</h2>
        <p>조회에 사용한 연락처로 본인 확인 후 예약 정보를 수정합니다.</p>
      </div>

      <div className="field-grid">
        <label>
          연락처
          <input value={form.phone} onChange={(event) => onChange('phone', event.target.value)} required />
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
            onChange={(event) => onChange('moveDate', event.target.value)}
            required
          />
        </label>
        <label>
          희망 시간
          <input
            type="time"
            value={form.moveTime}
            onChange={(event) => onChange('moveTime', event.target.value)}
            required
          />
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

      <div className="button-row">
        <button className="submit-button secondary" type="button" onClick={onCancel}>
          수정 취소
        </button>
        <button className="submit-button" type="submit" disabled={isUpdating}>
          {isUpdating ? '수정 저장 중' : '수정 저장'}
        </button>
      </div>
    </form>
  )
}
