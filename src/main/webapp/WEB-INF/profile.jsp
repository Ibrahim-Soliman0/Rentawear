<%@ page import="dto.UserSessionDTO" %><%-- profile.jsp — My Account: Profile, Rent History, Billing
     CSS: styles.css (account-* additions appended — see styles-additions.css)
     JS:  assets/js/profile.js
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="layout.jsp">
    <jsp:param name="title" value="My Account – Rentawear"/>
    <jsp:param name="description" value="Manage your profile, rental history, and billing settings."/>
    <jsp:param name="activePage" value="account"/>
</jsp:include>

<main class="rw-main">
    <div class="account-layout">

        <%-- ═══════════════════════════════════════════════════════════
             SIDEBAR
             ═══════════════════════════════════════════════════════════ --%>
        <aside class="account-sidebar">

            <div class="account-sidebar-identity">
                <div class="account-avatar-ring">
                    <img src="${requestScope.avatarDataUri}"
                         alt="Profile picture"
                         class="account-avatar"
                         id="avatarPreview"/>
                </div>
                <div class="account-sidebar-name-block">
                    <p class="account-sidebar-name" id="sidebarName">—</p>
                    <p class="account-sidebar-email" id="sidebarEmail">—</p>
                </div>
            </div>

            <div class="account-sidebar-divider"></div>

            <p class="account-nav-label">My Account</p>
            <a href="#" class="account-nav-link active" id="nav-profile"
               onclick="switchTab('profile');return false;">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <circle cx="12" cy="8" r="4"/>
                    <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
                </svg>
                Profile &amp; Settings
            </a>
            <a href="#" class="account-nav-link" id="nav-history"
               onclick="switchTab('history');return false;">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <circle cx="12" cy="12" r="9"/>
                    <polyline points="12 7 12 12 15 15"/>
                </svg>
                Rent History
            </a>
            <a href="#" class="account-nav-link" id="nav-billing"
               onclick="switchTab('billing');return false;">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <rect x="2" y="5" width="20" height="14" rx="2"/>
                    <line x1="2" y1="10" x2="22" y2="10"/>
                </svg>
                Billing &amp; Limits
            </a>

            <div class="account-sidebar-divider account-sidebar-divider--push"></div>

            <a href="${pageContext.request.contextPath}/logout" class="account-nav-link account-nav-logout">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
                Log out
            </a>

        </aside>

        <%-- ═══════════════════════════════════════════════════════════
             MAIN CONTENT
             ═══════════════════════════════════════════════════════════ --%>
        <div class="account-content">

            <nav class="account-breadcrumb" aria-label="Breadcrumb">
                <a href="${pageContext.request.contextPath}/">Home</a>
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <polyline points="9 18 15 12 9 6"/>
                </svg>
                <span id="breadcrumb-current">Edit Profile</span>
            </nav>


            <%-- ─────────────────────────────────────────────────────
                 TAB: EDIT PROFILE
                 ───────────────────────────────────────────────────── --%>
            <div class="account-tab active" id="tab-profile">

                <div class="account-page-header">
                    <p class="section-eyebrow">My Account</p>
                    <h1 class="account-page-title">Edit Profile</h1>
                </div>

                <%-- Avatar --%>
                <div class="account-section">
                    <div class="account-avatar-card">
                        <div class="account-avatar-ring account-avatar-ring--lg">
                            <img src="${requestScope.avatarDataUri}"
                                 alt="Profile picture"
                                 class="account-avatar"
                                 id="avatarPreviewMain"/>
                            <label class="account-avatar-cam" for="avatarInput" title="Change photo">
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none"
                                     stroke="white" stroke-width="2" aria-hidden="true">
                                    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                                    <circle cx="12" cy="13" r="4"/>
                                </svg>
                            </label>
                            <input type="file" id="avatarInput" accept="image/*"
                                   style="display:none" onchange="previewAvatar(event)"/>
                        </div>
                        <div class="account-avatar-meta">
                            <p class="account-avatar-name" id="avatarName">—</p>
                            <p class="account-avatar-hint">JPG, GIF or PNG &middot; Max 800 KB</p>
                            <div class="account-avatar-actions">
                                <label for="avatarInput" class="btn-rw-outline btn-rw--sm" style="cursor:pointer">
                                    Upload Photo
                                </label>
                                <button class="btn-rw-outline btn-rw--sm" onclick="removeAvatar()" type="button">
                                    Remove
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <%-- Personal Information --%>
                <div class="account-section">
                    <div class="account-section-header">
                        <h2 class="account-section-title">Personal Information</h2>
                        <p class="account-section-sub">Update your basic profile details.</p>
                    </div>
                    <div class="account-form-grid">
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="fullName">Full Name</label>
                            <div class="rw-input-wrap">
                                <input id="fullName" class="rw-form-input" type="text"
                                       name="fullName" placeholder="Jane Doe"/>
                            </div>
                        </div>
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="email">Email Address</label>
                            <div class="rw-input-wrap">
                                <input id="email" class="rw-form-input" type="email"
                                       name="email" placeholder="jane@example.com"/>
                            </div>
                        </div>
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="dob">Date of Birth</label>
                            <div class="rw-input-wrap">
                                <input id="dob" class="rw-form-input" type="date"
                                       name="dob"/>
                            </div>
                        </div>
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="jobTitle">Job Title</label>
                            <div class="rw-input-wrap">
                                <input id="jobTitle" class="rw-form-input" type="text"
                                       name="jobTitle" placeholder="Marketing Director"/>
                            </div>
                        </div>
                        <div class="rw-form-group account-form-full">
                            <div class="rw-form-group">
                                <label class="rw-form-label">Style Interests</label>

                                <!-- Dropdown to pick from -->
                                <div class="rw-input-wrap rw-input-wrap--select">
                                    <select id="styleInterestsSelect" class="rw-form-input rw-form-select">
                                        <option value="" disabled selected>Select categories</option>
                                        <c:forEach var="category" items="${requestScope.categories}">
                                            <option value="${category.id}">${category.name}</option>
                                        </c:forEach>
                                    </select>
                                    <i class="bi bi-chevron-down rw-select-chevron"></i>
                                </div>

                                <!-- Selected tags appear here -->
                                <div class="rw-tags-wrap" id="styleTagsWrap"></div>

                                <!-- Hidden inputs submitted to backend -->
                                <div id="styleHiddenInputs"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <%-- Shipping Address --%>
                <div class="account-section">
                    <div class="account-section-header">
                        <h2 class="account-section-title">Shipping Address</h2>
                        <p class="account-section-sub">Where should we deliver your rentals?</p>
                    </div>
                    <div class="account-form-grid">
                        <div class="rw-form-group account-form-full">
                            <label class="rw-form-label" for="address">Street Address</label>
                            <div class="rw-input-wrap">
                                <input id="address" class="rw-form-input" type="text"
                                       name="address" placeholder="123 Fashion Ave"/>
                            </div>
                        </div>
                    </div>
                </div>

                <%-- Change Password --%>
                <div class="account-section">
                    <div class="account-section-header">
                        <h2 class="account-section-title">Change Password</h2>
                        <p class="account-section-sub">Leave blank to keep your current password.</p>
                    </div>
                    <div class="account-form-grid">
                        <div class="rw-form-group account-form-full">
                            <label class="rw-form-label" for="currentPassword">Current Password</label>
                            <div class="rw-input-wrap">
                                <input id="currentPassword" class="rw-form-input" type="password"
                                       name="currentPassword" placeholder="••••••••"/>
                            </div>
                        </div>
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="newPassword">New Password</label>
                            <div class="rw-input-wrap">
                                <input id="newPassword" class="rw-form-input" type="password"
                                       name="newPassword" placeholder="••••••••"/>
                            </div>
                        </div>
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="confirmPassword">Confirm New Password</label>
                            <div class="rw-input-wrap">
                                <input id="confirmPassword" class="rw-form-input" type="password"
                                       name="confirmPassword" placeholder="••••••••"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="account-form-actions">
                    <button class="btn-rw-outline btn-rw--sm" onclick="resetForm()" type="button">Cancel</button>
                    <button class="btn-rw-primary btn-rw--sm" onclick="saveProfile()" type="button">Save Changes
                    </button>
                </div>

            </div>
            <%-- /tab-profile --%>


            <%-- ─────────────────────────────────────────────────────
                 TAB: RENT HISTORY
                 ───────────────────────────────────────────────────── --%>
            <div class="account-tab" id="tab-history">

                <div class="account-page-header">
                    <p class="section-eyebrow">My Account</p>
                    <h1 class="account-page-title">Rent History</h1>
                </div>
                <p class="account-page-sub">View and manage your current and past rental orders.</p>

                <%-- ── Active orders ── --%>
                <p class="account-group-label">Current Rentals</p>
                <div id="activeOrdersList">
                    <%-- Populated by profile.js via GET /profile/orders --%>
                    <div class="account-orders-loading">
                        <span class="account-orders-spinner"></span>
                        Loading your rentals…
                    </div>
                </div>

                <%-- ── Past orders ── --%>
                <p class="account-group-label" style="margin-top:40px">Past Rentals</p>
                <div id="pastOrdersList">
                    <%-- Populated by profile.js --%>
                </div>

            </div>
            <%-- /tab-history --%>


            <%-- ─────────────────────────────────────────────────────
                 TAB: BILLING & LIMITS
                 ───────────────────────────────────────────────────── --%>
            <div class="account-tab" id="tab-billing">

                <div class="account-page-header">
                    <p class="section-eyebrow">My Account</p>
                    <h1 class="account-page-title">Billing &amp; Limits</h1>
                </div>

                <%-- Credit Limit Banner — populated by JS from RW_USER --%>
                <div class="account-section account-credit-banner">
                    <div class="account-credit-top">
                        <div class="account-credit-label-block">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                 stroke="currentColor" stroke-width="2" aria-hidden="true">
                                <rect x="2" y="5" width="20" height="14" rx="2"/>
                                <line x1="2" y1="10" x2="22" y2="10"/>
                            </svg>
                            <span class="account-credit-label">Rental Credit Limit</span>
                        </div>
                        <span class="account-credit-amount">
            £<span id="creditLimitDisplay">—</span>
            <span class="account-credit-total"> remaining</span>
        </span>
                    </div>
                    <div class="account-credit-bar-track">
                        <div class="account-credit-bar-fill" id="creditBar" style="width:0%"></div>
                    </div>
                    <p class="account-credit-hint">
                        This is the maximum outstanding rental value allowed on your account at one time.
                    </p>
                    <%-- ── Increase button ── --%>
                    <div class="account-credit-action">
                        <button class="btn-rw-outline btn-rw--sm" type="button"
                                id="increaseCreditBtn" onclick="increaseCredit()">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none"
                                 stroke="currentColor" stroke-width="2.5" aria-hidden="true"
                                 style="margin-right:5px; vertical-align:middle;">
                                <line x1="12" y1="5" x2="12" y2="19"/>
                                <line x1="5" y1="12" x2="19" y2="12"/>
                            </svg>
                            Increase by £500
                        </button>
                        <p class="account-credit-feedback" id="creditFeedback" style="display:none"></p>
                    </div>
                </div>

                <%-- Payment Cards header --%>
                <div class="account-section-header" style="margin-top:32px; margin-bottom:16px;">
                    <div style="display:flex; align-items:center; justify-content:space-between;">
                        <div>
                            <h2 class="account-section-title">Payment Cards</h2>
                            <p class="account-section-sub">Cards saved to your account.</p>
                        </div>
                        <button class="btn-rw-primary btn-rw--sm" type="button"
                                onclick="openAddCardModal()">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none"
                                 stroke="currentColor" stroke-width="2.5" aria-hidden="true"
                                 style="margin-right:5px; vertical-align:middle;">
                                <line x1="12" y1="5" x2="12" y2="19"/>
                                <line x1="5" y1="12" x2="19" y2="12"/>
                            </svg>
                            Add Card
                        </button>
                    </div>
                </div>

                <%-- Card rows — rendered by JS from RW_USER.paymentCards --%>
                <div id="cardList"></div>

                <%-- Empty state — shown by JS if no cards --%>
                <div class="account-empty-state" id="noCardsState" style="display:none">
                    <svg width="36" height="36" viewBox="0 0 24 24" fill="none"
                         stroke="var(--rw-stone)" stroke-width="1.5" aria-hidden="true">
                        <rect x="2" y="5" width="20" height="14" rx="2"/>
                        <line x1="2" y1="10" x2="22" y2="10"/>
                    </svg>
                    <p class="account-empty-label">No payment cards saved yet</p>
                    <button class="btn-rw-primary btn-rw--sm" type="button"
                            onclick="openAddCardModal()">Add Your First Card
                    </button>
                </div>

            </div>
            <%-- /tab-billing --%>

        </div>
        <%-- /account-content --%>
    </div>
    <%-- /account-layout --%>
