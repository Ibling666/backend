package com.usei.usei.api;

import com.usei.usei.controllers.IndicadorRiesgoBL;
import com.usei.usei.dto.IndicadorRiesgoDTO;
import com.usei.usei.dto.RiesgoResponse;
import com.usei.usei.models.IndicadorRiesgo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/indicadores")
@CrossOrigin(origins = "*")
public class IndicadorRiesgoAPI {

    private static final Logger logger = LoggerFactory.getLogger(IndicadorRiesgoAPI.class);

    @Autowired
    private IndicadorRiesgoBL indicadorRiesgoBL;

    // Obtener todos los indicadores activos
    @GetMapping
    public ResponseEntity<?> obtenerIndicadoresActivos() {
        try {
            logger.info("Solicitud para obtener indicadores activos");

            List<IndicadorRiesgo> indicadores = indicadorRiesgoBL.obtenerIndicadoresActivos();
            List<IndicadorRiesgoDTO> respuesta = indicadores.stream()
                    .map(i -> indicadorRiesgoBL.convertirADTO(i))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new RiesgoResponse<>(true, "Indicadores obtenidos", respuesta));

        } catch (Exception e) {
            logger.error("Error al obtener indicadores", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RiesgoResponse<>(false, "Error al obtener indicadores: " + e.getMessage(), null));
        }
    }

    // Obtener indicador por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerIndicadorPorId(@PathVariable Long id) {
        try {
            logger.info("Solicitud para obtener indicador ID: {}", id);

            Optional<IndicadorRiesgo> indicador = indicadorRiesgoBL.obtenerIndicadorPorId(id);

            if (indicador.isPresent()) {
                IndicadorRiesgoDTO respuesta = indicadorRiesgoBL.convertirADTO(indicador.get());
                return ResponseEntity.ok(new RiesgoResponse<>(true, "Indicador encontrado", respuesta));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new RiesgoResponse<>(false, "Indicador no encontrado", null));
            }

        } catch (Exception e) {
            logger.error("Error al obtener indicador", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RiesgoResponse<>(false, "Error al obtener indicador: " + e.getMessage(), null));
        }
    }

    // Obtener indicadores en alerta
    @GetMapping("/alertas")
    public ResponseEntity<?> obtenerIndicadoresEnAlerta() {
        try {
            logger.info("Solicitud para obtener indicadores en alerta");

            List<IndicadorRiesgo> indicadores = indicadorRiesgoBL.obtenerIndicadoresEnAlerta();
            List<IndicadorRiesgoDTO> respuesta = indicadores.stream()
                    .map(i -> indicadorRiesgoBL.convertirADTO(i))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new RiesgoResponse<>(true, "Indicadores en alerta obtenidos", respuesta));

        } catch (Exception e) {
            logger.error("Error al obtener indicadores en alerta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RiesgoResponse<>(false, "Error al obtener indicadores en alerta: " + e.getMessage(), null));
        }
    }

    // Actualizar valor de indicador
    @PatchMapping("/{id}/valor")
    public ResponseEntity<?> actualizarValorIndicador(
            @PathVariable Long id,
            @RequestBody BigDecimal nuevoValor) {
        try {
            logger.info("Solicitud para actualizar valor de indicador ID: {} a {}", id, nuevoValor);

            IndicadorRiesgo indicador = indicadorRiesgoBL.actualizarValorIndicador(id, nuevoValor);
            IndicadorRiesgoDTO respuesta = indicadorRiesgoBL.convertirADTO(indicador);

            return ResponseEntity.ok(new RiesgoResponse<>(true, "Valor de indicador actualizado", respuesta));

        } catch (RuntimeException e) {
            logger.error("Error al actualizar valor de indicador", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new RiesgoResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error al actualizar valor de indicador", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RiesgoResponse<>(false, "Error al actualizar valor: " + e.getMessage(), null));
        }
    }

    // Crear nuevo indicador
    @PostMapping
    public ResponseEntity<?> crearIndicador(@RequestBody IndicadorRiesgoDTO dto) {
        try {
            logger.info("Solicitud para crear indicador: {}", dto.getNombre());

            IndicadorRiesgo indicador = indicadorRiesgoBL.crearIndicador(dto);
            IndicadorRiesgoDTO respuesta = indicadorRiesgoBL.convertirADTO(indicador);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RiesgoResponse<>(true, "Indicador creado exitosamente", respuesta));

        } catch (Exception e) {
            logger.error("Error al crear indicador", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RiesgoResponse<>(false, "Error al crear indicador: " + e.getMessage(), null));
        }
    }

    // Actualizar indicador completo
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarIndicador(@PathVariable Long id, @RequestBody IndicadorRiesgoDTO dto) {
        try {
            logger.info("Solicitud para actualizar indicador ID: {}", id);

            IndicadorRiesgo indicador = indicadorRiesgoBL.actualizarIndicador(id, dto);
            IndicadorRiesgoDTO respuesta = indicadorRiesgoBL.convertirADTO(indicador);

            return ResponseEntity.ok(new RiesgoResponse<>(true, "Indicador actualizado exitosamente", respuesta));

        } catch (RuntimeException e) {
            logger.error("Error al actualizar indicador", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new RiesgoResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error al actualizar indicador", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RiesgoResponse<>(false, "Error al actualizar indicador: " + e.getMessage(), null));
        }
    }

    // Desactivar indicador
    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarIndicador(@PathVariable Long id) {
        try {
            logger.info("Solicitud para desactivar indicador ID: {}", id);

            indicadorRiesgoBL.desactivarIndicador(id);

            return ResponseEntity.ok(new RiesgoResponse<>(true, "Indicador desactivado exitosamente", null));

        } catch (Exception e) {
            logger.error("Error al desactivar indicador", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RiesgoResponse<>(false, "Error al desactivar indicador: " + e.getMessage(), null));
        }
    }
}