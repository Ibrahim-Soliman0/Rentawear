<%-- scripts.jsp — All JS loaded at the bottom of every page --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- Bootstrap 5 JS bundle (includes Popper) -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<%-- Load order matters:
     1. utils.js        — CTX and esc(), required by everything
     2. card-factory.js — CardFactory, required by any page with product cards
     3. search.js       — depends on utils.js + CardFactory.searchResult()
     4. cart.js         — depends on utils.js

     Page-specific files (injected via ${param.extraJS}):
       home.js          — trending + occasion loaders (index.jsp only)
       product.js       — detail page interactions (product page only)
--%>
<script src="${pageContext.request.contextPath}/assets/js/utils.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/card-factory.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/search.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cart.js"></script>

<%-- Extra page-specific script. --%>
<c:if test="${not empty param.extraJS}">
  <script src="${param.extraJS}"></script>
</c:if>

</body>
</html>