/* ============================================================
   card-factory.js — Product card rendering factory
   Depends on : utils.js  (CTX, esc — must load first)
   Loaded on  : every page that renders product cards
                (home, category, search results)
   Exports    : window.CardFactory

   CardFactory.grid(product)       → HTMLElement  full grid card
   CardFactory.skeleton()          → HTMLElement  shimmer placeholder
   CardFactory.searchResult(p)     → HTMLElement  compact modal row

   product shape (list endpoint):
   {
     id             : string | number,
     name           : string,
     brand          : string,
     pricePerDay    : number,
     imageUrl       : string,         // absolute path or full URL
     isNew          : boolean,
     isPopular      : boolean,
     sizes          : string[],       // all declared sizes e.g. ["XS","S","M","L"]
     availableSizes : string[]        // in-stock subset
   }
   ============================================================ */

const CardFactory = (function () {

    /* ──────────────────────────────────────────────────────────
       PRIVATE HELPERS
       ────────────────────────────────────────────────────────── */

    /**
     * Normalise a raw server product object.
     * Fills every field with a safe default so the public methods
     * never need to guard against missing or malformed data.
     */
    function _normalise(raw) {
        return {
            id             : raw.id            ?? '',
            name           : raw.name          ?? '',
            brand          : raw.brand         ?? '',
            pricePerDay    : Number(raw.pricePerDay ?? 0),
            imageUrl       : raw.imageUrl      || `${CTX}/assets/img/placeholder.jpg`,
            isNew          : Boolean(raw.isNew),
            isPopular      : Boolean(raw.isPopular),
            sizes          : Array.isArray(raw.sizes)          ? raw.sizes          : [],
            availableSizes : Array.isArray(raw.availableSizes) ? raw.availableSizes : [],
        };
    }

    /**
     * Build the badge HTML for a product.
     * Popular is rendered first — the CSS sibling rule
     * (.badge-popular + .badge-new) offsets the NEW badge below it
     * when both are present.
     */
    function _badges(p) {
        const popular = p.isPopular ? `<span class="badge-popular">Popular</span>` : '';
        const isNew   = p.isNew    ? `<span class="badge-new">New</span>`          : '';
        return popular + isNew;
    }

    /**
     * Build the size chip row HTML.
     * - No sizes declared  → returns empty string (chips row omitted)
     * - Sizes declared but nothing in stock → "Unavailable" chip
     * - Normal case        → up to 5 chips, out-of-stock ones struck through
     */
    function _sizeChips(p) {
        if (!p.sizes.length) return '';

        if (!p.availableSizes.length) {
            return `<div class="rw-size-chips">
                <span class="rw-size-chip unavailable">Unavailable</span>
              </div>`;
        }

        const chips = p.sizes.slice(0, 5).map(s => {
            const mod = p.availableSizes.includes(s) ? '' : ' unavailable';
            return `<span class="rw-size-chip${mod}">${esc(s)}</span>`;
        }).join('');

        return `<div class="rw-size-chips">${chips}</div>`;
    }

    /*
     * COLOR CHIPS — ready for when color selection lands on the card.
     * Uncomment this function and its call-site in grid() together,
     * then uncomment the matching CSS block in styles.css §21.
     *
     * function _colorChips(p) {
     *   const colors = Array.isArray(p.colors) ? p.colors : [];
     *   if (!colors.length) return '';
     *   const MAX  = 4;
     *   const dots = colors.slice(0, MAX).map(c =>
     *     `<span class="rw-color-dot"
     *             style="background:${esc(c.hex)}"
     *             title="${esc(c.name)}"></span>`).join('');
     *   const more = colors.length > MAX
     *     ? `<span class="rw-color-more">+${colors.length - MAX}</span>` : '';
     *   return `<div class="rw-color-chips">${dots}${more}</div>`;
     * }
     */

    /**
     * Attach the wishlist heart toggle to a rendered card element.
     * Client-side only for now — server persistence added in Slice 5.
     */
    function _attachWishlist(cardEl, product) {
        const btn = cardEl.querySelector('.wishlist-btn');
        if (!btn) return;

        btn.addEventListener('click', e => {
            e.preventDefault();
            e.stopPropagation();
            const active = btn.classList.toggle('active');
            btn.querySelector('i').className = active ? 'bi bi-heart-fill' : 'bi bi-heart';
            btn.setAttribute('aria-label',
                `${active ? 'Remove' : 'Add'} ${product.name} ${active ? 'from' : 'to'} wishlist`);
        });
    }


    /* ──────────────────────────────────────────────────────────
       PUBLIC METHODS
       ────────────────────────────────────────────────────────── */

    /**
     * skeleton()
     * Shimmer loading placeholder. Matches the grid card's 3/4 image
     * aspect ratio and three-line body so the layout doesn't shift when
     * real cards replace it.
     */
    function skeleton() {
        const el = document.createElement('div');
        el.className = 'rw-card rw-card-skeleton';
        el.setAttribute('aria-hidden', 'true');
        el.innerHTML = `
      <div class="card-img-wrap"></div>
      <div class="card-body">
        <span class="rw-skel-line rw-skel-line--brand"></span>
        <span class="rw-skel-line rw-skel-line--name"></span>
        <span class="rw-skel-line rw-skel-line--price"></span>
      </div>`;
        return el;
    }


    /**
     * grid(rawProduct)
     * Full product card for home grids, category pages, and the search
     * results page. Includes hero image, badges, wishlist button,
     * quick-view button, price, and size chips.
     */
    function grid(rawProduct) {
        const p         = _normalise(rawProduct);
        const id        = esc(String(p.id));
        const available = p.availableSizes.length > 0;

        const card = document.createElement('div');
        card.className         = `rw-card${available ? '' : ' rw-card--unavailable'}`;
        card.dataset.productId = String(p.id);

        card.innerHTML = `
      <a href="${CTX}/product?id=${id}"
         class="card-img-wrap"
         tabindex="-1"
         aria-hidden="true">
        <img src="${esc(p.imageUrl)}"
             alt="${esc(p.name)}"
             loading="lazy"
             width="400" height="533"/>
        ${_badges(p)}
        <button class="wishlist-btn rw-icon-btn"
                data-product-id="${id}"
                aria-label="Add ${esc(p.name)} to wishlist"
                type="button">
          <i class="bi bi-heart" aria-hidden="true"></i>
        </button>
        <button class="rw-card-qv-btn"
                data-qv-id="${id}"
                aria-label="Quick view ${esc(p.name)}"
                type="button">
          <i class="bi bi-eye" aria-hidden="true"></i> Quick View
        </button>
      </a>
      <div class="card-body">
        <div class="brand-name">${esc(p.brand)}</div>
        <a href="${CTX}/product?id=${id}"
           class="product-name">${esc(p.name)}</a>
        <div class="price">
          $${p.pricePerDay.toFixed(2)}<span> / day</span>
        </div>
        ${_sizeChips(p)}
      </div>`;

        _attachWishlist(card, p);
        return card;
    }


    /**
     * searchResult(rawProduct)
     * Compact horizontal row for the search modal results list.
     * Same normalised product shape as grid() — no logic duplication.
     * Omits wishlist, quick-view, and size chips by design.
     */
    function searchResult(rawProduct) {
        const p  = _normalise(rawProduct);
        const id = esc(String(p.id));

        const el = document.createElement('a');
        el.className = 'rw-result-item';
        el.href      = `${CTX}/product?id=${id}`;
        el.innerHTML = `
      <img src="${esc(p.imageUrl)}"
           alt="${esc(p.name)}"
           loading="lazy"/>
      <div class="rw-result-info">
        <div class="rw-result-name">${esc(p.name)}</div>
        <div class="rw-result-brand">${esc(p.brand)}</div>
        <div class="rw-result-price">
          $${p.pricePerDay.toFixed(2)}<span> / day</span>
        </div>
      </div>`;
        return el;
    }


    return { skeleton, grid, searchResult };

})();


/* ── Export ─────────────────────────────────────────────────── */
window.CardFactory = CardFactory;