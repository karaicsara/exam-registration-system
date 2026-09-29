import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, LabelList, ResponsiveContainer } from 'recharts';
import { useAuth } from '../../context/AuthContext';
import { getStatistika as getPrijavaStatistika } from '../../api/prijave';
import { getStatistika as getUplataStatistika } from '../../api/uplate';
import { getStatistika as getTerminStatistika } from '../../api/ispitniTermini';
import { formatujCenu, formatujDatum } from '../../utils/format';
import Kartica from '../../components/Kartica';
import Poruka from '../../components/Poruka';

export default function AdminStatistikaPage() {
  const { token } = useAuth();
  const [prihod, setPrihod] = useState(null);
  const [uplate, setUplate] = useState(null);
  const [termini, setTermini] = useState(null);
  const [greska, setGreska] = useState('');

  useEffect(() => {
    Promise.all([getPrijavaStatistika(token), getUplataStatistika(token), getTerminStatistika(token)])
      .then(([p, u, t]) => {
        setPrihod(p);
        setUplate(u);
        setTermini(t);
      })
      .catch((e) => setGreska(e.message));
  }, [token]);

  if (greska) return <Poruka>{greska}</Poruka>;
  if (!prihod || !uplate || !termini) return <p>Učitavanje statistike...</p>;

  const ukupanNetoPrihod = prihod.prihodPoOblasti.reduce((zbir, stavka) => zbir + stavka.netoIznos, 0);
  const najveciTermin = termini.najtrazenijiTermini[0];

  return (
    <div>
      <h1>Statistika</h1>

      <div className="stat-red">
        <Kartica className="stat-kartica" naslov="Ukupan evidentiran prihod">
          <p className="stat-vrednost">{formatujCenu(ukupanNetoPrihod)}</p>
          <p className="tekst-prigušen">Neto iznos (nakon popusta), otkazane prijave nisu uračunate.</p>
        </Kartica>
        <Kartica className="stat-kartica" naslov="Uplate na čekanju">
          <p className="stat-vrednost stat-vrednost--cekanje">{uplate.brojNaCekanju}</p>
          <p className="tekst-prigušen">Čekaju odluku administratora.</p>
        </Kartica>
        <Kartica className="stat-kartica" naslov="Procenat odobrenih uplata">
          <p className="stat-vrednost stat-vrednost--uspeh">{uplate.procenatOdobrenih.toFixed(0)}%</p>
          <p className="tekst-prigušen">{uplate.brojOdobrenih} odobrenih / {uplate.brojOdbijenih} odbijenih</p>
        </Kartica>
        <Kartica className="stat-kartica" naslov="Popunjenost termina">
          <p className="stat-vrednost">{termini.procenatDostupnihTermina.toFixed(0)}%</p>
          <p className="tekst-prigušen">termina je i dalje slobodno za prijavu, od ukupno {termini.brojTermina}</p>
        </Kartica>
      </div>

      <div className="stat-sekcije">
        <Kartica naslov="Prihod po oblasti ispita" className="kartica--grafik">
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={prihod.prihodPoOblasti} margin={{ top: 20, right: 20, left: 0, bottom: 10 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="tipIspitaNaziv" />
              <YAxis allowDecimals={false} />
              <Tooltip formatter={(vrednost) => formatujCenu(vrednost)} />
              <Bar dataKey="netoIznos" name="Neto prihod" fill="var(--boja-grafik-1)" radius={[6, 6, 0, 0]}>
                <LabelList dataKey="netoIznos" position="top" formatter={formatujCenu} />
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </Kartica>

        <Kartica naslov="Odnos statusa uplata" className="kartica--grafik">
          <div className="uplata-status-traka">
            <UplataStatusSegment naziv="Na čekanju" broj={uplate.brojNaCekanju} boja="var(--boja-cekanje)" />
            <UplataStatusSegment naziv="Odobrene" broj={uplate.brojOdobrenih} boja="var(--boja-uspeh)" />
            <UplataStatusSegment naziv="Odbijene" broj={uplate.brojOdbijenih} boja="var(--boja-greska)" />
          </div>
          <ul className="legenda">
            <li><span className="legenda__tacka" style={{ background: 'var(--boja-cekanje)' }} /> Na čekanju — {uplate.brojNaCekanju}</li>
            <li><span className="legenda__tacka" style={{ background: 'var(--boja-uspeh)' }} /> Odobrene — {uplate.brojOdobrenih}</li>
            <li><span className="legenda__tacka" style={{ background: 'var(--boja-greska)' }} /> Odbijene — {uplate.brojOdbijenih}</li>
          </ul>
        </Kartica>

        <Kartica naslov="Najtraženiji termini" className="kartica--grafik kartica--siroka">
          {termini.najtrazenijiTermini.length === 0 && <p className="tekst-prigušen">Još uvek nema prijava na terminima.</p>}
          <ol className="rang-lista">
            {termini.najtrazenijiTermini.map((termin) => (
              <li key={termin.terminId}>
                <span className="rang-lista__naziv">
                  {termin.tipIspitaNaziv} — {formatujDatum(termin.datumOdrzavanja)}, {termin.mestoNaziv}
                </span>
                <span className="rang-lista__traka">
                  <span
                    className="rang-lista__ispuna"
                    style={{ width: `${(termin.brojPrijava / (najveciTermin?.brojPrijava || 1)) * 100}%` }}
                  />
                </span>
                <span className="rang-lista__broj">{termin.brojPrijava}</span>
              </li>
            ))}
          </ol>
        </Kartica>
      </div>
    </div>
  );
}

function UplataStatusSegment({ naziv, broj, boja }) {
  return (
    <div className="uplata-status-segment" style={{ flexGrow: Math.max(broj, 0.3) }}>
      <div className="uplata-status-segment__traka" style={{ background: boja }} />
      <span>{naziv}: {broj}</span>
    </div>
  );
}
