import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children, uloga }) {
  const { jeUlogovan, ucitavanje, nalog } = useAuth();

  if (ucitavanje) return null;
  if (!jeUlogovan) return <Navigate to="/prijava" replace />;
  if (uloga && nalog.userType !== uloga) return <Navigate to="/" replace />;

  return children;
}
