# -*- coding: utf-8 -*-
"""Genera PDF: gestión de usuarios y roles — Hotel Transilvania."""

from pathlib import Path

from fpdf import FPDF

ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / "docs" / "Gestion_Usuarios_y_Roles_Hotel_Transilvania.pdf"


class Pdf(FPDF):
    def __init__(self):
        super().__init__()
        font_dir = Path(__file__).resolve().parent.parent
        dejavu = (
            Path(__import__("fpdf").__file__).parent / "font" / "DejaVuSans.ttf"
        )
        if dejavu.exists():
            self.add_font("DejaVu", "", str(dejavu))
            self._font = "DejaVu"
        else:
            self._font = "Helvetica"

    def set_body_font(self, size=11, style=""):
        self.set_font(self._font, style, size)

    def h1(self, text):
        self.set_body_font(16, "B")
        self.multi_cell(self.epw, 10, text)
        self.ln(4)

    def h2(self, text):
        self.set_body_font(13, "B")
        self.multi_cell(self.epw, 8, text)
        self.ln(2)

    def p(self, text):
        self.set_body_font(11)
        self.multi_cell(self.epw, 6, text)
        self.ln(2)

    def bullet(self, text):
        self.set_body_font(11)
        self.multi_cell(self.epw, 6, f"  - {text}")

    def code_block(self, text):
        self.set_body_font(8)
        self.set_fill_color(245, 245, 245)
        for line in text.strip().split("\n"):
            self.multi_cell(self.epw, 5, f"  {line}", fill=True)
        self.ln(3)


