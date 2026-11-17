
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/customer-cancel")
public class CustomerCancelServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String bookingIdText = request.getParameter("booking_id");

        if (bookingIdText == null || !bookingIdText.matches("\\d+")) {
            response.sendError(400, "Invalid booking ID!");
            return;
        }

        int bookingId = Integer.parseInt(bookingIdText);
        int userId = (Integer) session.getAttribute("userId");

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                String sql = "SELECT slot_id, status FROM bookings " +
                             "WHERE booking_id = ? AND user_id = ? FOR UPDATE";

                int slotId;
                String status;

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, userId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            response.getWriter().println("Booking not found!");
                            return;
                        }

                        slotId = rs.getInt("slot_id");
                        status = rs.getString("status");
                    }
                }

                if (!status.equalsIgnoreCase("pending") &&
                    !status.equalsIgnoreCase("confirmed")) {

                    con.rollback();
                    response.getWriter().println(
                        "This booking cannot be cancelled!");
                    return;
                }

                try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE bookings SET status = 'cancelled' " +
                    "WHERE booking_id = ? AND user_id = ?")) {

                    ps.setInt(1, bookingId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE slots SET status = 'free' WHERE slot_no = ?")) {

                    ps.setInt(1, slotId);
                    ps.executeUpdate();
                }

                con.commit();
                response.sendRedirect("mybookings");

            } catch (Exception e) {
                con.rollback();
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println(
                "Unable to cancel booking!");
        }
    }
}
