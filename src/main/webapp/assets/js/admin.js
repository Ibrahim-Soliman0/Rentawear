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
        ? `<img
       src="${imgUrl(core.imageUrl, 'sm')}"
       srcset="${imgUrl(core.imageUrl, 'sm')} 400w, ${imgUrl(core.imageUrl, 'md')} 800w"
       sizes="60px"
       alt="${escHtml(core.name)}"
       class="adm-product-img"
       loading="lazy"
       decoding="async"/>`
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


/* ── Color/hex picker sync ──────────────────────────────────── */
document.getElementById('newColorHex')?.addEventListener('input', function () {
  document.getElementById('newColorHexText').value = this.value.toUpperCase();
});
document.getElementById('newColorHexText')?.addEventListener('input', function () {
  if (/^#[0-9A-Fa-f]{6}$/.test(this.value)) {
    document.getElementById('newColorHex').value = this.value;
  }
});

/* ── Open Add modal ─────────────────────────────────────────── */
document.getElementById('openAddProductModal')?.addEventListener('click', () => {
  currentEditProductId = null;
  document.getElementById('saveProductBtn')
      .querySelector('.adm-btn-text').textContent = 'Save Product';
  document.getElementById('productModalTitle').textContent = 'Add New Product';
  document.getElementById('productForm').reset();
  document.getElementById('variantColorGroups').innerHTML = '';
  document.getElementById('variantsEmptyHint').style.display = 'block';
  clearAllProductErrors();
  // Reset to first tab
  document.querySelectorAll('.adm-modal-tab').forEach((t, i) => t.classList.toggle('active', i === 0));
  document.querySelectorAll('.adm-tab-panel').forEach((p, i) => p.classList.toggle('active', i === 0));
  // Disable variants tab until product saved
  document.getElementById('variantsTabBtn').disabled = false;
  openModal('productModalOverlay');
});

document.getElementById('closeProductModal')?.addEventListener('click', () => {
  closeModal('productModalOverlay');
  // Reset button text and banner
  document.getElementById('saveProductBtn').querySelector('.adm-btn-text').textContent = 'Save Product';
  const banner = document.getElementById('modalBanner');
  if (banner) banner.style.display = 'none';
});
document.getElementById('cancelProductModal')?.addEventListener('click', () => {
  closeModal('productModalOverlay');
  document.getElementById('saveProductBtn').querySelector('.adm-btn-text').textContent = 'Save Product';
  const banner = document.getElementById('modalBanner');
  if (banner) banner.style.display = 'none';
});

/* ── Open Edit modal ────────────────────────────────────────── */
function openEditProduct(id) {
  currentEditProductId = id;
  document.getElementById('productModalTitle').textContent = 'Edit Product';
  document.getElementById('saveProductBtn')
      .querySelector('.adm-btn-text').textContent = 'Save Changes';
  clearAllProductErrors();
  document.getElementById('variantColorGroups').innerHTML = '';
  document.getElementById('variantsEmptyHint').style.display = 'block';
  // Reset to first tab
  document.querySelectorAll('.adm-modal-tab').forEach((t, i) => t.classList.toggle('active', i === 0));
  document.querySelectorAll('.adm-tab-panel').forEach((p, i) => p.classList.toggle('active', i === 0));
  // Enable variants tab for edit
  document.getElementById('variantsTabBtn').disabled = false;

  let req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');
  req.onreadystatechange = function () {
    if (req.readyState === 4 && req.status === 200) {
      const data = JSON.parse(req.responseText);
      const core = data.core;

      document.getElementById('productId').value          = core.id;
      document.getElementById('productName').value        = core.name;
      document.getElementById('productCategory').value    = core.categoryId || '';
      document.getElementById('productPrice').value       = core.pricePerDay;
      document.getElementById('productImage').value       = core.imageUrl || '';
      document.getElementById('productDescription').value = data.description || '';

      // Populate variants
      if (data.stockByColor && Object.keys(data.stockByColor).length > 0) {
        document.getElementById('variantsEmptyHint').style.display = 'none';
        Object.entries(data.stockByColor).forEach(([colorKey, variants]) => {
          // colorKey format: "#HEX-Color Name" e.g. "#F4C2C2-Blush Pink"
          const dashIdx  = colorKey.indexOf('-');
          const hex      = dashIdx > -1 ? colorKey.substring(0, dashIdx) : '#cccccc';
          const colorName = dashIdx > -1 ? colorKey.substring(dashIdx + 1) : colorKey;
          addColorGroup(colorName, hex, variants);
        });
      }

      openModal('productModalOverlay');
    }
  };
  req.open('GET', CTX + '/admin/products/' + id, true);
  req.send();
}
/* ── Variant color group management ─────────────────────────── */
let colorGroupCounter = 0;

