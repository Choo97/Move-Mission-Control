import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import './App.css'
import { AdminFaqListView } from './components/AdminFaqListView'
import { AdminLoginView } from './components/AdminLoginView'
import { AdminNotificationListView } from './components/AdminNotificationListView'
import { AdminReviewListView } from './components/AdminReviewListView'
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

type ActiveView = 'create' | 'search' | 'faq'

type LandingProps = {
  onReserveClick: () => void
  onSearchClick: () => void
  onFaqClick: () => void
}

const getViewFromUrl = (): ActiveView => {
  const view = new URLSearchParams(window.location.search).get('view')
  return view === 'search' || view === 'faq' ? view : 'create'
}

const isAdminRoute = () => window.location.pathname.startsWith('/admin')
const isLoginRoute = () => window.location.pathname === '/login'
const adminConsoleView = (path: string): 'reservations' | 'notifications' | 'faqs' | 'reviews' => {
  if (path.startsWith('/admin/notifications')) {
    return 'notifications'
  }

  if (path.startsWith('/admin/reviews')) {
    return 'reviews'
  }

  if (path.startsWith('/admin/faqs')) {
    return 'faqs'
  }

  return 'reservations'
}
const adminRouteSearch = () => {
  const params = new URLSearchParams(window.location.search)
  params.delete('view')
  const queryString = params.toString()
  return queryString ? `?${queryString}` : ''
}

const getInitialSearchForm = (): ReservationSearchForm => ({
  ...initialSearchForm,
  reservationId: new URLSearchParams(window.location.search).get('reservationId') ?? '',
})

const formatPhoneNumber = (value: string) => {
  const digits = value.replace(/\D/g, '').slice(0, 11)

  if (digits.length <= 3) {
    return digits
  }

  if (digits.length <= 7) {
    return `${digits.slice(0, 3)}-${digits.slice(3)}`
  }

  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
}

