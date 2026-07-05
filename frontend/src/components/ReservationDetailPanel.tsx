import { useState } from 'react'
import type { ChangeEvent, Dispatch, FormEvent, ReactNode, SetStateAction } from 'react'
import { API_BASE_URL } from '../reservationData'
import type { CustomerGuideItem, ReservationResponse, ReviewForm, ReviewResponse, ServiceMode } from '../types'
import { StatusNotice } from './StatusNotice'
import './ReservationDetailPanel.css'

type Props = {
  activeView: 'create' | 'search'
  reservation: ReservationResponse | null
  lookupPhone: string
  customerGuides: CustomerGuideItem[]
  isLoadingCustomerGuides: boolean
  customerGuideErrorMessage: string
  actionMessage: string
  photoFiles: File[]
  reviewForm: ReviewForm
  submittedReview: ReviewResponse | null
  isNewlyCreated: boolean
  isCanceling: boolean
  isAcceptingEstimate: boolean
  isUploadingPhotos: boolean
  isSubmittingReview: boolean
  onStartEdit: () => void
  onCancelReservation: () => void
  onAcceptEstimate: () => void
  onSelectPhotoFiles: (event: ChangeEvent<HTMLInputElement>) => void
  onUploadPhotos: (event: FormEvent<HTMLFormElement>) => void
  onReviewFormChange: Dispatch<SetStateAction<ReviewForm>>
  onSubmitReview: (event: FormEvent<HTMLFormElement>) => void
  onShowSearchForm: () => void
  serviceMode: ServiceMode
}

const photoUrl = (fileUrl: string) =>
  fileUrl.startsWith('http') ? fileUrl : `${API_BASE_URL}${fileUrl}`

type DetailNavItem = {
  id: string
  label: string
}

function getCustomerActionAvailability(
  reservation: ReservationResponse,
  actionMessage: string,
  isNonProfitMode = false,
) {
  const hasPendingRequest = reservation.customerRequests.some((request) => request.status === 'PENDING')
  const canRequestEdit = reservation.editable && !hasPendingRequest
  const canRequestCancel = reservation.cancelable && !hasPendingRequest
  const canUseSupportActions = canRequestEdit || canRequestCancel
  const hasPrimaryEstimateAction = !isNonProfitMode && reservation.estimateAcceptable

  return {
    canRequestCancel,
    canRequestEdit,
    canUseSupportActions,
    hasPendingRequest,
    hasPrimaryEstimateAction,
    shouldRender: Boolean(actionMessage || hasPendingRequest || canUseSupportActions || hasPrimaryEstimateAction),
  }
}

