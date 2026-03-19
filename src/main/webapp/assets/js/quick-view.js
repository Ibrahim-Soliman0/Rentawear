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

  let active           = null;
  let activeColor      = null;
  let activeSize       = null;
  let activeVariantId  = null;   // product_variants.id — for cart_items insert
  let activeInventoryQty = null; // stock qty for + button cap in cart
  let activeImage      = null;
  let startDate   = null;
  let endDate     = null;
  let fpStart     = null;
  let fpEnd       = null;

  // ── Flatpickr — initialise once per page load ─────────────
  const startInput = document.getElementById('qvStartDate');
  const endInput   = document.getElementById('qvEndDate');

  if (startInput && typeof flatpickr === 'function') {
    fpStart = flatpickr(startInput, {
      dateFormat:    'd/m/Y',
      minDate:       'today',
      disableMobile: true,
      onChange(dates) {
        startDate = dates[0] || null;
        if (fpEnd) {
          if (startDate) {
            const minEnd = new Date(startDate);
            minEnd.setDate(minEnd.getDate() + 1);
            const maxEnd = new Date(startDate);
            maxEnd.setDate(maxEnd.getDate() + 30);
            fpEnd.set('minDate', minEnd);
            fpEnd.set('maxDate', maxEnd);
            if (endDate && (endDate <= startDate || endDate > maxEnd)) {
              fpEnd.clear();
              endDate = null;
            }
          } else {
            fpEnd.set('minDate', 'today');
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
      minDate:       'today',
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

  // ── Gate: button disabled only when no size selected ──────
  function _checkAddBtn() {
    if (!addBtn) return;
    addBtn.disabled = !activeSize;
  }

  // ── Enable / disable date pickers ────────────────────────
  // Called when switching to a sold-out colour — no point selecting
  // dates if nothing in this colour can be added to the bag.
  function _setDatesEnabled(enabled) {
    [startInput, endInput].forEach(inp => {
      if (!inp) return;
      inp.disabled = !enabled;
      inp.closest?.('.rw-input-wrap, .qv-date-field')
          ?.classList.toggle('qv-field-disabled', !enabled);
    });
    if (!enabled) {
      fpStart?.clear(); fpEnd?.clear();
      startDate = null; endDate = null;
      if (fpEnd) { fpEnd.set('minDate', 'today'); fpEnd.set('maxDate', null); }
      if (summaryEl) summaryEl.textContent = '';
    }
  }

  // ── Resolve variant ID and inventory qty from loaded detail ──
  // Must be called whenever activeColor or activeSize changes.
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
      colorNameEl.textContent   = name;
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

    // Placeholder while detail loads
    const ph = document.createElement('span');
    ph.className = 'qv-size'; ph.textContent = '—'; ph.style.opacity = '0.3';
    sizesEl?.appendChild(ph);

    _setColorName(null);

    // Wire "View full details" link
    if (viewFullEl) {
      viewFullEl.href    = `${CTX}/product/${data.id}`;
      viewFullEl.style.display = 'block';
    }

    startDate = null; endDate = null;
    fpStart?.clear(); fpEnd?.clear();
    if (fpEnd) { fpEnd.set('minDate', 'today'); fpEnd.set('maxDate', null); }
    if (summaryEl) summaryEl.textContent = '';
    _setDatesEnabled(true);

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
        // Disable date pickers if the default colour is fully sold out
        _setDatesEnabled(!!defaultGroup.available);
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
    if (mainImg) {
      mainImg.src    = imgUrl(basePath, 'md');
      mainImg.srcset = `${imgUrl(basePath,'sm')} 400w, ${imgUrl(basePath,'md')} 800w, ${imgUrl(basePath,'lg')} 1400w`;
      mainImg.sizes  = '(max-width:700px) 100vw, 50vw';
    }
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
        // Disable date pickers for sold-out colours
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
  }

  // ── Thumbnails ────────────────────────────────────────────
  function renderThumbs(images) {
    if (!thumbsEl) return;
    while (thumbsEl.firstChild) thumbsEl.removeChild(thumbsEl.firstChild);
    images.forEach((img, i) => {
      const btn = document.createElement('button');
      btn.type      = 'button';
      btn.className = 'qv-thumb' + (i === 0 ? ' active' : '');
      btn.style.backgroundImage    = `url('${imgUrl(img.base, 'sm')}')`;
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

  // ── Sizes — allSizes shows all, availSizes marks in-stock ──
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
      image: trigger.dataset.image || `${CTX}/assets/img/placeholder`,
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

    const days    = Math.round((endDate - startDate) / 86400000);
    const isoDate = d => d.toISOString().slice(0, 10);

    const detail    = overlay._detail;
    const colorMeta = (detail?.swatches || []).find(s => s.color === activeColor);

    Cart.add({
      id:           active.id,
      name:         active.name,
      brand:        active.brand,
      imageUrl:     activeImage || active.image,
      pricePerDay:  Number(active.price),
      size:         activeSize  || 'OS',
      color:        activeColor || null,
      colorName:    colorMeta?.name || null,
      variantId:    activeVariantId,
      inventoryQty: activeInventoryQty,
      days,
      dates:        `${isoDate(startDate)}/${isoDate(endDate)}`,
    });
    close();
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
    if (fpEnd) { fpEnd.set('minDate', 'today'); fpEnd.set('maxDate', null); }
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