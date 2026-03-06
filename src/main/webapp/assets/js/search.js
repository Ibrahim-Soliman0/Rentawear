/* ============================================================
   search.js — Search modal + dual-handle price range slider
   Depends on: utils.js (CTX, esc)
   Load order: AFTER utils.js
   ============================================================ */


/* ── Dual-handle price range slider ───────────────────────── */
(function () {
  const minSlider  = document.getElementById('priceRangeMin');
  const maxSlider  = document.getElementById('priceRangeMax');
  const minDisplay = document.getElementById('priceMin');
  const maxDisplay = document.getElementById('priceMax');
  const fill       = document.getElementById('rangeFill');

  if (!minSlider || !maxSlider) return;

  const GAP = 20; // minimum $ gap between the two handles

  function updateFill() {
    const min   = parseInt(minSlider.value);
    const max   = parseInt(maxSlider.value);
    const total = parseInt(maxSlider.max) - parseInt(minSlider.min);

    fill.style.left  = ((min - parseInt(minSlider.min)) / total * 100) + '%';
    fill.style.right = ((parseInt(maxSlider.max) - max)  / total * 100) + '%';

    minDisplay.textContent = min;
    maxDisplay.textContent = max;
  }

  minSlider.addEventListener('input', () => {
    if (parseInt(minSlider.value) > parseInt(maxSlider.value) - GAP)
      minSlider.value = parseInt(maxSlider.value) - GAP;
    updateFill();
    runSearch();
  });

  maxSlider.addEventListener('input', () => {
    if (parseInt(maxSlider.value) < parseInt(minSlider.value) + GAP)
      maxSlider.value = parseInt(minSlider.value) + GAP;
    updateFill();
    runSearch();
  });

  updateFill(); // set initial fill on page load

  /* Expose so the modal-reset code below can call it */
  window._updateRangeFill = updateFill;
})();


/* ── Search modal ──────────────────────────────────────────── */
(function () {
  const searchInput = document.getElementById('searchInput');
  const searchClear = document.getElementById('searchClear');
  const resultsBox  = document.getElementById('searchResults');

  if (!searchInput) return; // search modal not present on this page

  let debounceTimer  = null;
  let activeCategory = 'all';

  /* ── Category pills ── */
  document.querySelectorAll('[data-filter-category]').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('[data-filter-category]')
              .forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      activeCategory = pill.dataset.filterCategory;
      runSearch();
    });
  });

  /* ── Typing in the search box ── */
  searchInput.addEventListener('input', () => {
    searchClear.classList.toggle('d-none', !searchInput.value.trim());
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

  /* ── Reset everything when modal closes ── */
  document.getElementById('searchModal')?.addEventListener('hidden.bs.modal', () => {
    searchInput.value = '';
    searchClear.classList.add('d-none');

    const minS = document.getElementById('priceRangeMin');
    const maxS = document.getElementById('priceRangeMax');
    if (minS) minS.value = 10;
    if (maxS) maxS.value = 500;
    document.getElementById('priceMin') && (document.getElementById('priceMin').textContent = '10');
    document.getElementById('priceMax') && (document.getElementById('priceMax').textContent = '500');
    window._updateRangeFill?.();

    activeCategory = 'all';
    document.querySelectorAll('[data-filter-category]')
            .forEach((p, i) => p.classList.toggle('active', i === 0));

    resetResults();
  });

  /* ── Focus input when modal opens ── */
  document.getElementById('searchModal')?.addEventListener('shown.bs.modal', () => {
    searchInput.focus();
  });

  /* ── Helpers ── */
  function resetResults() {
    resultsBox.innerHTML = '<p class="rw-search-placeholder">Start typing to see results…</p>';
  }

  /* ── Fetch results from SearchServlet ── */
  window.runSearch = async function () {
    const q        = searchInput.value.trim();
    const minPrice = document.getElementById('priceRangeMin')?.value ?? 10;
    const maxPrice = document.getElementById('priceRangeMax')?.value ?? 500;

    if (q.length < 2) { resetResults(); return; }

    resultsBox.innerHTML = '<p class="rw-search-placeholder">Searching…</p>';

    try {
      const url = `${CTX}/SearchServlet?q=${encodeURIComponent(q)}`
                + `&category=${activeCategory}`
                + `&minPrice=${minPrice}&maxPrice=${maxPrice}&limit=5`;

      const res  = await fetch(url);
      if (!res.ok) throw new Error('Network error');
      const data = await res.json();
      renderResults(data.results, data.total, q);
    } catch (err) {
      console.error('Search error:', err);
      resultsBox.innerHTML = '<p class="rw-search-placeholder">Could not load results. Try again.</p>';
    }
  };

  /* ── Build result item HTML ── */
  function renderResults(items, total, q) {
    if (!items?.length) {
      resultsBox.innerHTML = '<p class="rw-search-placeholder">No results found.</p>';
      return;
    }

    const minPrice = document.getElementById('priceRangeMin')?.value ?? 10;
    const maxPrice = document.getElementById('priceRangeMax')?.value ?? 500;

    const html = items.map(item => `
      <a class="rw-result-item" href="${CTX}/product?id=${item.id}">
        <img src="${item.imageUrl || CTX + '/assets/img/placeholder.jpg'}"
             alt="${esc(item.name)}" loading="lazy"/>
        <div class="rw-result-info">
          <div class="rw-result-brand">${esc(item.brand ?? '')}</div>
          <div class="rw-result-name">${esc(item.name)}</div>
          <div class="rw-result-price">
            $${Number(item.pricePerDay).toFixed(2)}<span> / day</span>
          </div>
        </div>
      </a>`).join('');

    const viewAll = total > items.length
      ? `<a class="rw-search-view-all"
            href="${CTX}/search?q=${encodeURIComponent(q)}&category=${activeCategory}&minPrice=${minPrice}&maxPrice=${maxPrice}">
           View all ${total} results <i class="bi bi-arrow-right"></i>
         </a>`
      : '';

    resultsBox.innerHTML = html + viewAll;
  }
})();