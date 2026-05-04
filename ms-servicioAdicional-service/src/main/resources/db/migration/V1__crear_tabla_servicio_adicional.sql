CREATE TABLE servicio_adicional (
    idServicio BIGINT AUTO_INCREMENT PRIMARY KEY,
    idHotel BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL
);