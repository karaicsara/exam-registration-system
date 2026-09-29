import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Poruka from '../components/Poruka';
import Kartica from '../components/Kartica';

export default function LoginPage() {
  const { prijaviSeKaoKandidat, prijaviSeKaoAdministrator } = useAuth();
  const navigate = useNavigate();

  const [uloga, setUloga] = useState('KANDIDAT');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [greska, setGreska] = useState('');
  const [ucitavanje, setUcitavanje] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setGreska('');
    setUcitavanje(true);
    try {
      if (uloga === 'KANDIDAT') {
        await prijaviSeKaoKandidat(username, password);
        navigate('/moje-prijave');
      } else {
        await prijaviSeKaoAdministrator(username, password);
        navigate('/admin');
      }
    } catch (error) {
      setGreska(error.message);
    } finally {
      setUcitavanje(false);
    }
  }

  return (
    <div className="stranica-uska">
      <Kartica naslov="Prijava na sistem">
        <div className="uloga-prekidac">
          <button
            type="button"
            className={uloga === 'KANDIDAT' ? 'aktivno' : ''}
            onClick={() => setUloga('KANDIDAT')}
          >
            Kandidat
          </button>
          <button
            type="button"
            className={uloga === 'ADMINISTRATOR' ? 'aktivno' : ''}
            onClick={() => setUloga('ADMINISTRATOR')}
          >
            Administrator
          </button>
        </div>

        <form onSubmit={handleSubmit} className="forma">
          <label>
            Korisničko ime
            <input value={username} onChange={(e) => setUsername(e.target.value)} required autoFocus />
          </label>
          <label>
            Lozinka
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </label>

          <Poruka>{greska}</Poruka>

          <button type="submit" className="dugme dugme--primarno" disabled={ucitavanje}>
            {ucitavanje ? 'Prijavljivanje...' : 'Prijavi se'}
          </button>
        </form>

        {uloga === 'KANDIDAT' && (
          <p className="tekst-prigušen">
            Nemate nalog? <Link to="/registracija">Registrujte se</Link>
          </p>
        )}
      </Kartica>
    </div>
  );
}
