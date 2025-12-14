package com.usei.usei.controllers;

import com.usei.usei.models.LogUsuario;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.LogUsuarioDAO;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class LoginLogService {

    private final LogUsuarioDAO logRepo;
    private final EntityManager em;

    public LoginLogService(LogUsuarioDAO logRepo, EntityManager em) {
        this.logRepo = logRepo;
        this.em = em;
    }

    @Transactional
    public void saveLogin(Long idUsuario, String motivo) {
        // compatibilidad con llamadas antiguas
        saveLogin(idUsuario, motivo, "Evento de autenticación: " + motivo);
    }

    @Transactional
    public void saveLogin(Long idUsuario, String motivo, String detalle) {
        if (idUsuario == null) return;

        // referencia perezosa
        Usuario usuarioRef = em.getReference(Usuario.class, idUsuario);

        LogUsuario log = new LogUsuario();
        log.setUsuario(usuarioRef);
        log.setFechaLog(LocalDateTime.now(ZoneId.of("America/La_Paz")));

        log.setTipoLog("SEGURIDAD");
        log.setModulo("AUTH");

        String m = (motivo == null || motivo.isBlank()) ? "LOGIN" : motivo.trim();
        log.setMotivo(m);

        log.setNivel("INFO");
        log.setMensaje(m);

        // ✅ NUNCA null
        String det = (detalle == null || detalle.isBlank())
                ? "Acción de autenticación registrada."
                : detalle.trim();
        log.setDetalle(det);

        logRepo.save(log);
    }
}
