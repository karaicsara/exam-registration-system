import { apiFetch } from './client';

export function login(username, password) {
  return apiFetch('/api/administratori/login', { method: 'POST', body: { username, password } });
}
