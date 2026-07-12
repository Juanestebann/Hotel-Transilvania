# Hotel Transilvania — Sistema de Gestión de Reservas Hoteleras

## Descripción General

Hotel Transilvania es un sistema basado en microservicios Spring Boot para gestionar usuarios, clientes, hoteles, habitaciones, disponibilidad, reservas, pagos, reseñas, notificaciones y servicios adicionales. La evidencia académica principal se concentra en la interoperabilidad entre servicios, seguridad distribuida, documentación OpenAPI, trazabilidad por Gateway y pruebas unitarias preparadas para una medición posterior de cobertura.

## Arquitectura

El proyecto está organizado en los siguientes servicios:

- `eureka-server`: descubrimiento de servicios con Eureka.
- `api-gateway`: punto central de entrada HTTP y enrutamiento hacia microservicios.
- `ms-auth-usuarios-service`: autenticación y gestión de usuarios.
- `ms-cliente-service`: gestión y validación de clientes.
- `ms-hotel-service`: gestión de hoteles.
- `ms-habitacion-service`: gestión de habitaciones y validación remota de hotel.
- `ms-disponibilidad-service`: gestión de disponibilidad de habitaciones.
- `ms-reserva-service`: gestión de reservas.
- `ms-pago-service`: gestión de pagos y confirmación de reservas mediante flujo saga mínimo.
- `ms-notificacion-service`: gestión de notificaciones.
- `ms-resena-service`: gestión de reseñas.
- `ms-servicio-adicional-service`: gestión de servicios adicionales.

El Gateway enruta rutas `/api/v1/**` hacia servicios registrados en Eureka. En perfil local los servicios usan Eureka en `http://localhost:8761/eureka`; en perfil Docker usan `http://eureka-server:8761/eureka`.

## Puertos

| Componente | Servicio | Puerto |
| --- | --- | ---: |
| Gateway | `api-gateway` | `8080` |
| Auth/Usuarios | `ms-auth-usuarios-service` | `8081` |
| Cliente | `ms-cliente-service` | `8082` |
| Hotel | `ms-hotel-service` | `8083` |
| Habitación | `ms-habitacion-service` | `8084` |
| Disponibilidad | `ms-disponibilidad-service` | `8085` |
| Reserva | `ms-reserva-service` | `8086` |
| Pago | `ms-pago-service` | `8087` |
| Notificación | `ms-notificacion-service` | `8088` |
| Reseña | `ms-resena-service` | `8089` |
| Servicio adicional | `ms-servicio-adicional-service` | `8090` |
| Eureka | `eureka-server` | `8761` |

## Requisitos Previos

- Java 21.
- Maven Wrapper incluido en el repositorio: `mvnw` / `mvnw.cmd`.
- Docker Desktop, si se ejecuta con `docker compose`.
- MySQL local o XAMPP activo cuando se usen las propiedades actuales de base de datos.
- Git.
- Postman opcional para ejecutar la evidencia E2E por Gateway.

Las credenciales de usuarios de prueba no deben versionarse. Para la evidencia se usan placeholders:

- `ADMIN_USERNAME`
- `ADMIN_PASSWORD`
- `USER_USERNAME`
- `USER_PASSWORD`

## Ejecución Local

Para ejecución local sin Docker, iniciar primero Eureka, luego los microservicios y finalmente el Gateway. Cada servicio tiene su propio `pom.xml`; se puede ejecutar con el Maven Wrapper raíz usando `-f`:

