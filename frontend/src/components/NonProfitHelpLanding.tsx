import nonprofitHelpHero from '../assets/nonprofit-help-hero.png'
import './NonProfitHelpLanding.css'

type NonProfitHelpLandingProps = {
  onRequestClick: () => void
  onLookupClick: () => void
}

type HelpIconName = 'calendar' | 'check' | 'clipboard' | 'home' | 'mapPin' | 'message' | 'search' | 'shield' | 'truck' | 'user'

const audienceItems = [
  {
    icon: 'home',
    title: '갑작스러운 이사 준비가 필요한 신청자',
    description: '상황을 먼저 남겨주시면 운영자가 내용을 확인합니다.',
  },
  {
    icon: 'user',
    title: '혼자 짐 정리가 어려운 신청자',
    description: '짐 규모와 요청사항을 보고 가능한 도움 범위를 안내합니다.',
  },
  {
    icon: 'calendar',
    title: '이사 준비 절차를 안내받고 싶은 신청자',
    description: '일정, 주소, 사진 등 필요한 정보를 차근차근 입력할 수 있습니다.',
  },
  {
    icon: 'message',
    title: '운영자 확인 후 안내가 필요한 신청자',
    description: '도움 가능 여부는 접수 내용을 확인한 뒤 전달합니다.',
  },
] satisfies Array<{ icon: HelpIconName; title: string; description: string }>

const processSteps = [
  {
    step: 'STEP 01',
    icon: 'clipboard',
    title: '도움 요청 정보 입력',
    description: '출발지, 도착지, 이사 예정일 등 필요한 정보를 입력합니다.',
  },
  {
    step: 'STEP 02',
    icon: 'check',
    title: '요청 접수 완료',
    description: '접수가 완료되면 요청번호가 발급됩니다.',
  },
  {
    step: 'STEP 03',
    icon: 'search',
    title: '운영자 확인',
    description: '운영자가 요청 내용과 일정을 확인합니다.',
  },
  {
    step: 'STEP 04',
    icon: 'message',
    title: '가능 여부와 다음 안내 전달',
    description: '가능한 도움 범위와 필요한 절차를 안내합니다.',
  },
  {
    step: 'STEP 05',
    icon: 'truck',
    title: '도움 진행 또는 대체 안내',
    description: '도움 진행이 어렵다면 다른 방법을 함께 안내합니다.',
  },
] satisfies Array<{ step: string; icon: HelpIconName; title: string; description: string }>

const checkItems = [
  '접수 후 운영자가 내용을 확인합니다.',
  '접수만으로 도움 진행이 확정되는 것은 아닙니다.',
  '요청 내용, 일정, 지역, 운영 상황에 따라 도움 가능 여부가 달라질 수 있습니다.',
  '긴급 상황은 별도 연락 수단을 확인해 주세요.',
  '개인정보는 요청 확인과 안내 목적에만 사용합니다.',
]

const gratitudeMessages = [
  {
    message: '혼자 준비해야 해서 막막했는데, 어떤 정보를 먼저 정리해야 하는지 안내받아 마음이 놓였습니다.',
    name: '서울 관악구 신청자',
  },
  {
    message: '요청을 남긴 뒤 진행 상황을 확인할 수 있어서 가족과 일정을 맞추는 데 도움이 됐습니다.',
    name: '경기 성남시 신청자',
  },
]

function HelpLandingIcon({ name }: { name: HelpIconName }) {
  const icon = (() => {
    switch (name) {
      case 'calendar':
        return (
          <>
            <path d="M7 3v3" />
            <path d="M17 3v3" />
            <path d="M4 8h16" />
            <path d="M5 5h14a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1Z" />
          </>
        )
      case 'check':
        return <path d="m5 13 4 4L19 7" />
      case 'clipboard':
        return (
          <>
            <path d="M9 4h6l1 2h2a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h2l1-2Z" />
            <path d="M9 12h6" />
            <path d="M9 16h4" />
          </>
        )
      case 'home':
        return (
          <>
            <path d="m4 11 8-7 8 7" />
            <path d="M6 10v10h12V10" />
            <path d="M10 20v-6h4v6" />
          </>
        )
      case 'mapPin':
        return (
          <>
            <path d="M12 21s7-6.2 7-12A7 7 0 1 0 5 9c0 5.8 7 12 7 12Z" />
            <path d="M12 12a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
          </>
        )
      case 'message':
        return (
          <>
            <path d="M4 5h16v10H8l-4 4V5Z" />
            <path d="M8 9h8" />
            <path d="M8 12h5" />
          </>
        )
      case 'search':
        return (
          <>
            <path d="M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14Z" />
            <path d="m20 20-4-4" />
          </>
        )
      case 'shield':
        return (
          <>
            <path d="M12 3 20 6v6c0 5-3.4 8-8 9-4.6-1-8-4-8-9V6l8-3Z" />
            <path d="m8.5 12 2.2 2.2L15.8 9" />
          </>
        )
      case 'truck':
        return (
          <>
            <path d="M3 7h11v9H3V7Z" />
            <path d="M14 10h4l3 3v3h-7v-6Z" />
            <path d="M7 19a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z" />
            <path d="M17 19a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z" />
          </>
        )
      case 'user':
        return (
          <>
            <path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" />
            <path d="M4 21a8 8 0 0 1 16 0" />
          </>
        )
    }
  })()

  return (
    <svg aria-hidden="true" focusable="false" viewBox="0 0 24 24">
      {icon}
    </svg>
  )
}

