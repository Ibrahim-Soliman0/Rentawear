/* ============================================================
   auth.js — Login & Register client-side logic
   Depends on: utils.js (CTX)
   ============================================================ */

'use strict';

/* ── Show / hide password toggle ───────────────────────────── */
document.querySelectorAll('.rw-eye-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    const input = document.getElementById(btn.dataset.target);
    if (!input) return;
    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';
    btn.querySelector('i').className = isPassword ? 'bi bi-eye-slash' : 'bi bi-eye';
  });
});

/* ── Field error helpers ────────────────────────────────────── */
function showError(fieldId, errorId, message) {
  const wrap  = document.getElementById(fieldId)?.closest('.rw-input-wrap');
  const error = document.getElementById(errorId);
  if (wrap)  { wrap.classList.add('rw-input--error'); wrap.classList.remove('rw-input--ok'); }
  if (error) { error.textContent = message; error.classList.add('visible'); }
}

function showOk(fieldId, errorId) {
  const wrap  = document.getElementById(fieldId)?.closest('.rw-input-wrap');
  const error = document.getElementById(errorId);
  if (wrap)  { wrap.classList.remove('rw-input--error'); wrap.classList.add('rw-input--ok'); }
  if (error) { error.textContent = ''; error.classList.remove('visible'); }
}

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;


/* ──────────────────────────────────────────────────────────── */
/*  LOGIN FORM                                                   */
/* ──────────────────────────────────────────────────────────── */
(function () {
  const form = document.getElementById('loginForm');
  if (!form) return;

  const emailEl    = document.getElementById('loginEmail');
  const passwordEl = document.getElementById('loginPassword');

  emailEl?.addEventListener('blur',    () => validateLoginEmail());
  passwordEl?.addEventListener('blur', () => validateLoginPassword());

  function validateLoginEmail() {
    const val = emailEl.value.trim();
    if (!val)                { showError('loginEmail', 'emailError', 'Email is required.'); return false; }
    if (!EMAIL_RE.test(val)) { showError('loginEmail', 'emailError', 'Enter a valid email address.'); return false; }
    showOk('loginEmail', 'emailError');
    return true;
  }

  function validateLoginPassword() {
    const val = passwordEl.value;
    if (!val) { showError('loginPassword', 'passwordError', 'Password is required.'); return false; }
    showOk('loginPassword', 'passwordError');
    return true;
  }

  form.addEventListener('submit', e => {
    const ok = validateLoginEmail() & validateLoginPassword();
    if (!ok) { e.preventDefault(); return; }
    const btn = document.getElementById('loginBtn');
    if (btn) {
      btn.disabled = true;
      btn.querySelector('.rw-btn-text').classList.add('d-none');
      btn.querySelector('.rw-btn-spinner').classList.remove('d-none');
    }
  });
})();


