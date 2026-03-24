<%-- admin-dashboard.jsp — Rentawear Admin Dashboard --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Admin Dashboard – Rentawear</title>

  <!-- Fonts -->
  <link rel="preconnect" href="https://fonts.googleapis.com"/>
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
  <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,300;0,400;1,300&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet"/>

  <!-- Bootstrap 5 -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <!-- Bootstrap Icons -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>

  <!-- Admin CSS -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css"/>

</head>
<body class="adm-body">

<!-- ── Layout wrapper ─────────────────────────────────────── -->
<div class="adm-layout">

  <!-- ── Sidebar ──────────────────────────────────────────── -->
  <aside class="adm-sidebar" id="adminSidebar">

    <!-- Brand -->
    <div class="adm-sidebar-brand">
      <div class="adm-brand-icon">
        <i class="bi bi-bag-heart-fill"></i>
      </div>
      <span class="adm-brand-name">RentaWear</span>
    </div>

    <!-- Nav -->
    <nav class="adm-nav">
      <ul class="adm-nav-list">
        <li>
          <button class="adm-nav-item active" data-section="products">
            <i class="bi bi-box-seam"></i>
            <span>Inventory</span>
          </button>
        </li>
        <li>
          <button class="adm-nav-item" data-section="customers">
            <i class="bi bi-people"></i>
            <span>Customers</span>
          </button>
        </li>
        <li>
          <button class="adm-nav-item" data-section="orders">
            <i class="bi bi-receipt"></i>
            <span>Orders</span>
          </button>
        </li>
      </ul>
    </nav>

    <!-- Admin user -->
    <div class="adm-sidebar-footer">
      <div class="adm-admin-avatar">
        <i class="bi bi-person-fill"></i>
      </div>
      <div class="adm-admin-info">
<%--        <span class="adm-admin-name">${sessionScope.user.name}</span>--%>
        <span class="adm-admin-role">Superuser</span>
      </div>
      <a href="${pageContext.request.contextPath}/logout" class="adm-logout-btn" title="Sign out">
        <i class="bi bi-box-arrow-right"></i>
      </a>
    </div>

  </aside><!-- /.adm-sidebar -->

  <!-- ── Main content ──────────────────────────────────────── -->
  <main class="adm-main">

    <!-- Top bar -->
    <header class="adm-topbar">
      <button class="adm-hamburger d-lg-none" id="sidebarToggle">
        <i class="bi bi-list"></i>
      </button>
      <div class="adm-topbar-title">
        <h1 class="adm-page-title" id="pageTitle">Inventory Control</h1>
        <p class="adm-page-subtitle" id="pageSubtitle">Manage product pricing, stock levels and availability.</p>
      </div>
    </header>

    <!-- ══ PRODUCTS SECTION ══════════════════════════════════ -->
    <section class="adm-section active" id="section-products">

      <div class="adm-section-header">
        <h2 class="adm-section-title">Inventory Management</h2>
        <button class="adm-btn adm-btn--primary" id="openAddProductModal">
          <i class="bi bi-plus-lg"></i> Add New Product
        </button>
      </div>

      <div class="adm-card">
        <div class="adm-table-wrap">
          <table class="adm-table" id="productsTable">
            <thead>
              <tr>
                <th>Product Details</th>
                <th>Category</th>
                <th>Rental Price</th>
                <th>Stock Qty</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody id="productsTableBody">
              <!-- Populated by JS / servlet -->
              <tr class="adm-table-empty">
                <td colspan="6">
                  <i class="bi bi-box-seam"></i>
                  <p>No products found</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div id="productsPagination"></div>

    </section>

    <!-- ══ CUSTOMERS SECTION ═════════════════════════════════ -->
    <section class="adm-section" id="section-customers">

      <div class="adm-section-header">
        <h2 class="adm-section-title">Customer Profiles</h2>
      </div>

      <div class="adm-card">
        <div class="adm-table-wrap">
          <table class="adm-table" id="customersTable">
            <thead>
              <tr>
                <th>Customer</th>
                <th>Profession</th>
                <th>Interests</th>
                <th>Requested Credit Limit</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody id="customersTableBody">
              <tr class="adm-table-empty">
                <td colspan="5">
                  <i class="bi bi-people"></i>
                  <p>No customers found</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </section>

    <!-- ══ ORDERS SECTION ════════════════════════════════════ -->
    <section class="adm-section" id="section-orders">

      <div class="adm-section-header">
        <h2 class="adm-section-title">Order History</h2>
      </div>

      <div class="adm-card">
        <div class="adm-table-wrap">
          <table class="adm-table" id="ordersTable">
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Customer</th>
                <th>Product</th>
                <th>Creted At</th>
                <th>Total</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody id="ordersTableBody">
              <tr class="adm-table-empty">
                <td colspan="6">
                  <i class="bi bi-receipt"></i>
                  <p>No orders found</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </section>

  </main><!-- /.adm-main -->
</div><!-- /.adm-layout -->

