(function () {
    /* card-factory.js — produces markup for all product surfaces.
       Depends on: utils.js (CTX, esc) — must load first.

       Public API:
         CardFactory.skeleton()           → placeholder card while fetching
         CardFactory.grid(product)        → home strips and catalog grid
         CardFactory.searchResult(product)→ search modal result row
         CardFactory.cartItem(item)       → cart drawer item

       imgUrl(base, size) is exposed on window for use by quick-view.js
       and product.js — always use this, never append size suffix manually.
    */

    // ── Image URL helper ──────────────────────────────────────────────
    // base: the path stored in ProductDTO.imageUrl
    //       e.g. /assets/img/products/42/midnight-navy/a3f7c2d1
    //       or   /assets/img/placeholder  (when product has no images)
    // size: "sm" | "md" | "lg"
    function imgUrl(base, size) {
        const b = (base && !base.endsWith('.jpg') && !base.endsWith('.png'))
            ? base
            : '/assets/img/placeholder';
        return `${CTX}${b}_${size}.jpg`;
    }
    // Expose globally so quick-view.js and product.js can use the same rule
    window.imgUrl = imgUrl;

    const CardFactory = (function () {

        // ── Normalise raw DTO from servlet ────────────────────────────
        // Provides safe defaults for every field so rendering code
        // never has to guard against undefined.
        function _normalise(raw) {
            return {
                id:          String(raw.id ?? ''),
                name:        raw.name        ?? '',
                brand:       raw.brand       ?? '',
                pricePerDay: Number(raw.pricePerDay ?? 0),
                // imageUrl is a base path — no size suffix, no extension
                imageUrl:    raw.imageUrl    || `/assets/img/placeholder`,
                isNew:       Boolean(raw.isNew),
                description: raw.description ?? '',
                category:    raw.category    ?? '',
                // swatches: SwatchDTO[] — { color, hex, name, slug }
                swatches:    Array.isArray(raw.swatches) ? raw.swatches : [],
                // sizes: string[] — unique sizes from variants
                sizes:       Array.isArray(raw.sizes)    ? raw.sizes    : [],
                // cart-only fields
                dates:       raw.dates  ?? null,
                qty:         raw.qty    ?? 1,
            };
        }

        // ── Badge ─────────────────────────────────────────────────────
        // Only isNew badge — isPopular and rrp not used in this project.
        function _badgeNode(p) {
            if (!p.isNew) return null;
            const span = document.createElement('span');
            span.className = 'product-badge new';
            span.textContent = 'New';
            return span;
        }

        // ── Product image with srcset ─────────────────────────────────
        // Uses a real <img> so srcset works (background-image has no srcset).
        // object-fit:cover in CSS makes it behave like background-size:cover.
        // loading="lazy" defers off-screen images automatically.
        function _productImgNode(base, name) {
            const div = document.createElement('div');
            div.className = 'product-img-inner';
            const img = document.createElement('img');
            img.src = imgUrl(base, 'md');
            img.srcset = `${imgUrl(base,'sm')} 400w, ${imgUrl(base,'md')} 800w, ${imgUrl(base,'lg')} 1400w`;
            img.sizes = '(max-width:480px) 100vw, (max-width:900px) 50vw, 33vw';
            img.alt = name ? String(name) : '';
            img.loading = 'lazy';
            img.decoding = 'async';
            div.appendChild(img);
            return div;
        }

        // ── Swatch row ────────────────────────────────────────────────
        // s.hex   → CSS background color  (#2C3E50)
        // s.name  → tooltip               (Midnight Navy)
        // s.color → full encoded string   (#2C3E50-Midnight Navy)
        //           stored on dataset so quick-view can read it
        // s.slug  → URL/path slug         (midnight-navy)
        //           stored on dataset so JS can build image paths
        function _swatchRowNode(p) {
            if (!p.swatches || !p.swatches.length) return null;
            const wrap = document.createElement('div');
            wrap.className = 'swatch-row';
            p.swatches.slice(0,5).forEach((s, i) => {
                const span = document.createElement('span');
                span.className = 'swatch' + (i===0 ? ' active' : '');
                span.style.background = esc(s.hex || s || '#ccc');
                span.title = s.name || '';
                if (s.color) span.dataset.color = s.color;
                if (s.slug) span.dataset.slug = s.slug;
                wrap.appendChild(span);
            });
            return wrap;
        }

        // ── Wishlist heart SVG ────────────────────────────────────────
        function _wishlistSvgNode(active) {
            const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
            svg.setAttribute('viewBox','0 0 15 15');
            svg.setAttribute('aria-hidden','true');
            const path = document.createElementNS('http://www.w3.org/2000/svg','path');
            path.setAttribute('d','M7.5 13S1 9 1 4.5a3.5 3.5 0 0 1 6.5-1.8A3.5 3.5 0 0 1 14 4.5C14 9 7.5 13 7.5 13z');
            path.setAttribute('stroke-width','1.8');
            if (active) path.setAttribute('fill','currentColor'); else { path.setAttribute('fill','none'); path.setAttribute('stroke','currentColor'); }
            svg.appendChild(path);
            return svg;
        }

        // ── Skeleton ──────────────────────────────────────────────────
        // Shown while the section fetch is in flight.
        // Matches the same DOM structure as grid() so CSS sizing is identical.
        function skeleton() {
            const el = document.createElement('div');
            el.className = 'product-card product-skel';
            el.setAttribute('aria-hidden','true');

            const imgWrap = document.createElement('div'); imgWrap.className = 'product-img-wrap product-img-wrap--skel';
            const footer = document.createElement('div'); footer.className = 'product-footer';
            const info = document.createElement('div'); info.className = 'product-info';
            const l1 = document.createElement('div'); l1.className = 'rw-skel-line rw-skel-line--brand';
            const l2 = document.createElement('div'); l2.className = 'rw-skel-line rw-skel-line--name';
            const l3 = document.createElement('div'); l3.className = 'rw-skel-line rw-skel-line--price';
            info.appendChild(l1); info.appendChild(l2); info.appendChild(l3); footer.appendChild(info);
            el.appendChild(imgWrap); el.appendChild(footer);
            return el;
        }

        // ── Grid card ─────────────────────────────────────────────────
        // Used by: home page strips, catalog grid.
        // Image: _md at desktop, browser picks smaller via srcset on mobile.
        // QV dataset: all fields quick-view.js needs for Phase 1 open.
        function grid(rawProduct) {
            const p = _normalise(rawProduct);
            const el = document.createElement('div'); el.className = 'product-card';

            const imgWrap = document.createElement('div'); imgWrap.className = 'product-img-wrap';
            imgWrap.appendChild(_productImgNode(p.imageUrl, p.name));

            const badge = _badgeNode(p); if (badge) imgWrap.appendChild(badge);

            const wishBtn = document.createElement('button');
            wishBtn.className = 'product-wishlist'; wishBtn.type = 'button'; wishBtn.setAttribute('aria-label','Add to wishlist');
            wishBtn.appendChild(_wishlistSvgNode(false));

            const qvBtn = document.createElement('button');
            qvBtn.className = 'product-qv'; qvBtn.type = 'button'; qvBtn.setAttribute('aria-label', `Quick view ${p.name}`);
            // small eye icon
            const qvSvg = document.createElementNS('http://www.w3.org/2000/svg','svg'); qvSvg.setAttribute('viewBox','0 0 14 14'); qvSvg.setAttribute('aria-hidden','true');
            const circ = document.createElementNS('http://www.w3.org/2000/svg','circle'); circ.setAttribute('cx','7'); circ.setAttribute('cy','7'); circ.setAttribute('r','4');
            const pth = document.createElementNS('http://www.w3.org/2000/svg','path'); pth.setAttribute('d','M1 7s2-5 6-5 6 5 6 5-2 5-6 5-6-5-6-5z');
            qvSvg.appendChild(circ); qvSvg.appendChild(pth); qvBtn.appendChild(qvSvg); qvBtn.appendChild(document.createTextNode(' Quick View'));

            imgWrap.appendChild(wishBtn); imgWrap.appendChild(qvBtn);

            const swatchNode = _swatchRowNode(p);

            const footer = document.createElement('div'); footer.className = 'product-footer';
            const info = document.createElement('div'); info.className = 'product-info';
            const brandP = document.createElement('p'); brandP.className = 'product-brand'; brandP.textContent = p.brand;
            const nameP = document.createElement('p'); nameP.className = 'product-name'; nameP.textContent = p.name;
            const priceP = document.createElement('p'); priceP.className = 'product-price';
            const strong = document.createElement('strong'); strong.textContent = `£${p.pricePerDay.toFixed(0)}`;
            priceP.appendChild(document.createTextNode('From ')); priceP.appendChild(strong); priceP.appendChild(document.createTextNode('/day'));

            info.appendChild(brandP); info.appendChild(nameP); info.appendChild(priceP); footer.appendChild(info);

            el.appendChild(imgWrap); if (swatchNode) el.appendChild(swatchNode); el.appendChild(footer);

            // quick-view dataset
            if (qvBtn) {
                qvBtn.dataset.qv = '';
                qvBtn.dataset.id = String(p.id);
                qvBtn.dataset.name = p.name;
                qvBtn.dataset.brand = p.brand;
                qvBtn.dataset.price = String(p.pricePerDay);
                qvBtn.dataset.image = p.imageUrl;
                qvBtn.dataset.desc = p.description || '';
                qvBtn.dataset.sizes = (p.sizes || []).join(',');
            }

            // wishlist toggle
            wishBtn.addEventListener('click', (e) => {
                e.preventDefault(); e.stopPropagation();
                const active = wishBtn.classList.toggle('active');
                while (wishBtn.firstChild) wishBtn.removeChild(wishBtn.firstChild);
                wishBtn.appendChild(_wishlistSvgNode(active));
                wishBtn.setAttribute('aria-label', `${active ? 'Remove' : 'Add'} ${p.name} ${active ? 'from' : 'to'} wishlist`);
            });

            return el;
        }

        // ── Search result row ─────────────────────────────────────────
        // Used by: search modal results list.
        // Image: _sm — smallest surface, smallest file.
        // Entire row is a link to the PDP.
        function searchResult(raw) {
            const p = _normalise(raw);
            const a = document.createElement('a'); a.className = 'search-result-item'; a.href = `${CTX}/product?id=${encodeURIComponent(p.id)}`;
            const thumb = document.createElement('div'); thumb.className = 'search-result-thumb';
            const img = document.createElement('img'); img.src = imgUrl(p.imageUrl,'sm'); img.alt = p.name || ''; img.loading = 'lazy'; img.decoding = 'async'; thumb.appendChild(img);
            const info = document.createElement('div'); info.className = 'search-result-info';
            const brand = document.createElement('p'); brand.className = 'search-result-brand'; brand.textContent = p.brand;
            const name = document.createElement('p'); name.className = 'search-result-name'; name.textContent = p.name;
            const price = document.createElement('p'); price.className = 'search-result-price'; price.textContent = `From £${p.pricePerDay.toFixed(0)}/day`;
            info.appendChild(brand); info.appendChild(name); info.appendChild(price);
            a.appendChild(thumb); a.appendChild(info);
            return a;
        }

        // ── Cart drawer item ──────────────────────────────────────────
        // Used by: cart drawer (cart.js calls this for every item in state).
        // Image: _sm — smallest file for smallest surface.
        // Qty controls and remove button wired by cart.js via event delegation.
        function cartItem(raw) {
            const it = _normalise(raw);
            const li = document.createElement('li'); li.className = 'cart-item'; li.dataset.id = String(it.id);
            const imgWrap = document.createElement('div'); imgWrap.className = 'cart-item-img';
            const img = document.createElement('img'); img.src = imgUrl(it.imageUrl,'sm'); img.alt = it.name || ''; img.loading = 'lazy'; img.decoding = 'async'; imgWrap.appendChild(img);
            const info = document.createElement('div'); info.className = 'cart-item-info';
            const brand = document.createElement('span'); brand.className = 'cart-item-brand'; brand.textContent = it.brand;
            const name = document.createElement('p'); name.className = 'cart-item-name'; name.textContent = it.name;
            const dates = document.createElement('p'); dates.className = 'cart-item-dates'; dates.textContent = it.dates || '';
            const price = document.createElement('p'); price.className = 'cart-item-price'; price.textContent = `£${it.pricePerDay.toFixed(0)}/day`;
            info.appendChild(brand); info.appendChild(name); info.appendChild(dates); info.appendChild(price);

            const actions = document.createElement('div'); actions.className = 'cart-item-qty';
            const removeBtn = document.createElement('button'); removeBtn.className = 'cart-remove'; removeBtn.dataset.id = String(it.id); removeBtn.type = 'button'; removeBtn.setAttribute('aria-label', `Remove ${it.name} from bag`); removeBtn.textContent = 'Remove';
            const qtyControls = document.createElement('div'); qtyControls.className = 'qty-controls';
            const dec = document.createElement('button'); dec.className = 'qty-btn'; dec.dataset.action = 'dec'; dec.dataset.id = String(it.id); dec.type = 'button'; dec.setAttribute('aria-label','Decrease quantity'); dec.textContent = '−';
            const num = document.createElement('span'); num.className = 'qty-num'; num.textContent = String(it.qty);
            const inc = document.createElement('button'); inc.className = 'qty-btn'; inc.dataset.action = 'inc'; inc.dataset.id = String(it.id); inc.type = 'button'; inc.setAttribute('aria-label','Increase quantity'); inc.textContent = '+';
            qtyControls.appendChild(dec); qtyControls.appendChild(num); qtyControls.appendChild(inc);
            actions.appendChild(removeBtn); actions.appendChild(qtyControls);

            li.appendChild(imgWrap); li.appendChild(info); li.appendChild(actions);
            return li;
        }

        return { skeleton, grid, searchResult, cartItem };
    })();

    window.CardFactory = CardFactory;
})();

