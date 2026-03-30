-- ============================================================
--  Script de datos de prueba - Sistema Control Activos CEMC
--  Base de datos: matiascousiño (MySQL)
--
--  INSTRUCCIONES:
--  1. Abre MySQL Workbench o la terminal de MySQL
--  2. Conéctate a tu servidor local
--  3. Primero crea la base de datos (si no existe):
--       CREATE DATABASE `matiascousiño` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--  4. Luego ejecuta este archivo
-- ============================================================

-- Usar la base de datos (las comillas invertidas permiten la ñ)
USE `matiascousiño`;

-- La tabla se crea sola al iniciar el proyecto (ddl-auto=update)
-- Este script solo inserta datos de ejemplo

INSERT INTO solicitudes_prestamo (nombre_solicitante, taller, materiales, fecha_devolucion, fecha_solicitud, estado)
VALUES
    ('Maria Torres',   'Electronica',        'Osciloscopio',             '2025-10-21', '2025-10-18 09:00:00', 'DEVUELTO'),
    ('Carlos Diaz',    'Telecomunicaciones', 'Cautin, estano',           '2025-10-25', '2025-10-20 10:30:00', 'PENDIENTE'),
    ('Ana Munoz',      'Electronica',        'Multimetro Digital',       '2025-10-24', '2025-10-22 08:45:00', 'DEVUELTO'),
    ('Luis Herrera',   'Telecomunicaciones', 'Protoboard, resistencias', '2025-11-01', '2025-10-28 11:00:00', 'PENDIENTE');
