<%-- cart-drawer.jsp - Redesign cart drawer (matches attached HTML)
     Items injected by assets/js/cart.js using CardFactory.cartItem
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@ page isELIgnored="false" %>

<div class="cart-drawer" id="cartDrawer" aria-hidden="true">
  <div class="cart-drawer-backdrop" id="cartBackdrop"></div>
  <div class="cart-panel" role="dialog" aria-label="Cart drawer">

    <div class="cart-header">
      <div class="cart-header-left">
        <h2 class="cart-title">My Bag</h2>
        <span class="cart-count-label" id="cartCountLabel">0 items</span>
      </div>
      <button class="cart-close" id="cartClose" aria-label="Close cart" type="button">
        <svg viewBox="0 0 24 24" aria-hidden="true"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <div class="cart-body" id="cartBody">
      <div class="cart-empty" id="cartEmpty">
        <div class="cart-empty-icon">
          <i class="bi bi-bag"></i>
        </div>
        <h3>Your bag is empty</h3>
        <p>Add items to your bag to get started</p>
      </div>

      <%-- Cart items container - CardFactory.cartItem li nodes will be appended here --%>
      <ul class="cart-items" id="cartItems" style="display:none;"></ul>
    </div>

    <div class="cart-footer" id="cartFooter">
      <div class="cart-summary" id="cartSummary">
        <div class="cart-summary-row">
          <span>Rental subtotal</span>
          <span id="cartSubtotal">$0.00</span>
        </div>
        <div class="cart-summary-row">
          <span>Delivery</span>
          <span id="cartDelivery">Free</span>
        </div>
        <div class="cart-summary-row total">
          <span>Total</span>
          <span id="cartTotal">$0.00</span>
        </div>
      </div>

      <%--TODO: Hide Button If cart is Empty--%>
      <a class="cart-cta" href="${pageContext.request.contextPath}/checkout">
        Proceed to Checkout →
      </a>

      <span class="cart-continue" id="cartContinue" role="button" tabindex="0">← Continue Shopping</span>
    </div>

  </div>
</div>