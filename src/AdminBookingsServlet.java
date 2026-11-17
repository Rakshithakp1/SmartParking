
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/admin-bookings")
public class AdminBookingsServlet extends HttpServlet {

    private String safe(String value) {
        if (value == null) return "-";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("admin-login.html");
            return;
        }

        String sql =
            "SELECT b.booking_id, u.name AS customer_name, " +
            "b.slot_id, b.vehicle_number, b.booking_date, " +
            "b.start_time, b.end_time, b.total_amount, b.status " +
            "FROM bookings b " +
            "JOIN users u ON b.user_id = u.user_id " +
            "ORDER BY b.booking_id DESC";

        StringBuilder rows = new StringBuilder();

        int total = 0;
        int pending = 0;
        int completed = 0;
        int cancelled = 0;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                total++;

                String status = rs.getString("status");
                String normalized = status == null
                    ? "" : status.trim().toLowerCase();

                if (normalized.equals("pending")) pending++;
                else if (normalized.equals("completed")) completed++;
                else if (normalized.equals("cancelled")) cancelled++;

                String badgeClass = "pending";

                if (normalized.equals("completed")) {
                    badgeClass = "completed";
                } else if (normalized.equals("cancelled")) {
                    badgeClass = "cancelled";
                }

                rows.append("<tr>");

                rows.append("<td><strong class='booking-id'>#")
                    .append(rs.getInt("booking_id"))
                    .append("</strong></td>");

                rows.append("<td><div class='customer'>")
                    .append("<div class='avatar'>")
                    .append(safe(rs.getString("customer_name"))
                        .substring(0, 1).toUpperCase())
                    .append("</div><strong>")
                    .append(safe(rs.getString("customer_name")))
                    .append("</strong></div></td>");

                rows.append("<td><span class='slot'>PARKING ")
                    .append(rs.getInt("slot_id"))
                    .append("</span></td>");

                rows.append("<td>")
                    .append(safe(rs.getString("vehicle_number")))
                    .append("</td>");

                rows.append("<td>")
                    .append(safe(rs.getString("booking_date")))
                    .append("</td>");

                rows.append("<td>")
                    .append(safe(rs.getString("start_time")))
                    .append("</td>");

                rows.append("<td>")
                    .append(safe(rs.getString("end_time")))
                    .append("</td>");

                rows.append("<td><strong class='amount'>Rs. ")
                    .append(safe(rs.getString("total_amount")))
                    .append("</strong></td>");

                rows.append("<td><span class='badge ")
                    .append(badgeClass)
                    .append("'>")
                    .append(safe(status))
                    .append("</span></td>");

