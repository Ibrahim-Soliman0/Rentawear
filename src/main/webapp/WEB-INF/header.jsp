<%-- header.jsp — Page <head> only --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <meta name="theme-color" content="#00A898"/>
  <meta name="ctx" content="${pageContext.request.contextPath}"/>

  <title>${not empty param.title ? param.title : 'Rentawear – Rent Premium Fashion'}</title>
  <meta name="description" content="${not empty param.description ? param.description : 'Rent designer dresses, suits, and more for any occasion.'}"/>

  <!-- Google Fonts — Inter (auth forms) + Cormorant Garamond + DM Sans (new editorial UI) -->
  <link rel="preconnect" href="https://fonts.googleapis.com"/>
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Cormorant+Garamond:ital,wght@0,300;0,400;1,300;1,400&family=DM+Sans:wght@300;400;500&display=swap" rel="stylesheet"/>

  <!-- Bootstrap 5.3 CSS -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>

  <!-- Bootstrap Icons -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>

  <!-- Custom Styles -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css"/>

  <!-- Extra page-specific CSS hook -->
  ${param.extraCSS}
</head>
<body>
