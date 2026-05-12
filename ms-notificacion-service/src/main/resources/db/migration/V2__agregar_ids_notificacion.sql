ALTER TABLE notificacion
    ADD COLUMN id_cliente BIGINT NULL;

ALTER TABLE notificacion
    ADD COLUMN id_usuario BIGINT NULL;

ALTER TABLE notificacion
    ADD COLUMN id_reserva BIGINT NULL;