function App() {
  const [adminRoute, setAdminRoute] = useState(isAdminRoute)
  const [loginRoute, setLoginRoute] = useState(isLoginRoute)
  const [currentPath, setCurrentPath] = useState(window.location.pathname)
  const [activeView, setActiveView] = useState<ActiveView>(getViewFromUrl)
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
  const [completedReservationId, setCompletedReservationId] = useState<number | null>(null)

  const today = useMemo(() => new Date().toISOString().slice(0, 10), [])

  useEffect(() => {
    const saveTimer = window.setTimeout(() => saveReservationDraft(form), 300)
    return () => window.clearTimeout(saveTimer)
  }, [form])

  useEffect(() => {
    const syncViewFromUrl = () => {
      const view = new URLSearchParams(window.location.search).get('view')
      if (view === 'admin') {
        window.location.replace(`/admin/reservations${adminRouteSearch()}`)
        return
      }

      setAdminRoute(isAdminRoute())
      setLoginRoute(isLoginRoute())
      setCurrentPath(window.location.pathname)
      setActiveView(getViewFromUrl())
    }

    syncViewFromUrl()
    window.addEventListener('popstate', syncViewFromUrl)
    return () => window.removeEventListener('popstate', syncViewFromUrl)
  }, [])

  const changeView = (nextView: ActiveView) => {
    setActiveView(nextView)
    setAdminRoute(false)
    setLoginRoute(false)

    const params = new URLSearchParams(window.location.search)
    if (nextView === 'create') {
      params.delete('view')
    } else {
      params.set('view', nextView)
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

  const backToCustomerHome = () => {
    setAdminRoute(false)
    setLoginRoute(false)
    setActiveView('create')
    window.history.replaceState(null, '', '/')
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
    const nextValue = key === 'phone' ? formatPhoneNumber(String(value)) : value
    setForm((current) => ({ ...current, [key]: nextValue as ReservationForm[K] }))
  }, [])

  const updateSearchField = <K extends keyof ReservationSearchForm>(
    key: K,
    value: ReservationSearchForm[K],
  ) => {
    const nextValue = key === 'phone' ? formatPhoneNumber(String(value)) : value
    setSearchForm((current) => ({ ...current, [key]: nextValue as ReservationSearchForm[K] }))
  }

  const updateEditField = <K extends keyof ReservationEditForm>(
    key: K,
    value: ReservationEditForm[K],
  ) => {
    const nextValue = key === 'phone' ? formatPhoneNumber(String(value)) : value
    setEditForm((current) => (current ? { ...current, [key]: nextValue as ReservationEditForm[K] } : current))
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
      setCompletedReservationId(createdReservation.id)
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
    setCompletedReservationId(null)
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
      setActionMessage('예약 수정 요청이 접수되었습니다. 관리자 확인 후 반영됩니다.')
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

    if (!window.confirm('예약 취소 요청을 접수하시겠습니까? 관리자 확인 후 처리됩니다.')) {
      return
    }

    setIsCanceling(true)
    setActionMessage('')

    try {
      await showReservation(await cancelReservationApi(reservation.id, searchForm.phone))
      setEditForm(null)
      setActionMessage('예약 취소 요청이 접수되었습니다. 관리자 확인 후 처리됩니다.')
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

  if (adminRoute) {
    const currentAdminView = adminConsoleView(currentPath)

    return (
      <main className="app-shell">
        <AdminConsoleNav activeView={currentAdminView} />
        {currentAdminView === 'notifications' && <AdminNotificationListView />}
        {currentAdminView === 'reviews' && <AdminReviewListView />}
        {currentAdminView === 'faqs' && <AdminFaqListView />}
        {currentAdminView === 'reservations' && <AdminReservationListView />}
      </main>
    )
  }

  if (loginRoute) {
    return (
      <main className="app-shell">
        <header className="top-bar">
          <div className="brand-mark" aria-label="24nalpo">
            <span>24</span>
          </div>
          <div className="brand-copy">
            <p className="eyebrow">Moving Reservation Platform</p>
            <h1>24nalpo</h1>
          </div>
        </header>
        <AdminLoginView onBackHome={backToCustomerHome} />
      </main>
    )
  }

  return (
    <main className="app-shell">
      <header className="top-bar">
        <div className="brand-mark" aria-label="24nalpo">
          <span>24</span>
        </div>
        <div className="brand-copy">
          <p className="eyebrow">Moving Reservation Platform</p>
          <h1>24nalpo</h1>
        </div>
        <a className="admin-link" href="/admin/reservations">
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
            setCompletedReservationId(null)
          }}
        >
          이사 예약
        </button>
        <button
          type="button"
          className={activeView === 'faq' ? 'active' : ''}
          onClick={() => {
            changeView('faq')
            setActionMessage('')
            setCompletedReservationId(null)
          }}
        >
          이용 안내
        </button>
        <button
          type="button"
          className={activeView === 'search' ? 'active' : ''}
          onClick={() => {
            changeView('search')
            setSearchErrorMessage('')
            setActionMessage('')
            setCompletedReservationId(null)
          }}
        >
          예약 조회
        </button>
      </nav>

      {activeView === 'faq' ? (
        <section className="workspace"><FaqView /></section>
      ) : (
        <>
        {activeView === 'create' && (
          <LandingSections
            onReserveClick={() => {
              document.getElementById('reservation-form')?.scrollIntoView({ behavior: 'smooth' })
            }}
            onSearchClick={() => {
              changeView('search')
              setSearchErrorMessage('')
              setActionMessage('')
            }}
            onFaqClick={() => {
              changeView('faq')
              setActionMessage('')
            }}
          />
        )}

        <section id="reservation-form" className="workspace">
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
            currentMoveDate={reservation.moveDate}
            currentMoveTime={reservation.moveTime}
            isUpdating={isUpdating}
            onSubmit={submitReservationUpdate}
            onChange={updateEditField}
            onCancel={() => setEditForm(null)}
          />
        )}

        <ReservationDetailPanel
          activeView={activeView}
          reservation={reservation}
          lookupPhone={searchForm.phone}
          customerGuides={customerGuides}
          isLoadingCustomerGuides={isLoadingCustomerGuides}
          customerGuideErrorMessage={customerGuideErrorMessage}
          actionMessage={actionMessage}
          photoFiles={photoFiles}
          reviewForm={reviewForm}
          submittedReview={submittedReview}
          isNewlyCreated={reservation !== null && reservation.id === completedReservationId}
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
          onShowSearchForm={() => {
            changeView('search')
            setSearchErrorMessage('')
            setActionMessage('')
            window.setTimeout(() => {
              document.getElementById('reservation-form')?.scrollIntoView({ behavior: 'smooth' })
            }, 0)
          }}
        />
        </section>
        </>
      )}
      <SiteFooter />
    </main>
  )
}

function AdminConsoleNav({ activeView }: { activeView: 'reservations' | 'notifications' | 'faqs' | 'reviews' }) {
  return (
    <nav className="admin-console-nav" aria-label="관리자 메뉴">
      <a className={activeView === 'reservations' ? 'active' : ''} href="/admin/reservations">
        예약 관리
      </a>
      <a className={activeView === 'notifications' ? 'active' : ''} href="/admin/notifications">
        알림 이력
      </a>
      <a className={activeView === 'reviews' ? 'active' : ''} href="/admin/reviews">
        리뷰 관리
      </a>
      <a className={activeView === 'faqs' ? 'active' : ''} href="/admin/faqs">
        FAQ 관리
      </a>
    </nav>
  )
}

