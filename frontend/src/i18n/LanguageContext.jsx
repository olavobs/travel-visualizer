import { createContext, useContext, useState } from 'react';
import translations from './translations';

const LanguageContext = createContext(null);

export function LanguageProvider({ children }) {
  const [lang, setLang] = useState(() => localStorage.getItem('lang') ?? 'en');

  const switchLang = (l) => {
    localStorage.setItem('lang', l);
    setLang(l);
  };

  return (
    <LanguageContext.Provider value={{ lang, switchLang }}>
      {children}
    </LanguageContext.Provider>
  );
}

export function useT() {
  const { lang } = useContext(LanguageContext);
  const dict = translations[lang] ?? translations.en;

  return function t(path, vars) {
    const keys = path.split('.');
    let val = dict;
    for (const k of keys) val = val?.[k];
    if (val == null) {
      const fallback = keys.reduce((o, k) => o?.[k], translations.en);
      val = fallback ?? path;
    }
    if (vars) {
      return String(val).replace(/\{(\w+)\}/g, (_, k) => vars[k] ?? `{${k}}`);
    }
    return String(val);
  };
}

export function useLang() {
  return useContext(LanguageContext);
}
