CREATE TABLE resena (
                        id_resena BIGINT AUTO_INCREMENT PRIMARY KEY,
                        id_cliente BIGINT NOT NULL,
                        id_hotel BIGINT NOT NULL,
                        id_reserva BIGINT NOT NULL,
                        calificacion INT NOT NULL,
                        comentario VARCHAR(500) NOT NULL,
                        fecha_comentario DATE NOT NULL,
                        estado_resena VARCHAR(50) NOT NULL
);