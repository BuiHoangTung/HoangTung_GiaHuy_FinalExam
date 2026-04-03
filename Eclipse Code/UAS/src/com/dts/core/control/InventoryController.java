package com.dts.core.control;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class InventoryController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String SESSION_KEY = "inventoryItems";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = safe(req.getParameter("action"));

        if (action.isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "list":
                showAllItems(req, res);
                break;
            case "search":
                searchItems(req, res);
                break;
            case "lowstock":
                showLowStockItems(req, res);
                break;
            case "detail":
                showItemDetail(req, res);
                break;
            case "delete":
                deleteItem(req, res);
                break;
            case "stats":
                showStatistics(req, res);
                break;
            default:
                writeMessage(res, "Invalid action: " + action);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = safe(req.getParameter("action"));

        if (action.isEmpty()) {
            action = "add";
        }

        switch (action) {
            case "add":
                addItem(req, res);
                break;
            case "update":
                updateItem(req, res);
                break;
            case "restock":
                restockItem(req, res);
                break;
            default:
                writeMessage(res, "Unsupported POST action: " + action);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Item> getInventory(HttpServletRequest req) {
        HttpSession session = req.getSession();
        Object data = session.getAttribute(SESSION_KEY);

        if (data == null) {
            List<Item> items = new ArrayList<>();
            items.add(new Item(1, "Laptop Dell", "Electronics", 15, 850.0));
            items.add(new Item(2, "Wireless Mouse", "Accessories", 8, 25.5));
            items.add(new Item(3, "Mechanical Keyboard", "Accessories", 4, 59.9));
            items.add(new Item(4, "Monitor 24 inch", "Electronics", 12, 180.0));
            items.add(new Item(5, "USB-C Cable", "Accessories", 30, 8.5));

            session.setAttribute(SESSION_KEY, items);
            return items;
        }

        return (List<Item>) data;
    }

    private void showAllItems(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<Item> items = getInventory(req);

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        out.println("<html><head><title>Inventory List</title></head><body>");
        out.println("<h1>Inventory List</h1>");
        out.println("<p><b>Total items:</b> " + items.size() + "</p>");
        out.println("<p>");
        out.println("<a href='?action=stats'>View Statistics</a> | ");
        out.println("<a href='?action=lowstock'>Low Stock Items</a>");
        out.println("</p>");

        out.println("<form method='get' action='InventoryController'>");
        out.println("<input type='hidden' name='action' value='search'/>");
        out.println("<input type='text' name='keyword' placeholder='Search by name'/>");
        out.println("<button type='submit'>Search</button>");
        out.println("</form>");

        out.println("<h2>Add New Item</h2>");
        out.println("<form method='post' action='InventoryController'>");
        out.println("<input type='hidden' name='action' value='add'/>");
        out.println("Name: <input type='text' name='name'/><br/><br/>");
        out.println("Category: <input type='text' name='category'/><br/><br/>");
        out.println("Quantity: <input type='number' name='quantity'/><br/><br/>");
        out.println("Price: <input type='text' name='price'/><br/><br/>");
        out.println("<button type='submit'>Add Item</button>");
        out.println("</form>");

        out.println("<h2>Current Items</h2>");
        out.println("<table border='1' cellpadding='8' cellspacing='0'>");
        out.println("<tr>");
        out.println("<th>ID</th><th>Name</th><th>Category</th><th>Quantity</th><th>Price</th><th>Actions</th>");
        out.println("</tr>");

        for (Item item : items) {
            out.println("<tr>");
            out.println("<td>" + item.getId() + "</td>");
            out.println("<td>" + item.getName() + "</td>");
            out.println("<td>" + item.getCategory() + "</td>");
            out.println("<td>" + item.getQuantity() + "</td>");
            out.println("<td>" + item.getPrice() + "</td>");
            out.println("<td>"
                    + "<a href='?action=detail&id=" + item.getId() + "'>Detail</a> | "
                    + "<a href='?action=delete&id=" + item.getId() + "' onclick=\"return confirm('Delete this item?')\">Delete</a>"
                    + "</td>");
            out.println("</tr>");
        }

        out.println("</table>");
        out.println("</body></html>");
    }

    private void searchItems(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<Item> items = getInventory(req);
        String keyword = safe(req.getParameter("keyword")).toLowerCase(Locale.ROOT);

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        out.println("<html><head><title>Search Result</title></head><body>");
        out.println("<h1>Search Result</h1>");
        out.println("<p>Keyword: <b>" + keyword + "</b></p>");
        out.println("<p><a href='InventoryController?action=list'>Back to List</a></p>");

        out.println("<table border='1' cellpadding='8' cellspacing='0'>");
        out.println("<tr><th>ID</th><th>Name</th><th>Category</th><th>Quantity</th><th>Price</th></tr>");

        boolean found = false;
        for (Item item : items) {
            if (item.getName().toLowerCase(Locale.ROOT).contains(keyword)) {
                found = true;
                out.println("<tr>");
                out.println("<td>" + item.getId() + "</td>");
                out.println("<td>" + item.getName() + "</td>");
                out.println("<td>" + item.getCategory() + "</td>");
                out.println("<td>" + item.getQuantity() + "</td>");
                out.println("<td>" + item.getPrice() + "</td>");
                out.println("</tr>");
            }
        }

        if (!found) {
            out.println("<tr><td colspan='5'>No items found.</td></tr>");
        }

        out.println("</table>");
        out.println("</body></html>");
    }

    private void showLowStockItems(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<Item> items = getInventory(req);

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        out.println("<html><head><title>Low Stock Items</title></head><body>");
        out.println("<h1>Low Stock Items</h1>");
        out.println("<p><a href='InventoryController?action=list'>Back to List</a></p>");

        out.println("<table border='1' cellpadding='8' cellspacing='0'>");
        out.println("<tr><th>ID</th><th>Name</th><th>Category</th><th>Quantity</th><th>Status</th></tr>");

        boolean found = false;
        for (Item item : items) {
            if (item.getQuantity() <= 5) {
                found = true;
                out.println("<tr>");
                out.println("<td>" + item.getId() + "</td>");
                out.println("<td>" + item.getName() + "</td>");
                out.println("<td>" + item.getCategory() + "</td>");
                out.println("<td>" + item.getQuantity() + "</td>");
                out.println("<td>Low Stock</td>");
                out.println("</tr>");
            }
        }

        if (!found) {
            out.println("<tr><td colspan='5'>No low stock items.</td></tr>");
        }

        out.println("</table>");
        out.println("</body></html>");
    }

    private void showItemDetail(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<Item> items = getInventory(req);
        int id = parseInt(req.getParameter("id"), -1);

        Item target = null;
        for (Item item : items) {
            if (item.getId() == id) {
                target = item;
                break;
            }
        }

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        out.println("<html><head><title>Item Detail</title></head><body>");
        out.println("<h1>Item Detail</h1>");
        out.println("<p><a href='InventoryController?action=list'>Back to List</a></p>");

        if (target == null) {
            out.println("<p>Item not found.</p>");
            out.println("</body></html>");
            return;
        }

        out.println("<p><b>ID:</b> " + target.getId() + "</p>");
        out.println("<p><b>Name:</b> " + target.getName() + "</p>");
        out.println("<p><b>Category:</b> " + target.getCategory() + "</p>");
        out.println("<p><b>Quantity:</b> " + target.getQuantity() + "</p>");
        out.println("<p><b>Price:</b> " + target.getPrice() + "</p>");

        out.println("<h2>Update Item</h2>");
        out.println("<form method='post' action='InventoryController'>");
        out.println("<input type='hidden' name='action' value='update'/>");
        out.println("<input type='hidden' name='id' value='" + target.getId() + "'/>");
        out.println("Name: <input type='text' name='name' value='" + target.getName() + "'/><br/><br/>");
        out.println("Category: <input type='text' name='category' value='" + target.getCategory() + "'/><br/><br/>");
        out.println("Quantity: <input type='number' name='quantity' value='" + target.getQuantity() + "'/><br/><br/>");
        out.println("Price: <input type='text' name='price' value='" + target.getPrice() + "'/><br/><br/>");
        out.println("<button type='submit'>Update Item</button>");
        out.println("</form>");

        out.println("<h2>Restock</h2>");
        out.println("<form method='post' action='InventoryController'>");
        out.println("<input type='hidden' name='action' value='restock'/>");
        out.println("<input type='hidden' name='id' value='" + target.getId() + "'/>");
        out.println("Add Quantity: <input type='number' name='amount' min='1'/><br/><br/>");
        out.println("<button type='submit'>Restock</button>");
        out.println("</form>");

        out.println("</body></html>");
    }

    private void addItem(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<Item> items = getInventory(req);

        String name = safe(req.getParameter("name"));
        String category = safe(req.getParameter("category"));
        int quantity = parseInt(req.getParameter("quantity"), -1);
        double price = parseDouble(req.getParameter("price"), -1);

        if (name.isEmpty() || category.isEmpty() || quantity < 0 || price < 0) {
            writeMessage(res, "Invalid input when adding item. <br/><a href='InventoryController?action=list'>Back</a>");
            return;
        }

        int nextId = getNextId(items);
        Item item = new Item(nextId, name, category, quantity, price);
        items.add(item);

        res.sendRedirect("InventoryController?action=list");
    }

    private void updateItem(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<Item> items = getInventory(req);

        int id = parseInt(req.getParameter("id"), -1);
        String name = safe(req.getParameter("name"));
        String category = safe(req.getParameter("category"));
        int quantity = parseInt(req.getParameter("quantity"), -1);
        double price = parseDouble(req.getParameter("price"), -1);

        if (id < 0 || name.isEmpty() || category.isEmpty() || quantity < 0 || price < 0) {
            writeMessage(res, "Invalid input when updating item.");
            return;
        }

        for (Item item : items) {
            if (item.getId() == id) {
                item.setName(name);
                item.setCategory(category);
                item.setQuantity(quantity);
                item.setPrice(price);
                break;
            }
        }

        res.sendRedirect("InventoryController?action=detail&id=" + id);
    }

    private void restockItem(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<Item> items = getInventory(req);

        int id = parseInt(req.getParameter("id"), -1);
        int amount = parseInt(req.getParameter("amount"), 0);

        if (id < 0 || amount <= 0) {
            writeMessage(res, "Invalid restock amount.");
            return;
        }

        for (Item item : items) {
            if (item.getId() == id) {
                item.setQuantity(item.getQuantity() + amount);
                break;
            }
        }

        res.sendRedirect("InventoryController?action=detail&id=" + id);
    }

    private void deleteItem(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<Item> items = getInventory(req);
        int id = parseInt(req.getParameter("id"), -1);

        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            if (item.getId() == id) {
                iterator.remove();
                break;
            }
        }

        res.sendRedirect("InventoryController?action=list");
    }

    private void showStatistics(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<Item> items = getInventory(req);

        int totalQuantity = 0;
        int lowStockCount = 0;
        double totalValue = 0.0;

        for (Item item : items) {
            totalQuantity += item.getQuantity();
            totalValue += item.getQuantity() * item.getPrice();
            if (item.getQuantity() <= 5) {
                lowStockCount++;
            }
        }

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        out.println("<html><head><title>Inventory Statistics</title></head><body>");
        out.println("<h1>Inventory Statistics</h1>");
        out.println("<p><a href='InventoryController?action=list'>Back to List</a></p>");
        out.println("<p><b>Total product types:</b> " + items.size() + "</p>");
        out.println("<p><b>Total quantity in stock:</b> " + totalQuantity + "</p>");
        out.println("<p><b>Low stock items:</b> " + lowStockCount + "</p>");
        out.println("<p><b>Total inventory value:</b> " + totalValue + "</p>");
        out.println("</body></html>");
    }

    private int getNextId(List<Item> items) {
        int max = 0;
        for (Item item : items) {
            if (item.getId() > max) {
                max = item.getId();
            }
        }
        return max + 1;
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(safe(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(safe(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void writeMessage(HttpServletResponse res, String message) throws IOException {
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        out.println("<html><body>");
        out.println("<p>" + message + "</p>");
        out.println("</body></html>");
    }

    public static class Item {
        private int id;
        private String name;
        private String category;
        private int quantity;
        private double price;

        public Item(int id, String name, String category, int quantity, double price) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.price = price;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}