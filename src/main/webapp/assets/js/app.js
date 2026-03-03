/* app.js — Rentawear front-end logic */

/* ============================================================
   SEARCH MODAL
   ============================================================
   Debounced input → fetch('/SearchServlet?q=…&category=…&maxPrice=…')
   Renders top 5 results + "View All" link.
   Replace the mock fetch below with your real servlet call.
   ============================================================ */

(function () {
  const searchInput  = document.getElementById('searchInput');
  const searchClear  = document.getElementById('searchClear');
  const priceRange   = document.getElementById('priceRange');
  const priceDisplay = document.getElementById('priceDisplay');
  const resultsBox   = document.getElementById('searchResults');

  if (!searchInput) return; // guard: only run on pages that include the modal

  let debounceTimer   = null;
  let activeCategory  = 'all';
  const CTX           = document.querySelector('meta[name="ctx"]')?.content ?? '';

  /* ── Category pills inside the modal ── */
  document.querySelectorAll('[data-filter-category]').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('[data-filter-category]').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      activeCategory = pill.dataset.filterCategory;
      runSearch();
    });
  });

  /* ── Price range ── */
  priceRange?.addEventListener('input', () => {
    priceDisplay.textContent = priceRange.value;
    runSearch();
  });

  /* ── Search input ── */
  searchInput.addEventListener('input', () => {
    const hasValue = searchInput.value.trim().length > 0;
    searchClear.classList.toggle('d-none', !hasValue);
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(runSearch, 320);
  });

  /* ── Clear button ── */
  searchClear.addEventListener('click', () => {
    searchInput.value = '';
    searchClear.classList.add('d-none');
    resetResults();
    searchInput.focus();
  });

  /* ── Reset modal state when closed ── */
  document.getElementById('searchModal')?.addEventListener('hidden.bs.modal', () => {
    searchInput.value = '';
    searchClear.classList.add('d-none');
    priceRange.value     = 200;
    priceDisplay.textContent = '200';
    activeCategory = 'all';
    document.querySelectorAll('[data-filter-category]').forEach((p, i) => {
      p.classList.toggle('active', i === 0);
    });
    resetResults();
  });

  /* ── Focus input when modal opens ── */
  document.getElementById('searchModal')?.addEventListener('shown.bs.modal', () => {
    searchInput.focus();
  });

  function resetResults() {
    resultsBox.innerHTML = '<p class="rw-search-placeholder">Start typing to see results…</p>';
  }

  /* ── Core search function ── */
  async function runSearch() {
    const q = searchInput.value.trim();
    if (q.length < 2) { resetResults(); return; }

    resultsBox.innerHTML = '<p class="rw-search-placeholder">Searching…</p>';

    try {
      /* ── Replace this URL with your real servlet endpoint ── */
      const url = `${CTX}/SearchServlet?q=${encodeURIComponent(q)}&category=${activeCategory}&maxPrice=${priceRange.value}&limit=5`;
      const res  = await fetch(url);
      if (!res.ok) throw new Error('Network response was not ok');
      const data = await res.json(); // expects { results: [...], total: N }

      renderResults(data.results, data.total, q);
    } catch (err) {
      console.error('Search error:', err);
      resultsBox.innerHTML = '<p class="rw-search-placeholder">Could not load results. Try again.</p>';
    }
  }

  /* ── Render result items ── */
  function renderResults(items, total, q) {
    if (!items || items.length === 0) {
      resultsBox.innerHTML = '<p class="rw-search-placeholder">No results found.</p>';
      return;
    }

    const CTX_PATH = CTX;
    const html = items.map(item => `
      <a class="rw-result-item" href="${CTX_PATH}/product?id=${item.id}">
        <img src="${item.imageUrl || CTX_PATH + '/assets/img/placeholder.jpg'}"
             alt="${escHtml(item.name)}" loading="lazy"/>
        <div class="rw-result-info">
          <div class="result-brand">${escHtml(item.brand ?? '')}</div>
          <div class="result-name">${escHtml(item.name)}</div>
          <div class="result-price">$${Number(item.pricePerDay).toFixed(2)}<span style="font-weight:400;color:var(--rw-gray-500);font-size:.75rem;"> / day</span></div>
        </div>
      </a>
    `).join('');

    const viewAll = total > items.length
      ? `<a class="rw-search-view-all"
            href="${CTX}/search?q=${encodeURIComponent(q)}&category=${activeCategory}&maxPrice=${priceRange.value}">
           View all ${total} results <i class="bi bi-arrow-right"></i>
         </a>`
      : '';

    resultsBox.innerHTML = html + viewAll;
  }

  function escHtml(str) {
    return String(str)
      .replace(/&/g,'&amp;').replace(/</g,'&lt;')
      .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

})();


/* ============================================================
   CATEGORY PILLS (homepage)
   ============================================================ */
document.querySelectorAll('.rw-pill[data-category]').forEach(pill => {
  pill.addEventListener('click', () => {
    document.querySelectorAll('.rw-pill[data-category]').forEach(p => p.classList.remove('active'));
    pill.classList.add('active');
    // TODO: trigger product list reload based on pill.dataset.category
  });
});