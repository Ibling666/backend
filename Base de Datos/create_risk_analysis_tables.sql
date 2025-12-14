-- ===============================================
-- MÓDULO DE ANÁLISIS DE RIESGOS
-- Sistema de Gestión de Riesgos de Seguridad
-- ===============================================

-- Eliminar tablas existentes si existen (en orden correcto por dependencias)
DROP TABLE IF EXISTS historial_kri CASCADE;
DROP TABLE IF EXISTS accion_mitigacion CASCADE;
DROP TABLE IF EXISTS riesgo_evento CASCADE;
DROP TABLE IF EXISTS indicador_riesgo CASCADE;

-- Tabla para almacenar eventos de riesgo
CREATE TABLE riesgo_evento (
    id_riesgo SERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    categoria VARCHAR(100) NOT NULL, -- Ej: 'Seguridad de Datos', 'Acceso no autorizado', 'Pérdida de información'
    
    -- Análisis de riesgo
    probabilidad INTEGER NOT NULL CHECK (probabilidad >= 1 AND probabilidad <= 5), -- 1=Muy Baja, 2=Baja, 3=Media, 4=Alta, 5=Muy Alta
    impacto INTEGER NOT NULL CHECK (impacto >= 1 AND impacto <= 5), -- 1=Muy Bajo, 2=Bajo, 3=Medio, 4=Alto, 5=Muy Alto
    nivel_riesgo VARCHAR(20) NOT NULL, -- 'Bajo', 'Medio', 'Alto', 'Crítico'
    valor_riesgo INTEGER NOT NULL, -- Calculado: probabilidad * impacto (1-25)
    
    -- Consecuencias y mitigación
    consecuencias TEXT NOT NULL,
    plan_accion TEXT NOT NULL,
    
    -- Información administrativa
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_identificacion DATE NOT NULL,
    responsable VARCHAR(150) NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'Identificado', -- 'Identificado', 'En Análisis', 'En Mitigación', 'Controlado', 'Cerrado'
    
    -- Auditoría
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_registro VARCHAR(100) NOT NULL
);

-- Tabla para indicadores clave de riesgo (KRI)
CREATE TABLE indicador_riesgo (
    id_indicador SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    tipo_indicador VARCHAR(100) NOT NULL, -- Ej: 'Operacional', 'Seguridad', 'Cumplimiento'
    umbral_critico DECIMAL(10,2) NOT NULL,
    umbral_advertencia DECIMAL(10,2) NOT NULL,
    valor_actual DECIMAL(10,2) NOT NULL DEFAULT 0,
    unidad_medida VARCHAR(50) NOT NULL, -- Ej: 'Intentos', 'Porcentaje', 'Número'
    frecuencia_medicion VARCHAR(50) NOT NULL, -- 'Diario', 'Semanal', 'Mensual'
    
    -- Estado del indicador
    estado_actual VARCHAR(20) NOT NULL DEFAULT 'Normal', -- 'Normal', 'Advertencia', 'Crítico'
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    
    -- Auditoría
    usuario_creacion VARCHAR(100) NOT NULL
);

-- Tabla para historial de mediciones de KRI
CREATE TABLE historial_kri (
    id_medicion SERIAL PRIMARY KEY,
    id_indicador INTEGER NOT NULL REFERENCES indicador_riesgo(id_indicador),
    valor_medido DECIMAL(10,2) NOT NULL,
    fecha_medicion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_evaluado VARCHAR(20) NOT NULL, -- 'Normal', 'Advertencia', 'Crítico'
    observaciones TEXT,
    usuario_medicion VARCHAR(100) NOT NULL
);

