/* ============================================================
   app.js — Rentawear front-end logic
   ============================================================ */

const CTX = document.querySelector('meta[name="ctx"]')?.content ?? '';


/* ============================================================
   DUAL-HANDLE PRICE RANGE SLIDER
   ============================================================ */
(function () {
  const minSlider   = document.getElementById('priceRangeMin');
  const maxSlider   = document.getElementById('priceRangeMax');
  const minDisplay  = document.getElementById('priceMin');
  const maxDisplay  = document.getElementById('priceMax');
  const fill        = document.getElementById('rangeFill');

  if (!minSlider || !maxSlider) return;

  const GAP = 20; // minimum gap between handles ($)

  function updateFill() {
    const min = parseInt(minSlider.value);
    const max = parseInt(maxSlider.value);
    const total = parseInt(maxSlider.max) - parseInt(minSlider.min);
    const leftPct  = ((min - parseInt(minSlider.min)) / total) * 100;
    const rightPct = ((parseInt(maxSlider.max) - max)  / total) * 100;
    fill.style.left  = leftPct  + '%';
    fill.style.right = rightPct + '%';
    minDisplay.textContent = min;
    maxDisplay.textContent = max;
  }

  minSlider.addEventListener('input', () => {
    if (parseInt(minSlider.value) > parseInt(maxSlider.value) - GAP) {
      minSlider.value = parseInt(maxSlider.value) - GAP;
    }
    updateFill();
    runSearch();
  });

  maxSlider.addEventListener('input', () => {
    if (parseInt(maxSlider.value) < parseInt(minSlider.value) + GAP) {
      maxSlider.value = parseInt(minSlider.value) + GAP;
    }
    updateFill();
    runSearch();
  });

  updateFill(); // initialise on load
})();


/* ============================================================
   SEARCH MODAL
   ============================================================ */
(function () {
  const searchInput = document.getElementById('searchInput');
  const searchClear = document.getElementById('searchClear');
  const resultsBox  = document.getElementById('searchResults');

  if (!searchInput) return;

  let debounceTimer  = null;
  let activeCategory = 'all';

  /* Category pills */
  document.querySelectorAll('[data-filter-category]').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('[data-filter-category]').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      activeCategory = pill.dataset.filterCategory;
      runSearch();
    });
  });

  /* Input */
  searchInput.addEventListener('input', () => {
    searchClear.classList.toggle('d-none', !searchInput.value.trim());
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(runSearch, 320);
  });

  /* Clear */
  searchClear.addEventListener('click', () => {
    searchInput.value = '';
    searchClear.classList.add('d-none');
    resetResults();
    searchInput.focus();
  });

  /* Reset when modal closes */
  document.getElementById('searchModal')?.addEventListener('hidden.bs.modal', () => {
    searchInput.value = '';
    searchClear.classList.add('d-none');
    document.getElementById('priceRangeMin') && (document.getElementById('priceRangeMin').value = 10);
    document.getElementById('priceRangeMax') && (document.getElementById('priceRangeMax').value = 500);
    document.getElementById('priceMin') && (document.getElementById('priceMin').textContent = '10');
    document.getElementById('priceMax') && (document.getElementById('priceMax').textContent = '500');
    document.getElementById('rangeFill') && updateRangeFill();
    activeCategory = 'all';
    document.querySelectorAll('[data-filter-category]').forEach((p, i) => p.classList.toggle('active', i === 0));
    resetResults();
  });

  /* Focus input when modal opens */
  document.getElementById('searchModal')?.addEventListener('shown.bs.modal', () => searchInput.focus());

  function resetResults() {
    resultsBox.innerHTML = '<p class="rw-search-placeholder">Start typing to see results…</p>';
  }

  function updateRangeFill() {
    const minS = document.getElementById('priceRangeMin');
    const maxS = document.getElementById('priceRangeMax');
    const fill = document.getElementById('rangeFill');
    if (!minS || !maxS || !fill) return;
    const total    = parseInt(maxS.max) - parseInt(minS.min);
    const leftPct  = ((parseInt(minS.value) - parseInt(minS.min)) / total) * 100;
    const rightPct = ((parseInt(maxS.max) - parseInt(maxS.value)) / total) * 100;
    fill.style.left  = leftPct  + '%';
    fill.style.right = rightPct + '%';
  }

  window.runSearch = async function () {
    const q       = searchInput?.value.trim() ?? '';
    const minPrice = document.getElementById('priceRangeMin')?.value ?? 10;
    const maxPrice = document.getElementById('priceRangeMax')?.value ?? 500;

    if (q.length < 2) { resetResults(); return; }

    resultsBox.innerHTML = '<p class="rw-search-placeholder">Searching…</p>';

    try {
      const url = `${CTX}/SearchServlet?q=${encodeURIComponent(q)}&category=${activeCategory}&minPrice=${minPrice}&maxPrice=${maxPrice}&limit=5`;
      const res = await fetch(url);
      if (!res.ok) throw new Error('Network error');
      const data = await res.json();
      renderResults(data.results, data.total, q);
    } catch (err) {
      console.error('Search error:', err);
      resultsBox.innerHTML = '<p class="rw-search-placeholder">Could not load results. Try again.</p>';
    }
  };

  function renderResults(items, total, q) {
    if (!items?.length) { resultsBox.innerHTML = '<p class="rw-search-placeholder">No results found.</p>'; return; }
    const minPrice = document.getElementById('priceRangeMin')?.value ?? 10;
    const maxPrice = document.getElementById('priceRangeMax')?.value ?? 500;

    const html = items.map(item => `
      <a class="rw-result-item" href="${CTX}/product?id=${item.id}">
        <img src="${item.imageUrl || CTX + '/assets/img/placeholder.jpg'}" alt="${esc(item.name)}" loading="lazy"/>
        <div class="rw-result-info">
          <div class="rw-result-brand">${esc(item.brand ?? '')}</div>
          <div class="rw-result-name">${esc(item.name)}</div>
          <div class="rw-result-price">$${Number(item.pricePerDay).toFixed(2)}<span> / day</span></div>
        </div>
      </a>`).join('');

    const viewAll = total > items.length
      ? `<a class="rw-search-view-all"
            href="${CTX}/search?q=${encodeURIComponent(q)}&category=${activeCategory}&minPrice=${minPrice}&maxPrice=${maxPrice}">
           View all ${total} results <i class="bi bi-arrow-right"></i>
         </a>` : '';

    resultsBox.innerHTML = html + viewAll;
  }
})();


