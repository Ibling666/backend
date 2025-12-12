package com.usei.usei.controllers;

import com.usei.usei.models.ConfiguracionSeguridad;
import com.usei.usei.repositories.ConfiguracionSeguridadDAO;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic implementation for managing security configuration
 */
@Service
public class ConfiguracionSeguridadBL implements ConfiguracionSeguridadService {

    private final ConfiguracionSeguridadDAO configuracionDAO;

    public ConfiguracionSeguridadBL(ConfiguracionSeguridadDAO configuracionDAO) {
        this.configuracionDAO = configuracionDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfiguracionSeguridad> getActiveConfiguration() {
        return configuracionDAO.findActiveConfiguration();
    }

    @Override
    @Transactional
    public ConfiguracionSeguridad updateConfiguration(ConfiguracionSeguridad newConfig, Long userId) {
        // Deactivate all existing configurations
        Optional<ConfiguracionSeguridad> existingConfig = configuracionDAO.findActiveConfiguration();
        if (existingConfig.isPresent()) {
            ConfiguracionSeguridad existing = existingConfig.get();
            existing.setActiva(false);
            configuracionDAO.save(existing);
        }

        // Create new active configuration
        newConfig.setIdConfig(null); // Force new entity
        newConfig.setFechaModificacion(LocalDateTime.now());
        newConfig.setUsuarioModificacion(userId);
        newConfig.setActiva(true);

        ConfiguracionSeguridad saved = configuracionDAO.save(newConfig);

        System.out.println("✅ Security configuration updated successfully!");
        System.out.println("✅ New configuration ID: " + saved.getIdConfig());
        System.out.println("✅ Users will be forced to update passwords on next login based on new policies.");

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfiguracionSeguridad> findById(Long id) {
        return configuracionDAO.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<ConfiguracionSeguridad> findAll() {
        return configuracionDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveConfiguration() {
        return configuracionDAO.existsActiveConfiguration();
    }

    /**
     * Get the current configuration or create a default one if none exists
     * This ensures the system always has a valid configuration
     */
    @Transactional
    public ConfiguracionSeguridad getCurrentConfigurationOrDefault() {
        Optional<ConfiguracionSeguridad> config = getActiveConfiguration();

        if (config.isPresent()) {
            return config.get();
        }

        // Create default configuration if none exists
        ConfiguracionSeguridad defaultConfig = new ConfiguracionSeguridad(
                12,    // min password length
                3,     // max login attempts
                60,    // password expiry days
                12,    // months no reuse
                true,  // require uppercase
                true,  // require lowercase
                true,  // require numbers
                true,  // require symbols
                1L     // system user
        );

        return configuracionDAO.save(defaultConfig);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionSeguridad obtenerConfiguracionPorId(Long id) {
        Optional<ConfiguracionSeguridad> config = configuracionDAO.findById(id);
        return config.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionSeguridad obtenerConfiguracionActiva() {
        Optional<ConfiguracionSeguridad> config = configuracionDAO.findActiveConfiguration();
        return config.orElse(null);
    }

    @Override
    @Transactional
    public void eliminarConfiguracion(Long id) {
        configuracionDAO.deleteById(id);
    }
}