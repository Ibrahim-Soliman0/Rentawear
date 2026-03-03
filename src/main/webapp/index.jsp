<%-- index.jsp — Home page --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<%-- 1. Page <head> + <body> open tag --%>
<jsp:include page="WEB-INF/header.jsp">
  <jsp:param name="title"          value="Rentawear – Rent Premium Fashion"/>
  <jsp:param name="description"    value="Rent designer dresses, suits, and more for any occasion."/>
</jsp:include>

<%-- 2. Navbar --%>
<jsp:include page="WEB-INF/components/navbar.jsp">
  <jsp:param name="activePage" value="home"/>
</jsp:include>

<%-- 3. Page body --%>
<main class="rw-main">

<!-- ============================================================
     TRENDING RENTALS
     ============================================================ -->
<section class="rw-section">
  <div class="container-lg">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <h2 class="section-title mb-0">Trending Rentals</h2>
      <a href="#" class="rw-view-all">View All <i class="bi bi-arrow-right"></i></a>
    </div>

    <!-- Mobile: Horizontal Scroll -->
    <div class="rw-trending-scroll" id="trendingScroll">
      <%-- Cards injected by app.js --%>
    </div>

    <!-- Desktop: Grid -->
    <div class="rw-trending-grid d-none" id="trendingGrid">
      <%-- Cards injected by app.js --%>
    </div>
  </div>
</section>

<!-- ============================================================
     SHOP BY OCCASION
     ============================================================ -->
<section class="rw-section">
  <div class="container-lg">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <h2 class="section-title mb-0">Shop by Occasion</h2>
      <a href="#" class="rw-view-all">View All <i class="bi bi-arrow-right"></i></a>
    </div>
    <div class="rw-occasion-grid" id="occasionGrid">
      <%-- Cards injected by app.js --%>
    </div>
  </div>
</section>
</main>

<%-- 4. Footer --%>
<jsp:include page="WEB-INF/components/footer.jsp"/>

<%-- 5. Closing scripts + </body></html> --%>
<jsp:include page="WEB-INF/components/scripts.jsp"/>