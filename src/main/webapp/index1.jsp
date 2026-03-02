<%-- index.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<!-- Include header with parameters -->
<jsp:include page="WEB-INF/header.jsp">
  <jsp:param name="title" value="Rentawear – Rent Premium Fashion" />
  <jsp:param name="description" value="Rent designer dresses, suits, and more for any occasion. Affordable fashion at your fingertips." />
  <jsp:param name="activePage" value="home" />
  <jsp:param name="activeCategory" value="all" />
</jsp:include>

  <!-- ============================================
       TRENDING RENTALS
       ============================================ -->
  <section class="rw-section">
    <div class="container-lg">
      <!-- Section Header -->
      <div class="d-flex align-items-center justify-content-between mb-3">
        <h2 class="section-title mb-0">Trending Rentals</h2>
        <a href="#" class="rw-view-all">View All <i class="bi bi-arrow-right"></i></a>
      </div>

      <!-- Mobile: Horizontal Scroll -->
      <div class="rw-trending-scroll" id="trendingScroll">
        <!-- Cards inserted by JS -->
      </div>

      <!-- Desktop: Grid -->
      <div class="rw-trending-grid d-none" id="trendingGrid">
        <!-- Cards inserted by JS -->
      </div>
    </div>
  </section>

  <!-- ============================================
       SHOP BY OCCASION
       ============================================ -->
  <section class="rw-section">
    <div class="container-lg">
      <div class="d-flex align-items-center justify-content-between mb-3">
        <h2 class="section-title mb-0">Shop by Occasion</h2>
        <a href="#" class="rw-view-all">View All <i class="bi bi-arrow-right"></i></a>
      </div>

      <div class="rw-occasion-grid" id="occasionGrid">
        <!-- Cards inserted by JS -->
      </div>
    </div>
  </section>

  <!-- ============================================
       FOOTER (Desktop)
       ============================================ -->
  <footer class="rw-footer">
    <div class="container-lg">
      <div class="row g-4">
        <div class="col-lg-3">
          <div class="rw-logo mb-3" style="font-size:1.6rem;">
            <span class="logo-dark" style="color:#fff;">renta</span><span class="logo-teal">wear</span>
          </div>
          <p style="font-size:0.85rem; color:#8d8d8d;">Rent premium fashion for every occasion. Sustainable, stylish, and affordable.</p>
        </div>
        <div class="col-lg-3">
          <h6>Company</h6>
          <ul>
            <li><a href="#">About Us</a></li>
            <li><a href="#">Careers</a></li>
            <li><a href="#">Press</a></li>
            <li><a href="#">Blog</a></li>
          </ul>
        </div>
        <div class="col-lg-3">
          <h6>Support</h6>
          <ul>
            <li><a href="#">Help Centre</a></li>
            <li><a href="#">How It Works</a></li>
            <li><a href="#">Shipping &amp; Returns</a></li>
            <li><a href="#">Contact Us</a></li>
          </ul>
        </div>
        <div class="col-lg-3">
          <h6>Legal</h6>
          <ul>
            <li><a href="#">Terms of Service</a></li>
            <li><a href="#">Privacy Policy</a></li>
            <li><a href="#">Cookie Policy</a></li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom text-center">
        &copy; 2026 Rentawear. All rights reserved.
      </div>
    </div>
  </footer>

  <!-- ============================================
       BOTTOM NAV (Mobile)
       ============================================ -->
  <nav class="rw-bottom-nav" aria-label="Mobile navigation">
    <a href="${pageContext.request.contextPath}/" class="bnav-item active" aria-label="Home">
      <i class="bi bi-house-door-fill"></i>
      <span>Home</span>
    </a>
    <a href="#" class="bnav-item" aria-label="Explore">
      <i class="bi bi-compass"></i>
      <span>Explore</span>
    </a>
    <a href="#" class="bnav-item" aria-label="Rentals">
      <i class="bi bi-bag"></i>
      <span>Rentals</span>
    </a>
    <a href="#" class="bnav-item" aria-label="Profile">
      <i class="bi bi-person"></i>
      <span>Profile</span>
    </a>
  </nav>

  <!-- Bootstrap 5 JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <!-- Custom JS -->
  <script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>