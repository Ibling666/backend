package com.usei.usei.controllers;

import com.usei.usei.dto.RiesgoEventoDTO;
import com.usei.usei.models.RiesgoEvento;
import com.usei.usei.repositories.RiesgoEventoDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RiesgoEventoBL {
    
    private static final Logger logger = LoggerFactory.getLogger(RiesgoEventoBL.class);
    
    @Autowired
    private RiesgoEventoDAO riesgoEventoDAO;
    
    @Autowired
    private IndicadorRiesgoBL indicadorRiesgoBL;
    
    // Crear nuevo evento de riesgo
    @Transactional
    public RiesgoEvento crearRiesgoEvento(RiesgoEventoDTO dto) {
        logger.info("Creando nuevo evento de riesgo: {}", dto.getTitulo());
        
        RiesgoEvento riesgo = new RiesgoEvento();
        riesgo.setTitulo(dto.getTitulo());
        riesgo.setDescripcion(dto.getDescripcion());
        riesgo.setCategoria(dto.getCategoria());
        riesgo.setProbabilidad(dto.getProbabilidad());
        riesgo.setImpacto(dto.getImpacto());
        riesgo.setConsecuencias(dto.getConsecuencias());
        riesgo.setPlanAccion(dto.getPlanAccion());
        riesgo.setFechaIdentificacion(dto.getFechaIdentificacion() != null ? dto.getFechaIdentificacion() : LocalDate.now());
        riesgo.setResponsable(dto.getResponsable());
        riesgo.setEstado(dto.getEstado() != null ? dto.getEstado() : "Identificado");
        riesgo.setUsuarioRegistro(dto.getUsuarioRegistro());
        
        // El cálculo del nivel de riesgo se hace automáticamente en @PrePersist
        
        RiesgoEvento riesgoGuardado = riesgoEventoDAO.save(riesgo);
        
        // Crear KRI automáticamente para este riesgo
        try {
            indicadorRiesgoBL.crearKRIDesdeRiesgo(riesgoGuardado);
        } catch (Exception e) {
            logger.warn("Error al crear KRI para el riesgo: {}", e.getMessage());
        }
        
        return riesgoGuardado;
    }
    
    // Actualizar evento de riesgo
    @Transactional
    public RiesgoEvento actualizarRiesgoEvento(Long idRiesgo, RiesgoEventoDTO dto) {
        logger.info("Actualizando evento de riesgo ID: {}", idRiesgo);
        
        Optional<RiesgoEvento> optionalRiesgo = riesgoEventoDAO.findById(idRiesgo);
        if (optionalRiesgo.isEmpty()) {
            throw new RuntimeException("Evento de riesgo no encontrado con ID: " + idRiesgo);
        }
        
        RiesgoEvento riesgo = optionalRiesgo.get();
        riesgo.setTitulo(dto.getTitulo());
        riesgo.setDescripcion(dto.getDescripcion());
        riesgo.setCategoria(dto.getCategoria());
        riesgo.setProbabilidad(dto.getProbabilidad());
        riesgo.setImpacto(dto.getImpacto());
        riesgo.setConsecuencias(dto.getConsecuencias());
        riesgo.setPlanAccion(dto.getPlanAccion());
        riesgo.setFechaIdentificacion(dto.getFechaIdentificacion());
        riesgo.setResponsable(dto.getResponsable());
        riesgo.setEstado(dto.getEstado());
        
        // El cálculo del nivel de riesgo se hace automáticamente en @PreUpdate
        
        RiesgoEvento riesgoActualizado = riesgoEventoDAO.save(riesgo);
        
        // Actualizar KRI automáticamente después de actualizar el riesgo
        try {
            indicadorRiesgoBL.crearKRIDesdeRiesgo(riesgoActualizado);
        } catch (Exception e) {
            logger.warn("Error al actualizar KRI: {}", e.getMessage());
        }
        
        return riesgoActualizado;
    }
    
    // Obtener todos los eventos de riesgo
    public List<RiesgoEvento> obtenerTodosLosRiesgos() {
        return riesgoEventoDAO.findAll();
    }
    
    // Obtener evento de riesgo por ID
    public Optional<RiesgoEvento> obtenerRiesgoPorId(Long idRiesgo) {
        return riesgoEventoDAO.findById(idRiesgo);
    }
    
    // Obtener riesgos activos ordenados por criticidad
    public List<RiesgoEvento> obtenerRiesgosActivos() {
        return riesgoEventoDAO.findActiveRisks();
    }
    
    // Obtener riesgos por nivel
    public List<RiesgoEvento> obtenerRiesgosPorNivel(String nivelRiesgo) {
        return riesgoEventoDAO.findByNivelRiesgo(nivelRiesgo);
    }
    
    // Obtener riesgos por estado
    public List<RiesgoEvento> obtenerRiesgosPorEstado(String estado) {
        return riesgoEventoDAO.findByEstado(estado);
    }
    
    // Eliminar evento de riesgo
    @Transactional
    public void eliminarRiesgoEvento(Long idRiesgo) {
        logger.info("Eliminando evento de riesgo ID: {}", idRiesgo);
        riesgoEventoDAO.deleteById(idRiesgo);
    }
    
    // Obtener estadísticas de riesgos
    public Map<String, Object> obtenerEstadisticasRiesgos() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        List<RiesgoEvento> todosRiesgos = riesgoEventoDAO.findAll();
        List<RiesgoEvento> riesgosActivos = riesgoEventoDAO.findActiveRisks();
        
        estadisticas.put("totalRiesgos", todosRiesgos.size());
        estadisticas.put("riesgosActivos", riesgosActivos.size());
        
        // Contar por nivel
        Map<String, Long> porNivel = riesgosActivos.stream()
            .collect(Collectors.groupingBy(RiesgoEvento::getNivelRiesgo, Collectors.counting()));
        estadisticas.put("porNivel", porNivel);
        
        // Contar por estado
        Map<String, Long> porEstado = todosRiesgos.stream()
            .collect(Collectors.groupingBy(RiesgoEvento::getEstado, Collectors.counting()));
        estadisticas.put("porEstado", porEstado);
        
        // Contar por categoría
        Map<String, Long> porCategoria = todosRiesgos.stream()
            .collect(Collectors.groupingBy(RiesgoEvento::getCategoria, Collectors.counting()));
        estadisticas.put("porCategoria", porCategoria);
        
        // Riesgos críticos
        long riesgosCriticos = riesgosActivos.stream()
            .filter(r -> "Crítico".equals(r.getNivelRiesgo()))
            .count();
        estadisticas.put("riesgosCriticos", riesgosCriticos);
        
        // Riesgos altos
        long riesgosAltos = riesgosActivos.stream()
            .filter(r -> "Alto".equals(r.getNivelRiesgo()))
            .count();
        estadisticas.put("riesgosAltos", riesgosAltos);
        
        return estadisticas;
    }
    
    // Convertir Entity a DTO
    public RiesgoEventoDTO convertirADTO(RiesgoEvento riesgo) {
        RiesgoEventoDTO dto = new RiesgoEventoDTO();
        dto.setIdRiesgo(riesgo.getIdRiesgo());
        dto.setTitulo(riesgo.getTitulo());
        dto.setDescripcion(riesgo.getDescripcion());
        dto.setCategoria(riesgo.getCategoria());
        dto.setProbabilidad(riesgo.getProbabilidad());
        dto.setImpacto(riesgo.getImpacto());
        dto.setNivelRiesgo(riesgo.getNivelRiesgo());
        dto.setValorRiesgo(riesgo.getValorRiesgo());
        dto.setConsecuencias(riesgo.getConsecuencias());
        dto.setPlanAccion(riesgo.getPlanAccion());
        dto.setFechaRegistro(riesgo.getFechaRegistro());
        dto.setFechaIdentificacion(riesgo.getFechaIdentificacion());
        dto.setResponsable(riesgo.getResponsable());
        dto.setEstado(riesgo.getEstado());
        dto.setFechaActualizacion(riesgo.getFechaActualizacion());
        dto.setUsuarioRegistro(riesgo.getUsuarioRegistro());
        return dto;
    }
}
