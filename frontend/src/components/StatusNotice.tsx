type StatusNoticeProps = {
  title: string
  description: string
  actions?: string[]
  tone?: 'neutral' | 'info'
  className?: string
}

export function StatusNotice({
  title,
  description,
  actions = [],
  tone = 'neutral',
  className = '',
}: StatusNoticeProps) {
  const classNames = ['status-notice', `status-notice-${tone}`, className].filter(Boolean).join(' ')

  return (
    <div className={classNames} role="status">
      <strong>{title}</strong>
      <p>{description}</p>
      {actions.length > 0 && (
        <ul>
          {actions.map((action) => (
            <li key={action}>{action}</li>
          ))}
        </ul>
      )}
    </div>
  )
}
