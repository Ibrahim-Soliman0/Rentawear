/* ============================================================
   cart.js — Cart drawer state and rendering
   Depends on: utils.js (CTX, esc)
   Load order: AFTER utils.js

   Public API (available globally as Cart.*):
     Cart.add(item)      — add an item or increment its qty
     Cart.getItems()     — returns current items array
     Cart.render()       — re-render the drawer (called internally)

   item shape:
     { id, name, brand, imageUrl, pricePerDay, size, days }
   ============================================================ */

const Cart = (function () {

  /* ── State ── */
  let items = [];
  // Each item: { id, name, brand, imageUrl, pricePerDay, size, days, qty }


  /* ── DOM element lookups ─────────────────────────────────── */
  const el = {
    count:       () => document.getElementById('cartCount'),
    headerCount: () => document.getElementById('cartHeaderCount'),
    list:        () => document.getElementById('cartItemsList'),
    empty:       () => document.getElementById('cartEmpty'),
    subtotal:    () => document.getElementById('cartSubtotal'),
    total:       () => document.getElementById('cartTotal'),
    footer:      () => document.getElementById('cartFooter'),
  };


  /* ── Calculate subtotal ──────────────────────────────────── */
  function subtotal() {
    return items.reduce((sum, i) => sum + i.pricePerDay * i.days * i.qty, 0);
  }


  /* ── Re-render the entire drawer ─────────────────────────── */
  function render() {
    const count = items.reduce((sum, i) => sum + i.qty, 0);

    /* Update nav badge */
    const badge = el.count();
    if (badge) {
      badge.textContent = count;
      badge.classList.toggle('d-none', count === 0);
    }

    /* Update drawer header count */
    const hdr = el.headerCount();
    if (hdr) hdr.textContent = count === 0
      ? '0 items'
      : `${count} item${count !== 1 ? 's' : ''}`;

    const list  = el.list();
    const empty = el.empty();
    if (!list) return;

    /* Remove old item cards before re-drawing */
    list.querySelectorAll('.rw-cart-item').forEach(node => node.remove());

    /* Empty state */
    if (items.length === 0) {
      if (empty)      empty.style.display = 'flex';
      if (el.footer()) el.footer().classList.add('rw-cart-footer--empty');
      if (el.subtotal()) el.subtotal().textContent = '$0.00';
      if (el.total())    el.total().textContent    = '$0.00';
      return;
    }

    if (empty)       empty.style.display = 'none';
    if (el.footer()) el.footer().classList.remove('rw-cart-footer--empty');

    /* Build one card per item */
    items.forEach(item => {
      const card = document.createElement('div');
      card.className  = 'rw-cart-item';
      card.dataset.id = item.id;
      card.innerHTML  = `
        <img src="${item.imageUrl || CTX + '/assets/img/placeholder.jpg'}"
             alt="${esc(item.name)}" class="rw-cart-item-img"/>
        <div class="rw-cart-item-info">
          <div class="rw-cart-item-brand">${esc(item.brand ?? '')}</div>
          <div class="rw-cart-item-name">${esc(item.name)}</div>
          <div class="rw-cart-item-meta">
            Size: <strong>${esc(item.size)}</strong>
            &nbsp;·&nbsp; ${item.days} day${item.days !== 1 ? 's' : ''}
          </div>
          <div class="rw-cart-item-price">
            $${(item.pricePerDay * item.days * item.qty).toFixed(2)}
          </div>
        </div>
        <div class="rw-cart-item-actions">
          <button class="rw-cart-qty-btn" data-action="dec"
                  data-id="${item.id}" aria-label="Decrease quantity">
            <i class="bi bi-dash"></i>
          </button>
          <span class="rw-cart-qty">${item.qty}</span>
          <button class="rw-cart-qty-btn" data-action="inc"
                  data-id="${item.id}" aria-label="Increase quantity">
            <i class="bi bi-plus"></i>
          </button>
          <button class="rw-cart-remove" data-id="${item.id}" aria-label="Remove item">
            <i class="bi bi-trash3"></i>
          </button>
        </div>`;
      list.appendChild(card);
    });

    /* Update totals */
    const sub = subtotal();
    if (el.subtotal()) el.subtotal().textContent = `$${sub.toFixed(2)}`;
    if (el.total())    el.total().textContent    = `$${sub.toFixed(2)}`;
  }


  /* ── Event delegation for qty and remove buttons ─────────── */
  document.getElementById('cartItemsList')?.addEventListener('click', e => {
    const btn    = e.target.closest('[data-action]');
    const remove = e.target.closest('.rw-cart-remove');

    if (btn) {
      const item = items.find(i => i.id === btn.dataset.id);
      if (!item) return;
      if (btn.dataset.action === 'inc') {
        item.qty++;
      } else if (btn.dataset.action === 'dec') {
        item.qty--;
        if (item.qty <= 0) items = items.filter(i => i.id !== btn.dataset.id);
      }
      render();
    }

    if (remove) {
      items = items.filter(i => i.id !== remove.dataset.id);
      render();
    }
  });


  /* ── Public API ──────────────────────────────────────────── */
  return {
    /* Add an item. If the same id+size already exists, increment qty instead */
    add(item) {
      const existing = items.find(i => i.id === item.id && i.size === item.size);
      if (existing) {
        existing.qty++;
      } else {
        items.push({ ...item, qty: 1 });
      }
      render();
    },

    render,

    getItems: () => [...items], // return a copy so external code can't mutate state
  };

})();

/* Initialise badge on page load */
Cart.render();