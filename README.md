# B2B InvoiceHub

A simple B2B invoicing and credit-management project with:
- a Java + MySQL backend API
- a React frontend dashboard
- role-based login for Admin and Client users

This project lets you manage clients, create invoices, record payments, and track outstanding amounts.

## Key Features

- Client management (add, update, search, delete)
- Invoice creation with line items and tax calculation
- Payment recording (Cash, Bank Transfer, Cheque, UPI)
- Automatic invoice status updates (`UNPAID`, `PARTIAL`, `PAID`, `CANCELLED`)
- Dashboard and reports (outstanding, monthly summary, high-risk clients)
- Role-based access:
  - **Admin**: full access to all data
  - **Client**: sees only their own company invoices/payments

## Tech Stack

- **Backend:** Java (JDBC, layered architecture: model/dao/service/api)
- **Database:** MySQL
- **Frontend:** React + TypeScript + Vite + TanStack Query + Tailwind/shadcn UI

## Project Structure

```text
.
├── B2BInvoiceSystem/              # Java backend + SQL
│   ├── src/
│   ├── sql/database.sql
│   └── lib/mysql-connector-j.jar
├── stellar-invoice-dash-main/     # React frontend
└── DEMO_CREDENTIALS.md             # Demo login details
```

## Demo Credentials

Use these to test quickly:

### Admin
- Password: `admin123`

### Client Accounts

| Company | User ID | Password |
|---|---|---|
| Sharma Enterprises Pvt Ltd | `sharma_ent` | `sharma123` |
| MegaTech Solutions | `megatech` | `mega123` |
| Global Traders Co. | `global_traders` | `global123` |
| Sunrise Distributors | `sunrise` | `sunrise123` |
| Horizon Retail Ltd | `horizon` | `horizon123` |
| Drewrk Labs pvt ltd | `drewrk` | `drewrk123` |

You can also register a new client from the login page, then approve from admin side.

## How to Run (Full Setup)

### 1) Prerequisites

- Java 17+
- MySQL running
- Node.js 18+ and npm

### 2) Setup database

From project root:

```bash
cd "B2BInvoiceSystem"
mysql -u root -p < sql/database.sql
```

Then update DB credentials in:

`B2BInvoiceSystem/src/db/DatabaseConnection.java`

### 3) Start Java backend (port 8081)

```bash
cd "B2BInvoiceSystem"
javac -cp "lib/mysql-connector-j.jar" -d out src/model/*.java src/dao/*.java src/db/*.java src/service/*.java src/api/*.java src/ui/*.java
java -cp "out:lib/mysql-connector-j.jar" api.ApiServer
```

### 4) Start frontend (port 3000)

Open a new terminal:

```bash
cd "stellar-invoice-dash-main"
npm install
npm run dev
```

### 5) Open app

- Frontend: [http://localhost:3000](http://localhost:3000)
- Backend API: [http://localhost:8081/api](http://localhost:8081/api)

## Quick Usage

1. Open the app and log in as **Admin** or **Client**.
2. As Admin:
   - create clients/invoices
   - record payments
   - view reports/dashboard
3. As Client:
   - view only your company data
   - manage your own invoices/payments

## Troubleshooting

- If `3000` is busy, change frontend port in `stellar-invoice-dash-main/vite.config.ts`
- If `8081` is busy, stop the old Java process and restart API
- Make sure MySQL is running before starting backend

