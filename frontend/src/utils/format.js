export function formatujCenu(iznos) {
  if (iznos === null || iznos === undefined) return '—';
  return new Intl.NumberFormat('sr-RS', { maximumFractionDigits: 0 }).format(iznos) + ' RSD';
}

export function formatujDatum(datum) {
  if (!datum) return '—';
  return new Date(`${datum}T00:00:00`).toLocaleDateString('sr-RS', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

export function formatujDatumKratko(datum) {
  if (!datum) return '—';
  return new Date(`${datum}T00:00:00`).toLocaleDateString('sr-RS', { day: '2-digit', month: '2-digit' });
}
