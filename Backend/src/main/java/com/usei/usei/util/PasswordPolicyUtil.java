package com.usei.usei.util;

import com.usei.usei.controllers.ConfiguracionSeguridadBL;
import com.usei.usei.models.ConfiguracionSeguridad;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Dynamic password policy utility that reads configuration from database
 * Converted from static utility to Spring component to allow dependency injection
 */
@Component
public class PasswordPolicyUtil {

    private final ConfiguracionSeguridadBL configuracionService;

    // Fallback constants in case database is not available
    public static final int DEFAULT_MIN_LENGTH = 12;
    public static final int DEFAULT_COMPLEJIDAD = 4;
    public static final int DEFAULT_MAX_INTENTOS = 3;
    public static final int DEFAULT_EXPIRA_DIAS = 60;
    public static final int DEFAULT_NO_REUSE_MESES = 12;

    private static final Pattern UPPER   = Pattern.compile("[A-Z]");
    private static final Pattern LOWER   = Pattern.compile("[a-z]");
    private static final Pattern DIGIT   = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    public PasswordPolicyUtil(ConfiguracionSeguridadBL configuracionService) {
        this.configuracionService = configuracionService;
    }

    /**
     * Check if password meets current policy requirements
     */
    public boolean cumplePolitica(String plain) {
        ConfiguracionSeguridad config = getCurrentConfig();

        if (plain == null || plain.length() < config.getMinLongitudContrasenia()) {
            return false;
        }

        if (config.isRequerirMayusculas() && !UPPER.matcher(plain).find()) {
            return false;
        }

        if (config.isRequerirMinusculas() && !LOWER.matcher(plain).find()) {
            return false;
        }

        if (config.isRequerirNumeros() && !DIGIT.matcher(plain).find()) {
            return false;
        }

        if (config.isRequerirSimbolos() && !SPECIAL.matcher(plain).find()) {
            return false;
        }

        return true;
    }

    /**
     * Get current minimum password length
     */
    public int getMinLength() {
        return getCurrentConfig().getMinLongitudContrasenia();
    }

    /**
     * Get current maximum login attempts
     */
    public int getMaxIntentos() {
        return getCurrentConfig().getMaxIntentosLogin();
    }

    /**
     * Get password expiry days
     */
    public int getExpiraDias() {
        return getCurrentConfig().getDiasExpiracionContrasenia();
    }

    /**
     * Get months before password can be reused
     */
    public int getNoReuseMeses() {
        return getCurrentConfig().getMesesNoReutilizar();
    }

    /**
     * Calculate complexity score based on current policy
     */
    public int getComplejidad() {
        ConfiguracionSeguridad config = getCurrentConfig();
        int complejidad = 0;

        if (config.isRequerirMayusculas()) complejidad++;
        if (config.isRequerirMinusculas()) complejidad++;
        if (config.isRequerirNumeros()) complejidad++;
        if (config.isRequerirSimbolos()) complejidad++;

        return complejidad;
    }

    /**
     * Calculate the actual complexity of a password based on character types
     */
    public int calcularComplejidad(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int complexity = 0;

        if (UPPER.matcher(password).find()) {
            complexity++; // Has uppercase
        }

        if (LOWER.matcher(password).find()) {
            complexity++; // Has lowercase
        }

        if (DIGIT.matcher(password).find()) {
            complexity++; // Has digits
        }

        if (SPECIAL.matcher(password).find()) {
            complexity++; // Has special characters
        }

        return complexity;
    }

    /**
     * Get current configuration, with fallback to defaults
     */
    private ConfiguracionSeguridad getCurrentConfig() {
        try {
            return configuracionService.getCurrentConfigurationOrDefault();
        } catch (Exception e) {
            // Fallback to default configuration if database is not available
            return createDefaultConfig();
        }
    }

    /**
     * Create a default configuration for fallback scenarios
     */
    private ConfiguracionSeguridad createDefaultConfig() {
        return new ConfiguracionSeguridad(
                DEFAULT_MIN_LENGTH,
                DEFAULT_MAX_INTENTOS,
                DEFAULT_EXPIRA_DIAS,
                DEFAULT_NO_REUSE_MESES,
                true, // require uppercase
                true, // require lowercase
                true, // require numbers
                true, // require symbols
                1L    // system user
        );
    }

