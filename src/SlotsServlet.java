
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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        StringBuilder json = new StringBuilder();
        json.append("[");

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                response.setStatus(500);
                out.print("{\"error\":\"Database connection failed\"}");
                return;
            }

            String sql = "SELECT slot_no, status FROM slots ORDER BY slot_no";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{");
                json.append("\"slot_no\":").append(rs.getInt("slot_no")).append(",");
                json.append("\"status\":\"")
                    .append(rs.getString("status")).append("\"");
                json.append("}");

                first = false;
            }

            json.append("]");
            out.print(json.toString());

            rs.close();
            ps.close();

        } catch (Exception e) {
            response.setStatus(500);
            out.print("{\"error\":\"Unable to fetch parking slots\"}");
            e.printStackTrace();
        }
    }
}

