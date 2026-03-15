(function () {
  const overlay  = document.getElementById('qvOverlay');
  if (!overlay) return;

  const backdrop = document.getElementById('qvBackdrop');
  const closeBtn = document.getElementById('qvClose');
  const mainImg  = document.getElementById('qvMainImg');
  const thumbsEl = document.getElementById('qvThumbs');
  const colorsEl = document.getElementById('qvColors');
  const brandEl  = document.getElementById('qvBrand');
  const nameEl   = document.getElementById('qvName');
  const priceEl  = document.getElementById('qvPrice');
  const descEl   = document.getElementById('qvDesc');
  const sizesEl  = document.getElementById('qvSizes');
  const addBtn   = document.getElementById('qvAdd');


  let active      = null;
  let activeColor = null;
  let activeSize  = null;
  let activeImage = null;

  function openImmediate(data) {
    active = data;
    activeSize = data.sizes[0] || null;

    brandEl.textContent = data.brand;
    nameEl.textContent  = data.name;
    priceEl.textContent = `£${Number(data.price).toFixed(0)}/day`;
    descEl.textContent  = data.desc || 'Premium rental with dry cleaning included.';

    // Show card image immediately — base path + _md suffix
    setMainImage(data.image);

    // Clear any previous gallery state
    if (thumbsEl) {
      while (thumbsEl.firstChild) thumbsEl.removeChild(thumbsEl.firstChild);
    }
    if (colorsEl) {
      while (colorsEl.firstChild) colorsEl.removeChild(colorsEl.firstChild);
    }

    renderSizes(data.sizes);

    overlay.classList.add('open');
    document.body.style.overflow = 'hidden';
  }

  async function loadImages(productId) {
    try {
      const url = `${CTX}/ProductServlet?action=images&id=${productId}`;
      const dto = await (window.fetchJson ? window.fetchJson(url, { timeout: 8000 }) : (await fetch(url)).json());

      activeColor = dto.defaultColor;
      renderColors(dto.colorGroups);

      const defaultGroup = dto.colorGroups.find(g => g.color === dto.defaultColor)
          || dto.colorGroups[0];
      if (defaultGroup) setActiveGroup(defaultGroup);

    } catch (e) {
      console.error('[quick-view] image fetch failed:', e);
    }
  }

  function setMainImage(basePath) {
    activeImage = basePath;
    mainImg.style.backgroundImage = `url('${imgUrl(basePath, 'md')}')`;
  }

  function renderColors(groups) {
    if (!colorsEl) return;
    while (colorsEl.firstChild) colorsEl.removeChild(colorsEl.firstChild);
    groups.forEach(g => {
      const btn = document.createElement('button');
      btn.type             = 'button';
      btn.className        = 'qv-color-chip' + (g.color === activeColor ? ' active' : '');
      btn.style.background = g.hex;
      btn.title            = g.name;
      btn.dataset.color    = g.color;
      btn.addEventListener('click', () => {
        activeColor = g.color;
        colorsEl.querySelectorAll('.qv-color-chip')
            .forEach(b => b.classList.toggle('active', b.dataset.color === activeColor));
        setActiveGroup(g);
      });
      colorsEl.appendChild(btn);
    });
  }

  function setActiveGroup(group) {
    renderThumbs(group.images);
    if (group.images.length > 0) setMainImage(group.images[0].base);
  }

  function renderThumbs(images) {
    if (!thumbsEl) return;
    while (thumbsEl.firstChild) thumbsEl.removeChild(thumbsEl.firstChild);
    images.forEach((img, i) => {
      const btn = document.createElement('button');
      btn.type  = 'button';
      btn.className = 'qv-thumb' + (i === 0 ? ' active' : '');
      btn.style.backgroundImage = `url('${imgUrl(img.base, 'sm')}')`;
      btn.setAttribute('aria-label', img.alt || `Image ${i + 1}`);
      btn.addEventListener('click', () => {
        thumbsEl.querySelectorAll('.qv-thumb').forEach(t => t.classList.remove('active'));
        btn.classList.add('active');
        setMainImage(img.base);
      });
      thumbsEl.appendChild(btn);
    });
  }

  function renderSizes(sizes) {
    if (!sizesEl) return;
    while (sizesEl.firstChild) sizesEl.removeChild(sizesEl.firstChild);
    if (!sizes || !sizes.length) {
      const span = document.createElement('span');
      span.className = 'qv-size';
      span.textContent = 'One size';
      sizesEl.appendChild(span);
      activeSize = 'OS';
      return;
    }
    sizes.forEach((s, i) => {
      const btn = document.createElement('button');
      btn.type      = 'button';
      btn.className = 'qv-size' + (i === 0 ? ' active' : '');
      btn.textContent = s;
      if (i === 0) activeSize = s;
      btn.addEventListener('click', () => {
        activeSize = s;
        sizesEl.querySelectorAll('.qv-size')
            .forEach(b => b.classList.toggle('active', b === btn));
      });
      sizesEl.appendChild(btn);
    });
  }

  // ==================== Entry point ==========================
  document.addEventListener('click', e => {
    const trigger = e.target.closest('[data-qv]');
    if (!trigger) return;

    const data = {
      id:    trigger.dataset.id,
      name:  trigger.dataset.name  || '',
      brand: trigger.dataset.brand || '',
      price: trigger.dataset.price || 0,
      image: trigger.dataset.image || `${CTX}/assets/img/placeholder`,
      desc:  trigger.dataset.desc  || '',
      sizes: (trigger.dataset.sizes || '').split(',').filter(Boolean),
    };

    openImmediate(data);    // sync — zero latency
    loadImages(data.id);    // async — enriches while overlay is visible
  });

  // ================ Add to bag ==============
  addBtn?.addEventListener('click', () => {
    if (!active) return;
    Cart.add({
      id:          active.id,
      name:        active.name,
      brand:       active.brand,
      imageUrl:    activeImage || active.image,
      pricePerDay: Number(active.price),
      size:        activeSize || 'OS',
      days:        3,
    });
    close();
  });

  //=================== close ====================
  function close() {
    overlay.classList.remove('open');
    document.body.style.overflow = '';
    active = null; activeColor = null; activeSize = null; activeImage = null;
  }

  backdrop?.addEventListener('click', close);
  closeBtn?.addEventListener('click', close);
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape' && overlay.classList.contains('open')) close();
  });
})();