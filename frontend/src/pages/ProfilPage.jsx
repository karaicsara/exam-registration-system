import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import * as kandidatApi from '../api/kandidati';
import Kartica from '../components/Kartica';
import Poruka from '../components/Poruka';

export default function ProfilPage() {
  const { token } = useAuth();
  const [profil, setProfil] = useState(null);
  const [forma, setForma] = useState({ ime: '', prezime: '', telefon: '' });
  const [greska, setGreska] = useState('');
  const [poruka, setPoruka] = useState('');
  const [cuvanje, setCuvanje] = useState(false);

  useEffect(() => {
    kandidatApi.getMojiPodaci(token).then((podaci) => {
      setProfil(podaci);
      setForma({ ime: podaci.ime, prezime: podaci.prezime, telefon: podaci.telefon ?? '' });
    });
  }, [token]);

  function handleChange(polje) {
    return (event) => setForma((prethodno) => ({ ...prethodno, [polje]: event.target.value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setGreska('');
    setPoruka('');
    setCuvanje(true);
    try {
      const azurirano = await kandidatApi.azurirajMojePodatke(token, forma);
      setProfil(azurirano);
      setPoruka('Podaci su uspešno sačuvani.');
    } catch (error) {
      setGreska(error.message);
    } finally {
      setCuvanje(false);
    }
  }

  if (!profil) return <p className="stranica">Učitavanje profila...</p>;

  return (
    <div className="stranica-uska">
      <Kartica naslov="Moj profil">
        <p className="tekst-prigušen">
          Korisničko ime i email su vezani za vaš nalog i ne mogu se menjati: <strong>{profil.username}</strong> / {profil.email}
        </p>

        <form onSubmit={handleSubmit} className="forma">
          <label>
            Ime
            <input value={forma.ime} onChange={handleChange('ime')} required />
          </label>
          <label>
            Prezime
            <input value={forma.prezime} onChange={handleChange('prezime')} required />
          </label>
          <label>
            Telefon
            <input value={forma.telefon} onChange={handleChange('telefon')} />
          </label>

          <Poruka>{greska}</Poruka>
          <Poruka tip="uspeh">{poruka}</Poruka>

          <button type="submit" className="dugme dugme--primarno" disabled={cuvanje}>
            {cuvanje ? 'Čuvanje...' : 'Sačuvaj izmene'}
          </button>
        </form>
      </Kartica>
    </div>
  );
}