function LandingSections({ onReserveClick, onSearchClick, onFaqClick }: LandingProps) {
  return (
    <section className="landing-page" aria-label="24nalpo 서비스 소개">
      <div className="hero-section">
        <div className="hero-copy">
          <p className="eyebrow">Public Service Style Moving Platform</p>
          <h2>간편하게 예약하고 편하게 이사하세요</h2>
          <p>
            출발지와 도착지, 이사 날짜만 입력하면 예약 접수가 가능합니다.
            가능한 시간만 선택할 수 있어 처음 신청하는 고객도 빠르게 진행할 수 있습니다.
          </p>
          <div className="hero-actions">
            <button type="button" className="submit-button primary-action" onClick={onReserveClick}>
              이사 예약하기
            </button>
            <button type="button" className="submit-button secondary" onClick={onSearchClick}>
              예약 조회하기
            </button>
          </div>
        </div>
        <div className="moving-visual" aria-hidden="true">
          <div className="visual-card visual-card-map">
            <span>START</span>
            <strong>ROUTE</strong>
            <span>HOME</span>
          </div>
          <div className="truck-body">
            <div className="truck-cargo">
              <span />
              <span />
              <span />
            </div>
            <div className="truck-cab" />
            <div className="truck-wheel first" />
            <div className="truck-wheel second" />
          </div>
        </div>
      </div>

      <div className="feature-grid" aria-label="서비스 특징">
        <article>
          <span>01</span>
          <h3>빠른 예약 접수</h3>
          <p>온라인으로 간편하게 이사 예약을 신청할 수 있습니다.</p>
        </article>
        <article>
          <span>02</span>
          <h3>예약 가능 시간 확인</h3>
          <p>운영 시간과 휴무일을 기준으로 가능한 시간만 선택합니다.</p>
        </article>
        <article>
          <span>03</span>
          <h3>예약 상태 조회</h3>
          <p>예약번호와 연락처 인증으로 진행 상태를 확인합니다.</p>
        </article>
        <article>
          <span>04</span>
          <h3>사진 업로드</h3>
          <p>이삿짐 사진을 등록해 더 정확한 상담을 받을 수 있습니다.</p>
        </article>
      </div>

      <div className="process-section">
        <div>
          <p className="eyebrow">Reservation Process</p>
          <h2>예약 진행 절차</h2>
        </div>
        <ol className="process-timeline">
          <li><span>01</span><strong>예약 정보 입력</strong></li>
          <li><span>02</span><strong>가능 시간 선택</strong></li>
          <li><span>03</span><strong>예약 접수 완료</strong></li>
          <li><span>04</span><strong>관리자 확인</strong></li>
          <li><span>05</span><strong>견적 확인 및 진행</strong></li>
        </ol>
      </div>

      <div className="notice-section">
        <article>
          <p className="eyebrow">Guide</p>
          <h3>예약 전 확인사항</h3>
          <p>출발지와 도착지 주소, 층수, 엘리베이터 여부를 미리 확인하면 접수가 빨라집니다.</p>
        </article>
        <article>
          <p className="eyebrow">Checklist</p>
          <h3>이사 준비 체크리스트</h3>
          <p>깨지기 쉬운 짐, 대형 가전, 주차 정보는 요청사항에 남겨 주세요.</p>
        </article>
        <article>
          <p className="eyebrow">Support</p>
          <h3>자주 묻는 질문</h3>
          <p>예약 조회, 사진 업로드, 견적 확인 방법은 FAQ에서 확인할 수 있습니다.</p>
          <button type="button" className="text-button" onClick={onFaqClick}>FAQ 보기</button>
        </article>
      </div>
    </section>
  )
}

function SiteFooter() {
  return (
    <footer className="site-footer" aria-label="서비스 정보">
      <div className="footer-summary">
        <div>
          <strong>24nalpo</strong>
          <p>쉽고 빠른 이사 예약 접수 플랫폼</p>
        </div>
        <nav className="footer-links" aria-label="하단 메뉴">
          <a href="/?view=faq">FAQ</a>
          <a href="/?view=search">예약 조회</a>
          <span>개인정보처리방침 준비중</span>
          <span>이용약관 준비중</span>
        </nav>
      </div>
      <p className="footer-note">
        회사명 24nalpo · 사업자 정보 및 고객센터 연락처는 운영 정보 확정 후 입력합니다.
      </p>
    </footer>
  )
}

export default App
