package dao;

import db.DatabaseConnection;
import model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// handles db operations for clients
public class ClientDAO implements DAOInterface<Client> {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public boolean insert(Client client) throws Exception {
        String sql = "INSERT INTO clients " +
                     "(company_name, contact_person, email, phone, credit_limit, current_balance) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getCompanyName());
            ps.setString(2, client.getContactPerson());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getPhone());
            ps.setDouble(5, client.getCreditLimit());
            ps.setDouble(6, client.getCurrentBalance());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) client.setId(keys.getInt(1));
                return true;
            }
        }
        return false;
    }

    @Override
    public Client getById(int id) throws Exception {
        String sql = "SELECT * FROM clients WHERE client_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<Client> getAll() throws Exception {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients ORDER BY company_name";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) clients.add(mapRow(rs));
        }
        return clients;
    }

    public List<Client> searchByName(String keyword) throws Exception {
        List<Client> results = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE company_name LIKE ? ORDER BY company_name";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) results.add(mapRow(rs));
        }
        return results;
    }

    @Override
    public boolean update(Client client) throws Exception {
        String sql = "UPDATE clients SET company_name=?, contact_person=?, email=?, " +
                     "phone=?, credit_limit=?, current_balance=? WHERE client_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, client.getCompanyName());
            ps.setString(2, client.getContactPerson());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getPhone());
            ps.setDouble(5, client.getCreditLimit());
            ps.setDouble(6, client.getCurrentBalance());
            ps.setInt(7, client.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // update balance after payment
    public boolean updateBalance(int clientId, double newBalance) throws Exception {
        String sql = "UPDATE clients SET current_balance=? WHERE client_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, clientId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM clients WHERE client_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // map ResultSet row to Client object
    private Client mapRow(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("client_id"),
                rs.getString("company_name"),
                rs.getString("contact_person"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getDouble("credit_limit"),
                rs.getDouble("current_balance")
        );
    }
}
