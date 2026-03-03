<%-- search-modal.jsp — Search modal ONLY
     No parameters needed. Opened via data-bs-target="#searchModal".
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<div class="modal fade rw-search-modal" id="searchModal"
     tabindex="-1" aria-label="Search" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content rw-search-modal-content">

      <!-- Input row -->
      <div class="rw-search-input-row">
        <div class="rw-search-group">
          <span class="rw-search-icon-left"><i class="bi bi-search"></i></span>
          <input type="text" id="searchInput" class="rw-search-input"
                 placeholder="Search dresses, suits, brands…"
                 autocomplete="off" aria-label="Search"/>
          <button class="rw-search-clear d-none" id="searchClear"
                  type="button" aria-label="Clear search">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <button type="button" class="rw-modal-close" data-bs-dismiss="modal">Cancel</button>
      </div>

      <!-- Filters -->
      <div class="rw-search-filters">

        <!-- Category pills -->
        <div class="mb-3">
          <label class="rw-filter-label">Category</label>
          <div class="rw-filter-pills">
            <span class="rw-pill active" data-filter-category="all">All</span>
            <span class="rw-pill" data-filter-category="women">Women</span>
            <span class="rw-pill" data-filter-category="men">Men</span>
            <span class="rw-pill" data-filter-category="accessories">Accessories</span>
            <span class="rw-pill" data-filter-category="shoes">Shoes</span>
          </div>
        </div>

        <!-- Dual-handle price range -->
        <div>
          <label class="rw-filter-label">
            Price / day:
            <strong class="rw-price-value">
              $<span id="priceMin">10</span> – $<span id="priceMax">500</span>
            </strong>
          </label>
          <div class="rw-range-wrap">
            <div class="rw-range-track">
              <div class="rw-range-fill" id="rangeFill"></div>
            </div>
            <input type="range" class="rw-range rw-range-min" id="priceRangeMin"
                   min="10" max="500" value="10" step="10" aria-label="Minimum price"/>
            <input type="range" class="rw-range rw-range-max" id="priceRangeMax"
                   min="10" max="500" value="500" step="10" aria-label="Maximum price"/>
          </div>
        </div>

      </div>

      <!-- Results area -->
      <div class="rw-search-results" id="searchResults">
        <p class="rw-search-placeholder">Start typing to see results…</p>
      </div>

    </div>
  </div>
</div>