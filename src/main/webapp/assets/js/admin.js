/* ============================================================
   admin.js — Rentawear Admin Dashboard
   ============================================================ */

'use strict';

/* ── Section navigation ─────────────────────────────────────── */
const sectionTitles = {
  products:  { title: 'Inventory Control',    subtitle: 'Manage product pricing, stock levels and availability.' },
  customers: { title: 'Customer Profiles',    subtitle: 'Review and manage customer accounts.' },
  orders:    { title: 'Order History',         subtitle: 'Browse all rental orders placed on the platform.' },
};

document.querySelectorAll('.adm-nav-item').forEach(btn => {
  btn.addEventListener('click', () => {
    const section = btn.dataset.section;

    // Update nav active state
    document.querySelectorAll('.adm-nav-item').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    // Show correct section
    document.querySelectorAll('.adm-section').forEach(s => s.classList.remove('active'));
    document.getElementById(`section-${section}`)?.classList.add('active');

    // Update topbar title
    document.getElementById('pageTitle').textContent    = sectionTitles[section].title;
    document.getElementById('pageSubtitle').textContent = sectionTitles[section].subtitle;

    // Load data for section
    if (section === 'products')  loadProducts();
    if (section === 'customers') loadCustomers();
    if (section === 'orders')    loadOrders();

    // Close sidebar on mobile
    closeSidebar();
  });
});

/* ── Mobile sidebar ─────────────────────────────────────────── */
const sidebar        = document.getElementById('adminSidebar');
const sidebarOverlay = document.getElementById('sidebarOverlay');

document.getElementById('sidebarToggle')?.addEventListener('click', () => {
  sidebar.classList.add('open');
  sidebarOverlay.classList.add('open');
});

sidebarOverlay.addEventListener('click', closeSidebar);

function closeSidebar() {
  sidebar.classList.remove('open');
  sidebarOverlay.classList.remove('open');
}

/* ── Modal helpers ──────────────────────────────────────────── */
function openModal(overlayId) {
  document.getElementById(overlayId).classList.add('open');
}
function closeModal(overlayId) {
  document.getElementById(overlayId).classList.remove('open');
}

// Close modal on overlay click
document.querySelectorAll('.adm-modal-overlay').forEach(overlay => {
  overlay.addEventListener('click', e => {
    if (e.target === overlay) closeModal(overlay.id);
  });
});

/* ── Field error helpers ────────────────────────────────────── */
function showFieldError(inputId, errorId, message) {
  const input = document.getElementById(inputId);
  const error = document.getElementById(errorId);
  if (input) input.classList.add('adm-input--error');
  if (error) { error.textContent = message; error.classList.add('visible'); }
}
function clearFieldError(inputId, errorId) {
  const input = document.getElementById(inputId);
  const error = document.getElementById(errorId);
  if (input) input.classList.remove('adm-input--error');
  if (error) { error.textContent = ''; error.classList.remove('visible'); }
}
function clearAllProductErrors() {
  ['productName','productCategory','productPrice']
    .forEach((_, i, arr) => clearFieldError(arr[i], arr[i] + 'Error'));
}

/* ══════════════════════════════════════════════════════════════
   PRODUCTS
══════════════════════════════════════════════════════════════ */

let currentEditProductId = null;

// Load all products via AJAX
let currentPage     = 0;
const PAGE_SIZE     = 20;
let totalProducts   = 0;

function loadProducts(page = 0) {
  currentPage = page;
  const tbody = document.getElementById('productsTableBody');
  tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4"><span class="spinner-border spinner-border-sm text-secondary"></span></td></tr>';

  let req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');
  req.onreadystatechange = function () {
    if (req.readyState === 4 && req.status === 200) {
      const data = JSON.parse(req.responseText);
      totalProducts = data.total;
      renderProductsTable(data.products);
      renderPagination(data.page, data.total, data.pageSize);
    }
  };
  req.open('GET', CTX + '/admin/products?page=' + page + '&pageSize=' + PAGE_SIZE, true);
  req.send();
}

