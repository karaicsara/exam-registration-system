import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { jeUlogovan, jeKandidat, nalog, odjaviSe } = useAuth();
  const navigate = useNavigate();

  function handleOdjava() {
    odjaviSe();
    navigate('/');
  }

  return (
    <header className="navbar">
      <div className="navbar__brand">
        <NavLink to="/">
          <span className="navbar__brand-oznaka">FON</span>
          <span className="navbar__brand-tekst">Probni prijemni ispit</span>
        </NavLink>
      </div>

      <nav className="navbar__linkovi">
        <NavLink to="/" end>
          Početna
        </NavLink>
        <NavLink to="/termini">Termini i cene</NavLink>
        {jeKandidat && <NavLink to="/moje-prijave">Moje prijave</NavLink>}
        {jeKandidat && <NavLink to="/profil">Profil</NavLink>}
      </nav>

      <div className="navbar__nalog">
        {jeUlogovan ? (
          <>
            <span className="navbar__pozdrav">Zdravo, {nalog.ime}</span>
            <button type="button" className="dugme dugme--sekundarno" onClick={handleOdjava}>
              Odjava
            </button>
          </>
        ) : (
          <>
            <NavLink to="/prijava" className="dugme dugme--sekundarno">
              Prijava
            </NavLink>
            <NavLink to="/registracija" className="dugme dugme--primarno">
              Registracija
            </NavLink>
          </>
        )}
      </div>
    </header>
  );
}
