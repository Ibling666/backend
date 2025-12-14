package com.usei.usei.controllers;

import com.usei.usei.dto.response.LogUsuarioResponseDTO;
import com.usei.usei.models.LogUsuario;
import com.usei.usei.repositories.LogUsuarioDAO;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LogUsuarioQueryService {

    private final LogUsuarioDAO logUsuarioDAO;

    public LogUsuarioQueryService(LogUsuarioDAO logUsuarioDAO) {
        this.logUsuarioDAO = logUsuarioDAO;
    }

    private LogUsuarioResponseDTO toDto(LogUsuario l) {
        Long userId = (l.getUsuario() != null) ? l.getUsuario().getIdUsuario() : null; // ajusta getter si es distinto
        return new LogUsuarioResponseDTO(
                l.getIdLog(),
                l.getFechaLog(),
                l.getTipoLog(),
                userId,
                l.getModulo(),
                l.getMotivo(),
                l.getNivel(),
                l.getMensaje(),
                l.getDetalle()
        );
    }

    @Transactional(readOnly = true)
    public List<LogUsuarioResponseDTO> listarTodos() {
        return logUsuarioDAO.findAll(Sort.by(Sort.Direction.DESC, "fechaLog"))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<LogUsuarioResponseDTO> listarPaginado(Pageable pageable) {
        return logUsuarioDAO.findAll(pageable).map(this::toDto);
    }
}
