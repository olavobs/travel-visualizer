import { useState, useMemo } from 'react';
import { getPriceSummary } from '../api/flightApi';
import { formatPrice } from '../utils/currency';
import { useT } from '../i18n/LanguageContext';

// ─── Layout constants ──────────────────────────────────────────────────────────
const NODE_W  = 120;
const NODE_H  = 52;
const LEVEL_W = 230;
const ROW_H   = 90;
const PAD     = 30;

// ─── Tree building ─────────────────────────────────────────────────────────────

function buildForest(routes) {
  const byOrigin = {};
  for (const r of routes) {
    (byOrigin[r.origin] ??= []).push(r);
  }
  const allDestinations = new Set(routes.map(r => r.destination));
  const roots = [...new Set(routes.map(r => r.origin))].filter(a => !allDestinations.has(a));

  function buildNode(airport, path, seen) {
    const children = (byOrigin[airport] ?? [])
      .filter(r => !seen.has(r.destination))
      .map(r => buildNode(r.destination, [...path, r], new Set([...seen, r.destination])));
    return { airport, path, children, x: 0, y: 0 };
  }

  return roots.map(r => buildNode(r, [], new Set([r])));
}

function applyLayout(forest) {
  let leafIdx = 0;

  function position(node, depth) {
    node.x = PAD + depth * LEVEL_W;
    if (node.children.length === 0) {
      node.y = PAD + leafIdx * ROW_H + ROW_H / 2;
      leafIdx++;
    } else {
      node.children.forEach(c => position(c, depth + 1));
      node.y = (node.children[0].y + node.children[node.children.length - 1].y) / 2;
    }
  }

  forest.forEach(tree => { position(tree, 0); leafIdx++; });

  const nodes = [];
  const edges = [];

  function collect(node) {
    nodes.push(node);
    for (const child of node.children) {
      edges.push({ from: node, to: child, route: child.path.at(-1) });
      collect(child);
    }
  }
  forest.forEach(collect);

  const svgW = nodes.length ? Math.max(...nodes.map(n => n.x)) + NODE_W + PAD : 300;
  const svgH = Math.max(leafIdx * ROW_H + PAD * 2, 120);

  return { nodes, edges, svgW, svgH };
}

// ─── SVG primitives ────────────────────────────────────────────────────────────

function Edge({ from, to, highlighted }) {
  const x1 = from.x + NODE_W, y1 = from.y;
  const x2 = to.x,            y2 = to.y;
  const cx = (x1 + x2) / 2;
  return (
    <path
      d={`M ${x1} ${y1} C ${cx} ${y1}, ${cx} ${y2}, ${x2} ${y2}`}
      fill="none"
      stroke={highlighted ? '#2563eb' : '#cbd5e1'}
      strokeWidth={highlighted ? 2.5 : 1.5}
      strokeLinecap="round"
    />
  );
}

function Node({ node, highlighted, onClick }) {
  const isRoot = node.path.length === 0;
  const isLeaf = node.children.length === 0;
  const route  = node.path.at(-1);

  let fill = '#ffffff', stroke = '#e2e8f0';
  if (highlighted)  { fill = '#2563eb'; stroke = '#1d4ed8'; }
  else if (isLeaf)  { fill = '#f0f9ff'; stroke = '#7dd3fc'; }
  else if (isRoot)  { fill = '#f8fafc'; stroke = '#94a3b8'; }

  return (
    <g
      transform={`translate(${node.x}, ${node.y - NODE_H / 2})`}
      onClick={!isRoot ? onClick : undefined}
      style={{ cursor: !isRoot ? 'pointer' : 'default' }}
    >
      {!isRoot && <rect width={NODE_W} height={NODE_H} rx={8} fill="transparent" />}
      <rect width={NODE_W} height={NODE_H} rx={8} fill={fill} stroke={stroke} strokeWidth={highlighted ? 2 : 1.5} />
      <text x={NODE_W / 2} y={route ? 20 : NODE_H / 2 + 6} textAnchor="middle" fontSize={15} fontWeight="700"
            fill={highlighted ? '#ffffff' : '#0f172a'}>
        {node.airport}
      </text>
      {route && (
        <text x={NODE_W / 2} y={38} textAnchor="middle" fontSize={11} fill={highlighted ? '#bfdbfe' : '#64748b'}>
          {route.travelDate}
        </text>
      )}
    </g>
  );
}

// ─── Path summary panel ────────────────────────────────────────────────────────

