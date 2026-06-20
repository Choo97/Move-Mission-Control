import { useEffect, useState } from 'react'
import { getAdminReservation } from '../api/adminApi'
import { getErrorMessage } from '../api/apiError'
import { API_BASE_URL } from '../reservationData'
import type { AdminReservationDetailResponse } from '../types'

type Props = {
  reservationId: number
  onClose: () => void
}

const formatPrice = (price: number) => `${price.toLocaleString()}원`

export function AdminReservationDetailPanel({ reservationId, onClose }: Props) {
  const [reservation, setReservation] = useState<AdminReservationDetailResponse | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    const loadReservation = async () => {
      setIsLoading(true)
      setErrorMessage('')
      setReservation(null)

      try {
        setReservation(await getAdminReservation(reservationId))
      } catch (error) {
        setErrorMessage(getErrorMessage(error, '관리자 예약 상세를 불러오지 못했습니다.'))
      } finally {
        setIsLoading(false)
      }
    }

    void loadReservation()
  }, [reservationId])

  return (
    <aside className="admin-detail-panel">
      <div className="admin-detail-heading">
        <div>
          <p className="eyebrow">Reservation Detail</p>
          <h2>예약 #{reservationId}</h2>
        </div>
        <button type="button" onClick={onClose}>
          닫기
        </button>
      </div>

      {isLoading && <p className="admin-loading">예약 상세를 불러오는 중입니다.</p>}
      {errorMessage && <p className="message error">{errorMessage}</p>}

      {reservation && (
        <div className="admin-detail-content">
          <section>
            <div className="admin-detail-title">
              <strong>{reservation.customerName}</strong>
              <span className="admin-status">{reservation.statusLabel}</span>
            </div>
            <dl className="admin-detail-grid">
              <div>
                <dt>연락처</dt>
                <dd>{reservation.phone}</dd>
              </div>
              <div>
                <dt>이메일</dt>
                <dd>{reservation.email}</dd>
              </div>
              <div>
                <dt>이사일</dt>
                <dd>
                  {reservation.moveDate} {reservation.moveTime.slice(0, 5)}
                </dd>
              </div>
              <div>
                <dt>이사 유형</dt>
                <dd>{reservation.moveTypeLabel}</dd>
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
                <dt>거리</dt>
                <dd>{reservation.distanceKm === null ? '확인 전' : `${reservation.distanceKm}km`}</dd>
              </div>
              <div>
                <dt>최종 견적</dt>
                <dd>{formatPrice(reservation.finalEstimatedPrice)}</dd>
              </div>
            </dl>
          </section>

          <section>
            <h3>견적 내역</h3>
            <dl className="admin-detail-grid compact">
              {reservation.estimateLines.map((line) => (
                <div key={line.label}>
                  <dt>{line.label}</dt>
                  <dd>{formatPrice(line.amount)}</dd>
                </div>
              ))}
              <div>
                <dt>할인</dt>
                <dd>{formatPrice(reservation.discountAmount)}</dd>
              </div>
            </dl>
          </section>

          <section>
            <h3>메모</h3>
            <div className="admin-note-grid">
              <div>
                <strong>고객 요청사항</strong>
                <p>{reservation.memo || '입력된 요청사항이 없습니다.'}</p>
              </div>
              <div>
                <strong>관리자 메모</strong>
                <p>{reservation.adminMemo || '저장된 관리자 메모가 없습니다.'}</p>
              </div>
            </div>
          </section>

          <section>
            <h3>상태 이력</h3>
            {reservation.statusHistories.length > 0 ? (
              <ul className="admin-history-list">
                {reservation.statusHistories.map((history) => (
                  <li key={history.id}>
                    <strong>
                      {history.previousStatusLabel} → {history.changedStatusLabel}
                    </strong>
                    <span>
                      {history.changedAt.replace('T', ' ').slice(0, 16)} · {history.changedBy ?? 'system'}
                    </span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="admin-empty inline">상태 변경 이력이 없습니다.</p>
            )}
          </section>

          <section>
            <h3>고객 행동 이력</h3>
            {reservation.customerActionHistories.length > 0 ? (
              <ul className="admin-history-list">
                {reservation.customerActionHistories.map((history) => (
                  <li key={history.id}>
                    <strong>{history.summary}</strong>
                    <span>{history.createdAt.replace('T', ' ').slice(0, 16)}</span>
                    <p>{history.detail}</p>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="admin-empty inline">고객 행동 이력이 없습니다.</p>
            )}
          </section>

          <div className="admin-detail-actions">
            <a className="admin-row-link" href={`${API_BASE_URL}/admin/reservations/${reservation.id}`}>
              기존 상세 화면 열기
            </a>
          </div>
        </div>
      )}
    </aside>
  )
}
