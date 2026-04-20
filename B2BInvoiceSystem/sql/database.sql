-- B2B Invoice & Credit Management System
-- Database Schema

CREATE DATABASE IF NOT EXISTS b2b_invoice_db;
USE b2b_invoice_db;

-- clients table
CREATE TABLE IF NOT EXISTS clients (
    client_id       INT             NOT NULL AUTO_INCREMENT,
    company_name    VARCHAR(100)    NOT NULL,
    contact_person  VARCHAR(80)     NOT NULL,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    phone           VARCHAR(15)     NOT NULL,
    credit_limit    DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    current_balance DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (client_id),
    CONSTRAINT chk_credit_limit    CHECK (credit_limit >= 0),
    CONSTRAINT chk_current_balance CHECK (current_balance >= 0)
);

-- invoices table
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id      INT             NOT NULL AUTO_INCREMENT,
    client_id       INT             NOT NULL,
    invoice_number  VARCHAR(30)     NOT NULL UNIQUE,
    subtotal        DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    tax_rate        DECIMAL(5, 4)   NOT NULL DEFAULT 0.18,
    tax_amount      DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    amount_paid     DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    balance_due     DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    status          ENUM('UNPAID', 'PAID', 'PARTIAL', 'CANCELLED') DEFAULT 'UNPAID',
    due_date        VARCHAR(20),
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (invoice_id),
    FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE RESTRICT
);

-- invoice line items
CREATE TABLE IF NOT EXISTS invoice_items (
    item_id         INT             NOT NULL AUTO_INCREMENT,
    invoice_id      INT             NOT NULL,
    description     VARCHAR(200)    NOT NULL,
    quantity        INT             NOT NULL DEFAULT 1,
    unit_price      DECIMAL(10, 2)  NOT NULL,
    line_total      DECIMAL(12, 2)  NOT NULL,
    PRIMARY KEY (item_id),
    FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id) ON DELETE CASCADE,
    CONSTRAINT chk_quantity   CHECK (quantity > 0),
    CONSTRAINT chk_unit_price CHECK (unit_price > 0)
);

-- payments table
CREATE TABLE IF NOT EXISTS payments (
    payment_id      INT             NOT NULL AUTO_INCREMENT,
    invoice_id      INT             NOT NULL,
    client_id       INT             NOT NULL,
    amount          DECIMAL(12, 2)  NOT NULL,
    payment_mode    ENUM('CASH', 'BANK_TRANSFER', 'CHEQUE', 'UPI') NOT NULL,
    payment_date    VARCHAR(20)     NOT NULL,
    remarks         VARCHAR(255),
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (payment_id),
    FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id) ON DELETE RESTRICT,
    FOREIGN KEY (client_id)  REFERENCES clients(client_id)  ON DELETE RESTRICT,
    CONSTRAINT chk_amount CHECK (amount > 0)
);

-- sample data
INSERT IGNORE INTO clients (company_name, contact_person, email, phone, credit_limit, current_balance)
VALUES
  ('Sharma Enterprises Pvt Ltd',  'Rajesh Sharma',   'rajesh@sharmaent.com',  '9820001111', 500000.00, 0.00),
  ('MegaTech Solutions',          'Priya Mehta',     'priya@megatech.in',     '9820002222', 300000.00, 0.00),
  ('Global Traders Co.',          'Amit Desai',      'amit@globaltraders.in', '9820003333', 750000.00, 0.00),
  ('Sunrise Distributors',        'Sunita Patil',    'sunita@sunrise.com',    '9820004444', 200000.00, 0.00),
  ('Horizon Retail Ltd',          'Vikram Singh',    'vikram@horizon.in',     '9820005555', 400000.00, 0.00);

-- to run: mysql -u root -p < database.sql
