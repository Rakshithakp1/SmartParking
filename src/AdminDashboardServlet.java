
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

@WebServlet("/admin-stats")
public class AdminDashboardServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("adminId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Admin login required\"}");
            return;
        }

        String sql =
            "SELECT " +
            "(SELECT COUNT(*) FROM slots) AS totalSlots, " +
            "(SELECT COUNT(*) FROM slots WHERE LOWER(status)='free') AS availableSlots, " +
            "(SELECT COUNT(*) FROM slots WHERE LOWER(status)='booked') AS occupiedSlots, " +
            "(SELECT COUNT(*) FROM bookings) AS totalBookings";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String json = "{"
                    + "\"totalSlots\":" + rs.getInt("totalSlots") + ","
                    + "\"availableSlots\":" + rs.getInt("availableSlots") + ","
                    + "\"occupiedSlots\":" + rs.getInt("occupiedSlots") + ","
                    + "\"totalBookings\":" + rs.getInt("totalBookings")
                    + "}";

                response.getWriter().write(json);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Database error\"}");
        }
    }
}
