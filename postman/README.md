# Evidencia E2E via API Gateway

Esta carpeta contiene la evidencia versionable para el criterio **IE 3.3.3 - Interoperabilidad via Gateway** del proyecto Hotel Transilvania.

Formato elegido:

- `hotel-transilvania-gateway-e2e.postman_collection.json`: coleccion Postman con flujo feliz, pruebas negativas y limpieza.
- `hotel-transilvania-local.postman_environment.json`: environment local con variables para `http://localhost:8080`.

## Requisitos previos

- API Gateway disponible en `http://localhost:8080`.
- Eureka disponible y microservicios registrados.
- MySQL local/XAMPP activo si se ejecuta con el perfil Docker actual, porque los `application-docker.properties` usan `host.docker.internal` para la base de datos.
- Servicios del flujo levantados:
  - `ms-auth-usuarios-service`
  - `ms-cliente-service`
  - `ms-hotel-service`
  - `ms-habitacion-service`
  - `ms-disponibilidad-service`
  - `ms-reserva-service`
  - `ms-pago-service`
  - `ms-resena-service`
  - `api-gateway`
  - `eureka-server`
- Usuarios de prueba existentes en la base local:
  - un usuario con rol `ADMIN`;
  - un usuario con rol `USER`.

No guardar credenciales reales en los archivos versionados. Reemplazar en Postman los placeholders:

- `ADMIN_USERNAME`
- `ADMIN_PASSWORD`
- `USER_USERNAME`
- `USER_PASSWORD`

## Importacion en Postman

1. Abrir Postman.
2. Importar `hotel-transilvania-gateway-e2e.postman_collection.json`.
3. Importar `hotel-transilvania-local.postman_environment.json`.
4. Seleccionar el environment **Hotel Transilvania - Gateway Local**.
5. Editar localmente las variables `adminUsername`, `adminPassword`, `userUsername` y `userPassword`.

## Variables usadas

La coleccion usa y actualiza estas variables:

- `baseUrl`: `http://localhost:8080`
- `adminToken`
- `userToken`
- `idUsuario`
- `idCliente`
- `idHotel`
- `idHabitacion`
- `idDisponibilidad`
- `idReserva`
- `idPago`
- `idResena`
- `correlationId`
- `nonexistentId`
- `rutDocumento`
- `numeroHabitacion`
- `fechaInicio`
- `fechaFin`
- `fechaDisponibilidad`
- `fechaCreacion`
- `fechaPago`

`runSuffix`, `rutDocumento` y `numeroHabitacion` se autogeneran si estan vacios, para reducir choques por datos unicos al repetir la evidencia.

## Orden recomendado

Ejecutar en este orden:

1. Carpeta `00 Setup autenticacion`.
2. Carpeta `01 Flujo feliz Gateway`.
3. Carpeta `02 Pruebas negativas automaticas`.
4. Tomar evidencia: capturas o export del resultado del Runner.
5. Carpeta `99 Limpieza manual`.

No ejecutar la carpeta `03 Pruebas negativas manuales 503 504` dentro del flujo normal. Esa carpeta requiere detener o pausar servicios de forma controlada.

## Flujo feliz cubierto

Todas las requests pasan por el Gateway usando `{{baseUrl}}`, no por puertos directos de microservicios:

- `POST /api/v1/auth/login` como ADMIN.
- `POST /api/v1/auth/login` como USER.
- `GET /api/v1/usuarios/me`.
- `POST /api/v1/clientes`.
- `GET /api/v1/clientes/{id}/validacion`.
- `POST /api/v1/hoteles`.
- `POST /api/v1/habitaciones`.
- `POST /api/v1/disponibilidades`.
- `POST /api/v1/reservas`.
- `POST /api/v1/pagos` con `estadoPago = APROBADO`.
- `GET /api/v1/reservas/{id}` y validacion de `estadoReserva = CONFIRMADA`.
- `GET /api/v1/disponibilidades/{id}` y validacion de `estado = OCUPADA`.
- `POST /api/v1/resenas`.

Este flujo evidencia interoperabilidad real entre Gateway, Auth, Cliente, Hotel, Habitacion, Disponibilidad, Reserva, Pago y Resena.

Nota operativa: la evidencia versionada usa una reserva de una noche (`fechaFin` es el dia siguiente a `fechaInicio`). `ms-reserva-service` ocupa disponibilidad por cada noche entre `fechaInicio` y antes de `fechaFin`; si se aumenta el rango de fechas, se debe crear una disponibilidad por cada noche o el pago aprobado puede fallar al confirmar la reserva.

## Pruebas negativas automaticas

La carpeta `02 Pruebas negativas automaticas` valida:

- `401` sin token en `GET /api/v1/usuarios/me`.
- `403` usando token USER en endpoint ADMIN `GET /api/v1/clientes`.
- `404` con ID inexistente en `GET /api/v1/hoteles/{id}`.

## Pruebas negativas manuales 503 y 504

Estas pruebas son opcionales y deben ejecutarse solo si el entorno esta disponible.

Para validar `503`:

