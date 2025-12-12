package com.usei.usei.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.usei.usei.models.Estudiante;

import jakarta.mail.MessagingException;

public interface EstudianteService {

    public Iterable<Estudiante> findAll();

    public Optional<Estudiante> findById(Long id);

    public Estudiante save(Estudiante newEstudiante);

    public void deleteById(Long id);

    public Estudiante update (Estudiante newEstudiante, Long id);

    // ❌ ELIMINAR O COMENTAR ESTE MÉTODO
    // public Optional<Estudiante> login(int ci, String contrasena);

    public void enviarCorreosEstudiantes() throws MessagingException;

    public void enviarCodigoVerificacion(String correo) throws MessagingException;

    public String obtenerCodigoVerificacion();

    public Long findByCorreoInst(String correo) throws MessagingException;
    
    public List<Estudiante> saveAll(List<Estudiante> estudiantes);

    Page<Estudiante> findByNombreContainingOrCiContaining(String nombre, String ci, Pageable pageable);

    Page<Estudiante> findByNombre(String nombre, Pageable pageable);

    Page<Estudiante> findByCi(Integer ci, Pageable pageable);

    Page<Estudiante> findAll(Pageable pageable);

    Optional<Estudiante> existingStudent(int ci);

    List<Integer> findDistinctAnios();
    List<Integer> findDistinctSemestres();
    List<Estudiante> findNoCompletaronEncuestaByAnioAndSemestre(Integer anio, Integer semestre);
    List<Object[]> getEstudiantesCompletaronEncuestaByGeneroAndAnio(Integer anio);

    public List<Integer> findUniqueYears();
    List<Estudiante> findByCarrera(String carrera);
}