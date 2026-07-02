export type EstimateSettingResponse = {
  id: number
  settingKey: string
  label: string
  amount: number
  unit: string
}

export type EstimateSettingHistoryResponse = {
  id: number
  settingKey: string
  label: string
  previousAmount: number
  changedAmount: number
  unit: string
  changedBy: string | null
  changedAt: string
}

export type EstimateSettingPageResponse = {
  settings: EstimateSettingResponse[]
  histories: EstimateSettingHistoryResponse[]
}
