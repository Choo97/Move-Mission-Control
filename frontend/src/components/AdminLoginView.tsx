import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { loginAdmin } from '../api/adminApi'
import { getErrorMessage } from '../api/apiError'

type Props = {
  onBackHome: () => void
}

const nextPath = () => {
  const next = new URLSearchParams(window.location.search).get('next')

  if (next?.startsWith('/') && !next.startsWith('//')) {
    return next
  }

  return '/admin/reservations'
}

export function AdminLoginView({ onBackHome }: Props) {
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const notice = useMemo(() => {
    const params = new URLSearchParams(window.location.search)

    if (params.has('error')) {
      return '아이디 또는 비밀번호가 올바르지 않습니다.'
    }

    if (params.has('logout')) {
      return '로그아웃되었습니다.'
    }

    if (params.has('expired')) {
      return '보안을 위해 로그인 세션이 만료되었습니다. 다시 로그인해 주세요.'
    }

    if (params.has('passwordChanged')) {
      return '비밀번호가 변경되었습니다. 새 비밀번호로 다시 로그인해 주세요.'
    }

    return '관리자 기능은 로그인한 사용자만 사용할 수 있습니다.'
  }, [])

  const submitLogin = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSubmitting(true)
    setMessage('')

    try {
      await loginAdmin(username, password)
      window.location.assign(nextPath())
    } catch (error) {
      setMessage(getErrorMessage(error, '관리자 로그인에 실패했습니다.'))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="admin-login-page">
      <div className="admin-login-shell">
        <div className="admin-login-heading">
          <p className="eyebrow">Admin Access</p>
          <h2>관리자 로그인</h2>
          <p>예약 현황, 고객 요청, 알림 발송 내역을 확인하려면 관리자 계정으로 로그인해 주세요.</p>
        </div>

        <form className="admin-login-form" onSubmit={submitLogin}>
          <p className="message info">{notice}</p>
          {message && <p className="message error">{message}</p>}

          <label>
            아이디
            <input
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              placeholder="admin"
              autoComplete="username"
              required
            />
          </label>

          <label>
            비밀번호
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="비밀번호"
              autoComplete="current-password"
              required
            />
          </label>

          <div className="button-row">
            <button className="submit-button secondary" type="button" onClick={onBackHome}>
              고객 화면
            </button>
            <button className="submit-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? '로그인 중' : '로그인'}
            </button>
          </div>
        </form>
      </div>
    </section>
  )
}
