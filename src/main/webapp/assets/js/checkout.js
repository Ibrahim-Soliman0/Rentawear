/* ============================================================
   checkout.js  —  Rentawear checkout page

   Boot sequence
   ─────────────────────────────────────────────────────────────
   1. renderCart()       — synchronous, reads localStorage
                           → populates order summary + rental period rows
                           → writes totalAmount hidden field
   2. loadUserSession()  — async, calls GET /user/session
                           → renders address + payment cards
                           → checks total against user.creditLimit
   3. attachFormHandler() — intercepts placeOrder as AJAX

   Credit limit check
   ─────────────────────────────────────────────────────────────
   After /user/session returns, JS compares the computed total
   (already written to #totalAmountInput) against user.creditLimit.
   If total > creditLimit → show warning banner + disable confirm.
   The servlet also validates this server-side using user.creditLimit().
   ============================================================ */

'use strict';

/* ── DOM refs ────────────────────────────────────────────────────────────── */
const dom = {
    itemsList: () => document.getElementById('checkoutCartItems'),
    rentalList: () => document.getElementById('rentalItemsList'),
    pricing: () => document.getElementById('checkoutPricing'),
    subtotalEl: () => document.getElementById('coSubtotal'),
    totalEl: () => document.getElementById('coTotal'),
    confirmBtn: () => document.getElementById('confirmPayBtn'),
    cartJsonInput: () => document.getElementById('cartJson'),
    paymentIdInput: () => document.getElementById('selectedPaymentId'),
    deliveryInput: () => document.getElementById('deliveryDateInput'),
    returnInput: () => document.getElementById('returnDateInput'),
    totalAmountInput: () => document.getElementById('totalAmountInput'),
    addressContainer: () => document.getElementById('addressContainer'),
    paymentContainer: () => document.getElementById('paymentContainer'),
    creditWarning: () => document.getElementById('creditWarning'),
    creditWarningMsg: () => document.getElementById('creditWarningMsg'),
    form: () => document.getElementById('checkoutForm'),
};

/* ── Image URL helper ────────────────────────────────────────────────────── */
// Returns null when base is missing or already-resolved — callers must guard.
// Mirrors imgUrl() in card-factory.js so all surfaces behave identically.
function coImgUrl(base, size) {
    if (!base || base.includes('placeholder') || base.endsWith('.jpg') || base.endsWith('.png')) {
        return null;
    }
    return `${window.CTX ?? ''}${base}_${size}.jpg`;
}

// Replaces a broken or missing <img> with the standard placeholder icon.
// Called via onerror on every <img> in this file so 404s get the same
// treatment as a missing image path.
function _imgToIcon(img) {
    const icon = document.createElement('div');
    icon.className = 'product-img-placeholder';
    const i = document.createElement('i');
    i.className = 'bi bi-image';
    icon.appendChild(i);
    if (img.parentNode) img.parentNode.replaceChild(icon, img);
}

/* ── Date formatters ─────────────────────────────────────────────────────── */
function formatDateRange(startIso, endIso) {
    if (!startIso || !endIso) return '';
    const a = new Date(startIso + 'T00:00:00Z');
    const b = new Date(endIso + 'T00:00:00Z');
    if (isNaN(a) || isNaN(b)) return `${startIso} → ${endIso}`;
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const fmtA = `${a.getUTCDate()} ${months[a.getUTCMonth()]}`;
    const fmtB = `${b.getUTCDate()} ${months[b.getUTCMonth()]} ${b.getUTCFullYear()}`;
    return `${fmtA} → ${fmtB}`;
}

function fmt(amount) {
    return '£' + Number(amount).toFixed(2);
}

/* ── Read cart from localStorage ─────────────────────────────────────────── */
function loadCartItems() {
    try {
        const raw = localStorage.getItem('rw_cart_items');
        if (!raw) return [];
        const parsed = JSON.parse(raw);
        return Array.isArray(parsed) ? parsed : [];
    } catch (_) {
        return [];
    }
}

/* ── Order success overlay ───────────────────────────────────────────────── */
/**
 * Shows the full-screen success overlay, waits for the CSS animations to
 * complete, then redirects to redirectUrl.
 */
function showOrderSuccess(redirectUrl) {
    localStorage.removeItem('rw_cart_items');
    localStorage.removeItem('rw_cart_synced');
    if (window.Cart?.clearLocalStorage) window.Cart.clearLocalStorage();

    const overlay = document.getElementById('orderSuccessOverlay');
    if (!overlay) {
        window.location.href = redirectUrl;
        return;
    }

    overlay.setAttribute('aria-hidden', 'false');
    overlay.classList.add('visible');

    // Match animation timing (~2.3s)
    setTimeout(() => {
        window.location.href = redirectUrl;
    }, 2300);
}

