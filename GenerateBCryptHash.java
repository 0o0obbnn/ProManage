import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateBCryptHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String hash = encoder.encode(password);

        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);

        // 验证
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + matches);

        // 验证迁移脚本中的hash
        String migrationHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2ELM5u7Nwes7Gfv.MjYc66W";
        boolean migrationMatches = encoder.matches(password, migrationHash);
        System.out.println("Migration Hash Verification: " + migrationMatches);
    }
}
