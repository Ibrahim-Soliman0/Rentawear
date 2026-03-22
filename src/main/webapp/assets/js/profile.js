/* ================================================================
   profile.js  —  Rentawear account page
   Context path injected by profile.jsp before this script loads:
       <script>window.RW_CTX = '${pageContext.request.contextPath}';</script>
   ================================================================ */
/* ══════════════════════════════════════════════════════════════
   STYLE INTERESTS TAG WIDGET
   ══════════════════════════════════════════════════════════════ */
(function () {
    const select     = document.getElementById('styleInterestsSelect');
    const tagsWrap   = document.getElementById('styleTagsWrap');
    const hiddenWrap = document.getElementById('styleHiddenInputs');
    const selected   = new Set();

    function removeTag(val) {
        selected.delete(val);
        tagsWrap?.querySelector(`[data-val="${val}"]`)?.remove();
        document.getElementById(`si_${val}`)?.remove();
    }

    window.addInterestTag = function (val, label) {
        if (!tagsWrap) return;
        if (selected.has(val)) return;
        selected.add(val);

        const tag = document.createElement('span');
        tag.className   = 'rw-tag';
        tag.dataset.val = val;

        if (select) {
            // Editable mode — show X and allow removal
            tag.innerHTML = `${label} <i class="bi bi-x"></i>`;
            tag.addEventListener('click', () => removeTag(val));
        } else {
            // Read-only mode — plain tag, no interaction
            tag.innerHTML  = label;
            tag.style.cursor = 'default';
            tag.style.pointerEvents = 'none';
        }

        tagsWrap.appendChild(tag);

        // Only add hidden input in editable mode
        if (hiddenWrap && select) {
            const input = document.createElement('input');
            input.type  = 'hidden';
            input.name  = 'styleInterests';
            input.value = val;
            input.id    = `si_${val}`;
            hiddenWrap.appendChild(input);
        }
    };

    // Dropdown change handler — only in editable mode
    if (select) {
        select.addEventListener('change', () => {
            const val   = select.value;
            const label = select.options[select.selectedIndex].text;
            select.value = '';
            window.addInterestTag(val, label);
        });
    }
})();
/* ══════════════════════════════════════════════════════════════
   POPULATE PAGE FROM RW_USER
   ══════════════════════════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', function () {
    const u = window.RW_USER;
    if (!u) return;

    /* Sidebar */
    setText('sidebarName', u.name || '');
    setText('sidebarEmail', u.email || '');

    /* Avatar card name */
    setText('avatarName', u.name || '');

    /* Profile form fields */
    setValue('fullName', u.name || '');
    setValue('email', u.email || '');
    setValue('dob', u.birthday || '');
    setValue('jobTitle', u.job || '');
    setValue('address', u.address || '');

    /* Interests: pre-fill existing tags from RW_USER */
    if (u.interests && u.interests.length) {
        u.interests.forEach(i => {
            if (i.categoryId != null && i.categoryName) {
                window.addInterestTag(String(i.categoryId), i.categoryName);
            }
        });
    }

    /* Credit limit */
    const limit = u.creditLimit != null ? parseFloat(u.creditLimit) : 0;
    setText('creditLimitDisplay', limit.toFixed(2));
    const bar = document.getElementById('creditBar');
    if (bar) bar.style.width = Math.min((limit / 2000) * 100, 100) + '%';

    /* Payment cards */
    renderCards(u.paymentCards || []);

    /* Orders */
    loadOrders();
});

function setText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
}

function setValue(id, val) {
    const el = document.getElementById(id);
    if (el) {
        el.value = val;
        el.defaultValue = val;
    }
}

/* ══════════════════════════════════════════════════════════════
   CARD RENDERING
   ══════════════════════════════════════════════════════════════ */
