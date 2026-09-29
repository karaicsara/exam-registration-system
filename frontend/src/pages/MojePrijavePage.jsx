import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getMojePrijave, otkaziPrijavu } from '../api/prijave';
import { posaljiDokaz } from '../api/uplate';
import { formatujCenu, formatujDatum } from '../utils/format';
import { prijavaStatusInfo, uplataStatusInfo } from '../utils/status';
import StatusPill from '../components/StatusPill';
import Kartica from '../components/Kartica';
import Poruka from '../components/Poruka';

export default function MojePrijavePage() {
  const { token } = useAuth();
  const location = useLocation();
  const porukaOUspehu = location.state?.poruka;
  const [prijave, setPrijave] = useState([]);
  const [ucitavanje, setUcitavanje] = useState(true);
  const [greska, setGreska] = useState('');
  const [akcijaId, setAkcijaId] = useState(null);

  function ucitaj() {
    setUcitavanje(true);
    getMojePrijave(token)
      .then(setPrijave)
      .catch((e) => setGreska(e.message))
      .finally(() => setUcitavanje(false));
  }

  // eslint-disable-next-line react-hooks/set-state-in-effect -- lista se osvežava pri promeni korisnika
  useEffect(ucitaj, [token]);

  async function handleOtkazi(id) {
    if (!window.confirm('Da li ste sigurni da želite da otkažete prijavu?')) return;
    setAkcijaId(id);
    setGreska('');
    try {
      await otkaziPrijavu(token, id);
      ucitaj();
    } catch (error) {
      setGreska(error.message);
    } finally {
      setAkcijaId(null);
    }
  }

  async function handleDostaviUplatu(id, fajl) {
    if (!fajl) return;
    setAkcijaId(id);
    setGreska('');
    try {
      await posaljiDokaz(token, id, fajl);
      ucitaj();
    } catch (error) {
      setGreska(error.message);
    } finally {
      setAkcijaId(null);
    }
  }

  if (ucitavanje) return <p className="stranica">Učitavanje prijava...</p>;

  return (
    <div className="stranica">
      <h1>Moje prijave</h1>
      <Poruka tip="uspeh">{porukaOUspehu}</Poruka>
      <Poruka>{greska}</Poruka>

      {prijave.length === 0 && <p>Trenutno nemate nijednu prijavu. Posetite stranicu sa terminima da biste se prijavili.</p>}

      <div className="prijava-lista">
        {prijave.map((prijava) => {
          const status = prijavaStatusInfo(prijava.status);
          return (
            <Kartica
              key={prijava.id}
              naslov={`Prijava od ${formatujDatum(prijava.datumKreiranja)}`}
              akcija={<StatusPill oznaka={status.oznaka} ton={status.ton} />}
            >
              <ul className="stavka-lista">
                {prijava.stavke.map((stavka) => (
                  <li key={stavka.id}>
                    {stavka.tipIspitaNaziv} — {formatujDatum(stavka.datumOdrzavanja)} u {stavka.vremePocetka}, {stavka.mestoNaziv}
                    <span className="tekst-prigušen"> ({formatujCenu(stavka.cenaNaDanPrijave)})</span>
                  </li>
                ))}
              </ul>

              <div className="prijava-obracun">
                <span>Bruto iznos: {formatujCenu(prijava.brutoIznos)}</span>
                {prijava.popust > 0 && <span>Popust: -{formatujCenu(prijava.popust)}</span>}
                <strong>Za uplatu: {formatujCenu(prijava.ukupanIznos)}</strong>
              </div>

              {prijava.uplate.length > 0 && (
                <div className="uplata-lista">
                  <h4>Dokazi o uplati</h4>
                  {prijava.uplate.map((uplata) => {
                    const uStatus = uplataStatusInfo(uplata.status);
                    return (
                      <div key={uplata.id} className="uplata-red">
                        <StatusPill oznaka={uStatus.oznaka} ton={uStatus.ton} />
                        <span className="tekst-prigušen">{formatujDatum(uplata.datumSlanja)}</span>
                        {uplata.napomena && <span className="tekst-prigušen"> — {uplata.napomena}</span>}
                      </div>
                    );
                  })}
                </div>
              )}

              <div className="kartica__akcije">
                {prijava.mozeSeDostaviUplata && (
                  <label className="dugme dugme--sekundarno dugme--fajl">
                    {akcijaId === prijava.id ? 'Slanje...' : 'Dostavi dokaz o uplati'}
                    <input
                      type="file"
                      accept="image/jpeg,image/png,application/pdf"
                      hidden
                      onChange={(e) => handleDostaviUplatu(prijava.id, e.target.files[0])}
                    />
                  </label>
                )}
                {prijava.mozeSeOtkazati && (
                  <button
                    className="dugme dugme--opasnost"
                    disabled={akcijaId === prijava.id}
                    onClick={() => handleOtkazi(prijava.id)}
                  >
                    Otkaži prijavu
                  </button>
                )}
              </div>
            </Kartica>
          );
        })}
      </div>
    </div>
  );
}