/* ── Submit error banner ─────────────────────────────────────────────────── */
function showSubmitError(message) {
    let el = document.getElementById('checkoutSubmitError');
    if (!el) {
        el = document.createElement('div');
        el.id = 'checkoutSubmitError';
        el.className = 'rw-auth-alert rw-auth-alert--error';
        el.style.marginTop = '16px';
        const btn = dom.confirmBtn();
        if (btn) btn.parentNode.insertBefore(el, btn);
    }
    el.innerHTML = `
        <svg width="16" height="16" viewBox="0 0 24 24" stroke="currentColor" fill="none"
             stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <span>${message} Please try again later.</span>`;
    el.style.display = 'flex';
    el.scrollIntoView({behavior: 'smooth', block: 'nearest'});
}

function hideSubmitError() {
    const el = document.getElementById('checkoutSubmitError');
    if (el) el.style.display = 'none';
}

function setButtonLoading(loading) {
    const btn = dom.confirmBtn();
    if (!btn) return;
    btn.disabled = loading;
    btn.textContent = loading ? 'Processing…' : 'Confirm & Pay';
}

/* ── Credit limit check ──────────────────────────────────────────────────── */
function checkCreditLimit(total, creditLimit) {
    const warning = dom.creditWarning();
    const msgEl = dom.creditWarningMsg();
    const btn = dom.confirmBtn();

    if (creditLimit != null && total > creditLimit) {
        if (msgEl) {
            msgEl.textContent =
                `Your order total (${fmt(total)}) exceeds your available credit limit (${fmt(creditLimit)}).`;
        }
        if (warning) warning.classList.add('show');
        if (btn) btn.disabled = true;
    } else {
        if (warning) warning.classList.remove('show');
    }
}

/* ── Rental period section ───────────────────────────────────────────────── */
function buildRentalRow(item) {
    const li = document.createElement('li');
    li.className = 'rental-item-row';

    let dateRange = '';
    if (item.startDate && item.endDate) {
        dateRange = formatDateRange(item.startDate, item.endDate);
    } else if (item.dates) {
        const parts = item.dates.split('/');
        dateRange = parts.length === 2 ? formatDateRange(parts[0], parts[1]) : item.dates;
    }

    const daysLabel = item.days ? `${item.days} day${item.days !== 1 ? 's' : ''}` : '';

    const rentalThumbSrc = coImgUrl(item.imageUrl, 'sm');
    const rentalThumbHtml = rentalThumbSrc
        ? `<img src="${rentalThumbSrc}" alt="${item.name ?? ''}" loading="lazy" decoding="async"
                onerror="_imgToIcon(this)"/>`
        : `<div class="product-img-placeholder"><i class="bi bi-image"></i></div>`;

    li.innerHTML = `
        <div class="rental-item-thumb">${rentalThumbHtml}</div>
        <div class="rental-item-info">
            <div class="rental-item-name">${item.name ?? ''}</div>
            ${dateRange ? `
            <div class="rental-item-dates">
                <svg viewBox="0 0 24 24">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                    <line x1="16" y1="2" x2="16" y2="6"/>
                    <line x1="8"  y1="2" x2="8"  y2="6"/>
                    <line x1="3"  y1="10" x2="21" y2="10"/>
                </svg>
                ${dateRange}
            </div>` : ''}
            ${daysLabel ? `<div class="rental-item-duration">${daysLabel}</div>` : ''}
        </div>`;
    return li;
}

function renderRentalPeriod(items) {
    const list = dom.rentalList();
    if (!list) return;
    list.innerHTML = '';    // clear skeleton

    if (!items.length) {
        const li = document.createElement('li');
        li.innerHTML = '<p class="rental-empty">No items in your cart.</p>';
        list.appendChild(li);
        const delInput = dom.deliveryInput();
        const retInput = dom.returnInput();
        if (delInput) delInput.value = '';
        if (retInput) retInput.value = '';
        return;
    }

    const startDates = items.map(i => i.startDate).filter(Boolean).sort();
    const endDates = items.map(i => i.endDate).filter(Boolean).sort();
    const earliest = startDates[0] ?? null;
    const latest = endDates[endDates.length - 1] ?? null;

    const delInput = dom.deliveryInput();
    const retInput = dom.returnInput();
    if (delInput) delInput.value = earliest ?? '';
    if (retInput) retInput.value = latest ?? '';

    const frag = document.createDocumentFragment();
    items.forEach(item => frag.appendChild(buildRentalRow(item)));
    list.appendChild(frag);
}

