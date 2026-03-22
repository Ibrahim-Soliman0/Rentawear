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
function coImgUrl(base, size) {
    const b = (base && !base.endsWith('.jpg') && !base.endsWith('.png'))
        ? base : '/assets/img/placeholder';
    return `${window.CTX ?? ''}${b}_${size}.jpg`;
}

/* ── Date formatters ─────────────────────────────────────────────────────── */

// "2024-11-12" → "12 Nov 2024"
function formatDateFull(iso) {
    if (!iso) return '—';
    const d = new Date(iso + 'T00:00:00');
    if (isNaN(d)) return iso;
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return `${d.getUTCDate()} ${months[d.getUTCMonth()]} ${d.getUTCFullYear()}`;
}

// "2024-11-12", "2024-11-16" → "12 Nov → 16 Nov 2024"
function formatDateRange(startIso, endIso) {
    if (!startIso || !endIso) return '';
    const a = new Date(startIso + 'T00:00:00');
    const b = new Date(endIso + 'T00:00:00');
    if (isNaN(a) || isNaN(b)) return `${startIso} → ${endIso}`;
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const fmtA = `${a.getUTCDate()} ${months[a.getUTCMonth()]}`;
    const fmtB = `${b.getUTCDate()} ${months[b.getUTCMonth()]} ${b.getUTCFullYear()}`;
    return `${fmtA} → ${fmtB}`;
}

function fmt(amount) {
    return '$' + Number(amount).toFixed(2);
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
// Called after loadUserSession() resolves so we have both the total and the limit.
// creditLimit from UserSessionDTO is a BigDecimal — arrives as a number in JSON.
function checkCreditLimit(total, creditLimit) {
    const warning = dom.creditWarning();
    const msgEl = dom.creditWarningMsg();
    const btn = dom.confirmBtn();

    if (creditLimit != null && total > creditLimit) {
        // Insufficient credit
        if (msgEl) {
            msgEl.textContent =
                `Your order total (${fmt(total)}) exceeds your available credit limit (${fmt(creditLimit)}).`;
        }
        if (warning) warning.classList.add('show');
        if (btn) btn.disabled = true;
    } else {
        // Sufficient credit — hide warning and re-enable button
        // (only if it wasn't disabled for other reasons like empty cart / no address)
        if (warning) warning.classList.remove('show');
    }
}

/* ── Rental period section ───────────────────────────────────────────────── */

// Renders one row per cart item: thumbnail | product name | date range + duration

function buildRentalRow(item) {
    const li = document.createElement('li');
    li.className = 'rental-item-row';

    // Resolve date range from item fields
    let dateRange = '';
    if (item.startDate && item.endDate) {
        dateRange = formatDateRange(item.startDate, item.endDate);
    } else if (item.dates) {
        const parts = item.dates.split('/');
        dateRange = parts.length === 2
            ? formatDateRange(parts[0], parts[1])
            : item.dates;
    }

    const daysLabel = item.days
        ? `${item.days} day${item.days !== 1 ? 's' : ''}`
        : '';

    li.innerHTML = `
        <div class="rental-item-thumb">
            <img src="${coImgUrl(item.imageUrl, 'sm')}"
                 alt="${item.name ?? ''}" loading="lazy" decoding="async" />
        </div>
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

        // Write placeholder dates to hidden inputs
        const delInput = dom.deliveryInput();
        const retInput = dom.returnInput();
        if (delInput) delInput.value = '';
        if (retInput) retInput.value = '';
        return;
    }

    // Find the earliest startDate and latest endDate across all items
    const startDates = items.map(i => i.startDate).filter(Boolean).sort();
    const endDates = items.map(i => i.endDate).filter(Boolean).sort();
    const earliest = startDates[0] ?? null;
    const latest = endDates[endDates.length - 1] ?? null;

    // Write to hidden inputs for the servlet
    const delInput = dom.deliveryInput();
    const retInput = dom.returnInput();
    if (delInput) delInput.value = earliest ?? '';
    if (retInput) retInput.value = latest ?? '';

    // Render one row per item
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
            <a href="${window.CTX ?? ''}/explore" class="co-empty-link">Continue browsing →</a>
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
        dateRange = parts.length === 2
            ? formatDateRange(parts[0], parts[1])
            : item.dates;
    }

    li.innerHTML = `
        <div class="co-item-img">
            <img src="${coImgUrl(item.imageUrl, 'sm')}"
                 alt="${item.name ?? ''}" loading="lazy" decoding="async" />
        </div>
        <div class="co-item-info">
            <div class="co-item-brand">${item.brand ?? ''}</div>
            <div class="co-item-name">${item.name ?? ''}</div>
            ${variantLabel ? `<div class="co-item-meta">${variantLabel}</div>` : ''}
            ${dateRange ? `<div class="co-item-meta co-item-dates">📅 ${dateRange}</div>` : ''}
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

    // Write total to hidden field so servlet can check against creditLimit
    const totalInput = dom.totalAmountInput();
    if (totalInput) totalInput.value = subtotal.toFixed(2);

    const pricing = dom.pricing();
    if (pricing) pricing.style.display = 'block';

    return subtotal;   // returned so loadUserSession() can use it for credit check
}

function renderCart() {
    const items = loadCartItems();
    const list = dom.itemsList();
    if (!list) return;

    list.innerHTML = '';    // clear skeleton

    // Always render rental period rows (even on empty — shows empty state)
    renderRentalPeriod(items);

    if (!items.length) {
        renderEmpty();
        return;
    }

    // Order summary items
    const frag = document.createDocumentFragment();
    items.forEach(item => frag.appendChild(buildSummaryRow(item)));
    list.appendChild(frag);

    const subtotal = renderPricing(items);

    // Enable confirm provisionally — loadUserSession() may disable it
    // if address is missing, no payment card, or credit limit exceeded
    const btn = dom.confirmBtn();
    if (btn) btn.disabled = false;

    // Pre-populate cart JSON for the servlet
    const cartJsonInput = dom.cartJsonInput();
    if (cartJsonInput) cartJsonInput.value = JSON.stringify(items);

    return subtotal;
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
            container.querySelectorAll('.payment-option')
                .forEach(o => o.classList.remove('selected'));
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

        // Credit limit check — read the total that renderPricing() already wrote
        const totalInput = dom.totalAmountInput();
        const total = totalInput ? Number(totalInput.value) : 0;

        // user.creditLimit comes from UserSessionDTO.creditLimit() — BigDecimal serialises as number
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

        // Refresh snapshot and total at submit time
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
            // Build form data from all hidden fields
            const formData = new FormData(form);

            // FormData never includes the submit button that triggered the submit
            // — add it manually so the servlet receives action=placeOrder
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
                // Clear cart — order placed
                localStorage.removeItem('rw_cart_items');
                localStorage.removeItem('rw_cart_synced');
                if (window.Cart?.clearLocalStorage) window.Cart.clearLocalStorage();

                window.location.href = data.redirect
                    ?? `${window.CTX ?? ''}/profile#history`;
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