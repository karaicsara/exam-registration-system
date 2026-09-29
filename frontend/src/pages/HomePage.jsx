import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getSviTipovi } from '../api/tipoviIspita';
import { getAktivneCene } from '../api/cenovnici';
import { getSviTermini } from '../api/ispitniTermini';
import { formatujCenu, formatujDatum } from '../utils/format';
import { useAuth } from '../context/AuthContext';
import Kartica from '../components/Kartica';

export default function HomePage() {
  const { jeUlogovan } = useAuth();
  const [tipovi, setTipovi] = useState([]);
  const [cene, setCene] = useState([]);
  const [termini, setTermini] = useState([]);

  useEffect(() => {
    getSviTipovi().then(setTipovi).catch(() => {});
    getAktivneCene().then(setCene).catch(() => {});
    getSviTermini().then(setTermini).catch(() => {});
  }, []);

  const nadolazeciTermini = [...termini]
    .filter((t) => t.mozeSeIzabrati)
    .sort((a, b) => a.datumOdrzavanja.localeCompare(b.datumOdrzavanja))
    .slice(0, 4);

  return (
    <div className="pocetna">
      <section className="hero">
        <div className="hero__tekst">
          <p className="hero__oznaka">Fakultet organizacionih nauka</p>
          <h1>Prijava na probni prijemni ispit</h1>
          <p className="hero__opis">
            Proverite svoje znanje pre pravog prijemnog ispita. Izaberite termin iz matematike ili testa opšte
            informisanosti, prijavite se elektronski i pratite status svoje prijave i uplate na jednom mestu.
          </p>
          <div className="hero__akcije">
            {!jeUlogovan && (
              <Link to="/registracija" className="dugme dugme--primarno dugme--veliko">
                Registruj se kao kandidat
              </Link>
            )}
            <Link
              to="/termini"
              className={`dugme dugme--veliko ${jeUlogovan ? 'dugme--primarno' : 'dugme--sekundarno'}`}
            >
              Pregledaj termine
            </Link>
          </div>
        </div>
      </section>

      <section className="pocetna__oblasti">
        {tipovi.map((tip) => {
          const cena = cene.find((c) => c.tipIspitaId === tip.id);
          return (
            <Kartica key={tip.id} naslov={tip.naziv} className="kartica--oblast">
              <p className="kartica__cena">{cena ? formatujCenu(cena.iznos) : 'Cena uskoro dostupna'}</p>
              <p>Probni ispit koji simulira uslove pravog prijemnog ispita, uz detaljnu povratnu informaciju.</p>
            </Kartica>
          );
        })}
      </section>

      <section className="pocetna__termini">
        <h2>Najbliži slobodni termini</h2>
        {nadolazeciTermini.length === 0 && <p>Trenutno nema slobodnih termina za prikaz.</p>}
        <div className="karta-grid">
          {nadolazeciTermini.map((termin) => (
            <Kartica key={termin.id} naslov={termin.tipIspitaNaziv}>
              <p>{formatujDatum(termin.datumOdrzavanja)} u {termin.vremePocetka}</p>
              <p className="tekst-prigušen">{termin.mestoNaziv}</p>
              <p className="kartica__cena">{formatujCenu(termin.trenutnaCena)}</p>
            </Kartica>
          ))}
        </div>
      </section>

      <section className="pocetna__info">
        <Kartica naslov="Kako izgleda proces prijave?">
          <ol className="koraci">
            <li>Registrujte nalog i verifikujte email adresu kodom koji dobijate na mejl.</li>
            <li>Izaberite jedan ili više termina iz raspoloživih oblasti ispita.</li>
            <li>Sistem obračunava ukupnu cenu, uz eventualni popust za kombinovanu prijavu.</li>
            <li>Uplatite iznos i dostavite dokaz o uplati kroz aplikaciju.</li>
            <li>Nakon što administrator evidentira uplatu, vaša prijava dobija status „Prijavljen”.</li>
          </ol>
        </Kartica>
      </section>
    </div>
  );
}
