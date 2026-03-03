<%-- navbar.jsp — Top navigation bar + search modal + mobile offcanvas + cart drawer --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<!-- ============================================================
     TOP NAVBAR
     ============================================================ -->
<nav class="rw-topnav" aria-label="Main navigation">
  <div class="container-lg d-flex align-items-center justify-content-between">

    <!-- LEFT: Logo (desktop) / Hamburger (mobile) -->
    <div class="d-flex align-items-center">
      <a href="${pageContext.request.contextPath}/" class="rw-logo d-none d-lg-inline-flex" aria-label="Rentawear Home">
        <span class="logo-dark">renta</span><span class="logo-teal">wear</span>
      </a>
      <button class="rw-hamburger d-lg-none" type="button"
              data-bs-toggle="offcanvas" data-bs-target="#mobileMenu"
              aria-controls="mobileMenu" aria-label="Open menu">
        <i class="bi bi-list"></i>
      </button>
    </div>

    <!-- CENTER: Logo on mobile -->
    <a href="${pageContext.request.contextPath}/" class="rw-logo d-lg-none rw-logo-center" aria-label="Rentawear Home">
      <span class="logo-dark">renta</span><span class="logo-teal">wear</span>
    </a>

    <!-- CENTER: Desktop Nav Links -->
    <div class="rw-desktop-nav align-items-center gap-1 d-none d-lg-flex">
      <a href="${pageContext.request.contextPath}/"
         class="nav-link ${param.activePage == 'home' ? 'active' : ''}">Home</a>

      <div class="dropdown">
        <a href="#" class="nav-link dropdown-toggle ${param.activePage == 'women' ? 'active' : ''}"
           data-bs-toggle="dropdown" aria-expanded="false">Women</a>
        <ul class="dropdown-menu rw-dropdown">
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/women/dresses">Dresses</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/women/tops">Tops</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/women/accessories">Accessories</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/women/shoes">Shoes</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/women/bags">Bags</a></li>
        </ul>
      </div>

      <div class="dropdown">
        <a href="#" class="nav-link dropdown-toggle ${param.activePage == 'men' ? 'active' : ''}"
           data-bs-toggle="dropdown" aria-expanded="false">Men</a>
        <ul class="dropdown-menu rw-dropdown">
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/men/suits">Suits &amp; Tailoring</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/men/shirts">Shirts</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/men/accessories">Accessories</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/men/shoes">Shoes</a></li>
        </ul>
      </div>

      <div class="dropdown">
        <a href="#" class="nav-link dropdown-toggle ${param.activePage == 'new' ? 'active' : ''}"
           data-bs-toggle="dropdown" aria-expanded="false">New Arrivals</a>
        <ul class="dropdown-menu rw-dropdown">
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/new/this-week">This Week</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/new/this-month">This Month</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/new/trending">Trending Now</a></li>
        </ul>
      </div>
    </div>

    <!-- RIGHT: Icons — cart always visible, others desktop only -->
    <div class="rw-nav-icons">
      <button class="rw-icon-btn" id="searchToggle"
              data-bs-toggle="modal" data-bs-target="#searchModal" aria-label="Search">
        <i class="bi bi-search"></i>
      </button>

      <!-- Wishlist: desktop only -->
      <a href="${pageContext.request.contextPath}/wishlist"
         class="rw-icon-btn d-none d-lg-inline-flex" aria-label="Wishlist">
        <i class="bi bi-heart"></i>
      </a>

      <!-- Cart: always visible, opens cart drawer -->
      <button class="rw-icon-btn rw-cart-btn" id="cartToggle"
              data-bs-toggle="offcanvas" data-bs-target="#cartDrawer"
              aria-controls="cartDrawer" aria-label="Shopping bag">
        <i class="bi bi-bag"></i>
        <span class="rw-cart-count d-none" id="cartCount">0</span>
      </button>

      <!-- Profile: desktop only -->
      <a href="${pageContext.request.contextPath}/profile"
         class="rw-icon-btn d-none d-lg-inline-flex" aria-label="Profile">
        <i class="bi bi-person-circle" style="font-size:1.25rem;"></i>
      </a>
    </div>
  </div>
