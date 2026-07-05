import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { SegmentSection } from './RouteDetail';
import * as api from '../api/flightApi';
import { LanguageProvider } from '../i18n/LanguageContext';

vi.mock('../api/flightApi');

const segment = { id: 5, transportType: 'FLIGHT', label: null };

const noRecords = [];
const unpurchasedRecords = [
  { id: 1, money: { amount: 500, currency: 'BRL' }, recordedAt: '2026-04-10T00:00:00Z', purchased: false },
  { id: 2, money: { amount: 400, currency: 'BRL' }, recordedAt: '2026-04-11T00:00:00Z', purchased: false },
];
const withPurchasedRecord = [
  { id: 1, money: { amount: 500, currency: 'BRL' }, recordedAt: '2026-04-10T00:00:00Z', purchased: true },
  { id: 2, money: { amount: 400, currency: 'BRL' }, recordedAt: '2026-04-11T00:00:00Z', purchased: false },
];

function renderSection(props = {}) {
  return render(
    <LanguageProvider>
      <SegmentSection
        routeId={1}
        segment={segment}
        onDeleted={vi.fn()}
        onPurchased={vi.fn()}
        onPurchasedChange={vi.fn()}
        blockedByPurchase={false}
        {...props}
      />
    </LanguageProvider>
  );
}

describe('SegmentSection – blockedByPurchase', () => {
  beforeEach(() => vi.resetAllMocks());

  it('enables bought buttons when no sibling is purchased', async () => {
    api.getPriceHistory.mockResolvedValue(unpurchasedRecords);
    renderSection({ blockedByPurchase: false });

    await waitFor(() => {
      screen.getAllByRole('button', { name: /bought/i })
        .forEach(btn => expect(btn).not.toBeDisabled());
    });
  });

  it('disables bought buttons when a sibling segment is purchased', async () => {
    api.getPriceHistory.mockResolvedValue(unpurchasedRecords);
    renderSection({ blockedByPurchase: true });

    await waitFor(() => {
      screen.getAllByRole('button', { name: /bought/i })
        .forEach(btn => expect(btn).toBeDisabled());
    });
  });

  it('does not disable the purchased badge on its own already-purchased record', async () => {
    api.getPriceHistory.mockResolvedValue(withPurchasedRecord);
    renderSection({ blockedByPurchase: false });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /purchased/i })).not.toBeDisabled();
    });
  });

  it('calls onPurchasedChange(segmentId, true) when history has a purchased record', async () => {
    api.getPriceHistory.mockResolvedValue(withPurchasedRecord);
    const onPurchasedChange = vi.fn();
    renderSection({ onPurchasedChange });

    await waitFor(() => {
      expect(onPurchasedChange).toHaveBeenCalledWith(5, true);
    });
  });

  it('calls onPurchasedChange(segmentId, false) when no record is purchased', async () => {
    api.getPriceHistory.mockResolvedValue(unpurchasedRecords);
    const onPurchasedChange = vi.fn();
    renderSection({ onPurchasedChange });

    await waitFor(() => {
      expect(onPurchasedChange).toHaveBeenCalledWith(5, false);
    });
  });

  it('calls onPurchasedChange(segmentId, false) when history is empty', async () => {
    api.getPriceHistory.mockResolvedValue(noRecords);
    const onPurchasedChange = vi.fn();
    renderSection({ onPurchasedChange });

    await waitFor(() => {
      expect(onPurchasedChange).toHaveBeenCalledWith(5, false);
    });
  });
});
