import { useState } from 'react';
import { createRoute } from '../api/flightApi';
import { useT } from '../i18n/LanguageContext';
import AirportInput from './AirportInput';

export default function CreateRouteModal({ onClose, onCreated }) {
  const t = useT();
  const [origin, setOrigin]           = useState('');
  const [destination, setDestination] = useState('');
  const [travelDate, setTravelDate]   = useState('');
  const [error, setError]             = useState(null);
  const [submitting, setSubmitting]   = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!origin)      { setError(t('createRoute.invalidOrigin'));      return; }
    if (!destination) { setError(t('createRoute.invalidDestination')); return; }
    setSubmitting(true);
    setError(null);
    try {
      await createRoute(origin, destination, travelDate);
      onCreated();
    } catch (e) {
      setError(e.message);
      setSubmitting(false);
    }
  };

  return (
    <div className="cr-overlay" onClick={onClose}>
      <div className="cr-card" onClick={e => e.stopPropagation()}>

        {error && <div className="error-banner">{error}</div>}

        <form onSubmit={handleSubmit} className="cr-form">
          <div className="cr-row">
            <div className="cr-field">
              <label className="cr-label">{t('createRoute.origin')}</label>
              <AirportInput
                value={origin}
                onChange={setOrigin}
                placeholder="GRU"
                autoFocus
              />
            </div>
            <div className="cr-field">
              <label className="cr-label">{t('createRoute.destination')}</label>
              <AirportInput
                value={destination}
                onChange={setDestination}
                placeholder="LIS"
              />
            </div>
          </div>

          <div className="cr-field">
            <label className="cr-label">{t('createRoute.travelDate')}</label>
            <input
              className="cr-input"
              type="date" value={travelDate}
              onChange={e => setTravelDate(e.target.value)} required
            />
          </div>

          <button type="submit" className="cr-submit" disabled={submitting}>
            {submitting ? t('createRoute.creating') : t('createRoute.create')}
          </button>
        </form>
      </div>
    </div>
  );
}