function addColorGroup(colorName, hex, existingVariants) {
  colorName = colorName || document.getElementById('newColorName').value.trim();
  hex       = hex       || document.getElementById('newColorHex').value;

  if (!colorName) {
    document.getElementById('newColorName').focus();
    return;
  }

  const groupId = 'cg-' + (colorGroupCounter++);
  document.getElementById('variantsEmptyHint').style.display = 'none';

  const card = document.createElement('div');
  card.className   = 'adm-color-group-card';
  card.id          = groupId;
  card.dataset.color = colorName;
  card.dataset.hex   = hex;

  card.innerHTML = `
    <div class="adm-color-group-header">
      <div class="adm-color-group-title">
        <span class="adm-color-swatch-sm" style="background:${hex}"></span>
        <span>${escHtml(colorName)}</span>
        <span style="font-size:0.72rem;color:var(--adm-muted);font-weight:400;">${hex}</span>
      </div>
      <button type="button" class="adm-remove-color-btn" onclick="removeColorGroup('${groupId}')">
        <i class="bi bi-trash3"></i> Remove color
      </button>
    </div>
    <div class="adm-color-group-body">
      <div class="adm-size-rows" id="${groupId}-sizes"></div>
      <button type="button" class="adm-add-size-btn" onclick="addSizeRow('${groupId}')">
        <i class="bi bi-plus"></i> Add size
      </button>
    </div>`;

  document.getElementById('variantColorGroups').appendChild(card);

  // Populate existing variants or add one empty row
  if (existingVariants && existingVariants.length > 0) {
    existingVariants.forEach(v => addSizeRow(groupId, v.size, v.quantity, v.variantId));
  } else {
    addSizeRow(groupId);
  }

  // Clear the add-color inputs
  document.getElementById('newColorName').value    = '';
  document.getElementById('newColorHex').value     = '#000000';
  document.getElementById('newColorHexText').value = '';
}

function removeColorGroup(groupId) {
  document.getElementById(groupId)?.remove();
  if (document.getElementById('variantColorGroups').children.length === 0) {
    document.getElementById('variantsEmptyHint').style.display = 'block';
  }
}

let sizeRowCounter = 0;

function addSizeRow(groupId, size, qty, variantId) {
  const rowId = 'sr-' + (sizeRowCounter++);
  const row   = document.createElement('div');
  row.className = 'adm-size-row';
  row.id        = rowId;
  row.innerHTML = `
    ${variantId ? `<input type="hidden" name="variantId" value="${variantId}"/>` : ''}
    <input type="text"   class="adm-form-input adm-size-row-input"
           placeholder="Size (e.g. XS, S, M, L, XL)"
           value="${escHtml(size || '')}"
           data-group="${groupId}" name="variantSize"/>
    <input type="number" class="adm-form-input adm-size-qty-input"
           placeholder="Qty" min="0" value="${qty !== undefined ? qty : ''}"
           name="variantQty"/>
    <button type="button" class="adm-size-row-remove"
            onclick="document.getElementById('${rowId}').remove()"
            title="Remove size">
      <i class="bi bi-dash"></i>
    </button>`;
  document.getElementById(groupId + '-sizes').appendChild(row);
}

