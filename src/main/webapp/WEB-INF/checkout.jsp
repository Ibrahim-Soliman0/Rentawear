<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="layout.jsp">
    <jsp:param name="title" value="My Account – Rentawear"/>
    <jsp:param name="description" value="Manage your profile, rental history, and billing settings."/>
    <jsp:param name="activePage" value="account"/>
</jsp:include>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Checkout</title>
</head>
<body>

<%-- ── Main Content ─────────────────────────────────────────── --%>
<main class="rw-main checkout-page">
    <div class="checkout-container">

        <%-- Page Title --%>
        <h1 class="checkout-title">Checkout</h1>

        <%-- Flash messages (errors / success from servlet) --%>
        <c:if test="${not empty errorMessage}">
            <div class="rw-auth-alert rw-auth-alert--error" style="margin-bottom: 20px;">
                <svg width="16" height="16" viewBox="0 0 24 24" stroke="currentColor" fill="none" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                    ${errorMessage}
            </div>
        </c:if>
        <c:if test="${not empty successMessage}">
            <div class="rw-auth-alert rw-auth-alert--success" style="margin-bottom: 20px;">
                <svg width="16" height="16" viewBox="0 0 24 24" stroke="currentColor" fill="none" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                    <polyline points="22 4 12 14.01 9 11.01"/>
                </svg>
                    ${successMessage}
            </div>
        </c:if>

        <%-- Two-column layout --%>
        <form action="${pageContext.request.contextPath}/checkout" method="post" id="checkoutForm">
            <div class="checkout-layout">

                <%-- ── LEFT COLUMN ───────────────────────────────── --%>
                <div class="checkout-steps">

                    <%-- Step 1: Rental Period --%>
                    <section class="checkout-section">
                        <div class="checkout-section-header">
                            <span class="checkout-section-title">1. Rental Period</span>
                            <button type="button" class="checkout-edit-link" onclick="window.history.back()">Edit
                            </button>
                        </div>

                        <div class="rental-period-row">
                            <div class="rental-date-box">
                                <div class="rental-date-label">Delivery Date</div>
                                <div class="rental-date-value">
                                    <c:choose>
                                        <c:when test="${not empty deliveryDate}">${deliveryDate}</c:when>
                                        <c:otherwise>Tue, Nov 12</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="rental-period-arrow">
                                <svg viewBox="0 0 24 24">
                                    <line x1="5" y1="12" x2="19" y2="12"/>
                                    <polyline points="12 5 19 12 12 19"/>
                                </svg>
                            </div>
                            <div class="rental-date-box">
                                <div class="rental-date-label">Return Date</div>
                                <div class="rental-date-value">
                                    <c:choose>
                                        <c:when test="${not empty returnDate}">${returnDate}</c:when>
                                        <c:otherwise>Sat, Nov 16</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                        <input type="hidden" name="deliveryDate"
                               value="${not empty deliveryDate ? deliveryDate : 'Tue, Nov 12'}"/>
                        <input type="hidden" name="returnDate"
                               value="${not empty returnDate   ? returnDate   : 'Sat, Nov 16'}"/>
                    </section>

                    <%-- Step 2: Shipping Address --%>
                    <section class="checkout-section">
                        <div class="checkout-section-header">
                            <span class="checkout-section-title">2. Shipping Address</span>
                        </div>

                        <%-- Saved addresses (dynamic via JSTL) --%>
                        <c:choose>
                            <c:when test="${not empty addresses}">
                                <c:forEach var="addr" items="${addresses}">
                                    <label class="address-option ${addr.id == selectedAddressId ? 'selected' : ''}"
                                           onclick="selectAddress(this, '${addr.id}')">
                                        <div class="address-radio"></div>
                                        <div class="address-body">
                                            <div class="address-name">
                                                    ${addr.label}
                                                <button type="button" class="checkout-edit-link"
                                                        style="font-size:0.72rem">Edit
                                                </button>
                                            </div>
                                            <div class="address-lines">
                                                    ${addr.fullName}<br/>
                                                    ${addr.line1}<br/>
                                                    ${addr.city}, ${addr.state} ${addr.zip}<br/>
                                                    ${addr.country}
                                            </div>
                                        </div>
                                    </label>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <%-- Fallback static address for demo --%>
                                <div class="address-option selected" id="addr-home"
                                     onclick="selectAddress(this, 'home')">
                                    <div class="address-radio"></div>
                                    <div class="address-body">
                                        <div class="address-name">
                                            Home
                                            <button type="button" class="checkout-edit-link" style="font-size:0.72rem">
                                                Edit
                                            </button>
                                        </div>
                                        <div class="address-lines">
                                            Jane Doe<br/>
                                            123 Fashion Avenue, Apt 4B<br/>
                                            New York, NY 10001<br/>
                                            United States
                                        </div>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <input type="hidden" name="selectedAddressId" id="selectedAddressId"
                               value="${not empty selectedAddressId ? selectedAddressId : 'home'}"/>
                    </section>

                    <%-- Step 3: Payment Method --%>
                    <section class="checkout-section">
                        <div class="checkout-section-header">
                            <span class="checkout-section-title">3. Payment Method</span>
                        </div>

                        <c:choose>
                            <c:when test="${not empty paymentMethods}">
                                <c:forEach var="pm" items="${paymentMethods}">
                                    <div class="payment-option ${pm.id == selectedPaymentId ? 'selected' : ''}"
                                         onclick="selectPayment(this, '${pm.id}')">
                                        <div class="payment-radio"></div>
                                        <div class="payment-icon">
                                            <svg viewBox="0 0 24 24">
                                                <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
                                                <line x1="1" y1="10" x2="23" y2="10"/>
                                            </svg>
                                        </div>
                                        <div class="payment-body">
                                            <div class="payment-label">${pm.label}</div>
                                            <div class="payment-sub">${pm.sub}</div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <%-- Static demo payment methods --%>
                                <div class="payment-option selected" id="pay-visa"
                                     onclick="selectPayment(this, 'visa')">
                                    <div class="payment-radio"></div>
                                    <div class="payment-icon">
                                        <svg viewBox="0 0 24 24">
                                            <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
                                            <line x1="1" y1="10" x2="23" y2="10"/>
                                        </svg>
                                    </div>
                                    <div class="payment-body">
                                        <div class="payment-label">Visa ending in 4242</div>
                                        <div class="payment-sub">Expires 12/24</div>
                                    </div>
                                </div>

                                <div class="payment-option" id="pay-paypal" onclick="selectPayment(this, 'paypal')">
                                    <div class="payment-radio"></div>
                                    <div class="payment-icon">
                                            <%-- PayPal icon --%>
                                        <svg viewBox="0 0 24 24" fill="none" stroke="var(--rw-muted)" stroke-width="1.6"
                                             stroke-linecap="round" stroke-linejoin="round">
                                            <path d="M6.5 3h8c2.5 0 4 1.5 3.5 4-0.6 3-3 4-5.5 4H10l-1 6H5l2.5-14z"/>
                                            <path d="M10 11h2c2.5 0 4.5-1 5-3.5"/>
                                        </svg>
                                    </div>
                                    <div class="payment-body">
                                        <div class="payment-label">PayPal</div>
                                        <div class="payment-sub">jane.doe@example.com</div>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <input type="hidden" name="selectedPaymentId" id="selectedPaymentId"
                               value="${not empty selectedPaymentId ? selectedPaymentId : 'visa'}"/>
                    </section>

                </div>
                <%-- end .checkout-steps --%>

                <%-- ── RIGHT COLUMN: Order Summary ────────────────── --%>
                <aside class="order-summary">
                    <div class="order-summary-title">Order Summary</div>

                    <%-- Product line --%>
                    <c:choose>
                        <c:when test="${not empty cartItem}">
                            <div class="order-item">
                                <div class="order-item-img">
                                    <img src="${cartItem.imageUrl}" alt="${cartItem.name}"/>
                                </div>
                                <div class="order-item-info">
                                    <div class="order-item-brand">${cartItem.brand}</div>
                                    <div class="order-item-name">${cartItem.name}</div>
                                    <div class="order-item-meta">Size: ${cartItem.size} • ${cartItem.days} Days</div>
                                </div>
                                <div class="order-item-price">$${cartItem.price}</div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <%-- Static demo product --%>
                            <div class="order-item">
                                <div class="order-item-img">
                                    <img src="https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=200&q=80&fit=crop"
                                         alt="Silk Pleated Midi Dress"/>
                                </div>
                                <div class="order-item-info">
                                    <div class="order-item-brand">GUCCI</div>
                                    <div class="order-item-name">Silk Pleated Midi Dress</div>
                                    <div class="order-item-meta">Size: S &bull; 4 Days</div>
                                </div>
                                <div class="order-item-price">$180</div>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <%-- Discount code --%>
                    <div class="discount-row">
                        <div class="discount-input-wrap">
                            <input type="text" name="discountCode" id="discountCode"
                                   class="discount-input"
                                   placeholder="Discount code"
                                   value="${not empty discountCode ? discountCode : ''}"/>
                        </div>
                        <button type="submit" name="action" value="applyDiscount" class="discount-apply-btn">Apply
                        </button>
                    </div>

                    <c:if test="${not empty discountError}">
                        <p style="font-size:0.78rem;color:var(--rw-danger);margin:-8px 0 12px;">
                                ${discountError}
                        </p>
                    </c:if>
                    <c:if test="${not empty discountSuccess}">
                        <p style="font-size:0.78rem;color:var(--rw-teal);margin:-8px 0 12px;">
                                ${discountSuccess}
                        </p>
                    </c:if>

                    <%-- Price breakdown --%>
                    <div class="summary-row">
                            <span class="label">
                                Rental (<c:out value="${not empty rentalDays ? rentalDays : 4}"/> days)
                            </span>
                        <span>$<c:out value="${not empty rentalPrice ? rentalPrice : '180.00'}"/></span>
                    </div>
                    <div class="summary-row">
                        <span class="label">Shipping</span>
                        <span class="value-free">Free</span>
                    </div>
                    <div class="summary-row">
                        <span class="label">Damage Protection</span>
                        <span>$<c:out value="${not empty damageProtection ? damageProtection : '15.00'}"/></span>
                    </div>
                    <div class="summary-row">
                        <span class="label">Estimated Taxes</span>
                        <span>$<c:out value="${not empty estimatedTax ? estimatedTax : '16.50'}"/></span>
                    </div>

                    <c:if test="${not empty discountAmount}">
                        <div class="summary-row">
                            <span class="label">Discount</span>
                            <span style="color:var(--rw-teal)">-$${discountAmount}</span>
                        </div>
                    </c:if>

                    <%-- Total --%>
                    <div class="summary-total-row">
                        <span class="summary-total-label">Total</span>
                        <span class="summary-total-value">
                                $<c:out value="${not empty orderTotal ? orderTotal : '211.50'}"/>
                            </span>
                    </div>

                    <%-- Confirm button --%>
                    <button type="submit" name="action" value="placeOrder" class="confirm-pay-btn">
                        Confirm &amp; Pay
                    </button>

                    <%-- Legal --%>
                    <p class="summary-legal">
                        By confirming your order, you agree to our
                        <a href="${pageContext.request.contextPath}/rental-agreement.jsp">Rental Agreement</a>
                        and
                        <a href="${pageContext.request.contextPath}/cancellation-policy.jsp">Cancellation Policy</a>.
                    </p>
                </aside>

            </div>
            <%-- end .checkout-layout --%>
        </form>

    </div>
    <%-- end .checkout-container --%>
</main>

<jsp:include page="components/footer.jsp"/>

<jsp:include page="components/scripts.jsp">
    <jsp:param name="extraJS" value="${pageContext.request.contextPath}/assets/js/profile.js"/>
    <jsp:param name="extraJS" value="${pageContext.request.contextPath}/assets/js/checkout.js"/>
</jsp:include>

</body>
</html>