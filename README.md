# banking-accounts-service

Microservicio REST para administrar cuentas bancarias, movimientos y estados de cuenta. Pertenece a la prueba técnica Devsu LLC y utiliza Java 21, Spring Boot, Gradle, PostgreSQL y RabbitMQ.

## Arquitectura y estructura

El proyecto sigue una separación por capas y puertos:

| Paquete | Responsabilidad |
|---|---|
| `domain` | Modelos, tipos, reglas y puertos de persistencia e integración |
| `application` | Servicios/casos de uso, comandos y modelos del reporte |
| `infrastructure` | Adaptadores PostgreSQL, cliente REST de Customers y mensajería RabbitMQ |
| `presentation` | Controladores REST, DTO y manejo de errores |

Los movimientos y la actualización de saldos se realizan en transacciones. El bloqueo pesimista de cuentas se utiliza durante las operaciones que modifican movimientos/saldos.

## Requisitos previos

- Java 21 (toolchain requerido por Gradle).
- PostgreSQL ejecutándose en el host Windows y accesible por TCP desde la aplicación.
- Las bases y esquemas existentes descritos abajo.
- RabbitMQ para la ejecución local. Al usar Docker Compose, RabbitMQ se inicia junto a ambos microservicios.
- Docker Desktop con Docker Compose para el despliegue coordinado.

## PostgreSQL

Accounts se conecta a la base existente `banking_accounts_db`, al esquema `accounts`, que debe contener las tablas `cuentas` y `movimientos` compatibles con las entidades del servicio.

La aplicación usa `spring.jpa.hibernate.ddl-auto: validate`: comprueba que las tablas existentes sean compatibles y no crea ni modifica el esquema de producción. No elimine, recree ni migre las bases como parte de esta configuración.

## Configuración

Las variables del proceso se leen desde `src/main/resources/application.yaml`. Para ejecución local se conservan los valores predeterminados indicados. En Docker Compose las credenciales se proporcionan desde el `.env` situado en la raíz del proyecto; no escriba credenciales reales en este README ni las versiona.

| Variable | Predeterminado local | Descripción |
|---|---|---|
| `SERVER_PORT` | `8082` | Puerto HTTP del servicio |
| `DB_URL` | `jdbc:postgresql://localhost:5432/banking_accounts_db` | URL JDBC. En Docker Compose se configura para la base Accounts del PostgreSQL del host |
| `DB_USERNAME` | `postgres` | Usuario de PostgreSQL |
| `DB_PASSWORD` | Valor local definido en `application.yaml`; configure uno propio fuera de desarrollo local | Contraseña de PostgreSQL |
| `RABBITMQ_HOST` | `localhost` | Host del broker; en Docker Compose es `rabbitmq` |
| `RABBITMQ_PORT` | `5672` | Puerto AMQP |
| `RABBITMQ_USERNAME` | Valor local definido en `application.yaml` | Usuario del broker |
| `RABBITMQ_PASSWORD` | Valor local definido en `application.yaml`; configure uno propio fuera de desarrollo local | Contraseña del broker |
| `CUSTOMERS_SERVICE_URL` | `http://localhost:8081/api` | URL base usada para consultar Customers |
| `CUSTOMERS_EXCHANGE` | `customers.events` | Exchange de eventos de clientes |
| `CUSTOMERS_QUEUE` | `accounts.customers.events` | Cola consumida por Accounts |
| `CUSTOMER_CREATED_ROUTING_KEY` | `customer.created` | Binding para el evento de creación |
| `CUSTOMER_STATUS_ROUTING_KEY` | `customer.status.changed` | Binding para el evento de cambio de estado |

En Compose, el archivo raíz `.env.example` documenta las variables de despliegue: `DB_USERNAME`, `DB_PASSWORD`, `CUSTOMERS_DB_URL`, `ACCOUNTS_DB_URL`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` y los puertos publicados opcionales. Compose asigna `ACCOUNTS_DB_URL` al `DB_URL` del contenedor Accounts. PostgreSQL sigue ejecutándose en el host Windows; desde los contenedores se accede mediante `host.docker.internal`.

## Ejecución local con Gradle

Con PostgreSQL, RabbitMQ y Customers Service disponibles, desde la raíz del repositorio:

```powershell
Set-Location .\banking-accounts-service
.\gradlew.bat bootRun
```

La API queda en `http://localhost:8082/api`. Accounts consulta el estado de clientes mediante Customers Service. Para la ejecución local, el valor predeterminado es `http://localhost:8081/api`.

## Ejecución con Docker Compose

El único Compose está en la raíz del repositorio. Copie y complete el archivo de variables y ejecute Compose desde el directorio raíz:

```powershell
Set-Location ..
Copy-Item .env.example .env
# Edite .env con las credenciales y URL de PostgreSQL correspondientes.
docker compose up --build -d
docker compose ps
```

Compose levanta RabbitMQ y los dos microservicios; no ejecuta PostgreSQL ni modifica las bases del host. RabbitMQ está disponible dentro de Docker como `rabbitmq`. Accounts llega a Customers por `http://banking-customers-service:8081/api`. La API pública de Accounts está en `http://localhost:8082/api`; Actuator expone health en `/api/actuator/health`.

