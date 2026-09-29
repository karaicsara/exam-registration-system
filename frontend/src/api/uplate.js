import { apiFetch } from './client';

export function posaljiDokaz(token, prijavaId, fajl) {
  const formData = new FormData();
  formData.append('fajl', fajl);
  return apiFetch(`/api/uplate/prijava/${prijavaId}`, { method: 'POST', token, body: formData, isFormData: true });
}

export function getUplateZaPrijavu(token, prijavaId) {
  return apiFetch(`/api/uplate/prijava/${prijavaId}`, { token });
}

export function odobriUplatu(token, id) {
  return apiFetch(`/api/uplate/${id}/odobri`, { method: 'PUT', token });
}

export function odbijUplatu(token, id, razlog) {
  return apiFetch(`/api/uplate/${id}/odbij`, { method: 'PUT', token, body: { razlog } });
}

export function getStatistika(token) {
  return apiFetch('/api/uplate/statistika', { token });
}
