package model;

import java.util.ArrayList;
import java.util.List;

// Invoice - represents a B2B invoice with line items, tax, and status
public class Invoice extends Entity {

    // status constants
    public static final String STATUS_UNPAID    = "UNPAID";
    public static final String STATUS_PAID      = "PAID";
    public static final String STATUS_PARTIAL   = "PARTIAL";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private int    clientId;
    private String clientName;
    private String invoiceNumber;
    private double subtotal;
    private double taxRate;      // e.g. 0.18 for 18% GST
    private double taxAmount;
    private double totalAmount;
    private double amountPaid;
    private double balanceDue;
    private String status;
    private String dueDate;

    private List<InvoiceItem> items;

    public Invoice() {
        super();
        this.items     = new ArrayList<>();
        this.status    = STATUS_UNPAID;
        this.taxRate   = 0.18; // default 18% GST
        this.amountPaid = 0.0;
    }

    public Invoice(int clientId, String clientName,
                   String invoiceNumber, double taxRate, String dueDate) {
        super();
        this.clientId      = clientId;
        this.clientName    = clientName;
        this.invoiceNumber = invoiceNumber;
        this.taxRate       = taxRate;
        this.dueDate       = dueDate;
        this.items         = new ArrayList<>();
        this.status        = STATUS_UNPAID;
        this.amountPaid    = 0.0;
    }

    // loading from db
    public Invoice(int id, int clientId, String clientName, String invoiceNumber,
                   double subtotal, double taxRate, double taxAmount,
                   double totalAmount, double amountPaid, String status, String dueDate) {
        super(id);
        this.clientId      = clientId;
        this.clientName    = clientName;
        this.invoiceNumber = invoiceNumber;
        this.subtotal      = subtotal;
        this.taxRate       = taxRate;
        this.taxAmount     = taxAmount;
        this.totalAmount   = totalAmount;
        this.amountPaid    = amountPaid;
        this.balanceDue    = totalAmount - amountPaid;
        this.status        = status;
        this.dueDate       = dueDate;
        this.items         = new ArrayList<>();
    }

    // business logic

    public void addItem(InvoiceItem item) {
        items.add(item);
        recalculate();
    }

    public void recalculate() {
        subtotal    = items.stream().mapToDouble(InvoiceItem::getLineTotal).sum();
        taxAmount   = subtotal * taxRate;
        totalAmount = subtotal + taxAmount;
        balanceDue  = totalAmount - amountPaid;
        updateStatus();
    }

    public void recordPayment(double amount) {
        this.amountPaid += amount;
        this.balanceDue  = totalAmount - amountPaid;
        updateStatus();
    }

    private void updateStatus() {
        if (amountPaid <= 0)                   status = STATUS_UNPAID;
        else if (amountPaid >= totalAmount)    status = STATUS_PAID;
        else                                   status = STATUS_PARTIAL;
    }

    @Override
    public void displayInfo() {
        System.out.println("╔═════════════════════════════════════════════════════╗");
        System.out.printf( "║  INVOICE: %-43s║%n", invoiceNumber);
        System.out.println("╠═════════════════════════════════════════════════════╣");
        System.out.printf( "║  Client       : %-36s║%n", clientName);
        System.out.printf( "║  Due Date     : %-36s║%n", dueDate);
        System.out.printf( "║  Status       : %-36s║%n", status);
        System.out.println("╠═════════════════════════════════════════════════════╣");
        System.out.println("║  ITEMS:                                             ║");
        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                items.get(i).displayItem(i + 1);
            }
        } else {
            System.out.println("  (No items loaded)");
        }
        System.out.println("╠═════════════════════════════════════════════════════╣");
        System.out.printf( "║  Subtotal     : ₹%-34.2f║%n", subtotal);
        System.out.printf( "║  GST (%.0f%%)   : ₹%-34.2f║%n", taxRate * 100, taxAmount);
        System.out.printf( "║  Total        : ₹%-34.2f║%n", totalAmount);
        System.out.printf( "║  Amount Paid  : ₹%-34.2f║%n", amountPaid);
        System.out.printf( "║  Balance Due  : ₹%-34.2f║%n", balanceDue);
        System.out.println("╚═════════════════════════════════════════════════════╝");
    }

    // quick summary display
    public void displaySummary() {
        System.out.printf("  [%d] %-15s | Client: %-20s | Total: ₹%9.2f | Status: %s%n",
                getId(), invoiceNumber, clientName, totalAmount, status);
    }

    @Override
    public String getEntityType() { return "INVOICE"; }

    // getters and setters
    public int getClientId()                      { return clientId; }
    public void setClientId(int id)               { this.clientId = id; }

    public String getClientName()                 { return clientName; }
    public void setClientName(String n)           { this.clientName = n; }

    public String getInvoiceNumber()              { return invoiceNumber; }
    public void setInvoiceNumber(String n)        { this.invoiceNumber = n; }

    public double getSubtotal()                   { return subtotal; }
    public double getTaxRate()                    { return taxRate; }
    public void setTaxRate(double r)              { this.taxRate = r; }
    public double getTaxAmount()                  { return taxAmount; }
    public double getTotalAmount()                { return totalAmount; }
    public double getAmountPaid()                 { return amountPaid; }
    public void setAmountPaid(double a)           { this.amountPaid = a; this.balanceDue = totalAmount - a; }
    public double getBalanceDue()                 { return balanceDue; }
    public String getStatus()                     { return status; }
    public void setStatus(String s)               { this.status = s; }
    public String getDueDate()                    { return dueDate; }
    public void setDueDate(String d)              { this.dueDate = d; }
    public List<InvoiceItem> getItems()           { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
    public void setSubtotal(double s)             { this.subtotal = s; }
    public void setTaxAmount(double t)            { this.taxAmount = t; }
    public void setTotalAmount(double t)          { this.totalAmount = t; }
    public void setBalanceDue(double b)           { this.balanceDue = b; }
}
