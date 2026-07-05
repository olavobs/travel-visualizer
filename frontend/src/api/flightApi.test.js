import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { getRoutes, createRoute, deleteRoute, addPrice, deletePriceRecord } from './flightApi';

function mockFetch(status, body) {
  return vi.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(body),
  });
}

beforeEach(() => { vi.useFakeTimers(); });
afterEach(() => { vi.restoreAllMocks(); vi.useRealTimers(); });

describe('getRoutes', () => {
  it('fetches from /api/routes', async () => {
    const routes = [{ id: 1, origin: 'GRU', destination: 'LIS', travelDate: '2026-12-01' }];
    global.fetch = mockFetch(200, routes);

    const promise = getRoutes();
    await vi.runAllTimersAsync();
    const result = await promise;

    expect(fetch).toHaveBeenCalledWith('/api/routes', expect.objectContaining({ method: undefined }));
    expect(result).toEqual(routes);
  });

  it('throws with server error message on failure', async () => {
    global.fetch = mockFetch(404, { error: 'Not found' });

    const promise = getRoutes();
    await vi.runAllTimersAsync();

    await expect(promise).rejects.toThrow('Not found');
  });

  it('throws generic message when error body has no message', async () => {
    global.fetch = mockFetch(500, {});

    const promise = getRoutes();
    await vi.runAllTimersAsync();

    await expect(promise).rejects.toThrow('Request failed: 500');
  });
});

describe('createRoute', () => {
  it('sends POST with travelDate in JSON body', async () => {
    global.fetch = mockFetch(201, { id: 1 });

    const promise = createRoute('GRU', 'LIS', '2026-12-01');
    await vi.runAllTimersAsync();
    await promise;

    expect(fetch).toHaveBeenCalledWith('/api/routes', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ origin: 'GRU', destination: 'LIS', travelDate: '2026-12-01' }),
    }));
  });
});

describe('deleteRoute', () => {
  it('sends DELETE and returns null for 204', async () => {
    global.fetch = vi.fn().mockResolvedValue({ ok: true, status: 204, json: () => Promise.resolve(null) });

    const promise = deleteRoute(42);
    await vi.runAllTimersAsync();
    const result = await promise;

    expect(fetch).toHaveBeenCalledWith('/api/routes/42', expect.objectContaining({ method: 'DELETE' }));
    expect(result).toBeNull();
  });
});

describe('addPrice', () => {
  it('sends POST to segment prices endpoint', async () => {
    global.fetch = mockFetch(201, { id: 10 });

    const promise = addPrice(1, 5, 2950.00, 'BRL', '2026-04-10');
    await vi.runAllTimersAsync();
    await promise;

    expect(fetch).toHaveBeenCalledWith('/api/routes/1/segments/5/prices', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ price: 2950.00, currency: 'BRL', recordedDate: '2026-04-10' }),
    }));
  });
});

describe('deletePriceRecord', () => {
  it('sends DELETE to segment price record endpoint', async () => {
    global.fetch = vi.fn().mockResolvedValue({ ok: true, status: 204, json: () => Promise.resolve(null) });

    const promise = deletePriceRecord(1, 5, 10);
    await vi.runAllTimersAsync();
    await promise;

    expect(fetch).toHaveBeenCalledWith('/api/routes/1/segments/5/prices/10', expect.objectContaining({ method: 'DELETE' }));
  });
});
