import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import {
  AdminAuthenticationRequiredError,
  getAdminNotifications,
  getAdminSession,
  logoutAdmin,
} from '../api/adminApi'
import { getErrorMessage } from '../api/apiError'
import { adminLoginHref, redirectToAdminExpiredLogin } from '../adminAuthNavigation'
import { StatusNotice } from './StatusNotice'
import type {
  AdminNotificationChannel,
  AdminNotificationListItemResponse,
  AdminNotificationListQuery,
  AdminNotificationStatus,
  AdminSessionResponse,
} from '../types'

const channelOptions: Array<{ value: AdminNotificationChannel; label: string }> = [
  { value: 'EMAIL', label: '이메일' },
  { value: 'SMS', label: 'SMS' },
  { value: 'KAKAO_ALIMTALK', label: '카카오 알림톡' },
]

const statusOptions: Array<{ value: AdminNotificationStatus; label: string }> = [
  { value: 'READY', label: '발송 준비' },
  { value: 'SENT', label: '발송 완료' },
  { value: 'FAILED', label: '발송 실패' },
]

const readInitialNotificationQuery = (): AdminNotificationListQuery => {
  const params = new URLSearchParams(window.location.search)
  const channelValue = params.get('channel')
  const statusValue = params.get('status')

  return {
    channel: channelOptions.some(({ value }) => value === channelValue)
      ? (channelValue as AdminNotificationChannel)
      : undefined,
    status: statusOptions.some(({ value }) => value === statusValue)
      ? (statusValue as AdminNotificationStatus)
      : undefined,
    keyword: params.get('keyword') ?? '',
  }
}

const formatDateTime = (dateTime: string | null) => (dateTime ? dateTime.replace('T', ' ').slice(0, 16) : '발송 전')