function renderCards(cards) {
    const list = document.getElementById('cardList');
    const empty = document.getElementById('noCardsState');
    if (!list) return;
    list.innerHTML = '';
    if (!cards.length) {
        if (empty) empty.style.display = 'flex';
        return;
    }
    if (empty) empty.style.display = 'none';
    cards.forEach(card => list.appendChild(buildCardRow(card)));
}

function buildCardRow(card) {
    const last4 = (card.cardNumber || '').slice(-4);
    const typeKey = (card.cardType || 'OTHER').toUpperCase();
    const row = document.createElement('div');
    row.className = 'account-section account-card-row';
    row.id = 'card-row-' + card.id;
    row.innerHTML = `
        <div class="account-card-icon">${cardBadgeHtml(typeKey)}</div>
        <div class="account-card-details">
            <p class="account-card-number">&bull;&bull;&bull;&bull; &bull;&bull;&bull;&bull; &bull;&bull;&bull;&bull; ${last4}</p>
            <p class="account-card-expiry">Expires ${card.expiryMonth}/${card.expiryYear}</p>
        </div>
        <button class="account-card-remove" type="button"
                onclick="removeCard(${card.id})"
                aria-label="Remove card ending ${last4}">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                <path d="M10 11v6M14 11v6"/>
                <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
            </svg>
        </button>`;
    return row;
}

function cardBadgeHtml(typeKey) {
    if (typeKey === 'VISA') {
        return '<span class="account-card-badge account-card-badge--visa">VISA</span>';
    }
    if (typeKey === 'MASTERCARD') {
        return '<span class="account-card-badge account-card-badge--mc">'
            + '<span class="mc-circle mc-circle--left"></span>'
            + '<span class="mc-circle mc-circle--right"></span>'
            + '</span>';
    }
    return '<span class="account-card-badge account-card-badge--other">'
        + '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">'
        + '<rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/>'
        + '</svg></span>';
}

/* ══════════════════════════════════════════════════════════════
   TAB SWITCHING
   ══════════════════════════════════════════════════════════════ */
const BREADCRUMBS = {
    profile: 'Edit Profile',
    history: 'Rent History',
    billing: 'Billing &amp; Limits'
};

function switchTab(name) {
    document.querySelectorAll('.account-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.account-nav-link').forEach(l => l.classList.remove('active'));

    const tab = document.getElementById('tab-' + name);
    const nav = document.getElementById('nav-' + name);
    if (tab) tab.classList.add('active');
    if (nav) nav.classList.add('active');

    const bc = document.getElementById('breadcrumb-current');
    if (bc) bc.innerHTML = BREADCRUMBS[name] || name;
}

if (location.hash === '#billing') switchTab('billing');

if (location.hash === '#history') switchTab('history');

/* ══════════════════════════════════════════════════════════════
   AVATAR PREVIEW
   ══════════════════════════════════════════════════════════════ */
function previewAvatar(e) {
    const file = e.target.files[0];
    if (!file) return;
    if (file.size > 800 * 1024) {
        alert('Max size is 800 KB.');
        return;
    }
    const reader = new FileReader();
    reader.onload = ev => {
        document.getElementById('avatarPreviewMain').src = ev.target.result;
        document.getElementById('avatarPreview').src = ev.target.result;
    };
    reader.readAsDataURL(file);
}

function removeAvatar() {
    const def = window.RW_AVATAR;
    document.getElementById('avatarPreviewMain').src = def;
    document.getElementById('avatarPreview').src = def;
    document.getElementById('avatarInput').value = '';
}

/* ══════════════════════════════════════════════════════════════
   SAVE PROFILE
   ══════════════════════════════════════════════════════════════ */
