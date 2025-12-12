package com.usei.usei.repositories;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import com.usei.usei.models.Rol;

public interface RolDAO extends CrudRepository<Rol, Long> {

    Optional<Rol> findByNombreRolIgnoreCase(String nombreRol);
    Optional<Rol> findByNombreRol(String nombreRol);
    boolean existsByNombreRolIgnoreCase(String nombreRol);
    Iterable<Rol> findByActivo(Boolean activo);

}
