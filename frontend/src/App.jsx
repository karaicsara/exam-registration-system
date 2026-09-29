import { Routes, Route } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import AdminLayout from './components/admin/AdminLayout';

import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import TerminiPage from './pages/TerminiPage';
import MojePrijavePage from './pages/MojePrijavePage';
import ProfilPage from './pages/ProfilPage';

import AdminHomePage from './pages/admin/AdminHomePage';
import AdminTerminiPage from './pages/admin/AdminTerminiPage';
import AdminTerminDetaljiPage from './pages/admin/AdminTerminDetaljiPage';
import AdminMestaPage from './pages/admin/AdminMestaPage';
import AdminCenovnikPage from './pages/admin/AdminCenovnikPage';
import AdminPrijavePage from './pages/admin/AdminPrijavePage';
import AdminStatistikaPage from './pages/admin/AdminStatistikaPage';

import './App.css';

function admin(element) {
  return (
    <ProtectedRoute uloga="ADMINISTRATOR">
      <AdminLayout>{element}</AdminLayout>
    </ProtectedRoute>
  );
}

export default function App() {
  const { jeAdmin } = useAuth();

  return (
    <div className="app-shell">
      {!jeAdmin && <Navbar />}
      <main className={jeAdmin ? '' : 'app-sadrzaj'}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/prijava" element={<LoginPage />} />
          <Route path="/registracija" element={<RegisterPage />} />
          <Route path="/termini" element={<TerminiPage />} />
          <Route
            path="/moje-prijave"
            element={
              <ProtectedRoute uloga="KANDIDAT">
                <MojePrijavePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profil"
            element={
              <ProtectedRoute uloga="KANDIDAT">
                <ProfilPage />
              </ProtectedRoute>
            }
          />

          <Route path="/admin" element={admin(<AdminHomePage />)} />
          <Route path="/admin/termini" element={admin(<AdminTerminiPage />)} />
          <Route path="/admin/termini/:id" element={admin(<AdminTerminDetaljiPage />)} />
          <Route path="/admin/mesta" element={admin(<AdminMestaPage />)} />
          <Route path="/admin/cenovnik" element={admin(<AdminCenovnikPage />)} />
          <Route path="/admin/prijave" element={admin(<AdminPrijavePage />)} />
          <Route path="/admin/statistika" element={admin(<AdminStatistikaPage />)} />
        </Routes>
      </main>
    </div>
  );
}
