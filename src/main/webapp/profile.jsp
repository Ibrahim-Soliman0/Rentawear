<%-- profile.jsp — Profile & Settings + Rent History page --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="WEB-INF/layout.jsp">
    <jsp:param name="title" value="My Account – Rentawear"/>
    <jsp:param name="description" value="Manage your profile, rental history, and billing settings."/>
    <jsp:param name="activePage" value="account"/>
</jsp:include>

<main class="rw-main">
    <div class="account-layout">

        <!-- ============================================================
             SIDEBAR
             ============================================================ -->
        <aside class="account-sidebar">

            <p class="sidebar-title">OVERVIEW</p>
            <div class="sidebar-item" onclick="switchTab('dashboard')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     style="vertical-align:middle;margin-right:8px">
                    <rect x="3" y="3" width="7" height="7"/>
                    <rect x="14" y="3" width="7" height="7"/>
                    <rect x="3" y="14" width="7" height="7"/>
                    <rect x="14" y="14" width="7" height="7"/>
                </svg>
                Dashboard
            </div>

            <p class="sidebar-title">MY ACCOUNT</p>
            <div class="sidebar-item active" id="nav-profile" onclick="switchTab('profile')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     style="vertical-align:middle;margin-right:8px">
                    <circle cx="12" cy="8" r="4"/>
                    <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
                </svg>
                Profile &amp; Settings
            </div>
            <div class="sidebar-item" id="nav-history" onclick="switchTab('history')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     style="vertical-align:middle;margin-right:8px">
                    <circle cx="12" cy="12" r="9"/>
                    <polyline points="12 7 12 12 15 15"/>
                </svg>
                Rent History
            </div>
            <div class="sidebar-item" id="nav-billing" onclick="switchTab('billing')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     style="vertical-align:middle;margin-right:8px">
                    <rect x="2" y="5" width="20" height="14" rx="2"/>
                    <line x1="2" y1="10" x2="22" y2="10"/>
                </svg>
                Billing &amp; Limits
            </div>

            <div style="position:absolute;bottom:30px;left:25px;">
                <div class="sidebar-item" style="color:#666">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                         style="vertical-align:middle;margin-right:8px">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                        <polyline points="16 17 21 12 16 7"/>
                        <line x1="21" y1="12" x2="9" y2="12"/>
                    </svg>
                    Log out
                </div>
            </div>
        </aside>

        <!-- ============================================================
             MAIN CONTENT
             ============================================================ -->
        <div class="account-content">

            <!-- ── Breadcrumb ── -->
            <div class="account-breadcrumb" id="breadcrumb">
                <a href="${pageContext.request.contextPath}/">My Account</a>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#aaa" stroke-width="2">
                    <polyline points="9 18 15 12 9 6"/>
                </svg>
                <a href="${pageContext.request.contextPath}/account/profile">Profile &amp; Settings</a>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#aaa" stroke-width="2">
                    <polyline points="9 18 15 12 9 6"/>
                </svg>
                <span id="breadcrumb-current">Edit Profile</span>
            </div>

            <!-- ══════════════════════════════════════════════════════════
                 TAB: EDIT PROFILE
                 ══════════════════════════════════════════════════════════ -->
            <div class="tab active" id="tab-profile">

                <div class="account-page-header">
                    <button class="back-btn" onclick="history.back()">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="2">
                            <polyline points="15 18 9 12 15 6"/>
                        </svg>
                        Back
                    </button>
                    <h1 class="account-title">Edit Profile</h1>
                </div>

                <!-- Avatar card -->
                <div class="card profile-card">
                    <div class="avatar-wrap">
                        <img
                                src="${not empty sessionScope.currentUser.avatarUrl ? sessionScope.currentUser.avatarUrl : pageContext.request.contextPath.concat('/assets/img/default-avatar.jpg')}"
                                alt="Profile picture"
                                class="avatar"
                                id="avatarPreview"
                        />
                        <label class="avatar-camera" for="avatarInput" title="Change photo">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                                <circle cx="12" cy="13" r="4"/>
                            </svg>
                        </label>
                        <input type="file" id="avatarInput" accept="image/*" style="display:none"
                               onchange="previewAvatar(event)"/>
                    </div>
                    <div>
                        <label class="btn-outline-sm" for="avatarInput">Upload New Picture</label>
                        <p class="avatar-hint">JPG, GIF or PNG. Max size of 800K.</p>
                    </div>
                    <button class="remove-btn" onclick="removeAvatar()">Remove</button>
                </div>

                <!-- Personal Information -->
                <div class="card">
                    <h2 class="card-section-title">Personal Information</h2>
                    <p class="card-section-sub">Update your basic profile details.</p>

                    <div class="form-grid" style="margin-top:20px">
                        <div class="form-field">
                            <label class="form-label">Full Name</label>
                            <input class="form-input" type="text"
                                   value="${not empty sessionScope.currentUser.fullName ? sessionScope.currentUser.fullName : 'Jane Doe'}"
                                   name="fullName"/>
                        </div>
                        <div class="form-field">
                            <label class="form-label">Email Address</label>
                            <input class="form-input" type="email"
                                   value="${not empty sessionScope.currentUser.email ? sessionScope.currentUser.email : 'jane@example.com'}"
                                   name="email"/>
                        </div>
                        <div class="form-field">
                            <label class="form-label">Date of Birth</label>
                            <div class="input-icon-wrap">
                                <input class="form-input" type="date"
                                       value="${not empty sessionScope.currentUser.dob ? sessionScope.currentUser.dob : '1990-04-12'}"
                                       name="dob"/>
                                <svg class="input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none"
                                     stroke="#aaa" stroke-width="2">
                                    <rect x="3" y="4" width="18" height="18" rx="2"/>
                                    <line x1="16" y1="2" x2="16" y2="6"/>
                                    <line x1="8" y1="2" x2="8" y2="6"/>
                                    <line x1="3" y1="10" x2="21" y2="10"/>
                                </svg>
                            </div>
                        </div>
                        <div class="form-field">
                            <label class="form-label">Job Title</label>
                            <input class="form-input" type="text"
                                   value="${not empty sessionScope.currentUser.jobTitle ? sessionScope.currentUser.jobTitle : 'Marketing Director'}"
                                   name="jobTitle"/>
                        </div>
                    </div>

                    <div class="form-field" style="margin-top:20px">
                        <label class="form-label">Fashion Interests &amp; Preferences</label>
                        <input class="form-input" type="text"
                               value="${not empty sessionScope.currentUser.interests ? sessionScope.currentUser.interests : 'Evening Wear, Suits, Cocktail Dresses'}"
                               name="interests"/>
                        <p class="field-hint">Separate with commas. This helps us recommend the best items for you.</p>
                    </div>
                </div>

                <!-- Shipping Address -->
                <div class="card">
                    <h2 class="card-section-title">Shipping Address</h2>
                    <p class="card-section-sub">Where should we deliver your rentals?</p>

                    <div class="form-grid" style="margin-top:20px">
                        <div class="form-field" style="grid-column:1/-1">
                            <label class="form-label">Street Address</label>
                            <input class="form-input" type="text"
                                   value="${not empty sessionScope.currentUser.address ? sessionScope.currentUser.address : ''}"
                                   name="address" placeholder="123 Fashion Ave"/>
                        </div>
                        <div class="form-field">
                            <label class="form-label">City</label>
                            <input class="form-input" type="text"
                                   value="${not empty sessionScope.currentUser.city ? sessionScope.currentUser.city : ''}"
                                   name="city" placeholder="New York"/>
                        </div>
                        <div class="form-field">
                            <label class="form-label">ZIP / Postal Code</label>
                            <input class="form-input" type="text"
                                   value="${not empty sessionScope.currentUser.zip ? sessionScope.currentUser.zip : ''}"
                                   name="zip" placeholder="10001"/>
                        </div>
                        <div class="form-field">
                            <label class="form-label">State / Province</label>
                            <input class="form-input" type="text"
                                   value="${not empty sessionScope.currentUser.state ? sessionScope.currentUser.state : ''}"
                                   name="state" placeholder="NY"/>
                        </div>
                        <div class="form-field">
                            <label class="form-label">Country</label>
                            <select class="form-input" name="country">
                                <option value="US" ${sessionScope.currentUser.country == 'US' ? 'selected' : ''}>United
                                    States
                                </option>
                                <option value="CA" ${sessionScope.currentUser.country == 'CA' ? 'selected' : ''}>
                                    Canada
                                </option>
                                <option value="GB" ${sessionScope.currentUser.country == 'GB' ? 'selected' : ''}>United
                                    Kingdom
                                </option>
                                <option value="AU" ${sessionScope.currentUser.country == 'AU' ? 'selected' : ''}>
                                    Australia
                                </option>
                                <option value="EG" ${sessionScope.currentUser.country == 'EG' ? 'selected' : ''}>Egypt
                                </option>
                            </select>
                        </div>
                    </div>
                </div>

                <!-- Change Password -->
                <div class="card">
                    <h2 class="card-section-title">Change Password</h2>
                    <p class="card-section-sub">Leave blank if you don't want to change it.</p>

                    <div class="form-grid" style="margin-top:20px">
                        <div class="form-field" style="grid-column:1/-1">
                            <label class="form-label">Current Password</label>
                            <input class="form-input" type="password" name="currentPassword" placeholder="••••••••"/>
                        </div>
                        <div class="form-field">
                            <label class="form-label">New Password</label>
                            <input class="form-input" type="password" name="newPassword" placeholder="••••••••"/>
                        </div>
                        <div class="form-field">
                            <label class="form-label">Confirm New Password</label>
                            <input class="form-input" type="password" name="confirmPassword" placeholder="••••••••"/>
                        </div>
                    </div>
                </div>

                <div class="form-actions">
                    <button class="btn-outline-sm" onclick="resetForm()">Cancel</button>
                    <button class="btn-primary-sm" onclick="saveProfile()">Save Changes</button>
                </div>

            </div><!-- /tab-profile -->


            <!-- ══════════════════════════════════════════════════════════
                 TAB: RENT HISTORY
                 ══════════════════════════════════════════════════════════ -->
            <div class="tab" id="tab-history">

                <div class="account-page-header" style="margin-bottom:4px">
                    <h1 class="account-title" style="margin:0">Rent History</h1>
                </div>
                <p style="color:#888;margin:0 0 28px">View and manage your current and past rental orders.</p>

                <!-- Current Rentals -->
                <h2 class="rentals-section-title">Current Rentals</h2>

                <div class="card rental-card">
                    <img src="${pageContext.request.contextPath}/assets/img/suit-placeholder.jpg"
                         alt="Navy Blue Peak Lapel Suit"
                         onerror="this.src='https://placehold.co/70x90/f0f0f0/999?text=Item'"/>
                    <div style="flex:1">
                        <div style="display:flex;align-items:center;gap:10px;margin-bottom:4px">
                            <strong>Navy Blue Peak Lapel Suit</strong>
                        </div>
                        <p style="color:#888;font-size:13px;margin:0 0 8px">Tom Ford • Size 40R</p>
                        <div class="rental-meta">
                            <span><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                       stroke-width="2"><polyline points="4 17 9 12 4 7"/><line x1="10" y1="12" x2="20"
                                                                                                y2="12"/></svg> ORD-98234</span>
                            <span><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                       stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="3"
                                                                                                               y1="10"
                                                                                                               x2="21"
                                                                                                               y2="10"/></svg> Oct 24 – Oct 31, 2023</span>
                            <span><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                       stroke-width="2"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2"
                                                                                                               y1="10"
                                                                                                               x2="22"
                                                                                                               y2="10"/></svg> $145.00</span>
                        </div>
                        <div style="display:flex;gap:10px;margin-top:12px">
                            <button class="btn-outline-sm">Return Item</button>
                            <button class="btn-primary-sm">Extend Rental</button>
                        </div>
                    </div>
                    <span class="status active">Active</span>
                </div>

                <div class="card rental-card">
                    <img src="${pageContext.request.contextPath}/assets/img/dress-placeholder.jpg"
                         alt="Emerald Green Silk Dress"
                         onerror="this.src='https://placehold.co/70x90/f0f0f0/999?text=Item'"/>
                    <div style="flex:1">
                        <div style="display:flex;align-items:center;gap:10px;margin-bottom:4px">
                            <strong>Emerald Green Silk Dress</strong>
                        </div>
                        <p style="color:#888;font-size:13px;margin:0 0 8px">Reformation • Size S</p>
                        <div class="rental-meta">
                            <span><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                       stroke-width="2"><polyline points="4 17 9 12 4 7"/><line x1="10" y1="12" x2="20"
                                                                                                y2="12"/></svg> ORD-98235</span>
                            <span><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                       stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="3"
                                                                                                               y1="10"
                                                                                                               x2="21"
                                                                                                               y2="10"/></svg> Oct 26 – Nov 02, 2023</span>
                            <span><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                       stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle
                                    cx="12" cy="10" r="3"/></svg> Arriving Tomorrow</span>
                        </div>
                        <div style="display:flex;gap:10px;margin-top:12px">
                            <button class="btn-outline-sm">
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                     stroke-width="2" style="margin-right:4px">
                                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                                    <circle cx="12" cy="10" r="3"/>
                                </svg>
                                Track Package
                            </button>
                        </div>
                    </div>
                    <span class="status delivery">Delivery in Progress</span>
                </div>

                <!-- Past Rentals -->
                <h2 class="rentals-section-title" style="margin-top:32px">Past Rentals</h2>

                <c:choose>
                    <c:when test="${not empty pastRentals}">
                        <c:forEach var="rental" items="${pastRentals}">
                            <div class="card rental-card">
                                <img src="${rental.imageUrl}" alt="${rental.itemName}"
                                     onerror="this.src='https://placehold.co/70x90/f0f0f0/999?text=Item'"/>
                                <div style="flex:1">
                                    <strong>${rental.itemName}</strong>
                                    <p style="color:#888;font-size:13px;margin:4px 0 8px">${rental.brand}
                                        • ${rental.size}</p>
                                    <div class="rental-meta">
                                        <span># ${rental.orderId}</span>
                                        <span>${rental.dateRange}</span>
                                        <span>$${rental.totalPrice}</span>
                                    </div>
                                    <div style="display:flex;gap:10px;margin-top:12px">
                                        <button class="btn-outline-sm">Rent Again</button>
                                        <button class="btn-outline-sm">Leave Review</button>
                                    </div>
                                </div>
                                <span class="status returned">Returned</span>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#ccc"
                                 stroke-width="1.5">
                                <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                                <line x1="3" y1="6" x2="21" y2="6"/>
                                <path d="M16 10a4 4 0 0 1-8 0"/>
                            </svg>
                            <p>No past rentals yet.</p>
                            <a href="${pageContext.request.contextPath}/products" class="btn-primary-sm">Browse
                                Collection</a>
                        </div>
                    </c:otherwise>
                </c:choose>

            </div><!-- /tab-history -->


            <!-- ══════════════════════════════════════════════════════════
                 TAB: BILLING & LIMITS (placeholder)
                 ══════════════════════════════════════════════════════════ -->
            <div class="tab" id="tab-billing">
                <div class="account-page-header">
                    <h1 class="account-title">Billing &amp; Limits</h1>
                </div>
                <div class="card">
                    <p style="color:#888">Billing information and rental limits will appear here.</p>
                </div>
            </div>

        </div><!-- /account-content -->
    </div><!-- /account-layout -->
</main>

<%-- Footer --%>
<jsp:include page="WEB-INF/components/footer.jsp"/>

<%-- Profile-specific CSS & JS --%>
<jsp:include page="WEB-INF/components/scripts.jsp">
    <jsp:param name="extraCSS" value="${pageContext.request.contextPath}/assets/css/profile.css"/>
    <jsp:param name="extraJS" value="${pageContext.request.contextPath}/assets/js/profile.js"/>
</jsp:include>

