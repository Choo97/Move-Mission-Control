import { useEffect, useState } from 'react'
import { getAdminReservations } from '../api/adminApi'
import { getErrorMessage } from '../api/apiError'
import { API_BASE_URL } from '../reservationData'
import { AdminReservationDetailPanel } from './AdminReservationDetailPanel'
import type {
  AdminReservationListQuery,
  AdminReservationPageResponse,
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

const initialQuery: AdminReservationListQuery = {
  status: undefined,
  keyword: '',
  needsDistance: false,
  sort: 'PRIORITY',
  page: 0,
  size: 10,
}

export function AdminReservationListView() {
  const [query, setQuery] = useState<AdminReservationListQuery>(initialQuery)
  const [reservationPage, setReservationPage] = useState<AdminReservationPageResponse | null>(null)
  const [selectedReservationId, setSelectedReservationId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    const loadReservations = async () => {
      setIsLoading(true)
      setErrorMessage('')

      try {
        setReservationPage(await getAdminReservations(query))
      } catch (error) {
        setReservationPage(null)
        setErrorMessage(getErrorMessage(error, '관리자 예약 목록을 불러오지 못했습니다.'))
      } finally {
        setIsLoading(false)
      }
    }

    void loadReservations()
  }, [query])

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
            value={query.keyword ?? ''}
            placeholder="고객명, 연락처, 주소"
            onChange={(event) => updateQuery('keyword', event.target.value)}
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

      {errorMessage && <p className="message error">{errorMessage}</p>}
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
              <p className="admin-empty">조건에 맞는 예약이 없습니다.</p>
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
            <p>목록에서 예약을 선택하면 상세 정보가 표시됩니다.</p>
          </aside>
        ) : (
          <AdminReservationDetailPanel
            reservationId={selectedReservationId}
            onClose={() => setSelectedReservationId(null)}
          />
        )}
      </div>
    </section>
  )
}
