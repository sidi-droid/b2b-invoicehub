package dao;

import db.DatabaseConnection;
import model.Invoice;
import model.InvoiceItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// handles db operations for invoices
public class InvoiceDAO implements DAOInterface<Invoice> {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public boolean insert(Invoice inv) throws Exception {
        String sql = "INSERT INTO invoices " +
                "(client_id, invoice_number, subtotal, tax_rate, tax_amount, " +
                "total_amount, amount_paid, balance_due, status, due_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    inv.getClientId());
            ps.setString(2, inv.getInvoiceNumber());
            ps.setDouble(3, inv.getSubtotal());
            ps.setDouble(4, inv.getTaxRate());
            ps.setDouble(5, inv.getTaxAmount());
            ps.setDouble(6, inv.getTotalAmount());
            ps.setDouble(7, inv.getAmountPaid());
            ps.setDouble(8, inv.getBalanceDue());
            ps.setString(9, inv.getStatus());
            ps.setString(10,inv.getDueDate());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    inv.setId(keys.getInt(1));
                    // insert each line item
                    for (InvoiceItem item : inv.getItems()) {
                        item.setInvoiceId(inv.getId());
                        insertItem(item);
                    }
                }
                return true;
            }
        }
        return false;
    }

    // insert a single line item
    public boolean insertItem(InvoiceItem item) throws Exception {
        String sql = "INSERT INTO invoice_items (invoice_id, description, quantity, unit_price, line_total) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    item.getInvoiceId());
            ps.setString(2, item.getDescription());
            ps.setInt(3,    item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getLineTotal());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) item.setItemId(keys.getInt(1));
                return true;
            }
        }
        return false;
    }

    @Override
    public Invoice getById(int id) throws Exception {
        String sql = "SELECT i.*, c.company_name FROM invoices i " +
                     "JOIN clients c ON i.client_id = c.client_id " +
                     "WHERE i.invoice_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Invoice inv = mapRow(rs);
                inv.setItems(getItemsByInvoiceId(id));
                return inv;
            }
        }
        return null;
    }

    @Override
    public List<Invoice> getAll() throws Exception {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT i.*, c.company_name FROM invoices i " +
                     "JOIN clients c ON i.client_id = c.client_id " +
                     "ORDER BY i.invoice_id DESC";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Invoice> getByClientId(int clientId) throws Exception {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT i.*, c.company_name FROM invoices i " +
                     "JOIN clients c ON i.client_id = c.client_id " +
                     "WHERE i.client_id = ? ORDER BY i.invoice_id DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Invoice> getByStatus(String status) throws Exception {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT i.*, c.company_name FROM invoices i " +
                     "JOIN clients c ON i.client_id = c.client_id " +
                     "WHERE i.status = ? ORDER BY i.invoice_id DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<InvoiceItem> getItemsByInvoiceId(int invoiceId) throws Exception {
        List<InvoiceItem> items = new ArrayList<>();
        String sql = "SELECT * FROM invoice_items WHERE invoice_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new InvoiceItem(
                        rs.getInt("item_id"),
                        rs.getInt("invoice_id"),
                        rs.getString("description"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price")
                ));
            }
        }
        return items;
    }

    @Override
    public boolean update(Invoice inv) throws Exception {
        String sql = "UPDATE invoices SET subtotal=?, tax_amount=?, total_amount=?, " +
                     "amount_paid=?, balance_due=?, status=?, due_date=? WHERE invoice_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, inv.getSubtotal());
            ps.setDouble(2, inv.getTaxAmount());
            ps.setDouble(3, inv.getTotalAmount());
            ps.setDouble(4, inv.getAmountPaid());
            ps.setDouble(5, inv.getBalanceDue());
            ps.setString(6, inv.getStatus());
            ps.setString(7, inv.getDueDate());
            ps.setInt(8,    inv.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cancelInvoice(int invoiceId) throws Exception {
        String sql = "UPDATE invoices SET status='CANCELLED' WHERE invoice_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws Exception {
        // delete items first (FK constraint)
        String delItems = "DELETE FROM invoice_items WHERE invoice_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(delItems)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        String sql = "DELETE FROM invoices WHERE invoice_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // reports

    public double getTotalOutstanding() throws Exception {
        String sql = "SELECT COALESCE(SUM(balance_due), 0) FROM invoices WHERE status != 'CANCELLED'";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0;
    }

    // returns [count, totalAmount, totalPaid, totalDue]
    public double[] getMonthlyStats(int month, int year) throws Exception {
        String sql = "SELECT COUNT(*), COALESCE(SUM(total_amount),0), " +
                     "COALESCE(SUM(amount_paid),0), COALESCE(SUM(balance_due),0) " +
                     "FROM invoices " +
                     "WHERE MONTH(created_at)=? AND YEAR(created_at)=? AND status != 'CANCELLED'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new double[]{rs.getDouble(1), rs.getDouble(2),
                                    rs.getDouble(3), rs.getDouble(4)};
            }
        }
        return new double[]{0, 0, 0, 0};
    }

    // map ResultSet row to Invoice object
    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice(
                rs.getInt("invoice_id"),
                rs.getInt("client_id"),
                rs.getString("company_name"),
                rs.getString("invoice_number"),
                rs.getDouble("subtotal"),
                rs.getDouble("tax_rate"),
                rs.getDouble("tax_amount"),
                rs.getDouble("total_amount"),
                rs.getDouble("amount_paid"),
                rs.getString("status"),
                rs.getString("due_date")
        );
        return inv;
    }
}
