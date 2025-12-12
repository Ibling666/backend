package com.usei.usei.controllers;

import com.usei.usei.models.LogUsuario;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.LogUsuarioDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LogUsuarioService {

    private final LogUsuarioDAO logUsuarioDAO;

    public LogUsuarioService(LogUsuarioDAO logUsuarioDAO) {
        this.logUsuarioDAO = logUsuarioDAO;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLogSeguridad(
            Usuario usuario,
            String motivo,        // LOGIN_OK, LOGIN_FAIL, etc.
            String nivel,         // INFO, WARN, ERROR...
            String mensaje,       // texto legible
            String detalle        // JSON o texto extra (puede ser null)
    ) {
        try {
            LogUsuario log = new LogUsuario(
                    usuario,
                    "SEGURIDAD",    // tipo_log
                    "AUTH",         // modulo (por ejemplo)
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
}
