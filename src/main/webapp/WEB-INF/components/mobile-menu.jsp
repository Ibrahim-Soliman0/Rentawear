<%-- mobile-menu.jsp — Mobile offcanvas slide menu ONLY
     Receives: activePage (e.g. "home", "women", "men", "new")
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<div class="offcanvas offcanvas-start rw-offcanvas"
     tabindex="-1" id="mobileMenu" aria-labelledby="mobileMenuLabel">

  <div class="offcanvas-header">
    <a href="${pageContext.request.contextPath}/" class="rw-logo" id="mobileMenuLabel">
      <span class="logo-dark">renta</span><span class="logo-teal">wear</span>
    </a>
    <button type="button" class="btn-close"
            data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>

  <!-- d-flex flex-column so mt-auto can push profile/wishlist to bottom -->
  <div class="offcanvas-body p-0 d-flex flex-column">

    <!-- Primary nav items -->
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
                data-bs-toggle="collapse" data-bs-target="#mobileWomen"
                aria-expanded="false" aria-controls="mobileWomen">
          <span><i class="bi bi-person"></i> Women</span>
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
                data-bs-toggle="collapse" data-bs-target="#mobileMen"
                aria-expanded="false" aria-controls="mobileMen">

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
                data-bs-toggle="collapse" data-bs-target="#mobileNew"
                aria-expanded="false" aria-controls="mobileNew">
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

    <!-- mt-auto pushes this block to the bottom of the offcanvas -->
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