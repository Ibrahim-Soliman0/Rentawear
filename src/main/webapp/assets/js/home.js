/* ============================================================
   home.js — Home page section loaders
   Depends on : utils.js        (CTX, esc)
                card-factory.js (CardFactory)
   Loaded on  : index.jsp only  (via ${param.extraJS})

   Sections driven by this file:
     #trendingScroll  — Trending Rentals   (ProductServlet, sort=popular)
     #womenScroll     — Women's Collection (ProductServlet, category=women)
     #menScroll       — Men's Collection   (ProductServlet, category=men)
     #occasionScroll  — Shop by Occasion   (static data)
   ============================================================ */

const SECTION_LIMIT = 4;
const _controllers = new Map();

// Use shared fetchJson from utils if available, otherwise use window.fetchJson (should exist after utils loads)
const _fetchJson = window.fetchJson || (async (url, { outerSignal = null, timeout = 8000 } = {}) => {
  if (window.fetchJson) return window.fetchJson(url, { outerSignal, timeout });
  // As a last resort: simple fetch (may not have timeout support)
  const res = await fetch(url);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return await res.json();
});

async function loadSection({ id, url, limit = SECTION_LIMIT, logLabel = id } = {}) {
  const container = document.getElementById(id);
  if (!container) return;

  // skeletons (batched)
  const skel = document.createDocumentFragment();
  for (let i = 0; i < limit; i++) skel.appendChild(CardFactory.skeleton());
  container.replaceChildren(skel);

  if (_controllers.has(id)) try { _controllers.get(id).abort(); } catch (e) { /* ignore */ }
  const controller = new AbortController();
  _controllers.set(id, controller);

  try {
    const data = await _fetchJson(url, { outerSignal: controller.signal, timeout: 8000 });
    _controllers.delete(id);

    const products = Array.isArray(data) ? data : (data && Array.isArray(data.products) ? data.products : []);
    if (!products.length) {
      const p = document.createElement('p');
      p.className = 'rw-no-results';
      p.textContent = 'No products found.';
      container.replaceChildren(p);
      return;
    }

    const frag = document.createDocumentFragment();
    for (const prod of products) frag.appendChild(CardFactory.grid(prod));
    container.replaceChildren(frag);
  } catch (err) {
    _controllers.delete(id);
    if (err && err.name === 'AbortError') return;
    console.error(`[home.js] ${logLabel}:`, err);
    const p = document.createElement('p');
    p.className = 'text-muted py-3 ps-1';
    p.textContent = 'Unable to load — please refresh.';
    container.replaceChildren(p);
  }
}

const SECTIONS = [
  { id: 'trendingScroll',    url: `${CTX}/products?pageSize=${SECTION_LIMIT}`,                logLabel: 'Trending' },
  { id: 'newArrivalsScroll', url: `${CTX}/products?newOnly=true&pageSize=${SECTION_LIMIT}`,   logLabel: 'New Arrivals' },
  { id: 'womenScroll',       url: `${CTX}/products?gender=FEMALE&pageSize=${SECTION_LIMIT}`,  logLabel: "Women's" },
  { id: 'menScroll',         url: `${CTX}/products?gender=MALE&pageSize=${SECTION_LIMIT}`,    logLabel: "Men's" },
];

const OCCASIONS = [
  { label: 'Wedding', bgClass: 'occ-wedding', href: `${CTX}/products?occasion=wedding` },
  { label: 'Black Tie', bgClass: 'occ-blacktie', href: `${CTX}/products?occasion=black-tie` },
  { label: 'Garden Party', bgClass: 'occ-garden', href: `${CTX}/products?occasion=garden-party` },
  { label: 'Business', bgClass: 'occ-business', href: `${CTX}/products?occasion=business` },
  { label: 'Cocktail', bgClass: 'occ-cocktail', href: `${CTX}/products?occasion=cocktail` },
  { label: 'Casual', bgClass: 'occ-casual', href: `${CTX}/products?occasion=casual` },
];

function buildOccasionCard(o) {
  const a = document.createElement('a');
  a.className = 'occ-card';
  if (o.href) a.href = o.href;
  const bg = document.createElement('div'); bg.className = 'occ-bg';
  if (o.bgClass) o.bgClass.split(/\s+/).filter(Boolean).forEach(c => bg.classList.add(esc(c)));
  const overlay = document.createElement('div'); overlay.className = 'occ-overlay';
  const span = document.createElement('span'); span.className = 'occ-label'; span.textContent = o.label || '';
  overlay.appendChild(span); a.appendChild(bg); a.appendChild(overlay);
  return a;
}

function loadOccasions() {
  const container = document.getElementById('occasionScroll'); if (!container) return;
  const frag = document.createDocumentFragment(); for (const o of OCCASIONS) frag.appendChild(buildOccasionCard(o));
  container.replaceChildren(frag);
}

function loadInterests() {
  const section = document.getElementById('interestsSection');
  const container = document.getElementById('interestsScroll');
  if (!section || !container) return;

  const sk = document.createDocumentFragment(); for (let i = 0; i < SECTION_LIMIT; i++) sk.appendChild(CardFactory.skeleton());
  container.replaceChildren(sk);

  const key = 'interests'; if (_controllers.has(key)) try { _controllers.get(key).abort(); } catch (e) { }
  const controller = new AbortController(); _controllers.set(key, controller);

  fetchJson(`${CTX}/products/interests?pageSize=${SECTION_LIMIT}`, { outerSignal: controller.signal, timeout: 8000 })
      .then(data => {
        _controllers.delete(key);
        const products = data && Array.isArray(data.products) ? data.products : [];
      if (!products.length) { section.hidden = true; return; }
      const frag = document.createDocumentFragment(); for (const p of products) frag.appendChild(CardFactory.grid(p));
      container.replaceChildren(frag);
    })
    .catch(err => {
      _controllers.delete(key);
      if (err && err.name === 'AbortError') return;
      console.error('[home.js] Interests:', err);
      section.hidden = true;
    });
}

document.addEventListener('DOMContentLoaded', () => {
  for (const s of SECTIONS) loadSection(s);
  loadOccasions();
  loadInterests();
});
