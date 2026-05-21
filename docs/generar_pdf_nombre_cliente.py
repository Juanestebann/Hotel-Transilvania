# -*- coding: utf-8 -*-
"""PDF: implicancias de agregar nombre al Cliente — Hotel Transilvania."""

from pathlib import Path

from fpdf import FPDF

ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / "docs" / "Implicancias_Nombre_Cliente_Hotel_Transilvania.pdf"


class Pdf(FPDF):
    def __init__(self):
        super().__init__()
        arial = Path(r"C:\Windows\Fonts\arial.ttf")
        arial_b = Path(r"C:\Windows\Fonts\arialbd.ttf")
        self._font = "Helvetica"
        if arial.exists():
            self.add_font("DocFont", "", str(arial))
            self.add_font("DocFont", "B", str(arial_b if arial_b.exists() else arial))
            self._font = "DocFont"

    def set_body_font(self, size=11, style=""):
        self.set_font(self._font, style, size)

    def _write_block(self, text, size=11, style="", line_h=6, gap=2):
        self.set_body_font(size, style)
        self.set_x(self.l_margin)
        self.multi_cell(self.epw, line_h, text)
        self.ln(gap)

    def h1(self, text):
        self._write_block(text, 16, "B", 10, 4)

    def h2(self, text):
        self._write_block(text, 13, "B", 8, 2)

    def h3(self, text):
        self._write_block(text, 11, "B", 7, 1)

    def p(self, text):
        self._write_block(text, 11, "", 6, 2)

    def bullet(self, text):
        self._write_block(f"  - {text}", 11, "", 6, 1)

    def code_block(self, text):
        self.set_body_font(8)
        self.set_fill_color(245, 245, 245)
        for line in text.strip().split("\n"):
            self.set_x(self.l_margin)
            self.multi_cell(self.epw, 5, f"  {line}", fill=True)
        self.ln(3)

    def table(self, headers, rows, col_weights):
        """Tabla con ancho total = epw (evita texto cortado a la derecha)."""
        total = sum(col_weights)
        widths = [self.epw * w / total for w in col_weights]
        line_h = 6

        self.set_body_font(9, "B")
        for i, h in enumerate(headers):
            self.cell(widths[i], 8, h, border=1, align="C")
        self.ln()

        self.set_body_font(8)
        for row in rows:
            x0 = self.l_margin
            y0 = self.get_y()
            heights = []
            for i, cell in enumerate(row):
                self.set_xy(x0 + sum(widths[:i]), y0)
                self.multi_cell(widths[i], line_h, cell, border=0, align="L")
                heights.append(self.get_y() - y0)
                self.set_xy(x0, y0)
            max_h = max(heights) if heights else line_h
            x = x0
            for w in widths:
                self.rect(x, y0, w, max_h)
                x += w
            self.set_y(y0 + max_h)

        self.ln(4)


