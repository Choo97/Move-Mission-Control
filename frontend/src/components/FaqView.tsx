import { useEffect, useState } from 'react'
import { getFaqs } from '../api/customerApi'
import { getErrorMessage } from '../api/apiError'
import type { FaqResponse } from '../types'
import { StatusNotice } from './StatusNotice'

export function FaqView() {
  const [faqs, setFaqs] = useState<FaqResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    void getFaqs()
      .then(setFaqs)
      .catch((error) => setErrorMessage(getErrorMessage(error, '자주 묻는 질문을 불러오지 못했습니다.')))
      .finally(() => setIsLoading(false))
  }, [])

  return (
    <section className="faq-view">
      <div className="section-heading">
        <p className="eyebrow">Customer Support</p>
        <h2>자주 묻는 질문</h2>
      </div>
      {isLoading && (
        <StatusNotice
          title="질문을 불러오는 중입니다"
          description="잠시만 기다리면 자주 묻는 질문 목록이 표시됩니다."
        />
      )}
      {errorMessage && (
        <StatusNotice
          title="질문을 불러오지 못했습니다"
          description={errorMessage}
          actions={['잠시 후 다시 접속해 주세요.', '예약 관련 문의는 예약 조회 화면에서 진행 상태를 먼저 확인해 주세요.']}
          tone="info"
        />
      )}
      <div className="faq-list">
        {faqs.map((faq) => (
          <details key={faq.id}>
            <summary>{faq.question}</summary>
            <p>{faq.answer}</p>
          </details>
        ))}
      </div>
      {!isLoading && !errorMessage && faqs.length === 0 && (
        <StatusNotice
          title="등록된 질문이 없습니다"
          description="운영자가 FAQ를 등록하면 이 영역에 질문과 답변이 표시됩니다."
          actions={['예약 신청과 예약 조회 기능은 계속 사용할 수 있습니다.']}
        />
      )}
    </section>
  )
}