export function ReservationDetailPanel({
  activeView,
  reservation,
  lookupPhone,
  customerGuides,
  isLoadingCustomerGuides,
  customerGuideErrorMessage,
  actionMessage,
  photoFiles,
  reviewForm,
  submittedReview,
  isNewlyCreated,
  isCanceling,
  isAcceptingEstimate,
  isUploadingPhotos,
  isSubmittingReview,
  onStartEdit,
  onCancelReservation,
  onAcceptEstimate,
  onSelectPhotoFiles,
  onUploadPhotos,
  onReviewFormChange,
  onSubmitReview,
  onShowSearchForm,
  serviceMode,
}: Props) {
  const isNonProfitMode = serviceMode === 'NON_PROFIT'
  const [openDetailSections, setOpenDetailSections] = useState<Record<string, boolean>>({})
  const actionAvailability = reservation
    ? getCustomerActionAvailability(reservation, actionMessage, isNonProfitMode)
    : null
  const detailNavItems: DetailNavItem[] = []

  if (reservation) {
    detailNavItems.push(
      { id: 'reservation-summary-section', label: '요약' },
      { id: 'reservation-progress-section', label: '진행' },
    )

    if (reservation.customerRequests.length > 0) {
      detailNavItems.push({ id: 'reservation-request-section', label: '요청' })
    }

    if (!isNonProfitMode && reservation.estimateLines.length > 0) {
      detailNavItems.push({ id: 'reservation-estimate-section', label: '견적' })
    }

    detailNavItems.push(
      { id: 'reservation-photo-section', label: '사진' },
      { id: 'reservation-review-section', label: isNonProfitMode ? '후기' : '리뷰' },
    )

    if (actionAvailability?.shouldRender) {
      detailNavItems.push({ id: 'reservation-action-section', label: '다음 행동' })
    }
  }

  const isDetailSectionOpen = (sectionId: string, defaultOpen: boolean) =>
    openDetailSections[sectionId] ?? defaultOpen

  const setDetailSectionOpen = (sectionId: string, isOpen: boolean) => {
    setOpenDetailSections((current) =>
      current[sectionId] === isOpen ? current : { ...current, [sectionId]: isOpen },
    )
  }

  const openAndScrollToDetailSection = (sectionId: string) => {
    setOpenDetailSections((current) => ({ ...current, [sectionId]: true }))
    window.setTimeout(() => {
      document.getElementById(sectionId)?.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      })
    }, 0)
  }

  return (
    <aside className="status-panel">
      <h2>{activeView === 'create' ? '접수 결과' : isNonProfitMode ? '요청 상세' : '예약 상세'}</h2>
      {reservation ? (
        <div className="result">
          {isNewlyCreated && (
            <ReservationCompleteCard
              reservation={reservation}
              lookupPhone={lookupPhone}
              onShowSearchForm={onShowSearchForm}
              isNonProfitMode={isNonProfitMode}
            />
          )}
          <ReservationDetailQuickNav items={detailNavItems} onNavigate={openAndScrollToDetailSection} />

          <DetailSection
            id="reservation-summary-section"
            title={`${isNonProfitMode ? '요청' : '예약'} #${reservation.id}`}
            description={isNonProfitMode ? '일정, 주소, 진행 상태를 먼저 확인합니다.' : '일정, 주소, 금액을 먼저 확인합니다.'}
            className="detail-section--summary"
            isOpen={isDetailSectionOpen('reservation-summary-section', true)}
            onOpenChange={(isOpen) => setDetailSectionOpen('reservation-summary-section', isOpen)}
          >
            {activeView === 'search' && (
              <ReservationLookupHeader reservation={reservation} isNonProfitMode={isNonProfitMode} />
            )}
            <ReservationSummary reservation={reservation} isNonProfitMode={isNonProfitMode} />
            <ReservationStateBadges reservation={reservation} isNonProfitMode={isNonProfitMode} />
          </DetailSection>

          <DetailSection
            id="reservation-progress-section"
            title="진행 단계"
            description="현재 예약이 어디까지 진행됐는지 확인합니다."
            className="detail-section--progress"
            isOpen={isDetailSectionOpen('reservation-progress-section', true)}
            onOpenChange={(isOpen) => setDetailSectionOpen('reservation-progress-section', isOpen)}
          >
            <ReservationProgress reservation={reservation} showHeading={false} isNonProfitMode={isNonProfitMode} />
            <CustomerStatusGuide
              guides={customerGuides}
              isLoading={isLoadingCustomerGuides}
              errorMessage={customerGuideErrorMessage}
              showHeading={false}
            />
          </DetailSection>

          {reservation.customerRequests.length > 0 && (
            <DetailSection
              id="reservation-request-section"
              title="요청 처리 현황"
              description="수정 또는 취소 요청이 어떻게 처리되고 있는지 확인합니다."
              className="detail-section--request"
              isOpen={isDetailSectionOpen('reservation-request-section', true)}
              onOpenChange={(isOpen) => setDetailSectionOpen('reservation-request-section', isOpen)}
            >
              <CustomerRequestSection reservation={reservation} showHeading={false} />
            </DetailSection>
          )}

          {!isNonProfitMode && reservation.estimateLines.length > 0 && (
            <DetailSection
              id="reservation-estimate-section"
              title="견적 내역"
              description="기본가와 추가 항목을 나눠서 확인합니다."
              className="detail-section--estimate"
              isOpen={isDetailSectionOpen('reservation-estimate-section', reservation.estimateAcceptable)}
              onOpenChange={(isOpen) => setDetailSectionOpen('reservation-estimate-section', isOpen)}
            >
              <EstimateLines reservation={reservation} showHeading={false} />
            </DetailSection>
          )}

          <DetailSection
            id="reservation-photo-section"
            title="짐 사진"
            description="짐 규모를 보여주는 사진을 확인하거나 추가합니다."
            className="detail-section--photo"
            isOpen={isDetailSectionOpen('reservation-photo-section', isNewlyCreated)}
            onOpenChange={(isOpen) => setDetailSectionOpen('reservation-photo-section', isOpen)}
          >
            <PhotoSection
              reservation={reservation}
              photoFiles={photoFiles}
              isUploadingPhotos={isUploadingPhotos}
              onSelectPhotoFiles={onSelectPhotoFiles}
              onUploadPhotos={onUploadPhotos}
              showHeading={false}
            />
          </DetailSection>

          <DetailSection
            id="reservation-review-section"
            title={isNonProfitMode ? '이용 후기' : '고객 리뷰'}
            description={isNonProfitMode ? '도움이 완료된 뒤 후기를 남길 수 있습니다.' : '이사가 완료된 뒤 리뷰를 남길 수 있습니다.'}
            className="detail-section--review"
            isOpen={isDetailSectionOpen('reservation-review-section', false)}
            onOpenChange={(isOpen) => setDetailSectionOpen('reservation-review-section', isOpen)}
          >
            <ReviewSection
              reservation={reservation}
              reviewForm={reviewForm}
              submittedReview={submittedReview}
              isSubmittingReview={isSubmittingReview}
              onReviewFormChange={onReviewFormChange}
              onSubmitReview={onSubmitReview}
              showHeading={false}
              isNonProfitMode={isNonProfitMode}
            />
          </DetailSection>

          {actionAvailability?.shouldRender && (
            <DetailSection
              id="reservation-action-section"
              title="다음 행동"
              description={isNonProfitMode ? '수정 요청이나 취소 요청을 처리합니다.' : '견적 동의, 수정 요청, 취소 요청을 처리합니다.'}
              className="detail-section--action"
              isOpen={isDetailSectionOpen('reservation-action-section', true)}
              onOpenChange={(isOpen) => setDetailSectionOpen('reservation-action-section', isOpen)}
            >
              <CustomerActions
                reservation={reservation}
                actionMessage={actionMessage}
                availability={actionAvailability}
                isCanceling={isCanceling}
                isAcceptingEstimate={isAcceptingEstimate}
                onStartEdit={onStartEdit}
                onCancelReservation={onCancelReservation}
                onAcceptEstimate={onAcceptEstimate}
                isNonProfitMode={isNonProfitMode}
              />
            </DetailSection>
          )}
        </div>
      ) : (
        <StatusNotice
          title={activeView === 'create' ? '아직 접수된 예약이 없습니다' : '아직 조회된 예약이 없습니다'}
          description={
            activeView === 'create'
              ? isNonProfitMode
                ? '도움 요청을 완료하면 조회번호와 접수 결과가 이곳에 표시됩니다.'
                : '예약 신청을 완료하면 예약번호와 접수 결과가 이곳에 표시됩니다.'
              : isNonProfitMode
                ? '조회번호와 연락처를 입력하면 요청 상태를 확인할 수 있습니다.'
                : '예약번호와 연락처를 입력하면 예약 상태와 견적 정보를 확인할 수 있습니다.'
          }
          actions={
            activeView === 'create'
              ? [
                  isNonProfitMode ? '필수 정보를 입력한 뒤 도움 요청을 제출해 주세요.' : '필수 정보를 입력한 뒤 예약을 제출해 주세요.',
                  isNonProfitMode ? '조회번호는 이후 확인에 필요하니 따로 보관해 주세요.' : '예약번호는 이후 조회에 필요하니 따로 보관해 주세요.',
                ]
              : [
                  isNonProfitMode ? '접수 완료 화면에서 받은 조회번호를 준비해 주세요.' : '예약 완료 화면에서 받은 예약번호를 준비해 주세요.',
                  '신청 때 입력한 연락처를 그대로 입력해 주세요.',
                ]
          }
        />
      )}
    </aside>
  )
}

