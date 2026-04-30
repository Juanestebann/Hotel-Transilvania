CREATE TABLE HOTEL (
    idHotel BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR (20) NOT NULL,
    direccion VARCHAR (30) NOT NULL,
    ciudad VARCHAR (20),
    pais VARCHAR (15),
    categoria VARCHAR (20) NOT NULL,
    descripcion VARCHAR (50)
)