package com.usei.usei.controllers;

import com.usei.usei.dto.IndicadorRiesgoDTO;
import com.usei.usei.models.IndicadorRiesgo;
import com.usei.usei.models.RiesgoEvento;
import com.usei.usei.repositories.IndicadorRiesgoDAO;
import com.usei.usei.repositories.RiesgoEventoDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IndicadorRiesgoBL {
    
    private static final Logger logger = LoggerFactory.getLogger(IndicadorRiesgoBL.class);
    
    @Autowired
    private IndicadorRiesgoDAO indicadorRiesgoDAO;
    
    @Autowired
    private RiesgoEventoDAO riesgoEventoDAO;
    
    // Crear KRI automáticamente desde un riesgo
    @Transactional
    public void crearKRIDesdeRiesgo(RiesgoEvento riesgo) {
        logger.info("Creando KRI automáticamente para riesgo: {}", riesgo.getTitulo());
        
        try {
            // Verificar si ya existe un KRI para esta categoría de riesgo
            List<IndicadorRiesgo> indicadoresExistentes = indicadorRiesgoDAO.findByActivoTrue();
            boolean kriExiste = indicadoresExistentes.stream()
                .anyMatch(ind -> ind.getNombre().equalsIgnoreCase(riesgo.getCategoria()));
            
            if (!kriExiste) {
                // Contar solo riesgos activos (no Controlado ni Cerrado)
                long conteoRiesgosActivos = riesgoEventoDAO.findAll().stream()
                    .filter(r -> r.getCategoria().equalsIgnoreCase(riesgo.getCategoria()))
                    .filter(r -> !r.getEstado().equalsIgnoreCase("Controlado") && !r.getEstado().equalsIgnoreCase("Cerrado"))
                    .count();
                
                // Crear nuevo KRI basado en la categoría del riesgo
                IndicadorRiesgo nuevoKRI = new IndicadorRiesgo();
                nuevoKRI.setNombre(riesgo.getCategoria());
                nuevoKRI.setDescripcion("Indicador para monitorear riesgos de tipo: " + riesgo.getCategoria());
                nuevoKRI.setTipoIndicador("Seguridad");
                nuevoKRI.setUmbralCritico(new BigDecimal("10.00"));
                nuevoKRI.setUmbralAdvertencia(new BigDecimal("5.00"));
                nuevoKRI.setValorActual(BigDecimal.valueOf(conteoRiesgosActivos));
                nuevoKRI.setUnidadMedida("Número de riesgos activos");
                nuevoKRI.setFrecuenciaMedicion("Diario");
                nuevoKRI.setUsuarioCreacion(riesgo.getUsuarioRegistro());
                
                indicadorRiesgoDAO.save(nuevoKRI);
                logger.info("KRI creado automáticamente: {} = {}", nuevoKRI.getNombre(), conteoRiesgosActivos);
            } else {
                // Si ya existe, actualizar el contador (solo riesgos activos)
                IndicadorRiesgo kriExistente = indicadoresExistentes.stream()
                    .filter(ind -> ind.getNombre().equalsIgnoreCase(riesgo.getCategoria()))
                    .findFirst()
                    .orElse(null);
                
                if (kriExistente != null) {
                    long conteoRiesgosActivos = riesgoEventoDAO.findAll().stream()
                        .filter(r -> r.getCategoria().equalsIgnoreCase(riesgo.getCategoria()))
                        .filter(r -> !r.getEstado().equalsIgnoreCase("Controlado") && !r.getEstado().equalsIgnoreCase("Cerrado"))
                        .count();
                    
                    kriExistente.setValorActual(BigDecimal.valueOf(conteoRiesgosActivos));
                    kriExistente.setUltimaActualizacion(LocalDateTime.now());
                    indicadorRiesgoDAO.save(kriExistente);
                    logger.info("KRI actualizado: {} = {} (solo riesgos activos)", kriExistente.getNombre(), conteoRiesgosActivos);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error al crear KRI desde riesgo", e);
        }
    }
    
    // Obtener todos los indicadores activos
    public List<IndicadorRiesgo> obtenerIndicadoresActivos() {
        return indicadorRiesgoDAO.findByActivoTrue();
    }
    
    // Obtener indicador por ID
    public Optional<IndicadorRiesgo> obtenerIndicadorPorId(Long idIndicador) {
        return indicadorRiesgoDAO.findById(idIndicador);
    }
    
    // Obtener indicadores en alerta (crítico o advertencia)
    public List<IndicadorRiesgo> obtenerIndicadoresEnAlerta() {
        return indicadorRiesgoDAO.findIndicadoresEnAlerta();
    }
    
    // Actualizar valor de indicador
    @Transactional
    public IndicadorRiesgo actualizarValorIndicador(Long idIndicador, BigDecimal nuevoValor) {
        logger.info("Actualizando valor de indicador ID: {} a {}", idIndicador, nuevoValor);
        
        Optional<IndicadorRiesgo> optionalIndicador = indicadorRiesgoDAO.findById(idIndicador);
        if (optionalIndicador.isEmpty()) {
            throw new RuntimeException("Indicador no encontrado con ID: " + idIndicador);
        }
        
        IndicadorRiesgo indicador = optionalIndicador.get();
        indicador.setValorActual(nuevoValor);
        indicador.setUltimaActualizacion(LocalDateTime.now());
        
        // El método evaluarEstado() se llama automáticamente en @PreUpdate
        
        return indicadorRiesgoDAO.save(indicador);
    }
    
    // Crear nuevo indicador
    @Transactional
    public IndicadorRiesgo crearIndicador(IndicadorRiesgoDTO dto) {
        logger.info("Creando nuevo indicador: {}", dto.getNombre());
        
        IndicadorRiesgo indicador = new IndicadorRiesgo();
        indicador.setNombre(dto.getNombre());
        indicador.setDescripcion(dto.getDescripcion());
        indicador.setTipoIndicador(dto.getTipoIndicador());
        indicador.setUmbralCritico(dto.getUmbralCritico());
        indicador.setUmbralAdvertencia(dto.getUmbralAdvertencia());
        indicador.setValorActual(dto.getValorActual() != null ? dto.getValorActual() : BigDecimal.ZERO);
        indicador.setUnidadMedida(dto.getUnidadMedida());
        indicador.setFrecuenciaMedicion(dto.getFrecuenciaMedicion());
        indicador.setUsuarioCreacion(dto.getUsuarioCreacion());
        
        return indicadorRiesgoDAO.save(indicador);
    }
    
    // Actualizar indicador
    @Transactional
    public IndicadorRiesgo actualizarIndicador(Long idIndicador, IndicadorRiesgoDTO dto) {
        logger.info("Actualizando indicador ID: {}", idIndicador);
        
        Optional<IndicadorRiesgo> optionalIndicador = indicadorRiesgoDAO.findById(idIndicador);
        if (optionalIndicador.isEmpty()) {
            throw new RuntimeException("Indicador no encontrado con ID: " + idIndicador);
        }
        
        IndicadorRiesgo indicador = optionalIndicador.get();
        indicador.setNombre(dto.getNombre());
        indicador.setDescripcion(dto.getDescripcion());
        indicador.setTipoIndicador(dto.getTipoIndicador());
        indicador.setUmbralCritico(dto.getUmbralCritico());
        indicador.setUmbralAdvertencia(dto.getUmbralAdvertencia());
        indicador.setValorActual(dto.getValorActual());
        indicador.setUnidadMedida(dto.getUnidadMedida());
        indicador.setFrecuenciaMedicion(dto.getFrecuenciaMedicion());
        indicador.setActivo(dto.getActivo());
        
        return indicadorRiesgoDAO.save(indicador);
    }
    
    // Desactivar indicador
    @Transactional
    public void desactivarIndicador(Long idIndicador) {
        logger.info("Desactivando indicador ID: {}", idIndicador);
        
        Optional<IndicadorRiesgo> optionalIndicador = indicadorRiesgoDAO.findById(idIndicador);
        if (optionalIndicador.isPresent()) {
            IndicadorRiesgo indicador = optionalIndicador.get();
            indicador.setActivo(false);
            indicadorRiesgoDAO.save(indicador);
        }
    }
    
    // Convertir Entity a DTO
    public IndicadorRiesgoDTO convertirADTO(IndicadorRiesgo indicador) {
        IndicadorRiesgoDTO dto = new IndicadorRiesgoDTO();
        dto.setIdIndicador(indicador.getIdIndicador());
        dto.setNombre(indicador.getNombre());
        dto.setDescripcion(indicador.getDescripcion());
        dto.setTipoIndicador(indicador.getTipoIndicador());
        dto.setUmbralCritico(indicador.getUmbralCritico());
        dto.setUmbralAdvertencia(indicador.getUmbralAdvertencia());
        dto.setValorActual(indicador.getValorActual());
        dto.setUnidadMedida(indicador.getUnidadMedida());
        dto.setFrecuenciaMedicion(indicador.getFrecuenciaMedicion());
        dto.setEstadoActual(indicador.getEstadoActual());
        dto.setUltimaActualizacion(indicador.getUltimaActualizacion());
        dto.setFechaCreacion(indicador.getFechaCreacion());
        dto.setActivo(indicador.getActivo());
        dto.setUsuarioCreacion(indicador.getUsuarioCreacion());
        return dto;
    }
}
