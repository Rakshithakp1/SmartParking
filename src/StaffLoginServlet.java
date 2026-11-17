

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

@WebServlet("/staff-login")
public class StaffLoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String sql = "SELECT * FROM staff WHERE email = ? AND password = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                response.getWriter().println("Database Connection Failed!");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, email);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        HttpSession session = request.getSession();

                        session.setAttribute("staffId",
                                rs.getInt("staff_id"));

                        session.setAttribute("staffName",
                                rs.getString("name"));

                        response.sendRedirect("staff-dashboard.html?name=" +
    java.net.URLEncoder.encode(rs.getString("name"), "UTF-8"));

                    } else {

                        response.getWriter().println(
                            "<h2>Invalid Staff Email or Password!</h2>" +
                            "<a href='staff-login.html'>Try Again</a>"
                        );
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().println(
                "Staff Login Failed! Please try again."
            );
        }
    }
}
