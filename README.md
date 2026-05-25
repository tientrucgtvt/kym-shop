# KYN AURA

Spring Boot + MySQL + Thymeleaf web app for stock, sales, returns, invoices, and reports.

## Requirements

- Java 11
- Maven
- MySQL 8+

## Database

The app can create the database automatically when the MySQL user has permission:

```properties
jdbc:mysql://localhost:3306/kin_shop?createDatabaseIfNotExist=true
```

Default credentials are:

- Username: `root`
- Password: empty

Override them with environment variables when needed:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-password"
$env:DB_URL="jdbc:mysql://localhost:3306/kin_shop?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
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
