import type { FormEvent } from 'react'
import type { ReservationSearchForm, ServiceMode } from '../types'
import { StatusNotice } from './StatusNotice'

type Props = {
  form: ReservationSearchForm
  errorMessage: string
  isSearching: boolean
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: <K extends keyof ReservationSearchForm>(key: K, value: ReservationSearchForm[K]) => void
  serviceMode?: ServiceMode
}

export function ReservationSearchFormView({
  form,
  errorMessage,
  isSearching,
  onSubmit,
  onChange,
  serviceMode = 'GENERAL',
}: Props) {
  const isNonProfitMode = serviceMode === 'NON_PROFIT'
  const submitButtonText = isSearching
    ? isNonProfitMode ? '요청 내역 확인 중' : '예약 조회 중'
    : isNonProfitMode ? '요청 내역 확인' : '예약 조회'

  return (
    <form className="reservation-form search-form" onSubmit={onSubmit}>
      <div className="section-heading">
        <div>
          <p className="eyebrow">{isNonProfitMode ? 'Request Lookup' : 'Reservation Lookup'}</p>
          <h2>{isNonProfitMode ? '요청 내역 확인' : '예약 조회'}</h2>
          <p>
            {isNonProfitMode
              ? '요청번호와 연락처로 접수 상태와 운영자 안내를 확인합니다.'
              : '예약번호와 연락처로 예약 상태, 견적, 사진 업로드 가능 여부를 확인합니다.'}
          </p>
        </div>
      </div>

      <div className="search-guide">
        <div>
          <strong>1</strong>
          <span>{isNonProfitMode ? '요청 완료 화면에 표시된 요청번호를 입력합니다.' : '예약 완료 화면에 표시된 예약번호를 입력합니다.'}</span>
        </div>
        <div>
          <strong>2</strong>
          <span>{isNonProfitMode ? '도움 요청 때 입력한 연락처를 그대로 입력합니다.' : '예약 신청 때 입력한 연락처를 그대로 입력합니다.'}</span>
        </div>
      </div>

      <div className="lookup-grid">
        <label>
          {isNonProfitMode ? '요청 번호' : '예약 번호'}
          <input
            type="number"
            min="1"
            value={form.reservationId}
            onChange={(event) => onChange('reservationId', event.target.value)}
            placeholder="1"
            required
          />
          <span className="field-message">
            {isNonProfitMode ? '요청 완료 화면에 표시된 숫자 번호를 입력해 주세요.' : '예약 완료 화면에 표시된 숫자 번호를 입력해 주세요.'}
          </span>
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
          <span className="field-message">{isNonProfitMode ? '도움 요청 때 입력한 연락처를 사용합니다.' : '예약 신청 때 입력한 연락처를 사용합니다.'}</span>
        </label>
      </div>

      <StatusNotice
        title={isNonProfitMode ? '요청번호를 모른다면' : '예약번호를 모른다면'}
        description={
          isNonProfitMode
            ? '도움 요청 완료 화면, 이메일 안내, 문자 안내를 먼저 확인해 주세요.'
            : '예약 완료 화면, 이메일 안내, 문자 안내를 먼저 확인해 주세요.'
        }
        actions={[
          isNonProfitMode
            ? '그래도 찾기 어렵다면 운영자에게 신청자명과 연락처를 알려 확인을 요청할 수 있습니다.'
            : '그래도 찾기 어렵다면 관리자에게 예약자명과 연락처를 알려 확인을 요청할 수 있습니다.',
          isNonProfitMode
            ? '연락처가 다르면 개인정보 보호를 위해 요청 정보가 표시되지 않습니다.'
            : '연락처가 다르면 개인정보 보호를 위해 예약 정보가 표시되지 않습니다.',
        ]}
        tone="info"
      />

      {errorMessage && (
        <StatusNotice
          title={isNonProfitMode ? '요청 내역을 확인하지 못했습니다' : '예약을 조회하지 못했습니다'}
          description={errorMessage}
          actions={[
            isNonProfitMode ? '요청 완료 화면에 나온 번호를 다시 확인해 주세요.' : '예약 완료 화면에 나온 번호를 다시 확인해 주세요.',
            isNonProfitMode ? '도움 요청 때 입력한 연락처와 같은 형식으로 입력해 주세요.' : '예약 신청 때 입력한 연락처와 같은 형식으로 입력해 주세요.',
          ]}
          tone="info"
        />
      )}

      <button className="submit-button" type="submit" disabled={isSearching}>
        {submitButtonText}
      </button>
    </form>
  )
}
