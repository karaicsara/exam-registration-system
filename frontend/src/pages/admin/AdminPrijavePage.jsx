import { Fragment, useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getSviTipovi } from '../../api/tipoviIspita';
import { pretraziPrijave } from '../../api/prijave';
import { odobriUplatu, odbijUplatu } from '../../api/uplate';
import { BASE_URL } from '../../api/client';
import { formatujCenu, formatujDatum } from '../../utils/format';
import { prijavaStatusInfo, uplataStatusInfo } from '../../utils/status';
import StatusPill from '../../components/StatusPill';
import Poruka from '../../components/Poruka';

const PRAZNI_FILTERI = { ime: '', email: '', tipIspitaId: '', datumTermina: '', status: '' };

export default function AdminPrijavePage() {
  const { token } = useAuth();
  const [tipovi, setTipovi] = useState([]);
  const [filteri, setFilteri] = useState(PRAZNI_FILTERI);
  const [prijave, setPrijave] = useState([]);
  const [prosirena, setProsirena] = useState(null);
  const [greska, setGreska] = useState('');
  const [akcijaId, setAkcijaId] = useState(null);

  function pretrazi() {
    pretraziPrijave(token, filteri).then(setPrijave).catch((e) => setGreska(e.message));
  }

  useEffect(() => {
    getSviTipovi().then(setTipovi).catch(() => {});
    pretrazi();
  }, [token]);

  function handleChange(polje) {
    return (event) => setFilteri((prethodno) => ({ ...prethodno, [polje]: event.target.value }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    pretrazi();
  }

  async function handleOdobri(uplataId) {
    setAkcijaId(uplataId);
    setGreska('');
    try {
      await odobriUplatu(token, uplataId);
      pretrazi();
    } catch (error) {
      setGreska(error.message);
    } finally {
      setAkcijaId(null);
    }
  }

  async function handleOdbij(uplataId) {
    const razlog = window.prompt('Unesite razlog odbijanja uplate:');
    if (!razlog) return;
    setAkcijaId(uplataId);
    setGreska('');
    try {
      await odbijUplatu(token, uplataId, razlog);
      pretrazi();
    } catch (error) {
      setGreska(error.message);
    } finally {
      setAkcijaId(null);
    }
  }

  return (
    <div>
      <h1>Prijave i uplate</h1>
      <Poruka>{greska}</Poruka>

      <form onSubmit={handleSubmit} className="forma forma--red forma--filter">
        <input placeholder="Ime ili prezime" value={filteri.ime} onChange={handleChange('ime')} />
        <input placeholder="Email" value={filteri.email} onChange={handleChange('email')} />
        <select value={filteri.tipIspitaId} onChange={handleChange('tipIspitaId')}>
          <option value="">Sve oblasti</option>
          {tipovi.map((tip) => (
            <option key={tip.id} value={tip.id}>{tip.naziv}</option>
          ))}
        </select>
        <input type="date" value={filteri.datumTermina} onChange={handleChange('datumTermina')} />
        <select value={filteri.status} onChange={handleChange('status')}>
          <option value="">Svi statusi</option>
          <option value="KREIRANA">Kreirana</option>
          <option value="PRIJAVLJENA">Prijavljena</option>
          <option value="OTKAZANA">Otkazana</option>
        </select>
        <button type="submit" className="dugme dugme--primarno">Pretraži</button>
      </form>

      <table className="admin-tabela">
        <thead>
          <tr>
            <th>Kandidat</th>
            <th>Datum prijave</th>
            <th>Za uplatu</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {prijave.map((prijava) => {
            const status = prijavaStatusInfo(prijava.status);
            const prosirenaVrsta = prosirena === prijava.id;
            return (
              <Fragment key={prijava.id}>
                <tr>
                  <td>{prijava.kandidatIme} {prijava.kandidatPrezime} <span className="tekst-prigušen">({prijava.kandidatEmail})</span></td>
                  <td>{formatujDatum(prijava.datumKreiranja)}</td>
                  <td>{formatujCenu(prijava.ukupanIznos)}</td>
                  <td><StatusPill oznaka={status.oznaka} ton={status.ton} /></td>
                  <td>
                    <button className="dugme dugme--sekundarno dugme--malo" onClick={() => setProsirena(prosirenaVrsta ? null : prijava.id)}>
                      {prosirenaVrsta ? 'Sakrij' : 'Detalji'}
                    </button>
                  </td>
                </tr>
                {prosirenaVrsta && (
                  <tr className="red-detalja">
                    <td colSpan={5}>
                      <div className="detalj-prijave">
                        <div>
                          <h4>Izabrani termini</h4>
                          <ul className="stavka-lista">
                            {prijava.stavke.map((stavka) => (
                              <li key={stavka.id}>
                                {stavka.tipIspitaNaziv} — {formatujDatum(stavka.datumOdrzavanja)} u {stavka.vremePocetka}, {stavka.mestoNaziv}
                                <span className="tekst-prigušen"> ({formatujCenu(stavka.cenaNaDanPrijave)})</span>
                              </li>
                            ))}
                          </ul>
                          <p>Bruto: {formatujCenu(prijava.brutoIznos)} · Popust: {formatujCenu(prijava.popust)} · Ukupno: <strong>{formatujCenu(prijava.ukupanIznos)}</strong></p>
                        </div>
                        <div>
                          <h4>Dokazi o uplati</h4>
                          {prijava.uplate.length === 0 && <p className="tekst-prigušen">Kandidat još nije dostavio dokaz o uplati.</p>}
                          {prijava.uplate.map((uplata) => {
                            const uStatus = uplataStatusInfo(uplata.status);
                            return (
                              <div key={uplata.id} className="uplata-red">
                                <StatusPill oznaka={uStatus.oznaka} ton={uStatus.ton} />
                                <a href={`${BASE_URL}${uplata.putanjaDokumenta}`} target="_blank" rel="noreferrer">Pregled dokumenta</a>
                                <span className="tekst-prigušen">{formatujDatum(uplata.datumSlanja)}</span>
                                {uplata.status === 'NA_CEKANJU' && (
                                  <span className="kolona-akcije">
                                    <button className="dugme dugme--primarno dugme--malo" disabled={akcijaId === uplata.id} onClick={() => handleOdobri(uplata.id)}>
                                      Odobri
                                    </button>
                                    <button className="dugme dugme--opasnost dugme--malo" disabled={akcijaId === uplata.id} onClick={() => handleOdbij(uplata.id)}>
                                      Odbij
                                    </button>
                                  </span>
                                )}
                                {uplata.napomena && <p className="tekst-prigušen">{uplata.napomena}</p>}
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    </td>
                  </tr>
                )}
              </Fragment>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
