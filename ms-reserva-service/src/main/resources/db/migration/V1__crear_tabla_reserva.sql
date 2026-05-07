CREATE TABLE reserva (
    idReserva BIGINT AUTO_INCREMENT PRIMARY KEY,
    idCliente BIGINT NOT NULL,
    idUsuario BIGINT NOT NULL,
    idHotel BIGINT NOT NULL,
    idHabitacion BIGINT NOT NULL,
    fechaInicio DATE NOT NULL,
    fechaFin DATE NOT NULL,
    cantidadPersonas INT NOT NULL,
    estadoReserva VARCHAR(50) NOT NULL,
    fechaCreacion DATETIME NOT NULL
);