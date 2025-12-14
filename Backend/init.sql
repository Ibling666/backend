-- Created by Redgate Data Modeler (https://datamodeler.redgate-platform.com)
-- Last modification date: 2025-10-18 02:41:59.666

-- tables
-- Table: Certificado
CREATE TABLE Certificado (
    id_certificado serial  NOT NULL,
    formato varchar(50)  NOT NULL,
    version int  NOT NULL,
    estado varchar(20)  NOT NULL,
    fecha_modificacion date  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    CONSTRAINT Certificado_pk PRIMARY KEY (id_certificado)
);

-- Table: Contrasenia
CREATE TABLE Contrasenia (
    id_pass serial  NOT NULL,
    contrasenia varchar(100)  NOT NULL,
    fecha_creacion date  NOT NULL,
    longitud int  NOT NULL,
    complejidad int  NOT NULL,
    intentos_restantes int  NOT NULL,
    ultimo_log date  NOT NULL,
    CONSTRAINT Contrasenia_pk PRIMARY KEY (id_pass)
);

-- Table: Encuesta
CREATE TABLE Encuesta (
    id_encuesta serial  NOT NULL,
    titulo varchar(40)  NOT NULL,
    descripcion text  NOT NULL,
    fecha_modificado date  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    Plazo_id_plazo int  NOT NULL,
    CONSTRAINT Encuesta_pk PRIMARY KEY (id_encuesta)
);

-- Table: Encuesta_Gestion
CREATE TABLE Encuesta_Gestion (
    id_encuesta_gestion serial  NOT NULL,
    Pregunta_id_pregunta int  NOT NULL,
    Encuesta_id_encuesta int  NOT NULL,
    anio int  NOT NULL,
    semestre int  NOT NULL,
    CONSTRAINT Encuesta_Gestion_pk PRIMARY KEY (id_encuesta_gestion)
);

-- Table: Estado_Certificado
CREATE TABLE Estado_Certificado (
    id_est_certificado serial  NOT NULL,
    archivo varchar(50)  NOT NULL,
    estado varchar(15)  NOT NULL,
    fecha_estado date  NOT NULL,
    Certificado_id_certificado int  NOT NULL,
    Estudiante_id_estudiante int  NOT NULL,
    CONSTRAINT Estado_Certificado_pk PRIMARY KEY (id_est_certificado)
);

-- Table: Estado_Encuesta
CREATE TABLE Estado_Encuesta (
    id_est_encuesta serial  NOT NULL,
    estado varchar(30)  NOT NULL,
    fecha_estado date  NOT NULL,
    Estudiante_id_estudiante int  NOT NULL,
    Encuesta_id_encuesta int  NOT NULL,
    CONSTRAINT Estado_Encuesta_pk PRIMARY KEY (id_est_encuesta)
);

-- Table: Estudiante
CREATE TABLE Estudiante (
    id_estudiante serial  NOT NULL,
    ci int  NOT NULL,
    nombre varchar(50)  NOT NULL,
    apellido varchar(50)  NOT NULL,
    correo_institucional varchar(50)  NOT NULL,
    correo_personal varchar(50)  NULL,
    carrera varchar(50)  NOT NULL,
    asignatura varchar(50)  NOT NULL,
    telefono int  NOT NULL,
    anio int  NOT NULL,
    semestre int  NOT NULL,
    estado_invitacion varchar(50)  NOT NULL,
    contrasena varchar(100)  NOT NULL,
    intentos_restantes int  NOT NULL DEFAULT 3,
    CONSTRAINT Estudiante_pk PRIMARY KEY (id_estudiante)
);

-- Table: H_Certificado
CREATE TABLE H_Certificado (
    id_certificado serial  NOT NULL,
    formato varchar(50)  NOT NULL,
    version int  NOT NULL,
    fecha_modificacion date  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    ver int  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Certificado_pk PRIMARY KEY (id_certificado)
);

-- Table: H_Contrasenia
CREATE TABLE H_Contrasenia (
    id_pass serial  NOT NULL,
    contrasenia varchar(100)  NOT NULL,
    fecha_creacion date  NOT NULL,
    longitud int  NOT NULL,
    complejidad int  NOT NULL,
    intentos_restantes int  NOT NULL,
    ultimo_log date  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Contrasenia_pk PRIMARY KEY (id_pass)
);

-- Table: H_Estudiante
CREATE TABLE H_Estudiante (
    id_estudiante serial  NOT NULL,
    ci int  NOT NULL,
    nombre varchar(50)  NOT NULL,
    apellido varchar(50)  NOT NULL,
    correo_insitucional varchar(50)  NOT NULL,
    correo_personal varchar(50)  NULL,
    carrera varchar(40)  NOT NULL,
    asignatura varchar(30)  NOT NULL,
    telefono int  NOT NULL,
    anio int  NOT NULL,
    semestre int  NOT NULL,
    ver int  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Estudiante_pk PRIMARY KEY (id_estudiante)
);

-- Table: H_Noticias
CREATE TABLE H_Noticias (
    id_noticia serial  NOT NULL,
    titulo varchar(50)  NOT NULL,
    descripcion text  NOT NULL,
    img varchar(50)  NOT NULL,
    fecha_modificado date  NOT NULL,
    estado varchar(30)  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    ver int  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Noticias_pk PRIMARY KEY (id_noticia)
);

-- Table: H_Reporte
CREATE TABLE H_Reporte (
    id_reporte serial  NOT NULL,
    titulo varchar(30)  NOT NULL,
    descripcion text  NOT NULL,
    formato varchar(50)  NOT NULL,
    fecha date  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    ver int  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Reporte_pk PRIMARY KEY (id_reporte)
);

-- Table: H_Respuesta
CREATE TABLE H_Respuesta (
    id_respuesta serial  NOT NULL,
    respuesta text  NOT NULL,
    Pregunta_id_pregunta int  NOT NULL,
    Estudiante_id_estudiante int  NOT NULL,
    ver int  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Respuesta_pk PRIMARY KEY (id_respuesta)
);

-- Table: H_Usuario
CREATE TABLE H_Usuario (
    id_usuario serial  NOT NULL,
    nombre varchar(50)  NOT NULL,
    telefono int  NOT NULL,
    correo varchar(40)  NOT NULL,
    rol varchar(20)  NOT NULL,
    usuario varchar(30)  NOT NULL,
    contrasenia varchar(100)  NOT NULL,
    ver int  NOT NULL,
    tx_date timestamp  NOT NULL,
    tx_user int  NOT NULL,
    CONSTRAINT H_Usuario_pk PRIMARY KEY (id_usuario)
);
-- Table: Log_Usuario
CREATE TABLE Log_Usuario (
    id_log serial  NOT NULL,
    fecha_log timestamp  NOT NULL,
    tipo_log varchar(150)  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    modulo varchar(150)  NOT NULL,
    motivo varchar(150)  NOT NULL,
    nivel varchar(150)  NOT NULL,
    mensaje varchar(150)  NOT NULL,
    detalle varchar(150)  NOT NULL,
    CONSTRAINT Log_Usuario_pk PRIMARY KEY (id_log)
);
-- Table: Noticias
CREATE TABLE Noticias (
    id_noticia serial  NOT NULL,
    titulo varchar(50)  NOT NULL,
    descripcion text  NOT NULL,
    img varchar(50)  NOT NULL,
    fecha_modificado date  NOT NULL,
    estado varchar(30)  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    CONSTRAINT Noticias_pk PRIMARY KEY (id_noticia)
);

-- Table: Notificacion
CREATE TABLE Notificacion (
    id_notificacion serial  NOT NULL,
    titulo varchar(80)  NOT NULL,
    contenido text  NOT NULL,
    fecha timestamp  NOT NULL,
    estado_notificacion boolean  NOT NULL,
    Estudiante_id_estudiante int  NOT NULL,
    Tipo_Notificacion_id_notificacion int  NOT NULL,
    CONSTRAINT Notificacion_pk PRIMARY KEY (id_notificacion)
);

-- Table: Opciones_Pregunta
CREATE TABLE Opciones_Pregunta (
    id_opciones serial  NOT NULL,
    opcion varchar(30)  NOT NULL,
    Pregunta_id_pregunta int  NOT NULL,
    CONSTRAINT Opciones_Pregunta_pk PRIMARY KEY (id_opciones)
);

-- Table: Parametros_Aviso
CREATE TABLE Parametros_Aviso (
    id_parametro serial  NOT NULL,
    porcentaje int  NOT NULL,
    fecha_cambio date  NOT NULL,
    fecha_notificacion date  NOT NULL,
    mensaje_predeterminado text  NOT NULL,
    CONSTRAINT Parametros_Aviso_pk PRIMARY KEY (id_parametro)
);

-- Table: Plazo
CREATE TABLE Plazo (
    id_plazo serial  NOT NULL,
    Fecha_finalizacion date  NOT NULL,
    fecha_modificacion date  NOT NULL,
    estado varchar(50)  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    CONSTRAINT id_plazo PRIMARY KEY (id_plazo)
);

-- Table: Pregunta
CREATE TABLE Pregunta (
    id_pregunta serial  NOT NULL,
    num_pregunta int  NOT NULL,
    pregunta text  NOT NULL,
    tipo_pregunta varchar(15)  NOT NULL,
    estado varchar(15)  NOT NULL,
    CONSTRAINT Pregunta_pk PRIMARY KEY (id_pregunta)
);

-- Table: Reporte
CREATE TABLE Reporte (
    id_reporte serial  NOT NULL,
    titulo varchar(30)  NOT NULL,
    descripcion text  NOT NULL,
    formato varchar(50)  NOT NULL,
    fecha date  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    CONSTRAINT Reporte_pk PRIMARY KEY (id_reporte)
);

-- Table: Respuesta
CREATE TABLE Respuesta (
    id_respuesta serial  NOT NULL,
    respuesta text  NOT NULL,
    Pregunta_id_pregunta int  NOT NULL,
    Estudiante_id_estudiante int  NOT NULL,
    CONSTRAINT Respuesta_pk PRIMARY KEY (id_respuesta)
);

-- Table: Roles
CREATE TABLE Roles (
    id_rol serial  NOT NULL,
    nombre_rol varchar(50)  NOT NULL,
    activo boolean  NOT NULL,
    accesos varchar(255)  NOT NULL,
    CONSTRAINT Roles_pk PRIMARY KEY (id_rol)
);

-- Table: Soporte
CREATE TABLE Soporte (
    id_soporte serial  NOT NULL,
    mensaje text  NOT NULL,
    fecha timestamp  NOT NULL,
    Tipo_Problema_id_problema int  NOT NULL,
    Usuario_id_usuario int  NOT NULL,
    CONSTRAINT Soporte_pk PRIMARY KEY (id_soporte)
);

-- Table: Tipo_Notificacion
CREATE TABLE Tipo_Notificacion (
    id_notificacion serial  NOT NULL,
    tipo varchar(80)  NOT NULL,
    CONSTRAINT Tipo_Notificacion_pk PRIMARY KEY (id_notificacion)
);

-- Table: Tipo_Problema
CREATE TABLE Tipo_Problema (
    id_problema serial  NOT NULL,
    problema varchar(80)  NOT NULL,
    CONSTRAINT Tipo_Problema_pk PRIMARY KEY (id_problema)
);

-- Table: Usuario
CREATE TABLE Usuario (
    id_usuario serial  NOT NULL,
    nombre varchar(50)  NOT NULL,
    apellido varchar(50)  NOT NULL,
    telefono int  NOT NULL,
    correo varchar(40)  NOT NULL,
    carrera varchar(40)  NOT NULL,
    rol varchar(20)  NOT NULL,
    ci varchar(20)  NOT NULL,
    intentos_restantes int  NOT NULL DEFAULT 3,
    Roles_id_rol int  NOT NULL,
    Contrasenia_id_pass int  NOT NULL,
    CONSTRAINT Usuario_pk PRIMARY KEY (id_usuario)
);

--Esto acelera las búsquedas por usuario
CREATE INDEX idx_log_usuario_user
ON Log_Usuario (Usuario_id_usuario);


-- Table: ConfiguracionSeguridad - Configurable security policies for Security role users
CREATE TABLE configuracion_seguridad (
    id_config serial  NOT NULL,
    min_longitud_contrasenia int  NOT NULL DEFAULT 12,
    max_intentos_login int  NOT NULL DEFAULT 3,
    dias_expiracion_contrasenia int  NOT NULL DEFAULT 60,
    meses_no_reutilizar int  NOT NULL DEFAULT 12,
    requerir_mayusculas boolean  NOT NULL DEFAULT true,
    requerir_minusculas boolean  NOT NULL DEFAULT true,
    requerir_numeros boolean  NOT NULL DEFAULT true,
    requerir_simbolos boolean  NOT NULL DEFAULT true,
    fecha_modificacion timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_modificacion int  NOT NULL DEFAULT 1,
    activa boolean  NOT NULL DEFAULT true,
    CONSTRAINT configuracion_seguridad_pk PRIMARY KEY (id_config)
);
-- Table: riesgo_evento
CREATE TABLE riesgo_evento (
    id_riesgo serial  NOT NULL,
    titulo varchar(200)  NOT NULL,
    descripcion text  NOT NULL,
    categoria varchar(100)  NOT NULL,
    probabilidad int  NOT NULL,
    impacto int  NOT NULL,
    nivel_riesgo varchar(20)  NOT NULL,
    valor_riesgo int  NOT NULL,
    consecuencias text  NOT NULL,
    plan_accion text  NOT NULL,
    fecha_registro timestamp  NOT NULL,
    fecha_identificacion date  NOT NULL,
    responsable varchar(150)  NOT NULL,
    estado varchar(50)  NOT NULL,
    fecha_actualizacion timestamp,
    usuario_registro varchar(100)  NOT NULL,
    CONSTRAINT riesgo_evento_pk PRIMARY KEY (id_riesgo)
);

-- Table: indicador_riesgo
CREATE TABLE indicador_riesgo (
    id_indicador serial  NOT NULL,
    nombre varchar(200)  NOT NULL,
    descripcion text  NOT NULL,
    tipo_indicador varchar(100)  NOT NULL,
    umbral_critico decimal(10,2)  NOT NULL,
    umbral_advertencia decimal(10,2)  NOT NULL,
    valor_actual decimal(10,2)  NOT NULL,
    unidad_medida varchar(50)  NOT NULL,
    frecuencia_medicion varchar(50)  NOT NULL,
    estado_actual varchar(20)  NOT NULL,
    ultima_actualizacion timestamp,
    fecha_creacion timestamp  NOT NULL,
    activo boolean  NOT NULL,
    usuario_creacion varchar(100)  NOT NULL,
    CONSTRAINT indicador_riesgo_pk PRIMARY KEY (id_indicador)
);

-- Table: historial_kri
CREATE TABLE historial_kri (
    id_medicion serial  NOT NULL,
    id_indicador int  NOT NULL,
    valor_medido decimal(10,2)  NOT NULL,
    fecha_medicion timestamp  NOT NULL,
    estado_evaluado varchar(20)  NOT NULL,
    observaciones text,
    usuario_medicion varchar(100)  NOT NULL,
    CONSTRAINT historial_kri_pk PRIMARY KEY (id_medicion)
);

-- Table: accion_mitigacion
CREATE TABLE accion_mitigacion (
    id_accion serial  NOT NULL,
    id_riesgo int  NOT NULL,
    descripcion_accion text  NOT NULL,
    responsable_accion varchar(150)  NOT NULL,
    fecha_inicio date  NOT NULL,
    fecha_limite date  NOT NULL,
    fecha_completada date,
    estado varchar(50)  NOT NULL,
    efectividad varchar(20),
    observaciones text,
    fecha_registro timestamp  NOT NULL,
    CONSTRAINT accion_mitigacion_pk PRIMARY KEY (id_accion)
);


-- foreign keys
-- Reference: Certificado_Usuario (table: Certificado)
ALTER TABLE Certificado ADD CONSTRAINT Certificado_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Encuesta_Gestion_Encuesta (table: Encuesta_Gestion)
ALTER TABLE Encuesta_Gestion ADD CONSTRAINT Encuesta_Gestion_Encuesta
    FOREIGN KEY (Encuesta_id_encuesta)
    REFERENCES Encuesta (id_encuesta)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Encuesta_Gestion_Pregunta (table: Encuesta_Gestion)
ALTER TABLE Encuesta_Gestion ADD CONSTRAINT Encuesta_Gestion_Pregunta
    FOREIGN KEY (Pregunta_id_pregunta)
    REFERENCES Pregunta (id_pregunta)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Encuesta_Plazo (table: Encuesta)
ALTER TABLE Encuesta ADD CONSTRAINT Encuesta_Plazo
    FOREIGN KEY (Plazo_id_plazo)
    REFERENCES Plazo (id_plazo)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Encuesta_Usuario (table: Encuesta)
ALTER TABLE Encuesta ADD CONSTRAINT Encuesta_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Estado_Certificado_Certificado (table: Estado_Certificado)
ALTER TABLE Estado_Certificado ADD CONSTRAINT Estado_Certificado_Certificado
    FOREIGN KEY (Certificado_id_certificado)
    REFERENCES Certificado (id_certificado)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;
-- Reference: Log_Usuario_Usuario (table: Log_Usuario)
ALTER TABLE Log_Usuario ADD CONSTRAINT Log_Usuario_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;
-- Reference: Estado_Certificado_Estudiante (table: Estado_Certificado)
ALTER TABLE Estado_Certificado ADD CONSTRAINT Estado_Certificado_Estudiante
    FOREIGN KEY (Estudiante_id_estudiante)
    REFERENCES Estudiante (id_estudiante)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Estado_Encuesta_Encuesta (table: Estado_Encuesta)
ALTER TABLE Estado_Encuesta ADD CONSTRAINT Estado_Encuesta_Encuesta
    FOREIGN KEY (Encuesta_id_encuesta)
    REFERENCES Encuesta (id_encuesta)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Estado_Encuesta_Estudiante (table: Estado_Encuesta)
ALTER TABLE Estado_Encuesta ADD CONSTRAINT Estado_Encuesta_Estudiante
    FOREIGN KEY (Estudiante_id_estudiante)
    REFERENCES Estudiante (id_estudiante)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Noticias_Usuario (table: Noticias)
ALTER TABLE Noticias ADD CONSTRAINT Noticias_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Notificacion_Tipo_Notificacion (table: Notificacion)
ALTER TABLE Notificacion ADD CONSTRAINT Notificacion_Tipo_Notificacion
    FOREIGN KEY (Tipo_Notificacion_id_notificacion)
    REFERENCES Tipo_Notificacion (id_notificacion)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Notificaciones_Estudiante (table: Notificacion)
ALTER TABLE Notificacion ADD CONSTRAINT Notificaciones_Estudiante
    FOREIGN KEY (Estudiante_id_estudiante)
    REFERENCES Estudiante (id_estudiante)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Opciones_Pregunta_Pregunta (table: Opciones_Pregunta)
ALTER TABLE Opciones_Pregunta ADD CONSTRAINT Opciones_Pregunta_Pregunta
    FOREIGN KEY (Pregunta_id_pregunta)
    REFERENCES Pregunta (id_pregunta)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: PLazo_Usuario (table: Plazo)
ALTER TABLE Plazo ADD CONSTRAINT PLazo_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Reporte_Usuario (table: Reporte)
ALTER TABLE Reporte ADD CONSTRAINT Reporte_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Respuesta_Estudiante (table: Respuesta)
ALTER TABLE Respuesta ADD CONSTRAINT Respuesta_Estudiante
    FOREIGN KEY (Estudiante_id_estudiante)
    REFERENCES Estudiante (id_estudiante)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Respuesta_Pregunta (table: Respuesta)
ALTER TABLE Respuesta ADD CONSTRAINT Respuesta_Pregunta
    FOREIGN KEY (Pregunta_id_pregunta)
    REFERENCES Pregunta (id_pregunta)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Soporte_Tipo_Problema (table: Soporte)
ALTER TABLE Soporte ADD CONSTRAINT Soporte_Tipo_Problema
    FOREIGN KEY (Tipo_Problema_id_problema)
    REFERENCES Tipo_Problema (id_problema)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Soporte_Usuario (table: Soporte)
ALTER TABLE Soporte ADD CONSTRAINT Soporte_Usuario
    FOREIGN KEY (Usuario_id_usuario)
    REFERENCES Usuario (id_usuario)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Usuario_Contrasenia (table: Usuario)
ALTER TABLE Usuario ADD CONSTRAINT Usuario_Contrasenia
    FOREIGN KEY (Contrasenia_id_pass)
    REFERENCES Contrasenia (id_pass)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- Reference: Usuario_Roles (table: Usuario)
ALTER TABLE Usuario ADD CONSTRAINT Usuario_Roles
    FOREIGN KEY (Roles_id_rol)
    REFERENCES Roles (id_rol)
    NOT DEFERRABLE
    INITIALLY IMMEDIATE
;

-- 1) Quitar la PK actual basada solo en id_pass
ALTER TABLE H_Contrasenia DROP CONSTRAINT IF EXISTS H_Contrasenia_pk;

