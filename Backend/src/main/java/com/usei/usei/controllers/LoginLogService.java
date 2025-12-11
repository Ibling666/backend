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
        // No hace SELECT: usa referencia perezosa
        Usuario usuarioRef = em.getReference(Usuario.class, idUsuario);
        LogUsuario log = new LogUsuario(
                usuarioRef,
                motivo,
                LocalDateTime.now(ZoneId.of("America/La_Paz"))
        );
        logRepo.save(log);
    }
}
