# Testing exploratorio

Las sesiones exploratorias no reemplazan los casos de [manual.md](manual.md). Sirven para encontrar huecos que esos casos todavía no nombran. Cada sesión deja objetivo, escenarios y hallazgos.

## Sesión EXP-01 — Seguridad del Sprint 1

- Fecha: 2026-11-19
- Objetivo: ver si el login, el JWT y el acceso a users filtran datos o aceptan credenciales débiles.
- Charter: recorrer registro, login, logout y el puerto directo de users sin seguir un caso escrito.
- Duración prevista: 45 minutos.

Escenarios explorados:

- Registrar un usuario y leer la fila que queda en MySQL.
- Iniciar sesión con email inexistente y, por separado, con contraseña incorrecta.
- Reusar un JWT después de su vencimiento en logout y en una petición autenticada.
- Llamar a users en el puerto 8081, fuera del gateway, a las rutas internas.
- Buscar la clave de MySQL y el secreto JWT dentro de `application.yml`.

Hallazgos:

| Id | Hallazgo | Estado |
|---|---|---|
| EXP-01-H1 | La contraseña se guardaba en texto plano y el login la comparaba por igualdad. | Corregido. El alta guarda BCrypt y la verificación usa `PasswordEncoder.matches`. |
| EXP-01-H2 | Un JWT vencido se trataba como firma válida y podía cerrar sesión. | Corregido. Vencido responde 401 y no entra en la blacklist. |
| EXP-01-H3 | `GET /internal/users` devolvía email y contraseña en el puerto 8081. | Corregido. Esa ruta ya no existe. La verificación es `POST /internal/credentials/verify`, exige `X-Internal-Token` y no devuelve la clave. |
| EXP-01-H4 | Login respondía "El usuario no existe" o "La contraseña es incorrecta". | Corregido. Ambos casos responden 401 con "Email o contraseña incorrectos". |
| EXP-01-H5 | `DB_PASSWORD` y `jwt.secret` estaban escritos en los YAML. | Corregido. Salen de variables de entorno. |

## Sesión EXP-02 — Límites de datos y arranque

- Fecha: a ejecutar en el próximo ciclo, antes de cerrar el Sprint 1.
- Objetivo: encontrar combinaciones de datos que el alta acepta y no deberían persistirse, y ver qué pasa si falta una variable de entorno.
- Charter: 30 minutos sobre `POST /users/register` y el arranque de users y auth.

Escenarios a recorrer:

- Nombre de un carácter, teléfono con letras, contraseña de 7 y de 73 caracteres, email sin dominio.
- CVU y alias de varias altas seguidas: longitud, caracteres y repetición.
- Arrancar users sin `DB_PASSWORD` y auth sin `JWT_SECRET` o sin `INTERNAL_API_KEY`.
- Pedir `/internal/credentials/verify` por el puerto 8080 y por el 8081 sin header.

Los hallazgos de esta sesión se anotan en esta tabla y, si se confirman, pasan a un caso `REG-nn`.

| Id | Escenario | Resultado | ¿Pasa a regresión? |
|---|---|---|---|
| | | | |