function ReservationDetailQuickNav({
  items,
  onNavigate,
}: {
  items: DetailNavItem[]
  onNavigate: (sectionId: string) => void
}) {
  return (
    <nav className="detail-quick-nav" aria-label="예약 상세 빠른 이동">
      <span>빠른 이동</span>
      <div>
        {items.map((item) => (
          <button key={item.id} type="button" onClick={() => onNavigate(item.id)}>
            {item.label}
          </button>
        ))}
      </div>
    </nav>
  )
}

function DetailSection({
  id,
  title,
  description,
  className,
  isOpen,
  onOpenChange,
  children,
}: {
  id: string
  title: string
  description: string
  className: string
  isOpen: boolean
  onOpenChange: (isOpen: boolean) => void
  children: ReactNode
}) {
  return (
    <details
      id={id}
      className={`detail-section ${className}`}
      open={isOpen}
      onToggle={(event) => onOpenChange(event.currentTarget.open)}
    >
      <summary>
        <span>
          <strong>{title}</strong>
          <small>{description}</small>
        </span>
      </summary>
      <div className="detail-section-body">{children}</div>
    </details>
  )
}

function ReservationCompleteCard({
  reservation,
  lookupPhone,
  onShowSearchForm,
  isNonProfitMode,
}: {
  reservation: ReservationResponse
  lookupPhone: string
  onShowSearchForm: () => void
  isNonProfitMode: boolean
}) {
  const [copyMessage, setCopyMessage] = useState('')

  const copyLookupCredentials = async () => {
    try {
      await navigator.clipboard.writeText(`${isNonProfitMode ? '조회번호' : '예약번호'}: ${reservation.id}\n연락처: ${lookupPhone}`)
      setCopyMessage(`${isNonProfitMode ? '요청 조회' : '예약 조회'} 정보를 복사했습니다.`)
    } catch {
      setCopyMessage('복사가 어렵다면 예약번호와 연락처를 직접 저장해 주세요.')
    }
  }

  const moveToPhotoSection = () => {
    document.getElementById('reservation-photo-section')?.scrollIntoView({
      behavior: 'smooth',
      block: 'start',
    })
  }

  return (
    <div className="completion-card completion-receipt" role="status" aria-live="polite">
      <div className="completion-receipt-heading">
        <p className="eyebrow">{isNonProfitMode ? '도움 요청 접수 완료' : '예약 접수 완료'}</p>
        <h3>{isNonProfitMode ? '도움 요청이 정상적으로 접수되었습니다' : '예약이 정상적으로 접수되었습니다'}</h3>
        <p className="completion-lead">
          {isNonProfitMode
            ? '조회번호와 연락처로 언제든지 진행 상황을 확인할 수 있습니다.'
            : '예약번호와 연락처로 언제든지 진행 상황을 확인할 수 있습니다.'}
        </p>
      </div>

      <div className="completion-summary-grid" aria-label="예약 완료 요약">
        <div className="reservation-number-box highlight">
          <span>{isNonProfitMode ? '조회번호' : '예약번호'}</span>
          <strong>{reservation.id}</strong>
          <small>조회할 때 꼭 필요합니다</small>
        </div>
        <div className="reservation-number-box">
          <span>현재 상태</span>
          <strong>{reservation.statusLabel}</strong>
          <small>
            {reservation.moveDate} {reservation.moveTime.slice(0, 5)}
          </small>
        </div>
        <div className="reservation-number-box">
          <span>조회 연락처</span>
          <strong className="phone-value">{lookupPhone || '미입력'}</strong>
          <small>신청 때 입력한 번호입니다</small>
        </div>
      </div>

      <div className="completion-actions" aria-label={isNonProfitMode ? '도움 요청 완료 후 다음 행동' : '예약 완료 후 다음 행동'}>
        <button className="submit-button secondary" type="button" onClick={() => void copyLookupCredentials()}>
          조회 정보 복사
        </button>
        <button className="submit-button secondary" type="button" onClick={onShowSearchForm}>
          {isNonProfitMode ? '내 요청 조회하기' : '내 예약 조회하기'}
        </button>
        <button className="submit-button secondary" type="button" onClick={moveToPhotoSection}>
          짐 사진 올리기
        </button>
      </div>
      {copyMessage && <p className="copy-message">{copyMessage}</p>}

      <div className="completion-flow" aria-label="예약 이후 진행 순서">
        <article>
          <span>1</span>
          <strong>관리자 확인</strong>
          <p>입력한 일정과 주소를 확인합니다.</p>
        </article>
        <article>
          <span>2</span>
          <strong>{isNonProfitMode ? '지원 가능 여부 안내' : '상담 및 견적 안내'}</strong>
          <p>{isNonProfitMode ? '필요한 경우 연락 후 도움 가능 여부를 안내합니다.' : '필요한 경우 연락 후 견적을 안내합니다.'}</p>
        </article>
        <article>
          <span>3</span>
          <strong>{isNonProfitMode ? '일정 확정' : '견적 동의 후 확정'}</strong>
          <p>{isNonProfitMode ? '일정이 맞으면 도움 일정이 확정됩니다.' : '견적을 확인하고 동의하면 예약이 확정됩니다.'}</p>
        </article>
      </div>
    </div>
  )
}

