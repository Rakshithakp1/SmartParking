
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/admin-slots")
public class AdminSlotsServlet extends HttpServlet {

    private String safe(String value) {
        if (value == null) return "";
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

        PrintWriter out = response.getWriter();

        int totalSlots = 0;
        int availableSlots = 0;
        int occupiedSlots = 0;

        StringBuilder rows = new StringBuilder();

        try {
            Connection con = DBConnection.getConnection();

            String sql = "SELECT slot_id, slot_number, floor_level, "
                    + "slot_type, status, price_per_hour "
                    + "FROM parking_slots ORDER BY slot_id ASC";

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    totalSlots++;

                    int slotId = rs.getInt("slot_id");

                    String slotNumber = safe(rs.getString("slot_number"));
                    String floor = safe(rs.getString("floor_level"));
                    String type = safe(rs.getString("slot_type"));
                    String status = safe(rs.getString("status"));

                    String price = rs.getBigDecimal("price_per_hour") == null
                            ? "0.00"
                            : rs.getBigDecimal("price_per_hour").toPlainString();

                    boolean available =
                            "Available".equalsIgnoreCase(status)
                            || "FREE".equalsIgnoreCase(status);

                    String statusClass;
                    String displayStatus;

                    if (available) {
                        availableSlots++;
                        statusClass = "available";
                        displayStatus = "AVAILABLE";
                    } else {
                        occupiedSlots++;
                        statusClass = "occupied";
                        displayStatus = "BOOKED";
                    }

                    rows.append("<tr class='slot-row'>");

                    rows.append("<td><b>#")
                        .append(slotId).append("</b></td>");

                    rows.append("<td><span class='slot-number'>")
                        .append(slotNumber).append("</span></td>");

                    rows.append("<td>").append(floor).append("</td>");

                    rows.append("<td><span class='type'>")
                        .append(type).append("</span></td>");

                    rows.append("<td class='price'>Rs. ")
                        .append(price).append("</td>");

                    rows.append("<td><span class='status ")
                        .append(statusClass).append("'>")
                        .append(displayStatus).append("</span></td>");

                    rows.append("</tr>");
                }
            }