                rows.append("</tr>");
            }

            if (total == 0) {
                rows.append("<tr><td colspan='9' class='empty'>")
                    .append("No bookings found</td></tr>");
            }

            response.getWriter().println(
                "<!DOCTYPE html><html><head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Admin Bookings - SmartPark</title>" +

                "<style>" +

                "*{margin:0;padding:0;box-sizing:border-box;}" +

                "body{font-family:Arial,sans-serif;" +
                "background:#f1f5f9;color:#18343d;min-height:100vh;}" +

                ".navbar{background:#102c35;padding:20px 5%;" +
                "display:flex;justify-content:space-between;" +
                "align-items:center;color:white;}" +

                ".brand{font-size:23px;font-weight:bold;letter-spacing:2px;}" +
                ".brand span{color:#39d6c0;}" +

                ".admin-tag{background:#174b51;padding:9px 16px;" +
                "border-radius:20px;color:#8ff5dd;font-size:12px;" +
                "font-weight:bold;}" +

                ".container{max-width:1400px;margin:35px auto;padding:0 22px;}" +

                ".heading{margin-bottom:28px;}" +
                ".heading small{color:#078b88;font-weight:bold;" +
                "letter-spacing:2px;}" +
                ".heading h1{font-size:30px;margin:10px 0;color:#123b45;}" +
                ".heading p{color:#71838c;font-size:14px;}" +

                ".stats{display:grid;grid-template-columns:repeat(4,1fr);" +
                "gap:18px;margin-bottom:30px;}" +

                ".stat{background:white;padding:23px;border-radius:15px;" +
                "box-shadow:0 5px 20px rgba(15,50,60,.06);" +
                "border:1px solid #e5eeee;}" +

                ".stat h2{font-size:32px;margin-bottom:8px;color:#123b45;}" +
                ".stat p{font-size:13px;color:#71838c;}" +

                ".stat.total{border-top:4px solid #0e9694;}" +
                ".stat.pending{border-top:4px solid #e8a32b;}" +
                ".stat.completed{border-top:4px solid #19a66b;}" +
                ".stat.cancelled{border-top:4px solid #e05c5c;}" +

                ".table-card{background:white;border-radius:16px;" +
                "padding:24px;box-shadow:0 5px 25px rgba(15,50,60,.06);}" +

                ".table-heading{display:flex;justify-content:space-between;" +
                "align-items:center;gap:15px;flex-wrap:wrap;margin-bottom:22px;}" +

                ".table-heading h2{font-size:20px;color:#123b45;}" +

                ".search{padding:12px 16px;border:1px solid #d8e4e7;" +
                "border-radius:9px;outline:none;width:260px;font-size:13px;}" +
                ".search:focus{border-color:#078b88;}" +

                ".table-wrap{width:100%;overflow-x:auto;}" +
                "table{width:100%;min-width:1050px;border-collapse:collapse;}" +

                "th{background:#edf5f5;color:#527078;padding:15px 12px;" +
                "text-align:left;font-size:11px;letter-spacing:.5px;" +
                "text-transform:uppercase;}" +

                "td{padding:15px 12px;border-bottom:1px solid #edf1f3;" +
                "font-size:12px;color:#526b73;}" +

                "tr:hover td{background:#f7fbfb;}" +

                ".booking-id{color:#078b88;}" +

                ".customer{display:flex;align-items:center;gap:9px;" +
                "color:#23434c;white-space:nowrap;}" +

                ".avatar{width:32px;height:32px;border-radius:50%;" +
                "background:#d8f3ef;color:#087f7c;display:flex;" +
                "align-items:center;justify-content:center;font-weight:bold;}" +

                ".slot{background:#e9f4f5;color:#087e83;" +
                "padding:7px 10px;border-radius:6px;font-weight:bold;" +
                "font-size:10px;white-space:nowrap;}" +

                ".amount{color:#087e83;white-space:nowrap;}" +

                ".badge{padding:7px 12px;border-radius:20px;" +
                "font-size:10px;font-weight:bold;text-transform:capitalize;" +
                "white-space:nowrap;}" +

                ".badge.pending{background:#fff3d8;color:#b77912;}" +
                ".badge.completed{background:#dff7e9;color:#13864d;}" +
                ".badge.cancelled{background:#ffe5e5;color:#c44747;}" +

                ".empty{text-align:center;padding:35px;color:#71838c;}" +

                ".back{display:inline-block;margin-top:25px;" +
                "padding:13px 22px;background:#103e49;color:white;" +
                "text-decoration:none;border-radius:9px;font-size:13px;" +
                "font-weight:bold;transition:.3s;}" +

                ".back:hover{background:#078b88;transform:translateY(-2px);}" +

                ".footer{text-align:center;padding:28px;color:#91a4aa;" +
                "font-size:11px;}" +

                "@media(max-width:800px){" +
                ".stats{grid-template-columns:repeat(2,1fr);}" +
                ".heading h1{font-size:24px;}" +
                ".container{padding:0 13px;}" +
                ".navbar{padding:17px 5%;}" +
                "}" +

                "@media(max-width:430px){" +
                ".stats{gap:10px;}" +
                ".stat{padding:16px;}" +
                ".stat h2{font-size:26px;}" +
                ".search{width:100%;}" +
                ".table-heading{align-items:stretch;}" +
                "}" +

                "</style></head><body>" +

                "<div class='navbar'>" +
                "<div class='brand'>SMART<span>PARK</span></div>" +
                "<div class='admin-tag'>ADMIN PANEL</div>" +
                "</div>" +

                "<div class='container'>" +

                "<div class='heading'>" +
                "<small>ADMINISTRATION / BOOKINGS</small>" +
                "<h1>All Customer Bookings</h1>" +
                "<p>Monitor and manage all parking reservations in one place.</p>" +
                "</div>" +

                "<div class='stats'>" +

                "<div class='stat total'><h2>" + total +
                "</h2><p>Total Bookings</p></div>" +

                "<div class='stat pending'><h2>" + pending +
                "</h2><p>Pending Bookings</p></div>" +

                "<div class='stat completed'><h2>" + completed +
                "</h2><p>Completed Bookings</p></div>" +

                "<div class='stat cancelled'><h2>" + cancelled +
                "</h2><p>Cancelled Bookings</p></div>" +

                "</div>" +

                "<div class='table-card'>" +

                "<div class='table-heading'>" +
                "<h2>Recent Bookings</h2>" +
                "<input class='search' id='searchBox' type='text' " +
                "placeholder='Search booking or customer...' " +
                "onkeyup='searchBookings()'>" +
                "</div>" +

                "<div class='table-wrap'>" +
                "<table id='bookingTable'>" +

                "<thead><tr>" +
                "<th>Booking ID</th><th>Customer</th>" +
                "<th>Slot</th><th>Vehicle Number</th>" +
                "<th>Booking Date</th><th>Start Time</th>" +
                "<th>End Time</th><th>Amount</th><th>Status</th>" +
                "</tr></thead>" +

                "<tbody>" + rows.toString() + "</tbody>" +

                "</table></div></div>" +

                "<a class='back' href='admin.html'>&#8592; BACK TO ADMIN DASHBOARD</a>" +

                "</div>" +

                "<div class='footer'>SMARTPARK MANAGEMENT SYSTEM<br>" +
                "Smart Parking. Easy Living.</div>" +

                "<script>" +
                "function searchBookings(){" +
                "var input=document.getElementById('searchBox').value.toLowerCase();" +
                "var rows=document.querySelectorAll('#bookingTable tbody tr');" +
                "rows.forEach(function(row){" +
                "row.style.display=row.innerText.toLowerCase().includes(input)?'':'none';" +
                "});}" +
                "</script>" +

                "</body></html>"
            );

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database Error: " + safe(e.getMessage()));
        }
    }
}
