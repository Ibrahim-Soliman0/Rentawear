/* ============================================================
   catalog.js — Product Catalog Page
   Depends on: utils.js (CTX, esc, fetchJson)
               card-factory.js (CardFactory)
   Loaded on:  catalog.jsp only (via extraJS param)
   ============================================================ */

'use strict';

(function () {

    /* ── Constants ───────────────────────────────────────────── */
    const PAGE_SIZE      = 12;
    const PRICE_DEBOUNCE = 500;

    /* ── State ────────────────────────────────────────────────── */
    let state = {
        q:           null,   // search query | null (browse mode when null)
        gender:      null,
        categoryIds: [],
        interestIds: [],
        newOnly:     false,
        minPrice:    null,
        maxPrice:    null,
        page:        0,
        sort:        'newest',
    };

    let urlGender      = null;
    let urlCategoryIds  = [];  // locked category IDs from the original URL
    let urlNewOnly      = false;
    let urlInterestIds  = [];  // locked interest IDs from the original URL

    let priceBounds    = { min: 0, max: 500 };
    let fetchController = null;
    let priceTimer      = null;

    const catNames = {};

    /* ── DOM refs ─────────────────────────────────────────────── */
    const grid          = document.getElementById('catalogGrid');
    const emptyState    = document.getElementById('catalogEmpty');
    const pagination    = document.getElementById('catalogPagination');
    const pgPrev        = document.getElementById('pgPrev');
    const pgNext        = document.getElementById('pgNext');
    const pgNums        = document.getElementById('pgNums');
    const resultCount   = document.getElementById('catalogResultCount');
    const chips         = document.getElementById('catalogChips');
    const filterBadge   = document.getElementById('catalogFilterBadge');
    const filterToggle  = document.getElementById('catalogFilterToggle');
    const sidebar       = document.getElementById('catalogSidebar');
    const backdrop      = document.getElementById('catalogBackdrop');
    const sidebarClose  = document.getElementById('catalogSidebarClose');
    const clearAllBtn   = document.getElementById('catalogClearAll');
    const emptyClear    = document.getElementById('catalogEmptyClear');
    const sortSel       = document.getElementById('catalogSort');
    const titleEl       = document.getElementById('catalogTitle');
    const eyebrowEl     = document.getElementById('catalogEyebrow');
    const bcMid         = document.getElementById('catalogBreadcrumbMid');
    const bcCurrent     = document.getElementById('catalogBreadcrumbCurrent');
    const genderFgroup  = document.getElementById('fgroup-gender');
    const catFgroup     = document.getElementById('fgroup-cats');
    /* Search query display (catalog.jsp search mode markup) */
    const searchQueryEl = document.getElementById('catalogSearchQuery');
    const clearSearchEl = document.getElementById('catalogClearSearch');

    const priceMinInput = document.getElementById('priceRangeMin');
    const priceMaxInput = document.getElementById('priceRangeMax');
    const priceMinDisp  = document.getElementById('priceMinDisp');
    const priceMaxDisp  = document.getElementById('priceMaxDisp');
    const priceFill     = document.getElementById('priceRangeFill');

    /* ── Init ─────────────────────────────────────────────────── */
    document.addEventListener('DOMContentLoaded', function () {

        document.querySelectorAll('.cat-chk').forEach(function (chk) {
            const id = parseInt(chk.value, 10);
            if (!isNaN(id)) catNames[id] = chk.dataset.name || String(id);
        });

        const initParams = new URLSearchParams(window.location.search);
        urlGender       = initParams.get('gender') || null;
        urlCategoryIds  = initParams.getAll('categoryIds')
            .map(v => parseInt(v, 10)).filter(v => !isNaN(v));
        urlNewOnly      = initParams.get('newOnly') === 'true';
        urlInterestIds  = initParams.getAll('interestIds')
            .map(v => parseInt(v, 10)).filter(v => !isNaN(v));

        readUrlIntoState();
        applyFilterGroupVisibility();
        syncSidebarToState();
        initPriceSlider();
        updateHeading();
        updateSearchQueryDisplay();
        fetchProducts();
    });

    window.addEventListener('popstate', function () {
        const p        = new URLSearchParams(window.location.search);
        urlGender      = p.get('gender') || null;
        urlCategoryIds = p.getAll('categoryIds')
            .map(v => parseInt(v, 10)).filter(v => !isNaN(v));
        urlNewOnly     = p.get('newOnly') === 'true';
        urlInterestIds = p.getAll('interestIds')
            .map(v => parseInt(v, 10)).filter(v => !isNaN(v));
        readUrlIntoState();
        applyFilterGroupVisibility();
        syncSidebarToState();
        updateHeading();
        updateSearchQueryDisplay();
        fetchProducts();
    });

    /* ── Filter group visibility ──────────────────────────────── */

    function applyFilterGroupVisibility() {
        const onInterests = state.interestIds.length > 0;
        const onSingleCat = urlCategoryIds.length > 0;

        /* Gender fgroup: hide when gender is locked from URL or on interests page */
        if (genderFgroup) {
            genderFgroup.classList.toggle('is-hidden', !!urlGender || onInterests);
        }

        /* New Arrivals checkbox: hide when it is the page context, not a filter */
        const newOnlyFgroup = document.getElementById('fgroup-new');
        if (newOnlyFgroup) {
            newOnlyFgroup.classList.toggle('is-hidden', urlNewOnly);
        }

        /* Categories fgroup */
        if (onSingleCat) {
            /* Locked to a specific category from nav — hide the fgroup */
            if (catFgroup) catFgroup.classList.add('is-hidden');
        } else if (onInterests) {
            /* Interests page — show only interest categories */
            if (catFgroup) catFgroup.classList.remove('is-hidden');
            document.querySelectorAll('.catalog-chk-label[data-cat-gender]').forEach(label => {
                const chk = label.querySelector('.cat-chk');
                if (!chk) return;
                const id          = parseInt(chk.value, 10);
                const inInterests = state.interestIds.includes(id);
                label.classList.toggle('is-hidden', !inInterests);
                if (!inInterests && chk.checked) {
                    chk.checked = false;
                    state.categoryIds = state.categoryIds.filter(c => c !== id);
                }
            });
        } else {
            /* Normal browsing or search mode — show all, filter by gender */
            if (catFgroup) catFgroup.classList.remove('is-hidden');
            filterCategoriesByGender();
        }
    }

    /* ── URL ↔ State ──────────────────────────────────────────── */

    function readUrlIntoState() {
        const p = new URLSearchParams(window.location.search);

        state.q       = p.get('q')       || null;
        state.gender  = p.get('gender')  || null;
        state.newOnly = p.get('newOnly') === 'true';
        state.page    = Math.max(0, parseInt(p.get('page') || '0', 10));
        state.sort    = p.get('sort')    || 'newest';

        const minP = p.get('minPrice');
        const maxP = p.get('maxPrice');
        state.minPrice = minP ? parseFloat(minP) : null;
        state.maxPrice = maxP ? parseFloat(maxP) : null;

        state.categoryIds = p.getAll('categoryIds')
            .map(v => parseInt(v, 10)).filter(v => !isNaN(v));

        state.interestIds = p.getAll('interestIds')
            .map(v => parseInt(v, 10)).filter(v => !isNaN(v));
    }

    function buildPageUrl() {
        const p = new URLSearchParams();
        if (state.q)             p.set('q',       state.q);
        if (state.gender)        p.set('gender',  state.gender);
        if (state.newOnly)       p.set('newOnly', 'true');
        state.categoryIds.forEach(id => p.append('categoryIds', id));
        state.interestIds.forEach(id => p.append('interestIds', id));
        if (state.minPrice != null)  p.set('minPrice', state.minPrice);
        if (state.maxPrice != null)  p.set('maxPrice', state.maxPrice);
        if (state.page > 0)          p.set('page',     state.page);
        if (state.sort !== 'newest') p.set('sort',     state.sort);
        const qs = p.toString();
        return window.location.pathname + (qs ? '?' + qs : '');
    }

    function buildApiUrl() {
        const p = new URLSearchParams();
        if (state.q)             p.set('q',       state.q);
        if (state.gender)        p.set('gender',  state.gender);
        if (state.newOnly)       p.set('newOnly', 'true');
        state.categoryIds.forEach(id => p.append('categoryIds', id));
        state.interestIds.forEach(id => p.append('interestIds', id));
        if (state.minPrice != null)  p.set('minPrice', state.minPrice);
        if (state.maxPrice != null)  p.set('maxPrice', state.maxPrice);
        p.set('page',     state.page);
        p.set('pageSize', PAGE_SIZE);
        return CTX + '/products?' + p.toString();
    }

    function pushUrl() {
        const url = buildPageUrl();
        if (url !== window.location.pathname + window.location.search) {
            window.history.pushState(null, '', url);
        }
    }

    /* ── Search query display elements ───────────────────────── */

    function updateSearchQueryDisplay() {
        if (!searchQueryEl) return;
        if (state.q) {
            searchQueryEl.textContent = `"${state.q}"`;
            searchQueryEl.style.display = 'inline';
            if (clearSearchEl) clearSearchEl.style.display = 'inline-flex';
        } else {
            searchQueryEl.style.display = 'none';
            if (clearSearchEl) clearSearchEl.style.display = 'none';
        }
    }

    /* Clear search → navigate to all products page, no query */
    if (clearSearchEl) {
        clearSearchEl.addEventListener('click', () => {
            window.location.href = CTX + '/catalog';
        });
    }

    /* ── Dynamic heading & breadcrumb ────────────────────────── */

    function updateHeading() {
        const title   = resolveTitle();
        const eyebrow = resolveEyebrow();
        if (titleEl)   titleEl.textContent   = title;
        if (eyebrowEl) eyebrowEl.textContent = eyebrow;
        document.title = title + ' – Rentawear';
        updateBreadcrumb();
    }

    /* Update eyebrow with result count once fetch completes */
    function updateEyebrowWithCount(total) {
        if (!state.q || !eyebrowEl) return;
        eyebrowEl.textContent =
            `${total} result${total !== 1 ? 's' : ''} for \u201c${state.q}\u201d`;
    }

    function resolveTitle() {
        /* Search mode — q present */
        if (state.q) return 'Search Results';

        /* Primary context wins — category is a refinement, not an override.
           newOnly is now checked before categoryIds so "New Arrivals" is never
           replaced by the selected category name; the category appears in the
           breadcrumb as a sub-segment instead. */
        if (state.interestIds.length) return 'Based on Your Interests';
        if (state.newOnly)            return 'New Arrivals';

        if (state.categoryIds.length === 1 && catNames[state.categoryIds[0]]) {
            return catNames[state.categoryIds[0]];
        }

        if (state.gender === 'FEMALE') return "Women's Collection";
        if (state.gender === 'MALE')   return "Men's Collection";
        return 'All Products';
    }

    function resolveEyebrow() {
        /* Set as placeholder — updateEyebrowWithCount() replaces it after fetch */
        if (state.q)                   return `Searching for \u201c${state.q}\u201d\u2026`;
        if (state.interestIds.length)  return 'Picked just for you';
        if (state.newOnly)             return 'Fresh in this week';
        if (state.gender === 'FEMALE') return 'Tailored for her';
        if (state.gender === 'MALE')   return 'Tailored for him';
        return 'Browse the collection';
    }

    function updateBreadcrumb() {
        if (!bcMid || !bcCurrent) return;
        bcMid.replaceChildren();

        // Resolve the single active category name, if exactly one is selected.
        // Multiple selections don't warrant a category crumb — it would be noisy.
        const activeCatId   = state.categoryIds.length === 1 ? state.categoryIds[0] : null;
        const activeCatName = activeCatId != null && catNames[activeCatId]
            ? catNames[activeCatId] : null;

        // ── Search: flat, no mid segment ─────────────────────────────────────
        if (state.q) {
            bcCurrent.textContent = 'Search Results';
            return;
        }

        // ── Compound: New Arrivals + single category ──────────────────────────
        // Home › New Arrivals › [Category]
        if (state.newOnly && activeCatName) {
            appendBcLink('New Arrivals', buildNewArrivalsBaseUrl());
            bcCurrent.textContent = activeCatName;
            return;
        }

        // ── Compound: Interests + single category ─────────────────────────────
        // Home › For You › [Category]
        if (state.interestIds.length && activeCatName) {
            appendBcLink('For You', buildInterestsBaseUrl());
            bcCurrent.textContent = activeCatName;
            return;
        }

        // ── Compound: gender + single category (no newOnly) ───────────────────
        // Home › Women's/Men's › [Category]
        if (state.gender && activeCatName && !state.newOnly) {
            appendBcLink(
                state.gender === 'FEMALE' ? "Women's" : "Men's",
                CTX + '/catalog?gender=' + state.gender
            );
            bcCurrent.textContent = activeCatName;
            return;
        }

        // ── Compound: gender + newOnly (no category) ──────────────────────────
        // Home › Women's/Men's › New Arrivals
        if (state.gender && state.newOnly) {
            appendBcLink(
                state.gender === 'FEMALE' ? "Women's" : "Men's",
                CTX + '/catalog?gender=' + state.gender
            );
            bcCurrent.textContent = 'New Arrivals';
            return;
        }

        // ── Simple: no gender, no special mode, single category ───────────────
        // Home › All Products › [Category]
        if (!state.gender && !state.interestIds.length && activeCatName && !state.newOnly) {
            appendBcLink('All Products', CTX + '/catalog');
            bcCurrent.textContent = activeCatName;
            return;
        }

        // ── Simple: no gender, newOnly only ───────────────────────────────────
        // Home › All Products › New Arrivals
        if (!state.gender && !state.interestIds.length && state.newOnly && !activeCatName) {
            appendBcLink('All Products', CTX + '/catalog');
            bcCurrent.textContent = 'New Arrivals';
            return;
        }

        // ── Fallback: interests / gender alone, all-products, etc ─────────────
        bcCurrent.textContent = resolveTitle();
    }

    // Appends a linked segment + separator to bcMid.
    function appendBcLink(label, href) {
        const a       = document.createElement('a');
        a.href        = href;
        a.textContent = label;
        const sep     = document.createElement('span');
        sep.className   = 'catalog-breadcrumb-sep';
        sep.textContent = '›';
        bcMid.appendChild(a);
        bcMid.appendChild(sep);
    }

    // Builds the New Arrivals URL without the active category, so the mid
    // breadcrumb link takes the user back to unfiltered new arrivals.
    function buildNewArrivalsBaseUrl() {
        const p = new URLSearchParams();
        p.set('newOnly', 'true');
        if (state.gender) p.set('gender', state.gender);
        return CTX + '/catalog?' + p.toString();
    }

    // Builds the interests base URL without the active category.
    function buildInterestsBaseUrl() {
        const p = new URLSearchParams();
        state.interestIds.forEach(id => p.append('interestIds', id));
        return CTX + '/catalog?' + p.toString();
    }

    /* ── Fetch & Render ───────────────────────────────────────── */

    function fetchProducts() {
        if (fetchController) { try { fetchController.abort(); } catch (e) {} }
        fetchController = new AbortController();

        renderSkeletons();

        const url    = buildApiUrl();
        const signal = fetchController.signal;

        fetch(url, { signal })
            .then(function (resp) {
                if (resp.status === 401) {
                    window.location.href = CTX + '/home';
                    return null;
                }
                if (!resp.ok) throw new Error('HTTP ' + resp.status);
                return resp.json();
            })
            .then(function (data) {
                if (!data) return;   // redirect already in flight
                fetchController = null;

                const pr = data.priceRange;
                if (pr && pr.min != null && pr.max != null) {
                    const lo = Math.floor(pr.min);
                    const hi = Math.ceil(pr.max);
                    if (lo !== priceBounds.min || hi !== priceBounds.max) {
                        priceBounds = { min: lo, max: hi };
                        calibratePriceSlider();
                    }
                }

                /* Update eyebrow with actual result count in search mode */
                updateEyebrowWithCount(data.total || 0);

                renderResultCount(data.total || 0, data.page, data.pageSize);
                renderCards(data.products || []);
                renderPagination(data.total || 0, data.page, data.pageSize);
                renderChips();
                renderFilterBadge();
            })
            .catch(function (err) {
                fetchController = null;
                if (err && err.name === 'AbortError') return;
                console.error('[catalog.js] fetch failed:', err);
                renderError();
            });
    }

    function applySortToGrid() {
        const cards = Array.from(grid.querySelectorAll('.product-card'));
        if (!cards.length) return;
        const px = el => {
            const s = el.querySelector('.product-price strong');
            return parseFloat((s ? s.textContent : '0').replace(/[^0-9.]/g, '')) || 0;
        };
        cards.sort((a, b) =>
            state.sort === 'price_asc'  ? px(a) - px(b) :
                state.sort === 'price_desc' ? px(b) - px(a) : 0
        );
        const frag = document.createDocumentFragment();
        cards.forEach(c => frag.appendChild(c));
        grid.appendChild(frag);
    }

    /* ── Render helpers ───────────────────────────────────────── */

    function renderSkeletons() {
        emptyState.style.display = 'none';
        pagination.style.display = 'none';
        if (resultCount) resultCount.textContent = '';
        const frag = document.createDocumentFragment();
        for (let i = 0; i < PAGE_SIZE; i++) frag.appendChild(CardFactory.skeleton());
        grid.replaceChildren(frag);
    }

    function renderCards(products) {
        if (!products.length) {
            grid.replaceChildren();
            emptyState.style.display = 'flex';
            return;
        }
        emptyState.style.display = 'none';
        const frag = document.createDocumentFragment();
        products.forEach(p => frag.appendChild(CardFactory.grid(p)));
        grid.replaceChildren(frag);
        if (state.sort !== 'newest') applySortToGrid();
    }

    function renderError() {
        grid.replaceChildren();
        const p = document.createElement('p');
        p.style.cssText = 'padding:48px;text-align:center;font-size:.85rem;color:var(--rw-danger);';
        p.textContent   = 'Could not load products — please refresh the page.';
        grid.appendChild(p);
        pagination.style.display = 'none';
    }

    function renderResultCount(total, page, pageSize) {
        if (!resultCount) return;
        if (!total) { resultCount.textContent = ''; return; }
        const start = page * pageSize + 1;
        const end   = Math.min((page + 1) * pageSize, total);
        resultCount.textContent = `${start}–${end} of ${total} product${total !== 1 ? 's' : ''}`;
    }

    /* ── Pagination ───────────────────────────────────────────── */

    function renderPagination(total, page, pageSize) {
        const totalPages = Math.ceil(total / pageSize);
        if (totalPages <= 1) { pagination.style.display = 'none'; return; }

        pagination.style.display = 'flex';
        pgPrev.disabled = page === 0;
        pgNext.disabled = page >= totalPages - 1;

        pgNums.replaceChildren();
        buildPageRange(page, totalPages).forEach(item => {
            if (item === '…') {
                const dot = document.createElement('span');
                dot.className   = 'catalog-pg-dots';
                dot.textContent = '…';
                pgNums.appendChild(dot);
            } else {
                const btn = document.createElement('button');
                btn.type        = 'button';
                btn.className   = 'catalog-pg-num' + (item === page ? ' is-active' : '');
                btn.textContent = String(item + 1);
                btn.addEventListener('click', () => goToPage(item));
                pgNums.appendChild(btn);
            }
        });
    }

    function buildPageRange(current, total) {
        const show = new Set(
            [0, total - 1, current, current - 1, current + 1]
                .filter(n => n >= 0 && n < total)
        );
        const sorted = [...show].sort((a, b) => a - b);
        const result = [];
        sorted.forEach((n, i) => {
            if (i > 0 && n > sorted[i - 1] + 1) result.push('…');
            result.push(n);
        });
        return result;
    }

    function goToPage(page) {
        state.page = page;
        pushUrl();
        fetchProducts();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    pgPrev.addEventListener('click', () => { if (state.page > 0) goToPage(state.page - 1); });
    pgNext.addEventListener('click', () => goToPage(state.page + 1));

    /* ── Filter chips ─────────────────────────────────────────── */

    function renderChips() {
        chips.replaceChildren();
        const frag = document.createDocumentFragment();

        /* Search query chip */
        if (state.q) {
            frag.appendChild(makeChip(`"${state.q}"`, () => {
                /* Removing the query chip → go to all products */
                window.location.href = CTX + '/catalog';
            }));
        }

        /* Gender chip — only when not URL-locked and not in interests mode */
        if (state.gender && !urlGender && !state.interestIds.length) {
            frag.appendChild(makeChip(
                state.gender === 'FEMALE' ? 'Women' : 'Men',
                () => { state.gender = null; onFilterChange(); }
            ));
        }

        /* newOnly chip — suppressed when it is the page context, not a user-applied filter */
        if (state.newOnly && !urlNewOnly) {
            frag.appendChild(makeChip('New Arrivals', () => {
                state.newOnly = false; onFilterChange();
            }));
        }

        /* Category chips — only when not locked by URL and not interests mode */
        if (!urlCategoryIds.length && !state.interestIds.length) {
            state.categoryIds.forEach(id => {
                frag.appendChild(makeChip(catNames[id] || `Cat ${id}`, () => {
                    state.categoryIds = state.categoryIds.filter(c => c !== id);
                    onFilterChange();
                }));
            });
        }

        /* Interests chip — suppressed when it is the page context */
        if (state.interestIds.length && !urlInterestIds.length) {
            frag.appendChild(makeChip('Your Interests', () => {
                state.interestIds = []; onFilterChange();
            }));
        }

        if (state.minPrice != null || state.maxPrice != null) {
            const lo = state.minPrice != null ? state.minPrice : priceBounds.min;
            const hi = state.maxPrice != null ? state.maxPrice : priceBounds.max;
            frag.appendChild(makeChip(`£${lo} – £${hi}`, () => {
                state.minPrice = null; state.maxPrice = null;
                resetPriceSlider(); onFilterChange();
            }));
        }

        chips.appendChild(frag);
    }

    function makeChip(label, onRemove) {
        const span = document.createElement('span');
        span.className   = 'catalog-chip';
        span.textContent = label;
        const x = document.createElement('button');
        x.type      = 'button';
        x.className = 'catalog-chip-x';
        x.setAttribute('aria-label', `Remove ${label} filter`);
        x.textContent = '×';
        x.addEventListener('click', onRemove);
        span.appendChild(x);
        return span;
    }

    function renderFilterBadge() {
        let count = 0;
        if (state.q)                                                          count++;
        if (state.gender && !urlGender && !state.interestIds.length)          count++;
        if (state.newOnly && !urlNewOnly)                                      count++;
        if (!urlCategoryIds.length && !state.interestIds.length)              count += state.categoryIds.length;
        if (state.interestIds.length && !urlInterestIds.length)               count++;
        if (state.minPrice != null || state.maxPrice != null)                 count++;
        filterBadge.textContent   = String(count);
        filterBadge.style.display = count > 0 ? 'inline-flex' : 'none';
    }

    /* ── Sidebar sync ─────────────────────────────────────────── */

    function syncSidebarToState() {
        document.querySelectorAll('input[name="filterGender"]').forEach(r => {
            r.checked = r.value === (state.gender || '');
        });

        const newChk = document.getElementById('filterNewOnly');
        if (newChk) newChk.checked = state.newOnly;

        document.querySelectorAll('.cat-chk').forEach(chk => {
            chk.checked = state.categoryIds.includes(parseInt(chk.value, 10));
        });

        applyFilterGroupVisibility();

        if (sortSel) sortSel.value = state.sort;

        if (state.minPrice != null) priceMinInput.value = state.minPrice;
        if (state.maxPrice != null) priceMaxInput.value = state.maxPrice;
        updatePriceDisplay();
    }

    function filterCategoriesByGender() {
        document.querySelectorAll('.catalog-chk-label[data-cat-gender]').forEach(label => {
            const hidden = !!state.gender && label.dataset.catGender !== state.gender;
            label.classList.toggle('is-hidden', hidden);
            if (hidden) {
                const chk = label.querySelector('.cat-chk');
                if (chk && chk.checked) {
                    chk.checked = false;
                    const id = parseInt(chk.value, 10);
                    state.categoryIds = state.categoryIds.filter(c => c !== id);
                }
            }
        });
    }

    /* ── Price slider ─────────────────────────────────────────── */

    function initPriceSlider() {
        calibratePriceSlider();
        priceMinInput.addEventListener('input', () => {
            clampSliders(); updatePriceDisplay(); schedulePriceFetch();
        });
        priceMaxInput.addEventListener('input', () => {
            clampSliders(); updatePriceDisplay(); schedulePriceFetch();
        });
    }

    function schedulePriceFetch() {
        clearTimeout(priceTimer);
        priceTimer = setTimeout(() => {
            const lo = parseInt(priceMinInput.value, 10);
            const hi = parseInt(priceMaxInput.value, 10);
            state.minPrice = lo > priceBounds.min ? lo : null;
            state.maxPrice = hi < priceBounds.max ? hi : null;
            onFilterChange();
        }, PRICE_DEBOUNCE);
    }

    function calibratePriceSlider() {
        let lo = priceBounds.min;
        let hi = priceBounds.max;
        if (lo === hi) { lo = Math.max(0, lo - 1); hi = hi + 1; }
        priceMinInput.min = lo; priceMinInput.max = hi;
        priceMaxInput.min = lo; priceMaxInput.max = hi;
        if (state.minPrice == null) priceMinInput.value = lo;
        if (state.maxPrice == null) priceMaxInput.value = hi;
        updatePriceDisplay();
    }

    function clampSliders() {
        const lo = parseInt(priceMinInput.value, 10);
        const hi = parseInt(priceMaxInput.value, 10);
        if (lo > hi) {
            if (document.activeElement === priceMinInput) priceMaxInput.value = lo;
            else priceMinInput.value = hi;
        }
    }

    function updatePriceDisplay() {
        const lo = parseInt(priceMinInput.value, 10);
        const hi = parseInt(priceMaxInput.value, 10);
        if (priceMinDisp) priceMinDisp.textContent = lo;
        if (priceMaxDisp) priceMaxDisp.textContent = hi;
        if (priceFill) {
            const range = (priceBounds.max - priceBounds.min) || 1;
            priceFill.style.left  = Math.max(0, ((lo - priceBounds.min) / range) * 100) + '%';
            priceFill.style.right = Math.max(0, ((priceBounds.max - hi)  / range) * 100) + '%';
        }
    }

    function resetPriceSlider() {
        priceMinInput.value = priceBounds.min;
        priceMaxInput.value = priceBounds.max;
        updatePriceDisplay();
    }

    /* ── Filter change entry point ────────────────────────────── */

    function onFilterChange() {
        state.page = 0;
        syncSidebarToState();
        updateHeading();
        updateSearchQueryDisplay();
        pushUrl();
        fetchProducts();
    }

    /* ── Sidebar controls wiring ──────────────────────────────── */

    document.querySelectorAll('input[name="filterGender"]').forEach(radio => {
        radio.addEventListener('change', () => {
            state.gender = radio.value || null;
            onFilterChange();
        });
    });

    const newOnlyChk = document.getElementById('filterNewOnly');
    if (newOnlyChk) {
        newOnlyChk.addEventListener('change', () => {
            state.newOnly = newOnlyChk.checked;
            onFilterChange();
        });
    }

    document.querySelectorAll('.cat-chk').forEach(chk => {
        chk.addEventListener('change', () => {
            const id = parseInt(chk.value, 10);
            if (chk.checked) {
                if (!state.categoryIds.includes(id)) state.categoryIds.push(id);
            } else {
                state.categoryIds = state.categoryIds.filter(c => c !== id);
            }
            onFilterChange();
        });
    });

    if (sortSel) {
        sortSel.addEventListener('change', () => {
            state.sort = sortSel.value;
            pushUrl();
            if (state.sort !== 'newest') applySortToGrid();
            else fetchProducts();
        });
    }

    document.querySelectorAll('.catalog-grid-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            grid.setAttribute('data-cols', btn.dataset.cols);
            document.querySelectorAll('.catalog-grid-btn').forEach(b =>
                b.classList.toggle('active', b === btn)
            );
        });
    });

    /* ── Clear all filters ────────────────────────────────────── */

    function clearFilters() {
        /* q is intentionally preserved — "Clear all" only resets the
           sidebar filters (gender, category, price) while staying on
           the current search results page.
           "Clear search" (#catalogClearSearch) is the one that
           navigates away to /catalog.                                 */
        state.minPrice = null;
        state.maxPrice = null;
        state.page     = 0;
        if (!urlGender)   state.gender  = null;
        if (!urlNewOnly)  state.newOnly = false;  // preserve when it is the page context
        /* Restore locked interest / category context if present (interest wins) */
        if (urlInterestIds.length > 0) {
            state.interestIds = [...urlInterestIds];
            state.categoryIds = [];
        } else if (urlCategoryIds.length > 0) {
            state.categoryIds = [...urlCategoryIds];
            state.interestIds = [];
        } else {
            state.categoryIds = [];
            state.interestIds = [];
        }
        resetPriceSlider();
        onFilterChange();
    }

    clearAllBtn.addEventListener('click', clearFilters);
    emptyClear.addEventListener('click', clearFilters);
    window.CatalogPage = { clearFilters };

    /* ── Mobile sidebar ───────────────────────────────────────── */

    const openSidebar = () => {
        sidebar.classList.add('is-open');
        backdrop.classList.add('is-open');
        document.body.style.overflow = 'hidden';
        filterToggle.setAttribute('aria-expanded', 'true');
    };
    const closeSidebar = () => {
        sidebar.classList.remove('is-open');
        backdrop.classList.remove('is-open');
        document.body.style.overflow = '';
        filterToggle.setAttribute('aria-expanded', 'false');
    };

    filterToggle.addEventListener('click', openSidebar);
    backdrop.addEventListener('click', closeSidebar);
    sidebarClose.addEventListener('click', closeSidebar);

    /* ── Filter group collapse ────────────────────────────────── */
    document.querySelectorAll('.catalog-fgroup-hd').forEach(hd => {
        hd.addEventListener('click', () =>
            hd.closest('.catalog-fgroup').classList.toggle('is-collapsed')
        );
        hd.addEventListener('keydown', e => {
            if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); hd.click(); }
        });
    });

}());