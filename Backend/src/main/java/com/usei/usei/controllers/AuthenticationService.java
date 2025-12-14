package com.usei.usei.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.usei.usei.models.*;
import com.usei.usei.repositories.*;

import java.time.LocalDate;
import java.util.*;

@Service
public class AuthenticationService {
    @Autowired
    private UsuarioDAO usuarioDAO;
    @Autowired
    private EstudianteDAO estudianteDAO;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ContraseniaDAO contraseniaDAO;
    @Autowired
    private RolDAO rolDAO;
    @Autowired
    private com.usei.usei.util.PasswordPolicyUtil passwordPolicyUtil;

    @Transactional
    public Map<String, Object> authenticate(String correo, String passwordPlano) {
        Map<String, Object> result = new HashMap<>();

        // LOGIN COMO USUARIO
        Optional<Usuario> usuarioOpt = usuarioDAO.findByCorreo(correo);
        if (usuarioOpt.isPresent()) {
            return authenticateUsuario(usuarioOpt.get(), passwordPlano);
        }

        // LOGIN COMO ESTUDIANTE
        Optional<Estudiante> estudianteOpt = estudianteDAO.findByCorreoInstitucional(correo);
        if (estudianteOpt.isPresent()) {
            return authenticateEstudiante(estudianteOpt.get(), passwordPlano);
        }

        // NO SE ENCONTRÓ EL USUARIO
        result.put("success", false);
        result.put("message", "No se encontró ninguna cuenta con ese correo electrónico.");
        return result;
    }

    private Map<String, Object> authenticateUsuario(Usuario usuario, String passwordPlano) {
        Map<String, Object> result = new HashMap<>();
        Contrasenia contrasenia = usuario.getContraseniaEntity();

        // Verificar que tenga contraseña configurada
        if (contrasenia == null) {
            result.put("success", false);
            result.put("message", "Usuario sin contraseña configurada");
            return result;
        }

        // Verificar si está bloqueado
        if (contrasenia.getIntentosRestantes() <= 0) {
            result.put("success", false);
            result.put("message", "Cuenta bloqueada por intentos fallidos. Contacte al administrador.");
            result.put("bloqueado", true);
            return result;
        }

        // Verificar contraseña
        if (!passwordEncoder.matches(passwordPlano, contrasenia.getContrasenia())) {
            // Decrementar intentos
            int restantes = Math.max(0, contrasenia.getIntentosRestantes() - 1);
            contrasenia.setIntentosRestantes(restantes);
            contraseniaDAO.save(contrasenia);

            result.put("success", false);
            result.put("message", "Credenciales incorrectas. Intentos restantes: " + restantes);
            result.put("intentosRestantes", restantes);
            return result;
        }

        // Verificar expiración de contraseña
        LocalDate fechaCreacion = contrasenia.getFechaCreacion();
        if (fechaCreacion != null && LocalDate.now().isAfter(fechaCreacion.plusDays(60))) {
            result.put("success", false);
            result.put("message", "Su contraseña ha expirado. Debe cambiarla.");
            result.put("expired", true);
            result.put("idUsuario", usuario.getIdUsuario());
            return result;
        }

        // Login exitoso - resetear intentos y actualizar último login
        contrasenia.setIntentosRestantes(3);
        contrasenia.setUltimoLog(LocalDate.now());
        contraseniaDAO.save(contrasenia);

        System.out.println("=== CHECKING POLICY COMPLIANCE IN AUTH SERVICE ===");
        boolean complies = passwordPolicyUtil.existingPasswordCompliesWithCurrentPolicy(contrasenia);
        System.out.println("Policy compliance result: " + complies);
        
        if (!complies) {
            System.out.println("Password does not comply with current policies - forcing password change");
            System.out.println("User: " + usuario.getCorreo() + " (ID: " + usuario.getIdUsuario() + ")");

            usuario.setCambioContrasenia(true);
            usuarioDAO.save(usuario);
            
            result.put("success", false);
            result.put("message", "Las políticas de seguridad han sido actualizadas. Debe cambiar su contraseña.");
            result.put("politicaActualizada", true);
            result.put("idUsuario", usuario.getIdUsuario());
            System.out.println("Returning politicaActualizada response");
            return result;
        } else {
            // 🔒 Password is compliant - ensure cambioContrasenia flag is cleared
            if (usuario.getCambioContrasenia() != null && usuario.getCambioContrasenia()) {
                System.out.println("🔒 Password is compliant - clearing cambioContrasenia flag");
                usuario.setCambioContrasenia(false);
                usuarioDAO.save(usuario);
            }
        }

        // OBTENER ACCESOS DESDE EL ROL Y GUARDAR EN DATA
        List<String> accesos = parseAccesos(usuario.getRolEntity());

        // Crear objeto interno "data" con todos los datos del usuario
        Map<String, Object> data = new HashMap<>();
        data.put("id_usuario", usuario.getIdUsuario());
        data.put("tipo", "usuario");
        data.put("correo", usuario.getCorreo());
        data.put("nombre", usuario.getNombre());
        data.put("apellido", usuario.getApellido());
        data.put("rol", usuario.getRol());
        data.put("carrera", usuario.getCarrera());
        data.put("cambioContrasenia", usuario.getCambioContrasenia());
        data.put("accesos", accesos); // se agregan los accesos dentro de data

        result.put("success", true);
        result.put("tipo", "usuario");
        result.put("data", data);
        result.put("accesos", accesos);
        result.put("cambioContrasenia", usuario.getCambioContrasenia());
        return result;
    }

