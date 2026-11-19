# Digital Money House

Billetera virtual del Sprint 1. Cinco proyectos Maven independientes (Java 17, Spring Boot 3.5). No hay un POM padre.

## Requisitos

- JDK 17 o superior
- Maven 3.9
- MySQL en `localhost:3306`
- Los servicios se descubren entre sí con Eureka

## Variables de entorno

La clave de MySQL, el secreto JWT y la clave del endpoint interno no están en los archivos de configuración. Definilas en la terminal antes de arrancar `users-service`, `auth-service` y `account-service`.

| Variable | Uso |
|---|---|
| `DB_USERNAME` | Usuario de MySQL. Si no se define, usa `root`. |
| `DB_PASSWORD` | Clave de MySQL. Obligatoria en users y account. |
| `JWT_SECRET` | Secreto HMAC del token. Mínimo 32 caracteres. Obligatoria en auth. |
| `JWT_EXPIRATION_MS` | Vida del token en milisegundos. Por defecto `86400000` (24 horas). |
| `INTERNAL_API_KEY` | Secreto compartido entre auth y users para `POST /internal/credentials/verify`. Tiene que ser el mismo valor en ambos servicios. |

Ejemplo en PowerShell, con valores propios:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<clave de MySQL>"
$env:JWT_SECRET = "<secreto de al menos 32 caracteres>"
$env:INTERNAL_API_KEY = "<clave interna compartida>"
```

Hay una plantilla vacía en `.env.example`. El archivo `.env` local no se versiona.

Las contraseñas de los usuarios se guardan con BCrypt. Un usuario cargado antes de este cambio, con la clave en texto plano, tiene que registrarse de nuevo.

## Cómo ejecutarlo

En este orden, cada comando en su carpeta:

```powershell
cd eureka-server
mvn spring-boot:run
```

```powershell
cd api-gateway
mvn spring-boot:run
```

```powershell
cd users-service
mvn spring-boot:run
```

```powershell
cd auth-service
mvn spring-boot:run
```

```powershell
cd account-service
mvn spring-boot:run
```

Tests:

```powershell
cd users-service
mvn test
cd ..\auth-service
mvn test
```

## Servicios

| Servicio | Puerto | Qué hace |
|---|---|---|
| `eureka-server` | 8761 | Registro. No se registra a sí mismo. |
| `api-gateway` | 8080 | Enruta `/users/**` a users, `/auth/**` y `/user/logout` a auth. No publica `/internal/**`. |
| `users-service` | 8081 | `POST /users/register`. Crea el usuario y la cuenta (CVU de 22 dígitos y alias de 3 palabras). Base `digital_wallet_users`. |
| `auth-service` | 8082 | `POST /auth/login` y `POST /user/logout`. JWT y blacklist en memoria. |
| `account-service` | 8083 | Esqueleto registrado en Eureka, sin endpoints de negocio. Base `digital_wallet_accounts`. |

Swagger UI, en users, auth y account: `http://localhost:<puerto>/swagger-ui/index.html`.

El tráfico de la aplicación entra por el gateway (`http://localhost:8080`).

## Cuentas

En el Sprint 1 la identidad de la cuenta (CVU, alias y vínculo con el usuario) vive en `users-service`, en la misma transacción del registro. `account-service` queda para las operaciones financieras del Sprint 2 (saldo, movimientos y tarjetas) y va a referenciar el id de cuenta ya creado. El detalle está en [docs/arquitectura.md](docs/arquitectura.md).

## Documentación

- [Arquitectura y red](docs/arquitectura.md)
- [Testing kickoff](docs/testing/kickoff.md)
- [Testing exploratorio](docs/testing/exploratorio.md)
- [Testing manual](docs/testing/manual.md)
- [Suites smoke y regresión](docs/testing/suites.md)
