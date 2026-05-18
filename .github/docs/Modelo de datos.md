# Modelo de Datos - BBDD Reportes

## Tabla: tipo_reporte
- **id_tipo_reporte** (PK)
- descripcion_tipo_reporte

## Tabla: estatus
- **id_estatus** (PK)
- descripcion_estatus

## Tabla: contacto
- **id_contacto** (PK)
- nombres (varchar)
- correo (varchar)
- telefono (number)
- id_canal_preferencia (FK)

## Tabla: canal_preferencia
- **id_canal_preferencia** (PK)
- descripcion

## Tabla: reporte_mascota
- **id_reporte_mascota** (PK)
- id_tipo_reporte (FK)
- id_estatus (FK)
- fecha_extravio (DATE)
- id_contacto (FK)
- fecha_avistamiento (DATE)
- fecha_reporte (timestamp)
- id_marca_distintiva (FK)
- id_mascota (FK)

## Tabla: mascota
- **id_mascota** (PK)
- nombre_mascota
- id_raza (FK)
- id_especie (FK)
- color_primario (varchar)
- color_secundario (varchar)
- tamano (varchar)
- id_sexo (FK)
- edad (number)
- detalles_extra (varchar)
- id_chip (FK, opcional)

## Tabla: raza
- **id_raza** (PK)
- descripcion

## Tabla: especie
- **id_especie** (PK)
- descripcion

## Tabla: sexo
- **id_sexo** (PK)
- descripcion

## Tabla: marca_distintiva
- **id_marca_distintiva** (PK)
- descripcion

## Relaciones

- reporte_mascota.id_tipo_reporte → tipo_reporte.id_tipo_reporte
- reporte_mascota.id_estatus → estatus.id_estatus
- reporte_mascota.id_contacto → contacto.id_contacto
- reporte_mascota.id_mascota → mascota.id_mascota
- reporte_mascota.id_marca_distintiva → marca_distintiva.id_marca_distintiva

- mascota.id_raza → raza.id_raza
- mascota.id_especie → especie.id_especie
- mascota.id_sexo → sexo.id_sexo

- contacto.id_canal_preferencia → canal_preferencia.id_canal_preferencia

# Modelo de Datos - BBDD Geolocalización

## Tabla: mapadecalor
- **id_mapadecalor** (PK)
- geohash
- cantidad_reportes (int)
- last_calculated_at

## Tabla: coordenadas
- **id_ubicacion_coordenadas** (PK)
- ubicacion_lat
- ubicacion_lon
- id_reporte (FK)
- id_comuna (FK)
- direccion
- created_at

## Tabla: comuna
- **id_comuna** (PK)
- nombre_comuna
- id_region (FK)

## Tabla: region
- **id_region** (PK)
- nombre_region

## Relaciones

- coordenadas.id_reporte → reporte_mascota.id_reporte_mascota
- coordenadas.id_comuna → comuna.id_comuna
- comuna.id_region → region.id_region

# Modelo de Datos - BBDD Autenticación

## Tabla: refresh_token
- **id_refresh_token** (PK)
- id_usuario (FK)
- token_hash
- expires_at (datetime)
- revoked (boolean)
- created_at (datetime)

## Tabla: usuario
- **id_usuario** (PK)
- email (UK)
- nombre_completo
- id_rol (FK)
- id_status (FK)
- contraseña
- email_verificado (boolean)
- created_at
- updated_at
- last_login_at

## Tabla: usuario_rol
- id_usuario (FK)
- id_role (FK)

## Tabla: rol
- **id_rol** (PK)
- descripcion

## Tabla: status
- **id_status** (PK)
- descripcion

## Relaciones

- refresh_token.id_usuario → usuario.id_usuario
- usuario.id_rol → rol.id_rol
- usuario.id_status → status.id_status
- usuario_rol.id_usuario → usuario.id_usuario
- usuario_rol.id_role → rol.id_rol









# Modelo de Datos - BBDD Motor de Coincidencias

## Tabla: coincidencia_request
- **id_coincidencia_request** (PK)
- id_perdido_reporte (FK)
- id_encontrado_reporte (FK)
- id_coincidencia_status (FK)
- requested_at (datetime)
- processed_at (datetime)

## Tabla: coincidencia_status
- **id_coincidencia_status** (PK)
- descripcion

## Tabla: coincidencias_results
- **id_coincidencia_resultado** (PK)
- id_coincidencia_request (FK)
- puntaje_total
- puntaje_raza
- puntaje_color
- puntaje_tamaño
- puntaje_distancia
- puntaje_fecha
- veredicto_final
- created_at

## Tabla: reglas_coincidencias
- **id_reglas_coincidencias** (PK)
- descripcion
- importancia (decimal)
- is_active

## Tabla: circuit_breaker_estado
- **id_circuit_breaker_estado** (PK)
- estado_circuitbreaker (FK)
- cantidad_fallas
- cantidad_exitos
- limite_fallas
- opened_at
- next_retry_at
- last_error
- updated_at

## Tabla: estado_circuitbreaker
- **id_estado_circuitbreaker** (PK)
- descripcion

## Relaciones

- coincidencia_request.id_coincidencia_status → coincidencia_status.id_coincidencia_status
- coincidencia_request.id_perdido_reporte → reporte_mascota.id_reporte_mascota
- coincidencia_request.id_encontrado_reporte → reporte_mascota.id_reporte_mascota

- coincidencias_results.id_coincidencia_request → coincidencia_request.id_coincidencia_request

- circuit_breaker_estado.estado_circuitbreaker → estado_circuitbreaker.id_estado_circuitbreaker