CREATE TABLE hotel (
    idHotel BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    direccion VARCHAR(100) NOT NULL,
    ciudad VARCHAR(50) NOT NULL,
    pais VARCHAR(50) NOT NULL,
    categoria VARCHAR(30) NOT NULL,
    descripcion VARCHAR(255) NOT NULL
);