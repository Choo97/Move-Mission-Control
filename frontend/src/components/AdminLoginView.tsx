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
  const defaultNotice = '관리자 기능은 로그인한 사용자만 사용할 수 있습니다.'
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

    return defaultNotice
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
          <h2>24nalpo Admin</h2>
          <span className="admin-login-mark" aria-hidden="true">
            <svg viewBox="0 0 24 24" role="img">
              <path d="M12 3 5 6v5c0 4.5 2.9 8.4 7 10 4.1-1.6 7-5.5 7-10V6l-7-3Z" />
              <path d="M9.5 11h5v5h-5z" />
              <path d="M10.5 11V9.7a1.5 1.5 0 0 1 3 0V11" />
            </svg>
          </span>
          <p>{defaultNotice}</p>
        </div>

        <form className="admin-login-form" onSubmit={submitLogin}>
          {notice !== defaultNotice && <p className="message info">{notice}</p>}
          {message && <p className="message error">{message}</p>}

          <label>
            아이디
            <input
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              placeholder="아이디를 입력하세요"
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
              placeholder="비밀번호를 입력하세요"
              autoComplete="current-password"
              required
            />
          </label>

          <div className="button-row">
            <button className="submit-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? '로그인 중' : '로그인'}
            </button>
            <button className="submit-button secondary" type="button" onClick={onBackHome}>
              고객 화면으로 돌아가기
            </button>
          </div>
        </form>
      </div>
      <footer className="admin-login-footer">
        <span>© 2026 24nalpo. All rights reserved.</span>
        <a href="/privacy">개인정보처리방침</a>
        <a href="/terms">이용약관</a>
        <span>고객센터 1234-5678 (평일 09:00 ~ 18:00)</span>
      </footer>
    </section>
  )
}
