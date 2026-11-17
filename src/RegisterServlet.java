
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");

        try {
            Connection con = DBConnection.getConnection();

            if (con == null) {
                out.println("Database connection failed!");
                return;
            }

            String sql = "INSERT INTO users (name, email, phone, password) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, password);

            int result = ps.executeUpdate();

            if (result > 0) {
                out.println("<h2>Registration Successful!</h2>");
                out.println("<a href='login.html'>Login Now</a>");
            }

            ps.close();
            con.close();

        } catch (SQLException e) {
            out.println("<h2>Registration Failed!</h2>");
            out.println("<p>Email may already be registered.</p>");
            e.printStackTrace();
        }
    }
}
