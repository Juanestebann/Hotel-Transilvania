CREATE TABLE disponibilidad (
    idDisponibilidad BIGINT AUTO_INCREMENT PRIMARY KEY,
    idHabitacion BIGINT NOT NULL,
    fecha DATE NOT NULL,
    estado VARCHAR(50) NOT NULL
);