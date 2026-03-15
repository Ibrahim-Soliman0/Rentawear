<%-- profile.jsp — My Account: Profile, Rent History, Billing
     CSS: styles.css (account-* additions appended — see styles-additions.css)
     JS:  assets/js/profile.js
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="WEB-INF/layout.jsp">
    <jsp:param name="title"       value="My Account – Rentawear"/>
    <jsp:param name="description" value="Manage your profile, rental history, and billing settings."/>
    <jsp:param name="activePage"  value="account"/>
</jsp:include>

<main class="rw-main">
    <div class="account-layout">

        <%-- ═══════════════════════════════════════════════════════════
             SIDEBAR
             ═══════════════════════════════════════════════════════════ --%>
        <aside class="account-sidebar">

            <div class="account-sidebar-identity">
                <div class="account-avatar-ring">
                    <img src="${not empty sessionScope.currentUser.avatarUrl
                            ? sessionScope.currentUser.avatarUrl
                            : pageContext.request.contextPath.concat('/assets/img/default-avatar.jpg')}"
                         alt="Profile picture"
                         class="account-avatar"
                         id="avatarPreview"/>
                </div>
                <div class="account-sidebar-name-block">
                    <p class="account-sidebar-name">
                        ${not empty sessionScope.currentUser.fullName
                                ? sessionScope.currentUser.fullName : 'Your Account'}
                    </p>
                    <p class="account-sidebar-email">
                        ${not empty sessionScope.currentUser.email
                                ? sessionScope.currentUser.email : ''}
                    </p>
                </div>
            </div>

            <div class="account-sidebar-divider"></div>

            <p class="account-nav-label">Overview</p>
            <a href="#" class="account-nav-link" id="nav-dashboard"
               onclick="switchTab('dashboard');return false;">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
                    <rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>
                </svg>
                Dashboard
            </a>

            <p class="account-nav-label">My Account</p>
            <a href="#" class="account-nav-link active" id="nav-profile"
               onclick="switchTab('profile');return false;">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
                </svg>
                Profile &amp; Settings
            </a>
            <a href="#" class="account-nav-link" id="nav-history"
               onclick="switchTab('history');return false;">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <circle cx="12" cy="12" r="9"/><polyline points="12 7 12 12 15 15"/>
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
                    <polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
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
                            <img src="${not empty sessionScope.currentUser.avatarUrl
                                    ? sessionScope.currentUser.avatarUrl
                                    : pageContext.request.contextPath.concat('/assets/img/default-avatar.jpg')}"
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
                            <p class="account-avatar-name">
                                ${not empty sessionScope.currentUser.fullName
                                        ? sessionScope.currentUser.fullName : 'Your Name'}
                            </p>
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
                                       value="${not empty sessionScope.currentUser.fullName
                                           ? sessionScope.currentUser.fullName : ''}"
                                       name="fullName" placeholder="Jane Doe"/>
                            </div>
                        </div>
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="email">Email Address</label>
                            <div class="rw-input-wrap">
                                <input id="email" class="rw-form-input" type="email"
                                       value="${not empty sessionScope.currentUser.email
                                           ? sessionScope.currentUser.email : ''}"
                                       name="email" placeholder="jane@example.com"/>
                            </div>
                        </div>
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="dob">Date of Birth</label>
                            <div class="rw-input-wrap">
                                <input id="dob" class="rw-form-input" type="date"
                                       value="${not empty sessionScope.currentUser.dob
                                           ? sessionScope.currentUser.dob : ''}"
                                       name="dob"/>
                            </div>
                        </div>
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="jobTitle">Job Title</label>
                            <div class="rw-input-wrap">
                                <input id="jobTitle" class="rw-form-input" type="text"
                                       value="${not empty sessionScope.currentUser.jobTitle
                                           ? sessionScope.currentUser.jobTitle : ''}"
                                       name="jobTitle" placeholder="Marketing Director"/>
                            </div>
                        </div>
                        <div class="rw-form-group account-form-full">
                            <label class="rw-form-label" for="interests">Fashion Interests &amp; Preferences</label>
                            <div class="rw-input-wrap">
                                <input id="interests" class="rw-form-input" type="text"
                                       value="${not empty sessionScope.currentUser.interests
                                           ? sessionScope.currentUser.interests : ''}"
                                       name="interests" placeholder="Evening Wear, Cocktail Dresses…"/>
                            </div>
                            <p class="account-field-hint">Separate with commas. Helps us recommend the right items.</p>
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
                                       value="${not empty sessionScope.currentUser.address
                                           ? sessionScope.currentUser.address : ''}"
                                       name="address" placeholder="123 Fashion Ave"/>
                            </div>
                        </div>
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="city">City</label>
                            <div class="rw-input-wrap">
                                <input id="city" class="rw-form-input" type="text"
                                       value="${not empty sessionScope.currentUser.city
                                           ? sessionScope.currentUser.city : ''}"
                                       name="city" placeholder="London"/>
                            </div>
                        </div>
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="zip">Postal Code</label>
                            <div class="rw-input-wrap">
                                <input id="zip" class="rw-form-input" type="text"
                                       value="${not empty sessionScope.currentUser.zip
                                           ? sessionScope.currentUser.zip : ''}"
                                       name="zip" placeholder="SW1A 1AA"/>
                            </div>
                        </div>
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="state">State / County</label>
                            <div class="rw-input-wrap">
                                <input id="state" class="rw-form-input" type="text"
                                       value="${not empty sessionScope.currentUser.state
                                           ? sessionScope.currentUser.state : ''}"
                                       name="state" placeholder="Greater London"/>
                            </div>
                        </div>
                        <div class="rw-form-group">
                            <label class="rw-form-label" for="country">Country</label>
                            <div class="rw-input-wrap rw-input-wrap--select">
                                <select id="country" class="rw-form-input rw-form-select" name="country">
                                    <option value="GB" ${sessionScope.currentUser.country == 'GB' ? 'selected' : ''}>United Kingdom</option>
                                    <option value="US" ${sessionScope.currentUser.country == 'US' ? 'selected' : ''}>United States</option>
                                    <option value="CA" ${sessionScope.currentUser.country == 'CA' ? 'selected' : ''}>Canada</option>
                                    <option value="AU" ${sessionScope.currentUser.country == 'AU' ? 'selected' : ''}>Australia</option>
                                    <option value="EG" ${sessionScope.currentUser.country == 'EG' ? 'selected' : ''}>Egypt</option>
                                </select>
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
                    <button class="btn-rw-primary btn-rw--sm" onclick="saveProfile()" type="button">Save Changes</button>
                </div>

            </div><%-- /tab-profile --%>


            <%-- ─────────────────────────────────────────────────────
                 TAB: RENT HISTORY
                 ───────────────────────────────────────────────────── --%>
            <div class="account-tab" id="tab-history">

                <div class="account-page-header">
                    <p class="section-eyebrow">My Account</p>
                    <h1 class="account-page-title">Rent History</h1>
                </div>
                <p class="account-page-sub">View and manage your current and past rental orders.</p>

                <p class="account-group-label">Current Rentals</p>

                <div class="account-section account-section--rental">
                    <div class="account-rental-card">
                        <div class="account-rental-img">
                            <img src="${pageContext.request.contextPath}/assets/img/suit-placeholder.jpg"
                                 alt="Navy Blue Peak Lapel Suit"
                                 onerror="this.src='https://placehold.co/80x110/EDE9E3/9E9189?text=Item'"/>
                        </div>
                        <div class="account-rental-body">
                            <div class="account-rental-top">
                                <div>
                                    <p class="account-rental-name">Navy Blue Peak Lapel Suit</p>
                                    <p class="account-rental-sub">Tom Ford &bull; Size 40R</p>
                                </div>
                                <span class="account-status account-status--active">Active</span>
                            </div>
                            <div class="account-rental-meta">
                                <span><svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="4 17 9 12 4 7"/><line x1="10" y1="12" x2="20" y2="12"/></svg> ORD-98234</span>
                                <span><svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="3" y1="10" x2="21" y2="10"/></svg> Oct 24 – Oct 31, 2023</span>
                                <span><svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/></svg> £145.00</span>
                            </div>
                            <div class="account-rental-actions">
                                <button class="btn-rw-outline btn-rw--sm" type="button">Return Item</button>
                                <button class="btn-rw-primary btn-rw--sm" type="button">Extend Rental</button>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="account-section account-section--rental">
                    <div class="account-rental-card">
                        <div class="account-rental-img">
                            <img src="${pageContext.request.contextPath}/assets/img/dress-placeholder.jpg"
                                 alt="Emerald Green Silk Dress"
                                 onerror="this.src='https://placehold.co/80x110/EDE9E3/9E9189?text=Item'"/>
                        </div>
                        <div class="account-rental-body">
                            <div class="account-rental-top">
                                <div>
                                    <p class="account-rental-name">Emerald Green Silk Dress</p>
                                    <p class="account-rental-sub">Reformation &bull; Size S</p>
                                </div>
                                <span class="account-status account-status--delivery">Delivery in Progress</span>
                            </div>
                            <div class="account-rental-meta">
                                <span><svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="4 17 9 12 4 7"/><line x1="10" y1="12" x2="20" y2="12"/></svg> ORD-98235</span>
                                <span><svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="3" y1="10" x2="21" y2="10"/></svg> Oct 26 – Nov 02, 2023</span>
                                <span><svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg> Arriving Tomorrow</span>
                            </div>
                            <div class="account-rental-actions">
                                <button class="btn-rw-outline btn-rw--sm" type="button">Track Package</button>
                            </div>
                        </div>
                    </div>
                </div>

                <p class="account-group-label" style="margin-top:40px">Past Rentals</p>

                <c:choose>
                    <c:when test="${not empty pastRentals}">
                        <c:forEach var="rental" items="${pastRentals}">
                            <div class="account-section account-section--rental">
                                <div class="account-rental-card">
                                    <div class="account-rental-img">
                                        <img src="${rental.imageUrl}" alt="${rental.itemName}"
                                             onerror="this.src='https://placehold.co/80x110/EDE9E3/9E9189?text=Item'"/>
                                    </div>
                                    <div class="account-rental-body">
                                        <div class="account-rental-top">
                                            <div>
                                                <p class="account-rental-name">${rental.itemName}</p>
                                                <p class="account-rental-sub">${rental.brand} &bull; ${rental.size}</p>
                                            </div>
                                            <span class="account-status account-status--returned">Returned</span>
                                        </div>
                                        <div class="account-rental-meta">
                                            <span># ${rental.orderId}</span>
                                            <span>${rental.dateRange}</span>
                                            <span>£${rental.totalPrice}</span>
                                        </div>
                                        <div class="account-rental-actions">
                                            <button class="btn-rw-outline btn-rw--sm" type="button">Rent Again</button>
                                            <button class="btn-rw-outline btn-rw--sm" type="button">Leave Review</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="account-empty-state">
                            <svg width="36" height="36" viewBox="0 0 24 24" fill="none"
                                 stroke="var(--rw-stone)" stroke-width="1.5" aria-hidden="true">
                                <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                                <line x1="3" y1="6" x2="21" y2="6"/>
                                <path d="M16 10a4 4 0 0 1-8 0"/>
                            </svg>
                            <p class="account-empty-label">No past rentals yet</p>
                            <a href="${pageContext.request.contextPath}/products"
                               class="btn-rw-primary btn-rw--sm">Browse Collection</a>
                        </div>
                    </c:otherwise>
                </c:choose>

            </div><%-- /tab-history --%>


            <%-- ─────────────────────────────────────────────────────
                 TAB: BILLING
                 ───────────────────────────────────────────────────── --%>
            <div class="account-tab" id="tab-billing">
                <div class="account-page-header">
                    <p class="section-eyebrow">My Account</p>
                    <h1 class="account-page-title">Billing &amp; Limits</h1>
                </div>
                <div class="account-section">
                    <p class="account-section-sub" style="margin:0">
                        Billing information and rental limits will appear here.
                    </p>
                </div>
            </div>

            <%-- ─────────────────────────────────────────────────────
                 TAB: DASHBOARD
                 ───────────────────────────────────────────────────── --%>
            <div class="account-tab" id="tab-dashboard">
                <div class="account-page-header">
                    <p class="section-eyebrow">My Account</p>
                    <h1 class="account-page-title">Dashboard</h1>
                </div>
                <div class="account-section">
                    <p class="account-section-sub" style="margin:0">
                        Your dashboard overview will appear here.
                    </p>
                </div>
            </div>

        </div><%-- /account-content --%>
    </div><%-- /account-layout --%>
</main>

<jsp:include page="WEB-INF/components/footer.jsp"/>

<jsp:include page="WEB-INF/components/scripts.jsp">
    <jsp:param name="extraJS" value="${pageContext.request.contextPath}/assets/js/profile.js"/>
</jsp:include>
