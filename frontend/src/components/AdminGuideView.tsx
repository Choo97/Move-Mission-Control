import { useEffect, useState } from 'react'
import {
  AdminAuthenticationRequiredError,
  getAdminSession,
  logoutAdmin,
} from '../api/adminApi'
import { getErrorMessage } from '../api/apiError'
import { adminLoginHref, redirectToAdminExpiredLogin } from '../adminAuthNavigation'
import type { AdminSessionResponse } from '../types'

const firstLookItems = [
  {
    title: '처리 필요 예약',
    description: '오늘 가장 먼저 확인할 예약입니다. 고객 요청, 거리 미입력, 알림 실패가 섞여 있을 수 있습니다.',
    href: '/admin/reservations?attentionRequired=true',
    label: '예약 큐 열기',
  },
  {
    title: '거리 확인 필요',
    description: '견적 정확도에 직접 영향을 주는 예약입니다. 거리 입력 후 견적 금액을 다시 확인하세요.',
    href: '/admin/reservations?needsDistance=true',
    label: '거리 확인',
  },
  {
    title: '실패 알림',
    description: '고객이 상태 변경을 받지 못했을 수 있습니다. 실패 사유와 연락처를 먼저 확인하세요.',
    href: '/admin/notifications?status=FAILED',
    label: '실패 알림',
  },
  {
    title: '신규 접수',
    description: '아직 상담을 시작하지 않은 예약입니다. 고객 정보와 이사 일정을 확인한 뒤 상담중으로 변경합니다.',
    href: '/admin/reservations?status=RECEIVED',
    label: '신규 접수',
  },
]

const workflowSteps = [
  {
    status: '접수',
    goal: '예약 정보가 충분한지 확인하고 상담을 시작합니다.',
    actions: ['고객명/연락처 확인', '출발지와 도착지 확인', '거리 입력 필요 여부 확인', '상담중으로 변경'],
  },
  {
    status: '상담중',
    goal: '상담 내용을 메모하고 견적 안내 준비를 마칩니다.',
    actions: ['짐 규모와 요청사항 확인', '관리자 메모 저장', '견적 금액 확인', '견적안내로 변경'],
  },
  {
    status: '견적안내',
    goal: '고객이 견적을 이해하고 동의할 수 있게 안내합니다.',
    actions: ['최종 견적 확인', '알림 발송 상태 확인', '고객 동의 여부 확인', '동의 후 확정 처리'],
  },
  {
    status: '확정',
    goal: '이사 진행 전 누락 정보를 다시 확인합니다.',
    actions: ['이사일/시간 재확인', '특이 요청 확인', '필요 시 고객에게 재안내', '이사 완료 후 완료 처리'],
  },
  {
    status: '완료/취소',
    goal: '추가 처리 없이 이력과 메모만 정리합니다.',
    actions: ['완료 예약은 리뷰 확인', '취소 예약은 사유 메모 확인', '추가 알림 필요 여부 점검'],
  },
]

const cautionItems = [
  '취소 처리는 고객 요청 여부와 상담 메모를 확인한 뒤 진행합니다.',
  '상태 변경 후 알림이 생성될 수 있으니 알림 이력에서 발송 상태를 확인합니다.',
  '거리 입력 전 견적은 정확하지 않을 수 있습니다. 견적 안내 전 반드시 거리와 차량/인원을 점검합니다.',
  '고객 수정 요청은 기존 정보와 요청 내용을 비교한 뒤 승인 또는 반려합니다.',
]

const responseTemplates = [
  {
    title: '예약 조회 안내',
    text: '예약번호와 연락처로 예약 조회 화면에서 진행 상황을 확인하실 수 있습니다.',
  },
  {
    title: '견적 동의 유도',
    text: '안내드린 견적을 확인하신 뒤 예약 상세 화면에서 동의해 주시면 예약 확정 단계로 진행됩니다.',
  },
  {
    title: '일정 변경 문의',
    text: '일정이나 주소가 바뀐 경우 예약 취소보다 수정 요청을 먼저 남겨 주세요. 확인 후 가능 여부를 안내드리겠습니다.',
  },
  {
    title: '취소 문의',
    text: '취소가 필요한 경우 요청을 접수해 주세요. 처리 전 확인이 필요한 내용이 있으면 별도로 연락드리겠습니다.',
  },
]

