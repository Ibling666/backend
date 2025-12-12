package com.usei.usei.repositories;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import com.usei.usei.models.Usuario;

public interface UsuarioDAO extends CrudRepository<Usuario, Long> {

    // Buscar usuario por correo
    Optional<Usuario> findByCorreo(String correo);

    // Buscar usuario por CI
    Optional<Usuario> findByCi(String ci);

    // Verificar existencia por CI
    boolean existsByCi(String ci);

    // ✅ Verificar existencia por correo (nuevo)
    boolean existsByCorreo(String correo);

    // Contar usuarios con determinado rol
    long countByRolEntity_IdRol(Long idRol);
}