/* ── Collect variants from the form ─────────────────────────── */
function collectVariants() {
  const variants = [];
  document.querySelectorAll('.adm-color-group-card').forEach(card => {
    const color = card.dataset.color;
    const hex   = card.dataset.hex;
    card.querySelectorAll('.adm-size-row').forEach(row => {
      const variantIdEl = row.querySelector('input[name="variantId"]');
      const sizeEl      = row.querySelector('input[name="variantSize"]');
      const qtyEl       = row.querySelector('input[name="variantQty"]');
      const size        = sizeEl?.value.trim();
      const qty         = parseInt(qtyEl?.value) || 0;
      if (size) {
        variants.push({
          variantId: variantIdEl ? parseInt(variantIdEl.value) : null,
          color:     hex + '-' + color,
          size,
          quantity:  qty
        });
      }
    });
  });
  return variants;
}
document.querySelectorAll('.adm-modal-tab').forEach(tab => {
  tab.addEventListener('click', (e) => {

    // Block variants tab if adding new product
    if (tab.dataset.tab === 'variants' && !currentEditProductId) {
      e.preventDefault();
      e.stopImmediatePropagation();
      showVariantsTabWarning();
      // Force stay on info tab
      document.querySelectorAll('.adm-modal-tab').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.adm-tab-panel').forEach(p => p.classList.remove('active'));
      document.querySelector('[data-tab="info"]').classList.add('active');
      document.getElementById('tab-info').classList.add('active');
      return;
    }

    document.querySelectorAll('.adm-modal-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.adm-tab-panel').forEach(p => p.classList.remove('active'));
    tab.classList.add('active');
    document.getElementById('tab-' + tab.dataset.tab).classList.add('active');
  });
});

