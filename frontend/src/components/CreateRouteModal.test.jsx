import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import CreateRouteModal from './CreateRouteModal';
import * as api from '../api/flightApi';
import { LanguageProvider } from '../i18n/LanguageContext';

vi.mock('../api/flightApi');

function renderModal() {
  const onClose = vi.fn();
  const onCreated = vi.fn();
  render(
    <LanguageProvider>
      <CreateRouteModal onClose={onClose} onCreated={onCreated} />
    </LanguageProvider>
  );
  return { onClose, onCreated };
}

function fillAndSubmit(origin, destination) {
  fireEvent.change(screen.getByPlaceholderText('GRU'), { target: { value: origin } });
  fireEvent.change(screen.getByPlaceholderText('LIS'), { target: { value: destination } });
  fireEvent.change(document.querySelector('input[type="date"]'), { target: { value: '2026-12-01' } });
  fireEvent.click(screen.getByRole('button', { name: /create route/i }));
}

describe('CreateRouteModal', () => {
  beforeEach(() => { vi.resetAllMocks(); });

  it('shows error when origin is not a valid IATA code', async () => {
    renderModal();
    fillAndSubmit('12X', 'LIS');

    await waitFor(() =>
      expect(screen.getByText(/Origin must be a 3-letter IATA code/i)).toBeInTheDocument()
    );
    expect(api.createRoute).not.toHaveBeenCalled();
  });

  it('shows error when destination is not a valid IATA code', async () => {
    renderModal();
    fillAndSubmit('GRU', '123');

    await waitFor(() =>
      expect(screen.getByText(/Destination must be a 3-letter IATA code/i)).toBeInTheDocument()
    );
    expect(api.createRoute).not.toHaveBeenCalled();
  });

  it('calls createRoute with valid airport codes', async () => {
    api.createRoute.mockResolvedValue({ id: 1 });
    const { onCreated } = renderModal();
    fillAndSubmit('GRU', 'LIS');

    await waitFor(() => expect(onCreated).toHaveBeenCalled());
    expect(api.createRoute).toHaveBeenCalledWith('GRU', 'LIS', '2026-12-01');
  });

  it('shows API error on failure', async () => {
    api.createRoute.mockRejectedValue(new Error('Route already exists'));
    renderModal();
    fillAndSubmit('GRU', 'LIS');

    await waitFor(() =>
      expect(screen.getByText('Route already exists')).toBeInTheDocument()
    );
  });

  it('closes when clicking overlay', () => {
    const { onClose } = renderModal();
    fireEvent.click(document.querySelector('.cr-overlay'));
    expect(onClose).toHaveBeenCalled();
  });
});
