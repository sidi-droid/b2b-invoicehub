# B2B Invoice & Credit Management System
### Java Console Application — Introduction to Algorithms

**Team:**
| Name | App ID |
|------|--------|
| Gunn Mulchandani | 2410731 |
| Durva Chhabria | 2404713 |
| Siddhant Amin | 2404447 |

---

## Project Structure

```
B2BInvoiceSystem/
├── src/
│   ├── model/
│   │   ├── Entity.java          ← Abstract base class
│   │   ├── Client.java          ← Extends Entity
│   │   ├── CreditClient.java    ← Extends Client
│   │   ├── Invoice.java         ← Extends Entity
│   │   ├── InvoiceItem.java     ← Line item model
│   │   └── Payment.java         ← Extends Entity
│   ├── dao/
│   │   ├── DAOInterface.java    ← Generic CRUD interface
│   │   ├── ClientDAO.java       ← Implements DAOInterface<Client>
│   │   ├── InvoiceDAO.java      ← Implements DAOInterface<Invoice>
│   │   └── PaymentDAO.java      ← Implements DAOInterface<Payment>
│   ├── service/
│   │   └── InvoiceService.java  ← Business logic layer
│   ├── db/
│   │   └── DatabaseConnection.java ← Singleton JDBC connection
│   └── ui/
│       └── Main.java            ← Console UI (entry point)
└── sql/
    └── database.sql             ← Schema + sample data
```

---

## Setup Instructions

### Step 1 — Set up the Database
```bash
mysql -u root -p < sql/database.sql
```

### Step 2 — Update DB credentials
Open `src/db/DatabaseConnection.java` and update:
```java
private static final String PASSWORD = "your_mysql_password";
```

### Step 3 — Download MySQL Connector
Download `mysql-connector-j-8.x.jar` from:
https://dev.mysql.com/downloads/connector/j/

Place it in a `lib/` folder in the project root.

### Step 4 — Compile
```bash
javac -cp "lib/mysql-connector-j-8.x.jar" -d out \
  src/model/*.java src/dao/*.java src/db/*.java \
  src/service/*.java src/ui/*.java
```

### Step 5 — Run
```bash
java -cp "out:lib/mysql-connector-j-8.x.jar" ui.Main
```

> On Windows, replace `:` with `;` in the classpath.

---

## OOP Concepts Demonstrated

| Concept | Where |
|---------|-------|
| Abstract Class | `Entity.java` |
| Inheritance | `Client → Entity`, `CreditClient → Client`, `Invoice → Entity`, `Payment → Entity` |
| Polymorphism | `displayInfo()` overridden in all subclasses |
| Encapsulation | All model fields private with getters/setters |
| Interface | `DAOInterface<T>` implemented by all DAOs |
| Method Overloading | `createInvoice()`, `generateInvoiceNumber()`, `displaySummary()` |
| Collections | `ArrayList<InvoiceItem>`, `HashMap<String, Double>` |

---

## Features
- ✔ Add / View / Update / Delete / Search Clients
- ✔ Create Invoices with line items and auto GST calculation
- ✔ Credit limit enforcement before invoice creation
- ✔ Record payments (Cash / Bank / Cheque / UPI)
- ✔ Auto-update invoice status (UNPAID → PARTIAL → PAID)
- ✔ Reports: Outstanding dues, high-risk clients, unpaid invoices
