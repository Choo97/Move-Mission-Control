import { useEffect, useMemo, useState } from 'react'
import { getPublicReviews } from '../api/customerApi'
import type { PublicReviewResponse } from '../types'
import './PublicReviewSection.css'

const formatReviewDate = (dateTime: string) => dateTime.slice(0, 10).replaceAll('-', '.')

const publicReviewIntro = (reviewCount: number) => {
  if (reviewCount === 0) {
    return '첫 후기가 등록되면 이곳에서 바로 확인할 수 있습니다.'
  }

  if (reviewCount === 1) {
    return '최근 고객 후기를 통해 예약 이후의 진행 과정을 확인해 보세요.'
  }

  return '이사 예약부터 진행까지, 고객님들이 남겨주신 후기를 확인해 보세요.'
}

function PublicRatingStars({ rating }: { rating: number }) {
  return (
    <span className="public-review-stars" aria-label={`${rating}점`}>
      {'★'.repeat(rating)}
      {'☆'.repeat(5 - rating)}
    </span>
  )
}

export function PublicReviewSection() {
  const [reviews, setReviews] = useState<PublicReviewResponse[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  const averageRating = useMemo(() => {
    if (reviews.length === 0) {
      return '0.0'
    }

    const totalRating = reviews.reduce((sum, review) => sum + review.rating, 0)
    return (totalRating / reviews.length).toFixed(1)
  }, [reviews])

  const replyCount = useMemo(
    () => reviews.filter((review) => Boolean(review.adminReply)).length,
    [reviews],
  )

  const reviewGridMode = reviews.length === 1 ? 'single' : reviews.length === 2 ? 'pair' : 'multiple'

  useEffect(() => {
    const loadReviews = async () => {
      setIsLoading(true)
      setErrorMessage('')

      try {
        setReviews(await getPublicReviews(6))
      } catch {
        setReviews([])
        setErrorMessage('공개 리뷰를 불러오지 못했습니다.')
      } finally {
        setIsLoading(false)
      }
    }

    void loadReviews()
  }, [])

  return (
    <section className="public-review-section" aria-labelledby="public-review-title">
      <div className="public-review-heading">
        <div>
          <p className="eyebrow">Customer Reviews</p>
          <h2 id="public-review-title">실제 이용 고객의 리뷰를 확인하세요</h2>
          <p>{publicReviewIntro(reviews.length)}</p>
        </div>
        <div className="public-review-summary" aria-label="공개 리뷰 요약">
          <article>
            <span>소개 중인 후기</span>
            <strong>{reviews.length.toLocaleString()}</strong>
          </article>
          <article>
            <span>평균 평점</span>
            <strong>{averageRating}</strong>
          </article>
          <article>
            <span>답변 포함</span>
            <strong>{replyCount.toLocaleString()}</strong>
          </article>
        </div>
      </div>

      {isLoading && (
        <div className="public-review-status" role="status">
          <strong>고객 후기를 불러오고 있습니다</strong>
          <p>최근 후기를 정리해 화면에 보여드릴게요.</p>
        </div>
      )}
      {!isLoading && errorMessage && (
        <div className="public-review-status">
          <strong>후기를 불러오지 못했습니다</strong>
          <p>{errorMessage}</p>
        </div>
      )}
      {!isLoading && !errorMessage && reviews.length === 0 && (
        <div className="public-review-status public-review-empty">
          <strong>아직 소개할 후기가 준비되지 않았습니다</strong>
          <p>이사가 완료된 고객의 후기가 등록되면 이 영역에 차례대로 보여드립니다.</p>
        </div>
      )}

      {!isLoading && reviews.length > 0 && (
        <div className={`public-review-grid ${reviewGridMode}`}>
          {reviews.map((review) => (
            <article key={review.id} className="public-review-card">
              <div className="public-review-card-heading">
                <PublicRatingStars rating={review.rating} />
                <span>{review.moveTypeLabel}</span>
              </div>
              <p>{review.content}</p>
              {review.adminReply && (
                <blockquote>
                  <strong>관리자 답변</strong>
                  <span>{review.adminReply}</span>
                </blockquote>
              )}
              <footer>
                <span>{review.customerName}</span>
                <time dateTime={review.createdAt}>{formatReviewDate(review.createdAt)}</time>
              </footer>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
