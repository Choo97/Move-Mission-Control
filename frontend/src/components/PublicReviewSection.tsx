import { useEffect, useMemo, useState } from 'react'
import { getPublicReviews } from '../api/customerApi'
import type { PublicReviewResponse } from '../types'
import './PublicReviewSection.css'

const formatReviewDate = (dateTime: string) => dateTime.slice(0, 10).replaceAll('-', '.')

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
          <p>관리자가 공개 처리한 리뷰만 개인정보 없이 보여드립니다.</p>
        </div>
        <div className="public-review-summary" aria-label="공개 리뷰 요약">
          <article>
            <span>공개 리뷰</span>
            <strong>{reviews.length.toLocaleString()}</strong>
          </article>
          <article>
            <span>평균 평점</span>
            <strong>{averageRating}</strong>
          </article>
          <article>
            <span>관리자 답변</span>
            <strong>{replyCount.toLocaleString()}</strong>
          </article>
        </div>
      </div>

      {isLoading && <p className="public-review-status">공개 리뷰를 불러오는 중입니다.</p>}
      {!isLoading && errorMessage && <p className="public-review-status">{errorMessage}</p>}
      {!isLoading && !errorMessage && reviews.length === 0 && (
        <p className="public-review-status">아직 공개된 리뷰가 없습니다.</p>
      )}

      {!isLoading && reviews.length > 0 && (
        <div className="public-review-grid">
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
