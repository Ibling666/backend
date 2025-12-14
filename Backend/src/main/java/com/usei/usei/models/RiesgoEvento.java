package com.usei.usei.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "riesgo_evento")
public class RiesgoEvento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_riesgo")
    private Long idRiesgo;

    @Basic(optional = false)
    @Column(name = "titulo")
    private String titulo;

    @Basic(optional = false)
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Basic(optional = false)
    @Column(name = "categoria")
    private String categoria;

    // Análisis de riesgo
    @Basic(optional = false)
    @Column(name = "probabilidad")
    private Integer probabilidad; // 1-5

    @Basic(optional = false)
    @Column(name = "impacto")
    private Integer impacto; // 1-5

    @Basic(optional = false)
    @Column(name = "nivel_riesgo")
    private String nivelRiesgo; // Bajo, Medio, Alto, Crítico

    @Basic(optional = false)
    @Column(name = "valor_riesgo")
    private Integer valorRiesgo; // probabilidad * impacto

    // Consecuencias y mitigación
    @Basic(optional = false)
    @Column(name = "consecuencias", columnDefinition = "TEXT")
    private String consecuencias;

    @Basic(optional = false)
    @Column(name = "plan_accion", columnDefinition = "TEXT")
    private String planAccion;

    // Información administrativa
    @Basic(optional = false)
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Basic(optional = false)
    @Column(name = "fecha_identificacion")
    private LocalDate fechaIdentificacion;

    @Basic(optional = false)
    @Column(name = "responsable")
    private String responsable;

    @Basic(optional = false)
    @Column(name = "estado")
    private String estado; // Identificado, En Análisis, En Mitigación, Controlado, Cerrado

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Basic(optional = false)
    @Column(name = "usuario_registro")
    private String usuarioRegistro;

    // Constructores
    public RiesgoEvento() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = "Identificado";
    }

    // Getters y Setters
    public Long getIdRiesgo() {
        return idRiesgo;
    }

    public void setIdRiesgo(Long idRiesgo) {
        this.idRiesgo = idRiesgo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getProbabilidad() {
        return probabilidad;
    }

    public void setProbabilidad(Integer probabilidad) {
        this.probabilidad = probabilidad;
    }

    public Integer getImpacto() {
        return impacto;
    }

    public void setImpacto(Integer impacto) {
        this.impacto = impacto;
    }

    public String getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(String nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public Integer getValorRiesgo() {
        return valorRiesgo;
    }

    public void setValorRiesgo(Integer valorRiesgo) {
        this.valorRiesgo = valorRiesgo;
    }

    public String getConsecuencias() {
        return consecuencias;
    }

    public void setConsecuencias(String consecuencias) {
        this.consecuencias = consecuencias;
    }

    public String getPlanAccion() {
        return planAccion;
    }

    public void setPlanAccion(String planAccion) {
        this.planAccion = planAccion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDate getFechaIdentificacion() {
        return fechaIdentificacion;
    }

    public void setFechaIdentificacion(LocalDate fechaIdentificacion) {
        this.fechaIdentificacion = fechaIdentificacion;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    // Método para calcular el nivel de riesgo
    public void calcularNivelRiesgo() {
        this.valorRiesgo = this.probabilidad * this.impacto;
        
        if (this.valorRiesgo >= 20) {
            this.nivelRiesgo = "Crítico";
        } else if (this.valorRiesgo >= 13) {
            this.nivelRiesgo = "Alto";
        } else if (this.valorRiesgo >= 7) {
            this.nivelRiesgo = "Medio";
        } else {
            this.nivelRiesgo = "Bajo";
        }
    }

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (estado == null) {
            estado = "Identificado";
        }
        calcularNivelRiesgo();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
        calcularNivelRiesgo();
    }
}