</main>


<%-- ═══════════════════════════════════════════════════════════
     ADD CARD MODAL
     ═══════════════════════════════════════════════════════════ --%>
<div class="account-modal-backdrop" id="addCardBackdrop" aria-hidden="true"
     onclick="closeAddCardModal()"></div>

<div class="account-modal" id="addCardModal" role="dialog"
     aria-modal="true" aria-labelledby="addCardTitle">

    <div class="account-modal-header">
        <h3 class="account-modal-title" id="addCardTitle">Add Payment Card</h3>
        <button class="account-modal-close" type="button"
                onclick="closeAddCardModal()" aria-label="Close">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" aria-hidden="true">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
        </button>
    </div>

    <div class="account-modal-body">

        <%-- Live card preview --%>
        <div class="account-card-preview" id="cardPreview">
            <div class="account-card-preview-chip">
                <svg width="28" height="22" viewBox="0 0 28 22" fill="none" aria-hidden="true">
                    <rect width="28" height="22" rx="4" fill="rgba(255,255,255,0.25)"/>
                    <line x1="0" y1="8" x2="28" y2="8" stroke="rgba(255,255,255,0.3)" stroke-width="1"/>
                    <line x1="0" y1="14" x2="28" y2="14" stroke="rgba(255,255,255,0.3)" stroke-width="1"/>
                    <line x1="9" y1="0" x2="9" y2="22" stroke="rgba(255,255,255,0.3)" stroke-width="1"/>
                    <line x1="19" y1="0" x2="19" y2="22" stroke="rgba(255,255,255,0.3)" stroke-width="1"/>
                </svg>
            </div>
            <p class="account-card-preview-number" id="previewNumber">•••• •••• •••• ••••</p>
            <div class="account-card-preview-bottom">
                <div>
                    <p class="account-card-preview-label">CARD HOLDER</p>
                    <p class="account-card-preview-value" id="previewName">FULL NAME</p>
                </div>
                <div>
                    <p class="account-card-preview-label">VALID THRU</p>
                    <p class="account-card-preview-value" id="previewExpiry">MM / YY</p>
                </div>
                <div class="account-card-preview-type" id="previewType"></div>
            </div>
        </div>

        <%-- Card Number --%>
        <div class="rw-form-group" style="margin-top:20px">
            <label class="rw-form-label" for="newCardNumber">Card Number</label>
            <div class="rw-input-wrap">
                <input id="newCardNumber" class="rw-form-input" type="text"
                       maxlength="19" placeholder="0000 0000 0000 0000"
                       oninput="formatCardNumber(this); updatePreview()"/>
            </div>
        </div>

        <%-- Cardholder Name --%>
        <div class="rw-form-group" style="margin-top:16px">
            <label class="rw-form-label" for="newCardholderName">Cardholder Name</label>
            <div class="rw-input-wrap">
                <input id="newCardholderName" class="rw-form-input" type="text"
                       placeholder="As it appears on the card"
                       oninput="updatePreview()"/>
            </div>
        </div>

        <%-- Expiry Month + Year --%>
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-top:16px;">
            <div class="rw-form-group">
                <label class="rw-form-label" for="newCardMonth">Expiry Month</label>
                <div class="rw-input-wrap rw-input-wrap--select">
                    <select id="newCardMonth" class="rw-form-input rw-form-select"
                            onchange="updatePreview()">
                        <option value="">MM</option>
                        <option value="01">01 – Jan</option>
                        <option value="02">02 – Feb</option>
                        <option value="03">03 – Mar</option>
                        <option value="04">04 – Apr</option>
                        <option value="05">05 – May</option>
                        <option value="06">06 – Jun</option>
                        <option value="07">07 – Jul</option>
                        <option value="08">08 – Aug</option>
                        <option value="09">09 – Sep</option>
                        <option value="10">10 – Oct</option>
                        <option value="11">11 – Nov</option>
                        <option value="12">12 – Dec</option>
                    </select>
                </div>
            </div>
            <div class="rw-form-group">
                <label class="rw-form-label" for="newCardYear">Expiry Year</label>
                <div class="rw-input-wrap rw-input-wrap--select">
                    <select id="newCardYear" class="rw-form-input rw-form-select"
                            onchange="updatePreview()">
                        <option value="">YY</option>
                        <%-- Populated by profile.js on page load --%>
                    </select>
                </div>
            </div>
        </div>

        <%-- CVV --%>
        <div class="rw-form-group" style="margin-top:16px">
            <label class="rw-form-label" for="newCardCvv">CVV</label>
            <div class="rw-input-wrap">
                <input id="newCardCvv" class="rw-form-input" type="password"
                       maxlength="4" placeholder="•••"
                       style="max-width:120px"/>
            </div>
        </div>

        <%-- Error message — always last --%>
        <p class="account-modal-error" id="addCardError" style="display:none"></p>

    </div>

    <div class="account-modal-footer">
        <button class="btn-rw-outline btn-rw--sm" type="button"
                onclick="closeAddCardModal()">Cancel
        </button>
        <button class="btn-rw-primary btn-rw--sm" type="button"
                id="saveCardBtn" onclick="saveCard()">Save Card
        </button>
    </div>
</div>


<jsp:include page="components/footer.jsp"/>

<%-- Inject user JSON before profile.js loads --%>
<script>
    window.RW_USER = <%= request.getAttribute("userJson") %>;
    window.RW_AVATAR = '<%= request.getAttribute("avatarDataUri") %>';
</script>

<jsp:include page="components/scripts.jsp">
    <jsp:param name="extraJS" value="${pageContext.request.contextPath}/assets/js/profile.js"/>
</jsp:include>
