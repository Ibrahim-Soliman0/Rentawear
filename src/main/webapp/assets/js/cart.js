/* ============================================================
   cart.js - Cart drawer state and rendering
   Depends on: utils.js (CTX, esc), CardFactory

   Targets the redesign cart drawer markup:
     - Badge:        #cartBadge (fallback #cartCount)
     - Count label:  #cartCountLabel (fallback #cartHeaderCount)
     - Items wrap:   #cartItems (fallback #cartItemsList)
     - Empty state:  #cartEmpty
     - Totals:       #cartSubtotal, #cartTotal

   Public API:
     Cart.add(item)
     Cart.getItems()
     Cart.render()
     Cart.remove(key)
     Cart.changeQty(key, delta)
     Cart.variantKey(item)

   item shape:
     { id, name, brand, imageUrl, pricePerDay,
       size, color, colorName, variantId, inventoryQty,
       days, dates }

   Variant key — "id:size:color" — is the unique identity for a cart
   entry. Two variants of the same product with different sizes or colours
   are separate line-items. CardFactory.cartItem() uses the same formula
   and stores it in data-key on all interactive elements.
   ============================================================ */

const Cart = (function () {
  let items = [];

  // ── Variant key ───────────────────────────────────────────────────────────
  function variantKey(item) {
    return `${item.id}:${item.size || ''}:${item.color || ''}`;
  }

  // ── DOM refs ──────────────────────────────────────────────────────────────
  const el = {
    badge:      () => document.getElementById('cartBadge')      || document.getElementById('cartCount'),
    countLabel: () => document.getElementById('cartCountLabel') || document.getElementById('cartHeaderCount'),
    itemsWrap:  () => document.getElementById('cartItems')      || document.getElementById('cartItemsList'),
    empty:      () => document.getElementById('cartEmpty'),
    subtotal:   () => document.getElementById('cartSubtotal'),
    total:      () => document.getElementById('cartTotal'),
  };

  // ── Toast ─────────────────────────────────────────────────────────────────
  // Shows a brief message near the cart. Reuses a single element so rapid
  // calls don't stack. Auto-dismisses after 2.5 s.
  let _toastEl   = null;
  let _toastTimer = null;

  function _toast(message) {
    if (!_toastEl) {
      _toastEl = document.createElement('div');
      _toastEl.className = 'cart-toast';
      // Minimal inline style as fallback — override in CSS for full design
      Object.assign(_toastEl.style, {
        position: 'fixed', bottom: '80px', right: '20px',
        background: 'var(--rw-charcoal, #333)', color: '#fff',
        padding: '10px 16px', borderRadius: '6px',
        fontSize: '13px', zIndex: '9999',
        opacity: '0', transition: 'opacity .2s',
        pointerEvents: 'none',
      });
      document.body.appendChild(_toastEl);
    }
    _toastEl.textContent = message;
    _toastEl.style.opacity = '1';
    clearTimeout(_toastTimer);
    _toastTimer = setTimeout(() => { _toastEl.style.opacity = '0'; }, 2500);
  }

  // ── Subtotal ──────────────────────────────────────────────────────────────
  function subtotal() {
    return items.reduce((sum, i) => sum + i.pricePerDay * i.days * i.qty, 0);
  }

  // ── Render ────────────────────────────────────────────────────────────────
  function render() {
    const count = items.reduce((sum, i) => sum + i.qty, 0);

    const badge = el.badge();
    if (badge) {
      badge.textContent = String(count);
      badge.classList.toggle('visible', count > 0);
    }
    const hdr = el.countLabel();
    if (hdr) hdr.textContent = count === 0 ? '0 items' : `${count} item${count !== 1 ? 's' : ''}`;

    const wrap  = el.itemsWrap();
    const empty = el.empty();
    if (!wrap) return;

    const cta = document.querySelector('.cart-cta');

    if (items.length === 0) {
      if (empty) empty.style.display = 'flex';
      wrap.style.display = 'none';
      while (wrap.firstChild) wrap.removeChild(wrap.firstChild);
      if (el.subtotal()) el.subtotal().textContent = '£0.00';
      if (el.total())    el.total().textContent    = '£0.00';
      if (cta) cta.style.display = 'none';
      return;
    }

    if (empty) empty.style.display = 'none';
    wrap.style.display = 'block';
    if (cta) cta.style.display = 'flex';

    // Diff by variant key — full rebuild only when the item list changes
    const existingKeys = [...wrap.querySelectorAll('.cart-item')].map(li => li.dataset.key);
    const currentKeys  = items.map(variantKey);
    const listChanged  = JSON.stringify(existingKeys) !== JSON.stringify(currentKeys);

    if (listChanged) {
      while (wrap.firstChild) wrap.removeChild(wrap.firstChild);
      const frag = document.createDocumentFragment();
      items.forEach(item => {
        try {
          frag.appendChild(CardFactory.cartItem(item));
        } catch (err) {
          console.error('[cart] CartFactory.cartItem failed for item:', item, err);
        }
      });
      wrap.appendChild(frag);
    } else {
      // Only qty changed — patch qty-num and + button state in-place.
      // Use a simple attribute-safe escaper instead of CSS.escape (not available in all envs).
      items.forEach(item => {
        const key = variantKey(item);
        // Escape only the characters that break a CSS attribute selector string
        const safeKey = key.replace(/\\/g, '\\\\').replace(/"/g, '\\"');
        const li  = wrap.querySelector(`.cart-item[data-key="${safeKey}"]`);
        if (!li) return;
        const num = li.querySelector('.qty-num');
        if (num) num.textContent = String(item.qty);
        const inc = li.querySelector('.qty-btn[data-action="inc"]');
        if (inc) {
          const cap = item.inventoryQty;
          inc.disabled = cap != null && item.qty >= cap;
        }
      });
    }

    const sub = subtotal();
    if (el.subtotal()) el.subtotal().textContent = `£${sub.toFixed(2)}`;
    if (el.total())    el.total().textContent    = `£${sub.toFixed(2)}`;
  }

  // ── Mutations ─────────────────────────────────────────────────────────────
  function remove(key) {
    items = items.filter(i => variantKey(i) !== key);
    render();
  }

  function changeQty(key, delta) {
    const item = items.find(i => variantKey(i) === key);
    if (!item) return;

    if (delta > 0 && item.inventoryQty != null && item.qty >= item.inventoryQty) {
      _toast(`Only ${item.inventoryQty} in stock for this variant.`);
      return;
    }

    item.qty += delta;
    if (item.qty <= 0) remove(key);
    else render();
  }

  // ── Event delegation ──────────────────────────────────────────────────────
  function handleClick(e) {
    const btn       = e.target.closest('[data-action]');
    const removeBtn = e.target.closest('.cart-remove');

    if (btn) {
      const key = btn.dataset.key;
      if (!key) return;
      if (btn.dataset.action === 'inc')      changeQty(key,  1);
      else if (btn.dataset.action === 'dec') changeQty(key, -1);
    }

    if (removeBtn) {
      const key = removeBtn.dataset.key;
      if (key) remove(key);
    }
  }

  document.getElementById('cartItems')?.addEventListener('click', handleClick);
  document.getElementById('cartItemsList')?.addEventListener('click', handleClick);

  // ── Public API ────────────────────────────────────────────────────────────
  return {
    add(item) {
      const key      = variantKey(item);
      const existing = items.find(i => variantKey(i) === key);

      if (existing) {
        if (existing.inventoryQty != null && existing.qty >= existing.inventoryQty) {
          _toast(`Only ${existing.inventoryQty} in stock for this variant.`);
          return;
        }
        existing.qty++;
      } else {
        items.push({ ...item, qty: 1 });
      }
      render();
    },
    render,
    getItems:   () => [...items],
    remove,
    changeQty,
    variantKey, // exposed for checkout servlet payload builder
  };
})();

// Initial render
Cart.render();