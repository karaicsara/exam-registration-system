import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getSviTipovi } from '../../api/tipoviIspita';
import { getSvaMesta } from '../../api/mestaOdrzavanja';
import { getSviTermini, kreirajTermin, izmeniTermin, obrisiTermin } from '../../api/ispitniTermini';
import { formatujCenu, formatujDatum } from '../../utils/format';
import Poruka from '../../components/Poruka';
import Kartica from '../../components/Kartica';

const PRAZNA_FORMA = { datumOdrzavanja: '', vremePocetka: '', napomena: '', dostupan: true, tipIspitaId: '', mestoOdrzavanjaId: '' };

export default function AdminTerminiPage() {
  const { token } = useAuth();
  const [tipovi, setTipovi] = useState([]);
  const [mesta, setMesta] = useState([]);
  const [termini, setTermini] = useState([]);
  const [forma, setForma] = useState(PRAZNA_FORMA);
  const [izmenaId, setIzmenaId] = useState(null);
  const [greska, setGreska] = useState('');

  function ucitajTermine() {
    getSviTermini(token).then(setTermini).catch((e) => setGreska(e.message));
  }

  useEffect(() => {
    getSviTipovi().then(setTipovi).catch(() => {});
    getSvaMesta().then(setMesta).catch(() => {});
    ucitajTermine();
  }, [token]);

  function handleChange(polje) {
    return (event) => {
      const vrednost = event.target.type === 'checkbox' ? event.target.checked : event.target.value;
      setForma((prethodno) => ({ ...prethodno, [polje]: vrednost }));
    };
  }

  function handleIzmeni(termin) {
    setIzmenaId(termin.id);
    setForma({
      datumOdrzavanja: termin.datumOdrzavanja,
      vremePocetka: termin.vremePocetka,
      napomena: termin.napomena ?? '',
      dostupan: termin.dostupan,
      tipIspitaId: termin.tipIspitaId,
      mestoOdrzavanjaId: termin.mestoOdrzavanjaId,
    });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setGreska('');
    try {
      if (izmenaId) {
        await izmeniTermin(token, izmenaId, forma);
      } else {
        await kreirajTermin(token, forma);
      }
      setForma(PRAZNA_FORMA);
      setIzmenaId(null);
      ucitajTermine();
    } catch (error) {
      setGreska(error.message);
    }
  }

  async function handleObrisi(id) {
    if (!window.confirm('Obrisati ovaj termin?')) return;
    setGreska('');
    try {
      await obrisiTermin(token, id);
      ucitajTermine();
    } catch (error) {
      setGreska(error.message);
    }
  }

  return (
    <div>
      <h1>Ispitni termini</h1>
      <Poruka>{greska}</Poruka>

      <Kartica naslov={izmenaId ? 'Izmena termina' : 'Novi termin'}>
        <form onSubmit={handleSubmit} className="forma forma--grid">
          <label>
            Oblast ispita
            <select value={forma.tipIspitaId} onChange={handleChange('tipIspitaId')} required>
              <option value="">Izaberite...</option>
              {tipovi.map((tip) => (
                <option key={tip.id} value={tip.id}>{tip.naziv}</option>
              ))}
            </select>
          </label>
          <label>
            Mesto održavanja
            <select value={forma.mestoOdrzavanjaId} onChange={handleChange('mestoOdrzavanjaId')} required>
              <option value="">Izaberite...</option>
              {mesta.map((mesto) => (
                <option key={mesto.id} value={mesto.id}>{mesto.naziv}</option>
              ))}
            </select>
          </label>
          <label>
            Datum održavanja
            <input type="date" value={forma.datumOdrzavanja} onChange={handleChange('datumOdrzavanja')} required />
          </label>
          <label>
            Vreme početka
            <input type="time" value={forma.vremePocetka} onChange={handleChange('vremePocetka')} required />
          </label>
          <label className="polje-siroko">
            Napomena
            <input value={forma.napomena} onChange={handleChange('napomena')} />
          </label>
          <label className="polje-checkbox">
            <input type="checkbox" checked={forma.dostupan} onChange={handleChange('dostupan')} />
            Termin je dostupan za prijavu
          </label>
          <div className="forma__akcije">
            <button type="submit" className="dugme dugme--primarno">
              {izmenaId ? 'Sačuvaj izmene' : 'Kreiraj termin'}
            </button>
            {izmenaId && (
              <button type="button" className="dugme dugme--sekundarno" onClick={() => { setIzmenaId(null); setForma(PRAZNA_FORMA); }}>
                Otkaži
              </button>
            )}
          </div>
        </form>
      </Kartica>

      <table className="admin-tabela">
        <thead>
          <tr>
            <th>Oblast</th>
            <th>Datum</th>
            <th>Vreme</th>
            <th>Mesto</th>
            <th>Cena</th>
            <th>Prijavljenih</th>
            <th>Dostupan</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {termini.map((termin) => (
            <tr key={termin.id}>
              <td>{termin.tipIspitaNaziv}</td>
              <td>{formatujDatum(termin.datumOdrzavanja)}</td>
              <td>{termin.vremePocetka}</td>
              <td>{termin.mestoNaziv}</td>
              <td>{formatujCenu(termin.trenutnaCena)}</td>
              <td>{termin.brojPrijavljenih}</td>
              <td>{termin.dostupan ? 'Da' : 'Ne'}</td>
              <td className="kolona-akcije">
                <Link to={`/admin/termini/${termin.id}`} className="dugme dugme--sekundarno dugme--malo">
                  Detalji
                </Link>
                <button className="dugme dugme--sekundarno dugme--malo" onClick={() => handleIzmeni(termin)}>
                  Izmeni
                </button>
                <button className="dugme dugme--opasnost dugme--malo" onClick={() => handleObrisi(termin.id)}>
                  Obriši
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
