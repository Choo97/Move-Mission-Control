import { useEffect, useState } from 'react'
import {
  AdminAuthenticationRequiredError,
  getAdminReservationConflictAttempts,
  getAdminReservations,
} from '../api/adminApi'
import { getErrorMessage } from '../api/apiError'
import { API_BASE_URL } from '../reservationData'
import { AdminReservationDetailPanel } from './AdminReservationDetailPanel'
import { AdminAvailabilitySettings } from './AdminAvailabilitySettings'
import { StatusNotice } from './StatusNotice'
import type {
  AdminDashboardTaskSummary,
  AdminReservationListQuery,
  AdminReservationPageResponse,
  AdminReservationConflictAttemptResponse,
  AdminReservationSort,
  ReservationStatus,
} from '../types'

const statusOptions: Array<{ value: ReservationStatus; label: string }> = [
  { value: 'RECEIVED', label: '접수' },
  { value: 'CONSULTING', label: '상담중' },
  { value: 'ESTIMATE_SENT', label: '견적안내' },
  { value: 'CONFIRMED', label: '확정' },
  { value: 'COMPLETED', label: '완료' },
  { value: 'CANCELED', label: '취소' },
]

const sortOptions: Array<{ value: AdminReservationSort; label: string }> = [
  { value: 'PRIORITY', label: '처리 우선순위' },
  { value: 'MOVE_DATE', label: '이사일 빠른순' },
  { value: 'CREATED_DESC', label: '최근 접수순' },
]

const pageSizeOptions = [10, 20, 50]

const readInitialAdminState = () => {
  const params = new URLSearchParams(window.location.search)
  const statusValue = params.get('status')
  const sortValue = params.get('sort')
  const pageValue = Number(params.get('page'))
  const sizeValue = Number(params.get('size'))
  const reservationIdValue = Number(params.get('reservationId'))

  const query: AdminReservationListQuery = {
    status: statusOptions.some(({ value }) => value === statusValue)
      ? (statusValue as ReservationStatus)
      : undefined,
    keyword: params.get('keyword') ?? '',
    needsDistance: params.get('needsDistance') === 'true',
    sort: sortOptions.some(({ value }) => value === sortValue)
      ? (sortValue as AdminReservationSort)
      : 'PRIORITY',
    page: Number.isInteger(pageValue) && pageValue >= 0 ? pageValue : 0,
    size: pageSizeOptions.includes(sizeValue) ? sizeValue : 10,
  }

  return {
    query,
    selectedReservationId:
      Number.isInteger(reservationIdValue) && reservationIdValue > 0
        ? reservationIdValue
        : null,
  }
}

const initialAdminState = readInitialAdminState()

