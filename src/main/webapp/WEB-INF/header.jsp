<%-- header.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <!-- rest of your code -->
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <meta name="theme-color" content="#00B5A5"/>

  <!-- Dynamic Page Title - can be overridden in child pages -->
  <title>${param.title != null ? param.title : 'Rentawear – Rent Premium Fashion'}</title>

  <!-- Meta Description - can be overridden -->
  <meta name="description" content="${param.description != null ? param.description : 'Rentawear – Rent designer dresses, suits, and more for any occasion. Affordable fashion at your fingertips.'}"/>

  <!-- Google Fonts – Inter -->
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css"/>

  <!-- Bootstrap 5.3 CSS -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />

  <!-- Bootstrap Icons -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet" />

    <!-- Custom Styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css"/>
  <!-- Additional CSS can be injected from child pages -->
  ${param.extraCSS}
</head>
<body>

  <!-- ============================================
       TOP NAVBAR
       ============================================ -->
  <nav class="rw-topnav">
    <div class="container-lg d-flex align-items-center justify-content-between">
      <!-- Logo -->
      <a href="${pageContext.request.contextPath}/" class="rw-logo" aria-label="Rentawear Home">
        <span class="logo-dark">renta</span><span class="logo-teal">wear</span>
      </a>

      <!-- Desktop Nav Links -->
      <div class="rw-desktop-nav align-items-center gap-1">
        <a href="${pageContext.request.contextPath}/" class="nav-link ${param.activePage == 'home' ? 'active' : ''}">Home</a>
        <a href="${pageContext.request.contextPath}/explore" class="nav-link ${param.activePage == 'explore' ? 'active' : ''}">Explore</a>
        <a href="${pageContext.request.contextPath}/rentals" class="nav-link ${param.activePage == 'rentals' ? 'active' : ''}">Rentals</a>
        <a href="${pageContext.request.contextPath}/occasions" class="nav-link ${param.activePage == 'occasions' ? 'active' : ''}">Occasions</a>
      </div>

      <!-- Right Actions -->
      <div class="d-flex align-items-center gap-2">
        <!-- Desktop nav extras -->
        <div class="rw-desktop-nav align-items-center gap-2">
          <a href="${pageContext.request.contextPath}/wishlist" class="nav-link" aria-label="Wishlist">
            <i class="bi bi-heart"></i>
          </a>
          <a href="${pageContext.request.contextPath}/cart" class="nav-link" aria-label="Shopping Bag">
            <i class="bi bi-bag"></i>
          </a>
        </div>
        <!-- Notification Bell -->
        <button class="notification-btn" aria-label="Notifications">
          <i class="bi bi-bell"></i>
          <span class="notification-dot"></span>
        </button>
        <!-- Profile (desktop) -->
        <div class="rw-desktop-nav">
          <a href="${pageContext.request.contextPath}/profile" class="nav-link" aria-label="Profile">
            <i class="bi bi-person-circle" style="font-size:1.3rem;"></i>
          </a>
        </div>
      </div>
    </div>
  </nav>

  <!-- ============================================
       SEARCH BAR
       ============================================ -->
  <section class="rw-search-bar">
    <div class="container-lg">
      <div class="input-group">
        <span class="input-group-text"><i class="bi bi-search"></i></span>
        <input type="text" class="form-control" placeholder="Search dresses, suits, brands..." aria-label="Search"
               value="${param.searchQuery != null ? param.searchQuery : ''}"/>
        <button class="filter-btn" type="button" aria-label="Filter"><i class="bi bi-sliders"></i></button>
      </div>
    </div>
  </section>

  <!-- ============================================
       CATEGORY PILLS
       ============================================ -->
  <section class="rw-categories">
    <div class="container-lg d-flex gap-2">
      <span class="rw-pill ${param.activeCategory == 'all' ? 'active' : ''}" data-category="all">All Items</span>
      <span class="rw-pill ${param.activeCategory == 'dresses' ? 'active' : ''}" data-category="dresses">Dresses</span>
      <span class="rw-pill ${param.activeCategory == 'suits' ? 'active' : ''}" data-category="suits">Suits &amp; Tailoring</span>
      <span class="rw-pill ${param.activeCategory == 'accessories' ? 'active' : ''}" data-category="accessories">Accessories</span>
      <span class="rw-pill ${param.activeCategory == 'shoes' ? 'active' : ''}" data-category="shoes">Shoes</span>
      <span class="rw-pill ${param.activeCategory == 'bags' ? 'active' : ''}" data-category="bags">Bags</span>
    </div>
  </section>