<!-- ══ ADD / EDIT PRODUCT MODAL ════════════════════════════ -->
<div class="adm-modal-overlay" id="productModalOverlay">
  <div class="adm-modal adm-modal--lg">

    <div class="adm-modal-header">
      <h3 class="adm-modal-title" id="productModalTitle">Add New Product</h3>
      <button class="adm-modal-close" id="closeProductModal">
        <i class="bi bi-x-lg"></i>
      </button>
    </div>

    <!-- Tabs -->
    <div class="adm-modal-tabs">
      <button class="adm-modal-tab active" data-tab="info">
        <i class="bi bi-info-circle"></i> Basic Info
      </button>
      <button class="adm-modal-tab" data-tab="variants" id="variantsTabBtn">
        <i class="bi bi-grid"></i> Variants & Stock
      </button>
    </div>

    <div class="adm-modal-body">
      <form id="productForm" novalidate>
        <input type="hidden" id="productId" name="productId"/>

        <!-- ── TAB: BASIC INFO ── -->
        <div class="adm-tab-panel active" id="tab-info">

          <div class="adm-form-group">
            <label class="adm-form-label">Product Name</label>
            <input type="text" id="productName" name="productName"
                   class="adm-form-input" placeholder="e.g. Silk Pleated Midi Dress"
                   maxlength="150" required/>
            <div class="adm-field-error" id="productNameError"></div>
          </div>

          <div class="adm-form-row-2">
            <div class="adm-form-group">
              <label class="adm-form-label">Category</label>
              <div class="adm-select-wrap">
                <select id="productCategory" name="productCategory"
                        class="adm-form-input adm-form-select" required>
                  <option value="" disabled selected>Select category</option>
                  <c:forEach var="category" items="${categories}">
                    <option value="${category.id}">${category.name}</option>
                  </c:forEach>
                </select>
                <i class="bi bi-chevron-down adm-select-chevron"></i>
              </div>
              <div class="adm-field-error" id="productCategoryError"></div>
            </div>
            <div class="adm-form-group">
              <label class="adm-form-label">Rental Price / day ($)</label>
              <input type="number" id="productPrice" name="productPrice"
                     class="adm-form-input" placeholder="45.00"
                     min="0" step="0.01" required/>
              <div class="adm-field-error" id="productPriceError"></div>
            </div>
          </div>

          <div class="adm-form-group">
            <label class="adm-form-label">Upload Image</label>
            <input type="file" id="productImageFile" name="productImageFile"
                   class="adm-form-input" accept="image/*"/>
            <div class="adm-form-hint">JPG/PNG up to 5MB. Upload replaces the current image.</div>
            <input type="hidden" id="productImage" name="productImage"/>
          </div>

          <div class="adm-form-group">
            <label class="adm-form-label">Description</label>
            <textarea id="productDescription" name="productDescription"
                      class="adm-form-input adm-form-textarea"
                      placeholder="Product description..."
                      rows="4" maxlength="1000"></textarea>
          </div>

        </div><!-- /tab-info -->

        <!-- ── TAB: VARIANTS & STOCK ── -->
        <div class="adm-tab-panel" id="tab-variants">

          <!-- Add new color group -->
          <div class="adm-variants-add-row">
            <div class="adm-form-group" style="flex:1">
              <label class="adm-form-label">Color Name</label>
              <input type="text" id="newColorName" class="adm-form-input"
                     placeholder="e.g. Midnight Navy"/>
            </div>
            <div class="adm-form-group" style="flex:0 0 120px">
              <label class="adm-form-label">Hex Code</label>
              <div style="display:flex;gap:0.4rem;align-items:center;">
                <input type="color" id="newColorHex" class="adm-color-picker" value="#000000"/>
                <input type="text"  id="newColorHexText" class="adm-form-input"
                       placeholder="#000000" maxlength="7" style="flex:1"/>
              </div>
            </div>
            <button type="button" class="adm-btn adm-btn--primary adm-variants-add-btn"
                    onclick="addColorGroup()">
              <i class="bi bi-plus-lg"></i> Add Color
            </button>
          </div>

          <!-- Color groups rendered here -->
          <div id="variantColorGroups"></div>

          <p class="adm-variants-hint" id="variantsEmptyHint">
            <i class="bi bi-info-circle"></i>
            No variants yet. Add a color above to get started.
          </p>

        </div><!-- /tab-variants -->

      </form>
    </div>

    <div class="adm-modal-footer">
      <button class="adm-btn adm-btn--ghost" id="cancelProductModal">Cancel</button>
      <button class="adm-btn adm-btn--primary" id="saveProductBtn">
        <span class="adm-btn-text">Save Product</span>
        <span class="adm-btn-spinner d-none">
          <span class="spinner-border spinner-border-sm"></span>
        </span>
      </button>
    </div>

  </div>
</div>
<!-- ══ DELETE CONFIRM MODAL ════════════════════════════════ -->
<div class="adm-modal-overlay" id="deleteModalOverlay">
  <div class="adm-modal adm-modal--sm">
    <div class="adm-modal-header">
      <h3 class="adm-modal-title">Delete Product</h3>
      <button class="adm-modal-close" id="closeDeleteModal">
        <i class="bi bi-x-lg"></i>
      </button>
    </div>
    <div class="adm-modal-body">
      <p class="adm-confirm-text">
        Are you sure you want to delete <strong id="deleteProductName"></strong>?
        This action cannot be undone.
      </p>
    </div>
    <div class="adm-modal-footer">
      <button class="adm-btn adm-btn--ghost" id="cancelDeleteModal">Cancel</button>
      <button class="adm-btn adm-btn--danger" id="confirmDeleteBtn">
        <i class="bi bi-trash3"></i> Delete
      </button>
    </div>
  </div>
</div>

<!-- Sidebar overlay (mobile) -->
<div class="adm-sidebar-overlay" id="sidebarOverlay"></div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  const CTX = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/assets/js/admin.js"></script>

</body>
</html>
