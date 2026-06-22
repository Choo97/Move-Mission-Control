import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import './App.css'
import { AdminReservationListView } from './components/AdminReservationListView'
import { ReservationCreateForm } from './components/ReservationCreateForm'
import { ReservationDetailPanel } from './components/ReservationDetailPanel'
import { ReservationEditFormView } from './components/ReservationEditFormView'
import { ReservationSearchFormView } from './components/ReservationSearchFormView'
import { FaqView } from './components/FaqView'
import {
  acceptEstimate as acceptEstimateApi,
  cancelReservation as cancelReservationApi,
  createReservation,
  createReview,
  getCustomerGuides,
  searchReservation as searchReservationApi,
  updateReservation,
  uploadReservationPhotos,
} from './api/customerApi'
import { getErrorMessage } from './api/apiError'
import {
  API_BASE_URL,
  initialForm,
  initialReviewForm,
  initialSearchForm,
  toEditForm,
} from './reservationData'
import { clearReservationDraft, loadReservationDraft, saveReservationDraft } from './reservationDraft'
import type {
  ReservationEditForm,
  ReservationForm,
  ReservationResponse,
  ReservationSearchForm,
  CustomerGuideItem,
  ReviewResponse,
} from './types'

type ActiveView = 'create' | 'search' | 'faq' | 'admin'

const adminQueryKeys = [
  'status',
  'keyword',
  'sort',
  'page',
  'size',
  'needsDistance',
  'reservationId',
]

const getInitialView = (): ActiveView => {
  const view = new URLSearchParams(window.location.search).get('view')
  return view === 'search' || view === 'faq' || view === 'admin' ? view : 'create'
}

const getInitialSearchForm = (): ReservationSearchForm => ({
  ...initialSearchForm,
  reservationId: new URLSearchParams(window.location.search).get('reservationId') ?? '',
})

