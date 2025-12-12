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

        LogUsuario log = new LogUsuario();
        log.setUsuario(usuarioRef);
        log.setFechaLog(LocalDateTime.now(ZoneId.of("America/La_Paz")));

        // Como es login, lo tratamos como log de seguridad
        log.setTipoLog("SEGURIDAD");     // SEGURIDAD / APLICACION
        log.setModulo("AUTH");           // módulo: autenticación / login

        // El "motivo" que ya le pasas (ej: "LOGIN_OK", "LOGIN_FAIL", "Inicio de sesión")
        log.setMotivo(motivo);

        // Nivel básico: si quieres, puedes refinar esto luego
        log.setNivel("INFO");            // o "WARN" si decides que algo es fallo

        // Mensaje legible (de momento igual que motivo)
        log.setMensaje(motivo);

        // Detalle extra (por ahora nada)
        log.setDetalle(null);

        logRepo.save(log);
    }


}