function renderPagination(page, total, pageSize) {
  const container = document.getElementById('productsPagination');
  if (!container) return;

  const totalPages = Math.ceil(total / pageSize);
  if (totalPages <= 1) {
    container.innerHTML = '';
    return;
  }

  const from = page * pageSize + 1;
  const to   = Math.min((page + 1) * pageSize, total);

  // Build page number buttons — show max 5 pages around current
  let pageButtons = '';
  const start = Math.max(0, page - 2);
  const end   = Math.min(totalPages - 1, page + 2);

  if (start > 0) {
    pageButtons += `<button class="adm-page-btn" onclick="loadProducts(0)">1</button>`;
    if (start > 1) pageButtons += `<span class="adm-page-ellipsis">…</span>`;
  }

  for (let i = start; i <= end; i++) {
    pageButtons += `
      <button class="adm-page-btn ${i === page ? 'active' : ''}"
              onclick="loadProducts(${i})">${i + 1}</button>`;
  }

  if (end < totalPages - 1) {
    if (end < totalPages - 2) pageButtons += `<span class="adm-page-ellipsis">…</span>`;
    pageButtons += `<button class="adm-page-btn" onclick="loadProducts(${totalPages - 1})">${totalPages}</button>`;
  }

  container.innerHTML = `
    <div class="adm-pagination">
      <span class="adm-pagination-info">
        Showing ${from}–${to} of ${total} products
      </span>
      <div class="adm-pagination-btns">
        <button class="adm-page-btn adm-page-btn--nav"
                onclick="loadProducts(${page - 1})"
                ${page === 0 ? 'disabled' : ''}>
          <i class="bi bi-chevron-left"></i>
        </button>
        ${pageButtons}
        <button class="adm-page-btn adm-page-btn--nav"
                onclick="loadProducts(${page + 1})"
                ${page >= totalPages - 1 ? 'disabled' : ''}>
          <i class="bi bi-chevron-right"></i>
        </button>
      </div>
    </div>`;
}

function renderProductsTable(products) {
  const tbody = document.getElementById('productsTableBody');
  if (!products || products.length === 0) {
    tbody.innerHTML = `
      <tr class="adm-table-empty">
        <td colspan="6">
          <i class="bi bi-box-seam"></i>
          <p>No products found</p>
        </td>
      </tr>`;
    return;
  }

  tbody.innerHTML = products.map(p => {
    const core = p.core;
    const inStock = p.inStock;

    return `
    <tr>
      <td>
        <div class="adm-product-cell">
          ${core.imageUrl
            ? `<img src="${core.imageUrl}" alt="${escHtml(core.name)}" class="adm-product-img"/>`
            : `<div class="adm-product-img-placeholder"><i class="bi bi-image"></i></div>`}
          <div>
            <div class="adm-product-name">${escHtml(core.name)}</div>
            ${core.brand ? `<div class="adm-product-brand">${escHtml(core.brand)}</div>` : ''}
          </div>
        </div>
      </td> 
      <td>${escHtml(core.categoryName || '—')}</td>
      <td>$${Number(core.pricePerDay).toFixed(2)} <span style="color:var(--adm-muted);font-size:0.75rem;">/ day</span></td>
      <td>
        <button class="adm-stock-btn" onclick="openStockPopover(event, ${core.id})">
          ${p.totalStock} units
          <i class="bi bi-chevron-down" style="font-size:0.65rem;"></i>
        </button>
      </td>     
      <td>${getStockBadge(inStock)}</td>
      <td>
        <div class="adm-action-btns">
          <button class="adm-icon-btn" title="Edit" onclick="openEditProduct(${core.id})">
            <i class="bi bi-pencil"></i>
          </button>
          <button class="adm-icon-btn adm-icon-btn--danger" title="Delete" onclick="openDeleteProduct(${core.id}, '${escHtml(core.name)}')">
            <i class="bi bi-trash3"></i>
          </button>
        </div>
      </td>
    </tr>`;
  }).join('');
}

