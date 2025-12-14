package com.usei.usei.dto.response;

import java.time.LocalDateTime;

public class LogUsuarioResponseDTO {
    public Long idLog;
    public LocalDateTime fechaLog;
    public String tipoLog;
    public Long usuarioIdUsuario;
    public String modulo;
    public String motivo;
    public String nivel;
    public String mensaje;
    public String detalle;

    public LogUsuarioResponseDTO() {}

    public LogUsuarioResponseDTO(Long idLog, LocalDateTime fechaLog, String tipoLog, Long usuarioIdUsuario,
                              String modulo, String motivo, String nivel, String mensaje, String detalle) {
        this.idLog = idLog;
        this.fechaLog = fechaLog;
        this.tipoLog = tipoLog;
        this.usuarioIdUsuario = usuarioIdUsuario;
        this.modulo = modulo;
        this.motivo = motivo;
        this.nivel = nivel;
        this.mensaje = mensaje;
        this.detalle = detalle;
    }
}
