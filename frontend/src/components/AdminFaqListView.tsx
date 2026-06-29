import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import {
  AdminAuthenticationRequiredError,
  createAdminFaq,
  getAdminFaqs,
  getAdminSession,
  logoutAdmin,
  updateAdminFaq,
  updateAdminFaqActive,
} from '../api/adminApi'
import { getErrorMessage } from '../api/apiError'
import { adminLoginHref, redirectToAdminExpiredLogin } from '../adminAuthNavigation'
import { StatusNotice } from './StatusNotice'
import type { AdminFaqResponse, AdminSessionResponse } from '../types'

type FaqFormState = {
  question: string
  answer: string
  displayOrder: string
}

const emptyFaqForm: FaqFormState = {
  question: '',
  answer: '',
  displayOrder: '1',
}

const toFaqForm = (faq: AdminFaqResponse): FaqFormState => ({
  question: faq.question,
  answer: faq.answer,
  displayOrder: String(faq.displayOrder),
})

const toFaqRequest = (form: FaqFormState) => ({
  question: form.question,
  answer: form.answer,
  displayOrder: Number(form.displayOrder),
})

const buildEditForms = (faqs: AdminFaqResponse[]) =>
  Object.fromEntries(faqs.map((faq) => [faq.id, toFaqForm(faq)]))