-- Tabla para acciones de mitigación de riesgos
CREATE TABLE accion_mitigacion (
    id_accion SERIAL PRIMARY KEY,
    id_riesgo INTEGER NOT NULL REFERENCES riesgo_evento(id_riesgo),
    descripcion_accion TEXT NOT NULL,
    responsable_accion VARCHAR(150) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_limite DATE NOT NULL,
    fecha_completada DATE,
    estado VARCHAR(50) NOT NULL DEFAULT 'Pendiente', -- 'Pendiente', 'En Progreso', 'Completada', 'Cancelada'
    efectividad VARCHAR(20), -- 'Efectiva', 'Parcialmente Efectiva', 'No Efectiva'
    observaciones TEXT,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_riesgo_estado ON riesgo_evento(estado);
CREATE INDEX idx_riesgo_nivel ON riesgo_evento(nivel_riesgo);
CREATE INDEX idx_riesgo_fecha ON riesgo_evento(fecha_identificacion);
CREATE INDEX idx_indicador_activo ON indicador_riesgo(activo);
CREATE INDEX idx_historial_kri_fecha ON historial_kri(fecha_medicion);
CREATE INDEX idx_accion_estado ON accion_mitigacion(estado);

-- Insertar 2 indicadores clave de riesgo predefinidos
INSERT INTO indicador_riesgo 
(nombre, descripcion, tipo_indicador, umbral_critico, umbral_advertencia, valor_actual, unidad_medida, frecuencia_medicion, usuario_creacion)
VALUES 
('Intentos de Acceso No Autorizados', 
 'Número de intentos fallidos de inicio de sesión que exceden el límite establecido en las políticas de seguridad',
 'Seguridad',
 50.00,
 30.00,
 0.00,
 'Intentos por día',
 'Diario',
 'SYSTEM'),
 
('Contraseñas No Conformes con Política', 
 'Porcentaje de contraseñas de usuarios que no cumplen con los requisitos actuales de política de seguridad',
 'Cumplimiento',
 25.00,
 15.00,
 0.00,
 'Porcentaje',
 'Semanal',
 'SYSTEM');

-- Insertar 2 riesgos de ejemplo relacionados al sistema
INSERT INTO riesgo_evento 
(titulo, descripcion, categoria, probabilidad, impacto, nivel_riesgo, valor_riesgo, consecuencias, plan_accion, fecha_identificacion, responsable, usuario_registro)
VALUES 
('Acceso No Autorizado por Fuerza Bruta',
 'Riesgo de que atacantes externos intenten acceder al sistema mediante múltiples intentos de inicio de sesión con diferentes combinaciones de contraseñas',
 'Seguridad de Acceso',
 4, -- Alta probabilidad
 5, -- Muy alto impacto
 'Crítico',
 20,
 'Compromiso de cuentas de usuario, acceso a información confidencial de estudiantes, modificación no autorizada de datos académicos, pérdida de integridad del sistema',
 'Implementar bloqueo temporal de cuenta después de 3 intentos fallidos, activar autenticación de dos factores (2FA), monitoreo en tiempo real de intentos fallidos, implementar CAPTCHA después del segundo intento fallido',
 CURRENT_DATE,
 'Administrador de Seguridad',
 'SYSTEM'),
 
('Contraseñas Débiles y No Conformes',
 'Riesgo de que usuarios utilicen contraseñas que no cumplan con las políticas de seguridad establecidas, facilitando ataques de diccionario o ingeniería social',
 'Gestión de Contraseñas',
 3, -- Media probabilidad
 4, -- Alto impacto
 'Alto',
 12,
 'Vulnerabilidad ante ataques de diccionario, compromiso fácil de cuentas de usuario, acceso no autorizado a información sensible, incumplimiento de normativas de seguridad',
 'Forzar actualización de contraseñas cuando cambien las políticas de seguridad, implementar verificación de contraseñas comprometidas en bases de datos públicas, educación continua sobre seguridad de contraseñas, auditorías periódicas de cumplimiento',
 CURRENT_DATE,
 'Administrador de Seguridad',
 'SYSTEM');

-- Comentarios sobre la tabla
COMMENT ON TABLE riesgo_evento IS 'Almacena eventos de riesgo identificados en el sistema de información';
COMMENT ON TABLE indicador_riesgo IS 'Define los indicadores clave de riesgo (KRI) monitoreados';
COMMENT ON TABLE historial_kri IS 'Historial de mediciones de los indicadores clave de riesgo';
COMMENT ON TABLE accion_mitigacion IS 'Acciones planificadas y ejecutadas para mitigar riesgos identificados';

COMMENT ON COLUMN riesgo_evento.probabilidad IS 'Escala 1-5: 1=Muy Baja, 2=Baja, 3=Media, 4=Alta, 5=Muy Alta';
COMMENT ON COLUMN riesgo_evento.impacto IS 'Escala 1-5: 1=Muy Bajo, 2=Bajo, 3=Medio, 4=Alto, 5=Muy Alto';
COMMENT ON COLUMN riesgo_evento.valor_riesgo IS 'Valor calculado: probabilidad × impacto (rango 1-25)';
COMMENT ON COLUMN riesgo_evento.nivel_riesgo IS 'Clasificación: Bajo(1-6), Medio(7-12), Alto(13-19), Crítico(20-25)';
