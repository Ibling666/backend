package com.usei.usei.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IndicadorRiesgoDTO {
    
    private Long idIndicador;
    private String nombre;
    private String descripcion;
    private String tipoIndicador;
    private BigDecimal umbralCritico;
    private BigDecimal umbralAdvertencia;
    private BigDecimal valorActual;
    private String unidadMedida;
    private String frecuenciaMedicion;
    private String estadoActual;
    private LocalDateTime ultimaActualizacion;
    private LocalDateTime fechaCreacion;
    private Boolean activo;
    private String usuarioCreacion;

    // Constructores
    public IndicadorRiesgoDTO() {
    }

    // Getters y Setters
    public Long getIdIndicador() {
        return idIndicador;
    }

    public void setIdIndicador(Long idIndicador) {
        this.idIndicador = idIndicador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoIndicador() {
        return tipoIndicador;
    }

    public void setTipoIndicador(String tipoIndicador) {
        this.tipoIndicador = tipoIndicador;
    }

    public BigDecimal getUmbralCritico() {
        return umbralCritico;
    }

    public void setUmbralCritico(BigDecimal umbralCritico) {
        this.umbralCritico = umbralCritico;
    }

    public BigDecimal getUmbralAdvertencia() {
        return umbralAdvertencia;
    }

    public void setUmbralAdvertencia(BigDecimal umbralAdvertencia) {
        this.umbralAdvertencia = umbralAdvertencia;
    }

    public BigDecimal getValorActual() {
        return valorActual;
    }

    public void setValorActual(BigDecimal valorActual) {
        this.valorActual = valorActual;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getFrecuenciaMedicion() {
        return frecuenciaMedicion;
    }

    public void setFrecuenciaMedicion(String frecuenciaMedicion) {
        this.frecuenciaMedicion = frecuenciaMedicion;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(String usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }
}