1. Ejecutar setup y crear al menos un hotel con la carpeta `01 Flujo feliz Gateway` hasta `POST hotel`.
2. Detener la dependencia `ms-hotel-service`.
3. Ejecutar la request `503 dependencia detenida - hotel-service requerido por habitacion`.
4. Resultado esperado: `503 Service Unavailable`.
5. Volver a levantar `ms-hotel-service`.

Para validar `504`:

1. Ejecutar setup y crear al menos un hotel con la carpeta `01 Flujo feliz Gateway` hasta `POST hotel`.
2. Pausar la dependencia `ms-hotel-service`.
3. Ejecutar la request `504 dependencia pausada - hotel-service requerido por habitacion`.
4. Resultado esperado: `504 Gateway Timeout`.
5. Reanudar `ms-hotel-service`.

Comandos orientativos si se usa Docker:

```powershell
docker compose stop ms-hotel-service
docker compose start ms-hotel-service
docker pause ms-hotel-service
docker unpause ms-hotel-service
```

No modificar `docker-compose.yml` para estas pruebas.

## Evidencia de X-Correlation-Id

Todas las requests incluyen el header:

```text
X-Correlation-Id: {{correlationId}}
```

La coleccion tiene un test global que valida que el Gateway devuelve el mismo header `X-Correlation-Id` en la respuesta. Para el profesor, conviene capturar:

- el resultado verde del test global en el Runner;
- el header enviado en una request;
- el header devuelto en la response.

## Validacion runtime realizada

Validado localmente el 2026-07-12 contra el API Gateway real en `http://localhost:8080`, con Docker Compose levantado y `api-gateway` publicado en `0.0.0.0:8080->8080/tcp`.

Comprobacion directa:

```powershell
curl.exe -i -H "X-Correlation-Id: codex-final-check" http://localhost:8080/api/v1/hoteles
```

Resultado observado: `HTTP/1.1 200 OK`, `Content-Type: application/json` y header de respuesta `X-Correlation-Id: codex-final-check`.

Flujo validado por Gateway con usuarios temporales de entorno local, no versionados como credenciales reales:

- login ADMIN y USER;
- `GET /api/v1/usuarios/me`;
- creacion y validacion de cliente;
- creacion de hotel, habitacion y disponibilidad;
- creacion de reserva;
- pago `APROBADO`;
- reserva verificada como `CONFIRMADA`;
- disponibilidad verificada como `OCUPADA`;
- creacion de resena;
- negativas `401`, `403`, `404`;
- `X-Correlation-Id` devuelto en todas las respuestas validadas.

Si se usan otros usuarios locales, ajustar solo las variables del environment en Postman.

### Revision final Postman / Gateway

Revision final ejecutada el 2026-07-12 contra el API Gateway real en `http://localhost:8080`.

- Metodo: validacion manual equivalente a la coleccion, porque Newman no estaba instalado en el entorno local.
- Docker Compose: servicios levantados y publicados en los puertos esperados.
- Gateway: `GET /api/v1/hoteles` respondio `200 OK`; no respondio Oracle XDB.
- Trazabilidad: `X-Correlation-Id: revision-final-gateway` fue enviado y devuelto por el Gateway; sin header enviado, el Gateway genero uno nuevo.
- Flujo feliz validado: login ADMIN, login USER, `usuarios/me`, cliente, validacion cliente, hotel, habitacion, disponibilidad de una noche, reserva, pago `APROBADO`, reserva `CONFIRMADA`, disponibilidad `OCUPADA` y resena.
- Casos negativos validados: `401` sin token, `403` con USER en endpoint ADMIN y `404` con ID inexistente.
- Casos `503` y `504`: no ejecutados en la revision final para no interrumpir contenedores; quedan contemplados como pruebas manuales tecnicas en esta guia.
- Limpieza: los datos temporales de la corrida final fueron eliminados por API en orden seguro.

Usuarios usados en la revision: `codex_e2e_admin` y `codex_e2e_user`, existentes solo en la base local de prueba. Sus credenciales no se versionan.

## Limpieza de datos temporales

Ejecutar la carpeta `99 Limpieza manual` al terminar. Intenta limpiar en orden:

1. resena;
2. pago;
3. cancelacion de reserva;
4. reserva;
5. disponibilidad;
6. habitacion;
7. hotel;
8. cliente.

Las pruebas de limpieza aceptan `2xx` o `404`, para permitir reintentos si algun recurso ya fue eliminado manualmente.

## Ejecucion con Newman

Si Newman esta instalado, se puede ejecutar una evidencia automatizada:

```powershell
newman run postman/hotel-transilvania-gateway-e2e.postman_collection.json `
  -e postman/hotel-transilvania-local.postman_environment.json `
  --env-var adminUsername=ADMIN_USERNAME `
  --env-var adminPassword=ADMIN_PASSWORD `
  --env-var userUsername=USER_USERNAME `
  --env-var userPassword=USER_PASSWORD
```

Para la entrega, exportar el resultado de Postman Runner o adjuntar capturas donde se vean los tests de estado HTTP, flujo confirmado y `X-Correlation-Id`.
