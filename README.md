# Flower Shop Backend API

Production-ready REST API for the Soulflow Flower Shop e-commerce platform. Built with **Java 21**, **Spring Boot 3.5**, **Spring Data JPA**, **Spring Security (JWT)**, **Redis**, and **SQL Server**.

## Architecture

This project follows a **Package-by-Feature** layout. Each domain module owns its entity, repository, service, controller, and DTOs.

```
com.souflow
├── FlowershopApplication.java       # Entry point
├── config/                          # Security, Redis, JWT properties
├── common/                          # Shared DTOs, exceptions, utilities
├── security/                        # JWT filter, UserDetails, JwtService
├── account/                         # Auth (login/register), accounts, roles
├── category/                        # Product categories
├── product/                         # Products & product images
├── cart/                            # Shopping carts & cart items
├── order/                           # Orders & order details
├── payment/                         # Payment records
├── discount/                        # Discounts & product-discount links
└── comment/                         # Product comments & replies
```

Each feature module follows **Controller → Service → Repository**:

| Layer      | Responsibility                                      |
|------------|-----------------------------------------------------|
| Controller | HTTP mapping, validation, standardized responses    |
| Service    | Business logic, transactions                        |
| Repository | Data access via Spring Data JPA                     |

## Tech Stack

| Component        | Technology                          |
|------------------|-------------------------------------|
| Runtime          | Java 21                             |
| Framework        | Spring Boot 3.5.14                  |
| ORM              | Spring Data JPA / Hibernate         |
| Database         | Microsoft SQL Server                |
| Cache            | Redis (Spring Cache)                |
| Security         | Spring Security + JWT (jjwt 0.12)   |
| API Docs         | Springdoc OpenAPI (Swagger UI)      |
| Env config       | `.env` via Spring `config.import`   |

## Prerequisites

- JDK 21
- Maven 3.9+ (or use `./mvnw`)
- SQL Server (local or remote)
- Redis Server
- Git

## Setup Instructions

### 1. Clone and configure environment

```bash
git clone <repository-url>
cd soulflow-api
cp .env.example .env
```

Edit `.env` with your credentials:

```env
SERVER_PORT=8080
DB_HOST=localhost
DB_PORT=1433
DB_NAME=flower_shop
DB_USERNAME=sa
DB_PASSWORD=your_password
JWT_SECRET=your-secure-secret-key-minimum-32-characters
JWT_EXPIRATION_MS=86400000
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
JPA_SHOW_SQL=false
```

### 2. Create the database

Run the provided SQL schema script against SQL Server to create the `flower_shop` database and all tables.

Then seed required roles:

```bash
# Run sql/seed-roles.sql in SSMS or sqlcmd
sqlcmd -S localhost -U sa -P your_password -i sql/seed-roles.sql
```

Required roles: `ADMIN`, `STAFF`, `CUSTOMER` (registration assigns `CUSTOMER` by default).

### 3. Start Redis

