(function () {
    /* card-factory.js — produces markup for all product surfaces.
       Depends on: utils.js (CTX, esc) — must load first.

       Public API:
         CardFactory.skeleton()            → placeholder card while fetching
         CardFactory.grid(product)         → home strips and catalog grid
         CardFactory.searchResult(product) → search modal result row
         CardFactory.cartItem(item)        → cart drawer item

       Navigation (PDP):
         grid() wraps the card in <a href="/product/{id}">.
         QV button uses e.preventDefault() (NOT stopPropagation) so anchor
         navigation is blocked while the click still bubbles to the
         document-level delegated listener in quick-view.js.
         Swatch row does the same.

       imgUrl(base, size) is exposed on window for product.js and quick-view.js.
    */

    // ── Image URL helper ──────────────────────────────────────────────────────
    // Returns null for placeholder/already-extended paths — callers must guard.
    function imgUrl(base, size) {
        if (!base || base.includes('placeholder') || base.endsWith('.jpg') || base.endsWith('.png')) {
            return null;
        }
        return `${CTX}${base}_${size}.jpg`;
    }
    window.imgUrl = imgUrl;

    const CardFactory = (function () {

        // ── Normalise ─────────────────────────────────────────────────────────
        function _normalise(raw) {
            const core = raw.core || raw;
            return {
                id:                  String(core.id          ?? ''),
                name:                core.name               ?? '',
                brand:               core.brand              ?? '',
                pricePerDay:         Number(core.pricePerDay ?? 0),
                imageUrl:            core.imageUrl           || null,
                isNew:               Boolean(raw.isNew),
                soldOut:             Boolean(raw.soldOut),
                category:            core.categoryId         ?? raw.category ?? '',
                swatches:            Array.isArray(raw.swatches) ? raw.swatches : [],
                primaryImageByColor: raw.primaryImageByColor || {},
                // cart-only fields
                dates:               raw.dates      ?? null,
                qty:                 Number(raw.qty  ?? 1),
                size:                raw.size        ?? null,
                color:               raw.color       ?? null,
                colorName:           raw.colorName   ?? null,
                variantId:           raw.variantId   ?? null,
                inventoryQty:        raw.inventoryQty ?? null,
            };
        }

        // ── Date range formatter ──────────────────────────────────────────────
        function _formatDates(raw) {
            if (!raw) return '';
            const parts = String(raw).split('/');
            if (parts.length !== 2) return raw;
            const months = ['Jan','Feb','Mar','Apr','May','Jun',
                'Jul','Aug','Sep','Oct','Nov','Dec'];
            const fmt = (d, showYear) =>
                `${d.getUTCDate()} ${months[d.getUTCMonth()]}${showYear ? ' ' + d.getUTCFullYear() : ''}`;
            const a = new Date(parts[0]);
            const b = new Date(parts[1]);
            if (isNaN(a) || isNaN(b)) return raw;
            const sameYear = a.getUTCFullYear() === b.getUTCFullYear();
            return sameYear
                ? `${fmt(a, false)} – ${fmt(b, true)}`
                : `${fmt(a, true)} – ${fmt(b, true)}`;
        }

        // ── Badge node ────────────────────────────────────────────────────────
        function _badgeNode(p) {
            if (p.soldOut) {
                const span = document.createElement('span');
                span.className   = 'product-badge sold-out';
                span.textContent = 'Sold Out';
                return span;
            }
            if (p.isNew) {
                const span = document.createElement('span');
                span.className   = 'product-badge new';
                span.textContent = 'New';
                return span;
            }
            return null;
        }

        // ── Placeholder icon ──────────────────────────────────────────────────
        function _makePlaceholderIcon() {
            const icon = document.createElement('div');
            icon.className = 'product-img-placeholder';
            const i = document.createElement('i');
            i.className = 'bi bi-image';
            icon.appendChild(i);
            return icon;
        }

        // ── Product image with srcset ─────────────────────────────────────────
        // Both <img> and placeholder always exist in the DOM, toggled via
        // display:none. onerror hides the img and shows the placeholder without
        // destroying either element, so subsequent swatch clicks can recover.
        function _productImgNode(base, name) {
            const div = document.createElement('div');
            div.className = 'product-img-inner';

            const img = document.createElement('img');
            img.alt      = name ? String(name) : '';
            img.loading  = 'lazy';
            img.decoding = 'async';

            const placeholder = _makePlaceholderIcon();

            const src = imgUrl(base, 'md');
            if (src) {
                img.src    = src;
                img.srcset = `${imgUrl(base,'sm')} 400w, ${imgUrl(base,'md')} 800w, ${imgUrl(base,'lg')} 1400w`;
                img.sizes  = '(max-width:480px) 100vw, (max-width:900px) 50vw, 33vw';
                img.style.display        = '';
                placeholder.style.display = 'none';
            } else {
                // No valid src, show placeholder, leave img hidden with no src
                img.style.display        = 'none';
                placeholder.style.display = '';
            }

            // onerror: hide broken img, reveal placeholder — does NOT destroy the element.
            // The img stays in the DOM so later valid swatch clicks can set a new src and show it.
            img.onerror = function () {
                img.style.display        = 'none';
                placeholder.style.display = '';
            };

            div.appendChild(img);
            div.appendChild(placeholder);
            return div;
        }

        // ── Swatch row ────────────────────────────────────────────────────────
        // Three changes from the original:
        //   1. Guard A removed — listener always attached regardless of whether
        //      any colour has an image, so swatch clicks never propagate to the
        //      anchor and navigate to PDP unintentionally.
        //   2. e.preventDefault() fires for every real swatch click, always
        //      before any return — anchor navigation is always blocked.
        //   3. Active-class update is unconditional, it no longer exits early
        //      if the clicked colour has no image.
        //   When a colour has an image: img shows, placeholder hides, src/srcset
        //   updated, qvBtn.dataset.image set.
        //   When a colour has no image: img hides, placeholder shows, qvBtn
        //   dataset cleared — card never gets stuck.
        //
        // initialActiveColor is passed in from grid() so the correct swatch
        // starts active (the one that owns the initial displayed image).
        function _swatchRowNode(p, qvBtn, el, initialActiveColor) {
            if (!p.swatches || !p.swatches.length) return null;

            const wrap = document.createElement('div');
            wrap.className = 'swatch-row';

            p.swatches.slice(0, 5).forEach((s, i) => {
                const span = document.createElement('span');
                // Active: prefer the colour that owns the initial image;
                // fall back to position 0 when no colour has any image.
                const isActive = initialActiveColor
                    ? s.color === initialActiveColor
                    : i === 0;
                span.className        = 'swatch' + (isActive ? ' active' : '');
                span.style.background = esc(s.hex || '#ccc');
                span.title            = s.name || '';
                if (s.color) span.dataset.color = s.color;
                if (s.slug)  span.dataset.slug  = s.slug;
                wrap.appendChild(span);
            });

            // Listener always attached, Guard A removed.
            wrap.addEventListener('click', e => {
                const sw = e.target.closest('.swatch[data-color]');
                // Click on gap between chips — don't interfere with bubbling.
                if (!sw) return;

                // Always block anchor navigation for real swatch clicks.
                e.preventDefault();

                // Always update active class — independent of whether the colour has an image.
                wrap.querySelectorAll('.swatch')
                    .forEach(s => s.classList.toggle('active', s === sw));

                const base        = p.primaryImageByColor[sw.dataset.color];
                const imgEl       = el.querySelector('.product-img-inner img');
                const placeholder = el.querySelector('.product-img-inner .product-img-placeholder');

                if (base) {
                    // Colour has an image, swap and reveal.
                    const sm = imgUrl(base, 'sm');
                    const md = imgUrl(base, 'md');
                    const lg = imgUrl(base, 'lg');
                    if (md && imgEl) {
                        imgEl.src           = md;
                        imgEl.srcset        = `${sm} 400w, ${md} 800w, ${lg} 1400w`;
                        imgEl.style.display = '';
                        if (placeholder) placeholder.style.display = 'none';
                    }
                    if (qvBtn) qvBtn.dataset.image = base;
                } else {
                    // Colour has no image, show placeholder, clear QV image.
                    if (imgEl)       imgEl.style.display        = 'none';
                    if (placeholder) placeholder.style.display  = '';
                    if (qvBtn)       qvBtn.dataset.image         = '';
                }
            });

            return wrap;
        }

        // ── Skeleton ──────────────────────────────────────────────────────────
        function skeleton() {
            const el = document.createElement('div');
            el.className = 'product-card product-skel';
            el.setAttribute('aria-hidden', 'true');

            const imgWrap = document.createElement('div');
            imgWrap.className = 'product-img-wrap product-img-wrap--skel';

            const footer = document.createElement('div');
            footer.className = 'product-footer';
            const info = document.createElement('div');
            info.className = 'product-info';

            ['rw-skel-line rw-skel-line--brand',
                'rw-skel-line rw-skel-line--name',
                'rw-skel-line rw-skel-line--price'].forEach(cls => {
                const d = document.createElement('div');
                d.className = cls;
                info.appendChild(d);
            });

            footer.appendChild(info);
            el.appendChild(imgWrap);
            el.appendChild(footer);
            return el;
        }

        // ── Grid card ─────────────────────────────────────────────────────────
        // Outer element changed from <div> to <a> for PDP navigation.
        //
        // QV BUTTON INTERACTION:
        //   The QV listener in quick-view.js is document-level delegation:
        //     document.addEventListener('click', e => { e.target.closest('[data-qv]')... })
        //   If we call stopPropagation() here, the event never reaches the
        //   document listener → QV breaks.
        //   If we call preventDefault() only, anchor navigation is cancelled
        //   AND the event still bubbles to document → QV works correctly.
        //
        // SWATCH ROW: same principle applied inside _swatchRowNode().
        //
        // Initial image and initial active swatch are both derived
        // from the first entry of primaryImageByColor, not from swatches[0] or
        // the stale p.imageUrl entity field. This guarantees:
        //   - The displayed image always matches the visually active swatch.
        //   - Products where the first variant has no image still show the
        //     first available image from any other colour.
        //   - qvBtn.dataset.image is consistent with what the card shows.
        function grid(rawProduct) {
            const p = _normalise(rawProduct);

            /* Outer element is a navigating anchor */
            const el = document.createElement('a');
            el.className = 'product-card';
            el.href      = `${CTX}/product/${encodeURIComponent(p.id)}`;
            el.setAttribute('aria-label', `View ${p.name}`);

            let initialColor = null;
            let initialImageUrl = null;

            if (p.swatches && p.swatches.length) {
                // Find the first swatch that has an image in primaryImageByColor
                for (let i = 0; i < p.swatches.length; i++) {
                    const color = p.swatches[i].color;
                    const base = p.primaryImageByColor[color];
                    if (base) {
                        initialColor = color;
                        initialImageUrl = base;
                        break;
                    }
                }
                // Fallback: if no swatch has an image, use the first swatch (show placeholder)
                if (!initialColor && p.swatches[0]) {
                    initialColor = p.swatches[0].color;
                    initialImageUrl = null;
                }
            } else {
                // No swatches at all – fallback to first key in primaryImageByColor
                initialColor = Object.keys(p.primaryImageByColor)[0] ?? null;
                initialImageUrl = initialColor ? p.primaryImageByColor[initialColor] : null;
            }

            const imgWrap = document.createElement('div');
            imgWrap.className = 'product-img-wrap';
            imgWrap.appendChild(_productImgNode(initialImageUrl, p.name));

            const badge = _badgeNode(p);
            if (badge) imgWrap.appendChild(badge);

            /* Quick-view button
               preventDefault() → anchor does not navigate
               No stopPropagation() → event bubbles to document QV handler */
            const qvBtn = document.createElement('button');
            qvBtn.className = 'product-qv';
            qvBtn.type      = 'button';
            qvBtn.setAttribute('aria-label', `Quick view ${p.name}`);
            qvBtn.addEventListener('click', e => {
                e.preventDefault();
                /* Event continues bubbling to document where quick-view.js picks it up */
            });

            const qvSvg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
            qvSvg.setAttribute('viewBox', '0 0 14 14');
            qvSvg.setAttribute('aria-hidden', 'true');
            const circ = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            circ.setAttribute('cx', '7'); circ.setAttribute('cy', '7'); circ.setAttribute('r', '4');
            const pth = document.createElementNS('http://www.w3.org/2000/svg', 'path');
            pth.setAttribute('d', 'M1 7s2-5 6-5 6 5 6 5-2 5-6 5-6-5-6-5z');
            qvSvg.appendChild(circ);
            qvSvg.appendChild(pth);
            qvBtn.appendChild(qvSvg);
            qvBtn.appendChild(document.createTextNode(' Quick View'));

            imgWrap.appendChild(qvBtn);

            // Pass initialColor so the correct swatch starts active.
            const swatchNode = _swatchRowNode(p, qvBtn, el, initialColor);

            const footer = document.createElement('div');
            footer.className = 'product-footer';
            const info = document.createElement('div');
            info.className = 'product-info';

            const brandP = document.createElement('p');
            brandP.className   = 'product-brand';
            brandP.textContent = p.brand;

            const nameP = document.createElement('p');
            nameP.className   = 'product-name';
            nameP.textContent = p.name;

            const priceP = document.createElement('p');
            priceP.className = 'product-price';
            // if (p.soldOut) {
            //     priceP.textContent = 'Sold out';
            //     priceP.style.color = 'var(--rw-muted)';
            // } else {
                const strong = document.createElement('strong');
                strong.textContent = `£${p.pricePerDay.toFixed(0)}`;
                priceP.appendChild(document.createTextNode('From '));
                priceP.appendChild(strong);
                priceP.appendChild(document.createTextNode('/day'));
            // }

            info.appendChild(brandP);
            info.appendChild(nameP);
            info.appendChild(priceP);
            footer.appendChild(info);

            el.appendChild(imgWrap);
            if (swatchNode) el.appendChild(swatchNode);
            el.appendChild(footer);

            qvBtn.dataset.qv    = '';
            qvBtn.dataset.id    = String(p.id);
            qvBtn.dataset.name  = p.name;
            qvBtn.dataset.brand = p.brand;
            qvBtn.dataset.price = String(p.pricePerDay);
            qvBtn.dataset.image = initialImageUrl || '';

            return el;
        }

        // ── Search result row ─────────────────────────────────────────────────
        // href updated: /products/{id} (JSON) → /product/{id} (HTML PDP).
        function searchResult(raw) {
            const p = _normalise(raw);

            const a = document.createElement('a');
            a.className = 'search-result-item';
            a.href      = `${CTX}/product/${encodeURIComponent(p.id)}`;

            const thumb = document.createElement('div');
            thumb.className = 'search-result-thumb';
            const thumbSrc = imgUrl(p.imageUrl, 'sm');
            if (thumbSrc) {
                const img = document.createElement('img');
                img.src      = thumbSrc;
                img.alt      = p.name || '';
                img.loading  = 'lazy';
                img.decoding = 'async';
                img.onerror  = function () { thumb.replaceChild(_makePlaceholderIcon(), img); };
                thumb.appendChild(img);
            } else {
                thumb.appendChild(_makePlaceholderIcon());
            }

            const info = document.createElement('div');
            info.className = 'search-result-info';

            const brand = document.createElement('p');
            brand.className   = 'search-result-brand';
            brand.textContent = p.brand;

            const name = document.createElement('p');
            name.className   = 'search-result-name';
            name.textContent = p.name;

            const price = document.createElement('p');
            price.className   = 'search-result-price';
            price.textContent = `From £${p.pricePerDay.toFixed(0)}/day`;

            info.appendChild(brand);
            info.appendChild(name);
            info.appendChild(price);
            a.appendChild(thumb);
            a.appendChild(info);
            return a;
        }

        // ── Cart drawer item ──────────────────────────────────────────────────
        function cartItem(raw) {
            const it  = _normalise(raw);
            const key = `${it.id}:${it.size || ''}:${it.color || ''}`;

            const li = document.createElement('li');
            li.className   = 'cart-item';
            li.dataset.key = key;
            li.dataset.id  = it.id;

            const imgWrap = document.createElement('div');
            imgWrap.className = 'cart-item-img';
            const cartSrc = imgUrl(it.imageUrl, 'sm');
            if (cartSrc) {
                const img = document.createElement('img');
                img.src      = cartSrc;
                img.alt      = it.name || '';
                img.loading  = 'lazy';
                img.decoding = 'async';
                img.onerror  = function () { imgWrap.replaceChild(_makePlaceholderIcon(), img); };
                imgWrap.appendChild(img);
            } else {
                imgWrap.appendChild(_makePlaceholderIcon());
            }

            const info = document.createElement('div');
            info.className = 'cart-item-info';

            const brand = document.createElement('span');
            brand.className   = 'cart-item-brand';
            brand.textContent = it.brand;
            info.appendChild(brand);

            const name = document.createElement('p');
            name.className   = 'cart-item-name';
            name.textContent = it.name;
            info.appendChild(name);

            const sizePart  = it.size      ? `Size ${it.size}` : '';
            const colorPart = it.colorName
                ? it.colorName
                : it.color
                    ? it.color.replace(/^#[0-9a-fA-F]+-/, '')
                    : '';
            if (sizePart || colorPart) {
                const variant = document.createElement('p');
                variant.className   = 'cart-item-dates';
                variant.textContent = [sizePart, colorPart].filter(Boolean).join(' · ');
                info.appendChild(variant);
            }

            const dates = document.createElement('p');
            dates.className   = 'cart-item-dates';
            dates.textContent = _formatDates(it.dates);
            info.appendChild(dates);

            const price = document.createElement('p');
            price.className   = 'cart-item-price';
            price.textContent = `£${it.pricePerDay.toFixed(0)}/day`;
            info.appendChild(price);

            const actions = document.createElement('div');
            actions.className = 'cart-item-qty';

            const removeBtn = document.createElement('button');
            removeBtn.className   = 'cart-remove';
            removeBtn.dataset.key = key;
            removeBtn.type        = 'button';
            removeBtn.setAttribute('aria-label', `Remove ${it.name} from bag`);
            removeBtn.textContent = 'Remove';

            const qtyControls = document.createElement('div');
            qtyControls.className = 'qty-controls';

            const dec = document.createElement('button');
            dec.className      = 'qty-btn';
            dec.dataset.action = 'dec';
            dec.dataset.key    = key;
            dec.type           = 'button';
            dec.setAttribute('aria-label', 'Decrease quantity');
            dec.textContent    = '−';

            const num = document.createElement('span');
            num.className   = 'qty-num';
            num.textContent = String(it.qty);

            const inc = document.createElement('button');
            inc.className      = 'qty-btn';
            inc.dataset.action = 'inc';
            inc.dataset.key    = key;
            inc.type           = 'button';
            inc.setAttribute('aria-label', 'Increase quantity');
            inc.textContent    = '+';
            if (it.inventoryQty != null) inc.dataset.inv = String(it.inventoryQty);
            if (it.inventoryQty != null && it.qty >= it.inventoryQty) inc.disabled = true;

            qtyControls.appendChild(dec);
            qtyControls.appendChild(num);
            qtyControls.appendChild(inc);
            actions.appendChild(removeBtn);
            actions.appendChild(qtyControls);

            li.appendChild(imgWrap);
            li.appendChild(info);
            li.appendChild(actions);
            return li;
        }

        return { skeleton, grid, searchResult, cartItem };
    })();

    window.CardFactory = CardFactory;
})();