            con.close();

        } catch (Exception e) {
            rows.append("<tr><td colspan='6' class='error'>")
                .append("Error loading parking slots: ")
                .append(safe(e.getMessage()))
                .append("</td></tr>");
        }

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>SmartPark - Manage Slots</title>");

        out.println("<style>");

        out.println("*{box-sizing:border-box;margin:0;padding:0;}");
        out.println("body{font-family:'Segoe UI',Arial,sans-serif;background:#f3f7fa;color:#172b3a;}");
        out.println(".navbar{background:#101f32;color:white;padding:20px 5%;display:flex;justify-content:space-between;align-items:center;}");
        out.println(".brand{font-size:25px;font-weight:800;letter-spacing:2px;color:#27d6c0;}");
        out.println(".navtag{font-size:12px;letter-spacing:2px;color:#b8c8d8;}");
        out.println(".hero{background:linear-gradient(120deg,#10243b,#087e83);padding:38px 5%;color:white;}");
        out.println(".crumb{font-size:12px;letter-spacing:2px;color:#a8e9df;margin-bottom:14px;}");
        out.println(".hero h1{font-size:34px;margin-bottom:10px;}");
        out.println(".hero p{color:#d5e9ed;font-size:15px;}");
        out.println(".container{width:90%;max-width:1400px;margin:30px auto;}");
        out.println(".stats{display:grid;grid-template-columns:repeat(3,1fr);gap:18px;margin-bottom:35px;}");
        out.println(".card{background:white;padding:24px;border-radius:15px;box-shadow:0 5px 22px #18334b12;border:1px solid #e7edf2;}");
        out.println(".card-icon{font-size:25px;margin-bottom:13px;}");
        out.println(".card-label{color:#748494;font-size:13px;font-weight:600;margin-bottom:10px;}");
        out.println(".card h2{font-size:30px;color:#142c42;}");
        out.println(".section-head{display:flex;justify-content:space-between;align-items:center;gap:15px;flex-wrap:wrap;margin-bottom:20px;}");
        out.println(".section-head h2{font-size:22px;color:#142c42;}");
        out.println(".search{padding:12px 16px;border:1px solid #dce5eb;border-radius:9px;width:290px;font-size:14px;outline:none;}");
        out.println(".search:focus{border-color:#0ba99b;}");
        out.println(".table-wrap{background:white;border-radius:15px;overflow-x:auto;box-shadow:0 5px 22px #18334b12;border:1px solid #e7edf2;}");
        out.println("table{width:100%;border-collapse:collapse;min-width:750px;}");
        out.println("th{background:#142c42;color:#dff8f5;padding:18px 15px;text-align:left;font-size:12px;letter-spacing:.7px;}");
        out.println("td{padding:18px 15px;border-bottom:1px solid #edf1f4;font-size:14px;color:#405366;}");
        out.println("tr:hover td{background:#f5fbfa;}");
        out.println(".slot-number{background:#e8f4f7;color:#087e83;padding:8px 13px;border-radius:7px;font-weight:800;}");
        out.println(".type{background:#edf4f8;color:#38536a;padding:7px 12px;border-radius:20px;font-size:12px;font-weight:600;}");
        out.println(".price{font-weight:800;color:#087e83;}");
        out.println(".status{padding:8px 13px;border-radius:20px;font-size:11px;font-weight:800;}");
        out.println(".available{background:#dff7e9;color:#16804a;}");
        out.println(".occupied{background:#fff0d9;color:#a56a00;}");
        out.println(".error{color:#c0392b;padding:20px;}");
        out.println(".footer{display:flex;justify-content:space-between;align-items:center;gap:15px;flex-wrap:wrap;margin-top:25px;padding:22px 0;color:#82909d;font-size:12px;}");
        out.println(".back{background:#102b40;color:white;text-decoration:none;padding:12px 20px;border-radius:8px;font-weight:600;}");
        out.println(".back:hover{background:#087e83;}");
        out.println(".empty{padding:30px;text-align:center;color:#8493a0;}");
        out.println("@media(max-width:700px){.stats{grid-template-columns:1fr;}.hero h1{font-size:27px;}.container{width:94%;}}");

        out.println("</style></head><body>");

        out.println("<div class='navbar'>");
        out.println("<div class='brand'>SMARTPARK</div>");
        out.println("<div class='navtag'>ADMIN PANEL</div>");
        out.println("</div>");

        out.println("<div class='hero'>");
        out.println("<div class='crumb'>ADMINISTRATION / PARKING SLOTS</div>");
        out.println("<h1>Manage Parking Slots</h1>");
        out.println("<p>Monitor parking availability, slot categories and parking charges.</p>");
        out.println("</div>");

        out.println("<div class='container'>");

        out.println("<div class='stats'>");

        out.println("<div class='card'><div class='card-icon'>P</div>");
        out.println("<div class='card-label'>TOTAL PARKING SLOTS</div>");
        out.println("<h2>" + totalSlots + "</h2></div>");

        out.println("<div class='card'><div class='card-icon'>✓</div>");
        out.println("<div class='card-label'>AVAILABLE SLOTS</div>");
        out.println("<h2>" + availableSlots + "</h2></div>");

        out.println("<div class='card'><div class='card-icon'>●</div>");
        out.println("<div class='card-label'>OCCUPIED SLOTS</div>");
        out.println("<h2>" + occupiedSlots + "</h2></div>");

        out.println("</div>");

        out.println("<div class='section-head'>");
        out.println("<h2>Parking Slot Details</h2>");
        out.println("<input class='search' id='searchBox' type='text' placeholder='Search slot, floor, type, status...'>");
        out.println("</div>");

        out.println("<div class='table-wrap'><table>");
        out.println("<thead><tr>");
        out.println("<th>SLOT ID</th><th>SLOT NUMBER</th>");
        out.println("<th>FLOOR</th><th>SLOT TYPE</th>");
        out.println("<th>PRICE / HOUR</th><th>STATUS</th>");
        out.println("</tr></thead><tbody>");

        if (rows.length() == 0) {
            out.println("<tr><td colspan='6' class='empty'>No parking slots found.</td></tr>");
        } else {
            out.println(rows.toString());
        }

        out.println("</tbody></table></div>");

        out.println("<div class='footer'>");
        out.println("<a class='back' href='admin.html'>BACK TO ADMIN DASHBOARD</a>");
        out.println("<span>SMARTPARK MANAGEMENT SYSTEM<br>Smart Parking. Easy Living.</span>");
        out.println("</div>");

        out.println("</div>");

        out.println("<script>");
        out.println("document.getElementById('searchBox').addEventListener('input',function(){");
        out.println("var q=this.value.toLowerCase();");
        out.println("document.querySelectorAll('.slot-row').forEach(function(row){");
        out.println("row.style.display=row.innerText.toLowerCase().includes(q)?'':'none';");
        out.println("});");
        out.println("});");
        out.println("</script>");

        out.println("</body></html>");
    }
}
