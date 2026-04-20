package model;

// single line item on an invoice
public class InvoiceItem {

    private int    itemId;
    private int    invoiceId;
    private String description;
    private int    quantity;
    private double unitPrice;
    private double lineTotal;

    public InvoiceItem() {}

    public InvoiceItem(String description, int quantity, double unitPrice) {
        this.description = description;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
        this.lineTotal   = quantity * unitPrice;
    }

    // loading from db
    public InvoiceItem(int itemId, int invoiceId, String description,
                       int quantity, double unitPrice) {
        this.itemId      = itemId;
        this.invoiceId   = invoiceId;
        this.description = description;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
        this.lineTotal   = quantity * unitPrice;
    }

    public void displayItem(int index) {
        System.out.printf("  %d. %-30s | Qty: %3d | Rate: ₹%8.2f | Total: ₹%10.2f%n",
                index, description, quantity, unitPrice, lineTotal);
    }

    // getters and setters
    public int getItemId()                     { return itemId; }
    public void setItemId(int id)              { this.itemId = id; }

    public int getInvoiceId()                  { return invoiceId; }
    public void setInvoiceId(int id)           { this.invoiceId = id; }

    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }

    public int getQuantity()                   { return quantity; }
    public void setQuantity(int q) {
        this.quantity  = q;
        this.lineTotal = q * unitPrice;
    }

    public double getUnitPrice()               { return unitPrice; }
    public void setUnitPrice(double p) {
        this.unitPrice = p;
        this.lineTotal = quantity * p;
    }

    public double getLineTotal()               { return lineTotal; }
}
