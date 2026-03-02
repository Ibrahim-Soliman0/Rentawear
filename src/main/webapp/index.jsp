<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page isELIgnored="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="description" content="RentaWear — Rent designer dresses, suits, and luxury fashion for any occasion. The premier clothing rental platform." />
  <meta name="keywords" content="clothing rental, dress rental, luxury fashion, designer clothes, rent a dress, suit rental" />
  <meta property="og:title" content="RentaWear — Rent the Runway. Own the Moment." />
  <meta property="og:description" content="Access the world's best closet. Rent designer clothing for weddings, galas, vacations, and everyday luxury." />
  <meta property="og:type" content="website" />
  <meta property="og:image" content="${pageContext.request.contextPath}/assets/images/og-image.jpg" />
  <meta property="og:url" content="${pageContext.request.contextPath}/" />
  <title>RentaWear — Rent Designer Fashion</title>
  <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/assets/images/favicon.ico" />
  <!-- Bootstrap 5 CSS -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" />
  <!-- Bootstrap Icons -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" />
  <!-- Custom Theme -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/custom-theme.css" />
</head>
<body>

<%-- ============================================================
     NAVBAR
     ============================================================ --%>

<%-- ============================================================
     CATEGORY FILTER BAR
     ============================================================ --%>
<section aria-label="Category filters">
  <div class="category-filters">
    <button class="category-filter-btn active" aria-pressed="true">All Items</button>
    <c:forEach var="category" items="${categories}">
      <button class="category-filter-btn" aria-pressed="false"><c:out value="${category.name}" /></button>
    </c:forEach>
    <%-- Fallback static categories when no dynamic data --%>
    <c:if test="${empty categories}">
      <button class="category-filter-btn">Dresses</button>
      <button class="category-filter-btn">Suits &amp; Tailoring</button>
      <button class="category-filter-btn">Streetwear</button>
      <button class="category-filter-btn">Accessories</button>
      <button class="category-filter-btn">Outerwear</button>
    </c:if>
  </div>
</section>