/* ============================================================
   CART DRAWER
   ============================================================ */
const Cart = (function () {
  let items = []; // { id, name, brand, imageUrl, pricePerDay, size, days, qty }

  const els = {
    count:       () => document.getElementById('cartCount'),
    headerCount: () => document.getElementById('cartHeaderCount'),
    list:        () => document.getElementById('cartItemsList'),
    empty:       () => document.getElementById('cartEmpty'),
    subtotal:    () => document.getElementById('cartSubtotal'),
    total:       () => document.getElementById('cartTotal'),
    footer:      () => document.getElementById('cartFooter'),
  };

  function subtotal() {
    return items.reduce((s, i) => s + i.pricePerDay * i.days * i.qty, 0);
  }

  function render() {
    const count     = items.reduce((s, i) => s + i.qty, 0);
    const cartCount = els.count();
    const hdrCount  = els.headerCount();

    /* Badge on nav icon */
    if (cartCount) {
      cartCount.textContent = count;
      cartCount.classList.toggle('d-none', count === 0);
    }
    if (hdrCount) hdrCount.textContent = count === 0 ? '0 items' : `${count} item${count !== 1 ? 's' : ''}`;

    const list  = els.list();
    const empty = els.empty();
    if (!list) return;

    if (items.length === 0) {
      /* Remove any previous item cards, leave the empty-state div */
      list.querySelectorAll('.rw-cart-item').forEach(el => el.remove());
      if (empty) empty.style.display = 'flex';
      if (els.footer()) els.footer().classList.add('rw-cart-footer--empty');
      if (els.subtotal()) els.subtotal().textContent = '$0.00';
      if (els.total())    els.total().textContent    = '$0.00';
      return;
    }

    if (empty) empty.style.display = 'none';
    if (els.footer()) els.footer().classList.remove('rw-cart-footer--empty');

    /* Re-render item cards */
    list.querySelectorAll('.rw-cart-item').forEach(el => el.remove());
    items.forEach(item => {
      const card = document.createElement('div');
      card.className = 'rw-cart-item';
      card.dataset.id = item.id;
      card.innerHTML = `
        <img src="${item.imageUrl || CTX + '/assets/img/placeholder.jpg'}" alt="${esc(item.name)}" class="rw-cart-item-img"/>
        <div class="rw-cart-item-info">
          <div class="rw-cart-item-brand">${esc(item.brand ?? '')}</div>
          <div class="rw-cart-item-name">${esc(item.name)}</div>
          <div class="rw-cart-item-meta">
            Size: <strong>${esc(item.size)}</strong> &nbsp;·&nbsp; ${item.days} day${item.days !== 1 ? 's' : ''}
          </div>
          <div class="rw-cart-item-price">$${(item.pricePerDay * item.days * item.qty).toFixed(2)}</div>
        </div>
        <div class="rw-cart-item-actions">
          <button class="rw-cart-qty-btn" data-action="dec" data-id="${item.id}" aria-label="Decrease">
            <i class="bi bi-dash"></i>
          </button>
          <span class="rw-cart-qty">${item.qty}</span>
          <button class="rw-cart-qty-btn" data-action="inc" data-id="${item.id}" aria-label="Increase">
            <i class="bi bi-plus"></i>
          </button>
          <button class="rw-cart-remove" data-id="${item.id}" aria-label="Remove item">
            <i class="bi bi-trash3"></i>
          </button>
        </div>`;
      list.appendChild(card);
    });

    const sub = subtotal();
    if (els.subtotal()) els.subtotal().textContent = `$${sub.toFixed(2)}`;
    if (els.total())    els.total().textContent    = `$${sub.toFixed(2)}`;
  }

  /* Delegate qty / remove clicks inside the drawer */
  document.getElementById('cartItemsList')?.addEventListener('click', e => {
    const btn    = e.target.closest('[data-action]');
    const remove = e.target.closest('.rw-cart-remove');

    if (btn) {
      const id   = btn.dataset.id;
      const item = items.find(i => i.id === id);
      if (!item) return;
      if (btn.dataset.action === 'inc') item.qty++;
      if (btn.dataset.action === 'dec') { item.qty--; if (item.qty <= 0) items = items.filter(i => i.id !== id); }
      render();
    }

    if (remove) {
      const id = remove.dataset.id;
      items = items.filter(i => i.id !== id);
      render();
    }
  });

  /* Public API */
  return {
    add(item) {
      const existing = items.find(i => i.id === item.id && i.size === item.size);
      if (existing) { existing.qty++; }
      else { items.push({ ...item, qty: 1 }); }
      render();
    },
    render,
    getItems: () => items,
  };
})();

/* Initialise cart badge on page load */
Cart.render();


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


/* ============================================================
   HELPERS
   ============================================================ */
function esc(str) {
  return String(str)
    .replace(/&/g,'&amp;').replace(/</g,'&lt;')
    .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}