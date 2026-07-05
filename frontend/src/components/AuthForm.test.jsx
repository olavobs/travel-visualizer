import { describe, it, expect } from 'vitest';
import { friendlyError } from './AuthForm';

const t = (key) => key;

describe('friendlyError', () => {
  describe('network / connectivity', () => {
    it('maps "timed out" to connection error', () => {
      expect(friendlyError('Request timed out', 'login', t)).toBe('auth.errConnection');
    });

    it('maps "network error" to connection error', () => {
      expect(friendlyError('network error', 'login', t)).toBe('auth.errConnection');
    });

    it('maps "failed to fetch" to connection error', () => {
      expect(friendlyError('Failed to fetch', 'login', t)).toBe('auth.errConnection');
    });

    it('maps 502 to unreachable', () => {
      expect(friendlyError('Request failed: 502', 'login', t)).toBe('auth.errUnreachable');
    });

    it('maps 503 to unreachable', () => {
      expect(friendlyError('Request failed: 503', 'login', t)).toBe('auth.errUnreachable');
    });
  });

  describe('authentication errors', () => {
    it('maps "Invalid credentials" to invalid credentials', () => {
      expect(friendlyError('Invalid credentials', 'login', t)).toBe('auth.errInvalidCredentials');
    });

    it('maps "Unauthorized" to invalid credentials', () => {
      expect(friendlyError('Unauthorized', 'login', t)).toBe('auth.errInvalidCredentials');
    });

    it('maps Request failed: 401 to invalid credentials', () => {
      expect(friendlyError('Request failed: 401', 'login', t)).toBe('auth.errInvalidCredentials');
    });
  });

  describe('registration errors', () => {
    it('maps "already" to email taken', () => {
      expect(friendlyError('Email already in use', 'register', t)).toBe('auth.errEmailTaken');
    });

    it('maps "conflict" to email taken', () => {
      expect(friendlyError('conflict', 'register', t)).toBe('auth.errEmailTaken');
    });

    it('maps Request failed: 409 to email taken', () => {
      expect(friendlyError('Request failed: 409', 'register', t)).toBe('auth.errEmailTaken');
    });

    it('maps password length message to too short', () => {
      expect(friendlyError('password must be at least 8 characters', 'register', t))
        .toBe('auth.errPasswordTooShort');
    });
  });

  describe('server errors', () => {
    it('maps Request failed: 500 to server error', () => {
      expect(friendlyError('Request failed: 500', 'login', t)).toBe('auth.errServer');
    });

    it('maps Request failed: 403 to server error', () => {
      expect(friendlyError('Request failed: 403', 'login', t)).toBe('auth.errServer');
    });
  });

  describe('fallback behaviour', () => {
    it('falls back to invalid credentials for unknown login errors', () => {
      expect(friendlyError('something weird', 'login', t)).toBe('auth.errInvalidCredentials');
    });

    it('returns raw message for unknown register errors', () => {
      expect(friendlyError('something weird', 'register', t)).toBe('something weird');
    });

    it('handles null raw in login mode gracefully', () => {
      expect(friendlyError(null, 'login', t)).toBe('auth.errInvalidCredentials');
    });

    it('handles undefined raw in register mode gracefully', () => {
      // no match + not login → returns raw (undefined); callers always pass a string
      expect(friendlyError(undefined, 'register', t)).toBeUndefined();
    });
  });
});
