package com.usei.usei.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.usei.usei.controllers.RolBL;
import com.usei.usei.models.Rol;

import java.util.Arrays;

@RestController
@RequestMapping("/rol")
@CrossOrigin(origins = "*")
public class RolAPI {

    @Autowired
    private RolBL rolBL;

    // CREAR ROL
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Rol rol) {
        try {
            if (rol.getNombreRol() == null || rol.getNombreRol().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El nombre del rol es obligatorio.");
            }

            Rol saved = rolBL.crearRolCompleto(rol);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al crear rol: " + ex.getMessage());
        }
    }

    // Listar roles
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(rolBL.listarRoles());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar roles: " + ex.getMessage());
        }
    }

    // Eliminar rol
    @DeleteMapping("/{idRol}")
    public ResponseEntity<?> eliminar(@PathVariable Long idRol) {
        try {
            rolBL.eliminarRol(idRol);
            return ResponseEntity.ok("Rol eliminado correctamente.");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar rol: " + ex.getMessage());
        }
    }

    // Cambiar estado (activar/desactivar rol)
    @PutMapping("/{idRol}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long idRol, @RequestBody Rol rolRequest) {
        try {
            boolean nuevoEstado = rolRequest.isActivo();
            Rol actualizado = rolBL.cambiarEstadoRol(idRol, nuevoEstado);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar el estado del rol: " + ex.getMessage());
        }
    }

    // Obtener rol por ID
    @GetMapping("/{idRol}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long idRol) {
        Rol rol = rolBL.obtenerRolPorId(idRol);
        if (rol == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rol no encontrado.");
        }
        return ResponseEntity.ok(rol);
    }
     //Modificar rol
    @PutMapping("/{idRol}")
    public ResponseEntity<?> actualizarRol(@PathVariable Long idRol, @RequestBody Rol rolActualizado) {
        try {
            Rol actualizado = rolBL.actualizarRol(idRol, rolActualizado);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el rol: " + ex.getMessage());
        }
    }

    //VERIFICAR ACCESO DE AUCERDO AL ROL Y SUS ACCESOS DEFINIDOS
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

    // Obtener los accesos
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
