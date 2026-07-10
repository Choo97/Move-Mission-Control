import { useEffect, useState } from 'react'
import {
  addOperatingHoliday,
  deleteOperatingHoliday,
  getEstimateSettings,
  getOperatingHolidays,
  getOperatingPolicy,
  getOperatingSchedules,
  updateEstimateSetting,
  updateOperatingPolicy,
  updateOperatingSchedule,
} from '../api/adminApi'
import { getErrorMessage } from '../api/apiError'
import type {
  EstimateSettingHistoryResponse,
  EstimateSettingResponse,
  OperatingHolidayResponse,
  OperatingPolicyResponse,
  OperatingScheduleResponse,
  ServiceMode,
} from '../types'

const serviceModeComparisonRows = [
  { label: '예약 버튼', general: '이사 예약하기', nonProfit: '이사 도움 요청하기' },
  { label: '사용자 명칭', general: '고객', nonProfit: '신청자' },
  { label: '견적 금액', general: '표시', nonProfit: '표시 안 함' },
  { label: '쿠폰/할인', general: '표시', nonProfit: '표시 안 함' },
  { label: '결제', general: '사용 안 함', nonProfit: '표시 안 함' },
  { label: '리뷰', general: '리뷰', nonProfit: '감사 메시지 또는 이용 후기' },
]

const serviceModeLabel = (serviceMode: ServiceMode) =>
  serviceMode === 'NON_PROFIT' ? '비영리 도움 요청' : '일반 이사 예약'

