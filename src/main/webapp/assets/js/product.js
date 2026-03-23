/* ============================================================
   product.js — Product Detail Page
   Depends on: utils.js (CTX, imgUrl, fetchJson)
               cart.js (Cart)

   Data source: window.RW_PRODUCT (ProductDetailDTO JSON)
                window.RW_RELATED (List<ProductCardDTO> JSON)

   Changes vs previous version:
     - Zoom/lightbox always opens the ACTIVE thumbnail (not a
       stale lightboxIndex that was only updated on thumb click).
     - Quantity selector removed entirely.
     - Date pickers: earliest bookable date = today + 2 days.
       Max rental duration = 30 days (end max = start + 30).
     - Sold-out colour: disables date pickers and Add to Bag,
       shows a visible banner with a clear UX message.
     - Images: placeholder icon shown when no image exists.
   ============================================================ */

'use strict';

(function () {

    const dto = window.RW_PRODUCT;
    if (!dto || !dto.core) {
        console.error('[product.js] window.RW_PRODUCT not set');
        return;
    }

    const core     = dto.core;
    const swatches = dto.swatches || [];

    /* ── State ────────────────────────────────────────────────── */
    let activeColor        = null;
    let activeSize         = null;
    let activeVariantId    = null;
    let activeInventoryQty = null;
    let activeImageBase    = null;
    let activeImages       = [];   // full image list for active colour (lightbox)
    let startDate          = null;
    let endDate            = null;
    let fpStart            = null;
    let fpEnd              = null;
    let colorIsSoldOut     = false;

    /* ── DOM refs ─────────────────────────────────────────────── */
    const mainImg       = document.getElementById('pdpMainImg');
    const mainImgWrap   = document.getElementById('pdpMainImgWrap');
    const thumbsEl      = document.getElementById('pdpThumbs');
    const colorsEl      = document.getElementById('pdpColors');
    const colorNameEl   = document.getElementById('pdpColorName');
    const sizesEl       = document.getElementById('pdpSizes');
    const stockLabelEl  = document.getElementById('pdpStockLabel');
    const atbBtn        = document.getElementById('pdpAtb');
    const nudgeEl       = document.getElementById('pdpNudge');
    const dateSummaryEl = document.getElementById('pdpDateSummary');
    const descSection   = document.getElementById('pdpDescSection');
    const descToggle    = document.getElementById('pdpDescToggle');
    const descEl        = document.getElementById('pdpDesc');
    const badgeNew      = document.getElementById('pdpBadgeNew');
    const priceValEl    = document.getElementById('pdpPriceVal');
    const wishlistBtn   = document.getElementById('pdpWishlist');
    const soldOutBanner = document.getElementById('pdpSoldOutBanner');
    const relatedGrid   = document.getElementById('pdpRelatedGrid');
    const relatedSection= document.getElementById('pdpRelatedSection');
    const zoomBtn       = document.getElementById('pdpZoomBtn');
    const lightbox      = document.getElementById('pdpLightbox');
    const lightboxImg   = document.getElementById('pdpLightboxImg');
    const lightboxClose = document.getElementById('pdpLightboxClose');
    const lightboxBack  = document.getElementById('pdpLightboxBackdrop');
    const lightboxPrev  = document.getElementById('pdpLightboxPrev');
    const lightboxNext  = document.getElementById('pdpLightboxNext');
    const bcMid         = document.getElementById('pdpBcMid');

    /* ── Earliest bookable date (today + 2) ──────────────────── */
    function getMinStartDate() {
        const d = new Date();
        d.setDate(d.getDate() + 2);
        d.setHours(0, 0, 0, 0);
        return d;
    }

    /* ── Init ─────────────────────────────────────────────────── */
    document.addEventListener('DOMContentLoaded', function () {
        populateStaticFields();
        initColors();
        initDatePickers();
        initAccordion();
        initWishlist();
        initLightbox();
        renderRelated();
        updateBreadcrumb();
    });

    /* ── Static fields ────────────────────────────────────────── */
    function populateStaticFields() {
        document.getElementById('pdpBrand').textContent = core.brand || 'rentawear';
        document.getElementById('pdpName').textContent  = core.name  || '';
        if (priceValEl) priceValEl.textContent = `£${Number(core.pricePerDay).toFixed(0)}`;
        if (descEl && dto.description) descEl.textContent = dto.description;
        if (badgeNew && dto.isNew) badgeNew.style.display = 'block';
    }

    /* ── Breadcrumb ───────────────────────────────────────────── */
    function updateBreadcrumb() {
        if (!bcMid) return;
        const sep = document.createElement('span');
        sep.className   = 'pdp-bc-sep';
        sep.textContent = '›';

        const params = new URLSearchParams(window.location.search);
        const from   = params.get('from');

        let midLabel, midHref;
        if (from === 'search') {
            midLabel = 'Search Results';
            midHref  = document.referrer || CTX + '/catalog';
        } else if (core.gender && core.categoryId) {
            midLabel = core.gender === 'FEMALE' ? "Women's" : "Men's";
            midHref  = `${CTX}/catalog?gender=${core.gender}`;
        } else {
            midLabel = 'All Products';
            midHref  = CTX + '/catalog';
        }

        const a = document.createElement('a');
        a.href        = midHref;
        a.textContent = midLabel;
        bcMid.appendChild(a);
        bcMid.appendChild(sep);
    }

    /* ── Colour swatches ──────────────────────────────────────── */
    function initColors() {
        if (!swatches.length) return;
        swatches.forEach((s, i) => {
            const btn = document.createElement('button');
            btn.type             = 'button';
            btn.className        = 'pdp-color-swatch';
            btn.style.background = s.hex;
            btn.title            = s.name;
            btn.dataset.color    = s.color;
            btn.setAttribute('aria-label', s.name);
            btn.addEventListener('click', () => selectColor(s.color, btn));
            colorsEl.appendChild(btn);
            if (i === 0) selectColor(s.color, btn);
        });
    }

    function selectColor(color, btn) {
        activeColor = color;

        colorsEl.querySelectorAll('.pdp-color-swatch')
            .forEach(b => b.classList.toggle('active', b === btn));

        const swatch = swatches.find(s => s.color === color);
        if (colorNameEl) colorNameEl.textContent = swatch ? swatch.name : '';

        /* Determine sold-out state for this colour */
        const availSizes = (dto.availableSizesByColor || {})[color] || [];
        colorIsSoldOut   = availSizes.length === 0;

        /* Gallery */
        const images = (dto.imagesByColor || {})[color] || [];
        activeImages = images;
        if (images.length > 0) {
            setMainImage(images[0]);
            renderThumbs(images);
        } else {
            setMainImagePlaceholder();
            renderThumbs([]);
        }

        /* Sizes */
        const allSizes = (dto.sizesByColor || {})[color] || [];
        renderSizes(allSizes, availSizes);

        /* Sold-out state — disable/enable dates + ATB */
        _setColorSoldOut(colorIsSoldOut);
    }

    /* ── Sold-out colour handler ──────────────────────────────── */
    function _setColorSoldOut(isSoldOut) {
        /* Banner */
        if (soldOutBanner) {
            soldOutBanner.style.display = isSoldOut ? 'flex' : 'none';
        }

        /* Date inputs */
        const startInput = document.getElementById('pdpStartDate');
        const endInput   = document.getElementById('pdpEndDate');
        const dateSection = document.getElementById('pdpDatesSection');

        [startInput, endInput].forEach(inp => {
            if (!inp) return;
            inp.disabled = isSoldOut;
        });
        if (dateSection) dateSection.classList.toggle('is-disabled', isSoldOut);

        if (isSoldOut) {
            fpStart?.clear(); fpEnd?.clear();
            startDate = null; endDate = null;
            if (fpEnd) { fpEnd.set('minDate', getMinStartDate()); fpEnd.set('maxDate', null); }
            if (dateSummaryEl) dateSummaryEl.textContent = '';
        }

        /* ATB button */
        _checkAtb();
    }

    /* ── Gallery ──────────────────────────────────────────────── */
    function setMainImage(base) {
        activeImageBase = base;
        if (!mainImg) return;

        const src = imgUrl(base, 'lg');
        if (!src) { setMainImagePlaceholder(); return; }

        mainImg.classList.add('is-loading');
        mainImg.src     = src;
        mainImg.srcset  = `${imgUrl(base,'sm')} 400w, ${imgUrl(base,'md')} 800w, ${imgUrl(base,'lg')} 1400w`;
        mainImg.style.display = '';
        mainImg.onload  = () => mainImg.classList.remove('is-loading');
        mainImg.onerror = () => setMainImagePlaceholder();

        /* Hide any existing placeholder */
        const ph = mainImgWrap?.querySelector('.pdp-img-placeholder');
        if (ph) ph.style.display = 'none';
    }

    function setMainImagePlaceholder() {
        activeImageBase = null;
        if (mainImg) mainImg.style.display = 'none';
        if (!mainImgWrap) return;
        let ph = mainImgWrap.querySelector('.pdp-img-placeholder');
        if (!ph) {
            ph = document.createElement('div');
            ph.className = 'pdp-img-placeholder';
            const i = document.createElement('i');
            i.className = 'bi bi-image';
            ph.appendChild(i);
            mainImgWrap.appendChild(ph);
        }
        ph.style.display = 'flex';
    }

    function renderThumbs(images) {
        if (!thumbsEl) return;
        thumbsEl.replaceChildren();
        if (!images.length) return;

        images.forEach((base, i) => {
            const btn = document.createElement('button');
            btn.type      = 'button';
            btn.className = 'pdp-thumb' + (i === 0 ? ' active' : '');
            btn.setAttribute('aria-label', `View image ${i + 1}`);

            const src = imgUrl(base, 'sm');
            if (src) {
                const img = document.createElement('img');
                img.src     = src;
                img.alt     = '';
                img.loading = 'lazy';
                img.decoding = 'async';
                img.onerror = () => {
                    img.style.display = 'none';
                    const ph = document.createElement('div');
                    ph.className = 'pdp-thumb-placeholder';
                    const ic = document.createElement('i');
                    ic.className = 'bi bi-image';
                    ph.appendChild(ic);
                    btn.appendChild(ph);
                };
                btn.appendChild(img);
            } else {
                const ph = document.createElement('div');
                ph.className = 'pdp-thumb-placeholder';
                const ic = document.createElement('i');
                ic.className = 'bi bi-image';
                ph.appendChild(ic);
                btn.appendChild(ph);
            }

            btn.addEventListener('click', () => {
                thumbsEl.querySelectorAll('.pdp-thumb')
                    .forEach(t => t.classList.remove('active'));
                btn.classList.add('active');
                setMainImage(base);
            });
            thumbsEl.appendChild(btn);
        });
    }

    /* ── Get the active thumbnail's index ────────────────────── */
    /* This is the single source of truth for lightbox opening.
       lightboxIndex was previously stale after colour changes —
       we now always read the DOM directly.                        */
    function getActiveThumbnailIndex() {
        if (!thumbsEl) return 0;
        const thumbs = Array.from(thumbsEl.querySelectorAll('.pdp-thumb'));
        const idx = thumbs.findIndex(b => b.classList.contains('active'));
        return idx >= 0 ? idx : 0;
    }

    /* ── Sizes ────────────────────────────────────────────────── */
    function renderSizes(allSizes, availSizes) {
        if (!sizesEl) return;
        sizesEl.replaceChildren();
        activeSize         = null;
        activeVariantId    = null;
        activeInventoryQty = null;

        if (!allSizes.length) {
            const span = document.createElement('span');
            span.className   = 'pdp-size-btn active';
            span.textContent = 'One Size';
            sizesEl.appendChild(span);
            activeSize = 'OS';
            resolveVariant();
            _checkAtb();
            return;
        }

        let firstAvail = null;
        allSizes.forEach(s => {
            const avail = availSizes.includes(s);
            const btn   = document.createElement('button');
            btn.type        = 'button';
            btn.className   = 'pdp-size-btn' + (!avail ? ' sold-out' : '');
            btn.textContent = s;
            btn.disabled    = !avail;
            if (avail && !firstAvail) firstAvail = { s, btn };

            btn.addEventListener('click', () => {
                activeSize = s;
                sizesEl.querySelectorAll('.pdp-size-btn')
                    .forEach(b => b.classList.toggle('active', b === btn));
                resolveVariant();
                _checkAtb();
            });
            sizesEl.appendChild(btn);
        });

        if (firstAvail) {
            activeSize = firstAvail.s;
            firstAvail.btn.classList.add('active');
            resolveVariant();
        }
        _checkAtb();
    }

    function resolveVariant() {
        if (!activeColor) { activeVariantId = null; activeInventoryQty = null; return; }
        const sizeKey = activeSize || 'OS';
        activeVariantId    = dto.variantIdByColorAndSize?.[activeColor]?.[sizeKey] ?? null;
        activeInventoryQty = activeVariantId != null
            ? (dto.quantityByVariantId?.[activeVariantId] ?? null)
            : null;
        updateStockLabel();
    }

    function updateStockLabel() {
        if (!stockLabelEl) return;
        if (colorIsSoldOut) {
            stockLabelEl.textContent = 'Sold out';
            stockLabelEl.style.color = 'var(--rw-danger)';
            return;
        }
        if (activeInventoryQty == null) { stockLabelEl.textContent = ''; return; }
        if (activeInventoryQty === 0)   {
            stockLabelEl.textContent = 'Out of stock';
            stockLabelEl.style.color = 'var(--rw-danger)';
            return;
        }
        if (activeInventoryQty <= 3) {
            stockLabelEl.textContent = `Only ${activeInventoryQty} left`;
            stockLabelEl.style.color = 'var(--rw-danger)';
        } else {
            stockLabelEl.textContent = 'In stock';
            stockLabelEl.style.color = 'var(--rw-teal)';
        }
    }

    /* ── Date pickers ─────────────────────────────────────────── */
    function initDatePickers() {
        const startInput = document.getElementById('pdpStartDate');
        const endInput   = document.getElementById('pdpEndDate');
        if (!startInput || typeof flatpickr !== 'function') return;

        fpStart = flatpickr(startInput, {
            dateFormat:    'd/m/Y',
            minDate:       getMinStartDate(),   /* earliest = today + 2 */
            disableMobile: true,
            onChange(dates) {
                startDate = dates[0] || null;
                if (fpEnd && startDate) {
                    const minEnd = new Date(startDate);
                    minEnd.setDate(minEnd.getDate() + 1);
                    const maxEnd = new Date(startDate);
                    maxEnd.setDate(maxEnd.getDate() + 30); /* max 30-day rental */
                    fpEnd.set('minDate', minEnd);
                    fpEnd.set('maxDate', maxEnd);
                    if (endDate && (endDate <= startDate || endDate > maxEnd)) {
                        fpEnd.clear(); endDate = null;
                    }
                } else if (fpEnd) {
                    fpEnd.set('minDate', getMinStartDate());
                    fpEnd.set('maxDate', null);
                }
                updateDateSummary();
                _checkAtb();
                nudgeEl?.classList.remove('show');
                document.getElementById('pdpStartField')?.classList.remove('is-error');
            }
        });

        if (endInput) {
            fpEnd = flatpickr(endInput, {
                dateFormat:    'd/m/Y',
                minDate:       getMinStartDate(),
                disableMobile: true,
                onChange(dates) {
                    endDate = dates[0] || null;
                    updateDateSummary();
                    _checkAtb();
                    nudgeEl?.classList.remove('show');
                    document.getElementById('pdpEndField')?.classList.remove('is-error');
                }
            });
        }
    }

    function updateDateSummary() {
        if (!dateSummaryEl) return;
        if (!startDate || !endDate) { dateSummaryEl.textContent = ''; return; }
        const days  = Math.round((endDate - startDate) / 86400000);
        const total = days * Number(core.pricePerDay);
        dateSummaryEl.textContent =
            `${days} day${days !== 1 ? 's' : ''} · £${total.toFixed(0)} total`;
    }

    /* ── Add to Bag gate ──────────────────────────────────────── */
    function _checkAtb() {
        if (!atbBtn) return;
        /* Disabled if: colour is sold out, or no size selected */
        atbBtn.disabled = colorIsSoldOut || !activeSize;
    }

    atbBtn?.addEventListener('click', () => {
        if (atbBtn.disabled) return;

        /* Sold-out guard (belt-and-braces) */
        if (colorIsSoldOut) return;

        if (!startDate || !endDate) {
            if (nudgeEl) {
                nudgeEl.textContent = !startDate && !endDate
                    ? 'Please select your rental dates.'
                    : !startDate ? 'Please select a start date.'
                        : 'Please select a return date.';
                nudgeEl.classList.add('show');
                if (!startDate) document.getElementById('pdpStartField')?.classList.add('is-error');
                if (!endDate)   document.getElementById('pdpEndField')?.classList.add('is-error');
                clearTimeout(atbBtn._nudgeTimer);
                atbBtn._nudgeTimer = setTimeout(() => {
                    nudgeEl.classList.remove('show');
                    document.getElementById('pdpStartField')?.classList.remove('is-error');
                    document.getElementById('pdpEndField')?.classList.remove('is-error');
                }, 4000);
            }
            return;
        }

        const days    = Math.round((endDate - startDate) / 86400000);
        const isoDate = d => d.toISOString().slice(0, 10);
        const swatch  = swatches.find(s => s.color === activeColor);

        try {
            Cart.add({
                id:           String(core.id),
                name:         core.name,
                brand:        core.brand,
                imageUrl:     activeImageBase || core.imageUrl,
                pricePerDay:  Number(core.pricePerDay),
                size:         activeSize     || 'OS',
                color:        activeColor    || null,
                colorName:    swatch?.name   || null,
                variantId:    activeVariantId,
                inventoryQty: activeInventoryQty,
                qty:          1,
                days,
                dates: `${isoDate(startDate)}/${isoDate(endDate)}`,
            });

            const span = atbBtn.querySelector('span');
            if (span) {
                const orig = span.textContent;
                span.textContent        = 'Added ✓';
                atbBtn.style.background = 'var(--rw-teal)';
                setTimeout(() => {
                    span.textContent        = orig;
                    atbBtn.style.background = '';
                }, 1800);
            }
        } catch (err) {
            console.error('[product.js] Cart.add failed:', err);
        }
    });

    /* ── Wishlist ─────────────────────────────────────────────── */
    function initWishlist() {
        wishlistBtn?.addEventListener('click', () => wishlistBtn.classList.toggle('active'));
    }

    /* ── Description accordion ────────────────────────────────── */
    function initAccordion() {
        descToggle?.addEventListener('click', () => descSection?.classList.toggle('open'));
        descSection?.classList.add('open');
    }

    /* ── Lightbox ─────────────────────────────────────────────── */
    function initLightbox() {
        /* Main image wrap click — open at the ACTIVE thumbnail index */
        mainImgWrap?.addEventListener('click', e => {
            if (e.target === zoomBtn || zoomBtn?.contains(e.target)) return;
            openLightbox(getActiveThumbnailIndex());
        });

        /* Zoom button — same: always read active thumb from DOM */
        zoomBtn?.addEventListener('click', e => {
            e.stopPropagation();
            openLightbox(getActiveThumbnailIndex());
        });

        lightboxClose?.addEventListener('click', closeLightbox);
        lightboxBack?.addEventListener('click',  closeLightbox);

        lightboxPrev?.addEventListener('click', () => {
            if (!activeImages.length) return;
            const idx = (getLightboxIndex() - 1 + activeImages.length) % activeImages.length;
            showLightboxImage(idx);
        });
        lightboxNext?.addEventListener('click', () => {
            if (!activeImages.length) return;
            const idx = (getLightboxIndex() + 1) % activeImages.length;
            showLightboxImage(idx);
        });

        document.addEventListener('keydown', e => {
            if (!lightbox?.classList.contains('open')) return;
            if (e.key === 'Escape')     closeLightbox();
            if (e.key === 'ArrowLeft')  lightboxPrev?.click();
            if (e.key === 'ArrowRight') lightboxNext?.click();
        });
    }

    /* Read the current lightbox index from the data attribute on the img */
    function getLightboxIndex() {
        return parseInt(lightboxImg?.dataset.index || '0', 10);
    }

    function openLightbox(idx) {
        if (!lightbox || !activeImages.length) return;
        showLightboxImage(idx >= 0 ? idx : 0);
        lightbox.classList.add('open');
        lightbox.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';
        updateLightboxNav();
    }

    function closeLightbox() {
        lightbox?.classList.remove('open');
        lightbox?.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = '';
    }

    function showLightboxImage(idx) {
        if (!lightboxImg || !activeImages[idx]) return;
        lightboxImg.src          = imgUrl(activeImages[idx], 'lg') || '';
        lightboxImg.alt          = `${core.name} — image ${idx + 1}`;
        lightboxImg.dataset.index = String(idx); /* store current index on the element */
        updateLightboxNav();

        /* Sync active thumbnail in the strip */
        if (thumbsEl) {
            thumbsEl.querySelectorAll('.pdp-thumb')
                .forEach((t, i) => t.classList.toggle('active', i === idx));
        }
    }

    function updateLightboxNav() {
        const multiple = activeImages.length > 1;
        if (lightboxPrev) lightboxPrev.style.display = multiple ? '' : 'none';
        if (lightboxNext) lightboxNext.style.display = multiple ? '' : 'none';
    }

    /* ── Related products ─────────────────────────────────────── */
    function renderRelated() {
        const related = window.RW_RELATED;
        if (!relatedGrid || !related || !related.length) {
            if (relatedSection) relatedSection.style.display = 'none';
            return;
        }
        const frag = document.createDocumentFragment();
        related.forEach(p => {
            try { frag.appendChild(CardFactory.grid(p)); }
            catch (e) { console.warn('[product.js] related card error:', e); }
        });
        relatedGrid.replaceChildren(frag);
    }

}());