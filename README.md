# The Choice Company — Spring Boot Backend

> Production-oriented REST API backend for **The Choice Company (TCC)** corporate gifting platform.

**Spring Boot 3.3.2 · Java 21 · PostgreSQL · Spring Security · JWT · Flyway · REST API · Cloudinary**


---

## 📌 Table of Contents

- [Project Overview](project-overview)
- [Core Capabilities](core-capabilities)
- [System Architecture](system-architecture)
- [Project Structure](project-structure)
- [Technology Stack](technology-stack)
- [Application Layers](application-layers)
- [Authentication & Security](authentication--security)
- [Admin Roles](admin-roles)
- [Product & Inventory System](product--inventory-system)
- [Product Image Management](product-image-management)
- [Cloudinary Integration](cloudinary-integration)
- [Database Architecture](database-architecture)
- [Flyway Migrations](flyway-migrations)
- [REST API](rest-api)
- [API Response Standards](api-response-standards)
- [Exception Handling](exception-handling)
- [Email System](email-system)
- [External Integrations](external-integrations)
- [Configuration & Profiles](configuration--profiles)
- [Environment Variables](environment-variables)
- [Prerequisites](prerequisites)
- [Local Development Setup](local-development-setup)
- [Swagger / OpenAPI](swagger--openapi)
- [Health Monitoring](health-monitoring)
- [Maven Commands](maven-commands)
- [Testing](testing)
- [Request Processing Flow](request-processing-flow)
- [Development Guidelines](development-guidelines)
- [Git Workflow](git-workflow)
- [Security Checklist](security-checklist)
- [Production Deployment Checklist](production-deployment-checklist)
- [Future Enhancements](future-enhancements)
- [Project Summary](project-summary)
- [License](license)

---

# 📌 Project Overview

The **The Choice Company (TCC) Backend** is a Spring Boot REST API that provides the server-side foundation for a corporate gifting platform.

The backend is designed around a layered architecture that separates:

- REST controllers
- Request/response DTOs
- Business services
- JPA entities
- Repositories
- Security
- Database migrations
- External integrations
- Centralized exception handling
- Validation
- Testing

The API is intended to serve both the public website and the protected administration dashboard.

---

# 🚀 Core Capabilities

The backend currently supports the following major capabilities:

### 🔐 Authentication & Authorization

- Administrator authentication
- JWT-based stateless authentication
- BCrypt password hashing
- Role-based authorization
- Protected admin APIs
- `SUPER_ADMIN` and `CONTENT_MANAGER` roles

### 📦 Product Management

- Product catalog
- Product details by slug
- Product pricing tiers
- Product inventory support
- Multiple product images
- Primary product image
- Image ordering
- Product image metadata
- Admin product management APIs

### 🖼️ Image Management

- Cloudinary-based image storage
- Single image upload
- Bulk image upload
- Gallery image upload
- Multiple images per product
- File validation
- Duplicate image protection
- Cloudinary upload rollback
- Image transformation URLs

### 📨 Customer & Content Management

- Customer inquiries
- Inquiry notes
- Catalogue requests
- Blog content
- Gallery content
- Newsletter subscriptions
- Demo/sample orders

### 🔌 External Integrations

- Cloudinary
- SMTP / transactional email
- WhatsApp
- Razorpay
- AWS SDK support

### 🛠️ Backend Infrastructure

- PostgreSQL
- Hibernate / JPA
- Flyway database migrations
- HikariCP connection pooling
- Spring Validation
- SpringDoc OpenAPI
- Spring Boot Actuator
- Centralized exception handling
- Unit and controller/integration testing

---

# 🏗️ System Architecture

```text
┌─────────────────────────────────────────────┐
│                  FRONTEND                   │
│             Next.js / React                 │
│                                             │
│       Public Website + Admin Dashboard      │
└──────────────────────┬──────────────────────┘
                       │
                       │ HTTPS / REST API
                       ▼
┌─────────────────────────────────────────────┐
│              SPRING BOOT API                │
│                                             │
│ Auth / Products / Inventory / Uploads       │
│ Inquiries / Orders / Blog / Gallery         │
│ Catalogue / Newsletter / Admin APIs         │
└──────────────────────┬──────────────────────┘
                       │
             ┌─────────┴──────────┐
             │                    │
             ▼                    ▼
┌────────────────────┐   ┌────────────────────┐
│   SERVICE LAYER    │   │ EXTERNAL SERVICES  │
│                    │   │                    │
│ Business Logic     │   │ Cloudinary         │
│ Validation         │   │ Email / SMTP       │
│ Transactions       │   │ WhatsApp           │
│ Data Transformation│   │ Razorpay           │
└─────────┬──────────┘   │ AWS                │
          │              └────────────────────┘
          ▼
┌────────────────────┐
│   REPOSITORY LAYER │
│   Spring Data JPA  │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│     POSTGRESQL     │
│      DATABASE      │
└────────────────────┘
```

