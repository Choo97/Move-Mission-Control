import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import {
  AdminAuthenticationRequiredError,
  getAdminReviews,
  getAdminSession,
  logoutAdmin,
  updateAdminReviewPublished,
  updateAdminReviewReply,
} from '../api/adminApi'
import { getErrorMessage } from '../api/apiError'
import { adminLoginHref, redirectToAdminExpiredLogin } from '../adminAuthNavigation'
import { StatusNotice } from './StatusNotice'
import type { AdminReviewResponse, AdminSessionResponse } from '../types'

type AdminReviewQuery = {
  rating?: number
  published?: boolean
  keyword?: string
}

const ratingOptions = [5, 4, 3, 2, 1]

const readInitialReviewQuery = (): AdminReviewQuery => {
  const params = new URLSearchParams(window.location.search)
  const rating = Number(params.get('rating'))
  const published = params.get('published')

  return {
    rating: ratingOptions.includes(rating) ? rating : undefined,
    published: published === 'true' ? true : published === 'false' ? false : undefined,
    keyword: params.get('keyword') ?? '',
  }
}

const formatDateTime = (dateTime: string) => dateTime.replace('T', ' ').slice(0, 16)

const formatMoveSchedule = (moveDate: string, moveTime: string) =>
  `${moveDate} ${moveTime.slice(0, 5)}`

const matchesKeyword = (review: AdminReviewResponse, keyword: string) => {
  if (!keyword) {
    return true
  }

  const normalizedKeyword = keyword.toLowerCase()
  return [
    String(review.reservationId),
    review.customerName,
    review.phone,
    review.email ?? '',
    review.content,
    review.adminReply ?? '',
  ].some((value) => value.toLowerCase().includes(normalizedKeyword))
}

const buildReplyForms = (reviews: AdminReviewResponse[]) =>
  Object.fromEntries(reviews.map((review) => [review.id, review.adminReply ?? '']))

function RatingStars({ rating }: { rating: number }) {
  return (
    <span className="admin-review-stars" aria-label={`${rating}점`}>
      {'★'.repeat(rating)}
      {'☆'.repeat(5 - rating)}
    </span>
  )
}

