from __future__ import annotations

from pathlib import Path
from html import escape

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    NextPageTemplate,
    PageBreak,
    PageTemplate,
    Paragraph,
    Preformatted,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "output" / "pdf"
PDF_PATH = OUT_DIR / "Guia_Testing_Swagger_Microservicios_Hotel_Transilvania.pdf"


def p(text: str, style: str = "Body"):
    return Paragraph(text, STYLES[style])


def code(text: str):
    return Paragraph(escape(text).replace("\n", "<br/>"), STYLES["CodeBlock"])


def bullet(items):
    story = []
    for item in items:
        story.append(p(f"- {item}", "GuideBullet"))
    return story


def table(data, widths=None, font_size=7.2, header=True):
    wrapped = []
    for row in data:
        wrapped.append([cell if hasattr(cell, "wrap") else p(str(cell), "TableCell") for cell in row])
    t = Table(wrapped, colWidths=widths, repeatRows=1 if header else 0, hAlign="LEFT")
    style = [
        ("GRID", (0, 0), (-1, -1), 0.35, colors.HexColor("#B7C0CC")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 4),
        ("RIGHTPADDING", (0, 0), (-1, -1), 4),
        ("TOPPADDING", (0, 0), (-1, -1), 3),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
    ]
    if header:
        style += [
            ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#203040")),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
            ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
        ]
    for r in range(1 if header else 0, len(data)):
        if r % 2 == 0:
            style.append(("BACKGROUND", (0, r), (-1, r), colors.HexColor("#F4F7FA")))
    t.setStyle(TableStyle(style))
    return t


def heading(text, level=1):
    return p(text, f"H{level}")