export function AdminReservationListView() {
  const [query, setQuery] = useState<AdminReservationListQuery>(initialAdminState.query)
  const [keywordInput, setKeywordInput] = useState(initialAdminState.query.keyword ?? '')
  const [reservationPage, setReservationPage] = useState<AdminReservationPageResponse | null>(null)
  const [conflictAttempts, setConflictAttempts] = useState<AdminReservationConflictAttemptResponse[]>([])
  const [selectedReservationId, setSelectedReservationId] = useState<number | null>(
    initialAdminState.selectedReservationId,
  )
  const [refreshVersion, setRefreshVersion] = useState(0)
  const [isLoading, setIsLoading] = useState(false)
  const [isAuthenticationRequired, setIsAuthenticationRequired] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    const loadReservations = async () => {
      setIsLoading(true)
      setIsAuthenticationRequired(false)
      setErrorMessage('')

      try {
        const [nextReservationPage, nextConflictAttempts] = await Promise.all([
          getAdminReservations(query),
          getAdminReservationConflictAttempts(),
        ])
        setReservationPage(nextReservationPage)
        setConflictAttempts(nextConflictAttempts)
      } catch (error) {
        setReservationPage(null)
        setConflictAttempts([])
        setIsAuthenticationRequired(error instanceof AdminAuthenticationRequiredError)
        setErrorMessage(getErrorMessage(error, '관리자 예약 목록을 불러오지 못했습니다.'))
      } finally {
        setIsLoading(false)
      }
    }

    void loadReservations()
  }, [query, refreshVersion])

  useEffect(() => {
    const debounceTimer = window.setTimeout(() => {
      setQuery((current) => {
        if ((current.keyword ?? '') === keywordInput) {
          return current
        }

        return { ...current, keyword: keywordInput, page: 0 }
      })
    }, 400)

    return () => window.clearTimeout(debounceTimer)
  }, [keywordInput])

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    params.set('view', 'admin')

    const setOrDelete = (key: string, value: string | number | undefined, keep: boolean) => {
      if (keep) {
        params.set(key, String(value))
      } else {
        params.delete(key)
      }
    }

    setOrDelete('status', query.status, Boolean(query.status))
    setOrDelete('keyword', query.keyword, Boolean(query.keyword))
    setOrDelete('sort', query.sort, query.sort !== 'PRIORITY')
    setOrDelete('page', query.page, query.page !== 0)
    setOrDelete('size', query.size, query.size !== 10)
    setOrDelete('needsDistance', 'true', Boolean(query.needsDistance))
    setOrDelete('reservationId', selectedReservationId ?? undefined, selectedReservationId !== null)

    const queryString = params.toString()
    window.history.replaceState(null, '', `${window.location.pathname}?${queryString}`)
  }, [query, selectedReservationId])

  const updateQuery = <K extends keyof AdminReservationListQuery>(
    key: K,
    value: AdminReservationListQuery[K],
  ) => {
    setQuery((current) => ({ ...current, [key]: value, page: 0 }))
  }

  const movePage = (nextPage: number) => {
    setQuery((current) => ({ ...current, page: nextPage }))
  }

  return (
    <section className="admin-reservation-view">
      <div className="admin-heading">
        <div>
          <p className="eyebrow">Admin Preview</p>
          <h2>관리자 예약 목록</h2>
        </div>
        <a className="admin-text-link" href={`${API_BASE_URL}/admin/reservations`}>
          기존 관리자 화면
        </a>
      </div>

      <AdminAvailabilitySettings />

      {reservationPage?.taskSummary && <AdminTaskSummaryCards summary={reservationPage.taskSummary} />}

      {conflictAttempts.length > 0 && (
        <section className="admin-conflict-attempts" aria-labelledby="conflict-attempts-title">
          <div>
            <p className="eyebrow">Schedule Conflict</p>
            <h3 id="conflict-attempts-title">중복 시간 예약 시도</h3>
            <p>예약은 생성되지 않았으며, 고객에게 다른 시간을 선택하도록 안내했습니다.</p>
          </div>
          <ul>
            {conflictAttempts.map((attempt) => (
              <li key={attempt.id}>
                <strong>{attempt.customerName}</strong>
                <span>{attempt.phone}</span>
                <span>
                  희망 {attempt.moveDate} {attempt.moveTime.slice(0, 5)}
                </span>
                <time dateTime={attempt.attemptedAt}>
                  시도 {attempt.attemptedAt.replace('T', ' ').slice(0, 16)}
                </time>
              </li>
            ))}
          </ul>
        </section>
      )}

      <div className="admin-filters" aria-label="관리자 예약 목록 필터">
        <label>
          상태
          <select
            value={query.status ?? ''}
            onChange={(event) =>
              updateQuery('status', event.target.value === '' ? undefined : (event.target.value as ReservationStatus))
            }
          >
            <option value="">전체</option>
            {statusOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
        <label>
          검색어
          <input
            value={keywordInput}
            placeholder="고객명, 연락처, 주소"
            onChange={(event) => setKeywordInput(event.target.value)}
          />
        </label>
        <label>
          정렬
          <select
            value={query.sort ?? 'PRIORITY'}
            onChange={(event) => updateQuery('sort', event.target.value as AdminReservationSort)}
          >
            {sortOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
        <label>
          페이지 크기
          <select value={query.size ?? 10} onChange={(event) => updateQuery('size', Number(event.target.value))}>
            {pageSizeOptions.map((size) => (
              <option key={size} value={size}>
                {size}개
              </option>
            ))}
          </select>
        </label>
        <label className="inline-check">
          <input
            type="checkbox"
            checked={Boolean(query.needsDistance)}
            onChange={(event) => updateQuery('needsDistance', event.target.checked)}
          />
          거리 확인 필요
        </label>
      </div>

      {errorMessage &&
        (isAuthenticationRequired ? (
          <div className="admin-auth-notice">
            <strong>{errorMessage}</strong>
            <a href={`${API_BASE_URL}/login`}>관리자 로그인</a>
          </div>
        ) : (
          <p className="message error">{errorMessage}</p>
        ))}
      {isLoading && <p className="admin-loading">예약 목록을 불러오는 중입니다.</p>}

      <div className="admin-master-detail">
        <div>
          <div className="admin-table-wrap">
            <table className="admin-reservation-table">
              <thead>
                <tr>
                  <th>예약</th>
                  <th>고객</th>
                  <th>이사일</th>
                  <th>경로</th>
                  <th>상태</th>
                  <th>견적</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                {reservationPage?.content.map((reservation) => (
                  <tr key={reservation.id} className={selectedReservationId === reservation.id ? 'selected' : ''}>
                    <td>#{reservation.id}</td>
                    <td>
                      <strong>{reservation.customerName}</strong>
                      <span>{reservation.phone}</span>
                    </td>
                    <td>
                      <strong>{reservation.moveDate}</strong>
                      <span>{reservation.moveTime.slice(0, 5)}</span>
                    </td>
                    <td>
                      <strong>{reservation.fromAddress}</strong>
                      <span>{reservation.toAddress}</span>
                    </td>
                    <td>
                      <span className="admin-status">{reservation.statusLabel}</span>
                      <span>{reservation.moveTypeLabel}</span>
                    </td>
                    <td>
                      <strong>{reservation.finalEstimatedPrice.toLocaleString()}원</strong>
                      <span>{reservation.distanceKm === null ? '거리 확인 전' : `${reservation.distanceKm}km`}</span>
                    </td>
                    <td>
                      <button
                        className="admin-row-button"
                        type="button"
                        onClick={() => setSelectedReservationId(reservation.id)}
                      >
                        상세
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {!isLoading && reservationPage?.content.length === 0 && (
              <StatusNotice
                title="조건에 맞는 예약이 없습니다"
                description="현재 필터와 검색어에 해당하는 예약이 없습니다."
                actions={[
                  '상태 필터를 전체로 바꿔 보세요.',
                  '검색어를 지우거나 거리 확인 필요 조건을 해제해 보세요.',
                ]}
                className="admin-empty-state"
              />
            )}
          </div>

          {reservationPage && (
            <div className="admin-pagination">
              <span>
                총 {reservationPage.totalElements.toLocaleString()}건 · {reservationPage.pageNumber + 1}/
                {Math.max(reservationPage.totalPages, 1)}페이지
              </span>
              <div>
                <button
                  type="button"
                  disabled={reservationPage.first || isLoading}
                  onClick={() => movePage(Math.max((query.page ?? 0) - 1, 0))}
                >
                  이전
                </button>
                <button
                  type="button"
                  disabled={reservationPage.last || isLoading}
                  onClick={() => movePage((query.page ?? 0) + 1)}
                >
                  다음
                </button>
              </div>
            </div>
          )}
        </div>

        {selectedReservationId === null ? (
          <aside className="admin-detail-panel placeholder">
            <h2>예약 상세</h2>
            <StatusNotice
              title="선택된 예약이 없습니다"
              description="목록에서 예약을 선택하면 고객 정보, 견적, 알림, 작업 이력을 확인할 수 있습니다."
              actions={['먼저 처리 우선순위가 높은 예약부터 상세 버튼을 눌러 확인해 주세요.']}
            />
          </aside>
        ) : (
          <AdminReservationDetailPanel
            reservationId={selectedReservationId}
            onClose={() => setSelectedReservationId(null)}
            onReservationChanged={() => setRefreshVersion((current) => current + 1)}
          />
        )}
      </div>
    </section>
  )
}

function AdminTaskSummaryCards({ summary }: { summary: AdminDashboardTaskSummary }) {
  const totalCount =
    summary.receivedCount +
    summary.consultingCount +
    summary.estimateAcceptancePendingCount +
    summary.distancePendingCount +
    summary.failedEmailCount
  const taskCards = [
    {
      label: '접수 대기',
      value: summary.receivedCount,
      description: '상담 시작이 필요한 예약',
    },
    {
      label: '상담 진행',
      value: summary.consultingCount,
      description: '상담 메모와 견적 확인 필요',
    },
    {
      label: '고객 동의 대기',
      value: summary.estimateAcceptancePendingCount,
      description: '견적 안내 후 동의 전 예약',
    },
    {
      label: '거리 확인 필요',
      value: summary.distancePendingCount,
      description: '거리 입력 후 견적 점검 필요',
    },
    {
      label: '이메일 실패',
      value: summary.failedEmailCount,
      description: '발송 실패 이력 확인 필요',
    },
  ]

  return (
    <section className="admin-task-summary" aria-labelledby="admin-task-summary-title">
      <div className="admin-task-summary-heading">
        <div>
          <p className="eyebrow">Today Focus</p>
          <h3 id="admin-task-summary-title">처리 우선순위 요약</h3>
        </div>
        <span>확인 필요 {totalCount.toLocaleString()}건</span>
      </div>
      <div className="admin-task-card-grid">
        {taskCards.map((card) => (
          <article key={card.label}>
            <span>{card.label}</span>
            <strong>{card.value.toLocaleString()}</strong>
            <p>{card.description}</p>
          </article>
        ))}
      </div>
    </section>
  )
}
