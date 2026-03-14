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


/* ── Generic product section loader ───────────────────────────
   loadSection(config) handles every product scroll strip on the
   home page. Adding a new section = adding one config object
   to the SECTIONS array below, no new functions needed.

   config shape:
   {
     id       : string,   // id of the .rw-home-scroll element
     url      : string,   // full fetch URL
     limit    : number,   // how many cards to show  (default 10)
     logLabel : string,   // prefix for console.error
   }
   ────────────────────────────────────────────────────────────── */
const SECTION_LIMIT = 4;

function loadSection({ id, url, limit = SECTION_LIMIT, logLabel = id }) {
    const container = document.getElementById(id);
    if (!container) return;

    /* Skeletons while fetch is in-flight */
    container.innerHTML = '';
    for (let i = 0; i < limit; i++) {
        container.appendChild(CardFactory.skeleton());
    }

    fetch(url)
        .then(res => {
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            return res.json();
        })
        .then(data => {
            container.innerHTML = '';
            const products = Array.isArray(data) ? data : (data.results ?? []);
            if (!products.length) {
                container.innerHTML = `<p class="rw-no-results">No products found.</p>`;
                return;
            }
            products.forEach(p => container.appendChild(CardFactory.grid(p)));
        })
        .catch(err => {
            console.error(`[home.js] ${logLabel}:`, err);
            container.innerHTML =
                `<p class="text-muted py-3 ps-1">Unable to load — please refresh.</p>`;
        });
}


/* ── Section configs ───────────────────────────────────────────
   To add another product section to the home page:
   1. Add a <div class="rw-home-scroll" id="newScroll"> in index.jsp
   2. Push a config object here — nothing else changes.
   ────────────────────────────────────────────────────────────── */
const SECTIONS = [
    {
        id       : 'trendingScroll',
        url      : `${CTX}/ProductServlet?action=list&limit=${SECTION_LIMIT}`,
        logLabel : 'Trending',
    },
    {
        id       : 'newArrivalsScroll',
        url      : `${CTX}/ProductServlet?action=list&sort=new&limit=${SECTION_LIMIT}`,
        logLabel : 'New Arrivals',
    },
    {
        id       : 'womenScroll',
        url      : `${CTX}/ProductServlet?action=list&category=women&limit=${SECTION_LIMIT}`,
        logLabel : "Women's",
    },
    {
        id       : 'menScroll',
        url      : `${CTX}/ProductServlet?action=list&category=men&limit=${SECTION_LIMIT}`,
        logLabel : "Men's",
    },
];


/* ── Occasion section ──────────────────────────────────────────
   Static editorial data — no servlet needed yet.
   To pull from the DB later: replace the OCCASIONS array with a
   fetch and call loadSection() instead. buildOccasionCard() and
   loadOccasions() stay unchanged.
   ────────────────────────────────────────────────────────────── */
const OCCASIONS = [
    {
        label    : 'Wedding',
        bgClass  : 'occ-wedding',
        href     : `${CTX}/products?occasion=wedding`,
    },
    {
        label    : 'Black Tie',
        bgClass  : 'occ-blacktie',
        href     : `${CTX}/products?occasion=black-tie`,
    },
    {
        label    : 'Garden Party',
        bgClass  : 'occ-garden',
        href     : `${CTX}/products?occasion=garden-party`,
    },
    {
        label    : 'Business',
        bgClass  : 'occ-business',
        href     : `${CTX}/products?occasion=business`,
    },
    {
        label    : 'Cocktail',
        bgClass  : 'occ-cocktail',
        href     : `${CTX}/products?occasion=cocktail`,
    },
    {
        label    : 'Casual',
        bgClass  : 'occ-casual',
        href     : `${CTX}/products?occasion=casual`,
    },
];

function buildOccasionCard(occasion) {
    const a = document.createElement('a');
    a.className = 'occ-card';
    a.href      = occasion.href;
    a.innerHTML = `
      <div class="occ-bg ${esc(occasion.bgClass)}"></div>
      <div class="occ-overlay">
        <span class="occ-label">${esc(occasion.label)}</span>
      </div>`;
    return a;
}

function loadOccasions() {
    const container = document.getElementById('occasionScroll');
    if (!container) return;
    container.innerHTML = '';
    OCCASIONS.forEach(o => container.appendChild(buildOccasionCard(o)));
}


/* ── Init ───────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
    SECTIONS.forEach(loadSection);
    loadOccasions();
    loadInterests();
});

/* ── Based on Your Interests ───────────────────────────────────
   Only runs when #interestsScroll exists in the DOM.
   The JSP only renders that element when the user is logged in
   AND has interests saved — so this function never needs to know
   about auth state itself.

   Behaviour:
   • Shows skeletons → fetches action=interests (servlet reads
     session user's interests and returns matched products).
   • If the response is empty (interests set but no matching
     products), the entire section is hidden so the user never
     sees an empty strip.
   • On any fetch error the section is also hidden silently —
     a broken personalisation strip is worse than no strip.
   ────────────────────────────────────────────────────────────── */
function loadInterests() {
    const section   = document.getElementById('interestsSection');
    const container = document.getElementById('interestsScroll');
    if (!section || !container) return;   // not logged in — JSP never rendered it

    /* Skeletons while in-flight */
    for (let i = 0; i < SECTION_LIMIT; i++) {
        container.appendChild(CardFactory.skeleton());
    }

    fetch(`${CTX}/ProductServlet?action=interests&limit=${SECTION_LIMIT}`)
        .then(res => {
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            return res.json();
        })
        .then(products => {
            container.innerHTML = '';
            if (!products.length) {
                /* No matches — hide the whole section cleanly */
                section.hidden = true;
                return;
            }
            products.forEach(p => container.appendChild(CardFactory.grid(p)));
        })
        .catch(err => {
            console.error('[home.js] Interests:', err);
            section.hidden = true;   // hide rather than show an error in a personalised section
        });
}
