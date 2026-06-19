import { useMemo, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import './App.css'
import { ReservationCreateForm } from './components/ReservationCreateForm'
import { ReservationDetailPanel } from './components/ReservationDetailPanel'
import { ReservationEditFormView } from './components/ReservationEditFormView'
import { ReservationSearchFormView } from './components/ReservationSearchFormView'
import {
  API_BASE_URL,
  initialForm,
  initialReviewForm,
  initialSearchForm,
  toEditForm,
} from './reservationData'
import type {
  ApiErrorResponse,
  ReservationEditForm,
  ReservationForm,
  ReservationPhotoResponse,
  ReservationResponse,
  ReservationSearchForm,
  ReviewResponse,
} from './types'

function App() {
  const [activeView, setActiveView] = useState<'create' | 'search'>('create')
  const [form, setForm] = useState<ReservationForm>(initialForm)
  const [searchForm, setSearchForm] = useState<ReservationSearchForm>(initialSearchForm)
  const [editForm, setEditForm] = useState<ReservationEditForm | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isSearching, setIsSearching] = useState(false)
  const [isUpdating, setIsUpdating] = useState(false)
  const [isCanceling, setIsCanceling] = useState(false)
  const [isAcceptingEstimate, setIsAcceptingEstimate] = useState(false)
  const [isUploadingPhotos, setIsUploadingPhotos] = useState(false)
  const [isSubmittingReview, setIsSubmittingReview] = useState(false)
  const [photoFiles, setPhotoFiles] = useState<File[]>([])
  const [reviewForm, setReviewForm] = useState(initialReviewForm)
  const [submittedReview, setSubmittedReview] = useState<ReviewResponse | null>(null)
  const [errorMessage, setErrorMessage] = useState('')
  const [searchErrorMessage, setSearchErrorMessage] = useState('')
  const [actionMessage, setActionMessage] = useState('')
  const [reservation, setReservation] = useState<ReservationResponse | null>(null)

  const today = useMemo(() => new Date().toISOString().slice(0, 10), [])

  const resetReservationContext = () => {
    setReservation(null)
    setEditForm(null)
    setPhotoFiles([])
    setSubmittedReview(null)
    setReviewForm(initialReviewForm)
  }

  const updateField = <K extends keyof ReservationForm>(key: K, value: ReservationForm[K]) => {
    setForm((current) => ({ ...current, [key]: value }))
  }

  const updateSearchField = <K extends keyof ReservationSearchForm>(
    key: K,
    value: ReservationSearchForm[K],
  ) => {
    setSearchForm((current) => ({ ...current, [key]: value }))
  }

  const updateEditField = <K extends keyof ReservationEditForm>(
    key: K,
    value: ReservationEditForm[K],
  ) => {
    setEditForm((current) => (current ? { ...current, [key]: value } : current))
  }

  const submitReservation = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSubmitting(true)
    setErrorMessage('')
    setActionMessage('')
    resetReservationContext()
    const submittedPhone = form.phone

    try {
      const response = await fetch(`${API_BASE_URL}/api/reservations`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })
      const data = await response.json()

      if (!response.ok) {
        const error = data as ApiErrorResponse
        throw new Error(error.message || '예약 신청에 실패했습니다.')
      }

      const createdReservation = data as ReservationResponse
      setReservation(createdReservation)
      setSearchForm({
        reservationId: String(createdReservation.id),
        phone: submittedPhone,
      })
      setForm(initialForm)
      setActiveView('search')
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '예약 신청에 실패했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const searchReservation = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSearching(true)
    setSearchErrorMessage('')
    setActionMessage('')
    resetReservationContext()

    try {
      const response = await fetch(`${API_BASE_URL}/api/reservations/search`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          reservationId: Number(searchForm.reservationId),
          phone: searchForm.phone,
        }),
      })
      const data = await response.json()

      if (!response.ok) {
        const error = data as ApiErrorResponse
        throw new Error(error.message || '예약 조회에 실패했습니다.')
      }

      setReservation(data as ReservationResponse)
      setPhotoFiles([])
    } catch (error) {
      setSearchErrorMessage(error instanceof Error ? error.message : '예약 조회에 실패했습니다.')
    } finally {
      setIsSearching(false)
    }
  }

  const startEdit = () => {
    if (!reservation) {
      return
    }

    setActionMessage('')
    setEditForm(toEditForm(reservation, searchForm.phone))
  }

  const submitReservationUpdate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!reservation || !editForm) {
      return
    }

    setIsUpdating(true)
    setActionMessage('')

    try {
      const response = await fetch(`${API_BASE_URL}/api/reservations/${reservation.id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(editForm),
      })
      const data = await response.json()

      if (!response.ok) {
        const error = data as ApiErrorResponse
        throw new Error(error.message || '예약 수정에 실패했습니다.')
      }

      const updatedReservation = data as ReservationResponse
      setReservation(updatedReservation)
      setSearchForm({
        reservationId: String(updatedReservation.id),
        phone: editForm.phone,
      })
      setEditForm(null)
      setActionMessage('예약 정보가 수정되었습니다.')
    } catch (error) {
      setActionMessage(error instanceof Error ? error.message : '예약 수정에 실패했습니다.')
    } finally {
      setIsUpdating(false)
    }
  }

  const cancelReservation = async () => {
    if (!reservation || !searchForm.phone) {
      setActionMessage('예약 조회에 사용한 연락처가 필요합니다.')
      return
    }

    if (!window.confirm('예약을 취소하시겠습니까?')) {
      return
    }

    setIsCanceling(true)
    setActionMessage('')

    try {
      const response = await fetch(`${API_BASE_URL}/api/reservations/${reservation.id}/cancel`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone: searchForm.phone }),
      })
      const data = await response.json()

      if (!response.ok) {
        const error = data as ApiErrorResponse
        throw new Error(error.message || '예약 취소에 실패했습니다.')
      }

      setReservation(data as ReservationResponse)
      setEditForm(null)
      setActionMessage('예약이 취소되었습니다.')
    } catch (error) {
      setActionMessage(error instanceof Error ? error.message : '예약 취소에 실패했습니다.')
    } finally {
      setIsCanceling(false)
    }
  }

  const acceptEstimate = async () => {
    if (!reservation || !searchForm.phone) {
      setActionMessage('예약 조회에 사용한 연락처가 필요합니다.')
      return
    }

    const confirmed = window.confirm(
      `${reservation.finalEstimatedPrice.toLocaleString()}원 견적에 동의하시겠습니까?`,
    )

    if (!confirmed) {
      return
    }

    setIsAcceptingEstimate(true)
    setActionMessage('')

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/reservations/${reservation.id}/estimate/accept`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ phone: searchForm.phone }),
        },
      )
      const data = await response.json()

      if (!response.ok) {
        const error = data as ApiErrorResponse
        throw new Error(error.message || '견적 동의에 실패했습니다.')
      }

      setReservation(data as ReservationResponse)
      setEditForm(null)
      setActionMessage('견적 동의가 완료되었습니다. 예약이 확정되었습니다.')
    } catch (error) {
      setActionMessage(error instanceof Error ? error.message : '견적 동의에 실패했습니다.')
    } finally {
      setIsAcceptingEstimate(false)
    }
  }

  const selectPhotoFiles = (event: ChangeEvent<HTMLInputElement>) => {
    setPhotoFiles(Array.from(event.target.files ?? []))
  }

  const uploadPhotos = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!reservation || !searchForm.phone) {
      setActionMessage('예약 조회에 사용한 연락처가 필요합니다.')
      return
    }

    if (photoFiles.length === 0) {
      setActionMessage('업로드할 짐 사진을 선택해 주세요.')
      return
    }

    const formData = new FormData()
    formData.append('phone', searchForm.phone)
    photoFiles.forEach((file) => formData.append('photos', file))

    setIsUploadingPhotos(true)
    setActionMessage('')

    try {
      const response = await fetch(`${API_BASE_URL}/api/reservations/${reservation.id}/photos`, {
        method: 'POST',
        body: formData,
      })
      const data = await response.json()

      if (!response.ok) {
        const error = data as ApiErrorResponse
        throw new Error(error.message || '짐 사진 업로드에 실패했습니다.')
      }

      const uploadedPhotos = data as ReservationPhotoResponse[]
      setReservation((current) =>
        current ? { ...current, photos: [...current.photos, ...uploadedPhotos] } : current,
      )
      setPhotoFiles([])
      setActionMessage(`${uploadedPhotos.length}장의 짐 사진이 업로드되었습니다.`)
    } catch (error) {
      setActionMessage(error instanceof Error ? error.message : '짐 사진 업로드에 실패했습니다.')
    } finally {
      setIsUploadingPhotos(false)
    }
  }

  const submitReview = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!reservation || !searchForm.phone) {
      setActionMessage('예약 조회에 사용한 연락처가 필요합니다.')
      return
    }

    setIsSubmittingReview(true)
    setActionMessage('')

    try {
      const response = await fetch(`${API_BASE_URL}/api/reviews`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          reservationId: reservation.id,
          phone: searchForm.phone,
          rating: reviewForm.rating,
          content: reviewForm.content,
        }),
      })
      const data = await response.json()

      if (!response.ok) {
        const error = data as ApiErrorResponse
        throw new Error(error.message || '리뷰 작성에 실패했습니다.')
      }

      const createdReview = data as ReviewResponse
      setSubmittedReview(createdReview)
      setReviewForm(initialReviewForm)
      setActionMessage('리뷰가 등록되었습니다.')
    } catch (error) {
      setActionMessage(error instanceof Error ? error.message : '리뷰 작성에 실패했습니다.')
    } finally {
      setIsSubmittingReview(false)
    }
  }

  return (
    <main className="app-shell">
      <header className="top-bar">
        <div>
          <p className="eyebrow">Move Mission Control</p>
          <h1>고객 이사 예약</h1>
        </div>
        <a className="admin-link" href={`${API_BASE_URL}/admin/reservations`}>
          관리자
        </a>
      </header>

      <nav className="view-tabs" aria-label="고객 예약 메뉴">
        <button
          type="button"
          className={activeView === 'create' ? 'active' : ''}
          onClick={() => {
            setActiveView('create')
            setErrorMessage('')
            setActionMessage('')
          }}
        >
          예약 신청
        </button>
        <button
          type="button"
          className={activeView === 'search' ? 'active' : ''}
          onClick={() => {
            setActiveView('search')
            setSearchErrorMessage('')
            setActionMessage('')
          }}
        >
          예약 조회
        </button>
      </nav>

      <section className="workspace">
        {activeView === 'create' ? (
          <ReservationCreateForm
            form={form}
            today={today}
            errorMessage={errorMessage}
            isSubmitting={isSubmitting}
            onSubmit={submitReservation}
            onChange={updateField}
          />
        ) : (
          <ReservationSearchFormView
            form={searchForm}
            errorMessage={searchErrorMessage}
            isSearching={isSearching}
            onSubmit={searchReservation}
            onChange={updateSearchField}
          />
        )}

        {editForm && reservation && (
          <ReservationEditFormView
            form={editForm}
            today={today}
            isUpdating={isUpdating}
            onSubmit={submitReservationUpdate}
            onChange={updateEditField}
            onCancel={() => setEditForm(null)}
          />
        )}

        <ReservationDetailPanel
          activeView={activeView}
          reservation={reservation}
          actionMessage={actionMessage}
          photoFiles={photoFiles}
          reviewForm={reviewForm}
          submittedReview={submittedReview}
          isCanceling={isCanceling}
          isAcceptingEstimate={isAcceptingEstimate}
          isUploadingPhotos={isUploadingPhotos}
          isSubmittingReview={isSubmittingReview}
          onStartEdit={startEdit}
          onCancelReservation={cancelReservation}
          onAcceptEstimate={acceptEstimate}
          onSelectPhotoFiles={selectPhotoFiles}
          onUploadPhotos={uploadPhotos}
          onReviewFormChange={setReviewForm}
          onSubmitReview={submitReview}
        />
      </section>
    </main>
  )
}

export default App
