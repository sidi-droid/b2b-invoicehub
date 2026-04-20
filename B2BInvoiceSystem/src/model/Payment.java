package model;

// Payment - records a payment made against an invoice
public class Payment extends Entity {

    public static final String MODE_CASH    = "CASH";
    public static final String MODE_BANK    = "BANK_TRANSFER";
    public static final String MODE_CHEQUE  = "CHEQUE";
    public static final String MODE_UPI     = "UPI";

    private int    invoiceId;
    private int    clientId;
    private String invoiceNumber;
    private String clientName;
    private double amount;
    private String paymentMode;
    private String paymentDate;
    private String remarks;

    public Payment() { super(); }

    // new payment
    public Payment(int invoiceId, int clientId, double amount,
                   String paymentMode, String paymentDate, String remarks) {
        super();
        this.invoiceId   = invoiceId;
        this.clientId    = clientId;
        this.amount      = amount;
        this.paymentMode = paymentMode;
        this.paymentDate = paymentDate;
        this.remarks     = remarks;
    }

    // loading from db
    public Payment(int id, int invoiceId, int clientId, String invoiceNumber,
                   String clientName, double amount, String paymentMode,
                   String paymentDate, String remarks) {
        super(id);
        this.invoiceId     = invoiceId;
        this.clientId      = clientId;
        this.invoiceNumber = invoiceNumber;
        this.clientName    = clientName;
        this.amount        = amount;
        this.paymentMode   = paymentMode;
        this.paymentDate   = paymentDate;
        this.remarks       = remarks;
    }

    @Override
    public void displayInfo() {
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.printf( "│ PAYMENT ID    : %-32d │%n", getId());
        System.out.printf( "│ Invoice       : %-32s │%n", invoiceNumber != null ? invoiceNumber : "#" + invoiceId);
        System.out.printf( "│ Client        : %-32s │%n", clientName != null ? clientName : "#" + clientId);
        System.out.printf( "│ Amount        : ₹%-31.2f │%n", amount);
        System.out.printf( "│ Mode          : %-32s │%n", paymentMode);
        System.out.printf( "│ Date          : %-32s │%n", paymentDate);
        System.out.printf( "│ Remarks       : %-32s │%n", remarks != null ? remarks : "-");
        System.out.println("└──────────────────────────────────────────────────┘");
    }

    public void displaySummary() {
        System.out.printf("  [%d] Date: %-12s | Invoice: %-14s | Amount: ₹%9.2f | Mode: %s%n",
                getId(), paymentDate,
                invoiceNumber != null ? invoiceNumber : "#" + invoiceId,
                amount, paymentMode);
    }

    @Override
    public String getEntityType() { return "PAYMENT"; }

    // getters and setters
    public int getInvoiceId()                   { return invoiceId; }
    public void setInvoiceId(int id)            { this.invoiceId = id; }

    public int getClientId()                    { return clientId; }
    public void setClientId(int id)             { this.clientId = id; }

    public String getInvoiceNumber()            { return invoiceNumber; }
    public void setInvoiceNumber(String n)      { this.invoiceNumber = n; }

    public String getClientName()               { return clientName; }
    public void setClientName(String n)         { this.clientName = n; }

    public double getAmount()                   { return amount; }
    public void setAmount(double a)             { this.amount = a; }

    public String getPaymentMode()              { return paymentMode; }
    public void setPaymentMode(String m)        { this.paymentMode = m; }

    public String getPaymentDate()              { return paymentDate; }
    public void setPaymentDate(String d)        { this.paymentDate = d; }

    public String getRemarks()                  { return remarks; }
    public void setRemarks(String r)            { this.remarks = r; }
}
