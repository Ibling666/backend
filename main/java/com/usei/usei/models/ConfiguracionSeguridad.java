package com.usei.usei.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_seguridad")
public class ConfiguracionSeguridad implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_config")
    private Long idConfig;

    @Basic(optional = false)
    @Column(name = "min_longitud_contrasenia")
    private int minLongitudContrasenia;

    @Basic(optional = false)
    @Column(name = "max_intentos_login")
    private int maxIntentosLogin;

    @Basic(optional = false)
    @Column(name = "dias_expiracion_contrasenia")
    private int diasExpiracionContrasenia;

    @Basic(optional = false)
    @Column(name = "meses_no_reutilizar")
    private int mesesNoReutilizar;

    @Basic(optional = false)
    @Column(name = "requerir_mayusculas")
    private boolean requerirMayusculas;

    @Basic(optional = false)
    @Column(name = "requerir_minusculas")
    private boolean requerirMinusculas;

    @Basic(optional = false)
    @Column(name = "requerir_numeros")
    private boolean requerirNumeros;

    @Basic(optional = false)
    @Column(name = "requerir_simbolos")
    private boolean requerirSimbolos;

    @Basic(optional = false)
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Basic(optional = false)
    @Column(name = "usuario_modificacion")
    private Long usuarioModificacion;

    @Basic(optional = false)
    @Column(name = "activa")
    private boolean activa;

    // ====== Constructors ======
    public ConfiguracionSeguridad() {
        this.fechaModificacion = LocalDateTime.now();
        this.activa = true;
    }

    public ConfiguracionSeguridad(int minLongitudContrasenia, int maxIntentosLogin, 
                                  int diasExpiracionContrasenia, int mesesNoReutilizar,
                                  boolean requerirMayusculas, boolean requerirMinusculas,
                                  boolean requerirNumeros, boolean requerirSimbolos,
                                  Long usuarioModificacion) {
        this();
        this.minLongitudContrasenia = minLongitudContrasenia;
        this.maxIntentosLogin = maxIntentosLogin;
        this.diasExpiracionContrasenia = diasExpiracionContrasenia;
        this.mesesNoReutilizar = mesesNoReutilizar;
        this.requerirMayusculas = requerirMayusculas;
        this.requerirMinusculas = requerirMinusculas;
        this.requerirNumeros = requerirNumeros;
        this.requerirSimbolos = requerirSimbolos;
        this.usuarioModificacion = usuarioModificacion;
    }

    // ====== Getters and Setters ======
    public Long getIdConfig() { return idConfig; }
    public void setIdConfig(Long idConfig) { this.idConfig = idConfig; }

    public int getMinLongitudContrasenia() { return minLongitudContrasenia; }
    public void setMinLongitudContrasenia(int minLongitudContrasenia) { this.minLongitudContrasenia = minLongitudContrasenia; }

    public int getMaxIntentosLogin() { return maxIntentosLogin; }
    public void setMaxIntentosLogin(int maxIntentosLogin) { this.maxIntentosLogin = maxIntentosLogin; }

    public int getDiasExpiracionContrasenia() { return diasExpiracionContrasenia; }
    public void setDiasExpiracionContrasenia(int diasExpiracionContrasenia) { this.diasExpiracionContrasenia = diasExpiracionContrasenia; }

    public int getMesesNoReutilizar() { return mesesNoReutilizar; }
    public void setMesesNoReutilizar(int mesesNoReutilizar) { this.mesesNoReutilizar = mesesNoReutilizar; }

    public boolean isRequerirMayusculas() { return requerirMayusculas; }
    public void setRequerirMayusculas(boolean requerirMayusculas) { this.requerirMayusculas = requerirMayusculas; }

    public boolean isRequerirMinusculas() { return requerirMinusculas; }
    public void setRequerirMinusculas(boolean requerirMinusculas) { this.requerirMinusculas = requerirMinusculas; }

    public boolean isRequerirNumeros() { return requerirNumeros; }
    public void setRequerirNumeros(boolean requerirNumeros) { this.requerirNumeros = requerirNumeros; }

    public boolean isRequerirSimbolos() { return requerirSimbolos; }
    public void setRequerirSimbolos(boolean requerirSimbolos) { this.requerirSimbolos = requerirSimbolos; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public Long getUsuarioModificacion() { return usuarioModificacion; }
    public void setUsuarioModificacion(Long usuarioModificacion) { this.usuarioModificacion = usuarioModificacion; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    // ====== Utility Methods ======
    @Override
    public int hashCode() {
        return (idConfig != null ? idConfig.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ConfiguracionSeguridad)) return false;
        ConfiguracionSeguridad other = (ConfiguracionSeguridad) object;
        return (this.idConfig != null || other.idConfig == null)
                && (this.idConfig == null || this.idConfig.equals(other.idConfig));
    }

    @Override
    public String toString() {
        return "ConfiguracionSeguridad[ idConfig=" + idConfig + 
               ", minLongitud=" + minLongitudContrasenia + 
               ", maxIntentos=" + maxIntentosLogin + " ]";
    }
}