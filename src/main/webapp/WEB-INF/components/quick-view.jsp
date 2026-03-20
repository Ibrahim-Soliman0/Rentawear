<%-- quick-view.jsp - Quick view overlay for redesign cards
     All element IDs are unchanged — quick-view.js targets IDs only.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@ page isELIgnored="false" %>

<div class="qv-overlay" id="qvOverlay" role="dialog" aria-modal="true" aria-label="Quick View">
  <div class="qv-backdrop" id="qvBackdrop"></div>

  <div class="qv-modal">

    <%-- Image panel --%>
    <div class="qv-image-panel">
      <div class="qv-img-frame">
        <img class="qv-main-img" id="qvMainImg" alt="" />
      </div>
      <div class="qv-thumbs" id="qvThumbs"></div>
      <button class="qv-close" id="qvClose" aria-label="Close quick view" type="button">
        <svg viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
          <line x1="18" y1="6" x2="6" y2="18"/>
          <line x1="6"  y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>

    <%-- Details panel --%>
    <div class="qv-details">

      <div class="qv-brand" id="qvBrand"></div>
      <div class="qv-name"  id="qvName"></div>
      <div class="qv-price-row">
        <span class="qv-price-main" id="qvPrice"></span>
      </div>
      <div class="qv-divider"></div>

      <%-- Colour label + swatches --%>
      <div class="qv-color-label" id="qvColorLabel" style="display:none; align-items:center; gap:6px; margin-bottom:8px;">
        <span class="qv-label" style="margin-bottom:0;">Colour</span>
        <span class="qv-color-name-val" id="qvColorName"></span>
      </div>
      <div class="swatch-row" id="qvColors" style="margin-bottom:20px;"></div>

      <%-- Size selector --%>
      <div class="qv-label">Select Size</div>
      <div class="qv-sizes" id="qvSizes"></div>

      <div class="qv-divider"></div>

      <%-- Rental dates --%>
      <div class="qv-dates">
        <div class="qv-label">Rental Dates</div>
        <div class="qv-date-inputs">
          <div class="qv-date-field" id="qvStartField">
            <label for="qvStartDate">From</label>
            <input type="text" id="qvStartDate" placeholder="DD/MM/YYYY" autocomplete="off" readonly>
          </div>
          <span class="qv-date-sep">→</span>
          <div class="qv-date-field" id="qvEndField">
            <label for="qvEndDate">To</label>
            <input type="text" id="qvEndDate" placeholder="DD/MM/YYYY" autocomplete="off" readonly>
          </div>
        </div>
        <div class="qv-date-summary" id="qvDateSummary"></div>
      </div>

      <%-- Nudge alert — shown when ATB clicked without dates --%>
      <div class="qv-nudge" id="qvNudge" role="alert" aria-live="polite"></div>

      <button class="qv-atb" id="qvAdd" type="button" disabled>
        <svg viewBox="0 0 24 24" aria-hidden="true"
             fill="none" stroke="currentColor"
             stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
          <line x1="3" y1="6" x2="21" y2="6"/>
          <path d="M16 10a4 4 0 0 1-8 0"/>
        </svg>
        <span>Add to Bag</span>
      </button>

      <button class="qv-wishlist-btn" id="qvWish" type="button">
        <svg viewBox="0 0 24 24" aria-hidden="true"
             fill="none" stroke="currentColor"
             stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
        </svg>
        <span>Add to Wishlist</span>
      </button>

      <%--      &lt;%&ndash; View full product page link — populated by quick-view.js &ndash;%&gt;--%>
      <%--      <a class="qv-view-full" id="qvViewFull" href="#" style="display:none;">View full details</a>--%>

      <%-- Description accordion at bottom --%>
      <div class="qv-accordion-item" id="qvDescSection">
        <button class="qv-acc-trigger" id="qvDescToggle" type="button">
          Description
          <svg viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </button>
        <div class="qv-acc-body">
          <div class="qv-acc-body-inner">
            <p id="qvDesc"></p>
          </div>
        </div>
      </div>

    </div>
  </div>
</div>
