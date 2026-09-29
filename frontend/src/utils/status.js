export const PRIJAVA_STATUS_INFO = {
  KREIRANA: { oznaka: 'Kreirana', ton: 'cekanje' },
  PRIJAVLJENA: { oznaka: 'Prijavljen', ton: 'uspeh' },
  OTKAZANA: { oznaka: 'Otkazana', ton: 'neutralno' },
};

export const UPLATA_STATUS_INFO = {
  NA_CEKANJU: { oznaka: 'Na čekanju', ton: 'cekanje' },
  ODOBRENA: { oznaka: 'Odobrena', ton: 'uspeh' },
  ODBIJENA: { oznaka: 'Odbijena', ton: 'greska' },
};

export function prijavaStatusInfo(status) {
  return PRIJAVA_STATUS_INFO[status] ?? { oznaka: status, ton: 'neutralno' };
}

export function uplataStatusInfo(status) {
  return UPLATA_STATUS_INFO[status] ?? { oznaka: status, ton: 'neutralno' };
}