---

# 📂 Project Structure

```text
tcc-backend/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
├── .gitignore
│
├── src/
│   │
│   ├── main/
│   │   │
│   │   ├── java/
│   │   │   └── com/thechoicecompany/tcc/
│   │   │       │
│   │   │       ├── config/
│   │   │       │   ├── AppProperties.java
│   │   │       │   ├── CloudinaryConfig.java
│   │   │       │   ├── CorsConfig.java
│   │   │       │   └── SecurityConfig.java
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── InquiryController.java
│   │   │       │   ├── ProductController.java
│   │   │       │   ├── BlogController.java
│   │   │       │   ├── GalleryController.java
│   │   │       │   ├── OrderController.java
│   │   │       │   ├── NewsletterController.java
│   │   │       │   └── UploadController.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── request/
│   │   │       │   └── response/
│   │   │       │       └── ProductImageResponse.java
│   │   │       │
│   │   │       ├── entity/
│   │   │       │   ├── User.java
│   │   │       │   ├── Inquiry.java
│   │   │       │   ├── InquiryNote.java
│   │   │       │   ├── Product.java
│   │   │       │   ├── ProductImage.java
│   │   │       │   ├── ProductPricingTier.java
│   │   │       │   ├── BlogPost.java
│   │   │       │   ├── GalleryItem.java
│   │   │       │   ├── DemoOrder.java
│   │   │       │   └── NewsletterSubscriber.java
│   │   │       │
│   │   │       ├── enums/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       ├── service/
│   │   │       │   ├── UploadService.java
│   │   │       │   └── ProductImageService.java
│   │   │       └── util/
│   │   │
│   │   └── resources/
│   │       │
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       │
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__create_tables.sql
│   │       │       ├── V2__seed_admin_user.sql
│   │       │       ├── V3__...
│   │       │       └── V4__add_product_images.sql
│   │       │
│   │       └── templates/
│   │           ├── inquiry-ack.html
│   │           └── order-confirmation.html
│   │
│   └── test/
│       │
│       ├── java/
│       │   └── com/thechoicecompany/tcc/
│       │       ├── InquiryServiceTest.java
│       │       ├── InquiryControllerTest.java
│       │       └── ApplicationTests.java
│       │
│       └── resources/
│           └── application-test.properties
│
└── .gitignore
```

> The exact source tree may contain additional classes. The actual source tree and `pom.xml` remain the source of truth.

---

# 🧰 Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Web | Spring Web / REST |
| Security | Spring Security |
| Authentication | JWT / JJWT 0.12.6 |
| Password Hashing | BCrypt |
| ORM | Hibernate |
| Persistence | Spring Data JPA |
| Database | PostgreSQL |
| Migration | Flyway 10.17.x |
| Connection Pool | HikariCP |
| Validation | Jakarta Bean Validation |
| API Documentation | SpringDoc OpenAPI 2.6.0 |
| Email | Spring Mail + Thymeleaf |
| Image Storage | Cloudinary |
| HTTP Client | OkHttp |
| Mapping | MapStruct |
| Cloud SDK | AWS SDK v2 |
| Testing | JUnit / Mockito / MockMvc / H2 |
| Build Tool | Maven 3.9.x |

> Dependency versions defined in `pom.xml` are the final source of truth.

---

# 🧱 Application Layers

## 1. Controller Layer

Controllers expose REST endpoints and are responsible for:

- Receiving HTTP requests
- Request validation
- Calling service methods
- Returning response DTOs
- Endpoint-level authorization

Controllers include:

```text
AuthController
InquiryController
ProductController
BlogController
GalleryController
OrderController
NewsletterController
UploadController
```

> Business logic should remain in services rather than controllers.

