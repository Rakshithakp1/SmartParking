
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

@WebServlet("/payment-history")
public class PaymentHistoryServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.getWriter().println("Please login first!");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        try (PrintWriter out = response.getWriter();
             Connection con = DBConnection.getConnection()) {

            String sql = "SELECT payment_id, booking_id, amount, " +
                    "payment_method, payment_status, payment_date " +
                    "FROM payments WHERE user_id = ? " +
                    "ORDER BY payment_id DESC";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            out.println("<html><head><title>Payment History</title>");
            out.println("<style>");
            out.println("body{font-family:Arial;background:#eaf5f5;padding:30px;}");
            out.println("h2{text-align:center;color:#087e8b;}");
            out.println("table{width:100%;background:white;border-collapse:collapse;}");
            out.println("th,td{padding:12px;border:1px solid #ccc;text-align:center;}");
            out.println("th{background:#087e8b;color:white;}");
            out.println("a{display:block;text-align:center;margin:20px;}");
            out.println("</style></head><body>");

            out.println("<h2>My Payment History</h2>");
            out.println("<table>");
            out.println("<tr><th>Payment ID</th><th>Booking ID</th>");
            out.println("<th>Amount</th><th>Method</th>");
            out.println("<th>Status</th><th>Date</th></tr>");

            boolean found = false;

            while (rs.next()) {
                found = true;

                out.println("<tr>");
                out.println("<td>" + rs.getInt("payment_id") + "</td>");
                out.println("<td>" + rs.getInt("booking_id") + "</td>");
                out.println("<td>Rs. " + rs.getBigDecimal("amount") + "</td>");
                out.println("<td>" + rs.getString("payment_method") + "</td>");
                out.println("<td>" + rs.getString("payment_status") + "</td>");
                out.println("<td>" + rs.getTimestamp("payment_date") + "</td>");
                out.println("</tr>");
            }

            if (!found) {
                out.println("<tr><td colspan='6'>No Payments Found</td></tr>");
            }

            out.println("</table>");
            out.println("<a href='customer-dashboard.html'>Back to Dashboard</a>");
            out.println("</body></html>");

            ps.close();
            rs.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Unable to load payment history.");
        }
    }
}