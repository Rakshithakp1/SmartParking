
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
        "jdbc:mysql://localhost:3306/smart_parking";

    private static final String USER = "root";

    private static final String PASSWORD = "Parking@New2026!";

    public static Connection getConnection() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                URL, USER, PASSWORD
            );

            System.out.println("MySQL Database Connected Successfully!");

            return con;

        } catch (ClassNotFoundException | SQLException e) {

            System.out.println("Database Connection Failed!");
            e.printStackTrace();

            return null;
        }
    }
}
