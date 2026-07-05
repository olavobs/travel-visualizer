import { useState } from 'react';
import { createRoute } from '../api/flightApi';
import { useT } from '../i18n/LanguageContext';

const IATA_RE = /^[A-Za-z]{3}$/;

export default function CreateRouteModal({ onClose, onCreated }) {
  const t = useT();
  const [origin, setOrigin]           = useState('');
  const [destination, setDestination] = useState('');
  const [travelDate, setTravelDate]   = useState('');
  const [error, setError]             = useState(null);
  const [submitting, setSubmitting]   = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!IATA_RE.test(origin)) {
      setError(t('createRoute.invalidOrigin'));
      return;
    }
    if (!IATA_RE.test(destination)) {
      setError(t('createRoute.invalidDestination'));
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await createRoute(origin.toUpperCase(), destination.toUpperCase(), travelDate);
      onCreated();
    } catch (e) {
      setError(e.message);
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <h2>{t('createRoute.title')}</h2>

        {error && <div className="error-banner">{error}</div>}

        <form onSubmit={handleSubmit}>
          <label>
            {t('createRoute.origin')}
            <input
              type="text" placeholder="REC"
              value={origin} onChange={e => setOrigin(e.target.value)}
              maxLength={3} required autoFocus
            />
          </label>

          <label>
            {t('createRoute.destination')}
            <input
              type="text" placeholder="LIS"
              value={destination} onChange={e => setDestination(e.target.value)}
              maxLength={3} required
            />
          </label>

          <label>
            {t('createRoute.travelDate')}
            <input
              type="date" value={travelDate}
              onChange={e => setTravelDate(e.target.value)} required
            />
          </label>

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              {t('createRoute.cancel')}
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? t('createRoute.creating') : t('createRoute.create')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
