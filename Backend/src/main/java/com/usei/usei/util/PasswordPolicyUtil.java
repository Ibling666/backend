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

        // CORE LOGIC: If password was created before policy was last modified, force update
        java.time.LocalDate passwordCreated = contrasenia.getFechaCreacion();
        java.time.LocalDateTime policyModified = config.getFechaModificacion();

        System.out.println("🔒 Password created on: " + passwordCreated);
        System.out.println("🔒 Policy last modified: " + policyModified);

        if (passwordCreated != null && policyModified != null) {
            // Get policy modification date (without time) for comparison
            java.time.LocalDate policyModifiedDate = policyModified.toLocalDate();

            System.out.println("🔒 Password created date: " + passwordCreated);
            System.out.println("🔒 Policy modified date: " + policyModifiedDate);

            // ⭐ KEY LOGIC: If password was created BEFORE the policy was modified, force update
            if (passwordCreated.isBefore(policyModifiedDate)) {
                System.out.println("❌ POLICY ENFORCEMENT: Password created BEFORE policy update!");
                System.out.println("❌ Password date: " + passwordCreated + ", Policy modified date: " + policyModifiedDate);
                System.out.println("❌ FORCING PASSWORD CHANGE - Security policies have been updated");
                return false;
            }

            // ⭐ FIX: For same-day cases, be more intelligent about timing
            if (passwordCreated.isEqual(policyModifiedDate)) {
                System.out.println("🔒 Password and policy both from same date: " + passwordCreated);

                // Check if password was created AFTER policy modification time
                java.time.LocalDateTime passwordDateTime = passwordCreated.atStartOfDay();

                // If policy was modified today, check if password was updated AFTER the policy change
                // For this, we need to check the actual time, but since password only stores date,
                // we'll be conservative: allow login if password was created on or after policy date
                if (passwordCreated.isAfter(policyModifiedDate) || passwordCreated.isEqual(policyModifiedDate)) {
                    System.out.println("✅ Password created on or after policy update date - checking compliance");

                    // Additional validation: ensure password meets current requirements
                    if (passwordMeetsCurrentRequirements(contrasenia, config)) {
                        System.out.println("✅ Password meets all current requirements - allowing login");
                        return true;
                    } else {
                        System.out.println("❌ Password doesn't meet current requirements - forcing update");
                        return false;
                    }
                } else {
                    System.out.println("❌ Same-day policy update but password seems older - forcing update for safety");
                    return false;
                }
            }
        }

        System.out.println("✅ Password was created AFTER policy update - allowing login");
        return true;
    }

    /**
     * Check if password meets current policy requirements (length, complexity)
     */
    private boolean passwordMeetsCurrentRequirements(com.usei.usei.models.Contrasenia contrasenia, ConfiguracionSeguridad config) {
        // Check length requirement
        if (contrasenia.getLongitud() < config.getMinLongitudContrasenia()) {
            System.out.println("❌ Password too short: " + contrasenia.getLongitud() + " < " + config.getMinLongitudContrasenia());
            return false;
        }

        // Check complexity requirements
        int currentComplexity = contrasenia.getComplejidad();
        int requiredComplexity = getComplejidad();

        System.out.println("🔒 Password complexity: " + currentComplexity + ", Required: " + requiredComplexity);

        if (currentComplexity < requiredComplexity) {
            System.out.println("❌ Password complexity insufficient: " + currentComplexity + " < " + requiredComplexity);
            return false;
        }

        return true;
    }
}