export function NonProfitHelpLanding({ onRequestClick, onLookupClick }: NonProfitHelpLandingProps) {
  return (
    <section className="help-landing" aria-label="비영리 이사 도움 요청 안내">
      <section className="help-hero" aria-labelledby="help-hero-title">
        <div className="help-hero-copy">
          <h1 id="help-hero-title">이사가 막막할 때, 도움 요청을 남겨주세요</h1>
          <p>
            출발지와 도착지, 이사 예정일을 남겨주시면 운영자가 내용을 확인한 뒤 가능한 도움 범위를
            안내합니다.
          </p>
          <div className="help-hero-actions">
            <button type="button" onClick={onRequestClick}>
              이사 도움 요청하기
            </button>
            <button type="button" onClick={onLookupClick}>
              요청 내역 확인하기
            </button>
          </div>
        </div>

        <div className="help-hero-visual">
          <img src={nonprofitHelpHero} alt="" />
          <div className="help-operator-note" aria-hidden="true">
            <span>운영자 확인</span>
            <strong>요청 내용 확인 후 안내</strong>
          </div>
          <div className="help-route-note" aria-hidden="true">
            <HelpLandingIcon name="mapPin" />
            <span>출발지와 도착지 확인</span>
          </div>
        </div>
      </section>

      <section className="help-audience" aria-labelledby="help-audience-title">
        <div className="help-section-title">
          <h2 id="help-audience-title">도움이 필요한 분</h2>
          <p>도움 진행을 보장하기보다, 상황을 확인하고 가능한 범위를 안내하는 접수 흐름입니다.</p>
        </div>
        <div className="help-audience-grid">
          {audienceItems.map((item) => (
            <article key={item.title}>
              <span>
                <HelpLandingIcon name={item.icon} />
              </span>
              <div>
                <h3>{item.title}</h3>
                <p>{item.description}</p>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="help-process" aria-labelledby="help-process-title">
        <div className="help-section-title">
          <h2 id="help-process-title">진행 절차</h2>
          <p>요청은 가격 비교가 아니라 운영자 확인과 다음 안내를 중심으로 진행됩니다.</p>
        </div>
        <ol className="help-process-list">
          {processSteps.map((item) => (
            <li key={item.step}>
              <span className="help-step-icon">
                <HelpLandingIcon name={item.icon} />
              </span>
              <small>{item.step}</small>
              <strong>{item.title}</strong>
              <p>{item.description}</p>
            </li>
          ))}
        </ol>
      </section>

      <section className="help-info-row" aria-label="요청 전 확인과 조회 안내">
        <article className="help-caution-card">
          <div>
            <HelpLandingIcon name="shield" />
            <h2>요청 전 확인사항</h2>
          </div>
          <ul>
            {checkItems.map((item) => (
              <li key={item}>{item}</li>
            ))}
          </ul>
        </article>

        <article className="help-lookup-card">
          <h2>요청번호와 연락처로 진행 상태를 확인하세요</h2>
          <p>요청 시 발급되는 요청번호와 입력한 연락처로 접수 상태와 운영자 안내를 확인할 수 있습니다.</p>
          <div className="help-lookup-preview" aria-hidden="true">
            <span>요청번호</span>
            <strong>24N-2505-0001</strong>
            <span>연락처</span>
            <strong>010-0000-0000</strong>
          </div>
          <button type="button" onClick={onLookupClick}>
            요청 내역 확인하기
          </button>
        </article>
      </section>

      <section className="help-stories" aria-labelledby="help-stories-title">
        <div className="help-section-title">
          <h2 id="help-stories-title">이용 후기</h2>
          <p>도움을 요청한 신청자들이 남긴 이야기를 소개합니다.</p>
        </div>
        <div className="help-story-grid">
          {gratitudeMessages.map((story) => (
            <article key={story.name}>
              <p>{story.message}</p>
              <footer>{story.name}</footer>
            </article>
          ))}
        </div>
      </section>

      <section className="help-final-cta" aria-labelledby="help-final-title">
        <div>
          <h2 id="help-final-title">필요한 상황을 남기면, 운영자가 확인 후 안내합니다</h2>
          <p>도움 가능 여부는 요청 내용, 일정, 지역, 운영 상황에 따라 달라질 수 있습니다.</p>
        </div>
        <div className="help-hero-actions">
          <button type="button" onClick={onRequestClick}>
            이사 도움 요청하기
          </button>
          <button type="button" onClick={onLookupClick}>
            요청 내역 확인하기
          </button>
        </div>
      </section>
    </section>
  )
}
