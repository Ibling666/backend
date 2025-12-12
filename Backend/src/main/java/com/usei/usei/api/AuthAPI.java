package com.usei.usei.api;

import com.usei.usei.controllers.AuthenticationService;
import com.usei.usei.dto.request.UnifiedLoginRequest;
import com.usei.usei.dto.response.LoginResponseDTO;
import com.usei.usei.dto.UnsuccessfulResponse;
import com.usei.usei.util.TokenGenerator;
import com.usei.usei.services.CaptchaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // <-- agregado
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthAPI {

    @Autowired
    private AuthenticationService authService;
    @Autowired
    private TokenGenerator tokenGenerator;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private com.usei.usei.controllers.AuditBL auditBL;


    // banderas de configuración para el captcha
    @Value("${security.captcha.enabled:true}")
    private boolean captchaEnabled; // si es false, se salta toda la validación

    @Value("${security.captcha.dev-bypass-token:}")
    private String captchaDevBypassToken; // si coincide con el token recibido, considera válido

    /**
     * Endpoint unificado de login para Usuario y Estudiante
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UnifiedLoginRequest request) {
        try {
            // CAPTCHA (con control por config y bypass)
            if (captchaEnabled) {
                // 1) Debe venir token
                if (request.getCaptchaToken() == null || request.getCaptchaToken().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new UnsuccessfulResponse(
                            "400 Bad Request",
                            "El token de reCAPTCHA es obligatorio",
                            "/auth/login"
                        )
                    );
                }

                // 2) Bypass de desarrollo por token (si está configurado)
                boolean bypassOk = (captchaDevBypassToken != null && !captchaDevBypassToken.isBlank()
                        && captchaDevBypassToken.equals(request.getCaptchaToken()));

                // 3) Validación real contra el servicio (o bypass)
                boolean captchaValido = bypassOk || captchaService.verifyCaptcha(request.getCaptchaToken());
                if (!captchaValido) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new UnsuccessfulResponse(
                            "403 Forbidden",
                            "Verificación de reCAPTCHA fallida. Por favor, inténtelo de nuevo.",
                            "/auth/login"
                        )
                    );
                }
            }

            // Validar que vengan los datos requeridos
            if (request.getCorreo() == null || request.getCorreo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new UnsuccessfulResponse(
                        "400 Bad Request",
                        "El correo electrónico es requerido",
                        "/auth/login"
                    )
                );
            }
            
            if (request.getContrasena() == null || request.getContrasena().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new UnsuccessfulResponse(
                        "400 Bad Request",
                        "La contraseña es requerida",
                        "/auth/login"
                    )
                );
            }
            
            // Intentar autenticar
            Map<String, Object> authResult = authService.authenticate(
                request.getCorreo().trim(), 
                request.getContrasena()
            );
            
            Boolean success = (Boolean) authResult.get("success");
            
            // Si falla la autenticación
            if (!success) {
                String message = (String) authResult.get("message");
                Boolean expired = (Boolean) authResult.getOrDefault("expired", false);
                Boolean bloqueado = (Boolean) authResult.getOrDefault("bloqueado", false);
                Boolean politicaActualizada = (Boolean) authResult.getOrDefault("politicaActualizada", false);
                
                HttpStatus status;
                String statusCode;
                
                if (bloqueado) {
                    status = HttpStatus.LOCKED;
                    statusCode = "423 Locked";
                } else if (expired) {
                    status = HttpStatus.FORBIDDEN;
                    statusCode = "403 Forbidden";
                } else if (politicaActualizada) {
                    status = HttpStatus.UPGRADE_REQUIRED;
                    statusCode = "426 Upgrade Required";
                    System.out.println("🔒 Returning POLITICA_ACTUALIZADA response to frontend");
                } else {
                    status = HttpStatus.UNAUTHORIZED;
                    statusCode = "401 Unauthorized";
                }
                
                UnsuccessfulResponse response = new UnsuccessfulResponse(
                    statusCode,
                    message,
                    "/auth/login"
                );
                
                // En casos especiales: política actualizada (mapa con info adicional)
                if (politicaActualizada) {
                    Map<String, Object> policyResponse = new HashMap<>();
                    policyResponse.put("timeStamp", response.getTimeStamp());
                    policyResponse.put("status", statusCode);
                    policyResponse.put("error", message);
                    policyResponse.put("path", "/auth/login");
                    policyResponse.put("politicaActualizada", true);
                    policyResponse.put("idUsuario", authResult.get("idUsuario"));
                    return ResponseEntity.status(status).body(policyResponse);
                }
                
                return ResponseEntity.status(status).body(response);
            }
            
            // Login exitoso
            String tipo = (String) authResult.get("tipo");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) authResult.get("data"); 
            @SuppressWarnings("unchecked")
            List<String> accesos = (List<String>) authResult.get("accesos");

            // Primer login (se logea por primera vez)
            // Si es 'usuario' y el flag 'cambio_contrasenia' viene en true se debe forzar cambio de contraseña
            boolean requireFirstChange = false;
            if ("usuario".equalsIgnoreCase(tipo) && data != null) {
                Object changeFlag = data.get("cambio_contrasenia");
                requireFirstChange = (changeFlag instanceof Boolean)
                        ? (Boolean) changeFlag
                        : "true".equalsIgnoreCase(String.valueOf(changeFlag));
            }

            if (requireFirstChange) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("status", "403 Forbidden");
                payload.put("reason", "FIRST_LOGIN_PASSWORD_CHANGE_REQUIRED");
                payload.put("message", "Debe cambiar su contraseña (primer ingreso).");
                payload.put("redirect", "/usuario/change-password");
                // datos para mandar al front
                payload.put("id_usuario", data.get("id_usuario"));
                payload.put("correo", data.get("correo"));
                payload.put("carrera", data.get("carrera"));
                payload.put("nombre", data.get("nombre"));
                payload.put("ci", data.get("ci"));
                payload.put("cambio_contrasenia", true);
                // no generamos token hasta que cambie la contraseña
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(payload);
            }

            // Generar token normal
            String token = "";
            if ("usuario".equals(tipo)) {
                String idUsuario = String.valueOf(data.get("id_usuario"));
                String correoUsuario = (String) data.get("correo");

                token = tokenGenerator.generateToken(
                    idUsuario,
                    "usuario",
                    correoUsuario,
                    60
                );

            } else if ("estudiante".equals(tipo)) {
                String idEstudiante = String.valueOf(data.get("id_estudiante"));
                String correoEstudiante = (String) data.get("correo");

                token = tokenGenerator.generateToken(
                    idEstudiante,
                    "estudiante",
                    correoEstudiante,
                    60
                );
            }
            // >>> Registrar LOG de login exitoso (solo cuando realmente hay sesión)
            try {
                if ("usuario".equalsIgnoreCase(tipo)) {
                    Long idUsuario = Long.valueOf(String.valueOf(data.get("id_usuario")));
                    auditBL.registerLogin(idUsuario);
                }
                // Si luego quieres también para estudiante y tienes el id_usuario equivalente, lo pones aquí.
            } catch (Exception auditEx) {
                // No interrumpir la sesión si falla la auditoría
                auditEx.printStackTrace();
            }



            //Agregar accesos dentro de data
            if (!data.containsKey("accesos") && accesos != null) {
                data.put("accesos", accesos);
            }

            LoginResponseDTO response = new LoginResponseDTO(
                "200 OK",
                "Inicio de sesión correcto",
                token,
                60,
                data,
                accesos
            );

            return ResponseEntity.ok(response);

            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new UnsuccessfulResponse(
                    "500 Internal Server Error",
                    "Error en el servidor: " + e.getMessage(),
                    "/auth/login"
                ));
        }
    }
}
