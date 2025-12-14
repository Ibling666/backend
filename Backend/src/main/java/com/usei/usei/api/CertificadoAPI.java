package com.usei.usei.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.usei.usei.controllers.CertificadoService;
import com.usei.usei.controllers.UsuarioService;
import com.usei.usei.controllers.LogUsuarioService;

import com.usei.usei.models.Certificado;
import com.usei.usei.models.MessageResponse;
import com.usei.usei.models.Usuario;

import com.usei.usei.util.TokenGenerator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/certificado")
public class CertificadoAPI {

    @Autowired
    private CertificadoService certificadoService;

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

    // tu columna detalle probablemente tiene límite (en tu ejemplo 150 en RolAPI)
    private String safeDetalle(String s) {
        if (s == null) return "SIN_DETALLE";
        s = String.valueOf(s).trim();
        if (s.isBlank()) s = "SIN_DETALLE";
        // Ajusta si tu columna detalle es 255, puedes dejar 255
        return (s.length() > 255) ? s.substring(0, 255) : s;
    }

    /**
     * Registra log sin romper el endpoint.
     * Si no hay token válido, simplemente no registra.
     */
    private void tryLog(String authHeader, String motivo, String nivel, String mensaje, String detalle) {
        try {
            Usuario authUser = getUsuarioFromToken(authHeader);
            if (authUser == null) return;

            // Ajusta TIPO/MODULO a tu estándar de sistema
            // tipo_log: "SEGURIDAD" / "AUDITORIA" / etc.
            // modulo: tu lista del front
            logUsuarioService.registrarLog(
                    authUser,
                    "AUDITORIA",
                    "Módulo Certificados",
                    motivo,
                    nivel,
                    mensaje,
                    safeDetalle(detalle)
            );
        } catch (Exception ignored) {
            // Nunca romper funcionalidad por un log
        }
    }