---

## 2. DTO Layer

DTOs control the data entering and leaving the API.

### Request DTOs

Validation commonly uses:

```java
@Valid
@NotBlank
@NotNull
@Pattern
@Size
```

### Response DTOs

Examples:

```text
ApiResponse
PagedResponse
AuthResponse
InquiryResponse
ProductResponse
ProductImageResponse
```

DTO separation prevents direct exposure of JPA entities through REST APIs.

---

## 3. Service Layer

The service layer contains application business logic.

| Service | Responsibility |
|---|---|
| `AuthService` | Authentication |
| `InquiryService` | Inquiry processing |
| `ProductService` | Product catalog and pricing |
| `ProductImageService` | Product image management |
| `OrderService` | Demo/sample order processing |
| `BlogService` | Blog management |
| `GalleryService` | Gallery management |
| `NewsletterService` | Newsletter subscriptions |
| `EmailService` | Transactional email |
| `WhatsAppService` | WhatsApp integration |
| `UploadService` | Cloudinary upload/delete operations |

Responsibilities include:

- Business rules
- Validation
- Transactions
- Repository interaction
- External service interaction
- Cloudinary integration
- Image rollback
- Data transformation

---

## 4. Repository Layer

Repositories use Spring Data JPA for:

- CRUD
- JPQL/custom queries
- Filtering
- Pagination
- Slug lookups
- Product queries
- Image queries
- Persistence

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

---

# 🔐 Authentication & Security

The backend uses:

- Spring Security
- JWT
- JJWT `0.12.6`
- BCrypt password hashing
- Stateless authentication
- Role-based authorization

Security components:

```text
JwtTokenProvider
JwtAuthFilter
UserDetailsServiceImpl
SecurityConfig
```

## Login Flow

```text
POST /api/auth/login
        │
        ▼
AuthController
        │
        ▼
AuthService
        │
        ├── Find User
        ├── Verify Password
        ├── Verify Account
        └── Generate JWT
        │
        ▼
AuthResponse
```

## Protected API Flow

```text
Frontend
   │
   │ Authorization: Bearer <JWT>
   ▼
JwtAuthFilter
   │
   ▼
Validate JWT
   │
   ▼
Load User
   │
   ▼
Spring Security Context
   │
   ▼
Protected Controller
```

---

# 👤 Admin Roles

The current role-aware authorization model includes:

```text
SUPER_ADMIN
CONTENT_MANAGER
```

Protected upload/admin operations require an appropriate role.

---

# 📦 Product & Inventory System

The product domain supports:

- Product catalog
- Product details
- Slug-based lookup
- Quantity-based pricing tiers
- Inventory support
- Multiple product images
- Primary image
- Image ordering

### Product entities

```text
Product
ProductImage
ProductPricingTier
```

### Example product relationship

```text
Product
  │
  ├── ProductPricingTier
  ├── ProductPricingTier
  │
  ├── ProductImage
  ├── ProductImage
  ├── ProductImage
  └── ProductImage
```

---

# 🖼️ Product Image Management

Products support **multiple images**.

Images are stored in Cloudinary, while PostgreSQL stores their metadata.

## ProductImage Metadata

```text
id
product_id
image_url
public_id
sort_order
is_primary
created_at
```

## Current Upload Rules

| Rule | Current Value |
|---|---|
| Maximum images per product | 8 |
| Maximum file size | 5 MB per image |
| JPEG | Supported |
| PNG | Supported |
| WEBP | Supported |
| GIF | Supported |

The backend validates files before uploading them to Cloudinary.

---

# ☁️ Cloudinary Integration

Cloudinary is used for product and gallery image storage.

```text
CloudinaryConfig
       ↓
UploadService
       ↓
ProductImageService
       ↓
ProductImage Entity
       ↓
PostgreSQL Metadata
```

Cloudinary responsibilities:

- Image storage
- Secure HTTPS URLs
- Image transformations
- Image resizing
- Automatic WebP conversion
- Public ID management
- Image deletion support

Configured transformation size is approximately:

```text
1200 × 1200
```

---

# 📤 Image Upload Architecture

Cloudinary credentials are never exposed to the browser.

