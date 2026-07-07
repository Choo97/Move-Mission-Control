import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ChangeEvent, FormEvent, ReactNode } from 'react'
import './App.css'
import landingMovingScene from './assets/landing-moving-scene.png'
import { AdminAvailabilitySettings } from './components/AdminAvailabilitySettings'
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
import { PublicReviewSection } from './components/PublicReviewSection'
import {
  acceptEstimate as acceptEstimateApi,
  cancelReservation as cancelReservationApi,
  createReservation,
  createReview,
  getCustomerGuides,
  getServicePolicy,
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
  ServiceMode,
} from './types'

type ActiveView = 'create' | 'search' | 'faq'

type LandingProps = {
  onReserveClick: () => void
  onSearchClick: () => void
  onFaqClick: () => void
  serviceMode: ServiceMode
}

const getViewFromUrl = (): ActiveView => {
  const view = new URLSearchParams(window.location.search).get('view')
  return view === 'search' || view === 'faq' ? view : 'create'
}

const isAdminRoute = () => window.location.pathname.startsWith('/admin')
const isLoginRoute = () => window.location.pathname === '/login'
const adminConsoleView = (path: string): 'reservations' | 'notifications' | 'reviews' | 'availability' | 'faqs' => {
  if (path.startsWith('/admin/notifications')) {
    return 'notifications'
  }

  if (path.startsWith('/admin/reviews')) {
    return 'reviews'
  }

  if (path.startsWith('/admin/faqs')) {
    return 'faqs'
  }

  if (path.startsWith('/admin/availability')) {
    return 'availability'
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
  const [serviceMode, setServiceMode] = useState<ServiceMode>('GENERAL')

  const today = useMemo(() => new Date().toISOString().slice(0, 10), [])
  const isNonProfitMode = serviceMode === 'NON_PROFIT'

  useEffect(() => {
    let active = true

    void getServicePolicy()
      .then((policy) => {
        if (active) {
          setServiceMode(policy.serviceMode)
        }
      })
      .catch(() => {
        if (active) {
          setServiceMode('GENERAL')
        }
      })

    return () => {
      active = false
    }
  }, [])

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

    if (
      !window.confirm(
        '예약 취소 요청을 접수합니다. 일정이나 주소 변경이 필요하다면 취소하지 않고 예약 수정 요청을 이용해 주세요. 그래도 취소 요청을 접수하시겠습니까?',
      )
    ) {
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
    const currentAdminViewLabel = adminConsoleViewLabel(currentAdminView)

    return (
      <main className="app-shell admin-app-shell">
        <AdminConsoleNav activeView={currentAdminView} />
        <div className="admin-content-shell">
          <AdminTopCommand currentViewLabel={currentAdminViewLabel} />
          <div className="admin-view-stack">
            {currentAdminView === 'notifications' && <AdminNotificationListView />}
            {currentAdminView === 'reviews' && <AdminReviewListView />}
            {currentAdminView === 'availability' && <AdminAvailabilitySettings />}
            {currentAdminView === 'faqs' && <AdminFaqListView />}
            {currentAdminView === 'reservations' && <AdminReservationListView />}
          </div>
        </div>
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
      <header className="top-bar customer-top-bar">
        <a className="brand-home" href="/" aria-label="24nalpo 홈">
          24nalpo
        </a>

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
            {isNonProfitMode ? '도움 요청' : '이사 예약'}
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
            {isNonProfitMode ? '요청 조회' : '예약 조회'}
          </button>
        </nav>

        <a className="admin-link" href="/admin/reservations">
          관리자
        </a>
      </header>

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
            serviceMode={serviceMode}
          />
        )}

        <section id="reservation-form" className={`workspace${reservation ? ' workspace-with-detail' : ''}`}>
          {activeView === 'create' ? (
          <ReservationCreateForm
            form={form}
            today={today}
            errorMessage={errorMessage}
            isSubmitting={isSubmitting}
            onSubmit={submitReservation}
            onChange={updateField}
            serviceMode={serviceMode}
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
          serviceMode={serviceMode}
        />
        </section>
        </>
      )}
      <SiteFooter serviceMode={serviceMode} />
    </main>
  )
}

type AdminConsoleView = 'reservations' | 'notifications' | 'reviews' | 'availability' | 'faqs'