    // =========================
    // ENDPOINTS
    // =========================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("formato") MultipartFile file,
            @RequestParam("UsuarioIdUsuario") Long usuarioId
    ) {

        System.out.println("ID de usuario recibido: " + usuarioId);

        if (usuarioId == null) {
            return new ResponseEntity<>(new MessageResponse("El ID del usuario no puede ser nulo"), HttpStatus.BAD_REQUEST);
        }

        try {
            // Verificar si la carpeta /formatos existe, si no, crearla
            Path directorioFormatos = Paths.get("src/main/resources/static/documents/formatos");
            if (!Files.exists(directorioFormatos)) {
                Files.createDirectories(directorioFormatos);
            }

            // Guardar archivo
            String rutaAbsoluta = directorioFormatos.toFile().getAbsolutePath();
            Path rutaCompleta = Paths.get(rutaAbsoluta + "//" + file.getOriginalFilename());

            System.out.println("Ruta completa del archivo: " + rutaCompleta);
            Files.write(rutaCompleta, file.getBytes());

            // Crear nuevo certificado
            Certificado certificado = new Certificado();
            certificado.setFormato(file.getOriginalFilename());
            certificado.setVersion(certificadoService.obtenerUltimaVersion() + 1);
            certificado.setEstado("Suspendido"); // por defecto
            certificado.setFechaModificacion(new Date());

            // Asignar usuario del registro (tu modelo exige usuarioIdUsuario)
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(usuarioId);
            certificado.setUsuarioIdUsuario(usuario);

            Certificado saved = certificadoService.save(certificado);

            // ✅ LOG: SUBIR_CERTIFICADO (textual)
            String msg = "Se subió un nuevo certificado al sistema.";
            String det = "Archivo: " + saved.getFormato()
                    + " | Versión: " + saved.getVersion()
                    + " | Estado inicial: " + saved.getEstado()
                    + " | ID Certificado: " + saved.getIdCertificado();
            tryLog(authHeader, "SUBIR_CERTIFICADO", "INFO", msg, det);

            System.out.println("Certificado guardado con éxito");

            return new ResponseEntity<>(new MessageResponse("Certificado registrado exitosamente"), HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("Error al registrar el certificado: " + e.getMessage());

            // ✅ LOG de error (opcional, pero útil)
            tryLog(authHeader, "SUBIR_CERTIFICADO_ERROR", "ERROR",
                    "Ocurrió un error al subir un certificado.",
                    "Error: " + e.getMessage());

            return new ResponseEntity<>(
                    new MessageResponse("Error al registrar el certificado: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/{id_certificado}")
    public ResponseEntity<?> read(@PathVariable(value = "id_certificado") Long id_certificado) {
        Optional<Certificado> oCertificado = certificadoService.findById(id_certificado);
        // ✅ NO LOG EN GET
        return oCertificado.map(certificado -> ResponseEntity.ok(certificado))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<?> readAll(
            @RequestParam(defaultValue = "idCertificado") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortType) {

        Sort sort = sortType.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Iterable<Certificado> certificados = certificadoService.findAll(sort);

        // ✅ NO LOG EN GET
        return ResponseEntity.ok(certificados);
    }

    @PutMapping("/{id_certificado}")
    public ResponseEntity<?> update(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable(value = "id_certificado") Long id_certificado,
            @RequestParam("formato") MultipartFile file,
            @RequestParam("version") int version,
            @RequestParam("fechaModificacion") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaModificacion,
            @RequestParam("UsuarioIdUsuario") Long usuarioId) {

        Optional<Certificado> oCertificado = certificadoService.findById(id_certificado);
        if (oCertificado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path directorioFormatos = Paths.get("src//main//resources//static//documents/formatos");
            String rutaAbsoluta = directorioFormatos.toFile().getAbsolutePath();
            byte[] fileBytes = file.getBytes();
            Path rutaCompleta = Paths.get(rutaAbsoluta + "//" + file.getOriginalFilename());
            Files.write(rutaCompleta, fileBytes);

            oCertificado.get().setFormato(file.getOriginalFilename());
            oCertificado.get().setVersion(version);
            oCertificado.get().setFechaModificacion(fechaModificacion);

            Usuario usuario = new Usuario();
            usuario.setIdUsuario(usuarioId);
            oCertificado.get().setUsuarioIdUsuario(usuario);

            Certificado saved = certificadoService.save(oCertificado.get());

            // (Opcional) log si este endpoint lo usas para editar/subir otra versión
            tryLog(authHeader, "ACTUALIZAR_CERTIFICADO", "INFO",
                    "Se actualizó un certificado.",
                    "ID Certificado: " + saved.getIdCertificado()
                            + " | Archivo: " + saved.getFormato()
                            + " | Versión: " + saved.getVersion());

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            tryLog(authHeader, "ACTUALIZAR_CERTIFICADO_ERROR", "ERROR",
                    "Ocurrió un error al actualizar un certificado.",
                    "ID Certificado: " + id_certificado + " | Error: " + e.getMessage());

            return new ResponseEntity<>(new MessageResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/remision")
    public ResponseEntity<?> enviarCertificado(@RequestParam("idEstudiante") Long idEstudiante) {
        System.out.println("idEstudiante recibido: " + idEstudiante);
        try {
            certificadoService.enviarCertificadoConCondiciones(idEstudiante);

            // ✅ Si quieres log aquí también, me dices y lo metemos con authHeader
            return new ResponseEntity<>(
                    new MessageResponse("Certificado enviado correctamente si se cumplieron las condiciones."),
                    HttpStatus.OK
            );
        } catch (MessagingException e) {
            return new ResponseEntity<>(new MessageResponse("Error al enviar el certificado: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse("Error inesperado: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ CAMBIAR ESTADO (En uso / Suspendido)
    @PutMapping("/{id_certificado}/estado")
    public ResponseEntity<?> updateCertificadoState(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable(value = "id_certificado") Long idCertificado,
            @RequestBody Certificado certificado) {

        Optional<Certificado> existingCertificado = certificadoService.findById(idCertificado);

        if (existingCertificado.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Certificado no encontrado"), HttpStatus.NOT_FOUND);
        }

        try {
            Certificado certificadoToUpdate = existingCertificado.get();
            String estadoAntes = certificadoToUpdate.getEstado();
            String nuevoEstado = certificado.getEstado();

            // Si el estado cambia a "En uso", verificar que no haya otro "En uso"
            if ("En uso".equalsIgnoreCase(nuevoEstado)) {
                Optional<Certificado> certificadoEnUso = certificadoService.findCertificadoEnUso();
                if (certificadoEnUso.isPresent() && !certificadoEnUso.get().getIdCertificado().equals(idCertificado)) {

                    // ✅ LOG intento fallido (opcional pero MUY útil)
                    tryLog(authHeader, "MARCAR_CERTIFICADO_EN_USO_RECHAZADO", "WARN",
                            "Se intentó marcar un certificado como 'En uso', pero ya existe otro en uso.",
                            "Intento sobre ID Certificado: " + idCertificado
                                    + " | Estado actual: " + estadoAntes
                                    + " | Ya en uso: ID=" + certificadoEnUso.get().getIdCertificado()
                                    + " archivo=" + certificadoEnUso.get().getFormato());

                    return new ResponseEntity<>(new MessageResponse("Ya hay otro archivo en uso."), HttpStatus.CONFLICT);
                }
            }

            // Actualizar el estado del certificado
            certificadoToUpdate.setEstado(nuevoEstado);
            certificadoService.save(certificadoToUpdate);

            // ✅ LOG: solo textual y bonito
            if ("En uso".equalsIgnoreCase(nuevoEstado)) {
                tryLog(authHeader, "MARCAR_CERTIFICADO_EN_USO", "INFO",
                        "Se marcó un certificado como 'En uso'.",
                        "Certificado: ID=" + certificadoToUpdate.getIdCertificado()
                                + " | Archivo=" + certificadoToUpdate.getFormato()
                                + " | Estado: " + estadoAntes + " -> " + nuevoEstado);
            } else if ("Suspendido".equalsIgnoreCase(nuevoEstado)) {
                tryLog(authHeader, "SUSPENDER_CERTIFICADO", "INFO",
                        "Se suspendió un certificado.",
                        "Certificado: ID=" + certificadoToUpdate.getIdCertificado()
                                + " | Archivo=" + certificadoToUpdate.getFormato()
                                + " | Estado: " + estadoAntes + " -> " + nuevoEstado);
            } else {
                // Por si luego agregas más estados
                tryLog(authHeader, "CAMBIAR_ESTADO_CERTIFICADO", "INFO",
                        "Se cambió el estado de un certificado.",
                        "Certificado: ID=" + certificadoToUpdate.getIdCertificado()
                                + " | Archivo=" + certificadoToUpdate.getFormato()
                                + " | Estado: " + estadoAntes + " -> " + nuevoEstado);
            }

            return new ResponseEntity<>(new MessageResponse("Estado actualizado correctamente"), HttpStatus.OK);

        } catch (Exception ex) {
            tryLog(authHeader, "CAMBIAR_ESTADO_CERTIFICADO_ERROR", "ERROR",
                    "Ocurrió un error al cambiar el estado del certificado.",
                    "ID Certificado: " + idCertificado + " | Error: " + ex.getMessage());

            return new ResponseEntity<>(new MessageResponse("Error inesperado: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