```text
Admin Browser
     │
     │ Multipart File
     ▼
Next.js Route Handler
/api/admin/upload/image
     │
     │ JWT Bearer Token
     ▼
Spring Boot
/api/upload/image
     │
     ▼
UploadService
     │
     ▼
Cloudinary
     │
     ▼
secure_url + public_id
     │
     ▼
Next.js
     │
     ▼
Admin UI
```

---

# 📦 Product Image Upload Flow

When creating a product:

```text
1. Admin selects images
        ↓
2. Frontend validates files
        ↓
3. Maximum 8 images
        ↓
4. Maximum 5 MB/image
        ↓
5. Duplicate files rejected
        ↓
6. Images uploaded to Cloudinary
        ↓
7. Cloudinary returns URL + public ID
        ↓
8. Product is created
        ↓
9. ProductImage records are saved
        ↓
10. First/selected image becomes primary
```

---

# 🔄 Image Upload Rollback

Bulk image uploads include rollback protection.

Example:

```text
Image 1 → Success
Image 2 → Success
Image 3 → Success
Image 4 → FAILED
```

The service attempts to remove already-uploaded Cloudinary images:

```text
Upload Failure
      ↓
Rollback uploaded Cloudinary images
      ↓
Return error
```

This helps prevent orphaned Cloudinary files when a multi-image upload partially fails.

---

# 📐 Image Transformation URLs

`ProductImageResponse` provides frontend-ready image URLs.

| Response Field | Size | Usage |
|---|---:|---|
| `thumbnailUrl` | 150 × 150 | Small previews |
| `listingUrl` | 600 × 600 | Product listing |
| `detailUrl` | 1000 × 1000 | Product detail |

The frontend does not need to construct Cloudinary transformation URLs manually.

---

# 🗄️ Product Images Database

Migration:

```text
V4__add_product_images.sql
```

The table contains:

```sql
product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(1000) NOT NULL,
    public_id VARCHAR(500) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

A partial unique index ensures that each product can have only one primary image:

```sql
CREATE UNIQUE INDEX idx_product_images_primary
ON product_images (product_id)
WHERE is_primary = TRUE;
```

---

# 🧹 Existing Product Image Migration

Products that previously stored a single image in `products.image` can be migrated into the new `product_images` structure.

Previous model:

```text
Product
  └── image
```

Current model:

```text
Product
  ├── ProductImage
  ├── ProductImage
  ├── ProductImage
  └── ProductImage
```

This allows multiple images while maintaining compatibility with existing product data.

---

# 🐘 Database Architecture

PostgreSQL is the primary database.

Major tables include:

```text
users
inquiries
inquiry_notes
products
product_pricing_tiers
product_images
blog_posts
gallery_items
demo_orders
newsletter_subscribers
```

Persistence stack:

```text
Spring Data JPA
       +
Hibernate
       +
PostgreSQL
```

---

# 🔄 Flyway Migrations

Database schema changes are managed through:

```text
src/main/resources/db/migration/
```

Current documented migrations include:

```text
V1__create_tables.sql
V2__seed_admin_user.sql
V3__...
V4__add_product_images.sql
```

## V1 — Create Tables

Creates the foundational database structure including:

- Tables
- Primary keys
- Foreign keys
- Indexes
- JSONB columns where required
- Database constraints
- Required triggers

## V2 — Seed Admin

Creates the initial administrator account using a BCrypt password hash.

> Change the default admin password immediately after the first login.

## V4 — Product Images

Creates the `product_images` table and migrates existing product image data.

> The exact migration history is determined by the files present in `src/main/resources/db/migration/`.

---

# 🔌 REST API

The backend exposes public and protected REST APIs.

## Authentication

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/auth/login` | None | Admin login |

## Inquiries

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/inquiries` | Public | Submit inquiry |
| `GET` | `/api/inquiries` | JWT | List inquiries |

## Products

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/products` | Public | Product list |
| `GET` | `/api/products/{slug}` | Public | Product details |

Additional admin product endpoints depend on the current `ProductController` implementation.

## Product / Image Upload

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/upload/image` | JWT + Role | Upload single image |
| `POST` | `/api/upload/gallery` | JWT + Role | Upload gallery image |
| `POST` | `/api/upload/images/bulk` | JWT + Role | Upload multiple images |

Supported roles:

```text
CONTENT_MANAGER
SUPER_ADMIN
```

## Blog

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/blog` | Public | Get blog posts |

