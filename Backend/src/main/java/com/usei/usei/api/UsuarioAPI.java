package com.usei.usei.api;

import java.util.*;
import com.usei.usei.dto.response.UsuarioResponseDTO;
import com.usei.usei.repositories.ContraseniaDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.usei.usei.controllers.PasswordChangeStatus;
import com.usei.usei.controllers.RolBL;
import com.usei.usei.controllers.SecurityBL;
import com.usei.usei.controllers.LoginStatus;
import com.usei.usei.controllers.UsuarioService;
import com.usei.usei.dto.SuccessfulResponse;
import com.usei.usei.dto.UnsuccessfulResponse;
import com.usei.usei.dto.request.LoginRequestUserDTO;
import com.usei.usei.models.LoginResponse;
import com.usei.usei.models.Rol;
import com.usei.usei.models.Usuario;
import com.usei.usei.util.TokenGenerator;
import com.usei.usei.controllers.LogUsuarioService;
import jakarta.servlet.http.HttpServletRequest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/usuario")
public class UsuarioAPI {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Autowired
    private RolBL rolBL;

    @Autowired
    private ContraseniaDAO contraseniaDAO;

    @Autowired
    private SecurityBL securityBL;

    @Autowired
    private LogUsuarioService logUsuarioService;


    public UsuarioAPI() {
        System.out.println("🔧 UsuarioAPI constructor called");
    }

    @PostConstruct
    public void init() {
        System.out.println("🔧 UsuarioAPI @PostConstruct called");
        System.out.println("🔧 usuarioService: " + (usuarioService != null ? "OK" : "NULL"));
        System.out.println("🔧 tokenGenerator: " + (tokenGenerator != null ? "OK" : "NULL"));
        System.out.println("🔧 rolBL: " + (rolBL != null ? "OK" : "NULL"));
        System.out.println("🔧 contraseniaDAO: " + (contraseniaDAO != null ? "OK" : "NULL"));
        System.out.println("🔧 securityBL: " + (securityBL != null ? "OK" : "NULL"));
    }

