import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getSviTipovi } from '../../api/tipoviIspita';
import { getAktivneCene, getIstorija, kreirajNovuCenu } from '../../api/cenovnici';
import { formatujCenu, formatujDatum } from '../../utils/format';
import Poruka from '../../components/Poruka';
import Kartica from '../../components/Kartica';

const PRAZNA_FORMA = { tipIspitaId: '', naziv: '', iznos: '', vaziOd: '', vaziDo: '' };

export default function AdminCenovnikPage() {
  const { token } = useAuth();
  const [tipovi, setTipovi] = useState([]);
  const [aktivneCene, setAktivneCene] = useState([]);
  const [istorija, setIstorija] = useState({});
  const [forma, setForma] = useState(PRAZNA_FORMA);
  const [greska, setGreska] = useState('');
  const [poruka, setPoruka] = useState('');

  function ucitaj() {
    getAktivneCene().then(setAktivneCene).catch((e) => setGreska(e.message));
  }

  useEffect(() => {
    getSviTipovi().then((podaci) => {
      setTipovi(podaci);
      podaci.forEach((tip) => {
        getIstorija(token, tip.id).then((lista) =>
          setIstorija((prethodno) => ({ ...prethodno, [tip.id]: lista }))
        );
      });
    });
    ucitaj();
  }, [token]);

  function handleChange(polje) {
    return (event) => setForma((prethodno) => ({ ...prethodno, [polje]: event.target.value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setGreska('');
    setPoruka('');
    try {
      await kreirajNovuCenu(token, { ...forma, iznos: Number(forma.iznos) });
      setPoruka('Nova cena je uspešno aktivirana.');
      setForma(PRAZNA_FORMA);
      ucitaj();
      getIstorija(token, forma.tipIspitaId).then((lista) =>
        setIstorija((prethodno) => ({ ...prethodno, [forma.tipIspitaId]: lista }))
      );
    } catch (error) {
      setGreska(error.message);
    }
  }

  return (
    <div>
      <h1>Cenovnik</h1>
      <Poruka>{greska}</Poruka>
      <Poruka tip="uspeh">{poruka}</Poruka>

      <div className="karta-grid">
        {aktivneCene.map((cena) => (
          <Kartica key={cena.id} naslov={cena.tipIspitaNaziv}>
            <p className="kartica__cena">{formatujCenu(cena.iznos)}</p>
            <p className="tekst-prigušen">Važi od {formatujDatum(cena.vaziOd)}{cena.vaziDo ? ` do ${formatujDatum(cena.vaziDo)}` : ', bez ograničenja'}</p>
          </Kartica>
        ))}
      </div>

      <Kartica naslov="Nova cena">
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
            Naziv cenovnika
            <input value={forma.naziv} onChange={handleChange('naziv')} required />
          </label>
          <label>
            Iznos (RSD)
            <input type="number" min="1" value={forma.iznos} onChange={handleChange('iznos')} required />
          </label>
          <label>
            Važi od
            <input type="date" value={forma.vaziOd} onChange={handleChange('vaziOd')} required />
          </label>
          <label>
            Važi do (opciono)
            <input type="date" value={forma.vaziDo} onChange={handleChange('vaziDo')} />
          </label>
          <div className="forma__akcije">
            <button type="submit" className="dugme dugme--primarno">
              Aktiviraj novu cenu
            </button>
          </div>
        </form>
      </Kartica>

      {tipovi.map((tip) => (
        <Kartica key={tip.id} naslov={`Istorija cena — ${tip.naziv}`}>
          <table className="admin-tabela">
            <thead>
              <tr>
                <th>Naziv</th>
                <th>Iznos</th>
                <th>Važi od</th>
                <th>Važi do</th>
                <th>Aktivan</th>
              </tr>
            </thead>
            <tbody>
              {(istorija[tip.id] ?? []).map((cena) => (
                <tr key={cena.id}>
                  <td>{cena.naziv}</td>
                  <td>{formatujCenu(cena.iznos)}</td>
                  <td>{formatujDatum(cena.vaziOd)}</td>
                  <td>{cena.vaziDo ? formatujDatum(cena.vaziDo) : '—'}</td>
                  <td>{cena.aktivan ? 'Da' : 'Ne'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Kartica>
      ))}
    </div>
  );
}
