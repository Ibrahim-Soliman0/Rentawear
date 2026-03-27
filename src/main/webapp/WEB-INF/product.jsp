<%-- product.jsp — Product Detail Page (PDP)
     Served by ProductPageServlet at GET /product/{id}

     Server renders: name, brand, price, description (SEO + no FOUC).
     JS (product.js) handles all interactivity.
     window.RW_PRODUCT = full ProductDetailDTO JSON
     window.RW_RELATED = List<ProductCardDTO> JSON
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="layout.jsp">
    <jsp:param name="title"       value="${pageTitle} – Rentawear"/>
    <jsp:param name="description" value="Rent ${pageTitle} — premium fashion from £25/day."/>
    <jsp:param name="activePage"  value="catalog"/>
    <jsp:param name="extraCSS"    value="${pageContext.request.contextPath}/assets/css/product.css"/>
</jsp:include>

<main class="rw-main">
    <div class="pdp-wrap">

        <%-- Breadcrumb — JS fills #pdpBcMid --%>
        <nav class="pdp-breadcrumb" id="pdpBreadcrumb" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/">Home</a>
            <span class="pdp-bc-sep">›</span>
            <span id="pdpBcMid"></span>
            <span class="pdp-bc-current" id="pdpBcCurrent">${pageTitle}</span>
        </nav>

        <div class="pdp-layout">

            <%-- ════════════════════════════════════════════════════
                 LEFT — GALLERY
                 ════════════════════════════════════════════════════ --%>
            <div class="pdp-gallery">

                <div class="pdp-main-img-wrap" id="pdpMainImgWrap">
                    <%-- Main image — JS sets src once colour is resolved --%>
                    <img class="pdp-main-img" id="pdpMainImg" alt="${pageTitle}"
                         loading="eager" decoding="async" style="display:none"/>

                    <%-- Placeholder shown until JS sets a real image --%>
                    <div class="pdp-img-placeholder" id="pdpImgPlaceholder">
                        <i class="bi bi-image"></i>
                    </div>

                    <span class="pdp-badge pdp-badge--new" id="pdpBadgeNew"
                          style="display:none">New</span>

                    <button class="pdp-zoom-btn" id="pdpZoomBtn" type="button"
                            aria-label="View full image">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" stroke-width="2"
                             stroke-linecap="round" aria-hidden="true">
                            <polyline points="15 3 21 3 21 9"/>
                            <polyline points="9 21 3 21 3 15"/>
                            <line x1="21" y1="3" x2="14" y2="10"/>
                            <line x1="3"  y1="21" x2="10" y2="14"/>
                        </svg>
                    </button>
                </div>

                <%-- Thumbnail strip — populated by JS --%>
                <div class="pdp-thumbs" id="pdpThumbs" aria-label="Product images"></div>

            </div>

            <%-- ════════════════════════════════════════════════════
                 RIGHT — DETAILS PANEL
                 ════════════════════════════════════════════════════ --%>
            <div class="pdp-details">

                <p class="pdp-brand" id="pdpBrand">rentawear</p>
                <h1 class="pdp-name"  id="pdpName">${pageTitle}</h1>

                <p class="pdp-price">
                    <span class="pdp-price-val"  id="pdpPriceVal">—</span>
                    <span class="pdp-price-unit">/day</span>
                </p>

                <div class="pdp-divider"></div>

                <%-- Colour swatches --%>
                <div class="pdp-section">
                    <div class="pdp-label-row">
                        <span class="pdp-label">Colour</span>
                        <span class="pdp-label-val" id="pdpColorName"></span>
                    </div>
                    <div class="pdp-colors" id="pdpColors"></div>
                </div>

                <%-- ── Sold-out colour banner ─────────────────────────
                     Shown by JS when the selected colour has 0 stock.
                     Hidden by default.                                --%>
                <div class="pdp-soldout-banner" id="pdpSoldOutBanner" style="display:none">
                    <i class="bi bi-slash-circle"></i>
                    <div>
                        <strong>This colour is sold out</strong>
                        <span>Select another colour to continue, or check back soon.</span>
                    </div>
                </div>

                <%-- Size selector --%>
                <div class="pdp-section">
                    <div class="pdp-label-row">
                        <span class="pdp-label">Size</span>
                        <span class="pdp-label-val" id="pdpStockLabel"></span>
