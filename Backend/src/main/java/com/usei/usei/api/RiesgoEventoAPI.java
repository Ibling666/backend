package com.usei.usei.api;

import com.usei.usei.controllers.RiesgoEventoBL;
import com.usei.usei.dto.RiesgoEventoDTO;
import com.usei.usei.dto.RiesgoResponse;
import com.usei.usei.models.RiesgoEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/riesgos")
@CrossOrigin(origins = "*")
public class RiesgoEventoAPI {
    
    private static final Logger logger = LoggerFactory.getLogger(RiesgoEventoAPI.class);
    
    @Autowired
    private RiesgoEventoBL riesgoEventoBL;
    
    // Crear nuevo evento de riesgo
    @PostMapping
    public ResponseEntity<?> crearRiesgoEvento(@RequestBody RiesgoEventoDTO dto) {
        try {
            logger.info("Solicitud para crear evento de riesgo: {}", dto.getTitulo());
            
            RiesgoEvento riesgo = riesgoEventoBL.crearRiesgoEvento(dto);
            RiesgoEventoDTO respuesta = riesgoEventoBL.convertirADTO(riesgo);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RiesgoResponse<>(true, "Evento de riesgo creado exitosamente", respuesta));
                
        } catch (Exception e) {
            logger.error("Error al crear evento de riesgo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RiesgoResponse<>(false, "Error al crear evento de riesgo: " + e.getMessage(), null));
        }
    }
    
    // Obtener todos los eventos de riesgo
    @GetMapping
    public ResponseEntity<?> obtenerTodosLosRiesgos() {
        try {
            logger.info("Solicitud para obtener todos los eventos de riesgo");
            
            List<RiesgoEvento> riesgos = riesgoEventoBL.obtenerTodosLosRiesgos();
            List<RiesgoEventoDTO> respuesta = riesgos.stream()
                .map(r -> riesgoEventoBL.convertirADTO(r))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(new RiesgoResponse<>(true, "Eventos de riesgo obtenidos", respuesta));
            
        } catch (Exception e) {
            logger.error("Error al obtener eventos de riesgo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RiesgoResponse<>(false, "Error al obtener eventos de riesgo: " + e.getMessage(), null));
        }
    }
    
    // Obtener evento de riesgo por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerRiesgoPorId(@PathVariable Long id) {
        try {
            logger.info("Solicitud para obtener evento de riesgo ID: {}", id);
            
            Optional<RiesgoEvento> riesgo = riesgoEventoBL.obtenerRiesgoPorId(id);
            
            if (riesgo.isPresent()) {
                RiesgoEventoDTO respuesta = riesgoEventoBL.convertirADTO(riesgo.get());
                return ResponseEntity.ok(new RiesgoResponse<>(true, "Evento de riesgo encontrado", respuesta));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new RiesgoResponse<>(false, "Evento de riesgo no encontrado", null));
            }
            
        } catch (Exception e) {
            logger.error("Error al obtener evento de riesgo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RiesgoResponse<>(false, "Error al obtener evento de riesgo: " + e.getMessage(), null));
        }
    }
    
    // Obtener riesgos activos (no cerrados)
    @GetMapping("/activos")
    public ResponseEntity<?> obtenerRiesgosActivos() {
        try {
            logger.info("Solicitud para obtener riesgos activos");
            
            List<RiesgoEvento> riesgos = riesgoEventoBL.obtenerRiesgosActivos();
            List<RiesgoEventoDTO> respuesta = riesgos.stream()
                .map(r -> riesgoEventoBL.convertirADTO(r))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(new RiesgoResponse<>(true, "Riesgos activos obtenidos", respuesta));
            
        } catch (Exception e) {
            logger.error("Error al obtener riesgos activos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RiesgoResponse<>(false, "Error al obtener riesgos activos: " + e.getMessage(), null));
        }
    }
    
    // Obtener riesgos por nivel
    @GetMapping("/nivel/{nivelRiesgo}")
    public ResponseEntity<?> obtenerRiesgosPorNivel(@PathVariable String nivelRiesgo) {
        try {
            logger.info("Solicitud para obtener riesgos de nivel: {}", nivelRiesgo);
            
            List<RiesgoEvento> riesgos = riesgoEventoBL.obtenerRiesgosPorNivel(nivelRiesgo);
            List<RiesgoEventoDTO> respuesta = riesgos.stream()
                .map(r -> riesgoEventoBL.convertirADTO(r))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(new RiesgoResponse<>(true, "Riesgos por nivel obtenidos", respuesta));
            
        } catch (Exception e) {
            logger.error("Error al obtener riesgos por nivel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RiesgoResponse<>(false, "Error al obtener riesgos por nivel: " + e.getMessage(), null));
        }
    }
    
    // Obtener riesgos por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> obtenerRiesgosPorEstado(@PathVariable String estado) {
        try {
            logger.info("Solicitud para obtener riesgos en estado: {}", estado);
            
            List<RiesgoEvento> riesgos = riesgoEventoBL.obtenerRiesgosPorEstado(estado);
            List<RiesgoEventoDTO> respuesta = riesgos.stream()
                .map(r -> riesgoEventoBL.convertirADTO(r))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(new RiesgoResponse<>(true, "Riesgos por estado obtenidos", respuesta));
            
        } catch (Exception e) {
            logger.error("Error al obtener riesgos por estado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RiesgoResponse<>(false, "Error al obtener riesgos por estado: " + e.getMessage(), null));
        }
    }
    
    // Actualizar evento de riesgo
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarRiesgoEvento(@PathVariable Long id, @RequestBody RiesgoEventoDTO dto) {
        try {
            logger.info("Solicitud para actualizar evento de riesgo ID: {}", id);
            
            RiesgoEvento riesgo = riesgoEventoBL.actualizarRiesgoEvento(id, dto);
            RiesgoEventoDTO respuesta = riesgoEventoBL.convertirADTO(riesgo);
            
            return ResponseEntity.ok(new RiesgoResponse<>(true, "Evento de riesgo actualizado exitosamente", respuesta));
            
        } catch (RuntimeException e) {
            logger.error("Error al actualizar evento de riesgo", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RiesgoResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error al actualizar evento de riesgo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RiesgoResponse<>(false, "Error al actualizar evento de riesgo: " + e.getMessage(), null));
        }
    }
    
    // Eliminar evento de riesgo
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarRiesgoEvento(@PathVariable Long id) {
        try {
            logger.info("Solicitud para eliminar evento de riesgo ID: {}", id);
            
            riesgoEventoBL.eliminarRiesgoEvento(id);
            
            return ResponseEntity.ok(new RiesgoResponse<>(true, "Evento de riesgo eliminado exitosamente", null));
            
        } catch (Exception e) {
            logger.error("Error al eliminar evento de riesgo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RiesgoResponse<>(false, "Error al eliminar evento de riesgo: " + e.getMessage(), null));
        }
    }
    
    // Obtener estadísticas de riesgos
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasRiesgos() {
        try {
            logger.info("Solicitud para obtener estadísticas de riesgos");
            
            Map<String, Object> estadisticas = riesgoEventoBL.obtenerEstadisticasRiesgos();
            
            return ResponseEntity.ok(new RiesgoResponse<>(true, "Estadísticas obtenidas", estadisticas));
            
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de riesgos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RiesgoResponse<>(false, "Error al obtener estadísticas: " + e.getMessage(), null));
        }
    }
}
