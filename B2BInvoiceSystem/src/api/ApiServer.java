package api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import model.Client;
import model.Invoice;
import model.InvoiceItem;
import model.Payment;
import service.InvoiceService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// REST API server for the B2B Invoice System
public class ApiServer {

    private static final int PORT = 8081;
    private static final InvoiceService service = new InvoiceService();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/clients", new ClientHandler());
        server.createContext("/api/invoices", new InvoiceHandler());
        server.createContext("/api/payments", new PaymentHandler());
        server.createContext("/api/reports", new ReportHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("API Server running on http://localhost:" + PORT);
    }

    // CORS and response helpers

    private static void addCorsHeaders(HttpExchange exchange) {
        Headers h = exchange.getResponseHeaders();
        h.set("Access-Control-Allow-Origin", "*");
        h.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        h.set("Access-Control-Allow-Headers", "Content-Type");
        h.set("Content-Type", "application/json; charset=UTF-8");
    }

    private static boolean handlePreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        addCorsHeaders(exchange);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        String json = "{\"error\":" + jsonString(message) + "}";
        sendJson(exchange, status, json);
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // URL and query param helpers

    private static String[] getPathParts(HttpExchange exchange, String contextPrefix) {
        String path = exchange.getRequestURI().getPath();
        String remainder = path.substring(contextPrefix.length());
        if (remainder.startsWith("/")) remainder = remainder.substring(1);
        if (remainder.isEmpty()) return new String[0];
        return remainder.split("/");
    }

    private static String getQueryParam(HttpExchange exchange, String name) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    // JSON serialization helpers (manual, no library)

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private static String jsonString(String s) {
        if (s == null) return "null";
        return "\"" + escapeJson(s) + "\"";
    }

    // client to JSON
    private static String toJson(Client c) {
        return "{" +
            "\"id\":" + c.getId() + "," +
            "\"companyName\":" + jsonString(c.getCompanyName()) + "," +
            "\"contactPerson\":" + jsonString(c.getContactPerson()) + "," +
            "\"email\":" + jsonString(c.getEmail()) + "," +
            "\"phone\":" + jsonString(c.getPhone()) + "," +
            "\"creditLimit\":" + c.getCreditLimit() + "," +
            "\"currentBalance\":" + c.getCurrentBalance() + "," +
            "\"availableCredit\":" + c.getAvailableCredit() +
            "}";
    }

