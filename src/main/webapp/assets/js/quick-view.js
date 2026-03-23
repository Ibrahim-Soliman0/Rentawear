(function () {
  const overlay     = document.getElementById('qvOverlay');
  if (!overlay) return;

  const backdrop    = document.getElementById('qvBackdrop');
  const closeBtn    = document.getElementById('qvClose');
  const mainImg     = document.getElementById('qvMainImg');
  const thumbsEl    = document.getElementById('qvThumbs');
  const colorsEl    = document.getElementById('qvColors');
  const brandEl     = document.getElementById('qvBrand');
  const nameEl      = document.getElementById('qvName');
  const priceEl     = document.getElementById('qvPrice');
  const descEl      = document.getElementById('qvDesc');
  const descSection = document.getElementById('qvDescSection');
  const descToggle  = document.getElementById('qvDescToggle');
  const sizesEl     = document.getElementById('qvSizes');
  const addBtn      = document.getElementById('qvAdd');
  const nudgeEl     = document.getElementById('qvNudge');
  const summaryEl   = document.getElementById('qvDateSummary');
  const viewFullEl  = document.getElementById('qvViewFull');
  const colorLabelEl = document.getElementById('qvColorLabel');
  const colorNameEl  = document.getElementById('qvColorName');
  const soldOutEl    = document.getElementById('qvSoldOutBanner'); // new

  let active              = null;
  let activeColor         = null;
  let activeSize          = null;
  let activeVariantId     = null;
  let activeInventoryQty  = null;
  let activeImage         = null;
  let startDate           = null;
  let endDate             = null;
  let fpStart             = null;
  let fpEnd               = null;

  // ── Earliest bookable date: today + 2 ─────────────────────
  function getMinStartDate() {
    const d = new Date();
    d.setDate(d.getDate() + 2);
    d.setHours(0, 0, 0, 0);
    return d;
  }

  // ── Flatpickr — initialised once per page load ─────────────
  const startInput = document.getElementById('qvStartDate');
  const endInput   = document.getElementById('qvEndDate');

  if (startInput && typeof flatpickr === 'function') {
    fpStart = flatpickr(startInput, {
      dateFormat:    'd/m/Y',
      minDate:       getMinStartDate(),   /* earliest = today + 2 */
      disableMobile: true,
      onChange(dates) {
        startDate = dates[0] || null;
        if (fpEnd) {
          if (startDate) {
            const minEnd = new Date(startDate);
            minEnd.setDate(minEnd.getDate() + 1);
            const maxEnd = new Date(startDate);
            maxEnd.setDate(maxEnd.getDate() + 30); /* max 30-day rental */
            fpEnd.set('minDate', minEnd);
            fpEnd.set('maxDate', maxEnd);
            if (endDate && (endDate <= startDate || endDate > maxEnd)) {
              fpEnd.clear();
              endDate = null;
            }
          } else {
            fpEnd.set('minDate', getMinStartDate());
            fpEnd.set('maxDate', null);
          }
        }
        _updateDateSummary();
        _checkAddBtn();
        if (nudgeEl) nudgeEl.classList.remove('show');
        document.getElementById('qvStartField')?.classList.remove('qv-field-error');
      }
    });
  }

  if (endInput && typeof flatpickr === 'function') {
    fpEnd = flatpickr(endInput, {
      dateFormat:    'd/m/Y',
      minDate:       getMinStartDate(),
      disableMobile: true,
      onChange(dates) {
        endDate = dates[0] || null;
        _updateDateSummary();
        _checkAddBtn();
        if (nudgeEl) nudgeEl.classList.remove('show');
        document.getElementById('qvEndField')?.classList.remove('qv-field-error');
      }
    });
  }

  // ── Gate: disabled when no size, or colour is sold out ──────
  function _checkAddBtn() {
    if (!addBtn) return;
    addBtn.disabled = !activeSize;
  }

  // ── Enable / disable date pickers + show sold-out banner ────
  function _setDatesEnabled(enabled) {
    [startInput, endInput].forEach(inp => {
      if (!inp) return;
      inp.disabled = !enabled;
      inp.closest?.('.qv-date-field')
          ?.classList.toggle('qv-field-disabled', !enabled);
    });

    /* Sold-out banner */
    if (soldOutEl) soldOutEl.style.display = enabled ? 'none' : 'flex';

    if (!enabled) {
      fpStart?.clear(); fpEnd?.clear();
      startDate = null; endDate = null;
      if (fpEnd) { fpEnd.set('minDate', getMinStartDate()); fpEnd.set('maxDate', null); }
      if (summaryEl) summaryEl.textContent = '';
    }
  }

  // ── Resolve variant from loaded detail ──────────────────────
  function _resolveVariant() {
    const detail = overlay._detail;
    if (!detail || !activeColor) {
      activeVariantId = null; activeInventoryQty = null; return;
    }
    const sizeKey      = activeSize || 'OS';
    activeVariantId    = detail.variantIdByColorAndSize?.[activeColor]?.[sizeKey] ?? null;
    activeInventoryQty = activeVariantId != null
        ? (detail.quantityByVariantId?.[activeVariantId] ?? null)
        : null;
  }

  function _updateDateSummary() {
    if (!summaryEl) return;
    if (!startDate || !endDate || !active) { summaryEl.textContent = ''; return; }
    const days  = Math.round((endDate - startDate) / 86400000);
    const total = days * Number(active.price);
    summaryEl.textContent = `${days} day${days !== 1 ? 's' : ''} · £${total.toFixed(0)} total`;
  }

  function _setColorName(name) {
    if (!colorNameEl || !colorLabelEl) return;
    if (name) {
      colorNameEl.textContent    = name;
      colorLabelEl.style.display = 'flex';
    } else {
      colorLabelEl.style.display = 'none';
    }
  }

  // ── Open (sync — zero latency) ────────────────────────────
  function openImmediate(data) {
    active             = data;
    activeSize         = null;
    activeVariantId    = null;
    activeInventoryQty = null;
    overlay._detail    = null;

    brandEl.textContent = data.brand;
    nameEl.textContent  = data.name;
    priceEl.textContent = `£${Number(data.price).toFixed(0)}/day`;
    if (descEl) descEl.textContent = '';

    setMainImage(data.image);

    if (thumbsEl) while (thumbsEl.firstChild) thumbsEl.removeChild(thumbsEl.firstChild);
    if (colorsEl) while (colorsEl.firstChild) colorsEl.removeChild(colorsEl.firstChild);
    if (sizesEl)  while (sizesEl.firstChild)  sizesEl.removeChild(sizesEl.firstChild);

    // Size placeholder while detail loads
    const ph = document.createElement('span');
    ph.className = 'qv-size'; ph.textContent = '—'; ph.style.opacity = '0.3';
    sizesEl?.appendChild(ph);

    _setColorName(null);

    if (viewFullEl) {
      viewFullEl.href          = `${CTX}/product/${data.id}`;
      viewFullEl.style.display = 'block';
    }

    startDate = null; endDate = null;
    fpStart?.clear(); fpEnd?.clear();
    if (fpEnd) { fpEnd.set('minDate', getMinStartDate()); fpEnd.set('maxDate', null); }
    if (summaryEl) summaryEl.textContent = '';
    _setDatesEnabled(true); // resets sold-out banner

    descSection?.classList.remove('open');
    if (nudgeEl) nudgeEl.classList.remove('show');
    if (addBtn) addBtn.disabled = true;

    overlay.classList.add('open');
    document.body.style.overflow = 'hidden';
  }

  // ── Load full detail async ────────────────────────────────
  async function loadImages(productId) {
    try {
      const dto = await fetchJson(`${CTX}/products/${productId}`, { timeout: 8000 });
      overlay._detail = dto;

      if (descEl && dto.description) descEl.textContent = dto.description;

      const colorGroups = (dto.swatches || []).map(swatch => ({
        color:     swatch.color,
        hex:       swatch.hex,
        name:      swatch.name,
        available: !!(dto.availableSizesByColor?.[swatch.color]?.length),
        images:    (dto.imagesByColor?.[swatch.color] || []).map((base, i) => ({
          base, alt: `${swatch.name} image ${i + 1}`
        }))
      }));

      const defaultGroup = colorGroups.find(g => g.available) || colorGroups[0];
      activeColor = defaultGroup?.color || null;

      renderColors(colorGroups);
      _setColorName(defaultGroup?.name || null);

      if (defaultGroup) {
        setActiveGroup(defaultGroup);
        renderSizes(
            dto.sizesByColor?.[activeColor]          || [],
            dto.availableSizesByColor?.[activeColor] || []
        );
        _setDatesEnabled(defaultGroup.available);
      }

      _resolveVariant();
      _checkAddBtn();
    } catch (e) {
      console.error('[quick-view] detail fetch failed:', e);
    }
  }

  // ── Image helpers ─────────────────────────────────────────
  function setMainImage(basePath) {
    activeImage = basePath;
    if (!mainImg) return;
    const src = imgUrl(basePath, 'md');
    if (!src) {
      /* Placeholder when no real image */
      mainImg.removeAttribute('src');
      mainImg.removeAttribute('srcset');
      mainImg.style.display = 'none';
      /* show placeholder icon if present in the panel */
      const ph = mainImg.parentElement?.querySelector('.qv-img-placeholder');
      if (ph) ph.style.display = 'flex';
      return;
    }
    /* Hide placeholder if shown */
    const ph = mainImg.parentElement?.querySelector('.qv-img-placeholder');
    if (ph) ph.style.display = 'none';
    mainImg.style.display = '';
    mainImg.src    = src;
    mainImg.srcset = `${imgUrl(basePath,'sm')} 400w, ${imgUrl(basePath,'md')} 800w, ${imgUrl(basePath,'lg')} 1400w`;
    mainImg.sizes  = '(max-width:700px) 100vw, 50vw';
  }

  // ── Colour chips ──────────────────────────────────────────
  function renderColors(groups) {
    if (!colorsEl) return;
    while (colorsEl.firstChild) colorsEl.removeChild(colorsEl.firstChild);

    groups.forEach(g => {
      const btn = document.createElement('button');
      btn.type             = 'button';
      btn.className        = 'swatch'
          + (g.color === activeColor ? ' active'   : '')
          + (!g.available            ? ' sold-out' : '');
      btn.style.width      = '22px';
      btn.style.height     = '22px';
      btn.style.background = g.hex;
      btn.title            = g.name + (!g.available ? ' (Sold out)' : '');
      btn.dataset.color    = g.color;

      btn.addEventListener('click', () => {
        activeColor = g.color;
        _setColorName(g.name);
        colorsEl.querySelectorAll('.swatch')
            .forEach(b => b.classList.toggle('active', b.dataset.color === activeColor));
        setActiveGroup(g);
        const detail = overlay._detail;
        if (detail) {
          renderSizes(
              detail.sizesByColor?.[activeColor]          || [],
              detail.availableSizesByColor?.[activeColor] || []
          );
        }
        /* Disable/enable dates + show/hide sold-out banner */
        _setDatesEnabled(g.available);
        _resolveVariant();
        _checkAddBtn();
      });
      colorsEl.appendChild(btn);
    });
  }

  function setActiveGroup(group) {
    renderThumbs(group.images);
    if (group.images.length > 0) setMainImage(group.images[0].base);
    else setMainImage(null);
  }

  // ── Thumbnails ────────────────────────────────────────────
  // Only renders thumbnails for images that have a valid URL.
  // Hides the entire strip when there are 0 or 1 valid images —
  // a single thumbnail adds no navigation value and looks broken.
  function renderThumbs(images) {
    if (!thumbsEl) return;
    while (thumbsEl.firstChild) thumbsEl.removeChild(thumbsEl.firstChild);

    /* Filter to only images that produce a real URL */
    const valid = (images || []).filter(img => !!imgUrl(img.base, 'sm'));

    /* Hide the strip when there is nothing to navigate between */
    thumbsEl.style.display = valid.length > 1 ? '' : 'none';
    if (!valid.length) return;

    valid.forEach((img, i) => {
      const src = imgUrl(img.base, 'sm');
      const btn = document.createElement('button');
      btn.type                     = 'button';
      btn.className                = 'qv-thumb' + (i === 0 ? ' active' : '');
      btn.style.backgroundImage    = `url('${src}')`;
      btn.style.backgroundSize     = 'cover';
      btn.style.backgroundPosition = 'center';
      btn.setAttribute('aria-label', img.alt || `Image ${i + 1}`);
      btn.addEventListener('click', () => {
        thumbsEl.querySelectorAll('.qv-thumb').forEach(t => t.classList.remove('active'));
        btn.classList.add('active');
        setMainImage(img.base);
      });
      thumbsEl.appendChild(btn);
    });
  }

  // ── Sizes ──────────────────────────────────────────────────
  function renderSizes(allSizes, availSizes = []) {
    if (!sizesEl) return;
    while (sizesEl.firstChild) sizesEl.removeChild(sizesEl.firstChild);
    activeSize = null;

    if (!allSizes || !allSizes.length) {
      const span = document.createElement('span');
      span.className = 'qv-size active'; span.textContent = 'One size';
      sizesEl.appendChild(span);
      activeSize = 'OS';
      _resolveVariant();
      _checkAddBtn();
      return;
    }

    let firstAvailable = null;
    allSizes.forEach(s => {
      const isAvail = availSizes.includes(s);
      const btn = document.createElement('button');
      btn.type        = 'button';
      btn.className   = 'qv-size' + (!isAvail ? ' sold-out' : '');
      btn.textContent = s;
      btn.disabled    = !isAvail;
      if (isAvail && !firstAvailable) firstAvailable = { s, btn };
      btn.addEventListener('click', () => {
        activeSize = s;
        sizesEl.querySelectorAll('.qv-size')
            .forEach(b => b.classList.toggle('active', b === btn));
        _resolveVariant();
        _checkAddBtn();
      });
      sizesEl.appendChild(btn);
    });

    if (firstAvailable) {
      activeSize = firstAvailable.s;
      firstAvailable.btn.classList.add('active');
    }
    _resolveVariant();
    _checkAddBtn();
  }

  // ── Entry point ───────────────────────────────────────────
  document.addEventListener('click', e => {
    const trigger = e.target.closest('[data-qv]');
    if (!trigger) return;
    const data = {
      id:    trigger.dataset.id,
      name:  trigger.dataset.name  || '',
      brand: trigger.dataset.brand || '',
      price: trigger.dataset.price || 0,
      image: trigger.dataset.image || null,
    };
    openImmediate(data);
    loadImages(data.id);
  });

  // ── Add to Bag ────────────────────────────────────────────
  addBtn?.addEventListener('click', () => {
    if (!active || addBtn.disabled) return;

    if (!startDate || !endDate) {
      if (nudgeEl) {
        nudgeEl.textContent = (!startDate && !endDate)
            ? 'Please select your rental dates.'
            : !startDate
                ? 'Please select a start date.'
                : 'Please select a return date.';
        nudgeEl.classList.add('show');
        if (!startDate) document.getElementById('qvStartField')?.classList.add('qv-field-error');
        if (!endDate)   document.getElementById('qvEndField')?.classList.add('qv-field-error');
        clearTimeout(addBtn._nudgeTimer);
        addBtn._nudgeTimer = setTimeout(() => {
          nudgeEl.classList.remove('show');
          document.getElementById('qvStartField')?.classList.remove('qv-field-error');
          document.getElementById('qvEndField')?.classList.remove('qv-field-error');
        }, 4000);
      }
      return;
    }

    const days      = Math.round((endDate - startDate) / 86400000);
    const isoDate   = d => d.toISOString().slice(0, 10);
    const detail    = overlay._detail;
    const colorMeta = (detail?.swatches || []).find(s => s.color === activeColor);

    try {
      window.Cart.add({
        // ── CHANGED ───────────────────────────────────────────────────────
        // id must be variantId (the normalised item's identity root).
        // productId is kept separately so the server knows which product
        // this variant belongs to.
        id:           activeVariantId,          // was: active.id
        productId:    active.id,                // NEW: product PK for server payload
        // ─────────────────────────────────────────────────────────────────
        name:         active.name,
        brand:        active.brand,
        imageUrl:     activeImage || active.image,
        pricePerDay:  Number(active.price),
        size:         activeSize  || 'OS',
        color:        activeColor || null,
        colorName:    colorMeta?.name || null,
        variantId:    activeVariantId,          // kept for clarity / server payload
        inventoryQty: activeInventoryQty,
        qty:          1,
        days,
        startDate:    isoDate(startDate),       // was buried inside dates string
        endDate:      isoDate(endDate),         // now separate — server needs these
        dates:        `${isoDate(startDate)}/${isoDate(endDate)}`, // CardFactory._formatDates()
        qty:          1,
      });
    } catch (err) {
      console.error('[quick-view] Cart.add failed:', err);
    } finally {
      close();
    }
  });

  // ── Description accordion ─────────────────────────────────
  descToggle?.addEventListener('click', () => {
    descSection?.classList.toggle('open');
  });

  // ── Close ─────────────────────────────────────────────────
  function close() {
    overlay.classList.remove('open');
    document.body.style.overflow = '';
    active = null; activeColor = null; activeSize = null;
    activeVariantId = null; activeInventoryQty = null; activeImage = null;
    overlay._detail = null;
    startDate = null; endDate = null;
    fpStart?.clear(); fpEnd?.clear();
    if (fpEnd) { fpEnd.set('minDate', getMinStartDate()); fpEnd.set('maxDate', null); }
    if (summaryEl)   summaryEl.textContent = '';
    if (descSection) descSection.classList.remove('open');
    if (nudgeEl) nudgeEl.classList.remove('show');
    _setColorName(null);
    _setDatesEnabled(true);
    clearTimeout(addBtn?._nudgeTimer);
    document.getElementById('qvStartField')?.classList.remove('qv-field-error');
    document.getElementById('qvEndField')?.classList.remove('qv-field-error');
  }

  backdrop?.addEventListener('click', close);
  closeBtn?.addEventListener('click', close);
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape' && overlay.classList.contains('open')) close();
  });
})();