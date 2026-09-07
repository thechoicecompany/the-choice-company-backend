# The Choice Company — Spring Boot Backend

> Production-oriented REST API backend for **The Choice Company (TCC)** corporate gifting platform.

**Spring Boot 3.3.2 · Java 21 · PostgreSQL · Spring Security · JWT · Flyway · REST API · Cloudinary**

---

## 📌 Table of Contents

- [Project Overview](#project-overview)
- [Core Capabilities](#core-capabilities)
- [System Architecture](#system-architecture)
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [Application Layers](#application-layers)
- [Authentication & Security](authentication--security)
- [Admin Roles](#admin-roles)
- [User Management](#user-management)
- [Product & Inventory System](product--inventory-system)
- [Sample Products](#sample-products)
- [Product Image Management](#product-image-management)
- [Cloudinary Integration](#cloudinary-integration)
- [Database Architecture](#database-architecture)
- [Flyway Migrations](#flyway-migrations)
- [REST API](#rest-api)
- [API Response Standards](api-response-standards)
- [Exception Handling](#exception-handling)
- [Email System](#email-system)
- [External Integrations](#external-integrations)
- [Configuration & Profiles](configuration--profiles)
- [Environment Variables](#environment-variables)
- [Prerequisites](#prerequisites)
- [Local Development Setup](#local-development-setup)
- [Swagger / OpenAPI](swagger--openapi)
- [Health Monitoring](health-monitoring)
- [Maven Commands](maven-commands)
- [Testing](testing)
- [Request Processing Flow](request-processing-flow)
- [Development Guidelines](#development-guidelines)
- [Git Workflow](git-workflow)
- [Security Checklist](#security-checklist)
- [Production Deployment Checklist](#production-deployment-checklist)
- [Future Enhancements](#future-enhancements)
- [Project Summary](#project-summary)
- [License](#license)

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

The API serves both the public website and the protected administration dashboard.

---

# 🚀 Core Capabilities

### 🔐 Authentication & Authorization

- Administrator authentication via JWT
- BCrypt password hashing (strength 10)
- Role-based method-level authorization (`@PreAuthorize`)
- Stateless session management
- JWT verification endpoint for Next.js middleware
- Four-role RBAC: `SUPER_ADMIN`, `SALES_MANAGER`, `SALES_EXECUTIVE`, `CONTENT_MANAGER`

### 👤 User Management

- `SUPER_ADMIN` can create admin users with any role
- List, activate/deactivate, and delete admin users
- Passwords hashed before storage — never returned in responses
- Dev-only bootstrap endpoint for first `SUPER_ADMIN` creation (`@Profile("dev")`)

### 📦 Product Management

- Product catalog with slug-based lookup
- Quantity-based pricing tiers
- Product inventory tracking
- Multiple Cloudinary images per product
- Primary image enforcement via partial unique index
- Admin product management APIs

### 🛍️ Sample Products (Shop)

- Separate sample product catalog for the storefront shop
- Independent image management (`sample_product_images`)
- Admin CRUD for sample products
- Public browsing endpoints

### 🖼️ Image Management

- Cloudinary-based image storage
- Single and bulk image upload
- Gallery image upload
- File validation before upload
- Duplicate image protection
- Cloudinary upload rollback on partial failure
- Frontend-ready transformation URLs (thumbnail, listing, detail)

### 📨 Customer & Content Management

- Customer inquiries with notes and status tracking
- Catalogue download requests
- Contact messages with status management
- Blog content management
- Gallery content management
- Newsletter subscriptions
- Demo/sample orders with full lifecycle management

### 📊 Dashboard

- Real-time inquiry statistics (today, week, month)
- Catalogue download trends
- Inventory health (low stock, out of stock)
- Demo order counts
- Top states and categories by inquiry volume
- Monthly catalogue trend chart data

### 🔌 External Integrations

- Cloudinary (image storage)
- SMTP / Thymeleaf (transactional email)
- WhatsApp
- Razorpay (payment processing)
- AWS SDK v2

### 🛠️ Backend Infrastructure

- PostgreSQL with HikariCP connection pooling
- Flyway database migrations (schema-only, `ddl-auto=none`)
- Spring Validation
- SpringDoc OpenAPI / Swagger
- Spring Boot Actuator
- Centralized exception handling
- Async configuration

---

# 🏗️ System Architecture

```text
┌─────────────────────────────────────────────┐
│                  FRONTEND                   │
│             Next.js 15 / React              │
│                                             │
│       Public Website + Admin Dashboard      │
└──────────────────────┬──────────────────────┘
                       │
                       │ HTTPS / REST API
                       ▼
┌─────────────────────────────────────────────┐
│              SPRING BOOT API                │
│                                             │
│ Auth / Users / Products / Sample Products   │
│ Inventory / Uploads / Inquiries / Orders    │
│ Blog / Gallery / Catalogue / Newsletter     │
│ Contact / Dashboard / Admin APIs            │
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
├── mvnw / mvnw.cmd
├── README.md
├── .gitignore
│
└── src/
    ├── main/
    │   ├── java/com/thechoicecompany/
    │   │   │
    │   │   ├── config/
    │   │   │   ├── AppProperties.java
    │   │   │   ├── AsyncConfig.java
    │   │   │   ├── CloudinaryConfig.java
    │   │   │   ├── CorsConfig.java
    │   │   │   ├── SecurityConfig.java
    │   │   │   └── WebConfig.java
    │   │   │
    │   │   ├── controller/
    │   │   │   ├── AdminBlogController.java
    │   │   │   ├── AdminDashboardController.java
    │   │   │   ├── AdminGalleryController.java
    │   │   │   ├── AdminOrderController.java
    │   │   │   ├── AdminProductController.java
    │   │   │   ├── AdminSampleProductController.java
    │   │   │   ├── AdminUserController.java
    │   │   │   ├── AuthController.java
    │   │   │   ├── BlogController.java
    │   │   │   ├── BootstrapController.java       ← dev profile only
    │   │   │   ├── CatalogueController.java
    │   │   │   ├── ContactController.java
    │   │   │   ├── GalleryController.java
    │   │   │   ├── InquiryController.java
    │   │   │   ├── NewsletterController.java
    │   │   │   ├── OrderController.java
    │   │   │   ├── OrderTrackingController.java
    │   │   │   ├── ProductController.java
    │   │   │   ├── SampleProductController.java
    │   │   │   └── UploadController.java
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── AddNoteRequest.java
    │   │   │   │   ├── BlogRequest.java
    │   │   │   │   ├── CatalogueRequestDto.java
    │   │   │   │   ├── ContactRequest.java
    │   │   │   │   ├── CreateProductRequest.java
    │   │   │   │   ├── CreateSampleProductRequest.java
    │   │   │   │   ├── CreateUserRequest.java
    │   │   │   │   ├── DemoOrderRequest.java
    │   │   │   │   ├── GalleryItemUpdateRequest.java
    │   │   │   │   ├── InquiryRequest.java
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── NewsletterRequest.java
    │   │   │   │   ├── UpdateContactStatusRequest.java
    │   │   │   │   ├── UpdateInquiryStatusRequest.java
    │   │   │   │   ├── UpdateInventoryRequest.java
    │   │   │   │   ├── UpdateOrderStatusRequest.java
    │   │   │   │   ├── UpdatePricingRequest.java
    │   │   │   │   ├── UpdateProductRequest.java
    │   │   │   │   └── UpdateSampleProductRequest.java
    │   │   │   │
    │   │   │   └── response/
    │   │   │       ├── ApiResponse.java
    │   │   │       ├── AuthResponse.java
    │   │   │       ├── CatalogueResponseDto.java
    │   │   │       ├── ContactResponse.java
    │   │   │       ├── DashboardStatsResponse.java
    │   │   │       ├── DemoOrderDetailDto.java
    │   │   │       ├── DemoOrderSummaryDto.java
    │   │   │       ├── GalleryItemDetailDto.java
    │   │   │       ├── GalleryItemDto.java
    │   │   │       ├── InquiryResponse.java
    │   │   │       ├── InventoryResponse.java
    │   │   │       ├── OrderTrackingDto.java
    │   │   │       ├── PagedResponse.java
    │   │   │       ├── ProductAdminResponse.java
    │   │   │       ├── ProductImageResponse.java
    │   │   │       ├── ProductResponse.java
    │   │   │       ├── SampleProductAdminResponse.java
    │   │   │       ├── SampleProductImageResponse.java
    │   │   │       └── SampleProductResponse.java
    │   │   │
    │   │   ├── entity/
    │   │   │   ├── BlogPost.java
    │   │   │   ├── CatalogueRequest.java
    │   │   │   ├── ContactMessage.java
    │   │   │   ├── DemoOrder.java
    │   │   │   ├── GalleryItem.java
    │   │   │   ├── Inquiry.java
    │   │   │   ├── InquiryNote.java
    │   │   │   ├── NewsletterSubscriber.java
    │   │   │   ├── Product.java
    │   │   │   ├── ProductImage.java
    │   │   │   ├── ProductInventory.java
    │   │   │   ├── ProductPricingTier.java
    │   │   │   ├── SampleProduct.java
    │   │   │   ├── SampleProductImage.java
    │   │   │   └── User.java
    │   │   │
    │   │   ├── enums/
    │   │   │   ├── ContactStatus.java
    │   │   │   ├── InquirySource.java
    │   │   │   ├── InquiryStatus.java
    │   │   │   ├── InventoryAction.java
    │   │   │   ├── OrderStatus.java
    │   │   │   └── UserRole.java
    │   │   │
    │   │   ├── exception/
    │   │   │   ├── BusinessException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   │
    │   │   ├── repository/
    │   │   │   ├── BlogRepository.java
    │   │   │   ├── CatalogueRequestRepository.java
    │   │   │   ├── ContactMessageRepository.java
    │   │   │   ├── GalleryRepository.java
    │   │   │   ├── InquiryRepository.java
    │   │   │   ├── InventoryRepository.java
    │   │   │   ├── NewsletterRepository.java
    │   │   │   ├── OrderRepository.java
    │   │   │   ├── ProductImageRepository.java
    │   │   │   ├── ProductRepository.java
    │   │   │   ├── SampleProductImageRepository.java
    │   │   │   ├── SampleProductRepository.java
    │   │   │   └── UserRepository.java
    │   │   │
    │   │   ├── security/
    │   │   │   ├── JwtAuthFilter.java
    │   │   │   ├── JwtTokenProvider.java
    │   │   │   └── UserDetailsServiceImpl.java
    │   │   │
    │   │   ├── service/
    │   │   │   ├── AdminGalleryService.java
    │   │   │   ├── AdminOrderService.java
    │   │   │   ├── AuthService.java
    │   │   │   ├── BlogService.java
    │   │   │   ├── ContactService.java
    │   │   │   ├── DashboardService.java
    │   │   │   ├── EmailService.java
    │   │   │   ├── GalleryService.java
    │   │   │   ├── InquiryService.java
    │   │   │   ├── InventoryService.java
    │   │   │   ├── NewsletterService.java
    │   │   │   ├── OrderService.java
    │   │   │   ├── ProductImageService.java
    │   │   │   ├── ProductService.java
    │   │   │   ├── SampleProductService.java
    │   │   │   ├── UploadService.java
    │   │   │   └── WhatsAppService.java
    │   │   │
    │   │   └── util/
    │   │       ├── ReferenceGenerator.java
    │   │       └── SlugUtils.java
    │   │
    │   └── resources/
    │       ├── application.properties
    │       ├── application-dev.properties
    │       ├── application-prod.properties
    │       ├── application-test.properties
    │       │
    │       ├── db/migration/
    │       │   ├── V1__create_tables.sql
    │       │   ├── V2__seed_admin_user.sql
    │       │   ├── V3__add_inventory_and_catalogue.sql
    │       │   ├── V4__add_product_images.sql
    │       │   ├── V6__create_sample_products.sql
    │       │   ├── V7__create_contact_messages.sql
    │       │   ├── V8__gallery_files_fields.sql
    │       │   ├── V9__gallery_cloudinary_fields.sql
    │       │   ├── V10__add_blog_featured_image_public_id.sql
    │       │   └── V11__baseline_no_op.sql
    │       │
    │       └── templates/email/
    │           ├── catalogue-ack.html
    │           ├── catalogue-internal-alert.html
    │           ├── contact-ack.html
    │           ├── contact-internal-alert.html
    │           ├── inquiry-ack.html
    │           ├── inquiry-internal-alert.html
    │           └── order-confirmation.html
    │
    └── test/
        ├── java/com/thechoicecompany/
        │   ├── InquiryServiceTest.java
        │   ├── InquiryControllerTest.java
        │   └── ApplicationTests.java
        └── resources/
            └── application-test.properties
```

---

# 🧰 Technology Stack
```
| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Web | Spring Web / REST |
| Security | Spring Security |
| Authentication | JWT / JJWT 0.12.6 |
| Password Hashing | BCrypt (strength 10) |
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

  ```
> Dependency versions defined in `pom.xml` are the final source of truth.
---


# 🧱 Application Layers

## 1. Controller Layer

Controllers expose REST endpoints and handle:

- HTTP request reception
- Request validation delegation
- Service method invocation
- Response DTO return
- Endpoint-level authorization

## 2. DTO Layer

DTOs control data entering and leaving the API. Request DTOs use Jakarta Bean Validation annotations. Response DTOs prevent direct JPA entity exposure.

## 3. Service Layer
```
| Service | Responsibility |
|---|---|
| `AuthService` | Authentication and JWT generation |
| `InquiryService` | Inquiry processing and notes |
| `ProductService` | Product catalog and pricing |
| `ProductImageService` | Product image management |
| `SampleProductService` | Sample shop product management |
| `AdminOrderService` | Demo order admin operations |
| `OrderService` | Demo order creation and payment |
| `BlogService` | Blog content management |
| `GalleryService` | Public gallery |
| `AdminGalleryService` | Gallery admin operations |
| `ContactService` | Contact message management |
| `DashboardService` | Aggregated stats for admin dashboard |
| `InventoryService` | Stock management |
| `NewsletterService` | Newsletter subscriptions |
| `EmailService` | Transactional email via Thymeleaf |
| `WhatsAppService` | WhatsApp notifications |
| `UploadService` | Cloudinary upload and delete |
```
## 4. Repository Layer

Spring Data JPA repositories handle CRUD, pagination, custom JPQL queries, slug lookups, and filtered searches.

---

# 🔐 Authentication & Security

Security components:

```text
JwtTokenProvider       — token generation and validation
JwtAuthFilter          — per-request token extraction
UserDetailsServiceImpl — loads user from DB for Spring Security
SecurityConfig         — filter chain and endpoint authorization
```

## Login Flow

```text
POST /api/auth/login
        ↓
AuthController → AuthService
        ↓
Find user → Verify password → Update lastLogin → Generate JWT
        ↓
AuthResponse { token, expiresIn, user }
```

## JWT Verify (used by Next.js middleware)

```text
GET /api/auth/verify
Authorization: Bearer <token>
        ↓
JwtTokenProvider.validateToken()
        ↓
{ "valid": true } or 401
```

## Protected API Flow

```text
Request with Authorization: Bearer <JWT>
        ↓
JwtAuthFilter → validate → load user → set SecurityContext
        ↓
@PreAuthorize check
        ↓
Controller method
```

---

# 👤 Admin Roles
```
The system uses four roles enforced at method level via `@PreAuthorize`:

| Role | Access Level |
|---|---|
| `SUPER_ADMIN` | Full access — all endpoints including user management |
| `SALES_MANAGER` | Inquiries, orders, dashboard, products |
| `SALES_EXECUTIVE` | Inquiries, orders, dashboard (view only) |
| `CONTENT_MANAGER` | Blog, gallery, products |
```
---

# 👥 User Management
```
Admin users are managed exclusively by `SUPER_ADMIN` via `AdminUserController`.
```
```
| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/admin/users` | List all admin users |
| `POST` | `/api/admin/users` | Create new admin user |
| `PATCH` | `/api/admin/users/{id}/toggle-active` | Activate or deactivate user |
| `DELETE` | `/api/admin/users/{id}` | Permanently delete user |
```
Password is always BCrypt-hashed before storage and never returned in any response.

### First Admin Bootstrap (dev only)

The `BootstrapController` is annotated `@Profile("dev")` — it does not exist in production. Use it once to create the first `SUPER_ADMIN` when the `users` table is empty:



POST /api/dev/bootstrap/admin

After the first user is created, all subsequent users are created through the admin panel User Management page.

---

# 📦 Product & Inventory System

### Product entities

```text
Product → ProductPricingTier (one-to-many)
Product → ProductImage (one-to-many)
Product → ProductInventory (one-to-one)
```

### Inventory tracking

```text
stock_qty       — total units on hand
reserved_qty    — units held for pending orders
available_qty   — stock_qty - reserved_qty (computed)
reorder_level   — triggers low-stock alert
```

---

# 🛍️ Sample Products

A separate product catalog for the storefront sample shop:

```text
SampleProduct → SampleProductImage (one-to-many)
```

Sample products have independent pricing (`sample_price`, `bulk_price`), shipping days, and max sample quantity per order. They are managed through `AdminSampleProductController` and browsed publicly via `SampleProductController`.

---

# 🖼️ Product Image Management

Images are stored in Cloudinary; PostgreSQL stores metadata only.

## Upload Rules
```
| Rule | Value |
|---|---|
| Max images per product | 8 |
| Max file size | 5 MB |
| Formats | JPEG, PNG, WEBP, GIF |
```
## Transformation URLs (returned in response)
```
| Field | Size | Usage |
|---|---|---|
| `thumbnailUrl` | 150 × 150 | Card previews |
| `listingUrl` | 600 × 600 | Product listing |
| `detailUrl` | 1000 × 1000 | Product detail page |
```
## Upload Rollback

If a bulk upload partially fails, already-uploaded Cloudinary images are deleted to prevent orphaned files.

---

# ☁️ Cloudinary Integration

```text
CloudinaryConfig → UploadService → ProductImageService / AdminGalleryService
```

Responsibilities: secure HTTPS URLs, image transformations, public ID management, deletion support.

> `CLOUDINARY_API_SECRET` must never be exposed to the browser or committed to version control.

---

# 🗄️ Database Architecture

## Tables

```text
users
inquiries
inquiry_notes
products
product_pricing_tiers
product_images
product_inventory
sample_products
sample_product_images
blog_posts
gallery_items
catalogue_requests
contact_messages
demo_orders
newsletter_subscribers
```

## Schema Management

Flyway owns the schema exclusively. Hibernate is set to `ddl-auto=none` in all profiles — it never alters the database.

```text
Spring Data JPA + Hibernate + PostgreSQL
Schema controlled by: Flyway only
ddl-auto: none (all profiles)
```

---

# 🔄 Flyway Migrations

```text
src/main/resources/db/migration/
```
```
| File | Description |
|---|---|
| `V1__create_tables.sql` | All core tables, indexes, triggers |
| `V2__seed_admin_user.sql` | Default SUPER_ADMIN seed (change password immediately) |
| `V3__add_inventory_and_catalogue.sql` | `product_inventory` and `catalogue_requests` tables |
| `V4__add_product_images.sql` | `product_images` table and image migration |
| `V6__create_sample_products.sql` | `sample_products` and `sample_product_images` tables |
| `V7__create_contact_messages.sql` | `contact_messages` table |
| `V8__gallery_files_fields.sql` | Gallery file type columns |
| `V9__gallery_cloudinary_fields.sql` | Cloudinary columns, removes legacy local-disk columns |
| `V10__add_blog_featured_image_public_id.sql` | `featured_image_public_id` on `blog_posts` |
| `V11__baseline_no_op.sql` | Baseline marker — confirms `ddl-auto=none` takeover |
```
> V5 was intentionally removed (duplicate). `spring.flyway.out-of-order=true` is set to handle the gap.

### Key Flyway properties

```properties
spring.flyway.enabled=true
spring.flyway.out-of-order=true
spring.flyway.baseline-on-migrate=true
spring.jpa.hibernate.ddl-auto=none
```

---

# 🔌 REST API

## Authentication
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/auth/login` | Public | Admin login |
| `GET` | `/api/auth/verify` | Bearer | Verify JWT (middleware) |
```
## User Management (SUPER_ADMIN only)
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/admin/users` | JWT | List admin users |
| `POST` | `/api/admin/users` | JWT | Create admin user |
| `PATCH` | `/api/admin/users/{id}/toggle-active` | JWT | Toggle active status |
| `DELETE` | `/api/admin/users/{id}` | JWT | Delete admin user |
```
## Products (Public)
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/products` | Public | Product list |
| `GET` | `/api/products/{slug}` | Public | Product by slug |
```
## Products (Admin)
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/admin/products` | JWT | Paginated product list |
| `POST` | `/api/admin/products` | JWT | Create product |
| `PATCH` | `/api/admin/products/{id}` | JWT | Update product |
| `DELETE` | `/api/admin/products/{id}` | JWT | Soft delete |
| `DELETE` | `/api/admin/products/{id}/permanent` | JWT | Hard delete |
| `PUT` | `/api/admin/products/{id}/pricing` | JWT | Update pricing tiers |
```
## Sample Products (Public)
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/sample-products` | Public | Sample product list |
| `GET` | `/api/sample-products/{slug}` | Public | Sample product by slug |
```
## Sample Products (Admin)
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/admin/sample-products` | JWT | List sample products |
| `POST` | `/api/admin/sample-products` | JWT | Create sample product |
| `PATCH` | `/api/admin/sample-products/{id}` | JWT | Update sample product |
| `DELETE` | `/api/admin/sample-products/{id}` | JWT | Soft delete |
| `DELETE` | `/api/admin/sample-products/{id}/permanent` | JWT | Hard delete |
```
## Inventory (Admin)
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/admin/products/inventory` | JWT | All inventory |
| `PATCH` | `/api/admin/products/{id}/inventory` | JWT | Update stock |
| `GET` | `/api/admin/products/inventory/low-stock` | JWT | Low stock items |
| `GET` | `/api/admin/products/inventory/out-of-stock` | JWT | Out of stock items |
```
## Demo Orders
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/demo-orders` | Public | Submit demo order |
| `GET` | `/api/orders/track` | Public | Track order by ID |
| `GET` | `/api/admin/demo-orders` | JWT | List orders (paginated) |
| `GET` | `/api/admin/demo-orders/{orderId}` | JWT | Order detail |
| `PATCH` | `/api/admin/demo-orders/{orderId}/status` | JWT | Update order status |
```
## Inquiries
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/inquiries` | Public | Submit inquiry |
| `GET` | `/api/inquiries` | JWT | List inquiries |
| `PATCH` | `/api/inquiries/{id}/status` | JWT | Update status |
| `POST` | `/api/inquiries/{id}/notes` | JWT | Add note |
```
## Contact Messages
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/contact` | Public | Submit contact message |
| `GET` | `/api/admin/contact` | JWT | List messages |
| `PATCH` | `/api/admin/contact/{id}/status` | JWT | Update status |
```
## Catalogue
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/catalogue/request` | Public | Request catalogue |
| `GET` | `/api/catalogue/admin/requests` | JWT | List requests |
```
## Blog
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/blog` | Public | Published posts |
| `GET` | `/api/blog/{slug}` | Public | Post by slug |
| `POST` | `/api/admin/blog` | JWT | Create post |
| `PATCH` | `/api/admin/blog/{id}` | JWT | Update post |
| `DELETE` | `/api/admin/blog/{id}` | JWT | Delete post |
```
## Gallery
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/gallery` | Public | Gallery items |
| `POST` | `/api/admin/gallery` | JWT | Add gallery item |
| `DELETE` | `/api/admin/gallery/{id}` | JWT | Delete item |
```
## Image Upload
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/upload/image` | JWT | Upload single image |
| `POST` | `/api/upload/images/bulk` | JWT | Bulk upload |
| `POST` | `/api/upload/gallery` | JWT | Upload gallery item |
```
## Dashboard
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/admin/dashboard/stats` | JWT | Full dashboard stats |
```
## Newsletter
```
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/newsletter` | Public | Subscribe |
```
> Use Swagger/OpenAPI as the authoritative endpoint reference.

---

# 📤 Standard API Response

```json
{
  "success": true,
  "message": "Request processed successfully",
  "data": {}
}
```

Paginated response:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

---

# ❌ Exception Handling
```
Centralized via `GlobalExceptionHandler`.

| Exception | HTTP Status |
|---|---|
| Validation failure | 400 |
| Unauthorized | 401 |
| Forbidden | 403 |
| `ResourceNotFoundException` | 404 |
| `DuplicateResourceException` | 409 |
| `BusinessException` | 400 |
| Unhandled | 500 |
```
---

# 📧 Email System

HTML transactional emails via Thymeleaf templates:

```text
templates/email/
├── catalogue-ack.html            — catalogue download confirmation
├── catalogue-internal-alert.html — internal team notification
├── contact-ack.html              — contact form confirmation
├── contact-internal-alert.html   — internal team notification
├── inquiry-ack.html              — inquiry submission confirmation
├── inquiry-internal-alert.html   — internal team notification
└── order-confirmation.html       — demo order confirmation
```

---

# ⚙️ Configuration & Profiles
```
| File | Purpose |
|---|---|
| `application.properties` | Common configuration, `ddl-auto=none`, Flyway settings |
| `application-dev.properties` | Local dev overrides |
| `application-prod.properties` | Production — strict settings, reduced logging |
| `application-test.properties` | H2 in-memory for tests |
```
### Critical shared settings

```properties
spring.jpa.hibernate.ddl-auto=none
spring.flyway.enabled=true
spring.flyway.out-of-order=true
spring.flyway.baseline-on-migrate=true
```

---

# 🌍 Environment Variables

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

> Never commit real credentials. Use environment variables or a cloud secret manager in production.

---

# 💻 Prerequisites
```
| Software | Version |
|---|---|
| Java | 21 LTS |
| Maven | 3.9.x |
| PostgreSQL | 14+ |
| Git | Latest stable |
```
---

# 🏁 Local Development Setup

## Step 1 — Clone

```bash
git clone <repository-url>
cd tcc-backend
```

## Step 2 — Create Database

```sql
CREATE DATABASE tcc_db;
```

## Step 3 — Configure Environment

Copy `.env.example` to `.env` and fill in values. Never commit `.env`.

## Step 4 — Run

```powershell
# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

```bash
# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Flyway runs migrations automatically on startup. The default admin credentials are seeded by V2.

> **Change the default admin password immediately after first login.**

---

# 🌐 Application URLs
```
| Resource | URL |
|---|---|
| API | `http://localhost:8089` |
| Swagger UI | `http://localhost:8089/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8089/api-docs` |
| Health | `http://localhost:8089/actuator/health` |
```
---

# 🔨 Maven Commands

```bash
# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Build JAR (skip tests)
./mvnw clean package -DskipTests

# Run production JAR
java -jar target/tcc-backend-*.jar --spring.profiles.active=prod
```

---

# 🛠️ Development Guidelines

When adding a new feature:

```text
1. Flyway migration (if schema changes)
2. Entity
3. Repository
4. Request DTO
5. Response DTO
6. Service
7. Controller
8. SecurityConfig (if new public endpoint)
9. Tests
10. Update README
```

**Never put business logic in controllers. Never edit a Flyway migration after it has been applied.**

---

# 🔐 Security Checklist

Before production deployment:

- [ ] Change default admin password (V2 seed)
- [ ] Generate strong random JWT secret (min 256-bit)
- [ ] Set `ddl-auto=none` in all profiles
- [ ] Verify Flyway migration sequence — no gaps, no duplicates
- [ ] Never commit `.env` or credentials
- [ ] Never expose `CLOUDINARY_API_SECRET` to browser
- [ ] Enable HTTPS
- [ ] Restrict CORS to production domain
- [ ] Review all `permitAll()` endpoints in `SecurityConfig`
- [ ] Disable or restrict Swagger in production
- [ ] Restrict Actuator endpoints in production
- [ ] Configure rate limiting
- [ ] Configure database backups
- [ ] Remove or restrict `BootstrapController` (already `@Profile("dev")`)
- [ ] Review `@PreAuthorize` on all admin endpoints

---

# 📦 Production Deployment Checklist

## Database
- [ ] Production PostgreSQL provisioned
- [ ] Database user with least-privilege permissions
- [ ] Flyway migration history verified on fresh DB
- [ ] Backups configured

## Application
- [ ] JAR built with `./mvnw clean package -DskipTests`
- [ ] All environment variables set
- [ ] `spring.profiles.active=prod`
- [ ] Health endpoint responding

## Security
- [ ] JWT secret rotated
- [ ] Admin password changed
- [ ] CORS locked to production domain
- [ ] HTTPS enforced
- [ ] `ddl-auto=none` confirmed

## Integrations
- [ ] SMTP tested
- [ ] Cloudinary credentials set
- [ ] Razorpay credentials set (if live)
- [ ] WhatsApp credentials set (if live)

## Monitoring
- [ ] Actuator health configured
- [ ] Application logs streaming
- [ ] Error alerting configured

---

# 🔮 Future Enhancements

- Refresh tokens and token revocation
- Password reset via email
- Account lockout after failed attempts
- MFA
- Audit logging
- Redis caching for dashboard stats
- Full Razorpay payment lifecycle
- Invoice generation
- Docker and CI/CD pipeline
- AWS/cloud deployment automation
- Rate limiting middleware

---

# 📝 Project Summary

```text
Authentication & RBAC (4 roles)
User Management (SUPER_ADMIN)
Product Catalog + Pricing Tiers
Sample Product Shop
Product Inventory
Multiple Cloudinary Images per Product
Customer Inquiries + Notes
Demo Orders + Status Tracking
Order Tracking (public)
Contact Messages
Catalogue Download Requests
Blog
Gallery (Cloudinary)
Newsletter
Admin Dashboard Stats
Transactional Email (7 templates)
WhatsApp Integration
Razorpay Integration
PostgreSQL + Flyway (ddl-auto=none)
JWT Security
Swagger / OpenAPI
Spring Boot Actuator
```

---

# 📄 License

Proprietary software developed for **The Choice Company**.
All rights reserved.

---

# ⭐ Development Principle

> **Keep the backend secure, maintainable, testable, scalable, and easy for the frontend team to consume.**