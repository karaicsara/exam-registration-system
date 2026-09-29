import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getSvaMesta, kreirajMesto, izmeniMesto, obrisiMesto } from '../../api/mestaOdrzavanja';
import Poruka from '../../components/Poruka';
import Kartica from '../../components/Kartica';

const PRAZNA_FORMA = { naziv: '', adresa: '' };

export default function AdminMestaPage() {
  const { token } = useAuth();
  const [mesta, setMesta] = useState([]);
  const [forma, setForma] = useState(PRAZNA_FORMA);
  const [izmenaId, setIzmenaId] = useState(null);
  const [greska, setGreska] = useState('');

  function ucitaj() {
    getSvaMesta().then(setMesta).catch((e) => setGreska(e.message));
  }

  useEffect(ucitaj, []);

  function handleChange(polje) {
    return (event) => setForma((prethodno) => ({ ...prethodno, [polje]: event.target.value }));
  }

  function handleIzmeni(mesto) {
    setIzmenaId(mesto.id);
    setForma({ naziv: mesto.naziv, adresa: mesto.adresa });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setGreska('');
    try {
      if (izmenaId) {
        await izmeniMesto(token, izmenaId, forma);
      } else {
        await kreirajMesto(token, forma);
      }
      setForma(PRAZNA_FORMA);
      setIzmenaId(null);
      ucitaj();
    } catch (error) {
      setGreska(error.message);
    }
  }

  async function handleObrisi(id) {
    if (!window.confirm('Obrisati ovo mesto održavanja?')) return;
    setGreska('');
    try {
      await obrisiMesto(token, id);
      ucitaj();
    } catch (error) {
      setGreska(error.message);
    }
  }

  return (
    <div>
      <h1>Mesta održavanja</h1>
      <Poruka>{greska}</Poruka>

      <Kartica naslov={izmenaId ? 'Izmena mesta' : 'Novo mesto održavanja'}>
        <form onSubmit={handleSubmit} className="forma forma--red">
          <label>
            Naziv
            <input value={forma.naziv} onChange={handleChange('naziv')} required />
          </label>
          <label>
            Adresa
            <input value={forma.adresa} onChange={handleChange('adresa')} required />
          </label>
          <button type="submit" className="dugme dugme--primarno">
            {izmenaId ? 'Sačuvaj izmene' : 'Dodaj mesto'}
          </button>
          {izmenaId && (
            <button type="button" className="dugme dugme--sekundarno" onClick={() => { setIzmenaId(null); setForma(PRAZNA_FORMA); }}>
              Otkaži
            </button>
          )}
        </form>
      </Kartica>

      <table className="admin-tabela">
        <thead>
          <tr>
            <th>Naziv</th>
            <th>Adresa</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {mesta.map((mesto) => (
            <tr key={mesto.id}>
              <td>{mesto.naziv}</td>
              <td>{mesto.adresa}</td>
              <td className="kolona-akcije">
                <button className="dugme dugme--sekundarno dugme--malo" onClick={() => handleIzmeni(mesto)}>
                  Izmeni
                </button>
                <button className="dugme dugme--opasnost dugme--malo" onClick={() => handleObrisi(mesto.id)}>
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