<main>
  <div class="container py-5">

    <%-- ============================================================
         TRENDING RENTALS SECTION
         ============================================================ --%>
    <section class="mb-5" aria-labelledby="trending-heading">
      <div class="section-header mb-4">
        <h2 id="trending-heading">Trending Rentals</h2>
        <a href="${pageContext.request.contextPath}/explore?sort=trending" class="section-header__link" aria-label="See all trending rentals">See all</a>
      </div>

      <div class="row g-3 g-md-4">
        <c:choose>
          <c:when test="${not empty trendingProducts}">
            <c:forEach var="product" items="${trendingProducts}" varStatus="loop">
              <c:if test="${loop.index < 4}">
                <div class="col-6 col-lg-3">
                  <article class="product-card" role="article">
                    <a href="${pageContext.request.contextPath}/product/${product.id}" class="d-block text-decoration-none">
                      <div class="product-card__image-wrap">
                        <img src="${pageContext.request.contextPath}/assets/images/products/<c:out value='${product.imageFile}'/>"
                             alt="<c:out value='${product.name}'/>"
                             loading="lazy"
                             width="280" height="373" />
                        <c:if test="${product.popular}">
                          <span class="product-card__badge">Popular</span>
                        </c:if>
                        <button class="product-card__wishlist"
                                aria-label="Add ${product.name} to wishlist"
                                data-product-id="${product.id}">
                          <i class="bi bi-heart" aria-hidden="true"></i>
                        </button>
                      </div>
                      <div class="product-card__body">
                        <p class="product-card__brand"><c:out value="${product.brand}" /></p>
                        <h3 class="product-card__name"><c:out value="${product.name}" /></h3>
                        <p class="product-card__price">
                          <strong>$<fmt:formatNumber value="${product.pricePerDay}" pattern="#,##0" /></strong>
                          <span> / <c:out value="${product.rentalPeriodLabel}" default="4 days" /></span>
                        </p>
                      </div>
                    </a>
                  </article>
                </div>
              </c:if>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <%-- Static fallback data --%>
            <div class="col-6 col-lg-3">
              <article class="product-card">
                <a href="${pageContext.request.contextPath}/product/1" class="d-block text-decoration-none">
                  <div class="product-card__image-wrap">
                    <img src="${pageContext.request.contextPath}/assets/images/products/emerald-silk-dress.jpg"
                         alt="Emerald Silk Dress by Reformation" loading="lazy" width="280" height="373" />
                    <span class="product-card__badge">Popular</span>
                    <button class="product-card__wishlist" aria-label="Add Emerald Silk Dress to wishlist">
                      <i class="bi bi-heart" aria-hidden="true"></i>
                    </button>
                  </div>
                  <div class="product-card__body">
                    <p class="product-card__brand">Reformation</p>
                    <h3 class="product-card__name">Emerald Silk Dress</h3>
                    <p class="product-card__price"><strong>$45</strong> <span>/ 4 days</span></p>
                  </div>
                </a>
              </article>
            </div>
            <div class="col-6 col-lg-3">
              <article class="product-card">
                <a href="${pageContext.request.contextPath}/product/2" class="d-block text-decoration-none">
                  <div class="product-card__image-wrap">
                    <img src="${pageContext.request.contextPath}/assets/images/products/navy-tailored-suit.jpg"
                         alt="Navy Tailored Suit by Hugo Boss" loading="lazy" width="280" height="373" />
                    <button class="product-card__wishlist" aria-label="Add Navy Tailored Suit to wishlist">
                      <i class="bi bi-heart" aria-hidden="true"></i>
                    </button>
                  </div>
                  <div class="product-card__body">
                    <p class="product-card__brand">Hugo Boss</p>
                    <h3 class="product-card__name">Navy Tailored Suit</h3>
                    <p class="product-card__price"><strong>$85</strong> <span>/ 4 days</span></p>
                  </div>
                </a>
              </article>
            </div>
            <div class="col-6 col-lg-3">
              <article class="product-card">
                <a href="${pageContext.request.contextPath}/product/3" class="d-block text-decoration-none">
                  <div class="product-card__image-wrap">
                    <img src="${pageContext.request.contextPath}/assets/images/products/classic-trench-coat.jpg"
                         alt="Classic Trench Coat by Burberry" loading="lazy" width="280" height="373" />
                    <button class="product-card__wishlist" aria-label="Add Classic Trench Coat to wishlist">
                      <i class="bi bi-heart" aria-hidden="true"></i>
                    </button>
                  </div>
                  <div class="product-card__body">
                    <p class="product-card__brand">Burberry</p>
                    <h3 class="product-card__name">Classic Trench Coat</h3>
                    <p class="product-card__price"><strong>$120</strong> <span>/ 7 days</span></p>
                  </div>
                </a>
              </article>
            </div>
            <div class="col-6 col-lg-3">
              <article class="product-card">
                <a href="${pageContext.request.contextPath}/product/4" class="d-block text-decoration-none">
                  <div class="product-card__image-wrap">
                    <img src="${pageContext.request.contextPath}/assets/images/products/black-evening-gown.jpg"
                         alt="Black Evening Gown by Valentino" loading="lazy" width="280" height="373" />
                    <button class="product-card__wishlist" aria-label="Add Black Evening Gown to wishlist">
                      <i class="bi bi-heart" aria-hidden="true"></i>
                    </button>
                  </div>
                  <div class="product-card__body">
                    <p class="product-card__brand">Valentino</p>
                    <h3 class="product-card__name">Black Evening Gown</h3>
                    <p class="product-card__price"><strong>$150</strong> <span>/ 4 days</span></p>
                  </div>
                </a>
              </article>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </section>

    <%-- ============================================================
         SHOP BY OCCASION SECTION
         ============================================================ --%>
    <section class="mb-5" aria-labelledby="occasions-heading">
      <div class="section-header mb-4">
        <h2 id="occasions-heading">Shop by Occasion</h2>
        <a href="${pageContext.request.contextPath}/explore" class="section-header__link">Explore all</a>
      </div>

      <div class="row g-3">
        <c:choose>
          <c:when test="${not empty occasions}">
            <c:forEach var="occasion" items="${occasions}">
              <div class="col-6 col-lg-3">
                <a href="${pageContext.request.contextPath}/explore?occasion=${occasion.slug}" class="d-block text-decoration-none">
                  <div class="occasion-card">
                    <img src="${pageContext.request.contextPath}/assets/images/occasions/<c:out value='${occasion.imageFile}'/>"
                         alt="<c:out value='${occasion.name}'/> fashion" loading="lazy"
                         width="300" height="300" />
                    <div class="occasion-card__overlay">
                      <span class="occasion-card__label"><c:out value="${occasion.name}" /></span>
                    </div>
                  </div>
                </a>
              </div>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <%-- Static fallback occasions --%>
            <c:forEach var="occ" items="${['Wedding Guest','Vacation','Black Tie','Everyday Cool']}">
              <div class="col-6 col-lg-3">
                <a href="${pageContext.request.contextPath}/explore?occasion=${occ.toLowerCase().replace(' ','-')}" class="d-block text-decoration-none">
                  <div class="occasion-card">
                    <img src="${pageContext.request.contextPath}/assets/images/occasions/${occ.toLowerCase().replace(' ','-')}.jpg"
                         alt="${occ} fashion" loading="lazy" width="300" height="300" />
                    <div class="occasion-card__overlay">
                      <span class="occasion-card__label">${occ}</span>
                    </div>
                  </div>
                </a>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>
    </section>

    <%-- ============================================================
         STATS / VALUE PROPOSITION SECTION
         ============================================================ --%>
    <section class="mb-5 py-5 px-4 rounded-3 bg-white shadow-sm" aria-labelledby="stats-heading">
      <h2 id="stats-heading" class="visually-hidden">Our Numbers</h2>
      <div class="row text-center g-4">
        <div class="col-6 col-md-3">
          <p class="h2 fw-bold mb-1" style="color: var(--rw-primary);">5,000+</p>
          <p class="text-muted small mb-0">Designer Pieces</p>
        </div>
        <div class="col-6 col-md-3">
          <p class="h2 fw-bold mb-1" style="color: var(--rw-primary);">200+</p>
          <p class="text-muted small mb-0">Luxury Brands</p>
        </div>
        <div class="col-6 col-md-3">
          <p class="h2 fw-bold mb-1" style="color: var(--rw-primary);">50K+</p>
          <p class="text-muted small mb-0">Happy Members</p>
        </div>
        <div class="col-6 col-md-3">
          <p class="h2 fw-bold mb-1" style="color: var(--rw-primary);">4.9★</p>
          <p class="text-muted small mb-0">Average Rating</p>
        </div>
      </div>
    </section>

    <%-- ============================================================
         FEATURES / HOW IT WORKS SECTION
         ============================================================ --%>
    <section class="mb-5" aria-labelledby="how-it-works-heading">
      <div class="text-center mb-4">
        <h2 id="how-it-works-heading">How It Works</h2>
        <p class="lead mx-auto" style="max-width: 520px;">Renting luxury fashion has never been easier. Three simple steps.</p>
      </div>
      <div class="row g-4">
        <div class="col-md-4">
          <div class="text-center p-4">
            <div class="mb-3 mx-auto d-flex align-items-center justify-content-center rounded-circle bg-white shadow-sm"
                 style="width:64px;height:64px;border:1.5px solid var(--rw-border);">
              <i class="bi bi-search fs-4" style="color:var(--rw-primary);" aria-hidden="true"></i>
            </div>
            <h3 class="h5 fw-bold mb-2">Browse &amp; Pick</h3>
            <p class="text-muted small mb-0">Filter by occasion, size, brand, or rental period. Find your perfect look in seconds.</p>
          </div>
        </div>
        <div class="col-md-4">
          <div class="text-center p-4">
            <div class="mb-3 mx-auto d-flex align-items-center justify-content-center rounded-circle bg-white shadow-sm"
                 style="width:64px;height:64px;border:1.5px solid var(--rw-border);">
              <i class="bi bi-calendar-check fs-4" style="color:var(--rw-primary);" aria-hidden="true"></i>
            </div>
            <h3 class="h5 fw-bold mb-2">Choose Your Dates</h3>
            <p class="text-muted small mb-0">Select your rental window. We deliver straight to your door before your event.</p>
          </div>
        </div>
        <div class="col-md-4">
          <div class="text-center p-4">
            <div class="mb-3 mx-auto d-flex align-items-center justify-content-center rounded-circle bg-white shadow-sm"
                 style="width:64px;height:64px;border:1.5px solid var(--rw-border);">
              <i class="bi bi-arrow-return-left fs-4" style="color:var(--rw-primary);" aria-hidden="true"></i>
            </div>
            <h3 class="h5 fw-bold mb-2">Wear &amp; Return</h3>
            <p class="text-muted small mb-0">Wear it, love it, return it. We handle cleaning. No hassle, no dry-cleaning bills.</p>
          </div>
        </div>
      </div>
    </section>

    <%-- ============================================================
         NEW ARRIVALS SECTION
         ============================================================ --%>
    <section class="mb-5" aria-labelledby="new-arrivals-heading">
      <div class="section-header mb-4">
        <h2 id="new-arrivals-heading">New Arrivals</h2>
        <a href="${pageContext.request.contextPath}/explore?sort=newest" class="section-header__link">See all</a>
      </div>
      <div class="row g-3 g-md-4">
        <c:choose>
          <c:when test="${not empty newArrivals}">
            <c:forEach var="item" items="${newArrivals}" varStatus="st">
              <c:if test="${st.index < 4}">
                <div class="col-6 col-lg-3">
                  <article class="product-card">
                    <a href="${pageContext.request.contextPath}/product/${item.id}" class="d-block text-decoration-none">
                      <div class="product-card__image-wrap">
                        <img src="${pageContext.request.contextPath}/assets/images/products/<c:out value='${item.imageFile}'/>"
                             alt="<c:out value='${item.name}'/>" loading="lazy" width="280" height="373" />
                        <button class="product-card__wishlist" aria-label="Add <c:out value='${item.name}'/> to wishlist">
                          <i class="bi bi-heart" aria-hidden="true"></i>
                        </button>
                      </div>
                      <div class="product-card__body">
                        <p class="product-card__brand"><c:out value="${item.brand}" /></p>
                        <h3 class="product-card__name"><c:out value="${item.name}" /></h3>
                        <p class="product-card__price">
                          <strong>$<fmt:formatNumber value="${item.pricePerDay}" pattern="#,##0" /></strong>
                          <span> / <c:out value="${item.rentalPeriodLabel}" default="4 days" /></span>
                        </p>
                      </div>
                    </a>
                  </article>
                </div>
              </c:if>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <%-- Placeholder cards --%>
            <c:forEach begin="1" end="4" var="i">
              <div class="col-6 col-lg-3">
                <article class="product-card">
                  <div class="product-card__image-wrap bg-light">
                    <img src="${pageContext.request.contextPath}/assets/images/products/placeholder.jpg"
                         alt="New arrival item ${i}" loading="lazy" width="280" height="373" />
                    <button class="product-card__wishlist" aria-label="Add item to wishlist">
                      <i class="bi bi-heart" aria-hidden="true"></i>
                    </button>
                  </div>
                  <div class="product-card__body">
                    <p class="product-card__brand">Designer Brand</p>
                    <h3 class="product-card__name">Luxury Piece</h3>
                    <p class="product-card__price"><strong>$—</strong> <span>/ 4 days</span></p>
                  </div>
                </article>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>
    </section>

    <%-- ============================================================
         TESTIMONIALS SECTION
         ============================================================ --%>
    <section class="mb-5" aria-labelledby="testimonials-heading">
      <div class="text-center mb-4">
        <h2 id="testimonials-heading">What Our Members Say</h2>
      </div>
      <div class="row g-4">
        <c:choose>
          <c:when test="${not empty testimonials}">
            <c:forEach var="t" items="${testimonials}" varStatus="ts">
              <c:if test="${ts.index < 3}">
                <div class="col-md-4">
                  <div class="card h-100 p-4">
                    <div class="card-body p-0">
                      <div class="mb-3" aria-label="${t.rating} out of 5 stars">
                        <c:forEach begin="1" end="5" var="star">
                          <i class="bi bi-star-fill small" style="color:${star <= t.rating ? '#f5a623' : '#dee2e6'};" aria-hidden="true"></i>
                        </c:forEach>
                      </div>
                      <blockquote class="border-0 p-0 mb-3">
                        <p class="text-secondary" style="font-size:0.9rem;line-height:1.65;">
                          "<c:out value="${t.review}" />"
                        </p>
                      </blockquote>
                      <footer class="d-flex align-items-center gap-2">
                        <img src="${pageContext.request.contextPath}/assets/images/avatars/<c:out value='${t.avatarFile}'/>"
                             alt="<c:out value='${t.author}'/>" loading="lazy"
                             class="profile-avatar" style="width:36px;height:36px;" />
                        <div>
                          <p class="mb-0 fw-semibold" style="font-size:0.875rem;"><c:out value="${t.author}" /></p>
                          <p class="mb-0 text-muted" style="font-size:0.75rem;"><c:out value="${t.location}" /></p>
                        </div>
                      </footer>
                    </div>
                  </div>
                </div>
              </c:if>
            </c:forEach>
          </c:when>
          <c:otherwise>
            <div class="col-md-4">
              <div class="card h-100 p-4">
                <div class="card-body p-0">
                  <div class="mb-3">
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                  </div>
                  <blockquote class="border-0 p-0 mb-3">
                    <p class="text-secondary" style="font-size:0.9rem;line-height:1.65;">
                      "RentaWear has completely transformed how I approach event dressing. It's sustainable, high-end, and absolutely effortless."
                    </p>
                  </blockquote>
                  <footer class="d-flex align-items-center gap-2">
                    <div class="rounded-circle bg-light d-flex align-items-center justify-content-center"
                         style="width:36px;height:36px;flex-shrink:0;">
                      <i class="bi bi-person text-muted" aria-hidden="true"></i>
                    </div>
                    <div>
                      <p class="mb-0 fw-semibold" style="font-size:0.875rem;">Sarah Jenkins</p>
                      <p class="mb-0 text-muted" style="font-size:0.75rem;">Corporate Lawyer, NYC</p>
                    </div>
                  </footer>
                </div>
              </div>
            </div>
            <div class="col-md-4">
              <div class="card h-100 p-4">
                <div class="card-body p-0">
                  <div class="mb-3">
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                  </div>
                  <blockquote class="border-0 p-0 mb-3">
                    <p class="text-secondary" style="font-size:0.9rem;line-height:1.65;">
                      "I wore a Gucci gown to my sister's wedding that would have cost $3,000 to buy. I rented it for $150. Absolute game changer."
                    </p>
                  </blockquote>
                  <footer class="d-flex align-items-center gap-2">
                    <div class="rounded-circle bg-light d-flex align-items-center justify-content-center"
                         style="width:36px;height:36px;flex-shrink:0;">
                      <i class="bi bi-person text-muted" aria-hidden="true"></i>
                    </div>
                    <div>
                      <p class="mb-0 fw-semibold" style="font-size:0.875rem;">Elena Rodriguez</p>
                      <p class="mb-0 text-muted" style="font-size:0.75rem;">Software Engineer, LA</p>
                    </div>
                  </footer>
                </div>
              </div>
            </div>
            <div class="col-md-4">
              <div class="card h-100 p-4">
                <div class="card-body p-0">
                  <div class="mb-3">
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                    <i class="bi bi-star-fill small" style="color:#f5a623;" aria-hidden="true"></i>
                    <i class="bi bi-star-half small" style="color:#f5a623;" aria-hidden="true"></i>
                  </div>
                  <blockquote class="border-0 p-0 mb-3">
                    <p class="text-secondary" style="font-size:0.9rem;line-height:1.65;">
                      "The Tom Ford suit I rented for my board presentation arrived perfectly pressed. The service is exceptional from start to finish."
                    </p>
                  </blockquote>
                  <footer class="d-flex align-items-center gap-2">
                    <div class="rounded-circle bg-light d-flex align-items-center justify-content-center"
                         style="width:36px;height:36px;flex-shrink:0;">
                      <i class="bi bi-person text-muted" aria-hidden="true"></i>
                    </div>
                    <div>
                      <p class="mb-0 fw-semibold" style="font-size:0.875rem;">David Chen</p>
                      <p class="mb-0 text-muted" style="font-size:0.75rem;">Marketing Director, Chicago</p>
                    </div>
                  </footer>
                </div>
              </div>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </section>

    <%-- ============================================================
         CTA BANNER
         ============================================================ --%>
    <section class="mb-5 rounded-3 p-5 text-center text-white"
             style="background: linear-gradient(135deg, #1aab96 0%, #148a78 100%);"
             aria-labelledby="cta-heading">
      <h2 id="cta-heading" class="text-white mb-2" style="font-size:clamp(1.5rem,3vw,2rem);">
        Rent the Runway. Own the Moment.
      </h2>
      <p class="mb-4" style="color:rgba(255,255,255,0.85);font-size:1rem;max-width:480px;margin:0 auto 1.5rem;">
        Join the premier luxury clothing rental platform and get unlimited access to the world's best closet.
      </p>
      <c:choose>
        <c:when test="${not empty currentUser}">
          <a href="${pageContext.request.contextPath}/explore" class="btn btn-lg"
             style="background:var(--rw-white);color:var(--rw-primary);font-weight:700;"
             aria-label="Start browsing designer fashion">
            Browse Now
          </a>
        </c:when>
        <c:otherwise>
          <a href="${pageContext.request.contextPath}/register" class="btn btn-lg me-2"
             style="background:var(--rw-white);color:var(--rw-primary);font-weight:700;"
             aria-label="Create a free account">
            Join Free
          </a>
          <a href="${pageContext.request.contextPath}/explore" class="btn btn-lg btn-outline-light"
             aria-label="Explore the catalog">
            Explore Catalog
          </a>
        </c:otherwise>
      </c:choose>
    </section>

    <%-- ============================================================
         NEWSLETTER SIGNUP SECTION
         ============================================================ --%>
    <section class="mb-5 p-5 rounded-3 bg-white shadow-sm" aria-labelledby="newsletter-heading">
      <div class="row align-items-center g-4">
        <div class="col-md-6">
          <h2 id="newsletter-heading" class="mb-2">Stay in Style</h2>
          <p class="text-muted mb-0">Get the latest arrivals, exclusive member discounts, and style inspiration delivered to your inbox.</p>
        </div>
        <div class="col-md-6">
          <form action="${pageContext.request.contextPath}/newsletter/subscribe" method="post" novalidate>
            <div class="promo-input-group">
              <label for="newsletter-email" class="visually-hidden">Email address</label>
              <input type="email" id="newsletter-email" name="email" class="form-control"
                     placeholder="Your email address" required autocomplete="email" />
              <button type="submit" class="btn btn-primary">Subscribe</button>
            </div>
            <p class="text-muted" style="font-size:0.75rem;margin-top:-0.25rem;">
              No spam, ever. Unsubscribe at any time.
            </p>
          </form>
        </div>
      </div>
    </section>

  </div><%-- /container --%>
</main>

<%-- ============================================================
     FOOTER
     ============================================================ --%>

<!-- Bootstrap 5 JS Bundle -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

<script>
  /* Category filter toggle */
  document.querySelectorAll('.category-filter-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      document.querySelectorAll('.category-filter-btn').forEach(function(b) {
        b.classList.remove('active');
        b.setAttribute('aria-pressed', 'false');
      });
      this.classList.add('active');
      this.setAttribute('aria-pressed', 'true');
    });
  });

  /* Wishlist heart toggle */
  document.querySelectorAll('.product-card__wishlist').forEach(function(btn) {
    btn.addEventListener('click', function(e) {
      e.preventDefault();
      var icon = this.querySelector('i');
      if (icon.classList.contains('bi-heart')) {
        icon.classList.replace('bi-heart', 'bi-heart-fill');
        icon.style.color = 'var(--rw-danger)';
        this.setAttribute('aria-label', this.getAttribute('aria-label').replace('Add', 'Remove'));
      } else {
        icon.classList.replace('bi-heart-fill', 'bi-heart');
        icon.style.color = '';
        this.setAttribute('aria-label', this.getAttribute('aria-label').replace('Remove', 'Add'));
      }
    });
  });
</script>

</body>
</html>
