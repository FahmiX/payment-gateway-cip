# Payment Gateway — CIP

A Spring Boot-based payment gateway service that orchestrates transactions between a Core Banking system and a Biller system. It provides RESTful APIs for clients to initiate payments, handles authentication via JWT, and ensures data integrity with PostgreSQL.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 3.5.x |
| Security | Spring Security + JWT |
| Database | PostgreSQL 18 |
| Migrations | Flyway |
| HTTP Client | Spring Cloud OpenFeign |
| API Docs | SpringDoc OpenAPI (Swagger UI) & Postman |
| Build Tool | Maven |

---

## Prerequisites

- Java 25+
- Maven 3.9.16
- PostgreSQL 18

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/FahmiX/payment-gateway-cip.git
cd payment-gateway-cip
```

### 2. Configure environment variables

Copy the example environment file and fill in your values:

```bash
cp .env.example .env
```

`.env` fields:

```env
DB_NAME=payment_gateway_cip
DB_USERNAME=postgres
DB_PASSWORD=your_password
DB_URL=jdbc:postgresql://localhost:5432/payment_gateway_cip
SECRET_KEY=your_jwt_secret_key_minimum_32_chars
```
### 3. Run the application

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Or build and run the JAR:

```bash
java -jar target/payment-gateway-0.0.1-SNAPSHOT.jar
```

This starts:
- **PostgreSQL 18** on port `5432`
- **Payment Gateway API** on port `8080`
- **Core Banking API** on port `8080`
- **Biller API** on port `8080`
(CoreBank and Biller are mocked within the same application for simulation purposes, you can change their ports in `application.yml` if needed)

---

### 4. Run database migrations

Flyway migrations run automatically on application startup. The initial migration creates the `transactions` table.


## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON spec:

```
http://localhost:8080/v3/api-docs
```

---

## API Endpoints

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/auth/token` | Public | Generate a JWT token |

### Payments

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/payments` | Bearer JWT | Create a new payment |
| `GET` | `/api/payments/{id}` | Bearer JWT | Get payment by transaction ID |

### CoreBank (Feign Proxy)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/corebank/debit` | Bearer JWT | Debit an account (mocked) |

### Biller (Feign Proxy)
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/biller/pay` | Bearer JWT | Pay a bill (mocked) |

### Other

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/actuator/health` | Public | Health check |
| `GET` | `/actuator/info` | Public | Application info |

---

## Usage Example

**Step 1 — Get a JWT token:**

```bash
curl -X GET http://localhost:8080/api/auth/token
```

**Step 2 — Create a payment:**

```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Authorization: Bearer <your_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "INV-12345",
    "channel": "MOBILE_BANKING",
    "amount": 250000,
    "currency": "IDR",
    "paymentMethod": "VIRTUAL_ACCOUNT"
  }'
```

---

## Payment Flow

```
Client → Payment Gateway
           │
           ├── 1. Create and check for new payment
           ├── 2. Save as PENDING transaction
           ├── 3. Call CoreBank (debit)
           │       └── BALANCE INSUFFICIENT → mark FAILED, return error
           │       └── BALANCE SUFFICIENT → continue
           ├── 4. Call Biller (pay)
           │       └── FAILED → mark FAILED, return error
           │       └── SUCCESS → continue
           └── 5. Mark SUCCESS, return response
```

---

## Project Structure

```
src/main/java/com/cip/payment_gateway/
├── client/          # Feign clients (CoreBank, Biller)
├── config/          # Security, JWT, OpenAPI, Feign config
├── controller/      # REST controllers (Auth, Payment, Feign proxies)
├── dto/
│   ├── request/     # PaymentRequest, BillerRequest, CoreBankRequest
│   └── response/    # PaymentResponse, BillerResponse, CoreBankResponse
├── enums/           # TransactionChannel, TransactionStatus
├── exception/       # GlobalExceptionHandler, DuplicateOrderException
├── model/           # Transactions entity
├── repository/      # TransactionRepository (JPA)
└── service/
    └── impl/        # PaymentServiceImpl
```

## Running Tests

```bash
./mvnw test or ./mvnw validate
```

---

## License

See [LICENSE](LICENSE) for details.