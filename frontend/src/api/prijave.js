import { apiFetch } from './client';

export function kreirajPrijavu(token, terminiIds) {
  return apiFetch('/api/prijave', { method: 'POST', token, body: { terminiIds } });
}

export function getMojePrijave(token) {
  return apiFetch('/api/prijave/moje', { token });
}

export function otkaziPrijavu(token, id) {
  return apiFetch(`/api/prijave/${id}/otkazi`, { method: 'PUT', token });
}

export function pretraziPrijave(token, filteri) {
  const params = new URLSearchParams();
  Object.entries(filteri).forEach(([kljuc, vrednost]) => {
    if (vrednost) params.set(kljuc, vrednost);
  });
  const query = params.toString() ? `?${params.toString()}` : '';
  return apiFetch(`/api/prijave${query}`, { token });
}

export function getDetalj(token, id) {
  return apiFetch(`/api/prijave/${id}`, { token });
}

export function getStatistika(token) {
  return apiFetch('/api/prijave/statistika', { token });
}
