import { useState } from 'react';
import { login, register } from '../api/authApi';
import { useT, useLang } from '../i18n/LanguageContext';

export function friendlyError(raw, mode, t) {
  const msg = (raw ?? '').toLowerCase();

  if (msg.includes('timed out') || msg.includes('network error') || msg.includes('failed to fetch'))
    return t('auth.errConnection');
  if (msg.includes('invalid credentials') || msg.includes('unauthorized') || msg.includes(': 401'))
    return t('auth.errInvalidCredentials');
  if (msg.includes('already') || msg.includes('conflict') || msg.includes(': 409'))
    return t('auth.errEmailTaken');
  if (msg.includes('password') && (msg.includes('8') || msg.includes('least')))
    return t('auth.errPasswordTooShort');
  if (msg.includes(': 403'))
    return t('auth.errServer');
  if (msg.includes(': 502') || msg.includes(': 503') || msg.includes('networkerror'))
    return t('auth.errUnreachable');
  if (msg.includes(': 5'))
    return t('auth.errServer');
  if (mode === 'login')
    return t('auth.errInvalidCredentials');
  return raw;
}

export default function AuthForm({ onAuthenticated }) {
  const t = useT();
  const { lang, switchLang } = useLang();
  const [mode, setMode]         = useState('login');
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState(null);
  const [loading, setLoading]   = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      if (mode === 'login') {
        await login(email, password);
      } else {
        await register(email, password);
      }
      onAuthenticated();
    } catch (err) {
      setError(friendlyError(err.message, mode, t));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-lang-toggle">
          <button className="btn btn-lang" onClick={() => switchLang(lang === 'en' ? 'pt' : 'en')}>
            {lang === 'en' ? 'PT' : 'EN'}
          </button>
        </div>
        <h1 className="auth-title">{t('auth.title')}</h1>
        <div className="auth-tabs">
          <button
            className={`auth-tab ${mode === 'login' ? 'auth-tab--active' : ''}`}
            onClick={() => { setMode('login'); setError(null); }}
            type="button"
          >
            {t('auth.login')}
          </button>
          <button
            className={`auth-tab ${mode === 'register' ? 'auth-tab--active' : ''}`}
            onClick={() => { setMode('register'); setError(null); }}
            type="button"
          >
            {t('auth.register')}
          </button>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {error && (
            <div className="auth-error" role="alert">
              <span className="auth-error__icon">⚠</span>
              <span className="auth-error__text">{error}</span>
              <button
                type="button"
                className="auth-error__dismiss"
                aria-label="Dismiss"
                onClick={() => setError(null)}
              >×</button>
            </div>
          )}

          <label className="auth-label">
            {t('auth.email')}
            <input
              className="auth-input"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
              placeholder="you@example.com"
            />
          </label>

          <label className="auth-label">
            {t('auth.password')}
            <input
              className="auth-input"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={8}
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              placeholder={mode === 'register' ? t('auth.minPassword') : ''}
            />
          </label>

          <button className="btn btn-primary auth-submit" type="submit" disabled={loading}>
            {loading ? t('auth.loading') : mode === 'login' ? t('auth.login') : t('auth.createAccount')}
          </button>
        </form>
      </div>
    </div>
  );
}