## Gallery

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/gallery` | Public | Get gallery items |

## Demo Orders

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/demo-orders` | Token | Save demo/sample order |

> Swagger/OpenAPI should be used as the authoritative source for the complete endpoint list because additional controller endpoints may exist.

---

# 📖 Swagger / OpenAPI

The project uses SpringDoc OpenAPI.

### Swagger UI

```text
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON

```text
http://localhost:8080/v3/api-docs
```

Swagger can be used to:

- View API documentation
- Inspect request/response models
- Test endpoints
- Test JWT-protected endpoints
- Verify validation rules

---

# 📤 Standard API Response

The backend uses response wrappers such as:

```text
ApiResponse
PagedResponse
```

Example:

```json
{
  "success": true,
  "message": "Request processed successfully",
  "data": {}
}
```

Example paginated response:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

> The exact JSON structure depends on the implementation of the response DTOs.

---

# ❌ Exception Handling

Centralized exception handling is provided through:

```text
GlobalExceptionHandler
```

Common custom exceptions:

```text
ResourceNotFoundException
BusinessException
DuplicateResourceException
```

## HTTP Status Codes

| Status | Meaning |
|---:|---|
| `400` | Bad Request |
| `401` | Unauthorized |
| `403` | Forbidden |
| `404` | Not Found |
| `409` | Conflict / Duplicate |
| `500` | Internal Server Error |

### Error flow

```text
Invalid request
      ↓
Validation Exception
```

```text
Missing database record
      ↓
ResourceNotFoundException
```

```text
Business rule failure
      ↓
BusinessException
```

```text
Duplicate resource
      ↓
DuplicateResourceException
```

---

# 📧 Email System

The backend supports HTML transactional emails using Thymeleaf.

```text
src/main/resources/templates/

├── inquiry-ack.html
└── order-confirmation.html
```

### Inquiry Acknowledgement

Sent after a customer submits an inquiry.

### Order Confirmation

Sent after a demo/sample order is successfully processed.

Email delivery uses:

```text
Spring Mail
    +
SMTP
    +
Thymeleaf
```

---

# 🔌 External Integrations

## ☁️ Cloudinary

Used for:

- Product image storage
- Gallery image storage
- Image transformations
- Secure image URLs
- Public ID management

## 📧 SMTP

Used for:

- Inquiry acknowledgement
- Order confirmation
- Transactional email notifications

## 💬 WhatsApp

The backend contains configuration support for WhatsApp integration.

## 💳 Razorpay

The project contains configuration support for Razorpay payment integration.

## ☁️ AWS

AWS SDK v2 support is configured for cloud-related functionality where required.

> Availability and activation of each external integration depend on the active configuration and implementation.

---

# ⚙️ Configuration & Profiles

The application uses Spring Boot properties and environment variables.

```text
src/main/resources/

├── application.properties
├── application-dev.properties
└── application-prod.properties
```

Test configuration:

```text
src/test/resources/
└── application-test.properties
```

| File | Purpose |
|---|---|
| `application.properties` | Common configuration |
| `application-dev.properties` | Local development |
| `application-prod.properties` | Production |
| `application-test.properties` | Automated tests |

Configuration covers:

- PostgreSQL
- HikariCP
- JPA/Hibernate
- Flyway
- Jackson
- SMTP
- JWT
- Razorpay
- WhatsApp
- AWS
- Cloudinary
- Logging
- Spring Profiles
- Actuator
- Swagger/OpenAPI
- Thymeleaf
- CORS

---

# 🌍 Environment Variables

Sensitive values must be externalized.

## Database

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

## JWT

```text
JWT_SECRET
JWT_EXPIRATION
```

## Mail

```text
MAIL_HOST
MAIL_PORT
MAIL_USERNAME
MAIL_PASSWORD
MAIL_FROM
```

## Razorpay

```text
RAZORPAY_KEY_ID
RAZORPAY_KEY_SECRET
```

## WhatsApp

```text
WHATSAPP_API_URL
WHATSAPP_ACCESS_TOKEN
WHATSAPP_PHONE_NUMBER_ID
```

## AWS

```text
AWS_REGION
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_S3_BUCKET
```

## Cloudinary

```text
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
```

## Frontend / CORS

```text
FRONTEND_URL
CORS_ALLOWED_ORIGINS
```

### Cloudinary properties

```properties
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

