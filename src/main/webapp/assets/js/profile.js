function switchTab(name) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.sidebar-item').forEach(s => s.classList.remove('active'));

    const tab = document.getElementById('tab-' + name);
    const nav = document.getElementById('nav-' + name);

    if (tab) tab.classList.add('active');
    if (nav) nav.classList.add('active');

    const labels = {
        profile: 'Edit Profile',
        history: 'Rent History',
        billing: 'Billing & Limits',
        dashboard: 'Dashboard'
    };
    const bc = document.getElementById('breadcrumb-current');
    if (bc) bc.textContent = labels[name] || '';
}

/* ─── Avatar preview ────────────────────────────────────────────── */
function previewAvatar(e) {
    const file = e.target.files[0];
    if (!file) return;
    if (file.size > 800 * 1024) {
        alert('Image must be under 800KB.');
        return;
    }
    const reader = new FileReader();
    reader.onload = ev => document.getElementById('avatarPreview').src = ev.target.result;
    reader.readAsDataURL(file);
}

function removeAvatar() {
    document.getElementById('avatarPreview').src = 'assets/img/default-avatar.jpg';
    document.getElementById('avatarInput').value = '';
}

/* ─── Save / Cancel stubs (wire to your servlet) ───────────────── */
function saveProfile() {
    // TODO: POST to /account/profile servlet
    alert('Profile saved!');
}

function resetForm() {
    document.querySelectorAll('#tab-profile input, #tab-profile select').forEach(el => el.value = el.defaultValue);
}