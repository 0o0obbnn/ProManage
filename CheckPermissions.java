import java.sql.*;

public class CheckPermissions {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/promanage";
        String username = "postgres";
        String password = "postgres";

        try {
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");

            // Establish connection
            Connection connection = DriverManager.getConnection(url, username, password);

            // Query users
            System.out.println("=== Users ===");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, username, email FROM tb_user ORDER BY id");
            while (rs.next()) {
                System.out.println("ID: " + rs.getLong("id") + ", Username: " + rs.getString("username") + ", Email: " + rs.getString("email"));
            }

            // Query user roles
            System.out.println("\n=== User Roles ===");
            rs = stmt.executeQuery("SELECT ur.user_id, u.username, r.role_name, r.role_code FROM tb_user_role ur JOIN tb_user u ON ur.user_id = u.id JOIN tb_role r ON ur.role_id = r.id ORDER BY ur.user_id");
            while (rs.next()) {
                System.out.println("User ID: " + rs.getLong("user_id") + ", Username: " + rs.getString("username") + ", Role: " + rs.getString("role_name") + " (" + rs.getString("role_code") + ")");
            }

            // Query roles and their permissions
            System.out.println("\n=== Role Permissions (Admin Role) ===");
            rs = stmt.executeQuery("SELECT p.permission_name, p.permission_code FROM tb_role_permission rp JOIN tb_permission p ON rp.permission_id = p.id WHERE rp.role_id = 1 AND p.deleted = FALSE ORDER BY p.sort");
            while (rs.next()) {
                System.out.println("Permission: " + rs.getString("permission_name") + " (" + rs.getString("permission_code") + ")");
            }

            // Query document permissions
            System.out.println("\n=== Document Permissions ===");
            rs = stmt.executeQuery("SELECT permission_name, permission_code FROM tb_permission WHERE permission_code LIKE 'document:%' AND deleted = FALSE ORDER BY sort");
            while (rs.next()) {
                System.out.println("Permission: " + rs.getString("permission_name") + " (" + rs.getString("permission_code") + ")");
            }

            // Close connection
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}