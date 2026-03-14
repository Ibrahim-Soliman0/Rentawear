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

  if (!modal || !input || !results) return;

  let debounceTimer = null;
  let activeCategory = 'all';

  function openSearch() {
    modal.classList.add('open');
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    setTimeout(() => input.focus(), 0);
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
    results.innerHTML = '';
    if (trending) trending.style.display = 'block';
    activeCategory = 'all';
    document.querySelectorAll('.filter-pill').forEach((p, i) => p.classList.toggle('active', i === 0));
  }

  function setLoading() {
    if (trending) trending.style.display = 'none';
    results.style.display = 'block';
    results.innerHTML = '<div class="search-placeholder">Searching...</div>';
  }

  function setEmpty() {
    if (trending) trending.style.display = 'none';
    results.style.display = 'block';
    results.innerHTML = '<div class="search-placeholder">No results found.</div>';
  }

  function setError() {
    if (trending) trending.style.display = 'none';
    results.style.display = 'block';
    results.innerHTML = '<div class="search-placeholder">Could not load results. Try again.</div>';
  }

  function renderResults(items, total, q) {
    if (!items?.length) return setEmpty();

    results.style.display = 'block';
    results.innerHTML = '';
    const frag = document.createDocumentFragment();

    // Use CardFactory.searchResult for consistent markup
    items.forEach(item => {
      frag.appendChild(CardFactory.searchResult(item));
    });

    if (total > items.length) {
      const a = document.createElement('a');
      a.className = 'search-view-all';
      a.href = `${CTX}/search?q=${encodeURIComponent(q)}&category=${activeCategory}`;
      a.textContent = `View all ${total} results`;
      frag.appendChild(a);
    }

    results.appendChild(frag);
  }

  async function runSearch() {
    const q = input.value.trim();
    if (q.length < 2) {
      results.style.display = 'none';
      results.innerHTML = '';
      if (trending) trending.style.display = 'block';
      return;
    }

    setLoading();

    try {
      // const url = `${CTX}/SearchServlet?q=${encodeURIComponent(q)}&category=${activeCategory}&minPrice=0&maxPrice=999999&limit=6`;
      const url = `${CTX}/ProductServlet?action=search&q=${encodeURIComponent(q)}&category=${activeCategory}&limit=6`
      const res = await fetch(url);

      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      renderResults(data.results, data.total, q);
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
    results.innerHTML = '';
    if (trending) trending.style.display = 'block';
    input.focus();
  });

  document.querySelectorAll('.filter-pill[data-cat]').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.filter-pill[data-cat]').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      activeCategory = pill.dataset.cat || 'all';
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