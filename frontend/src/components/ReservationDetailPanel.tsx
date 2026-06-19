import type { ChangeEvent, Dispatch, FormEvent, SetStateAction } from 'react'
import { API_BASE_URL } from '../reservationData'
import type { ReservationResponse, ReviewForm, ReviewResponse } from '../types'

type Props = {
  activeView: 'create' | 'search'
  reservation: ReservationResponse | null
  actionMessage: string
  photoFiles: File[]
  reviewForm: ReviewForm
  submittedReview: ReviewResponse | null
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
}

const photoUrl = (fileUrl: string) =>
  fileUrl.startsWith('http') ? fileUrl : `${API_BASE_URL}${fileUrl}`

export function ReservationDetailPanel({
  activeView,
  reservation,
  actionMessage,
  photoFiles,
  reviewForm,
  submittedReview,
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
}: Props) {
  return (
    <aside className="status-panel">
      <h2>{activeView === 'create' ? '접수 결과' : '예약 상세'}</h2>
      {reservation ? (
        <div className="result">
          <strong>예약 #{reservation.id}</strong>
          <ReservationSummary reservation={reservation} />
          <ReservationProgress reservation={reservation} />
          <ReservationStateBadges reservation={reservation} />
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
        <p className="empty-state">
          {activeView === 'create'
            ? '예약 신청을 완료하면 접수 결과가 여기에 표시됩니다.'
            : '예약을 조회하면 상세 정보가 여기에 표시됩니다.'}
        </p>
      )}
    </aside>
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

function ReservationStateBadges({ reservation }: { reservation: ReservationResponse }) {
  return (
    <div className="action-state">
      {reservation.editable && <span>수정 가능</span>}
      {reservation.cancelable && <span>취소 가능</span>}
      {reservation.estimateAcceptable && <span>견적 동의 가능</span>}
      {reservation.estimateAccepted && <span>견적 동의 완료</span>}
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
        <p className="empty-state">업로드된 짐 사진이 없습니다.</p>
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
    return null
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
  if (!actionMessage && !reservation.editable && !reservation.cancelable && !reservation.estimateAcceptable) {
    return null
  }

  return (
    <div className="customer-actions">
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
        {reservation.editable && (
          <button className="submit-button secondary" type="button" onClick={onStartEdit}>
            예약 수정
          </button>
        )}
        {reservation.cancelable && (
          <button className="submit-button danger" type="button" disabled={isCanceling} onClick={onCancelReservation}>
            {isCanceling ? '취소 처리 중' : '예약 취소'}
          </button>
        )}
      </div>
    </div>
  )
}
