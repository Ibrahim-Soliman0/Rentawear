/* ============================================================
   cart.js  —  Rentawear cart  (OOP)
   Depends on: CardFactory (card-factory.js must load first)

   Item shape flowing through this file
   ─────────────────────────────────────────────────────────────
   The server returns CartItemDTO.  Before anything touches the
   UI the raw DTO is normalised by CartItemNormaliser into the
   flat shape CardFactory.cartItem() and _subtotal() expect:

   RAW from server (CartItemDTO)        NORMALISED (used everywhere)
   ────────────────────────────         ────────────────────────────
   cartItemId          (int)        →   cartItemId
   variantId           (int)        →   id          ← variantKey root
   core.id             (int)        →   productId   ← sent on mutations
   core.name           (string)     →   name
   core.brand          (string)     →   brand
   core.pricePerDay    (double)     →   pricePerDay
   core.imageUrl       (string)     →   imageUrl
   size                (string)     →   size
   color               (string)     →   color
   —                                →   colorName   (stripped from color)
   rentalDays          (int)        →   days        ← _subtotal()
   startDate           (LocalDate)  →   \
   endDate             (LocalDate)  →    → dates    ← "2024-11-12/2024-11-16"
   —                                →   inventoryQty (not in DTO — fetched
                                                       from variant on server,
                                                       returned in items list)
   qty                 (int)        →   qty         ← managed by Cart

   Variant key  =  "variantId:size:color"
   This is the unique identity for a cart line-item.  Multiple
   variants of the same product (different sizes / colours) are
   separate line-items sharing the same productId.

   Mutation payloads sent to the server
   ─────────────────────────────────────────────────────────────
   /cart/add        cartItemId, variantId, qty, startDate, endDate
   /cart/remove     cartItemId
   /cart/update-qty cartItemId, qty

   Public API  (window.Cart)
   ─────────────────────────────────────────────────────────────
   Cart.add(item)            — item must be a NORMALISED item
   Cart.remove(key)
   Cart.changeQty(key, delta)
   Cart.getItems()           — returns normalised items
   Cart.render()
   Cart.syncFromServer()
   Cart.variantKey(item)
   Cart.clearLocalStorage()
   ============================================================ */

'use strict';

/* ── CartItemNormaliser ───────────────────────────────────────────────────────
   Single place that knows about both the server DTO shape and the
   CardFactory shape.  Everything else in this file works with the
   normalised object only.
   ──────────────────────────────────────────────────────────────────────────── */
class CartItemNormaliser {

    /**
     * Convert a raw CartItemDTO (as returned by /cart/items) into the
     * flat object shape that CardFactory.cartItem() and Cart._subtotal() need.
     *
     * @param  {Object} raw  — raw JSON from the server
     * @returns {Object}     — normalised item
     */
    static fromDTO(raw) {
        const core = raw.core ?? {};

        // Build the ISO range string CardFactory._formatDates() expects:
        // "2024-11-12/2024-11-16"
        const dates = (raw.startDate && raw.endDate)
            ? `${raw.startDate}/${raw.endDate}`
            : null;

        // colorName — strip the hex prefix if present, e.g. "#2D5A3D-Forest Green" → "Forest Green"
        // If the color has no prefix it is used as-is for display.
        const colorName = raw.color
            ? raw.color.replace(/^#[0-9a-fA-F]+-/, '')
            : null;

        return {
            // ── Identity ──────────────────────────────────────────────────────
            cartItemId: raw.cartItemId ?? null,   // DB primary key of cart_items row
            id: raw.variantId ?? null,   // variantKey root  ("id" expected by CardFactory)
            productId: core.id ?? null,   // product PK — sent in mutation payloads

            // ── Display fields (CardFactory.cartItem) ─────────────────────────
            name: core.name ?? '',
            brand: core.brand ?? '',
            pricePerDay: Number(core.pricePerDay ?? 0),
            imageUrl: core.imageUrl ?? '/assets/img/placeholder',

            // ── Variant fields ────────────────────────────────────────────────
            size: raw.size ?? null,
            color: raw.color ?? null,   // raw value used in variantKey
            colorName: colorName,                 // display value used in UI

            // ── Rental period ─────────────────────────────────────────────────
            startDate: raw.startDate ?? null,   // "2024-11-12" — sent in mutations
            endDate: raw.endDate ?? null,   // "2024-11-16" — sent in mutations
            days: raw.rentalDays ?? 0,      // _subtotal() uses this
            dates: dates,                     // CardFactory._formatDates() uses this

            // ── Stock ─────────────────────────────────────────────────────────
            inventoryQty: raw.inventoryQty ?? null,  // inc-button cap in CardFactory

            // ── Quantity — managed client-side ────────────────────────────────
            // Server returns current qty; Cart mutates it locally then syncs.
            qty: Number(raw.qty ?? 1),
        };
    }

    /**
     * Normalise an array of raw DTOs in one call.
     * @param  {Array} rawList
     * @returns {Array}
     */
    static fromDTOList(rawList) {
        if (!Array.isArray(rawList)) return [];
        return rawList.map(CartItemNormaliser.fromDTO);
    }
}


/* ── CartStorage ─────────────────────────────────────────────────────────────
   Owns every interaction with localStorage.
   Stores normalised items only — the DTO shape never reaches localStorage.
   All methods are safe in private-browsing mode.
   ──────────────────────────────────────────────────────────────────────────── */
class CartStorage {

