import { useCallback, useEffect, useState } from 'react'
import {
  AdminAuthenticationRequiredError,
  approveAdminCustomerRequest,
  getAdminReservation,
  rejectAdminCustomerRequest,
  resendAdminReservationFailedEmails,
  resendAdminReservationFailedSms,
  sendAdminReservationEmails,
  sendAdminReservationSms,
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
const formatAdjustment = (price: number) => `${price > 0 ? '+' : ''}${price.toLocaleString()}원`

const statusOptions: Array<{ value: ReservationStatus; label: string }> = [
  { value: 'RECEIVED', label: '접수' },
  { value: 'CONSULTING', label: '상담중' },
  { value: 'ESTIMATE_SENT', label: '견적안내' },
  { value: 'CONFIRMED', label: '확정' },
  { value: 'COMPLETED', label: '완료' },
  { value: 'CANCELED', label: '취소' },
]

const getAdminNextTask = (reservation: AdminReservationDetailResponse) => {
  if (reservation.customerRequests.some((request) => request.status === 'PENDING')) {
    return '고객 수정/취소 요청을 승인 또는 반려하세요.'
  }

  if (reservation.status === 'CANCELED') {
    return '취소된 예약입니다. 추가 상담이 필요한 경우 메모만 남겨 주세요.'
  }

  if (reservation.status === 'COMPLETED') {
    return '완료된 예약입니다. 리뷰와 고객 행동 이력을 확인하세요.'
  }

  if (reservation.distanceKm === null) {
    return '이동 거리를 확인하고 견적을 점검하세요.'
  }

  if (reservation.status === 'RECEIVED') {
    return '고객에게 상담을 시작하고 상태를 상담중으로 변경하세요.'
  }

  if (reservation.status === 'CONSULTING') {
    return '상담 내용을 메모하고 견적 안내 상태로 변경하세요.'
  }

  if (reservation.status === 'ESTIMATE_SENT' && !reservation.estimateAccepted) {
    return '고객의 견적 동의 여부를 확인하세요.'
  }

  if (reservation.status === 'CONFIRMED') {
    return '이사 진행 후 완료 상태로 변경하세요.'
  }

  return '예약 정보를 확인하고 필요한 운영 작업을 진행하세요.'
}

const countNotifications = (
  reservation: AdminReservationDetailResponse,
  status: 'READY' | 'SENT' | 'FAILED',
) => reservation.notifications.filter((notification) => notification.status === status).length

export function AdminReservationDetailPanel({ reservationId, onClose, onReservationChanged }: Props) {
  const [reservation, setReservation] = useState<AdminReservationDetailResponse | null>(null)
  const [selectedStatus, setSelectedStatus] = useState<ReservationStatus>('RECEIVED')
  const [estimateAmount, setEstimateAmount] = useState('')
  const [distanceAmount, setDistanceAmount] = useState('')
  const [adminMemo, setAdminMemo] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isUpdatingDistance, setIsUpdatingDistance] = useState(false)
  const [isUpdatingEstimate, setIsUpdatingEstimate] = useState(false)
  const [isUpdatingMemo, setIsUpdatingMemo] = useState(false)
  const [isResendingEmail, setIsResendingEmail] = useState(false)
  const [isSendingEmail, setIsSendingEmail] = useState(false)
  const [isResendingSms, setIsResendingSms] = useState(false)
  const [isSendingSms, setIsSendingSms] = useState(false)
  const [processingCustomerRequestId, setProcessingCustomerRequestId] = useState<number | null>(null)
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

  const sendReadySms = async () => {
    if (!reservation) {
      return
    }

    setIsSendingSms(true)
    setErrorMessage('')
    setActionMessage('')

    try {
      const result = await sendAdminReservationSms(reservation.id)
      setReservation(result.reservation)
      setActionMessage(`SMS ${result.sentCount}건 발송, ${result.failedCount}건 실패로 처리했습니다.`)
    } catch (error) {
      showAdminError(error, '관리자 예약 SMS 발송에 실패했습니다.')
    } finally {
      setIsSendingSms(false)
    }
  }

  const resendFailedSms = async () => {
    if (!reservation) {
      return
    }

    setIsResendingSms(true)
    setErrorMessage('')
    setActionMessage('')

    try {
      const result = await resendAdminReservationFailedSms(reservation.id)
      setReservation(result.reservation)
      setActionMessage(`실패 SMS ${result.sentCount}건 재발송, ${result.failedCount}건 실패로 처리했습니다.`)
    } catch (error) {
      showAdminError(error, '관리자 예약 실패 SMS 재발송에 실패했습니다.')
    } finally {
      setIsResendingSms(false)
    }
  }

  const approveCustomerRequest = async (requestId: number) => {
    setProcessingCustomerRequestId(requestId)
    setErrorMessage('')
    setActionMessage('')

    try {
      const updatedReservation = await approveAdminCustomerRequest(requestId)
      setReservation(updatedReservation)
      setSelectedStatus(updatedReservation.status)
      setEstimateAmount(String(updatedReservation.estimatedPrice))
      setDistanceAmount(updatedReservation.distanceKm === null ? '' : String(updatedReservation.distanceKm))
      setAdminMemo(updatedReservation.adminMemo ?? '')
      setActionMessage('고객 요청을 승인했습니다.')
      onReservationChanged()
    } catch (error) {
      showAdminError(error, '고객 요청 승인에 실패했습니다.')
    } finally {
      setProcessingCustomerRequestId(null)
    }
  }

  const rejectCustomerRequest = async (requestId: number) => {
    const rejectionReason = window.prompt('반려 사유를 입력해 주세요.')

    if (rejectionReason === null) {
      return
    }

    if (rejectionReason.trim() === '') {
      setErrorMessage('반려 사유를 입력해 주세요.')
      setActionMessage('')
      return
    }

    setProcessingCustomerRequestId(requestId)
    setErrorMessage('')
    setActionMessage('')

    try {
      const updatedReservation = await rejectAdminCustomerRequest(requestId, rejectionReason.trim())
      setReservation(updatedReservation)
      setActionMessage('고객 요청을 반려했습니다.')
      onReservationChanged()
    } catch (error) {
      showAdminError(error, '고객 요청 반려에 실패했습니다.')
    } finally {
      setProcessingCustomerRequestId(null)
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
          <AdminWorkflowSummary reservation={reservation} />

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

          <section className="admin-operation-panel" aria-labelledby="admin-operation-title">
            <div className="admin-section-heading">
              <div>
                <p className="eyebrow">Operation</p>
                <h3 id="admin-operation-title">운영 처리</h3>
              </div>
              <span>현재 할 일: {getAdminNextTask(reservation)}</span>
            </div>

            <div className="admin-operation-grid">
              <article className="admin-operation-card">
                <div>
                  <h4>상태 변경</h4>
                  <p>상담 진행, 견적 안내, 확정, 완료 같은 예약 흐름을 관리자 기준으로 기록합니다.</p>
                </div>
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
              </article>

              <article className="admin-operation-card">
                <div>
                  <h4>이동 거리</h4>
                  <p>출발지와 도착지를 확인한 뒤 거리 기준 견적을 다시 계산할 때 사용합니다.</p>
                </div>
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
                </div>
              </article>

              <article className="admin-operation-card admin-operation-card--wide">
                <div>
                  <h4>견적 저장</h4>
                  <p>고객에게 안내할 최종 금액의 기준입니다. 쿠폰 할인이 있으면 최종 견적에 반영됩니다.</p>
                </div>
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
                  <div className="admin-estimate-summary">
                    <dt>기본 계산 합계</dt>
                    <dd>{formatPrice(reservation.baseEstimatedPrice)}</dd>
                  </div>
                  {reservation.estimatedPrice !== reservation.baseEstimatedPrice && (
                    <div>
                      <dt>관리자 조정</dt>
                      <dd>{formatAdjustment(reservation.estimatedPrice - reservation.baseEstimatedPrice)}</dd>
                    </div>
                  )}
                  {reservation.discountAmount > 0 && (
                    <div>
                      <dt>{reservation.couponName ? `${reservation.couponName} 할인` : '쿠폰 할인'}</dt>
                      <dd>-{formatPrice(reservation.discountAmount)}</dd>
                    </div>
                  )}
                  <div className="admin-estimate-total">
                    <dt>최종 견적</dt>
                    <dd>{formatPrice(reservation.finalEstimatedPrice)}</dd>
                  </div>
                </dl>
              </article>

              <article className="admin-operation-card admin-operation-card--wide">
                <div>
                  <h4>고객 요청사항과 관리자 메모</h4>
                  <p>고객 요청은 확인용이고, 관리자 메모는 고객에게 보이지 않는 내부 기록입니다.</p>
                </div>
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
              </article>
            </div>
          </section>

          <section>
            <h3>고객 요청 처리</h3>
            {reservation.customerRequests.length > 0 ? (
              <ul className="admin-history-list customer-request-review-list">
                {reservation.customerRequests.map((request) => (
                  <li key={request.id} className={request.status.toLowerCase()}>
                    <div className="admin-history-heading">
                      <strong>{request.requestTypeLabel}</strong>
                      <div>
                        <span className={`history-badge ${request.status.toLowerCase()}`}>
                          {request.statusLabel}
                        </span>
                      </div>
                    </div>
                    <span>{request.requestedAt.replace('T', ' ').slice(0, 16)}</span>
                    <p>{request.detail}</p>
                    {request.rejectionReason && <p>반려 사유: {request.rejectionReason}</p>}
                    {request.status === 'PENDING' && (
                      <div className="admin-request-actions">
                        <button
                          type="button"
                          disabled={processingCustomerRequestId === request.id}
                          onClick={() => void approveCustomerRequest(request.id)}
                        >
                          승인
                        </button>
                        <button
                          type="button"
                          disabled={processingCustomerRequestId === request.id}
                          onClick={() => void rejectCustomerRequest(request.id)}
                        >
                          반려
                        </button>
                      </div>
                    )}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="admin-empty inline">처리할 고객 요청이 없습니다.</p>
            )}
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
              <div className="admin-notification-actions">
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
                <button
                  type="button"
                  disabled={
                    isSendingSms ||
                    !reservation.notifications.some(
                      (notification) => notification.channel === 'SMS' && notification.status === 'READY',
                    )
                  }
                  onClick={sendReadySms}
                >
                  {isSendingSms ? '발송 중' : '준비 SMS 발송'}
                </button>
                <button
                  type="button"
                  disabled={
                    isResendingSms ||
                    !reservation.notifications.some(
                      (notification) => notification.channel === 'SMS' && notification.status === 'FAILED',
                    )
                  }
                  onClick={resendFailedSms}
                >
                  {isResendingSms ? '재발송 중' : '실패 SMS 재발송'}
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

function AdminWorkflowSummary({ reservation }: { reservation: AdminReservationDetailResponse }) {
  const readyNotifications = countNotifications(reservation, 'READY')
  const failedNotifications = countNotifications(reservation, 'FAILED')
  const pendingCustomerRequestCount = reservation.customerRequests.filter((request) => request.status === 'PENDING').length
  const latestCustomerAction = reservation.customerActionHistories[0]
  const taskItems = [
    {
      label: '다음 처리',
      value: getAdminNextTask(reservation),
    },
    {
      label: '고객 입력',
      value: reservation.photos.length > 0
        ? `짐 사진 ${reservation.photos.length}장 업로드됨`
        : reservation.memo
          ? '요청사항 있음, 사진 없음'
          : '요청사항과 사진 없음',
    },
    {
      label: '고객 요청',
      value: pendingCustomerRequestCount > 0
        ? `승인/반려 필요 ${pendingCustomerRequestCount}건`
        : '처리 대기 요청 없음',
    },
    {
      label: '견적 상태',
      value: reservation.estimateAccepted
        ? `고객 동의 완료 ${reservation.acceptedEstimatePrice?.toLocaleString() ?? ''}원`
        : `${reservation.finalEstimatedPrice.toLocaleString()}원, 동의 전`,
    },
    {
      label: '알림 상태',
      value: failedNotifications > 0
        ? `실패 ${failedNotifications}건 확인 필요`
        : readyNotifications > 0
          ? `발송 준비 ${readyNotifications}건`
          : '대기 중인 알림 없음',
    },
  ]

  return (
    <section className="admin-workflow-summary" aria-labelledby="admin-workflow-summary-title">
      <div className="admin-workflow-heading">
        <div>
          <p className="eyebrow">Customer Flow</p>
          <h3 id="admin-workflow-summary-title">고객 흐름 요약</h3>
        </div>
        <span>{reservation.statusLabel}</span>
      </div>
      <div className="admin-workflow-grid">
        {taskItems.map((item) => (
          <article key={item.label}>
            <span>{item.label}</span>
            <strong>{item.value}</strong>
          </article>
        ))}
      </div>
      <div className="admin-flow-path" aria-label="고객 예약 흐름">
        <span className="done">예약 접수</span>
        <span className={reservation.photos.length > 0 || reservation.memo ? 'done' : ''}>정보 확인</span>
        <span className={reservation.distanceKm !== null ? 'done' : ''}>거리/견적 점검</span>
        <span className={reservation.status === 'ESTIMATE_SENT' || reservation.estimateAccepted ? 'done' : ''}>
          견적 안내
        </span>
        <span className={reservation.estimateAccepted ? 'done' : ''}>고객 동의</span>
      </div>
      {latestCustomerAction && (
        <p className="admin-latest-action">
          최근 고객 행동: {latestCustomerAction.summary} · {latestCustomerAction.createdAt.replace('T', ' ').slice(0, 16)}
        </p>
      )}
    </section>
  )
}
