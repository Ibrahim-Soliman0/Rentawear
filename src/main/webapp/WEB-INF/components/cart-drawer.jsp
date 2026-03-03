<%-- cart-drawer.jsp — Cart slide-out drawer ONLY
     No parameters needed. Opened via data-bs-target="#cartDrawer".
     Items are injected dynamically by app.js (Cart module).
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<div class="offcanvas offcanvas-end rw-cart-drawer"
     tabindex="-1" id="cartDrawer" aria-labelledby="cartDrawerLabel">

  <!-- Header -->
  <div class="offcanvas-header rw-cart-header">
    <h5 class="rw-cart-title" id="cartDrawerLabel">
      <i class="bi bi-bag me-2"></i>My Bag
      <span class="rw-cart-header-count" id="cartHeaderCount">0 items</span>
    </h5>
    <button type="button" class="btn-close"
            data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>

  <!-- Body: scrollable item list, populated by app.js -->
  <div class="offcanvas-body rw-cart-body" id="cartItemsList">
    <div class="rw-cart-empty" id="cartEmpty">
      <i class="bi bi-bag-x"></i>
      <p>Your bag is empty</p>
      <span>Add items to get started</span>
    </div>
  </div>

  <!-- Footer: order summary + action buttons, always visible -->
  <div class="rw-cart-footer" id="cartFooter">

    <div class="rw-cart-summary">
      <div class="rw-cart-summary-row">
        <span>Subtotal</span>
        <span id="cartSubtotal">$0.00</span>
      </div>
      <div class="rw-cart-summary-row rw-cart-summary-total">
        <span>Total</span>
        <span id="cartTotal">$0.00</span>
      </div>
    </div>

    <div class="rw-cart-actions">
      <button class="rw-cart-btn-secondary" data-bs-dismiss="offcanvas">
        <i class="bi bi-arrow-left me-2"></i>Continue Shopping
      </button>
      <a href="${pageContext.request.contextPath}/checkout" class="rw-cart-btn-primary">
        Proceed to Checkout<i class="bi bi-arrow-right ms-2"></i>
      </a>
    </div>

  </div>
</div>