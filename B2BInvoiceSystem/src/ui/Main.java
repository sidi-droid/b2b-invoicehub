package ui;

import dao.ClientDAO;
import dao.InvoiceDAO;
import dao.PaymentDAO;
import db.DatabaseConnection;
import model.*;
import service.InvoiceService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// console UI for the B2B Invoice System
public class Main {

    private static final Scanner       scanner = new Scanner(System.in);
    private static final InvoiceService service = new InvoiceService();

    public static void main(String[] args) {
        printBanner();
        try {
            DatabaseConnection.getInstance();
            mainMenu();
        } catch (Exception e) {
            System.out.println("\n  ✘ Failed to connect to database: " + e.getMessage());
            System.out.println("  Please check DatabaseConnection.java and ensure MySQL is running.");
        } finally {
            try { DatabaseConnection.getInstance().closeConnection(); } catch (Exception ignored) {}
            System.out.println("\n  Thank you for using B2B Invoice System. Goodbye!");
        }
    }

    // main menu
    private static void mainMenu() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║     B2B INVOICE & CREDIT MANAGEMENT      ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  1. Client Management                    ║");
            System.out.println("║  2. Invoice Management                   ║");
            System.out.println("║  3. Payment Management                   ║");
            System.out.println("║  4. Reports & Summary                    ║");
            System.out.println("║  0. Exit                                 ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.print("  Enter choice: ");
            int choice = readInt();
            switch (choice) {
                case 1: clientMenu();  break;
                case 2: invoiceMenu(); break;
                case 3: paymentMenu(); break;
                case 4: reportsMenu(); break;
                case 0: return;
                default: System.out.println("  ✘ Invalid choice.");
            }
        }
    }

    // client management

    private static void clientMenu() {
        while (true) {
            System.out.println("\n── CLIENT MANAGEMENT ─────────────────────");
            System.out.println("  1. Add New Client");
            System.out.println("  2. View All Clients");
            System.out.println("  3. View Client Details");
            System.out.println("  4. Update Client");
            System.out.println("  5. Delete Client");
            System.out.println("  6. Search Client by Name");
            System.out.println("  0. Back");
            System.out.print("  Enter choice: ");
            int choice = readInt();
            switch (choice) {
                case 1: addClient();          break;
                case 2: viewAllClients();     break;
                case 3: viewClientDetails();  break;
                case 4: updateClient();       break;
                case 5: deleteClient();       break;
                case 6: searchClient();       break;
                case 0: return;
                default: System.out.println("  ✘ Invalid choice.");
            }
        }
    }

    private static void addClient() {
        System.out.println("\n── ADD NEW CLIENT ────────────────────────");
        try {
            System.out.print("  Company Name    : "); String company = readString();
            System.out.print("  Contact Person  : "); String contact = readString();
            System.out.print("  Email           : "); String email   = readString();
            System.out.print("  Phone           : "); String phone   = readString();
            System.out.print("  Credit Limit (₹): "); double limit  = readDouble();

            if (company.isEmpty() || email.isEmpty())
                throw new Exception("Company name and email cannot be empty.");
            if (limit <= 0)
                throw new Exception("Credit limit must be greater than zero.");

            Client client = new Client(company, contact, email, phone, limit);
            if (service.getClientDAO().insert(client)) {
                System.out.println("\n  ✔ Client added successfully! ID: " + client.getId());
            } else {
                System.out.println("  ✘ Failed to add client.");
            }
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void viewAllClients() {
        System.out.println("\n── ALL CLIENTS ───────────────────────────");
        try {
            List<Client> clients = service.getClientDAO().getAll();
            if (clients.isEmpty()) {
                System.out.println("  No clients found."); return;
            }
            System.out.printf("  %-5s %-25s %-20s %14s %14s%n",
                    "ID", "Company", "Contact", "Credit Limit", "Outstanding");
            System.out.println("  " + "─".repeat(80));
            for (Client c : clients) {
                System.out.printf("  %-5d %-25s %-20s %13.2f %14.2f%n",
                        c.getId(), c.getCompanyName(), c.getContactPerson(),
                        c.getCreditLimit(), c.getCurrentBalance());
            }
            System.out.println("  " + "─".repeat(80));
            System.out.printf("  Total clients: %d%n", clients.size());
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void viewClientDetails() {
        System.out.print("\n  Enter Client ID: ");
        int id = readInt();
        try {
            Client client = service.getClientDAO().getById(id);
            if (client == null) { System.out.println("  ✘ Client not found."); return; }
            System.out.println();
            client.displayInfo();
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void updateClient() {
        System.out.print("\n  Enter Client ID to update: ");
        int id = readInt();
        try {
            Client client = service.getClientDAO().getById(id);
            if (client == null) { System.out.println("  ✘ Client not found."); return; }

            System.out.println("  (Press ENTER to keep existing value)");
            System.out.print("  Company Name [" + client.getCompanyName() + "]: ");
            String v = readString(); if (!v.isEmpty()) client.setCompanyName(v);

            System.out.print("  Contact Person [" + client.getContactPerson() + "]: ");
            v = readString(); if (!v.isEmpty()) client.setContactPerson(v);

            System.out.print("  Email [" + client.getEmail() + "]: ");
            v = readString(); if (!v.isEmpty()) client.setEmail(v);

            System.out.print("  Phone [" + client.getPhone() + "]: ");
            v = readString(); if (!v.isEmpty()) client.setPhone(v);

            System.out.print("  Credit Limit [" + client.getCreditLimit() + "]: ");
            v = readString();
            if (!v.isEmpty()) client.setCreditLimit(Double.parseDouble(v));

            if (service.getClientDAO().update(client))
                System.out.println("  ✔ Client updated successfully.");
            else
                System.out.println("  ✘ Update failed.");
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void deleteClient() {
        System.out.print("\n  Enter Client ID to delete: ");
        int id = readInt();
        System.out.print("  Are you sure? (yes/no): ");
        if (!readString().equalsIgnoreCase("yes")) {
            System.out.println("  Cancelled."); return;
        }
        try {
            if (service.getClientDAO().delete(id))
                System.out.println("  ✔ Client deleted.");
            else
                System.out.println("  ✘ Client not found or could not be deleted.");
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void searchClient() {
        System.out.print("\n  Enter search keyword: ");
        String keyword = readString();
        try {
            List<Client> results = service.getClientDAO().searchByName(keyword);
            if (results.isEmpty()) {
                System.out.println("  No clients found matching '" + keyword + "'."); return;
            }
            System.out.println("\n  Search Results:");
            for (Client c : results) c.displaySummary();
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // invoice management

    private static void invoiceMenu() {
        while (true) {
            System.out.println("\n── INVOICE MANAGEMENT ────────────────────");
            System.out.println("  1. Create New Invoice");
            System.out.println("  2. View All Invoices");
            System.out.println("  3. View Invoice Details");
            System.out.println("  4. View Invoices by Client");
            System.out.println("  5. Filter by Status");
            System.out.println("  6. Cancel Invoice");
            System.out.println("  0. Back");
            System.out.print("  Enter choice: ");
            int choice = readInt();
            switch (choice) {
                case 1: createInvoice();        break;
                case 2: viewAllInvoices();      break;
                case 3: viewInvoiceDetails();   break;
                case 4: viewInvoicesByClient(); break;
                case 5: filterInvoiceStatus();  break;
                case 6: cancelInvoice();        break;
                case 0: return;
                default: System.out.println("  ✘ Invalid choice.");
            }
        }
    }

    private static void createInvoice() {
        System.out.println("\n── CREATE NEW INVOICE ────────────────────");
        try {
            System.out.print("  Client ID     : "); int clientId = readInt();
            Client client = service.getClientDAO().getById(clientId);
            if (client == null) { System.out.println("  ✘ Client not found."); return; }
            System.out.println("  Client: " + client.getCompanyName() +
                               " | Available Credit: ₹" + String.format("%,.2f", client.getAvailableCredit()));

            System.out.print("  Due in (days) : "); int days = readInt();
            System.out.print("  Tax Rate % [18]: "); String tr = readString();
            double taxRate = tr.isEmpty() ? 0.18 : Double.parseDouble(tr) / 100;

            // collect line items
            List<InvoiceItem> items = new ArrayList<>();
            while (true) {
                System.out.println("\n  ── Add Item (type DONE to finish) ──");
                System.out.print("  Description (or DONE): "); String desc = readString();
                if (desc.equalsIgnoreCase("DONE")) break;
                System.out.print("  Quantity              : "); int qty = readInt();
                System.out.print("  Unit Price (₹)        : "); double price = readDouble();
                if (qty <= 0 || price <= 0) {
                    System.out.println("  ✘ Quantity and price must be positive."); continue;
                }
                items.add(new InvoiceItem(desc, qty, price));
                System.out.println("  ✔ Item added.");
            }

            if (items.isEmpty()) { System.out.println("  ✘ No items added. Invoice cancelled."); return; }

            Invoice inv = service.createInvoice(clientId, items, taxRate, days);
            System.out.println("\n  ✔ Invoice created successfully!");
            System.out.println();
            inv.displayInfo();

        } catch (Exception e) {
            System.out.println("\n  ✘ " + e.getMessage());
        }
    }

    private static void viewAllInvoices() {
        System.out.println("\n── ALL INVOICES ──────────────────────────");
        try {
            List<Invoice> invoices = service.getInvoiceDAO().getAll();
            if (invoices.isEmpty()) { System.out.println("  No invoices found."); return; }
            System.out.printf("  %-5s %-16s %-22s %12s %-10s%n",
                    "ID", "Invoice No.", "Client", "Total (₹)", "Status");
            System.out.println("  " + "─".repeat(72));
            for (Invoice inv : invoices) inv.displaySummary();
            System.out.println("  " + "─".repeat(72));
            System.out.printf("  Total invoices: %d%n", invoices.size());
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void viewInvoiceDetails() {
        System.out.print("\n  Enter Invoice ID: ");
        int id = readInt();
        try {
            Invoice inv = service.getInvoiceDAO().getById(id);
            if (inv == null) { System.out.println("  ✘ Invoice not found."); return; }
            System.out.println();
            inv.displayInfo();
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void viewInvoicesByClient() {
        System.out.print("\n  Enter Client ID: ");
        int clientId = readInt();
        try {
            List<Invoice> invoices = service.getInvoiceDAO().getByClientId(clientId);
            if (invoices.isEmpty()) { System.out.println("  No invoices for this client."); return; }
            System.out.println("\n  Invoices for Client #" + clientId + ":");
            for (Invoice inv : invoices) inv.displaySummary();
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void filterInvoiceStatus() {
        System.out.println("\n  Status: 1.UNPAID  2.PAID  3.PARTIAL  4.CANCELLED");
        System.out.print("  Choose: ");
        int c = readInt();
        String[] statuses = {"UNPAID", "PAID", "PARTIAL", "CANCELLED"};
        if (c < 1 || c > 4) { System.out.println("  ✘ Invalid."); return; }
        try {
            List<Invoice> invoices = service.getInvoiceDAO().getByStatus(statuses[c - 1]);
            if (invoices.isEmpty()) { System.out.println("  No invoices found."); return; }
            for (Invoice inv : invoices) inv.displaySummary();
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void cancelInvoice() {
        System.out.print("\n  Enter Invoice ID to cancel: ");
        int id = readInt();
        System.out.print("  Confirm cancel? (yes/no): ");
        if (!readString().equalsIgnoreCase("yes")) { System.out.println("  Cancelled."); return; }
        try {
            Invoice inv = service.getInvoiceDAO().getById(id);
            if (inv == null) { System.out.println("  ✘ Invoice not found."); return; }
            service.getInvoiceDAO().cancelInvoice(id);
            // release client balance
            Client client = service.getClientDAO().getById(inv.getClientId());
            if (client != null) {
                double newBal = Math.max(0, client.getCurrentBalance() - inv.getBalanceDue());
                service.getClientDAO().updateBalance(client.getId(), newBal);
            }
            System.out.println("  ✔ Invoice cancelled successfully.");
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // payment management

    private static void paymentMenu() {
        while (true) {
            System.out.println("\n── PAYMENT MANAGEMENT ────────────────────");
            System.out.println("  1. Record New Payment");
            System.out.println("  2. View All Payments");
            System.out.println("  3. View Payments by Client");
            System.out.println("  4. View Payments by Invoice");
            System.out.println("  0. Back");
            System.out.print("  Enter choice: ");
            int choice = readInt();
            switch (choice) {
                case 1: recordPayment();           break;
                case 2: viewAllPayments();         break;
                case 3: viewPaymentsByClient();    break;
                case 4: viewPaymentsByInvoice();   break;
                case 0: return;
                default: System.out.println("  ✘ Invalid choice.");
            }
        }
    }

    private static void recordPayment() {
        System.out.println("\n── RECORD PAYMENT ────────────────────────");
        try {
            System.out.print("  Invoice ID  : "); int invoiceId = readInt();
            Invoice inv = service.getInvoiceDAO().getById(invoiceId);
            if (inv == null) { System.out.println("  ✘ Invoice not found."); return; }

            System.out.println("  Invoice: " + inv.getInvoiceNumber() +
                               " | Balance Due: ₹" + String.format("%,.2f", inv.getBalanceDue()));

            System.out.print("  Amount (₹)  : "); double amount = readDouble();
            System.out.println("  Payment Mode: 1.CASH  2.BANK_TRANSFER  3.CHEQUE  4.UPI");
            System.out.print("  Choose mode : "); int m = readInt();
            String[] modes = {Payment.MODE_CASH, Payment.MODE_BANK, Payment.MODE_CHEQUE, Payment.MODE_UPI};
            if (m < 1 || m > 4) { System.out.println("  ✘ Invalid mode."); return; }
            System.out.print("  Remarks     : "); String remarks = readString();

            Payment payment = service.recordPayment(
                    invoiceId, inv.getClientId(), amount, modes[m - 1], remarks);

            System.out.println("\n  ✔ Payment recorded! Payment ID: " + payment.getId());
            System.out.println("  Updated Invoice Status: ");
            Invoice updated = service.getInvoiceDAO().getById(invoiceId);
            if (updated != null) updated.displaySummary();

        } catch (Exception e) {
            System.out.println("\n  ✘ " + e.getMessage());
        }
    }

    private static void viewAllPayments() {
        System.out.println("\n── ALL PAYMENTS ──────────────────────────");
        try {
            List<Payment> payments = service.getPaymentDAO().getAll();
            if (payments.isEmpty()) { System.out.println("  No payments found."); return; }
            for (Payment p : payments) p.displaySummary();
            System.out.printf("  Total records: %d%n", payments.size());
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void viewPaymentsByClient() {
        System.out.print("\n  Enter Client ID: ");
        int clientId = readInt();
        try {
            List<Payment> payments = service.getPaymentDAO().getByClientId(clientId);
            if (payments.isEmpty()) { System.out.println("  No payments for this client."); return; }
            System.out.println("\n  Payment History for Client #" + clientId + ":");
            for (Payment p : payments) p.displaySummary();
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void viewPaymentsByInvoice() {
        System.out.print("\n  Enter Invoice ID: ");
        int invoiceId = readInt();
        try {
            List<Payment> payments = service.getPaymentDAO().getByInvoiceId(invoiceId);
            if (payments.isEmpty()) { System.out.println("  No payments for this invoice."); return; }
            for (Payment p : payments) p.displaySummary();
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // reports

    private static void reportsMenu() {
        while (true) {
            System.out.println("\n── REPORTS & SUMMARY ─────────────────────");
            System.out.println("  1. Total Outstanding Dues");
            System.out.println("  2. Outstanding by Client");
            System.out.println("  3. High-Risk Clients (≥80% credit used)");
            System.out.println("  4. Unpaid Invoices");
            System.out.println("  5. Monthly Invoice & Payment Summary");
            System.out.println("  0. Back");
            System.out.print("  Enter choice: ");
            int choice = readInt();
            switch (choice) {
                case 1: totalOutstanding();    break;
                case 2: outstandingByClient(); break;
                case 3: highRiskClients();     break;
                case 4: unpaidInvoices();      break;
                case 5: monthlySummary();      break;
                case 0: return;
                default: System.out.println("  ✘ Invalid choice.");
            }
        }
    }

    private static void totalOutstanding() {
        try {
            double total = service.getTotalOutstanding();
            System.out.printf("%n  ══ TOTAL OUTSTANDING DUES ══%n");
            System.out.printf("  ₹%,.2f%n", total);
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void outstandingByClient() {
        try {
            Map<String, Double> summary = service.getOutstandingSummary();
            if (summary.isEmpty()) { System.out.println("  No outstanding dues."); return; }
            System.out.println("\n  ── OUTSTANDING BY CLIENT ──────────────");
            System.out.printf("  %-30s %15s%n", "Client", "Outstanding (₹)");
            System.out.println("  " + "─".repeat(47));
            double total = 0;
            for (Map.Entry<String, Double> entry : summary.entrySet()) {
                System.out.printf("  %-30s %15.2f%n", entry.getKey(), entry.getValue());
                total += entry.getValue();
            }
            System.out.println("  " + "─".repeat(47));
            System.out.printf("  %-30s %15.2f%n", "TOTAL", total);
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void highRiskClients() {
        try {
            List<Client> clients = service.getHighRiskClients();
            if (clients.isEmpty()) { System.out.println("  No high-risk clients."); return; }
            System.out.println("\n  ── HIGH-RISK CLIENTS (≥80% credit used) ──");
            for (Client c : clients) {
                double utilization = (c.getCurrentBalance() / c.getCreditLimit()) * 100;
                System.out.printf("  %-25s | Used: %5.1f%% | Outstanding: ₹%,.2f%n",
                        c.getCompanyName(), utilization, c.getCurrentBalance());
            }
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void unpaidInvoices() {
        try {
            List<Invoice> invoices = service.getInvoiceDAO().getByStatus(Invoice.STATUS_UNPAID);
            invoices.addAll(service.getInvoiceDAO().getByStatus(Invoice.STATUS_PARTIAL));
            if (invoices.isEmpty()) { System.out.println("  All invoices are paid!"); return; }
            System.out.println("\n  ── UNPAID / PARTIAL INVOICES ──────────");
            for (Invoice inv : invoices) inv.displaySummary();
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    private static void monthlySummary() {
        System.out.println("\n── MONTHLY INVOICE & PAYMENT SUMMARY ─────");
        System.out.print("  Enter Month (1-12): "); int month = readInt();
        System.out.print("  Enter Year (e.g. 2026): "); int year = readInt();
        if (month < 1 || month > 12 || year < 2000) {
            System.out.println("  ✘ Invalid month or year."); return;
        }
        try {
            java.util.Map<String, Double> s = service.getMonthlySummary(month, year);
            String[] monthNames = {"", "January","February","March","April","May","June",
                                   "July","August","September","October","November","December"};
            System.out.printf("%n  ══ %s %d ══%n", monthNames[month], year);
            System.out.println("  ┌─────────────────────────────────────────┐");
            System.out.printf( "  │  Invoices Raised  : %-6.0f               │%n", s.get("invoiceCount"));
            System.out.printf( "  │  Total Invoiced   : ₹%-18.2f    │%n",          s.get("invoiceTotal"));
            System.out.printf( "  │  Amount Collected : ₹%-18.2f    │%n",          s.get("invoicePaid"));
            System.out.printf( "  │  Balance Due      : ₹%-18.2f    │%n",          s.get("invoiceDue"));
            System.out.println("  ├─────────────────────────────────────────┤");
            System.out.printf( "  │  Payments Received: %-6.0f               │%n", s.get("paymentCount"));
            System.out.printf( "  │  Total Paid       : ₹%-18.2f    │%n",          s.get("paymentTotal"));
            System.out.println("  └─────────────────────────────────────────┘");
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // utility methods

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║        B2B INVOICE & CREDIT MANAGEMENT           ║");
        System.out.println("  ║              Java Console Application            ║");
        System.out.println("  ╠══════════════════════════════════════════════════╣");
        System.out.println("  ║  Team: Gunn Mulchandani | Durva Chhabria        ║");
        System.out.println("  ║        Siddhant Amin                            ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
        System.out.println("  Connecting to database...");
    }

    private static int readInt() {
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("  ✘ Enter a valid number: ");
            }
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.print("  ✘ Enter a valid number: ");
            }
        }
    }

    private static String readString() {
        return scanner.nextLine().trim();
    }
}