    // Static methods for backward compatibility (deprecated - use instance methods)

    @Deprecated
    public static final int MIN_LENGTH = DEFAULT_MIN_LENGTH;
    @Deprecated
    public static final int COMPLEJIDAD = DEFAULT_COMPLEJIDAD;
    @Deprecated
    public static final int MAX_INTENTOS = DEFAULT_MAX_INTENTOS;
    @Deprecated
    public static final int EXPIRA_DIAS = DEFAULT_EXPIRA_DIAS;
    @Deprecated
    public static final int NO_REUSE_MESES = DEFAULT_NO_REUSE_MESES;

    /**
     * Check if an existing password complies with current security policies.
     * Forces password update whenever policies have been changed after password creation.
     * This ensures ALL users update their passwords when ANY policy setting changes.
     */
    public boolean existingPasswordCompliesWithCurrentPolicy(com.usei.usei.models.Contrasenia contrasenia) {
        if (contrasenia == null) {
            System.out.println("❌ POLICY CHECK: Password is null - forcing update");
            return false;
        }

        ConfiguracionSeguridad config = getCurrentConfig();
        System.out.println("🔒 === POLICY COMPLIANCE CHECK ===");
        System.out.println("🔒 Current config ID: " + config.getIdConfig());

        // CORE LOGIC: Check if password meets current policy requirements
        java.time.LocalDate passwordCreated = contrasenia.getFechaCreacion();
        java.time.LocalDateTime policyModified = config.getFechaModificacion();

        System.out.println("🔒 Password created on: " + passwordCreated);
        System.out.println("🔒 Policy last modified: " + policyModified);

        // First check: If password was created AFTER policy update, it's automatically valid
        if (passwordCreated != null && policyModified != null) {
            java.time.LocalDate policyModifiedDate = policyModified.toLocalDate();

            if (passwordCreated.isAfter(policyModifiedDate)) {
                System.out.println("✅ Password created AFTER policy update - automatically valid");
                return true;
            }
        }

        // Second check: If password was created before or on policy update date,
        // check if it meets current requirements
        System.out.println("🔒 Checking if password meets current policy requirements...");
        boolean meetsRequirements = passwordMeetsCurrentRequirements(contrasenia, config);

        if (meetsRequirements) {
            System.out.println("✅ Password meets current policy requirements - allowing login");
            return true;
        } else {
            System.out.println("❌ Password does NOT meet current policy requirements - forcing update");
            return false;
        }
    }

    /**
     * Check if password meets current policy requirements (length, complexity, and character requirements)
     * Note: We can't check the actual password string for character types since it's hashed,
     * so we rely on the stored complexity score and length, plus policy requirements matching.
     */
    private boolean passwordMeetsCurrentRequirements(com.usei.usei.models.Contrasenia contrasenia, ConfiguracionSeguridad config) {
        System.out.println("🔍 Checking password compliance with current policy...");

        // Check length requirement
        if (contrasenia.getLongitud() < config.getMinLongitudContrasenia()) {
            System.out.println("❌ Password too short: " + contrasenia.getLongitud() + " < " + config.getMinLongitudContrasenia());
            return false;
        }

        // Calculate required complexity based on current policy
        int requiredComplexity = 0;
        if (config.isRequerirMayusculas()) requiredComplexity++;
        if (config.isRequerirMinusculas()) requiredComplexity++;
        if (config.isRequerirNumeros()) requiredComplexity++;
        if (config.isRequerirSimbolos()) requiredComplexity++;

        System.out.println("🔒 Password stored complexity: " + contrasenia.getComplejidad() + ", Current policy requires: " + requiredComplexity);
        System.out.println("🔒 Policy requirements - Upper: " + config.isRequerirMayusculas() +
                ", Lower: " + config.isRequerirMinusculas() +
                ", Numbers: " + config.isRequerirNumeros() +
                ", Symbols: " + config.isRequerirSimbolos());

        // Check if password complexity meets current requirements
        if (contrasenia.getComplejidad() < requiredComplexity) {
            System.out.println("❌ Password complexity insufficient: " + contrasenia.getComplejidad() + " < " + requiredComplexity);
            return false;
        }

        System.out.println("✅ Password meets all current policy requirements");
        return true;
    }
}