function showVariantsTabWarning() {
  // Shake the variants tab
  const variantsTab = document.getElementById('variantsTabBtn');
  variantsTab.classList.add('adm-tab-shake');
  setTimeout(() => variantsTab.classList.remove('adm-tab-shake'), 500);

  // Show inline warning inside the variants tab area
  showModalBanner('Please fill in the basic product info and click "Save Product" first.', 'warning');
}
/* ── Save product ───────────────────────────────────────────── */
document.getElementById('saveProductBtn')?.addEventListener('click', () => {
  if (!validateProductForm()) return;

  const btn = document.getElementById('saveProductBtn');
  btn.disabled = true;
  btn.querySelector('.adm-btn-text').classList.add('d-none');
  btn.querySelector('.adm-btn-spinner').classList.remove('d-none');

  const isNew = !currentEditProductId;

  const payload = JSON.stringify({
    id:          currentEditProductId,
    name:        document.getElementById('productName').value.trim(),
    categoryId:  parseInt(document.getElementById('productCategory').value),
    pricePerDay: parseFloat(document.getElementById('productPrice').value),
    imageUrl:    document.getElementById('productImage').value.trim(),
    description: document.getElementById('productDescription').value.trim(),
    variants:    collectVariants()
  });

  const url    = currentEditProductId
      ? CTX + '/admin/products/' + currentEditProductId
      : CTX + '/admin/products';
  const method = currentEditProductId ? 'PUT' : 'POST';

  let req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');
  req.onreadystatechange = function () {
    if (req.readyState === 4) {
      btn.disabled = false;
      btn.querySelector('.adm-btn-text').classList.remove('d-none');
      btn.querySelector('.adm-btn-spinner').classList.add('d-none');

      if (req.status === 200) {
        const data = JSON.parse(req.responseText);

        if (isNew) {
          // ── Step 1 complete: product created ──
          // Store the new ID so subsequent saves go to PUT
          currentEditProductId = data.id;
          document.getElementById('productId').value = data.id;

          // Enable variants tab and switch to it
          const variantsTabBtn = document.getElementById('variantsTabBtn');
          variantsTabBtn.disabled = false;
          variantsTabBtn.click();

          // Update modal title and button label
          document.getElementById('productModalTitle').textContent = 'Edit Product — Add Variants';
          btn.querySelector('.adm-btn-text').textContent = 'Save Variants';

          // Show a success hint
          showModalBanner('Product created! Now add variants and stock below.', 'success');

          // Reload table in background
          loadProducts(currentPage);

        } else {
          // ── Edit complete: close modal ──
          closeModal('productModalOverlay');
          loadProducts(currentPage);
        }
      } else {
        showModalBanner('Something went wrong. Please try again.', 'error');
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

// ADD TO CSS:
/*
.adm-product-img {
  width: 48px;
  height: 60px;
  object-fit: cover;
  object-position: top;
  border-radius: var(--adm-radius);
  background: var(--adm-stone);
  flex-shrink: 0;
}
*/

/* ── Image URL helper (mirrors card-factory.js) ─────────────── */
function imgUrl(base, size) {
  const b = (base && !base.endsWith('.jpg') && !base.endsWith('.png'))
      ? base
      : '/assets/img/placeholder';
  return `${CTX}${b}_${size}.jpg`;
}

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
      renderStockPopover(data.stockByColor,data.description);

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

function renderStockPopover(stockByColor, description) {
  const body = document.getElementById('stockPopoverBody');
  if (!body) return;

  // Description section
  const descHtml = description
      ? `<div class="adm-stock-description">${escHtml(description)}</div>`
      : '';

  if (!stockByColor || Object.keys(stockByColor).length === 0) {
    body.innerHTML = descHtml + '<p class="adm-stock-empty">No variants found.</p>';
    return;
  }

  body.innerHTML = descHtml + Object.entries(stockByColor).map(([colorKey, variants]) => {

    let hex = '#ccc';
    let name = colorKey;

    const dashIndex = colorKey.indexOf('-');

    if (dashIndex !== -1) {
      const hexPart = colorKey.substring(0, dashIndex);
      const namePart = colorKey.substring(dashIndex + 1);

      if (isHexColor(hexPart)) {
        hex = hexPart;
      }

      name = namePart || hexPart;
    } else if (isHexColor(colorKey)) {
      hex = colorKey;
    }

    return `
      <div class="adm-stock-color-group">
        <div class="adm-stock-color-header">
          <span class="adm-stock-color-swatch" style="background:${hex}"></span>
          <span class="adm-stock-color-name">${escHtml(name)}</span>
        </div>
        <div class="adm-stock-sizes">
          ${variants.map(v => `
            <div class="adm-stock-size-row">
              <span class="adm-stock-size-label">Size ${escHtml(v.size || '—')}</span>
              <span class="adm-stock-size-qty ${
        v.quantity === 0
            ? 'adm-stock-qty--zero'
            : v.quantity <= 3
                ? 'adm-stock-qty--low'
                : 'adm-stock-qty--ok'
    }">
                ${v.quantity}
              </span>
            </div>
          `).join('')}
        </div>
      </div>`;
  }).join('');
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

function showModalBanner(message, type) {
  let banner = document.getElementById('modalBanner');
  if (!banner) {
    banner = document.createElement('div');
    banner.id = 'modalBanner';
    document.querySelector('.adm-modal-body').prepend(banner);
  }
  const icons = {
    success: 'check-circle',
    error:   'exclamation-circle',
    warning: 'exclamation-triangle'
  };
  banner.className = `adm-modal-banner adm-modal-banner--${type}`;
  banner.innerHTML = `<i class="bi bi-${icons[type] || 'info-circle'}"></i> ${escHtml(message)}`;
  banner.style.display = 'flex';
  setTimeout(() => { banner.style.display = 'none'; }, 4000);
}
/* ── Init: load products on page load ───────────────────────── */
loadProducts();