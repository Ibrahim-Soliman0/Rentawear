<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="mobile-drawer" id="mobileDrawer" aria-hidden="true">
  <div class="drawer-backdrop" id="drawerBackdrop"></div>
  <div class="drawer-panel" role="dialog" aria-label="Mobile menu">

    <div class="drawer-header">
      <a href="${pageContext.request.contextPath}/" class="rw-logo" style="font-size:1.4rem;">
        renta<em>wear</em>
      </a>
      <button class="drawer-close" id="drawerClose" aria-label="Close menu" type="button">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <line x1="18" y1="6" x2="6" y2="18"/>
          <line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>

    <nav class="drawer-nav">
      <a class="drawer-item ${param.activePage == 'home' ? 'active-link' : ''}"
         href="${pageContext.request.contextPath}/">Home</a>

      <button class="drawer-item" data-target="sub-women" type="button">
        Women
        <i class="bi bi-chevron-down rw-chevron"></i>
      </button>
      <div class="drawer-sub" id="sub-women">
        <c:forEach var="cat" items="${navCategoriesFemale}">
          <a href="${pageContext.request.contextPath}/catalog?gender=FEMALE&amp;categoryIds=${cat.id}">
              ${cat.name}
          </a>
        </c:forEach>
        <a href="${pageContext.request.contextPath}/catalog?gender=FEMALE">
          View All Women's →
        </a>
      </div>

      <button class="drawer-item" data-target="sub-men" type="button">
        Men
        <i class="bi bi-chevron-down rw-chevron"></i>
      </button>
      <div class="drawer-sub" id="sub-men">
        <c:forEach var="cat" items="${navCategoriesMale}">
          <a href="${pageContext.request.contextPath}/catalog?gender=MALE&amp;categoryIds=${cat.id}">
              ${cat.name}
          </a>
        </c:forEach>
        <a href="${pageContext.request.contextPath}/catalog?gender=MALE">
          View All Men's →
        </a>
      </div>

      <a class="drawer-item ${param.activePage == 'new' ? 'active-link' : ''}"
         href="${pageContext.request.contextPath}/catalog?newOnly=true">
        New Arrivals
      </a>

      <a class="drawer-item"
         href="${pageContext.request.contextPath}/catalog">
        All Products
      </a>

      <div class="drawer-divider"></div>
    </nav>

    <div class="drawer-footer">
      <a href="${pageContext.request.contextPath}/profile" class="drawer-footer-item">
        <i class="bi bi-person"></i>
        My Profile
      </a>
    </div>

  </div>
</div>