
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/admin-payments")
public class AdminPaymentsServlet extends HttpServlet {

    private String safe(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
protected void doPost(HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {

    HttpSession session = request.getSession(false);

    if (session == null || session.getAttribute("adminId") == null) {
        response.sendRedirect("admin-login.html");
        return;
    }

    try {
        int paymentId = Integer.parseInt(
                request.getParameter("paymentId"));

        Connection con = DBConnection.getConnection();

        String sql = "UPDATE payments SET payment_status = 'Completed' "
                + "WHERE payment_id = ? AND payment_status = 'Pending'";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, paymentId);

        ps.executeUpdate();

        ps.close();
        con.close();

    } catch (Exception e) {
        throw new ServletException("Unable to update payment status.", e);
    }

    response.sendRedirect("admin-payments");
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

        int totalPayments = 0;
        int pendingPayments = 0;
        int completedPayments = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        StringBuilder rows = new StringBuilder();

        try {
            Connection con = DBConnection.getConnection();

            String sql = "SELECT p.payment_id, p.booking_id, "
                    + "p.user_id, u.name, p.amount, "
                    + "p.payment_method, p.payment_status, p.payment_date "
                    + "FROM payments p "
                    + "JOIN users u ON p.user_id = u.user_id "
                    + "ORDER BY p.payment_id DESC";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                totalPayments++;

                int paymentId = rs.getInt("payment_id");
                int bookingId = rs.getInt("booking_id");
                int userId = rs.getInt("user_id");

                String name = safe(rs.getString("name"));
                BigDecimal amount = rs.getBigDecimal("amount");
                String method = safe(rs.getString("payment_method"));
                String status = safe(rs.getString("payment_status"));
                String date = safe(String.valueOf(rs.getTimestamp("payment_date")));

                if (amount != null) {
                    totalRevenue = totalRevenue.add(amount);
                }

                if ("Pending".equalsIgnoreCase(status)) {
                    pendingPayments++;
                } else if ("Completed".equalsIgnoreCase(status)
                        || "Success".equalsIgnoreCase(status)) {
                    completedPayments++;
                }

                String badgeClass = "pending";

                if ("Completed".equalsIgnoreCase(status)
                        || "Success".equalsIgnoreCase(status)) {
                    badgeClass = "completed";
                } else if ("Failed".equalsIgnoreCase(status)) {
                    badgeClass = "failed";
                }

                rows.append("<tr class='payment-row'>");

                rows.append("<td><b>#").append(paymentId).append("</b></td>");
                rows.append("<td>#").append(bookingId).append("</td>");
                rows.append("<td>").append(userId).append("</td>");

                rows.append("<td><div class='customer'>")
                    .append("<span class='avatar'>")
                    .append(name.isEmpty() ? "?" : safe(name.substring(0, 1).toUpperCase()))
                    .append("</span><b>").append(name)
                    .append("</b></div></td>");

                rows.append("<td class='amount'>₹")
                    .append(amount == null ? "0.00" : amount.toPlainString())
                    .append("</td>");

                rows.append("<td><span class='method'>")
                    .append(method).append("</span></td>");

                rows.append("<td><span class='status ")
                    .append(badgeClass).append("'>")
                    .append(status).append("</span></td>");
                  if ("Pending".equalsIgnoreCase(status)) {

    rows.append("<td>")
        .append("<form method='post' action='admin-payments'>")
        .append("<input type='hidden' name='paymentId' value='")
        .append(paymentId)
        .append("'>")
        .append("<button type='submit' class='complete-btn'>")
        .append("Mark as Completed")
        .append("</button>")
        .append("</form></td>");

} else {

    rows.append("<td><span class='completed'>✓ Completed</span></td>");

}  

                rows.append("<td class='date'>").append(date).append("</td>");

                rows.append("</tr>");
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            rows.append("<tr><td colspan='8'>")
                .append(safe(e.getMessage()))
                .append("</td></tr>");
        }

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>SmartPark - Admin Payments</title>");

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
        out.println(".container{width:90%;max-width:1450px;margin:30px auto;}");
        out.println(".stats{display:grid;grid-template-columns:repeat(4,1fr);gap:18px;margin-bottom:35px;}");
        out.println(".card{background:white;padding:23px;border-radius:15px;box-shadow:0 5px 22px #18334b12;border:1px solid #e7edf2;}");
        out.println(".card-label{color:#748494;font-size:13px;font-weight:600;margin-bottom:12px;}");
        out.println(".card h2{font-size:29px;color:#142c42;}");
        out.println(".card-icon{font-size:22px;margin-bottom:12px;}");
        out.println(".section-head{display:flex;justify-content:space-between;align-items:center;gap:15px;flex-wrap:wrap;margin-bottom:20px;}");
        out.println(".section-head h2{font-size:22px;color:#142c42;}");
        out.println(".search{padding:12px 16px;border:1px solid #dce5eb;border-radius:9px;width:280px;font-size:14px;outline:none;}");
        out.println(".search:focus{border-color:#0ba99b;}");
        out.println(".table-wrap{background:white;border-radius:15px;overflow-x:auto;box-shadow:0 5px 22px #18334b12;border:1px solid #e7edf2;}");
        out.println("table{width:100%;border-collapse:collapse;min-width:1000px;}");
        out.println("th{background:#142c42;color:#dff8f5;padding:17px 13px;text-align:left;font-size:12px;letter-spacing:.5px;}");
        out.println("td{padding:17px 13px;border-bottom:1px solid #edf1f4;font-size:13px;color:#405366;}");
        out.println("tr:hover td{background:#f5fbfa;}");
        out.println(".customer{display:flex;align-items:center;gap:10px;color:#1c354a;}");
        out.println(".avatar{width:32px;height:32px;border-radius:50%;background:#d8f5ef;color:#087e83;display:inline-flex;align-items:center;justify-content:center;font-weight:bold;}");
        out.println(".amount{font-weight:800;color:#087e83;}");
        out.println(".method{background:#edf4f8;padding:7px 12px;border-radius:20px;font-weight:600;color:#38536a;}");
        out.println(".status{padding:7px 12px;border-radius:20px;font-size:11px;font-weight:800;}");
        out.println(".pending{background:#fff2d8;color:#a56a00;}");
        out.println(".completed{background:#dff7e9;color:#16804a;}");
        out.println(".failed{background:#ffe2e2;color:#c0392b;}");
        out.println(".complete-btn{background:#087e83;color:white;border:none;padding:9px 12px;border-radius:7px;font-size:11px;font-weight:700;cursor:pointer;white-space:nowrap;}");
out.println(".complete-btn:hover{background:#065f63;}");

        out.println(".date{white-space:nowrap;font-size:12px;}");
        out.println(".footer{display:flex;justify-content:space-between;align-items:center;gap:15px;flex-wrap:wrap;margin-top:25px;padding:22px 0;color:#82909d;font-size:12px;}");
        out.println(".back{background:#102b40;color:white;text-decoration:none;padding:12px 20px;border-radius:8px;font-weight:600;}");
        out.println(".back:hover{background:#087e83;}");
        out.println(".empty{padding:30px;text-align:center;color:#8493a0;}");
        out.println("@media(max-width:850px){.stats{grid-template-columns:repeat(2,1fr);}.hero h1{font-size:27px;}}");
        out.println("@media(max-width:500px){.stats{grid-template-columns:1fr 1fr;gap:10px;}.card{padding:15px;}.card h2{font-size:23px;}.container{width:94%;}.search{width:100%;}}");

        out.println("</style></head><body>");

        out.println("<div class='navbar'>");
        out.println("<div class='brand'>SMARTPARK</div>");
        out.println("<div class='navtag'>ADMIN PANEL</div>");
        out.println("</div>");

        out.println("<div class='hero'>");
        out.println("<div class='crumb'>ADMINISTRATION / PAYMENTS</div>");
        out.println("<h1>Payment Management</h1>");
        out.println("<p>Track transactions, monitor revenue and review customer payments.</p>");
        out.println("</div>");

        out.println("<div class='container'>");

        out.println("<div class='stats'>");

        out.println("<div class='card'><div class='card-icon'>💳</div>");
        out.println("<div class='card-label'>TOTAL PAYMENTS</div><h2>" + totalPayments + "</h2></div>");

        out.println("<div class='card'><div class='card-icon'>Rs.</div>");
        out.println("<div class='card-label'>TOTAL PAYMENT AMOUNT</div><h2>₹" + totalRevenue.toPlainString() + "</h2></div>");

        out.println("<div class='card'><div class='card-icon'>⏳</div>");
        out.println("<div class='card-label'>PENDING PAYMENTS</div><h2>" + pendingPayments + "</h2></div>");

        out.println("<div class='card'><div class='card-icon'>✓</div>");
        out.println("<div class='card-label'>COMPLETED PAYMENTS</div><h2>" + completedPayments + "</h2></div>");

        out.println("</div>");

        out.println("<div class='section-head'>");
        out.println("<h2>Recent Payments</h2>");
        out.println("<input class='search' id='searchBox' type='text' placeholder='Search payment, customer, method...'>");
        out.println("</div>");

        out.println("<div class='table-wrap'><table>");
        out.println("<thead><tr>");
        out.println("<th>PAYMENT ID</th><th>BOOKING ID</th><th>CUSTOMER ID</th>");
        out.println("<th>CUSTOMER NAME</th><th>AMOUNT</th><th>METHOD</th>");
        out.println("<th>STATUS</th><th>ACTION</th><th>PAYMENT DATE</th>");
        out.println("</tr></thead><tbody id='paymentTable'>");

        if (rows.length() == 0) {
            out.println("<tr><td colspan='8' class='empty'>No payment records found.</td></tr>");
        } else {
            out.println(rows.toString());
        }

        out.println("</tbody></table></div>");

        out.println("<div class='footer'>");
        out.println("<a class='back' href='admin.html'> BACK TO ADMIN DASHBOARD</a>");
        out.println("<span>SMARTPARK MANAGEMENT SYSTEM<br>Smart Parking. Easy Living.</span>");
        out.println("</div>");

        out.println("</div>");

        out.println("<script>");
        out.println("document.getElementById('searchBox').addEventListener('input',function(){");
        out.println("var q=this.value.toLowerCase();");
        out.println("document.querySelectorAll('.payment-row').forEach(function(row){");
        out.println("row.style.display=row.innerText.toLowerCase().includes(q)?'':'none';");
        out.println("});");
        out.println("});");
        out.println("</script>");

        out.println("</body></html>");
    }
}
