<%-- index.jsp — Home page --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- layout.jsp handles head + navbar + search modal + cart drawer + mobile menu --%>
<jsp:include page="WEB-INF/layout.jsp">
  <jsp:param name="title"       value="Rentawear – Rent Premium Fashion"/>
  <jsp:param name="description" value="Rent designer dresses, suits, and more for any occasion."/>
  <jsp:param name="activePage"  value="home"/>
</jsp:include>

<main class="rw-main">

  <!-- ============================================================
       HERO
       ============================================================ -->
  <section class="rw-hero">
    <div class="rw-hero-inner container-lg">
      <p class="rw-hero-eyebrow">New Season Arrivals</p>
      <h1 class="rw-hero-title">
        Rent the look.<br>
        Own the moment.
      </h1>
      <p class="rw-hero-sub">
        Designer fashion for every occasion,
        delivered to your door.
      </p>
      <div class="rw-hero-actions">
        <a href="${pageContext.request.contextPath}/products?category=women"
           class="btn-rw btn-rw-primary btn-rw--lg">
          Shop Women
        </a>
        <a href="${pageContext.request.contextPath}/products?category=men"
           class="rw-hero-btn-outline btn-rw--lg">
          Shop Men
        </a>
      </div>
      <div class="rw-hero-trust">
        <span class="rw-hero-trust-item">
          <i class="bi bi-shield-check" aria-hidden="true"></i>
          Dry-cleaned &amp; insured
        </span>
        <span class="rw-hero-trust-item">
          <i class="bi bi-truck" aria-hidden="true"></i>
          Free delivery over $60
        </span>
        <span class="rw-hero-trust-item">
          <i class="bi bi-arrow-return-left" aria-hidden="true"></i>
          Easy returns
        </span>
      </div>
    </div>
  </section>

  <!-- ============================================================
     SHOP BY OCCASION
     Cards injected by home.js — loadOccasions()
     ============================================================ -->
  <section class="rw-section">
    <div class="container-lg">
      <div class="rw-section-header">
        <h2 class="section-title mb-0">Shop by Occasion</h2>
        <a href="${pageContext.request.contextPath}/products" class="rw-view-all">View All <i class="bi bi-arrow-right" aria-hidden="true"></i></a>
      </div>
      <div class="rw-home-scroll"
           id="occasionScroll"
           aria-label="Shop by occasion"></div>
    </div>
  </section>


  <!-- ============================================================
       TRENDING RENTALS
       Cards injected by home.js — loadSection({ id:'trendingScroll' })
       ============================================================ -->
  <section class="rw-section">
    <div class="container-lg">
      <div class="rw-section-header d-flex align-items-center justify-content-between mb-3">
        <h2 class="section-title mb-0">Trending Rentals</h2>
        <a href="${pageContext.request.contextPath}/products?sort=popular" class="rw-view-all">View All <i class="bi bi-arrow-right" aria-hidden="true"></i></a>
      </div>
      <div class="rw-home-scroll"
           id="trendingScroll"
           aria-label="Trending rentals"></div>
    </div>
  </section>


  <!-- ============================================================
       NEW ARRIVALS
       Cards injected by home.js — loadSection({ id:'newArrivalsScroll' })
       ============================================================ -->
  <section class="rw-section">
    <div class="container-lg">
      <div class="rw-section-header d-flex align-items-center justify-content-between mb-3">
        <h2 class="section-title mb-0">New Arrivals</h2>
        <a href="${pageContext.request.contextPath}/products?sort=new" class="rw-view-all">View All <i class="bi bi-arrow-right" aria-hidden="true"></i></a>
      </div>
      <div class="rw-home-scroll"
           id="newArrivalsScroll"
           aria-label="New arrivals"></div>
    </div>
  </section>

  <!-- ============================================================
       WOMEN'S COLLECTION
       Cards injected by home.js — loadSection({ id:'womenScroll' })
       ============================================================ -->
  <section class="rw-section">
    <div class="container-lg">
      <div class="rw-section-header d-flex align-items-center justify-content-between mb-3">
        <h2 class="section-title mb-0">Women's Collection</h2>
        <a href="${pageContext.request.contextPath}/products?category=women" class="rw-view-all">View All <i class="bi bi-arrow-right" aria-hidden="true"></i></a>
      </div>
      <div class="rw-home-scroll"
           id="womenScroll"
           aria-label="Women's collection"></div>
    </div>
  </section>

  <!-- ============================================================
       MEN'S COLLECTION
       Cards injected by home.js — loadSection({ id:'menScroll' })
       ============================================================ -->
  <section class="rw-section">
    <div class="container-lg">
      <div class="rw-section-header d-flex align-items-center justify-content-between mb-3">
        <h2 class="section-title mb-0">Men's Collection</h2>
        <a href="${pageContext.request.contextPath}/products?category=men" class="rw-view-all">View All <i class="bi bi-arrow-right" aria-hidden="true"></i></a>
      </div>
      <div class="rw-home-scroll"
           id="menScroll"
           aria-label="Men's collection"></div>
    </div>
  </section>


  <!-- ============================================================
       BASED ON YOUR INTERESTS
       Only rendered when user is logged in and has interests set.
       Section is hidden by JS if servlet returns no matches.
       Cards injected by home.js — loadInterests()
       ============================================================ -->
  <c:if test="${not empty sessionScope.currentUser and not empty sessionScope.currentUser.interests}">
    <section class="rw-section rw-section--personalised" id="interestsSection">
      <div class="container-lg">
        <div class="rw-personalised-header">
          <h2 class="section-title mb-0">Based on Your Interests</h2>
          <div class="d-flex align-items-center gap-3">
          <span class="rw-personalised-badge">
            <i class="bi bi-stars" aria-hidden="true"></i>
            For you
          </span>
            <a href="${pageContext.request.contextPath}/products?sort=interests" class="rw-view-all">View All <i class="bi bi-arrow-right" aria-hidden="true"></i></a>
          </div>
        </div>
        <div class="rw-home-scroll"
             id="interestsScroll"
             aria-label="Recommended for you"></div>
      </div>
    </section>
  </c:if>


</main>

<%-- Footer --%>
<jsp:include page="WEB-INF/components/footer.jsp"/>

<%-- home.js loaded last — depends on card-factory.js (already in scripts.jsp) --%>
<jsp:include page="WEB-INF/components/scripts.jsp">
  <jsp:param name="extraJS"
             value="${pageContext.request.contextPath}/assets/js/home.js"/>
</jsp:include>
