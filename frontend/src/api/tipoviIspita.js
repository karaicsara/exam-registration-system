import { apiFetch } from './client';

export function getSviTipovi() {
  return apiFetch('/api/tipovi-ispita');
}
