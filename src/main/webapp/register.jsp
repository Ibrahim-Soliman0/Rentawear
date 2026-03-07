<%-- register.jsp — Rentawear Register Page --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<%
  String errorMsg    = (String) request.getAttribute("errorMsg");
%>

<jsp:include page="WEB-INF/layout.jsp">
  <jsp:param name="activePage"  value="account"/>
  <jsp:param name="title"       value="Create Account – Rentawear"/>
  <jsp:param name="description" value="Join Rentawear and start renting premium fashion today."/>
</jsp:include>

<main class="rw-main rw-auth-bg">
  <div class="rw-auth-wrap">

    <div class="rw-auth-card">

      <!-- Logo -->
      <a href="${pageContext.request.contextPath}/" class="rw-logo rw-auth-logo">
        <span class="logo-dark">renta</span><span class="logo-teal">wear</span>
      </a>
      <p class="rw-auth-subtitle">Create your free account and start renting</p>

      <!-- Trust badges -->
      <div class="rw-trust-row">
        <span class="rw-trust-badge"><i class="bi bi-shield-check-fill"></i> Secure</span>
        <span class="rw-trust-badge"><i class="bi bi-lock-fill"></i> Private</span>
        <span class="rw-trust-badge"><i class="bi bi-star-fill"></i> Free to join</span>
      </div>

      <!-- Server-side error -->
      <% if (errorMsg != null && !errorMsg.isEmpty()) { %>
      <div class="rw-auth-alert rw-auth-alert--error">
        <i class="bi bi-exclamation-circle-fill"></i> <%= errorMsg %>
      </div>
      <% } %>

      <!-- Form -->
      <form id="registerForm" action="${pageContext.request.contextPath}/RegisterServlet" method="post" novalidate>

        <!-- Full Name -->
        <div class="rw-form-group">
          <label for="fullName" class="rw-form-label">Full Name</label>
          <div class="rw-input-wrap">
            <input
                    type="text"
                    id="fullName"
                    name="fullName"
                    class="rw-form-input"
                    placeholder="e.g. Jane Doe"
                    autocomplete="name"
                    required
            />
          </div>
          <div class="rw-field-error" id="fullNameError"></div>
        </div>

        <!-- Email + Password row -->
        <div class="rw-form-row-2">

          <div class="rw-form-group">
            <label for="regEmail" class="rw-form-label">Email Address</label>
            <div class="rw-input-wrap">
              <i class="bi bi-envelope rw-input-icon"></i>
              <input
                      type="email"
                      id="regEmail"
                      name="email"
                      class="rw-form-input"
                      placeholder="jane@example.com"
                      autocomplete="email"
                      required
              />
            </div>
            <div class="rw-field-error" id="regEmailError"></div>
          </div>

          <div class="rw-form-group">
            <label for="regPassword" class="rw-form-label">Password</label>
            <div class="rw-input-wrap">
              <i class="bi bi-lock rw-input-icon"></i>
              <input
                      type="password"
                      id="regPassword"
                      name="password"
                      class="rw-form-input"
                      placeholder="••••••••"
                      autocomplete="new-password"
                      required
              />
              <button type="button" class="rw-eye-btn" data-target="regPassword" aria-label="Toggle password">
                <i class="bi bi-eye"></i>
              </button>
            </div>
            <div class="rw-field-error" id="regPasswordError"></div>
            <!-- Password strength -->
            <div class="rw-pwd-strength">
              <div class="rw-pwd-strength-bar">
                <div class="rw-pwd-strength-fill" id="pwdStrengthFill"></div>
              </div>
              <span class="rw-pwd-strength-label" id="pwdStrengthLabel"></span>
            </div>
          </div>

        </div>

        <!-- Date of Birth + Job Title row -->
        <div class="rw-form-row-2">

          <div class="rw-form-group">
            <label for="dob" class="rw-form-label">Date of Birth</label>
            <div class="rw-input-wrap">
              <input
                      type="date"
                      id="dob"
                      name="dob"
                      class="rw-form-input"
                      autocomplete="bday"
              />
            </div>
          </div>

          <div class="rw-form-group">
            <label for="jobTitle" class="rw-form-label">Job Title</label>
            <div class="rw-input-wrap">
              <i class="bi bi-briefcase rw-input-icon"></i>
              <input
                      type="text"
                      id="jobTitle"
                      name="jobTitle"
                      class="rw-form-input"
                      placeholder="e.g. Marketing Director"
                      autocomplete="organization-title"
              />
            </div>
          </div>

        </div>

        <!-- Shipping Address -->
        <div class="rw-form-group">
          <label for="shippingAddress" class="rw-form-label">Shipping Address</label>
          <div class="rw-input-wrap rw-input-wrap--textarea">
            <textarea
                    id="shippingAddress"
                    name="shippingAddress"
                    class="rw-form-input rw-form-textarea"
                    placeholder="Enter your primary shipping address"
                    rows="3"
                    autocomplete="street-address"
            ></textarea>
          </div>
        </div>

        <!-- Style Interests + Credit Limit row -->
        <div class="rw-form-row-2">

          <div class="rw-form-group">
            <label for="styleInterests" class="rw-form-label">Style Interests</label>
            <div class="rw-input-wrap rw-input-wrap--select">
              <select id="styleInterests" name="styleInterests" class="rw-form-input rw-form-select">
                <option value="" disabled selected>Select categories</option>
                <option value="casual">Casual</option>
                <option value="formal">Formal</option>
                <option value="evening">Evening Wear</option>
                <option value="business">Business</option>
                <option value="wedding">Wedding</option>
                <option value="streetwear">Streetwear</option>
                <option value="resort">Resort / Vacation</option>
              </select>
              <i class="bi bi-chevron-down rw-select-chevron"></i>
            </div>
          </div>

          <div class="rw-form-group">
            <label for="creditLimit" class="rw-form-label">Requested Credit Limit</label>
            <div class="rw-input-wrap">
              <span class="rw-input-prefix">$</span>
              <input
                      type="number"
                      id="creditLimit"
                      name="creditLimit"
                      class="rw-form-input"
                      placeholder="5,000"
                      min="0"
                      step="500"
                      value="5000"
              />
            </div>
          </div>

        </div>

        <!-- Terms checkbox -->
        <div class="rw-form-group">
          <label class="rw-checkbox-label">
            <input type="checkbox" name="agreeTerms" id="agreeTerms" class="rw-checkbox" required/>
            <span class="rw-checkbox-custom"></span>
            I agree to the
            <a href="${pageContext.request.contextPath}/terms" target="_blank">Terms of Service</a>
            and
            <a href="${pageContext.request.contextPath}/privacy" target="_blank">Privacy Policy</a>
          </label>
          <div class="rw-field-error" id="termsError"></div>
        </div>

        <!-- Submit -->
        <button type="submit" class="rw-auth-btn-primary" id="registerBtn">
          <span class="rw-btn-text">Create Account</span>
          <span class="rw-btn-spinner d-none">
            <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
          </span>
        </button>

      </form>

      <!-- Footer link -->
      <p class="rw-auth-switch" style="margin-top: 1.25rem;">
        Already have an account?
        <a href="login.jsp">Sign in</a>
      </p>

    </div><!-- /.rw-auth-card -->
  </div><!-- /.rw-auth-wrap -->
</main>

<jsp:include page="WEB-INF/components/footer.jsp"/>
<jsp:include page="WEB-INF/components/scripts.jsp">
  <jsp:param name="extraJS" value="${pageContext.request.contextPath}/assets/js/auth.js"/>
</jsp:include>
