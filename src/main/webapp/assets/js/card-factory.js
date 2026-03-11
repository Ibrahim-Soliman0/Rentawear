(function () {
    /* card-factory.js — produces markup matching redesign classes */
    const CardFactory = (function () {
        function _normalise(raw) {
            return {
                id: String(raw.id ?? '') ?? '',
                name: raw.name ?? '',
                brand: raw.brand ?? '',
                pricePerDay: Number(raw.pricePerDay ?? 0),
                imageUrl: raw.imageUrl || `${CTX}/assets/img/placeholder.jpg`,
                isNew: Boolean(raw.isNew),
                isPopular: Boolean(raw.isPopular),
                swatches: Array.isArray(raw.swatches) ? raw.swatches : [],
                rrp: raw.rrp ?? null,
                dates: raw.dates ?? null,
                qty: raw.qty ?? 1
            };
        }

        function _badgeHtml(p) {
            if (p.isNew) return `<span class="product-badge new">New</span>`;
            if (p.isPopular) return `<span class="product-badge">Trending</span>`;
            return '';
        }

        function _swatchRow(p) {
            if (!p.swatches.length) return '';
            const chips = p.swatches.slice(0, 5).map((s, i) => {
                const color = esc(s.color || s || '#ccc');
                const active = i === 0 ? ' active' : '';
                return `<span class="swatch${active}" style="background:${color}"></span>`;
            }).join('');
            return `<div class="swatch-row" style="margin-bottom:10px">${chips}</div>`;
        }

        function _wishlistSvg(active) {
            // simple 15x15 heart used in markup sample
            return `<svg viewBox="0 0 15 15" aria-hidden="true"><path d="M7.5 13S1 9 1 4.5a3.5 3.5 0 0 1 6.5-1.8A3.5 3.5 0 0 1 14 4.5C14 9 7.5 13 7.5 13z" stroke-width="1.8" ${active ? 'fill="currentColor"' : 'fill="none" stroke="currentColor"'}></path></svg>`;
        }

        function skeleton() {
            const el = document.createElement('div');
            el.className = 'product-card product-skel';
            el.setAttribute('aria-hidden', 'true');
            el.innerHTML = `
        <div class="product-img-wrap">
          <div class="product-img-bg pg-3" style="height:100%"></div>
        </div>
        <div class="product-footer">
          <div class="product-info">
            <div class="rw-skel-line rw-skel-line--brand"></div>
            <div class="rw-skel-line rw-skel-line--name"></div>
            <div class="rw-skel-line rw-skel-line--price"></div>
          </div>
        </div>`;
            return el;
        }

        function grid(rawProduct) {
            const p = _normalise(rawProduct);
            const el = document.createElement('div');
            el.className = 'product-card';
            el.innerHTML = `
        <div class="product-img-wrap">
          <div class="product-img-bg pg-3" style="height:100%;background-image:url('${esc(p.imageUrl)}');background-size:cover;background-position:center"
></div>
          ${_badgeHtml(p)}
          <button class="product-wishlist" type="button" aria-label="Add to wishlist">${_wishlistSvg(false)}</button>
          <button class="product-qv" type="button" aria-label="Quick view ${esc(p.name)}">
            <svg viewBox="0 0 14 14" aria-hidden="true"><circle cx="7" cy="7" r="4"></circle><path d="M1 7s2-5 6-5 6 5 6 5-2 5-6 5-6-5-6-5z"></path></svg>
            Quick View
          </button>
        </div>
        ${_swatchRow(p)}
        <div class="product-footer">
          <div class="product-info">
            <p class="product-brand">${esc(p.brand)}</p>
            <p class="product-name">${esc(p.name)}</p>
            <p class="product-price">From <strong>£${p.pricePerDay.toFixed(0)}</strong>/day ${p.rrp ? `<s>RRP £${esc(String(p.rrp))}</s>` : ''}</p>
          </div>
        </div>
      `.trim();

            // attach wishlist toggle behaviour (keeps existing helper if present)
            const btn = el.querySelector('.product-wishlist');
            if (btn) {
                btn.addEventListener('click', e => {
                    e.preventDefault();
                    e.stopPropagation();
                    const active = btn.classList.toggle('active');
                    btn.innerHTML = _wishlistSvg(active);
                    btn.setAttribute('aria-label', `${active ? 'Remove' : 'Add'} ${p.name} ${active ? 'from' : 'to'} wishlist`);
                });
            }

            // quick-view dataset for existing quick-view listener (safe to include)
            const qv = el.querySelector('.product-qv');
            if (qv) {
                qv.dataset.qv = '';
                qv.dataset.id = esc(p.id);
                qv.dataset.name = esc(p.name);
                qv.dataset.brand = esc(p.brand);
                qv.dataset.price = String(p.pricePerDay);
                qv.dataset.image = esc(p.imageUrl);
            }

            return el;
        }

        function searchResult(rawProduct) {
            const p = _normalise(rawProduct);
            const a = document.createElement('a');
            a.className = 'search-result-item';
            a.href = `${CTX || ''}/product?id=${esc(p.id)}`;
            a.innerHTML = `
        <div class="search-result-thumb pg-3" style="background-image:url('${esc(p.imageUrl)}');background-size:cover;background-position:center"></div>
        <div class="search-result-info">
          <p class="search-result-brand">${esc(p.brand)}</p>
          <p class="search-result-name">${esc(p.name)}</p>
          <p class="search-result-price">From £${p.pricePerDay.toFixed(0)}/day ${p.rrp ? `<span>RRP £${esc(String(p.rrp))}</span>` : ''}</p>
        </div>
      `.trim();
            return a;
        }

        function cartItem(raw) {
            const it = _normalise(raw);
            const li = document.createElement('li');
            li.className = 'cart-item';
            li.dataset.id = it.id;
            li.innerHTML = `
        <div class="cart-item-img pg-3" tyle="border-radius:4px;background-image:url('${esc(it.imageUrl)}');background-size:cover;background-position:center" aria-hidden="true"></div>
        <div class="cart-item-info">
          <span class="cart-item-brand">${esc(it.brand)}</span>
          <p class="cart-item-name">${esc(it.name)}</p>
          <p class="cart-item-dates">${esc(it.dates ?? '')}</p>
          <p class="cart-item-price">£${it.pricePerDay.toFixed(0)}/day</p>
        </div>
        <div class="cart-item-qty">
          <button class="cart-remove" data-id="${esc(it.id)}" type="button" aria-label="Remove item">Remove</button>
          <div class="qty-controls">
            <button class="qty-btn" data-action="dec" data-id="${esc(it.id)}" type="button" aria-label="Decrease quantity">−</button>
            <span class="qty-num">${esc(String(it.qty))}</span>
            <button class="qty-btn" data-action="inc" data-id="${esc(it.id)}" type="button" aria-label="Increase quantity">+</button>
          </div>
        </div>
      `.trim();

            return li;
        }

        return { skeleton, grid, searchResult, cartItem };
    })();

    window.CardFactory = CardFactory;
})();