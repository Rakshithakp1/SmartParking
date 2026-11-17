
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

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");

        String title;
        String message;
        String icon;
        String buttonText;
        String buttonLink;
        String color;

        try {
            Connection con = DBConnection.getConnection();

            if (con == null) {
                throw new SQLException("Database connection failed");
            }

            String sql = "INSERT INTO users (name, email, phone, password) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, password);

            int result = ps.executeUpdate();

            ps.close();
            con.close();

            if (result > 0) {
                title = "Registration Successful!";
                message = "Welcome to SmartPark! Your account has been created successfully. You can now log in and book your parking slot.";
                icon = "✓";
                buttonText = "Login Now";
                buttonLink = "login.html";
                color = "#16a34a";
            } else {
                title = "Registration Failed!";
                message = "We could not create your account. Please try again.";
                icon = "!";
                buttonText = "Try Again";
                buttonLink = "register.html";
                color = "#dc2626";
            }

        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {
                title = "Email Already Registered!";
                message = "This email is already linked to an account. Please use another email or log in.";
            } else {
                title = "Registration Failed!";
                message = "Something went wrong. Please try again later.";
            }

            icon = "!";
            buttonText = "Try Again";
            buttonLink = "register.html";
            color = "#dc2626";

            e.printStackTrace();
        }

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>SmartPark - Registration</title>");

        out.println("<style>");
        out.println("*{box-sizing:border-box;margin:0;padding:0;}");
        out.println("body{font-family:Arial,sans-serif;background:linear-gradient(135deg,#e0f2fe,#f0fdfa);min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px;}");
        out.println(".card{background:white;width:100%;max-width:480px;padding:45px 30px;border-radius:20px;text-align:center;box-shadow:0 15px 45px rgba(0,0,0,0.12);}");
        out.println(".icon{width:85px;height:85px;border-radius:50%;background:" + color + ";color:white;font-size:48px;line-height:85px;margin:0 auto 25px;font-weight:bold;}");
        out.println("h1{font-size:27px;color:#123047;margin-bottom:15px;}");
        out.println("p{font-size:16px;line-height:1.7;color:#64748b;margin-bottom:28px;}");
        out.println(".btn{display:inline-block;background:#073b4c;color:white;text-decoration:none;padding:14px 30px;border-radius:9px;font-weight:bold;margin:6px;}");
        out.println(".btn:hover{background:#0e7490;}");
        out.println(".home{background:#e2e8f0;color:#123047;}");
        out.println(".brand{color:#0e7490;font-size:14px;font-weight:bold;margin-bottom:25px;letter-spacing:2px;}");
        out.println("</style>");

        out.println("</head>");
        out.println("<body>");
        out.println("<div class='card'>");
        out.println("<div class='brand'>SMARTPARK</div>");
        out.println("<div class='icon'>" + icon + "</div>");
        out.println("<h1>" + title + "</h1>");
        out.println("<p>" + message + "</p>");
        out.println("<a class='btn' href='" + buttonLink + "'>" + buttonText + "</a>");
        out.println("<a class='btn home' href='index.html'>Home</a>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
