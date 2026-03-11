<%-- quick-view.jsp - Quick view overlay for redesign cards
     Class names aligned to §20 of styles.css (qv-overlay, qv-modal …).
     ALL element IDs are unchanged — quick-view.js targets IDs only.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<div class="qv-overlay" id="qvOverlay" role="dialog" aria-modal="true" aria-label="Quick View">
  <div class="qv-backdrop" id="qvBackdrop"></div>

  <div class="qv-modal">

    <%-- Image panel (left half on desktop, top on mobile) --%>
    <div class="qv-image-panel">
      <div class="qv-main-img" id="qvMainImg" aria-hidden="true"></div>
      <button class="qv-close" id="qvClose" aria-label="Close quick view" type="button">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <line x1="18" y1="6" x2="6" y2="18"/>
          <line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>

    <%-- Details panel (right half on desktop, below image on mobile) --%>
    <div class="qv-details">
      <div class="qv-brand" id="qvBrand"></div>
      <div class="qv-name"  id="qvName"></div>

      <div class="qv-price-row">
        <span class="qv-price-main" id="qvPrice"></span>
      </div>

      <div class="qv-divider"></div>

      <p id="qvDesc" style="font-size:0.85rem;color:var(--rw-muted);margin-bottom:20px;line-height:1.7;"></p>

      <div class="qv-label">Select Size</div>
      <div class="qv-sizes" id="qvSizes"></div>

      <div style="margin-bottom:24px;"></div>

      <button class="qv-atb" id="qvAdd" type="button">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
          <line x1="3" y1="6" x2="21" y2="6"/>
          <path d="M16 10a4 4 0 0 1-8 0"/>
        </svg>
        Add to Bag
      </button>

      <button class="qv-wishlist-btn" id="qvWish" type="button">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
        </svg>
        Add to Wishlist
      </button>
    </div>

  </div>
</div>
