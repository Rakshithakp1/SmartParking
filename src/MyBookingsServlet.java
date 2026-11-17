
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

@WebServlet("/mybookings")
public class MyBookingsServlet extends HttpServlet {

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

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT booking_id, slot_id, vehicle_number, " +
                 "booking_date, start_time, end_time, total_amount, status " +
                 "FROM bookings WHERE user_id = ? ORDER BY booking_id DESC")) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                PrintWriter out = response.getWriter();

                out.println("<html><head><title>My Bookings</title>");
                out.println("<style>");
                out.println("body{font-family:Arial;background:#f4f6f9;padding:30px;}");
                out.println("h2{text-align:center;color:#183b56;}");
                out.println("table{width:100%;border-collapse:collapse;background:white;}");
                out.println("th,td{padding:12px;border:1px solid #ddd;text-align:center;}");
                out.println("th{background:#183b56;color:white;}");
                out.println("a{display:inline-block;margin-top:20px;color:#183b56;}");
                out.println(".status{padding:6px 12px;border-radius:15px;font-weight:bold;}");
                out.println(".pending{background:#fff3cd;color:#856404;}");
                out.println(".confirmed{background:#d4edda;color:#155724;}");
                out.println(".completed{background:#cce5ff;color:#004085;}");
                out.println(".cancelled{background:#f8d7da;color:#721c24;}");
                out.println(".cancel-btn{background:#dc3545;color:white;border:0;padding:8px 12px;border-radius:5px;cursor:pointer;}");
                out.println("</style></head><body>");

                out.println("<h2>My Parking Bookings</h2>");
                out.println("<table>");
                out.println("<tr><th>Booking ID</th><th>Slot</th>");
                out.println("<th>Vehicle</th><th>Date</th><th>Start Time</th>");
                out.println("<th>End Time</th><th>Amount</th><th>Status</th>");
                out.println("<th>Action</th></tr>");

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    int bookingId = rs.getInt("booking_id");
                    String status = rs.getString("status");
                    String statusClass = status.toLowerCase();

                    if (!statusClass.equals("pending") &&
                        !statusClass.equals("confirmed") &&
                        !statusClass.equals("completed") &&
                        !statusClass.equals("cancelled")) {
                        statusClass = "pending";
                    }

                    out.println("<tr>");
                    out.println("<td>" + bookingId + "</td>");
                    out.println("<td>" + rs.getInt("slot_id") + "</td>");
                    out.println("<td>" + rs.getString("vehicle_number") + "</td>");
                    out.println("<td>" + rs.getDate("booking_date") + "</td>");
                    out.println("<td>" + rs.getTime("start_time") + "</td>");
                    out.println("<td>" + rs.getTime("end_time") + "</td>");
                    out.println("<td>Rs. " + rs.getBigDecimal("total_amount") + "</td>");

                    out.println("<td><span class='status " + statusClass + "'>" +
                                status + "</span></td>");

                    out.println("<td>");

                    if (status.equalsIgnoreCase("pending") ||
                        status.equalsIgnoreCase("confirmed")) {

                        out.println("<form action='customer-cancel' method='post' " +
                                    "onsubmit=\"return confirm('Are you sure you want to cancel this booking?');\">");

                        out.println("<input type='hidden' name='booking_id' value='" +
                                    bookingId + "'>");

                        out.println("<button class='cancel-btn' type='submit'>Cancel Booking</button>");
                        out.println("</form>");

                    } else {
                        out.println("Not Available");
                    }

                    out.println("</td>");
                    out.println("</tr>");
                }

                if (!found) {
                    out.println("<tr><td colspan='9'>No bookings found!</td></tr>");
                }

                out.println("</table>");
                out.println("<a href='customer-dashboard.html'>Back to Dashboard</a>");
                out.println("</body></html>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Unable to load bookings!");
        }
    }
}
