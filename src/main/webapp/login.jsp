<%-- login.jsp — Rentawear Login Page --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="WEB-INF/layout.jsp">
  <jsp:param name="activePage"  value="account"/>
  <jsp:param name="title"       value="Sign In – Rentawear"/>
  <jsp:param name="description" value="Sign in to your Rentawear account to manage rentals and orders."/>
</jsp:include>

<main class="rw-main rw-auth-bg">
  <div class="rw-auth-wrap">

    <div class="rw-auth-card">

      <!-- Logo -->
      <a href="${pageContext.request.contextPath}/" class="rw-logo rw-auth-logo">
        <span class="logo-dark">renta</span><span class="logo-teal">wear</span>
      </a>
      <p class="rw-auth-subtitle">Welcome back — sign in to continue</p>

      <!-- Server-side alerts -->
      <c:if test="${not empty requestScope.errorMsg}">
        <div class="rw-auth-alert rw-auth-alert--error">
          <i class="bi bi-exclamation-circle-fill"></i> ${requestScope.errorMsg}
        </div>
      </c:if>
      <c:if test="${not empty param.successMsg}">
        <div class="rw-auth-alert rw-auth-alert--success">
          <i class="bi bi-check-circle-fill"></i> ${param.successMsg}
        </div>
      </c:if>

      <!-- Form -->
      <form id="loginForm" action="${pageContext.request.contextPath}/login" method="post" novalidate>

        <!-- Email -->
        <div class="rw-form-group">
          <label for="loginEmail" class="rw-form-label">Email address</label>
          <div class="rw-input-wrap">
            <i class="bi bi-envelope rw-input-icon"></i>
            <input
                    type="email"
                    id="loginEmail"
                    name="email"
                    class="rw-form-input"
                    placeholder="you@example.com"
                    value="${not empty requestScope.prefillEmail ? requestScope.prefillEmail : param.email}"
                    autocomplete="email"
                    required
                    maxlength="100"
            />
          </div>
          <div class="rw-field-error" id="emailError"></div>
        </div>

        <!-- Password -->
        <div class="rw-form-group">
          <div class="rw-label-row">
            <label for="loginPassword" class="rw-form-label">Password</label>
            <a href="${pageContext.request.contextPath}/forgot-password" class="rw-forgot-link">Forgot password?</a>
          </div>
          <div class="rw-input-wrap">
            <i class="bi bi-lock rw-input-icon"></i>
            <input
                    type="password"
                    id="loginPassword"
                    name="password"
                    class="rw-form-input"
                    placeholder="Enter your password"
                    autocomplete="current-password"
                    required
                    maxlength="128"
            />
            <button type="button" class="rw-eye-btn" data-target="loginPassword" aria-label="Toggle password">
              <i class="bi bi-eye"></i>
            </button>
          </div>
          <div class="rw-field-error" id="passwordError"></div>
        </div>

        <!-- Remember me -->
        <div class="rw-remember-row">
          <label class="rw-checkbox-label">
            <input type="checkbox" name="rememberMe" id="rememberMe" class="rw-checkbox"/>
            <span class="rw-checkbox-custom"></span>
            Keep me signed in
          </label>
        </div>

        <!-- Submit -->
        <button type="submit" class="rw-auth-btn-primary" id="loginBtn">
          <span class="rw-btn-text">Sign in</span>
          <span class="rw-btn-spinner d-none">
            <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
          </span>
        </button>

      </form>

      <!-- Footer link -->
      <p class="rw-auth-switch" style="margin-top: 1.25rem">
        Don't have an account?
        <a href="${pageContext.request.contextPath}/register.jsp">Create one free</a>
      </p>

    </div><!-- /.rw-auth-card -->
  </div><!-- /.rw-auth-wrap -->
</main>

<jsp:include page="WEB-INF/components/footer.jsp"/>
<jsp:include page="WEB-INF/components/scripts.jsp">
  <jsp:param name="extraJS" value="${pageContext.request.contextPath}/assets/js/auth.js"/>
</jsp:include>
