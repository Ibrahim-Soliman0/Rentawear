/* quick-view.js - lightweight quick view using dataset payload on cards */
(function () {
  const overlay = document.getElementById('qvOverlay');
  if (!overlay) return;

  const backdrop = document.getElementById('qvBackdrop');
  const closeBtn = document.getElementById('qvClose');
  const mainImg = document.getElementById('qvMainImg');
  const brandEl = document.getElementById('qvBrand');
  const nameEl = document.getElementById('qvName');
  const priceEl = document.getElementById('qvPrice');
  const descEl = document.getElementById('qvDesc');
  const sizesEl = document.getElementById('qvSizes');
  const addBtn = document.getElementById('qvAdd');

  let active = null;
  let activeSize = null;

  function open(data) {
    active = data;
    activeSize = data.sizes[0] || null;
    brandEl.textContent = data.brand;
    nameEl.textContent = data.name;
    priceEl.textContent = `$${Number(data.price).toFixed(2)} / day`;
    descEl.textContent = data.desc || 'Premium rental with dry cleaning included.';
    mainImg.style.backgroundImage = `url('${data.image}')`;
    renderSizes(data.sizes);
    overlay.classList.add('open');
    document.body.style.overflow = 'hidden';
  }

  function close() {
    overlay.classList.remove('open');
    document.body.style.overflow = '';
    active = null;
    activeSize = null;
  }

  function renderSizes(sizes) {
    sizesEl.innerHTML = '';
    if (!sizes || !sizes.length) {
      sizesEl.innerHTML = '<span class="qv-size">One size</span>';
      return;
    }
    sizes.forEach(s => {
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'qv-size' + (s === activeSize ? ' active' : '');
      btn.textContent = s;
      btn.addEventListener('click', () => {
        activeSize = s;
        renderSizes(sizes);
      });
      sizesEl.appendChild(btn);
    });
  }

  document.addEventListener('click', (e) => {
    const trigger = e.target.closest('[data-qv]');
    if (trigger) {
      const data = {
        id: trigger.dataset.id,
        name: trigger.dataset.name || '',
        brand: trigger.dataset.brand || '',
        price: trigger.dataset.price || 0,
        image: trigger.dataset.image || `${CTX}/assets/img/placeholder.jpg`,
        desc: trigger.dataset.desc || '',
        sizes: (trigger.dataset.sizes || '').split(',').filter(Boolean),
      };
      open(data);
    }
  });

  addBtn?.addEventListener('click', () => {
    if (!active) return;
    Cart.add({
      id: active.id,
      name: active.name,
      brand: active.brand,
      imageUrl: active.image,
      pricePerDay: Number(active.price),
      size: activeSize || 'OS',
      days: 3,
    });
    close();
  });

  backdrop?.addEventListener('click', close);
  closeBtn?.addEventListener('click', close);
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && overlay.classList.contains('open')) close(); });
})();

