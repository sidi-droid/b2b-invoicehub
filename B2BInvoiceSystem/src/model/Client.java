package model;

// Client - represents a business client
public class Client extends Entity {

    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private double creditLimit;
    private double currentBalance;  // outstanding amount owed

    public Client() {
        super();
    }

    // new client (no ID yet)
    public Client(String companyName, String contactPerson,
                  String email, String phone, double creditLimit) {
        super();
        this.companyName   = companyName;
        this.contactPerson = contactPerson;
        this.email         = email;
        this.phone         = phone;
        this.creditLimit   = creditLimit;
        this.currentBalance = 0.0;
    }

    // loading from db
    public Client(int id, String companyName, String contactPerson,
                  String email, String phone,
                  double creditLimit, double currentBalance) {
        super(id);
        this.companyName    = companyName;
        this.contactPerson  = contactPerson;
        this.email          = email;
        this.phone          = phone;
        this.creditLimit    = creditLimit;
        this.currentBalance = currentBalance;
    }

    @Override
    public void displayInfo() {
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.printf( "│ CLIENT ID     : %-28d │%n", getId());
        System.out.printf( "│ Company       : %-28s │%n", companyName);
        System.out.printf( "│ Contact       : %-28s │%n", contactPerson);
        System.out.printf( "│ Email         : %-28s │%n", email);
        System.out.printf( "│ Phone         : %-28s │%n", phone);
        System.out.printf( "│ Credit Limit  : ₹%-27.2f │%n", creditLimit);
        System.out.printf( "│ Outstanding   : ₹%-27.2f │%n", currentBalance);
        System.out.printf( "│ Available     : ₹%-27.2f │%n", getAvailableCredit());
        System.out.println("└─────────────────────────────────────────────┘");
    }

    @Override
    public String getEntityType() {
        return "CLIENT";
    }

    // business logic
    public double getAvailableCredit() {
        return creditLimit - currentBalance;
    }

    public boolean canAccommodateInvoice(double amount) {
        return getAvailableCredit() >= amount;
    }

    // short summary line
    public void displaySummary() {
        System.out.printf("  [%d] %-25s | Outstanding: ₹%,.2f | Available Credit: ₹%,.2f%n",
                getId(), companyName, currentBalance, getAvailableCredit());
    }

    // getters and setters
    public String getCompanyName()               { return companyName; }
    public void setCompanyName(String n)         { this.companyName = n; }

    public String getContactPerson()             { return contactPerson; }
    public void setContactPerson(String c)       { this.contactPerson = c; }

    public String getEmail()                     { return email; }
    public void setEmail(String e)               { this.email = e; }

    public String getPhone()                     { return phone; }
    public void setPhone(String p)               { this.phone = p; }

    public double getCreditLimit()               { return creditLimit; }
    public void setCreditLimit(double l)         { this.creditLimit = l; }

    public double getCurrentBalance()            { return currentBalance; }
    public void setCurrentBalance(double b)      { this.currentBalance = b; }

    @Override
    public String toString() {
        return String.format("Client{id=%d, company='%s', balance=%.2f}",
                getId(), companyName, currentBalance);
    }
}
