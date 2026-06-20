import { useCallback, useEffect, useState } from 'react'
import {
  AdminAuthenticationRequiredError,
  calculateAdminReservationDistance,
  getAdminReservation,
  resendAdminReservationFailedEmails,
  sendAdminReservationEmails,
  updateAdminReservationDistance,
  updateAdminReservationEstimate,
  updateAdminReservationMemo,
  updateAdminReservationStatus,
} from '../api/adminApi'
import { getErrorMessage } from '../api/apiError'
import { API_BASE_URL } from '../reservationData'
import type { AdminReservationDetailResponse, ReservationStatus } from '../types'

type Props = {
  reservationId: number
  onClose: () => void
  onReservationChanged: () => void
}

const formatPrice = (price: number) => `${price.toLocaleString()}원`

const statusOptions: Array<{ value: ReservationStatus; label: string }> = [
  { value: 'RECEIVED', label: '접수' },
  { value: 'CONSULTING', label: '상담중' },
  { value: 'ESTIMATE_SENT', label: '견적안내' },
  { value: 'CONFIRMED', label: '확정' },
  { value: 'COMPLETED', label: '완료' },
  { value: 'CANCELED', label: '취소' },
]

export function AdminReservationDetailPanel({ reservationId, onClose, onReservationChanged }: Props) {
  const [reservation, setReservation] = useState<AdminReservationDetailResponse | null>(null)
  const [selectedStatus, setSelectedStatus] = useState<ReservationStatus>('RECEIVED')
  const [estimateAmount, setEstimateAmount] = useState('')
  const [distanceAmount, setDistanceAmount] = useState('')
  const [adminMemo, setAdminMemo] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isCalculatingDistance, setIsCalculatingDistance] = useState(false)
  const [isUpdatingDistance, setIsUpdatingDistance] = useState(false)
  const [isUpdatingEstimate, setIsUpdatingEstimate] = useState(false)
  const [isUpdatingMemo, setIsUpdatingMemo] = useState(false)
  const [isResendingEmail, setIsResendingEmail] = useState(false)
  const [isSendingEmail, setIsSendingEmail] = useState(false)
  const [isAuthenticationRequired, setIsAuthenticationRequired] = useState(false)
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [actionMessage, setActionMessage] = useState('')

  const showAdminError = useCallback((error: unknown, fallbackMessage: string) => {
    setIsAuthenticationRequired(error instanceof AdminAuthenticationRequiredError)
    setErrorMessage(getErrorMessage(error, fallbackMessage))
  }, [])

  useEffect(() => {
    const loadReservation = async () => {
      setIsLoading(true)
      setErrorMessage('')
      setReservation(null)

      try {
        const loadedReservation = await getAdminReservation(reservationId)
        setReservation(loadedReservation)
        setSelectedStatus(loadedReservation.status)
        setEstimateAmount(String(loadedReservation.estimatedPrice))
        setDistanceAmount(loadedReservation.distanceKm === null ? '' : String(loadedReservation.distanceKm))
        setAdminMemo(loadedReservation.adminMemo ?? '')
      } catch (error) {
        showAdminError(error, '관리자 예약 상세를 불러오지 못했습니다.')
      } finally {
        setIsLoading(false)
      }
    }

    void loadReservation()
  }, [reservationId, showAdminError])

  const submitStatusUpdate = async () => {
    if (!reservation || selectedStatus === reservation.status) {
      return
    }

    setIsUpdatingStatus(true)
    setErrorMessage('')
    setActionMessage('')

    try {
      const updatedReservation = await updateAdminReservationStatus(reservation.id, selectedStatus)
      setReservation(updatedReservation)
      setSelectedStatus(updatedReservation.status)
      setEstimateAmount(String(updatedReservation.estimatedPrice))
      setDistanceAmount(updatedReservation.distanceKm === null ? '' : String(updatedReservation.distanceKm))
      setActionMessage(`예약 상태가 '${updatedReservation.statusLabel}'(으)로 변경되었습니다.`)
      onReservationChanged()
    } catch (error) {
      showAdminError(error, '관리자 예약 상태 변경에 실패했습니다.')
    } finally {
      setIsUpdatingStatus(false)
    }
  }

  const submitEstimateUpdate = async () => {
    if (!reservation) {
      return
    }

    const nextEstimateAmount = Number(estimateAmount)

    if (estimateAmount.trim() === '' || Number.isNaN(nextEstimateAmount)) {
      setErrorMessage('견적 금액을 숫자로 입력해 주세요.')
      setActionMessage('')
      return
    }

    if (nextEstimateAmount < 0) {
      setErrorMessage('견적 금액은 0원 이상이어야 합니다.')
      setActionMessage('')
      return
    }

    setIsUpdatingEstimate(true)
    setErrorMessage('')
    setActionMessage('')

    try {
      const updatedReservation = await updateAdminReservationEstimate(reservation.id, nextEstimateAmount)
      setReservation(updatedReservation)
      setSelectedStatus(updatedReservation.status)
      setEstimateAmount(String(updatedReservation.estimatedPrice))
      setDistanceAmount(updatedReservation.distanceKm === null ? '' : String(updatedReservation.distanceKm))
      setActionMessage(`견적 금액이 ${formatPrice(updatedReservation.estimatedPrice)}으로 저장되었습니다.`)
      onReservationChanged()
    } catch (error) {
      showAdminError(error, '관리자 예약 견적 저장에 실패했습니다.')
    } finally {
      setIsUpdatingEstimate(false)
    }
  }

  const submitDistanceUpdate = async () => {
    if (!reservation) {
      return
    }

    const nextDistance = distanceAmount.trim() === '' ? null : Number(distanceAmount)

    if (nextDistance !== null && Number.isNaN(nextDistance)) {
      setErrorMessage('이동 거리를 숫자로 입력해 주세요.')
      setActionMessage('')
      return
    }

    if (nextDistance !== null && nextDistance < 0) {
      setErrorMessage('이동 거리는 0km 이상이어야 합니다.')
      setActionMessage('')
      return
    }

    setIsUpdatingDistance(true)
    setErrorMessage('')
    setActionMessage('')

    try {
      const updatedReservation = await updateAdminReservationDistance(reservation.id, nextDistance)
      setReservation(updatedReservation)
      setSelectedStatus(updatedReservation.status)
      setEstimateAmount(String(updatedReservation.estimatedPrice))
      setDistanceAmount(updatedReservation.distanceKm === null ? '' : String(updatedReservation.distanceKm))
      setActionMessage(
        updatedReservation.distanceKm === null
          ? '이동 거리를 확인 전으로 저장했습니다.'
          : `이동 거리를 ${updatedReservation.distanceKm}km로 저장하고 견적을 다시 계산했습니다.`,
      )
      onReservationChanged()
    } catch (error) {
      showAdminError(error, '관리자 예약 이동 거리 저장에 실패했습니다.')
    } finally {
      setIsUpdatingDistance(false)
    }
  }

  const calculateDistance = async () => {
    if (!reservation) {
      return
    }

    setIsCalculatingDistance(true)
    setErrorMessage('')
    setActionMessage('')

    try {
      const updatedReservation = await calculateAdminReservationDistance(reservation.id)
      setReservation(updatedReservation)
      setSelectedStatus(updatedReservation.status)
      setEstimateAmount(String(updatedReservation.estimatedPrice))
      setDistanceAmount(updatedReservation.distanceKm === null ? '' : String(updatedReservation.distanceKm))
      setActionMessage(`이동 거리를 ${updatedReservation.distanceKm}km로 자동 계산하고 견적에 반영했습니다.`)
      onReservationChanged()
    } catch (error) {
      showAdminError(error, '관리자 예약 이동 거리 자동 계산에 실패했습니다.')
    } finally {
      setIsCalculatingDistance(false)
    }
  }

  const submitAdminMemoUpdate = async () => {
    if (!reservation || adminMemo === (reservation.adminMemo ?? '')) {
      return
    }

    if (adminMemo.length > 1000) {
      setErrorMessage('관리자 메모는 1,000자 이내로 입력해 주세요.')
      setActionMessage('')
      return
    }

    setIsUpdatingMemo(true)
    setErrorMessage('')
    setActionMessage('')

    try {
      const updatedReservation = await updateAdminReservationMemo(reservation.id, adminMemo)
      setReservation(updatedReservation)
      setAdminMemo(updatedReservation.adminMemo ?? '')
      setActionMessage(adminMemo.trim() === '' ? '관리자 메모를 비웠습니다.' : '관리자 메모를 저장했습니다.')
    } catch (error) {
      showAdminError(error, '관리자 예약 메모 저장에 실패했습니다.')
    } finally {
      setIsUpdatingMemo(false)
    }
  }

  const sendReadyEmails = async () => {
    if (!reservation) {
      return
    }

    setIsSendingEmail(true)
    setErrorMessage('')
    setActionMessage('')

    try {
      const result = await sendAdminReservationEmails(reservation.id)
      setReservation(result.reservation)
      setActionMessage(`이메일 ${result.sentCount}건 발송, ${result.failedCount}건 실패로 처리했습니다.`)
    } catch (error) {
      showAdminError(error, '관리자 예약 이메일 발송에 실패했습니다.')
    } finally {
      setIsSendingEmail(false)
    }
  }

  const resendFailedEmails = async () => {
    if (!reservation) {
      return
    }

    setIsResendingEmail(true)
    setErrorMessage('')
    setActionMessage('')

    try {
      const result = await resendAdminReservationFailedEmails(reservation.id)
      setReservation(result.reservation)
      setActionMessage(`실패 이메일 ${result.sentCount}건 재발송, ${result.failedCount}건 실패로 처리했습니다.`)
    } catch (error) {
      showAdminError(error, '관리자 예약 실패 이메일 재발송에 실패했습니다.')
    } finally {
      setIsResendingEmail(false)
    }
  }

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
      {errorMessage &&
        (isAuthenticationRequired ? (
          <div className="admin-auth-notice">
            <strong>{errorMessage}</strong>
            <a href={`${API_BASE_URL}/login`}>관리자 로그인</a>
          </div>
        ) : (
          <p className="message error">{errorMessage}</p>
        ))}
      {actionMessage && <p className="message info">{actionMessage}</p>}

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
            <h3>상태 변경</h3>
            <div className="admin-status-update">
              <label>
                예약 상태
                <select
                  value={selectedStatus}
                  onChange={(event) => setSelectedStatus(event.target.value as ReservationStatus)}
                >
                  {statusOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <button
                type="button"
                disabled={isUpdatingStatus || selectedStatus === reservation.status}
                onClick={submitStatusUpdate}
              >
                {isUpdatingStatus ? '변경 중' : '상태 저장'}
              </button>
            </div>
          </section>

          <section>
            <h3>이동 거리</h3>
            <div className="admin-distance-update">
              <label>
                거리(km)
                <input
                  type="number"
                  min="0"
                  step="1"
                  placeholder="확인 전"
                  value={distanceAmount}
                  onChange={(event) => setDistanceAmount(event.target.value)}
                />
              </label>
              <button
                type="button"
                disabled={
                  isUpdatingDistance ||
                  (distanceAmount.trim() === '' ? null : Number(distanceAmount)) === reservation.distanceKm
                }
                onClick={submitDistanceUpdate}
              >
                {isUpdatingDistance ? '저장 중' : '거리 저장'}
              </button>
              <button type="button" disabled={isCalculatingDistance} onClick={calculateDistance}>
                {isCalculatingDistance ? '계산 중' : '자동 계산'}
              </button>
            </div>
          </section>

          <section>
            <h3>견적 내역</h3>
            <div className="admin-estimate-update">
              <label>
                견적 금액
                <input
                  type="number"
                  min="0"
                  step="1000"
                  value={estimateAmount}
                  onChange={(event) => setEstimateAmount(event.target.value)}
                />
              </label>
              <button
                type="button"
                disabled={isUpdatingEstimate || Number(estimateAmount) === reservation.estimatedPrice}
                onClick={submitEstimateUpdate}
              >
                {isUpdatingEstimate ? '저장 중' : '견적 저장'}
              </button>
            </div>
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
              <div className="admin-memo-editor">
                <strong>관리자 메모</strong>
                <textarea
                  maxLength={1000}
                  placeholder="상담 내용과 현장 특이사항을 기록하세요."
                  value={adminMemo}
                  onChange={(event) => setAdminMemo(event.target.value)}
                />
                <div className="admin-memo-footer">
                  <span>{adminMemo.length}/1,000자</span>
                  <button
                    type="button"
                    disabled={isUpdatingMemo || adminMemo === (reservation.adminMemo ?? '')}
                    onClick={submitAdminMemoUpdate}
                  >
                    {isUpdatingMemo ? '저장 중' : '메모 저장'}
                  </button>
                </div>
                {reservation.adminMemoUpdatedBy && <small>최근 저장: {reservation.adminMemoUpdatedBy}</small>}
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

          <section>
            <div className="admin-section-heading">
              <h3>알림 이력</h3>
              <div className="admin-email-actions">
                <button
                  type="button"
                  disabled={
                    isSendingEmail ||
                    !reservation.notifications.some(
                      (notification) => notification.channel === 'EMAIL' && notification.status === 'READY',
                    )
                  }
                  onClick={sendReadyEmails}
                >
                  {isSendingEmail ? '발송 중' : '준비 이메일 발송'}
                </button>
                <button
                  type="button"
                  disabled={
                    isResendingEmail ||
                    !reservation.notifications.some(
                      (notification) => notification.channel === 'EMAIL' && notification.status === 'FAILED',
                    )
                  }
                  onClick={resendFailedEmails}
                >
                  {isResendingEmail ? '재발송 중' : '실패 이메일 재발송'}
                </button>
              </div>
            </div>
            {reservation.notifications.length > 0 ? (
              <ul className="admin-history-list notification-history-list">
                {reservation.notifications.map((notification) => (
                  <li key={notification.id}>
                    <div className="admin-history-heading">
                      <strong>{notification.typeLabel}</strong>
                      <div>
                        <span className="history-badge">{notification.channelLabel}</span>
                        <span className={`history-badge ${notification.status.toLowerCase()}`}>
                          {notification.statusLabel}
                        </span>
                      </div>
                    </div>
                    <span>
                      {notification.createdAt.replace('T', ' ').slice(0, 16)} · {notification.recipientContact}
                    </span>
                    <p>{notification.message}</p>
                    {notification.failureReason && <p className="history-error">{notification.failureReason}</p>}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="admin-empty inline">알림 이력이 없습니다.</p>
            )}
          </section>

          <section>
            <h3>관리자 감사로그</h3>
            {reservation.auditLogs.length > 0 ? (
              <ul className="admin-history-list">
                {reservation.auditLogs.map((auditLog) => (
                  <li key={auditLog.id}>
                    <strong>{auditLog.action}</strong>
                    <span>
                      {auditLog.createdAt.replace('T', ' ').slice(0, 16)} · {auditLog.createdBy}
                    </span>
                    <p>{auditLog.detail}</p>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="admin-empty inline">관리자 감사로그가 없습니다.</p>
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
