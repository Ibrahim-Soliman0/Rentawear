<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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
          <button class="nav-link-btn ${param.activePage == 'women' ? 'active' : ''}"
                  aria-haspopup="true" type="button">
            Women
            <i class="bi bi-chevron-down rw-chevron"></i>
          </button>
          <div class="nav-dropdown wide" role="menu">
            <div class="dropdown-col">
              <div class="dropdown-col-label">Women's Categories</div>
              <c:forEach var="cat" items="${navCategoriesFemale}">
                <a href="${pageContext.request.contextPath}/catalog?gender=FEMALE&amp;categoryIds=${cat.id}">
                    ${cat.name}
                </a>
              </c:forEach>
            </div>
            <div class="dropdown-col">
              <div class="dropdown-col-label">Browse</div>
              <a href="${pageContext.request.contextPath}/catalog?newOnly=true&amp;gender=FEMALE">
                New Arrivals
                <span class="dropdown-item-sub">Fresh this week</span>
              </a>
              <a href="${pageContext.request.contextPath}/catalog?gender=FEMALE" class="dropdown-cta">
                View All Women's →
              </a>
            </div>
          </div>
        </li>

        <li id="dd-men">
          <button class="nav-link-btn ${param.activePage == 'men' ? 'active' : ''}"
                  aria-haspopup="true" type="button">
            Men
            <i class="bi bi-chevron-down rw-chevron"></i>
          </button>
          <div class="nav-dropdown wide" role="menu">
            <div class="dropdown-col">
              <div class="dropdown-col-label">Men's Categories</div>
              <c:forEach var="cat" items="${navCategoriesMale}">
                <a href="${pageContext.request.contextPath}/catalog?gender=MALE&amp;categoryIds=${cat.id}">
                    ${cat.name}
                </a>
              </c:forEach>
            </div>
            <div class="dropdown-col">
              <div class="dropdown-col-label">Browse</div>
              <a href="${pageContext.request.contextPath}/catalog?newOnly=true&amp;gender=MALE">
                New Arrivals
                <span class="dropdown-item-sub">Fresh this week</span>
              </a>
              <a href="${pageContext.request.contextPath}/catalog?gender=MALE" class="dropdown-cta">
                View All Men's →
              </a>
            </div>
          </div>
        </li>

        <li>
          <a class="nav-link-btn ${param.activePage == 'new' ? 'active' : ''}"
             href="${pageContext.request.contextPath}/catalog?newOnly=true">
            New Arrivals
          </a>
        </li>

        <li>
          <a class="nav-link-btn"
             href="${pageContext.request.contextPath}/catalog">
            All Products
          </a>
        </li>
      </ul>
    </div>

    <div class="nav-right">
      <button class="nav-icon-btn" id="searchToggle" aria-label="Search" type="button">
        <i class="bi bi-search"></i>
      </button>
      <button class="nav-icon-btn" id="cartToggle" aria-label="Shopping bag" type="button">
        <i class="bi bi-bag"></i>
        <span class="cart-badge" id="cartBadge">0</span>
      </button>
      <a class="nav-icon-btn nav-desktop-only" aria-label="Profile" id="desktopProfile" href="${pageContext.request.contextPath}/profile">
        <i class="bi bi-person"></i>
      </a>
    </div>

  </div>
</nav>