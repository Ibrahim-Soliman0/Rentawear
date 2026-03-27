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

// Shared fetch helper with timeout and optional outer AbortSignal.
// Uses native fetch when available; otherwise falls back to XMLHttpRequest.
// Returns a Promise that resolves to parsed JSON or rejects with an Error.
function fetchJson(url, { outerSignal = null, timeout = 8000 } = {}) {
  // If fetch exists in the environment, use it (wired with AbortController)
  if (typeof fetch === 'function') {
    const local = new AbortController();
    if (outerSignal) outerSignal.addEventListener('abort', () => local.abort(), { once: true });
    const timer = setTimeout(() => local.abort(), timeout);
    return fetch(url, { signal: local.signal })
      .then(res => {
        clearTimeout(timer);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .finally(() => clearTimeout(timer));
  }

  // Fallback: XMLHttpRequest implementation
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    let timedOut = false;
    const timer = setTimeout(() => {
      timedOut = true;
      try { xhr.abort(); } catch (e) { /* ignore */ }
      const err = new Error('Timeout');
      err.name = 'AbortError';
      reject(err);
    }, timeout);

    if (outerSignal) {
      outerSignal.addEventListener('abort', () => {
        try { xhr.abort(); } catch (e) { /* ignore */ }
        const err = new Error('Aborted');
        err.name = 'AbortError';
        clearTimeout(timer);
        reject(err);
      }, { once: true });
    }

    xhr.open('GET', url, true);
    xhr.onreadystatechange = function () {
      if (xhr.readyState !== 4) return;
      clearTimeout(timer);
      if (timedOut) return; // already rejected
      if (xhr.status >= 200 && xhr.status < 300) {
        try {
          const json = JSON.parse(xhr.responseText);
          resolve(json);
        } catch (e) {
          reject(e);
        }
      } else {
        const err = new Error(`HTTP ${xhr.status}`);
        reject(err);
      }
    };
    xhr.onerror = function () {
      clearTimeout(timer);
      reject(new Error('Network error'));
    };
    try { xhr.send(); } catch (e) { clearTimeout(timer); reject(e); }
  });
}

// Expose helpers globally
window.CTX = CTX;
window.esc = esc;
window.fetchJson = fetchJson;
