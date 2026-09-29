export default function Kartica({ naslov, akcija, children, className = '' }) {
  return (
    <section className={`kartica ${className}`}>
      {(naslov || akcija) && (
        <header className="kartica__zaglavlje">
          {naslov && <h3>{naslov}</h3>}
          {akcija}
        </header>
      )}
      <div className="kartica__telo">{children}</div>
    </section>
  );
}
