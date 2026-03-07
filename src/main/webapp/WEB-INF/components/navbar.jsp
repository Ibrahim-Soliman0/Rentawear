<%-- navbar.jsp — Top navigation bar ONLY
     Receives: activePage (e.g. "home", "women", "men", "new")
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

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

    <!-- CENTER: Desktop nav links + dropdowns -->
    <div class="rw-desktop-nav align-items-center gap-1 d-none d-lg-flex">

      <a href="${pageContext.request.contextPath}/"
         class="nav-link ${param.activePage == 'home' ? 'active' : ''}">Home</a>

      <div class="dropdown">
        <a href="#" class="nav-link dropdown-toggle ${param.activePage == 'women' ? 'active' : ''}"
            aria-expanded="false">Women</a>
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
            aria-expanded="false">Men</a>
        <ul class="dropdown-menu rw-dropdown">
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/men/suits">Suits &amp; Tailoring</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/men/shirts">Shirts</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/men/accessories">Accessories</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/men/shoes">Shoes</a></li>
        </ul>
      </div>

      <div class="dropdown">
        <a href="#" class="nav-link dropdown-toggle ${param.activePage == 'new' ? 'active' : ''}"
            aria-expanded="false">New Arrivals</a>
        <ul class="dropdown-menu rw-dropdown">
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/new/this-week">This Week</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/new/this-month">This Month</a></li>
          <li><a class="dropdown-item" href="${pageContext.request.contextPath}/new/trending">Trending Now</a></li>
        </ul>
      </div>

    </div>

    <!-- RIGHT: Action icons -->
    <div class="rw-nav-icons">

      <button class="rw-icon-btn" data-bs-toggle="modal"
              data-bs-target="#searchModal" aria-label="Search">
        <i class="bi bi-search"></i>
      </button>

      <a href="${pageContext.request.contextPath}/wishlist"
         class="rw-icon-btn d-none d-lg-inline-flex" aria-label="Wishlist">
        <i class="bi bi-heart"></i>
      </a>

      <button class="rw-icon-btn rw-cart-btn" data-bs-toggle="offcanvas"
              data-bs-target="#cartDrawer" aria-controls="cartDrawer" aria-label="Shopping bag">
        <i class="bi bi-bag"></i>
        <span class="rw-cart-count d-none" id="cartCount">0</span>
      </button>

      <a href="${pageContext.request.contextPath}/profile"
         class="rw-icon-btn d-none d-lg-inline-flex" aria-label="Profile">
        <i class="bi bi-person-circle" style="font-size:1.25rem;"></i>
      </a>

    </div>
  </div>
</nav>