const TOKEN_KEY = 'fpm_token';
const TIMEOUT_MS = 8000;

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

async function authRequest(url, body) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), TIMEOUT_MS);
  try {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
      signal: controller.signal,
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.message ?? data.error ?? `Request failed: ${res.status}`);
    return data;
  } catch (e) {
    if (e.name === 'AbortError') throw new Error('Request timed out');
    throw e;
  } finally {
    clearTimeout(timeout);
  }
}

export async function register(email, password) {
  const data = await authRequest('/api/auth/register', { email, password });
  setToken(data.token);
  return data;
}

export async function login(email, password) {
  const data = await authRequest('/api/auth/login', { email, password });
  setToken(data.token);
  return data;
}
