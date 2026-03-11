<%-- register.jsp — Rentawear Register Page --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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
        <span class="rw-trust-badge"><i class="bi bi-shield-fill-check"></i> Secure</span>
        <span class="rw-trust-badge"><i class="bi bi-lock-fill"></i> Private</span>
        <span class="rw-trust-badge"><i class="bi bi-star-fill"></i> Free to join</span>
      </div>

      <!-- Server-side error -->
      <c:if test="${not empty errorMsg}">
        <div class="rw-auth-alert rw-auth-alert--error">
          <i class="bi bi-exclamation-circle-fill"></i> ${errorMsg}
        </div>
      </c:if>

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
                    maxlength="60"
            />
          </div>
          <div class="rw-field-error" id="fullNameError"></div>
        </div>

        <!-- Gender -->
        <div class="rw-form-group">
          <label class="rw-form-label">Gender</label>
          <div class="rw-gender-row">
            <label class="rw-gender-option">
              <input type="radio" name="gender" value="male" class="rw-gender-radio" required/>
              <span class="rw-gender-btn">
        <i class="bi bi-gender-male"></i> Male
      </span>
            </label>
            <label class="rw-gender-option">
              <input type="radio" name="gender" value="female" class="rw-gender-radio" required/>
              <span class="rw-gender-btn">
        <i class="bi bi-gender-female"></i> Female
      </span>
            </label>
          </div>
          <div class="rw-field-error" id="genderError"></div>
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
                      maxlength="128"
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
                      maxlength="100"
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
              <i class="bi bi-calendar3 rw-input-icon"></i>
              <input
                      type="text"
                      id="dob"
                      name="dob"
                      class="rw-form-input"
                      placeholder="MM/DD/YYYY"
                      autocomplete="off"
                      readonly
              />
            </div>
            <div class="rw-field-error" id="dobError"></div>
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
                      maxlength="60"
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
                    maxlength="250"
            ></textarea>
          </div>
        </div>
        <!-- Credit Limit — single field -->
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
                    max="999999"
            />
          </div>
        </div>

        <!-- Style Interests — full width -->
        <div class="rw-form-group">
          <label class="rw-form-label">Style Interests</label>
          <div class="rw-interests-grid">
            <label class="rw-interest-option">
              <input type="checkbox" name="styleInterests" value="casual" class="rw-interest-check"/>
              <span class="rw-interest-btn">Casual</span>
            </label>
            <label class="rw-interest-option">
              <input type="checkbox" name="styleInterests" value="formal" class="rw-interest-check"/>
              <span class="rw-interest-btn">Formal</span>
            </label>
            <label class="rw-interest-option">
              <input type="checkbox" name="styleInterests" value="evening" class="rw-interest-check"/>
              <span class="rw-interest-btn">Evening Wear</span>
            </label>
            <label class="rw-interest-option">
              <input type="checkbox" name="styleInterests" value="business" class="rw-interest-check"/>
              <span class="rw-interest-btn">Business</span>
            </label>
            <label class="rw-interest-option">
              <input type="checkbox" name="styleInterests" value="wedding" class="rw-interest-check"/>
              <span class="rw-interest-btn">Wedding</span>
            </label>
            <label class="rw-interest-option">
              <input type="checkbox" name="styleInterests" value="streetwear" class="rw-interest-check"/>
              <span class="rw-interest-btn">Streetwear</span>
            </label>
            <label class="rw-interest-option">
              <input type="checkbox" name="styleInterests" value="resort" class="rw-interest-check"/>
              <span class="rw-interest-btn">Resort / Vacation</span>
            </label>
            <label class="rw-interest-option">
              <input type="checkbox" name="styleInterests" value="sport" class="rw-interest-check"/>
              <span class="rw-interest-btn">Sportswear</span>
            </label>
            <label class="rw-interest-option">
              <input type="checkbox" name="styleInterests" value="vintage" class="rw-interest-check"/>
              <span class="rw-interest-btn">Vintage</span>
            </label>
            <label class="rw-interest-option">
              <input type="checkbox" name="styleInterests" value="luxury" class="rw-interest-check"/>
              <span class="rw-interest-btn">Luxury</span>
            </label>
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


