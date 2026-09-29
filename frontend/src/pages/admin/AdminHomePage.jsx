import { Link } from 'react-router-dom';
import Kartica from '../../components/Kartica';

const STAVKE = [
  { put: '/admin/termini', naslov: 'Ispitni termini', opis: 'Kreiranje, izmena i brisanje termina po oblastima i lokacijama.' },
  { put: '/admin/mesta', naslov: 'Mesta održavanja', opis: 'Upravljanje salama i amfiteatrima na kojima se termini održavaju.' },
  { put: '/admin/cenovnik', naslov: 'Cenovnik', opis: 'Definisanje i izmena aktivnih cena po oblasti ispita.' },
  { put: '/admin/prijave', naslov: 'Prijave i uplate', opis: 'Pretraga prijava, pregled detalja i odlučivanje o dostavljenim uplatama.' },
  { put: '/admin/statistika', naslov: 'Statistika', opis: 'Prihod po oblastima, status uplata i najtraženiji termini.' },
];

export default function AdminHomePage() {
  return (
    <div>
      <h1>Administratorski pregled</h1>
      <p className="tekst-prigušen">Izaberite oblast rada iz menija sa leve strane ili sa kartica ispod.</p>
      <div className="karta-grid">
        {STAVKE.map((stavka) => (
          <Link key={stavka.put} to={stavka.put} className="karta-link">
            <Kartica naslov={stavka.naslov}>
              <p>{stavka.opis}</p>
            </Kartica>
          </Link>
        ))}
      </div>
    </div>
  );
}
