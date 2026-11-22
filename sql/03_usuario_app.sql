-- =====================================================================
-- Usuario de la aplicación con privilegios mínimos (como en el informe
-- de la guía 18). El backend se conecta con este usuario, nunca con postgres.
-- ANTES de ejecutar: cambia CAMBIAR_AL_EJECUTAR por una contraseña real
-- y NO hagas commit de ese cambio.
-- =====================================================================
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'app_ferreteria') THEN
    CREATE USER app_ferreteria WITH PASSWORD 'CAMBIAR_AL_EJECUTAR';
  END IF;
END $$;

GRANT CONNECT ON DATABASE ferreteria TO app_ferreteria;
GRANT USAGE ON SCHEMA public TO app_ferreteria;
-- Solo datos: leer, crear, modificar y borrar filas. No puede crear ni borrar tablas.
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_ferreteria;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_ferreteria;
