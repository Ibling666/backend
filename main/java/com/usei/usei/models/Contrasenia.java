package com.usei.usei.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "contrasenia")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Contrasenia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pass")
    private Long idPass;

    @Column(nullable = false, length = 100)
    private String contrasenia;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();

    @Column(nullable = false)
    private int longitud;

    @Column(nullable = false)
    private int complejidad;

    @Column(name = "intentos_restantes", nullable = false)
    private int intentosRestantes = 3;

    @Column(name = "ultimo_log", nullable = false)
    private LocalDate ultimoLog = LocalDate.now();

    public Contrasenia() {}

    public Contrasenia(String contrasenia, int longitud, int complejidad) {
        this.contrasenia = contrasenia;
        this.longitud = longitud;
        this.complejidad = complejidad;
        this.fechaCreacion = LocalDate.now();
        this.intentosRestantes = 3;
        this.ultimoLog = LocalDate.now();
    }

    // --- Getters y Setters correctos ---
    public Long getIdPass() { return idPass; }
    public void setIdPass(Long idPass) { this.idPass = idPass; }

    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public int getLongitud() { return longitud; }
    public void setLongitud(int longitud) { this.longitud = longitud; }

    public int getComplejidad() { return complejidad; }
    public void setComplejidad(int complejidad) { this.complejidad = complejidad; }

    public int getIntentosRestantes() { return intentosRestantes; }
    public void setIntentosRestantes(int intentosRestantes) { this.intentosRestantes = intentosRestantes; }

    public LocalDate getUltimoLog() { return ultimoLog; }
    public void setUltimoLog(LocalDate ultimoLog) { this.ultimoLog = ultimoLog; }
}
