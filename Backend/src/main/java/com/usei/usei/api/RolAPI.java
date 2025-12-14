package com.usei.usei.api;

import java.util.Map;
import java.util.Arrays;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.usei.usei.controllers.RolBL;
import com.usei.usei.controllers.UsuarioService;
import com.usei.usei.controllers.LogUsuarioService;

import com.usei.usei.models.Rol;
import com.usei.usei.models.Usuario;

import com.usei.usei.util.TokenGenerator;

@RestController
@RequestMapping("/rol")
@CrossOrigin(origins = "*")
public class RolAPI {

    @Autowired
    private RolBL rolBL;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private LogUsuarioService logUsuarioService;

    // =========================
    // HELPERS (token -> Usuario)
    // =========================

    private Usuario getUsuarioFromToken(String authHeader) {
        try {
            if (authHeader == null || authHeader.isBlank()) return null;

            // TokenGenerator ya soporta "Bearer "
            Jws<Claims> claims = tokenGenerator.validateAndParseToken(authHeader);
            if (claims == null) return null;

            Object idObj = claims.getBody().get("id"); // tu token guarda "id"
            if (idObj == null) return null;

            Long userId;
            if (idObj instanceof Number n) userId = n.longValue();
            else userId = Long.parseLong(String.valueOf(idObj));

            return usuarioService.findById(userId).orElse(null);

        } catch (Exception e) {
            return null;
        }
    }

    // tu columna detalle es length=255 (según tu LogUsuario), aquí lo limito por si acaso
    private String safeDetalle(String s) {
        if (s == null) return "SIN_DETALLE";
        s = String.valueOf(s).trim();
        if (s.isBlank()) s = "SIN_DETALLE";
        return (s.length() > 255) ? s.substring(0, 255) : s;
    }

    /**
     * Registra log sin romper el endpoint.
     * Si no hay token válido, simplemente no registra.
     */
    private void tryLog(String authHeader, String modulo, String tipo, String motivo, String nivel, String mensaje, String detalle) {
        try {
            Usuario authUser = getUsuarioFromToken(authHeader);
            if (authUser == null) return;

            logUsuarioService.registrarLog(
                    authUser,
                    modulo,    // ej: "Módulo Gestión de Roles"
                    tipo,      // ej: "ROL"
                    motivo,    // ej: "CREAR_ROL"
                    nivel,     // "INFO"
                    mensaje,   // texto corto
                    safeDetalle(detalle)
            );
        } catch (Exception ignored) {
            // Nunca romper funcionalidad por un log
        }
    }

    // ============ ENDPOINTS ============

