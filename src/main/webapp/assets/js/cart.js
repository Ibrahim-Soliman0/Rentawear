/* ============================================================
   cart.js - Cart drawer state and rendering
   Depends on: utils.js (CTX, esc), CardFactory

   Targets the redesign cart drawer markup (WEB-INF/components/cart-drawer.jsp):
     - Badge:        #cartBadge (fallback #cartCount)
     - Count label:  #cartCountLabel (fallback #cartHeaderCount)
     - Items wrap:   #cartItems (fallback #cartItemsList)
     - Empty state:  #cartEmpty
     - Totals:       #cartSubtotal, #cartTotal

   Public API:
     Cart.add(item)
     Cart.getItems()
     Cart.render()
     Cart.remove(id)
     Cart.changeQty(id, delta)

   item shape:
     { id, name, brand, imageUrl, pricePerDay, size, days }
   ============================================================ */

const Cart = (function () {
  let items = [];

  const el = {
    badge:      () => document.getElementById('cartBadge') || document.getElementById('cartCount'),
    countLabel: () => document.getElementById('cartCountLabel') || document.getElementById('cartHeaderCount'),
    itemsWrap:  () => document.getElementById('cartItems') || document.getElementById('cartItemsList'),
    empty:      () => document.getElementById('cartEmpty'),
    subtotal:   () => document.getElementById('cartSubtotal'),
    total:      () => document.getElementById('cartTotal'),
  };

  function subtotal() {
    return items.reduce((sum, i) => sum + i.pricePerDay * i.days * i.qty, 0);
  }

  function render() {
    const count = items.reduce((sum, i) => sum + i.qty, 0);

    // Badge and header count — unchanged
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
      wrap.innerHTML = '';
      if (el.subtotal()) el.subtotal().textContent = '£0.00';
      if (el.total())    el.total().textContent    = '£0.00';
      if (cta) cta.style.display = 'none';
      return;
    }

    if (empty) empty.style.display = 'none';
    wrap.style.display = 'flex';
    if (cta) cta.style.display = 'flex';

    const existingIds = [...wrap.querySelectorAll('.cart-item')]
        .map(li => li.dataset.id);
    const currentIds  = items.map(i => String(i.id));
    const listChanged = JSON.stringify(existingIds) !== JSON.stringify(currentIds);

    if (listChanged) {
      wrap.innerHTML = '';
      items.forEach(item => wrap.appendChild(CardFactory.cartItem(item)));
    } else {
      items.forEach(item => {
        const li  = wrap.querySelector(`.cart-item[data-id="${item.id}"]`);
        const num = li?.querySelector('.qty-num');
        if (num) num.textContent = String(item.qty);
      });
    }

    const sub = subtotal();
    if (el.subtotal()) el.subtotal().textContent = `£${sub.toFixed(2)}`;
    if (el.total())    el.total().textContent    = `£${sub.toFixed(2)}`;
  }

  function remove(id) {
    items = items.filter(i => String(i.id) !== String(id));
    render();
  }

  function changeQty(id, delta) {
    const item = items.find(i => String(i.id) === String(id));
    if (!item) return;

    item.qty += delta;
    if (item.qty <= 0) {
      remove(id);
    } else {
      render();
    }
  }

  function handleClick(e) {
    const btn = e.target.closest('[data-action]');
    const removeBtn = e.target.closest('.cart-remove');

    if (btn) {
      const id = btn.dataset.id;
      if (!id) return;

      if (btn.dataset.action === 'inc') {
        changeQty(id, 1);
      } else if (btn.dataset.action === 'dec') {
        changeQty(id, -1);
      }
    }

    if (removeBtn) {
      const id = removeBtn.dataset.id;
      if (id) remove(id);
    }
  }

  // Bind on either wrapper (redesign or legacy)
  document.getElementById('cartItems')?.addEventListener('click', handleClick);
  document.getElementById('cartItemsList')?.addEventListener('click', handleClick);

  return {
    add(item) {
      const existing = items.find(i => i.id === item.id && i.size === item.size);
      if (existing) existing.qty++;
      else items.push({ ...item, qty: 1 });
      render();
    },
    render,
    getItems: () => [...items],
    remove,
    changeQty
  };
})();

// Initial render
Cart.render();