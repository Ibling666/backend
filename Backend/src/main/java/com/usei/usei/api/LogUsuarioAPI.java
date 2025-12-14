package com.usei.usei.api;

import com.usei.usei.util.TokenGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/log-usuario")
@CrossOrigin(origins = "*")
public class LogUsuarioAPI {

    private final JdbcTemplate jdbcTemplate;
    private final TokenGenerator tokenGenerator;

    @Autowired
    public LogUsuarioAPI(JdbcTemplate jdbcTemplate, TokenGenerator tokenGenerator) {
        this.jdbcTemplate = jdbcTemplate;
        this.tokenGenerator = tokenGenerator;
    }

    /**
     * LISTAR LOGS (para tu tabla)
     */
    @GetMapping
    public ResponseEntity<?> listarLogs(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Si quieres que listar también sea protegido por token, valida aquí:
        Integer userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "No autenticado",
                    "message", "Token inválido o no proporcionado"
            ));
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT
                  id_log,
                  fecha_log,
                  tipo_log,
                  usuario_id_usuario,
                  modulo,
                  motivo,
                  nivel,
                  mensaje,
                  detalle
                FROM log_usuario
                ORDER BY fecha_log DESC
                """);

        return ResponseEntity.ok(rows);
    }

    /**
     * AUDITORÍA: registrar acceso al módulo "Revisión de Logs"
     * Front: POST /log-usuario/auditoria/acceso
     */
    @PostMapping("/auditoria/acceso")
    public ResponseEntity<?> auditarAccesoLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        Integer userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "No autenticado",
                    "message", "Token inválido o no proporcionado (userId no encontrado)"
            ));
        }

        // detalle opcional, pero en tu BD ES NOT NULL -> siempre mandamos algo
        String detalle = "";
        if (body != null && body.get("detalle") != null) {
            detalle = String.valueOf(body.get("detalle")).trim();
        }
        if (detalle.isBlank()) {
            detalle = "El usuario abrió la pantalla de Revisión de Logs";
        }

        // Puedes ajustar estos textos si quieres
        String tipoLog = "SEGURIDAD";
        String modulo = "Revisión de Logs";
        String motivo = "ACCESO";
        String nivel = "INFO";
        String mensaje = "ACCESO_MODULO";

        // Insert asegurando detalle NO NULL
        jdbcTemplate.update("""
                INSERT INTO log_usuario
                (fecha_log, tipo_log, usuario_id_usuario, modulo, motivo, nivel, mensaje, detalle)
                VALUES (NOW(), ?, ?, ?, ?, ?, ?, ?)
                """, tipoLog, userId, modulo, motivo, nivel, mensaje, detalle);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Auditoría registrada",
                "userId", userId
        ));
    }

    /**
     * Extrae userId desde JWT (CLAIM "id")
     */
    private Integer extractUserIdFromToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) return null;

        Jws<Claims> claims = tokenGenerator.validateAndParseToken(authHeader);
        if (claims == null) return null;

        Object idObj = claims.getBody().get("id"); // tu token guarda "id"
        if (idObj == null) return null;

        try {
            // puede venir como String o Number
            if (idObj instanceof Number n) return n.intValue();
            return Integer.parseInt(String.valueOf(idObj));
        } catch (Exception e) {
            return null;
        }
    }
}