function getStockBadge(inStock) {
  if (inStock) {
    return '<span class="adm-badge adm-badge--success">In Stock</span>';
  } else {
    return '<span class="adm-badge adm-badge--danger">Out of Stock</span>';
  }
}

// ── Add product modal ──
document.getElementById('openAddProductModal')?.addEventListener('click', () => {
  currentEditProductId = null;
  document.getElementById('productModalTitle').textContent = 'Add New Product';
  document.getElementById('productForm').reset();
  clearAllProductErrors();
  openModal('productModalOverlay');
});

document.getElementById('closeProductModal')?.addEventListener('click', () => closeModal('productModalOverlay'));
document.getElementById('cancelProductModal')?.addEventListener('click', () => closeModal('productModalOverlay'));

// ── Edit product ──
function openEditProduct(id) {
  currentEditProductId = id;
  document.getElementById('productModalTitle').textContent = 'Edit Product';
  clearAllProductErrors();

  let req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');
  req.onreadystatechange = function () {
    if (req.readyState === 4 && req.status === 200) {
      const p = JSON.parse(req.responseText);
      const core = p.core;
      document.getElementById('productId').value          = core.id;
      document.getElementById('productName').value        = core.name;
      document.getElementById('productCategory').value    = core.categoryId || '';
      document.getElementById('productPrice').value       = core.pricePerDay;
      document.getElementById('productImage').value       = core.imageUrl || '';
      document.getElementById('productDescription').value = p.description || '';
      openModal('productModalOverlay');
    }
  };
  req.open('GET', CTX + '/admin/products/' + id, true);
  req.send();
}

// ── Save product (add or edit) ──
document.getElementById('saveProductBtn')?.addEventListener('click', () => {
  if (!validateProductForm()) return;

  const btn = document.getElementById('saveProductBtn');
  btn.disabled = true;
  btn.querySelector('.adm-btn-text').classList.add('d-none');
  btn.querySelector('.adm-btn-spinner').classList.remove('d-none');

  const payload = JSON.stringify({
    id:          currentEditProductId,
    name:        document.getElementById('productName').value.trim(),
    brand:       document.getElementById('productBrand').value.trim(),
    categoryId:  document.getElementById('productCategory').value,
    rentalPrice: document.getElementById('productPrice').value,
    stockQty:    document.getElementById('productStock').value,
    imageUrl:    document.getElementById('productImage').value.trim(),
    description: document.getElementById('productDescription').value.trim(),
  });

  const url    = currentEditProductId
    ? CTX + '/admin/UpdateProductServlet'
    : CTX + '/admin/AddProductServlet';
  const method = currentEditProductId ? 'PUT' : 'POST';

  let req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');
  req.onreadystatechange = function () {
    if (req.readyState === 4) {
      btn.disabled = false;
      btn.querySelector('.adm-btn-text').classList.remove('d-none');
      btn.querySelector('.adm-btn-spinner').classList.add('d-none');

      if (req.status === 200) {
        closeModal('productModalOverlay');
        loadProducts();
      }
    }
  };
  req.open(method, url, true);
  req.setRequestHeader('Content-Type', 'application/json');
  req.send(payload);
});

// ── Delete product ──
let deleteProductId = null;

function openDeleteProduct(id, name) {
  deleteProductId = id;
  document.getElementById('deleteProductName').textContent = name;
  openModal('deleteModalOverlay');
}

document.getElementById('closeDeleteModal')?.addEventListener('click',  () => closeModal('deleteModalOverlay'));
document.getElementById('cancelDeleteModal')?.addEventListener('click', () => closeModal('deleteModalOverlay'));

document.getElementById('confirmDeleteBtn')?.addEventListener('click', () => {
  if (!deleteProductId) return;

  let req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');
  req.onreadystatechange = function () {
    if (req.readyState === 4 && req.status === 200) {
      closeModal('deleteModalOverlay');
      loadProducts();
    }
  };
  req.open('DELETE', CTX + '/admin/products/' + deleteProductId, true);
  req.send();
});