function ReservationLookupHeader({
  reservation,
  isNonProfitMode,
}: {
  reservation: ReservationResponse
  isNonProfitMode: boolean
}) {
  return (
    <div className="lookup-result-card">
      <div>
        <span>현재 상태</span>
        <strong>{reservation.statusLabel}</strong>
      </div>
      <div>
        <span>이사 일정</span>
        <strong>
          {reservation.moveDate} {reservation.moveTime}
        </strong>
      </div>
      {!isNonProfitMode && (
        <div>
          <span>예상 금액</span>
          <strong>{reservation.finalEstimatedPrice.toLocaleString()}원</strong>
        </div>
      )}
    </div>
  )
}

function ReservationSummary({
  reservation,
  isNonProfitMode,
}: {
  reservation: ReservationResponse
  isNonProfitMode: boolean
}) {
  return (
    <dl>
      <div>
        <dt>{isNonProfitMode ? '신청자' : '고객'}</dt>
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
      {!isNonProfitMode && (
        <>
          <div>
            <dt>할인</dt>
            <dd>{reservation.discountAmount.toLocaleString()}원</dd>
          </div>
          <div>
            <dt>예상금액</dt>
            <dd>{reservation.finalEstimatedPrice.toLocaleString()}원</dd>
          </div>
          {reservation.acceptedEstimatePrice !== null && (
            <div>
              <dt>동의금액</dt>
              <dd>{reservation.acceptedEstimatePrice.toLocaleString()}원</dd>
            </div>
          )}
        </>
      )}
    </dl>
  )
}

