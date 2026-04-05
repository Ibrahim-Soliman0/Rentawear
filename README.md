# Rentawear - Apparel Rental Web Application - ITI

**Project Name**: Rentawear  
**Slogan**: _"Fashion For Every Occasion, Just a Click Away"_

---

## 🔹 About the Project

Rentawear is a **Java-based full-stack e-commerce web application** dedicated to renting out apparel and fashion wear. Developed as part of the **ITI Professional Training Program**, it demonstrates expertise in **Jakarta EE web development, ORM integration, and responsive user interfaces**.

### **📌 What This Project Showcases**

✔ **Jakarta Servlets & JSP/JSTL** for dynamic, responsive web pages  
✔ **Hibernate ORM & JPA** for scalable database management and object-relational mapping  
✔ **HikariCP** for high-performance JDBC connection pooling  
✔ **MVC Design Pattern** for maintainable and scalable architecture  
✔ **jBcrypt Encryption** for secure password hashing and user authentication  
✔ **Thumbnailator** for efficient on-the-fly image processing and compression  
✔ **MapStruct** for clean and automated Data Transfer Object (DTO) mapping

---

## 🎯 Key Features

### User Features

**Product Catalog** – Browse fashion wear through structured categories  
**Ordering System** – End-to-end cart and checkout cycle for seamless rentals  
**User Authentication** – Secure registration and login using jBcrypt hashing  
**Image Gallery** – View high-quality product images, resized and compressed by Thumbnailator  
**Profile Management** – Manage personal details and view rental history

### Admin Features

**Product Management** – Add, update, or remove clothing items and their mappings  
**Category Management** – Organize the catalog efficiently  
**Order Tracking** – Monitor user rental requests and current statuses  

---

## 👤 Rentawear Application

The application provides intuitive interfaces for both shoppers and administrators.

### Sign in/up
1. **User Registration**: Secure account creation.
2. **User Login**: Access to personal cart and rental history.

### User Dashboard
1. **Catalog Browsing**
   - Users can browse various categories and view detailed product mappings.
2. **Shopping Cart**
   - Add items to the cart and proceed to an end-to-end checkout cycle.
3. **Profile Settings**
   - Manage account details and track ongoing rentals.

---

## 📸 Screenshots

Here is a glimpse of the Rentawear application interfaces:

### User Interface
- **Home Page**:
  ![Home Page](screens/home-page.png)
- **Register**:
  ![Register](screens/register.png)
- **Sign In**:
  ![Sign In](screens/signin.png)
- **Categories**:
  ![Categories](screens/categories.png)
- **Search**:
  ![Search](screens/search.png)
- **Detailed Search**:
  ![Detailed Search](screens/detailed-search.png)
- **Products**:
  ![Products](screens/products.png)
- **Quick View**:
  ![Quick View](screens/quick-view.png)
- **Product Full Page**:
  ![Product Full Page](screens/product-full-page.png)
- **Cart**:
  ![Cart](screens/cart.png)
- **Checkout Process**:
  ![Checkout](screens/checkout.png)
- **Rent History**:
  ![Rent History](screens/rent-history.png)
- **User Profile**:
  ![User Profile](screens/user-profile.png)
  ![User Profile 2](screens/user-profile2.png)
- **Billing Limits**:
  ![Billing Limits](screens/user-billing-limits.png)
- **Add Card**:
  ![Add Card](screens/user-add-card.png)


### Admin Interface
- **Inventory/Dashboard**:
  ![Admin Inventory](screens/admin-inventory.png)
- **Add Category**:
  ![Add Category](screens/admin-add-cateogry.png)
- **Add Product**:
  ![Add Product](screens/admin-add-product.png)
- **Edit Product**:
  ![Edit Product](screens/admin-edit-product.png)
- **Edit Variants**:
  ![Edit Variants](screens/admin-edit-variants.png)
- **Customers Management**:
  ![Admin Customers](screens/admin-customers.png)
- **Orders Management**:
  ![Admin Orders](screens/admin-orders.png)

---

##  System Architecture

The application follows a **three-tier MVC architecture** with a modular design:

| **Layer**      | **Responsibilities**                                                |
| -------------- | ------------------------------------------------------------------- |
| **Model**      | Business logic, entity mappings, database access (Hibernate, JPA).  |
| **View**       | Jakarta Server Pages (JSP), JSTL, HTML, CSS, JavaScript.            |
| **Controller** | Jakarta Servlets handling user interactions and updating model data.|