> `CLOUDINARY_API_SECRET` must never be exposed to frontend/browser code.

---

# 💻 Prerequisites

Install:

| Software | Version |
|---|---|
| Java | 21 LTS |
| Maven | 3.9.x |
| PostgreSQL | Supported production release |
| Git | Latest stable |
| IDE | IntelliJ IDEA / Eclipse / VS Code |

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

Verify PostgreSQL:

```bash
psql --version
```

---

# 🏁 Local Development Setup

## Step 1 — Clone Repository

```bash
git clone <repository-url>
cd <backend-directory>
```

---

## Step 2 — Create PostgreSQL Database

Using PostgreSQL CLI:

```bash
createdb tcc_db_dev
```

Or:

```sql
CREATE DATABASE tcc_db_dev;
```

---

## Step 3 — Configure Environment

Configure development values such as:

```text
DB_URL=jdbc:postgresql://localhost:5432/tcc_db_dev
DB_USERNAME=postgres
DB_PASSWORD=your_password

JWT_SECRET=your-development-secret

CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

Also configure SMTP, Razorpay, WhatsApp, AWS, and CORS variables if those integrations are enabled in the current environment.

> Never commit real credentials.

---

## Step 4 — Run Development Profile

### Windows PowerShell

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### Linux/macOS

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

# 🌐 Application URLs

Application:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

Health:

```text
http://localhost:8080/actuator/health
```

---

# ❤️ Health Monitoring

Spring Boot Actuator provides application health information.

### Health endpoint

```text
http://localhost:8080/actuator/health
```

Example:

```json
{
  "status": "UP"
}
```

The health endpoint can be used by:

- Deployment platforms
- Load balancers
- Monitoring systems
- Infrastructure health checks

---

# 🔨 Maven Commands

## Run Application

```bash
./mvnw spring-boot:run
```

## Run Development Profile

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Run Tests

```bash
./mvnw test
```

## Build Without Tests

```bash
./mvnw clean package -DskipTests
```

## Clean Build

```bash
./mvnw clean package
```

## Run JAR

```bash
java -jar target/tcc-backend-2.0.0.jar
```

### Windows

```powershell
.\mvnw.cmd test
```

```powershell
.\mvnw.cmd clean package
```

---

# 🧪 Testing

The backend includes unit and controller/application tests.

Documented tests include:

```text
InquiryServiceTest.java
InquiryControllerTest.java
ApplicationTests.java
```

## Unit Tests

`InquiryServiceTest` uses Mockito to test service/business logic independently.

## Controller Tests

`InquiryControllerTest` uses MockMvc to test:

- HTTP requests
- HTTP responses
- Validation
- Controller behavior

## Application Test

`ApplicationTests` verifies that the Spring application context starts successfully.

Run all tests:

```bash
./mvnw test
```

---

# 🔄 Request Processing Flow

Example inquiry request:

```text
Frontend
   │
   │ POST /api/inquiries
   ▼
InquiryController
   │
   │ @Valid Request DTO
   ▼
Validation
   │
   ▼
InquiryService
   │
   ├── Business validation
   ├── Generate reference
   ├── Save inquiry
   └── Send notification
   │
   ▼
InquiryRepository
   │
   ▼
PostgreSQL
```

---

# 🔢 Reference Number Generator

The backend includes:

```text
ReferenceGenerator
```

Reference format:

```text
TCC-2026-XXXXX
```

Example:

```text
TCC-2026-48321
```

This provides a human-readable reference number for inquiries/orders.

---

# 🔗 Slug Utility

The application includes:

```text
SlugUtils
```

Example:

```text
Premium Corporate Gift Box
```

becomes:

```text
premium-corporate-gift-box
```

This supports readable URLs such as:

```text
/api/products/premium-corporate-gift-box
```

---

# 🛠️ Development Guidelines

When adding a new feature, follow this sequence:

```text
1. Create / Update Entity
        ↓
2. Create Repository
        ↓
3. Create Request DTO
        ↓
4. Create Response DTO
        ↓
5. Implement Service
        ↓
6. Create Controller Endpoint
        ↓
7. Add Validation
        ↓
8. Add Exception Handling
        ↓
9. Add Flyway Migration
        ↓
10. Add Unit / Integration Tests
        ↓