Logs y detención, también desde la raíz:

```powershell
docker compose logs -f banking-accounts-service
docker compose down
```

## API REST

Todas las rutas de la tabla incluyen el prefijo `/api`. Los identificadores son numéricos. Los tipos admitidos son `AHORROS` y `CORRIENTE` para cuentas, y `DEPOSITO` y `RETIRO` para movimientos.

### Cuentas

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/cuentas` | Crea una cuenta |
| `GET` | `/api/cuentas` | Lista cuentas; acepta el filtro opcional `?clienteId={id}` |
| `GET` | `/api/cuentas/{id}` | Consulta una cuenta |
| `PUT` | `/api/cuentas/{id}` | Actualiza número y tipo de cuenta |
| `PATCH` | `/api/cuentas/{id}/estado` | Cambia el estado de la cuenta |

Crear cuenta (`POST /api/cuentas`):

```json
{
  "numeroCuenta": "225487002",
  "tipoCuenta": "CORRIENTE",
  "clienteId": 4,
  "saldoInicial": 250000.00
}
```

Respuesta `201 Created` (el identificador y los valores son ilustrativos):

```json
{
  "cuentaId": 6,
  "numeroCuenta": "225487002",
  "tipoCuenta": "CORRIENTE",
  "clienteId": 4,
  "saldoInicial": 250000.00,
  "saldoActual": 250000.00,
  "estado": true
}
```

`numeroCuenta` no puede estar vacío y admite hasta 30 caracteres; `tipoCuenta`, `clienteId` positivo y `saldoInicial` son obligatorios. El saldo inicial no puede ser negativo y admite hasta dos decimales. El cliente debe existir y estar activo. El número de cuenta debe ser único.

Actualizar cuenta (`PUT /api/cuentas/6`) recibe `numeroCuenta` y `tipoCuenta`, sin campos de saldo:

```json
{
  "numeroCuenta": "225487003",
  "tipoCuenta": "AHORROS"
}
```

El resultado tiene la forma de `AccountResponse` mostrada arriba; `saldoInicial` y `saldoActual` no se modifican por este endpoint.

Cambiar estado (`PATCH /api/cuentas/6/estado`):

```json
{
  "estado": false
}
```

Devuelve la cuenta actualizada (`200 OK`). Al reactivar una cuenta se valida que su cliente esté activo.

`GET /api/cuentas` devuelve un arreglo de `AccountResponse`; con `?clienteId=4` lista las cuentas de ese cliente. `GET /api/cuentas/6` devuelve un único `AccountResponse`.

### Movimientos

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/movimientos` | Registra un depósito o retiro |
| `GET` | `/api/movimientos` | Lista movimientos; acepta el filtro opcional `?cuentaId={id}` |
| `GET` | `/api/movimientos/{id}` | Consulta un movimiento |
| `PUT` | `/api/movimientos/{id}` | Actualiza un movimiento y recalcula el saldo afectado |

Registrar un depósito (`POST /api/movimientos`):

```json
{
  "cuentaId": 1,
  "tipoMovimiento": "DEPOSITO",
  "valor": 600.00,
  "descripcion": "Depósito de prueba"
}
```

Respuesta `201 Created`:

```json
{
  "movimientoId": 1,
  "cuentaId": 1,
  "fecha": "2026-11-01T12:00:00Z",
  "tipoMovimiento": "DEPOSITO",
  "valor": 600.00,
  "saldo": 700.00,
  "descripcion": "Depósito de prueba"
}
```

Para un retiro, `tipoMovimiento` debe ser `RETIRO` y `valor` debe ser negativo. El cero y el signo que no corresponda al tipo se rechazan. `cuentaId`, tipo y valor son obligatorios; el valor admite hasta dos decimales y `descripcion` hasta 250 caracteres.

Actualizar (`PUT /api/movimientos/1`) utiliza `cuentaId`, `tipoMovimiento`, `valor`, `descripcion` y, opcionalmente, `fecha` en formato ISO-8601 con offset. Si se omite `fecha`, se conserva la fecha actual del movimiento. El cuerpo puede reasignar el movimiento a otra cuenta; se recalculan los saldos de la cuenta anterior y la nueva.

```json
{
  "cuentaId": 1,
  "tipoMovimiento": "DEPOSITO",
  "valor": 500.00,
  "descripcion": "Movimiento corregido",
  "fecha": "2026-11-01T12:00:00Z"
}
```

La respuesta de `POST`, `PUT` y `GET /api/movimientos/{id}` es un `MovementResponse` con `movimientoId`, `cuentaId`, `fecha`, `tipoMovimiento`, `valor`, `saldo` y `descripcion`. `GET /api/movimientos` devuelve un arreglo de esas respuestas; `?cuentaId=1` aplica el filtro.

### Reporte de estado de cuenta

`GET /api/reportes` requiere los tres parámetros:

