package com.usei.usei.repositories;

import com.usei.usei.models.LogUsuario;
import com.usei.usei.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogUsuarioDAO extends JpaRepository<LogUsuario, Long> {
    List<LogUsuario> findByUsuario(Usuario usuario);
}