    static ITEMS_KEY = 'rw_cart_items';
    static SYNCED_KEY = 'rw_cart_synced';

    save(items) {
        try {
            localStorage.setItem(CartStorage.ITEMS_KEY, JSON.stringify(items));
        } catch (err) {
            console.warn('[CartStorage] save failed:', err);
        }
    }

    load() {
        try {
            const raw = localStorage.getItem(CartStorage.ITEMS_KEY);
            if (!raw) return [];
            const parsed = JSON.parse(raw);
            return Array.isArray(parsed) ? parsed : [];
        } catch (err) {
            console.warn('[CartStorage] load failed — clearing:', err);
            this.clear();
            return [];
        }
    }

    clear() {
        try {
            localStorage.removeItem(CartStorage.ITEMS_KEY);
            localStorage.removeItem(CartStorage.SYNCED_KEY);
        } catch (_) {
        }
    }

    markSynced() {
        try {
            localStorage.setItem(CartStorage.SYNCED_KEY, '1');
        } catch (_) {
        }
    }

    hasSynced() {
        try {
            return localStorage.getItem(CartStorage.SYNCED_KEY) === '1';
        } catch (_) {
            return false;
        }
    }
}


/* ── CartServer ──────────────────────────────────────────────────────────────
   Owns every fetch() call.
   Uses cartItemId for mutations — simpler and safer than re-deriving
   productId + size + color on the server.
   ──────────────────────────────────────────────────────────────────────────── */
class CartServer {

    constructor(contextPath = window.CTX) {
        this._ctx = contextPath;
    }

    get _headers() {
        return {'X-Requested-With': 'XMLHttpRequest'};
    }

    get _postHeaders() {
        return {
            ...this._headers,
            'Content-Type': 'application/x-www-form-urlencoded',
        };
    }

    async _checkResponse(res, label) {
        if (!res.ok) throw new Error(`[CartServer] ${label} — HTTP ${res.status}`);
        return res.json();
    }

    /**
     * GET /cart/items
     * Server returns: { items: [ ...CartItemDTO... ] }
     * Each DTO includes inventoryQty (joined from product_variants).
     */
    async fetchItems() {
        const res = await fetch(`${this._ctx}/cart/items`, {
            method: 'GET',
            credentials: 'same-origin',
            headers: this._headers,
        });
        return this._checkResponse(res, 'fetchItems');
    }

    /**
     * POST /cart/add
     * Payload: variantId, qty, startDate, endDate
     * Server derives rentalDays from the dates.
     * Returns: { success, cartItemId?, message? }
     * cartItemId is returned so the client can store it immediately.
     */
    async addItem(item) {
        const body = new URLSearchParams({
            variantId: item.id,          // item.id IS the variantId after normalisation
            qty: item.qty ?? 1,
            startDate: item.startDate ?? '',
            endDate: item.endDate ?? '',
        });
        const res = await fetch(`${this._ctx}/cart/add`, {
            method: 'POST',
            credentials: 'same-origin',
            headers: this._postHeaders,
            body,
        });
        return this._checkResponse(res, 'addItem');
    }