def build():
    pdf = Pdf()
    pdf.set_auto_page_break(auto=True, margin=20)
    pdf.set_margins(20, 20, 20)
    pdf.add_page()

    pdf.h1("Hotel Transilvania")
    pdf.h2("Gestion de usuarios y roles (ms-auth-usuarios-service)")
    pdf.p(
        "Documento de referencia para el equipo. Explica como se crean usuarios, "
        "que rol se asigna en cada caso y como obtener un usuario ADMIN sin editar "
        "la base de datos (cuando ya existe un administrador)."
    )
    pdf.p("Fecha de referencia: mayo 2026 | Puerto del servicio: 8081")

    pdf.h2("1. Resumen")
    pdf.bullet("El registro publico (/auth/register) siempre crea usuarios con rol USER.")
    pdf.bullet(
        "Un ADMIN autenticado puede crear usuarios USER o ADMIN via POST /api/v1/usuarios."
    )
    pdf.bullet(
        "Un ADMIN puede cambiar el rol de un usuario con PUT /api/v1/usuarios/{id}."
    )
    pdf.bullet(
        "Un usuario USER no puede crearse a si mismo como ADMIN ni modificar roles."
    )
    pdf.bullet(
        "Todos los endpoints de usuarios viven en un solo microservicio (no estan repartidos)."
    )
    pdf.ln(4)

    pdf.h2("2. Microservicio y ubicacion en el codigo")
    pdf.p(
        "Tanto /auth/register (siempre USER) como /usuarios (crear ADMIN con token ADMIN) "
        "pertenecen al MISMO microservicio. No hay un MS separado para /usuarios."
    )
    pdf.p("Microservicio: ms-auth-usuarios-service")
    pdf.p("Puerto: 8081")
    pdf.p("Base de datos: auth_usuarios_db (tabla usuario)")
    pdf.p("Modulo Maven: ms-auth-usuarios-service/")
    pdf.ln(2)

    pdf.h2("2.1 Mapa endpoint -> controlador -> servicio")
    pdf.set_body_font(9, "B")
    cw = [42, 32, 28, 38, 50]
    hdrs = ["HTTP / Ruta", "Controlador", "Metodo Java", "Servicio", "Rol / acceso"]
    for i, h in enumerate(hdrs):
        pdf.cell(cw[i], 7, h, border=1, align="C")
    pdf.ln()
    pdf.set_body_font(8)
    map_rows = [
        (
            "POST /api/v1/auth/register",
            "AuthController",
            "register()",
            "UsuarioService.register()",
            "Siempre USER. Publico.",
        ),
        (
            "POST /api/v1/auth/login",
            "AuthController",
            "login()",
            "AuthenticationManager + JwtService",
            "Publico. Devuelve JWT.",
        ),
        (
            "GET /api/v1/auth/validate",
            "AuthController",
            "validateToken()",
            "-",
            "Requiere token valido.",
        ),
        (
            "POST /api/v1/usuarios",
            "UsuarioController",
            "save()",
            "UsuarioService.save()",
            "USER o ADMIN. Solo token ADMIN.",
        ),
        (
            "PUT /api/v1/usuarios/{id}",
            "UsuarioController",
            "update()",
            "UsuarioService.update()",
            "Cambiar rol. Solo ADMIN.",
        ),
        (
            "GET /api/v1/usuarios",
            "UsuarioController",
            "findAll()",
            "UsuarioService.findAll()",
            "Solo ADMIN.",
        ),
        (
            "GET /api/v1/usuarios/{id}",
            "UsuarioController",
            "findById()",
            "UsuarioService.findById()",
            "USER o ADMIN.",
        ),
        (
            "DELETE /api/v1/usuarios/{id}",
            "UsuarioController",
            "delete()",
            "UsuarioService.delete()",
            "Solo ADMIN.",
        ),
    ]
    for row in map_rows:
        for i, cell in enumerate(row):
            pdf.cell(cw[i], 12, cell, border=1)
        pdf.ln()

    pdf.ln(4)
    pdf.h2("2.2 Rutas de archivos en el proyecto")
    pdf.code_block(
        """ms-auth-usuarios-service/src/main/java/.../controller/
  AuthController.java      -> @RequestMapping("/api/v1/auth")
  UsuarioController.java   -> @RequestMapping("/api/v1/usuarios")

ms-auth-usuarios-service/src/main/java/.../service/
  UsuarioService.java      -> register(), save(), update(), ...

ms-auth-usuarios-service/src/main/java/.../dto/
  RegisterRequest.java     -> solo nombre y password (register)

Base de datos: auth_usuarios_db"""
    )
    pdf.p(
        "Resumen: /auth/* es autenticacion y registro publico. /usuarios/* es CRUD de "
        "usuarios con @PreAuthorize. Ambos comparten UsuarioService y la misma BD."
    )

    pdf.h2("3. Registro publico (solo USER)")
    pdf.p("Endpoint: POST /api/v1/auth/register")
    pdf.p("No requiere token. El cuerpo solo admite nombre y contraseña (RegisterRequest).")
    pdf.p(
        "En UsuarioService.register() el rol se fuerza a USER antes de guardar. "
        "Aunque se intente enviar otro rol por otro medio, este endpoint no lo acepta."
    )
    pdf.code_block(
        """POST http://localhost:8081/api/v1/auth/register
Content-Type: application/json

{
  "nombre": "maria",
  "password": "miClave123"
}

Respuesta: rol siempre "USER"."""
    )

    pdf.h2("4. Creacion por administrador (USER o ADMIN)")
    pdf.p("Endpoint: POST /api/v1/usuarios")
    pdf.p("Requiere: Authorization: Bearer <token de usuario ADMIN>")
    pdf.p("El cuerpo incluye el campo rol (USER o ADMIN).")
    pdf.code_block(
        """POST http://localhost:8081/api/v1/auth/login
{ "nombre": "admin", "password": "admin123" }

POST http://localhost:8081/api/v1/usuarios
Authorization: Bearer <token>
Content-Type: application/json

{
  "nombre": "nuevo_admin",
  "password": "claveSegura1",
  "rol": "ADMIN"
}"""
    )

    pdf.h2("5. Cambiar rol de un usuario existente")
    pdf.p("Endpoint: PUT /api/v1/usuarios/{id}")
    pdf.p("Solo ADMIN. Permite actualizar nombre, contraseña (opcional) y rol.")
    pdf.code_block(
        """PUT http://localhost:8081/api/v1/usuarios/2
Authorization: Bearer <token_admin>
Content-Type: application/json

{
  "nombre": "maria",
  "password": "",
  "rol": "ADMIN"
}"""
    )
    pdf.p(
        "Nota: si password va vacio, conviene omitir el campo o no cambiarla segun "
        "la logica del servicio (solo re codifica si no esta en blanco)."
    )

    pdf.h2("6. Tabla comparativa")
    pdf.set_body_font(9, "B")
    col_w = [40, 28, 28, 38, 36]
    headers = ["Accion", "Solo USER?", "Sin tocar BD?", "Quien puede", "Microservicio"]
    for i, h in enumerate(headers):
        pdf.cell(col_w[i], 8, h, border=1, align="C")
    pdf.ln()
    pdf.set_body_font(8)
    rows = [
        ("POST /auth/register", "Si", "N/A", "Cualquiera", "ms-auth :8081"),
        ("POST /usuarios", "No", "Si", "Solo ADMIN", "ms-auth :8081"),
        ("PUT /usuarios/{id}", "No", "Si", "Solo ADMIN", "ms-auth :8081"),
        ("Usuario USER", "No puede ADMIN", "No", "USER", "-"),
        ("Script SQL / phpMyAdmin", "No", "Manual", "MySQL local", "auth_usuarios_db"),
    ]
    for row in rows:
        for i, cell in enumerate(row):
            pdf.cell(col_w[i], 7, cell, border=1)
        pdf.ln()

    pdf.ln(6)
    pdf.h2("7. Por que a veces se usa la base de datos")
    pdf.p(
        "Si el equipo solo usa /auth/register o aun no existe ningun usuario ADMIN "
        "(primer arranque), no hay forma de llamar a POST /usuarios con permisos de "
        "administrador. En ese caso es normal:"
    )
    pdf.bullet("Ejecutar el script scripts/sql/run-seed.ps1 (usuario admin / admin123).")
    pdf.bullet("O insertar manualmente en auth_usuarios_db.usuario.")
    pdf.p(
        "Una vez existe un ADMIN, el resto de altas y cambios de rol pueden hacerse por API."
    )

    pdf.h2("8. Usuarios de prueba (seed)")
    pdf.bullet("admin / admin123  ->  rol ADMIN")
    pdf.bullet("user / user123    ->  rol USER")
    pdf.p("Ver scripts/sql/README.md para repoblar datos tras reinstalar XAMPP.")

    pdf.h2("9. Seguridad (motivo del diseno)")
    pdf.p(
        "Antes era posible registrarse como ADMIN enviando rol en el body del register. "
        "Eso se corrigio: el registro publico solo crea USER para evitar escalada de "
        "privilegios. La gestion de ADMIN queda en endpoints protegidos."
    )

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    pdf.output(str(OUTPUT))
    print(f"PDF generado: {OUTPUT}")


if __name__ == "__main__":
    build()
