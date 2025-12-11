package com.usei.usei.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Log_Usuario") // respeta el nombre exacto de la tabla
public class LogUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @Column(name = "fecha_log", nullable = false)
    private LocalDateTime fechaLog;

    @Column(name = "motivo", length = 150, nullable = false)
    private String motivo;

    // FK -> Usuario.id_usuario
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Usuario_id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;

    public LogUsuario() {}

    public LogUsuario(Usuario usuario, String motivo, LocalDateTime fechaLog) {
        this.usuario = usuario;
        this.motivo = motivo;
        this.fechaLog = fechaLog;
    }

    // getters & setters
    public Long getIdLog() { return idLog; }
    public void setIdLog(Long idLog) { this.idLog = idLog; }

    public LocalDateTime getFechaLog() { return fechaLog; }
    public void setFechaLog(LocalDateTime fechaLog) { this.fechaLog = fechaLog; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
