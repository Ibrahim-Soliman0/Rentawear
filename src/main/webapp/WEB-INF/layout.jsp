<%-- layout.jsp
     Single coordinator: includes every shared UI component.
     Each page calls THIS file once.

     Usage in any page (e.g. index.jsp):
       <jsp:include page="WEB-INF/layout.jsp">
         <jsp:param name="activePage" value="home"/>
         <jsp:param name="title"      value="Rentawear – Home"/>
       </jsp:include>
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<%-- 1. <head> + <body> open tag --%>
<jsp:include page="header.jsp">
  <jsp:param name="title"       value="${param.title}"/>
  <jsp:param name="description" value="${param.description}"/>
  <jsp:param name="extraCSS"    value="${param.extraCSS}"/>
</jsp:include>

<%-- 2. Top navigation bar --%>
<%-- 2a. Announcement bar (matches redesign HTML) --%>
<div class="ann-bar">
  Free delivery on orders over $60 <span>&middot;</span> Dry-cleaned &amp; insured <span>&middot;</span> Easy returns within 48 hrs
</div>

<%-- 2b. Top navigation bar --%>
<jsp:include page="components/navbar.jsp">
  <jsp:param name="activePage" value="${param.activePage}"/>
</jsp:include>

<%-- 3. Search modal (custom overlay, hidden until search icon clicked) --%>
<jsp:include page="components/search-modal.jsp"/>

<%-- 4. Cart drawer (custom overlay, hidden until cart icon clicked) --%>
<jsp:include page="components/cart-drawer.jsp"/>

<%-- 5. Mobile drawer (custom overlay, hidden until hamburger clicked) --%>
<jsp:include page="components/mobile-menu.jsp">
  <jsp:param name="activePage" value="${param.activePage}"/>
</jsp:include>

<%-- 6. Quick view overlay --%>
<jsp:include page="components/quick-view.jsp"/>
