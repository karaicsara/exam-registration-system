import { createContext, useContext, useState } from 'react';
import * as kandidatApi from '../api/kandidati';
import * as administratorApi from '../api/administratori';

const AuthContext = createContext(null);
const STORAGE_KEY = 'anastasijaProbni.nalog';

function procitajSacuvaniNalog() {
  const sacuvano = localStorage.getItem(STORAGE_KEY);
  if (!sacuvano) return null;
  try {
    return JSON.parse(sacuvano);
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function AuthProvider({ children }) {
  const [nalog, setNalog] = useState(procitajSacuvaniNalog);
  const ucitavanje = false;

  function sacuvajNalog(odgovor) {
    setNalog(odgovor);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(odgovor));
    return odgovor;
  }

  async function prijaviSeKaoKandidat(username, password) {
    const odgovor = await kandidatApi.login(username, password);
    return sacuvajNalog(odgovor);
  }

  async function prijaviSeKaoAdministrator(username, password) {
    const odgovor = await administratorApi.login(username, password);
    return sacuvajNalog(odgovor);
  }

  function odjaviSe() {
    setNalog(null);
    localStorage.removeItem(STORAGE_KEY);
  }

  const jeKandidat = nalog?.userType === 'KANDIDAT';
  const jeAdmin = nalog?.userType === 'ADMINISTRATOR';

  const value = {
    nalog,
    token: nalog?.token ?? null,
    jeUlogovan: !!nalog,
    jeKandidat,
    jeAdmin,
    ucitavanje,
    prijaviSeKaoKandidat,
    prijaviSeKaoAdministrator,
    postaviNalog: sacuvajNalog,
    odjaviSe,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components -- hook deljen iz istog fajla radi jednostavnosti uvoza
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth mora biti korišćen unutar AuthProvider-a');
  return context;
}
