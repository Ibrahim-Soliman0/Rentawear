/* profile.js — Account page tab switching and avatar handling
   Selectors match the account-* classes in styles.css additions:
     tabs    → .account-tab
     sidebar → .account-nav-link
*/

function switchTab(name) {
    document.querySelectorAll('.account-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.account-nav-link').forEach(s => s.classList.remove('active'));

    const tab = document.getElementById('tab-' + name);
    const nav = document.getElementById('nav-' + name);
    if (tab) tab.classList.add('active');
    if (nav) nav.classList.add('active');

    const labels = {
        profile:   'Edit Profile',
        history:   'Rent History',
        billing:   'Billing & Limits',
        dashboard: 'Dashboard'
    };
    const bc = document.getElementById('breadcrumb-current');
    if (bc) bc.textContent = labels[name] || '';
}

/* ─── Avatar preview ────────────────────────────────────────── */
function previewAvatar(e) {
    const file = e.target.files[0];
    if (!file) return;
    if (file.size > 800 * 1024) { alert('Image must be under 800 KB.'); return; }
    const reader = new FileReader();
    reader.onload = ev => {
        // Update both sidebar thumbnail and the main avatar card
        ['avatarPreview', 'avatarPreviewMain'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.src = ev.target.result;
        });
    };
    reader.readAsDataURL(file);
}

function removeAvatar() {
    const fallback = document.querySelector('meta[name="ctx"]')?.content + '/assets/img/default-avatar.jpg';
    ['avatarPreview', 'avatarPreviewMain'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.src = fallback || 'assets/img/default-avatar.jpg';
    });
    const input = document.getElementById('avatarInput');
    if (input) input.value = '';
}

/* ─── Save / Cancel ─────────────────────────────────────────── */
function saveProfile() {
    // TODO: POST to /account/profile servlet
    alert('Profile saved!');
}

function resetForm() {
    document.querySelectorAll('#tab-profile input, #tab-profile select')
        .forEach(el => el.value = el.defaultValue);
}