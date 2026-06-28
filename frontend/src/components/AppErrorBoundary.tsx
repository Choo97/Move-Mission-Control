import { Component } from 'react'
import type { ErrorInfo, ReactNode } from 'react'

type Props = {
  children: ReactNode
}

type State = {
  hasError: boolean
  message: string
}

export class AppErrorBoundary extends Component<Props, State> {
  state: State = {
    hasError: false,
    message: '',
  }

  static getDerivedStateFromError(error: unknown): State {
    return {
      hasError: true,
      message: error instanceof Error ? error.message : '알 수 없는 화면 오류가 발생했습니다.',
    }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('React render error', error, errorInfo)
  }

  render() {
    if (!this.state.hasError) {
      return this.props.children
    }

    const isAdminPath = window.location.pathname.startsWith('/admin')

    return (
      <main className="app-shell app-error-page">
        <section className="app-error-panel" aria-labelledby="app-error-title">
          <p className="eyebrow">Screen Error</p>
          <h1 id="app-error-title">화면을 표시하는 중 문제가 발생했습니다</h1>
          <p>
            입력한 데이터나 기존 예약 정보 중 화면에서 처리하지 못한 값이 있을 수 있습니다.
            페이지를 새로고침해도 반복되면 방금 작업한 예약 번호와 함께 알려주세요.
          </p>
          <div className="app-error-detail">
            <span>오류 정보</span>
            <code>{this.state.message}</code>
          </div>
          <div className="app-error-actions">
            <button type="button" onClick={() => window.location.reload()}>
              새로고침
            </button>
            {isAdminPath ? (
              <button type="button" onClick={() => window.location.assign('/admin/reservations')}>
                관리자 목록
              </button>
            ) : (
              <button type="button" onClick={() => window.location.assign('/')}>
                홈으로 이동
              </button>
            )}
          </div>
        </section>
      </main>
    )
  }
}
