import { getToken, clearToken } from './authApi';

const BASE = '/api/routes';
const TIMEOUT_MS = 8000;

async function request(url, options = {}) {
  const token = getToken();
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), TIMEOUT_MS);
  try {
    const res = await fetch(url, {
      ...options,
      headers: {
        ...(options.headers ?? {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      signal: controller.signal,
    });
    if (res.status === 401) {
      clearToken();
      window.dispatchEvent(new Event('auth:expired'));
      throw new Error('Session expired. Please log in again.');
    }
    if (!res.ok) {
      const body = await res.json().catch(() => ({}));
      throw new Error(body.error ?? `Request failed: ${res.status}`);
    }
    if (res.status === 204) return null;
    return res.json();
  } catch (e) {
    if (e.name === 'AbortError') throw new Error('Request timed out');
    throw e;
  } finally {
    clearTimeout(timeout);
  }
}

// ── Routes ────────────────────────────────────────────────────────────────────

export function getRoutes() {
  return request(BASE);
}

export function createRoute(origin, destination, travelDate) {
  return request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ origin, destination, travelDate }),
  });
}

export function deleteRoute(routeId) {
  return request(`${BASE}/${routeId}`, { method: 'DELETE' });
}

export function setRouteStatus(routeId, status) {
  return request(`${BASE}/${routeId}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  });
}

export function getPriceSummary(routeId) {
  return request(`${BASE}/${routeId}/prices/summary`);
}

// ── Segments ──────────────────────────────────────────────────────────────────

export function getSegments(routeId) {
  return request(`${BASE}/${routeId}/segments`);
}

export function createSegment(routeId, transportType, label) {
  return request(`${BASE}/${routeId}/segments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ transportType, label: label || null }),
  });
}

export function deleteSegment(routeId, segmentId) {
  return request(`${BASE}/${routeId}/segments/${segmentId}`, { method: 'DELETE' });
}

// ── Prices ────────────────────────────────────────────────────────────────────

export function getPriceHistory(routeId, segmentId) {
  return request(`${BASE}/${routeId}/segments/${segmentId}/prices`);
}

export function addPrice(routeId, segmentId, price, currency, recordedDate) {
  return request(`${BASE}/${routeId}/segments/${segmentId}/prices`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ price, currency, recordedDate }),
  });
}

export function updatePrice(routeId, segmentId, priceId, price, currency, recordedDate) {
  return request(`${BASE}/${routeId}/segments/${segmentId}/prices/${priceId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ price, currency, recordedDate }),
  });
}

export function deletePriceRecord(routeId, segmentId, priceId) {
  return request(`${BASE}/${routeId}/segments/${segmentId}/prices/${priceId}`, { method: 'DELETE' });
}

export function togglePricePurchased(routeId, segmentId, priceId) {
  return request(`${BASE}/${routeId}/segments/${segmentId}/prices/${priceId}/purchase`, { method: 'PATCH' });
}