function saveProfile() {
    const payload = {
        name: document.getElementById('fullName').value.trim(),
        email: document.getElementById('email').value.trim(),
        birthday: document.getElementById('dob').value,
        job: document.getElementById('jobTitle').value.trim(),
        address: document.getElementById('address').value.trim(),
        interests: getSelectedInterestIds(),
        currentPassword: document.getElementById('currentPassword').value,
        newPassword: document.getElementById('newPassword').value,
        confirmPassword: document.getElementById('confirmPassword').value
    };

    if (!payload.name) {
        showProfileError('Full name is required.');
        return;
    }
    if (!payload.email) {
        showProfileError('Email address is required.');
        return;
    }

    const btn = document.querySelector('#tab-profile .btn-rw-primary');
    btn.disabled = true;
    btn.textContent = 'Saving…';

    fetch(CTX + '/profile/update', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                window.RW_USER.name = payload.name;
                window.RW_USER.email = payload.email;
                window.RW_USER.birthday = payload.birthday;
                window.RW_USER.job = payload.job;
                window.RW_USER.address = payload.address;

                setText('sidebarName', payload.name);
                setText('sidebarEmail', payload.email);
                setText('avatarName', payload.name);

                setValue('currentPassword', '');
                setValue('newPassword', '');
                setValue('confirmPassword', '');

                showProfileSuccess('Profile updated successfully.');
            } else {
                showProfileError(data.message || 'Could not save profile. Please try again.');
            }
        })
        .catch(() => showProfileError('Network error. Please try again.'))
        .finally(() => {
            btn.disabled = false;
            btn.textContent = 'Save Changes';
        });
}

function resetForm() {
    document.querySelectorAll('#tab-profile input, #tab-profile select')
        .forEach(el => el.value = el.defaultValue);
    clearProfileMessages();
}

function getSelectedInterestIds() {
    return Array.from(
        document.querySelectorAll('#styleHiddenInputs input[name="styleInterests"]')
    ).map(el => parseInt(el.value)).filter(v => !isNaN(v));
}

function showProfileError(msg) {
    clearProfileMessages();
    const el = getOrCreateProfileMessage('profile-error-msg', 'account-form-error');
    el.textContent = msg;
    el.style.display = 'block';
    el.scrollIntoView({behavior: 'smooth', block: 'nearest'});
}

function showProfileSuccess(msg) {
    clearProfileMessages();
    const el = getOrCreateProfileMessage('profile-success-msg', 'account-form-success');
    el.textContent = msg;
    el.style.display = 'block';
    el.scrollIntoView({behavior: 'smooth', block: 'nearest'});
}

function clearProfileMessages() {
    ['profile-error-msg', 'profile-success-msg'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'none';
    });
}

function getOrCreateProfileMessage(id, className) {
    let el = document.getElementById(id);
    if (!el) {
        el = document.createElement('p');
        el.id = id;
        el.className = className;
        el.style.display = 'none';
        const actions = document.querySelector('.account-form-actions');
        if (actions) actions.parentNode.insertBefore(el, actions);
    }
    return el;
}

/* ══════════════════════════════════════════════════════════════
   ADD CARD MODAL
   ══════════════════════════════════════════════════════════════ */
function openAddCardModal() {
    document.getElementById('addCardBackdrop').classList.add('active');
    document.getElementById('addCardModal').classList.add('active');
    document.getElementById('addCardError').style.display = 'none';

    document.getElementById('newCardNumber').value = '';
    document.getElementById('newCardholderName').value = '';
    document.getElementById('newCardMonth').value = '';
    document.getElementById('newCardYear').value = '';
    document.getElementById('newCardCvv').value = '';

    updatePreview();
    document.getElementById('newCardNumber').focus();
}

function closeAddCardModal() {
    document.getElementById('addCardBackdrop').classList.remove('active');
    document.getElementById('addCardModal').classList.remove('active');
}

(function populateYears() {
    const sel = document.getElementById('newCardYear');
    if (!sel) return;
    const base = new Date().getFullYear();
    for (let i = 0; i <= 12; i++) {
        const yr = base + i;
        const opt = document.createElement('option');
        opt.value = String(yr).slice(-2);
        opt.textContent = yr;
        sel.appendChild(opt);
    }
})();