/* ──────────────────────────────────────────────────────────── */
/*  REGISTER FORM                                               */
/* ──────────────────────────────────────────────────────────── */
(function () {
  const form = document.getElementById('registerForm');
  if (!form) return;

  const fullNameEl  = document.getElementById('fullName');
  const emailEl     = document.getElementById('regEmail');
  const passwordEl  = document.getElementById('regPassword');
  const termsEl     = document.getElementById('agreeTerms');
  const dobEl = document.getElementById("dob");
  const creditEl = document.getElementById("creditLimit");

  /* ── Password strength meter ── */
  const strengthFill  = document.getElementById('pwdStrengthFill');
  const strengthLabel = document.getElementById('pwdStrengthLabel');

  function calcStrength(pwd) {
    if (!pwd) return 0;
    let score = 0;
    if (pwd.length >= 8)           score++;
    if (/[A-Z]/.test(pwd))         score++;
    if (/[0-9]/.test(pwd))         score++;
    if (/[^A-Za-z0-9]/.test(pwd))  score++;
    return score;
  }

  passwordEl?.addEventListener('input', () => {
    const score  = calcStrength(passwordEl.value);
    const levels = ['', 'weak', 'medium', 'medium', 'strong'];
    const labels = ['', 'Weak', 'Medium', 'Medium', 'Strong'];

    strengthFill?.classList.remove('weak', 'medium', 'strong');
    strengthLabel?.classList.remove('weak', 'medium', 'strong');

    if (passwordEl.value && strengthFill && strengthLabel) {
      const level = levels[score] || 'weak';
      strengthFill.classList.add(level);
      strengthLabel.classList.add(level);
      strengthLabel.textContent = labels[score] || 'Weak';
    } else if (strengthLabel) {
      strengthLabel.textContent = '';
    }
  });

  creditEl?.addEventListener('input', () => {
    if (parseInt(creditEl.value) > 999999) {
      creditEl.value = 999999;
    }
    if (parseInt(creditEl.value) < 0) {
      creditEl.value = 0;
    }
  });

  /* ── Live input validators ── */
  fullNameEl?.addEventListener('input', validateFullName);
  let emailDebounce = null;
  emailEl?.addEventListener('input', () => {
    const formatOk = validateRegEmail();
    if (!formatOk) return;

    clearTimeout(emailDebounce);
    emailDebounce = setTimeout(checkEmailAvailability, 500);
  });
  passwordEl?.addEventListener('input', validateRegPassword);
  dobEl?.addEventListener('change',validateDob);

  function validateFullName() {
    const val = fullNameEl.value.trim();
    if (!val) {
      showError('fullName', 'fullNameError', 'Full name is required.');
      return false;
    }
    if (val.length < 2) {
      showError('fullName', 'fullNameError', 'Name must be at least 2 characters.');
      return false;
    }
    if (/\d/.test(val)) {
      showError('fullName', 'fullNameError', 'Name cannot contain numbers.');
      return false;
    }
    if (/[^a-zA-Z\s\-\']/.test(val)) {
      showError('fullName', 'fullNameError', 'Name can only contain letters, spaces, hyphens, or apostrophes.');
      return false;
    }
    showOk('fullName', 'fullNameError');
    return true;
  }

  function validateDob() {
    const dobEl = document.getElementById('dob');
    if (!dobEl || !dobEl.value) return true; // optional field

    const parts    = dobEl.value.split('/');
    const selected = new Date(parts[2], parts[0] - 1, parts[1]);
    const today    = new Date();
    today.setHours(0, 0, 0, 0);

    if (selected > today) {
      showError('dob', 'dobError', 'Date of birth cannot be in the future.');
      return false;
    }
    const minAge = new Date(today);
    minAge.setFullYear(minAge.getFullYear() - 18);
    if (selected > minAge) {
      showError('dob', 'dobError', 'You must be at least 18 years old.');
      return false;
    }

    showOk('dob', 'dobError');
    return true;
  }

  function validateRegEmail() {
    const val = emailEl.value.trim();
    const errorEl = document.getElementById('regEmailError');

    // Always reset color to red before any error check
    if (errorEl) errorEl.style.color = '';

    if (!val)                { showError('regEmail', 'regEmailError', 'Email is required.'); return false; }
    if (!EMAIL_RE.test(val)) { showError('regEmail', 'regEmailError', 'Enter a valid email address.'); return false; }
    showOk('regEmail', 'regEmailError');
    return true;
  }

  function checkEmailAvailability() {
    const errorEl = document.getElementById('regEmailError');

    // Show checking state
    if (errorEl) {
      errorEl.style.color = 'var(--rw-gray-500)';
      errorEl.textContent = 'Checking availability...';
      errorEl.classList.add('visible');
    }

    let req;
    if (window.XMLHttpRequest)
      req = new XMLHttpRequest();
    else
      req = new ActiveXObject("Microsoft.XMLHTTP");

    req.onreadystatechange = function () {
      if (req.readyState === 4 && req.status === 200) {
        const data = JSON.parse(req.responseText);

        if (data.taken) {
          errorEl.style.color = '';
          showError('regEmail', 'regEmailError', 'This email is already registered.');
        } else {
          const wrap = emailEl.closest('.rw-input-wrap');
          if (wrap) { wrap.classList.remove('rw-input--error'); wrap.classList.add('rw-input--ok'); }
          if (errorEl) {
            errorEl.style.color = '#27ae60';
            errorEl.textContent = 'Email is available!';
            errorEl.classList.add('visible');
          }
        }
      }
    };

    req.open("GET", CTX + "/checkRegisteredEmail?email=" + encodeURIComponent(emailEl.value.trim()), true);
    req.send();
  }
  function validateRegPassword() {
    const val = passwordEl.value;
    if (!val)           { showError('regPassword', 'regPasswordError', 'Password is required.'); return false; }
    if (val.length < 8) { showError('regPassword', 'regPasswordError', 'Password must be at least 8 characters.'); return false; }
    showOk('regPassword', 'regPasswordError');
    return true;
  }

  function validateTerms() {
    const err = document.getElementById('termsError');
    if (!termsEl.checked) {
      if (err) { err.textContent = 'You must agree to the terms to continue.'; err.classList.add('visible'); }
      return false;
    }
    if (err) { err.textContent = ''; err.classList.remove('visible'); }
    return true;
  }

  function validateGender() {
    const selected = document.querySelector('input[name="gender"]:checked');
    const err = document.getElementById('genderError');
    if (!selected) {
      if (err) { err.textContent = 'Please select a gender.'; err.classList.add('visible'); }
      return false;
    }
    if (err) { err.textContent = ''; err.classList.remove('visible'); }
    return true;
  }
  /* ── Submit ── */
  form.addEventListener('submit', e => {
    const ok = [
      validateFullName(),
      validateGender(),
      validateRegEmail(),
      validateRegPassword(),
      validateDob(),
      validateTerms(),
    ].every(Boolean);

    if (!ok) {
      e.preventDefault();
      form.querySelector('.rw-input--error')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }

    const btn = document.getElementById('registerBtn');
    if (btn) {
      btn.disabled = true;
      btn.querySelector('.rw-btn-text').classList.add('d-none');
      btn.querySelector('.rw-btn-spinner').classList.remove('d-none');
    }
  });
})();

flatpickr('#dob', {
    maxDate: 'today',
    dateFormat: 'm/d/Y',
    disableMobile: true,
    allowInput: false,
});
// Style Interests tag selector
(function () {
  const select      = document.getElementById('styleInterestsSelect');
  const tagsWrap    = document.getElementById('styleTagsWrap');
  const hiddenWrap  = document.getElementById('styleHiddenInputs');
  if (!select) return;

  const selected = new Set(); // track selected values

  select.addEventListener('change', () => {
    const val   = select.value;
    const label = select.options[select.selectedIndex].text;

    // reset dropdown
    select.value = '';

    // skip if already selected
    if (selected.has(val)) return;
    selected.add(val);

    // create tag
    const tag = document.createElement('span');
    tag.className   = 'rw-tag';
    tag.dataset.val = val;
    tag.innerHTML   = `${label} <i class="bi bi-x"></i>`;
    tag.addEventListener('click', () => removeTag(val));
    tagsWrap.appendChild(tag);

    // create hidden input for form submission
    const input = document.createElement('input');
    input.type  = 'hidden';
    input.name  = 'styleInterests';
    input.value = val;
    input.id    = `si_${val}`;
    hiddenWrap.appendChild(input);
  });

  function removeTag(val) {
    selected.delete(val);
    tagsWrap.querySelector(`[data-val="${val}"]`)?.remove();
    document.getElementById(`si_${val}`)?.remove();
  }
})();
