import React from 'react';

interface StatusCardProps {
  title: string;
  status: 'success' | 'warning' | 'error' | 'info';
  icon: React.ReactNode;
  value: string;
  description?: string;
  meta?: Record<string, string | number | undefined>;
}

export const StatusCard: React.FC<StatusCardProps> = ({
  title,
  status,
  icon,
  value,
  description,
  meta,
}) => {
  return (
    <div className={`status-card status-${status}`}>
      <div className="status-card-header">
        <span className="status-card-icon">{icon}</span>
        <h3 className="status-card-title">{title}</h3>
      </div>
      <div className="status-card-value">{value}</div>
      {description && <p className="status-card-desc">{description}</p>}
      {meta && (
        <div className="status-card-meta">
          {Object.entries(meta).map(
            ([k, v]) =>
              v !== undefined && (
                <div key={k} className="meta-row">
                  <span className="meta-key">{k}:</span>
                  <span className="meta-val">{v}</span>
                </div>
              )
          )}
        </div>
      )}
    </div>
  );
};
