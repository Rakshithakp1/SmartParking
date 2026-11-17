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

@WebServlet("/admin-payments")
public class AdminPaymentsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        out.println("<html><head><title>Admin Payment Management</title>");

        out.println("<style>");
        out.println("body{font-family:Arial;background:#eef4f7;padding:25px;}");
        out.println("h1{text-align:center;color:#073b4c;}");
        out.println("table{width:100%;border-collapse:collapse;background:white;}");
        out.println("th,td{padding:12px;border:1px solid #ddd;text-align:center;}");
        out.println("th{background:#087e8b;color:white;}");
        out.println("tr:nth-child(even){background:#f2f2f2;}");
        out.println("a{display:inline-block;margin-top:20px;color:#087e8b;}");
        out.println("</style></head><body>");

        out.println("<h1>Admin - Payment Management</h1>");

        out.println("<table>");
        out.println("<tr>");
        out.println("<th>Payment ID</th>");
        out.println("<th>Booking ID</th>");
        out.println("<th>Customer ID</th>");
        out.println("<th>Customer Name</th>");
        out.println("<th>Amount</th>");
        out.println("<th>Method</th>");
        out.println("<th>Status</th>");
        out.println("<th>Date</th>");
        out.println("</tr>");

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

                out.println("<tr>");

                out.println("<td>" + rs.getInt("payment_id") + "</td>");
                out.println("<td>" + rs.getInt("booking_id") + "</td>");
                out.println("<td>" + rs.getInt("user_id") + "</td>");
                out.println("<td>" + rs.getString("name") + "</td>");
                out.println("<td>Rs. " + rs.getBigDecimal("amount") + "</td>");
                out.println("<td>" + rs.getString("payment_method") + "</td>");
                out.println("<td>" + rs.getString("payment_status") + "</td>");
                out.println("<td>" + rs.getTimestamp("payment_date") + "</td>");

                out.println("</tr>");
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            out.println("<tr><td colspan='8'>");
            out.println("Error loading payments: " + e.getMessage());
            out.println("</td></tr>");
        }

        out.println("</table>");

        out.println("<center>");
        out.println("<a href='admin.html'>Back to Admin Dashboard</a>");
        out.println("</center>");

        out.println("</body></html>");
    }
}