    /**
     * POST /cart/remove
     * Payload: cartItemId
     * Using the PK is simpler than re-deriving variant on the server.
     * Returns: { success, message? }
     */
    async removeItem(item) {
        const body = new URLSearchParams({
            cartItemId: item.cartItemId,
        });
        const res = await fetch(`${this._ctx}/cart/remove`, {
            method: 'POST',
            credentials: 'same-origin',
            headers: this._postHeaders,
            body,
        });
        return this._checkResponse(res, 'removeItem');
    }

    /**
     * POST /cart/update-qty
     * Payload: cartItemId, qty (absolute, not delta)
     * Returns: { success, message? }
     */
    async updateQty(item, newQty) {
        const body = new URLSearchParams({
            cartItemId: item.cartItemId,
            qty: newQty,
        });
        const res = await fetch(`${this._ctx}/cart/update-qty`, {
            method: 'POST',
            credentials: 'same-origin',
            headers: this._postHeaders,
            body,
        });
        return this._checkResponse(res, 'updateQty');
    }
}


/* ── CartRenderer ────────────────────────────────────────────────────────────
   Owns every DOM read/write for the cart drawer.
   Receives only normalised items — knows nothing about DTOs.
   ──────────────────────────────────────────────────────────────────────────── */
class CartRenderer {

    get _badge()      { return document.getElementById('cartBadge')      ?? document.getElementById('cartCount');       }
    get _countLabel() { return document.getElementById('cartCountLabel') ?? document.getElementById('cartHeaderCount'); }
    get _itemsWrap()  { return document.getElementById('cartItems')      ?? document.getElementById('cartItemsList');   }
    get _empty()      { return document.getElementById('cartEmpty');      }
    get _subtotal()   { return document.getElementById('cartSubtotal');   }
    get _total()      { return document.getElementById('cartTotal');      }
    get _cta()        { return document.querySelector('.cart-cta');       }

    render(items, subtotal) {
        const totalQty     = items.reduce((sum, i) => sum + i.qty, 0); // total units
        const productCount = items.length;                              // distinct line-items

        this._renderBadge(productCount);     // badge shows number of products
        this._renderCountLabel(totalQty);    // "3 items" label shows total units

        if (!this._itemsWrap) return;
        items.length === 0 ? this._renderEmpty() : this._renderItems(items);
        if (items.length > 0) this._renderTotals(subtotal);
    }

    _renderBadge(count) {
        if (!this._badge) return;
        this._badge.textContent = String(count);
        this._badge.classList.toggle('visible', count > 0);
    }

    _renderCountLabel(totalQty) {
        if (!this._countLabel) return;
        this._countLabel.textContent =
            totalQty === 0 ? '0 items' : `${totalQty} item${totalQty !== 1 ? 's' : ''}`;
    }

    _renderEmpty() {
        if (this._empty)    this._empty.style.display  = 'flex';
        if (this._cta)      this._cta.style.display    = 'none';
        if (this._subtotal) this._subtotal.textContent = '$0.00';
        if (this._total)    this._total.textContent    = '$0.00';
        const wrap = this._itemsWrap;
        wrap.style.display = 'none';
        while (wrap.firstChild) wrap.removeChild(wrap.firstChild);
    }

