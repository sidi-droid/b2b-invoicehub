package service;

import dao.ClientDAO;
import dao.InvoiceDAO;
import dao.PaymentDAO;
import model.Client;
import model.Invoice;
import model.InvoiceItem;
import model.Payment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// business logic layer - credit checks, invoicing, payments, reports
public class InvoiceService {

    private final ClientDAO  clientDAO  = new ClientDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private int invoiceCounter = 1000;

    public String generateInvoiceNumber() {
        invoiceCounter++;
        return "INV-" + LocalDate.now().getYear() + "-" + invoiceCounter;
    }

    // generate with custom prefix
    public String generateInvoiceNumber(String prefix) {
        invoiceCounter++;
        return prefix + "-" + LocalDate.now().getYear() + "-" + invoiceCounter;
    }

    // creates invoice only if client has enough credit
    public Invoice createInvoice(int clientId, List<InvoiceItem> items,
                                  double taxRate, int dueDays) throws Exception {
        Client client = clientDAO.getById(clientId);
        if (client == null)
            throw new Exception("Client not found with ID: " + clientId);

        String dueDate = LocalDate.now().plusDays(dueDays).format(DATE_FMT);
        Invoice inv = new Invoice(clientId, client.getCompanyName(),
                                  generateInvoiceNumber(), taxRate, dueDate);
        for (InvoiceItem item : items) inv.addItem(item);

        // credit check
        if (!client.canAccommodateInvoice(inv.getTotalAmount())) {
            throw new Exception(String.format(
                "Credit limit exceeded!\n" +
                "  Invoice Total  : ₹%.2f\n" +
                "  Available Credit: ₹%.2f\n" +
                "  Please reduce invoice amount or increase client credit limit.",
                inv.getTotalAmount(), client.getAvailableCredit()));
        }

        invoiceDAO.insert(inv);

        // update client outstanding balance
        double newBalance = client.getCurrentBalance() + inv.getTotalAmount();
        clientDAO.updateBalance(clientId, newBalance);

        return inv;
    }

    // default 18% GST and 30 day due
    public Invoice createInvoice(int clientId, List<InvoiceItem> items) throws Exception {
        return createInvoice(clientId, items, 0.18, 30);
    }

    // record payment and update invoice + client balances
    public Payment recordPayment(int invoiceId, int clientId, double amount,
                                  String mode, String remarks) throws Exception {
        Invoice inv = invoiceDAO.getById(invoiceId);
        if (inv == null)
            throw new Exception("Invoice not found with ID: " + invoiceId);

        if (inv.getStatus().equals(Invoice.STATUS_CANCELLED))
            throw new Exception("Cannot record payment for a cancelled invoice.");

        if (amount <= 0)
            throw new Exception("Payment amount must be greater than zero.");

        if (amount > inv.getBalanceDue())
            throw new Exception(String.format(
                "Payment amount (₹%.2f) exceeds balance due (₹%.2f).",
                amount, inv.getBalanceDue()));

        String today = LocalDate.now().format(DATE_FMT);
        Payment payment = new Payment(invoiceId, clientId, amount, mode, today, remarks);
        if (!paymentDAO.insert(payment)) {
            throw new Exception("Failed to save payment record to the database.");
        }

        inv.recordPayment(amount);
        invoiceDAO.update(inv);

        // update client outstanding balance
        Client client = clientDAO.getById(clientId);
        if (client != null) {
            double newBalance = Math.max(0, client.getCurrentBalance() - amount);
            clientDAO.updateBalance(clientId, newBalance);
        }

        return payment;
    }

    // returns client name -> outstanding balance map
    public Map<String, Double> getOutstandingSummary() throws Exception {
        Map<String, Double> summary = new HashMap<>();
        List<Client> clients = clientDAO.getAll();
        for (Client c : clients) {
            if (c.getCurrentBalance() > 0) {
                summary.put(c.getCompanyName(), c.getCurrentBalance());
            }
        }
        return summary;
    }

    // clients who used more than 80% of their credit limit
    public List<Client> getHighRiskClients() throws Exception {
        List<Client> all = clientDAO.getAll();
        List<Client> highRisk = new java.util.ArrayList<>();
        for (Client c : all) {
            if (c.getCreditLimit() > 0) {
                double utilization = (c.getCurrentBalance() / c.getCreditLimit()) * 100;
                if (utilization >= 80) highRisk.add(c);
            }
        }
        return highRisk;
    }

    public double getTotalOutstanding() throws Exception {
        return invoiceDAO.getTotalOutstanding();
    }

    // monthly summary with invoice and payment stats
    public Map<String, Double> getMonthlySummary(int month, int year) throws Exception {
        double[] invStats = invoiceDAO.getMonthlyStats(month, year);
        double[] payStats = paymentDAO.getMonthlyStats(month, year);

        Map<String, Double> summary = new HashMap<>();
        summary.put("invoiceCount",  invStats[0]);
        summary.put("invoiceTotal",  invStats[1]);
        summary.put("invoicePaid",   invStats[2]);
        summary.put("invoiceDue",    invStats[3]);
        summary.put("paymentCount",  payStats[0]);
        summary.put("paymentTotal",  payStats[1]);
        return summary;
    }

    // dao access helpers for UI
    public ClientDAO  getClientDAO()  { return clientDAO; }
    public InvoiceDAO getInvoiceDAO() { return invoiceDAO; }
    public PaymentDAO getPaymentDAO() { return paymentDAO; }
}
