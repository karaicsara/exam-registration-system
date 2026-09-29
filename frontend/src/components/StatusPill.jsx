export default function StatusPill({ oznaka, ton = 'neutralno' }) {
  return <span className={`status-pill status-pill--${ton}`}>{oznaka}</span>;
}