    _renderItems(items) {
        if (this._empty) this._empty.style.display = 'none';
        if (this._cta)   this._cta.style.display   = 'flex';

        const wrap = this._itemsWrap;
        wrap.style.display = 'block';

        const existingKeys = [...wrap.querySelectorAll('.cart-item')].map(li => li.dataset.key);
        const currentKeys  = items.map(i => this._keyOf(i));
        const listChanged  = JSON.stringify(existingKeys) !== JSON.stringify(currentKeys);

        if (listChanged) {
            while (wrap.firstChild) wrap.removeChild(wrap.firstChild);
            const frag = document.createDocumentFragment();
            items.forEach(item => {
                try   { frag.appendChild(CardFactory.cartItem(item)); }
                catch (err) { console.error('[CartRenderer] CardFactory.cartItem failed:', item, err); }
            });
            wrap.appendChild(frag);
        } else {
            items.forEach(item => {
                const li = wrap.querySelector(`.cart-item[data-key="${this._cssSafeKey(this._keyOf(item))}"]`);
                if (!li) return;
                const num = li.querySelector('.qty-num');
                if (num) num.textContent = String(item.qty);
                const inc = li.querySelector('.qty-btn[data-action="inc"]');
                if (inc) inc.disabled = item.inventoryQty != null && item.qty >= item.inventoryQty;
            });
        }
    }

    _renderTotals(subtotal) {
        const formatted = `$${subtotal.toFixed(2)}`;
        if (this._subtotal) this._subtotal.textContent = formatted;
        if (this._total)    this._total.textContent    = formatted;
    }

