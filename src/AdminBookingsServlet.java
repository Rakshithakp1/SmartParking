
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

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

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

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            response.getWriter().println(
                "<html><head><title>Admin Bookings</title>" +
                "<style>" +
                "body{font-family:Arial;background:#eef3f7;padding:25px;}" +
                "h1{text-align:center;color:#073b4c;}" +
                "table{width:100%;border-collapse:collapse;background:white;}" +
                "th,td{padding:12px;border:1px solid #ddd;text-align:center;}" +
                "th{background:#087e8b;color:white;}" +
                "a{display:inline-block;margin-top:20px;color:#087e8b;}" +
                "</style></head><body>"
            );

            response.getWriter().println("<h1>All Customer Bookings</h1>");
            response.getWriter().println("<table>");

            response.getWriter().println(
                "<tr><th>Booking ID</th><th>Customer</th>" +
                "<th>Slot</th><th>Vehicle Number</th>" +
                "<th>Booking Date</th><th>Start Time</th>" +
                "<th>End Time</th><th>Amount</th><th>Status</th></tr>"
            );

            boolean found = false;

            while (rs.next()) {
                found = true;

                response.getWriter().println("<tr>");

                for (int i = 1; i <= 9; i++) {
                    String value = rs.getString(i);
                    response.getWriter().println(
                        "<td>" + (value == null ? "-" : value) + "</td>"
                    );
                }

                response.getWriter().println("</tr>");
            }

            if (!found) {
                response.getWriter().println(
                    "<tr><td colspan='9'>No bookings found</td></tr>"
                );
            }

            response.getWriter().println("</table>");
            response.getWriter().println(
                "<a href='admin.html'>Back to Admin Dashboard</a>"
            );
            response.getWriter().println("</body></html>");

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database Error: " + e.getMessage());
        }
    }
}
