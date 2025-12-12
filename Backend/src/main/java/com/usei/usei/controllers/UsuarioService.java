package com.usei.usei.controllers;

import java.util.Optional;

import com.usei.usei.models.Usuario;
import jakarta.mail.MessagingException;

public interface UsuarioService {

    Iterable<Usuario> findAll();

    Optional<Usuario> findById(Long id);

    Usuario save(Usuario newUsuario);

    void deleteById(Long id);

    Usuario update(Usuario newUsuario, Long id);

    Optional<Usuario> login(String correo, String contrasenia);

    Long findByMail(String correo) throws MessagingException;

    void enviarCodigoVerificacion(String correo) throws MessagingException;

    String obtenerCodigoVerificacion();

    // ==== NUEVO: envío manual de credenciales ====
    void enviarCredencialesUsuario(Usuario usuario);

    // ==== NUEVO: asignación de rol existente ====
    Usuario assignRole(Long userId, Long roleId, String roleName);

    Usuario removeRole(Long userId);

    // ==== NUEVO: verificar duplicado de CI ====
    boolean existsByCi(String ci);

    Optional<Usuario> findByCorreo(String correo);
}