def build():
    pdf = Pdf()
    pdf.set_auto_page_break(auto=True, margin=18)
    pdf.set_margins(left=18, top=18, right=18)
    pdf.add_page()

    pdf.h1("Hotel Transilvania")
    pdf.h2("Implicancias de implementar el atributo nombre en Cliente")
    pdf.p(
        "Documento técnico de análisis de impacto. Describe el estado actual del "
        "modelo Cliente, los cambios necesarios por capa, lo que no se modifica, "
        "riesgos, decisiones de diseño y recomendación final."
    )
    pdf.p("Microservicio afectado principalmente: ms-cliente-service (puerto 8082)")
    pdf.p("Base de datos: clientes_db | Tabla: cliente")

    pdf.h2("1. Situación actual")
    pdf.p(
        "La entidad Cliente no posee un campo nombre de persona. Se identifica "
        "mediante rutDocumento (único), teléfono, dirección, rolCliente y tipoCliente."
    )
    pdf.code_block(
        """Campos actuales (Cliente.java / V1__crear_tabla_cliente.sql):
  - idCliente (PK)
  - rutDocumento (UNIQUE, obligatorio)
  - telefono (obligatorio)
  - direccion (obligatorio)
  - rolCliente (obligatorio)
  - tipoCliente (obligatorio)"""
    )
    pdf.p(
        "Nota: el microservicio Usuario ya tiene el campo nombre, pero cumple otra "
        "función: es el nombre de usuario para login (admin, user), no el nombre "
        "del huésped."
    )

    pdf.h2("2. Por qué evaluar este cambio")
    pdf.bullet("Dominio hotelero: un huésped se reconoce por nombre y documento.")
    pdf.bullet("Mejor experiencia en listados, notificaciones e informes.")
    pdf.bullet("El impacto técnico es acotado: otros MS solo referencian idCliente.")
    pdf.bullet("No es obligatorio para que reservas o pagos funcionen hoy.")
    pdf.ln(2)

    pdf.h2("3. Cambios obligatorios (ms-cliente-service)")
    pdf.h3("3.1 Modelo y persistencia")
    pdf.bullet(
        "Cliente.java: nuevo atributo nombre con @Column y validación (@NotBlank)."
    )
    pdf.bullet(
        "Nueva migración Flyway V2__agregar_nombre_cliente.sql (ALTER TABLE). "
        "No modificar V1 si ya se ejecutó en algún entorno."
    )
    pdf.bullet("ClienteService.actualizar(): agregar setNombre(...).")
    pdf.bullet("ClienteService.guardar(): opcional log con nombre.")
    pdf.p("ClienteController: sin cambio de rutas; el JSON de POST/PUT incluye nombre.")

    pdf.h3("3.2 Ejemplo de migración SQL")
    pdf.code_block(
        """-- V2__agregar_nombre_cliente.sql (ejemplo)
ALTER TABLE cliente
  ADD COLUMN nombre VARCHAR(100) NOT NULL DEFAULT '';

-- Luego actualizar registros existentes y quitar DEFAULT si se desea."""
    )

    pdf.h3("3.3 Ejemplo de body API (Postman)")
    pdf.code_block(
        """POST http://localhost:8082/api/v1/clientes
Authorization: Bearer <token_admin>

{
  "rutDocumento": "11111111-1",
  "nombre": "Juan Pérez",
  "telefono": "+56911111111",
  "direccion": "Calle Principal 100",
  "rolCliente": "HUESPED",
  "tipoCliente": "NACIONAL"
}"""
    )

    pdf.h2("4. Cambios recomendados (integraciones)")
    pdf.p(
        "Otros microservicios consumen GET /api/v1/clientes/{id} vía WebClient y "
        "deserializan la respuesta en ClienteDTO. Conviene agregar el campo nombre "
        "en los DTO para reflejar el JSON (Jackson lo mapea sin cambiar la lógica)."
    )
    pdf.table(
        ["Microservicio", "Archivo", "Cambio"],
        [
            ("ms-reserva-service", "dto/ClienteDTO.java", "Agregar private String nombre"),
            (
                "ms-resena-service",
                "dto/ClienteDTO.java",
                "Agregar nombre (hoy solo: id, rut, tipo)",
            ),
            ("ms-notificacion-service", "dto/ClienteDTO.java", "Agregar nombre"),
        ],
        [1.1, 1.0, 1.2],
    )
    pdf.p(
        "ClienteClient en reserva, reseña y notificación: sin cambio de código. "
        "Siguen llamando obtenerClientePorId(id)."
    )

    pdf.h2("5. Lo que NO cambia")
    pdf.bullet(
        "Reserva, Reseña, Notificación, Pago: solo guardan idCliente (referencia lógica)."
    )
    pdf.bullet("Migraciones Flyway de otros microservicios.")
    pdf.bullet("Endpoints de otros MS (solo validan existencia del cliente por ID).")
    pdf.bullet("Usuario (auth): modelo independiente; ya tiene nombre de login.")
    pdf.ln(2)

    pdf.h2("6. Flujo de validación actual (sin usar campos del DTO)")
    pdf.p(
        "ReservaService.validarReservaCompleta() llama clienteClient.obtenerClientePorId(). "
        "Si el cliente no existe devuelve 404 o excepción. No lee rut ni teléfono del DTO. "
        "Agregar nombre no altera este flujo salvo que se quiera validar el campo después."
    )

    pdf.h2("7. Base de datos y datos existentes")
    pdf.p("Si ya hay filas en la tabla cliente, definir estrategia:")
    pdf.bullet("Opción A: columna NOT NULL con DEFAULT '' y luego UPDATE manual.")
    pdf.bullet("Opción B: columna nullable, rellenar datos, luego NOT NULL en V3.")
    pdf.bullet("Bases nuevas: pueden incluir nombre en V1 solo si Flyway nunca corrió.")
    pdf.p("Scripts seed (scripts/sql/02_clientes_db.sql): agregar columna nombre en INSERT.")

    pdf.h2("8. Decisiones de diseño a acordar")
    pdf.table(
        ["Tema", "Opciones"],
        [
            ("Obligatoriedad", "Recomendado: @NotBlank + NOT NULL"),
            ("Unicidad", "No necesario; rutDocumento ya es UNIQUE"),
            ("Longitud", "VARCHAR(100) o VARCHAR(255)"),
            ("Búsqueda", "Opcional: findByNombre o GET ?nombre="),
        ],
        [1, 2],
    )

    pdf.h2("9. Riesgos y mitigación")
    pdf.bullet("Flyway checksum: no editar V1; usar solo V2.")
    pdf.bullet("Clientes sin nombre en BD: plan de migración de datos.")
    pdf.bullet("Postman/colecciones desactualizadas: actualizar ejemplos JSON.")
    pdf.bullet("DTOs desalineados: tres ClienteDTO distintos hoy; buen momento para alinearlos.")

    pdf.h2("10. Mejoras opcionales posteriores")
    pdf.bullet("Notificaciones personalizadas: usar cliente.getNombre() en el mensaje.")
    pdf.bullet("Filtro GET /clientes?nombre=... en ClienteController.")
    pdf.bullet("Documentación técnica y PDFs del proyecto.")

    pdf.h2("11. Esfuerzo estimado")
    pdf.bullet("Mínimo (cliente + V2 + service): 3-4 archivos, complejidad baja.")
    pdf.bullet("Recomendado (+ 3 DTOs + seed SQL): 6-8 archivos, complejidad baja.")
    pdf.bullet("Completo (+ búsqueda + docs): 10+ archivos, complejidad media.")
    pdf.ln(2)

    pdf.h2("12. Diagrama de impacto (texto)")
    pdf.code_block(
        """ms-cliente-service  <-- CAMBIO PRINCIPAL
  Cliente.java + V2 SQL + ClienteService
        |
        | GET /clientes/{id}  (JSON incluye "nombre")
        v
  +------------------+------------------+------------------+
  | ms-reserva       | ms-resena        | ms-notificacion  |
  | ClienteDTO       | ClienteDTO       | ClienteDTO       |
  | +campo nombre    | +campo nombre    | +campo nombre    |
  | (logica igual)   | (logica igual)   | (logica igual)   |
  +------------------+------------------+------------------+

  reserva / resena / notificacion / pago  --> idCliente  --> SIN CAMBIO"""
    )

    pdf.h2("13. Recomendación final")
    pdf.p(
        "No es estrictamente necesario para el funcionamiento actual del sistema, "
        "porque las integraciones solo validan el id del cliente. Si el objetivo "
        "es un sistema hotelero realista, presentación al cliente del encargo o "
        "mejor trazabilidad del huésped, se recomienda implementar nombre como "
        "campo obligatorio. El costo de desarrollo es bajo y el riesgo es manejable "
        "con una migración V2 y actualización de datos de prueba."
    )
    pdf.p(
        "Comparación: Usuario ya tiene nombre (login). Cliente es el que carece de "
        "nombre de persona; son cambios independientes."
    )

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    pdf.output(str(OUTPUT))
    print(f"PDF generado: {OUTPUT}")


if __name__ == "__main__":
    build()
