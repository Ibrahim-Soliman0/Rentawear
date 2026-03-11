<%-- scripts.jsp — All JS loaded at the bottom of every page --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

  <!-- Bootstrap is kept for legacy/admin/auth pages. The redesign shell uses custom JS. -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css"/>
  <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>

  <%-- Load order matters:
       1. utils.js  — CTX and esc() used by all other files
       2. search.js — depends on utils.js
       3. cart.js   — depends on utils.js
       4. main.js   — page interactions, depends on utils.js
  --%>
  <script src="${pageContext.request.contextPath}/assets/js/utils.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/search.js"></script>
  <script src ="${pageContext.request.contextPath}/assets/js/card-factory.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/cart.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/quick-view.js"></script>
  <script src="${pageContext.request.contextPath}/assets/js/shell-manager.js"></script>

<%-- Extra page-specific script. --%>
<c:if test="${not empty param.extraJS}">
  <script src="${param.extraJS}"></script>
</c:if>

</body>
</html>