     // Autenticación para Estudiante (usa campo contrasena hasheado con BCrypt)

    private Map<String, Object> authenticateEstudiante(Estudiante estudiante, String passwordPlano) {
        Map<String, Object> result = new HashMap<>();
        String contrasenaHasheada = estudiante.getContrasena();

        // Verificar que tenga contraseña
        if (contrasenaHasheada == null || contrasenaHasheada.isEmpty()) {
            result.put("success", false);
            result.put("message", "Estudiante sin contraseña configurada");
            return result;
        }

        // Verificar contraseña
        if (!contrasenaHasheada.startsWith("$2a$") && !contrasenaHasheada.startsWith("$2b$")) {
            if (!contrasenaHasheada.equals(passwordPlano)) {
                result.put("success", false);
                result.put("message", "Credenciales incorrectas");
                return result;
            }
        } else {
            if (!passwordEncoder.matches(passwordPlano, contrasenaHasheada)) {
                result.put("success", false);
                result.put("message", "Credenciales incorrectas");
                return result;
            }
        }

        // Accesos predefinidos para estudiantes
        List<String> accesos = Arrays.asList(
                "Encuesta de graduación",
                "Certificados de estudiantes"
        );

        // Crear objeto data
        Map<String, Object> data = new HashMap<>();
        data.put("id_estudiante", estudiante.getIdEstudiante());
        data.put("tipo", "estudiante");
        data.put("correo", estudiante.getCorreoInstitucional());
        data.put("nombre", estudiante.getNombre());
        data.put("apellido", estudiante.getApellido());
        data.put("ci", estudiante.getCi());
        data.put("carrera", estudiante.getCarrera());
        data.put("accesos", accesos); // ✅ Agregar accesos dentro de data

        result.put("success", true);
        result.put("tipo", "estudiante");
        result.put("data", data);
        result.put("accesos", accesos);
        return result;
    }

     // Parsea los accesos del rol desde formato CSV (campo texto)

    private List<String> parseAccesos(Rol rol) {
        if (rol == null || rol.getAccesos() == null || rol.getAccesos().isEmpty()) {
            return new ArrayList<>();
        }

        // Los accesos están guardados como CSV: "Gestión de contraseñas,Gestión de usuarios y roles"
        String[] accesosArray = rol.getAccesos().split(",");
        List<String> resultado = new ArrayList<>();

        for (String acceso : accesosArray) {
            String accesoLimpio = acceso.trim();
            if (!accesoLimpio.isEmpty()) {
                resultado.add(accesoLimpio);
            }
        }
        return resultado;
    }

     // Verifica si un rol tiene un acceso específico
    public boolean tieneAcceso(Rol rol, String accesoRequerido) {
        List<String> accesos = parseAccesos(rol);
        return accesos.stream()
                .anyMatch(acceso -> acceso.equalsIgnoreCase(accesoRequerido));
    }
}
