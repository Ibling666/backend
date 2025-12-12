package com.usei.usei.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Log_Usuario")
public class LogUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @Column(name = "fecha_log", nullable = false)
    private LocalDateTime fechaLog;

    @Column(name = "tipo_log", length = 150, nullable = false)
    private String tipoLog;      // SEGURIDAD / APLICACION

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Usuario_id_usuario")   // FK de tu tabla
    private Usuario usuario;    // Usuario_id_usuario

    @Column(name = "modulo", length = 150, nullable = false)
    private String modulo;      // AUTH, ENCUESTA, CERTIFICADO, etc.

    @Column(name = "motivo", length = 150, nullable = false)
    private String motivo;      // LOGIN_OK, LOGIN_FAIL, CAMBIO_ROL, etc.

    @Column(name = "nivel", length = 150, nullable = false)
    private String nivel;       // INFO, WARN, ERROR, CRITICO

    @Column(name = "mensaje", length = 150)
    private String mensaje;     // Descripción cortita legible

    @Column(name = "detalle", length = 150)
    private String detalle;     // Info extra (ids, antes/después, etc.)

    public LogUsuario() {
    }

    // Constructor de conveniencia (opcional)
    public LogUsuario(
            Usuario usuario,
            String tipoLog,
            String modulo,
            String motivo,
            String nivel,
            String mensaje,
            String detalle
    ) {
        this.usuario = usuario;
        this.tipoLog = tipoLog;
        this.modulo = modulo;
        this.motivo = motivo;
        this.nivel = nivel;
        this.mensaje = mensaje;
        this.detalle = detalle;
        this.fechaLog = LocalDateTime.now();
    }

    // ===== Getters y setters =====

    public Long getIdLog() { return idLog; }
    public void setIdLog(Long idLog) { this.idLog = idLog; }

    public LocalDateTime getFechaLog() { return fechaLog; }
    public void setFechaLog(LocalDateTime fechaLog) { this.fechaLog = fechaLog; }

    public String getTipoLog() { return tipoLog; }
    public void setTipoLog(String tipoLog) { this.tipoLog = tipoLog; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
