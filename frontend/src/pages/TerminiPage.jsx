import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSviTipovi } from '../api/tipoviIspita';
import { getSviTermini } from '../api/ispitniTermini';
import { kreirajPrijavu } from '../api/prijave';
import { useAuth } from '../context/AuthContext';
import { formatujCenu, formatujDatum } from '../utils/format';
import Poruka from '../components/Poruka';

export default function TerminiPage() {
  const { token, jeKandidat, jeUlogovan } = useAuth();
  const navigate = useNavigate();

  const [tipovi, setTipovi] = useState([]);
  const [tipFilter, setTipFilter] = useState('');
  const [termini, setTermini] = useState([]);
  const [izabrani, setIzabrani] = useState([]);
  const [greska, setGreska] = useState('');
  const [ucitavanje, setUcitavanje] = useState(true);
  const [slanje, setSlanje] = useState(false);

  useEffect(() => {
    getSviTipovi().then(setTipovi).catch(() => {});
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- učitavanje se osvežava pri svakoj promeni filtera
    setUcitavanje(true);
    getSviTermini(token, tipFilter || undefined)
      .then(setTermini)
      .catch((e) => setGreska(e.message))
      .finally(() => setUcitavanje(false));
  }, [token, tipFilter]);

  function toggleIzbor(terminId) {
    setIzabrani((prethodno) =>
      prethodno.includes(terminId) ? prethodno.filter((id) => id !== terminId) : [...prethodno, terminId]
    );
  }

  const izabraniTermini = useMemo(
    () => termini.filter((t) => izabrani.includes(t.id)),
    [termini, izabrani]
  );
  const ukupnaCena = izabraniTermini.reduce((zbir, t) => zbir + t.trenutnaCena, 0);

  async function handlePrijava() {
    setGreska('');
    setSlanje(true);
    try {
      await kreirajPrijavu(token, izabrani);
      navigate('/moje-prijave', { state: { poruka: 'Prijava je uspešno kreirana.' } });
    } catch (error) {
      setGreska(error.message);
    } finally {
      setSlanje(false);
    }
  }

  return (
    <div className="stranica">
      <div className="stranica__zaglavlje">
        <h1>Termini i cene</h1>
        <select value={tipFilter} onChange={(e) => setTipFilter(e.target.value)}>
          <option value="">Sve oblasti</option>
          {tipovi.map((tip) => (
            <option key={tip.id} value={tip.id}>
              {tip.naziv}
            </option>
          ))}
        </select>
      </div>

      <Poruka>{greska}</Poruka>

      {ucitavanje ? (
        <p>Učitavanje termina...</p>
      ) : (
        <div className="termin-tabela">
          <table>
            <thead>
              <tr>
                {jeKandidat && <th></th>}
                <th>Oblast</th>
                <th>Datum</th>
                <th>Vreme</th>
                <th>Mesto</th>
                <th>Cena</th>
                <th>Prijavljenih</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {termini.map((termin) => (
                <tr key={termin.id} className={!termin.mozeSeIzabrati ? 'red-neaktivan' : ''}>
                  {jeKandidat && (
                    <td>
                      <input
                        type="checkbox"
                        disabled={!termin.mozeSeIzabrati}
                        checked={izabrani.includes(termin.id)}
                        onChange={() => toggleIzbor(termin.id)}
                      />
                    </td>
                  )}
                  <td>{termin.tipIspitaNaziv}</td>
                  <td>{formatujDatum(termin.datumOdrzavanja)}</td>
                  <td>{termin.vremePocetka}</td>
                  <td>{termin.mestoNaziv}</td>
                  <td>{formatujCenu(termin.trenutnaCena)}</td>
                  <td>{termin.brojPrijavljenih}</td>
                  <td>{termin.mozeSeIzabrati ? 'Slobodan' : 'Nedostupan'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {jeKandidat && izabrani.length > 0 && (
        <div className="traka-akcije">
          <div>
            <strong>{izabrani.length}</strong> izabranih termina — ukupno <strong>{formatujCenu(ukupnaCena)}</strong>
            {izabrani.length >= 2 && <span className="tekst-prigušen"> (popust se obračunava pri kreiranju prijave)</span>}
          </div>
          <button className="dugme dugme--primarno" onClick={handlePrijava} disabled={slanje}>
            {slanje ? 'Kreiranje prijave...' : 'Kreiraj prijavu'}
          </button>
        </div>
      )}

      {!jeUlogovan && (
        <Poruka tip="info">Prijavite se kao kandidat da biste mogli da izaberete termine i kreirate prijavu.</Poruka>
      )}
    </div>
  );
}