---

## 🗄️ Database

The application uses MySQL for production and H2 In-Memory Database for testing:

### Core Entities

- **Users** – Secure accounts and profiles
- **Products** – Fashion items available for rent
- **Categories** – Structured product categorization
- **Orders** – Rental transaction records
- **Images** – Product image metadata and paths

### Schema Diagram

The following diagram demonstrates the database schema design:

![Database Schema](screens/schema.jpg)

---

## Technology Stack

| **Category**        | **Technology**                               |
|---------------------|----------------------------------------------|
| **Language**        | Java 21                                      |
| **Web Layer**       | Jakarta Servlets 6.0, JSP, JSTL              |
| **ORM & Persistence** | Hibernate ORM 6.2, JPA                       |
| **Database**        | MySQL 8+ (Production), H2 Database (Testing) |
| **Connection Pool** | HikariCP                                     |
| **DTO Mapping**     | MapStruct                                    |
| **Security**        | jBcrypt Password Hashing                     |
| **Image Processing** | Thumbnailator                               |
| **JSON Processing** | Gson, Jakarta JSON                           |
| **Logging**         | SLF4J, Log4j2                                |
| **Testing**         | JUnit 5, Mockito                             |
| **Build Tool**      | Maven                                        |

---

## Prerequisites

- **Java Development Kit (JDK) 21**
- **Apache Tomcat (10.1+)** (Recommended for Jakarta EE 10 / Servlet 6.0 support)
- **MySQL Server 8.0+**
- **Apache Maven 3.8+**

---

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Rentawear
```

### 2. Database Setup

1. Create a MySQL database for the project (e.g., `rentawear_db`).
2. Copy the `.env.example` file to create your own `.env` file!

```bash
cp .env.example src/main/resources/.env
```

Update `src/main/resources/.env` with your specific MySQL credentials:

```properties
DB_USER=your_mysql_username
DB_PASSWORD=your_mysql_password
DB_URL=jdbc:mysql://localhost:3306/rentawear_db
LOG_DIR=C:/path/to/your/log/directory
```

### 3. Build the Project

```bash
mvn clean install
```

This compiles the source code, runs integration tests (using an isolated **H2 Database**), and packages the application into a `.war` file inside the `target/` directory.

---

## ▶️ Running the Application

### Option 1: Using Standalone Tomcat

1. Copy the generated `target/Rentawear.war` to the `webapps/` directory of your Apache Tomcat installation.
2. Start the Tomcat server.
3. Access the application at: `http://localhost:8080/Rentawear`

### Option 2: Using Maven Tomcat Plugin

**Using Maven:**
```bash
mvn tomcat7:run
```

Access the application per the console output (typically `http://localhost:8080/rentawear`).

---

## 🧪 Testing

The repository comes with expansive unit and integration test coverage for the persistence and service layers. Tests spin up an **H2 In-Memory Database** so that they run independently without adjusting the main local MySQL instance.

```bash
mvn test
```

---

## 📁 Project Structure

```
Rentawear/
├── src/main/java/       # Source code (Entities, Servlets, Services, Mappers)
├── src/main/resources/  # Configurations (.env, persistence.xml, log4j2.properties)
├── src/main/webapp/     # Web tier (JSP, CSS, JS, WEB-INF)
├── src/test/java/       # JUnit 5 & Mockito test cases
├── target/              # Compiled classes and packaged WAR
└── pom.xml              # Maven dependencies and build config
```

---

## 🔐 Security Features

- **jBcrypt** – Secure password hashing for user accounts
- **Environment Variables** – Hidden credentials via `.env` files
- **Tomcat Configuration** – Secure application roles and constraints (via `web.xml`)
- **Jakarta Servlets Filter** – Request authentication and authorization handling

---

## 🤝 Contributors

**Team Members:**  
- [Noureen Ashraf](https://github.com/Noureenaboarab)
- [Ibrahim Soliman](https://github.com/Ibrahim-Soliman0)

**Mentors & Instructors:**  
Information Technology Institute (ITI)

---

## 📄 License

This project is developed as part of the ITI Java curriculum and is for educational purposes.