export function AdminFaqListView() {
  const [faqs, setFaqs] = useState<AdminFaqResponse[]>([])
  const [adminSession, setAdminSession] = useState<AdminSessionResponse | null>(null)
  const [createForm, setCreateForm] = useState<FaqFormState>(emptyFaqForm)
  const [editForms, setEditForms] = useState<Record<number, FaqFormState>>({})
  const [processingFaqId, setProcessingFaqId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isCreating, setIsCreating] = useState(false)
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const [isAuthenticationRequired, setIsAuthenticationRequired] = useState(false)
  const [message, setMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')

  const summary = useMemo(() => ({
    totalCount: faqs.length,
    activeCount: faqs.filter((faq) => faq.active).length,
    hiddenCount: faqs.filter((faq) => !faq.active).length,
  }), [faqs])

  useEffect(() => {
    const loadFaqs = async () => {
      setIsLoading(true)
      setErrorMessage('')
      setIsAuthenticationRequired(false)

      try {
        const [nextAdminSession, nextFaqs] = await Promise.all([getAdminSession(), getAdminFaqs()])
        setAdminSession(nextAdminSession)
        setFaqs(nextFaqs)
        setEditForms(buildEditForms(nextFaqs))
      } catch (error) {
        setAdminSession(null)
        setFaqs([])
        setEditForms({})
        if (error instanceof AdminAuthenticationRequiredError) {
          setIsAuthenticationRequired(true)
          setErrorMessage(getErrorMessage(error, '관리자 로그인이 필요합니다.'))
          redirectToAdminExpiredLogin()
          return
        }

        setIsAuthenticationRequired(false)
        setErrorMessage(getErrorMessage(error, '관리자 FAQ 목록을 불러오지 못했습니다.'))
      } finally {
        setIsLoading(false)
      }
    }

    void loadFaqs()
  }, [])

  const updateFaqInList = (updatedFaq: AdminFaqResponse) => {
    setFaqs((current) =>
      current
        .map((faq) => (faq.id === updatedFaq.id ? updatedFaq : faq))
        .sort((left, right) => left.displayOrder - right.displayOrder || left.id - right.id),
    )
    setEditForms((current) => ({ ...current, [updatedFaq.id]: toFaqForm(updatedFaq) }))
  }

  const submitCreate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsCreating(true)
    setMessage('')
    setErrorMessage('')

    try {
      const createdFaq = await createAdminFaq(toFaqRequest(createForm))
      setFaqs((current) => [...current, createdFaq].sort((left, right) => left.displayOrder - right.displayOrder || left.id - right.id))
      setEditForms((current) => ({ ...current, [createdFaq.id]: toFaqForm(createdFaq) }))
      setCreateForm({ ...emptyFaqForm, displayOrder: String(createdFaq.displayOrder + 1) })
      setMessage('FAQ를 추가했습니다. 공개 상태이면 고객 FAQ 화면에 표시됩니다.')
    } catch (error) {
      setErrorMessage(getErrorMessage(error, 'FAQ 추가에 실패했습니다.'))
    } finally {
      setIsCreating(false)
    }
  }

  const submitUpdate = async (faqId: number) => {
    const form = editForms[faqId]
    if (!form) {
      return
    }

    setProcessingFaqId(faqId)
    setMessage('')
    setErrorMessage('')

    try {
      updateFaqInList(await updateAdminFaq(faqId, toFaqRequest(form)))
      setMessage('FAQ를 수정했습니다.')
    } catch (error) {
      setErrorMessage(getErrorMessage(error, 'FAQ 수정에 실패했습니다.'))
    } finally {
      setProcessingFaqId(null)
    }
  }

  const toggleActive = async (faq: AdminFaqResponse) => {
    setProcessingFaqId(faq.id)
    setMessage('')
    setErrorMessage('')

    try {
      const updatedFaq = await updateAdminFaqActive(faq.id, !faq.active)
      updateFaqInList(updatedFaq)
      setMessage(updatedFaq.active ? 'FAQ를 공개했습니다.' : 'FAQ를 숨김 처리했습니다.')
    } catch (error) {
      setErrorMessage(getErrorMessage(error, 'FAQ 공개 상태 변경에 실패했습니다.'))
    } finally {
      setProcessingFaqId(null)
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
    <section className="admin-faq-view">
      <div className="admin-heading">
        <div>
          <p className="eyebrow">FAQ Manager</p>
          <h2>FAQ 관리</h2>
          <p>고객이 보는 자주 묻는 질문을 등록하고 공개 여부를 관리합니다.</p>
        </div>
        <a className="admin-outline-link" href="/?view=faq">고객 FAQ 보기</a>
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
          <div className="admin-faq-summary-row" aria-label="FAQ 요약">
            <article>
              <span>전체</span>
              <strong>{summary.totalCount.toLocaleString()}</strong>
              <p>등록된 FAQ</p>
            </article>
            <article>
              <span>공개</span>
              <strong>{summary.activeCount.toLocaleString()}</strong>
              <p>고객 화면에 표시</p>
            </article>
            <article>
              <span>숨김</span>
              <strong>{summary.hiddenCount.toLocaleString()}</strong>
              <p>관리자만 확인</p>
            </article>
          </div>

          <form className="admin-faq-create-panel" onSubmit={submitCreate}>
            <div>
              <p className="eyebrow">New FAQ</p>
              <h3>FAQ 추가</h3>
            </div>
            <label>
              정렬 순서
              <input
                type="number"
                min="1"
                max="999"
                value={createForm.displayOrder}
                onChange={(event) => setCreateForm((current) => ({ ...current, displayOrder: event.target.value }))}
              />
            </label>
            <label>
              질문
              <input
                value={createForm.question}
                maxLength={200}
                placeholder="예: 예약 후에는 어떻게 확인하나요?"
                onChange={(event) => setCreateForm((current) => ({ ...current, question: event.target.value }))}
              />
            </label>
            <label>
              답변
              <textarea
                value={createForm.answer}
                maxLength={1000}
                rows={4}
                placeholder="고객에게 보여줄 답변을 입력하세요."
                onChange={(event) => setCreateForm((current) => ({ ...current, answer: event.target.value }))}
              />
            </label>
            <button type="submit" disabled={isCreating}>
              {isCreating ? '추가 중' : 'FAQ 추가'}
            </button>
          </form>

          {isLoading && <p className="admin-loading">FAQ 목록을 불러오는 중입니다.</p>}

          {!isLoading && faqs.length === 0 && (
            <StatusNotice
              title="등록된 FAQ가 없습니다"
              description="고객이 자주 묻는 질문을 등록하면 고객 이용 안내 화면에 표시할 수 있습니다."
              actions={['먼저 예약 조회, 견적, 사진 업로드처럼 반복되는 질문부터 등록해 보세요.']}
              className="admin-empty-state"
            />
          )}

          <div className="admin-faq-list">
            {faqs.map((faq) => {
              const form = editForms[faq.id] ?? toFaqForm(faq)
              const isProcessing = processingFaqId === faq.id

              return (
                <article
                  key={faq.id}
                  className={`admin-faq-card ${faq.active ? 'active' : 'hidden'}`}
                  data-faq-id={faq.id}
                >
                  <div className="admin-faq-card-heading">
                    <div>
                      <span className={`history-badge ${faq.active ? 'sent' : 'failed'}`}>
                        {faq.active ? '공개' : '숨김'}
                      </span>
                      <strong>FAQ #{faq.id}</strong>
                    </div>
                    <button type="button" disabled={isProcessing} onClick={() => void toggleActive(faq)}>
                      {faq.active ? '숨김 처리' : '공개 처리'}
                    </button>
                  </div>
                  <div className="admin-faq-edit-grid">
                    <label>
                      정렬 순서
                      <input
                        type="number"
                        min="1"
                        max="999"
                        value={form.displayOrder}
                        onChange={(event) =>
                          setEditForms((current) => ({
                            ...current,
                            [faq.id]: { ...form, displayOrder: event.target.value },
                          }))
                        }
                      />
                    </label>
                    <label>
                      질문
                      <input
                        value={form.question}
                        maxLength={200}
                        onChange={(event) =>
                          setEditForms((current) => ({
                            ...current,
                            [faq.id]: { ...form, question: event.target.value },
                          }))
                        }
                      />
                    </label>
                    <label>
                      답변
                      <textarea
                        value={form.answer}
                        maxLength={1000}
                        rows={3}
                        onChange={(event) =>
                          setEditForms((current) => ({
                            ...current,
                            [faq.id]: { ...form, answer: event.target.value },
                          }))
                        }
                      />
                    </label>
                  </div>
                  <div className="admin-faq-card-actions">
                    <button type="button" disabled={isProcessing} onClick={() => void submitUpdate(faq.id)}>
                      {isProcessing ? '처리 중' : '수정 저장'}
                    </button>
                  </div>
                </article>
              )
            })}
          </div>
        </>
      )}
    </section>
  )
}