/* ── Order summary (right column) ───────────────────────────────────────── */
function renderEmpty() {
    const list = dom.itemsList();
    if (!list) return;
    list.innerHTML = '';
    const li = document.createElement('li');
    li.innerHTML = `
        <div class="co-empty">
            <div class="co-empty-icon">
                <svg viewBox="0 0 24 24">
                    <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                    <line x1="3" y1="6" x2="21" y2="6"/>
                    <path d="M16 10a4 4 0 0 1-8 0"/>
                </svg>
            </div>
            <p class="co-empty-label">Your cart is empty</p>
            <a href="${window.CTX ?? ''}/home" class="co-empty-link">Continue browsing →</a>
        </div>`;
    list.appendChild(li);
    const pricing = dom.pricing();
    if (pricing) pricing.style.display = 'none';
    const btn = dom.confirmBtn();
    if (btn) btn.disabled = true;
}

function buildSummaryRow(item) {
    const li = document.createElement('li');
    li.className = 'co-item';
    const lineTotal = (item.pricePerDay ?? 0) * (item.days ?? 0) * (item.qty ?? 1);
    const sizePart = item.size ? `Size ${item.size}` : '';
    const colorPart = item.colorName
        || (item.color ? item.color.replace(/^#[0-9a-fA-F]+-/, '') : '');
    const variantLabel = [sizePart, colorPart].filter(Boolean).join(' · ');

    let dateRange = '';
    if (item.startDate && item.endDate) {
        dateRange = formatDateRange(item.startDate, item.endDate);
    } else if (item.dates) {
        const parts = item.dates.split('/');
        dateRange = parts.length === 2 ? formatDateRange(parts[0], parts[1]) : item.dates;
    }

    const summaryThumbSrc = coImgUrl(item.imageUrl, 'sm');
    const summaryThumbHtml = summaryThumbSrc
        ? `<img src="${summaryThumbSrc}" alt="${item.name ?? ''}" loading="lazy" decoding="async"
                onerror="_imgToIcon(this)"/>`
        : `<div class="product-img-placeholder"><i class="bi bi-image"></i></div>`;

    li.innerHTML = `
        <div class="co-item-img">${summaryThumbHtml}</div>
        <div class="co-item-info">
            <div class="co-item-brand">${item.brand ?? ''}</div>
            <div class="co-item-name">${item.name ?? ''}</div>
            ${variantLabel ? `<div class="co-item-meta">${variantLabel}</div>` : ''}
            ${dateRange ? `<div class="co-item-meta co-item-dates">${dateRange}</div>` : ''}
            ${(item.qty ?? 1) > 1 ? `<div class="co-item-meta">Qty: ${item.qty}</div>` : ''}
        </div>
        <div class="co-item-price">${fmt(lineTotal)}</div>`;
    return li;
}

function renderPricing(items) {
    const subtotal = items.reduce(
        (sum, i) => sum + (i.pricePerDay ?? 0) * (i.days ?? 0) * (i.qty ?? 1), 0
    );
    if (dom.subtotalEl()) dom.subtotalEl().textContent = fmt(subtotal);
    if (dom.totalEl()) dom.totalEl().textContent = fmt(subtotal);
    const totalInput = dom.totalAmountInput();
    if (totalInput) totalInput.value = subtotal.toFixed(2);
    const pricing = dom.pricing();
    if (pricing) pricing.style.display = 'block';
    return subtotal;
}

function renderCart() {
    const items = loadCartItems();
    const list = dom.itemsList();
    if (!list) return;

    list.innerHTML = '';
    renderRentalPeriod(items);

    if (!items.length) {
        renderEmpty();
        return;
    }

    const frag = document.createDocumentFragment();
    items.forEach(item => frag.appendChild(buildSummaryRow(item)));
    list.appendChild(frag);

    renderPricing(items);

    const btn = dom.confirmBtn();
    if (btn) btn.disabled = false;

    const cartJsonInput = dom.cartJsonInput();
    if (cartJsonInput) cartJsonInput.value = JSON.stringify(items);
}

/* ── User session ────────────────────────────────────────────────────────── */
function renderAddress(user) {
    const container = dom.addressContainer();
    if (!container) return;

    if (user.address) {
        container.innerHTML = `
            <div class="address-card">
                <div class="address-card-icon">
                    <svg viewBox="0 0 24 24">
                        <path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0 1 18 0z"/>
                        <circle cx="12" cy="10" r="3"/>
                    </svg>
                </div>
                <div class="address-card-body">
                    <div class="address-card-name">${user.name ?? ''}</div>
                    <div class="address-card-line">${user.address}</div>
                </div>
            </div>`;
    } else {
        container.innerHTML = `
            <div class="section-missing">
                <p>No delivery address saved on your account.</p>
                <a href="${window.CTX ?? ''}/profile">Add an address in your profile →</a>
            </div>`;
        const btn = dom.confirmBtn();
        if (btn) btn.disabled = true;
    }
}

function renderPaymentCards(user) {
    const container = dom.paymentContainer();
    const payInput = dom.paymentIdInput();
    if (!container) return;

    const cards = user.paymentCards ?? [];

    if (!cards.length) {
        container.innerHTML = `
            <div class="section-missing">
                <p>No payment method saved on your account.</p>
                <a href="${window.CTX ?? ''}/profile#billing">Add a card in your profile →</a>
            </div>`;
        const btn = dom.confirmBtn();
        if (btn) btn.disabled = true;
        return;
    }

    container.innerHTML = '';
    cards.forEach((card, index) => {
        const isFirst = index === 0;
        const div = document.createElement('div');
        div.className = `payment-option${isFirst ? ' selected' : ''}`;
        div.innerHTML = `
            <div class="payment-radio"></div>
            <div class="payment-icon">
                <svg viewBox="0 0 24 24">
                    <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
                    <line x1="1" y1="10" x2="23" y2="10"/>
                </svg>
            </div>
            <div class="payment-body">
                <div class="payment-label">${card.cardType} ending in ${card.lastFour}</div>
                <div class="payment-sub">Expires ${card.expiryMonth}/${card.expiryYear}</div>
            </div>`;

        div.addEventListener('click', () => {
            container.querySelectorAll('.payment-option').forEach(o => o.classList.remove('selected'));
            div.classList.add('selected');
            if (payInput) payInput.value = card.id;
        });

        container.appendChild(div);
        if (isFirst && payInput) payInput.value = card.id;
    });
}

async function loadUserSession() {
    try {
        const res = await fetch(`${window.CTX ?? ''}/user/session`, {
            method: 'GET',
            credentials: 'same-origin',
            headers: {'X-Requested-With': 'XMLHttpRequest'},
        });

        if (res.status === 401) {
            window.location.href = `${window.CTX ?? ''}/login`;
            return;
        }

        const user = await res.json();
        renderAddress(user);
        renderPaymentCards(user);

        const totalInput = dom.totalAmountInput();
        const total = totalInput ? Number(totalInput.value) : 0;
        checkCreditLimit(total, user.creditLimit ?? null);

    } catch (err) {
        console.error('[checkout] failed to load user session:', err);
        const container = dom.addressContainer();
        if (container) {
            container.innerHTML = `
                <div class="section-missing">
                    <p>Could not load your account details. Please refresh the page.</p>
                </div>`;
        }
    }
}

/* ── AJAX form submission ─────────────────────────────────────────────────── */
function attachFormHandler() {
    const form = dom.form();
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        const trigger = e.submitter;
        if (!trigger || trigger.value !== 'placeOrder') return;

        e.preventDefault();
        hideSubmitError();
        setButtonLoading(true);

        const items = loadCartItems();
        const cartJsonInput = dom.cartJsonInput();
        const totalAmtInput = dom.totalAmountInput();
        if (cartJsonInput) cartJsonInput.value = JSON.stringify(items);
        if (totalAmtInput) {
            const subtotal = items.reduce(
                (sum, i) => sum + (i.pricePerDay ?? 0) * (i.days ?? 0) * (i.qty ?? 1), 0
            );
            totalAmtInput.value = subtotal.toFixed(2);
        }

        try {
            const formData = new FormData(form);
            formData.append('action', trigger.value);

            const res = await fetch(`${window.CTX ?? ''}/checkout`, {
                method: 'POST',
                credentials: 'same-origin',
                headers: {'X-Requested-With': 'XMLHttpRequest'},
                body: new URLSearchParams(formData),
            });

            const text = await res.text();
            let data;
            try {
                data = JSON.parse(text);
            } catch (_) {
                throw new Error('Unexpected server response.');
            }

            if (data.success) {
                // ── Show animation then redirect ──────────────────────────────
                // showOrderSuccess() clears the cart and handles the redirect
                // after the animation completes (~2.2 s).
                showOrderSuccess(data.redirect ?? `${window.CTX ?? ''}/profile#history`);

            } else {
                showSubmitError(data.message
                    ? `${data.message}.`
                    : 'Something went wrong placing your order.');
                setButtonLoading(false);
            }

        } catch (err) {
            console.error('[checkout] submit error:', err);
            showSubmitError(err.message || 'A network error occurred.');
            setButtonLoading(false);
        }
    });
}

/* ── Boot ────────────────────────────────────────────────────────────────── */
renderCart();           // sync — localStorage is instant, writes totalAmount
loadUserSession();      // async — replaces skeletons, runs credit check
attachFormHandler();