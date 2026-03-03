<%-- scripts.jsp — All JS loaded at the bottom of every page --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

  <!-- Bootstrap 5 JS bundle (includes Popper) -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

  <!-- Custom app JS -->
  <script src="${pageContext.request.contextPath}/assets/js/app.js"></script>

  <!-- Extra page-specific JS hook -->
  ${param.extraJS}

</body>
</html>