/* ============================================================
   shell-manager.js - Interaction glue for redesign shell
   - Mobile drawer open/close + accordion
   - Cart drawer open/close (Cart module renders contents)
   - Desktop nav dropdown open/close (hover + click/escape)
   ============================================================ */

(function () {
  const mobileDrawer = document.getElementById('mobileDrawer');
  const drawerToggle = document.getElementById('drawerToggle');
  const drawerBackdrop = document.getElementById('drawerBackdrop');
  const drawerClose = document.getElementById('drawerClose');

  function openDrawer() {
    if (!mobileDrawer) return;
    mobileDrawer.classList.add('open');
    mobileDrawer.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
  }

  function closeDrawer() {
    if (!mobileDrawer) return;
    mobileDrawer.classList.remove('open');
    mobileDrawer.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  }

  drawerToggle?.addEventListener('click', openDrawer);
  drawerBackdrop?.addEventListener('click', closeDrawer);
  drawerClose?.addEventListener('click', closeDrawer);

  // Drawer accordion
  document.querySelectorAll('.drawer-item[data-target]').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = btn.dataset.target;
      const sub = id ? document.getElementById(id) : null;
      if (!sub) return;
      const isOpen = sub.classList.toggle('open');
      btn.classList.toggle('open', isOpen);
    });
  });

  // Cart drawer open/close
  const cartDrawer = document.getElementById('cartDrawer');
  const cartToggle = document.getElementById('cartToggle');
  const cartBackdrop = document.getElementById('cartBackdrop');
  const cartClose = document.getElementById('cartClose');
  const cartContinue = document.getElementById('cartContinue');

  function openCart() {
    if (!cartDrawer) return;
    cartDrawer.classList.add('open');
    cartDrawer.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
  }

  function closeCart() {
    if (!cartDrawer) return;
    cartDrawer.classList.remove('open');
    cartDrawer.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  }

  cartToggle?.addEventListener('click', openCart);
  cartBackdrop?.addEventListener('click', closeCart);
  cartClose?.addEventListener('click', closeCart);
  cartContinue?.addEventListener('click', closeCart);
  cartContinue?.addEventListener('keydown', e => {
    if (e.key === 'Enter' || e.key === ' ') closeCart();
  });

  // Desktop dropdown behavior (click-to-toggle; click outside closes)
  const dropdownHosts = ['dd-women', 'dd-men', 'dd-new', 'dd-occ']
    .map(id => document.getElementById(id))
    .filter(Boolean);

  function closeAllDropdowns(except) {
    dropdownHosts.forEach(host => {
      if (except && host === except) return;
      host.classList.remove('open');
    });
  }

  dropdownHosts.forEach(host => {
    const btn = host.querySelector('.nav-link-btn');

    // Close all others when hovering into a new item
    host.addEventListener('mouseenter', () => closeAllDropdowns(host));

    btn?.addEventListener('click', (e) => {
      e.preventDefault();
      const next = !host.classList.contains('open');
      closeAllDropdowns(host);
      host.classList.toggle('open', next);
    });
  });

  document.addEventListener('click', (e) => {
    if (e.target.closest('.nav-dropdown') || e.target.closest('.nav-link-btn')) return;
    closeAllDropdowns();
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      closeAllDropdowns();
      closeDrawer();
      closeCart();
    }
  });
})();

