import { apiFetch, BASE_URL } from './client';

export function getSviTermini(token, tipIspitaId) {
  const query = tipIspitaId ? `?tipIspitaId=${tipIspitaId}` : '';
  return apiFetch(`/api/ispitni-termini${query}`, { token });
}

export function kreirajTermin(token, dto) {
  return apiFetch('/api/ispitni-termini', { method: 'POST', token, body: dto });
}

export function izmeniTermin(token, id, dto) {
  return apiFetch(`/api/ispitni-termini/${id}`, { method: 'PUT', token, body: dto });
}

export function obrisiTermin(token, id) {
  return apiFetch(`/api/ispitni-termini/${id}`, { method: 'DELETE', token });
}

export function getKandidatiZaTermin(token, id) {
  return apiFetch(`/api/ispitni-termini/${id}/kandidati`, { token });
}

export function getStatistika(token) {
  return apiFetch('/api/ispitni-termini/statistika', { token });
}

export function getDetaljiTermina(token, id) {
  return apiFetch(`/api/ispitni-termini/${id}/detalji`, { token });
}

export async function preuzmiPdfSpiska(token, id) {
  const response = await fetch(`${BASE_URL}/api/ispitni-termini/${id}/pdf`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) {
    throw new Error(`Preuzimanje PDF dokumenta nije uspelo (${response.status})`);
  }
  const blob = await response.blob();

  const raspolozenje = response.headers.get('Content-Disposition') || '';
  const poklapanje = raspolozenje.match(/filename="?([^"]+)"?/);
  const nazivFajla = poklapanje ? poklapanje[1] : 'spisak_kandidata.pdf';

  const url = window.URL.createObjectURL(blob);
  const privremeniLink = document.createElement('a');
  privremeniLink.href = url;
  privremeniLink.download = nazivFajla;
  document.body.appendChild(privremeniLink);
  privremeniLink.click();
  privremeniLink.remove();
  window.URL.revokeObjectURL(url);
}
