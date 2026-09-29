import { apiFetch } from './client';

export function getAktivneCene() {
  return apiFetch('/api/cenovnici');
}

export function getIstorija(token, tipIspitaId) {
  return apiFetch(`/api/cenovnici/istorija/${tipIspitaId}`, { token });
}

export function kreirajNovuCenu(token, dto) {
  return apiFetch('/api/cenovnici', { method: 'POST', token, body: dto });
}
