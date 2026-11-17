
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
    "<!DOCTYPE html>" +
    "<html><head><title>Payment Successful - SmartPark</title>" +
    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
    "<style>" +
    "*{box-sizing:border-box;margin:0;padding:0;}" +
    "body{font-family:Arial,sans-serif;min-height:100vh;" +
    "display:flex;align-items:center;justify-content:center;" +
    "padding:20px;background:linear-gradient(135deg,#071c27,#123e49,#087e83);}" +
    ".card{width:100%;max-width:480px;background:#fff;border-radius:22px;" +
    "overflow:hidden;box-shadow:0 20px 60px rgba(0,0,0,.35);}" +
    ".top{text-align:center;padding:35px 20px;background:linear-gradient(135deg,#103943,#087e83);color:white;}" +
    ".check{width:85px;height:85px;border-radius:50%;background:#d1fae5;" +
    "color:#059669;display:flex;align-items:center;justify-content:center;" +
    "font-size:48px;margin:0 auto 18px;font-weight:bold;}" +
    ".top h1{font-size:25px;margin-bottom:10px;}" +
    ".top p{color:#d5eeee;font-size:14px;}" +
    ".amount{font-size:38px;font-weight:bold;margin-top:20px;color:#65f0bd;}" +
    ".body{padding:28px;}" +
    ".body h3{color:#123e49;margin-bottom:20px;font-size:18px;}" +
    ".detail{display:flex;justify-content:space-between;gap:10px;" +
    "padding:14px 0;border-bottom:1px solid #e5eeee;font-size:14px;}" +
    ".detail span{color:#64748b;}" +
    ".detail strong{color:#173d46;text-align:right;}" +
    ".status{color:#d97706!important;background:#fef3c7;" +
    "padding:6px 13px;border-radius:20px;font-size:12px;}" +
    ".btn{display:block;text-align:center;text-decoration:none;" +
    "background:linear-gradient(135deg,#087e83,#10505b);color:white;" +
    "padding:16px;border-radius:12px;margin-top:25px;font-weight:bold;" +
    "letter-spacing:1px;transition:.3s;}" +
    ".btn:hover{transform:translateY(-3px);}" +
    ".footer{text-align:center;color:#94a3b8;font-size:11px;margin-top:22px;}" +
    "</style></head><body>" +
    "<div class='card'>" +
    "<div class='top'>" +
    "<div class='check'>&#10003;</div>" +
    "<h1>PAYMENT SUCCESSFUL!</h1>" +
    "<p>Your payment record has been saved successfully.</p>" +
    "<div class='amount'>Rs. " + amount + "</div>" +
    "</div>" +
    "<div class='body'>" +
    "<h3>Payment Details</h3>" +
    "<div class='detail'><span>Booking ID</span><strong>" + bookingId + "</strong></div>" +
    "<div class='detail'><span>Payment Amount</span><strong>Rs. " + amount + "</strong></div>" +
    "<div class='detail'><span>Payment Method</span><strong>" + method + "</strong></div>" +
    "<div class='detail'><span>Payment Status</span><strong class='status'>PENDING</strong></div>" +
    "<a class='btn' href='customer-dashboard.html'>GO TO MY DASHBOARD &#8594;</a>" +
    "<div class='footer'>SMARTPARK MANAGEMENT SYSTEM<br>Smart Parking. Easy Living.</div>" +
    "</div></div></body></html>"
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
