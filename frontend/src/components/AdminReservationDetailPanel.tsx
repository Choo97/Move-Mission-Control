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
import { adminLoginHref, redirectToAdminExpiredLogin } from '../adminAuthNavigation'
import { API_BASE_URL } from '../reservationData'
import type {
  AdminNotificationHistoryResponse,
  AdminReservationDetailResponse,
  AdminReservationStatusOptionResponse,
  ReservationStatus,
} from '../types'

type Props = {
  reservationId: number
  onClose: () => void
  onReservationChanged: () => void
}

type StatusUpdateNotice = {
  previousStatusLabel: string
  nextStatusLabel: string
  nextTask: string
  readyNotificationCount: number
}

const formatPrice = (price: number | null | undefined) => (price == null ? '견적 확인 전' : `${price.toLocaleString()}원`)
const formatAdjustment = (price: number) => `${price > 0 ? '+' : ''}${price.toLocaleString()}원`
const formatEstimateInput = (price: number | null) => (price === null ? '' : String(price))
const formatDateTime = (dateTime: string) => dateTime.replace('T', ' ').slice(0, 16)
const formatBoolean = (value: boolean) => (value ? '예' : '아니오')
const adminPhotoUrl = (fileUrl: string) => (fileUrl.startsWith('http') ? fileUrl : `${API_BASE_URL}${fileUrl}`)

const statusButtonLabel = (option: AdminReservationStatusOptionResponse) => {
  if (option.status === 'CANCELED') {
    return '취소 처리'
  }

  if (option.status === 'COMPLETED') {
    return '완료 처리'
  }

  if (option.status === 'CONFIRMED') {
    return '확정 처리'
  }

  if (option.status === 'CONSULTING') {
    return '상담 중으로 변경'
  }

  if (option.status === 'ESTIMATE_SENT') {
    return '견적 안내로 변경'
  }

  return `${option.statusLabel}로 변경`
}

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

const notificationStatusPriority: Record<AdminNotificationHistoryResponse['status'], number> = {
  FAILED: 0,
  READY: 1,
  SENT: 2,
}

const sortNotificationsForAdmin = (notifications: AdminNotificationHistoryResponse[]) =>
  [...notifications].sort((left, right) => {
    const priorityDifference = notificationStatusPriority[left.status] - notificationStatusPriority[right.status]

    if (priorityDifference !== 0) {
      return priorityDifference
    }

    return right.createdAt.localeCompare(left.createdAt)
  })

const getNotificationActionLabel = (notification: AdminNotificationHistoryResponse) => {
  if (notification.status === 'FAILED') {
    return '재발송 필요'
  }

  if (notification.status === 'READY') {
    return `${notification.channelLabel} 발송 대기`
  }

  return '발송 완료'
}

const getNotificationGuide = (reservation: AdminReservationDetailResponse) => {
  const failedCount = countNotifications(reservation, 'FAILED')
  const readyCount = countNotifications(reservation, 'READY')

  if (failedCount > 0) {
    return '실패 알림을 먼저 확인하고 재발송 버튼으로 처리하세요.'
  }

  if (readyCount > 0) {
    return '발송 대기 알림이 있습니다. 이메일 또는 SMS 발송 버튼으로 고객에게 안내하세요.'
  }

  return '지금 바로 처리할 알림은 없습니다. 발송 완료 이력만 확인하면 됩니다.'
}

const countCustomerRequests = (
  reservation: AdminReservationDetailResponse,
  status: 'PENDING' | 'APPROVED' | 'REJECTED',
) => reservation.customerRequests.filter((request) => request.status === status).length

const createStatusUpdateNotice = (
  previousReservation: AdminReservationDetailResponse,
  updatedReservation: AdminReservationDetailResponse,
): StatusUpdateNotice => ({
  previousStatusLabel: previousReservation.statusLabel,
  nextStatusLabel: updatedReservation.statusLabel,
  nextTask: getAdminNextTask(updatedReservation),
  readyNotificationCount: countNotifications(updatedReservation, 'READY'),
})

