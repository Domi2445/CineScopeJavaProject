package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;

/**
 * PBKDF2 Hasher für Passwörter.
 * Format: iterations:saltBase64:hashBase64
 * PBKDF2 ist sicherer als SHA256, da es absichtlich langsam ist und Brute-Force-Angriffe erschwert.
 */
public class PasswordUtil
{
    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final int SALT_LEN = 16;
    private static final int HASH_LEN = 32; // bytes
    private static final int ITERATIONS = 10000;

    public static String hashPassword(String password)
    {
        Objects.requireNonNull(password);
        try
        {
            byte[] salt = new byte[SALT_LEN];
            new SecureRandom().nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_LEN * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGO);
            byte[] hash = skf.generateSecret(spec).getEncoded();

            return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            throw new RuntimeException("Fehler beim Hashen des Passworts", e);
        }
    }

    public static boolean verifyPassword(String password, String stored)
    {
        if (password == null || stored == null) return false;
        try
        {
            String[] parts = stored.split(":");
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, expected.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGO);
            byte[] actual = skf.generateSecret(spec).getEncoded();

            if (actual.length != expected.length) return false;
            int diff = 0;
            for (int i = 0; i < actual.length; i++) diff |= actual[i] ^ expected[i];
            return diff == 0;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}