// ── Product form validation ──
function validateProductForm() {
  let valid = true;
  clearAllProductErrors();

  const name  = document.getElementById('productName').value.trim();
  const cat   = document.getElementById('productCategory').value;
  const price = parseFloat(document.getElementById('productPrice').value);

  if (!name)        { showFieldError('productName',     'productNameError',     'Product name is required.');  valid = false; }
  if (!cat)         { showFieldError('productCategory', 'productCategoryError', 'Please select a category.');  valid = false; }
  if (isNaN(price) || price < 0) { showFieldError('productPrice', 'productPriceError', 'Enter a valid price.'); valid = false; }

  return valid;
}

/* ══════════════════════════════════════════════════════════════
   CUSTOMERS
══════════════════════════════════════════════════════════════ */

function loadCustomers() {
  const tbody = document.getElementById('customersTableBody');
  tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4"><span class="spinner-border spinner-border-sm text-secondary"></span></td></tr>';

  let req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');
  req.onreadystatechange = function () {
    if (req.readyState === 4 && req.status === 200) {
      const data = JSON.parse(req.responseText);
      renderCustomersTable(data.customers);
    }
  };
  req.open('GET', CTX + '/admin/getCustomers', true);
  req.send();
}

function renderCustomersTable(customers) {
  const tbody = document.getElementById('customersTableBody');
  if (!customers || customers.length === 0) {
    tbody.innerHTML = `
      <tr class="adm-table-empty">
        <td colspan="5">
          <i class="bi bi-people"></i>
          <p>No customers found</p>
        </td>
      </tr>`;
    return;
  }

  tbody.innerHTML = customers.map(c => `
    <tr>
      <td>
        <div class="adm-customer-cell">
          <div>
            <div class="adm-customer-name">${escHtml(c.name)}</div>
            <div class="adm-customer-email">${escHtml(c.email)}</div>
          </div>
        </div>
      </td>
      <td>${escHtml(c.job || '—')}</td>
      <td>
        <div class="adm-interests-cell">
          ${(c.interests || []).map(i => `<span class="adm-interest-pill">${escHtml(i)}</span>`).join('')  || '—'}
        </div>
      </td>
      <td>$${Number(c.creditLimit || 0).toLocaleString('en-US', {minimumFractionDigits: 2})}</td>
      <td>
        <div class="adm-action-btns">
        <button class="adm-icon-btn adm-icon-btn--view"
                title="View Profile" onclick="window.location.href='${CTX}/admin/profile?id=${c.id}'">
            <i class="bi bi-person-lines-fill"></i>
          </button>
        </div>
      </td>
    </tr>`).join('');
}

/* ══════════════════════════════════════════════════════════════
   ORDERS
══════════════════════════════════════════════════════════════ */

function loadOrders() {
  const tbody = document.getElementById('ordersTableBody');
  tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4"><span class="spinner-border spinner-border-sm text-secondary"></span></td></tr>';

  let req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');
  req.onreadystatechange = function () {
    if (req.readyState === 4 && req.status === 200) {
      const data = JSON.parse(req.responseText);
      renderOrdersTable(data.orders);
    }
  };
  req.open('GET', CTX + '/admin/getOrders', true);
  req.send();
}

