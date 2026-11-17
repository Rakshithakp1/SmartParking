
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.getWriter().println("Please login first!");
            return;
        }

        try {
            int userId = (Integer) session.getAttribute("userId");

            int bookingId = Integer.parseInt(
                request.getParameter("bookingId")
            );

            BigDecimal amount = new BigDecimal(
                request.getParameter("amount")
            );

            String method = request.getParameter("paymentMethod");

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                response.getWriter().println("Invalid payment amount!");
                return;
            }

            if (!"UPI".equals(method) &&
                !"Card".equals(method) &&
                !"Cash".equals(method)) {

                response.getWriter().println("Invalid payment method!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {

                con.setAutoCommit(false);

                try {
                    // Check booking and get actual amount
                    String checkSql =
                        "SELECT total_amount FROM bookings " +
                        "WHERE booking_id = ? AND user_id = ? FOR UPDATE";

                    BigDecimal actualAmount = null;

                    try (PreparedStatement ps =
                             con.prepareStatement(checkSql)) {

                        ps.setInt(1, bookingId);
                        ps.setInt(2, userId);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                actualAmount = rs.getBigDecimal("total_amount");
                            }
                        }
                    }

                    if (actualAmount == null) {
                        con.rollback();
                        response.getWriter().println("Invalid booking!");
                        return;
                    }

                    // Verify amount
                    if (amount.compareTo(actualAmount) != 0) {
                        con.rollback();
                        response.getWriter().println(
                            "Payment Failed! Amount does not match booking amount."
                        );
                        return;
                    }

                    // Prevent duplicate payment
                    String duplicateSql =
                        "SELECT payment_id FROM payments " +
                        "WHERE booking_id = ?";

                    try (PreparedStatement ps =
                             con.prepareStatement(duplicateSql)) {

                        ps.setInt(1, bookingId);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                con.rollback();
                                response.getWriter().println(
                                    "Payment record already exists for this booking!"
                                );
                                return;
                            }
                        }
                    }

                    // Save payment
                    String insertSql =
                        "INSERT INTO payments " +
                        "(booking_id, user_id, amount, payment_method, payment_status) " +
                        "VALUES (?, ?, ?, ?, 'Pending')";

                    try (PreparedStatement ps =
                             con.prepareStatement(insertSql)) {

                        ps.setInt(1, bookingId);
                        ps.setInt(2, userId);
                        ps.setBigDecimal(3, amount);
                        ps.setString(4, method);

                        ps.executeUpdate();
                    }

                    con.commit();

                    response.getWriter().println(
                        "<h2>Payment Record Saved Successfully!</h2>" +
                        "<p>Booking ID: " + bookingId + "</p>" +
                        "<p>Amount: Rs. " + amount + "</p>" +
                        "<p>Payment Method: " + method + "</p>" +
                        "<p>Payment Status: Pending</p>" +
                        "<a href='customer-dashboard.html'>Go to Dashboard</a>"
                    );

                } catch (Exception e) {
                    con.rollback();
                    throw e;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println(
                "Payment Failed! Please check the details."
            );
        }
    }
}
