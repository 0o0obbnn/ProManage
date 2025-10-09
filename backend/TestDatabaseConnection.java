import java.sql.*;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://192.168.2.144:5432/promanage";
        String user = "postgres";
        String password = "postgres";

        System.out.println("=== Testing Database Connection ===");
        System.out.println("URL: " + url);
        System.out.println("User: " + user);
        System.out.println();

        try {
            // Load PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ PostgreSQL Driver loaded successfully");

            // Connect to database
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Database connection successful!");
            System.out.println();

            // Check current database
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT current_database()");
            if (rs.next()) {
                System.out.println("Current database: " + rs.getString(1));
            }
            System.out.println();

            // List all tables
            System.out.println("=== Existing Tables ===");
            rs = stmt.executeQuery("SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename");
            int tableCount = 0;
            while (rs.next()) {
                System.out.println("  - " + rs.getString(1));
                tableCount++;
            }

            if (tableCount == 0) {
                System.out.println("  (No tables found)");
                System.out.println();
                System.out.println("❌ Database is empty. Need to execute migrations.");
            } else {
                System.out.println();
                System.out.println("✅ Found " + tableCount + " tables");
            }

            // Close connections
            rs.close();
            stmt.close();
            conn.close();

            System.out.println();
            System.out.println("=== Connection Test Complete ===");

        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL Driver not found!");
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
