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
    public void registrar(Usuario usuario, String motivo) {
        try {
            if (usuario == null) {
                System.err.println("⚠️ No se puede registrar log: usuario es null");
                return;
            }

            LogUsuario log = new LogUsuario(
                    usuario,
                    motivo,
                    LocalDateTime.now()
            );

            logUsuarioDAO.save(log);
            System.out.println("✅ Log guardado correctamente: " + motivo);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error al registrar log: " + e.getMessage());
        }
    }
}
