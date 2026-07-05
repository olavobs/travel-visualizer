export const CURRENCIES = [
  { code: 'BRL', label: 'BRL – R$' },
  { code: 'USD', label: 'USD – US$' },
  { code: 'EUR', label: 'EUR – €' },
  { code: 'GBP', label: 'GBP – £' },
];

const SYMBOLS = { BRL: 'R$', USD: 'US$', EUR: '€', GBP: '£' };

export function formatPrice(price, currency) {
  if (price == null) return null;
  const symbol = SYMBOLS[currency] ?? currency;
  return `${symbol} ${Number(price).toFixed(2)}`;
}
