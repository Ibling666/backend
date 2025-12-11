package com.usei.usei.controllers;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.usei.usei.models.Contrasenia;
import com.usei.usei.models.Rol;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.ContraseniaDAO;
import com.usei.usei.repositories.RolDAO;
import com.usei.usei.repositories.UsuarioDAO;
import com.usei.usei.util.PasswordPolicyUtil;
import com.usei.usei.models.LogUsuario;
import com.usei.usei.repositories.LogUsuarioDAO;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioBL implements UsuarioService {

    @Autowired private UsuarioDAO usuarioDAO;
    @Autowired private RolDAO rolDAO;
    @Autowired private ContraseniaDAO contraseniaDAO;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder; // BCrypt
    private String codigoVerificacion;
    @Autowired
    private LogUsuarioService logUsuarioService;
    @Autowired
    private LogUsuarioDAO logUsuarioDAO;

    @Autowired
    public UsuarioBL(UsuarioDAO usuarioDAO,
                     JavaMailSender mailSender,
                     PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    /* ==========================
       CRUD BÁSICO
       ========================== */
    @Override
    @Transactional(readOnly = true)
    public Iterable<Usuario> findAll() { return usuarioDAO.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) { return usuarioDAO.findById(id); }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        // Verificación de no duplicados de CI y Correos institucionales al crear usuario
        Optional<Usuario> existentePorCi = usuarioDAO.findByCi(usuario.getCi());
        if (existentePorCi.isPresent() && !existentePorCi.get().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("Ya existe un usuario con el CI " + usuario.getCi());
        }

        if (usuario.getCorreo() != null && !usuario.getCorreo().isBlank()) {
            Optional<Usuario> existentePorCorreo = usuarioDAO.findByCorreo(usuario.getCorreo());
            if (existentePorCorreo.isPresent() && !existentePorCorreo.get().getIdUsuario().equals(usuario.getIdUsuario())) {
                throw new RuntimeException("Ya existe un usuario con el correo " + usuario.getCorreo());
            }
        }


        // Generar contraseña por defecto si no tiene aún
        if (usuario.getContraseniaEntity() == null) {
            final String contraseniaGenerada = buildInitialPassword(
                    nullSafe(usuario.getNombre()), nullSafe(usuario.getApellido()), nullSafe(usuario.getCi())
            );

            // Hash (temporal)
            String hash = passwordEncoder.encode(contraseniaGenerada);

            // Crear entidad Contrasenia con política
            Contrasenia contrasenia = new Contrasenia();
            contrasenia.setContrasenia(hash);
            contrasenia.setFechaCreacion(LocalDate.now());
            contrasenia.setUltimoLog(LocalDate.now());
            contrasenia.setLongitud(Math.max(PasswordPolicyUtil.MIN_LENGTH, contraseniaGenerada.length()));
            contrasenia.setComplejidad(PasswordPolicyUtil.COMPLEJIDAD); // 4: mayus/minus/num/especial
            contrasenia.setIntentosRestantes(PasswordPolicyUtil.MAX_INTENTOS);

            contrasenia = contraseniaDAO.save(contrasenia);
            usuario.setContraseniaEntity(contrasenia);

            // Forzar cambio de contraseña al primer login
            usuario.setCambioContrasenia(true);

            // Enviar correo de notificación (si hay correo)
            if (usuario.getCorreo() != null && !usuario.getCorreo().isBlank()) {
                try {
                    String cuerpo = """
                    Estimado/a %s %s,
                    
                    Su cuenta ha sido creada exitosamente para el Sistema USEI.
                    
                    Sus credenciales iniciales son:
                    Usuario: %s
                    Contraseña: %s
                    
                    Por seguridad, deberá cambiar su contraseña en su primer inicio de sesión.
                    
                    Saludos cordiales,
                    Equipo USEI
                    Universidad Católica Boliviana "San Pablo"
                    """.formatted(
                            usuario.getNombre(),
                            usuario.getApellido(),
                            usuario.getCi(),
                            contraseniaGenerada
                    );
                    enviarCorreo(usuario.getCorreo(), "Credenciales de acceso - Sistema USEI", cuerpo);
                } catch (Exception e) {
                    System.err.println("Error al enviar correo a " + usuario.getCorreo() + ": " + e.getMessage());
                }
            }
            Usuario saved = usuarioDAO.save(usuario);
            registrarLog(saved, "Creación de usuario");
            return saved;

        }
        return usuarioDAO.save(usuario);
    }

    // Eliminar usuario por ID
    @Override
    @Transactional
    public void deleteById(Long id) {
        Usuario usuario = usuarioDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para eliminar"));

        Usuario dummyUser = usuarioDAO.findById(1L)
                .orElseThrow(() -> new RuntimeException("No existe usuario sistema (id=1) para reasignar logs"));

        List<LogUsuario> logs = logUsuarioDAO.findByUsuario(usuario);
        for (LogUsuario log : logs) {
            log.setUsuario(dummyUser);
        }
        logUsuarioDAO.saveAll(logs);
        registrarLog(dummyUser, "Eliminación del usuario: " + usuario.getNombre() + " " + usuario.getApellido());
        usuarioDAO.delete(usuario);

        System.out.println("✅ Usuario eliminado correctamente y logs reasignados a usuario sistema.");
    }



    @Override
    @Transactional
    public Usuario update(Usuario usuario, Long id) {
        Usuario u = usuarioDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        u.setNombre(usuario.getNombre());
        u.setApellido(usuario.getApellido());
        u.setTelefono(usuario.getTelefono());
        u.setCorreo(usuario.getCorreo());
        u.setCarrera(usuario.getCarrera());
        u.setCi(usuario.getCi());
        u.setCambioContrasenia(usuario.getCambioContrasenia());
        if (usuario.getRolEntity() != null) {
            u.setRolEntity(usuario.getRolEntity());
            u.setRol(usuario.getRolEntity().getNombreRol());
        }
        Usuario updated = usuarioDAO.save(u);
        registrarLog(updated, "Actualización de usuario");
        return updated;

    }

    /* ==========================
       LOGIN (plano; recomendado usar SecurityBL.login() en API)
       ========================== */
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> login(String correo, String contraseniaIngresada) {
        Optional<Usuario> ou = usuarioDAO.findByCorreo(correo);
        Usuario usuario = ou.orElse(null);
        if (usuario == null) return Optional.empty();

        Contrasenia pass = usuario.getContraseniaEntity();
        if (pass != null && passwordEncoder.matches(contraseniaIngresada, pass.getContrasenia())) {
            return Optional.of(usuario);
        }
        return Optional.empty();
    }

    /* ==========================
       BÚSQUEDA POR CORREO (lo pide SecurityBL)
       ========================== */
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByCorreo(String correo) {
        return usuarioDAO.findByCorreo(correo);
    }

    /* ==========================
       CÓDIGO DE VERIFICACIÓN EMAIL
       ========================== */
    @Override
    @Transactional(readOnly = true)
    public Long findByMail(String correo) {
        return usuarioDAO.findByCorreo(correo)
                .map(Usuario::getIdUsuario)
                .orElse(0L);
    }

    @Override
    public void enviarCodigoVerificacion(String correo) throws MessagingException {
        Usuario usuario = usuarioDAO.findByCorreo(correo).orElse(null);
        if (usuario == null)
            throw new MessagingException("No existe usuario con correo: " + correo);

        this.codigoVerificacion = generarCodigoVerificacion();
        String cuerpo = "Estimado " + usuario.getNombre()
                + ", su código de verificación es: " + this.codigoVerificacion;

        enviarCorreo(correo, "Código de seguridad para cambio de contraseña", cuerpo);
    }

    private String generarCodigoVerificacion() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder codigo = new StringBuilder(6);
        for (int i = 0; i < 6; i++)
            codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        return codigo.toString();
    }

    @Override
    public String obtenerCodigoVerificacion() { return this.codigoVerificacion; }

    private void enviarCorreo(String to, String subject, String body) throws MessagingException {
        try {
            System.out.println("🔹 === EMAIL SENDING DEBUG ===");
            System.out.println("📧 To: " + to);
            System.out.println("📝 Subject: " + subject);
            System.out.println("📄 Body length: " + (body != null ? body.length() : 0) + " characters");
            
            // For development/testing - skip SSL verification
            System.setProperty("mail.smtp.ssl.trust", "*");
            System.setProperty("mail.smtp.starttls.enable", "true");
            System.setProperty("mail.smtp.ssl.checkserveridentity", "false");
            
            System.out.println("🔧 Creating MimeMessage...");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body != null ? body : "", false); // Changed to false for plain text
            helper.setFrom("pruebasu123@gmail.com");
            
            System.out.println("📤 Sending email via mailSender...");
            mailSender.send(message);
            
            System.out.println("✅ Email sent successfully!");
            System.out.println("🔹 === EMAIL SENDING COMPLETE ===");
            
        } catch (Exception e) {
            // For development: log the error but don't fail the operation
            System.err.println("❌ === EMAIL SENDING FAILED ===");
            System.err.println("❌ Error type: " + e.getClass().getSimpleName());
            System.err.println("❌ Error message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("❌ Root cause: " + e.getCause().getMessage());
            }
            System.err.println("❌ === EMAIL ERROR DETAILS ===");
            e.printStackTrace();
            
            // In production, you would want to throw the exception:
            // throw new MessagingException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /* ==========================
       ROLES
       ========================== */
    @Override
    @Transactional
    public Usuario assignRole(Long userId, Long roleId, String roleName) {
        Usuario user = usuarioDAO.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        Rol rol;
        if (roleId != null)
            rol = rolDAO.findById(roleId).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        else if (roleName != null && !roleName.isBlank())
            rol = rolDAO.findByNombreRol(roleName).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        else throw new RuntimeException("Debe especificar roleId o roleName");

        user.setRolEntity(rol);
        user.setRol(rol.getNombreRol());
        return usuarioDAO.save(user);
    }

    @Override
    @Transactional
    public Usuario removeRole(Long userId) {
        Usuario user = usuarioDAO.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        Rol sinRol = rolDAO.findByNombreRol("SIN_ROL")
                .orElseGet(() -> rolDAO.save(new Rol("SIN_ROL")));
        user.setRolEntity(sinRol);
        user.setRol(sinRol.getNombreRol());
        return usuarioDAO.save(user);
    }

    /* ENVÍO DE CREDENCIALES */
    @Override
    public void enviarCredencialesUsuario(Usuario usuario) {
        try {
            if (usuario == null) throw new RuntimeException("Usuario no válido.");

            // Generar contraseña inicial
            String contraseniaGenerada = buildInitialPassword(
                    nullSafe(usuario.getNombre()),
                    nullSafe(usuario.getApellido()),
                    nullSafe(usuario.getCi())
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(usuario.getCorreo());
            helper.setSubject("Credenciales de acceso - Sistema Encuesta a Tiempo de Graduación - USEI");

            // ✅ Usar logo por URL pública
            String logoUrl = "https://lpz.ucb.edu.bo/wp-content/uploads/2021/09/USEI.png";

            // ✅ Contenido HTML
            String contenido = """
        <html>
        <body style="font-family: Arial, sans-serif; background-color:#f4f6f7; padding:20px; color:#333;">
            <div style="max-width:600px; margin:auto; background:#fff; border-radius:10px; padding:25px; box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                <div style="text-align:center; margin-bottom:20px;">
                    <img src='%s' alt='Logo USEI' style="width:120px;"/>
                </div>

                <p>Estimado/a <strong>%s %s</strong>,</p>

                <p>Reciba un cordial saludo. A través de este mensaje le hacemos llegar sus credenciales de acceso al <strong>Sistema USEI</strong>:</p>

                <div style="background:#eef8f5; border-left:4px solid #63C7B2; padding:12px 18px; margin:20px 0; font-size:15px;">
                    <p><strong>Usuario (correo):</strong> %s</p>
                    <p><strong>Contraseña inicial:</strong> %s</p>
                </div>

                <p>Por motivos de seguridad, se le solicita cambiar su contraseña al ingresar por primera vez al sistema.</p>

                <p style="margin-top:25px;">Atentamente,</p>
                <p><strong>Equipo USEI<br>
                Universidad Católica Boliviana “San Pablo”</strong></p>

                <hr style="border:none; border-top:1px solid #ddd; margin:25px 0;">
                <p style="font-size:12px; color:#777;">Este mensaje fue generado automáticamente. Por favor, no responda a este correo.</p>
            </div>
        </body>
        </html>
        """.formatted(
                    logoUrl,
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getCorreo(),
                    contraseniaGenerada
            );

            helper.setText(contenido, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error al enviar credenciales: " + e.getMessage());
        }
    }


    @Override
    public boolean existsByCi(String ci) { return usuarioDAO.existsByCi(ci); }

    /* ==========================
       Helpers
       ========================== */
    private static String nullSafe(String s) { return (s == null) ? "" : s.trim(); }

    /**
     * Regla única para generar la clave inicial:
     *   inicial del nombre (MAYÚS) + apellido completo en minúsculas (sin espacios) + CI
     * Si prefieres DOS INICIALES, cambia aquí y queda consistente en toda la clase.
     */
    private static String buildInitialPassword(String nombre, String apellido, String ci) {
        String inicialNombre = nombre.isEmpty() ? "" : nombre.substring(0,1).toUpperCase();
        String apellidoMin   = apellido.toLowerCase().replaceAll("\\s+", "");
        return inicialNombre + apellidoMin + ci;
        // Variante con 2 iniciales:
        // String inicialApellido = apellido.isEmpty() ? "" : apellido.substring(0,1).toUpperCase();
        // return inicialNombre + inicialApellido + ci;
    }

    //Metodo auxiliar para el manejo de logs en abm usuario
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void registrarLog(Usuario usuario, String motivo) {
        try {
            if (usuario == null) {
                System.err.println("No se puede registrar log: usuario es null.");
                return;
            }

            // Crear registro de log
            LogUsuario log = new LogUsuario(
                    usuario,
                    motivo,
                    java.time.LocalDateTime.now()
            );

            logUsuarioDAO.save(log);
            System.out.println("Log registrado correctamente: " + motivo + " (Usuario ID: " + usuario.getIdUsuario() + ")");

        } catch (Exception e) {
            System.err.println("Error al registrar log de usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
