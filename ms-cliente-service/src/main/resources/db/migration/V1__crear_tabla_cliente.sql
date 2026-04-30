CREATE TABLE cliente (
    idCliente BIGINT AUTO_INCREMENT PRIMARY KEY,
    rutDocumento VARCHAR(50) NOT NULL UNIQUE,
    telefono VARCHAR(50) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    rolCliente VARCHAR(50) NOT NULL,
    tipoCliente VARCHAR(50) NOT NULL
);