    // CREAR ROL
    @PostMapping
    public ResponseEntity<?> crear(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Rol rol
    ) {
        try {
            if (rol.getNombreRol() == null || rol.getNombreRol().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El nombre del rol es obligatorio.");
            }

            Rol saved = rolBL.crearRolCompleto(rol);

            tryLog(
                    authHeader,
                    "Módulo Gestión de Roles",
                    "ROL",
                    "CREAR_ROL",
                    "INFO",
                    "Se creó un rol",
                    "Creó el rol '" + saved.getNombreRol() + "' (ID: " + saved.getIdRol() + ")"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (RuntimeException ex) {
            tryLog(
                    authHeader,
                    "Módulo Gestión de Roles",
                    "ROL",
                    "CREAR_ROL_ERROR",
                    "ERROR",
                    "Error al crear rol",
                    "Error: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

        } catch (Exception ex) {
            ex.printStackTrace();
            tryLog(
                    authHeader,
                    "Módulo Gestión de Roles",
                    "ROL",
                    "CREAR_ROL_ERROR",
                    "ERROR",
                    "Error inesperado al crear rol",
                    "Error inesperado: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al crear rol: " + ex.getMessage());
        }
    }

    // LISTAR ROLES (NO LOG EN GET)
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            Iterable<Rol> roles = rolBL.listarRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar roles: " + ex.getMessage());
        }
    }

    // OBTENER ROL POR ID (NO LOG EN GET)
    @GetMapping("/{idRol}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long idRol) {
        Rol rol = rolBL.obtenerRolPorId(idRol);
        if (rol == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rol no encontrado.");
        }
        return ResponseEntity.ok(rol);
    }

    // ELIMINAR ROL
    @DeleteMapping("/{idRol}")
    public ResponseEntity<?> eliminar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idRol
    ) {
        try {
            Rol antes = rolBL.obtenerRolPorId(idRol);
            if (antes == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rol no encontrado.");
            }

            rolBL.eliminarRol(idRol);

            tryLog(
                    authHeader,
                    "Módulo Gestión de Roles",
                    "ROL",
                    "ELIMINAR_ROL",
                    "INFO",
                    "Se eliminó un rol",
                    "Eliminó el rol '" + antes.getNombreRol() + "' (ID: " + idRol + ")"
            );

            return ResponseEntity.ok("Rol eliminado correctamente.");

        } catch (RuntimeException ex) {
            tryLog(
                    authHeader,
                    "Módulo Gestión de Roles",
                    "ROL",
                    "ELIMINAR_ROL_ERROR",
                    "ERROR",
                    "Error al eliminar rol",
                    "ID=" + idRol + " | Error: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

        } catch (Exception ex) {
            ex.printStackTrace();
            tryLog(
                    authHeader,
                    "Módulo Gestión de Roles",
                    "ROL",
                    "ELIMINAR_ROL_ERROR",
                    "ERROR",
                    "Error inesperado al eliminar rol",
                    "ID=" + idRol + " | Error: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar rol: " + ex.getMessage());
        }
    }

    // CAMBIAR ESTADO (activar/desactivar rol)
    @PutMapping("/{idRol}/estado")
    public ResponseEntity<?> cambiarEstado(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idRol,
            @RequestBody Rol rolRequest
    ) {
        try {
            Rol antes = rolBL.obtenerRolPorId(idRol);
            if (antes == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rol no encontrado.");
            }

            boolean estadoAntes = Boolean.TRUE.equals(antes.getActivo());
            boolean nuevoEstado = rolRequest.isActivo();

            Rol actualizado = rolBL.cambiarEstadoRol(idRol, nuevoEstado);

            tryLog(
                    authHeader,
                    "Módulo Gestión de Roles",
                    "ROL",
                    "CAMBIO_ESTADO_ROL",
                    "INFO",
                    "Se cambió el estado de un rol",
                    "Rol '" + actualizado.getNombreRol() + "' (ID: " + idRol + ") activo: " + estadoAntes + " → " + nuevoEstado
            );

            return ResponseEntity.ok(actualizado);

        } catch (RuntimeException ex) {
            tryLog(
                    authHeader,
                    "Módulo Gestión de Roles",
                    "ROL",
                    "CAMBIO_ESTADO_ROL_ERROR",
                    "ERROR",
                    "Error al cambiar estado de rol",
                    "ID=" + idRol + " | Error: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

        } catch (Exception ex) {
            ex.printStackTrace();
            tryLog(
                    authHeader,
                    "Módulo Gestión de Roles",
                    "ROL",
                    "CAMBIO_ESTADO_ROL_ERROR",
                    "ERROR",
                    "Error inesperado al cambiar estado de rol",
                    "ID=" + idRol + " | Error: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar el estado del rol: " + ex.getMessage());
        }
    }

    // ACTUALIZAR ROL
    @PutMapping("/{idRol}")
    public ResponseEntity<?> actualizarRol(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idRol,
            @RequestBody Rol rolActualizado
    ) {
        try {
            Rol antes = rolBL.obtenerRolPorId(idRol);
            if (antes == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rol no encontrado.");
            }

            String nombreAntes = antes.getNombreRol();
            boolean activoAntes = Boolean.TRUE.equals(antes.getActivo());
            String accesosAntes = (antes.getAccesos() == null) ? "" : antes.getAccesos();

            Rol actualizado = rolBL.actualizarRol(idRol, rolActualizado);

            String detalle = "Rol '" + nombreAntes + "' (ID: " + idRol + ") → '" + actualizado.getNombreRol() + "'. "
                    + "Activo: " + activoAntes + " → " + actualizado.isActivo() + ". "
                    + "Accesos: " + (accesosAntes.isBlank() ? "Sin accesos" : "Actualizados")
                    + " → " + ((actualizado.getAccesos() == null || actualizado.getAccesos().isBlank()) ? "Sin accesos" : "Actualizados");

            tryLog(
                    authHeader,
                    "Módulo Gestión de Roles",
                    "ROL",
                    "ACTUALIZAR_ROL",
                    "INFO",
                    "Se actualizó un rol",
                    detalle
            );

            return ResponseEntity.ok(actualizado);

        } catch (RuntimeException ex) {
            tryLog(
                    authHeader,
                    "Módulo Gestión de Roles",
                    "ROL",
                    "ACTUALIZAR_ROL_ERROR",
                    "ERROR",
                    "Error al actualizar rol",
                    "ID=" + idRol + " | Error: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

        } catch (Exception ex) {
            ex.printStackTrace();
            tryLog(
                    authHeader,
                    "Módulo Gestión de Roles",
                    "ROL",
                    "ACTUALIZAR_ROL_ERROR",
                    "ERROR",
                    "Error inesperado al actualizar rol",
                    "ID=" + idRol + " | Error: " + ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el rol: " + ex.getMessage());
        }
    }

    // VERIFICAR ACCESO (normalmente se usa sin token desde el frontend)
    @PostMapping("/verificar-acceso")
    public ResponseEntity<?> verificarAcceso(@RequestBody Map<String, Object> request) {
        try {
            Long idRol = Long.parseLong(request.get("idRol").toString());
            String accesoRequerido = request.get("acceso").toString();

            Rol rol = rolBL.obtenerRolPorId(idRol);
            if (rol == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "tieneAcceso", false,
                                "error", "Rol no encontrado"
                        ));
            }

            if (rol.getAccesos() == null || rol.getAccesos().isEmpty()) {
                return ResponseEntity.ok(Map.of("tieneAcceso", false));
            }

            String[] accesos = rol.getAccesos().split(",");
            boolean tieneAcceso = Arrays.stream(accesos)
                    .map(String::trim)
                    .anyMatch(a -> a.equalsIgnoreCase(accesoRequerido));

            return ResponseEntity.ok(Map.of(
                    "tieneAcceso", tieneAcceso,
                    "accesosDisponibles", Arrays.asList(accesos)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al verificar acceso: " + e.getMessage()
                    ));
        }
    }

    // OBTENER ACCESOS
    @GetMapping("/{idRol}/accesos")
    public ResponseEntity<?> obtenerAccesos(@PathVariable Long idRol) {
        try {
            Rol rol = rolBL.obtenerRolPorId(idRol);
            if (rol == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Rol no encontrado"));
            }

            if (rol.getAccesos() == null || rol.getAccesos().isEmpty()) {
                return ResponseEntity.ok(Map.of("accesos", new String[0]));
            }

            String[] accesos = rol.getAccesos().split(",");
            return ResponseEntity.ok(Map.of(
                    "idRol", idRol,
                    "nombreRol", rol.getNombreRol(),
                    "accesos", Arrays.stream(accesos).map(String::trim).toArray()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener accesos: " + e.getMessage()));
        }
    }
}
