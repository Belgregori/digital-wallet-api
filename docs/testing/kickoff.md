# Testing kickoff

Pautas para escribir casos, reportar defectos y separar smoke de regresión en Digital Money House.

## Cómo escribir un caso

Cada caso tiene un id estable, un objetivo y un resultado observable. No se describe la implementación interna.

| Campo | Qué va |
|---|---|
| Id | `SMK-nn` si es smoke, `REG-nn` si es regresión. No se reutiliza un id dado de baja. |
| Título | Acción y resultado, en una línea. |
| Precondiciones | Servicios levantados, datos que ya existen, variables de entorno. |
| Pasos | Numerados. Una acción por paso. |
| Datos | Valores concretos del request. Sin contraseñas reales de MySQL ni el secreto JWT. |
| Resultado esperado | Código HTTP, campos presentes y campos que no deben aparecer. |
| Suite | Smoke o regresión. |

Un caso de smoke responde si el flujo principal sigue vivo: registrar, iniciar sesión, cerrar sesión y ver Swagger. Un caso de regresión cubre un defecto ya corregido o una regla que no puede romperse (BCrypt, token vencido, mensaje genérico de login, formato de CVU y alias, validaciones).

Si un caso nuevo nace de un bug de producción o de una corrección, entra en regresión. Si solo comprueba que el sistema arranca y el camino feliz responde, entra en smoke.

## Cómo reportar un defecto

Título: `qué falla` + `dónde`. Ejemplo: `Login distingue usuario inexistente de contraseña incorrecta`.

En el cuerpo:

1. Servicio, puerto y endpoint.
2. Request completo, sin secretos de infraestructura.
3. Respuesta real (status y cuerpo).
4. Respuesta esperada.
5. Severidad: bloqueante, mayor, menor.
6. Caso de prueba relacionado, si existe. Si no existe, se crea uno de regresión al cerrar el defecto.

No se cierra un defecto sin evidencia: salida de `mvn test`, respuesta HTTP o captura del caso manual. La evidencia se agrega en [suites.md](suites.md).

## Severidad

- Bloqueante: no se puede registrar, iniciar sesión o el servicio no arranca.
- Mayor: se filtra una contraseña, se acepta un token vencido o el mensaje de login revela si el usuario existe.
- Menor: texto de validación incompleto o un campo con longitud distinta a la pactada, sin filtrar datos.

## Definición de listo para un caso

El caso está escrito antes de ejecutar la suite, tiene id, y después de correrlo queda el resultado (aprobado o fallido) con fecha en la suite correspondiente.
