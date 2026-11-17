
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

@WebServlet("/slots")
public class SlotsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            String sql =
                "SELECT slot_id AS slot_no, status " +
                "FROM parking_slots ORDER BY slot_id";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    if (!first) {
                        json.append(",");
                    }

                    String dbStatus = rs.getString("status");

                    String status =
                        ("Available".equalsIgnoreCase(dbStatus)
                         || "FREE".equalsIgnoreCase(dbStatus))
                        ? "FREE" : "BOOKED";

                    json.append("{");
                    json.append("\"slot_no\":")
                        .append(rs.getInt("slot_no")).append(",");
                    json.append("\"status\":\"")
                        .append(status).append("\"");
                    json.append("}");

                    first = false;
                }

                json.append("]");
                out.print(json.toString());

            } catch (Exception e) {
                response.setStatus(500);
                out.print("{\"error\":\"Unable to fetch parking slots\"}");
                e.printStackTrace();
            }
        }
    }
}