```powershell
.\mvnw.cmd -f eureka-server\pom.xml spring-boot:run
.\mvnw.cmd -f ms-auth-usuarios-service\pom.xml spring-boot:run
.\mvnw.cmd -f ms-cliente-service\pom.xml spring-boot:run
.\mvnw.cmd -f ms-hotel-service\pom.xml spring-boot:run
.\mvnw.cmd -f ms-habitacion-service\pom.xml spring-boot:run
.\mvnw.cmd -f ms-disponibilidad-service\pom.xml spring-boot:run
.\mvnw.cmd -f ms-reserva-service\pom.xml spring-boot:run
.\mvnw.cmd -f ms-pago-service\pom.xml spring-boot:run
.\mvnw.cmd -f ms-notificacion-service\pom.xml spring-boot:run
.\mvnw.cmd -f ms-resena-service\pom.xml spring-boot:run
.\mvnw.cmd -f ms-servicio-adicional-service\pom.xml spring-boot:run
.\mvnw.cmd -f api-gateway\pom.xml spring-boot:run
```

Las propiedades locales apuntan a MySQL en `localhost:3306` y a Eureka en `localhost:8761`.

## Ejecución con Docker

Los `Dockerfile` copian `target/*.jar`, por lo que los JAR deben existir antes de construir las imágenes. Flujo recomendado:

```powershell
docker compose down
.\mvnw.cmd clean package -DskipTests
.\mvnw.cmd -f eureka-server\pom.xml clean package -DskipTests
docker compose build
docker compose up -d
docker compose ps
```

Nota: el `pom.xml` raíz compila los microservicios y el Gateway registrados como módulos; `eureka-server` tiene `pom.xml` propio. Si el JAR de Eureka ya existe, el segundo comando de Maven para Eureka puede no ser necesario.

En perfil Docker, los servicios usan `SPRING_PROFILES_ACTIVE=docker` y las bases de datos apuntan a `host.docker.internal:3306`. Por eso MySQL/XAMPP debe estar activo en la máquina anfitriona si se mantiene esta configuración.

## Eureka

Panel de Eureka:

- <http://localhost:8761>

La revisión debe confirmar que los microservicios estén registrados antes de ejecutar el flujo por Gateway.

## API Gateway

Base principal:

- <http://localhost:8080>

La evidencia académica debe ejecutarse por el Gateway y no por los puertos directos de los microservicios. Esto demuestra centralización de acceso, enrutamiento con Eureka y trazabilidad con `X-Correlation-Id`.

## Swagger

Swagger UI por servicio:

- <http://localhost:8081/swagger-ui/index.html>
- <http://localhost:8082/swagger-ui/index.html>
- <http://localhost:8083/swagger-ui/index.html>
- <http://localhost:8084/swagger-ui/index.html>
- <http://localhost:8085/swagger-ui/index.html>
- <http://localhost:8086/swagger-ui/index.html>
- <http://localhost:8087/swagger-ui/index.html>
- <http://localhost:8088/swagger-ui/index.html>
- <http://localhost:8089/swagger-ui/index.html>
- <http://localhost:8090/swagger-ui/index.html>

Si un servicio no está levantado o no aparece en Eureka, su Swagger individual tampoco estará disponible.

## Postman

La evidencia E2E versionada está en:

- `postman/hotel-transilvania-gateway-e2e.postman_collection.json`
- `postman/hotel-transilvania-local.postman_environment.json`
- `postman/README.md`

Uso recomendado:

