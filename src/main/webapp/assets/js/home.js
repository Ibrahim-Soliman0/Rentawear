/* ============================================================
   home.js — Home page section loaders
   Depends on : utils.js        (CTX, esc)
                card-factory.js (CardFactory)
   Loaded on  : index.jsp only  (via ${param.extraJS})

   Fix: loadInterests() now sends interestIds (not categoryIds)
   so ProductFilterDTO.isInterestBased() returns true and the
   correct findByInterests() query runs server-side.

   Also sets the href of #interestsViewAll dynamically so the
   "View All" link carries the user's actual interest IDs.
   ============================================================ */

const SECTION_LIMIT = 4;
const _controllers  = new Map();

async function loadSection({ id, url, limit = SECTION_LIMIT, logLabel = id } = {}) {
  const container = document.getElementById(id);
  if (!container) return;

  const skel = document.createDocumentFragment();
  for (let i = 0; i < limit; i++) skel.appendChild(CardFactory.skeleton());
  container.replaceChildren(skel);

  if (_controllers.has(id)) try { _controllers.get(id).abort(); } catch (e) {}
  const controller = new AbortController();
  _controllers.set(id, controller);

  try {
    const data = await fetchJson(url, { outerSignal: controller.signal, timeout: 8000 });
    _controllers.delete(id);

    const products = Array.isArray(data)
        ? data
        : (data && Array.isArray(data.products) ? data.products : []);

    if (!products.length) {
      const p = document.createElement('p');
      p.className   = 'rw-no-results';
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
    p.className   = 'text-muted py-3 ps-1';
    p.textContent = 'Unable to load — please refresh.';
    container.replaceChildren(p);
  }
}

const SECTIONS = [
  { id: 'newArrivalsScroll', url: `${CTX}/products?newOnly=true&pageSize=${SECTION_LIMIT}`,   logLabel: 'New Arrivals' },
  { id: 'womenScroll',       url: `${CTX}/products?gender=FEMALE&pageSize=${SECTION_LIMIT}`,  logLabel: "Women's"      },
  { id: 'menScroll',         url: `${CTX}/products?gender=MALE&pageSize=${SECTION_LIMIT}`,    logLabel: "Men's"        },
];

const OCCASIONS = [
  { label: 'Wedding',      bgClass: 'occ-wedding',  href: `${CTX}/catalog?gender=FEMALE` },
  { label: 'Black Tie',    bgClass: 'occ-blacktie', href: `${CTX}/catalog?gender=MALE`   },
  { label: 'Garden Party', bgClass: 'occ-garden',   href: `${CTX}/catalog?gender=FEMALE` },
  { label: 'Business',     bgClass: 'occ-business', href: `${CTX}/catalog?gender=MALE`   },
  { label: 'Cocktail',     bgClass: 'occ-cocktail', href: `${CTX}/catalog`               },
  { label: 'Casual',       bgClass: 'occ-casual',   href: `${CTX}/catalog`               },
];

function buildOccasionCard(o) {
  const a = document.createElement('a');
  a.className = 'occ-card';
  if (o.href) a.href = o.href;
  const bg      = document.createElement('div'); bg.className = 'occ-bg';
  if (o.bgClass) o.bgClass.split(/\s+/).filter(Boolean).forEach(c => bg.classList.add(esc(c)));
  const overlay = document.createElement('div'); overlay.className = 'occ-overlay';
  const span    = document.createElement('span'); span.className = 'occ-label'; span.textContent = o.label || '';
  overlay.appendChild(span); a.appendChild(bg); a.appendChild(overlay);
  return a;
}

function loadOccasions() {
  const container = document.getElementById('occasionScroll');
  if (!container) return;
  const frag = document.createDocumentFragment();
  for (const o of OCCASIONS) frag.appendChild(buildOccasionCard(o));
  container.replaceChildren(frag);
}

function loadInterests() {
  const section   = document.getElementById('interestsSection');
  const container = document.getElementById('interestsScroll');
  if (!section || !container) return;

  const u = window.RW_USER;
  if (!u || !u.interests || !u.interests.length) {
    section.hidden = true;
    return;
  }

  /* Show skeletons while loading */
  const sk = document.createDocumentFragment();
  for (let i = 0; i < SECTION_LIMIT; i++) sk.appendChild(CardFactory.skeleton());
  container.replaceChildren(sk);

  const key = 'interests';
  if (_controllers.has(key)) try { _controllers.get(key).abort(); } catch (e) {}
  const controller = new AbortController();
  _controllers.set(key, controller);

  /* Build URL using interestIds (not categoryIds) so the server routes to
     findByInterests() via ProductFilterDTO.isInterestBased()              */
  const params = new URLSearchParams();
  params.append('pageSize', SECTION_LIMIT);
  u.interests.forEach(interest => {
    if (interest.categoryId != null) {
      params.append('interestIds', interest.categoryId);
    }
  });

  /* Set the "View All" link href dynamically with the user's interestIds
     so the catalog page opens correctly pre-filtered to their interests   */
  const viewAllLink = document.getElementById('interestsViewAll');
  if (viewAllLink) {
    const allParams = new URLSearchParams();
    u.interests.forEach(interest => {
      if (interest.categoryId != null) {
        allParams.append('interestIds', interest.categoryId);
      }
    });
    viewAllLink.href = `${CTX}/catalog?${allParams.toString()}`;
  }

  fetchJson(`${CTX}/products?${params.toString()}`, { outerSignal: controller.signal, timeout: 8000 })
      .then(data => {
        _controllers.delete(key);
        const products = data && Array.isArray(data.products) ? data.products : [];

        if (!products.length) {
          section.hidden = true;
          return;
        }

        const frag = document.createDocumentFragment();
        for (const p of products) frag.appendChild(CardFactory.grid(p));
        container.replaceChildren(frag);
      })
      .catch(err => {
        _controllers.delete(key);
        if (err && err.name === 'AbortError') return;
        console.error('[home.js] Interests Error:', err);
        section.hidden = true;
      });
}

document.addEventListener('DOMContentLoaded', () => {
  for (const s of SECTIONS) loadSection(s);
  // loadOccasions();
  loadInterests();
});