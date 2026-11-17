

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/staff-update")
public class StaffUpdateServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String bookingId = request.getParameter("booking_id");
        String status = request.getParameter("status");

        if (bookingId == null ||
            !bookingId.matches("\\d+") ||
            status == null ||
            !(status.equals("confirmed") ||
              status.equals("completed") ||
              status.equals("cancelled"))) {

            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid booking details!");
            return;
        }

        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, Integer.parseInt(bookingId));

            int rows = ps.executeUpdate();

            if (rows > 0) {
                response.sendRedirect("staff-appointments");
            } else {
                response.getWriter().println("Booking not found!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Unable to update booking status!");
        }
    }
}
