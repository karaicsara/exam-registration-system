import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getDetaljiTermina, preuzmiPdfSpiska } from '../../api/ispitniTermini';
import { formatujDatum } from '../../utils/format';
import { prijavaStatusInfo, uplataStatusInfo } from '../../utils/status';
import StatusPill from '../../components/StatusPill';
import Poruka from '../../components/Poruka';
import Kartica from '../../components/Kartica';

export default function AdminTerminDetaljiPage() {
  const { id } = useParams();
  const { token } = useAuth();
  const [detalji, setDetalji] = useState(null);
  const [greska, setGreska] = useState('');
  const [preuzimanje, setPreuzimanje] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- podaci se osvežavaju pri promeni izabranog termina
    setDetalji(null);
    setGreska('');
    getDetaljiTermina(token, id).then(setDetalji).catch((e) => setGreska(e.message));
  }, [token, id]);

  async function handlePreuzmiPdf() {
    setPreuzimanje(true);
    setGreska('');
    try {
      await preuzmiPdfSpiska(token, id);
    } catch (error) {
      setGreska(error.message);
    } finally {
      setPreuzimanje(false);
    }
  }

  if (greska && !detalji) {
    return (
      <div>
        <Poruka>{greska}</Poruka>
        <Link to="/admin/termini">Nazad na termine</Link>
      </div>
    );
  }
  if (!detalji) return <p>Učitavanje detalja termina...</p>;

  return (
    <div>
      <div className="stranica__zaglavlje">
        <h1>Detalji termina</h1>
        <Link to="/admin/termini" className="dugme dugme--sekundarno dugme--malo">
          Nazad na termine
        </Link>
      </div>

      <Poruka>{greska}</Poruka>

      <Kartica naslov={`${detalji.tipIspitaNaziv} — ${formatujDatum(detalji.datumOdrzavanja)} u ${detalji.vremePocetka}`}>
        <p>
          <strong>Mesto održavanja:</strong> {detalji.mestoNaziv}, {detalji.mestoAdresa}
        </p>
        <p>
          <strong>Ukupan broj prijavljenih kandidata:</strong> {detalji.brojPrijavljenih}
        </p>
        <div className="kartica__akcije">
          <button type="button" className="dugme dugme--primarno" onClick={handlePreuzmiPdf} disabled={preuzimanje}>
            {preuzimanje ? 'Priprema PDF-a...' : 'Preuzmi PDF'}
          </button>
        </div>
      </Kartica>

      {detalji.kandidati.length === 0 ? (
        <Poruka tip="info">Za ovaj termin trenutno nema prijavljenih kandidata.</Poruka>
      ) : (
        <table className="admin-tabela">
          <thead>
            <tr>
              <th>#</th>
              <th>Ime i prezime</th>
              <th>Korisničko ime</th>
              <th>Email</th>
              <th>Telefon</th>
              <th>Datum prijave</th>
              <th>Status prijave</th>
              <th>Status uplate</th>
            </tr>
          </thead>
          <tbody>
            {detalji.kandidati.map((kandidat) => {
              const statusPrijave = prijavaStatusInfo(kandidat.statusPrijave);
              const imaUplatu = kandidat.statusUplate !== 'Nema uplate';
              const statusUplate = imaUplatu ? uplataStatusInfo(kandidat.statusUplate) : null;
              return (
                <tr key={kandidat.redniBroj}>
                  <td>{kandidat.redniBroj}</td>
                  <td>
                    {kandidat.ime} {kandidat.prezime}
                  </td>
                  <td>{kandidat.username}</td>
                  <td>{kandidat.email}</td>
                  <td>{kandidat.telefon || '-'}</td>
                  <td>{formatujDatum(kandidat.datumKreiranjaPrijave)}</td>
                  <td>
                    <StatusPill oznaka={statusPrijave.oznaka} ton={statusPrijave.ton} />
                  </td>
                  <td>
                    {imaUplatu ? (
                      <StatusPill oznaka={statusUplate.oznaka} ton={statusUplate.ton} />
                    ) : (
                      <span className="tekst-prigušen">Nema uplate</span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </div>
  );
}
