<%-- search-modal.jsp - Redesign search modal (matches attached HTML)
     Search results injected by assets/js/search.js using CardFactory
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@ page isELIgnored="false" %>

<div class="search-modal" id="searchModal" role="dialog" aria-label="Search" aria-hidden="true">
  <div class="search-backdrop" id="searchBackdrop"></div>

  <div class="search-panel">
    <div class="search-input-row">
      <div class="search-field-wrap">
        <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input type="text" class="search-input" id="searchInput" placeholder="Search dresses, suits, brands..." autocomplete="off" spellcheck="false" aria-label="Search input">
        <button class="search-clear" id="searchClear" aria-label="Clear search" type="button">
          <svg viewBox="0 0 24 24" aria-hidden="true"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
      <button class="search-cancel" id="searchCancel" type="button">Cancel</button>
    </div>

    <div class="search-filters">
      <span class="filter-label">Filter:</span>
      <button class="filter-pill active" data-cat="all" type="button">All</button>
      <button class="filter-pill" data-cat="women" type="button">Women</button>
      <button class="filter-pill" data-cat="men" type="button">Men</button>
      <button class="filter-pill" data-cat="accessories" type="button">Accessories</button>
      <button class="filter-pill" data-cat="shoes" type="button">Shoes</button>
    </div>

    <div class="search-results-wrap" id="searchResultsWrap">
      <div class="search-results-inner">
        <div class="search-trending" id="searchTrending">
          <div class="search-trending-label">Trending searches</div>
          <div class="search-trending-pills">
            <button class="search-trending-pill" data-query="Evening dress" type="button">
              <svg viewBox="0 0 24 24" aria-hidden="true"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
              Evening dress
            </button>
            <button class="search-trending-pill" data-query="Zimmermann" type="button">
              <svg viewBox="0 0 24 24" aria-hidden="true"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
              Zimmermann
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

        <%-- Search results container - CardFactory.searchResult nodes will be appended here --%>
        <div id="searchResults" class="search-results-container" style="display:none;"></div>
      </div>
    </div>
  </div>

</div>