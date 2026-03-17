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
  ['productName','productBrand','productCategory','productPrice','productStock']
    .forEach((_, i, arr) => clearFieldError(arr[i], arr[i] + 'Error'));
}

/* ══════════════════════════════════════════════════════════════
   PRODUCTS
══════════════════════════════════════════════════════════════ */

let currentEditProductId = null;

// Load all products via AJAX
function loadProducts() {
  const tbody = document.getElementById('productsTableBody');
  tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4"><span class="spinner-border spinner-border-sm text-secondary"></span></td></tr>';

  let req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');
  req.onreadystatechange = function () {
    if (req.readyState === 4 && req.status === 200) {
      const data = JSON.parse(req.responseText);
      renderProductsTable(data.products);
    }
  };
  req.open('GET', CTX + '/admin/GetProductsServlet', true);
  req.send();
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

  tbody.innerHTML = products.map(p => `
    <tr>
      <td>
        <div class="adm-product-cell">
          ${p.imageUrl
            ? `<img src="${p.imageUrl}" alt="${escHtml(p.name)}" class="adm-product-img"/>`
            : `<div class="adm-product-img-placeholder"><i class="bi bi-image"></i></div>`}
          <div>
            <div class="adm-product-name">${escHtml(p.name)}</div>
          </div>
        </div>
      </td>
      <td>${escHtml(p.categoryName || '—')}</td>
      <td>$${Number(p.basePrice).toFixed(2)} <span style="color:var(--adm-muted);font-size:0.75rem;">/ day</span></td>
      <td>${p.totalStock}</td>
      <td>${getStockBadge(p.totalStock)}</td>
      <td>
        <div class="adm-action-btns">
          <button class="adm-icon-btn" title="Edit" onclick="openEditProduct(${p.id})">
            <i class="bi bi-pencil"></i>
          </button>
          <button class="adm-icon-btn adm-icon-btn--danger" title="Delete" onclick="openDeleteProduct(${p.id}, '${escHtml(p.name)}')">
            <i class="bi bi-trash3"></i>
          </button>
        </div>
      </td>
    </tr>`).join('');
}

function getStockBadge(qty) {
  if (qty <= 0)  return '<span class="adm-badge adm-badge--danger">Out of Stock</span>';
  if (qty <= 3)  return '<span class="adm-badge adm-badge--warning">Low Stock</span>';
  return '<span class="adm-badge adm-badge--success">In Stock</span>';
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
      document.getElementById('productId').value          = p.id;
      document.getElementById('productName').value        = p.name;
      document.getElementById('productBrand').value       = p.brand || '';
      document.getElementById('productCategory').value    = p.categoryId || '';
      document.getElementById('productPrice').value       = p.rentalPrice;
      document.getElementById('productStock').value       = p.stockQty;
      document.getElementById('productImage').value       = p.imageUrl || '';
      document.getElementById('productDescription').value = p.description || '';
      openModal('productModalOverlay');
    }
  };
  req.open('GET', CTX + '/admin/GetProductServlet?id=' + id, true);
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
  req.open('DELETE', CTX + '/admin/DeleteProductServlet?id=' + deleteProductId, true);
  req.send();
});

// ── Product form validation ──
function validateProductForm() {
  let valid = true;
  clearAllProductErrors();

  const name  = document.getElementById('productName').value.trim();
  const brand = document.getElementById('productBrand').value.trim();
  const cat   = document.getElementById('productCategory').value;
  const price = parseFloat(document.getElementById('productPrice').value);
  const stock = parseInt(document.getElementById('productStock').value);

  if (!name)        { showFieldError('productName',     'productNameError',     'Product name is required.');  valid = false; }
  if (!brand)       { showFieldError('productBrand',    'productBrandError',    'Brand is required.');          valid = false; }
  if (!cat)         { showFieldError('productCategory', 'productCategoryError', 'Please select a category.');  valid = false; }
  if (isNaN(price) || price < 0) { showFieldError('productPrice', 'productPriceError', 'Enter a valid price.'); valid = false; }
  if (isNaN(stock) || stock < 0) { showFieldError('productStock', 'productStockError', 'Enter a valid quantity.'); valid = false; }

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
          <button class="adm-icon-btn adm-icon-btn--view" title="View Profile"
                  onclick="window.location.href='${CTX}/admin/CustomerProfileServlet?id=${c.id}'">
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
    const period      = firstItem
        ? `${escHtml(firstItem.startDate)} → ${escHtml(firstItem.endDate)}`
        : '—';
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
        <td style="font-size:0.82rem;">${period}</td>
        <td>$${Number(o.totalAmount).toFixed(2)}</td>
        <td>${getOrderBadge(o.status)}</td>
      </tr>
      <tr class="adm-order-detail-row" id="order-detail-${o.id}" style="display:none;">
        <td colspan="6">
          <div class="adm-order-items-wrap">
            ${itemsDetail}
          </div>
        </td>
      </tr>`;
  }).join('');
}

function toggleOrderItems(btn) {
  const orderId   = btn.dataset.order;
  const detailRow = document.getElementById(`order-detail-${orderId}`);
  const icon      = btn.querySelector('i');
  const isOpen    = detailRow.style.display !== 'none';

  detailRow.style.display = isOpen ? 'none' : 'table-row';
  icon.className = isOpen ? 'bi bi-chevron-down' : 'bi bi-chevron-up';
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


/* ── Init: load products on page load ───────────────────────── */
loadProducts();
