package com.usei.usei.controllers;

import org.springframework.stereotype.Component;

@Component
public class AuditBL {

    private final LoginLogService loginLogService;

    public AuditBL(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    /** Registra login exitoso */
    public void registerLogin(Long idUsuario) {
        loginLogService.saveLogin(idUsuario, "LOGIN", "Inicio de sesión exitoso");
    }

    /** Registra eventos especiales del flujo de autenticación */
    public void registerEvent(Long idUsuario, String motivo) {
        // si no mandan detalle, al menos se guarda uno por defecto
        loginLogService.saveLogin(idUsuario, motivo, "Evento de autenticación: " + motivo);
    }
}