function renderOrdersTable(orders) {
  const tbody = document.getElementById('ordersTableBody');
  if (!orders || orders.length === 0) {
    tbody.innerHTML = `
      <tr class="adm-table-empty">
        <td colspan="6">
          <i class="bi bi-receipt"></i>
          <p>No orders found</p>
        </td>
      </tr>`;
    return;
  }

  tbody.innerHTML = orders.map(o => {
    const firstItem   = o.orderItems && o.orderItems.length > 0 ? o.orderItems[0] : null;
    const productName = firstItem ? escHtml(firstItem.productName) : '—';
    const createdAt   = o.createdAt ? escHtml(o.createdAt) : '—';
    const hasMore     = o.orderItems && o.orderItems.length > 0;

    // Build items detail rows
    const itemsDetail = (o.orderItems || []).map(item => `
      <div class="adm-order-item-row">
        <span class="adm-order-item-name">${escHtml(item.productName)}</span>
        <span class="adm-order-item-meta">
          ${item.color ? `<span class="adm-order-meta-pill">${escHtml(item.color)}</span>` : ''}
          ${item.size  ? `<span class="adm-order-meta-pill">${escHtml(item.size)}</span>`  : ''}
          ${item.quantity ? `<span class="adm-order-meta-pill">Qty: ${item.quantity}</span>` : ''}
          <span class="adm-order-meta-pill">$${Number(item.priceAtPurchase).toFixed(2)}</span>
        </span>
        <span class="adm-order-item-dates">${escHtml(item.startDate)} → ${escHtml(item.endDate)}</span>
      </div>`).join('');

    return `
      <tr class="adm-order-main-row">
        <td><span style="font-family:monospace;font-size:0.8rem;">#${o.id}</span></td>
        <td>
          <div style="font-weight:500;font-size:0.875rem;">${escHtml(o.customerName)}</div>
          <div style="font-size:0.75rem;color:var(--adm-muted);">${escHtml(o.customerEmail)}</div>
        </td>
        <td>
          ${productName}
          ${hasMore ? `<button class="adm-expand-btn" onclick="toggleOrderItems(this)" data-order="${o.id}">
            <i class="bi bi-chevron-down"></i> ${o.orderItems.length} item${o.orderItems.length !== 1 ? 's' : ''}
          </button>` : ''}
        </td>
        <td style="font-size:0.82rem;">${createdAt}</td>
        <td>$${Number(o.totalAmount).toFixed(2)}</td>
        <td>${getOrderBadge(o.status)}</td>
      </tr>
      <tr class="adm-order-detail-row" id="order-detail-${o.id}" style="display:none;">
        <td colspan="6">
          <div class="adm-order-items-wrap">
            <div class="adm-order-items-inner">
              ${itemsDetail}
            </div>
          </div>
        </td>
      </tr>`;
  }).join('');
}

function toggleOrderItems(btn) {
  const orderId   = btn.dataset.order;
  const detailRow = document.getElementById(`order-detail-${orderId}`);
  const wrap      = detailRow.querySelector('.adm-order-items-wrap');
  const icon      = btn.querySelector('i');
  const isOpen    = detailRow.classList.contains('open');

  if (isOpen) {
    wrap.classList.remove('open');
    icon.className = 'bi bi-chevron-down';
    wrap.addEventListener('transitionend', () => {
      detailRow.style.display = 'none';
      detailRow.classList.remove('open');
    }, { once: true });
  } else {
    detailRow.style.display = 'table-row';
    detailRow.classList.add('open');
    icon.className = 'bi bi-chevron-up';
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        wrap.classList.add('open');
      });
    });
  }
}
function getOrderBadge(status) {
  const map = {
    'ORDERED':   'adm-badge--warning',
    'DELIVERED': 'adm-badge--success',
    'SHIPPED':   'adm-badge--warning',
    'CONFIRMED': 'adm-badge--neutral',
    'CANCELLED': 'adm-badge--danger',
  };

  const cls = map[status] || 'adm-badge--neutral';

  return `<span class="adm-badge ${cls}">${status}</span>`;
}

