package dao;

import db.DatabaseConnection;
import model.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// handles db operations for payments
public class PaymentDAO implements DAOInterface<Payment> {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public boolean insert(Payment payment) throws Exception {
        String sql = "INSERT INTO payments " +
                     "(invoice_id, client_id, amount, payment_mode, payment_date, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    payment.getInvoiceId());
            ps.setInt(2,    payment.getClientId());
            ps.setDouble(3, payment.getAmount());
            ps.setString(4, payment.getPaymentMode());
            ps.setString(5, payment.getPaymentDate());
            ps.setString(6, payment.getRemarks());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) payment.setId(keys.getInt(1));
                return true;
            }
        }
        return false;
    }

    @Override
    public Payment getById(int id) throws Exception {
        String sql = "SELECT p.*, i.invoice_number, c.company_name " +
                     "FROM payments p " +
                     "JOIN invoices i ON p.invoice_id = i.invoice_id " +
                     "JOIN clients c ON p.client_id = c.client_id " +
                     "WHERE p.payment_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<Payment> getAll() throws Exception {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, i.invoice_number, c.company_name " +
                     "FROM payments p " +
                     "JOIN invoices i ON p.invoice_id = i.invoice_id " +
                     "JOIN clients c ON p.client_id = c.client_id " +
                     "ORDER BY p.payment_date DESC";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Payment> getByClientId(int clientId) throws Exception {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, i.invoice_number, c.company_name " +
                     "FROM payments p " +
                     "JOIN invoices i ON p.invoice_id = i.invoice_id " +
                     "JOIN clients c ON p.client_id = c.client_id " +
                     "WHERE p.client_id = ? ORDER BY p.payment_date DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Payment> getByInvoiceId(int invoiceId) throws Exception {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, i.invoice_number, c.company_name " +
                     "FROM payments p " +
                     "JOIN invoices i ON p.invoice_id = i.invoice_id " +
                     "JOIN clients c ON p.client_id = c.client_id " +
                     "WHERE p.invoice_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean update(Payment payment) throws Exception {
        String sql = "UPDATE payments SET amount=?, payment_mode=?, " +
                     "payment_date=?, remarks=? WHERE payment_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, payment.getAmount());
            ps.setString(2, payment.getPaymentMode());
            ps.setString(3, payment.getPaymentDate());
            ps.setString(4, payment.getRemarks());
            ps.setInt(5,    payment.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // returns [count, totalAmount] for given month
    // payment_date is stored as 'dd-MM-yyyy' VARCHAR
    public double[] getMonthlyStats(int month, int year) throws Exception {
        String monthStr = String.format("%02d-%04d", month, year);
        String sql = "SELECT COUNT(*), COALESCE(SUM(amount),0) " +
                     "FROM payments WHERE SUBSTRING(payment_date,4,7)=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, monthStr);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new double[]{rs.getDouble(1), rs.getDouble(2)};
            }
        }
        return new double[]{0, 0};
    }

    @Override
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM payments WHERE payment_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // map ResultSet row to Payment object
    private Payment mapRow(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("payment_id"),
                rs.getInt("invoice_id"),
                rs.getInt("client_id"),
                rs.getString("invoice_number"),
                rs.getString("company_name"),
                rs.getDouble("amount"),
                rs.getString("payment_mode"),
                rs.getString("payment_date"),
                rs.getString("remarks")
        );
    }
}
