(function () {
    /* card-factory.js — produces markup for all product surfaces.
       Depends on: utils.js (CTX, esc) — must load first.

       Public API:
         CardFactory.skeleton()            → placeholder card while fetching
         CardFactory.grid(product)         → home strips and catalog grid
         CardFactory.searchResult(product) → search modal result row
         CardFactory.cartItem(item)        → cart drawer item

       DTO shapes expected:
         grid/searchResult: ProductCardDTO  { core: { id, name, brand, pricePerDay,
                                             imageUrl, gender, categoryId },
                                             isNew, soldOut, swatches,
                                             primaryImageByColor }
         cartItem:          flat object     { id, name, brand, pricePerDay,
                                             imageUrl, dates, qty }

       imgUrl(base, size) is exposed on window for use by quick-view.js.
       Always use this — never append size suffix manually.
    */

    // ── Image URL helper ──────────────────────────────────────────────────────
    // base: stored in imageUrl fields, e.g. /assets/img/products/42/navy/abc123
    //       (no size suffix, no extension)
    // size: "sm" (400px) | "md" (800px) | "lg" (1400px)
    // Falls back to placeholder when base is empty or already has an extension.
    function imgUrl(base, size) {
        const b = (base && !base.endsWith('.jpg') && !base.endsWith('.png'))
            ? base
            : '/assets/img/placeholder';
        return `${CTX}${b}_${size}.jpg`;
    }
    window.imgUrl = imgUrl;

    const CardFactory = (function () {

        // ── Normalise ─────────────────────────────────────────────────────────
        // ProductCardDTO nests core fields. Cart items are flat.
        // core || raw handles both without breaking cart rendering.
        function _normalise(raw) {
            const core = raw.core || raw;
            return {
                id:                  String(core.id          ?? ''),
                name:                core.name               ?? '',
                brand:               core.brand              ?? '',
                pricePerDay:         Number(core.pricePerDay ?? 0),
                imageUrl:            core.imageUrl           || '/assets/img/placeholder',
                isNew:               Boolean(raw.isNew),
                soldOut:             Boolean(raw.soldOut),
                category:            core.categoryId         ?? raw.category ?? '',
                swatches:            Array.isArray(raw.swatches) ? raw.swatches : [],
                primaryImageByColor: raw.primaryImageByColor || {},
                // cart-only fields
                dates:               raw.dates      ?? null,
                qty:                 Number(raw.qty  ?? 1),
                size:                raw.size        ?? null,
                color:               raw.color       ?? null,   // machine key  e.g. "#FF0000-Red"
                colorName:           raw.colorName   ?? null,   // display name e.g. "Red"
                variantId:           raw.variantId   ?? null,
                inventoryQty:        raw.inventoryQty ?? null,  // max allowed qty for + cap
            };
        }

        // ── Date range formatter ──────────────────────────────────────────────
        // Accepts ISO range: "2026-03-19/2026-03-26"
        // Produces: "19 Mar – 26 Mar 2026" (same year) or "19 Dec 2025 – 3 Jan 2026"
        // Falls back to the raw string if unparseable.
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
        // Returns the correct badge for the card's state:
        //   soldOut → charcoal "Sold Out" badge (top-right)
        //   isNew   → teal "New" badge (top-left, standard position)
        //   neither → null
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

        // ── Product image with srcset ─────────────────────────────────────────
        // Real <img> so srcset/lazy-loading work natively.
        function _productImgNode(base, name) {
            const div = document.createElement('div');
            div.className = 'product-img-inner';
            const img = document.createElement('img');
            img.src     = imgUrl(base, 'md');
            img.srcset  = `${imgUrl(base,'sm')} 400w, ${imgUrl(base,'md')} 800w, ${imgUrl(base,'lg')} 1400w`;
            img.sizes   = '(max-width:480px) 100vw, (max-width:900px) 50vw, 33vw';
            img.alt     = name ? String(name) : '';
            img.loading  = 'lazy';
            img.decoding = 'async';
            div.appendChild(img);
            return div;
        }

        // ── Swatch row ────────────────────────────────────────────────────────
        // Clicking a swatch swaps the card image via primaryImageByColor map
        // and updates the QV dataset.image so quick-view opens the right image.
        // Returns null when the product has no swatches (single-colour products).
        function _swatchRowNode(p, qvBtn, el) {
            if (!p.swatches || !p.swatches.length) return null;

            const wrap = document.createElement('div');
            wrap.className = 'swatch-row';

            p.swatches.slice(0, 5).forEach((s, i) => {
                const span = document.createElement('span');
                span.className        = 'swatch' + (i === 0 ? ' active' : '');
                span.style.background = esc(s.hex || '#ccc');
                span.title            = s.name || '';
                if (s.color) span.dataset.color = s.color;
                if (s.slug)  span.dataset.slug  = s.slug;
                wrap.appendChild(span);
            });

            // Wire image swap only when the map is populated
            if (Object.keys(p.primaryImageByColor).length) {
                wrap.addEventListener('click', e => {
                    const sw = e.target.closest('.swatch[data-color]');
                    if (!sw) return;
                    const base = p.primaryImageByColor[sw.dataset.color];
                    if (!base) return;

                    const img = el.querySelector('.product-img-inner img');
                    if (img) {
                        img.src    = imgUrl(base, 'md');
                        img.srcset = `${imgUrl(base,'sm')} 400w, ${imgUrl(base,'md')} 800w, ${imgUrl(base,'lg')} 1400w`;
                    }
                    // Keep QV in sync so it opens with the selected colour image
                    if (qvBtn) qvBtn.dataset.image = base;

                    wrap.querySelectorAll('.swatch')
                        .forEach(s => s.classList.toggle('active', s === sw));
                });
            }

            return wrap;
        }

        // ── Wishlist heart SVG ────────────────────────────────────────────────
        function _wishlistSvgNode(filled) {
            const svg  = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
            svg.setAttribute('viewBox', '0 0 15 15');
            svg.setAttribute('aria-hidden', 'true');
            const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
            path.setAttribute('d', 'M7.5 13S1 9 1 4.5a3.5 3.5 0 0 1 6.5-1.8A3.5 3.5 0 0 1 14 4.5C14 9 7.5 13 7.5 13z');
            path.setAttribute('stroke-width', '1.8');
            if (filled) {
                path.setAttribute('fill', 'currentColor');
            } else {
                path.setAttribute('fill', 'none');
                path.setAttribute('stroke', 'currentColor');
            }
            svg.appendChild(path);
            return svg;
        }

        // ── Skeleton ──────────────────────────────────────────────────────────
        // Shown while the section fetch is in-flight.
        // Matches grid() DOM structure so CSS sizing is identical.
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
        // Used by: home page strips, catalog grid.
        //
        // Sold-out logic:
        //   soldOut = true  → charcoal badge replaces wishlist button entirely
        //   soldOut = false → wishlist button shown as normal
        //
        // QV dataset carries the minimum needed for the instant open (Phase 1).
        // Full detail (description, swatches, sizes, images) is fetched async
        // from /products/{id} by quick-view.js once the overlay is visible.
        function grid(rawProduct) {
            const el = document.createElement('div');
            el.className = 'product-card';

            const p = _normalise(rawProduct);

            // ── Resolve the correct initial image ─────────────────────────────
            // Swatch[0] is rendered as active, so the card image must match it.
            // primaryImageByColor keys are the raw color strings (e.g. "#FF0000-Red"),
            // identical to swatch.color — both are built from the same product_images
            // rows in the mapper. We fall back to p.imageUrl only when no images exist.
            const firstSwatchColor = p.swatches[0]?.color ?? null;
            const initialImageUrl  =
                (firstSwatchColor != null && p.primaryImageByColor[firstSwatchColor] != null)
                    ? p.primaryImageByColor[firstSwatchColor]
                    : p.imageUrl;

            // Image wrap
            const imgWrap = document.createElement('div');
            imgWrap.className = 'product-img-wrap';
            imgWrap.appendChild(_productImgNode(initialImageUrl, p.name));

            // Badge (sold-out or new — mutually exclusive, sold-out wins)
            const badge = _badgeNode(p);
            if (badge) imgWrap.appendChild(badge);

            // Quick-view button (always present)
            const qvBtn = document.createElement('button');
            qvBtn.className = 'product-qv';
            qvBtn.type      = 'button';
            qvBtn.setAttribute('aria-label', `Quick view ${p.name}`);
            const qvSvg  = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
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

            // Wishlist button — hidden when sold out
            if (!p.soldOut) {
                const wishBtn = document.createElement('button');
                wishBtn.className = 'product-wishlist';
                wishBtn.type      = 'button';
                wishBtn.setAttribute('aria-label', `Add ${p.name} to wishlist`);
                wishBtn.appendChild(_wishlistSvgNode(false));

                wishBtn.addEventListener('click', e => {
                    e.preventDefault();
                    e.stopPropagation();
                    const isActive = wishBtn.classList.toggle('active');
                    while (wishBtn.firstChild) wishBtn.removeChild(wishBtn.firstChild);
                    wishBtn.appendChild(_wishlistSvgNode(isActive));
                    wishBtn.setAttribute('aria-label',
                        `${isActive ? 'Remove' : 'Add'} ${p.name} ${isActive ? 'from' : 'to'} wishlist`);
                });

                imgWrap.appendChild(wishBtn);
            }

            imgWrap.appendChild(qvBtn);

            // Swatch row — pass qvBtn and el so swatches can keep both in sync
            const swatchNode = _swatchRowNode(p, qvBtn, el);

            // Footer
            const footer = document.createElement('div');
            footer.className = 'product-footer';
            const info = document.createElement('div');
            info.className = 'product-info';

            const brandP = document.createElement('p');
            brandP.className = 'product-brand';
            brandP.textContent = p.brand;

            const nameP = document.createElement('p');
            nameP.className = 'product-name';
            nameP.textContent = p.name;

            const priceP = document.createElement('p');
            priceP.className = 'product-price';
            if (p.soldOut) {
                priceP.textContent = 'Sold out';
                priceP.style.color = 'var(--rw-muted)';
            } else {
                const strong = document.createElement('strong');
                strong.textContent = `£${p.pricePerDay.toFixed(0)}`;
                priceP.appendChild(document.createTextNode('From '));
                priceP.appendChild(strong);
                priceP.appendChild(document.createTextNode('/day'));
            }

            info.appendChild(brandP);
            info.appendChild(nameP);
            info.appendChild(priceP);
            footer.appendChild(info);

            // Assemble card
            el.appendChild(imgWrap);
            if (swatchNode) el.appendChild(swatchNode);
            el.appendChild(footer);

            // QV dataset — minimum for instant open; full detail loaded async.
            // dataset.image uses the same resolved URL as the card so the QV
            // panel opens with the correct colour image before async detail loads.
            qvBtn.dataset.qv    = '';
            qvBtn.dataset.id    = String(p.id);
            qvBtn.dataset.name  = p.name;
            qvBtn.dataset.brand = p.brand;
            qvBtn.dataset.price = String(p.pricePerDay);
            qvBtn.dataset.image = initialImageUrl;

            return el;
        }

        // ── Search result row ─────────────────────────────────────────────────
        // Used by: search modal results list.
        // ProductSearchDTO only has core — no swatches or price range needed.
        // Image: _sm — smallest surface, smallest file.
        function searchResult(raw) {
            const p = _normalise(raw);

            const a = document.createElement('a');
            a.className = 'search-result-item';
            a.href      = `${CTX}/products/${encodeURIComponent(p.id)}`;

            const thumb = document.createElement('div');
            thumb.className = 'search-result-thumb';
            const img = document.createElement('img');
            img.src      = imgUrl(p.imageUrl, 'sm');
            img.alt      = p.name || '';
            img.loading  = 'lazy';
            img.decoding = 'async';
            thumb.appendChild(img);

            const info = document.createElement('div');
            info.className = 'search-result-info';

            const brand = document.createElement('p');
            brand.className  = 'search-result-brand';
            brand.textContent = p.brand;

            const name = document.createElement('p');
            name.className  = 'search-result-name';
            name.textContent = p.name;

            const price = document.createElement('p');
            price.className  = 'search-result-price';
            price.textContent = `From £${p.pricePerDay.toFixed(0)}/day`;

            info.appendChild(brand);
            info.appendChild(name);
            info.appendChild(price);
            a.appendChild(thumb);
            a.appendChild(info);
            return a;
        }

        // ── Cart drawer item ──────────────────────────────────────────────────
        // All interactive elements carry data-key (variantKey = "id:size:color")
        // so cart.js can target the exact variant even when multiple variants of
        // the same product are in the cart simultaneously.
        // data-inv on the inc button carries the inventory cap so cart.js can
        // disable it when qty reaches the stock limit.
        function cartItem(raw) {
            const it  = _normalise(raw);
            const key = `${it.id}:${it.size || ''}:${it.color || ''}`;

            const li = document.createElement('li');
            li.className   = 'cart-item';
            li.dataset.key = key;
            li.dataset.id  = it.id;

            const imgWrap = document.createElement('div');
            imgWrap.className = 'cart-item-img';
            const img = document.createElement('img');
            img.src      = imgUrl(it.imageUrl, 'sm');
            img.alt      = it.name || '';
            img.loading  = 'lazy';
            img.decoding = 'async';
            imgWrap.appendChild(img);

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

            // Size · Color line
            const sizePart  = it.size      ? `Size ${it.size}` : '';
            const colorPart = it.colorName
                ? it.colorName
                : it.color
                    ? it.color.replace(/^#[0-9a-fA-F]+-/, '') // strip hex prefix if no display name
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
            // Store inventory cap so cart.js can disable when qty reaches limit
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