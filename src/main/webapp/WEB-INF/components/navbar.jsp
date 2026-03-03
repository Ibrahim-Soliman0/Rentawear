<%-- navbar.jsp — Top navigation bar + search modal + mobile offcanvas --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<!-- ============================================================
     TOP NAVBAR
     ============================================================ -->
<nav class="rw-topnav" aria-label="Main navigation">
  <div class="container-lg d-flex align-items-center justify-content-between">

    <!-- LEFT: Logo (desktop) / Hamburger (mobile) -->
    <div class="d-flex align-items-center gap-2">

      <!-- Logo — visible on desktop -->
      <a href="${pageContext.request.contextPath}/" class="rw-logo d-none d-lg-block" aria-label="Rentawear Home">
        <span class="logo-dark">renta</span><span class="logo-teal">wear</span>
      </a>

      <!-- Hamburger — visible on mobile/tablet -->
      <button class="rw-hamburger d-lg-none"
              type="button"
              data-bs-toggle="offcanvas"
              data-bs-target="#mobileMenu"
              aria-controls="mobileMenu"
              aria-label="Open menu">
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

      <!-- Women dropdown -->
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

      <!-- Men dropdown -->
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

      <!-- New Arrivals dropdown -->
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

    <!-- RIGHT: Icons -->
    <div class="d-flex align-items-center gap-2">

      <!-- Search icon (all screens) -->
      <button class="rw-icon-btn" id="searchToggle" aria-label="Search" data-bs-toggle="modal" data-bs-target="#searchModal">
        <i class="bi bi-search"></i>
      </button>

      <!-- Desktop only extras -->
      <div class="d-none d-lg-flex align-items-center gap-2">
        <a href="${pageContext.request.contextPath}/wishlist" class="rw-icon-btn" aria-label="Wishlist">
          <i class="bi bi-heart"></i>
        </a>
        <a href="${pageContext.request.contextPath}/cart" class="rw-icon-btn" aria-label="Bag">
          <i class="bi bi-bag"></i>
        </a>
        <a href="${pageContext.request.contextPath}/profile" class="rw-icon-btn" aria-label="Profile">
          <i class="bi bi-person-circle" style="font-size:1.3rem;"></i>
        </a>
      </div>

    </div>
  </div>
</nav>

<!-- ============================================================
     SEARCH MODAL
     ============================================================ -->
<div class="modal fade rw-search-modal" id="searchModal" tabindex="-1" aria-label="Search" aria-hidden="true">
  <div class="modal-dialog modal-dialog-top">
    <div class="modal-content rw-search-modal-content">

      <!-- Search input row -->
      <div class="rw-search-input-row">
        <div class="input-group rw-search-group">
          <span class="input-group-text rw-search-icon-left"><i class="bi bi-search"></i></span>
          <input type="text" id="searchInput" class="form-control rw-search-input"
                 placeholder="Search dresses, suits, brands…" autocomplete="off" aria-label="Search"/>
          <button class="btn rw-search-clear d-none" id="searchClear" type="button" aria-label="Clear">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <button type="button" class="rw-modal-close" data-bs-dismiss="modal" aria-label="Close">Cancel</button>
      </div>

      <!-- Filters row -->
      <div class="rw-search-filters">
        <!-- Category filter -->
        <div class="rw-filter-group">
          <label class="rw-filter-label">Category</label>
          <div class="d-flex flex-wrap gap-2">
            <span class="rw-pill active" data-filter-category="all">All</span>
            <span class="rw-pill" data-filter-category="women">Women</span>
            <span class="rw-pill" data-filter-category="men">Men</span>
            <span class="rw-pill" data-filter-category="accessories">Accessories</span>
            <span class="rw-pill" data-filter-category="shoes">Shoes</span>
          </div>
        </div>

        <!-- Price filter -->
        <div class="rw-filter-group mt-3">
          <label class="rw-filter-label">Max Price per Day:
            <strong class="rw-price-value">$<span id="priceDisplay">200</span></strong>
          </label>
          <input type="range" class="rw-range" id="priceRange" min="10" max="500" value="200" step="10"
                 aria-label="Max price per day"/>
        </div>
      </div>

      <!-- Results -->
      <div class="rw-search-results" id="searchResults">
        <p class="rw-search-placeholder">Start typing to see results…</p>
      </div>

    </div>
  </div>
</div>

<!-- ============================================================
     MOBILE OFFCANVAS MENU
     ============================================================ -->
<div class="offcanvas offcanvas-start rw-offcanvas" tabindex="-1" id="mobileMenu" aria-labelledby="mobileMenuLabel">
  <div class="offcanvas-header">
    <a href="${pageContext.request.contextPath}/" class="rw-logo" id="mobileMenuLabel">
      <span class="logo-dark">renta</span><span class="logo-teal">wear</span>
    </a>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-0">
    <ul class="rw-mobile-nav">

      <li>
        <a href="${pageContext.request.contextPath}/"
           class="rw-mobile-link ${param.activePage == 'home' ? 'active' : ''}">
          <i class="bi bi-house-door"></i> Home
        </a>
      </li>

      <!-- Women accordion -->
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

      <!-- Men accordion -->
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

      <!-- New Arrivals accordion -->
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

      <li class="rw-mobile-divider"></li>

      <li>
        <a href="${pageContext.request.contextPath}/profile" class="rw-mobile-link">
          <i class="bi bi-person-circle"></i> Profile
        </a>
      </li>
      <li>
        <a href="${pageContext.request.contextPath}/wishlist" class="rw-mobile-link">
          <i class="bi bi-heart"></i> Wishlist
        </a>
      </li>
      <li>
        <a href="${pageContext.request.contextPath}/cart" class="rw-mobile-link">
          <i class="bi bi-bag"></i> My Bag
        </a>
      </li>

    </ul>
  </div>
</div>