import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import CreateRouteModal from './CreateRouteModal';
import * as api from '../api/flightApi';

vi.mock('../api/flightApi');

describe('CreateRouteModal', () => {
  const onClose = vi.fn();
  const onCreated = vi.fn();

  beforeEach(() => { vi.resetAllMocks(); });

  function fillAndSubmit(origin, destination) {
    fireEvent.change(screen.getByPlaceholderText('REC'), { target: { value: origin } });
    fireEvent.change(screen.getByPlaceholderText('LIS'), { target: { value: destination } });
    const dateInput = screen.getByDisplayValue('');
    fireEvent.change(dateInput, { target: { value: '2026-12-01' } });
    fireEvent.click(screen.getByText('Create Route'));
  }

  it('shows error when origin is not a 3-letter IATA code', async () => {
    render(<CreateRouteModal onClose={onClose} onCreated={onCreated} />);
    fillAndSubmit('12X', 'LIS');

    await waitFor(() =>
      expect(screen.getByText(/Origin must be a 3-letter IATA code/i)).toBeInTheDocument()
    );
    expect(api.createRoute).not.toHaveBeenCalled();
  });

  it('shows error when destination is not a 3-letter IATA code', async () => {
    render(<CreateRouteModal onClose={onClose} onCreated={onCreated} />);
    fillAndSubmit('GRU', '1234');

    await waitFor(() =>
      expect(screen.getByText(/Destination must be a 3-letter IATA code/i)).toBeInTheDocument()
    );
    expect(api.createRoute).not.toHaveBeenCalled();
  });

  it('calls createRoute with uppercased codes on valid input', async () => {
    api.createRoute.mockResolvedValue({ id: 1 });
    render(<CreateRouteModal onClose={onClose} onCreated={onCreated} />);
    fillAndSubmit('gru', 'lis');

    await waitFor(() => expect(onCreated).toHaveBeenCalled());
    expect(api.createRoute).toHaveBeenCalledWith('GRU', 'LIS', '2026-12-01');
  });

  it('shows API error on failure', async () => {
    api.createRoute.mockRejectedValue(new Error('Route already exists'));
    render(<CreateRouteModal onClose={onClose} onCreated={onCreated} />);
    fillAndSubmit('GRU', 'LIS');

    await waitFor(() =>
      expect(screen.getByText('Route already exists')).toBeInTheDocument()
    );
  });

  it('closes when clicking overlay', () => {
    render(<CreateRouteModal onClose={onClose} onCreated={onCreated} />);
    fireEvent.click(document.querySelector('.modal-overlay'));
    expect(onClose).toHaveBeenCalled();
  });
});