function App() {
  const [activeView, setActiveView] = useState<ActiveView>(getInitialView)
  const [form, setForm] = useState<ReservationForm>(loadReservationDraft)
  const [searchForm, setSearchForm] = useState<ReservationSearchForm>(getInitialSearchForm)
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
  const [customerGuides, setCustomerGuides] = useState<CustomerGuideItem[]>([])
  const [isLoadingCustomerGuides, setIsLoadingCustomerGuides] = useState(false)
  const [customerGuideErrorMessage, setCustomerGuideErrorMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [searchErrorMessage, setSearchErrorMessage] = useState('')
  const [actionMessage, setActionMessage] = useState('')
  const [reservation, setReservation] = useState<ReservationResponse | null>(null)

  const today = useMemo(() => new Date().toISOString().slice(0, 10), [])

  useEffect(() => {
    const saveTimer = window.setTimeout(() => saveReservationDraft(form), 300)
    return () => window.clearTimeout(saveTimer)
  }, [form])

  const changeView = (nextView: ActiveView) => {
    setActiveView(nextView)

    const params = new URLSearchParams(window.location.search)
    if (nextView === 'create') {
      params.delete('view')
    } else {
      params.set('view', nextView)
    }

    if (nextView !== 'admin') {
      adminQueryKeys.forEach((key) => params.delete(key))
    }

    const queryString = params.toString()
    window.history.replaceState(
      null,
      '',
      `${window.location.pathname}${queryString ? `?${queryString}` : ''}`,
    )
  }

  const resetReservationContext = () => {
    setReservation(null)
    setEditForm(null)
    setPhotoFiles([])
    setSubmittedReview(null)
    setReviewForm(initialReviewForm)
    setCustomerGuides([])
    setIsLoadingCustomerGuides(false)
    setCustomerGuideErrorMessage('')
  }

  const showReservation = async (nextReservation: ReservationResponse) => {
    setReservation(nextReservation)
    setCustomerGuides([])
    setCustomerGuideErrorMessage('')
    setIsLoadingCustomerGuides(true)

    try {
      setCustomerGuides(await getCustomerGuides(nextReservation.status))
    } catch (error) {
      setCustomerGuideErrorMessage(getErrorMessage(error, '고객 안내를 불러오지 못했습니다.'))
    } finally {
      setIsLoadingCustomerGuides(false)
    }
  }

  const updateField = useCallback(<K extends keyof ReservationForm>(key: K, value: ReservationForm[K]) => {
    setForm((current) => ({ ...current, [key]: value }))
  }, [])

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
      const createdReservation = await createReservation(form)
      await showReservation(createdReservation)
      setSearchForm({
        reservationId: String(createdReservation.id),
        phone: submittedPhone,
      })
      clearReservationDraft()
      setForm(initialForm)
      changeView('search')
    } catch (error) {
      setErrorMessage(getErrorMessage(error, '예약 신청에 실패했습니다.'))
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
      await showReservation(await searchReservationApi(searchForm))
      setPhotoFiles([])
    } catch (error) {
      setSearchErrorMessage(getErrorMessage(error, '예약 조회에 실패했습니다.'))
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
      const updatedReservation = await updateReservation(reservation.id, editForm)
      await showReservation(updatedReservation)
      setSearchForm({
        reservationId: String(updatedReservation.id),
        phone: editForm.phone,
      })
      setEditForm(null)
      setActionMessage('예약 정보가 수정되었습니다.')
    } catch (error) {
      setActionMessage(getErrorMessage(error, '예약 수정에 실패했습니다.'))
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
      await showReservation(await cancelReservationApi(reservation.id, searchForm.phone))
      setEditForm(null)
      setActionMessage('예약이 취소되었습니다.')
    } catch (error) {
      setActionMessage(getErrorMessage(error, '예약 취소에 실패했습니다.'))
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
      await showReservation(await acceptEstimateApi(reservation.id, searchForm.phone))
      setEditForm(null)
      setActionMessage('견적 동의가 완료되었습니다. 예약이 확정되었습니다.')
    } catch (error) {
      setActionMessage(getErrorMessage(error, '견적 동의에 실패했습니다.'))
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

    setIsUploadingPhotos(true)
    setActionMessage('')

    try {
      const uploadedPhotos = await uploadReservationPhotos(reservation.id, searchForm.phone, photoFiles)
      setReservation((current) =>
        current ? { ...current, photos: [...current.photos, ...uploadedPhotos] } : current,
      )
      setPhotoFiles([])
      setActionMessage(`${uploadedPhotos.length}장의 짐 사진이 업로드되었습니다.`)
    } catch (error) {
      setActionMessage(getErrorMessage(error, '짐 사진 업로드에 실패했습니다.'))
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
      const createdReview = await createReview(reservation.id, searchForm.phone, reviewForm)
      setSubmittedReview(createdReview)
      setReviewForm(initialReviewForm)
      setActionMessage('리뷰가 등록되었습니다.')
    } catch (error) {
      setActionMessage(getErrorMessage(error, '리뷰 작성에 실패했습니다.'))
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
            changeView('create')
            setErrorMessage('')
            setActionMessage('')
          }}
        >
          예약 신청
        </button>
        <button
          type="button"
          className={activeView === 'faq' ? 'active' : ''}
          onClick={() => {
            changeView('faq')
            setActionMessage('')
          }}
        >
          자주 묻는 질문
        </button>
        <button
          type="button"
          className={activeView === 'search' ? 'active' : ''}
          onClick={() => {
            changeView('search')
            setSearchErrorMessage('')
            setActionMessage('')
          }}
        >
          예약 조회
        </button>
        <button
          type="button"
          className={activeView === 'admin' ? 'active' : ''}
          onClick={() => {
            changeView('admin')
            setActionMessage('')
          }}
        >
          관리자 목록
        </button>
      </nav>

      {activeView === 'admin' ? (
        <AdminReservationListView />
      ) : activeView === 'faq' ? (
        <section className="workspace"><FaqView /></section>
      ) : (
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
          customerGuides={customerGuides}
          isLoadingCustomerGuides={isLoadingCustomerGuides}
          customerGuideErrorMessage={customerGuideErrorMessage}
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
      )}
    </main>
  )
}

export default App
