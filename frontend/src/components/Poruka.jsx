export default function Poruka({ tip = 'greska', children }) {
  if (!children) return null;
  return <div className={`poruka poruka--${tip}`}>{children}</div>;
}
