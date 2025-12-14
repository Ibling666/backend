-- ===============================================
-- MÓDULO DE ANÁLISIS DE RIESGOS
-- Sistema de Gestión de Riesgos de Seguridad
-- VERSIÓN FINAL - LISTA PARA USAR
-- ===============================================

-- PASO 1: Eliminar tablas existentes si existen (en orden correcto por dependencias)
DROP TABLE IF EXISTS historial_kri CASCADE;
DROP TABLE IF EXISTS accion_mitigacion CASCADE;
DROP TABLE IF EXISTS riesgo_evento CASCADE;
DROP TABLE IF EXISTS indicador_riesgo CASCADE;

-- PASO 2: Crear tabla para eventos de riesgo
CREATE TABLE riesgo_evento (
    id_riesgo SERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    
    -- Análisis de riesgo
    probabilidad INTEGER NOT NULL CHECK (probabilidad >= 1 AND probabilidad <= 5),
    impacto INTEGER NOT NULL CHECK (impacto >= 1 AND impacto <= 5),
    nivel_riesgo VARCHAR(20) NOT NULL,
    valor_riesgo INTEGER NOT NULL,
    
    -- Consecuencias y mitigación
    consecuencias TEXT NOT NULL,
    plan_accion TEXT NOT NULL,
    
    -- Información administrativa
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_identificacion DATE NOT NULL,
    responsable VARCHAR(150) NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'Identificado',
    
    -- Auditoría
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_registro VARCHAR(100) NOT NULL
);

-- PASO 3: Crear tabla para indicadores clave de riesgo (KRI)
CREATE TABLE indicador_riesgo (
    id_indicador SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    tipo_indicador VARCHAR(100) NOT NULL,
    umbral_critico DECIMAL(10,2) NOT NULL,
    umbral_advertencia DECIMAL(10,2) NOT NULL,
    valor_actual DECIMAL(10,2) NOT NULL DEFAULT 0,
    unidad_medida VARCHAR(50) NOT NULL,
    frecuencia_medicion VARCHAR(50) NOT NULL,
    
    -- Estado del indicador
    estado_actual VARCHAR(20) NOT NULL DEFAULT 'Normal',
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    
    -- Auditoría
    usuario_creacion VARCHAR(100) NOT NULL
);

-- PASO 4: Crear tabla para historial de mediciones de KRI
CREATE TABLE historial_kri (
    id_medicion SERIAL PRIMARY KEY,
    id_indicador INTEGER NOT NULL REFERENCES indicador_riesgo(id_indicador),
    valor_medido DECIMAL(10,2) NOT NULL,
    fecha_medicion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_evaluado VARCHAR(20) NOT NULL,
    observaciones TEXT,
    usuario_medicion VARCHAR(100) NOT NULL
);

-- PASO 5: Crear tabla para acciones de mitigación
CREATE TABLE accion_mitigacion (
    id_accion SERIAL PRIMARY KEY,
    id_riesgo INTEGER NOT NULL REFERENCES riesgo_evento(id_riesgo),
    descripcion_accion TEXT NOT NULL,
    responsable_accion VARCHAR(150) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_limite DATE NOT NULL,
    fecha_completada DATE,
    estado VARCHAR(50) NOT NULL DEFAULT 'Pendiente',
    efectividad VARCHAR(20),
    observaciones TEXT,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- PASO 6: Crear índices para mejorar rendimiento
CREATE INDEX idx_riesgo_estado ON riesgo_evento(estado);
CREATE INDEX idx_riesgo_nivel ON riesgo_evento(nivel_riesgo);
CREATE INDEX idx_riesgo_fecha ON riesgo_evento(fecha_identificacion);
CREATE INDEX idx_indicador_activo ON indicador_riesgo(activo);
CREATE INDEX idx_historial_kri_fecha ON historial_kri(fecha_medicion);
CREATE INDEX idx_accion_estado ON accion_mitigacion(estado);

-- PASO 7: Insertar 2 indicadores clave de riesgo predefinidos
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

-- PASO 8: Insertar 2 riesgos de ejemplo relacionados al sistema
INSERT INTO riesgo_evento 
(titulo, descripcion, categoria, probabilidad, impacto, nivel_riesgo, valor_riesgo, consecuencias, plan_accion, fecha_identificacion, responsable, usuario_registro)
VALUES 
('Acceso No Autorizado por Fuerza Bruta',
 'Riesgo de que atacantes externos intenten acceder al sistema mediante múltiples intentos de inicio de sesión con diferentes combinaciones de contraseñas',
 'Seguridad de Acceso',
 4,
 5,
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
 3,
 4,
 'Alto',
 12,
 'Vulnerabilidad ante ataques de diccionario, compromiso fácil de cuentas de usuario, acceso no autorizado a información sensible, incumplimiento de normativas de seguridad',
 'Forzar actualización de contraseñas cuando cambien las políticas de seguridad, implementar verificación de contraseñas comprometidas en bases de datos públicas, educación continua sobre seguridad de contraseñas, auditorías periódicas de cumplimiento',
 CURRENT_DATE,
 'Administrador de Seguridad',
 'SYSTEM');

-- ===============================================
-- SCRIPT COMPLETADO EXITOSAMENTE
-- ===============================================
-- 4 tablas creadas: riesgo_evento, indicador_riesgo, historial_kri, accion_mitigacion
-- 2 KRI insertados
-- 2 Riesgos de ejemplo insertados
-- ===============================================