    private Usuario getActorFromRequest(HttpServletRequest request) {
        try {
            if (request == null) return null;

            Object idObj = request.getAttribute("userId");
            if (idObj == null) idObj = request.getAttribute("id_usuario");
            if (idObj == null) idObj = request.getAttribute("idUsuario");

            if (idObj == null) return null;

            Long actorId = (idObj instanceof Number n)
                    ? n.longValue()
                    : Long.parseLong(String.valueOf(idObj));

            return usuarioService.findById(actorId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    private Usuario getActor(HttpServletRequest request) {
        // A) lo que puso el interceptor
        Usuario actor = getActorFromRequest(request);
        if (actor != null) return actor;

        // B) fallback: leer Authorization y parsear token
        try {
            if (request == null) return null;

            String auth = request.getHeader("Authorization");
            if (auth == null || auth.isBlank()) return null;

            // Intento 1: tal cual (por si tu TokenGenerator espera "Bearer ...")
            try {
                Jws<Claims> c = tokenGenerator.validateAndParseToken(auth);
                Usuario u = resolveUserFromClaims(c);
                if (u != null) return u;
            } catch (Exception ignored) {}

            // Intento 2: quitar "Bearer "
            if (auth.startsWith("Bearer ")) {
                String raw = auth.substring(7).trim();
                if (!raw.isBlank()) {
                    Jws<Claims> c2 = tokenGenerator.validateAndParseToken(raw);
                    return resolveUserFromClaims(c2);
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    private Usuario resolveUserFromClaims(Jws<Claims> claims) {
        if (claims == null) return null;

        Object idObj = claims.getBody().get("id_usuario");
        if (idObj == null) idObj = claims.getBody().get("id");
        if (idObj == null) idObj = claims.getBody().get("userId");
        if (idObj == null) idObj = claims.getBody().getSubject(); // a veces el id viene como subject

        if (idObj == null) return null;

        Long id;
        if (idObj instanceof Number n) id = n.longValue();
        else id = Long.parseLong(String.valueOf(idObj));

        return usuarioService.findById(id).orElse(null);
    }

    private String safeDetalle(String s) {
        if (s == null || s.isBlank()) return "Edición de usuario.";
        s = s.trim();
        return (s.length() > 255) ? s.substring(0, 255) : s;
    }

    // CREAR USUARIO
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            if (!body.containsKey("nombre") || !body.containsKey("apellido")
                    || !body.containsKey("ci") || !body.containsKey("idRol")) {
                return ResponseEntity.badRequest()
                        .body("Faltan campos obligatorios: nombre, apellido, ci, idRol");
            }

            String nombre = body.get("nombre").toString().trim();
            String apellido = body.get("apellido").toString().trim();
            String ci = body.get("ci").toString().trim();
            int telefono = body.get("telefono") != null ? Integer.parseInt(body.get("telefono").toString()) : 0;
            String correo = body.get("correo") != null ? body.get("correo").toString().trim() : "";
            String carrera = body.get("carrera") != null ? body.get("carrera").toString().trim() : "";

            Long idRol = Long.parseLong(body.get("idRol").toString());
            Rol rol = rolBL.obtenerRolPorId(idRol);
            if (rol == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El rol especificado no existe.");

            // UsuarioBL.save() crea contraseña y lo hashea.
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setApellido(apellido);
            nuevoUsuario.setCi(ci);
            nuevoUsuario.setTelefono(telefono);
            nuevoUsuario.setCorreo(correo);
            nuevoUsuario.setCarrera(carrera);
            nuevoUsuario.setRol(rol.getNombreRol());
            nuevoUsuario.setRolEntity(rol);

            // UsuarioBL.save() detecta la entidad contraseña == null, genera contraseña inicial por defecto, la HASHEA,
            // aplica política, setea cambioContrasenia=true y envía el correo.
            Usuario saved = usuarioService.save(nuevoUsuario);
            saved.setContraseniaEntity(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear usuario: " + e.getMessage());
        }
    }

    // OBTENER USUARIO POR ID
    @GetMapping("/{id_usuario}")
    public ResponseEntity<?> read(@PathVariable("id_usuario") Long idUsuario) {
        Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
        if (oUsuario.isEmpty()) return ResponseEntity.notFound().build();

        Usuario usuario = oUsuario.get();
        usuario.setContraseniaEntity(null);
        return ResponseEntity.ok(usuario);
    }

    // LISTAR TODOS LOS USUARIOS
    @GetMapping
    public ResponseEntity<?> readAll() {
        try {
            List<UsuarioResponseDTO> usuarios = new ArrayList<>();
            usuarioService.findAll().forEach(u -> {
                String rol = (u.getRol() != null && !u.getRol().isBlank())
                        ? u.getRol()
                        : (u.getRolEntity() != null ? u.getRolEntity().getNombreRol() : "SIN_ROL");

                usuarios.add(new UsuarioResponseDTO(
                        u.getIdUsuario(),
                        u.getNombre(),
                        u.getApellido(),
                        u.getCorreo(),
                        u.getCi(),
                        u.getTelefono(),
                        u.getCarrera(),
                        rol
                ));
            });

            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar usuarios: " + e.getMessage());
        }
    }

    // ELIMINAR USUARIO
    @DeleteMapping("/{id_usuario}")
    public ResponseEntity<?> delete(@PathVariable("id_usuario") Long idUsuario) {
        Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
        if (oUsuario.isEmpty()) return ResponseEntity.notFound().build();

        usuarioService.deleteById(idUsuario);
        return ResponseEntity.ok("Usuario eliminado correctamente.");
    }


    // ACTUALIZAR USUARIO
    // ACTUALIZAR USUARIO
    @PutMapping("/{id_usuario}")
    public ResponseEntity<?> update(
            @PathVariable("id_usuario") Long idUsuario,
            @RequestBody Usuario usuario,
            HttpServletRequest request
    ) {
        try {
            Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
            if (oUsuario.isEmpty()) return ResponseEntity.notFound().build();

            Usuario u = oUsuario.get();

            // ===== snapshot antes (para detalle del log) =====
            String beforeNombre   = u.getNombre();
            String beforeApellido = u.getApellido();
            String beforeCi       = u.getCi();
            String beforeCorreo   = u.getCorreo();
            Integer beforeTelefono= u.getTelefono();
            String beforeCarrera  = u.getCarrera();
            String beforeRol      = u.getRol();

            // ===== aplicar cambios (PUT = reemplazo completo) =====
            u.setNombre(usuario.getNombre());
            u.setApellido(usuario.getApellido());
            u.setTelefono(usuario.getTelefono());
            u.setCorreo(usuario.getCorreo());
            u.setCarrera(usuario.getCarrera());
            u.setRol(usuario.getRol());
            u.setCi(usuario.getCi());

            Usuario updated = usuarioService.save(u);

            // ===== LOG (actor = usuario logueado) =====
            try {
                // ✅ CAMBIO: usar getActor(request) (interceptor o token)
                Usuario actor = getActor(request);

                if (actor == null) {
                    System.out.println("⚠️ No se registró log PUT /usuario: actor=null. Verifica Authorization Bearer o que /usuario/** esté protegido.");
                } else if (logUsuarioService == null) {
                    System.out.println("⚠️ No se registró log PUT /usuario: logUsuarioService=null (inyección falló).");
                } else {
                    StringBuilder detalle = new StringBuilder();
                    detalle.append("Se editó usuario (PUT) ID=").append(updated.getIdUsuario())
                            .append(" CI=").append(updated.getCi()).append(". Cambios:");

                    if (!Objects.equals(beforeNombre, updated.getNombre()))
                        detalle.append(" nombre: '").append(beforeNombre).append("' -> '").append(updated.getNombre()).append("';");
                    if (!Objects.equals(beforeApellido, updated.getApellido()))
                        detalle.append(" apellido: '").append(beforeApellido).append("' -> '").append(updated.getApellido()).append("';");
                    if (!Objects.equals(beforeCorreo, updated.getCorreo()))
                        detalle.append(" correo: '").append(beforeCorreo).append("' -> '").append(updated.getCorreo()).append("';");
                    if (!Objects.equals(beforeCi, updated.getCi()))
                        detalle.append(" ci: '").append(beforeCi).append("' -> '").append(updated.getCi()).append("';");
                    if (!Objects.equals(beforeTelefono, updated.getTelefono()))
                        detalle.append(" telefono: '").append(beforeTelefono).append("' -> '").append(updated.getTelefono()).append("';");
                    if (!Objects.equals(beforeCarrera, updated.getCarrera()))
                        detalle.append(" carrera: '").append(beforeCarrera).append("' -> '").append(updated.getCarrera()).append("';");
                    if (!Objects.equals(beforeRol, updated.getRol()))
                        detalle.append(" rol: '").append(beforeRol).append("' -> '").append(updated.getRol()).append("';");

                    // ✅ CAMBIO: detalle SIEMPRE no nulo/ni vacío y max 255
                    String det = safeDetalle(detalle.toString());

                    logUsuarioService.registrarLogSeguridad(
                            actor,
                            "ACTUALIZACION_USUARIO",
                            "INFO",
                            "Edición de usuario",
                            det
                    );
                }
            } catch (Exception logEx) {
                System.out.println("⚠️ Falló el registro del log PUT /usuario, pero la edición se mantuvo. " + logEx.getMessage());
            }

            updated.setContraseniaEntity(null);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    // EDITAR USUARIO
    @PatchMapping("/{id_usuario}")
    public ResponseEntity<?> editarUsuario(
            @PathVariable("id_usuario") Long idUsuario,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        try {
            Optional<Usuario> oUsuario = usuarioService.findById(idUsuario);
            if (oUsuario.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
            }

            Usuario u = oUsuario.get();

            // ===== snapshot antes (para detalle del log) =====
            String beforeNombre = u.getNombre();
            String beforeApellido = u.getApellido();
            String beforeCi = u.getCi();
            String beforeCorreo = u.getCorreo();
            Integer beforeTelefono = u.getTelefono();
            String beforeCarrera = u.getCarrera();
            String beforeRol = u.getRol();

            // ===== aplicar cambios =====
            if (body.containsKey("nombre")) u.setNombre(String.valueOf(body.get("nombre")));
            if (body.containsKey("apellido")) u.setApellido(String.valueOf(body.get("apellido")));
            if (body.containsKey("ci")) u.setCi(String.valueOf(body.get("ci")));
            if (body.containsKey("correo")) u.setCorreo(String.valueOf(body.get("correo")));

            if (body.containsKey("telefono")) {
                String telStr = String.valueOf(body.get("telefono"));
                if (telStr != null && !telStr.isBlank() && !"null".equalsIgnoreCase(telStr)) {
                    try {
                        u.setTelefono(Integer.parseInt(telStr));
                    } catch (NumberFormatException ex) {
                        return ResponseEntity.badRequest().body("El campo 'telefono' debe ser numérico.");
                    }
                }
            }

            if (body.containsKey("carrera")) u.setCarrera(String.valueOf(body.get("carrera")));

            // 🔹 Actualizar rol si llega idRol o rol
            if (body.containsKey("idRol") || body.containsKey("rol")) {
                Rol nuevoRol = null;

                if (body.containsKey("idRol")) {
                    String idRolStr = String.valueOf(body.get("idRol"));
                    if (idRolStr != null && !idRolStr.isBlank() && !"null".equalsIgnoreCase(idRolStr)) {
                        try {
                            Long idRol = Long.parseLong(idRolStr);
                            nuevoRol = rolBL.obtenerRolPorId(idRol);
                        } catch (NumberFormatException ex) {
                            return ResponseEntity.badRequest().body("El campo 'idRol' debe ser un número válido.");
                        }
                    }
                } else {
                    String nombreRol = String.valueOf(body.get("rol"));
                    if (nombreRol != null && !nombreRol.isBlank() && !"null".equalsIgnoreCase(nombreRol)) {
                        nuevoRol = rolBL.obtenerRolPorNombre(nombreRol);
                    }
                }

                if (nuevoRol == null) {
                    return ResponseEntity.badRequest().body("El rol especificado no existe o no es válido.");
                }

                u.setRolEntity(nuevoRol);
                u.setRol(nuevoRol.getNombreRol());
            }

            Usuario actualizado = usuarioService.save(u);

            // ===== LOG (actor = usuario logueado) =====
            try {
                // ✅ CAMBIO: usar getActor(request) (interceptor o token)
                Usuario actor = getActor(request);

                if (actor == null) {
                    System.out.println("⚠️ No se registró log: actor=null. Verifica Authorization Bearer o que /usuario/** esté protegido.");
                } else if (logUsuarioService == null) {
                    System.out.println("⚠️ No se registró log: logUsuarioService=null (inyección falló).");
                } else {
                    StringBuilder detalle = new StringBuilder();
                    detalle.append("Se editó usuario ID=").append(actualizado.getIdUsuario())
                            .append(" CI=").append(actualizado.getCi()).append(". Cambios:");

                    if (!Objects.equals(beforeNombre, actualizado.getNombre()))
                        detalle.append(" nombre: '").append(beforeNombre).append("' -> '").append(actualizado.getNombre()).append("';");
                    if (!Objects.equals(beforeApellido, actualizado.getApellido()))
                        detalle.append(" apellido: '").append(beforeApellido).append("' -> '").append(actualizado.getApellido()).append("';");
                    if (!Objects.equals(beforeCorreo, actualizado.getCorreo()))
                        detalle.append(" correo: '").append(beforeCorreo).append("' -> '").append(actualizado.getCorreo()).append("';");
                    if (!Objects.equals(beforeCi, actualizado.getCi()))
                        detalle.append(" ci: '").append(beforeCi).append("' -> '").append(actualizado.getCi()).append("';");
                    if (!Objects.equals(beforeTelefono, actualizado.getTelefono()))
                        detalle.append(" telefono: '").append(beforeTelefono).append("' -> '").append(actualizado.getTelefono()).append("';");
                    if (!Objects.equals(beforeCarrera, actualizado.getCarrera()))
                        detalle.append(" carrera: '").append(beforeCarrera).append("' -> '").append(actualizado.getCarrera()).append("';");
                    if (!Objects.equals(beforeRol, actualizado.getRol()))
                        detalle.append(" rol: '").append(beforeRol).append("' -> '").append(actualizado.getRol()).append("';");

                    // ✅ CAMBIO: detalle SIEMPRE no nulo y no vacío
                    String det = safeDetalle(detalle.toString());

                    logUsuarioService.registrarLogSeguridad(
                            actor,
                            "ACTUALIZACION_USUARIO",
                            "INFO",
                            "Edición de usuario",
                            det
                    );
                }
            } catch (Exception logEx) {
                System.out.println("⚠️ Falló el registro del log, pero la edición se mantuvo. " + logEx.getMessage());
            }

            actualizado.setContraseniaEntity(null);
            return ResponseEntity.ok(actualizado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    // ENVIO DE CREDENCIALES USUARIO Y CONTRASEÑA POR  DEFECTO (INICIAL)
    @PostMapping("/{id}/enviarCredenciales")
    public ResponseEntity<?> enviarCredenciales(@PathVariable Long id) {
        try {
            Optional<Usuario> oUsuario = usuarioService.findById(id);
            if (oUsuario.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado con ID: " + id);

            Usuario usuario = oUsuario.get();

            try {
                usuarioService.enviarCredencialesUsuario(usuario);
                return ResponseEntity.ok("Credenciales enviadas correctamente a " + usuario.getCorreo());
            } catch (Exception emailError) {
                // Email failed but continue operation for development
                System.err.println("Email send failed for user " + id + ": " + emailError.getMessage());
                return ResponseEntity.ok("Usuario procesado. Email no enviado (error de configuración SMTP): " + emailError.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar usuario: " + e.getMessage());
        }
    }

    // CAMBIAR CONTRASEÑA (con hash + políticas + historial)
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam Long idUsuario,
                                            @RequestBody HashMap<String, String> body) {
        try {
            System.out.println("=== CHANGE PASSWORD DEBUG ===");
            System.out.println("idUsuario: " + idUsuario);
            System.out.println("securityBL is null? " + (securityBL == null));
            System.out.println("usuarioService is null? " + (usuarioService == null));
            System.out.println("tokenGenerator is null? " + (tokenGenerator == null));
            System.out.println("rolBL is null? " + (rolBL == null));

            if (securityBL == null) {
                System.err.println("SecurityBL is null! Dependency injection failed!");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error de configuración: SecurityBL no está disponible. Contacte al administrador.");
            }

            String nuevaPassStr = body.get("newPassword");
            if (nuevaPassStr == null || nuevaPassStr.isBlank()) {
                return ResponseEntity.badRequest().body("La nueva contraseña no puede estar vacía.");
            }

            System.out.println("Calling securityBL.changePassword...");
            PasswordChangeStatus status = securityBL.changePassword(idUsuario, nuevaPassStr);
            System.out.println("Password change status: " + status);

            switch (status) {
                case CAMBIO_OK:
                    return ResponseEntity.ok("Contraseña actualizada correctamente.");
                case POLITICA_NO_CUMPLIDA:
                    return ResponseEntity.badRequest()
                            .body("La contraseña no cumple la política (mín 12, mayús, minús, número, especial).");
                case REUTILIZACION_ULTIMA:
                    return ResponseEntity.badRequest().body("No puedes reutilizar la contraseña actual.");
                case REUTILIZACION_HISTORIAL:
                    return ResponseEntity.badRequest().body("No puedes reutilizar una contraseña usada en los últimos 12 meses.");
                case USUARIO_SIN_CONTRASENIA:
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario no tiene contraseña asociada.");
                default:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo cambiar la contraseña.");
            }
        } catch (Exception e) {
            System.err.println("Exception in changePassword: " + e.getClass().getSimpleName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar la contraseña: " + e.getMessage());
        }
    }

    // VERIFICACION DE DUPLICADOS (correo / ci)
    @GetMapping("/verificar")
    public ResponseEntity<?> verificarDuplicados(
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String ci) {

        try {
            Map<String, Boolean> result = new HashMap<>();
            result.put("existeCorreo", correo != null && usuarioService.findByCorreo(correo).isPresent());
            result.put("existeCi", ci != null && usuarioService.existsByCi(ci));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al verificar duplicados: " + e.getMessage());
        }
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestUserDTO loginRequestUser) {
        try {
            // Use SecurityBL to validate and decrement attempts on Contrasenia
            LoginStatus status = securityBL.login(loginRequestUser.getCorreo(), loginRequestUser.getContrasena());
            switch (status) {
                case OK: {
                    Optional<Usuario> usuario = usuarioService.findByCorreo(loginRequestUser.getCorreo());
                    if (usuario.isPresent()) {
                        Usuario user = usuario.get();
                        user.setContraseniaEntity(null);

                        String token = tokenGenerator.generateToken(
                                String.valueOf(user.getIdUsuario()),
                                user.getRol(),
                                user.getCorreo(),
                                60
                        );

                        Map<String, Object> data = new HashMap<>();
                        data.put("id_usuario", user.getIdUsuario());
                        data.put("rol", user.getRol());
                        data.put("correo", user.getCorreo());
                        data.put("carrera", user.getCarrera());
                        data.put("nombre", user.getNombre());
                        data.put("ci", user.getCi());
                        data.put("cambio_contrasenia", user.getCambioContrasenia());

                        SuccessfulResponse response = new SuccessfulResponse(
                                "200 OK",
                                "Inicio de sesión correcto",
                                token,
                                60,
                                data
                        );
                        return ResponseEntity.ok(response);
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Usuario no encontrado tras validación");
                }
                case BLOQUEADO: {
                    UnsuccessfulResponse blocked = new UnsuccessfulResponse(
                            "403 Forbidden",
                            "Cuenta bloqueada por intentos fallidos. Use recuperación de contraseña.",
                            "/usuario/recover"
                    );
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(blocked);
                }
                case CREDENCIALES: {
                    // We can try to fetch remaining attempts to return to client
                    Optional<Usuario> u = usuarioService.findByCorreo(loginRequestUser.getCorreo());
                    Integer remaining = null;
                    if (u.isPresent() && u.get().getContraseniaEntity() != null) remaining = u.get().getContraseniaEntity().getIntentosRestantes();
                    Map<String,Object> payload = new HashMap<>();
                    payload.put("status","401 Unauthorized");
                    payload.put("message","Credenciales incorrectas");
                    payload.put("remainingAttempts", remaining);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(payload);
                }
                case EXPIRADA: {
                    UnsuccessfulResponse expired = new UnsuccessfulResponse(
                            "403 Forbidden",
                            "Contraseña expirada. Debe cambiarla.",
                            "/usuario/change-password"
                    );
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(expired);
                }
                case POLITICA_ACTUALIZADA: {
                    UnsuccessfulResponse policyUpdated = new UnsuccessfulResponse(
                            "403 Forbidden",
                            "Las políticas de seguridad han sido actualizadas. Debe cambiar su contraseña para cumplir con los nuevos requisitos.",
                            "/usuario/change-password"
                    );
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(policyUpdated);
                }
                default:
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UnsuccessfulResponse("401", "Credenciales incorrectas", "/usuario/login"));
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new LoginResponse<Object>(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // EMAIL: ENVIAR CÓDIGO
    // En UsuarioAPI
    // En com.usei.usei.api.UsuarioAPI
    @PostMapping("/enviarCodigoVerificacion")
    public ResponseEntity<?> enviarCodigoVerificacion(@RequestBody Map<String, String> body) {
        try {
            String correo = (body != null) ? String.valueOf(body.get("correo")).trim() : null;
            if (correo == null || correo.isBlank()) {
                return ResponseEntity.badRequest().body("Debe proporcionar un correo.");
            }
            if (!correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                return ResponseEntity.badRequest().body("Correo inválido.");
            }

            Long idUsuario = usuarioService.findByMail(correo);
            if (idUsuario == 0L) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontró un usuario con ese correo.");
            }

            // envía el email y guarda el código asociado al correo
            usuarioService.enviarCodigoVerificacion(correo);

            // validacion en el front
            String codigoVerificacion = usuarioService.obtenerCodigoVerificacion();

            Map<String, Object> payload = new HashMap<>();
            payload.put("mensaje", "Código de verificación enviado exitosamente");
            payload.put("idUsuario", idUsuario);
            payload.put("codigoVerificacion", codigoVerificacion);
            return ResponseEntity.ok(payload);

        } catch (jakarta.mail.MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el código de verificación: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado: " + e.getMessage());
        }
    }

    // ROLES
    // LISTA DE ROLES DE ACUERDO AL ROL ASIGNADO AL USUARIO
    @GetMapping("/rol")
    public ResponseEntity<?> listarRoles() {
        try {
            Iterable<Rol> roles = rolBL.listarRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new UnsuccessfulResponse("500", "Error al listar roles: " + ex.getMessage(), "/usuario/rol")
            );
        }
    }

    // ACTUALIZAR ESTADO DEL ROL ACTIVO O INACTIVO
    @PutMapping("/rol/{idRol}/estado")
    public ResponseEntity<?> actualizarEstadoRol(@PathVariable Long idRol, @RequestBody Map<String, Object> body) {
        try {
            if (!body.containsKey("activo")) {
                return ResponseEntity.badRequest().body("Campo 'activo' es obligatorio.");
            }

            boolean nuevoEstado = Boolean.parseBoolean(String.valueOf(body.get("activo")));
            Rol rolActualizado = rolBL.cambiarEstadoRol(idRol, nuevoEstado);

            return ResponseEntity.ok(rolActualizado);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al cambiar el estado del rol: " + ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado: " + e.getMessage());
        }
    }

    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email address is required");
            }

            System.out.println("=== EMAIL TEST ENDPOINT CALLED ===");
            System.out.println("Testing email to: " + email);

            // Create a test user to send credentials
            Usuario testUser = new Usuario();
            testUser.setNombre("Test");
            testUser.setApellido("User");
            testUser.setCi("12345");
            testUser.setCorreo(email);

            usuarioService.enviarCredencialesUsuario(testUser);

            System.out.println("=== EMAIL TEST COMPLETED ===");
            return ResponseEntity.ok().body("Test email sent to " + email + ". Check backend logs for details.");

        } catch (Exception e) {
            System.err.println("=== EMAIL TEST FAILED ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Email test failed: " + e.getMessage());
        }
    }

    @GetMapping("/test-security-bl")
    public ResponseEntity<?> testSecurityBL() {
        try {
            System.out.println("=== SECURITY BL TEST ===");
            System.out.println("securityBL is null? " + (securityBL == null));

            if (securityBL == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("ERROR: SecurityBL is null - dependency injection failed!");
            }

            return ResponseEntity.ok().body("SUCCESS: SecurityBL is properly injected!");

        } catch (Exception e) {
            System.err.println("Error testing SecurityBL: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Exception testing SecurityBL: " + e.getMessage());
        }
    }

    @PostMapping("/force-policy-enforcement")
    public ResponseEntity<?> forcePolicyEnforcement() {
        try {
            System.out.println("=== MANUAL POLICY ENFORCEMENT TEST ===");

            if (securityBL == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("ERROR: SecurityBL is null!");
            }

            securityBL.enforcePasswordPolicyUpdateForAllUsers();
            return ResponseEntity.ok().body("Policy enforcement completed. Check logs for details.");

        } catch (Exception e) {
            System.err.println("Error in manual policy enforcement: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error in policy enforcement: " + e.getMessage());
        }
    }

    @GetMapping("/check-password-change-flags")
    public ResponseEntity<?> checkPasswordChangeFlags() {
        try {
            System.out.println("=== CHECKING PASSWORD CHANGE FLAGS ===");

            Iterable<Usuario> allUsers = usuarioService.findAll();
            Map<String, Object> result = new HashMap<>();
            int totalUsers = 0;
            int markedForChange = 0;

            for (Usuario user : allUsers) {
                totalUsers++;
                if (user.getCambioContrasenia() != null && user.getCambioContrasenia()) {
                    markedForChange++;
                    System.out.println("User marked for password change: " + user.getCorreo() + " (ID: " + user.getIdUsuario() + ")");
                } else {
                    System.out.println("User NOT marked for password change: " + user.getCorreo() + " (ID: " + user.getIdUsuario() + ")");
                }
            }

            result.put("totalUsers", totalUsers);
            result.put("markedForChange", markedForChange);
            result.put("message", markedForChange + " out of " + totalUsers + " users are marked for password change");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("Error checking password change flags: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking flags: " + e.getMessage());
        }
    }
}