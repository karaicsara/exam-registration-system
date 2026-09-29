import { apiFetch } from './client';

export function getSvaMesta() {
  return apiFetch('/api/mesta-odrzavanja');
}

export function kreirajMesto(token, dto) {
  return apiFetch('/api/mesta-odrzavanja', { method: 'POST', token, body: dto });
}

export function izmeniMesto(token, id, dto) {
  return apiFetch(`/api/mesta-odrzavanja/${id}`, { method: 'PUT', token, body: dto });
}

export function obrisiMesto(token, id) {
  return apiFetch(`/api/mesta-odrzavanja/${id}`, { method: 'DELETE', token });
}
