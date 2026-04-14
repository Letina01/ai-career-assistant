export default function SectionCard({ title, subtitle, children, className = '' }) {
  return (
    <section className={`card section-card ${className}`}>
      <div className="card-head">
        <div>
          <h2>{title}</h2>
          {subtitle && <p className="muted">{subtitle}</p>}
        </div>
      </div>
      {children}
    </section>
  );
}
