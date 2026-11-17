
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

@WebServlet("/staff-appointments")
public class StaffAppointmentsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String sql = "SELECT b.booking_id, u.name, b.slot_id, " +
             "b.vehicle_number, b.booking_date, " +
             "b.start_time, b.end_time, b.total_amount, b.status " +
             "FROM bookings b " +
             "JOIN users u ON b.user_id = u.user_id " +
             "ORDER BY b.booking_id DESC";

        out.println("<html><head><title>Staff Bookings</title>");
        out.println("<style>");
        out.println("body{font-family:Arial;background:#eef4f7;padding:25px;}");
        out.println("h2{text-align:center;color:#073b4c;}");
        out.println("table{width:100%;border-collapse:collapse;background:white;}");
        out.println("th,td{padding:12px;border:1px solid #ccc;text-align:center;}");
        out.println("th{background:#073b4c;color:white;}");
        out.println("select,button{padding:7px;margin:3px;}");
        out.println("button{background:#073b4c;color:white;border:0;cursor:pointer;border-radius:4px;}");
        out.println("</style></head><body>");

        out.println("<h2>Customer Parking Bookings</h2>");

        out.println("<table><tr>");
        out.println("<th>Booking ID</th><th>Customer</th>");
        out.println("<th>Slot</th><th>Vehicle</th>");
        out.println("<th>Date</th><th>Start</th><th>End</th>");
        out.println("<th>Amount</th><th>Status</th>");
        out.println("<th>Update Status</th>");
        out.println("</tr>");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("booking_id");
                String currentStatus = rs.getString("status");

                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + rs.getString("name") + "</td>");
                out.println("<td>" + rs.getInt("slot_id") + "</td>");
                out.println("<td>" + rs.getString("vehicle_number") + "</td>");
                out.println("<td>" + rs.getString("booking_date") + "</td>");
                out.println("<td>" + rs.getString("start_time") + "</td>");
                out.println("<td>" + rs.getString("end_time") + "</td>");
                out.println("<td>Rs. " + rs.getBigDecimal("total_amount") + "</td>");
                out.println("<td>" + currentStatus + "</td>");

                out.println("<td>");
                out.println("<form action='staff-update' method='post'>");
                out.println("<input type='hidden' name='booking_id' value='" + id + "'>");
                out.println("<select name='status' required>");

                String[] statuses = {"confirmed", "completed", "cancelled"};

                for (String status : statuses) {
                    if (status.equalsIgnoreCase(currentStatus)) {
                        out.println("<option value='" + status + "' selected>" + status + "</option>");
                    } else {
                        out.println("<option value='" + status + "'>" + status + "</option>");
                    }
                }

                out.println("</select>");
                out.println("<button type='submit'>Update</button>");
                out.println("</form>");
                out.println("</td>");

                out.println("</tr>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<tr><td colspan='10'>Unable to load bookings.</td></tr>");
        }

        out.println("</table>");
        out.println("<br><a href='staff-dashboard.html'>Back to Dashboard</a>");
        out.println("</body></html>");
    }
}