    private static String toJsonArray(List<Client> clients) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < clients.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(clients.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    // invoice item to JSON
    private static String toJson(InvoiceItem item) {
        return "{" +
            "\"itemId\":" + item.getItemId() + "," +
            "\"invoiceId\":" + item.getInvoiceId() + "," +
            "\"description\":" + jsonString(item.getDescription()) + "," +
            "\"quantity\":" + item.getQuantity() + "," +
            "\"unitPrice\":" + item.getUnitPrice() + "," +
            "\"lineTotal\":" + item.getLineTotal() +
            "}";
    }

    // invoice to JSON (includes items)
    private static String toJson(Invoice inv) {
        StringBuilder itemsJson = new StringBuilder("[");
        List<InvoiceItem> items = inv.getItems();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) itemsJson.append(",");
                itemsJson.append(toJson(items.get(i)));
            }
        }
        itemsJson.append("]");

        return "{" +
            "\"id\":" + inv.getId() + "," +
            "\"clientId\":" + inv.getClientId() + "," +
            "\"clientName\":" + jsonString(inv.getClientName()) + "," +
            "\"invoiceNumber\":" + jsonString(inv.getInvoiceNumber()) + "," +
            "\"subtotal\":" + inv.getSubtotal() + "," +
            "\"taxRate\":" + inv.getTaxRate() + "," +
            "\"taxAmount\":" + inv.getTaxAmount() + "," +
            "\"totalAmount\":" + inv.getTotalAmount() + "," +
            "\"amountPaid\":" + inv.getAmountPaid() + "," +
            "\"balanceDue\":" + inv.getBalanceDue() + "," +
            "\"status\":" + jsonString(inv.getStatus()) + "," +
            "\"dueDate\":" + jsonString(inv.getDueDate()) + "," +
            "\"items\":" + itemsJson.toString() +
            "}";
    }

    private static String toJsonInvoiceArray(List<Invoice> invoices) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < invoices.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(invoices.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    // payment to JSON
    private static String toJson(Payment p) {
        return "{" +
            "\"id\":" + p.getId() + "," +
            "\"invoiceId\":" + p.getInvoiceId() + "," +
            "\"clientId\":" + p.getClientId() + "," +
            "\"invoiceNumber\":" + jsonString(p.getInvoiceNumber()) + "," +
            "\"clientName\":" + jsonString(p.getClientName()) + "," +
            "\"amount\":" + p.getAmount() + "," +
            "\"paymentMode\":" + jsonString(p.getPaymentMode()) + "," +
            "\"paymentDate\":" + jsonString(p.getPaymentDate()) + "," +
            "\"remarks\":" + jsonString(p.getRemarks()) +
            "}";
    }

    private static String toJsonPaymentArray(List<Payment> payments) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < payments.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(payments.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    // JSON parsing helpers (simple key-value extraction)

    // extract a string value for a given key from JSON
    private static String parseJsonString(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return null;

        int colonIdx = json.indexOf(':', keyIdx + search.length());
        if (colonIdx == -1) return null;

        int start = colonIdx + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length()) return null;

        if (json.charAt(start) == 'n' && json.startsWith("null", start)) return null;

        if (json.charAt(start) != '"') return null;
        start++;

        StringBuilder value = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"':  value.append('"');  break;
                    case '\\': value.append('\\'); break;
                    case 'n':  value.append('\n'); break;
                    case 'r':  value.append('\r'); break;
                    case 't':  value.append('\t'); break;
                    default:   value.append(next); break;
                }
                i++;
            } else if (c == '"') {
                return value.toString();
            } else {
                value.append(c);
            }
        }
        return null;
    }

    // extract a double value for a given key from JSON
    private static double parseJsonDouble(String json, String key, double defaultVal) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return defaultVal;

        int colonIdx = json.indexOf(':', keyIdx + search.length());
        if (colonIdx == -1) return defaultVal;

        int start = colonIdx + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length()) return defaultVal;

        StringBuilder num = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || c == '.' || c == '-' || c == 'e' || c == 'E' || c == '+') {
                num.append(c);
            } else {
                break;
            }
        }
        if (num.length() == 0) return defaultVal;
        try {
            return Double.parseDouble(num.toString());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static int parseJsonInt(String json, String key, int defaultVal) {
        return (int) parseJsonDouble(json, key, defaultVal);
    }

    // parse the items array from invoice creation body
    private static List<InvoiceItem> parseItemsArray(String json) {
        List<InvoiceItem> items = new ArrayList<>();

        int itemsKeyIdx = json.indexOf("\"items\"");
        if (itemsKeyIdx == -1) return items;

        int bracketStart = json.indexOf('[', itemsKeyIdx);
        if (bracketStart == -1) return items;

        // find matching closing bracket
        int depth = 0;
        int bracketEnd = -1;
        for (int i = bracketStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                i++;
                while (i < json.length()) {
                    if (json.charAt(i) == '\\') { i++; }
                    else if (json.charAt(i) == '"') { break; }
                    i++;
                }
            } else if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) { bracketEnd = i; break; }
            }
        }
        if (bracketEnd == -1) return items;

        String arrayContent = json.substring(bracketStart + 1, bracketEnd);

        // split into individual objects
        int objStart = -1;
        int objDepth = 0;
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '"') {
                i++;
                while (i < arrayContent.length()) {
                    if (arrayContent.charAt(i) == '\\') { i++; }
                    else if (arrayContent.charAt(i) == '"') { break; }
                    i++;
                }
            } else if (c == '{') {
                if (objDepth == 0) objStart = i;
                objDepth++;
            } else if (c == '}') {
                objDepth--;
                if (objDepth == 0 && objStart >= 0) {
                    String objStr = arrayContent.substring(objStart, i + 1);
                    String desc = parseJsonString(objStr, "description");
                    int qty = parseJsonInt(objStr, "quantity", 0);
                    double price = parseJsonDouble(objStr, "unitPrice", 0);
                    if (desc != null && qty > 0 && price > 0) {
                        items.add(new InvoiceItem(desc, qty, price));
                    }
                    objStart = -1;
                }
            }
        }

        return items;
    }

    // client handler
    static class ClientHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handlePreflight(exchange)) return;

            String method = exchange.getRequestMethod().toUpperCase();
            String[] parts = getPathParts(exchange, "/api/clients");

            try {
                // GET /api/clients/search?q=keyword
                if (method.equals("GET") && parts.length >= 1 && parts[0].equals("search")) {
                    String keyword = getQueryParam(exchange, "q");
                    if (keyword == null || keyword.isEmpty()) {
                        sendError(exchange, 400, "Missing query parameter 'q'");
                        return;
                    }
                    List<Client> results = service.getClientDAO().searchByName(keyword);
                    sendJson(exchange, 200, toJsonArray(results));
                    return;
                }

                // GET /api/clients/{id}
                if (method.equals("GET") && parts.length == 1) {
                    int id = Integer.parseInt(parts[0]);
                    Client client = service.getClientDAO().getById(id);
                    if (client == null) {
                        sendError(exchange, 404, "Client not found");
                        return;
                    }
                    sendJson(exchange, 200, toJson(client));
                    return;
                }

                // GET /api/clients
                if (method.equals("GET") && parts.length == 0) {
                    List<Client> clients = service.getClientDAO().getAll();
                    sendJson(exchange, 200, toJsonArray(clients));
                    return;
                }

                // POST /api/clients
                if (method.equals("POST") && parts.length == 0) {
                    String body = readBody(exchange);
                    String companyName   = parseJsonString(body, "companyName");
                    String contactPerson = parseJsonString(body, "contactPerson");
                    String email         = parseJsonString(body, "email");
                    String phone         = parseJsonString(body, "phone");
                    double creditLimit   = parseJsonDouble(body, "creditLimit", 0);

                    if (companyName == null || companyName.isEmpty()) {
                        sendError(exchange, 400, "companyName is required");
                        return;
                    }

                    Client client = new Client(companyName, contactPerson, email, phone, creditLimit);
                    boolean ok = service.getClientDAO().insert(client);
                    if (ok) {
                        sendJson(exchange, 201, toJson(client));
                    } else {
                        sendError(exchange, 500, "Failed to create client");
                    }
                    return;
                }

                // PUT /api/clients/{id}
                if (method.equals("PUT") && parts.length == 1) {
                    int id = Integer.parseInt(parts[0]);
                    Client existing = service.getClientDAO().getById(id);
                    if (existing == null) {
                        sendError(exchange, 404, "Client not found");
                        return;
                    }

                    String body = readBody(exchange);

                    String companyName   = parseJsonString(body, "companyName");
                    String contactPerson = parseJsonString(body, "contactPerson");
                    String email         = parseJsonString(body, "email");
                    String phone         = parseJsonString(body, "phone");
                    double creditLimit   = parseJsonDouble(body, "creditLimit", -1);

                    if (companyName != null)   existing.setCompanyName(companyName);
                    if (contactPerson != null) existing.setContactPerson(contactPerson);
                    if (email != null)         existing.setEmail(email);
                    if (phone != null)         existing.setPhone(phone);
                    if (creditLimit >= 0)      existing.setCreditLimit(creditLimit);

                    boolean ok = service.getClientDAO().update(existing);
                    if (ok) {
                        sendJson(exchange, 200, toJson(existing));
                    } else {
                        sendError(exchange, 500, "Failed to update client");
                    }
                    return;
                }

                // DELETE /api/clients/{id}
                if (method.equals("DELETE") && parts.length == 1) {
                    int id = Integer.parseInt(parts[0]);
                    boolean ok = service.getClientDAO().delete(id);
                    if (ok) {
                        sendJson(exchange, 200, "{\"message\":\"Client deleted\"}");
                    } else {
                        sendError(exchange, 404, "Client not found or could not be deleted");
                    }
                    return;
                }

                sendError(exchange, 405, "Method not allowed");

            } catch (NumberFormatException e) {
                sendError(exchange, 400, "Invalid ID format");
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }

    // invoice handler
    static class InvoiceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handlePreflight(exchange)) return;

            String method = exchange.getRequestMethod().toUpperCase();
            String[] parts = getPathParts(exchange, "/api/invoices");

            try {
                // PUT /api/invoices/{id}/cancel
                if (method.equals("PUT") && parts.length == 2 && parts[1].equals("cancel")) {
                    int id = Integer.parseInt(parts[0]);
                    Invoice inv = service.getInvoiceDAO().getById(id);
                    if (inv == null) {
                        sendError(exchange, 404, "Invoice not found");
                        return;
                    }
                    if (inv.getStatus().equals(Invoice.STATUS_CANCELLED)) {
                        sendError(exchange, 400, "Invoice is already cancelled");
                        return;
                    }

                    service.getInvoiceDAO().cancelInvoice(id);

                    // release client balance
                    Client client = service.getClientDAO().getById(inv.getClientId());
                    if (client != null) {
                        double newBal = Math.max(0, client.getCurrentBalance() - inv.getBalanceDue());
                        service.getClientDAO().updateBalance(client.getId(), newBal);
                    }

                    Invoice updated = service.getInvoiceDAO().getById(id);
                    sendJson(exchange, 200, toJson(updated));
                    return;
                }

                // GET /api/invoices/{id}
                if (method.equals("GET") && parts.length == 1) {
                    int id = Integer.parseInt(parts[0]);
                    Invoice inv = service.getInvoiceDAO().getById(id);
                    if (inv == null) {
                        sendError(exchange, 404, "Invoice not found");
                        return;
                    }
                    sendJson(exchange, 200, toJson(inv));
                    return;
                }

                // GET /api/invoices (optional filters: clientId, status)
                if (method.equals("GET") && parts.length == 0) {
                    String clientIdParam = getQueryParam(exchange, "clientId");
                    String statusParam   = getQueryParam(exchange, "status");

                    List<Invoice> invoices;
                    if (clientIdParam != null && !clientIdParam.isEmpty()) {
                        invoices = service.getInvoiceDAO().getByClientId(Integer.parseInt(clientIdParam));
                    } else if (statusParam != null && !statusParam.isEmpty()) {
                        invoices = service.getInvoiceDAO().getByStatus(statusParam.toUpperCase());
                    } else {
                        invoices = service.getInvoiceDAO().getAll();
                    }
                    sendJson(exchange, 200, toJsonInvoiceArray(invoices));
                    return;
                }

                // POST /api/invoices
                if (method.equals("POST") && parts.length == 0) {
                    String body = readBody(exchange);
                    int clientId  = parseJsonInt(body, "clientId", -1);
                    double taxRate = parseJsonDouble(body, "taxRate", -1);
                    int dueDays    = parseJsonInt(body, "dueDays", -1);

                    if (clientId <= 0) {
                        sendError(exchange, 400, "clientId is required and must be positive");
                        return;
                    }

                    List<InvoiceItem> items = parseItemsArray(body);
                    if (items.isEmpty()) {
                        sendError(exchange, 400, "At least one item is required in the items array");
                        return;
                    }

                    Invoice inv;
                    if (taxRate >= 0 && dueDays > 0) {
                        inv = service.createInvoice(clientId, items, taxRate, dueDays);
                    } else {
                        inv = service.createInvoice(clientId, items);
                    }

                    Invoice created = service.getInvoiceDAO().getById(inv.getId());
                    sendJson(exchange, 201, toJson(created));
                    return;
                }

                sendError(exchange, 405, "Method not allowed");

            } catch (NumberFormatException e) {
                sendError(exchange, 400, "Invalid ID or number format");
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }

    // payment handler
    static class PaymentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handlePreflight(exchange)) return;

            String method = exchange.getRequestMethod().toUpperCase();
            String[] parts = getPathParts(exchange, "/api/payments");

            try {
                // GET /api/payments (optional filters: clientId, invoiceId)
                if (method.equals("GET") && parts.length == 0) {
                    String clientIdParam  = getQueryParam(exchange, "clientId");
                    String invoiceIdParam = getQueryParam(exchange, "invoiceId");

                    List<Payment> payments;
                    if (clientIdParam != null && !clientIdParam.isEmpty()) {
                        payments = service.getPaymentDAO().getByClientId(Integer.parseInt(clientIdParam));
                    } else if (invoiceIdParam != null && !invoiceIdParam.isEmpty()) {
                        payments = service.getPaymentDAO().getByInvoiceId(Integer.parseInt(invoiceIdParam));
                    } else {
                        payments = service.getPaymentDAO().getAll();
                    }
                    sendJson(exchange, 200, toJsonPaymentArray(payments));
                    return;
                }

                // POST /api/payments
                if (method.equals("POST") && parts.length == 0) {
                    String body = readBody(exchange);
                    int invoiceId   = parseJsonInt(body, "invoiceId", -1);
                    int clientId    = parseJsonInt(body, "clientId", -1);
                    double amount   = parseJsonDouble(body, "amount", -1);
                    String mode     = parseJsonString(body, "paymentMode");
                    String remarks  = parseJsonString(body, "remarks");

                    if (invoiceId <= 0) {
                        sendError(exchange, 400, "invoiceId is required and must be positive");
                        return;
                    }
                    // derive client from invoice when omitted (e.g. older clients / JSON without clientId)
                    if (clientId <= 0) {
                        Invoice invForClient = service.getInvoiceDAO().getById(invoiceId);
                        if (invForClient == null) {
                            sendError(exchange, 400, "invoice not found for invoiceId");
                            return;
                        }
                        clientId = invForClient.getClientId();
                    }
                    if (clientId <= 0) {
                        sendError(exchange, 400, "clientId is required and must be positive");
                        return;
                    }
                    if (amount <= 0) {
                        sendError(exchange, 400, "amount is required and must be positive");
                        return;
                    }
                    if (mode == null || mode.isEmpty()) {
                        sendError(exchange, 400, "paymentMode is required");
                        return;
                    }

                    Payment payment = service.recordPayment(invoiceId, clientId, amount, mode,
                                                             remarks != null ? remarks : "");

                    Payment created = service.getPaymentDAO().getById(payment.getId());
                    sendJson(exchange, 201, toJson(created));
                    return;
                }

                sendError(exchange, 405, "Method not allowed");

            } catch (NumberFormatException e) {
                sendError(exchange, 400, "Invalid number format");
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }

    // report handler
    static class ReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handlePreflight(exchange)) return;

            String method = exchange.getRequestMethod().toUpperCase();
            if (!method.equals("GET")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String[] parts = getPathParts(exchange, "/api/reports");

            try {
                if (parts.length == 0) {
                    sendError(exchange, 400, "Specify a report: outstanding, outstanding-by-client, high-risk, monthly");
                    return;
                }

                String reportName = parts[0];

                // GET /api/reports/outstanding
                if (reportName.equals("outstanding")) {
                    double total = service.getTotalOutstanding();
                    sendJson(exchange, 200, "{\"total\":" + total + "}");
                    return;
                }

                // GET /api/reports/outstanding-by-client
                if (reportName.equals("outstanding-by-client")) {
                    Map<String, Double> summary = service.getOutstandingSummary();
                    StringBuilder sb = new StringBuilder("{");
                    boolean first = true;
                    for (Map.Entry<String, Double> entry : summary.entrySet()) {
                        if (!first) sb.append(",");
                        sb.append(jsonString(entry.getKey())).append(":").append(entry.getValue());
                        first = false;
                    }
                    sb.append("}");
                    sendJson(exchange, 200, sb.toString());
                    return;
                }

                // GET /api/reports/high-risk
                if (reportName.equals("high-risk")) {
                    List<Client> highRisk = service.getHighRiskClients();
                    sendJson(exchange, 200, toJsonArray(highRisk));
                    return;
                }

                // GET /api/reports/monthly?month=4&year=2026
                if (reportName.equals("monthly")) {
                    String monthStr = getQueryParam(exchange, "month");
                    String yearStr  = getQueryParam(exchange, "year");
                    if (monthStr == null || yearStr == null) {
                        sendError(exchange, 400, "Both 'month' and 'year' query parameters are required");
                        return;
                    }
                    int month = Integer.parseInt(monthStr);
                    int year  = Integer.parseInt(yearStr);

                    Map<String, Double> summary = service.getMonthlySummary(month, year);
                    StringBuilder sb = new StringBuilder("{");
                    boolean first = true;
                    for (Map.Entry<String, Double> entry : summary.entrySet()) {
                        if (!first) sb.append(",");
                        sb.append(jsonString(entry.getKey())).append(":").append(entry.getValue());
                        first = false;
                    }
                    sb.append("}");
                    sendJson(exchange, 200, sb.toString());
                    return;
                }

                sendError(exchange, 404, "Unknown report: " + reportName);

            } catch (NumberFormatException e) {
                sendError(exchange, 400, "Invalid number format in parameters");
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }
}
