# Recipe Budy Meal Application
A recipe management app built with Java Spring and Thymeleaf, allowing users to create, edit, and save their favorite recipes.

---

## Tech Stack

- Java 17
- Spring Boot 3.4.0
- Spring Data JPA
- Spring Security
- Spring Cloud OpenFeign
- MySQL
- Thymeleaf
- Bootstrap 5.3
- Maven

---

## Features

- User registration and authentication
- Create, edit, and delete recipes
- Browse recipes by categories
- Comment and rate recipes
- Save favorite recipes
- Weekly meal planning
- Admin panel for user management

---

## Functionalities

- **Authentication & Authorization** - Spring Security with role-based access (USER, ADMIN)
- **CRUD Operations** - Full recipe management
- **Microservice Communication** - Feign Client integration with Meal Planning service
- **Scheduled Tasks** - Daily comment reports and analytics
- **Caching** - User favorites caching
- **Exception Handling** - Global exception handler
- **Validation** - Bean validation on all forms

---

## Integrations

**Meal Planning Microservice**
- Port: 8081
- Communication: Spring Cloud OpenFeign
- Endpoints: Create, view, and delete meal plans
- Independent microservice architecture
