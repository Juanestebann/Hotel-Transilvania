CREATE TABLE habitacion (

    idHabitacion BIGINT PRIMARY KEY AUTO_INCREMENT,

    numeroHabitacion VARCHAR(10) NOT NULL UNIQUE,

    tipoHabitacion VARCHAR(30) NOT NULL,

    precioBase DOUBLE NOT NULL,

    capacidad INT NOT NULL,

    estadoHabitacion VARCHAR(20) NOT NULL,

    idHotel BIGINT NOT NULL
);