
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.Duration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/booking")
public class BookingServlet extends HttpServlet {

    private String safe(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String name = request.getParameter("name");
        String slot = request.getParameter("slot");
        String date = request.getParameter("date");
        String time = request.getParameter("time");
        String endTime = request.getParameter("endTime");
        String vehicle = request.getParameter("vehicle");

        Integer userId = (Integer) request.getSession()
                                      .getAttribute("userId");

        if (userId == null) {
            response.sendRedirect("login.html");
            return;
        }

        try {
            if (slot == null || date == null || time == null
                    || endTime == null || vehicle == null
                    || slot.isEmpty() || date.isEmpty()
                    || time.isEmpty() || endTime.isEmpty()
                    || vehicle.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Please fill all booking details."
                );
            }

            int slotId = Integer.parseInt(slot);

            LocalTime start = LocalTime.parse(time);
            LocalTime end = LocalTime.parse(endTime);

            if (!end.isAfter(start)) {
                throw new IllegalArgumentException(
                    "End time must be later than starting time."
                );
            }

            long minutes = Duration.between(start, end).toMinutes();

            // Charge for every started hour
            int hours = (int) Math.ceil(minutes / 60.0);

            try (Connection con = DBConnection.getConnection()) {

                if (con == null) {
                    throw new SQLException("Database connection failed.");
                }

                con.setAutoCommit(false);

                try {
                    // Check slot availability and hourly price
                    String checkSql =
                        "SELECT status, price_per_hour FROM parking_slots " +
                        "WHERE slot_id = ? FOR UPDATE";

                    int hourlyPrice;

                    try (PreparedStatement ps =
                             con.prepareStatement(checkSql)) {

                        ps.setInt(1, slotId);

                        try (ResultSet rs = ps.executeQuery()) {

                            if (!rs.next()) {
                                throw new SQLException(
                                    "Invalid parking slot."
                                );
                            }

                            String status = rs.getString("status");

                            if (!"Available".equalsIgnoreCase(status)
                                    && !"FREE".equalsIgnoreCase(status)) {
                                throw new SQLException(
                                    "This parking slot is already booked."
                                );
                            }

                            hourlyPrice = rs.getInt("price_per_hour");
                        }
                    }

                    if (hourlyPrice <= 0) {
                        throw new SQLException("Invalid parking price.");
                    }

                    int totalAmount = hourlyPrice * hours;

                    // Insert booking with calculated amount
                    String insertSql =
                        "INSERT INTO bookings " +
                        "(user_id, slot_id, vehicle_number, booking_date, " +
                        "start_time, end_time, total_amount, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')";

                    try (PreparedStatement ps =
                             con.prepareStatement(insertSql)) {

                        ps.setInt(1, userId);
                        ps.setInt(2, slotId);
                        ps.setString(3, vehicle.trim());
                        ps.setString(4, date);
                        ps.setString(5, time);
                        ps.setString(6, endTime);
                        ps.setInt(7, totalAmount);

                        ps.executeUpdate();
                    }

                    // Mark slot as booked
                    String updateSql =
                        "UPDATE parking_slots SET status = 'Booked' " +
                        "WHERE slot_id = ?";

                    try (PreparedStatement ps =
                             con.prepareStatement(updateSql)) {

                        ps.setInt(1, slotId);
                        ps.executeUpdate();
                    }

                    con.commit();

                    // Booking success page
                    response.getWriter().println(
                        "<!DOCTYPE html>" +
                        "<html><head><meta charset='UTF-8'>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "<title>Booking Confirmed | SmartPark</title>" +

                        "<style>" +
                        "*{box-sizing:border-box;margin:0;padding:0}" +
                        "body{font-family:Arial;background:linear-gradient(135deg,#071b2b,#103c4a);min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px;color:white}" +
                        ".card{width:100%;max-width:550px;background:#102d3b;border:1px solid #28717a;border-radius:22px;padding:32px;box-shadow:0 20px 60px #0008;text-align:center}" +
                        ".brand{font-size:24px;font-weight:bold;letter-spacing:2px;color:#5eead4}" +
                        ".success{width:75px;height:75px;margin:25px auto;border-radius:50%;background:#0c8b78;display:flex;align-items:center;justify-content:center;font-size:42px}" +
                        "h1{font-size:28px;margin-bottom:10px}" +
                        ".subtitle{color:#b5cbd1;line-height:1.6;margin-bottom:22px}" +
                        ".details{background:#0a202d;border:1px solid #28505d;border-radius:15px;padding:18px;text-align:left}" +
                        ".row{display:flex;justify-content:space-between;gap:12px;padding:12px 0;border-bottom:1px solid #29434d}" +
                        ".row:last-child{border:0}" +
                        ".label{color:#a9c0c8}" +
                        ".value{font-weight:bold;text-align:right;overflow-wrap:anywhere}" +
                        ".amount{color:#5eead4;font-size:22px}" +
                        ".button{display:block;margin-top:24px;padding:15px;background:#2dd4bf;color:#062a30;text-decoration:none;border-radius:10px;font-weight:bold}" +
                        ".footer{color:#8daab4;font-size:12px;margin-top:20px}" +
                        "</style></head><body>" +

                        "<div class='card'>" +
                        "<div class='brand'>SMARTPARK</div>" +
                        "<div class='success'>&#10003;</div>" +
                        "<h1>Booking Confirmed!</h1>" +
                        "<p class='subtitle'>Your parking space has been reserved successfully.</p>" +

                        "<div class='details'>" +
                        "<div class='row'><span class='label'>Customer</span><span class='value'>" + safe(name) + "</span></div>" +
                        "<div class='row'><span class='label'>Slot ID</span><span class='value'>" + slotId + "</span></div>" +
                        "<div class='row'><span class='label'>Vehicle Number</span><span class='value'>" + safe(vehicle) + "</span></div>" +
                        "<div class='row'><span class='label'>Booking Date</span><span class='value'>" + safe(date) + "</span></div>" +
                        "<div class='row'><span class='label'>Starting Time</span><span class='value'>" + safe(time) + "</span></div>" +
                        "<div class='row'><span class='label'>Ending Time</span><span class='value'>" + safe(endTime) + "</span></div>" +
                        "<div class='row'><span class='label'>Duration</span><span class='value'>" + hours + " Hour(s)</span></div>" +
                        "<div class='row'><span class='label'>Price Per Hour</span><span class='value'>Rs. " + hourlyPrice + "</span></div>" +
                        "<div class='row'><span class='label'>Total Amount</span><span class='value amount'>Rs. " + totalAmount + "</span></div>" +
                        "<div class='row'><span class='label'>Booking Status</span><span class='value' style='color:#5eead4'>PENDING</span></div>" +
                        "</div>" +

                        "<a class='button' href='customer-dashboard.html'>GO TO MY DASHBOARD &#8594;</a>" +
                        "<p class='footer'>SMARTPARK MANAGEMENT SYSTEM<br>Smart Parking. Easy Living.</p>" +
                        "</div></body></html>"
                    );

                } catch (Exception e) {
                    con.rollback();
                    throw e;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

            response.getWriter().println(
                "<h2>Booking Failed!</h2>" +
                "<p>" + safe(e.getMessage()) + "</p>" +
                "<a href='booking.html'>Try Again</a>"
            );
        }
    }
}
