package com.usei.usei.controllers;

import com.usei.usei.controllers.LoginLogService;
import org.springframework.stereotype.Component;

@Component
public class AuditBL {

    private final LoginLogService loginLogService;

    public AuditBL(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    /** Registra login exitoso */
    public void registerLogin(Long idUsuario) {
        loginLogService.saveLogin(idUsuario, "LOGIN");
    }

    /** (Opcional) Registra eventos especiales del flujo de autenticación */
    public void registerEvent(Long idUsuario, String motivo) {
        loginLogService.saveLogin(idUsuario, motivo);
    }
}