<%--                        <span class="pdp-size-guide">Size Guide</span>--%>
                    </div>
                    <div class="pdp-sizes" id="pdpSizes"></div>
                </div>

                <div class="pdp-divider"></div>

                <%-- Rental date pickers --%>
                <div class="pdp-section" id="pdpDatesSection">
                    <div class="pdp-label-row">
                        <span class="pdp-label">Rental Dates</span>
                        <span class="pdp-date-summary" id="pdpDateSummary"></span>
                    </div>
                    <%-- Info line: earliest available and max duration --%>
                    <p class="pdp-date-hint">
                        <i class="bi bi-info-circle"></i>
                        Book from 2 days in advance &middot; Max 30-day rental
                    </p>
                    <div class="pdp-date-inputs">
                        <div class="pdp-date-field" id="pdpStartField">
                            <label class="pdp-date-label" for="pdpStartDate">From</label>
                            <input type="text" id="pdpStartDate"
                                   class="pdp-date-input" placeholder="DD/MM/YYYY"
                                   autocomplete="off" readonly/>
                        </div>
                        <span class="pdp-date-sep">→</span>
                        <div class="pdp-date-field" id="pdpEndField">
                            <label class="pdp-date-label" for="pdpEndDate">To</label>
                            <input type="text" id="pdpEndDate"
                                   class="pdp-date-input" placeholder="DD/MM/YYYY"
                                   autocomplete="off" readonly/>
                        </div>
                    </div>
                </div>

                <%-- Nudge message (missing dates / sold out) --%>
                <p class="pdp-nudge" id="pdpNudge" role="alert" aria-live="polite"></p>

                <%-- Add to Bag --%>
                <button class="pdp-atb" id="pdpAtb" type="button" disabled>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"
                         aria-hidden="true">
                        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                        <line x1="3" y1="6" x2="21" y2="6"/>
                        <path d="M16 10a4 4 0 0 1-8 0"/>
                    </svg>
                    <span>Add to Bag</span>
                </button>

<%--                <button class="pdp-wishlist" type="button" id="pdpWishlist">--%>
<%--                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"--%>
<%--                         stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"--%>
<%--                         aria-hidden="true">--%>
<%--                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>--%>
<%--                    </svg>--%>
<%--                    Add to Wishlist--%>
<%--                </button>--%>

                <div class="pdp-divider"></div>

                <%-- Description accordion (open by default) --%>
                <div class="pdp-accordion" id="pdpDescSection">
                    <button class="pdp-acc-trigger" id="pdpDescToggle" type="button">
                        Description
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                             aria-hidden="true">
                            <polyline points="6 9 12 15 18 9"/>
                        </svg>
                    </button>
                    <div class="pdp-acc-body">
                        <div class="pdp-acc-body-inner">
                            <p id="pdpDesc"></p>
                        </div>
                    </div>
                </div>

                <%-- Trust badges --%>
                <div class="pdp-trust-row">
                    <div class="pdp-trust-item">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                             stroke="var(--rw-teal)" stroke-width="1.6" aria-hidden="true">
                            <rect x="3" y="4" width="18" height="18" rx="2"/>
                            <line x1="3" y1="10" x2="21" y2="10"/>
                        </svg>
                        Delivered before your event
                    </div>
                    <div class="pdp-trust-item">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                             stroke="var(--rw-teal)" stroke-width="1.6" aria-hidden="true">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                            <polyline points="22 4 12 14.01 9 11.01"/>
                        </svg>
                        Dry-cleaned &amp; insured
                    </div>
                    <div class="pdp-trust-item">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                             stroke="var(--rw-teal)" stroke-width="1.6" aria-hidden="true">
                            <polyline points="1 4 1 10 7 10"/>
                            <path d="M3.51 15a9 9 0 1 0 .49-3.5"/>
                        </svg>
                        Easy 48-hr return
                    </div>
                </div>

            </div><%-- /pdp-details --%>

        </div><%-- /pdp-layout --%>

        <%-- ════════════════════════════════════════════════════
             RELATED PRODUCTS
             ════════════════════════════════════════════════════ --%>
        <section class="pdp-related" id="pdpRelatedSection">
            <div class="pdp-related-hd">
                <p class="section-eyebrow">Complete the look</p>
                <h2 class="section-title">You May Also <em>Like</em></h2>
            </div>
            <div class="product-scroll" id="pdpRelatedGrid"></div>
        </section>

    </div><%-- /pdp-wrap --%>

    <%-- LIGHTBOX --%>
    <div class="pdp-lightbox" id="pdpLightbox" aria-hidden="true">
        <div class="pdp-lightbox-backdrop" id="pdpLightboxBackdrop"></div>
        <button class="pdp-lightbox-close" id="pdpLightboxClose" type="button" aria-label="Close">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true">
                <line x1="18" y1="6"  x2="6"  y2="18"/>
                <line x1="6"  y1="6"  x2="18" y2="18"/>
            </svg>
        </button>
        <img class="pdp-lightbox-img" id="pdpLightboxImg" alt=""/>
        <button class="pdp-lightbox-nav pdp-lightbox-prev" id="pdpLightboxPrev"
                type="button" aria-label="Previous image">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true">
                <polyline points="15 18 9 12 15 6"/>
            </svg>
        </button>
        <button class="pdp-lightbox-nav pdp-lightbox-next" id="pdpLightboxNext"
                type="button" aria-label="Next image">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true">
                <polyline points="9 18 15 12 9 6"/>
            </svg>
        </button>
    </div>

</main>

<jsp:include page="components/footer.jsp"/>

<script>
    window.RW_PRODUCT = <%= request.getAttribute("productJson") %>;
    window.RW_RELATED = <%= request.getAttribute("relatedJson") %>;
</script>

<jsp:include page="components/scripts.jsp">
    <jsp:param name="extraJS"
               value="${pageContext.request.contextPath}/assets/js/product.js"/>
</jsp:include>
