# Testing manual

Casos para ejecutar contra el gateway `http://localhost:8080`, con Eureka y los servicios del Sprint 1 en marcha. Las variables `DB_PASSWORD`, `JWT_SECRET` e `INTERNAL_API_KEY` tienen que estar definidas.

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
