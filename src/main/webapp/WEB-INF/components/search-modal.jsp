<%-- search-modal.jsp — Redesigned search modal
     Filter icon inside search bar opens gender + category panel.
     Active filters shown as chips. Price slider below chips.
     Search results below price slider.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="search-modal" id="searchModal" role="dialog"
     aria-label="Search" aria-hidden="true">
  <div class="search-backdrop" id="searchBackdrop"></div>

  <div class="search-panel">

    <%-- ── Input row ─────────────────────────────────────────── --%>
    <div class="search-input-row">
      <div class="search-field-wrap">

        <%-- Search icon (left) --%>
        <svg class="search-icon" viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="11" cy="11" r="7"/>
          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>

        <%-- Text input --%>
        <input type="text" class="search-input" id="searchInput"
               placeholder="Search dresses, suits, brands..."
               autocomplete="off" spellcheck="false" aria-label="Search input"/>

        <%-- Clear button — shown only when input has text --%>
        <button class="search-clear" id="searchClear"
                aria-label="Clear search" type="button">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <line x1="18" y1="6"  x2="6"  y2="18"/>
            <line x1="6"  y1="6"  x2="18" y2="18"/>
          </svg>
        </button>

        <%-- Divider --%>
        <span class="search-field-divider" aria-hidden="true"></span>

        <%-- Filter icon button (right of input) --%>
        <button class="search-filter-btn" id="searchFilterBtn"
                type="button" aria-label="Open filters"
                aria-expanded="false" aria-controls="searchFilterPanel">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
               stroke-width="2" stroke-linecap="round" aria-hidden="true">
            <line x1="4"  y1="6"  x2="20" y2="6"/>
            <line x1="8"  y1="12" x2="16" y2="12"/>
            <line x1="11" y1="18" x2="13" y2="18"/>
          </svg>
          <span class="search-filter-badge" id="searchFilterBadge"
                aria-hidden="true" style="display:none"></span>
        </button>
      </div>

      <button class="search-cancel" id="searchCancel" type="button">Cancel</button>
    </div>

    <%-- ── Body: filter column + results side by side ─────────── --%>
    <div class="search-body-row" id="searchBodyRow">

      <%-- Filter column — slides in from the left when toggled --%>
      <div class="search-filter-col" id="searchFilterPanel" aria-hidden="true">
        <div class="sfp-inner">

          <%-- Gender --%>
          <div class="sfp-section">
            <p class="sfp-label">Gender</p>
            <div class="sfp-radio-row">
              <label class="sfp-radio-label">
                <input type="radio" name="searchGender" value=""
                       class="sfp-radio" id="sgAll" checked/>
                <span class="sfp-radio-btn">All</span>
              </label>
              <label class="sfp-radio-label">
                <input type="radio" name="searchGender" value="FEMALE"
                       class="sfp-radio" id="sgFemale"/>
                <span class="sfp-radio-btn">Women</span>
              </label>
              <label class="sfp-radio-label">
                <input type="radio" name="searchGender" value="MALE"
                       class="sfp-radio" id="sgMale"/>
                <span class="sfp-radio-btn">Men</span>
              </label>
            </div>
          </div>

          <%-- Categories — filtered by selected gender via search.js --%>
          <div class="sfp-section">
            <p class="sfp-label">Categories</p>
            <div class="sfp-cats-grid" id="searchCatsGrid">
              <c:forEach var="cat" items="${navCategoriesFemale}">
                <label class="sfp-cat-label" data-cat-gender="FEMALE">
                  <input type="checkbox" class="sfp-cat-chk"
                         value="${cat.id}" data-name="${cat.name}"
                         data-gender="FEMALE"/>
                  <span class="sfp-cat-box"></span>
                    ${cat.name}
                  <span class="sfp-cat-tag">W</span>
                </label>
              </c:forEach>
              <c:forEach var="cat" items="${navCategoriesMale}">
                <label class="sfp-cat-label" data-cat-gender="MALE">
                  <input type="checkbox" class="sfp-cat-chk"
                         value="${cat.id}" data-name="${cat.name}"
                         data-gender="MALE"/>
                  <span class="sfp-cat-box"></span>
                    ${cat.name}
                  <span class="sfp-cat-tag">M</span>
                </label>
              </c:forEach>
            </div>
          </div>

          <%-- Price slider — always visible inside the filter column --%>
          <div class="sfp-section sfp-section--price" id="searchPriceWrap">
            <p class="sfp-label">Price / day</p>
            <div class="search-price-hd">
              <span class="search-price-vals">
                £<span id="searchPriceMinDisp">0</span>&nbsp;–&nbsp;£<span id="searchPriceMaxDisp">500</span>
              </span>
            </div>
            <div class="search-range-wrap">
              <div class="search-range-track">
                <div class="search-range-fill" id="searchRangeFill"></div>
              </div>
              <input type="range" class="search-range-input"
                     id="searchRangeMin" min="0" max="500" value="0"
                     step="1" aria-label="Minimum price per day"/>
              <input type="range" class="search-range-input"
                     id="searchRangeMax" min="0" max="500" value="500"
                     step="1" aria-label="Maximum price per day"/>
            </div>
          </div>

        </div><%-- /sfp-inner --%>

        <%-- Done button — visible only on mobile --%>
        <div class="sfp-footer">
          <button type="button" class="sfp-done-btn" id="searchFilterDone">Done</button>
        </div>

      </div><%-- /search-filter-col --%>

      <%-- Results column — always visible, scrolls independently --%>
      <div class="search-results-wrap" id="searchResultsWrap">
        <div class="search-results-inner">

          <%-- Active filter chips --%>
          <div class="search-chips" id="searchChips" style="display:none"></div>

          <%-- Trending — shown when no query --%>
          <div class="search-trending" id="searchTrending">
            <div class="search-trending-label">Trending searches</div>
            <div class="search-trending-pills">
              <button class="search-trending-pill" data-query="Evening dress" type="button">
                <svg viewBox="0 0 24 24" aria-hidden="true"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
                Evening dress
              </button>
              <button class="search-trending-pill" data-query="Wedding guest" type="button">
                <svg viewBox="0 0 24 24" aria-hidden="true"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
                Wedding guest
              </button>
              <button class="search-trending-pill" data-query="Black tie suit" type="button">
                <svg viewBox="0 0 24 24" aria-hidden="true"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
                Black tie suit
              </button>
              <button class="search-trending-pill" data-query="Midi dress" type="button">
                <svg viewBox="0 0 24 24" aria-hidden="true"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
                Midi dress
              </button>
              <button class="search-trending-pill" data-query="Cocktail" type="button">
                <svg viewBox="0 0 24 24" aria-hidden="true"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
                Cocktail
              </button>
            </div>
          </div>

          <%-- Results container — view-all link appended here by search.js --%>
          <div id="searchResults" style="display:none"></div>

        </div>
      </div><%-- /search-results-wrap --%>

    </div><%-- /search-body-row --%>

  </div><%-- /search-panel --%>
</div>
