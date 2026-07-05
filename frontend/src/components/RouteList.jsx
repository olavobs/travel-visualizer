import { useState } from 'react';
import { useT } from '../i18n/LanguageContext';

const STATUS_BADGE_CLASS = {
  WATCHING:  'badge--watching',
  BOOKED:    'badge--booked',
  CANCELLED: 'badge--cancelled',
};

export default function RouteList({ routes, onSelect, onDelete }) {
  const t = useT();
  const [filter, setFilter] = useState('All');

  const FILTERS = [
    { key: 'All',       label: t('routeList.filterAll'),       status: null },
    { key: 'Watching',  label: t('routeList.filterWatching'),  status: 'WATCHING' },
    { key: 'Purchased', label: t('routeList.filterPurchased'), status: 'BOOKED' },
    { key: 'Cancelled', label: t('routeList.filterCancelled'), status: 'CANCELLED' },
  ];

  const STATUS_LABELS = {
    WATCHING:  t('routeList.statusWatching'),
    BOOKED:    t('routeList.statusPurchased'),
    CANCELLED: t('routeList.statusCancelled'),
  };

  const activeFilter = FILTERS.find(f => f.key === filter);
  const visible = activeFilter?.status
    ? routes.filter(r => r.status === activeFilter.status)
    : routes;

  const countFor = (f) =>
    f.status ? routes.filter(r => r.status === f.status).length : routes.length;

  return (
    <div>
      <div className="filter-tabs">
        {FILTERS.map(f => (
          <button
            key={f.key}
            className={`filter-tab ${filter === f.key ? 'filter-tab--active' : ''}`}
            onClick={() => setFilter(f.key)}
          >
            {f.label}
            <span className="filter-count">{countFor(f)}</span>
          </button>
        ))}
      </div>

      {visible.length === 0 ? (
        <p className="empty-state">
          {filter === 'All'
            ? t('routeList.emptyAll')
            : t('routeList.emptyFiltered', { filter: activeFilter?.label?.toLowerCase() ?? '' })}
        </p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>{t('routeList.colRoute')}</th>
              <th>{t('routeList.colDate')}</th>
              <th>{t('routeList.colStatus')}</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {visible.map(route => (
              <tr key={route.id}>
                <td>
                  <span className="airport">{route.origin}</span>
                  <span className="arrow">→</span>
                  <span className="airport">{route.destination}</span>
                </td>
                <td>{route.travelDate}</td>
                <td>
                  <span className={`badge ${STATUS_BADGE_CLASS[route.status] ?? 'badge--watching'}`}>
                    {STATUS_LABELS[route.status] ?? route.status}
                  </span>
                </td>
                <td className="row-actions">
                  <button className="btn btn-secondary" onClick={() => onSelect(route)}>
                    {t('routeList.viewDetails')}
                  </button>
                  <button className="btn btn-danger" onClick={() => onDelete(route)}>
                    {t('routeList.delete')}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
