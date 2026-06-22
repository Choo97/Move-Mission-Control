import { useEffect, useState } from 'react'
import { getFaqs } from '../api/customerApi'
import { getErrorMessage } from '../api/apiError'
import type { FaqResponse } from '../types'

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
      {isLoading && <p className="message">질문을 불러오는 중입니다.</p>}
      {errorMessage && <p className="message error">{errorMessage}</p>}
      <div className="faq-list">
        {faqs.map((faq) => (
          <details key={faq.id}>
            <summary>{faq.question}</summary>
            <p>{faq.answer}</p>
          </details>
        ))}
      </div>
    </section>
  )
}
