
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

@WebServlet("/booking")
public class BookingServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String name = request.getParameter("name");
        String slot = request.getParameter("slot");
        String date = request.getParameter("date");
        String time = request.getParameter("time");
        String vehicle = request.getParameter("vehicle");

        Integer userId = (Integer) request.getSession()
                                      .getAttribute("userId");

        if (userId == null) {
            response.getWriter().println("Please login first!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            int slotId = Integer.parseInt(slot);

            con.setAutoCommit(false);

            // Step 1: Check slot availability
            String checkSql =
                "SELECT status FROM slots WHERE slot_no = ? FOR UPDATE";

            try (PreparedStatement checkPs =
                     con.prepareStatement(checkSql)) {

                checkPs.setInt(1, slotId);

                try (ResultSet rs = checkPs.executeQuery()) {

                    if (!rs.next()) {
                        con.rollback();
                        response.getWriter().println("Invalid Slot!");
                        return;
                    }

                    String status = rs.getString("status");

                    if (!"FREE".equalsIgnoreCase(status)) {
                        con.rollback();

                        response.getWriter().println(
                            "<h2>Booking Failed!</h2>" +
                            "<p>This slot is already BOOKED. Please select another slot.</p>" +
                            "<a href='slots.html'>View Available Slots</a>"
                        );
                        return;
                    }
                }
            }

            // Step 2: Insert booking with Rs.200 amount
            String sql = "INSERT INTO bookings " +
                "(user_id, slot_id, vehicle_number, booking_date, " +
                "start_time, end_time, total_amount, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, 200, 'pending')";

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ps.setInt(2, slotId);
                ps.setString(3, vehicle);
                ps.setString(4, date);
                ps.setString(5, time);
                ps.setString(6, time);

                int result = ps.executeUpdate();

                if (result > 0) {

                    // Step 3: Update slot status
                    String updateSql =
                        "UPDATE slots SET status = 'BOOKED' WHERE slot_no = ?";

                    try (PreparedStatement updatePs =
                             con.prepareStatement(updateSql)) {

                        updatePs.setInt(1, slotId);
                        updatePs.executeUpdate();
                    }

                    con.commit();

                    response.getWriter().println(
                        "<h2>Booking Successful!</h2>" +
                        "<p>Customer: " + name + "</p>" +
                        "<p>Slot: " + slotId + "</p>" +
                        "<p>Vehicle: " + vehicle + "</p>" +
                        "<p>Amount: Rs.200</p>" +
                        "<a href='customer-dashboard.html'>Go to Dashboard</a>"
                    );
                }
            }

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();

            response.getWriter().println(
                "Booking Failed! Please check the booking details and database."
            );
        }
    }
}