```text
/api/reportes?fechaInicio=2026-01-01&fechaFin=2026-01-31&clienteId=4
```

`fechaInicio` y `fechaFin` usan `YYYY-MM-DD`; el rango es inclusivo. Se incluyen las cuentas del cliente aunque no tengan movimientos en ese período. Cada movimiento listado corresponde al rango; el reporte no incluye datos personales del cliente.

Respuesta `200 OK`:

```json
{
  "clienteId": 4,
  "fechaInicio": "2026-01-01",
  "fechaFin": "2026-01-31",
  "cuentas": [
    {
      "numeroCuenta": "225487002",
      "tipoCuenta": "CORRIENTE",
      "saldoInicial": 250000.00,
      "saldoActual": 250600.00,
      "estado": true,
      "movimientos": [
        {
          "fecha": "2026-01-15T10:30:00Z",
          "tipoMovimiento": "DEPOSITO",
          "valor": 600.00,
          "saldo": 250600.00
        }
      ]
    }
  ]
}
```

## Reglas de negocio

- Un cliente puede tener varias cuentas; al crear un cliente no se crea ninguna cuenta automáticamente.
- No se crea una cuenta para un cliente inexistente o inactivo.
- El número de cuenta es único.
- No se permiten movimientos en cuentas inactivas ni para clientes inactivos.
- Los depósitos deben tener valor positivo y los retiros valor negativo; no se acepta cero.
- Los depósitos y retiros actualizan saldo actual. Un retiro solo se acepta si el saldo disponible es suficiente.
- Crear o editar movimientos y sus saldos se guarda de forma transaccional. Al editar un movimiento se recalculan los saldos cronológicamente de las cuentas afectadas.
- Desactivar un cliente mediante el evento de estado desactiva sus cuentas. Reactivar el cliente no reactiva automáticamente las cuentas.

## Códigos HTTP y errores

| Código | Uso |
|---|---|
| `200 OK` | Consultas y actualizaciones correctas |
| `201 Created` | Creación de cuenta o movimiento |
| `400 Bad Request` | Validación, JSON, parámetros o tipos de entrada inválidos |
| `404 Not Found` | Cuenta, movimiento o cliente referenciado no encontrado; ruta inexistente |
| `405 Method Not Allowed` | Método HTTP no admitido |
| `409 Conflict` | Regla de negocio o conflicto de integridad de datos |
| `422 Unprocessable Content` | Saldo insuficiente para el movimiento |
| `503 Service Unavailable` | Customers Service no disponible o respuesta no utilizable |
| `500 Internal Server Error` | Error inesperado |

Las respuestas de error usan la forma:

```json
{
  "timestamp": "2026-11-01T12:00:00Z",
  "status": 422,
  "error": "SALDO_NO_DISPONIBLE",
  "message": "Saldo no disponible",
  "path": "/api/movimientos"
}
```

El código de saldo insuficiente es `422`, con `error: SALDO_NO_DISPONIBLE` y el mensaje `Saldo no disponible`. Los errores internos de persistencia no exponen detalles de PostgreSQL.

## Integración RabbitMQ

Customers Service publica eventos JSON en el exchange durable `customers.events`. Accounts declara y consume la cola durable `accounts.customers.events`, enlazada con:

| Routing key | Comportamiento en Accounts |
|---|---|
| `customer.created` | Registra el evento en los logs; no crea una cuenta |
| `customer.status.changed` | Si `estado` es `false`, desactiva todas las cuentas del cliente de forma transaccional |

La creación de un cliente no crea cuentas automáticamente. La reactivación de un cliente no reactiva automáticamente sus cuentas. La aplicación valida los eventos recibidos; un evento no soportado o inválido es rechazado sin reencolado automático.

El consumidor configura el dead-letter exchange `customers.events.dead` (tipo `direct`) y la dead-letter queue `accounts.customers.events.dead`. El binding usa la routing key `accounts.customers.events.dead`.

Además del consumo de eventos, Accounts consulta `GET {CUSTOMERS_SERVICE_URL}/clientes/{id}` para verificar que el cliente esté activo al crear cuentas y al realizar operaciones que requieren un cliente operativo. La respuesta debe incluir el booleano `estado`.

## Pruebas

Desde el directorio del servicio:

```powershell
.\gradlew.bat test
```

La suite incluye pruebas unitarias de depósitos, retiros, saldo insuficiente, cuenta inactiva y cliente inactivo, además de una prueba de integración REST que cubre persistencia, actualización del saldo, edición de movimientos y rechazo de retiro sin fondos. Las pruebas utilizan H2; la ejecución normal del servicio utiliza PostgreSQL. No se requiere Testcontainers.

## Postman

La colección está en `postman/banking-accounts-service.postman_collection.json`. En Postman, seleccione **Import > File** y elija ese archivo. La colección define `baseUrl` como `http://localhost:8082/api`; modifíquelo solo si el puerto publicado se cambió.

Incluye solicitudes para cuentas, movimientos y reporte. Para validar creación de cuentas, use un `clienteId` existente y activo en Customers Service.
