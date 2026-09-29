import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function AdminLayout({ children }) {
  const { nalog, odjaviSe } = useAuth();
  const navigate = useNavigate();

  function handleOdjava() {
    odjaviSe();
    navigate('/');
  }

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-sidebar__brand">
          <span className="navbar__brand-oznaka">FON</span>
          <span>Administracija</span>
        </div>
        <nav className="admin-sidebar__nav">
          <NavLink to="/admin" end>
            Pregled
          </NavLink>
          <NavLink to="/admin/termini">Ispitni termini</NavLink>
          <NavLink to="/admin/mesta">Mesta održavanja</NavLink>
          <NavLink to="/admin/cenovnik">Cenovnik</NavLink>
          <NavLink to="/admin/prijave">Prijave i uplate</NavLink>
          <NavLink to="/admin/statistika">Statistika</NavLink>
        </nav>
        <div className="admin-sidebar__nalog">
          <span>{nalog?.ime} {nalog?.prezime}</span>
          <button type="button" className="dugme dugme--sekundarno" onClick={handleOdjava}>
            Odjava
          </button>
        </div>
      </aside>
      <main className="admin-sadrzaj">{children}</main>
    </div>
  );
}
