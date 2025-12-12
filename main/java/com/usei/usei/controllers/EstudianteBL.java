package com.usei.usei.controllers;

import com.usei.usei.models.Estudiante;
import com.usei.usei.repositories.EstudianteDAO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstudianteBL implements EstudianteService {

    final private EstudianteDAO estudianteDAO;
    final private JavaMailSender mailSender;
    
    // ✅ AGREGAR ESTE CAMPO
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String codigoVerificacion;

    @Autowired
    public EstudianteBL(EstudianteDAO estudianteDAO, JavaMailSender mailSender, SecurityBL securityBL) {
        this.estudianteDAO = estudianteDAO;
        this.mailSender = mailSender;
    }

    @Override
    @Transactional
    public List<Estudiante> saveAll(List<Estudiante> estudiantes) {
        // ✅ HASHEAR CONTRASEÑAS ANTES DE GUARDAR
        for (Estudiante estudiante : estudiantes) {
            if (estudiante.getContrasena() != null && 
                !estudiante.getContrasena().startsWith("$2a$") &&
                !estudiante.getContrasena().startsWith("$2b$")) {
                // Solo hashear si no está ya hasheada
                String hashedPassword = passwordEncoder.encode(estudiante.getContrasena());
                estudiante.setContrasena(hashedPassword);
            }
        }
        return estudianteDAO.saveAll(estudiantes);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<Estudiante> findAll() {
        return estudianteDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Estudiante> findById(Long id) {
        return estudianteDAO.findById(id);
    }

    @Override
    @Transactional
    public Estudiante save(Estudiante estudiante) {
        // ✅ HASHEAR CONTRASEÑA SI NO ESTÁ HASHEADA
        if (estudiante.getContrasena() != null && 
            !estudiante.getContrasena().startsWith("$2a$") &&
            !estudiante.getContrasena().startsWith("$2b$")) {
            String hashedPassword = passwordEncoder.encode(estudiante.getContrasena());
            estudiante.setContrasena(hashedPassword);
        }
        return estudianteDAO.save(estudiante);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        estudianteDAO.deleteById(id);
    }

    @Override
    @Transactional
    public Estudiante update(Estudiante estudiante, Long id) {
        Optional<Estudiante> existingEstudiante = estudianteDAO.findById(id);
        if (existingEstudiante.isPresent()) {
            Estudiante estudianteToUpdate = existingEstudiante.get();
            estudianteToUpdate.setNombre(estudiante.getNombre());
            estudianteToUpdate.setApellido(estudiante.getApellido());
            
            // ✅ SOLO ACTUALIZAR CONTRASEÑA SI SE PROPORCIONA UNA NUEVA
            if (estudiante.getContrasena() != null && !estudiante.getContrasena().isEmpty()) {
                // Si la nueva contraseña no está hasheada, hashearla
                if (!estudiante.getContrasena().startsWith("$2a$") &&
                    !estudiante.getContrasena().startsWith("$2b$")) {
                    String hashedPassword = passwordEncoder.encode(estudiante.getContrasena());
                    estudianteToUpdate.setContrasena(hashedPassword);
                } else {
                    estudianteToUpdate.setContrasena(estudiante.getContrasena());
                }
            }
            
            return estudianteDAO.save(estudianteToUpdate);
        } else {
            throw new RuntimeException("Estudiante no encontrado con el id: " + id);
        }
    }

    // ❌ ELIMINAR O COMENTAR ESTE MÉTODO (ya no se usa)
    // @Override
    // @Transactional(readOnly = true)
    // public Optional<Estudiante> login(int ci, String contrasena) {
    //     return estudianteDAO.findByCiAndContrasena(ci, contrasena);
    // }

    @Override
    @Transactional(readOnly = true)
    public Optional<Estudiante> existingStudent(int ci) {
        return estudianteDAO.findByCi(ci);
    }

    @Override
    public void enviarCorreosEstudiantes() throws MessagingException {
        List<Estudiante> estudiantes = estudianteDAO.findByCorreoInstitucionalIsNotNull();

        String asunto = "Información sobre Alumni UCB";
        String mensaje = "Estimado estudiante, usted está a punto de salir y por lo tanto puede acceder al cuestionario para ser parte de Alumni UCB. Por favor, revise el siguiente enlace importante: ";
        String link = "<a href='http://localhost:5173/'>Acceder al enlace</a>";

        for (Estudiante estudiante : estudiantes) {
            String correo = estudiante.getCorreoInstitucional();
            if ("completo".equalsIgnoreCase(estudiante.getEstadoInvitacion())) {
                System.out.println("Correo no enviado, estado de invitación completo para: " + estudiante.getNombre());
                continue;
            }

            String cuerpoCorreo = mensaje + link;
            enviarCorreo(correo, asunto, cuerpoCorreo);
            System.out.println("Correo enviado a: " + estudiante.getNombre());
            estudiante.setEstadoInvitacion("completo");
            estudianteDAO.save(estudiante);
        }
    }
    
    @Override
    public void enviarCodigoVerificacion(String correo) throws MessagingException {
        Estudiante estudiante = estudianteDAO.findByCorreoInstitucional(correo).orElse(null);
        
        if (estudiante == null) {
            throw new MessagingException("No se encontró estudiante con ese correo");
        }

        String asunto = "Codigo de Seguridad para cambio de contraseña";
        String mensaje = "Estimado estudiante, usted a solicitado un cambio de contraseña, por favor ingrese el siguiente codigo de seguridad: ";

        if (!correo.endsWith("@ucb.edu.bo")) {
            System.out.println("Correo inválido: " + correo);
            throw new MessagingException("Correo inválido");
        } else {
            codigoVerificacion = generarCodigoVerificacion();
            String cuerpoCorreo = mensaje + codigoVerificacion;
            enviarCorreo(correo, asunto, cuerpoCorreo);
            System.out.println("Correo enviado a: " + estudiante.getNombre());
        }
    }

    private void enviarCorreo(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
    }

    private String generarCodigoVerificacion() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder codigo = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(caracteres.length());
            codigo.append(caracteres.charAt(index));
        }

        return codigo.toString();
    }

    @Override
    public String obtenerCodigoVerificacion() {
        return codigoVerificacion;
    }

    @Override
    public Long findByCorreoInst(String correo) {
        Estudiante estudiante = estudianteDAO.findByCorreoInstitucional(correo).orElse(null);
        if (estudiante == null) {
            return 0L;
        } else {
            return estudiante.getIdEstudiante();
        }
    }

    @Override
    public Page<Estudiante> findByNombreContainingOrCiContaining(String nombre, String ci, Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page<Estudiante> findByNombre(String nombre, Pageable pageable) {
        return estudianteDAO.findByNombreContainingIgnoreCase(nombre, pageable);
    }

    @Override
    public Page<Estudiante> findByCi(Integer ci, Pageable pageable) {
        return estudianteDAO.findByCi(ci, pageable);
    }

    @Override
    public Page<Estudiante> findAll(Pageable pageable) {
        return estudianteDAO.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> findDistinctAnios() {
        return estudianteDAO.findDistinctAnios();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Integer> findDistinctSemestres() {
        return estudianteDAO.findDistinctSemestres();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estudiante> findNoCompletaronEncuestaByAnioAndSemestre(Integer anio, Integer semestre) {
        return estudianteDAO.findNoCompletaronEncuestaByAnioAndSemestre(anio, semestre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getEstudiantesCompletaronEncuestaByGeneroAndAnio(Integer anio) {
        return estudianteDAO.countEstudiantesCompletaronEncuestaByGeneroAndAnio(anio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> findUniqueYears() {
        return estudianteDAO.findUniqueYears();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estudiante> findByCarrera(String carrera) {
        System.out.println("Buscando estudiantes para la carrera: " + carrera);
        List<Estudiante> estudiantes = estudianteDAO.findByCarrera(carrera);
        System.out.println("Estudiantes encontrados: " + estudiantes.size());
        return estudiantes;
    }
}