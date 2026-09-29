import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as kandidatApi from '../api/kandidati';
import { useAuth } from '../context/AuthContext';
import Poruka from '../components/Poruka';
import Kartica from '../components/Kartica';

const PRAZNA_FORMA = { ime: '', prezime: '', telefon: '', username: '', password: '', email: '' };

export default function RegisterPage() {
  const { postaviNalog } = useAuth();
  const navigate = useNavigate();

  const [korak, setKorak] = useState('podaci');
  const [forma, setForma] = useState(PRAZNA_FORMA);
  const [kod, setKod] = useState('');
  const [greska, setGreska] = useState('');
  const [poruka, setPoruka] = useState('');
  const [ucitavanje, setUcitavanje] = useState(false);

  function handleChange(polje) {
    return (event) => setForma((prethodno) => ({ ...prethodno, [polje]: event.target.value }));
  }

  async function handlePodaciSubmit(event) {
    event.preventDefault();
    setGreska('');
    setUcitavanje(true);
    try {
      const odgovor = await kandidatApi.zapocniRegistraciju(forma);
      setPoruka(odgovor.poruka);
      setKorak('verifikacija');
    } catch (error) {
      setGreska(error.message);
    } finally {
      setUcitavanje(false);
    }
  }

  async function handleVerifikacijaSubmit(event) {
    event.preventDefault();
    setGreska('');
    setUcitavanje(true);
    try {
      const odgovor = await kandidatApi.verifikujKod(forma.email, kod);
      postaviNalog(odgovor);
      navigate('/moje-prijave');
    } catch (error) {
      setGreska(error.message);
    } finally {
      setUcitavanje(false);
    }
  }

  async function handlePonovniKod() {
    setGreska('');
    try {
      const odgovor = await kandidatApi.ponovoPosaljiKod(forma.email);
      setPoruka(odgovor.poruka);
    } catch (error) {
      setGreska(error.message);
    }
  }

  return (
    <div className="stranica-uska">
      <Kartica naslov="Registracija kandidata">
        {korak === 'podaci' && (
          <form onSubmit={handlePodaciSubmit} className="forma">
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
              <input value={forma.telefon} onChange={handleChange('telefon')} placeholder="06x xxx xxxx" />
            </label>
            <label>
              Korisničko ime
              <input value={forma.username} onChange={handleChange('username')} required />
            </label>
            <label>
              Lozinka
              <input type="password" value={forma.password} onChange={handleChange('password')} required minLength={6} />
            </label>
            <label>
              Email adresa
              <input type="email" value={forma.email} onChange={handleChange('email')} required />
            </label>

            <Poruka>{greska}</Poruka>

            <button type="submit" className="dugme dugme--primarno" disabled={ucitavanje}>
              {ucitavanje ? 'Slanje...' : 'Pošalji verifikacioni kod'}
            </button>
          </form>
        )}

        {korak === 'verifikacija' && (
          <form onSubmit={handleVerifikacijaSubmit} className="forma">
            <Poruka tip="uspeh">{poruka}</Poruka>
            <p>
              Unesite šestocifreni kod poslat na adresu <strong>{forma.email}</strong>.
            </p>
            <label>
              Verifikacioni kod
              <input value={kod} onChange={(e) => setKod(e.target.value)} required maxLength={6} autoFocus />
            </label>

            <Poruka>{greska}</Poruka>

            <button type="submit" className="dugme dugme--primarno" disabled={ucitavanje}>
              {ucitavanje ? 'Potvrđivanje...' : 'Potvrdi i završi registraciju'}
            </button>
            <button type="button" className="dugme dugme--sekundarno" onClick={handlePonovniKod}>
              Pošalji kod ponovo
            </button>
          </form>
        )}
      </Kartica>
    </div>
  );
}
