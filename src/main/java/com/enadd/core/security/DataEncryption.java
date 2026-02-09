package com.enadd.core.security;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;


/**
 * Secure data encryption handler for sensitive player data.
 * Uses AES-GCM encryption for secure storage.
 */
public final class DataEncryption {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private static volatile SecretKeySpec secretKey;
    private static volatile boolean initialized = false;
    private static final SecureRandom secureRandom = new SecureRandom();

    // Prevent instantiation
    private DataEncryption() {}

    /**
     * Initialize the encryption system with a secret key.
     *
     * @param keyBase64 the Base64-encoded secret key (must be 32 bytes for AES-256)
     */
    public static synchronized void initialize(String keyBase64) {
        if (initialized) {
            return;
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);

            if (keyBytes.length != 32) {
                throw new IllegalArgumentException("Key must be 32 bytes for AES-256");
            }

            secretKey = new SecretKeySpec(keyBytes, "AES");
            initialized = true;

        } catch (IllegalArgumentException e) {
            // Generate a random key if invalid key provided
            generateRandomKey();
        }
    }

    /**
     * Initialize with an auto-generated secure key.
     *
     * @return the Base64-encoded key (save this for future use)
     */
    public static synchronized String initializeWithGeneratedKey() {
        String keyBase64 = generateRandomKey();
        initialize(keyBase64);
        return keyBase64;
    }

    /**
     * Generate a random 256-bit key.
     *
     * @return the Base64-encoded key
     */
    private static String generateRandomKey() {
        byte[] keyBytes = new byte[32];
        secureRandom.nextBytes(keyBytes);

        String keyBase64 = Base64.getEncoder().encodeToString(keyBytes);
        secretKey = new SecretKeySpec(keyBytes, "AES");
        initialized = true;

        return keyBase64;
    }

    /**
     * Check if encryption is initialized.
     *
     * @return true if encryption is ready
     */
    public static boolean isInitialized() {
        return initialized && secretKey != null;
    }

    /**
     * Encrypt plaintext data.
     *
     * @param plaintext the data to encrypt
     * @return the Base64-encoded encrypted data (includes IV)
     * @throws EncryptionException if encryption fails
     */
    public static String encrypt(String plaintext) {
        if (!isInitialized()) {
            throw new IllegalStateException("Encryption not initialized");
        }

        if (plaintext == null || plaintext.isEmpty()) {
            return "";
        }

        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encrypt
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    /**
     * Decrypt encrypted data.
     *
     * @param encryptedBase64 the Base64-encoded encrypted data
     * @return the decrypted plaintext
     * @throws EncryptionException if decryption fails
     */
    public static String decrypt(String encryptedBase64) {
        if (!isInitialized()) {
            throw new IllegalStateException("Encryption not initialized");
        }

        if (encryptedBase64 == null || encryptedBase64.isEmpty()) {
            return "";
        }

        try {
            // Decode from Base64
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Decrypt
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }

    /**
     * Encrypt data with a given key (for one-time operations).
     *
     * @param plaintext the data to encrypt
     * @param keyBase64 the Base64-encoded key
     * @return the Base64-encoded encrypted data
     */
    public static String encryptWithKey(String plaintext, String keyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        SecretKeySpec tempKey = new SecretKeySpec(keyBytes, "AES");

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, tempKey, parameterSpec);

            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    /**
     * Decrypt data with a given key (for one-time operations).
     *
     * @param encryptedBase64 the Base64-encoded encrypted data
     * @param keyBase64 the Base64-encoded key
     * @return the decrypted plaintext
     */
    public static String decryptWithKey(String encryptedBase64, String keyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        SecretKeySpec tempKey = new SecretKeySpec(keyBytes, "AES");

        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, tempKey, parameterSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }

    /**
     * Generate a secure random string (for tokens, IDs, etc.).
     *
     * @param length the desired length
     * @return a secure random string
     */
    public static String generateSecureToken(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    /**
     * Shutdown encryption and clear sensitive data.
     */
    public static synchronized void shutdown() {
        if (secretKey != null) {
            byte[] keyBytes = secretKey.getEncoded();
            for (int i = 0; i < keyBytes.length; i++) {
                keyBytes[i] = 0;
            }
            secretKey = null;
        }
        initialized = false;
    }

    /**
     * Custom exception for encryption errors.
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
