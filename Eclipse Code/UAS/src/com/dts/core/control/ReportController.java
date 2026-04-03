package com.dts.core.control;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReportController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);

    static {
        DATE_FORMAT.setLenient(false);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = trim(req.getParameter("action"));

        if (action == null || action.isEmpty()) {
            action = "home";
        }

        log("ReportController - doGet action = " + action);

        switch (action) {
            case "home":
                showHome(req, res);
                break;

            case "daily":
                prepareDailyReport(req, res);
                break;

            case "monthly":
                prepareMonthlyReport(req, res);
                break;

            case "summary":
                showSummary(req, res);
                break;

            case "export":
                exportCsv(req, res);
                break;

            default:
                req.setAttribute("errorMessage", "Invalid action: " + action);
                forward(req, res, "/Error.jsp");
                break;
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = trim(req.getParameter("action"));

        if (action == null || action.isEmpty()) {
            action = "generate";
        }

        log("ReportController - doPost action = " + action);

        switch (action) {
            case "generate":
                generateReport(req, res);
                break;

            default:
                req.setAttribute("errorMessage", "Unsupported POST action: " + action);
                forward(req, res, "/Error.jsp");
                break;
        }
    }

    private void showHome(HttpServletRequest req, HttpServletResponse res) throws IOException {
        System.out.println("Hello, world from Bui Hoang Tung");
        System.out.println("Hello, world from Gia Huy");
        System.out.println("Hello, world from new Gia Huy");

        res.sendRedirect("ReportDateSelector.jsp");
    }

    private void prepareDailyReport(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String today = DATE_FORMAT.format(new Date());

        req.setAttribute("reportType", "daily");
        req.setAttribute("fromDate", today);
        req.setAttribute("toDate", today);
        req.setAttribute("message", "Daily report is ready for selection.");

        forward(req, res, "/ReportDateSelector.jsp");
    }

    private void prepareMonthlyReport(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Date now = new Date();
        String currentDate = DATE_FORMAT.format(now);

        req.setAttribute("reportType", "monthly");
        req.setAttribute("fromDate", currentDate);
        req.setAttribute("toDate", currentDate);
        req.setAttribute("message", "Monthly report is ready for selection.");

        forward(req, res, "/ReportDateSelector.jsp");
    }

    private void showSummary(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String fromDate = trim(req.getParameter("fromDate"));
        String toDate = trim(req.getParameter("toDate"));

        if (!isValidDate(fromDate) || !isValidDate(toDate)) {
            req.setAttribute("errorMessage", "Invalid date format. Please use yyyy-MM-dd.");
            forward(req, res, "/Error.jsp");
            return;
        }

        Date from = parseDate(fromDate);
        Date to = parseDate(toDate);

        if (from.after(to)) {
            req.setAttribute("errorMessage", "From date cannot be later than To date.");
            forward(req, res, "/Error.jsp");
            return;
        }

        ReportData reportData = buildDummyReportData(from, to, "summary");

        req.setAttribute("reportType", "summary");
        req.setAttribute("fromDate", fromDate);
        req.setAttribute("toDate", toDate);
        req.setAttribute("totalOrders", reportData.getTotalOrders());
        req.setAttribute("totalRevenue", reportData.getTotalRevenue());
        req.setAttribute("totalProducts", reportData.getTotalProducts());
        req.setAttribute("lowStockItems", reportData.getLowStockItems());
        req.setAttribute("generatedAt", DATE_FORMAT.format(new Date()));

        forward(req, res, "/ReportSummary.jsp");
    }

    private void generateReport(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String reportType = trim(req.getParameter("reportType"));
        String fromDate = trim(req.getParameter("fromDate"));
        String toDate = trim(req.getParameter("toDate"));

        if (reportType == null || reportType.isEmpty()) {
            reportType = "custom";
        }

        if (!isValidDate(fromDate) || !isValidDate(toDate)) {
            req.setAttribute("errorMessage", "Invalid input date. Required format: yyyy-MM-dd.");
            forward(req, res, "/Error.jsp");
            return;
        }

        Date from = parseDate(fromDate);
        Date to = parseDate(toDate);

        if (from.after(to)) {
            req.setAttribute("errorMessage", "Start date must be earlier than or equal to end date.");
            forward(req, res, "/Error.jsp");
            return;
        }

        long days = getDaysBetween(from, to) + 1;

        ReportData reportData = buildDummyReportData(from, to, reportType);

        req.setAttribute("reportType", reportType);
        req.setAttribute("fromDate", fromDate);
        req.setAttribute("toDate", toDate);
        req.setAttribute("numberOfDays", days);
        req.setAttribute("totalOrders", reportData.getTotalOrders());
        req.setAttribute("totalRevenue", reportData.getTotalRevenue());
        req.setAttribute("totalProducts", reportData.getTotalProducts());
        req.setAttribute("lowStockItems", reportData.getLowStockItems());
        req.setAttribute("status", "Report generated successfully.");
        req.setAttribute("generatedBy", "ReportController");
        req.setAttribute("generatedAt", new Date());

        forward(req, res, "/ReportResult.jsp");
    }

    private void exportCsv(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String fromDate = trim(req.getParameter("fromDate"));
        String toDate = trim(req.getParameter("toDate"));
        String reportType = trim(req.getParameter("reportType"));

        if (reportType == null || reportType.isEmpty()) {
            reportType = "custom";
        }

        if (!isValidDate(fromDate) || !isValidDate(toDate)) {
            res.setContentType("text/plain");
            res.getWriter().write("Invalid date format. Please use yyyy-MM-dd.");
            return;
        }

        Date from = parseDate(fromDate);
        Date to = parseDate(toDate);

        if (from.after(to)) {
            res.setContentType("text/plain");
            res.getWriter().write("From date cannot be later than To date.");
            return;
        }

        ReportData reportData = buildDummyReportData(from, to, reportType);

        res.setContentType("text/csv");
        res.setHeader("Content-Disposition", "attachment; filename=report.csv");

        PrintWriter out = res.getWriter();
        out.println("Report Type,From Date,To Date,Total Orders,Total Revenue,Total Products,Low Stock Items");
        out.println(
                reportType + "," +
                fromDate + "," +
                toDate + "," +
                reportData.getTotalOrders() + "," +
                reportData.getTotalRevenue() + "," +
                reportData.getTotalProducts() + "," +
                reportData.getLowStockItems()
        );
        out.flush();
    }

    private ReportData buildDummyReportData(Date from, Date to, String reportType) {
        long days = getDaysBetween(from, to) + 1;

        int totalOrders = (int) (days * 4);
        int totalProducts = (int) (days * 7);
        int lowStockItems = (int) Math.max(1, days / 2);

        double multiplier = 1.0;
        if ("daily".equalsIgnoreCase(reportType)) {
            multiplier = 1.1;
        } else if ("monthly".equalsIgnoreCase(reportType)) {
            multiplier = 1.5;
        } else if ("summary".equalsIgnoreCase(reportType)) {
            multiplier = 1.3;
        }

        double totalRevenue = totalOrders * 125.75 * multiplier;

        return new ReportData(totalOrders, totalRevenue, totalProducts, lowStockItems);
    }

    private boolean isValidDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try {
            DATE_FORMAT.parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private Date parseDate(String value) {
        try {
            return DATE_FORMAT.parse(value);
        } catch (ParseException e) {
            return new Date();
        }
    }

    private long getDaysBetween(Date from, Date to) {
        long diffInMillis = to.getTime() - from.getTime();
        return TimeUnit.MILLISECONDS.toDays(diffInMillis);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private void forward(HttpServletRequest req, HttpServletResponse res, String path)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = req.getRequestDispatcher(path);
        dispatcher.forward(req, res);
    }

    public static class ReportData {
        private int totalOrders;
        private double totalRevenue;
        private int totalProducts;
        private int lowStockItems;

        public ReportData(int totalOrders, double totalRevenue, int totalProducts, int lowStockItems) {
            this.totalOrders = totalOrders;
            this.totalRevenue = totalRevenue;
            this.totalProducts = totalProducts;
            this.lowStockItems = lowStockItems;
        }

        public int getTotalOrders() {
            return totalOrders;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }

        public int getTotalProducts() {
            return totalProducts;
        }

        public int getLowStockItems() {
            return lowStockItems;
        }
    }
}