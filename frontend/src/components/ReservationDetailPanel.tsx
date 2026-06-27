import type { ChangeEvent, Dispatch, FormEvent, SetStateAction } from 'react'
import { API_BASE_URL } from '../reservationData'
import type { CustomerGuideItem, ReservationResponse, ReviewForm, ReviewResponse } from '../types'
import { StatusNotice } from './StatusNotice'

type Props = {
  activeView: 'create' | 'search'
  reservation: ReservationResponse | null
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
}

const photoUrl = (fileUrl: string) =>
  fileUrl.startsWith('http') ? fileUrl : `${API_BASE_URL}${fileUrl}`

export function ReservationDetailPanel({
  activeView,
  reservation,
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
}: Props) {
  return (
    <aside className="status-panel">
      <h2>{activeView === 'create' ? '접수 결과' : '예약 상세'}</h2>
      {reservation ? (
        <div className="result">
          {isNewlyCreated && (
            <ReservationCompleteCard reservation={reservation} onShowSearchForm={onShowSearchForm} />
          )}
          {activeView === 'search' && <ReservationLookupHeader reservation={reservation} />}
          <strong>예약 #{reservation.id}</strong>
          <ReservationSummary reservation={reservation} />
          <ReservationProgress reservation={reservation} />
          <CustomerStatusGuide
            guides={customerGuides}
            isLoading={isLoadingCustomerGuides}
            errorMessage={customerGuideErrorMessage}
          />
          <ReservationStateBadges reservation={reservation} />
          <CustomerRequestSection reservation={reservation} />
          <EstimateLines reservation={reservation} />
          <PhotoSection
            reservation={reservation}
            photoFiles={photoFiles}
            isUploadingPhotos={isUploadingPhotos}
            onSelectPhotoFiles={onSelectPhotoFiles}
            onUploadPhotos={onUploadPhotos}
          />
          <ReviewSection
            reservation={reservation}
            reviewForm={reviewForm}
            submittedReview={submittedReview}
            isSubmittingReview={isSubmittingReview}
            onReviewFormChange={onReviewFormChange}
            onSubmitReview={onSubmitReview}
          />
          <CustomerActions
            reservation={reservation}
            actionMessage={actionMessage}
            isCanceling={isCanceling}
            isAcceptingEstimate={isAcceptingEstimate}
            onStartEdit={onStartEdit}
            onCancelReservation={onCancelReservation}
            onAcceptEstimate={onAcceptEstimate}
          />
        </div>
      ) : (
        <StatusNotice
          title={activeView === 'create' ? '아직 접수된 예약이 없습니다' : '아직 조회된 예약이 없습니다'}
          description={
            activeView === 'create'
              ? '예약 신청을 완료하면 예약번호와 접수 결과가 이곳에 표시됩니다.'
              : '예약번호와 연락처를 입력하면 예약 상태와 견적 정보를 확인할 수 있습니다.'
          }
          actions={
            activeView === 'create'
              ? ['필수 정보를 입력한 뒤 예약을 제출해 주세요.', '예약번호는 이후 조회에 필요하니 따로 보관해 주세요.']
              : ['예약 완료 화면에서 받은 예약번호를 준비해 주세요.', '신청 때 입력한 연락처를 그대로 입력해 주세요.']
          }
        />
      )}
    </aside>
  )
}

function ReservationCompleteCard({
  reservation,
  onShowSearchForm,
}: {
  reservation: ReservationResponse
  onShowSearchForm: () => void
}) {
  return (
    <div className="completion-card" role="status" aria-live="polite">
      <p className="eyebrow">Reservation Complete</p>
      <h3>예약 접수가 완료되었습니다</h3>
      <div className="reservation-number-box">
        <span>예약번호</span>
        <strong>{reservation.id}</strong>
      </div>
      <p>
        예약 조회에는 예약번호와 연락처가 필요합니다. 번호를 따로 보관해 주세요.
      </p>
      <ol>
        <li>관리자가 예약 정보를 확인합니다.</li>
        <li>상담 후 견적 안내가 진행됩니다.</li>
        <li>견적 동의 후 예약이 확정됩니다.</li>
      </ol>
      <button className="submit-button secondary" type="button" onClick={onShowSearchForm}>
        예약 조회하기
      </button>
    </div>
  )
}

function ReservationLookupHeader({ reservation }: { reservation: ReservationResponse }) {
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
      <div>
        <span>예상 금액</span>
        <strong>{reservation.finalEstimatedPrice.toLocaleString()}원</strong>
      </div>
    </div>
  )
}

