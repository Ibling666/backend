package com.usei.usei.repositories;

import com.usei.usei.models.Contrasenia;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ContraseniaDAO extends JpaRepository<Contrasenia, Long> {

    // Leer normal por id (ya viene de JpaRepository -> findById)

    // Leer con bloqueo para escenarios de concurrencia (login / decremento de intentos)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Contrasenia c WHERE c.idPass = :id")
    Optional<Contrasenia> findByIdForUpdate(@Param("id") Long id);

    // Actualizar intentos y último log de forma atómica (opcional, útil en login)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Contrasenia c SET c.intentosRestantes = :intentos, c.ultimoLog = :ultimoLog WHERE c.idPass = :id")
    int updateIntentosYUltimoLog(@Param("id") Long id,
                                 @Param("intentos") Integer intentos,
                                 @Param("ultimoLog") LocalDate ultimoLog);

    // Actualizar al cambiar contraseña (hash + política + fechas + intentos)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE Contrasenia c
              SET c.contrasenia = :hash,
                  c.fechaCreacion = :fechaCreacion,
                  c.ultimoLog = :ultimoLog,
                  c.longitud = :longitud,
                  c.complejidad = :complejidad,
                  c.intentosRestantes = :intentos
            WHERE c.idPass = :id
           """)
    int actualizarTrasCambio(@Param("id") Long id,
                             @Param("hash") String hash,
                             @Param("fechaCreacion") LocalDate fechaCreacion,
                             @Param("ultimoLog") LocalDate ultimoLog,
                             @Param("longitud") Integer longitud,
                             @Param("complejidad") Integer complejidad,
                             @Param("intentos") Integer intentos);
}
