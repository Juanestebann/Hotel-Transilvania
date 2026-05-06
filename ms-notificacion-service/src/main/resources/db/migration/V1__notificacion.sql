CREATE TABLE notificacion (
  idNotificacion BIGINT AUTO_INCREMENT PRIMARY KEY,
  tipo VARCHAR(50) NOT NULL,
  mensaje TEXT NOT NULL,
  estado VARCHAR(20) NOT NULL,
  fecha_envio DATETIME NOT NULL
);