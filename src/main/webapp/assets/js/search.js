/* ============================================================
   search.js - Redesign search modal + servlet-backed results
   Depends on: utils.js (CTX, esc), CardFactory

   Markup expected (WEB-INF/components/search-modal.jsp):
     #searchToggle, #searchModal, #searchBackdrop, #searchCancel
     #searchInput, #searchClear
     .filter-pill[data-cat]
     .search-trending-pill[data-query]
     #searchTrending, #searchResults
   ============================================================ */

(function () {
  const searchToggle = document.getElementById('searchToggle');
  const modal = document.getElementById('searchModal');
  const backdrop = document.getElementById('searchBackdrop');
  const cancel = document.getElementById('searchCancel');
  const input = document.getElementById('searchInput');
  const clearBtn = document.getElementById('searchClear');
  const trending = document.getElementById('searchTrending');
  const results = document.getElementById('searchResults');

  // Price range UI
  const priceContainer = document.getElementById('searchPrice');
  const priceMinInput = document.getElementById('searchPriceMin');
  const priceMaxInput = document.getElementById('searchPriceMax');
  const rangeMin = document.getElementById('searchRangeMin');
  const rangeMax = document.getElementById('searchRangeMax');

  if (!modal || !input || !results) return;

  let debounceTimer = null;
  let activeCategory = 'all';
  let priceBounds = { min: 0, max: 1000 };
  let selectedPrice = { min: null, max: null };

  function openSearch() {
    modal.classList.add('open');
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    setTimeout(() => input.focus(), 0);
    // // fetch price bounds for current category
    // loadPriceBounds(activeCategory);
  }

  async function loadPriceBounds(category) {
    try {
      const genderMap = { women: 'FEMALE', men: 'MALE' };
      const gender = genderMap[category] || '';
      const url = `${CTX}/products?${gender ? 'gender=' + gender + '&' : ''}pageSize=1`;
      const data = await fetchJson(url, { timeout: 8000 });
      const min = data.priceRange?.min ?? 0;
      const max = data.priceRange?.max ?? 0;
      priceBounds = { min: Math.floor(min || 0), max: Math.ceil(max || 0) };
      selectedPrice.min = priceBounds.min;
      selectedPrice.max = priceBounds.max;
      renderPriceUI();
    } catch (e) {
      // keep UI hidden if price load fails
      console.error('[search.js] priceRange:', e);
      if (priceContainer) priceContainer.setAttribute('aria-hidden', 'true');
    }
  }

  function renderPriceUI() {
    if (!priceContainer) return;
    priceContainer.setAttribute('aria-hidden', 'false');
    const min = priceBounds.min; const max = priceBounds.max;
    rangeMin.min = min; rangeMin.max = max; rangeMin.value = selectedPrice.min ?? min;
    rangeMax.min = min; rangeMax.max = max; rangeMax.value = selectedPrice.max ?? max;
    priceMinInput.value = selectedPrice.min;
    priceMaxInput.value = selectedPrice.max;
  }

  function clampPrices() {
    if (!rangeMin || !rangeMax) return;
    let a = Number(rangeMin.value);
    let b = Number(rangeMax.value);
    if (a > b) {
      // swap to keep min <= max
      const tmp = a; a = b; b = tmp;
    }
    selectedPrice.min = Math.max(priceBounds.min, Math.min(priceBounds.max, Math.floor(a)));
    selectedPrice.max = Math.max(priceBounds.min, Math.min(priceBounds.max, Math.ceil(b)));
    priceMinInput.value = selectedPrice.min;
    priceMaxInput.value = selectedPrice.max;
  }

  function closeSearch() {
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
    reset();
  }

  function reset() {
    input.value = '';
    clearBtn?.classList.remove('show');
    results.style.display = 'none';
    // clear children safely
    while (results.firstChild) results.removeChild(results.firstChild);
    if (trending) trending.style.display = 'block';
    activeCategory = 'all';
    document.querySelectorAll('.filter-pill').forEach((p, i) => p.classList.toggle('active', i === 0));
  }

  function _placeholderNode(text) {
    const div = document.createElement('div');
    div.className = 'search-placeholder';
    div.textContent = text;
    return div;
  }

  function setLoading() {
    if (trending) trending.style.display = 'none';
    results.style.display = 'block';
    while (results.firstChild) results.removeChild(results.firstChild);
    results.appendChild(_placeholderNode('Searching...'));
  }

  function setEmpty() {
    if (trending) trending.style.display = 'none';
    results.style.display = 'block';
    while (results.firstChild) results.removeChild(results.firstChild);
    results.appendChild(_placeholderNode('No results found.'));
  }

  function setError() {
    if (trending) trending.style.display = 'none';
    results.style.display = 'block';
    while (results.firstChild) results.removeChild(results.firstChild);
    results.appendChild(_placeholderNode('Could not load results. Try again.'));
  }

  function renderResults(items, total, q) {
    if (!items?.length) return setEmpty();

    results.style.display = 'block';
    while (results.firstChild) results.removeChild(results.firstChild);
    const frag = document.createDocumentFragment();

    // Use CardFactory.searchResult for consistent markup
    items.forEach(item => {
      frag.appendChild(CardFactory.searchResult(item));
    });

    if (total > items.length) {
      const a = document.createElement('a');
      a.className = 'search-view-all';
      // include selected price range in query params
      const minP = selectedPrice.min ?? priceBounds.min;
      const maxP = selectedPrice.max ?? priceBounds.max;
      a.href = `${CTX}/search?q=${encodeURIComponent(q)}&category=${activeCategory}&minPrice=${minP}&maxPrice=${maxP}`;
      a.textContent = `View all ${total} results`;
      frag.appendChild(a);
    }

    results.appendChild(frag);
  }

  async function runSearch() {
    const q = input.value.trim();
    if (q.length < 2) {
      results.style.display = 'none';
      while (results.firstChild) results.removeChild(results.firstChild);
      if (trending) trending.style.display = 'block';
      return;
    }

    setLoading();

    try {
      // include price bounds in the search request so server can filter results
      const minP = selectedPrice.min ?? priceBounds.min;
      const maxP = selectedPrice.max ?? priceBounds.max;
      const genderMap = { women: 'FEMALE', men: 'MALE' };
      const gender = genderMap[activeCategory] || '';
      const url = `${CTX}/products/search?q=${encodeURIComponent(q)}${gender ? '&gender=' + gender : ''}&minPrice=${minP}&maxPrice=${maxP}&pageSize=6`;
      const data = await fetchJson(url, { timeout: 8000 });
      renderResults(data.products, data.total, q);
    } catch (e) {
      console.error('[search.js] Search error:', e);
      setError();
    }
  }

  // Events
  searchToggle?.addEventListener('click', openSearch);
  backdrop?.addEventListener('click', closeSearch);
  cancel?.addEventListener('click', closeSearch);

  document.addEventListener('keydown', e => {
    if (e.key === 'Escape' && modal.classList.contains('open')) closeSearch();
  });

  input.addEventListener('input', () => {
    const hasValue = !!input.value.trim();
    clearBtn?.classList.toggle('show', hasValue);
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(runSearch, 300);
  });

  clearBtn?.addEventListener('click', () => {
    input.value = '';
    clearBtn.classList.remove('show');
    results.style.display = 'none';
    while (results.firstChild) results.removeChild(results.firstChild);
    if (trending) trending.style.display = 'block';
    input.focus();
  });

  // Range slider events
  rangeMin?.addEventListener('input', () => { clampPrices(); });
  rangeMax?.addEventListener('input', () => { clampPrices(); });

  document.querySelectorAll('.filter-pill[data-cat]').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.filter-pill[data-cat]').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      activeCategory = pill.dataset.cat || 'all';
      // refetch price bounds for selected category
      // loadPriceBounds(activeCategory);
      runSearch();
    });
  });

  document.querySelectorAll('.search-trending-pill[data-query]').forEach(btn => {
    btn.addEventListener('click', () => {
      input.value = btn.dataset.query || '';
      clearBtn?.classList.toggle('show', !!input.value.trim());
      runSearch();
      input.focus();
    });
  });
})();