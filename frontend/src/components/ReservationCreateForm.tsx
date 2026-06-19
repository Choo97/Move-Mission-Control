import type { FormEvent } from 'react'
import { moveTypeOptions } from '../reservationData'
import type { MoveType, ReservationForm } from '../types'

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
  return (
    <form className="reservation-form" onSubmit={onSubmit}>
      <div className="section-heading">
        <h2>예약 정보</h2>
        <p>이사 일정과 주소를 입력하면 접수 상태로 예약됩니다.</p>
      </div>

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
            value={form.phone}
            onChange={(event) => onChange('phone', event.target.value)}
            placeholder="010-1234-5678"
            required
          />
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
      </div>

      <div className="address-grid">
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

      {errorMessage && <p className="message error">{errorMessage}</p>}

      <button className="submit-button" type="submit" disabled={isSubmitting}>
        {isSubmitting ? '예약 접수 중' : '예약 신청'}
      </button>
    </form>
  )
}
