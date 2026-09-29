export const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export async function apiFetch(path, { method = 'GET', body, token, isFormData = false } = {}) {
  const headers = {};
  if (!isFormData) headers['Content-Type'] = 'application/json';
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const response = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: isFormData ? body : body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!response.ok) {
    let poruka = `Greška (${response.status})`;
    try {
      const podaci = await response.json();
      poruka = podaci.message || podaci.detail || poruka;
    } catch {
      // odgovor servera nije JSON, zadržavamo podrazumevanu poruku
    }
    const greska = new Error(poruka);
    greska.status = response.status;
    throw greska;
  }

  if (response.status === 204) return null;
  const tekst = await response.text();
  return tekst ? JSON.parse(tekst) : null;
}
