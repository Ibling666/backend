package com.usei.usei.dto.response;

public class UsuarioResponseDTO {
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String correo;
    private String ci;
    private int telefono;
    private String carrera;
    private String rol;

    // Constructor
    public UsuarioResponseDTO(Long idUsuario, String nombre, String apellido,
                              String correo, String ci, int telefono,
                              String carrera, String rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.ci = ci;
        this.telefono = telefono;
        this.carrera = carrera;
        this.rol = rol;
    }

    // Getters y setters

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getCi() { return ci; }
    public void setCi(String ci) { this.ci = ci; }

    public int getTelefono() { return telefono; }
    public void setTelefono(int telefono) { this.telefono = telefono; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
