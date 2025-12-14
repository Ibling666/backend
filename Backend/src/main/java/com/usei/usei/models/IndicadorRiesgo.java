package com.usei.usei.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "indicador_riesgo")
public class IndicadorRiesgo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_indicador")
    private Long idIndicador;

    @Basic(optional = false)
    @Column(name = "nombre")
    private String nombre;

    @Basic(optional = false)
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Basic(optional = false)
    @Column(name = "tipo_indicador")
    private String tipoIndicador;

    @Basic(optional = false)
    @Column(name = "umbral_critico")
    private BigDecimal umbralCritico;

    @Basic(optional = false)
    @Column(name = "umbral_advertencia")
    private BigDecimal umbralAdvertencia;

    @Basic(optional = false)
    @Column(name = "valor_actual")
    private BigDecimal valorActual;

    @Basic(optional = false)
    @Column(name = "unidad_medida")
    private String unidadMedida;

    @Basic(optional = false)
    @Column(name = "frecuencia_medicion")
    private String frecuenciaMedicion;

    @Basic(optional = false)
    @Column(name = "estado_actual")
    private String estadoActual; // Normal, Advertencia, Crítico

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @Basic(optional = false)
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Basic(optional = false)
    @Column(name = "activo")
    private Boolean activo;

    @Basic(optional = false)
    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    // Constructores
    public IndicadorRiesgo() {
        this.fechaCreacion = LocalDateTime.now();
        this.ultimaActualizacion = LocalDateTime.now();
        this.activo = true;
        this.estadoActual = "Normal";
        this.valorActual = BigDecimal.ZERO;
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
        evaluarEstado();
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

    // Método para evaluar el estado según umbrales
    public void evaluarEstado() {
        if (valorActual.compareTo(umbralCritico) >= 0) {
            this.estadoActual = "Crítico";
        } else if (valorActual.compareTo(umbralAdvertencia) >= 0) {
            this.estadoActual = "Advertencia";
        } else {
            this.estadoActual = "Normal";
        }
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        ultimaActualizacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
        if (estadoActual == null) {
            estadoActual = "Normal";
        }
        if (valorActual == null) {
            valorActual = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ultimaActualizacion = LocalDateTime.now();
        evaluarEstado();
    }
}