function ServiceModeGuide({ selectedMode }: { selectedMode: ServiceMode | null }) {
  return (
    <div className="service-mode-guide" aria-label="서비스 운영 방식별 고객 화면 적용 내용">
      <div className="service-mode-guide-heading">
        <strong>
          {selectedMode ? `현재 선택: ${serviceModeLabel(selectedMode)}` : '고객 화면 적용 내용'}
        </strong>
        <span>
          {selectedMode
            ? '저장하면 고객 화면에 아래 기준으로 적용됩니다.'
            : '운영 정책을 불러오면 현재 선택 모드가 강조됩니다.'}
        </span>
      </div>
      <div className="service-mode-table-wrap">
        <table className="service-mode-table">
          <thead>
            <tr>
              <th scope="col">항목</th>
              <th scope="col" className={selectedMode === 'GENERAL' ? 'selected' : ''}>일반 모드</th>
              <th scope="col" className={selectedMode === 'NON_PROFIT' ? 'selected' : ''}>비영리 모드</th>
            </tr>
          </thead>
          <tbody>
            {serviceModeComparisonRows.map((row) => (
              <tr key={row.label}>
                <th scope="row">{row.label}</th>
                <td className={selectedMode === 'GENERAL' ? 'selected' : ''}>{row.general}</td>
                <td className={selectedMode === 'NON_PROFIT' ? 'selected' : ''}>{row.nonProfit}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export function AdminAvailabilitySettings() {
  const [schedules, setSchedules] = useState<OperatingScheduleResponse[]>([])
  const [holidays, setHolidays] = useState<OperatingHolidayResponse[]>([])
  const [policy, setPolicy] = useState<OperatingPolicyResponse | null>(null)
  const [estimateSettings, setEstimateSettings] = useState<EstimateSettingResponse[]>([])
  const [estimateHistories, setEstimateHistories] = useState<EstimateSettingHistoryResponse[]>([])
  const [holidayDate, setHolidayDate] = useState('')
  const [holidayReason, setHolidayReason] = useState('')
  const [message, setMessage] = useState('')
  const [isPolicyLoading, setIsPolicyLoading] = useState(true)
  const [policyErrorMessage, setPolicyErrorMessage] = useState('')

  useEffect(() => {
    void Promise.allSettled([getOperatingSchedules(), getOperatingHolidays(), getOperatingPolicy(), getEstimateSettings()])
      .then(([scheduleResult, holidayResult, policyResult, estimateResult]) => {
        const failedMessages: string[] = []

        if (scheduleResult.status === 'fulfilled') {
          setSchedules(scheduleResult.value)
        } else {
          failedMessages.push(getErrorMessage(scheduleResult.reason, '요일별 운영시간을 불러오지 못했습니다.'))
        }

        if (holidayResult.status === 'fulfilled') {
          setHolidays(holidayResult.value)
        } else {
          failedMessages.push(getErrorMessage(holidayResult.reason, '휴무일을 불러오지 못했습니다.'))
        }

        if (policyResult.status === 'fulfilled') {
          setPolicy(policyResult.value)
          setPolicyErrorMessage('')
        } else {
          const policyError = getErrorMessage(policyResult.reason, '운영 정책을 불러오지 못했습니다.')
          setPolicyErrorMessage(policyError)
          failedMessages.push(policyError)
        }

        if (estimateResult.status === 'fulfilled') {
          setEstimateSettings(estimateResult.value.settings)
          setEstimateHistories(estimateResult.value.histories)
        } else {
          failedMessages.push(getErrorMessage(estimateResult.reason, '견적 정책을 불러오지 못했습니다.'))
        }

        if (failedMessages.length > 0) {
          setMessage([...new Set(failedMessages)].join(' '))
        }
        setIsPolicyLoading(false)
      })
      .catch((error) => {
        const fallbackMessage = getErrorMessage(error, '운영 일정을 불러오지 못했습니다.')
        setPolicyErrorMessage(fallbackMessage)
        setMessage(fallbackMessage)
        setIsPolicyLoading(false)
      })
  }, [])

  const changeSchedule = <K extends keyof OperatingScheduleResponse>(
    id: number,
    key: K,
    value: OperatingScheduleResponse[K],
  ) => setSchedules((current) => current.map((item) => (item.id === id ? { ...item, [key]: value } : item)))

  const saveSchedule = async (schedule: OperatingScheduleResponse) => {
    setMessage('')
    try {
      const updated = await updateOperatingSchedule(schedule.id, schedule)
      setSchedules((current) => current.map((item) => (item.id === updated.id ? updated : item)))
      setMessage(`${updated.dayLabel} 운영시간을 저장했습니다.`)
    } catch (error) {
      setMessage(getErrorMessage(error, '운영시간 저장에 실패했습니다.'))
    }
  }

  const changePolicy = <K extends keyof OperatingPolicyResponse>(key: K, value: OperatingPolicyResponse[K]) =>
    setPolicy((current) => (current ? { ...current, [key]: value } : current))

  const savePolicy = async () => {
    if (!policy) return
    setMessage('')
    try {
      const updated = await updateOperatingPolicy(policy)
      setPolicy(updated)
      setMessage('운영 정책을 저장했습니다.')
    } catch (error) {
      setMessage(getErrorMessage(error, '운영 정책 저장에 실패했습니다.'))
    }
  }

  const changeEstimateSetting = (id: number, amount: number) =>
    setEstimateSettings((current) => current.map((item) => (item.id === id ? { ...item, amount } : item)))

  const saveEstimateSetting = async (setting: EstimateSettingResponse) => {
    setMessage('')
    try {
      const updated = await updateEstimateSetting(setting.id, setting.amount)
      setEstimateSettings(updated.settings)
      setEstimateHistories(updated.histories)
      setMessage(`${setting.label} 값을 저장했습니다.`)
    } catch (error) {
      setMessage(getErrorMessage(error, '견적 정책 저장에 실패했습니다.'))
    }
  }

  const addHoliday = async () => {
    if (!holidayDate) return
    setMessage('')
    try {
      const added = await addOperatingHoliday(holidayDate, holidayReason)
      setHolidays((current) => [...current, added].sort((a, b) => a.holidayDate.localeCompare(b.holidayDate)))
      setHolidayDate('')
      setHolidayReason('')
      setMessage('휴무일을 등록했습니다.')
    } catch (error) {
      setMessage(getErrorMessage(error, '휴무일 등록에 실패했습니다.'))
    }
  }

  const removeHoliday = async (holidayId: number) => {
    setMessage('')
    try {
      await deleteOperatingHoliday(holidayId)
      setHolidays((current) => current.filter((holiday) => holiday.id !== holidayId))
      setMessage('휴무일을 삭제했습니다.')
    } catch (error) {
      setMessage(getErrorMessage(error, '휴무일 삭제에 실패했습니다.'))
    }
  }

  const formatEstimateValue = (amount: number, unit: string) =>
    unit === '원' ? `${amount.toLocaleString()}원` : `${amount}${unit}`

  return (
    <section className="admin-availability-settings">
      <div className="admin-section-heading">
        <div><p className="eyebrow">Operations Settings</p><h3>운영 설정</h3></div>
      </div>
      <div className="holiday-editor admin-service-mode-panel">
        <h4>서비스 운영 방식</h4>
        {policy ? (
          <>
            <div className="admin-service-mode-choice-grid" aria-label="서비스 운영 방식 선택">
              <label className={policy.serviceMode === 'GENERAL' ? 'selected' : ''}>
                <input
                  type="radio"
                  name="serviceMode"
                  value="GENERAL"
                  checked={policy.serviceMode === 'GENERAL'}
                  onChange={() => changePolicy('serviceMode', 'GENERAL')}
                />
                <span>
                  <strong>일반 이사 예약</strong>
                  <small>고객이 이사 서비스를 예약하고 견적을 확인하는 일반 모드입니다.</small>
                </span>
              </label>
              <label className={policy.serviceMode === 'NON_PROFIT' ? 'selected' : ''}>
                <input
                  type="radio"
                  name="serviceMode"
                  value="NON_PROFIT"
                  checked={policy.serviceMode === 'NON_PROFIT'}
                  onChange={() => changePolicy('serviceMode', 'NON_PROFIT')}
                />
                <span>
                  <strong>비영리 도움 요청</strong>
                  <small>비용 노출을 줄이고 도움 요청 중심으로 안내하는 모드입니다.</small>
                </span>
              </label>
            </div>
            <div className="holiday-form admin-policy-number-form">
              <label>
                예약 시작(일 후)
                <input type="number" min={0} max={30} value={policy.minAdvanceDays}
                  onChange={(event) => changePolicy('minAdvanceDays', Number(event.target.value))} />
              </label>
              <label>
                예약 종료(일 후)
                <input type="number" min={1} max={365} value={policy.maxAdvanceDays}
                  onChange={(event) => changePolicy('maxAdvanceDays', Number(event.target.value))} />
              </label>
              <label>
                하루 최대(건)
                <input type="number" min={1} max={50} value={policy.maxDailyReservations}
                  onChange={(event) => changePolicy('maxDailyReservations', Number(event.target.value))} />
              </label>
              <button type="button" onClick={() => void savePolicy()}>운영 정책 저장</button>
            </div>
            <ServiceModeGuide selectedMode={policy.serviceMode} />
          </>
        ) : (
          <>
            <p className="message info">
              {isPolicyLoading
                ? '운영 정책을 불러오는 중입니다.'
                : `${policyErrorMessage || '운영 정책을 불러오지 못했습니다.'} 관리자 로그인 상태와 백엔드 배포 상태를 확인해 주세요.`}
            </p>
            <ServiceModeGuide selectedMode={null} />
          </>
        )}
      </div>
      <div className="holiday-editor admin-estimate-policy-panel">
        <h4>견적 정책 설정</h4>
        <p>예약 접수와 거리 입력 시 자동 견적에 반영되는 기준값입니다.</p>
        <ul>
          {estimateSettings.map((setting) => (
            <li key={setting.id}>
              <strong>{setting.label}</strong>
              <span>{setting.settingKey}</span>
              <input type="number" min={0} step={1} value={setting.amount}
                onChange={(event) => changeEstimateSetting(setting.id, Number(event.target.value))} />
              <em>{setting.unit}</em>
              <button type="button" onClick={() => void saveEstimateSetting(setting)}>저장</button>
            </li>
          ))}
        </ul>
        {estimateHistories.length > 0 && (
          <>
            <h4>최근 견적 정책 변경 이력</h4>
            <ul>
              {estimateHistories.slice(0, 5).map((history) => (
                <li key={history.id}>
                  <strong>{history.label}</strong>
                  <span>
                    {formatEstimateValue(history.previousAmount, history.unit)}
                    {' -> '}
                    {formatEstimateValue(history.changedAmount, history.unit)}
                  </span>
                  <small>{history.changedBy ?? '기록 없음'}</small>
                </li>
              ))}
            </ul>
          </>
        )}
      </div>
      <div className="operating-schedule-list admin-schedule-panel">
        <h4>요일별 운영시간</h4>
        {schedules.map((schedule) => (
          <div className="operating-schedule-row" key={schedule.id}>
            <strong>{schedule.dayLabel}</strong>
            <label className="inline-check">
              <input type="checkbox" checked={schedule.open}
                onChange={(event) => changeSchedule(schedule.id, 'open', event.target.checked)} />영업
            </label>
            <input type="time" value={schedule.startTime.slice(0, 5)} disabled={!schedule.open}
              onChange={(event) => changeSchedule(schedule.id, 'startTime', event.target.value)} />
            <span>~</span>
            <input type="time" value={schedule.endTime.slice(0, 5)} disabled={!schedule.open}
              onChange={(event) => changeSchedule(schedule.id, 'endTime', event.target.value)} />
            <select value={schedule.slotMinutes} disabled={!schedule.open}
              onChange={(event) => changeSchedule(schedule.id, 'slotMinutes', Number(event.target.value))}>
              <option value={30}>30분</option><option value={60}>1시간</option><option value={120}>2시간</option>
            </select>
            <button type="button" onClick={() => void saveSchedule(schedule)}>저장</button>
          </div>
        ))}
      </div>
      <div className="holiday-editor admin-holiday-panel">
        <h4>휴무일 관리</h4>
        <div className="holiday-form">
          <input type="date" value={holidayDate} min={new Date().toISOString().slice(0, 10)}
            onChange={(event) => setHolidayDate(event.target.value)} />
          <input value={holidayReason} maxLength={100} placeholder="휴무 사유"
            onChange={(event) => setHolidayReason(event.target.value)} />
          <button type="button" disabled={!holidayDate} onClick={() => void addHoliday()}>추가</button>
        </div>
        <ul>
          {holidays.map((holiday) => (
            <li key={holiday.id}>
              <strong>{holiday.holidayDate}</strong><span>{holiday.reason}</span>
              <button type="button" onClick={() => void removeHoliday(holiday.id)}>삭제</button>
            </li>
          ))}
        </ul>
      </div>
      {message && <p className="message">{message}</p>}
    </section>
  )
}