export function AdminGuideView() {
  const [adminSession, setAdminSession] = useState<AdminSessionResponse | null>(null)
  const [isAuthenticationRequired, setIsAuthenticationRequired] = useState(false)
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    const loadSession = async () => {
      setErrorMessage('')
      setIsAuthenticationRequired(false)

      try {
        setAdminSession(await getAdminSession())
      } catch (error) {
        setAdminSession(null)
        if (error instanceof AdminAuthenticationRequiredError) {
          setIsAuthenticationRequired(true)
          setErrorMessage(getErrorMessage(error, '관리자 로그인이 필요합니다.'))
          redirectToAdminExpiredLogin()
          return
        }

        setErrorMessage(getErrorMessage(error, '관리자 로그인 상태를 확인하지 못했습니다.'))
      }
    }

    void loadSession()
  }, [])

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
    <section className="admin-guide-view">
      <div className="admin-heading">
        <div>
          <p className="eyebrow">Operations Playbook</p>
          <h2>운영 가이드</h2>
          <p>처음 운영하는 사람도 오늘 처리할 예약과 상태별 작업 순서를 바로 따라갈 수 있도록 정리했습니다.</p>
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
          <section className="admin-guide-focus" aria-labelledby="admin-guide-focus-title">
            <div className="admin-guide-section-heading">
              <div>
                <p className="eyebrow">Start Here</p>
                <h3 id="admin-guide-focus-title">오늘 먼저 볼 것</h3>
              </div>
              <span>상단 빠른 작업과 같은 기준입니다.</span>
            </div>
            <div className="admin-guide-focus-grid">
              {firstLookItems.map((item) => (
                <article key={item.title}>
                  <strong>{item.title}</strong>
                  <p>{item.description}</p>
                  <a href={item.href}>{item.label}</a>
                </article>
              ))}
            </div>
          </section>

          <section className="admin-guide-workflow" aria-labelledby="admin-guide-workflow-title">
            <div className="admin-guide-section-heading">
              <div>
                <p className="eyebrow">Status Workflow</p>
                <h3 id="admin-guide-workflow-title">예약 상태별 처리 순서</h3>
              </div>
            </div>
            <div className="admin-guide-workflow-list">
              {workflowSteps.map((step) => (
                <article key={step.status}>
                  <div>
                    <span>{step.status}</span>
                    <strong>{step.goal}</strong>
                  </div>
                  <ol>
                    {step.actions.map((action) => (
                      <li key={action}>{action}</li>
                    ))}
                  </ol>
                </article>
              ))}
            </div>
          </section>

          <div className="admin-guide-columns">
            <section className="admin-guide-cautions" aria-labelledby="admin-guide-cautions-title">
              <div className="admin-guide-section-heading">
                <div>
                  <p className="eyebrow">Risk Check</p>
                  <h3 id="admin-guide-cautions-title">실수하기 쉬운 작업</h3>
                </div>
              </div>
              <ul>
                {cautionItems.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </section>

            <section className="admin-guide-templates" aria-labelledby="admin-guide-templates-title">
              <div className="admin-guide-section-heading">
                <div>
                  <p className="eyebrow">Customer Reply</p>
                  <h3 id="admin-guide-templates-title">고객 문의 응대 문구</h3>
                </div>
              </div>
              <div className="admin-guide-template-list">
                {responseTemplates.map((template) => (
                  <article key={template.title}>
                    <strong>{template.title}</strong>
                    <p>{template.text}</p>
                  </article>
                ))}
              </div>
            </section>
          </div>
        </>
      )}
    </section>
  )
}