function formatCardNumber(input) {
    let v = input.value.replace(/\D/g, '').slice(0, 16);
    input.value = v.replace(/(.{4})/g, '$1 ').trim();
}

function detectCardType(raw) {
    if (/^4/.test(raw)) return 'VISA';
    if (/^5[1-5]/.test(raw)) return 'MASTERCARD';
    if (/^3[47]/.test(raw)) return 'AMEX';
    return 'OTHER';
}

function updatePreview() {
    const raw = document.getElementById('newCardNumber').value.replace(/\D/g, '');
    const month = document.getElementById('newCardMonth').value;
    const year = document.getElementById('newCardYear').value;
    const cardholderName = document.getElementById('newCardholderName').value.trim();
    const type = detectCardType(raw);

    let display = '•••• •••• •••• ••••';
    if (raw.length > 0) {
        display = raw.padEnd(16, '•').replace(/(.{4})/g, '$1 ').trim();
    }
    document.getElementById('previewNumber').textContent = display;
    document.getElementById('previewExpiry').textContent = (month || 'MM') + ' / ' + (year || 'YY');
    document.getElementById('previewName').textContent = cardholderName || 'FULL NAME';

    const typeEl = document.getElementById('previewType');
    if (type === 'VISA') {
        typeEl.innerHTML = '<span style="font-weight:800;font-size:18px;letter-spacing:1px;color:#fff;">VISA</span>';
    } else if (type === 'MASTERCARD') {
        typeEl.innerHTML = '<span style="display:inline-flex;">'
            + '<span style="width:22px;height:22px;border-radius:50%;background:rgba(235,0,27,0.85);display:inline-block;"></span>'
            + '<span style="width:22px;height:22px;border-radius:50%;background:rgba(247,158,27,0.85);display:inline-block;margin-left:-8px;"></span>'
            + '</span>';
    } else {
        typeEl.innerHTML = '';
    }

    document.getElementById('cardPreview').className =
        'account-card-preview account-card-preview--' + type.toLowerCase();
}

function saveCard() {
    const raw = document.getElementById('newCardNumber').value.replace(/\D/g, '');
    const cardholderName = document.getElementById('newCardholderName').value.trim();
    const month = document.getElementById('newCardMonth').value;
    const year = document.getElementById('newCardYear').value;
    const cvv = document.getElementById('newCardCvv').value.trim();

    if (raw.length < 13) {
        showCardError('Please enter a valid card number.');
        return;
    }
    if (!cardholderName) {
        showCardError('Please enter the cardholder name.');
        return;
    }
    if (!month) {
        showCardError('Please select the expiry month.');
        return;
    }
    if (!year) {
        showCardError('Please select the expiry year.');
        return;
    }
    if (cvv.length < 3) {
        showCardError('Please enter a valid CVV.');
        return;
    }

    document.getElementById('addCardError').style.display = 'none';

    const btn = document.getElementById('saveCardBtn');
    btn.disabled = true;
    btn.textContent = 'Saving…';

    fetch(CTX + '/profile/cards/add', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: 'cardNumber=' + encodeURIComponent(raw)
            + '&cardholderName=' + encodeURIComponent(cardholderName)
            + '&expiryMonth=' + encodeURIComponent(month)
            + '&expiryYear=' + encodeURIComponent(year)
            + '&cvv=' + encodeURIComponent(cvv)
            + '&cardType=' + encodeURIComponent(detectCardType(raw))
    })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                window.RW_USER.paymentCards = window.RW_USER.paymentCards || [];
                window.RW_USER.paymentCards.push(data.card);
                renderCards(window.RW_USER.paymentCards);
                closeAddCardModal();
            } else {
                showCardError(data.message || 'Could not save card. Please try again.');
            }
        })
        .catch(() => showCardError('Network error. Please try again.'))
        .finally(() => {
            btn.disabled = false;
            btn.textContent = 'Save Card';
        });
}

function showCardError(msg) {
    const err = document.getElementById('addCardError');
    err.textContent = msg;
    err.style.display = 'block';
}

