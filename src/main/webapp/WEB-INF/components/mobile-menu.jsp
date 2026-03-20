<%-- mobile-menu.jsp - Redesign mobile drawer (matches attached HTML)
     Receives: activePage (e.g. "home", "women", "men", "new")
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@ page isELIgnored="false" %>

<div class="mobile-drawer" id="mobileDrawer" aria-hidden="true">
  <div class="drawer-backdrop" id="drawerBackdrop"></div>
  <div class="drawer-panel" role="dialog" aria-label="Mobile menu">

    <div class="drawer-header">
      <a href="${pageContext.request.contextPath}/" class="rw-logo" style="font-size:1.4rem;">renta<em>wear</em></a>
      <button class="drawer-close" id="drawerClose" aria-label="Close menu" type="button">
        <svg viewBox="0 0 24 24" aria-hidden="true"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <nav class="drawer-nav">
      <a class="drawer-item ${param.activePage == 'home' ? 'active-link' : ''}" href="${pageContext.request.contextPath}/">Home</a>

      <button class="drawer-item" data-target="sub-women" type="button">
        Women
        <i class="bi bi-chevron-down rw-chevron"></i>      </button>
      <div class="drawer-sub" id="sub-women">
        <a href="${pageContext.request.contextPath}/women/dresses">Dresses</a>
        <a href="${pageContext.request.contextPath}/women/tops">Tops &amp; Blouses</a>
        <a href="${pageContext.request.contextPath}/women/jumpsuits">Jumpsuits &amp; Playsuits</a>
        <a href="${pageContext.request.contextPath}/women/sets">Co-ords &amp; Sets</a>
        <a href="${pageContext.request.contextPath}/women/shoes">Shoes &amp; Bags</a>
        <a href="${pageContext.request.contextPath}/products?category=women">View All Women's -&gt;</a>
      </div>

      <button class="drawer-item" data-target="sub-men" type="button">
        Men
        <i class="bi bi-chevron-down rw-chevron"></i>      </button>
      <div class="drawer-sub" id="sub-men">
        <a href="${pageContext.request.contextPath}/men/suits">Suits &amp; Tailoring</a>
        <a href="${pageContext.request.contextPath}/men/shirts">Shirts &amp; Tops</a>
        <a href="${pageContext.request.contextPath}/men/blazers">Blazers &amp; Jackets</a>
        <a href="${pageContext.request.contextPath}/men/shoes">Shoes &amp; Accessories</a>
        <a href="${pageContext.request.contextPath}/products?category=men">View All Men's -&gt;</a>
      </div>

      <button class="drawer-item" data-target="sub-new" type="button">
        New Arrivals
        <i class="bi bi-chevron-down rw-chevron"></i>      </button>
      <div class="drawer-sub" id="sub-new">
        <a href="${pageContext.request.contextPath}/new/this-week">This Week</a>
        <a href="${pageContext.request.contextPath}/new/this-month">This Month</a>
        <a href="${pageContext.request.contextPath}/new/trending">Trending Now</a>
        <a href="${pageContext.request.contextPath}/new/editors-picks">Editor's Picks</a>
      </div>

      <button class="drawer-item" data-target="sub-occ" type="button">
        Occasions
        <i class="bi bi-chevron-down rw-chevron"></i>      </button>
      <div class="drawer-sub" id="sub-occ">
        <a href="${pageContext.request.contextPath}/products?occasion=wedding">Wedding</a>
        <a href="${pageContext.request.contextPath}/products?occasion=black-tie">Black Tie</a>
        <a href="${pageContext.request.contextPath}/products?occasion=garden-party">Garden Party</a>
        <a href="${pageContext.request.contextPath}/products?occasion=cocktail">Cocktail</a>
        <a href="${pageContext.request.contextPath}/products?occasion=business">Business</a>
      </div>

      <div class="drawer-divider"></div>
    </nav>

    <div class="drawer-footer">
      <a href="${pageContext.request.contextPath}/profile" class="drawer-footer-item">
        <i class="bi bi-person"></i>
        My Profile
      </a>
<%--      <a href="${pageContext.request.contextPath}/wishlist" class="drawer-footer-item">--%>
<%--        <i class="bi bi-heart"></i>--%>
<%--        Wishlist--%>
<%--      </a>--%>
    </div>

  </div>
</div>

