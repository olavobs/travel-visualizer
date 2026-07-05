import { useState } from 'react';
import { login, register } from '../api/authApi';
import { useT, useLang } from '../i18n/LanguageContext';

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
      setError(err.message);
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
          {error && <p className="auth-error">{error}</p>}

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
