import { useState, useEffect, useCallback, useMemo } from 'react';
import {
  getSegments, createSegment, deleteSegment,
  getPriceHistory, addPrice, updatePrice, deletePriceRecord,
  togglePricePurchased, setRouteStatus,
} from '../api/flightApi';
import { CURRENCIES, formatPrice } from '../utils/currency';
import { useT } from '../i18n/LanguageContext';
import {
  ResponsiveContainer, LineChart, Line, XAxis, YAxis,
  CartesianGrid, Tooltip, Legend,
} from 'recharts';

const STATUSES = ['WATCHING', 'BOOKED', 'CANCELLED'];
const STATUS_BADGE_CLASS = { WATCHING: 'badge--watching', BOOKED: 'badge--booked', CANCELLED: 'badge--cancelled' };

const TRANSPORT_ICONS = {
  FLIGHT: '✈',
  BUS:    '🚌',
  CAR:    '🚗',
  BOAT:   '🚢',
  OTHER:  '✏️',
};

const TRANSPORT_TYPES = ['FLIGHT', 'BUS', 'CAR', 'BOAT', 'OTHER'];

const today = () => new Date().toISOString().split('T')[0];

const CHART_COLORS = ['#2563eb', '#16a34a', '#dc2626', '#d97706', '#7c3aed'];

function PurchasedDot({ cx, cy, payload, stroke }) {
  if (cx == null || cy == null) return null;
  if (payload.purchased)
    return <circle cx={cx} cy={cy} r={6} fill="#16a34a" stroke="#fff" strokeWidth={2} />;
  return <circle cx={cx} cy={cy} r={3} fill={stroke} />;
}