function ReservationSummary({ reservation }: { reservation: ReservationResponse }) {
  return (
    <dl>
      <div>
        <dt>고객</dt>
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

function ReservationProgress({ reservation }: { reservation: ReservationResponse }) {
  if (reservation.status === 'CANCELED') {
    return (
      <div className="progress-section canceled">
        <h3>진행 단계</h3>
        <p>예약이 취소되었습니다.</p>
      </div>
    )
  }

  const currentIndex = progressSteps.findIndex((step) => step.status === reservation.status)

  return (
    <div className="progress-section">
      <h3>진행 단계</h3>
      <ol className="progress-steps" aria-label="예약 진행 단계">
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
              <strong>{step.label}</strong>
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
}: {
  guides: CustomerGuideItem[]
  isLoading: boolean
  errorMessage: string
}) {
  if (isLoading) {
    return (
      <div className="status-guide muted">
        <h3>고객 안내</h3>
        <p>고객 안내를 불러오는 중입니다.</p>
      </div>
    )
  }

  if (errorMessage) {
    return (
      <div className="status-guide warning">
        <h3>고객 안내</h3>
        <p>{errorMessage}</p>
      </div>
    )
  }

  if (guides.length === 0) {
    return null
  }

  return (
    <div className="status-guide">
      <h3>고객 안내</h3>
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

function ReservationStateBadges({ reservation }: { reservation: ReservationResponse }) {
  const hasPendingRequest = reservation.customerRequests.some((request) => request.status === 'PENDING')

  return (
    <div className="action-state">
      {hasPendingRequest && <span>요청 처리 대기</span>}
      {reservation.editable && <span>수정 가능</span>}
      {reservation.cancelable && <span>취소 가능</span>}
      {reservation.estimateAcceptable && <span>견적 동의 가능</span>}
      {reservation.estimateAccepted && <span>견적 동의 완료</span>}
    </div>
  )
}

function CustomerRequestSection({ reservation }: { reservation: ReservationResponse }) {
  if (reservation.customerRequests.length === 0) {
    return null
  }

  return (
    <div className="customer-request-section">
      <h3>요청 처리 현황</h3>
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

function EstimateLines({ reservation }: { reservation: ReservationResponse }) {
  if (reservation.estimateLines.length === 0) {
    return null
  }

  return (
    <div className="estimate-lines">
      <h3>견적 내역</h3>
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
}: {
  reservation: ReservationResponse
  photoFiles: File[]
  isUploadingPhotos: boolean
  onSelectPhotoFiles: (event: ChangeEvent<HTMLInputElement>) => void
  onUploadPhotos: (event: FormEvent<HTMLFormElement>) => void
}) {
  return (
    <div className="photo-section">
      <h3>짐 사진</h3>
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
}: {
  reservation: ReservationResponse
  reviewForm: ReviewForm
  submittedReview: ReviewResponse | null
  isSubmittingReview: boolean
  onReviewFormChange: Dispatch<SetStateAction<ReviewForm>>
  onSubmitReview: (event: FormEvent<HTMLFormElement>) => void
}) {
  if (reservation.status !== 'COMPLETED') {
    const message =
      reservation.status === 'CANCELED'
        ? '취소된 예약은 리뷰를 작성할 수 없습니다.'
        : '이사가 완료된 뒤 리뷰를 작성할 수 있습니다.'

    return (
      <div className="review-section">
        <h3>고객 리뷰</h3>
        <p className="review-notice">{message}</p>
      </div>
    )
  }

  return (
    <div className="review-section">
      <h3>고객 리뷰</h3>
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
              placeholder="서비스 이용 후 느낀 점을 남겨주세요."
              required
            />
          </label>
          <button className="submit-button secondary" type="submit" disabled={isSubmittingReview}>
            {isSubmittingReview ? '리뷰 등록 중' : '리뷰 등록'}
          </button>
        </form>
      )}
    </div>
  )
}

function CustomerActions({
  reservation,
  actionMessage,
  isCanceling,
  isAcceptingEstimate,
  onStartEdit,
  onCancelReservation,
  onAcceptEstimate,
}: {
  reservation: ReservationResponse
  actionMessage: string
  isCanceling: boolean
  isAcceptingEstimate: boolean
  onStartEdit: () => void
  onCancelReservation: () => void
  onAcceptEstimate: () => void
}) {
  const hasPendingRequest = reservation.customerRequests.some((request) => request.status === 'PENDING')
  const canRequestEdit = reservation.editable && !hasPendingRequest
  const canRequestCancel = reservation.cancelable && !hasPendingRequest

  if (!actionMessage && !canRequestEdit && !canRequestCancel && !reservation.estimateAcceptable) {
    return null
  }

  return (
    <div className="customer-actions">
      <h3>다음에 할 수 있는 일</h3>
      {actionMessage && <p className="message info">{actionMessage}</p>}
      <div className="button-row customer-action-grid">
        {reservation.estimateAcceptable && (
          <button
            className="submit-button primary-action"
            type="button"
            disabled={isAcceptingEstimate}
            onClick={onAcceptEstimate}
          >
            {isAcceptingEstimate ? '견적 동의 처리 중' : '견적 동의'}
          </button>
        )}
        {canRequestEdit && (
          <button className="submit-button secondary" type="button" onClick={onStartEdit}>
            예약 수정 요청
          </button>
        )}
        {canRequestCancel && (
          <button className="submit-button danger" type="button" disabled={isCanceling} onClick={onCancelReservation}>
            {isCanceling ? '취소 요청 중' : '예약 취소 요청'}
          </button>
        )}
      </div>
    </div>
  )
}
