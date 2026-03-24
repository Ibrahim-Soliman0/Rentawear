<%-- catalog.jsp — Product Catalog Page
     Served by CatalogServlet at GET /catalog
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="layout.jsp">
    <jsp:param name="title"       value="${pageTitle} – Rentawear"/>
    <jsp:param name="description" value="Shop ${pageTitle} — rent premium fashion from £25/day."/>
    <jsp:param name="activePage"  value="catalog"/>
    <jsp:param name="extraCSS"    value="${pageContext.request.contextPath}/assets/css/catalog.css"/>
</jsp:include>

<main class="rw-main">
    <div class="catalog-wrap">

        <%-- Breadcrumb --%>
        <nav class="catalog-breadcrumb" id="catalogBreadcrumb" aria-label="Breadcrumb">
            <a href="${pageContext.request.contextPath}/">Home</a>
            <span class="catalog-breadcrumb-sep">›</span>
            <span id="catalogBreadcrumbMid"></span>
            <span id="catalogBreadcrumbCurrent">${pageTitle}</span>
        </nav>

        <%-- Page header --%>
        <div class="catalog-page-hd">
            <div>
                <p class="section-eyebrow" id="catalogEyebrow">${pageEyebrow}</p>
                <h1 class="catalog-page-title" id="catalogTitle">
                    ${pageTitle}
                    <span class="catalog-search-query" id="catalogSearchQuery"
                          style="display:none"></span>
                </h1>
                <button class="catalog-clear-search" id="catalogClearSearch"
                        type="button" style="display:none" aria-label="Clear search">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none"
                         stroke="currentColor" stroke-width="2.5"
                         stroke-linecap="round" aria-hidden="true">
                        <line x1="18" y1="6"  x2="6"  y2="18"/>
                        <line x1="6"  y1="6"  x2="18" y2="18"/>
                    </svg>
                    Clear search
                </button>
            </div>
            <p class="catalog-result-count" id="catalogResultCount" aria-live="polite"></p>
        </div>

        <%-- Active filter chips --%>
        <div class="catalog-chips" id="catalogChips" aria-label="Active filters"></div>

        <%-- Mobile filter toggle --%>
        <button class="catalog-filter-toggle" id="catalogFilterToggle"
                type="button" aria-controls="catalogSidebar" aria-expanded="false">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true">
                <line x1="4"  y1="6"  x2="20" y2="6"/>
                <line x1="4"  y1="12" x2="14" y2="12"/>
                <line x1="4"  y1="18" x2="9"  y2="18"/>
            </svg>
            Filters
            <span class="catalog-filter-badge" id="catalogFilterBadge" style="display:none"></span>
        </button>

        <div class="catalog-layout">

            <%-- ── SIDEBAR ────────────────────────────────────────────── --%>
            <aside class="catalog-sidebar" id="catalogSidebar"
                   role="complementary" aria-label="Product filters">

                <div class="catalog-sidebar-hd">
                    <span class="catalog-sidebar-lbl">Filters</span>
                    <button class="catalog-clear-btn" id="catalogClearAll" type="button">
                        Clear all
                    </button>
                    <button class="catalog-sidebar-close" id="catalogSidebarClose"
                            type="button" aria-label="Close filters">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" stroke-width="2.5"
                             stroke-linecap="round" aria-hidden="true">
                            <line x1="18" y1="6"  x2="6"  y2="18"/>
                            <line x1="6"  y1="6"  x2="18" y2="18"/>
                        </svg>
                    </button>
                </div>

                <%-- Gender --%>
                <div class="catalog-fgroup" id="fgroup-gender">
                    <div class="catalog-fgroup-hd" data-group="gender"
                         role="button" tabindex="0" aria-expanded="true">
                        <span>Gender</span>
                        <svg class="catalog-fgroup-chev" viewBox="0 0 24 24" aria-hidden="true">
                            <polyline points="6 9 12 15 18 9"/>
                        </svg>
                    </div>
                    <div class="catalog-fgroup-body">
                        <label class="catalog-chk-label">
                            <input type="radio" name="filterGender" value=""
                                   id="gAll" class="catalog-chk"/>
                            <span class="catalog-chk-box"></span>All
                        </label>
                        <label class="catalog-chk-label">
                            <input type="radio" name="filterGender" value="FEMALE"
                                   id="gFemale" class="catalog-chk"/>
                            <span class="catalog-chk-box"></span>Women
                        </label>
                        <label class="catalog-chk-label">
                            <input type="radio" name="filterGender" value="MALE"
                                   id="gMale" class="catalog-chk"/>
                            <span class="catalog-chk-box"></span>Men
                        </label>
                    </div>
                </div>

                <%-- Availability --%>
                <div class="catalog-fgroup" id="fgroup-avail">
                    <div class="catalog-fgroup-hd" data-group="avail"
                         role="button" tabindex="0" aria-expanded="true">
                        <span>Availability</span>
                        <svg class="catalog-fgroup-chev" viewBox="0 0 24 24" aria-hidden="true">
                            <polyline points="6 9 12 15 18 9"/>
                        </svg>
                    </div>
                    <div class="catalog-fgroup-body">
                        <label class="catalog-chk-label">
                            <input type="checkbox" id="filterNewOnly" class="catalog-chk"/>
                            <span class="catalog-chk-box"></span>New Arrivals Only
                        </label>
                    </div>
                </div>

                <%-- Categories --%>
                <div class="catalog-fgroup" id="fgroup-cats">
                    <div class="catalog-fgroup-hd" data-group="cats"
                         role="button" tabindex="0" aria-expanded="true">
                        <span>Categories</span>
                        <svg class="catalog-fgroup-chev" viewBox="0 0 24 24" aria-hidden="true">
                            <polyline points="6 9 12 15 18 9"/>
                        </svg>
                    </div>
                    <div class="catalog-fgroup-body">
                        <c:forEach var="cat" items="${filterCategories}">
                            <label class="catalog-chk-label" data-cat-gender="${cat.gender}">
                                <input type="checkbox"
                                       class="catalog-chk cat-chk"
                                       value="${cat.id}"
                                       data-name="${cat.name}"
                                       data-gender="${cat.gender}"/>
                                <span class="catalog-chk-box"></span>
                                    ${cat.name}
                                <span class="catalog-cat-tag">
                                        ${cat.gender == 'FEMALE' ? 'W' : 'M'}
                                </span>
                            </label>
                        </c:forEach>
                    </div>
                </div>

                <%-- Price Range — NEW markup, no Apply button --%>
                <div class="catalog-fgroup" id="fgroup-price">
                    <div class="catalog-fgroup-hd" data-group="price"
                         role="button" tabindex="0" aria-expanded="true">
                        <span>Price / day</span>
                        <svg class="catalog-fgroup-chev" viewBox="0 0 24 24" aria-hidden="true">
                            <polyline points="6 9 12 15 18 9"/>
                        </svg>
                    </div>
                    <div class="catalog-fgroup-body">
                        <div class="catalog-price-vals">
                        <span class="catalog-price-val">
                            £<span id="priceMinDisp">0</span>
                        </span>
                            <span class="catalog-price-val">
                            £<span id="priceMaxDisp">500</span>
                        </span>
                        </div>
                        <%-- Dual-handle slider — no Apply button, auto-applies on drag --%>
                        <div class="catalog-range-wrap">
                            <div class="catalog-range-track">
                                <div class="catalog-range-fill" id="priceRangeFill"></div>
                            </div>
                            <input type="range" class="catalog-range-input"
                                   id="priceRangeMin" min="0" max="500" value="0"
                                   step="1" aria-label="Minimum price per day"/>
                            <input type="range" class="catalog-range-input"
                                   id="priceRangeMax" min="0" max="500" value="500"
                                   step="1" aria-label="Maximum price per day"/>
                        </div>
                    </div>
                </div>

            </aside>

            <%-- ── PRODUCTS ───────────────────────────────────────────── --%>
            <div class="catalog-products">

                <div class="catalog-toolbar">
                    <div class="catalog-sort-wrap">
                        <label class="catalog-sort-lbl" for="catalogSort">Sort:</label>
                        <select class="catalog-sort-sel" id="catalogSort" aria-label="Sort products">
                            <option value="newest">Newest First</option>
                            <option value="price_asc">Price: Low to High</option>
                            <option value="price_desc">Price: High to Low</option>
                        </select>
                    </div>
                    <div class="catalog-grid-toggle" role="group" aria-label="Grid columns">
                        <button type="button" class="catalog-grid-btn active"
                                data-cols="4" title="4-column grid" aria-label="4 columns">
                            <svg viewBox="0 0 16 16" fill="currentColor" width="13" height="13" aria-hidden="true">
                                <rect x="0"   y="0"   width="3" height="3" rx="0.4"/>
                                <rect x="4.5" y="0"   width="3" height="3" rx="0.4"/>
                                <rect x="9"   y="0"   width="3" height="3" rx="0.4"/>
                                <rect x="13"  y="0"   width="3" height="3" rx="0.4"/>
                                <rect x="0"   y="4.5" width="3" height="3" rx="0.4"/>
                                <rect x="4.5" y="4.5" width="3" height="3" rx="0.4"/>
                                <rect x="9"   y="4.5" width="3" height="3" rx="0.4"/>
                                <rect x="13"  y="4.5" width="3" height="3" rx="0.4"/>
                            </svg>
                        </button>
                        <button type="button" class="catalog-grid-btn"
                                data-cols="2" title="2-column grid" aria-label="2 columns">
                            <svg viewBox="0 0 16 16" fill="currentColor" width="13" height="13" aria-hidden="true">
                                <rect x="0"   y="0"   width="6.5" height="3" rx="0.4"/>
                                <rect x="9.5" y="0"   width="6.5" height="3" rx="0.4"/>
                                <rect x="0"   y="4.5" width="6.5" height="3" rx="0.4"/>
                                <rect x="9.5" y="4.5" width="6.5" height="3" rx="0.4"/>
                            </svg>
                        </button>
                    </div>
                </div>

                <div class="catalog-grid" id="catalogGrid"
                     data-cols="4" aria-live="polite" aria-label="Products"></div>

                <div class="catalog-empty" id="catalogEmpty" style="display:none" role="status">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none"
                         stroke="var(--rw-stone)" stroke-width="1.2" aria-hidden="true">
                        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                        <line x1="3" y1="6" x2="21" y2="6"/>
                        <path d="M16 10a4 4 0 0 1-8 0"/>
                    </svg>
                    <h3 class="catalog-empty-title">No products found</h3>
                    <p class="catalog-empty-sub">Try adjusting your filters to find more items.</p>
                    <button class="btn-primary" id="catalogEmptyClear" type="button">
                        Clear Filters
                    </button>
                </div>

                <nav class="catalog-pagination" id="catalogPagination"
                     style="display:none" aria-label="Pagination">
                    <button class="catalog-pg-btn" id="pgPrev"
                            type="button" aria-label="Previous page" disabled>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true">
                            <polyline points="15 18 9 12 15 6"/>
                        </svg>
                    </button>
                    <div id="pgNums" style="display:flex;gap:4px;"></div>
                    <button class="catalog-pg-btn" id="pgNext"
                            type="button" aria-label="Next page">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true">
                            <polyline points="9 18 15 12 9 6"/>
                        </svg>
                    </button>
                </nav>

            </div><%-- /catalog-products --%>

        </div><%-- /catalog-layout --%>

    </div><%-- /catalog-wrap --%>

    <div class="catalog-sidebar-backdrop" id="catalogBackdrop" aria-hidden="true"></div>

</main>

<jsp:include page="components/footer.jsp"/>
<jsp:include page="components/scripts.jsp">
    <jsp:param name="extraJS"
               value="${pageContext.request.contextPath}/assets/js/catalog.js"/>
</jsp:include>