SERVICES = [
    {
        "name": "ms-auth-usuarios-service",
        "port": "8081",
        "base": "/api/v1/auth, /api/v1/usuarios",
        "swagger": "http://localhost:8081/swagger-ui.html",
        "controller": "AuthController, UsuarioController",
        "service": "UsuarioService, CustomUserDetailsService",
        "repository": "UsuarioRepository",
        "model": "Usuario",
        "dtos": "RegisterRequest",
        "token": "Parcial. /auth/login y /auth/register son publicos; usuarios requiere JWT.",
        "deps": "No consume otros microservicios.",
        "body": '{"nombre":"admin","password":"admin123","rol":"ADMIN"}',
        "validations": [
            "Usuario: @NotBlank en nombre, password y rol.",
            "RegisterRequest: @NotBlank en nombre y password.",
            "Service: register fuerza rol USER y encripta password con BCrypt; save/update normalizan rol a mayusculas.",
            "No implementado actualmente: email, longitud minima de password, validacion formal de roles fuera de save/update si se usa register.",
        ],
        "rules": [
            "Login autentica por nombre/password y devuelve JWT con nombre y rol.",
            "Swagger/OpenAPI es publico.",
            "Authorize en Swagger debe usarse como Bearer token: pegar el JWT devuelto por /api/v1/auth/login.",
        ],
        "endpoints": [
            ["POST", "/api/v1/auth/register", "register", "No", "Publico", "-", "-", '{"nombre":"juan","password":"123456"}', "201 con message, idUsuario, nombre, rol USER", "400 validacion; 500 error interno"],
            ["POST", "/api/v1/auth/login", "login", "No", "Publico", "-", "-", '{"nombre":"juan","password":"123456"}', "200 con token, nombre y rol", "401 credenciales invalidas; 500"],
            ["GET", "/api/v1/auth/validate", "validateToken", "Si", "USER/ADMIN autenticado", "-", "-", "-", "200 {valid:true}", "401 sin token"],
            ["GET", "/api/v1/usuarios", "findAll", "Si", "ADMIN", "-", "-", "-", "200 lista usuarios", "401, 403"],
            ["GET", "/api/v1/usuarios/{id}", "findById", "Si", "ADMIN en controller; SecurityConfig tambien permite USER/ADMIN para GET exacto", "id", "-", "-", "200 usuario", "401, 403, 404"],
            ["GET", "/api/v1/usuarios/rol/{rol}", "findByRol", "Si", "ADMIN", "rol", "-", "-", "200 lista", "401, 403"],
            ["POST", "/api/v1/usuarios", "save", "Si", "ADMIN", "-", "-", '{"nombre":"operador","password":"123456","rol":"USER"}', "201 usuario", "400, 401, 403"],
            ["PUT", "/api/v1/usuarios/{id}", "update", "Si", "ADMIN", "id", "-", '{"nombre":"operador2","password":"123456","rol":"ADMIN"}', "200 usuario", "400, 401, 403, 404"],
            ["DELETE", "/api/v1/usuarios/{id}", "delete", "Si", "ADMIN", "id", "-", "-", "204", "401, 403, 404"],
        ],
    },
    {
        "name": "ms-cliente-service",
        "port": "8082",
        "base": "/api/v1/clientes",
        "swagger": "http://localhost:8082/swagger-ui.html",
        "controller": "ClienteController",
        "service": "ClienteService",
        "repository": "ClienteRepository",
        "model": "Cliente",
        "dtos": "No tiene DTOs propios",
        "token": "Si. Todos los endpoints de negocio requieren JWT y @PreAuthorize ADMIN.",
        "deps": "No consume otros microservicios.",
        "body": '{"rutDocumento":"11111111-1","telefono":"+56911111111","direccion":"Av. Siempre Viva 123","rolCliente":"TITULAR","tipoCliente":"NACIONAL"}',
        "validations": [
            "Cliente: @NotBlank en rutDocumento, telefono, direccion, rolCliente y tipoCliente.",
            "rutDocumento es unique a nivel de columna.",
            "Service: no hay validacion manual de formato RUT, telefono, rolCliente o tipoCliente.",
        ],
        "rules": [
            "Buscar cliente inexistente por id o rut devuelve 404 mediante NoSuchElementException.",
            "Filtros por rolCliente y tipoCliente devuelven lista, aunque este vacia.",
        ],
        "endpoints": [
            ["GET", "/api/v1/clientes", "findAll", "Si", "ADMIN", "-", "rolCliente?, tipoCliente?", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/clientes/{id}", "findById", "Si", "ADMIN", "id", "-", "-", "200 cliente", "401, 403, 404"],
            ["GET", "/api/v1/clientes/rut/{rutDocumento}", "findByRutDocumento", "Si", "ADMIN", "rutDocumento", "-", "-", "200 cliente", "401, 403, 404"],
            ["POST", "/api/v1/clientes", "guardarCliente", "Si", "ADMIN", "-", "-", '{"rutDocumento":"22222222-2","telefono":"+56922222222","direccion":"Calle 2","rolCliente":"TITULAR","tipoCliente":"NACIONAL"}', "201 cliente", "400, 401, 403"],
            ["PUT", "/api/v1/clientes/{id}", "actualizarCliente", "Si", "ADMIN", "id", "-", '{"rutDocumento":"22222222-2","telefono":"+56933333333","direccion":"Nueva direccion","rolCliente":"TITULAR","tipoCliente":"EXTRANJERO"}', "200 cliente", "400, 401, 403, 404"],
            ["DELETE", "/api/v1/clientes/{id}", "eliminarCliente", "Si", "ADMIN", "id", "-", "-", "204", "401, 403, 404"],
        ],
    },
    {
        "name": "ms-hotel-service",
        "port": "8083",
        "base": "/api/v1/hoteles",
        "swagger": "http://localhost:8083/swagger-ui.html",
        "controller": "HotelController",
        "service": "HotelService",
        "repository": "HotelRepository",
        "model": "Hotel",
        "dtos": "No tiene DTOs propios",
        "token": "GET publico; POST, PUT y DELETE requieren JWT ADMIN.",
        "deps": "No consume otros microservicios.",
        "body": '{"nombre":"Hotel Transilvania","direccion":"Camino del Bosque 1","ciudad":"Santiago","pais":"Chile","categoria":"5 estrellas","descripcion":"Hotel tematico"}',
        "validations": [
            "Hotel: @NotBlank en nombre, direccion, ciudad, pais, categoria y descripcion.",
            "Columnas con longitudes: nombre 50, direccion 100, ciudad 50, pais 50, categoria 30, descripcion 255.",
            "Service: no valida rango de estrellas ni duplicidad de hotel.",
        ],
        "rules": [
            "Consultas GET son publicas por SecurityConfig.",
            "Filtros por categoria, ciudad y pais usan RequestParam especifico en el mismo path base.",
        ],
        "endpoints": [
            ["GET", "/api/v1/hoteles", "findAll", "No", "Publico", "-", "-", "-", "200 lista", "404 si service busca id inexistente no aplica aqui"],
            ["GET", "/api/v1/hoteles/{id}", "findById", "No", "Publico", "id", "-", "-", "200 hotel", "404"],
            ["GET", "/api/v1/hoteles?categoria=5 estrellas", "findByCategoria", "No", "Publico", "-", "categoria", "-", "200 lista", "500 si param ambiguo con otros"],
            ["GET", "/api/v1/hoteles?ciudad=Santiago", "findByCiudad", "No", "Publico", "-", "ciudad", "-", "200 lista", "500 si param ambiguo con otros"],
            ["GET", "/api/v1/hoteles?pais=Chile", "findByPais", "No", "Publico", "-", "pais", "-", "200 lista", "500 si param ambiguo con otros"],
            ["POST", "/api/v1/hoteles", "guardarHotel", "Si", "ADMIN", "-", "-", '{"nombre":"Hotel Centro","direccion":"Av. Centro 100","ciudad":"Santiago","pais":"Chile","categoria":"4 estrellas","descripcion":"Hotel urbano"}', "201 hotel", "400, 401, 403"],
            ["PUT", "/api/v1/hoteles/{id}", "actualizarHotel", "Si", "ADMIN", "id", "-", '{"nombre":"Hotel Centro Plus","direccion":"Av. Centro 100","ciudad":"Santiago","pais":"Chile","categoria":"5 estrellas","descripcion":"Actualizado"}', "200 hotel", "400, 401, 403, 404"],
            ["DELETE", "/api/v1/hoteles/{id}", "eliminarHotel", "Si", "ADMIN", "id", "-", "-", "204", "401, 403, 404"],
        ],
    },
    {
        "name": "ms-habitacion-service",
        "port": "8084",
        "base": "/api/v1/habitaciones",
        "swagger": "http://localhost:8084/swagger-ui.html",
        "controller": "HabitacionController",
        "service": "HabitacionService",
        "repository": "HabitacionRepository",
        "model": "Habitacion",
        "dtos": "HotelDTO",
        "token": "GET protegido por SecurityConfig general aunque el controller no tenga @PreAuthorize; POST, PUT y DELETE requieren ADMIN.",
        "deps": "WebClient a ms-hotel-service para validar idHotel.",
        "body": '{"numeroHabitacion":"101","tipoHabitacion":"DOBLE","precioBase":75000,"capacidad":2,"estadoHabitacion":"DISPONIBLE","idHotel":1}',
        "validations": [
            "Habitacion: @NotBlank en numeroHabitacion, tipoHabitacion, estadoHabitacion; @NotNull en precioBase, capacidad e idHotel.",
            "numeroHabitacion es unique a nivel de columna.",
            "Service: estadoHabitacion debe ser DISPONIBLE, OCUPADA o MANTENIMIENTO.",
            "Service: valida que idHotel exista via HotelClient.",
            "No implementado actualmente: @Positive para precioBase, @Min(1) para capacidad.",
        ],
        "rules": [
            "No se puede crear habitacion sin hotel valido.",
            "No se valida que la habitacion pertenezca a hotel en este servicio mas alla del idHotel enviado; esa relacion se verifica en reserva.",
        ],
        "endpoints": [
            ["GET", "/api/v1/habitaciones", "findAll", "Si", "USER/ADMIN autenticado", "-", "-", "-", "200 lista", "401"],
            ["GET", "/api/v1/habitaciones/{id}", "findById", "Si", "USER/ADMIN autenticado", "id", "-", "-", "200 habitacion", "401, 404"],
            ["GET", "/api/v1/habitaciones/estado/{estadoHabitacion}", "findByEstadoHabitacion", "Si", "USER/ADMIN autenticado", "estadoHabitacion", "-", "-", "200 lista", "400 estado invalido"],
            ["GET", "/api/v1/habitaciones/capacidad/{capacidad}", "findByCapacidadMinima", "Si", "USER/ADMIN autenticado", "capacidad", "-", "-", "200 lista", "401"],
            ["GET", "/api/v1/habitaciones/hotel/{idHotel}", "findByIdHotel", "Si", "USER/ADMIN autenticado", "idHotel", "-", "-", "200 lista", "401"],
            ["POST", "/api/v1/habitaciones", "guardarHabitacion", "Si", "ADMIN", "-", "-", '{"numeroHabitacion":"102","tipoHabitacion":"SUITE","precioBase":125000,"capacidad":4,"estadoHabitacion":"DISPONIBLE","idHotel":1}', "201 habitacion", "400, 401, 403, 503"],
            ["PUT", "/api/v1/habitaciones/{id}", "actualizarHabitacion", "Si", "ADMIN", "id", "-", '{"numeroHabitacion":"102","tipoHabitacion":"SUITE","precioBase":130000,"capacidad":4,"estadoHabitacion":"MANTENIMIENTO","idHotel":1}', "200 habitacion", "400, 401, 403, 404, 503"],
            ["DELETE", "/api/v1/habitaciones/{id}", "eliminarHabitacion", "Si", "ADMIN", "id", "-", "-", "204", "401, 403, 404"],
        ],
    },
    {
        "name": "ms-disponibilidad-service",
        "port": "8085",
        "base": "/api/v1/disponibilidades",
        "swagger": "http://localhost:8085/swagger-ui.html",
        "controller": "DisponibilidadController",
        "service": "DisponibilidadService",
        "repository": "DisponibilidadRepository",
        "model": "Disponibilidad",
        "dtos": "HabitacionDTO",
        "token": "Si. GET/PUT USER o ADMIN; POST/DELETE ADMIN.",
        "deps": "WebClient a ms-habitacion-service para validar idHabitacion.",
        "body": '{"idHabitacion":1,"fecha":"2026-07-10","estado":"DISPONIBLE"}',
        "validations": [
            "Disponibilidad: @NotNull en idHabitacion y fecha; @NotBlank en estado.",
            "Service: estado debe ser DISPONIBLE, OCUPADA o MANTENIMIENTO.",
            "Service: valida habitacion existente via HabitacionClient.",
            "Service: evita duplicado por idHabitacion + fecha.",
        ],
        "rules": [
            "Consulta principal para reserva: /habitacion/{idHabitacion}/fecha/{fecha}.",
            "La actualizacion permite USER o ADMIN, porque reserva usa este endpoint para ocupar/liberar disponibilidad.",
        ],
        "endpoints": [
            ["GET", "/api/v1/disponibilidades", "findAll", "Si", "USER/ADMIN", "-", "-", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/disponibilidades/{id}", "findById", "Si", "USER/ADMIN", "id", "-", "-", "200 disponibilidad", "401, 403, 404"],
            ["GET", "/api/v1/disponibilidades/habitacion/{idHabitacion}/fecha/{fecha}", "findByIdHabitacionAndFecha", "Si", "USER/ADMIN", "idHabitacion, fecha", "-", "-", "200 disponibilidad", "401, 403, 404"],
            ["POST", "/api/v1/disponibilidades", "guardar", "Si", "ADMIN", "-", "-", '{"idHabitacion":1,"fecha":"2026-07-10","estado":"DISPONIBLE"}', "201 disponibilidad", "400, 401, 403, 409 no implementado, 503"],
            ["PUT", "/api/v1/disponibilidades/{id}", "actualizar", "Si", "USER/ADMIN", "id", "-", '{"idHabitacion":1,"fecha":"2026-07-10","estado":"OCUPADA"}', "200 disponibilidad", "400, 401, 403, 404, 503"],
            ["DELETE", "/api/v1/disponibilidades/{id}", "eliminar", "Si", "ADMIN", "id", "-", "-", "204", "401, 403, 404"],
        ],
    },
    {
        "name": "ms-reserva-service",
        "port": "8086",
        "base": "/api/v1/reservas",
        "swagger": "http://localhost:8086/swagger-ui.html",
        "controller": "ReservaController",
        "service": "ReservaService",
        "repository": "ReservaRepository",
        "model": "Reserva",
        "dtos": "ClienteDTO, UsuarioDTO, HotelDTO, HabitacionDTO, DisponibilidadDTO",
        "token": "Si. GET lista/PUT/DELETE ADMIN; GET id, POST y cambio estado USER o ADMIN.",
        "deps": "WebClient a cliente, auth/usuarios, hotel, habitacion y disponibilidad.",
        "body": '{"idCliente":1,"idUsuario":1,"idHotel":1,"idHabitacion":1,"fechaInicio":"2026-07-10","fechaFin":"2026-07-12","cantidadPersonas":2,"estadoReserva":"PENDIENTE"}',
        "validations": [
            "Reserva: @NotNull en ids, fechaInicio, fechaFin, cantidadPersonas; @Min(1) en cantidadPersonas; @NotBlank en estadoReserva.",
            "Service: estadoReserva debe ser CONFIRMADA, PENDIENTE, CANCELADA o FINALIZADA.",
            "Service: fechaFin debe ser posterior a fechaInicio.",
            "Service: valida cliente, usuario, hotel, habitacion y disponibilidad por WebClient.",
            "Service: valida que habitacion.idHotel coincida con idHotel de reserva.",
            "Service: valida habitacion DISPONIBLE y capacidad suficiente.",
            "Importante: al crear una reserva no se fuerza PENDIENTE; acepta el estado enviado si es valido. Recomendacion: forzar PENDIENTE en guardar.",
        ],
        "rules": [
            "No se puede crear reserva si cliente, usuario, hotel o habitacion no existen.",
            "No se puede reservar si la habitacion no pertenece al hotel o no esta DISPONIBLE.",
            "No se puede confirmar si no hay disponibilidad diaria DISPONIBLE entre fechaInicio inclusive y fechaFin exclusiva.",
            "Cuando una reserva pasa a CONFIRMADA, marca disponibilidad como OCUPADA.",
            "Cuando deja de estar CONFIRMADA, se libera disponibilidad a DISPONIBLE.",
        ],
        "endpoints": [
            ["GET", "/api/v1/reservas", "findAll", "Si", "ADMIN", "-", "idCliente?, idUsuario?, idHotel?, idHabitacion?, estadoReserva?", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/reservas/{id}", "findById", "Si", "USER/ADMIN", "id", "-", "-", "200 reserva", "401, 403, 404"],
            ["POST", "/api/v1/reservas", "guardarReserva", "Si", "USER/ADMIN", "-", "-", '{"idCliente":1,"idUsuario":1,"idHotel":1,"idHabitacion":1,"fechaInicio":"2026-07-10","fechaFin":"2026-07-12","cantidadPersonas":2,"estadoReserva":"PENDIENTE"}', "201 reserva", "400, 401, 403, 404 desde dependencia, 503"],
            ["PUT", "/api/v1/reservas/{id}", "actualizarReserva", "Si", "ADMIN", "id", "-", '{"idCliente":1,"idUsuario":1,"idHotel":1,"idHabitacion":1,"fechaInicio":"2026-07-11","fechaFin":"2026-07-13","cantidadPersonas":2,"estadoReserva":"CONFIRMADA"}', "200 reserva", "400, 401, 403, 404, 503"],
            ["PUT", "/api/v1/reservas/{id}/estado?estadoReserva=CONFIRMADA", "cambiarEstado", "Si", "USER/ADMIN", "id", "estadoReserva", "-", "200 reserva; ocupa/libera disponibilidad segun estado", "400, 401, 403, 404, 503"],
            ["DELETE", "/api/v1/reservas/{id}", "eliminarReserva", "Si", "ADMIN", "id", "-", "-", "204; libera disponibilidad antes de eliminar", "401, 403, 404, 503"],
        ],
    },
    {
        "name": "ms-pago-service",
        "port": "8087",
        "base": "/api/v1/pagos",
        "swagger": "http://localhost:8087/swagger-ui.html",
        "controller": "PagoController",
        "service": "PagoService",
        "repository": "PagoRepository",
        "model": "Pago",
        "dtos": "ReservaDTO, UsuarioDTO",
        "token": "Si. Listar y filtros por estado ADMIN; crear y consultar por id/reserva USER o ADMIN.",
        "deps": "WebClient a ms-reserva-service y ms-auth-usuarios-service.",
        "body": '{"reservaId":1,"idUsuario":1,"monto":150000,"metodoPago":"TARJETA","estadoPago":"APROBADO","fechaPago":"2026-07-01T10:00:00"}',
        "validations": [
            "Pago: @NotNull en reservaId, idUsuario, monto, fechaPago; @NotBlank en metodoPago y estadoPago.",
            "Service: monto debe ser mayor a 0.",
            "Service: estadoPago debe ser PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO o ANULADO.",
            "Service: valida reserva y usuario existentes por WebClient.",
            "No implementado actualmente: impedir dos pagos para la misma reserva.",
        ],
        "rules": [
            "APROBADO cambia reserva a CONFIRMADA.",
            "RECHAZADO cambia reserva a CANCELADA.",
            "REEMBOLSADO cambia reserva a CANCELADA.",
            "PENDIENTE cambia reserva a PENDIENTE.",
            "ANULADO no cambia automaticamente el estado de la reserva.",
        ],
        "endpoints": [
            ["GET", "/api/v1/pagos", "findAll", "Si", "ADMIN", "-", "-", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/pagos/{id}", "findById", "Si", "USER/ADMIN", "id", "-", "-", "200 pago", "401, 403, 404"],
            ["POST", "/api/v1/pagos", "save", "Si", "USER/ADMIN", "-", "-", '{"reservaId":1,"idUsuario":1,"monto":150000,"metodoPago":"TARJETA","estadoPago":"APROBADO","fechaPago":"2026-07-01T10:00:00"}', "201 pago; si APROBADO confirma reserva", "400, 401, 403, 404, 503"],
            ["PUT", "/api/v1/pagos/{id}", "update", "Si", "ADMIN", "id", "-", '{"reservaId":1,"idUsuario":1,"monto":150000,"metodoPago":"TRANSFERENCIA","estadoPago":"REEMBOLSADO","fechaPago":"2026-07-02T10:00:00"}', "200 pago; actualiza reserva segun estado", "400, 401, 403, 404, 503"],
            ["DELETE", "/api/v1/pagos/{id}", "delete", "Si", "ADMIN", "id", "-", "-", "204", "401, 403, 404"],
            ["GET", "/api/v1/pagos/reserva/{reservaId}", "findByReservaId", "Si", "USER/ADMIN", "reservaId", "-", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/pagos/estado/{estadoPago}", "findByEstadoPago", "Si", "ADMIN", "estadoPago", "-", "-", "200 lista", "401, 403"],
        ],
    },
    {
        "name": "ms-notificacion-service",
        "port": "8088",
        "base": "/api/v1/notificaciones",
        "swagger": "http://localhost:8088/swagger-ui.html",
        "controller": "NotificacionController",
        "service": "NotificacionService",
        "repository": "NotificacionRepository",
        "model": "Notificacion",
        "dtos": "ClienteDTO, UsuarioDTO, ReservaDTO",
        "token": "Si. Listar/crear/actualizar/eliminar ADMIN; buscar por id USER o ADMIN.",
        "deps": "WebClient a cliente, auth/usuarios y reserva.",
        "body": '{"idCliente":1,"idUsuario":1,"idReserva":1,"tipo":"RESERVA","mensaje":"Reserva creada","estado":"PENDIENTE","fechaEnvio":"2026-07-01T10:00:00"}',
        "validations": [
            "Notificacion: @NotNull en idCliente, idUsuario, idReserva y fechaEnvio.",
            "@NotBlank en tipo, mensaje y estado.",
            "Service: valida cliente, usuario y reserva existentes por WebClient.",
            "No implementado actualmente: catalogo de tipo/estado.",
        ],
        "rules": ["No se envia notificacion real; se registra en base de datos."],
        "endpoints": [
            ["GET", "/api/v1/notificaciones", "findAll", "Si", "ADMIN", "-", "idCliente?, idUsuario?, idReserva?", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/notificaciones/{id}", "findById", "Si", "USER/ADMIN", "id", "-", "-", "200 notificacion", "401, 403, 404"],
            ["POST", "/api/v1/notificaciones", "guardar", "Si", "ADMIN", "-", "-", '{"idCliente":1,"idUsuario":1,"idReserva":1,"tipo":"EMAIL","mensaje":"Pago aprobado","estado":"ENVIADA","fechaEnvio":"2026-07-01T10:00:00"}', "201 notificacion", "400, 401, 403, 404, 503"],
            ["PUT", "/api/v1/notificaciones/{id}", "actualizar", "Si", "ADMIN", "id", "-", '{"idCliente":1,"idUsuario":1,"idReserva":1,"tipo":"EMAIL","mensaje":"Actualizada","estado":"LEIDA","fechaEnvio":"2026-07-01T10:00:00"}', "200 notificacion", "400, 401, 403, 404, 503"],
            ["DELETE", "/api/v1/notificaciones/{id}", "eliminar", "Si", "ADMIN", "id", "-", "-", "204", "401, 403, 404"],
        ],
    },
    {
        "name": "ms-resena-service",
        "port": "8089",
        "base": "/api/v1/resenas",
        "swagger": "http://localhost:8089/swagger-ui.html",
        "controller": "ResenaController",
        "service": "ResenaService",
        "repository": "ResenaRepository",
        "model": "Resena",
        "dtos": "ClienteDTO, HotelDTO, HabitacionDTO",
        "token": "Si. Casi todo USER o ADMIN; filtro por estado ADMIN.",
        "deps": "WebClient a cliente, hotel y habitacion. No valida reserva aunque idReserva es obligatorio.",
        "body": '{"idCliente":1,"idHotel":1,"idHabitacion":1,"idReserva":1,"calificacion":5,"comentario":"Excelente estadia","estadoResena":"PUBLICADA"}',
        "validations": [
            "Resena: @NotNull en idCliente, idHotel, idHabitacion, idReserva y calificacion.",
            "@Min(1) y @Max(5) en calificacion; @NotBlank en comentario y estadoResena.",
            "@PrePersist asigna fechaComentario automaticamente.",
            "Service: valida cliente, hotel y habitacion existentes por WebClient.",
            "No implementado actualmente: validar reserva existente, que reserva pertenezca al cliente/hotel/habitacion, catalogo de estados.",
        ],
        "rules": ["Servicio real existente. No existe ms-comentario-service en el proyecto."],
        "endpoints": [
            ["GET", "/api/v1/resenas", "listarResenas", "Si", "USER/ADMIN", "-", "-", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/resenas/{id}", "buscarPorId", "Si", "USER/ADMIN", "id", "-", "-", "200 resena", "401, 403, 404"],
            ["POST", "/api/v1/resenas", "guardarResena", "Si", "USER/ADMIN", "-", "-", '{"idCliente":1,"idHotel":1,"idHabitacion":1,"idReserva":1,"calificacion":5,"comentario":"Excelente","estadoResena":"PUBLICADA"}', "200 resena creada", "400, 401, 403, 404, 503"],
            ["PUT", "/api/v1/resenas/{id}", "actualizarResena", "Si", "USER/ADMIN", "id", "-", '{"idCliente":1,"idHotel":1,"idHabitacion":1,"idReserva":1,"calificacion":4,"comentario":"Muy bueno","estadoResena":"PUBLICADA"}', "200 resena", "400, 401, 403, 404, 503"],
            ["DELETE", "/api/v1/resenas/{id}", "eliminarResena", "Si", "USER/ADMIN", "id", "-", "-", "200 texto", "401, 403, 404"],
            ["GET", "/api/v1/resenas/cliente/{idCliente}", "buscarPorCliente", "Si", "USER/ADMIN", "idCliente", "-", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/resenas/hotel/{idHotel}", "buscarPorHotel", "Si", "USER/ADMIN", "idHotel", "-", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/resenas/habitacion/{idHabitacion}", "buscarPorHabitacion", "Si", "USER/ADMIN", "idHabitacion", "-", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/resenas/reserva/{idReserva}", "buscarPorReserva", "Si", "USER/ADMIN", "idReserva", "-", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/resenas/calificacion/{calificacion}", "buscarPorCalificacion", "Si", "USER/ADMIN", "calificacion", "-", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/resenas/estado/{estadoResena}", "buscarPorEstado", "Si", "ADMIN", "estadoResena", "-", "-", "200 lista", "401, 403"],
        ],
    },
    {
        "name": "ms-servicio-adicional-service",
        "port": "8090",
        "base": "/api/v1/servicios-adicionales",
        "swagger": "http://localhost:8090/swagger-ui.html",
        "controller": "ServicioAdicionalController",
        "service": "ServicioAdicionalService",
        "repository": "ServicioAdicionalRepository",
        "model": "ServicioAdicionalModel",
        "dtos": "HotelDTO, ReservaDTO",
        "token": "Si. GET USER o ADMIN; crear/actualizar/eliminar ADMIN.",
        "deps": "WebClient a hotel y reserva.",
        "body": '{"idHotel":1,"idReserva":1,"nombre":"Spa","descripcion":"Acceso a spa","precio":25000,"estado":"ACTIVO"}',
        "validations": [
            "ServicioAdicionalModel: @NotNull en idHotel y precio; @NotBlank en nombre, descripcion y estado.",
            "idReserva es opcional.",
            "Service: valida hotel existente y, si idReserva no es null, valida reserva existente.",
            "No implementado actualmente: precio positivo, estados permitidos, relacion reserva-hotel.",
        ],
        "rules": ["Puede existir servicio asociado solo al hotel o tambien a una reserva."],
        "endpoints": [
            ["GET", "/api/v1/servicios-adicionales", "findAll", "Si", "USER/ADMIN", "-", "idHotel?, idReserva?, estado?, nombre?", "-", "200 lista", "401, 403"],
            ["GET", "/api/v1/servicios-adicionales/{id}", "findById", "Si", "USER/ADMIN", "id", "-", "-", "200 servicio", "401, 403, 404"],
            ["POST", "/api/v1/servicios-adicionales", "guardarServicio", "Si", "ADMIN", "-", "-", '{"idHotel":1,"idReserva":1,"nombre":"Desayuno","descripcion":"Desayuno buffet","precio":12000,"estado":"ACTIVO"}', "201 servicio", "400, 401, 403, 404, 503"],
            ["PUT", "/api/v1/servicios-adicionales/{id}", "actualizarServicio", "Si", "ADMIN", "id", "-", '{"idHotel":1,"idReserva":1,"nombre":"Desayuno premium","descripcion":"Buffet premium","precio":15000,"estado":"ACTIVO"}', "200 servicio", "400, 401, 403, 404, 503"],
            ["DELETE", "/api/v1/servicios-adicionales/{id}", "eliminarServicio", "Si", "ADMIN", "id", "-", "-", "204", "401, 403, 404"],
        ],
    },
]


MODEL_SECTIONS = [
    {
        "service": "ms-auth-usuarios-service",
        "model": "Usuario",
        "table": "usuario",
        "create_required": "nombre, password y rol si se usa /api/v1/usuarios. En /api/v1/auth/register solo nombre y password; rol lo asigna el sistema como USER.",
        "do_not_send": "idUsuario, _links. En register no enviar rol porque no existe en RegisterRequest.",
        "attrs": [
            ["idUsuario", "Long", "ID si", "Autoincremental si", "No enviar", "@Id, @GeneratedValue(IDENTITY), @Column(name=idUsuario)", "No aplica", "No", "Identificador unico del usuario generado por la base de datos.", "1", "abc"],
            ["nombre", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false)", "No aplica", "No", "Nombre de usuario usado para login.", "admin", ""],
            ["password", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false)", "No aplica", "No", "Contrasena del usuario; el service la guarda encriptada con BCrypt.", "admin123", ""],
            ["rol", "String", "No", "No", "Si en Usuario", "@NotBlank, @Column(nullable=false)", "No aplica", "Rol", "Rol de autorizacion usado por Spring Security.", "ADMIN", "INVITADO"],
        ],
        "populate": {
            "json": '{"nombre":"admin","password":"admin123","rol":"ADMIN"}',
            "rules": ["Para login se necesita que el usuario exista.", "register crea siempre rol USER.", "Para crear usuarios ADMIN desde Swagger se requiere token ADMIN en /api/v1/usuarios."],
            "deps": "No depende de otros microservicios.",
            "order": "Primero crear o sembrar ADMIN; luego crear usuarios USER si se necesita.",
            "items": [
                '{"nombre":"admin","password":"admin123","rol":"ADMIN"}',
                '{"nombre":"recepcion","password":"recep123","rol":"USER"}',
                '{"nombre":"ventas","password":"ventas123","rol":"USER"}',
                '{"nombre":"supervisor","password":"super123","rol":"ADMIN"}',
                '{"nombre":"clienteapp","password":"cliente123","rol":"USER"}',
            ],
        },
    },
    {
        "service": "ms-cliente-service",
        "model": "Cliente",
        "table": "cliente",
        "create_required": "rutDocumento, telefono, direccion, rolCliente, tipoCliente.",
        "do_not_send": "id, _links.",
        "attrs": [
            ["id", "Long", "ID si", "Autoincremental si", "No enviar", "@Id, @GeneratedValue(IDENTITY), @Column(name=idCliente)", "No aplica", "No", "Identificador del cliente.", "1", "abc"],
            ["rutDocumento", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false, unique=true)", "No aplica", "Tipo/documento", "Documento unico del cliente.", "11111111-1", ""],
            ["telefono", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false)", "No aplica", "No", "Telefono de contacto.", "+56911111111", ""],
            ["direccion", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false)", "No aplica", "No", "Direccion del cliente.", "Av. Siempre Viva 123", ""],
            ["rolCliente", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false)", "No aplica", "Rol/tipo", "Rol funcional del cliente.", "TITULAR", ""],
            ["tipoCliente", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false)", "No aplica", "Tipo", "Clasificacion del cliente.", "NACIONAL", ""],
        ],
        "populate": {
            "json": '{"rutDocumento":"11111111-1","telefono":"+56911111111","direccion":"Av. Siempre Viva 123","rolCliente":"TITULAR","tipoCliente":"NACIONAL"}',
            "rules": ["rutDocumento debe ser unico.", "El modelo no valida formato RUT ni telefono; usar datos coherentes para pruebas."],
            "deps": "No depende de otros microservicios.",
            "order": "Crear despues de obtener token ADMIN y antes de reservas, resenas y notificaciones.",
            "items": [
                '{"rutDocumento":"11111111-1","telefono":"+56911111111","direccion":"Av. Siempre Viva 123","rolCliente":"TITULAR","tipoCliente":"NACIONAL"}',
                '{"rutDocumento":"22222222-2","telefono":"+56922222222","direccion":"Calle Luna 45","rolCliente":"TITULAR","tipoCliente":"NACIONAL"}',
                '{"rutDocumento":"33333333-3","telefono":"+56933333333","direccion":"Pasaje Sol 10","rolCliente":"ACOMPANANTE","tipoCliente":"EXTRANJERO"}',
                '{"rutDocumento":"44444444-4","telefono":"+56944444444","direccion":"Ruta 5 Km 20","rolCliente":"EMPRESA","tipoCliente":"NACIONAL"}',
                '{"rutDocumento":"55555555-5","telefono":"+56955555555","direccion":"Los Pinos 777","rolCliente":"TITULAR","tipoCliente":"EXTRANJERO"}',
            ],
        },
    },
    {
        "service": "ms-hotel-service",
        "model": "Hotel",
        "table": "hotel",
        "create_required": "nombre, direccion, ciudad, pais, categoria, descripcion.",
        "do_not_send": "id, _links.",
        "attrs": [
            ["id", "Long", "ID si", "Autoincremental si", "No enviar", "@Id, @GeneratedValue(IDENTITY), @Column(name=idHotel)", "No aplica", "No", "Identificador del hotel.", "1", "abc"],
            ["nombre", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false,length=50)", "No aplica", "No", "Nombre comercial del hotel.", "Hotel Transilvania", ""],
            ["direccion", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false,length=100)", "No aplica", "No", "Direccion fisica.", "Camino del Bosque 1", ""],
            ["ciudad", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false,length=50)", "No aplica", "No", "Ciudad del hotel.", "Santiago", ""],
            ["pais", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false,length=50)", "No aplica", "No", "Pais del hotel.", "Chile", ""],
            ["categoria", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false,length=30)", "No aplica", "Tipo", "Categoria comercial; no hay enum ni rango implementado.", "5 estrellas", ""],
            ["descripcion", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false,length=255)", "No aplica", "No", "Descripcion breve del hotel.", "Hotel tematico familiar", ""],
        ],
        "populate": {
            "json": '{"nombre":"Hotel Transilvania","direccion":"Camino del Bosque 1","ciudad":"Santiago","pais":"Chile","categoria":"5 estrellas","descripcion":"Hotel tematico"}',
            "rules": ["GET es publico, pero crear/editar/eliminar requiere ADMIN.", "No hay validacion numerica de estrellas; categoria es texto."],
            "deps": "No depende de otros microservicios.",
            "order": "Crear antes de habitaciones, servicios adicionales, reservas, resenas.",
            "items": [
                '{"nombre":"Hotel Transilvania","direccion":"Camino del Bosque 1","ciudad":"Santiago","pais":"Chile","categoria":"5 estrellas","descripcion":"Hotel tematico"}',
                '{"nombre":"Hotel Castillo","direccion":"Calle Niebla 22","ciudad":"Valparaiso","pais":"Chile","categoria":"4 estrellas","descripcion":"Hotel patrimonial"}',
                '{"nombre":"Hotel Lago","direccion":"Costanera 100","ciudad":"Puerto Varas","pais":"Chile","categoria":"4 estrellas","descripcion":"Vista al lago"}',
                '{"nombre":"Hotel Desierto","direccion":"Av. Oasis 8","ciudad":"San Pedro","pais":"Chile","categoria":"3 estrellas","descripcion":"Hotel de aventura"}',
                '{"nombre":"Hotel Austral","direccion":"Ruta Austral 50","ciudad":"Punta Arenas","pais":"Chile","categoria":"5 estrellas","descripcion":"Hotel austral"}',
            ],
        },
    },
    {
        "service": "ms-habitacion-service",
        "model": "Habitacion",
        "table": "habitacion",
        "create_required": "numeroHabitacion, tipoHabitacion, precioBase, capacidad, estadoHabitacion, idHotel.",
        "do_not_send": "idHabitacion, _links.",
        "attrs": [
            ["idHabitacion", "Long", "ID si", "Autoincremental si", "No enviar", "@Id, @GeneratedValue(IDENTITY), @Column(name=idHabitacion)", "No aplica", "No", "Identificador de la habitacion.", "1", "abc"],
            ["numeroHabitacion", "String", "No", "No", "Si", "@NotBlank, @Column(nullable=false, unique=true)", "No aplica", "No", "Numero o codigo visible de habitacion.", "101", ""],
            ["tipoHabitacion", "String", "No", "No", "Si", "@NotBlank", "No aplica", "Tipo", "Tipo comercial de habitacion.", "DOBLE", ""],
            ["precioBase", "Double", "No", "No", "Si", "@NotNull", "No aplica", "No", "Precio base; no tiene @Positive en el modelo actual.", "75000", "-100"],
            ["capacidad", "Integer", "No", "No", "Si", "@NotNull", "No aplica", "No", "Cantidad maxima de personas; no tiene @Min en el modelo actual.", "2", "0"],
            ["estadoHabitacion", "String", "No", "No", "Si", "@NotBlank; service valida DISPONIBLE, OCUPADA, MANTENIMIENTO", "No aplica", "Estado", "Estado operativo de la habitacion.", "DISPONIBLE", "BLOQUEADA"],
            ["idHotel", "Long", "No", "No", "Si", "@NotNull", "ms-hotel-service", "No", "Hotel al que pertenece la habitacion.", "1", "999999"],
        ],
        "populate": {
            "json": '{"numeroHabitacion":"101","tipoHabitacion":"DOBLE","precioBase":75000,"capacidad":2,"estadoHabitacion":"DISPONIBLE","idHotel":1}',
            "rules": ["Debe existir hotel con idHotel.", "estadoHabitacion debe ser DISPONIBLE, OCUPADA o MANTENIMIENTO.", "numeroHabitacion debe ser unico."],
            "deps": "Requiere hotel existente.",
            "order": "Crear hoteles antes; luego habitaciones.",
            "items": [
                '{"numeroHabitacion":"101","tipoHabitacion":"SIMPLE","precioBase":50000,"capacidad":1,"estadoHabitacion":"DISPONIBLE","idHotel":1}',
                '{"numeroHabitacion":"102","tipoHabitacion":"DOBLE","precioBase":75000,"capacidad":2,"estadoHabitacion":"DISPONIBLE","idHotel":1}',
                '{"numeroHabitacion":"201","tipoHabitacion":"SUITE","precioBase":125000,"capacidad":4,"estadoHabitacion":"DISPONIBLE","idHotel":1}',
                '{"numeroHabitacion":"301","tipoHabitacion":"FAMILIAR","precioBase":100000,"capacidad":5,"estadoHabitacion":"MANTENIMIENTO","idHotel":2}',
                '{"numeroHabitacion":"401","tipoHabitacion":"DOBLE","precioBase":82000,"capacidad":2,"estadoHabitacion":"OCUPADA","idHotel":2}',
            ],
        },
    },
    {
        "service": "ms-disponibilidad-service",
        "model": "Disponibilidad",
        "table": "disponibilidad",
        "create_required": "idHabitacion, fecha, estado.",
        "do_not_send": "id, _links.",
        "attrs": [
            ["id", "Long", "ID si", "Autoincremental si", "No enviar", "@Id, @GeneratedValue(IDENTITY), @Column(name=idDisponibilidad)", "No aplica", "No", "Identificador de disponibilidad diaria.", "1", "abc"],
            ["idHabitacion", "Long", "No", "No", "Si", "@NotNull", "ms-habitacion-service", "No", "Habitacion cuya disponibilidad se controla.", "1", "999999"],
            ["fecha", "LocalDate", "No", "No", "Si", "@NotNull", "No aplica", "No", "Dia disponible u ocupado.", "2026-07-10", "fecha-mal"],
            ["estado", "String", "No", "No", "Si", "@NotBlank; service valida DISPONIBLE, OCUPADA, MANTENIMIENTO", "No aplica", "Estado", "Estado de esa habitacion para esa fecha.", "DISPONIBLE", "RESERVADA"],
        ],
        "populate": {
            "json": '{"idHabitacion":1,"fecha":"2026-07-10","estado":"DISPONIBLE"}',
            "rules": ["Debe existir habitacion.", "No duplicar misma idHabitacion y fecha.", "Usar DISPONIBLE para fechas que se quieren reservar."],
            "deps": "Requiere habitacion existente.",
            "order": "Crear habitaciones antes; luego crear disponibilidad por fecha.",
            "items": [
                '{"idHabitacion":1,"fecha":"2026-07-10","estado":"DISPONIBLE"}',
                '{"idHabitacion":1,"fecha":"2026-07-11","estado":"DISPONIBLE"}',
                '{"idHabitacion":2,"fecha":"2026-07-10","estado":"DISPONIBLE"}',
                '{"idHabitacion":2,"fecha":"2026-07-11","estado":"OCUPADA"}',
                '{"idHabitacion":3,"fecha":"2026-07-12","estado":"MANTENIMIENTO"}',
            ],
        },
    },
    {
        "service": "ms-reserva-service",
        "model": "Reserva",
        "table": "reserva",
        "create_required": "idCliente, idUsuario, idHotel, idHabitacion, fechaInicio, fechaFin, cantidadPersonas, estadoReserva. fechaCreacion esta marcada non-null en columna, pero el service la asigna automaticamente.",
        "do_not_send": "id, fechaCreacion, _links. Recomendacion: crear con estadoReserva PENDIENTE aunque el codigo acepta otros estados validos.",
        "attrs": [
            ["id", "Long", "ID si", "Autoincremental si", "No enviar", "@Id, @GeneratedValue(IDENTITY), @Column(name=idReserva)", "No aplica", "No", "Identificador de la reserva.", "1", "abc"],
            ["idCliente", "Long", "No", "No", "Si", "@NotNull", "ms-cliente-service", "No", "Cliente asociado.", "1", "999999"],
            ["idUsuario", "Long", "No", "No", "Si", "@NotNull", "ms-auth-usuarios-service", "No", "Usuario que registra o gestiona la reserva.", "1", "999999"],
            ["idHotel", "Long", "No", "No", "Si", "@NotNull", "ms-hotel-service", "No", "Hotel de la reserva.", "1", "999999"],
            ["idHabitacion", "Long", "No", "No", "Si", "@NotNull", "ms-habitacion-service", "No", "Habitacion reservada.", "1", "999999"],
            ["fechaInicio", "LocalDate", "No", "No", "Si", "@NotNull; service exige fechaFin posterior", "No aplica", "No", "Primer dia de estadia.", "2026-07-10", "2026-07-12 con fin 2026-07-10"],
            ["fechaFin", "LocalDate", "No", "No", "Si", "@NotNull; debe ser posterior a fechaInicio", "No aplica", "No", "Dia de salida; se usa como limite exclusivo.", "2026-07-12", "2026-07-10"],
            ["cantidadPersonas", "Integer", "No", "No", "Si", "@NotNull, @Min(1); service valida capacidad", "Habitacion.capacidad", "No", "Cantidad de huespedes.", "2", "0"],
            ["estadoReserva", "String", "No", "No", "Si", "@NotBlank; service valida CONFIRMADA, PENDIENTE, CANCELADA, FINALIZADA", "Disponibilidad si CONFIRMADA", "Estado", "Estado del ciclo de vida de la reserva.", "PENDIENTE", "NUEVA"],
            ["fechaCreacion", "LocalDateTime", "No", "No", "Sistema", "@Column(nullable=false); service setea LocalDateTime.now()", "No aplica", "No", "Fecha/hora en que se crea la reserva.", "2026-07-01T10:00:00", "null si no lo setea el service"],
        ],
        "populate": {
            "json": '{"idCliente":1,"idUsuario":1,"idHotel":1,"idHabitacion":1,"fechaInicio":"2026-07-10","fechaFin":"2026-07-12","cantidadPersonas":2,"estadoReserva":"PENDIENTE"}',
            "rules": ["Deben existir cliente, usuario, hotel y habitacion.", "La habitacion debe pertenecer al hotel.", "La habitacion debe estar DISPONIBLE.", "Si se confirma, debe haber disponibilidad DISPONIBLE para cada dia.", "fechaFin debe ser posterior a fechaInicio."],
            "deps": "Cliente, usuario, hotel, habitacion y disponibilidad.",
            "order": "Crear usuario/admin, hotel, cliente, habitacion y disponibilidad antes de reserva.",
            "items": [
                '{"idCliente":1,"idUsuario":1,"idHotel":1,"idHabitacion":1,"fechaInicio":"2026-07-10","fechaFin":"2026-07-12","cantidadPersonas":1,"estadoReserva":"PENDIENTE"}',
                '{"idCliente":2,"idUsuario":1,"idHotel":1,"idHabitacion":2,"fechaInicio":"2026-07-10","fechaFin":"2026-07-11","cantidadPersonas":2,"estadoReserva":"PENDIENTE"}',
                '{"idCliente":3,"idUsuario":2,"idHotel":1,"idHabitacion":3,"fechaInicio":"2026-07-12","fechaFin":"2026-07-13","cantidadPersonas":4,"estadoReserva":"PENDIENTE"}',
                '{"idCliente":4,"idUsuario":2,"idHotel":2,"idHabitacion":4,"fechaInicio":"2026-07-14","fechaFin":"2026-07-15","cantidadPersonas":3,"estadoReserva":"PENDIENTE"}',
                '{"idCliente":5,"idUsuario":1,"idHotel":2,"idHabitacion":5,"fechaInicio":"2026-07-16","fechaFin":"2026-07-18","cantidadPersonas":2,"estadoReserva":"PENDIENTE"}',
            ],
        },
    },
    {
        "service": "ms-pago-service",
        "model": "Pago",
        "table": "pago",
        "create_required": "reservaId, idUsuario, monto, metodoPago, estadoPago, fechaPago. Si fechaPago llega null, el service intenta asignar ahora antes de validar.",
        "do_not_send": "idPago, _links. fechaPago puede enviarse o dejarse para que el service la complete, aunque el modelo declara @NotNull.",
        "attrs": [
            ["idPago", "Long", "ID si", "Autoincremental si", "No enviar", "@Id, @GeneratedValue(IDENTITY), @Column(name=idPago)", "No aplica", "No", "Identificador del pago.", "1", "abc"],
            ["reservaId", "Long", "No", "No", "Si", "@NotNull", "ms-reserva-service", "No", "Reserva que se paga.", "1", "999999"],
            ["idUsuario", "Long", "No", "No", "Si", "@NotNull", "ms-auth-usuarios-service", "No", "Usuario que registra el pago.", "1", "999999"],
            ["monto", "BigDecimal", "No", "No", "Si", "@NotNull; service exige > 0", "No aplica", "No", "Monto del pago.", "150000", "0"],
            ["metodoPago", "String", "No", "No", "Si", "@NotBlank", "No aplica", "Tipo", "Medio usado para pagar.", "TARJETA", ""],
            ["estadoPago", "String", "No", "No", "Si", "@NotBlank; service valida PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO, ANULADO", "Reserva cambia de estado", "Estado", "Resultado/ciclo del pago.", "APROBADO", "PAGADO"],
            ["fechaPago", "LocalDateTime", "No", "No", "Si o sistema", "@NotNull; service asigna now si null", "No aplica", "No", "Fecha/hora del pago.", "2026-07-01T10:00:00", "fecha-mal"],
        ],
        "populate": {
            "json": '{"reservaId":1,"idUsuario":1,"monto":150000,"metodoPago":"TARJETA","estadoPago":"APROBADO","fechaPago":"2026-07-01T10:00:00"}',
            "rules": ["Debe existir reserva y usuario.", "monto debe ser mayor a 0.", "APROBADO confirma reserva; RECHAZADO/REEMBOLSADO cancelan; PENDIENTE deja pendiente."],
            "deps": "Reserva y usuario existentes.",
            "order": "Crear reserva antes de pago.",
            "items": [
                '{"reservaId":1,"idUsuario":1,"monto":150000,"metodoPago":"TARJETA","estadoPago":"APROBADO","fechaPago":"2026-07-01T10:00:00"}',
                '{"reservaId":2,"idUsuario":1,"monto":75000,"metodoPago":"TRANSFERENCIA","estadoPago":"PENDIENTE","fechaPago":"2026-07-01T11:00:00"}',
                '{"reservaId":3,"idUsuario":2,"monto":250000,"metodoPago":"DEBITO","estadoPago":"APROBADO","fechaPago":"2026-07-01T12:00:00"}',
                '{"reservaId":4,"idUsuario":2,"monto":100000,"metodoPago":"EFECTIVO","estadoPago":"RECHAZADO","fechaPago":"2026-07-01T13:00:00"}',
                '{"reservaId":5,"idUsuario":1,"monto":164000,"metodoPago":"TARJETA","estadoPago":"REEMBOLSADO","fechaPago":"2026-07-01T14:00:00"}',
            ],
        },
    },
    {
        "service": "ms-notificacion-service",
        "model": "Notificacion",
        "table": "notificacion",
        "create_required": "idCliente, idUsuario, idReserva, tipo, mensaje, estado, fechaEnvio.",
        "do_not_send": "id, _links.",
        "attrs": [
            ["id", "Long", "ID si", "Autoincremental si", "No enviar", "@Id, @GeneratedValue(IDENTITY), @Column(name=idNotificacion)", "No aplica", "No", "Identificador de notificacion.", "1", "abc"],
            ["idCliente", "Long", "No", "No", "Si", "@NotNull", "ms-cliente-service", "No", "Cliente destinatario o asociado.", "1", "999999"],
            ["idUsuario", "Long", "No", "No", "Si", "@NotNull", "ms-auth-usuarios-service", "No", "Usuario asociado.", "1", "999999"],
            ["idReserva", "Long", "No", "No", "Si", "@NotNull", "ms-reserva-service", "No", "Reserva relacionada.", "1", "999999"],
            ["tipo", "String", "No", "No", "Si", "@NotBlank", "No aplica", "Tipo", "Canal o tipo de notificacion; no hay enum implementado.", "EMAIL", ""],
            ["mensaje", "String", "No", "No", "Si", "@NotBlank", "No aplica", "No", "Texto de la notificacion.", "Pago aprobado", ""],
            ["estado", "String", "No", "No", "Si", "@NotBlank", "No aplica", "Estado", "Estado textual; no hay catalogo implementado.", "ENVIADA", ""],
            ["fechaEnvio", "LocalDateTime", "No", "No", "Si", "@NotNull", "No aplica", "No", "Fecha/hora de envio registrada.", "2026-07-01T10:00:00", "fecha-mal"],
        ],
        "populate": {
            "json": '{"idCliente":1,"idUsuario":1,"idReserva":1,"tipo":"EMAIL","mensaje":"Reserva creada","estado":"ENVIADA","fechaEnvio":"2026-07-01T10:00:00"}',
            "rules": ["Deben existir cliente, usuario y reserva.", "No hay envio real ni catalogo de estados/tipos implementado."],
            "deps": "Cliente, usuario y reserva.",
            "order": "Crear despues de reserva.",
            "items": [
                '{"idCliente":1,"idUsuario":1,"idReserva":1,"tipo":"EMAIL","mensaje":"Reserva creada","estado":"ENVIADA","fechaEnvio":"2026-07-01T10:00:00"}',
                '{"idCliente":2,"idUsuario":1,"idReserva":2,"tipo":"SMS","mensaje":"Pago pendiente","estado":"PENDIENTE","fechaEnvio":"2026-07-01T11:00:00"}',
                '{"idCliente":3,"idUsuario":2,"idReserva":3,"tipo":"EMAIL","mensaje":"Pago aprobado","estado":"ENVIADA","fechaEnvio":"2026-07-01T12:00:00"}',
                '{"idCliente":4,"idUsuario":2,"idReserva":4,"tipo":"EMAIL","mensaje":"Reserva cancelada","estado":"ENVIADA","fechaEnvio":"2026-07-01T13:00:00"}',
                '{"idCliente":5,"idUsuario":1,"idReserva":5,"tipo":"PUSH","mensaje":"Check-in proximo","estado":"PENDIENTE","fechaEnvio":"2026-07-01T14:00:00"}',
            ],
        },
    },
    {
        "service": "ms-resena-service",
        "model": "Resena",
        "table": "resena",
        "create_required": "idCliente, idHotel, idHabitacion, idReserva, calificacion, comentario, estadoResena.",
        "do_not_send": "id, fechaComentario, _links. fechaComentario se asigna con @PrePersist.",
        "attrs": [
            ["id", "Long", "ID si", "Autoincremental si", "No enviar", "@Id, @GeneratedValue(IDENTITY), @Column(name=id_resena)", "No aplica", "No", "Identificador de resena.", "1", "abc"],
            ["idCliente", "Long", "No", "No", "Si", "@NotNull", "ms-cliente-service", "No", "Cliente autor.", "1", "999999"],
            ["idHotel", "Long", "No", "No", "Si", "@NotNull", "ms-hotel-service", "No", "Hotel reseñado.", "1", "999999"],
            ["idHabitacion", "Long", "No", "No", "Si", "@NotNull", "ms-habitacion-service", "No", "Habitacion reseñada.", "1", "999999"],
            ["idReserva", "Long", "No", "No", "Si", "@NotNull", "No validado por WebClient actual", "No", "Reserva relacionada; el service no valida que exista.", "1", "999999"],
            ["calificacion", "Integer", "No", "No", "Si", "@NotNull, @Min(1), @Max(5)", "No aplica", "No", "Puntaje de 1 a 5.", "5", "6"],
            ["comentario", "String", "No", "No", "Si", "@NotBlank, @Column(length=500)", "No aplica", "No", "Comentario escrito.", "Excelente", ""],
            ["fechaComentario", "LocalDate", "No", "No", "Sistema", "@PrePersist asigna LocalDate.now()", "No aplica", "No", "Fecha automatica de creacion.", "2026-07-01", "No enviar"],
            ["estadoResena", "String", "No", "No", "Si", "@NotBlank", "No aplica", "Estado", "Estado textual de la resena; no hay enum.", "PUBLICADA", ""],
        ],
        "populate": {
            "json": '{"idCliente":1,"idHotel":1,"idHabitacion":1,"idReserva":1,"calificacion":5,"comentario":"Excelente estadia","estadoResena":"PUBLICADA"}',
            "rules": ["Deben existir cliente, hotel y habitacion.", "idReserva es obligatorio pero no se valida contra ms-reserva-service en el service actual.", "calificacion entre 1 y 5."],
            "deps": "Cliente, hotel, habitacion; reserva recomendada aunque no validada.",
            "order": "Crear despues de reserva para mantener coherencia.",
            "items": [
                '{"idCliente":1,"idHotel":1,"idHabitacion":1,"idReserva":1,"calificacion":5,"comentario":"Excelente estadia","estadoResena":"PUBLICADA"}',
                '{"idCliente":2,"idHotel":1,"idHabitacion":2,"idReserva":2,"calificacion":4,"comentario":"Muy buena atencion","estadoResena":"PUBLICADA"}',
                '{"idCliente":3,"idHotel":1,"idHabitacion":3,"idReserva":3,"calificacion":3,"comentario":"Correcto","estadoResena":"PUBLICADA"}',
                '{"idCliente":4,"idHotel":2,"idHabitacion":4,"idReserva":4,"calificacion":2,"comentario":"Puede mejorar","estadoResena":"PENDIENTE"}',
                '{"idCliente":5,"idHotel":2,"idHabitacion":5,"idReserva":5,"calificacion":5,"comentario":"Volveria","estadoResena":"PUBLICADA"}',
            ],
        },
    },
    {
        "service": "ms-servicio-adicional-service",
        "model": "ServicioAdicionalModel",
        "table": "servicio_adicional",
        "create_required": "idHotel, nombre, descripcion, precio, estado. idReserva es opcional.",
        "do_not_send": "id, _links.",
        "attrs": [
            ["id", "Long", "ID si", "Autoincremental si", "No enviar", "@Id, @GeneratedValue(IDENTITY), @Column(name=idServicio)", "No aplica", "No", "Identificador del servicio adicional.", "1", "abc"],
            ["idHotel", "Long", "No", "No", "Si", "@NotNull, @Column(nullable=false)", "ms-hotel-service", "No", "Hotel que ofrece el servicio.", "1", "999999"],
            ["idReserva", "Long", "No", "No", "Opcional", "@Column(nullable=true)", "ms-reserva-service si se envia", "No", "Reserva asociada si el servicio ya fue contratado.", "1", "null permitido"],
            ["nombre", "String", "No", "No", "Si", "@NotBlank, @Column(length=100)", "No aplica", "No", "Nombre del servicio.", "Spa", ""],
            ["descripcion", "String", "No", "No", "Si", "@NotBlank, @Column(length=200)", "No aplica", "No", "Detalle del servicio.", "Acceso a spa", ""],
            ["precio", "BigDecimal", "No", "No", "Si", "@NotNull", "No aplica", "No", "Precio del servicio; no tiene @Positive.", "25000", "-1"],
            ["estado", "String", "No", "No", "Si", "@NotBlank, @Column(length=20)", "No aplica", "Estado", "Estado textual; no hay enum implementado.", "ACTIVO", ""],
        ],
        "populate": {
            "json": '{"idHotel":1,"idReserva":1,"nombre":"Spa","descripcion":"Acceso a spa","precio":25000,"estado":"ACTIVO"}',
            "rules": ["Debe existir hotel.", "Si se envia idReserva, debe existir reserva.", "precio positivo es recomendacion; no esta validado actualmente."],
            "deps": "Hotel obligatorio; reserva opcional.",
            "order": "Crear despues de hotel; si se asocia a reserva, crear despues de reserva.",
            "items": [
                '{"idHotel":1,"idReserva":1,"nombre":"Desayuno","descripcion":"Desayuno buffet","precio":12000,"estado":"ACTIVO"}',
                '{"idHotel":1,"idReserva":2,"nombre":"Spa","descripcion":"Acceso a spa","precio":25000,"estado":"ACTIVO"}',
                '{"idHotel":1,"idReserva":null,"nombre":"Estacionamiento","descripcion":"Estacionamiento diario","precio":8000,"estado":"ACTIVO"}',
                '{"idHotel":2,"idReserva":4,"nombre":"Tour","descripcion":"Tour por la ciudad","precio":35000,"estado":"ACTIVO"}',
                '{"idHotel":2,"idReserva":5,"nombre":"Late checkout","descripcion":"Salida extendida","precio":20000,"estado":"ACTIVO"}',
            ],
        },
    },
]


def make_styles():
    base = getSampleStyleSheet()
    base.add(ParagraphStyle("TitleCustom", parent=base["Title"], fontName="Helvetica-Bold", fontSize=22, leading=27, alignment=TA_CENTER, textColor=colors.HexColor("#17212B"), spaceAfter=16))
    base.add(ParagraphStyle("Subtitle", parent=base["BodyText"], fontSize=11, leading=15, alignment=TA_CENTER, textColor=colors.HexColor("#405060"), spaceAfter=18))
    base.add(ParagraphStyle("H1", parent=base["Heading1"], fontName="Helvetica-Bold", fontSize=16, leading=20, textColor=colors.HexColor("#17212B"), spaceBefore=12, spaceAfter=8))
    base.add(ParagraphStyle("H2", parent=base["Heading2"], fontName="Helvetica-Bold", fontSize=12.5, leading=16, textColor=colors.HexColor("#203040"), spaceBefore=9, spaceAfter=5))
    base.add(ParagraphStyle("H3", parent=base["Heading3"], fontName="Helvetica-Bold", fontSize=10.8, leading=14, textColor=colors.HexColor("#33485C"), spaceBefore=7, spaceAfter=4))
    base.add(ParagraphStyle("Body", parent=base["BodyText"], fontName="Helvetica", fontSize=9, leading=12.2, textColor=colors.HexColor("#1D2733"), spaceAfter=5))
    base.add(ParagraphStyle("GuideBullet", parent=base["BodyText"], fontName="Helvetica", fontSize=8.7, leading=11.7, leftIndent=10, firstLineIndent=-6, spaceAfter=3))
    base.add(ParagraphStyle("TableCell", parent=base["BodyText"], fontName="Helvetica", fontSize=7.1, leading=8.7, alignment=TA_LEFT))
    base.add(ParagraphStyle("Small", parent=base["BodyText"], fontName="Helvetica", fontSize=7.6, leading=10))
    base.add(ParagraphStyle("CodeBlock", parent=base["Code"], fontName="Courier", fontSize=7.4, leading=9.2, leftIndent=6, rightIndent=6, backColor=colors.HexColor("#F1F4F7"), borderColor=colors.HexColor("#D3DAE3"), borderWidth=0.5, borderPadding=5, spaceBefore=4, spaceAfter=7))
    return base


STYLES = make_styles()


def header_footer(canvas, doc):
    canvas.saveState()
    width, height = doc.pagesize
    canvas.setStrokeColor(colors.HexColor("#CAD2DC"))
    canvas.setLineWidth(0.5)
    canvas.line(doc.leftMargin, height - 1.2 * cm, width - doc.rightMargin, height - 1.2 * cm)
    canvas.setFont("Helvetica", 7.5)
    canvas.setFillColor(colors.HexColor("#526170"))
    canvas.drawString(doc.leftMargin, height - 0.9 * cm, "Hotel Transilvania - Guia de testing Swagger")
    canvas.drawRightString(width - doc.rightMargin, 0.8 * cm, f"Pagina {doc.page}")
    canvas.restoreState()


class GuideDoc(BaseDocTemplate):
    def __init__(self, filename, **kwargs):
        super().__init__(filename, **kwargs)
        portrait_w, portrait_h = A4
        landscape_w, landscape_h = landscape(A4)
        portrait_frame = Frame(
            self.leftMargin,
            self.bottomMargin,
            portrait_w - self.leftMargin - self.rightMargin,
            portrait_h - self.topMargin - self.bottomMargin,
            id="portrait",
        )
        landscape_frame = Frame(
            self.leftMargin,
            self.bottomMargin,
            landscape_w - self.leftMargin - self.rightMargin,
            landscape_h - self.topMargin - self.bottomMargin,
            id="landscape",
        )
        self.addPageTemplates(
            [
                PageTemplate(id="Portrait", frames=[portrait_frame], onPage=header_footer, pagesize=A4),
                PageTemplate(id="Landscape", frames=[landscape_frame], onPage=header_footer, pagesize=landscape(A4)),
            ]
        )


def summary_table():
    rows = [["Microservicio", "Puerto", "Ruta base", "Swagger URL", "Token", "Dependencias"]]
    for svc in SERVICES:
        rows.append([svc["name"], svc["port"], svc["base"], svc["swagger"], svc["token"], svc["deps"]])
    return table(rows, widths=[3.1 * cm, 1.1 * cm, 3.0 * cm, 4.2 * cm, 3.1 * cm, 4.1 * cm])


def endpoint_table(svc):
    rows = [["HTTP", "URL", "Metodo", "JWT", "Roles", "PathVariable", "RequestParam", "Body JSON", "Exito", "Errores"]]
    rows.extend(svc["endpoints"])
    return table(rows, widths=[0.9 * cm, 3.7 * cm, 2.0 * cm, 1.1 * cm, 2.0 * cm, 1.8 * cm, 2.0 * cm, 4.5 * cm, 4.0 * cm, 3.0 * cm])


def model_attr_table(model):
    rows = [[
        "Atributo",
        "Tipo",
        "ID",
        "Auto",
        "Obligatorio",
        "Validacion / columna",
        "Relacion",
        "Estado/rol/tipo",
        "Explicacion",
        "Ejemplo valido",
        "Ejemplo invalido",
    ]]
    rows.extend(model["attrs"])
    return table(rows, widths=[2.2 * cm, 1.8 * cm, 1.3 * cm, 1.4 * cm, 2.0 * cm, 4.1 * cm, 2.7 * cm, 2.1 * cm, 4.3 * cm, 2.2 * cm, 2.4 * cm])


def population_table(model):
    rows = [["Campo", "Detalle"]]
    pop = model["populate"]
    rows.extend([
        ["Estructura JSON esperada", code(pop["json"])],
        ["Reglas para poblar", p("<br/>".join([f"- {escape(item)}" for item in pop["rules"]]), "TableCell")],
        ["Dependencias previas", pop["deps"]],
        ["Orden correcto", pop["order"]],
        ["5 objetos recomendados", code("\n".join(pop["items"]))],
    ])
    return table(rows, widths=[4.5 * cm, 13.2 * cm])


def build_story():
    story = []
    story.append(p("Guia de Testing Swagger", "TitleCustom"))
    story.append(p("Microservicios Hotel Transilvania", "Subtitle"))
    story.append(p("Documento tecnico y practico para probar manualmente endpoints desde Swagger. Elaborado revisando controllers, services, repositories, modelos, DTOs, WebClient, SecurityConfig y GlobalExceptionHandler del codigo local.", "Body"))
    story.append(Spacer(1, 0.4 * cm))

    story.append(heading("Indice", 1))
    index_items = [
        "1. Resumen general de microservicios",
        "2. Preparacion: orden de levantamiento y uso de JWT en Swagger",
        "3. Orden logico para crear datos de prueba",
        "4. Guia por microservicio con endpoints, ejemplos y validaciones",
        "5. Modelos y entidades por microservicio",
        "6. Datos necesarios para poblar cada microservicio",
        "7. Matriz de pruebas manuales",
        "8. Errores esperados y pruebas negativas",
    ]
    story.extend(bullet(index_items))
    story.append(PageBreak())

    story.append(heading("1. Resumen General", 1))
    story.append(summary_table())
    story.append(Spacer(1, 0.2 * cm))
    story.extend(bullet([
        "ms-comentario-service: no existe en el proyecto revisado.",
        "ms-resena-service: existe y cubre reseñas/comentarios de experiencia.",
        "Gateway existente: api-gateway en puerto 8080, enruta paths /api/v1/** hacia los servicios registrados en Eureka.",
        "Swagger por servicio queda en http://localhost:{puerto}/swagger-ui.html. El gateway no expone un Swagger agregado en el codigo revisado.",
    ]))

    story.append(heading("2. Preparacion Para Swagger", 1))
    story.append(heading("Orden recomendado para levantar", 2))
    story.extend(bullet([
        "1. eureka-server en puerto 8761.",
        "2. api-gateway en puerto 8080.",
        "3. ms-auth-usuarios-service en puerto 8081.",
        "4. ms-hotel-service en puerto 8083.",
        "5. ms-cliente-service en puerto 8082.",
        "6. ms-habitacion-service en puerto 8084.",
        "7. ms-disponibilidad-service en puerto 8085.",
        "8. ms-reserva-service en puerto 8086.",
        "9. ms-pago-service en puerto 8087.",
        "10. ms-notificacion-service, ms-resena-service y ms-servicio-adicional-service.",
    ]))
    story.append(heading("Como obtener y usar JWT", 2))
    story.extend(bullet([
        "Crear usuario publico con POST /api/v1/auth/register. Este endpoint crea rol USER.",
        "Para rol ADMIN, usar POST /api/v1/usuarios con token ADMIN ya existente o sembrar un usuario ADMIN en base de datos.",
        "Hacer POST /api/v1/auth/login con nombre y password.",
        "Copiar el campo token.",
        "En Swagger, presionar Authorize y pegar el valor como Bearer <token> si el cuadro no lo agrega automaticamente.",
    ]))
    story.append(code('POST http://localhost:8081/api/v1/auth/login\n{\n  "nombre": "admin",\n  "password": "admin123"\n}\n\nRespuesta esperada:\n{\n  "token": "eyJhbGciOiJIUzI1NiJ9...",\n  "nombre": "admin",\n  "rol": "ADMIN"\n}'))

    story.append(heading("3. Orden Logico Para Datos", 1))
    story.extend(bullet([
        "1. Crear usuario/admin y obtener token.",
        "2. Crear hotel.",
        "3. Crear cliente.",
        "4. Crear habitacion asociada al hotel.",
        "5. Crear disponibilidad para cada fecha de la estadia.",
        "6. Crear reserva PENDIENTE.",
        "7. Confirmar reserva con PUT /api/v1/reservas/{id}/estado?estadoReserva=CONFIRMADA o crear pago APROBADO.",
        "8. Verificar que disponibilidad cambie a OCUPADA.",
        "9. Cancelar o cambiar una reserva confirmada y verificar que disponibilidad vuelva a DISPONIBLE.",
    ]))

    story.append(PageBreak())
    for svc in SERVICES:
        story.append(heading(svc["name"], 1))
        meta = [
            ["Campo", "Valor"],
            ["Puerto", svc["port"]],
            ["Ruta base", svc["base"]],
            ["Swagger", svc["swagger"]],
            ["Controller principal", svc["controller"]],
            ["Service principal", svc["service"]],
            ["Repository principal", svc["repository"]],
            ["Modelo/entidad principal", svc["model"]],
            ["DTOs usados", svc["dtos"]],
            ["Requiere token", svc["token"]],
            ["Dependencias", svc["deps"]],
        ]
        story.append(table(meta, widths=[4 * cm, 13.8 * cm]))
        story.append(heading("Validaciones y reglas", 2))
        story.extend(bullet(svc["validations"]))
        story.extend(bullet(svc["rules"]))
        story.append(heading("Body JSON base", 2))
        story.append(code(svc["body"]))
        story.append(NextPageTemplate("Landscape"))
        story.append(PageBreak())
        story.append(heading(f"Endpoints Swagger - {svc['name']}", 1))
        story.append(endpoint_table(svc))
        story.append(NextPageTemplate("Portrait"))
        story.append(PageBreak())

    story.append(heading("Modelos Y Entidades Por Microservicio", 1))
    story.extend(bullet([
        "Los modelos revisados son entidades JPA reales ubicadas en src/main/java de cada microservicio.",
        "No se encontraron relaciones JPA como @ManyToOne o @OneToMany entre modelos. Las relaciones entre microservicios se expresan con campos id y se validan, cuando aplica, mediante WebClient en los services.",
        "api-gateway y eureka-server no tienen entidad de negocio JPA en el codigo revisado.",
        "ms-comentario-service no existe en el proyecto actual. El servicio equivalente encontrado es ms-resena-service con la entidad Resena.",
        "El campo _links aparece en respuestas por HATEOAS, pero no debe enviarse al crear objetos desde Swagger.",
    ]))
    story.append(PageBreak())

    for model in MODEL_SECTIONS:
        story.append(heading(f"{model['service']} - {model['model']}", 1))
        story.append(table([
            ["Campo", "Detalle"],
            ["Modelo o entidad", model["model"]],
            ["Tabla BD", model["table"]],
            ["Obligatorios al crear", model["create_required"]],
            ["No enviar en JSON", model["do_not_send"]],
        ], widths=[4 * cm, 13.8 * cm]))
        story.append(heading("Tabla de atributos", 2))
        story.append(NextPageTemplate("Landscape"))
        story.append(PageBreak())
        story.append(heading(f"Atributos - {model['model']}", 1))
        story.append(model_attr_table(model))
        story.append(NextPageTemplate("Portrait"))
        story.append(PageBreak())

    story.append(heading("Datos Necesarios Para Poblar Cada Microservicio", 1))
    story.extend(bullet([
        "Los ejemplos siguientes preparan datos base para crear 5 objetos por microservicio desde Swagger.",
        "Respeta el orden porque varios servicios validan IDs en otros microservicios mediante WebClient.",
        "Ajusta los IDs si tu base de datos ya tiene registros previos o si el autoincremental no parte en 1.",
    ]))
    story.append(heading("Orden global de poblamiento", 2))
    story.extend(bullet([
        "1. Crear o sembrar usuarios, incluyendo al menos un ADMIN.",
        "2. Crear hoteles.",
        "3. Crear clientes.",
        "4. Crear habitaciones asociadas a hoteles existentes.",
        "5. Crear disponibilidades asociadas a habitaciones existentes.",
        "6. Crear reservas usando cliente, usuario, hotel, habitacion y disponibilidad validos.",
        "7. Crear pagos asociados a reservas existentes.",
        "8. Crear notificaciones, resenas y servicios adicionales cuando ya existan sus dependencias.",
    ]))
    for model in MODEL_SECTIONS:
        story.append(heading(model["service"], 2))
        story.append(population_table(model))
        story.append(Spacer(1, 0.2 * cm))

    story.append(NextPageTemplate("Landscape"))
    story.append(PageBreak())
    story.append(heading("Matriz De Pruebas Manuales", 1))
    matrix_rows = [["Caso", "Datos validos", "Datos invalidos", "Resultado esperado", "HTTP", "Revisar en Swagger", "Revisar en BD"]]
    for svc in SERVICES:
        base = svc["base"].split(",")[0]
        matrix_rows.extend(
            [
                [f"{svc['name']} - crear/listar", svc["body"], "Campos obligatorios vacios o null", "Recurso creado o lista visible", "200/201 o 400", "Status, body, mensajes de validacion", "Registro creado/actualizado"],
                [f"{svc['name']} - buscar inexistente", f"GET {base}/999999", "ID inexistente", "Mensaje de no encontrado", "404", "Body con timestamp/status/message", "No debe crear registros"],
                [f"{svc['name']} - sin token", "Endpoint protegido sin Authorization", "Token ausente", "Bloqueo de seguridad", "401", "Respuesta Unauthorized", "Sin cambios"],
                [f"{svc['name']} - rol incorrecto", "Token USER en endpoint ADMIN", "Rol insuficiente", "Acceso denegado", "403", "Mensaje de permisos", "Sin cambios"],
            ]
        )
    story.append(table(matrix_rows, widths=[4.0 * cm, 5.4 * cm, 4.1 * cm, 3.8 * cm, 1.4 * cm, 4.3 * cm, 3.4 * cm]))

    story.append(NextPageTemplate("Portrait"))
    story.append(PageBreak())
    story.append(heading("Errores Esperados", 1))
    error_rows = [
        ["HTTP", "Causa tipica", "Ejemplo para provocar", "Respuesta esperada"],
        ["400 Bad Request", "Validacion Bean Validation, JSON invalido o IllegalArgumentException", "Enviar campo obligatorio vacio, estado invalido o monto <= 0", "Map con timestamp/status y message o errors"],
        ["401 Unauthorized", "Sin token o token invalido", "Ejecutar endpoint protegido sin Authorize", "Respuesta de Spring Security"],
        ["403 Forbidden", "Token valido con rol no permitido", "USER intentando DELETE /api/v1/hoteles/{id}", "Mensaje de permisos si lo captura GlobalExceptionHandler"],
        ["404 Not Found", "NoSuchElementException al buscar id inexistente", "GET /api/v1/reservas/999999", "Map con status 404 y message"],
        ["409 Conflict", "No esta mapeado explicitamente en los handlers revisados", "Duplicar disponibilidad devuelve actualmente 400 por IllegalArgumentException", "Recomendacion: mapear duplicados a 409"],
        ["500 Internal Server Error", "Excepcion no controlada", "Falla inesperada de codigo", "Map con status 500 y mensaje generico"],
        ["503 Service Unavailable", "WebClientRequestException en algunos servicios", "Apagar ms-hotel-service y crear habitacion", "Map con status 503 si el handler del servicio lo contempla"],
    ]
    story.append(table(error_rows, widths=[3 * cm, 5 * cm, 5 * cm, 5 * cm]))

    story.append(heading("Pruebas Negativas Recomendadas", 1))
    story.extend(bullet([
        "Crear con campos vacios: validar mensajes @NotBlank y @NotNull.",
        "Crear con ids inexistentes: hotel/cliente/usuario/habitacion/reserva segun dependencia.",
        "Crear reserva con fechaInicio igual o posterior a fechaFin: debe devolver 400.",
        "Crear reserva CONFIRMADA sin disponibilidad: debe devolver 400/404 segun falte registro o estado no disponible.",
        "Crear disponibilidad duplicada misma habitacion y fecha: actualmente 400; recomendado 409.",
        "Crear habitacion con estado INVALIDO: debe devolver 400.",
        "Crear pago con monto 0 o negativo: debe devolver 400.",
        "Crear pago APROBADO: revisar que reserva pase a CONFIRMADA y disponibilidad a OCUPADA.",
        "Crear pago RECHAZADO: revisar que reserva quede CANCELADA.",
        "Probar microservicio dependiente apagado: esperar 503 si existe handler WebClientRequestException; si no, puede caer en 500.",
    ]))

    story.append(heading("Notas De Implementacion Detectadas", 1))
    story.extend(bullet([
        "No hay endpoints PATCH en los controllers revisados.",
        "Reserva no fuerza estado PENDIENTE al crear; si el body envia CONFIRMADA, intenta confirmar y ocupar disponibilidad. La regla solicitada de 'al crear debe quedar PENDIENTE' no esta implementada actualmente.",
        "Pago no impide dos pagos para la misma reserva. Recomendacion: validar existencia de pago por reserva o crear indice unico si la regla aplica.",
        "Servicio adicional y notificacion no tienen catalogo de estados implementado.",
        "Resena exige idReserva pero no valida la reserva por WebClient.",
        "Varios duplicados o conflictos se devuelven como 400 por IllegalArgumentException; si se quiere 409, se debe agregar excepcion especifica o ResponseStatusException(CONFLICT).",
    ]))
    return story


def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    doc = GuideDoc(
        str(PDF_PATH),
        pagesize=A4,
        rightMargin=1.4 * cm,
        leftMargin=1.4 * cm,
        topMargin=1.6 * cm,
        bottomMargin=1.4 * cm,
    )
    doc.build(build_story())
    print(PDF_PATH)


if __name__ == "__main__":
    main()