const progressSteps = [
  { status: 'RECEIVED', label: '접수' },
  { status: 'CONSULTING', label: '상담중' },
  { status: 'ESTIMATE_SENT', label: '견적안내' },
  { status: 'CONFIRMED', label: '확정' },
  { status: 'COMPLETED', label: '완료' },
]

function ReservationProgress({
  reservation,
  showHeading = true,
  isNonProfitMode = false,
}: {
  reservation: ReservationResponse
  showHeading?: boolean
  isNonProfitMode?: boolean
}) {
  if (reservation.status === 'CANCELED') {
    return (
      <div className="progress-section canceled">
        {showHeading && <h3>진행 단계</h3>}
        <p>예약이 취소되었습니다.</p>
      </div>
    )
  }

  const currentIndex = progressSteps.findIndex((step) => step.status === reservation.status)

  return (
    <div className="progress-section">
      {showHeading && <h3>진행 단계</h3>}
      <ol className="progress-steps" aria-label={isNonProfitMode ? '도움 요청 진행 단계' : '예약 진행 단계'}>
        {progressSteps.map((step, index) => {
          const isDone = currentIndex >= 0 && index < currentIndex
          const isCurrent = currentIndex === index

          return (
            <li
              key={step.status}
              className={`${isDone ? 'done' : ''} ${isCurrent ? 'current' : ''}`.trim()}
              aria-current={isCurrent ? 'step' : undefined}
            >
              <span>{index + 1}</span>
              <strong>{isNonProfitMode && step.status === 'ESTIMATE_SENT' ? '지원안내' : step.label}</strong>
            </li>
          )
        })}
      </ol>
    </div>
  )
}