export function AdminReviewListView() {
  const [query, setQuery] = useState<AdminReviewQuery>(readInitialReviewQuery)
  const [keywordInput, setKeywordInput] = useState(() => readInitialReviewQuery().keyword ?? '')
  const [reviews, setReviews] = useState<AdminReviewResponse[]>([])
  const [replyForms, setReplyForms] = useState<Record<number, string>>({})
  const [adminSession, setAdminSession] = useState<AdminSessionResponse | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const [processingReviewId, setProcessingReviewId] = useState<number | null>(null)
  const [isAuthenticationRequired, setIsAuthenticationRequired] = useState(false)
  const [message, setMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')

  const filteredReviews = useMemo(
    () =>
      reviews.filter((review) =>
        (query.rating === undefined || review.rating === query.rating) &&
        (query.published === undefined || review.published === query.published) &&
        matchesKeyword(review, query.keyword?.trim() ?? ''),
      ),
    [query, reviews],
  )

  const summary = useMemo(() => {
    const totalRating = reviews.reduce((sum, review) => sum + review.rating, 0)
    const publishedCount = reviews.filter((review) => review.published).length
    const hiddenCount = reviews.filter((review) => !review.published).length
    const lowRatingCount = reviews.filter((review) => review.rating <= 3).length

    return {
      totalCount: reviews.length,
      filteredCount: filteredReviews.length,
      publishedCount,
      hiddenCount,
      averageRating: reviews.length === 0 ? '0.0' : (totalRating / reviews.length).toFixed(1),
      lowRatingCount,
    }
  }, [filteredReviews.length, reviews])

  const ratingDistribution = useMemo(
    () =>
      ratingOptions.map((rating) => ({
        rating,
        count: reviews.filter((review) => review.rating === rating).length,
      })),
    [reviews],
  )

  useEffect(() => {
    const loadReviews = async () => {
      setIsLoading(true)
      setMessage('')
      setErrorMessage('')
      setIsAuthenticationRequired(false)

      try {
        const [nextAdminSession, nextReviews] = await Promise.all([getAdminSession(), getAdminReviews()])
        setAdminSession(nextAdminSession)
        setReviews(nextReviews)
        setReplyForms(buildReplyForms(nextReviews))
      } catch (error) {
        setAdminSession(null)
        setReviews([])
        setReplyForms({})
        if (error instanceof AdminAuthenticationRequiredError) {
          setIsAuthenticationRequired(true)
          setErrorMessage(getErrorMessage(error, '관리자 로그인이 필요합니다.'))
          redirectToAdminExpiredLogin()
          return
        }

        setIsAuthenticationRequired(false)
        setErrorMessage(getErrorMessage(error, '관리자 리뷰 목록을 불러오지 못했습니다.'))
      } finally {
        setIsLoading(false)
      }
    }

    void loadReviews()
  }, [])

  useEffect(() => {
    const params = new URLSearchParams()
    if (query.rating !== undefined) {
      params.set('rating', String(query.rating))
    }
    if (query.published !== undefined) {
      params.set('published', String(query.published))
    }
    if (query.keyword) {
      params.set('keyword', query.keyword)
    }

    const queryString = params.toString()
    window.history.replaceState(null, '', `/admin/reviews${queryString ? `?${queryString}` : ''}`)
  }, [query])

  const submitFilter = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setQuery((current) => ({ ...current, keyword: keywordInput.trim() }))
  }

  const resetFilters = () => {
    setKeywordInput('')
    setQuery({})
  }

  const updateReviewInList = (updatedReview: AdminReviewResponse, syncReplyForm = true) => {
    setReviews((current) =>
      current.map((review) => (review.id === updatedReview.id ? updatedReview : review)),
    )
    if (syncReplyForm) {
      setReplyForms((current) => ({ ...current, [updatedReview.id]: updatedReview.adminReply ?? '' }))
    }
  }

  const togglePublished = async (review: AdminReviewResponse) => {
    setProcessingReviewId(review.id)
    setMessage('')
    setErrorMessage('')

    try {
      const updatedReview = await updateAdminReviewPublished(review.id, !review.published)
      updateReviewInList(updatedReview, false)
      setMessage(updatedReview.published ? '리뷰를 공개했습니다.' : '리뷰를 숨김 처리했습니다.')
    } catch (error) {
      if (error instanceof AdminAuthenticationRequiredError) {
        redirectToAdminExpiredLogin()
        return
      }

      setErrorMessage(getErrorMessage(error, '리뷰 공개 상태 변경에 실패했습니다.'))
    } finally {
      setProcessingReviewId(null)
    }
  }

  const submitReply = async (review: AdminReviewResponse) => {
    const reply = replyForms[review.id] ?? ''
    setProcessingReviewId(review.id)
    setMessage('')
    setErrorMessage('')

    try {
      const updatedReview = await updateAdminReviewReply(review.id, reply)
      updateReviewInList(updatedReview)
      setMessage(reply.trim() ? '리뷰 답변을 저장했습니다.' : '리뷰 답변을 삭제했습니다.')
    } catch (error) {
      if (error instanceof AdminAuthenticationRequiredError) {
        redirectToAdminExpiredLogin()
        return
      }

      setErrorMessage(getErrorMessage(error, '리뷰 답변 저장에 실패했습니다.'))
    } finally {
      setProcessingReviewId(null)
    }
  }

  const submitLogout = async () => {
    setIsLoggingOut(true)
    setErrorMessage('')

    try {
      await logoutAdmin()
      window.location.assign('/login?logout')
    } catch (error) {
      if (error instanceof AdminAuthenticationRequiredError) {
        redirectToAdminExpiredLogin()
        return
      }

      setErrorMessage(getErrorMessage(error, '관리자 로그아웃에 실패했습니다.'))
      setIsLoggingOut(false)
    }
  }

  return (
    <section className="admin-review-view">
      <div className="admin-heading">
        <div>
          <p className="eyebrow">Customer Voice</p>
          <h2>리뷰 관리</h2>
          <p>완료 예약에서 작성된 고객 리뷰와 평점을 확인하고 공개 여부를 관리합니다.</p>
        </div>
      </div>

      {!isAuthenticationRequired && adminSession && (
        <div className="admin-session-status">
          <span>로그인 관리자</span>
          <strong>{adminSession.username}</strong>
          <button type="button" onClick={submitLogout} disabled={isLoggingOut}>
            {isLoggingOut ? '로그아웃 중' : '로그아웃'}
          </button>
        </div>
      )}

      {message && <p className="message">{message}</p>}
      {errorMessage &&
        (isAuthenticationRequired ? (
          <div className="admin-auth-notice">
            <div>
              <strong>{errorMessage}</strong>
              <p>React 관리자 화면은 백엔드 관리자 로그인 세션을 사용합니다. 먼저 로그인한 뒤 이 화면을 새로고침해 주세요.</p>
            </div>
            <div className="admin-auth-actions">
              <a href={adminLoginHref()}>관리자 로그인</a>
            </div>
          </div>
        ) : (
          <p className="message error">{errorMessage}</p>
        ))}

      {!isAuthenticationRequired && (
        <>
          <div className="admin-review-summary-row" aria-label="리뷰 요약">
            <article>
              <span>전체 리뷰</span>
              <strong>{summary.totalCount.toLocaleString()}</strong>
              <p>등록된 고객 리뷰</p>
            </article>
            <article>
              <span>현재 조건</span>
              <strong>{summary.filteredCount.toLocaleString()}</strong>
              <p>필터 적용 결과</p>
            </article>
            <article>
              <span>공개</span>
              <strong>{summary.publishedCount.toLocaleString()}</strong>
              <p>고객 노출 가능</p>
            </article>
            <article>
              <span>숨김</span>
              <strong>{summary.hiddenCount.toLocaleString()}</strong>
              <p>관리자만 확인</p>
            </article>
            <article>
              <span>평균 평점</span>
              <strong>{summary.averageRating}</strong>
              <p>5점 만점 기준</p>
            </article>
            <article>
              <span>확인 필요</span>
              <strong>{summary.lowRatingCount.toLocaleString()}</strong>
              <p>3점 이하 피드백</p>
            </article>
          </div>

          <form className="admin-review-filters" onSubmit={submitFilter} aria-label="리뷰 필터">
            <label>
              평점
              <select
                value={query.rating ?? ''}
                onChange={(event) =>
                  setQuery((current) => ({
                    ...current,
                    rating: event.target.value === '' ? undefined : Number(event.target.value),
                  }))
                }
              >
                <option value="">전체 평점</option>
                {ratingOptions.map((rating) => (
                  <option key={rating} value={rating}>{rating}점</option>
                ))}
              </select>
            </label>
            <label>
              공개 상태
              <select
                value={query.published === undefined ? '' : String(query.published)}
                onChange={(event) => {
                  const value = event.target.value
                  setQuery((current) => ({
                    ...current,
                    published: value === '' ? undefined : value === 'true',
                  }))
                }}
              >
                <option value="">전체 상태</option>
                <option value="true">공개</option>
                <option value="false">숨김</option>
              </select>
            </label>
            <label>
              검색어
              <input
                value={keywordInput}
                placeholder="고객명, 연락처, 리뷰 내용, 예약번호"
                onChange={(event) => setKeywordInput(event.target.value)}
              />
            </label>
            <div className="admin-review-filter-actions">
              <button type="submit">검색</button>
              <button type="button" onClick={resetFilters}>초기화</button>
            </div>
            <div className="admin-review-rating-distribution" aria-label="별점 분포">
              {ratingDistribution.map(({ rating, count }) => (
                <button
                  key={rating}
                  type="button"
                  className={query.rating === rating ? 'active' : ''}
                  onClick={() => setQuery((current) => ({ ...current, rating }))}
                >
                  {rating}점 {count.toLocaleString()}건
                </button>
              ))}
            </div>
          </form>

          {isLoading && <p className="admin-loading">리뷰 목록을 불러오는 중입니다.</p>}

          {!isLoading && reviews.length === 0 && (
            <StatusNotice
              title="등록된 리뷰가 없습니다"
              description="완료된 예약에서 고객이 리뷰를 작성하면 이 화면에서 확인할 수 있습니다."
              actions={['시연할 때는 예약을 완료 상태로 변경한 뒤 고객 예약 상세에서 리뷰를 작성해 보세요.']}
              className="admin-empty-state"
            />
          )}

          {!isLoading && reviews.length > 0 && filteredReviews.length === 0 && (
            <StatusNotice
              title="조건에 맞는 리뷰가 없습니다"
              description="현재 평점과 검색어에 해당하는 리뷰가 없습니다."
              actions={['평점 필터를 전체로 바꾸거나 검색어를 지워 보세요.']}
              className="admin-empty-state"
            />
          )}

          <div className="admin-review-list">
            {filteredReviews.map((review) => (
              <article
                key={review.id}
                className={`${review.rating <= 3 ? 'needs-check' : ''} ${review.published ? 'published' : 'hidden'}`}
              >
                <div className="admin-review-card-heading">
                  <div>
                    <span className={`history-badge ${review.published ? 'sent' : 'failed'}`}>
                      {review.published ? '공개' : '숨김'}
                    </span>
                    <RatingStars rating={review.rating} />
                    <strong>{review.rating}점</strong>
                    <span className="history-badge sent">{review.statusLabel}</span>
                  </div>
                  <div className="admin-review-card-actions">
                    <button
                      type="button"
                      disabled={processingReviewId === review.id}
                      onClick={() => void togglePublished(review)}
                    >
                      {processingReviewId === review.id
                        ? '처리 중'
                        : review.published ? '숨김 처리' : '공개 처리'}
                    </button>
                    <a className="admin-row-link" href={`/admin/reservations/${review.reservationId}`}>
                      예약 상세
                    </a>
                  </div>
                </div>
                <p className="admin-review-content">{review.content}</p>
                <div className="admin-review-reply-panel">
                  <div>
                    <strong>관리자 답변</strong>
                    {review.adminReply && review.adminRepliedAt && (
                      <span>
                        {review.adminRepliedBy ?? 'admin'} · {formatDateTime(review.adminRepliedAt)}
                      </span>
                    )}
                  </div>
                  <textarea
                    rows={3}
                    maxLength={1000}
                    value={replyForms[review.id] ?? ''}
                    placeholder="고객 리뷰에 대한 관리자 답변을 입력하세요."
                    onChange={(event) =>
                      setReplyForms((current) => ({ ...current, [review.id]: event.target.value }))
                    }
                  />
                  <div className="admin-review-reply-actions">
                    <span>{(replyForms[review.id] ?? '').length.toLocaleString()} / 1,000자</span>
                    <button
                      type="button"
                      disabled={processingReviewId === review.id}
                      onClick={() => void submitReply(review)}
                    >
                      {processingReviewId === review.id ? '저장 중' : '답변 저장'}
                    </button>
                  </div>
                </div>
                <dl className="admin-review-meta">
                  <div>
                    <dt>고객</dt>
                    <dd>{review.customerName}</dd>
                  </div>
                  <div>
                    <dt>연락처</dt>
                    <dd>{review.phone}</dd>
                  </div>
                  <div>
                    <dt>이사 일정</dt>
                    <dd>{formatMoveSchedule(review.moveDate, review.moveTime)}</dd>
                  </div>
                  <div>
                    <dt>작성일</dt>
                    <dd>{formatDateTime(review.createdAt)}</dd>
                  </div>
                </dl>
              </article>
            ))}
          </div>
        </>
      )}
    </section>
  )
}
