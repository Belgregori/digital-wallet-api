# Testing manual

Casos para ejecutar contra el gateway `http://localhost:8080`, con Eureka y los servicios en marcha. Las variables `DB_PASSWORD`, `JWT_SECRET` e `INTERNAL_API_KEY` tienen que estar definidas, con el mismo valor en users, auth y account.

Los ids de smoke y regresión se agrupan en [suites.md](suites.md).

## Smoke

### SMK-01 Registro

1. `POST /users/register` con nombre, apellido, email nuevo, DNI de 8 dígitos, teléfono de 10 dígitos y contraseña de 8 a 72 caracteres.
2. Esperado: 201, sin campo `password`. `account.cvu` tiene 22 dígitos. `account.alias` son tres palabras de `palabras.txt` separadas por un punto. `account.balance` es 0.

### SMK-02 Login

1. `POST /auth/login` con el email y la contraseña del usuario de SMK-01.
2. Esperado: 200 y un `token` no vacío.

### SMK-03 Logout

1. `POST /user/logout` con `Authorization: Bearer <token de SMK-02>`.
2. Esperado: 200 y `{ "message": "Sesión cerrada" }`.

### SMK-04 Swagger

1. Abrir `/swagger-ui/index.html` en los puertos 8081, 8082 y 8083.
2. Esperado: 200. Users muestra el registro. Auth muestra login y logout. Account no define operaciones.

## Regresión

### REG-01 Credenciales incorrectas

1. `POST /auth/login` con un email que no existe y una contraseña de al menos 8 caracteres.
2. Repetir con un email registrado y una contraseña distinta.
3. Esperado en ambos: 401 y mensaje `Email o contraseña incorrectos`. El texto no dice cuál de los dos falló.

### REG-02 Email de login inválido

1. `POST /auth/login` con `email` en `no-es-email` y contraseña de 8 caracteres.
2. Esperado: 400. El mensaje nombra el campo `email`.

### REG-03 Datos de registro inválidos

1. `POST /users/register` con nombre vacío, DNI de 2 dígitos, teléfono vacío y contraseña vacía.
2. Esperado: 400.
3. Repetir con todos los campos válidos salvo la contraseña `corta`.
4. Esperado: 400 y el mensaje indica que `password` no cumple la longitud.

### REG-04 Token vencido

1. Emitir un JWT con el mismo `JWT_SECRET` y expiración en el pasado, o esperar a que venza uno de prueba con `JWT_EXPIRATION_MS` corto.
2. `POST /user/logout` con ese token.
3. Esperado: 401 y mensaje `El token está vencido`. Un segundo logout del mismo token sigue en 401. No queda aceptado como sesión cerrada.

### REG-05 Token ausente o alterado

1. `POST /user/logout` sin header `Authorization`.
2. Esperado: 400, `El token es obligatorio`.
3. Repetir con `Authorization: Bearer` seguido de un token válido al que se le agrega un carácter.
4. Esperado: 401, `El token no es válido`.

### REG-06 Endpoint interno

1. `GET http://localhost:8081/internal/users?email=alguien@example.com`.
2. Esperado: no devuelve credenciales. Sin la ruta pública, la respuesta es 401 si falta la clave interna, o 404 si la ruta ya no existe.
3. `POST http://localhost:8081/internal/credentials/verify` sin `X-Internal-Token`.
4. Esperado: 401 `No autorizado`, aunque el email y la contraseña sean correctos.
5. `POST http://localhost:8080/internal/credentials/verify`.
6. Esperado: el gateway no enruta esa ruta.

### REG-07 Formato de CVU y alias

1. Registrar tres usuarios distintos.
2. Esperado: cada CVU coincide con `^[0-9]{22}$`. Cada alias coincide con `^[a-z]+(\.[a-z]+){2}$`. No se repite CVU ni alias entre las tres respuestas.

### REG-08 Error 500

1. Con users detenido, o con `DB_PASSWORD` incorrecta de forma que el servicio no esté disponible, `POST http://localhost:8080/users/register` con un cuerpo válido.
2. Esperado: el gateway responde 500 (o 503 si no hay instancia en Eureka). El cuerpo no incluye la clave de MySQL ni el hash de la contraseña.
3. Cuando users está sano, el mismo request de un email nuevo responde 201. Este caso documenta la falla de infraestructura, no un error de validación.

### REG-09 Contraseña almacenada

1. Después de SMK-01, consultar la columna `password` del usuario en `digital_wallet_users`.
2. Esperado: el valor empieza con `$2a$` y no contiene la contraseña enviada.

## Sprint 2

El token sale de SMK-02. Los id salen de la respuesta del registro: `id` del usuario y `account.id` de la cuenta.

### SMK-05 Saldo

1. `GET /accounts/{accountId}` con `Authorization: Bearer <token>`.
2. Esperado: 200. `balance` es el dinero disponible. `cvu` tiene 22 dígitos y `alias` tiene tres palabras.

### SMK-06 Movimientos

1. `GET /accounts/{accountId}/transactions`.
2. Esperado: 200 y un arreglo. Con datos, el primero es el más reciente. `?limit=5` devuelve como máximo cinco.

### SMK-07 Perfil

1. `GET /users/{userId}` con el token del dueño.
2. Esperado: 200, con nombre, apellido, DNI, email, teléfono, `cvu` y `alias`. No aparece `password`.

### SMK-08 Tarjeta

1. `POST /cards` con número de 16 dígitos, titular, vencimiento `MM/yy`, CVV y `type` `DEBIT` o `CREDIT`.
2. `POST /accounts/{accountId}/cards` con `{ "cardId": <id> }`.
3. `GET /cards/accounts/{accountId}/cards` y `GET /cards/accounts/{accountId}/cards/{cardId}`.
4. `DELETE /cards/accounts/{accountId}/cards/{cardId}`.
5. Esperado: 201 al crear y al asociar, 200 en el listado y el detalle, 200 al borrar con `Tarjeta eliminada`. La respuesta no incluye el CVV.

### REG-10 Sin token o token vencido

1. `GET /accounts/{accountId}` sin `Authorization`, y otra vez con un JWT vencido.
2. Esperado: 403 en ambos.

### REG-11 Cuenta de otro usuario

1. Con el token de un usuario, `GET /accounts/{id}` de una cuenta que no es suya.
2. Esperado: 403.

### REG-12 Id inválido

1. `GET /accounts/abc` y `GET /users/abc` con un token vigente.
2. Esperado: 400.

### REG-13 Edición de perfil y alias

1. `PUT /users/{userId}` con nombre, apellido y teléfono válidos.
2. `PATCH /accounts/{accountId}` con `{ "alias": "rio.mar.sol" }`.
3. Esperado: 200 y los datos nuevos. Un alias que no son tres palabras responde 400. Un alias ya usado responde 409.

### REG-14 Tarjeta en otra cuenta

1. Asociar una tarjeta a la cuenta A.
2. Con el dueño de la cuenta B, `POST /accounts/{cuentaB}/cards` con el mismo `cardId`.
3. Esperado: 409.
4. `GET /cards/accounts/{cuentaA}/cards/999999` responde 404.
5. Borrar una tarjeta que no está en la cuenta responde 404.

### REG-15 Error 500 en cuenta

1. Con `account-service` detenido, `GET /accounts/{accountId}` con un token vigente.
2. Esperado: 500 o 503, sin la clave de MySQL ni el secreto JWT en el cuerpo.
