<%-- navbar.jsp - Redesign nav (matches attached HTML)
     Receives: activePage (e.g. "home", "women", "men", "new", "account")
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<nav class="rw-nav" aria-label="Main navigation">
  <div class="nav-inner">

    <div class="nav-left">
      <a href="${pageContext.request.contextPath}/" class="rw-logo" id="desktopLogo" aria-label="Rentawear Home">
        renta<em>wear</em>
      </a>
      <button class="nav-icon-btn nav-hamburger" id="drawerToggle" aria-label="Open menu" type="button">
        <i class="bi bi-list"></i>
      </button>
    </div>

    <div class="nav-center">
      <a href="${pageContext.request.contextPath}/" class="rw-logo nav-logo-mobile" aria-label="Rentawear Home">
        renta<em>wear</em>
      </a>

      <ul class="nav-links" id="desktopNav">
        <li>
          <a class="nav-link-btn ${param.activePage == 'home' ? 'active' : ''}"
             href="${pageContext.request.contextPath}/">Home</a>
        </li>

        <li id="dd-women">
          <button class="nav-link-btn ${param.activePage == 'women' ? 'active' : ''}" aria-haspopup="true" type="button">
            Women
            <i class="bi bi-chevron-down rw-chevron"></i>          </button>
          <div class="nav-dropdown wide" role="menu">
            <div class="dropdown-col">
              <div class="dropdown-col-label">Clothing</div>
              <a href="${pageContext.request.contextPath}/women/dresses">Dresses <span class="dropdown-item-sub">Evening, midi, mini &amp; more</span></a>
              <a href="${pageContext.request.contextPath}/women/tops">Tops &amp; Blouses</a>
              <a href="${pageContext.request.contextPath}/women/jumpsuits">Jumpsuits &amp; Playsuits</a>
              <a href="${pageContext.request.contextPath}/women/sets">Co-ords &amp; Sets</a>
              <a href="${pageContext.request.contextPath}/women/skirts">Skirts</a>
            </div>
            <div class="dropdown-col">
              <div class="dropdown-col-label">Accessories</div>
              <a href="${pageContext.request.contextPath}/women/shoes">Shoes &amp; Heels</a>
              <a href="${pageContext.request.contextPath}/women/bags">Bags &amp; Clutches</a>
              <a href="${pageContext.request.contextPath}/women/jewellery">Jewellery</a>
              <a href="${pageContext.request.contextPath}/women/wraps">Wraps &amp; Coverups</a>
              <a href="${pageContext.request.contextPath}/products?category=women" class="dropdown-cta">View All Women's →</a>
            </div>
          </div>
        </li>

        <li id="dd-men">
          <button class="nav-link-btn ${param.activePage == 'men' ? 'active' : ''}" aria-haspopup="true" type="button">
            Men
            <i class="bi bi-chevron-down rw-chevron"></i>          </button>
          <div class="nav-dropdown wide" role="menu">
            <div class="dropdown-col">
              <div class="dropdown-col-label">Clothing</div>
              <a href="${pageContext.request.contextPath}/men/suits">Suits &amp; Tailoring <span class="dropdown-item-sub">Tuxedos, slim-fit &amp; classic</span></a>
              <a href="${pageContext.request.contextPath}/men/shirts">Shirts &amp; Tops</a>
              <a href="${pageContext.request.contextPath}/men/trousers">Trousers &amp; Chinos</a>
              <a href="${pageContext.request.contextPath}/men/blazers">Blazers &amp; Jackets</a>
            </div>
            <div class="dropdown-col">
              <div class="dropdown-col-label">Accessories</div>
              <a href="${pageContext.request.contextPath}/men/shoes">Shoes &amp; Oxfords</a>
              <a href="${pageContext.request.contextPath}/men/ties">Ties &amp; Bowties</a>
              <a href="${pageContext.request.contextPath}/men/cufflinks">Cufflinks &amp; Pins</a>
              <a href="${pageContext.request.contextPath}/products?category=men" class="dropdown-cta">View All Men's →</a>
            </div>
          </div>
        </li>

        <li id="dd-new">
          <button class="nav-link-btn ${param.activePage == 'new' ? 'active' : ''}" aria-haspopup="true" type="button">
            New Arrivals
            <i class="bi bi-chevron-down rw-chevron"></i>          </button>
          <div class="nav-dropdown" role="menu">
            <a href="${pageContext.request.contextPath}/new/this-week">This Week</a>
            <a href="${pageContext.request.contextPath}/new/this-month">This Month</a>
            <hr>
            <a href="${pageContext.request.contextPath}/new/trending">Trending Now</a>
            <a href="${pageContext.request.contextPath}/new/editors-picks">Editor's Picks</a>
          </div>
        </li>

        <li id="dd-occ">
          <button class="nav-link-btn" aria-haspopup="true" type="button">
            Occasions
            <i class="bi bi-chevron-down rw-chevron"></i>          </button>
          <div class="nav-dropdown" role="menu">
            <a href="${pageContext.request.contextPath}/products?occasion=wedding">Wedding</a>
            <a href="${pageContext.request.contextPath}/products?occasion=black-tie">Black Tie</a>
            <a href="${pageContext.request.contextPath}/products?occasion=garden-party">Garden Party</a>
            <a href="${pageContext.request.contextPath}/products?occasion=cocktail">Cocktail</a>
            <a href="${pageContext.request.contextPath}/products?occasion=business">Business</a>
            <hr>
            <a href="${pageContext.request.contextPath}/products">All Occasions</a>
          </div>
        </li>
      </ul>
    </div>

    <div class="nav-right">
      <button class="nav-icon-btn" id="searchToggle" aria-label="Search" type="button">
        <i class="bi bi-search"></i>      </button>
      <button class="nav-icon-btn" id="cartToggle" aria-label="Shopping bag" type="button">
        <i class="bi bi-bag"></i>
        <span class="cart-badge" id="cartBadge">0</span>
      </button>
      <a class="nav-icon-btn nav-desktop-only" aria-label="Wishlist" href="${pageContext.request.contextPath}/wishlist">
        <i class="bi bi-heart"></i>
      </a>
      <a class="nav-icon-btn nav-desktop-only" aria-label="Profile" id="desktopProfile" href="${pageContext.request.contextPath}/profile">
        <i class="bi bi-person"></i>
      </a>
    </div>

  </div>
</nav>