11. Test API using Swagger / Postman
        ↓
12. Update README
```

## Important Architecture Rule

Do not put business logic directly inside controllers.

Preferred architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

---

# 🌿 Git Workflow

Recommended branch structure:

```text
main
│
├── feature/product-api
├── feature/product-images
├── feature/inquiry-management
├── feature/authentication
├── feature/order-management
├── feature/blog
└── fix/...
```

Recommended workflow:

```text
Create Branch
     ↓
Implement Feature
     ↓
Write Tests
     ↓
Run Tests
     ↓
Run Clean Build
     ↓
Code Review
     ↓
Pull Request
     ↓
Merge
```

---

# 🚫 Git Secret Management

Never commit:

```text
.env
.env.*

database passwords
JWT secrets
SMTP passwords
API tokens
AWS credentials
Razorpay secrets
WhatsApp tokens
Cloudinary API secret
production credentials
```

Recommended `.gitignore`:

```gitignore
target/
.idea/
.vscode/
*.iml

.env
.env.*

*.log
logs/
```

Production configuration may be version-controlled only when sensitive values are injected through environment variables or a secret manager.

---

# 🔐 Security Checklist

Before production deployment:

- [ ] Change default admin password
- [ ] Generate strong random JWT secret
- [ ] Never commit passwords
- [ ] Never commit API keys
- [ ] Never expose Cloudinary API secret
- [ ] Enable HTTPS
- [ ] Restrict CORS
- [ ] Use production PostgreSQL
- [ ] Use `ddl-auto=validate`
- [ ] Enable Flyway
- [ ] Configure SMTP securely
- [ ] Secure Razorpay credentials
- [ ] Secure WhatsApp credentials
- [ ] Secure AWS credentials
- [ ] Secure Cloudinary credentials
- [ ] Review Swagger exposure
- [ ] Review Actuator exposure
- [ ] Configure rate limiting
- [ ] Configure database backups
- [ ] Configure application monitoring
- [ ] Review authorization rules
- [ ] Review public endpoints
- [ ] Review file upload validation
- [ ] Review Cloudinary upload limits
- [ ] Review image cleanup strategy

---

# 🚀 Production Profile

Production configuration:

```text
application-prod.properties
```

Production should use:

```text
Flyway = enabled
JPA ddl-auto = validate
Production PostgreSQL
Production secrets
Restricted CORS
Reduced logging
HTTPS
```

Run the production JAR:

```bash
java -jar target/tcc-backend-2.0.0.jar \
  --spring.profiles.active=prod
