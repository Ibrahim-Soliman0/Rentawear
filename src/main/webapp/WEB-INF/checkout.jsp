<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         session="false" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<jsp:include page="layout.jsp">
    <jsp:param name="title" value="Checkout – Rentawear"/>
    <jsp:param name="description" value="Complete your rental order."/>
    <jsp:param name="activePage" value="checkout"/>
</jsp:include>

<html lang="en">
<head>
    <title>Checkout – Rentawear</title>
</head>
<body>

<main class="rw-main checkout-page">
    <div class="checkout-container">

        <h1 class="checkout-title">Checkout</h1>

        <%-- Flash messages --%>
        <c:if test="${not empty errorMessage}">
            <div class="rw-auth-alert rw-auth-alert--error" style="margin-bottom:20px;">
                <svg width="16" height="16" viewBox="0 0 24 24" stroke="currentColor" fill="none"
                     stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                    ${errorMessage}
            </div>
        </c:if>
        <c:if test="${not empty successMessage}">
            <div class="rw-auth-alert rw-auth-alert--success" style="margin-bottom:20px;">
                <svg width="16" height="16" viewBox="0 0 24 24" stroke="currentColor" fill="none"
                     stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                    <polyline points="22 4 12 14.01 9 11.01"/>
                </svg>
                    ${successMessage}
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/checkout" method="POST" id="checkoutForm">

            <%-- All hidden fields — populated by checkout.js before submit --%>
            <input type="hidden" name="cartJson" id="cartJson" value=""/>
            <input type="hidden" name="selectedPaymentId" id="selectedPaymentId" value=""/>
            <input type="hidden" name="deliveryDate" id="deliveryDateInput" value=""/>
            <input type="hidden" name="returnDate" id="returnDateInput" value=""/>
            <%-- totalAmount lets the servlet check against user.creditLimit() --%>
            <input type="hidden" name="totalAmount" id="totalAmountInput" value=""/>

            <div class="checkout-layout">

                <%-- ── LEFT: Steps ───────────────────────────────────────── --%>
                <div class="checkout-steps">

                    <%-- 1. Rental Period — one row per cart item, filled by checkout.js --%>
                    <section class="checkout-section">
                        <div class="checkout-section-header">
                            <span class="checkout-section-title">1. Rental Period</span>
                            <button type="button" class="checkout-edit-link"
                                    onclick="window.history.back()">Edit
                            </button>
                        </div>
                        <%-- Skeleton shown until JS runs --%>
                        <ul id="rentalItemsList">
                            <li class="rental-skel">
                                <div class="rental-skel-thumb"></div>
                                <div class="rental-skel-lines">
                                    <div class="rental-skel-line rental-skel-line--med"></div>
                                    <div class="rental-skel-line rental-skel-line--short"></div>
                                </div>
                            </li>
                        </ul>
                    </section>

                    <%-- 2. Shipping Address — filled by checkout.js via /user/session --%>
                    <section class="checkout-section">
                        <div class="checkout-section-header">
                            <span class="checkout-section-title">2. Shipping Address</span>
                        </div>
                        <div id="addressContainer">
                            <div class="section-skel">
                                <div class="section-skel-circle"></div>
                                <div class="section-skel-lines">
                                    <div class="section-skel-line section-skel-line--short"></div>
                                    <div class="section-skel-line section-skel-line--long"></div>
                                </div>
                            </div>
                        </div>
                    </section>

                    <%-- 3. Payment Method — filled by checkout.js via /user/session --%>
                    <section class="checkout-section">
                        <div class="checkout-section-header">
                            <span class="checkout-section-title">3. Payment Method</span>
                        </div>
                        <div id="paymentContainer">
                            <div class="section-skel">
                                <div class="section-skel-circle"></div>
                                <div class="section-skel-lines">
                                    <div class="section-skel-line section-skel-line--short"></div>
                                    <div class="section-skel-line section-skel-line--long"></div>
                                </div>
                            </div>
                        </div>
                    </section>

                </div>
                <%-- end .checkout-steps --%>

                <%-- ── RIGHT: Order Summary ────────────────────────────────── --%>
                <aside class="order-summary">
                    <div class="order-summary-title">Order Summary</div>

                    <%-- Items rendered by checkout.js from localStorage --%>
                    <ul id="checkoutCartItems">
                        <li class="co-skeleton">
                            <div class="co-skel-img"></div>
                            <div class="co-skel-lines">
                                <div class="co-skel-line co-skel-line--short"></div>
                                <div class="co-skel-line co-skel-line--med"></div>
                                <div class="co-skel-line co-skel-line--long"></div>
                                <div class="co-skel-line co-skel-line--med"></div>
                            </div>
                        </li>
                    </ul>

                    <%-- Pricing — hidden until JS populates it --%>
                    <div id="checkoutPricing" style="display:none;">
                        <div class="summary-row">
                            <span class="label">Rental subtotal</span>
                            <span id="coSubtotal">$0.00</span>
                        </div>
                        <div class="summary-row">
                            <span class="label">Shipping</span>
                            <span style="color:var(--rw-teal);font-weight:500;">Free</span>
                        </div>
                        <div class="summary-total-row">
                            <span class="summary-total-label">Total</span>
                            <span class="summary-total-value" id="coTotal">$0.00</span>
                        </div>
                    </div>

                    <%-- Credit limit warning — shown by JS when total > creditLimit --%>
                    <div class="credit-warning" id="creditWarning">
                        <svg viewBox="0 0 24 24">
                            <circle cx="12" cy="12" r="10"/>
                            <line x1="12" y1="8" x2="12" y2="12"/>
                            <line x1="12" y1="16" x2="12.01" y2="16"/>
                        </svg>
                        <span id="creditWarningMsg">Your order total exceeds your credit limit.</span>
                    </div>

                    <%-- Submit error injected by checkout.js --%>

                    <button type="submit" name="action" value="placeOrder"
                            class="confirm-pay-btn" id="confirmPayBtn" disabled>
                        Confirm &amp; Pay
                    </button>

                    <p class="summary-legal">
                        By confirming your order, you agree to our
                        Rental Agreement and Cancellation Policy.
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
    <jsp:param name="extraJS" value="${pageContext.request.contextPath}/assets/js/checkout.js"/>
</jsp:include>

</body>
</html>