1. Abrir Postman.
2. Importar la colección `hotel-transilvania-gateway-e2e.postman_collection.json`.
3. Importar el environment `hotel-transilvania-local.postman_environment.json`.
4. Seleccionar el environment local.
5. Reemplazar localmente los placeholders `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `USER_USERNAME` y `USER_PASSWORD`.
6. Ejecutar las carpetas en el orden documentado en `postman/README.md`.

No guardar credenciales reales en la colección ni en el environment versionado.

## Flujo Principal de Prueba

El flujo feliz por Gateway cubre:

1. Login ADMIN.
2. Login USER.
3. Crear cliente.
4. Crear hotel.
5. Crear habitación.
6. Crear disponibilidad.
7. Crear reserva.
8. Crear pago `APROBADO`.
9. Verificar reserva `CONFIRMADA`.
10. Verificar disponibilidad `OCUPADA`.
11. Crear reseña.

Este flujo evidencia interoperabilidad entre autenticación, clientes, hoteles, habitaciones, disponibilidad, reservas, pagos y reseñas.

## Casos Negativos

La evidencia contempla:

- `401` sin token.
- `403` con rol insuficiente.
- `404` para recurso inexistente.
- `503` con dependencia detenida, cuando el entorno permite detener un servicio de forma controlada.
- `504` por timeout, cuando el entorno permite pausar una dependencia de forma controlada.

Las pruebas `503` y `504` son manuales/operativas: no requieren modificar `docker-compose.yml`, puertos ni reglas de negocio.

## X-Correlation-Id

El Gateway genera o conserva el header `X-Correlation-Id` y lo devuelve en las respuestas. La colección Postman envía este header y valida que la respuesta lo conserve, lo que permite seguir una operación a través del Gateway y los servicios involucrados.

Para evidencia académica, capturar en Postman:

- header `X-Correlation-Id` enviado;
- header `X-Correlation-Id` recibido;
- resultado verde del test global de la colección.

## Mejoras Aplicadas

- Mejora 1 Seguridad USER/SERVICE: separación de acceso de usuario y llamadas internas entre servicios.
- Mejora 2 WebClient/properties/timeouts: URLs configurables por properties, sin `localhost` hardcodeado en Docker y con timeouts.
- Mejora 11A Saga mínima: pago aprobado confirma reserva y ocupa disponibilidad.
- Mejora A Evidencia WebClient: pruebas de clientes HTTP y propagación de errores remotos.
- Mejora D Gateway correlation id: filtro global `X-Correlation-Id` en el API Gateway.
- Mejora C Swagger/OpenAPI: documentación técnica de endpoints.
- Mejora E Postman/Gateway: colección E2E por `http://localhost:8080`.
- Mejora F Tests débiles: refuerzo de pruebas en Hotel, Habitación y Cliente.

## Pruebas Unitarias

Comandos dirigidos usados para validar servicios débiles, sin configurar JaCoCo:

```powershell
.\mvnw.cmd -f ms-hotel-service\pom.xml "-Dtest=HotelServiceTest,HotelControllerTest,HotelControllerSecurityTest" test
.\mvnw.cmd -f ms-habitacion-service\pom.xml "-Dtest=HabitacionServiceTest,HabitacionControllerTest,HabitacionControllerSecurityTest,HotelClientTest" test
.\mvnw.cmd -f ms-cliente-service\pom.xml "-Dtest=ClienteServiceTest,ClienteControllerTest,ClienteControllerSecurityTest" test
```

Suites completas por servicio:

```powershell
.\mvnw.cmd -f ms-hotel-service\pom.xml test
.\mvnw.cmd -f ms-habitacion-service\pom.xml test
.\mvnw.cmd -f ms-cliente-service\pom.xml test
```

JaCoCo queda pendiente para una mejora posterior; este README no configura cobertura.

## Observaciones Conocidas

- Si MySQL/XAMPP está apagado, algunos `contextLoads` pueden fallar por conexión a base de datos.
- Si el puerto `8080` está ocupado por Oracle XDB u otro proceso, el Gateway no responderá correctamente.
- Docker depende de JAR precompilado en cada servicio porque los `Dockerfile` usan `COPY target/*.jar app.jar`.
- En perfil Docker, las conexiones a MySQL usan `host.docker.internal`, por lo que se espera una base local accesible desde contenedores.
- La evidencia por Postman requiere usuarios ADMIN y USER existentes en la base local; las credenciales deben cargarse solo como variables locales de Postman.

## Evidencia Para Revisión Académica

Checklist recomendado para el profesor:

1. Revisar este `README.md`.
2. Verificar servicios en Eureka: <http://localhost:8761>.
3. Verificar Gateway: <http://localhost:8080>.
4. Revisar Swagger individual de los servicios.
5. Importar y ejecutar la colección Postman E2E por Gateway.
6. Capturar el flujo feliz, negativos `401/403/404` y trazabilidad `X-Correlation-Id`.
7. Revisar resultados de pruebas unitarias en los servicios reforzados.
