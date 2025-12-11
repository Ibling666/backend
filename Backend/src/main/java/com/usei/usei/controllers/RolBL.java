package com.usei.usei.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.usei.usei.models.Rol;
import com.usei.usei.repositories.RolDAO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RolBL {

    @Autowired
    private RolDAO rolDAO;

    @Transactional
    public Rol crearRolCompleto(Rol rol) {
        if (rol.getNombreRol() == null || rol.getNombreRol().isBlank()) {
            throw new RuntimeException("El nombre del rol es obligatorio.");
        }

        // Normalizar formato (minúsculas, sin espacios dobles)
        String normalizado = normalizarNombre(rol.getNombreRol());

        // Validar que no sea abreviación
        if (esAbreviacionProhibida(normalizado)) {
            throw new RuntimeException(
                    "Nombre de rol inválido. Usa el nombre completo (por ejemplo, 'Administrador', 'Director', 'Pasantia 2')."
            );
        }

        // Evitar duplicados
        if (rolDAO.existsByNombreRolIgnoreCase(normalizado)) {
            throw new RuntimeException("Ya existe un rol con el nombre " + capitalizar(normalizado));
        }

        rol.setNombreRol(capitalizar(normalizado));
        return rolDAO.save(rol);
    }

    @Transactional
    public Rol actualizarRol(Long idRol, Rol rolActualizado) {
        Rol existente = rolDAO.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + idRol));

        if (rolActualizado.getNombreRol() == null || rolActualizado.getNombreRol().isBlank()) {
            throw new RuntimeException("El nombre del rol no puede estar vacío.");
        }

        String normalizado = normalizarNombre(rolActualizado.getNombreRol());

        if (esAbreviacionProhibida(normalizado)) {
            throw new RuntimeException(
                    "Nombre de rol inválido. Usa el nombre completo (por ejemplo, 'Administrador', 'Director', 'Pasantia 2')."
            );
        }

        // Validar duplicado solo si el nombre se edito
        if (!existente.getNombreRol().equalsIgnoreCase(normalizado)
                && rolDAO.existsByNombreRolIgnoreCase(normalizado)) {
            throw new RuntimeException("Ya existe un rol con el nombre " + capitalizar(normalizado));
        }

        existente.setNombreRol(capitalizar(normalizado));
        existente.setActivo(rolActualizado.getActivo());
        existente.setAccesos(rolActualizado.getAccesos());

        return rolDAO.save(existente);
    }

    private String normalizarNombre(String nombre) {
        return nombre.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    // Capitaliza la primera letra de cada palabra
    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;

        StringBuilder resultado = new StringBuilder(texto.length());
        Matcher m = Pattern.compile("(^|\\s)([a-z])").matcher(texto);
        int lastIndex = 0;
        while (m.find()) {
            resultado.append(texto, lastIndex, m.start());
            resultado.append(m.group(1)); // espacio o inicio
            resultado.append(m.group(2).toUpperCase());
            lastIndex = m.end();
        }
        resultado.append(texto.substring(lastIndex));
        return resultado.toString();
    }

    private boolean esAbreviacionProhibida(String texto) {
        String[] prohibidas = {"admin", "adm", "dir", "est", "pas", "sec"};
        for (String p : prohibidas) {
            if (texto.equalsIgnoreCase(p)) return true;
        }
        return false;
    }

    @Transactional
    public void eliminarRol(Long idRol) {
        Rol rol = rolDAO.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        rolDAO.delete(rol);
    }

    public Iterable<Rol> listarRoles() {
        return rolDAO.findAll();
    }

    public Rol obtenerRolPorId(Long idRol) {
        return rolDAO.findById(idRol).orElse(null);
    }

    @Transactional(readOnly = true)
    public Rol obtenerRolPorNombre(String nombreRol) {
        if (nombreRol == null || nombreRol.isBlank()) return null;
        return rolDAO.findByNombreRolIgnoreCase(nombreRol).orElse(null);
    }

    public Rol cambiarEstadoRol(Long idRol, boolean nuevoEstado) {
        Rol rol = rolDAO.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + idRol));
        rol.setActivo(nuevoEstado);
        return rolDAO.save(rol);
    }
}
