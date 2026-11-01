# banking-accounts-service

Microservicio REST para administrar cuentas bancarias, movimientos y reportes. La aplicación trabaja con la base existente `banking_accounts_db`, escucha eventos publicados por `banking-customers-service` y no crea ni modifica el esquema de producción.

## Arquitectura

El código separa el dominio y sus puertos (`domain`), los casos de uso transaccionales (`application`), los adaptadores de PostgreSQL, REST de clientes y RabbitMQ (`infrastructure`), y la API/serialización de errores (`presentation`). Los saldos se calculan con `BigDecimal`; el registro de movimientos bloquea la fila de la cuenta con `PESSIMISTIC_WRITE`.

El proyecto existente usa Gradle y Spring Boot 4.1.2 con Java 21; se conserva esa configuración en lugar de migrar el build. Las pruebas usan H2 con un esquema efímero equivalente; la aplicación siempre valida el esquema real con `ddl-auto: validate`.

## Requisitos

- Java 21.
- PostgreSQL con la base `banking_accounts_db` y el esquema `accounts` ya creado.
- RabbitMQ para procesar eventos.
- Docker y Docker Compose para el despliegue en contenedores.

La aplicación no crea otra base de datos. El `docker-compose.yaml` levanta RabbitMQ y el servicio; PostgreSQL y `banking-customers-service` se consideran dependencias externas configurables.

## Configuración

La configuración está en `src/main/resources/application.yaml`. Variables:

| Variable | Predeterminado | Uso |
|---|---|---|
| `SERVER_PORT` | `8082` | Puerto HTTP |
| `DB_URL` | `jdbc:postgresql://localhost:5432/banking_accounts_db` | URL de la base existente |
| `DB_USERNAME` | `postgres` | Usuario PostgreSQL |
| `DB_PASSWORD` | `postgres` | Contraseña PostgreSQL |
| `RABBITMQ_HOST` | `localhost` | Host RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Puerto AMQP |
| `RABBITMQ_USERNAME` | `guest` | Usuario RabbitMQ |
| `RABBITMQ_PASSWORD` | `guest` | Contraseña RabbitMQ |
| `CUSTOMERS_SERVICE_URL` | `http://localhost:8081/api` | URL base del microservicio de clientes |
| `CUSTOMERS_EXCHANGE` | `customers.events` | Exchange topic |
| `CUSTOMERS_QUEUE` | `accounts.customers.events` | Cola consumida por este servicio |
| `CUSTOMER_CREATED_ROUTING_KEY` | `customer.created` | Routing key de alta de cliente |
| `CUSTOMER_STATUS_ROUTING_KEY` | `customer.status.changed` | Routing key de cambio de estado |

Para validar la creación de cuentas y los movimientos se consulta `GET {CUSTOMERS_SERVICE_URL}/clientes/{id}`. Se espera un booleano `estado` (también se acepta `status`). Los fallos del servicio de clientes se informan como `503`; no se permite operar con un estado de cliente desconocido.

## Ejecución local

Con PostgreSQL y RabbitMQ disponibles:

```powershell
.\gradlew.bat bootRun
```

La API queda en `http://localhost:8082/api`; el health check de Actuator está en `/api/actuator/health`.

## Ejecución con Docker

Configura `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y, si aplica, `CUSTOMERS_SERVICE_URL` en el entorno. En Windows/macOS la configuración predeterminada conecta con PostgreSQL y el servicio de clientes del host mediante `host.docker.internal`.

```powershell
docker compose up --build
```

RabbitMQ espera a estar saludable antes de iniciar la aplicación. PostgreSQL no se provisiona ni se recrea por Compose.
Para desarrollo, Compose usa `accounts` como credencial local de RabbitMQ (RabbitMQ restringe el usuario `guest` a conexiones locales). Sustituye ambas credenciales mediante variables de entorno fuera del entorno de prueba.

## API

Todas las rutas usan el prefijo `/api`. No hay endpoints de eliminación.

### Cuentas

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/cuentas` | Crear cuenta |
| `GET` | `/cuentas` | Listar cuentas; acepta `?clienteId=2` |
| `GET` | `/cuentas/{id}` | Consultar cuenta |
| `PUT` | `/cuentas/{id}` | Modificar número y tipo de cuenta |
| `PATCH` | `/cuentas/{id}/estado` | Activar/desactivar una cuenta |

Ejemplo para crear:

```json
{
  "numeroCuenta": "225487",
  "tipoCuenta": "CORRIENTE",
  "clienteId": 2,
  "saldoInicial": 100.00
}
```

El cliente debe existir y estar activo. El saldo inicial no es negativo ni editable mediante `PUT`, para no invalidar el historial de movimientos.

### Movimientos

| Método | Ruta | Uso |
|---|---|---|
| `POST` | `/movimientos` | Registrar depósito o retiro |
| `GET` | `/movimientos` | Listar movimientos; acepta `?cuentaId=1` |
| `GET` | `/movimientos/{id}` | Consultar movimiento |
| `PUT` | `/movimientos/{id}` | Modificar movimiento y recalcular saldos |

Depósito:

```json
{
  "cuentaId": 1,
  "tipoMovimiento": "DEPOSITO",
  "valor": 600.00,
  "descripcion": "Depósito"
}
```

Retiro: `tipoMovimiento` es `RETIRO` y `valor` debe ser negativo (por ejemplo, `-575.00`). El valor cero, un signo incongruente, una cuenta inactiva y un cliente inactivo son rechazados. El movimiento y el saldo se guardan en una única transacción. Si el retiro no está cubierto, la API responde `422` con `error: SALDO_NO_DISPONIBLE` y el mensaje exacto `Saldo no disponible`.

### Reporte

`GET /reportes?fechaInicio=2022-02-01&fechaFin=2022-02-28&clienteId=2` devuelve la identificación (`clienteId`), cuentas asociadas y movimientos del rango inclusivo. Se incluyen cuentas sin movimientos en el período. La identificación se limita a `clienteId`, porque este servicio no mantiene datos personales del cliente.

### Errores

Las respuestas de error contienen `timestamp`, `status`, `error`, `message` y `path`. Los detalles internos de PostgreSQL no se exponen.

## RabbitMQ

El consumidor declara un exchange `topic`, una cola durable, bindings para ambas routing keys, conversión JSON y dead-letter exchange/queue. `CustomerCreatedEvent` se registra en logs sin crear cuentas. `CustomerStatusChangedEvent` con `estado=false` desactiva las cuentas del cliente de forma transaccional; una reentrega es segura porque desactivar una cuenta repetidamente no cambia el resultado. La activación del cliente no activa cuentas automáticamente.

## Pruebas

```powershell
.\gradlew.bat test
.\gradlew.bat clean check
```

Incluye pruebas unitarias de depósitos, retiros, saldo insuficiente, cuenta inactiva y cliente inactivo, además de una prueba de integración REST que verifica persistencia y saldo usando H2. Testcontainers no es requisito para las pruebas locales.

## Postman

Importa `postman/banking-accounts-service.postman_collection.json`. La colección contiene operaciones de cuentas, depósitos, retiros, saldo insuficiente, movimientos, reporte y cambio de estado.