</nav>


<!-- ============================================================
     SEARCH MODAL
     ============================================================ -->
<div class="modal fade rw-search-modal" id="searchModal" tabindex="-1" aria-label="Search" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content rw-search-modal-content">

      <div class="rw-search-input-row">
        <div class="rw-search-group">
          <span class="rw-search-icon-left"><i class="bi bi-search"></i></span>
          <input type="text" id="searchInput" class="rw-search-input"
                 placeholder="Search dresses, suits, brands…" autocomplete="off" aria-label="Search"/>
          <button class="rw-search-clear d-none" id="searchClear" type="button" aria-label="Clear">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <button type="button" class="rw-modal-close" data-bs-dismiss="modal">Cancel</button>
      </div>

      <div class="rw-search-filters">
        <!-- Category -->
        <div class="mb-3">
          <label class="rw-filter-label">Category</label>
          <div class="rw-filter-pills">
            <span class="rw-pill active" data-filter-category="all">All</span>
            <span class="rw-pill" data-filter-category="women">Women</span>
            <span class="rw-pill" data-filter-category="men">Men</span>
            <span class="rw-pill" data-filter-category="accessories">Accessories</span>
            <span class="rw-pill" data-filter-category="shoes">Shoes</span>
          </div>
        </div>

        <!-- Price range (dual handle) -->
        <div>
          <label class="rw-filter-label">
            Price / day:
            <strong class="rw-price-value">
              $<span id="priceMin">10</span> – $<span id="priceMax">500</span>
            </strong>
          </label>
          <div class="rw-range-wrap">
            <div class="rw-range-track">
              <div class="rw-range-fill" id="rangeFill"></div>
            </div>
            <input type="range" class="rw-range rw-range-min" id="priceRangeMin"
                   min="10" max="500" value="10"  step="10" aria-label="Minimum price"/>
            <input type="range" class="rw-range rw-range-max" id="priceRangeMax"
                   min="10" max="500" value="500" step="10" aria-label="Maximum price"/>
          </div>
        </div>
      </div>

      <div class="rw-search-results" id="searchResults">
        <p class="rw-search-placeholder">Start typing to see results…</p>
      </div>
    </div>
  </div>
</div>


<!-- ============================================================
     CART DRAWER (slides from right)
     ============================================================ -->
<div class="offcanvas offcanvas-end rw-cart-drawer" tabindex="-1"
     id="cartDrawer" aria-labelledby="cartDrawerLabel">

  <!-- Header -->
  <div class="offcanvas-header rw-cart-header">
    <h5 class="rw-cart-title" id="cartDrawerLabel">
      <i class="bi bi-bag me-2"></i>My Bag
      <span class="rw-cart-header-count" id="cartHeaderCount">0 items</span>
    </h5>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>

  <!-- Body: scrollable item list -->
  <div class="offcanvas-body rw-cart-body" id="cartItemsList">
    <!-- Empty state (shown by default) -->
    <div class="rw-cart-empty" id="cartEmpty">
      <i class="bi bi-bag-x"></i>
      <p>Your bag is empty</p>
      <span>Add items to get started</span>
    </div>
    <!-- Cart items injected here by app.js -->
  </div>

  <!-- Footer: always visible at bottom of drawer -->
  <div class="rw-cart-footer" id="cartFooter">
    <!-- Order summary -->
    <div class="rw-cart-summary" id="cartSummary">
      <div class="rw-cart-summary-row">
        <span>Subtotal</span>
        <span id="cartSubtotal">$0.00</span>
      </div>
      <div class="rw-cart-summary-row rw-cart-summary-total">
        <span>Total</span>
        <span id="cartTotal">$0.00</span>
      </div>
    </div>
    <!-- Action buttons -->
    <div class="rw-cart-actions">
      <button class="rw-cart-btn-secondary" data-bs-dismiss="offcanvas">
        <i class="bi bi-arrow-left me-2"></i>Continue Shopping
      </button>
      <a href="${pageContext.request.contextPath}/checkout" class="rw-cart-btn-primary">
        Proceed to Checkout<i class="bi bi-arrow-right ms-2"></i>
      </a>
    </div>
  </div>
