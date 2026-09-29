import { apiFetch } from './client';

export function login(username, password) {
  return apiFetch('/api/kandidati/login', { method: 'POST', body: { username, password } });
}

export function zapocniRegistraciju(dto) {
  return apiFetch('/api/kandidati/registracija/pocetak', { method: 'POST', body: dto });
}

export function ponovoPosaljiKod(email) {
  return apiFetch('/api/kandidati/registracija/ponovo-posalji-kod', { method: 'POST', body: { email } });
}

export function verifikujKod(email, kod) {
  return apiFetch('/api/kandidati/registracija/verifikacija', { method: 'POST', body: { email, kod } });
}

export function getMojiPodaci(token) {
  return apiFetch('/api/kandidati/me', { token });
}

export function azurirajMojePodatke(token, dto) {
  return apiFetch('/api/kandidati/me', { method: 'PUT', token, body: dto });
}
