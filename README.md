# KYN AURA

Spring Boot + PostgreSQL + Thymeleaf web app for stock, sales, returns, invoices, and reports.

## Requirements

- Java 11
- Maven
- PostgreSQL 12+

## Database

Create a PostgreSQL database, for example:

```properties
kin_shop
```

Default credentials are:

- Username: `postgres`
- Password: `postgres`

Override them with environment variables when needed:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="kin_shop"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-password"
```

## Run

```powershell
mvn spring-boot:run
```

Open `http://localhost:8080`.

## Features

- Product and price management: SKU, name, import price, sale price, minimum stock, active status.
- Stock management: import stock, minus/export stock, and full stock movement history.
- Sale management: create order, apply discount, generate invoice, print invoice.
- Returns: refund/return sold items and restore returned quantity to stock.
- Reports: date-filtered sale report, refunds, net settlement, and estimated gross profit.
