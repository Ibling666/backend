package com.usei.usei.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HContraseniaDAO extends JpaRepository<com.usei.usei.models.Contrasenia, Long> {

    // Traer hashes del historial desde una fecha (para no reutilización)
    @Query(value = """
        SELECT h.contrasenia
          FROM H_Contrasenia h
         WHERE h.tx_user = :txUser
           AND h.tx_date >= :fromDate
        """, nativeQuery = true)
    List<String> findHashesSince(@Param("txUser") Long txUser,
                                 @Param("fromDate") LocalDateTime fromDate);

    // Insertar la contraseña actual al historial
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT INTO H_Contrasenia
        (id_pass, contrasenia, fecha_creacion, longitud, complejidad,
         intentos_restantes, ultimo_log, tx_date, tx_user)
        VALUES (:idPass, :hash, :fechaCreacion, :longitud, :complejidad,
                :intentos, :ultimoLog, :txDate, :txUser)
        """, nativeQuery = true)
    int insertHist(@Param("idPass") Long idPass,
                   @Param("hash") String hash,
                   @Param("fechaCreacion") LocalDate fechaCreacion,
                   @Param("longitud") Integer longitud,
                   @Param("complejidad") Integer complejidad,
                   @Param("intentos") Integer intentos,
                   @Param("ultimoLog") LocalDate ultimoLog,
                   @Param("txDate") LocalDateTime txDate,
                   @Param("txUser") Long txUser);
}