export function AdminNotificationListView() {
  const [query, setQuery] = useState<AdminNotificationListQuery>(readInitialNotificationQuery)
  const [keywordInput, setKeywordInput] = useState(() => readInitialNotificationQuery().keyword ?? '')
  const [notifications, setNotifications] = useState<AdminNotificationListItemResponse[]>([])
  const [adminSession, setAdminSession] = useState<AdminSessionResponse | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const [isAuthenticationRequired, setIsAuthenticationRequired] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  const summary = useMemo(() => {
    const failedCount = notifications.filter((notification) => notification.status === 'FAILED').length
    const readyCount = notifications.filter((notification) => notification.status === 'READY').length
    const sentCount = notifications.filter((notification) => notification.status === 'SENT').length

    return {
      failedCount,
      readyCount,
      sentCount,
      totalCount: notifications.length,
    }
  }, [notifications])

  useEffect(() => {
    const loadNotifications = async () => {
      setIsLoading(true)
      setErrorMessage('')
      setIsAuthenticationRequired(false)

      try {
        const [nextAdminSession, nextNotifications] = await Promise.all([
          getAdminSession(),
          getAdminNotifications(query),
        ])
        setAdminSession(nextAdminSession)
        setNotifications(nextNotifications)
      } catch (error) {
        setAdminSession(null)
        setNotifications([])
        if (error instanceof AdminAuthenticationRequiredError) {
          setIsAuthenticationRequired(true)
          setErrorMessage(getErrorMessage(error, '관리자 로그인이 필요합니다.'))
          redirectToAdminExpiredLogin()
          return
        }

        setIsAuthenticationRequired(false)
        setErrorMessage(getErrorMessage(error, '관리자 알림 이력을 불러오지 못했습니다.'))
      } finally {
        setIsLoading(false)
      }
    }

    void loadNotifications()
  }, [query])

  useEffect(() => {
    const params = new URLSearchParams()
    if (query.channel) {
      params.set('channel', query.channel)
    }
    if (query.status) {
      params.set('status', query.status)
    }
    if (query.keyword) {
      params.set('keyword', query.keyword)
    }

    const queryString = params.toString()
    window.history.replaceState(null, '', `/admin/notifications${queryString ? `?${queryString}` : ''}`)
  }, [query])

  const submitFilter = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setQuery((current) => ({ ...current, keyword: keywordInput.trim() }))
  }

  const resetFilters = () => {
    setKeywordInput('')
    setQuery({})
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
    <section className="admin-notification-view">
      <div className="admin-heading">
        <div>
          <p className="eyebrow">Notification Center</p>
          <h2>알림 발송 이력</h2>
          <p>고객 알림의 채널, 발송 상태, 실패 사유를 한 화면에서 확인합니다.</p>
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
          <div className="admin-notification-summary-row" aria-label="알림 이력 요약">
            <article>
              <span>전체</span>
              <strong>{summary.totalCount.toLocaleString()}</strong>
              <p>현재 조건의 알림 이력</p>
            </article>
            <article>
              <span>실패</span>
              <strong>{summary.failedCount.toLocaleString()}</strong>
              <p>재확인 또는 재발송 필요</p>
            </article>
            <article>
              <span>발송 대기</span>
              <strong>{summary.readyCount.toLocaleString()}</strong>
              <p>아직 고객에게 보내지 않음</p>
            </article>
            <article>
              <span>발송 완료</span>
              <strong>{summary.sentCount.toLocaleString()}</strong>
              <p>고객 안내 처리 완료</p>
            </article>
          </div>

          <form className="admin-notification-filters" onSubmit={submitFilter} aria-label="알림 이력 필터">
            <label>
              채널
              <select
                value={query.channel ?? ''}
                onChange={(event) =>
                  setQuery((current) => ({
                    ...current,
                    channel: event.target.value === '' ? undefined : (event.target.value as AdminNotificationChannel),
                  }))
                }
              >
                <option value="">전체 채널</option>
                {channelOptions.map((option) => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </select>
            </label>
            <label>
              상태
              <select
                value={query.status ?? ''}
                onChange={(event) =>
                  setQuery((current) => ({
                    ...current,
                    status: event.target.value === '' ? undefined : (event.target.value as AdminNotificationStatus),
                  }))
                }
              >
                <option value="">전체 상태</option>
                {statusOptions.map((option) => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </select>
            </label>
            <label>
              검색어
              <input
                value={keywordInput}
                placeholder="고객명, 연락처, 이메일"
                onChange={(event) => setKeywordInput(event.target.value)}
              />
            </label>
            <div className="admin-notification-filter-actions">
              <button type="submit">검색</button>
              <button type="button" onClick={resetFilters}>초기화</button>
            </div>
            <div className="admin-notification-quick-filters" aria-label="빠른 필터">
              <button type="button" onClick={() => setQuery((current) => ({ ...current, status: 'FAILED' }))}>
                실패만
              </button>
              <button type="button" onClick={() => setQuery((current) => ({ ...current, channel: 'EMAIL' }))}>
                이메일만
              </button>
              <button type="button" onClick={() => setQuery((current) => ({ ...current, channel: 'EMAIL', status: 'FAILED' }))}>
                이메일 실패
              </button>
            </div>
          </form>

          {isLoading && <p className="admin-loading">알림 이력을 불러오는 중입니다.</p>}

          <div className="admin-table-wrap">
            <table className="admin-notification-history-table">
              <thead>
                <tr>
                  <th>예약</th>
                  <th>고객</th>
                  <th>유형</th>
                  <th>채널</th>
                  <th>상태</th>
                  <th>수신처</th>
                  <th>일시</th>
                  <th>메시지</th>
                  <th>처리</th>
                </tr>
              </thead>
              <tbody>
                {notifications.map((notification) => (
                  <tr key={notification.id} className={notification.status.toLowerCase()}>
                    <td>
                      <a className="admin-row-link" href={`/admin/reservations/${notification.reservationId}`}>
                        #{notification.reservationId}
                      </a>
                    </td>
                    <td>
                      <strong>{notification.customerName}</strong>
                      <span>{notification.phone}</span>
                      <span>{notification.email ?? '이메일 미입력'}</span>
                    </td>
                    <td>{notification.typeLabel}</td>
                    <td>{notification.channelLabel}</td>
                    <td>
                      <span className={`history-badge ${notification.status.toLowerCase()}`}>{notification.statusLabel}</span>
                      {notification.failureReason && <span>{notification.failureReason}</span>}
                    </td>
                    <td>{notification.recipientContact}</td>
                    <td>
                      <span>생성 {formatDateTime(notification.createdAt)}</span>
                      <span>발송 {formatDateTime(notification.sentAt)}</span>
                    </td>
                    <td>{notification.message}</td>
                    <td>
                      <a className="admin-row-link" href={`/admin/reservations/${notification.reservationId}`}>
                        예약 상세
                      </a>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {!isLoading && notifications.length === 0 && (
              <StatusNotice
                title="조건에 맞는 알림 이력이 없습니다"
                description="현재 필터와 검색어에 해당하는 알림 이력이 없습니다."
                actions={['상태와 채널 필터를 전체로 바꾸거나 검색어를 지워 보세요.']}
                className="admin-empty-state"
              />
            )}
          </div>
        </>
      )}
    </section>
  )
}
