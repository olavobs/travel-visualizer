import { useState, useRef, useEffect } from 'react';
import AIRPORTS from '../data/airports.js';

function search(query) {
  const q = query.trim().toUpperCase();
  if (!q) return [];
  return AIRPORTS.filter(a =>
    a.code.startsWith(q) ||
    a.city.toUpperCase().includes(q) ||
    a.name.toUpperCase().includes(q)
  ).slice(0, 8);
}

export default function AirportInput({ value, onChange, placeholder, autoFocus }) {
  const [query, setQuery]       = useState(value || '');
  const [results, setResults]   = useState([]);
  const [open, setOpen]         = useState(false);
  const containerRef            = useRef(null);

  useEffect(() => {
    function handleClickOutside(e) {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleChange = (e) => {
    const val = e.target.value.toUpperCase().slice(0, 3);
    setQuery(val);
    const exact = AIRPORTS.find(a => a.code === val);
    onChange(exact ? exact.code : '');
    const hits = search(val);
    setResults(hits);
    setOpen(hits.length > 0);
  };

  const handleSelect = (airport) => {
    setQuery(airport.code);
    onChange(airport.code);
    setResults([]);
    setOpen(false);
  };

  const isValid = AIRPORTS.some(a => a.code === query);

  return (
    <div className="airport-input" ref={containerRef}>
      <input
        className={`cr-input${isValid ? ' cr-input--valid' : ''}`}
        type="text"
        placeholder={placeholder}
        value={query}
        onChange={handleChange}
        onFocus={() => query && setOpen(results.length > 0)}
        autoFocus={autoFocus}
        autoComplete="off"
        spellCheck={false}
      />
      {open && (
        <ul className="airport-dropdown">
          {results.map(a => (
            <li key={a.code} className="airport-option" onMouseDown={() => handleSelect(a)}>
              <span className="airport-option__code">{a.code}</span>
              <span className="airport-option__info">{a.city} — {a.name}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