function PriceChart({ history, t }) {
  const { currencies, chartData } = useMemo(() => {
    const sorted = [...history].sort((a, b) => a.recordedDate.localeCompare(b.recordedDate));
    const currencySet = [...new Set(sorted.map(r => r.currency))];
    const byDate = {};
    for (const r of sorted) {
      if (!byDate[r.recordedDate]) byDate[r.recordedDate] = { date: r.recordedDate };
      byDate[r.recordedDate][r.currency] = Number(r.price);
      if (r.purchased) byDate[r.recordedDate].purchased = true;
    }
    return { currencies: currencySet, chartData: Object.values(byDate) };
  }, [history]);

  if (chartData.length < 2) return null;

  return (
    <div className="price-chart">
      <p className="price-chart__title">{t('routeDetail.chartTitle')}</p>
      <ResponsiveContainer width="100%" height={200}>
        <LineChart data={chartData} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
          <XAxis dataKey="date" tick={{ fontSize: 11 }} tickLine={false} />
          <YAxis tick={{ fontSize: 11 }} tickLine={false} axisLine={false} width={60}
            tickFormatter={v => v.toLocaleString()} />
          <Tooltip
            formatter={(value, name) => [value.toLocaleString(), name]}
            contentStyle={{ fontSize: 12, borderRadius: 8, border: '1px solid #e2e8f0' }}
          />
          {currencies.length > 1 && <Legend iconSize={10} wrapperStyle={{ fontSize: 12 }} />}
          {currencies.map((currency, i) => (
            <Line
              key={currency}
              type="monotone"
              dataKey={currency}
              stroke={CHART_COLORS[i % CHART_COLORS.length]}
              strokeWidth={2}
              dot={<PurchasedDot />}
              activeDot={{ r: 5 }}
              connectNulls
            />
          ))}
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

function SegmentSection({ routeId, segment, onDeleted, onPurchased }) {
  const t = useT();
  const [history, setHistory]           = useState([]);
  const [open, setOpen]                 = useState(true);
  const [price, setPrice]               = useState('');
  const [currency, setCurrency]         = useState('BRL');
  const [recordedDate, setRecordedDate] = useState(today());
  const [error, setError]               = useState(null);
  const [submitting, setSubmitting]     = useState(false);
  const [deletingId, setDeletingId]     = useState(null);
  const [togglingId, setTogglingId] = useState(null);
  const [editingId, setEditingId]       = useState(null);
  const [editFields, setEditFields]     = useState({});
  const [deletingSegment, setDeletingSegment] = useState(false);

  const loadHistory = useCallback(async () => {
    try {
      setError(null);
      setHistory(await getPriceHistory(routeId, segment.id));
    } catch (e) {
      setError(e.message);
    }
  }, [routeId, segment.id]);

  useEffect(() => { loadHistory(); }, [loadHistory]);

  const handleAddPrice = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await addPrice(routeId, segment.id, parseFloat(price), currency, recordedDate);
      setPrice('');
      setRecordedDate(today());
      await loadHistory();
    } catch (e) {
      setError(e.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpdatePrice = async (priceId) => {
    setSubmitting(true);
    try {
      await updatePrice(routeId, segment.id, priceId,
        parseFloat(editFields.price), editFields.currency, editFields.recordedDate);
      setEditingId(null);
      setEditFields({});
      await loadHistory();
    } catch (e) {
      setError(e.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeletePrice = async (priceId) => {
    setDeletingId(priceId);
    try {
      await deletePriceRecord(routeId, segment.id, priceId);
      await loadHistory();
    } catch (e) {
      setError(e.message);
    } finally {
      setDeletingId(null);
    }
  };

  const handleTogglePurchased = async (priceId, wasPurchased) => {
    setTogglingId(priceId);
    try {
      await togglePricePurchased(routeId, segment.id, priceId);
      await loadHistory();
      if (!wasPurchased) onPurchased?.();
    } catch (e) {
      setError(e.message);
    } finally {
      setTogglingId(null);
    }
  };

  const handleDeleteSegment = async () => {
    if (!confirm(t('routeDetail.deleteSegmentConfirm', { type: segment.transportType }))) return;
    setDeletingSegment(true);
    try {
      await deleteSegment(routeId, segment.id);
      onDeleted(segment.id);
    } catch (e) {
      setError(e.message);
      setDeletingSegment(false);
    }
  };

  const icon = TRANSPORT_ICONS[segment.transportType] ?? '✏️';
  const title = segment.label
    ? `${icon} ${segment.label}`
    : `${icon} ${segment.transportType}`;

  return (
    <div className="segment-section">
      <div className="segment-header" onClick={() => setOpen(o => !o)}>
        <span className="segment-title">{title}</span>
        <div className="segment-header-actions" onClick={e => e.stopPropagation()}>
          <button
            className="btn btn-danger btn-sm"
            onClick={handleDeleteSegment}
            disabled={deletingSegment}
          >
            {deletingSegment ? '…' : t('routeDetail.remove')}
          </button>
          <span className="segment-toggle">{open ? '▲' : '▼'}</span>
        </div>
      </div>

      {open && (
        <div className="segment-body">
          {error && <div className="error-banner">{error}</div>}

          <form className="add-price-form" onSubmit={handleAddPrice}>
            <div className="form-row">
              <input
                type="number" step="0.01" min="0.01"
                placeholder={t('routeDetail.pricePlaceholder')}
                value={price}
                onChange={e => setPrice(e.target.value)}
                required
              />
              <select
                className="currency-select"
                value={currency}
                onChange={e => setCurrency(e.target.value)}
              >
                {CURRENCIES.map(c => (
                  <option key={c.code} value={c.code}>{c.label}</option>
                ))}
              </select>
              <input
                type="date" value={recordedDate}
                onChange={e => setRecordedDate(e.target.value)}
                required
              />
              <button className="btn btn-primary" type="submit" disabled={submitting}>
                {submitting ? t('routeDetail.addingPrice') : t('routeDetail.addPrice')}
              </button>
            </div>
          </form>

          <PriceChart history={history} t={t} />

          {history.length === 0 ? (
            <p className="empty-state">{t('routeDetail.noPrices')}</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>{t('routeDetail.colDateSeen')}</th>
                  <th>{t('routeDetail.colPrice')}</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {history.map(record => (
                  <tr key={record.id} className={[
                    editingId === record.id ? 'row--editing' : '',
                    record.purchased ? 'row--purchased' : '',
                  ].filter(Boolean).join(' ') || undefined}>
                    {editingId === record.id ? (
                      <td colSpan={3}>
                        <div className="edit-row">
                          <input
                            className="table-input edit-row__date" type="date"
                            value={editFields.recordedDate}
                            onChange={e => setEditFields(f => ({ ...f, recordedDate: e.target.value }))}
                          />
                          <input
                            className="table-input edit-row__price" type="number" step="0.01" min="0.01"
                            value={editFields.price}
                            onChange={e => setEditFields(f => ({ ...f, price: e.target.value }))}
                          />
                          <select
                            className="table-input edit-row__currency"
                            value={editFields.currency}
                            onChange={e => setEditFields(f => ({ ...f, currency: e.target.value }))}
                          >
                            {CURRENCIES.map(c => (
                              <option key={c.code} value={c.code}>{c.code}</option>
                            ))}
                          </select>
                          <div className="edit-row__actions">
                            <button className="btn btn-primary" onClick={() => handleUpdatePrice(record.id)} disabled={submitting}>
                              {submitting ? '…' : t('routeDetail.save')}
                            </button>
                            <button className="btn btn-secondary" onClick={() => { setEditingId(null); setEditFields({}); }} disabled={submitting}>
                              {t('routeDetail.cancel')}
                            </button>
                          </div>
                        </div>
                      </td>
                    ) : (
                      <>
                        <td>{record.recordedDate}</td>
                        <td>{formatPrice(record.price, record.currency)}</td>
                        <td className="action-cell">
                          <button
                            className={`btn btn-purchase btn-sm${record.purchased ? ' btn-purchase--active' : ''}`}
                            onClick={() => handleTogglePurchased(record.id, record.purchased)}
                            disabled={togglingId === record.id}
                            title={record.purchased ? t('routeDetail.unmarkTitle') : undefined}
                          >
                            {togglingId === record.id ? '…' : record.purchased ? t('routeDetail.purchasedBadge') : t('routeDetail.bought')}
                          </button>
                          <button className="btn btn-secondary btn-sm" onClick={() => {
                            setEditingId(record.id);
                            setEditFields({ price: record.price, currency: record.currency, recordedDate: record.recordedDate });
                          }}>{t('routeDetail.edit')}</button>
                          <button
                            className="btn btn-danger btn-sm"
                            onClick={() => handleDeletePrice(record.id)}
                            disabled={deletingId === record.id}
                          >
                            {deletingId === record.id ? '…' : t('routeDetail.delete')}
                          </button>
                        </td>
                      </>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}

export default function RouteDetail({ route, onBack, onRouteChange }) {
  const t = useT();
  const [currentStatus, setCurrentStatus] = useState(route.status ?? 'WATCHING');
  const [changingStatus, setChangingStatus] = useState(false);
  const [segments, setSegments]           = useState([]);
  const [showAddSegment, setShowAddSegment] = useState(false);
  const [newType, setNewType]             = useState('FLIGHT');
  const [newLabel, setNewLabel]           = useState('');
  const [error, setError]                 = useState(null);
  const [addingSegment, setAddingSegment] = useState(false);

  const STATUS_LABELS = {
    WATCHING:  t('routeDetail.statusWatching'),
    BOOKED:    t('routeDetail.statusPurchased'),
    CANCELLED: t('routeDetail.statusCancelled'),
  };

  const loadSegments = useCallback(async () => {
    try {
      setError(null);
      setSegments(await getSegments(route.id));
    } catch (e) {
      setError(e.message);
    }
  }, [route.id]);

  useEffect(() => { loadSegments(); }, [loadSegments]);

  const handleStatusChange = async (newStatus) => {
    if (newStatus === currentStatus) return;
    setChangingStatus(true);
    try {
      const updated = await setRouteStatus(route.id, newStatus);
      setCurrentStatus(updated.status);
      onRouteChange?.(updated);
    } catch (e) {
      setError(e.message);
    } finally {
      setChangingStatus(false);
    }
  };

  const handlePurchased = useCallback(() => {
    if (currentStatus !== 'BOOKED') {
      setCurrentStatus('BOOKED');
      onRouteChange?.({ ...route, status: 'BOOKED' });
    }
  }, [currentStatus, route, onRouteChange]);

  const handleAddSegment = async (e) => {
    e.preventDefault();
    setAddingSegment(true);
    try {
      const seg = await createSegment(route.id, newType, newLabel || null);
      setSegments(prev => [...prev, seg]);
      setNewType('FLIGHT');
      setNewLabel('');
      setShowAddSegment(false);
    } catch (e) {
      setError(e.message);
    } finally {
      setAddingSegment(false);
    }
  };

  const handleSegmentDeleted = (segId) => {
    setSegments(prev => prev.filter(s => s.id !== segId));
  };

  return (
    <div className="route-detail">
      <button className="btn-back" onClick={onBack}>{t('routeDetail.back')}</button>

      <div className="route-title-row">
        <div>
          <h2 className="route-title">
            {route.origin} → {route.destination}
            <span className="flight-date">{route.travelDate}</span>
          </h2>
          <div className="route-status-row">
            <span className="muted">{t('routeDetail.statusLabel')}</span>
            {STATUSES.map(s => (
              <button
                key={s}
                className={`btn btn-sm ${currentStatus === s ? `badge ${STATUS_BADGE_CLASS[s]}` : 'btn-ghost'}`}
                onClick={() => handleStatusChange(s)}
                disabled={changingStatus}
              >
                {STATUS_LABELS[s]}
              </button>
            ))}
          </div>
        </div>
        <button className="btn btn-primary" onClick={() => setShowAddSegment(s => !s)}>
          {t('routeDetail.addSegment')}
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {showAddSegment && (
        <form className="add-segment-form" onSubmit={handleAddSegment}>
          <select value={newType} onChange={e => setNewType(e.target.value)}>
            {TRANSPORT_TYPES.map(tp => (
              <option key={tp} value={tp}>{TRANSPORT_ICONS[tp]} {tp}</option>
            ))}
          </select>
          <input
            type="text"
            placeholder={t('routeDetail.labelPlaceholder')}
            value={newLabel}
            onChange={e => setNewLabel(e.target.value)}
            maxLength={100}
          />
          <button className="btn btn-primary" type="submit" disabled={addingSegment}>
            {addingSegment ? t('routeDetail.adding') : t('routeDetail.add')}
          </button>
          <button className="btn btn-secondary" type="button" onClick={() => setShowAddSegment(false)}>
            {t('routeDetail.cancel')}
          </button>
        </form>
      )}

      {segments.length === 0 ? (
        <p className="empty-state">{t('routeDetail.noSegments')}</p>
      ) : (
        segments.map(seg => (
          <SegmentSection
            key={seg.id}
            routeId={route.id}
            segment={seg}
            onDeleted={handleSegmentDeleted}
            onPurchased={handlePurchased}
          />
        ))
      )}
    </div>
  );
}
