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
  it('fetches from /v1/routes', async () => {
    const routes = [{ id: 1, origin: 'GRU', destination: 'LIS', travelDate: '2026-12-01' }];
    global.fetch = mockFetch(200, routes);

    const promise = getRoutes();
    await vi.runAllTimersAsync();
    const result = await promise;

    expect(fetch).toHaveBeenCalledWith('/v1/routes', expect.any(Object));
    expect(result).toEqual(routes);
  });

  it('throws with server error message on failure', async () => {
    global.fetch = mockFetch(404, { error: 'Not found' });

    const promise = getRoutes();
    const assertion = expect(promise).rejects.toThrow('Not found');
    await vi.runAllTimersAsync();
    await assertion;
  });

  it('throws generic message when error body has no message', async () => {
    global.fetch = mockFetch(500, {});

    const promise = getRoutes();
    const assertion = expect(promise).rejects.toThrow('Request failed: 500');
    await vi.runAllTimersAsync();
    await assertion;
  });
});

describe('createRoute', () => {
  it('sends POST with travelDate in JSON body', async () => {
    global.fetch = mockFetch(201, { id: 1 });

    const promise = createRoute('GRU', 'LIS', '2026-12-01');
    await vi.runAllTimersAsync();
    await promise;

    expect(fetch).toHaveBeenCalledWith('/v1/routes', expect.objectContaining({
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

    expect(fetch).toHaveBeenCalledWith('/v1/routes/42', expect.objectContaining({ method: 'DELETE' }));
    expect(result).toBeNull();
  });
});

describe('addPrice', () => {
  it('sends POST with nested money and ISO instant to segment prices endpoint', async () => {
    global.fetch = mockFetch(201, { id: 10 });

    const promise = addPrice(1, 5, 2950.00, 'BRL', '2026-04-10');
    await vi.runAllTimersAsync();
    await promise;

    expect(fetch).toHaveBeenCalledWith('/v1/routes/1/segments/5/prices', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ money: { amount: 2950.00, currency: 'BRL' }, recordedAt: '2026-04-10T00:00:00Z' }),
    }));
  });
});

describe('deletePriceRecord', () => {
  it('sends DELETE to segment price record endpoint', async () => {
    global.fetch = vi.fn().mockResolvedValue({ ok: true, status: 204, json: () => Promise.resolve(null) });

    const promise = deletePriceRecord(1, 5, 10);
    await vi.runAllTimersAsync();
    await promise;

    expect(fetch).toHaveBeenCalledWith('/v1/routes/1/segments/5/prices/10', expect.objectContaining({ method: 'DELETE' }));
  });
});
