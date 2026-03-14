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
        function _badgeHtml(p) {
            if (p.isNew) return `<span class="product-badge new">New</span>`;
            return '';
        }

        // ── Product image with srcset ─────────────────────────────────
        // Uses a real <img> so srcset works (background-image has no srcset).
        // object-fit:cover in CSS makes it behave like background-size:cover.
        // loading="lazy" defers off-screen images automatically.
        function _productImg(base, name) {
            return `
          <img src="${imgUrl(base, 'md')}"
               srcset="${imgUrl(base, 'sm')} 400w,
                       ${imgUrl(base, 'md')} 800w,
                       ${imgUrl(base, 'lg')} 1400w"
               sizes="(max-width:480px) 100vw,
                      (max-width:900px) 50vw,
                      33vw"
               alt="${esc(name)}"
               loading="lazy"
               decoding="async">`.trim();
        }

        // ── Swatch row ────────────────────────────────────────────────
        // s.hex   → CSS background color  (#2C3E50)
        // s.name  → tooltip               (Midnight Navy)
        // s.color → full encoded string   (#2C3E50-Midnight Navy)
        //           stored on dataset so quick-view can read it
        // s.slug  → URL/path slug         (midnight-navy)
        //           stored on dataset so JS can build image paths
        function _swatchRow(p) {
            if (!p.swatches.length) return '';
            const chips = p.swatches.slice(0, 5).map((s, i) => {
                const active = i === 0 ? ' active' : '';
                return `<span class="swatch${active}"
                              style="background:${esc(s.hex || '#ccc')}"
                              title="${esc(s.name || '')}"
                              data-color="${esc(s.color || '')}"
                              data-slug="${esc(s.slug || '')}"></span>`;
            }).join('');
            return `<div class="swatch-row">${chips}</div>`;
        }

        // ── Wishlist heart SVG ────────────────────────────────────────
        function _wishlistSvg(active) {
            return `<svg viewBox="0 0 15 15" aria-hidden="true">
              <path d="M7.5 13S1 9 1 4.5a3.5 3.5 0 0 1 6.5-1.8A3.5 3.5 0 0 1 14 4.5C14 9 7.5 13 7.5 13z"
                    stroke-width="1.8"
                    ${active ? 'fill="currentColor"' : 'fill="none" stroke="currentColor"'}>
              </path>
            </svg>`;
        }

        // ── Skeleton ──────────────────────────────────────────────────
        // Shown while the section fetch is in flight.
        // Matches the same DOM structure as grid() so CSS sizing is identical.
        function skeleton() {
            const el = document.createElement('div');
            el.className = 'product-card product-skel';
            el.setAttribute('aria-hidden', 'true');
            el.innerHTML = `
        <div class="product-img-wrap product-img-wrap--skel"></div>
        <div class="product-footer">
          <div class="product-info">
            <div class="rw-skel-line rw-skel-line--brand"></div>
            <div class="rw-skel-line rw-skel-line--name"></div>
            <div class="rw-skel-line rw-skel-line--price"></div>
          </div>
        </div>`.trim();
            return el;
        }

        // ── Grid card ─────────────────────────────────────────────────
        // Used by: home page strips, catalog grid.
        // Image: _md at desktop, browser picks smaller via srcset on mobile.
        // QV dataset: all fields quick-view.js needs for Phase 1 open.
        function grid(rawProduct) {
            const p  = _normalise(rawProduct);
            const el = document.createElement('div');
            el.className = 'product-card';

            el.innerHTML = `
        <div class="product-img-wrap">
          ${_productImg(p.imageUrl, p.name)}
          ${_badgeHtml(p)}
          <button class="product-wishlist" type="button" aria-label="Add to wishlist">
            ${_wishlistSvg(false)}
          </button>
          <button class="product-qv" type="button" aria-label="Quick view ${esc(p.name)}">
            <svg viewBox="0 0 14 14" aria-hidden="true">
              <circle cx="7" cy="7" r="4"></circle>
              <path d="M1 7s2-5 6-5 6 5 6 5-2 5-6 5-6-5-6-5z"></path>
            </svg>
            Quick View
          </button>
        </div>
        ${_swatchRow(p)}
        <div class="product-footer">
          <div class="product-info">
            <p class="product-brand">${esc(p.brand)}</p>
            <p class="product-name">${esc(p.name)}</p>
            <p class="product-price">From <strong>£${p.pricePerDay.toFixed(0)}/day</strong></p>
          </div>
        </div>`.trim();

            // Wishlist toggle — local only (no server sync needed for guests)
            // const wishBtn = el.querySelector('.product-wishlist');
            // if (wishBtn) {
            //     wishBtn.addEventListener('click', e => {
            //         e.preventDefault();
            //         e.stopPropagation();
            //         const active = wishBtn.classList.toggle('active');
            //         // wishBtn.innerHTML = _wishlistSvg(active);
            //         wishBtn.setAttribute('aria-label',
            //             `${active ? 'Remove' : 'Add'} ${p.name} ${active ? 'from' : 'to'} wishlist`);
            //     });
            // }

            // QV dataset — quick-view.js reads all of these in its click handler.
            // Phase 1 (immediate): name, brand, price, image, desc, sizes
            // Phase 2 (async):     id is used to fetch action=images
            const qvBtn = el.querySelector('.product-qv');
            if (qvBtn) {
                qvBtn.dataset.qv    = '';
                qvBtn.dataset.id    = p.id;
                qvBtn.dataset.name  = p.name;
                qvBtn.dataset.brand = p.brand;
                qvBtn.dataset.price = String(p.pricePerDay);
                qvBtn.dataset.image = p.imageUrl;
                qvBtn.dataset.desc  = p.description;
                qvBtn.dataset.sizes = p.sizes.join(',');
            }

            return el;
        }

        // ── Search result row ─────────────────────────────────────────
        // Used by: search modal results list.
        // Image: _sm — smallest surface, smallest file.
        // Entire row is a link to the PDP.
        function searchResult(rawProduct) {
            const p = _normalise(rawProduct);
            const a = document.createElement('a');
            a.className = 'search-result-item';
            a.href      = `${CTX}/product?id=${esc(p.id)}`;
            a.innerHTML = `
        <div class="search-result-thumb">
          <img src="${imgUrl(p.imageUrl, 'sm')}"
               alt="${esc(p.name)}"
               loading="lazy"
               decoding="async">
        </div>
        <div class="search-result-info">
          <p class="search-result-brand">${esc(p.brand)}</p>
          <p class="search-result-name">${esc(p.name)}</p>
          <p class="search-result-price">From <span>£${p.pricePerDay.toFixed(0)}/day</span></p>
        </div>`.trim();
            return a;
        }

        // ── Cart drawer item ──────────────────────────────────────────
        // Used by: cart drawer (cart.js calls this for every item in state).
        // Image: _sm — smallest file for smallest surface.
        // Qty controls and remove button wired by cart.js via event delegation.
        function cartItem(raw) {
            const it = _normalise(raw);
            const li = document.createElement('li');
            li.className  = 'cart-item';
            li.dataset.id = it.id;
            li.innerHTML  = `
        <div class="cart-item-img" aria-hidden="true">
          <img src="${imgUrl(it.imageUrl, 'sm')}"
               alt="${esc(it.name)}"
               loading="lazy"
               decoding="async">
        </div>
        <div class="cart-item-info">
          <span class="cart-item-brand">${esc(it.brand)}</span>
          <p class="cart-item-name">${esc(it.name)}</p>
          <p class="cart-item-dates">${esc(it.dates ?? '')}</p>
          <p class="cart-item-price">£${it.pricePerDay.toFixed(0)}/day</p>
        </div>
        <div class="cart-item-qty">
          <button class="cart-remove"
                  data-id="${esc(it.id)}"
                  type="button"
                  aria-label="Remove ${esc(it.name)} from bag">Remove</button>
          <div class="qty-controls">
            <button class="qty-btn"
                    data-action="dec"
                    data-id="${esc(it.id)}"
                    type="button"
                    aria-label="Decrease quantity">−</button>
            <span class="qty-num">${esc(String(it.qty))}</span>
            <button class="qty-btn"
                    data-action="inc"
                    data-id="${esc(it.id)}"
                    type="button"
                    aria-label="Increase quantity">+</button>
          </div>
        </div>`.trim();
            return li;
        }

        return { skeleton, grid, searchResult, cartItem };
    })();

    window.CardFactory = CardFactory;
})();