function removeCard(cardId) {
    if (!confirm('Remove this card from your account?')) return;

    fetch(CTX + '/profile/cards/remove', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: 'cardId=' + encodeURIComponent(cardId)
    })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                window.RW_USER.paymentCards = (window.RW_USER.paymentCards || [])
                    .filter(c => c.id !== cardId);
                renderCards(window.RW_USER.paymentCards);
            } else {
                alert(data.message || 'Could not remove card. Please try again.');
            }
        })
        .catch(() => alert('Network error. Please try again.'));
}

/* ══════════════════════════════════════════════════════════════
   RENT HISTORY — ORDERS
   ══════════════════════════════════════════════════════════════ */

const ORDER_STEPS = ['ORDERED', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'RETURNED'];

const STATUS_LABELS = {
    ORDERED: 'Ordered',
    CONFIRMED: 'Confirmed',
    SHIPPED: 'Shipped',
    DELIVERED: 'Delivered',
    RETURNED: 'Returned to Shop',
    CANCELLED: 'Cancelled'
};

/* ── Fetch orders — shows real server error message if it fails ── */
function loadOrders() {
    const userId = window.RW_VIEW_USER_ID;
    const url    = userId
        ? CTX + '/admin/profile/orders?userId=' + userId
        : CTX + '/profile/orders';

    fetch(url, { method: 'GET', headers: { 'Accept': 'application/json' } })
        .then(r => {
            if (!r.ok) return r.json().then(err => { throw new Error(err.message || 'Server error ' + r.status); });
            return r.json();
        })
        .then(data => {
            renderActiveOrders(data.active || []);
            renderPastOrders(data.past  || []);
        })
        .catch(err => {
            const active = document.getElementById('activeOrdersList');
            const past   = document.getElementById('pastOrdersList');
            if (active) active.innerHTML = '<p class="account-orders-error">' + escHtml(err.message) + '</p>';
            if (past)   past.innerHTML   = '';
        });
}

/* ── Active orders ── */
function renderActiveOrders(orders) {
    const container = document.getElementById('activeOrdersList');
    if (!container) return;

    if (!orders.length) {
        container.innerHTML =
            '<div class="account-empty-state">' +
            '<svg width="36" height="36" viewBox="0 0 24 24" fill="none" ' +
            'stroke="var(--rw-stone)" stroke-width="1.5" aria-hidden="true">' +
            '<path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>' +
            '<line x1="3" y1="6" x2="21" y2="6"/>' +
            '<path d="M16 10a4 4 0 0 1-8 0"/></svg>' +
            '<p class="account-empty-label">No active rentals right now</p>' +
            '<a href="' + CTX + '/products" class="btn-rw-primary btn-rw--sm">Browse Collection</a>' +
            '</div>';
        return;
    }

    container.innerHTML = orders.map(o => buildOrderCard(o)).join('');
}

/* ── Past orders ── */
function renderPastOrders(orders) {
    const container = document.getElementById('pastOrdersList');
    if (!container) return;

    if (!orders.length) {
        container.innerHTML =
            '<div class="account-empty-state">' +
            '<p class="account-empty-label">No past rentals yet</p>' +
            '</div>';
        return;
    }

    container.innerHTML = orders.map(o => buildOrderCard(o)).join('');
}

