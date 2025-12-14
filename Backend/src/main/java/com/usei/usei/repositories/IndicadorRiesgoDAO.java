package com.usei.usei.repositories;

import com.usei.usei.models.IndicadorRiesgo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndicadorRiesgoDAO extends JpaRepository<IndicadorRiesgo, Long> {
    
    // Buscar indicadores activos
    List<IndicadorRiesgo> findByActivoTrue();
    
    // Buscar por estado actual
    List<IndicadorRiesgo> findByEstadoActual(String estadoActual);
    
    // Buscar indicadores en estado crítico o advertencia
    @Query("SELECT i FROM IndicadorRiesgo i WHERE i.activo = true AND (i.estadoActual = 'Crítico' OR i.estadoActual = 'Advertencia') ORDER BY i.estadoActual DESC")
    List<IndicadorRiesgo> findIndicadoresEnAlerta();
    
    // Buscar por tipo de indicador
    List<IndicadorRiesgo> findByTipoIndicadorAndActivoTrue(String tipoIndicador);
}