    _keyOf(item)     { return `${item.id}:${item.size || ''}:${item.color || ''}`; }
    _cssSafeKey(key) { return key.replace(/\\/g, '\\\\').replace(/"/g, '\\"');     }
}


/* ── CartToast ───────────────────────────────────────────────────────────────
   Single-element toast.  Created lazily on first show().
   ──────────────────────────────────────────────────────────────────────────── */
class CartToast {

    constructor(duration = 2500) {
        this._duration = duration;
        this._el = null;
        this._timer = null;
    }

    show(message) {
        this._ensureElement();
        this._el.textContent = message;
        this._el.style.opacity = '1';
        clearTimeout(this._timer);
        this._timer = setTimeout(() => {
            this._el.style.opacity = '0';
        }, this._duration);
    }

    _ensureElement() {
        if (this._el) return;
        this._el = document.createElement('div');
        this._el.className = 'cart-toast';
        Object.assign(this._el.style, {
            position: 'fixed', bottom: '80px', right: '20px',
            background: 'var(--rw-charcoal, #333)', color: '#fff',
            padding: '10px 16px', borderRadius: '6px', fontSize: '13px',
            zIndex: '9999', opacity: '0', transition: 'opacity .2s',
            pointerEvents: 'none',
        });
        document.body.appendChild(this._el);
    }
}


/* ── Cart ────────────────────────────────────────────────────────────────────
   Orchestrator.  _items always holds normalised items.
   DTOs from the server are normalised immediately in syncFromServer().
   ──────────────────────────────────────────────────────────────────────────── */
class Cart {

    constructor() {
        this._items = [];
        this._storage = new CartStorage();
        this._server = new CartServer(window.CTX ?? '');
        this._renderer = new CartRenderer();
        this._toast = new CartToast();

        this._attachEvents();
        this._boot();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Variant key — "variantId:size:color"
     * item.id IS the variantId after normalisation.
     */
    variantKey(item) {
        return `${item.id}:${item.size || ''}:${item.color || ''}`;
    }

    getItems() {
        return [...this._items];
    }

    render() {
        this._renderer.render(this._items, this._subtotal());
    }

    /**
     * Fetch from server, normalise DTOs, overwrite localStorage, re-render.
     * Call after login, logout, or any external cart mutation.
     */
    async syncFromServer() {
        try {
            const data = await this._server.fetchItems();         // { items: [...DTOs] }
            this._items = CartItemNormaliser.fromDTOList(data.items); // normalise here
            this._storage.save(this._items);
            this._storage.markSynced();
            this.render();
            console.info(`[Cart] synced — ${this._items.length} item(s)`);
        } catch (err) {
            console.warn('[Cart] syncFromServer failed, using localStorage cache:', err);
            this._items = this._storage.load();                       // already normalised
            this.render();
        }
    }

    /**
     * Add an item.  item must already be normalised (built by the product page).
     * See CartItemNormaliser.fromDTO() for the expected shape.
     */
    async add(item) {
        const key = this.variantKey(item);
        const existing = this._findByKey(key);

        if (existing && existing.inventoryQty != null && existing.qty >= existing.inventoryQty) {
            this._toast.show(`Only ${existing.inventoryQty} in stock for this variant.`);
            return;
        }

        // Optimistic update
        existing ? existing.qty++ : this._items.push({...item, qty: 1});
        this._commitLocal();

        // Server sync
        try {
            const result = await this._server.addItem(existing ?? {...item, qty: 1});
            if (result.success) {
                // Store the cartItemId the server just created so future
                // remove / update-qty calls can use it immediately.
                const stored = this._findByKey(key);
                if (stored && result.cartItemId) stored.cartItemId = result.cartItemId;
                this._commitLocal();
            } else {
                this._toast.show(result.message ?? 'Could not add item. Please try again.');
                this._rollbackAdd(key, !!existing);
            }
        } catch (err) {
            console.error('[Cart] add — server error:', err);
            this._toast.show('Network error. Your cart will sync when you reconnect.');
        }
    }

    async remove(key) {
        const item = this._findByKey(key);
        if (!item) return;

        const snapshot = [...this._items];

        this._items = this._items.filter(i => this.variantKey(i) !== key);
        this._commitLocal();

        try {
            const result = await this._server.removeItem(item);
            if (!result.success) {
                this._toast.show(result.message ?? 'Could not remove item. Please try again.');
                this._items = snapshot;
                this._commitLocal();
            }
        } catch (err) {
            console.error('[Cart] remove — server error:', err);
            this._toast.show('Network error. Your cart will sync when you reconnect.');
        }
    }

    async changeQty(key, delta) {
        const item = this._findByKey(key);
        if (!item) return;

        if (delta > 0 && item.inventoryQty != null && item.qty >= item.inventoryQty) {
            this._toast.show(`Only ${item.inventoryQty} in stock for this variant.`);
            return;
        }

        if (item.qty + delta <= 0) {
            await this.remove(key);
            return;
        }

        const prevQty = item.qty;
        item.qty += delta;
        this._commitLocal();

        try {
            const result = await this._server.updateQty(item, item.qty);
            if (!result.success) {
                this._toast.show(result.message ?? 'Could not update quantity. Please try again.');
                item.qty = prevQty;
                this._commitLocal();
            }
        } catch (err) {
            console.error('[Cart] changeQty — server error:', err);
            this._toast.show('Network error. Your cart will sync when you reconnect.');
        }
    }

    clearLocalStorage() {
        this._storage.clear();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    _subtotal() {
        return this._items.reduce((sum, i) => sum + i.pricePerDay * i.days * i.qty, 0);
    }

    _findByKey(key) {
        return this._items.find(i => this.variantKey(i) === key) ?? null;
    }

    _commitLocal() {
        this.render();
        this._storage.save(this._items);
    }

    _rollbackAdd(key, wasExisting) {
        if (wasExisting) {
            const item = this._findByKey(key);
            if (item) item.qty--;
        } else {
            this._items = this._items.filter(i => this.variantKey(i) !== key);
        }
        this._commitLocal();
    }

    // ── Event delegation ──────────────────────────────────────────────────────

    _attachEvents() {
        const handler = (e) => {
            const btn = e.target.closest('[data-action]');
            const removeBtn = e.target.closest('.cart-remove');
            if (btn) {
                const key = btn.dataset.key;
                if (!key) return;
                if (btn.dataset.action === 'inc') this.changeQty(key, 1);
                if (btn.dataset.action === 'dec') this.changeQty(key, -1);
            }
            if (removeBtn) {
                const key = removeBtn.dataset.key;
                if (key) this.remove(key);
            }
        };
        document.getElementById('cartItems')?.addEventListener('click', handler);
        document.getElementById('cartItemsList')?.addEventListener('click', handler);
    }

    // ── Boot ──────────────────────────────────────────────────────────────────

    _boot() {
        if (!this._storage.hasSynced()) {
            this.syncFromServer();
        } else {
            this._items = this._storage.load();   // already normalised
            this.render();
            this.syncFromServer();                // silent background reconcile
        }
    }
}

window.Cart = new Cart();