-- 2) Crear PK compuesta
ALTER TABLE H_Contrasenia
  ADD CONSTRAINT H_Contrasenia_pk PRIMARY KEY (id_pass, tx_date);

-- 3) (opcional) Index para consultas por id_pass/TX_DATE
CREATE INDEX IF NOT EXISTS idx_hc_txdate ON H_Contrasenia (id_pass, tx_date DESC);

-- Insert default security configuration
INSERT INTO configuracion_seguridad (
    min_longitud_contrasenia, 
    max_intentos_login, 
    dias_expiracion_contrasenia, 
    meses_no_reutilizar,
    requerir_mayusculas, 
    requerir_minusculas, 
    requerir_numeros, 
    requerir_simbolos,
    fecha_modificacion,
    usuario_modificacion,
    activa
) VALUES (
    12,    -- Minimum password length
    3,     -- Maximum login attempts  
    60,    -- Password expiry in days
    12,    -- Months before password can be reused
    true,  -- Require uppercase letters
    true,  -- Require lowercase letters 
    true,  -- Require numbers
    true,  -- Require special characters
    CURRENT_TIMESTAMP,
    1,     -- Default user ID (system)
    true   -- Configuration is active
);

ALTER TABLE Contrasenia DROP COLUMN IF EXISTS contrasenia_id_pass;
-- Reference: historial_kri_indicador_riesgo (table: historial_kri)
ALTER TABLE historial_kri ADD CONSTRAINT historial_kri_indicador_riesgo
    FOREIGN KEY (id_indicador)
    REFERENCES indicador_riesgo (id_indicador)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: accion_mitigacion_riesgo_evento (table: accion_mitigacion)
ALTER TABLE accion_mitigacion ADD CONSTRAINT accion_mitigacion_riesgo_evento
    FOREIGN KEY (id_riesgo)
    REFERENCES riesgo_evento (id_riesgo)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;