```

Production secrets should be supplied through environment variables or a cloud secret manager.

---

# 📦 Production Deployment Checklist

## DATABASE

- [ ] Production PostgreSQL created
- [ ] Database user configured
- [ ] Database permissions reviewed
- [ ] Database backup configured
- [ ] Flyway migration history verified
- [ ] Product image migration verified

## APPLICATION

- [ ] Production JAR built
- [ ] Environment variables configured
- [ ] Production profile enabled
- [ ] Health endpoint working

## SECURITY

- [ ] JWT secret replaced
- [ ] Admin password changed
- [ ] CORS configured
- [ ] HTTPS enabled
- [ ] Authorization reviewed
- [ ] Rate limiting configured
- [ ] File upload restrictions verified

## DATABASE MIGRATION

- [ ] Flyway enabled
- [ ] `ddl-auto=validate`
- [ ] Migration scripts tested
- [ ] Product image migration verified

## CLOUDINARY

- [ ] Cloudinary account configured
- [ ] Cloud name configured
- [ ] API key configured
- [ ] API secret secured
- [ ] Upload folders reviewed
- [ ] Image size restrictions verified
- [ ] Allowed formats verified
- [ ] Image cleanup strategy configured

## INTEGRATIONS

- [ ] SMTP configured
- [ ] Email templates tested
- [ ] Razorpay configured if required
- [ ] WhatsApp configured if required
- [ ] AWS configured if required

## MONITORING

- [ ] Actuator configured
- [ ] Application logs available
- [ ] Error monitoring configured
- [ ] Database monitoring configured
- [ ] Cloudinary usage monitored

## API

- [ ] Swagger reviewed
- [ ] Public endpoints reviewed
- [ ] Protected endpoints tested
- [ ] Upload endpoints tested
- [ ] Postman/API tests completed

---

# 🏭 Production Readiness

The project follows a production-oriented backend architecture with:

- Layered architecture
- RESTful APIs
- DTO separation
- JWT authentication
- BCrypt password hashing
- Role-based security
- PostgreSQL
- Flyway migrations
- Product image management
- Cloudinary integration
- Request validation
- Centralized exception handling
- HikariCP connection pooling
- Environment-specific configuration
- Swagger/OpenAPI
- Actuator health monitoring
- Unit tests
- Controller/integration tests
- External service integration
- Image upload rollback handling

> A production-oriented architecture does not automatically guarantee production readiness.

Before going live, validate:

- Infrastructure
- Security
- Database
- Backups
- Monitoring
- Logging
- Secrets
- Rate limiting
- CORS
- Authentication
- Authorization
- File uploads
- Cloudinary storage
- Performance
- Scalability
- Disaster recovery

---

# 🔮 Future Enhancements

## Authentication

- Refresh tokens
- Password reset
- Email verification
- Multi-role administration
- Account lockout
- MFA
- Session/device management
- Audit logging

## Product System

- Advanced filtering
- Product search
- Product categories
- Advanced inventory management
- Product variants
- Bulk pricing
- Customization options
- Product image deletion
- Image replacement
- Image optimization

## Orders

- Complete order lifecycle
- Full payment processing
- Razorpay payment workflow
- Order tracking
- Invoice generation
- Customer order history

## Performance

- Redis caching
- Database query optimization
- Pagination improvements
- Async processing
- Background jobs

## Infrastructure

- Docker
- AWS deployment
- CI/CD
- Load balancing
- Centralized logging
- Monitoring
- Automated backups
- Secret manager integration

---

# 📊 Backend Capability Overview

```text
                    THE CHOICE COMPANY
                            │
                            ▼
                   ┌─────────────────┐
                   │ Spring Boot API │
                   └────────┬────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
 Authentication      Product System       Inquiry System
        │                   │                   │
        │                   ├── Pricing         ├── Inquiries
        │                   ├── Inventory       ├── Notes
        │                   ├── Images          └── Notifications
        │                   └── Cloudinary
        │
        └─────────────────────────────────────────────┐
                                                      │
        ┌───────────────────┬───────────────────┬─────┘
        ▼                   ▼                   ▼
   Content System       Order System       Newsletter
        │                   │                   │
        ├── Blog            └── Demo Orders     └── Subscribers
        └── Gallery
                            │
                            ▼
                    PostgreSQL Database
                            │
                            ▼
                  External Integrations
                 ┌──────────┼──────────┐
                 ▼          ▼          ▼
              Email      WhatsApp    Razorpay
                            │
                            ▼
                        Cloudinary
```

---

# 📝 Project Summary

The **The Choice Company Backend** is a Spring Boot REST API developed to support a corporate gifting platform.

The backend provides:

```text
Authentication
      +
Authorization
      +
Product Catalog
      +
Pricing Tiers
      +
Inventory Support
      +
Multiple Product Images
      +
Cloudinary Storage
      +
Customer Inquiries
      +
Inquiry Notes
      +
Demo Orders
      +
Blog
      +
Gallery
      +
Catalogue Requests
      +
Newsletter
      +
Email Notifications
      +
WhatsApp Integration
      +
Razorpay Integration
      +
PostgreSQL
      +
Flyway
      +
JWT Security
      +
Swagger / OpenAPI
      +
Testing
```

The architecture is designed to support future expansion into a complete corporate gifting platform with:

- Admin dashboard
- Customer management
- Bulk corporate orders
- Payment processing
- Order tracking
- Product customization
- Product variants
- Multiple product images
- Cloud image storage
- Notifications
- Redis caching
- Audit logging
- Monitoring
- CI/CD
- AWS/cloud deployment

---

# 📄 License

Proprietary software developed for:

**The Choice Company**

All rights reserved.

---

# 👨‍💻 Maintainer

**The Choice Company — Backend Team**

### Primary Technologies

- Java 21
- Spring Boot 3.3.2
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- JWT
- Flyway
- Cloudinary
- REST API
- Maven
- Swagger / OpenAPI
- Thymeleaf

---

# ⭐ Development Principle

> **Keep the backend secure, maintainable, testable, scalable, and easy for the frontend team to consume.**