function CustomerStatusGuide({
  guides,
  isLoading,
  errorMessage,
  showHeading = true,
}: {
  guides: CustomerGuideItem[]
  isLoading: boolean
  errorMessage: string
  showHeading?: boolean
}) {
  if (isLoading) {
    return (
      <div className="status-guide muted">
        {showHeading && <h3>고객 안내</h3>}
        <p>고객 안내를 불러오는 중입니다.</p>
      </div>
    )
  }

  if (errorMessage) {
    return (
      <div className="status-guide warning">
        {showHeading && <h3>고객 안내</h3>}
        <p>{errorMessage}</p>
      </div>
    )
  }

  if (guides.length === 0) {
    return null
  }

  return (
    <div className="status-guide">
      {showHeading && <h3>고객 안내</h3>}
      <ul>
        {guides.map((guide) => (
          <li key={`${guide.title}-${guide.description}`}>
            <strong>{guide.title}</strong>
            <span>{guide.description}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}

function ReservationStateBadges({
  reservation,
  isNonProfitMode,
}: {
  reservation: ReservationResponse
  isNonProfitMode: boolean
}) {
  const hasPendingRequest = reservation.customerRequests.some((request) => request.status === 'PENDING')

  return (
    <div className="action-state">
      {hasPendingRequest && <span>요청 처리 대기</span>}
      {reservation.editable && <span>정보 변경 요청 가능</span>}
      {reservation.cancelable && !reservation.editable && <span>예약 문의 가능</span>}
      {!isNonProfitMode && reservation.estimateAcceptable && <span>견적 확인 필요</span>}
      {!isNonProfitMode && reservation.estimateAccepted && <span>견적 동의 완료</span>}
    </div>
  )
}

function CustomerRequestSection({
  reservation,
  showHeading = true,
}: {
  reservation: ReservationResponse
  showHeading?: boolean
}) {
  if (reservation.customerRequests.length === 0) {
    return null
  }

  const pendingRequestCount = reservation.customerRequests.filter((request) => request.status === 'PENDING').length

  return (
    <div className="customer-request-section">
      {showHeading && <h3>요청 처리 현황</h3>}
      {pendingRequestCount > 0 && (
        <div className="pending-request-guide">
          <strong>관리자 확인 중입니다</strong>
          <p>처리 대기 중에는 같은 예약의 추가 수정 또는 취소 요청이 제한됩니다.</p>
        </div>
      )}
      <ul>
        {reservation.customerRequests.map((request) => (
          <li key={request.id} className={request.status.toLowerCase()}>
            <strong>
              {request.requestTypeLabel} · {request.statusLabel}
            </strong>
            <span>{request.requestedAt.replace('T', ' ').slice(0, 16)}</span>
            <p>{request.detail}</p>
            {request.rejectionReason && <p>반려 사유: {request.rejectionReason}</p>}
          </li>
        ))}
      </ul>
    </div>
  )
}

function EstimateLines({
  reservation,
  showHeading = true,
}: {
  reservation: ReservationResponse
  showHeading?: boolean
}) {
  if (reservation.estimateLines.length === 0) {
    return null
  }

  return (
    <div className="estimate-lines">
      {showHeading && <h3>견적 내역</h3>}
      <dl>
        {reservation.estimateLines.map((line) => (
          <div key={line.label}>
            <dt>{line.label}</dt>
            <dd>{line.amount.toLocaleString()}원</dd>
          </div>
        ))}
      </dl>
    </div>
  )
}

function PhotoSection({
  reservation,
  photoFiles,
  isUploadingPhotos,
  onSelectPhotoFiles,
  onUploadPhotos,
  showHeading = true,
}: {
  reservation: ReservationResponse
  photoFiles: File[]
  isUploadingPhotos: boolean
  onSelectPhotoFiles: (event: ChangeEvent<HTMLInputElement>) => void
  onUploadPhotos: (event: FormEvent<HTMLFormElement>) => void
  showHeading?: boolean
}) {
  return (
    <div className="photo-section">
      {showHeading && <h3>짐 사진</h3>}
      {reservation.photos.length > 0 ? (
        <div className="photo-grid">
          {reservation.photos.map((photo) => (
            <a
              key={photo.id}
              className="photo-card"
              href={photoUrl(photo.fileUrl)}
              target="_blank"
              rel="noreferrer"
            >
              <img src={photoUrl(photo.fileUrl)} alt={photo.originalFilename} />
              <span>{photo.originalFilename}</span>
            </a>
          ))}
        </div>
      ) : (
        <StatusNotice
          title="업로드된 짐 사진이 없습니다"
          description="짐 사진을 올리면 관리자가 짐 규모를 더 정확하게 확인할 수 있습니다."
          actions={reservation.editable ? ['이미지 파일을 선택한 뒤 사진 업로드 버튼을 눌러 주세요.'] : []}
        />
      )}
      {reservation.editable && (
        <form className="photo-upload-form" onSubmit={onUploadPhotos}>
          <label>
            사진 선택
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp"
              multiple
              onChange={onSelectPhotoFiles}
            />
          </label>
          {photoFiles.length > 0 && <p className="selected-files">{photoFiles.length}장 선택됨</p>}
          <button className="submit-button secondary" type="submit" disabled={isUploadingPhotos}>
            {isUploadingPhotos ? '사진 업로드 중' : '사진 업로드'}
          </button>
        </form>
      )}
    </div>
  )
}

function ReviewSection({
  reservation,
  reviewForm,
  submittedReview,
  isSubmittingReview,
  onReviewFormChange,
  onSubmitReview,
  showHeading = true,
  isNonProfitMode = false,
}: {
  reservation: ReservationResponse
  reviewForm: ReviewForm
  submittedReview: ReviewResponse | null
  isSubmittingReview: boolean
  onReviewFormChange: Dispatch<SetStateAction<ReviewForm>>
  onSubmitReview: (event: FormEvent<HTMLFormElement>) => void
  showHeading?: boolean
  isNonProfitMode?: boolean
}) {
  if (reservation.status !== 'COMPLETED') {
    const message =
      reservation.status === 'CANCELED'
        ? isNonProfitMode ? '취소된 요청은 후기를 작성할 수 없습니다.' : '취소된 예약은 리뷰를 작성할 수 없습니다.'
        : isNonProfitMode ? '도움이 완료된 뒤 후기를 작성할 수 있습니다.' : '이사가 완료된 뒤 리뷰를 작성할 수 있습니다.'

    return (
      <div className="review-section">
        {showHeading && <h3>{isNonProfitMode ? '이용 후기' : '고객 리뷰'}</h3>}
        <p className="review-notice">{message}</p>
      </div>
    )
  }

  return (
    <div className="review-section">
      {showHeading && <h3>{isNonProfitMode ? '이용 후기' : '고객 리뷰'}</h3>}
      {submittedReview ? (
        <div className="review-complete">
          <strong>{'★'.repeat(submittedReview.rating)}</strong>
          <p>{submittedReview.content}</p>
        </div>
      ) : (
        <form className="review-form" onSubmit={onSubmitReview}>
          <label>
            평점
            <select
              value={reviewForm.rating}
              onChange={(event) =>
                onReviewFormChange((current) => ({ ...current, rating: Number(event.target.value) }))
              }
            >
              <option value={5}>5점</option>
              <option value={4}>4점</option>
              <option value={3}>3점</option>
              <option value={2}>2점</option>
              <option value={1}>1점</option>
            </select>
          </label>
          <label>
            리뷰 내용
            <textarea
              value={reviewForm.content}
              onChange={(event) => onReviewFormChange((current) => ({ ...current, content: event.target.value }))}
              placeholder={isNonProfitMode ? '도움을 받은 뒤 느낀 점을 남겨주세요.' : '서비스 이용 후 느낀 점을 남겨주세요.'}
              required
            />
          </label>
          <button className="submit-button secondary" type="submit" disabled={isSubmittingReview}>
            {isSubmittingReview ? (isNonProfitMode ? '후기 등록 중' : '리뷰 등록 중') : (isNonProfitMode ? '후기 등록' : '리뷰 등록')}
          </button>
        </form>
      )}
    </div>
  )
}

function CustomerActions({
  reservation,
  actionMessage,
  availability,
  isCanceling,
  isAcceptingEstimate,
  onStartEdit,
  onCancelReservation,
  onAcceptEstimate,
  isNonProfitMode = false,
}: {
  reservation: ReservationResponse
  actionMessage: string
  availability?: ReturnType<typeof getCustomerActionAvailability>
  isCanceling: boolean
  isAcceptingEstimate: boolean
  onStartEdit: () => void
  onCancelReservation: () => void
  onAcceptEstimate: () => void
  isNonProfitMode?: boolean
}) {
  const {
    canRequestCancel,
    canRequestEdit,
    canUseSupportActions,
    hasPendingRequest,
    hasPrimaryEstimateAction,
    shouldRender,
  } = availability ?? getCustomerActionAvailability(reservation, actionMessage)
  const title = hasPrimaryEstimateAction
    ? '견적을 확인하고 예약을 확정해 주세요'
    : isNonProfitMode ? '도움 요청 진행 상황을 확인해 주세요' : '예약 진행 상황을 확인해 주세요'
  const description = hasPrimaryEstimateAction
    ? '안내된 견적이 괜찮다면 동의 후 예약이 확정됩니다. 일정이나 주소가 바뀐 경우에는 먼저 수정 요청을 남겨 주세요.'
    : isNonProfitMode
      ? '관리자가 요청 정보를 확인한 뒤 지원 가능 여부와 일정을 안내합니다. 정보가 바뀌었을 때만 요청을 남기면 됩니다.'
      : '관리자가 예약 정보를 확인한 뒤 상담과 견적 안내를 진행합니다. 정보가 바뀌었을 때만 요청을 남기면 됩니다.'

  if (!shouldRender) {
    return null
  }

  return (
    <div className="customer-actions">
      <div className="customer-actions-heading">
        <span>다음 단계</span>
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
      {actionMessage && <p className="message info">{actionMessage}</p>}
      {hasPendingRequest && (
        <p className="message info">처리 대기 중인 고객 요청이 있어 관리자 확인 전까지 추가 수정/취소 요청은 제한됩니다.</p>
      )}

      {hasPrimaryEstimateAction && (
        <div className="estimate-confirm-card">
          <div>
            <span>안내 견적</span>
            <strong>{reservation.finalEstimatedPrice.toLocaleString()}원</strong>
            <p>견적에 동의하면 예약이 확정되고, 이후 진행 안내를 받을 수 있습니다.</p>
          </div>
          <button
            className="submit-button primary-action"
            type="button"
            disabled={isAcceptingEstimate}
            onClick={onAcceptEstimate}
          >
            {isAcceptingEstimate ? '견적 동의 처리 중' : '견적 동의하고 예약 확정'}
          </button>
        </div>
      )}

      {canUseSupportActions && (
        <div className="reservation-support-actions">
          <div className="support-actions-heading">
            <strong>{isNonProfitMode ? '요청 정보가 바뀌었나요?' : '예약 정보가 바뀌었나요?'}</strong>
            <p>날짜, 주소, 요청사항 변경은 취소보다 수정 요청을 먼저 권장합니다.</p>
          </div>
          <div className="support-action-list">
            {canRequestEdit && (
              <button className="support-action-button" type="button" onClick={onStartEdit}>
                <strong>{isNonProfitMode ? '요청 수정' : '예약 수정 요청'}</strong>
                <span>일정, 주소, 요청사항을 바꿔야 할 때</span>
              </button>
            )}
            {canRequestCancel && (
              <button
                className="support-action-button quiet-danger"
                type="button"
                disabled={isCanceling}
                onClick={onCancelReservation}
              >
                <strong>{isCanceling ? '취소 요청 중' : isNonProfitMode ? '요청 취소가 필요해요' : '예약 취소가 필요해요'}</strong>
                <span>이사를 진행하기 어려운 경우에만 선택</span>
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