const adminQuickActions = [
  { label: '처리 필요 예약', value: '/admin/reservations?attentionRequired=true' },
  { label: '거리 확인 필요', value: '/admin/reservations?needsDistance=true' },
  { label: '신규 접수 확인', value: '/admin/reservations?status=RECEIVED' },
  { label: '실패 알림 확인', value: '/admin/notifications?status=FAILED' },
  { label: '낮은 평점 리뷰', value: '/admin/reviews?rating=3' },
  { label: '운영 시간 설정', value: '/admin/availability' },
  { label: 'FAQ 추가/수정', value: '/admin/faqs' },
]

const adminConsoleViewLabel = (activeView: AdminConsoleView) => {
  if (activeView === 'notifications') {
    return '알림 이력'
  }

  if (activeView === 'reviews') {
    return '리뷰 관리'
  }

  if (activeView === 'availability') {
    return '운영 설정'
  }

  if (activeView === 'faqs') {
    return 'FAQ 관리'
  }

  return '예약 관리'
}

function AdminTopCommand({ currentViewLabel }: { currentViewLabel: string }) {
  const [searchKeyword, setSearchKeyword] = useState(() => {
    if (!window.location.pathname.startsWith('/admin')) {
      return ''
    }

    return new URLSearchParams(window.location.search).get('keyword') ?? ''
  })

  const submitAdminSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const keyword = searchKeyword.trim()
    window.location.assign(`/admin/reservations${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''}`)
  }

  const runQuickAction = (event: ChangeEvent<HTMLSelectElement>) => {
    const destination = event.target.value

    if (destination) {
      window.location.assign(destination)
    }
  }

  return (
    <header className="admin-top-command">
      <div className="admin-top-title">
        <button className="admin-menu-button" type="button" aria-label="관리자 메뉴">
          <AdminNavIcon name="menu" />
        </button>
        <strong>{currentViewLabel}</strong>
      </div>
      <div className="admin-top-tools">
        <label className="admin-quick-action">
          <span>빠른 작업</span>
          <select value="" onChange={runQuickAction} aria-label="빠른 작업">
            <option value="">빠른 작업</option>
            {adminQuickActions.map((action) => (
              <option key={action.value} value={action.value}>
                {action.label}
              </option>
            ))}
          </select>
        </label>
        <form className="admin-global-search" onSubmit={submitAdminSearch} role="search">
          <AdminNavIcon name="search" />
          <input
            value={searchKeyword}
            onChange={(event) => setSearchKeyword(event.target.value)}
            placeholder="예약번호, 고객명, 연락처 검색"
            aria-label="예약 통합 검색"
          />
          <button type="submit">검색</button>
        </form>
        <a className="admin-icon-link has-alert" href="/admin/notifications?status=FAILED" aria-label="실패 알림 확인">
          <AdminNavIcon name="notifications" />
        </a>
        <a className="admin-icon-link" href="/admin/faqs" aria-label="FAQ 관리">
          <AdminNavIcon name="faqs" />
        </a>
        <a className="admin-customer-link" href="/">
          고객 화면
        </a>
      </div>
    </header>
  )
}

function AdminConsoleNav({ activeView }: { activeView: AdminConsoleView }) {
  const links: Array<{ href: string; icon: AdminNavIconName; label: string; view: AdminConsoleView }> = [
    { href: '/admin/reservations', icon: 'reservations', label: '예약 관리', view: 'reservations' },
    { href: '/admin/notifications', icon: 'notifications', label: '알림 이력', view: 'notifications' },
    { href: '/admin/reviews', icon: 'reviews', label: '리뷰 관리', view: 'reviews' },
    { href: '/admin/availability', icon: 'availability', label: '운영 설정', view: 'availability' },
    { href: '/admin/faqs', icon: 'faqs', label: 'FAQ 관리', view: 'faqs' },
  ]

  return (
    <nav className="admin-console-nav" aria-label="관리자 메뉴">
      <a className="admin-console-brand" href="/admin/reservations" aria-label="관리자 홈">
        <span className="admin-console-brand-mark">24</span>
        <span>
          <strong>24nalpo</strong>
          <small>Admin</small>
        </span>
      </a>
      <div className="admin-console-nav-links">
        {links.map((link) => (
          <a key={link.view} className={activeView === link.view ? 'active' : ''} href={link.href}>
            <AdminNavIcon name={link.icon} />
            <span>{link.label}</span>
          </a>
        ))}
      </div>
      <div className="admin-console-support">
        <span>운영 콘솔</span>
        <strong>예약 우선순위를 먼저 확인하세요.</strong>
      </div>
    </nav>
  )
}