```bash
redis-server
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

API base URL: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

## Environment Variables

| Variable            | Description                     | Default        |
|---------------------|---------------------------------|----------------|
| `SERVER_PORT`       | HTTP port                       | `8080`         |
| `DB_HOST`           | SQL Server host                 | `localhost`    |
| `DB_PORT`           | SQL Server port                 | `1433`         |
| `DB_NAME`           | Database name                   | `flower_shop`  |
| `DB_USERNAME`       | Database username               | `sa`           |
| `DB_PASSWORD`       | Database password               | *(required)*   |
| `JWT_SECRET`        | JWT signing secret              | *(change me)*  |
| `JWT_EXPIRATION_MS` | Token lifetime in milliseconds  | `86400000`     |
| `REDIS_HOST`        | Redis host                      | `localhost`    |
| `REDIS_PORT`        | Redis port                      | `6379`         |
| `REDIS_PASSWORD`    | Redis password (optional)       | empty          |
| `JPA_SHOW_SQL`      | Log SQL statements              | `false`        |

## API Response Format

All endpoints return a standardized envelope:

**Success:**
```json
{
  "timestamp": "2026-06-15T10:30:00",
  "status": 200,
  "message": "Lay danh sach san pham thanh cong",
  "data": { }
}
```

**Error:**
```json
{
  "timestamp": "2026-06-15T10:30:00",
  "status": 404,
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "San pham khong ton tai"
}
```

## Core API Flows

### Authentication Flow

```
POST /api/auth/register  →  Create account (role: CUSTOMER)  →  Return JWT
POST /api/auth/login     →  Validate credentials               →  Return JWT
GET  /api/auth/me        →  Bearer token required              →  Account profile
```

Include the token in subsequent requests:

```
Authorization: Bearer <accessToken>
```

### Shopping Flow

```
1. GET  /api/products              Browse catalog (public)
2. GET  /api/categories            Browse categories (public)
3. POST /api/carts                  Create cart (authenticated)
4. POST /api/carts/{id}/items       Add product to cart
5. POST /api/orders                 Checkout cart → creates order, deducts stock
6. POST /api/payments               Record payment, sets order status to PAID
```

### Product Management Flow (Authenticated)

```
POST   /api/categories          Create category
POST   /api/products            Create product
PUT    /api/products/{id}       Update product
DELETE /api/products/{id}       Soft-delete product
```

### Comments Flow

```
GET  /api/comments/product/{productId}   List comments (public)
POST /api/comments                       Post comment (authenticated)
POST /api/comments/{id}/replies            Reply to comment (authenticated)
```

## Security & Roles

| Role       | Access                                              |
|------------|-----------------------------------------------------|
| `CUSTOMER` | Carts, orders, comments, profile                    |
| `STAFF`    | Customer access + `/api/staff/**` endpoints         |
| `ADMIN`    | Full access including `/api/admin/**` endpoints     |

Public (no token): `GET` products, categories, comments, discounts, auth endpoints.

## Database Entity Mapping

| Table               | Entity        | Key Relationships                          |
|---------------------|---------------|--------------------------------------------|
| `roles`             | `Role`        | PK: `code`                                 |
| `accounts`          | `Account`     | ManyToOne → Role                           |
| `categories`        | `Category`    | OneToMany → Product                        |
| `products`          | `Product`     | ManyToOne → Category, ManyToMany → Discount|
| `product_images`    | `ProductImage`| ManyToOne → Product                        |
| `carts`             | `Cart`        | ManyToOne → Account, OneToMany → CartItem  |
| `items`             | `CartItem`    | ManyToOne → Cart, Product                  |
| `orders`            | `Order`       | ManyToOne → Account, OneToMany → OrderDetail|
| `orders_details`    | `OrderDetail` | ManyToOne → Order, Product                 |
| `payments`          | `Payment`     | ManyToOne → Order                          |
| `discounts`         | `Discount`    | ManyToMany → Product                       |
| `comments`          | `Comment`     | ManyToOne → Product, Account               |
| `replies`           | `Reply`       | ManyToOne → Comment, Account               |

Soft deletes use the `del_if` column via `SoftDeletableEntity`.

## Caching

Redis caches:
- `categories` — full category list (30 min TTL)
- `products` — full product list (30 min TTL)

Cache is evicted on create/update/delete operations.

## Build & Test

```bash
./mvnw clean compile    # Compile + checkstyle + spotless
./mvnw test             # Run unit tests
./mvnw package          # Build JAR
```

## Project Conventions

- Entity primary keys map DB column `pk` → Java field `id`
- Business identifiers (`id` VARCHAR column) → Java field `businessId`
- Snake_case DB columns → camelCase Java fields via `@Column(name = "...")`
- Passwords hashed with BCrypt
- Business exceptions use `ErrorCode` enum for consistent error codes

## License

Internal project — Soulflow Flower Shop team.
