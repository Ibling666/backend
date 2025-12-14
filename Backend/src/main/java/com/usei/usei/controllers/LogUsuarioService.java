package com.usei.usei.controllers;

import com.usei.usei.models.LogUsuario;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.LogUsuarioDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogUsuarioService {

    private final LogUsuarioDAO logUsuarioDAO;

    public LogUsuarioService(LogUsuarioDAO logUsuarioDAO) {
        this.logUsuarioDAO = logUsuarioDAO;
    }

    /**
     * ✅ Método base (NO CAMBIAR): es el que ya usas en todo el sistema.
     * Guarda el log en LogUsuario.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLog(
            Usuario usuario,
            String tipoLog,       // SEGURIDAD, SISTEMA, AUDITORIA, etc.
            String modulo,        // "Módulo Gestión de Roles", "AUTH", etc.
            String motivo,        // CREAR_ROL, SUBIR_CERTIFICADO...
            String nivel,         // INFO, WARN, ERROR...
            String mensaje,       // texto legible
            String detalle        // texto/JSON extra
    ) {
        try {
            LogUsuario log = new LogUsuario(
                    usuario,
                    tipoLog,
                    modulo,
                    motivo,
                    nivel,
                    mensaje,
                    detalle
            );
            logUsuarioDAO.save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ✅ Compatibilidad (NO CAMBIAR): si en otros lados llamabas a este método,
     * sigue funcionando igual.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLogSeguridad(
            Usuario usuario,
            String motivo,
            String nivel,
            String mensaje,
            String detalle
    ) {
        registrarLog(usuario, "SEGURIDAD", "AUTH", motivo, nivel, mensaje, detalle);
    }

    // ==========================================================
    // ✅ Helpers OPCIONALES (no afectan lo anterior)
    // ==========================================================

    /**
     * Helper para auditoría/acciones de módulos (Roles, Usuarios, Certificados, etc.)
     * Evita que repitas "SEGURIDAD" o "AUDITORIA" y el módulo a cada rato.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLogModulo(
            Usuario usuario,
            String modulo,
            String motivo,
            String nivel,
            String mensaje,
            String detalle
    ) {
        registrarLog(usuario, "SEGURIDAD", modulo, motivo, nivel, mensaje, detalle);
    }

    /**
     * Helper corto para logs tipo SISTEMA.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLogSistema(
            Usuario usuario,
            String modulo,
            String motivo,
            String nivel,
            String mensaje,
            String detalle
    ) {
        registrarLog(usuario, "SISTEMA", modulo, motivo, nivel, mensaje, detalle);
    }
}
