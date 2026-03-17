<%-- index.jsp — Home page --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
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
  <section class="hero">
    <div class="hero-text">
      <p class="hero-eyebrow">New Season Arrivals</p>
      <h1 class="hero-title">
        Rent the look.<br>
        <em>Own the moment.</em>
      </h1>
      <p class="hero-sub">
        Designer fashion for every occasion - delivered to your door, starting from $25 per day.
      </p>
      <div class="hero-actions">
        <a href="${pageContext.request.contextPath}/products?category=women" class="btn-primary">
          Shop Women →
        </a>
        <a href="${pageContext.request.contextPath}/products?category=men" class="btn-outline">Shop Men</a>
      </div>
    </div>

    <div class="hero-image">
      <div class="hero-image-placeholder">
  <img src="assets/img/hero.jpg"
       alt="assets/img/hero.jpg"
       loading="lazy"/>
        <p class="hero-img-label">Spring / Summer 2026 Edit</p>
      </div>
    </div>
  </section>

  <div class="how-strip">
    <div class="how-inner">
      <p class="how-label">How Rentawear Works</p>
      <div class="how-steps">
        <div class="how-step">
          <div class="step-icon">
            <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          </div>
          <div class="step-number">01</div>
          <h3 class="step-title">Browse &amp; Choose</h3>
          <p class="step-desc">Explore designer pieces filtered by occasion, size, or style.</p>
        </div>
        <div class="how-step">
          <div class="step-icon">
            <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          </div>
          <div class="step-number">02</div>
          <h3 class="step-title">Book Your Dates</h3>
          <p class="step-desc">Select your rental window. We deliver the day before your event.</p>
        </div>
        <div class="how-step">
          <div class="step-icon">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
          </div>
          <div class="step-number">03</div>
          <h3 class="step-title">Wear &amp; Return</h3>
          <p class="step-desc">Enjoy your moment, then drop off within 48 hours.</p>
        </div>
      </div>
    </div>
  </div>

  <section class="section">
    <div class="section-inner">
      <div class="section-header">
        <div>
          <p class="section-eyebrow">Curated for Every Event</p>
          <h2 class="section-title">Shop by <em>Occasion</em></h2>
        </div>
        <a href="${pageContext.request.contextPath}/products" class="view-all">View All →</a>
      </div>
      <div class="occasion-grid" id="occasionScroll" aria-label="Shop by occasion"></div>
    </div>
  </section>

  <section class="section section-alt">
    <div class="section-inner">
      <div class="section-header">
        <div>
          <p class="section-eyebrow">Most-booked this week</p>
          <h2 class="section-title">Trending <em>Rentals</em></h2>
        </div>
        <a href="${pageContext.request.contextPath}/products?sort=popular" class="view-all">View All →</a>
      </div>
      <div class="product-scroll" id="trendingScroll" aria-label="Trending rentals"></div>
    </div>
  </section>

  <section class="section">
    <div class="section-inner">
      <div class="section-header">
        <div>
          <p class="section-eyebrow">Fresh in this week</p>
          <h2 class="section-title">New <em>Arrivals</em></h2>
        </div>
        <a href="${pageContext.request.contextPath}/products?sort=new" class="view-all">View All →</a>
      </div>
      <div class="product-scroll" id="newArrivalsScroll" aria-label="New arrivals"></div>
    </div>
  </section>

  <section class="section section-alt">
    <div class="section-inner">
      <div class="section-header">
        <div>
          <p class="section-eyebrow">Tailored for her</p>
          <h2 class="section-title">Women's <em>Collection</em></h2>
        </div>
        <a href="${pageContext.request.contextPath}/products?category=women" class="view-all">View All →</a>
      </div>
      <div class="product-scroll" id="womenScroll" aria-label="Women's collection"></div>
    </div>
  </section>

  <section class="section">
    <div class="section-inner">
      <div class="section-header">
        <div>
          <p class="section-eyebrow">Tailored for him</p>
          <h2 class="section-title">Men's <em>Collection</em></h2>
        </div>
        <a href="${pageContext.request.contextPath}/products?category=men" class="view-all">View All →</a>
      </div>
      <div class="product-scroll" id="menScroll" aria-label="Men's collection"></div>
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