type AdminNavIconName = 'availability' | 'faqs' | 'menu' | 'notifications' | 'reservations' | 'reviews' | 'search'

function AdminNavIcon({ name }: { name: AdminNavIconName }) {
  const pathMap: Record<AdminNavIconName, ReactNode> = {
    availability: (
      <>
        <path d="M7 3v3" />
        <path d="M17 3v3" />
        <path d="M4 9h16" />
        <path d="M5 5h14a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1Z" />
      </>
    ),
    faqs: (
      <>
        <path d="M8.5 8a3.5 3.5 0 1 1 5.2 3.05c-.9.55-1.2 1.05-1.2 2.2" />
        <path d="M12.5 17h.01" />
        <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
      </>
    ),
    menu: (
      <>
        <path d="M4 7h16" />
        <path d="M4 12h16" />
        <path d="M4 17h16" />
      </>
    ),
    notifications: (
      <>
        <path d="M18 9a6 6 0 1 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9Z" />
        <path d="M10 21h4" />
      </>
    ),
    reservations: (
      <>
        <path d="M7 3v3" />
        <path d="M17 3v3" />
        <path d="M4 8h16" />
        <path d="M5 5h14a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1Z" />
        <path d="m9 14 2 2 4-5" />
      </>
    ),
    reviews: (
      <>
        <path d="m12 3 2.4 5 5.5.8-4 3.9.9 5.5-4.8-2.6-4.8 2.6.9-5.5-4-3.9 5.5-.8L12 3Z" />
      </>
    ),
    search: (
      <>
        <path d="M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16Z" />
        <path d="m21 21-4.3-4.3" />
      </>
    ),
  }

  return (
    <svg aria-hidden="true" focusable="false" viewBox="0 0 24 24">
      {pathMap[name]}
    </svg>
  )
}

type LandingIconName =
  | 'calendar'
  | 'check'
  | 'clipboard'
  | 'clock'
  | 'estimate'
  | 'headset'
  | 'home'
  | 'mapPin'
  | 'message'
  | 'package'
  | 'phone'
  | 'upload'

function ArrowIcon() {
  return (
    <svg aria-hidden="true" focusable="false" viewBox="0 0 20 20">
      <path d="M4 10h10" />
      <path d="m10 5 5 5-5 5" />
    </svg>
  )
}

function LandingIcon({ name }: { name: LandingIconName }) {
  const icon = (() => {
    switch (name) {
      case 'calendar':
        return (
          <>
            <rect x="4" y="5" width="16" height="15" rx="2" />
            <path d="M8 3v4M16 3v4M4 10h16M8 14h.01M12 14h.01M16 14h.01" />
          </>
        )
      case 'check':
        return <path d="m5 12 4 4L19 6" />
      case 'clipboard':
        return (
          <>
            <path d="M9 4h6l1 3H8l1-3Z" />
            <path d="M7 6H5a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-2" />
            <path d="M8 13h8M8 17h5" />
          </>
        )
      case 'clock':
        return (
          <>
            <circle cx="12" cy="12" r="8" />
            <path d="M12 7v5l3 2" />
          </>
        )
      case 'estimate':
        return (
          <>
            <path d="M6 3h9l3 3v15H6a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Z" />
            <path d="M14 3v4h4M8 12h8M8 16h5" />
          </>
        )
      case 'headset':
        return (
          <>
            <path d="M4 13v-1a8 8 0 0 1 16 0v1" />
            <path d="M4 13h3v5H5a1 1 0 0 1-1-1v-4ZM20 13h-3v5h2a1 1 0 0 0 1-1v-4Z" />
            <path d="M17 18c0 2-2 3-5 3" />
          </>
        )
      case 'home':
        return (
          <>
            <path d="m4 11 8-7 8 7" />
            <path d="M6 10v10h12V10M10 20v-6h4v6" />
          </>
        )
      case 'mapPin':
        return (
          <>
            <path d="M12 21s7-5 7-11a7 7 0 0 0-14 0c0 6 7 11 7 11Z" />
            <circle cx="12" cy="10" r="2.5" />
          </>
        )
      case 'message':
        return (
          <>
            <path d="M5 5h14a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H9l-5 4v-4H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2Z" />
            <path d="M8 10h8M8 14h5" />
          </>
        )
      case 'package':
        return (
          <>
            <path d="m12 3 8 4.5v9L12 21l-8-4.5v-9L12 3Z" />
            <path d="m4.5 7.5 7.5 4.3 7.5-4.3M12 12v9" />
          </>
        )
      case 'phone':
        return (
          <path d="M8 5 6 7c1 5 5 9 10 10l2-2-3-3-2 1c-2-1-3-2-4-4l1-2-2-2Z" />
        )
      case 'upload':
        return (
          <>
            <path d="M12 16V4" />
            <path d="m8 8 4-4 4 4" />
            <path d="M5 16v3h14v-3" />
          </>
        )
    }
  })()

  return (
    <svg aria-hidden="true" focusable="false" viewBox="0 0 24 24">
      {icon}
    </svg>
  )
}