</div>


<!-- ============================================================
     MOBILE OFFCANVAS MENU
     ============================================================ -->
<div class="offcanvas offcanvas-start rw-offcanvas" tabindex="-1"
     id="mobileMenu" aria-labelledby="mobileMenuLabel">

  <div class="offcanvas-header">
    <a href="${pageContext.request.contextPath}/" class="rw-logo" id="mobileMenuLabel">
      <span class="logo-dark">renta</span><span class="logo-teal">wear</span>
    </a>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>

  <div class="offcanvas-body p-0 d-flex flex-column">

    <!-- Top nav items -->
    <ul class="rw-mobile-nav">
      <li>
        <a href="${pageContext.request.contextPath}/"
           class="rw-mobile-link ${param.activePage == 'home' ? 'active' : ''}">
          <i class="bi bi-house-door"></i> Home
        </a>
      </li>

      <li>
        <button class="rw-mobile-link rw-mobile-accordion collapsed"
                data-bs-toggle="collapse" data-bs-target="#mobileWomen" aria-expanded="false">
          <span><i class="bi bi-person-dress"></i> Women</span>
          <i class="bi bi-chevron-down rw-chevron"></i>
        </button>
        <div class="collapse" id="mobileWomen">
          <ul class="rw-mobile-sub">
            <li><a href="${pageContext.request.contextPath}/women/dresses">Dresses</a></li>
            <li><a href="${pageContext.request.contextPath}/women/tops">Tops</a></li>
            <li><a href="${pageContext.request.contextPath}/women/accessories">Accessories</a></li>
            <li><a href="${pageContext.request.contextPath}/women/shoes">Shoes</a></li>
            <li><a href="${pageContext.request.contextPath}/women/bags">Bags</a></li>
          </ul>
        </div>
      </li>

      <li>
        <button class="rw-mobile-link rw-mobile-accordion collapsed"
                data-bs-toggle="collapse" data-bs-target="#mobileMen" aria-expanded="false">
          <span><i class="bi bi-person"></i> Men</span>
          <i class="bi bi-chevron-down rw-chevron"></i>
        </button>
        <div class="collapse" id="mobileMen">
          <ul class="rw-mobile-sub">
            <li><a href="${pageContext.request.contextPath}/men/suits">Suits &amp; Tailoring</a></li>
            <li><a href="${pageContext.request.contextPath}/men/shirts">Shirts</a></li>
            <li><a href="${pageContext.request.contextPath}/men/accessories">Accessories</a></li>
            <li><a href="${pageContext.request.contextPath}/men/shoes">Shoes</a></li>
          </ul>
        </div>
      </li>

      <li>
        <button class="rw-mobile-link rw-mobile-accordion collapsed"
                data-bs-toggle="collapse" data-bs-target="#mobileNew" aria-expanded="false">
          <span><i class="bi bi-stars"></i> New Arrivals</span>
          <i class="bi bi-chevron-down rw-chevron"></i>
        </button>
        <div class="collapse" id="mobileNew">
          <ul class="rw-mobile-sub">
            <li><a href="${pageContext.request.contextPath}/new/this-week">This Week</a></li>
            <li><a href="${pageContext.request.contextPath}/new/this-month">This Month</a></li>
            <li><a href="${pageContext.request.contextPath}/new/trending">Trending Now</a></li>
          </ul>
        </div>
      </li>
    </ul>

    <!-- Spacer pushes profile/wishlist to the bottom -->
    <div class="mt-auto">
      <div class="rw-mobile-divider"></div>
      <ul class="rw-mobile-nav">
        <li>
          <a href="${pageContext.request.contextPath}/wishlist" class="rw-mobile-link">
            <i class="bi bi-heart"></i> Wishlist
          </a>
        </li>
        <li>
          <a href="${pageContext.request.contextPath}/profile" class="rw-mobile-link">
            <i class="bi bi-person-circle"></i> Profile
          </a>
        </li>
      </ul>
    </div>

  </div>
</div>