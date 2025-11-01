package com.promanage.infrastructure.security;

import java.security.SecureRandom;
import java.util.Base64;

import lombok.extern.slf4j.Slf4j;

/**
 * JWT Secret Generator Utility
 *
 * <p>Generates cryptographically secure JWT secrets for production use. This utility should be used
 * during deployment to generate strong secrets.
 *
 * @author ProManage Team
 * @since 2025-10-03
 */
@Slf4j
public final class JwtSecretGenerator {

  private static final int DEFAULT_SECRET_LENGTH = 64; // 512 bits minimum for HS512
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private JwtSecretGenerator() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Generate a cryptographically secure JWT secret
   *
   * @return Base64-encoded secret string
   */
  public static String generateSecret() {
    return generateSecret(DEFAULT_SECRET_LENGTH);
  }

  /**
   * Generate a cryptographically secure JWT secret with specified length
   *
   * @param length Secret length in bytes (minimum 64)
   * @return Base64-encoded secret string
   */
  public static String generateSecret(int length) {
    if (length < 64) {
      throw new IllegalArgumentException(
          "Secret length must be at least 64 bytes for HS512 algorithm");
    }

    byte[] secretBytes = new byte[length];
    SECURE_RANDOM.nextBytes(secretBytes);

    String secret = Base64.getEncoder().encodeToString(secretBytes);

    log.info("Generated secure JWT secret with {} bytes ({} characters)", length, secret.length());
    return secret;
  }

  /**
   * Calculate entropy of a given string in bits
   *
   * @param input Input string
   * @return Entropy in bits
   */
  public static double calculateEntropy(String input) {
    if (input == null || input.isEmpty()) {
      return 0.0;
    }

    int[] charCounts = new int[256]; // Extended ASCII
    for (char c : input.toCharArray()) {
      charCounts[c]++;
    }

    double entropy = 0.0;
    int length = input.length();

    for (int count : charCounts) {
      if (count > 0) {
        double probability = (double) count / length;
        entropy -= probability * (Math.log(probability) / Math.log(2));
      }
    }

    return entropy * length; // Total entropy in bits
  }

  /**
   * Validate JWT secret strength
   *
   * @param secret Secret to validate
   * @return true if secret is strong enough
   */
  public static boolean isStrongSecret(String secret) {
    if (secret == null || secret.length() < 64) {
      return false;
    }

    // Check entropy (minimum 256 bits)
    double entropy = calculateEntropy(secret);
    if (entropy < 256) {
      log.warn("Secret has insufficient entropy: {} bits", entropy);
      return false;
    }

    // Check for character variety
    boolean hasLower = secret.chars().anyMatch(Character::isLowerCase);
    boolean hasUpper = secret.chars().anyMatch(Character::isUpperCase);
    boolean hasDigit = secret.chars().anyMatch(Character::isDigit);
    boolean hasSpecial = secret.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

    int varietyCount =
        (hasLower ? 1 : 0) + (hasUpper ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);

    if (varietyCount < 3) {
      log.warn("Secret lacks character variety");
      return false;
    }

    return true;
  }

  /**
   * Main method for generating secrets from command line
   *
   * @param args Command line arguments
   */
  @SuppressWarnings("PMD.SystemPrintln")
  public static void main(String[] args) {
    System.out.println("=== ProManage JWT Secret Generator ===");
    System.out.println();

    // Generate and display multiple secrets
    System.out.println("Generated JWT Secrets (use any ONE of these):");
    System.out.println("----------------------------------------");

    for (int i = 1; i <= 3; i++) {
      String secret = generateSecret();
      double entropy = calculateEntropy(secret);

      System.out.println("Secret " + i + ":");
      System.out.println(secret);
      System.out.println("Entropy: " + String.format("%.2f", entropy) + " bits");
      System.out.println("Length: " + secret.length() + " characters");
      System.out.println("Strength: " + (isStrongSecret(secret) ? "✅ STRONG" : "❌ WEAK"));
      System.out.println();
    }

    System.out.println("Environment variable configuration:");
    System.out.println("export JWT_SECRET=\"<your-chosen-secret>\"");
    System.out.println();
    System.out.println("Docker configuration:");
    System.out.println("docker run -e JWT_SECRET=\"<your-chosen-secret>\" ...");
    System.out.println();
    System.out.println(
        "⚠️  WARNING: Keep your JWT secret secure and never commit it to version control!");
  }
}
