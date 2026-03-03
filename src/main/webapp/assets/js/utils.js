/* ============================================================
   utils.js — Shared constants and helper functions
   Load order: FIRST (before search.js and cart.js)
   ============================================================ */

/* Context path — reads from <meta name="ctx"> set in header.jsp.
   Falls back to empty string if the meta tag is missing.
   Usage in other files:  fetch(CTX + '/SearchServlet?q=…')       */
const CTX = document.querySelector('meta[name="ctx"]')?.content ?? '';


/* HTML-escape user-supplied strings before injecting into innerHTML.
   Prevents XSS — always use this when building HTML from data.
   Usage:  element.innerHTML = `<p>${esc(user.name)}</p>`          */
function esc(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}