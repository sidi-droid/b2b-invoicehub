package model;

// premium client with credit score and dynamic credit limit
public class CreditClient extends Client {

    private int    creditScore;     // 300 – 900
    private String clientTier;     // BRONZE / SILVER / GOLD / PLATINUM

    public CreditClient(String companyName, String contactPerson,
                        String email, String phone,
                        double creditLimit, int creditScore) {
        super(companyName, contactPerson, email, phone, creditLimit);
        this.creditScore = creditScore;
        this.clientTier  = calculateTier(creditScore);
    }

    public CreditClient(int id, String companyName, String contactPerson,
                        String email, String phone,
                        double creditLimit, double currentBalance, int creditScore) {
        super(id, companyName, contactPerson, email, phone, creditLimit, currentBalance);
        this.creditScore = creditScore;
        this.clientTier  = calculateTier(creditScore);
    }

    private String calculateTier(int score) {
        if (score >= 800) return "PLATINUM";
        if (score >= 650) return "GOLD";
        if (score >= 500) return "SILVER";
        return "BRONZE";
    }

    // discount based on tier
    public double getDiscountRate() {
        switch (clientTier) {
            case "PLATINUM": return 0.10;
            case "GOLD":     return 0.07;
            case "SILVER":   return 0.05;
            default:         return 0.02;
        }
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.printf( "│ Credit Score  : %-28d │%n", creditScore);
        System.out.printf( "│ Client Tier   : %-28s │%n", clientTier);
        System.out.printf( "│ Discount Rate : %-27.0f%% │%n", getDiscountRate() * 100);
        System.out.println("└─────────────────────────────────────────────┘");
    }

    @Override
    public String getEntityType() {
        return "CREDIT-CLIENT";
    }

    // getters and setters
    public int getCreditScore()              { return creditScore; }
    public void setCreditScore(int score) {
        this.creditScore = score;
        this.clientTier  = calculateTier(score);
    }

    public String getClientTier()            { return clientTier; }
}
