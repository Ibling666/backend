package com.usei.usei.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RiesgoEventoDTO {
    
    private Long idRiesgo;
    private String titulo;
    private String descripcion;
    private String categoria;
    private Integer probabilidad;
    private Integer impacto;
    private String nivelRiesgo;
    private Integer valorRiesgo;
    private String consecuencias;
    private String planAccion;
    private LocalDateTime fechaRegistro;
    private LocalDate fechaIdentificacion;
    private String responsable;
    private String estado;
    private LocalDateTime fechaActualizacion;
    private String usuarioRegistro;

    // Constructores
    public RiesgoEventoDTO() {
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
}
