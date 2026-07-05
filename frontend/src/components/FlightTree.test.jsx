import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import FlightTree from './FlightTree';
import * as api from '../api/flightApi';
import { LanguageProvider } from '../i18n/LanguageContext';

vi.mock('../api/flightApi');

function renderTree(routes) {
  render(
    <LanguageProvider>
      <FlightTree routes={routes} />
    </LanguageProvider>
  );
}

const routes = [
  { id: 1, origin: 'GRU', destination: 'LIS', travelDate: '2026-12-01' },
  { id: 2, origin: 'LIS', destination: 'CDG', travelDate: '2026-12-05' },
];

describe('FlightTree', () => {
  beforeEach(() => { vi.resetAllMocks(); });

  it('shows empty state when no routes provided', () => {
    renderTree([]);
    expect(screen.getByText(/No routes yet/i)).toBeInTheDocument();
  });

  it('renders airport nodes from routes', () => {
    renderTree(routes);
    expect(screen.getByText('GRU')).toBeInTheDocument();
    expect(screen.getByText('LIS')).toBeInTheDocument();
    expect(screen.getByText('CDG')).toBeInTheDocument();
  });

  it('shows hint text before any node is selected', () => {
    renderTree(routes);
    expect(screen.getByText(/Click any node/i)).toBeInTheDocument();
  });

  it('shows error banner when price fetch fails', async () => {
    api.getPriceSummary.mockRejectedValue(new Error('Network error'));
    renderTree(routes);

    fireEvent.click(screen.getByText('CDG'));

    await waitFor(() =>
      expect(screen.getByText('Network error')).toBeInTheDocument()
    );
  });

  it('shows journey total when prices load successfully', async () => {
    api.getPriceSummary
      .mockResolvedValueOnce({ segments: [{ lowestPrice: 500.00, lowestCurrency: 'USD' }] })
      .mockResolvedValueOnce({ segments: [{ lowestPrice: 300.00, lowestCurrency: 'USD' }] });

    renderTree(routes);
    fireEvent.click(screen.getByText('CDG'));

    await waitFor(() =>
      expect(screen.getByText(/Journey total/i)).toBeInTheDocument()
    );
    expect(screen.getByText('US$ 800.00')).toBeInTheDocument();
  });
});
