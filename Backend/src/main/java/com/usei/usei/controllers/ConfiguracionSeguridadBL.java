package com.usei.usei.controllers;

import com.usei.usei.models.ConfiguracionSeguridad;
import com.usei.usei.repositories.ConfiguracionSeguridadDAO;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.usei.usei.controllers.UsuarioService;
import com.usei.usei.controllers.LogUsuarioService;
import com.usei.usei.models.Usuario;



/**
 * Business logic implementation for managing security configuration
 */
@Service
public class ConfiguracionSeguridadBL implements ConfiguracionSeguridadService {

    private final ConfiguracionSeguridadDAO configuracionDAO;
    private final UsuarioService usuarioService;
    private final LogUsuarioService logUsuarioService;


    public ConfiguracionSeguridadBL(ConfiguracionSeguridadDAO configuracionDAO,
                                    UsuarioService usuarioService,
                                    LogUsuarioService logUsuarioService) {
        this.configuracionDAO = configuracionDAO;
        this.usuarioService = usuarioService;
        this.logUsuarioService = logUsuarioService;
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
        // 🔹 REGISTRO DE LOG (actualización de políticas)
        usuarioService.findById(userId).ifPresent(usuario -> {
            String motivo = "CONFIG_SEGURIDAD_ACTUALIZADA";
            String nivel = "INFO";
            String mensaje = "Actualización de configuración de seguridad";
            String detalle = "Se actualizó la configuración de seguridad. "
                    + "ID nueva config=" + saved.getIdConfig()
                    + (existingConfig.isPresent()
                    ? (", ID config anterior=" + existingConfig.get().getIdConfig())
                    : "");

            logUsuarioService.registrarLogSeguridad(
                    usuario,
                    motivo,
                    nivel,
                    mensaje,
                    detalle
            );
        });

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
        // 🔹 REGISTRO DE LOG (eliminación de políticas) con usuario sistema (id=1)
        Long systemUserId = 1L;
        usuarioService.findById(systemUserId).ifPresent(usuario -> {
            String motivo = "CONFIG_SEGURIDAD_ELIMINADA";
            String nivel = "WARN";
            String mensaje = "Eliminación de configuración de seguridad";
            String detalle = "Se eliminó la configuración de seguridad con id=" + id;

            logUsuarioService.registrarLogSeguridad(
                    usuario,
                    motivo,
                    nivel,
                    mensaje,
                    detalle
            );
        });
    }
}