import { useState, useEffect, useCallback } from 'react';
import RouteList from './components/RouteList';
import RouteDetail from './components/RouteDetail';
import CreateRouteModal from './components/CreateRouteModal';
import FlightTree from './components/FlightTree';
import AuthForm from './components/AuthForm';
import { getRoutes, deleteRoute } from './api/flightApi';
import { getToken, clearToken } from './api/authApi';
import { LanguageProvider, useT, useLang } from './i18n/LanguageContext';
import './App.css';

function AppInner() {
  const t = useT();
  const { lang, switchLang } = useLang();
  const [authenticated, setAuthenticated] = useState(() => !!getToken());
  const [routes, setRoutes]               = useState([]);
  const [selectedRoute, setSelectedRoute] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [view, setView]                   = useState('list');
  const [error, setError]                 = useState(null);

  useEffect(() => {
    const handleExpired = () => setAuthenticated(false);
    window.addEventListener('auth:expired', handleExpired);
    return () => window.removeEventListener('auth:expired', handleExpired);
  }, []);

  const loadRoutes = useCallback(async () => {
    try {
      setError(null);
      setRoutes(await getRoutes());
    } catch (e) {
      setError(e.message);
    }
  }, []);

  useEffect(() => {
    if (authenticated) loadRoutes();
  }, [authenticated, loadRoutes]);

  if (!authenticated) {
    return <AuthForm onAuthenticated={() => setAuthenticated(true)} />;
  }

  const switchView = (v) => {
    setView(v);
    setSelectedRoute(null);
  };

  const handleDeleteRoute = async (route) => {
    try {
      await deleteRoute(route.id);
      if (selectedRoute?.id === route.id) setSelectedRoute(null);
      await loadRoutes();
    } catch (e) {
      setError(e.message);
    }
  };

  const handleLogout = () => {
    clearToken();
    setAuthenticated(false);
  };

  return (
    <div className="app">
      <header className="header">
        <h1>{t('app.title')}</h1>
        <div className="header-actions">
          <button className="btn btn-lang" onClick={() => switchLang(lang === 'en' ? 'pt' : 'en')}>
            {lang === 'en' ? 'PT' : 'EN'}
          </button>
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
            {t('app.newRoute')}
          </button>
          <button className="btn btn-secondary" onClick={handleLogout}>
            {t('app.logout')}
          </button>
        </div>
      </header>

      {error && <div className="error-banner">{error}</div>}

      <div className="tabs">
        <button
          className={`tab ${view === 'list' ? 'tab--active' : ''}`}
          onClick={() => switchView('list')}
        >
          {t('app.routes')}
        </button>
        <button
          className={`tab ${view === 'tree' ? 'tab--active' : ''}`}
          onClick={() => switchView('tree')}
        >
          {t('app.journeyTree')}
        </button>
      </div>

      <main>
        {view === 'list' ? (
          selectedRoute ? (
            <RouteDetail
              route={selectedRoute}
              onBack={() => setSelectedRoute(null)}
              onRouteChange={(updated) => {
                setSelectedRoute(updated);
                setRoutes(prev => prev.map(r => r.id === updated.id ? updated : r));
              }}
            />
          ) : (
            <RouteList routes={routes} onSelect={setSelectedRoute} onDelete={handleDeleteRoute} />
          )
        ) : (
          <FlightTree routes={routes} />
        )}
      </main>

      {showCreateModal && (
        <CreateRouteModal
          onClose={() => setShowCreateModal(false)}
          onCreated={() => {
            setShowCreateModal(false);
            loadRoutes();
          }}
        />
      )}
    </div>
  );
}

export default function App() {
  return (
    <LanguageProvider>
      <AppInner />
    </LanguageProvider>
  );
}
