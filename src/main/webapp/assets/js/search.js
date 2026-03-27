/* ============================================================
   search.js — Search modal
   Depends on: utils.js (CTX, esc, fetchJson), CardFactory
   ============================================================ */

'use strict';

(function () {

  const RESULT_SIZE     = 6;
  const SEARCH_DEBOUNCE = 300;
  const PRICE_DEBOUNCE  = 500;

  let state = {
    gender:      null,
    categoryIds: [],
    minPrice:    null,
    maxPrice:    null,
  };

  let priceBounds = { min: 0, max: 500 };
  let searchTimer = null;
  let priceTimer  = null;
  let abortCtrl   = null;

  const catNames = {};

  /* ── DOM refs ─────────────────────────────────────────────── */
  const modal       = document.getElementById('searchModal');
  const backdrop    = document.getElementById('searchBackdrop');
  const cancelBtn   = document.getElementById('searchCancel');
  const input       = document.getElementById('searchInput');
  const clearBtn    = document.getElementById('searchClear');
  const filterBtn   = document.getElementById('searchFilterBtn');
  const filterBadge = document.getElementById('searchFilterBadge');
  const filterPanel = document.getElementById('searchFilterPanel');
  const chips       = document.getElementById('searchChips');
  const priceWrap   = document.getElementById('searchPriceWrap');
  const rangeMin    = document.getElementById('searchRangeMin');
  const rangeMax    = document.getElementById('searchRangeMax');
  const minDisp     = document.getElementById('searchPriceMinDisp');
  const maxDisp     = document.getElementById('searchPriceMaxDisp');
  const rangeFill   = document.getElementById('searchRangeFill');
  const trending    = document.getElementById('searchTrending');
  const results     = document.getElementById('searchResults');

  if (!modal || !input || !results) return;

  /* Build catNames from checkbox data attributes */
  document.querySelectorAll('.sfp-cat-chk').forEach(chk => {
    const id = parseInt(chk.value, 10);
    if (!isNaN(id)) catNames[id] = chk.dataset.name || String(id);
  });

  /* ── Open / Close ─────────────────────────────────────────── */

  function openSearch() {
    modal.classList.add('open');
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    setTimeout(() => input.focus(), 60);
  }

  function closeSearch() {
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
    closePanelOnly();
    reset();
  }

  function closePanelOnly() {
    if (!filterPanel) return;
    filterPanel.classList.remove('is-open');
    filterPanel.setAttribute('aria-hidden', 'true');
    filterBtn.setAttribute('aria-expanded', 'false');
  }

  /* ── Reset modal state ────────────────────────────────────── */
  function reset() {
    input.value = '';
    clearBtn.classList.remove('show');
    state.gender      = null;
    state.categoryIds = [];
    state.minPrice    = null;
    state.maxPrice    = null;
    const allRadio = document.getElementById('sgAll');
    if (allRadio) allRadio.checked = true;
    document.querySelectorAll('.sfp-cat-chk').forEach(c => (c.checked = false));
    document.querySelectorAll('.sfp-cat-label').forEach(l => l.classList.remove('is-hidden'));
    resetPriceSlider();
    if (priceWrap) priceWrap.style.display = 'none';
    if (trending)  trending.style.display  = 'block';
    results.style.display = 'none';
    while (results.firstChild) results.removeChild(results.firstChild);
    renderChips();
    renderFilterBadge();
  }

  /* ── Build catalog URL from current filter state ──────────── */
  function buildCatalogUrl(q) {
    const p = new URLSearchParams();
    if (q)                      p.set('q', q);
    if (state.gender)           p.set('gender', state.gender);
    state.categoryIds.forEach(id => p.append('categoryIds', id));
    if (state.minPrice != null) p.set('minPrice', state.minPrice);
    if (state.maxPrice != null) p.set('maxPrice', state.maxPrice);
    return `${CTX}/catalog?${p.toString()}`;
  }

  /* ── Filter panel toggle ──────────────────────────────────── */

  filterBtn.addEventListener('click', e => {
    e.stopPropagation();
    const isOpen = filterPanel.classList.toggle('is-open');
    filterPanel.setAttribute('aria-hidden', String(!isOpen));
    filterBtn.setAttribute('aria-expanded', String(isOpen));
  });

  document.addEventListener('click', e => {
    if (filterPanel && !filterPanel.contains(e.target) && e.target !== filterBtn) {
      closePanelOnly();
    }
  });

  document.addEventListener('keydown', e => {
    if (e.key === 'Escape' && modal.classList.contains('open')) {
      if (filterPanel && filterPanel.classList.contains('is-open')) closePanelOnly();
      else closeSearch();
    }
  });

  /* ── Gender radios ────────────────────────────────────────── */

  document.querySelectorAll('input[name="searchGender"]').forEach(radio => {
    radio.addEventListener('change', () => {
      state.gender = radio.value || null;
      filterCatsByGender();
      scheduleSearch();
    });
  });

  function filterCatsByGender() {
    document.querySelectorAll('.sfp-cat-label[data-cat-gender]').forEach(label => {
      const hidden = !!state.gender && label.dataset.catGender !== state.gender;
      label.classList.toggle('is-hidden', hidden);
      if (hidden) {
        const chk = label.querySelector('.sfp-cat-chk');
        if (chk && chk.checked) {
          chk.checked = false;
          const id = parseInt(chk.value, 10);
          state.categoryIds = state.categoryIds.filter(c => c !== id);
        }
      }
    });
  }

  /* ── Category checkboxes ──────────────────────────────────── */

  document.querySelectorAll('.sfp-cat-chk').forEach(chk => {
    chk.addEventListener('change', () => {
      const id = parseInt(chk.value, 10);
      if (chk.checked) {
        if (!state.categoryIds.includes(id)) state.categoryIds.push(id);
      } else {
        state.categoryIds = state.categoryIds.filter(c => c !== id);
      }
      scheduleSearch();
    });
  });

  /* ── Filter chips ─────────────────────────────────────────── */

  function renderChips() {
    if (!chips) return;
    chips.replaceChildren();
    const frag = document.createDocumentFragment();

    if (state.gender) {
      frag.appendChild(makeChip(
          state.gender === 'FEMALE' ? 'Women' : 'Men',
          () => {
            state.gender = null;
            const r = document.getElementById('sgAll');
            if (r) r.checked = true;
            filterCatsByGender();
            scheduleSearch();
          }
      ));
    }

    state.categoryIds.forEach(id => {
      frag.appendChild(makeChip(catNames[id] || `Cat ${id}`, () => {
        state.categoryIds = state.categoryIds.filter(c => c !== id);
        const chk = document.querySelector(`.sfp-cat-chk[value="${id}"]`);
        if (chk) chk.checked = false;
        scheduleSearch();
      }));
    });

    if (state.minPrice != null || state.maxPrice != null) {
      const lo = state.minPrice != null ? state.minPrice : priceBounds.min;
      const hi = state.maxPrice != null ? state.maxPrice : priceBounds.max;
      frag.appendChild(makeChip(`£${lo} – £${hi}`, () => {
        state.minPrice = null;
        state.maxPrice = null;
        resetPriceSlider();
        scheduleSearch();
      }));
    }

    chips.appendChild(frag);
    chips.style.display = chips.childElementCount > 0 ? 'flex' : 'none';
  }

  function makeChip(label, onRemove) {
    const span = document.createElement('span');
    span.className   = 'search-chip';
    span.textContent = label;
    const x = document.createElement('button');
    x.type        = 'button';
    x.className   = 'search-chip-x';
    x.setAttribute('aria-label', `Remove ${label} filter`);
    x.textContent = '×';
    x.addEventListener('click', e => { e.stopPropagation(); onRemove(); });
    span.appendChild(x);
    return span;
  }

  function renderFilterBadge() {
    let count = 0;
    if (state.gender) count++;
    count += state.categoryIds.length;
    if (state.minPrice != null || state.maxPrice != null) count++;
    if (filterBadge) {
      filterBadge.textContent   = String(count);
      filterBadge.style.display = count > 0 ? 'inline-flex' : 'none';
    }
    filterBtn.classList.toggle('has-filters', count > 0);
  }

  /* ── Price slider ─────────────────────────────────────────── */

  function calibratePriceSlider(lo, hi) {
    if (lo === hi) { lo = Math.max(0, lo - 1); hi = hi + 1; }
    priceBounds = { min: lo, max: hi };
    if (!rangeMin || !rangeMax) return;
    rangeMin.min = lo; rangeMin.max = hi;
    rangeMax.min = lo; rangeMax.max = hi;
    if (state.minPrice == null) rangeMin.value = lo;
    if (state.maxPrice == null) rangeMax.value = hi;
    updatePriceDisplay();
  }

  function resetPriceSlider() {
    if (!rangeMin || !rangeMax) return;
    rangeMin.value = priceBounds.min;
    rangeMax.value = priceBounds.max;
    updatePriceDisplay();
  }

  function clampSliders() {
    if (!rangeMin || !rangeMax) return;
    const lo = parseInt(rangeMin.value, 10);
    const hi = parseInt(rangeMax.value, 10);
    if (lo > hi) {
      if (document.activeElement === rangeMin) rangeMax.value = lo;
      else rangeMin.value = hi;
    }
  }

  function updatePriceDisplay() {
    if (!rangeMin || !rangeMax) return;
    const lo = parseInt(rangeMin.value, 10);
    const hi = parseInt(rangeMax.value, 10);
    if (minDisp) minDisp.textContent = lo;
    if (maxDisp) maxDisp.textContent = hi;
    if (rangeFill) {
      const range = (priceBounds.max - priceBounds.min) || 1;
      rangeFill.style.left  = Math.max(0, ((lo - priceBounds.min) / range) * 100) + '%';
      rangeFill.style.right = Math.max(0, ((priceBounds.max - hi)  / range) * 100) + '%';
    }
  }

  function schedulePriceFetch() {
    clearTimeout(priceTimer);
    priceTimer = setTimeout(() => {
      const lo = parseInt(rangeMin.value, 10);
      const hi = parseInt(rangeMax.value, 10);
      state.minPrice = lo > priceBounds.min ? lo : null;
      state.maxPrice = hi < priceBounds.max ? hi : null;
      renderChips();
      renderFilterBadge();
      scheduleSearch();
    }, PRICE_DEBOUNCE);
  }

  if (rangeMin && rangeMax) {
    rangeMin.addEventListener('input', () => { clampSliders(); updatePriceDisplay(); schedulePriceFetch(); });
    rangeMax.addEventListener('input', () => { clampSliders(); updatePriceDisplay(); schedulePriceFetch(); });
  }

  /* ── Search input wiring ──────────────────────────────────── */

  input.addEventListener('input', () => {
    const hasValue = !!input.value.trim();
    clearBtn.classList.toggle('show', hasValue);
    clearTimeout(searchTimer);
    searchTimer = setTimeout(runSearch, SEARCH_DEBOUNCE);
  });

  /* Enter key → navigate directly to catalog search results page */
  input.addEventListener('keydown', e => {
    if (e.key !== 'Enter') return;
    e.preventDefault();
    const q = input.value.trim();
    if (!q) return;
    window.location.href = buildCatalogUrl(q);
  });

  /* Clear button → reset modal only, no page navigation */
  clearBtn.addEventListener('click', () => {
    input.value = '';
    clearBtn.classList.remove('show');
    if (priceWrap) priceWrap.style.display = 'none';
    results.style.display = 'none';
    while (results.firstChild) results.removeChild(results.firstChild);
    if (trending) trending.style.display = 'block';
    input.focus();
  });

  /* ── Run search ───────────────────────────────────────────────
     KEY ARCHITECTURE:
       - Fetch is isolated in its own try-catch.
       - On fetch error   → setError() and return.
       - On fetch success → ALL rendering happens OUTSIDE
         the try-catch so a DOM error in renderResults
         never silently triggers setError().
     ─────────────────────────────────────────────────────────── */

  async function runSearch() {
    const q = input.value.trim();

    if (q.length < 2) {
      if (priceWrap) priceWrap.style.display = 'none';
      results.style.display = 'none';
      while (results.firstChild) results.removeChild(results.firstChild);
      if (trending) trending.style.display = 'block';
      return;
    }

    if (trending) trending.style.display = 'none';
    setLoading();

    if (abortCtrl) { try { abortCtrl.abort(); } catch (e) {} }
    abortCtrl = new AbortController();

    /* ── Step 1: Fetch only — isolated in try-catch ─────────── */
    let data = null;
    try {
      const p = new URLSearchParams();
      p.set('q', q);
      if (state.gender) p.set('gender', state.gender);
      state.categoryIds.forEach(id => p.append('categoryIds', id));
      if (state.minPrice != null) p.set('minPrice', state.minPrice);
      if (state.maxPrice != null) p.set('maxPrice', state.maxPrice);
      p.set('pageSize', RESULT_SIZE);

      data = await fetchJson(
          `${CTX}/products/search?${p.toString()}`,
          { outerSignal: abortCtrl.signal, timeout: 8000 }
      );
      abortCtrl = null;
    } catch (err) {
      abortCtrl = null;
      if (err && err.name === 'AbortError') return;
      console.error('[search.js] fetch error:', err);
      setError();
      return; /* Stop here — do NOT proceed to rendering */
    }

    /* ── Step 2: Render — outside try-catch ─────────────────── */

    /* Calibrate price slider if bounds are in the response */
    if (data.priceRange && data.priceRange.min != null) {
      const lo = Math.floor(data.priceRange.min);
      const hi = Math.ceil(data.priceRange.max);
      calibratePriceSlider(lo, hi);
      if (priceWrap) priceWrap.style.display = 'block';
    }

    renderChips();
    renderFilterBadge();
    renderResults(data.products || [], data.total || 0, q);
  }

  /* ── Result rendering ─────────────────────────────────────── */

  function setLoading() {
    results.style.display = 'block';
    while (results.firstChild) results.removeChild(results.firstChild);
    results.appendChild(placeholder('Searching…'));
  }

  function setError() {
    results.style.display = 'block';
    while (results.firstChild) results.removeChild(results.firstChild);
    results.appendChild(placeholder('Could not load results. Try again.'));
  }

  function placeholder(text) {
    const div = document.createElement('div');
    div.className   = 'search-placeholder';
    div.textContent = text;
    return div;
  }

  function renderResults(items, total, q) {
    results.style.display = 'block';
    while (results.firstChild) results.removeChild(results.firstChild);

    if (!items.length) {
      results.appendChild(placeholder('No results found.'));
      return;
    }

    /* Build each result row individually — a failure on one
       card must never prevent the view-all link from appearing. */
    items.forEach(item => {
      try {
        results.appendChild(CardFactory.searchResult(item));
      } catch (cardErr) {
        console.warn('[search] card render error:', cardErr, item);
        /* Skip this card silently — others still render */
      }
    });

    /* View-all link — appended directly to results AFTER the loop,
       completely independent of the card-building above.           */
    const a = document.createElement('a');
    a.className   = 'search-view-all';
    a.href        = buildCatalogUrl(q);
    a.textContent = total > RESULT_SIZE
        ? `View all ${total} result${total !== 1 ? 's' : ''} →`
        : `View ${total} result${total !== 1 ? 's' : ''} →`;
    results.appendChild(a);
  }

  /* ── Entry points ─────────────────────────────────────────── */

  document.getElementById('searchToggle')?.addEventListener('click', openSearch);
  backdrop?.addEventListener('click', closeSearch);
  cancelBtn?.addEventListener('click', closeSearch);

  document.querySelectorAll('.search-trending-pill[data-query]').forEach(btn => {
    btn.addEventListener('click', () => {
      input.value = btn.dataset.query || '';
      clearBtn.classList.toggle('show', !!input.value.trim());
      runSearch();
      input.focus();
    });
  });

  function scheduleSearch() {
    renderChips();
    renderFilterBadge();
    clearTimeout(searchTimer);
    searchTimer = setTimeout(runSearch, SEARCH_DEBOUNCE);
  }

}());