function LandingSections({ onReserveClick, onSearchClick, onFaqClick, serviceMode }: LandingProps) {
  const isNonProfitMode = serviceMode === 'NON_PROFIT'

  return (
    <section className="landing-page" aria-label="24nalpo 서비스 소개">
      <section className="hero-section" aria-labelledby="landing-hero-title">
        <div className="hero-copy">
          <h2 id="landing-hero-title">
            간편하게 <span className="text-blue">{isNonProfitMode ? '요청하고' : '예약하고'}</span>
            <br />
            <span className="text-teal">{isNonProfitMode ? '차분하게' : '편하게'}</span> 이사하세요
          </h2>
          <p>
            {isNonProfitMode
              ? '출발지와 도착지, 이사 날짜를 입력하면 이사 도움 요청을 접수할 수 있습니다.'
              : '출발지와 도착지, 이사 날짜만 입력하면 예약 접수가 가능합니다.'}
          </p>
          <div className="hero-actions">
            <button type="button" className="submit-button primary-action" onClick={onReserveClick}>
              {isNonProfitMode ? '도움 요청하기' : '이사 예약하기'}
              <ArrowIcon />
            </button>
            <button type="button" className="submit-button secondary" onClick={onSearchClick}>
              {isNonProfitMode ? '요청 조회하기' : '예약 조회하기'}
              <ArrowIcon />
            </button>
          </div>
        </div>

        <div className="hero-showcase" aria-hidden="true">
          <div className="reservation-preview-card">
            <div className="preview-card-header">
              <strong>예약 미리보기</strong>
              <span>{isNonProfitMode ? '요청 접수' : '예약 접수'}</span>
            </div>
            <div className="route-summary">
              <div>
                <span className="route-dot start" />
                <p>출발지</p>
                <strong>서울 강남구 삼성로 123</strong>
              </div>
              <div>
                <span className="route-dot end" />
                <p>도착지</p>
                <strong>경기 성남시 분당구 판교로 242</strong>
              </div>
            </div>
            <div className="preview-meta-grid">
              <div>
                <span>이사 날짜</span>
                <strong>2026.07.18</strong>
              </div>
              <div>
                <span>요청 사항</span>
                <strong>포장 이사</strong>
              </div>
            </div>
            <div className="time-slot-strip">
              <span>08:00</span>
              <span className="selected">10:00</span>
              <span>12:00</span>
              <span>14:00</span>
              <span>16:00</span>
            </div>
          </div>

          <div className="moving-media-card">
            <div className="route-map-card">
              <span className="map-toggle active">지도</span>
              <span className="map-toggle">목록</span>
              <svg className="route-line" viewBox="0 0 100 100" preserveAspectRatio="none">
                <path className="route-line-shadow" d="M 23 48 C 39 27 62 34 78 68" />
                <path d="M 23 48 C 39 27 62 34 78 68" />
              </svg>
              <span className="map-pin start">
                <LandingIcon name="mapPin" />
              </span>
              <span className="map-pin end">
                <LandingIcon name="mapPin" />
              </span>
              <span className="map-city city-a">서울</span>
              <span className="map-city city-b">성남</span>
            </div>
            <img src={landingMovingScene} alt="" />
            <div className="photo-count">짐 사진 3 / 6</div>
          </div>
        </div>
      </section>

      <section className="workflow-section" aria-labelledby="workflow-title">
        <h2 id="workflow-title">
          {isNonProfitMode ? '24nalpo로 이사 도움 요청, 이렇게 간편합니다' : '24nalpo로 이사 예약, 이렇게 간편합니다'}
        </h2>
        <ol className="workflow-rail" aria-label={isNonProfitMode ? '도움 요청 진행 절차' : '예약 진행 절차'}>
          <li>
            <span className="workflow-icon"><LandingIcon name="clipboard" /></span>
            <div>
              <strong>1. 이사 정보 입력</strong>
              <p>출발지, 도착지, 날짜 선택</p>
            </div>
          </li>
          <li>
            <span className="workflow-icon"><LandingIcon name="calendar" /></span>
            <div>
              <strong>2. 시간대 선택</strong>
              <p>가능한 시간대 확인 후 선택</p>
            </div>
          </li>
          <li>
            <span className="workflow-icon"><LandingIcon name="upload" /></span>
            <div>
              <strong>3. 정보 및 사진 업로드</strong>
              <p>짐 정보와 사진을 업로드</p>
            </div>
          </li>
          <li>
            <span className="workflow-icon"><LandingIcon name="check" /></span>
            <div>
              <strong>{isNonProfitMode ? '4. 지원 가능 여부 확인' : '4. 견적 확인 및 예약 완료'}</strong>
              <p>{isNonProfitMode ? '관리자 확인 후 일정 조율' : '견적 확인 후 예약 완료'}</p>
            </div>
          </li>
        </ol>

        <div className="availability-band">
          <div className="calendar-preview" aria-hidden="true">
            <div className="calendar-header">
              <button type="button" tabIndex={-1} aria-label="이전 달">‹</button>
              <strong>2026년 7월</strong>
              <button type="button" tabIndex={-1} aria-label="다음 달">›</button>
            </div>
            <div className="calendar-grid">
              {['일', '월', '화', '수', '목', '금', '토', '12', '13', '14', '15', '16', '17', '18'].map((item) => (
                <span key={item} className={item === '18' ? 'selected' : ''}>
                  {item}
                </span>
              ))}
            </div>
          </div>
          <div className="time-preview" aria-hidden="true">
            <strong>2026년 7월 18일 (토)</strong>
            <div className="time-grid">
              <span>08:00 ~ 10:00</span>
              <span className="available">10:00 ~ 12:00</span>
              <span className="available">12:00 ~ 14:00</span>
              <span>14:00 ~ 16:00</span>
              <span className="available">16:00 ~ 18:00</span>
              <span>18:00 ~ 20:00</span>
            </div>
          </div>
          <div className="feature-list">
            <h3>{isNonProfitMode ? '가능한 시간에 맞춰 도움을 요청하세요' : '원하는 시간만, 빠르게 예약하세요'}</h3>
            <p>
              {isNonProfitMode
                ? '가능한 시간대만 확인하고 선택할 수 있어 도움 요청 접수를 차분하게 마칠 수 있습니다.'
                : '가능한 시간대만 확인하고 선택할 수 있어 불필요한 대기 없이 예약 접수를 마칠 수 있습니다.'}
            </p>
            <ul>
              <li><LandingIcon name="clock" />가능 시간만 선택</li>
              <li><LandingIcon name="clipboard" />{isNonProfitMode ? '요청 상태 조회' : '예약 상태 조회'}</li>
              <li><LandingIcon name="upload" />사진 업로드</li>
              <li><LandingIcon name="estimate" />{isNonProfitMode ? '지원 안내 확인' : '견적 동의'}</li>
            </ul>
          </div>
        </div>
      </section>

      <section className="status-section" aria-labelledby="status-title">
        <div className="status-copy">
          <h2 id="status-title">
            {isNonProfitMode ? '요청 후에도 진행 상황을 놓치지 않습니다' : '예약 후에도 진행 상황을 놓치지 않습니다'}
          </h2>
          <p>
            {isNonProfitMode
              ? '조회번호와 연락처로 상태를 조회하고, 필요한 자료를 바로 추가할 수 있습니다.'
              : '예약번호와 연락처로 상태를 조회하고, 필요한 자료를 바로 추가할 수 있습니다.'}
          </p>
          <ol className="status-timeline" aria-label={isNonProfitMode ? '도움 요청 상태 흐름' : '예약 상태 흐름'}>
            {(isNonProfitMode ? ['접수', '검토중', '지원안내', '확정', '완료'] : ['접수', '상담중', '견적안내', '확정', '완료']).map((label, index) => (
              <li key={label} className={index === 0 ? 'current' : ''}>
                <span>{index === 0 ? <LandingIcon name="check" /> : index + 1}</span>
                <strong>{label}</strong>
              </li>
            ))}
          </ol>
          <div className="lookup-preview" aria-hidden="true">
            <label>
              {isNonProfitMode ? '조회번호' : '예약번호'}
              <span>{isNonProfitMode ? '조회번호 입력' : '예약번호 입력'}</span>
            </label>
            <label>
              연락처
              <span>- 없이 숫자만 입력</span>
            </label>
            <button type="button" tabIndex={-1}>{isNonProfitMode ? '내 요청 조회하기' : '내 예약 조회하기'}</button>
          </div>
          <button type="button" className="text-button status-link" onClick={onSearchClick}>
            {isNonProfitMode ? '내 요청 조회하기' : '내 예약 조회하기'}
            <ArrowIcon />
          </button>
        </div>

        <div className="prep-panel">
          <div>
            <h3>이사를 위해 준비해주세요</h3>
            <ul className="prep-checklist">
              <li><LandingIcon name="check" />출발지와 도착지 주소</li>
              <li><LandingIcon name="check" />층수와 엘리베이터 여부</li>
              <li><LandingIcon name="check" />대형 가전과 파손 주의 물품</li>
              <li><LandingIcon name="check" />주차와 진입 정보</li>
            </ul>
          </div>

          <div className="upload-preview-card" aria-hidden="true">
            <strong>자료 추가하기</strong>
            <p>{isNonProfitMode ? '사진을 첨부하면 필요한 도움을 파악하는 데 도움이 됩니다.' : '사진을 첨부하면 정확한 견적에 도움이 됩니다.'}</p>
            <div className="upload-drop">
              <LandingIcon name="upload" />
              <span>사진을 드래그하거나 클릭하여 추가</span>
            </div>
            <div className="thumbnail-row">
              <span />
              <span />
              <span />
            </div>
          </div>

          <div className="estimate-preview-card" aria-hidden="true">
            <strong>{isNonProfitMode ? '지원 안내' : '견적 안내'}</strong>
            <div>
              <span>{isNonProfitMode ? '지원 가능 여부를 확인 중입니다.' : '견적이 준비되었습니다.'}</span>
              <b>{isNonProfitMode ? '확인 중' : '820,000원'}</b>
            </div>
            <button type="button" tabIndex={-1}>{isNonProfitMode ? '안내 확인하기' : '견적 확인하기'}</button>
          </div>
        </div>
      </section>

      <PublicReviewSection />

      <section className="handoff-section" aria-labelledby="handoff-title">
        <div>
          <h2 id="handoff-title">
            {isNonProfitMode ? '지금 바로 이사 도움 요청을 시작하세요' : '지금 바로 이사 예약을 시작하세요'}
          </h2>
          <p>
            {isNonProfitMode
              ? '필요한 정보만 차근차근 입력하면 도움 요청이 접수됩니다.'
              : '필요한 정보만 차근차근 입력하면 예약 접수가 완료됩니다.'}
          </p>
          <div className="hero-actions">
            <button type="button" className="submit-button primary-action" onClick={onReserveClick}>
              {isNonProfitMode ? '도움 요청하기' : '이사 예약하기'}
              <ArrowIcon />
            </button>
            <button type="button" className="submit-button secondary" onClick={onFaqClick}>
              이용 안내 보기
              <ArrowIcon />
            </button>
          </div>
        </div>
        <aside className="support-panel" aria-label="문의 안내">
          <LandingIcon name="headset" />
          <h3>궁금한 점이 있으신가요?</h3>
          <p>{isNonProfitMode ? '이사 도움 요청, 진행 상황 등 필요한 내용을 문의해 주세요.' : '이사 예약, 진행 상황 등 무엇이든 편하게 문의해 주세요.'}</p>
          <div>
            <span><LandingIcon name="phone" />고객센터 준비중</span>
            <span><LandingIcon name="message" />1:1 문의 준비중</span>
          </div>
        </aside>
      </section>
    </section>
  )
}

function SiteFooter({ serviceMode }: { serviceMode: ServiceMode }) {
  const isNonProfitMode = serviceMode === 'NON_PROFIT'

  return (
    <footer className="site-footer" aria-label="서비스 정보">
      <div className="footer-summary">
        <div>
          <strong>24nalpo</strong>
          <p>{isNonProfitMode ? '이사 도움 요청 접수 플랫폼' : '쉽고 빠른 이사 예약 접수 플랫폼'}</p>
        </div>
        <nav className="footer-links" aria-label="하단 메뉴">
          <a href="/?view=faq">FAQ</a>
          <a href="/?view=search">{isNonProfitMode ? '요청 조회' : '예약 조회'}</a>
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