/* ── Build one order card ── */
function buildOrderCard(order) {
    const isCancelled = order.status === 'CANCELLED';
    const statusLabel = STATUS_LABELS[order.status] || order.status;

    const badgeClass = {
        ORDERED: 'account-status--ordered',
        CONFIRMED: 'account-status--confirmed',
        SHIPPED: 'account-status--shipped',
        DELIVERED: 'account-status--delivered',
        RETURNED: 'account-status--returned',
        CANCELLED: 'account-status--cancelled'
    }[order.status] || '';

    const itemsHtml = (order.items || []).map(item => `
        <div class="account-rental-card">
            <div class="account-rental-img">
                <img src="${escHtml(item.imageUrl || '')}"
                     alt="${escHtml(item.productName)}"
                     onerror="this.src='https://placehold.co/80x110/EDE9E3/9E9189?text=Item'"/>
            </div>
            <div class="account-rental-body">
                <div class="account-rental-top">
                    <div>
                        <p class="account-rental-name">${escHtml(item.productName)}</p>
                        <p class="account-rental-sub">
                            ${escHtml(item.color)} &bull; Size ${escHtml(item.size || '—')}
                            &bull; Qty ${item.quantity}
                        </p>
                    </div>
                </div>
                <div class="account-rental-meta">
                    <span>
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" stroke-width="2" aria-hidden="true">
                            <rect x="3" y="4" width="18" height="18" rx="2"/>
                            <line x1="3" y1="10" x2="21" y2="10"/>
                        </svg>
                        ${formatDate(item.startDate)} – ${formatDate(item.endDate)}
                    </span>
                    <span>
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" stroke-width="2" aria-hidden="true">
                            <rect x="2" y="5" width="20" height="14" rx="2"/>
                            <line x1="2" y1="10" x2="22" y2="10"/>
                        </svg>
                        £${item.priceAtPurchase}
                    </span>
                </div>
            </div>
        </div>
    `).join('');

    const progressHtml = isCancelled ? buildCancelledBanner() : buildStatusBar(order.status);

    return `
        <div class="account-section account-section--rental account-order-card" id="order-${order.id}">
            <div class="account-order-header">
                <div class="account-order-meta">
                    <span class="account-order-id"># ORD-${order.id}</span>
                    <span class="account-order-date">${formatDate(order.createdAt)}</span>
                    <span class="account-order-total">£${order.totalAmount}</span>
                </div>
                <span class="account-status ${badgeClass}">${statusLabel}</span>
            </div>
            ${progressHtml}
            <div class="account-order-items">${itemsHtml}</div>
        </div>`;
}

/* ── Status progress bar ── */
function buildStatusBar(currentStatus) {
    const currentIdx = ORDER_STEPS.indexOf(currentStatus);

    const stepsHtml = ORDER_STEPS.map((step, idx) => {
        let cls = 'account-status-step';
        if (idx < currentIdx) cls += ' completed';
        if (idx === currentIdx) cls += ' active';

        const check = idx < currentIdx
            ? '<svg width="10" height="10" viewBox="0 0 24 24" fill="none" ' +
            'stroke="currentColor" stroke-width="3">' +
            '<polyline points="20 6 9 17 4 12"/></svg>'
            : '';

        return `<div class="${cls}">
                    <div class="account-status-dot">${check}</div>
                    <span class="account-status-step-label">${STATUS_LABELS[step]}</span>
                </div>`;
    }).join('');

    const fillPct = currentIdx <= 0
        ? 0
        : Math.round((currentIdx / (ORDER_STEPS.length - 1)) * 100);

    return `<div class="account-status-bar">
                <div class="account-status-track">
                    <div class="account-status-fill" style="width:${fillPct}%"></div>
                </div>
                <div class="account-status-steps">${stepsHtml}</div>
            </div>`;
}

/* ── Cancelled banner (replaces progress bar for cancelled orders) ── */
function buildCancelledBanner() {
    return `<div class="account-cancelled-banner">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="15" y1="9" x2="9" y2="15"/>
                    <line x1="9" y1="9" x2="15" y2="15"/>
                </svg>
                This order was cancelled.
            </div>`;
}

/* ── Helpers ── */
function formatDate(dateStr) {
    if (!dateStr) return '—';
    /* Parse as local date to avoid timezone shift */
    const [y, m, d] = dateStr.split('-').map(Number);
    return new Date(y, m - 1, d)
        .toLocaleDateString('en-GB', {day: 'numeric', month: 'short', year: 'numeric'});
}

function escHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