/* ── Utilities ──────────────────────────────────────────────── */
function escHtml(str) {
  return String(str || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

/* ── Stock Popover ──────────────────────────────────────────── */
let activePopover = null;
let activePopoverId = null;
function openStockPopover(event, productId) {
  event.stopPropagation();
  if (activePopoverId === productId) {
    closeStockPopover();
    return;
  }
  closeStockPopover();

  const popover = document.createElement('div');
  popover.className = 'adm-stock-popover';
  popover.id = 'stockPopover';
  popover.innerHTML = `
    <div class="adm-stock-popover-header">
      <span>Stock Breakdown</span>
      <button onclick="closeStockPopover()" class="adm-stock-popover-close">
        <i class="bi bi-x"></i>
      </button>
    </div>
    <div class="adm-stock-popover-body" id="stockPopoverBody">
      <div class="text-center py-3">
        <span class="spinner-border spinner-border-sm text-secondary"></span>
      </div>
    </div>`;

  document.body.appendChild(popover);

  // ── Smart positioning ──────────────────────────────────────
  const btnRect     = event.currentTarget.getBoundingClientRect();
  const popW        = 280;
  const popH        = 340; // estimated max height
  const scrollY     = window.scrollY;
  const scrollX     = window.scrollX;
  const vpW         = window.innerWidth;
  const vpH         = window.innerHeight;

  // Horizontal: prefer left-aligned to button, flip if off-screen
  let left = btnRect.left + scrollX;
  if (left + popW > vpW + scrollX - 12) {
    left = btnRect.right + scrollX - popW;
  }
  left = Math.max(scrollX + 8, left); // never off left edge

  // Vertical: prefer below button, flip above if not enough room
  let top;
  const spaceBelow = vpH - btnRect.bottom;
  const spaceAbove = btnRect.top;
  if (spaceBelow >= popH || spaceBelow >= spaceAbove) {
    top = btnRect.bottom + scrollY + 6;
  } else {
    top = btnRect.top + scrollY - popH - 6;
  }
  top = Math.max(scrollY + 8, top); // never off top edge

  popover.style.left = left + 'px';
  popover.style.top  = top  + 'px';

  activePopover = popover;
  activePopoverId = productId;

  // Fetch stock detail
  let req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');
  req.onreadystatechange = function () {
    if (req.readyState === 4 && req.status === 200) {
      const data = JSON.parse(req.responseText);
      renderStockPopover(data.stockByColor);

      // Re-adjust vertical position after content loads (real height now known)
      requestAnimationFrame(() => {
        const realH       = popover.offsetHeight;
        const spaceBelow2 = vpH - btnRect.bottom;
        if (spaceBelow2 < realH && btnRect.top > realH) {
          popover.style.top = (btnRect.top + scrollY - realH - 6) + 'px';
        }
      });
    }
  };
  req.open('GET', CTX + '/admin/products/' + productId, true);
  req.send();
}

function renderStockPopover(stockByColor) {
  const body = document.getElementById('stockPopoverBody');
  if (!body) return;

  if (!stockByColor || Object.keys(stockByColor).length === 0) {
    body.innerHTML = '<p class="adm-stock-empty">No variants found.</p>';
    return;
  }

  body.innerHTML = Object.entries(stockByColor).map(([color, variants]) => `
    <div class="adm-stock-color-group">
      <div class="adm-stock-color-header">
        <span class="adm-stock-color-swatch" style="background:${isHexColor(color) ? color : '#ccc'}"></span>
        <span class="adm-stock-color-name">${escHtml(color)}</span>
      </div>
      <div class="adm-stock-sizes">
        ${variants.map(v => `
          <div class="adm-stock-size-row">
            <span class="adm-stock-size-label">Size ${escHtml(v.size || '—')}</span>
            <span class="adm-stock-size-qty ${v.quantity === 0 ? 'adm-stock-qty--zero' : v.quantity <= 3 ? 'adm-stock-qty--low' : 'adm-stock-qty--ok'}">
              ${v.quantity}
            </span>
          </div>`).join('')}
      </div>
    </div>`).join('');
}

function isHexColor(str) {
  return /^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$/.test(str);
}

function closeStockPopover() {
  if (activePopover) {
    activePopover.remove();
    activePopover   = null;
    activePopoverId = null;
  }
}

// Close popover when clicking outside
document.addEventListener('click', (e) => {
  if (activePopover && !activePopover.contains(e.target)) {
    closeStockPopover();
  }
});
/* ── Init: load products on page load ───────────────────────── */
loadProducts();
