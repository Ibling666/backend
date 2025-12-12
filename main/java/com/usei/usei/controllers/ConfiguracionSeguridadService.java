package com.usei.usei.controllers;

import com.usei.usei.models.ConfiguracionSeguridad;
import java.util.Optional;

/**
 * Service interface for managing security configuration policies
 */
public interface ConfiguracionSeguridadService {

    /**
     * Get the current active security configuration
     */
    Optional<ConfiguracionSeguridad> getActiveConfiguration();

    /**
     * Update the security configuration (deactivates old and creates new)
     * Only users with 'Seguridad' role should be able to call this
     */
    ConfiguracionSeguridad updateConfiguration(ConfiguracionSeguridad newConfig, Long userId);

    /**
     * Get configuration by ID
     */
    Optional<ConfiguracionSeguridad> findById(Long id);

    /**
     * Return all configuration records (for audit / admin view)
     */
    java.util.List<ConfiguracionSeguridad> findAll();

    /**
     * Check if there's an active configuration
     */
    boolean hasActiveConfiguration();

    /**
     * Get configuration by ID (for backward compatibility)
     */
    ConfiguracionSeguridad obtenerConfiguracionPorId(Long id);

    /**
     * Get the active configuration (for backward compatibility)
     */
    ConfiguracionSeguridad obtenerConfiguracionActiva();

    /**
     * Delete configuration by ID
     */
    void eliminarConfiguracion(Long id);
}