package com.usei.usei.repositories;

import com.usei.usei.models.RiesgoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiesgoEventoDAO extends JpaRepository<RiesgoEvento, Long> {
    
    // Buscar riesgos por estado
    List<RiesgoEvento> findByEstado(String estado);
    
    // Buscar riesgos por nivel de riesgo
    List<RiesgoEvento> findByNivelRiesgo(String nivelRiesgo);
    
    // Buscar riesgos por categoría
    List<RiesgoEvento> findByCategoria(String categoria);
    
    // Buscar riesgos ordenados por valor de riesgo (más críticos primero)
    @Query("SELECT r FROM RiesgoEvento r ORDER BY r.valorRiesgo DESC")
    List<RiesgoEvento> findAllOrderByValorRiesgoDesc();
    
    // Contar riesgos por nivel
    @Query("SELECT r.nivelRiesgo, COUNT(r) FROM RiesgoEvento r WHERE r.estado != 'Cerrado' GROUP BY r.nivelRiesgo")
    List<Object[]> countByNivelRiesgo();
    
    // Obtener riesgos activos (no cerrados)
    @Query("SELECT r FROM RiesgoEvento r WHERE r.estado != 'Cerrado' ORDER BY r.valorRiesgo DESC")
    List<RiesgoEvento> findActiveRisks();
}
