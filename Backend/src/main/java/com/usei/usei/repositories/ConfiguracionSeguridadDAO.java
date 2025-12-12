package com.usei.usei.repositories;

import com.usei.usei.models.ConfiguracionSeguridad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for ConfiguracionSeguridad entity to manage security policy configuration
 */
@Repository
public interface ConfiguracionSeguridadDAO extends JpaRepository<ConfiguracionSeguridad, Long> {

    /**
     * Find the currently active security configuration
     * There should only be one active configuration at any time
     */
    @Query("SELECT c FROM ConfiguracionSeguridad c WHERE c.activa = true ORDER BY c.fechaModificacion DESC")
    Optional<ConfiguracionSeguridad> findActiveConfiguration();

    /**
     * Check if an active configuration exists
     */
    @Query("SELECT COUNT(c) > 0 FROM ConfiguracionSeguridad c WHERE c.activa = true")
    boolean existsActiveConfiguration();
}