export function AdminReservationDetailPanel({ reservationId, onClose, onReservationChanged }: Props) {
  const [reservation, setReservation] = useState<AdminReservationDetailResponse | null>(null)
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
  const [pendingStatusOption, setPendingStatusOption] = useState<AdminReservationStatusOptionResponse | null>(null)
  const [updatingStatus, setUpdatingStatus] = useState<ReservationStatus | null>(null)
  const [errorMessage, setErrorMessage] = useState('')
  const [actionMessage, setActionMessage] = useState('')
  const [statusUpdateNotice, setStatusUpdateNotice] = useState<StatusUpdateNotice | null>(null)

  const showAdminError = useCallback((error: unknown, fallbackMessage: string) => {
    if (error instanceof AdminAuthenticationRequiredError) {
      setIsAuthenticationRequired(true)
      setErrorMessage(getErrorMessage(error, '관리자 로그인이 필요합니다.'))
      redirectToAdminExpiredLogin()
      return
    }

    setIsAuthenticationRequired(false)
    setErrorMessage(getErrorMessage(error, fallbackMessage))
  }, [])

  const clearActionFeedback = () => {
    setActionMessage('')
    setStatusUpdateNotice(null)
  }

  useEffect(() => {
    const loadReservation = async () => {
      setIsLoading(true)
      setErrorMessage('')
      setActionMessage('')
      setStatusUpdateNotice(null)
      setReservation(null)

      try {
        const loadedReservation = await getAdminReservation(reservationId)
        setReservation(loadedReservation)
        setEstimateAmount(formatEstimateInput(loadedReservation.estimatedPrice))
        setDistanceAmount(loadedReservation.distanceKm === null ? '' : String(loadedReservation.distanceKm))
        setAdminMemo(loadedReservation.adminMemo ?? '')
        setPendingStatusOption(null)
      } catch (error) {
        showAdminError(error, '관리자 예약 상세를 불러오지 못했습니다.')
      } finally {
        setIsLoading(false)
      }
    }

    void loadReservation()
  }, [reservationId, showAdminError])

  const submitStatusUpdate = async (nextStatus: ReservationStatus) => {
    if (!reservation || nextStatus === reservation.status) {
      return
    }

    setUpdatingStatus(nextStatus)
    setErrorMessage('')
    clearActionFeedback()

    try {
      const updatedReservation = await updateAdminReservationStatus(reservation.id, nextStatus)
      setReservation(updatedReservation)
      setEstimateAmount(formatEstimateInput(updatedReservation.estimatedPrice))
      setDistanceAmount(updatedReservation.distanceKm === null ? '' : String(updatedReservation.distanceKm))
      setPendingStatusOption(null)
      setStatusUpdateNotice(createStatusUpdateNotice(reservation, updatedReservation))
      onReservationChanged()
    } catch (error) {
      showAdminError(error, '관리자 예약 상태 변경에 실패했습니다.')
    } finally {
      setUpdatingStatus(null)
    }
  }

  const submitEstimateUpdate = async () => {
    if (!reservation) {
      return
    }

    const nextEstimateAmount = Number(estimateAmount)

    if (estimateAmount.trim() === '' || Number.isNaN(nextEstimateAmount)) {
      setErrorMessage('견적 금액을 숫자로 입력해 주세요.')
      clearActionFeedback()
      return
    }

    if (nextEstimateAmount < 0) {
      setErrorMessage('견적 금액은 0원 이상이어야 합니다.')
      clearActionFeedback()
      return
    }

    setIsUpdatingEstimate(true)
    setErrorMessage('')
    clearActionFeedback()

    try {
      const updatedReservation = await updateAdminReservationEstimate(reservation.id, nextEstimateAmount)
      setReservation(updatedReservation)
      setEstimateAmount(formatEstimateInput(updatedReservation.estimatedPrice))
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
      clearActionFeedback()
      return
    }

    if (nextDistance !== null && nextDistance < 0) {
      setErrorMessage('이동 거리는 0km 이상이어야 합니다.')
      clearActionFeedback()
      return
    }

    setIsUpdatingDistance(true)
    setErrorMessage('')
    clearActionFeedback()

    try {
      const updatedReservation = await updateAdminReservationDistance(reservation.id, nextDistance)
      setReservation(updatedReservation)
      setEstimateAmount(formatEstimateInput(updatedReservation.estimatedPrice))
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
      clearActionFeedback()
      return
    }

    setIsUpdatingMemo(true)
    setErrorMessage('')
    clearActionFeedback()

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
    clearActionFeedback()

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
    clearActionFeedback()

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
    clearActionFeedback()

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
    clearActionFeedback()

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
    clearActionFeedback()

    try {
      const updatedReservation = await approveAdminCustomerRequest(requestId)
      setReservation(updatedReservation)
      setEstimateAmount(formatEstimateInput(updatedReservation.estimatedPrice))
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
      clearActionFeedback()
      return
    }

    setProcessingCustomerRequestId(requestId)
    setErrorMessage('')
    clearActionFeedback()

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

  const nextStatusOptions = reservation?.selectableStatuses.filter((option) => !option.current) ?? []
  const currentStatusDescription = reservation?.selectableStatuses.find((option) => option.current)?.description
  const notificationSummaryItems = reservation
    ? [
        {
          status: 'READY' as const,
          label: '발송 대기',
          count: countNotifications(reservation, 'READY'),
          description: '고객에게 아직 보내지 않은 안내',
        },
        {
          status: 'SENT' as const,
          label: '발송 완료',
          count: countNotifications(reservation, 'SENT'),
          description: '정상 발송 처리된 안내',
        },
        {
          status: 'FAILED' as const,
          label: '실패',
          count: countNotifications(reservation, 'FAILED'),
          description: '재발송 또는 설정 확인 필요',
        },
      ]
    : []
  const sortedNotifications = reservation ? sortNotificationsForAdmin(reservation.notifications) : []
  const hasReadyEmail = reservation?.notifications.some(
    (notification) => notification.channel === 'EMAIL' && notification.status === 'READY',
  ) ?? false
  const hasFailedEmail = reservation?.notifications.some(
    (notification) => notification.channel === 'EMAIL' && notification.status === 'FAILED',
  ) ?? false
  const hasReadySms = reservation?.notifications.some(
    (notification) => notification.channel === 'SMS' && notification.status === 'READY',
  ) ?? false
  const hasFailedSms = reservation?.notifications.some(
    (notification) => notification.channel === 'SMS' && notification.status === 'FAILED',
  ) ?? false

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
            <a href={adminLoginHref()}>관리자 로그인</a>
          </div>
        ) : (
          <p className="message error">{errorMessage}</p>
        ))}
      {statusUpdateNotice && (
        <section className="admin-status-result" aria-label="상태 변경 완료 안내" aria-live="polite">
          <div className="admin-status-result-main">
            <span>상태 변경 완료</span>
            <strong>
              {statusUpdateNotice.previousStatusLabel} → {statusUpdateNotice.nextStatusLabel}
            </strong>
            <p>다음 할 일: {statusUpdateNotice.nextTask}</p>
          </div>
          <div className="admin-status-result-meta">
            <span>준비된 알림 {statusUpdateNotice.readyNotificationCount}건</span>
            <span>상태 이력 기록 완료</span>
          </div>
        </section>
      )}
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

          <section className="admin-site-panel" aria-labelledby="admin-site-title">
            <div className="admin-section-heading">
              <div>
                <p className="eyebrow">Site Check</p>
                <h3 id="admin-site-title">현장 정보와 짐 사진</h3>
              </div>
              <span className="admin-section-count">짐 사진 {reservation.photos.length}장</span>
            </div>

            <div className="admin-site-grid">
              <article className="admin-site-card">
                <h4>출발지 현장</h4>
                <dl>
                  <div>
                    <dt>주소</dt>
                    <dd>{reservation.fromAddress}</dd>
                  </div>
                  <div>
                    <dt>층수</dt>
                    <dd>{reservation.fromFloor}층</dd>
                  </div>
                  <div>
                    <dt>엘리베이터</dt>
                    <dd>{formatBoolean(reservation.fromElevator)}</dd>
                  </div>
                  <div>
                    <dt>사다리차</dt>
                    <dd>{formatBoolean(reservation.fromLadderTruck)}</dd>
                  </div>
                </dl>
              </article>

              <article className="admin-site-card">
                <h4>도착지 현장</h4>
                <dl>
                  <div>
                    <dt>주소</dt>
                    <dd>{reservation.toAddress}</dd>
                  </div>
                  <div>
                    <dt>층수</dt>
                    <dd>{reservation.toFloor}층</dd>
                  </div>
                  <div>
                    <dt>엘리베이터</dt>
                    <dd>{formatBoolean(reservation.toElevator)}</dd>
                  </div>
                  <div>
                    <dt>사다리차</dt>
                    <dd>{formatBoolean(reservation.toLadderTruck)}</dd>
                  </div>
                </dl>
              </article>

              <article className="admin-site-card">
                <h4>짐 규모 기준</h4>
                <dl>
                  <div>
                    <dt>이사 유형</dt>
                    <dd>{reservation.moveTypeLabel}</dd>
                  </div>
                  <div>
                    <dt>고객 요청사항</dt>
                    <dd>{reservation.memo ? '입력됨' : '없음'}</dd>
                  </div>
                  <div>
                    <dt>사진 확인</dt>
                    <dd>{reservation.photos.length > 0 ? '업로드됨' : '없음'}</dd>
                  </div>
                </dl>
              </article>
            </div>

            {reservation.photos.length > 0 ? (
              <div className="admin-photo-grid">
                {reservation.photos.map((photo) => (
                  <a
                    key={photo.id}
                    className="admin-photo-card"
                    href={adminPhotoUrl(photo.fileUrl)}
                    target="_blank"
                    rel="noreferrer"
                  >
                    <img src={adminPhotoUrl(photo.fileUrl)} alt={photo.originalFilename} />
                    <span>{photo.originalFilename}</span>
                    <small>{formatDateTime(photo.uploadedAt)}</small>
                  </a>
                ))}
              </div>
            ) : (
              <p className="admin-empty inline">
                업로드된 짐 사진이 없습니다. 사진이 있으면 관리자가 짐 규모를 더 빠르게 판단할 수 있습니다.
              </p>
            )}
          </section>

          <section className="admin-operation-panel" aria-labelledby="admin-operation-title">
            <div className="admin-section-heading">
              <div>
                <p className="eyebrow">Operation</p>
                <h3 id="admin-operation-title">운영 처리</h3>
              </div>
              <span className="admin-section-count">현재 할 일: {getAdminNextTask(reservation)}</span>
            </div>

            <div className="admin-operation-grid">
              <article className="admin-operation-card">
                <div>
                  <h4>상태 변경</h4>
                  <p>상담 진행, 견적 안내, 확정, 완료 같은 예약 흐름을 관리자 기준으로 기록합니다.</p>
                </div>
                <div className="admin-status-update">
                  <div className="admin-status-current">
                    <span>현재 상태</span>
                    <strong>{reservation.statusLabel}</strong>
                    <p>{currentStatusDescription}</p>
                  </div>
                  {nextStatusOptions.length > 0 ? (
                    <div className="admin-status-actions" aria-label="변경 가능한 예약 상태">
                      {nextStatusOptions.map((option) => (
                        <button
                          key={option.status}
                          type="button"
                          disabled={updatingStatus !== null}
                          onClick={() => {
                            setErrorMessage('')
                            clearActionFeedback()
                            setPendingStatusOption(option)
                          }}
                          title={option.nextAction}
                        >
                          {updatingStatus === option.status ? '변경 중' : statusButtonLabel(option)}
                        </button>
                      ))}
                    </div>
                  ) : (
                    <p className="admin-status-help">완료 또는 취소된 예약은 추가 상태 변경이 없습니다.</p>
                  )}
                  {pendingStatusOption && (
                    <div className="admin-status-confirm" role="status">
                      <div>
                        <span>상태 변경 확인</span>
                        <strong>
                          {reservation.statusLabel} → {pendingStatusOption.statusLabel}
                        </strong>
                        <p>{pendingStatusOption.nextAction}</p>
                      </div>
                      <div className="admin-status-confirm-actions">
                        <button
                          type="button"
                          disabled={updatingStatus !== null}
                          onClick={() => setPendingStatusOption(null)}
                        >
                          취소
                        </button>
                        <button
                          type="button"
                          disabled={updatingStatus !== null}
                          onClick={() => submitStatusUpdate(pendingStatusOption.status)}
                        >
                          {updatingStatus === pendingStatusOption.status ? '변경 중' : '변경 확정'}
                        </button>
                      </div>
                    </div>
                  )}
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
                    disabled={
                      isUpdatingEstimate ||
                      (estimateAmount.trim() === ''
                        ? reservation.estimatedPrice === null
                        : Number(estimateAmount) === reservation.estimatedPrice)
                    }
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
                  {reservation.estimatedPrice !== null &&
                    reservation.baseEstimatedPrice !== null &&
                    reservation.estimatedPrice !== reservation.baseEstimatedPrice && (
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

          <section className="admin-followup-panel" aria-labelledby="admin-followup-title">
            <div className="admin-section-heading">
              <div>
                <p className="eyebrow">Customer Request</p>
                <h3 id="admin-followup-title">고객 요청 처리</h3>
              </div>
              <span className="admin-section-count">대기 {countCustomerRequests(reservation, 'PENDING')}건</span>
            </div>
            <div className="admin-history-summary">
              <span>승인 {countCustomerRequests(reservation, 'APPROVED')}건</span>
              <span>반려 {countCustomerRequests(reservation, 'REJECTED')}건</span>
            </div>
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
                    <span>{formatDateTime(request.requestedAt)}</span>
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

          <section className="admin-history-panel" aria-labelledby="admin-history-title">
            <div className="admin-section-heading">
              <div>
                <p className="eyebrow">Timeline</p>
                <h3 id="admin-history-title">운영 이력</h3>
              </div>
              <span className="admin-section-count">
                상태 {reservation.statusHistories.length}건 · 고객 행동 {reservation.customerActionHistories.length}건
              </span>
            </div>
            <div className="admin-history-columns">
              <article className="admin-history-card">
                <h4>상태 이력</h4>
                {reservation.statusHistories.length > 0 ? (
                  <ul className="admin-status-timeline" aria-label="예약 상태 변경 타임라인">
                    {reservation.statusHistories.map((history, index) => (
                      <li key={history.id} className={index === 0 ? 'latest' : undefined}>
                        <div className="admin-status-timeline-marker" aria-hidden="true">
                          <span>{index === 0 ? '최근' : reservation.statusHistories.length - index}</span>
                        </div>
                        <div className="admin-status-timeline-body">
                          <div className="admin-status-timeline-heading">
                            <strong>{history.changedStatusLabel}</strong>
                            {index === 0 && <span className="history-badge current">최근 변경</span>}
                          </div>
                          <div className="admin-status-transition">
                            <span>{history.previousStatusLabel}</span>
                            <b aria-hidden="true">→</b>
                            <span>{history.changedStatusLabel}</span>
                          </div>
                          <div className="admin-status-timeline-meta">
                            <div>
                              <span>변경 시각</span>
                              <strong>{formatDateTime(history.changedAt)}</strong>
                            </div>
                            <div>
                              <span>변경자</span>
                              <strong>{history.changedBy ?? '시스템'}</strong>
                            </div>
                          </div>
                        </div>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="admin-empty inline">상태 변경 이력이 없습니다.</p>
                )}
              </article>

              <article className="admin-history-card">
                <h4>고객 행동 이력</h4>
                {reservation.customerActionHistories.length > 0 ? (
                  <ul className="admin-history-list">
                    {reservation.customerActionHistories.map((history) => (
                      <li key={history.id}>
                        <strong>{history.summary}</strong>
                        <span>{formatDateTime(history.createdAt)}</span>
                        <p>{history.detail}</p>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="admin-empty inline">고객 행동 이력이 없습니다.</p>
                )}
              </article>

              <article className="admin-history-card admin-history-card--wide">
                <h4>관리자 감사로그</h4>
                {reservation.auditLogs.length > 0 ? (
                  <ul className="admin-history-list">
                    {reservation.auditLogs.map((auditLog) => (
                      <li key={auditLog.id}>
                        <strong>{auditLog.action}</strong>
                        <span>
                          {formatDateTime(auditLog.createdAt)} · {auditLog.createdBy}
                        </span>
                        <p>{auditLog.detail}</p>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="admin-empty inline">관리자 감사로그가 없습니다.</p>
                )}
              </article>
            </div>
          </section>

          <section className="admin-notification-panel" aria-labelledby="admin-notification-title">
            <div className="admin-section-heading">
              <div>
                <p className="eyebrow">Notification</p>
                <h3 id="admin-notification-title">알림 이력</h3>
              </div>
              <div className="admin-notification-actions">
                <button
                  type="button"
                  disabled={isSendingEmail || !hasReadyEmail}
                  onClick={sendReadyEmails}
                >
                  {isSendingEmail ? '발송 중' : '준비 이메일 발송'}
                </button>
                <button
                  type="button"
                  disabled={isResendingEmail || !hasFailedEmail}
                  onClick={resendFailedEmails}
                >
                  {isResendingEmail ? '재발송 중' : '실패 이메일 재발송'}
                </button>
                <button type="button" disabled={isSendingSms || !hasReadySms} onClick={sendReadySms}>
                  {isSendingSms ? '발송 중' : '준비 SMS 발송'}
                </button>
                <button type="button" disabled={isResendingSms || !hasFailedSms} onClick={resendFailedSms}>
                  {isResendingSms ? '재발송 중' : '실패 SMS 재발송'}
                </button>
              </div>
            </div>
            <div className="admin-notification-summary-grid">
              {notificationSummaryItems.map((item) => (
                <article key={item.status} className={`admin-notification-summary-card ${item.status.toLowerCase()}`}>
                  <span>{item.label}</span>
                  <strong>{item.count}건</strong>
                  <p>{item.description}</p>
                </article>
              ))}
            </div>
            <p className="admin-notification-guide">{getNotificationGuide(reservation)}</p>
            {reservation.notifications.length > 0 ? (
              <ul className="admin-notification-list">
                {sortedNotifications.map((notification) => (
                  <li key={notification.id} className={`admin-notification-item ${notification.status.toLowerCase()}`}>
                    <div className="admin-notification-heading">
                      <div>
                        <span>{getNotificationActionLabel(notification)}</span>
                        <strong>{notification.typeLabel}</strong>
                      </div>
                      <div className="admin-notification-badges">
                        <span className="history-badge">{notification.channelLabel}</span>
                        <span className={`history-badge ${notification.status.toLowerCase()}`}>
                          {notification.statusLabel}
                        </span>
                      </div>
                    </div>
                    <div className="admin-notification-meta-grid">
                      <div>
                        <span>수신처</span>
                        <strong>{notification.recipientContact}</strong>
                      </div>
                      <div>
                        <span>생성</span>
                        <strong>{formatDateTime(notification.createdAt)}</strong>
                      </div>
                      <div>
                        <span>발송</span>
                        <strong>{notification.sentAt ? formatDateTime(notification.sentAt) : '발송 전'}</strong>
                      </div>
                    </div>
                    <p className="admin-notification-message">{notification.message}</p>
                    {notification.failureReason && (
                      <p className="admin-notification-failure">실패 사유: {notification.failureReason}</p>
                    )}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="admin-empty inline">알림 이력이 없습니다.</p>
            )}
          </section>

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
        : `${formatPrice(reservation.finalEstimatedPrice)}, 동의 전`,
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