function PathSummary({ node, summary, loading, error }) {
  const t = useT();
  const fullPath = [...node.path.map(r => r.origin), node.airport].join(' → ');

  const totalDisplay = useMemo(() => {
    if (!summary) return null;
    const allSegments = summary.summaries.flatMap(s => s.segments ?? []);
    const getBestPrice = (s) => s.purchasedPrice != null ? s.purchasedPrice : s.lowestPrice;
    const getBestCurrency = (s) => s.purchasedPrice != null ? s.purchasedCurrency : s.lowestCurrency;
    const currencies = allSegments.map(getBestCurrency).filter(Boolean);
    const allSame = currencies.length > 0 && currencies.every(c => c === currencies[0]);
    const total = allSegments.reduce((sum, s) => sum + (Number(getBestPrice(s)) || 0), 0);
    return allSame
      ? formatPrice(total, currencies[0])
      : `${total.toFixed(2)} (mixed currencies)`;
  }, [summary]);

  return (
    <div className="path-summary">
      <div className="path-summary-header">
        <span className="path-summary-route">{fullPath}</span>
      </div>

      {loading && <p className="muted">{t('tree.loading')}</p>}
      {error   && <div className="error-banner">{error}</div>}

      {summary && (
        <>
          <table className="table">
            <thead>
              <tr><th>{t('tree.colLeg')}</th><th>{t('tree.colDate')}</th><th>{t('tree.colBestPrice')}</th></tr>
            </thead>
            <tbody>
              {node.path.map((route, i) => (
                <tr key={route.id}>
                  <td>
                    <span className="airport">{route.origin}</span>
                    <span className="arrow">→</span>
                    <span className="airport">{route.destination}</span>
                  </td>
                  <td>{route.travelDate}</td>
                  <td>
                    {(() => {
                      const segs = summary.summaries[i]?.segments ?? [];
                      const getPrice = (s) => s.purchasedPrice != null ? s.purchasedPrice : s.lowestPrice;
                      const getCurrency = (s) => s.purchasedPrice != null ? s.purchasedCurrency : s.lowestCurrency;
                      const total = segs.reduce((sum, s) => sum + (Number(getPrice(s)) || 0), 0);
                      const currency = segs.map(getCurrency).find(Boolean);
                      return total > 0 && currency
                        ? formatPrice(total, currency)
                        : <span className="muted">—</span>;
                    })()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="path-total">
            {t('tree.journeyTotal')}
            <strong>{totalDisplay}</strong>
          </div>
        </>
      )}
    </div>
  );
}

// ─── Main component ────────────────────────────────────────────────────────────

export default function FlightTree({ routes }) {
  const t = useT();
  const [selectedNode, setSelectedNode] = useState(null);
  const [summary, setSummary]           = useState(null);
  const [loading, setLoading]           = useState(false);
  const [pathError, setPathError]       = useState(null);

  const { nodes, edges, svgW, svgH } = useMemo(() => applyLayout(buildForest(routes)), [routes]);

  const highlightedIds = useMemo(
    () => new Set(selectedNode?.path.map(r => r.id) ?? []),
    [selectedNode]
  );

  const handleNodeClick = async (node) => {
    setSelectedNode(node);
    setSummary(null);
    setPathError(null);
    setLoading(true);
    try {
      const summaries = await Promise.all(node.path.map(r => getPriceSummary(r.id)));
      setSummary({ summaries });
    } catch (e) {
      setPathError(e.message);
    } finally {
      setLoading(false);
    }
  };

  if (routes.length === 0) {
    return <p className="empty-state">{t('tree.empty')}</p>;
  }

  return (
    <div>
      <div className="tree-scroll">
        <svg width={svgW} height={svgH}>
          {edges.map((e, i) => (
            <Edge key={i} from={e.from} to={e.to} highlighted={highlightedIds.has(e.route.id)} />
          ))}
          {nodes.map((node, i) => (
            <Node
              key={i}
              node={node}
              highlighted={selectedNode !== null && node.path.every(r => highlightedIds.has(r.id))}
              onClick={() => handleNodeClick(node)}
            />
          ))}
        </svg>
      </div>

      {selectedNode?.path.length > 0 && (
        <PathSummary node={selectedNode} summary={summary} loading={loading} error={pathError} />
      )}

      {!selectedNode && (
        <p className="tree-hint">{t('tree.hint')}</p>
      )}
    </div>
  );
}
