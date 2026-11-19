# Suites

Clasificación de los casos de [manual.md](manual.md). La evidencia automática es la corrida de `mvn test` del 2026-11-19. Los casos manuales que pegan contra los servicios levantados quedan pendientes de una pasada con el entorno completo y se marcan así en la tabla.

## Smoke

Camino mínimo para decir que el Sprint 1 responde.

| Id | Caso | Tipo de evidencia |
|---|---|---|
| SMK-01 | Registro 201, sin contraseña, con CVU y alias | Manual. El test `registerSavesUserAndAccountAndStoresBcryptHash` cubre la forma de la respuesta y el hash. |
| SMK-02 | Login 200 con token | Manual. El test `loginReturnsJwtWhenCredentialsMatch` cubre la emisión del token. |
| SMK-03 | Logout 200 | Manual. El test `logoutStoresTokenInBlacklist` cubre la invalidación. |
| SMK-04 | Swagger en 8081, 8082 y 8083 | Manual, contra los servicios en marcha. |

## Regresión

Reglas que ya fallaron o que no pueden volver atrás.

| Id | Caso | Tipo de evidencia |
|---|---|---|
| REG-01 | 401 genérico si el usuario no existe o la contraseña no coincide | Test `verifyCredentialsRejectsUnknownUserAndWrongPasswordWithTheSameMessage` y `loginReturns401WhenCredentialsAreWrong`. |
| REG-02 | Email de login inválido | Test `loginReturns400WhenEmailIsInvalid`. |
| REG-03 | Registro con datos inválidos y contraseña corta | Tests `registerReturnsBadRequestWhenBodyIsInvalid` y `registerReturnsBadRequestWhenPasswordIsTooShort`. |
| REG-04 | Token vencido no cierra sesión | Tests `expiredTokenCannotBeUsed` y `logoutRejectsExpiredToken`. |
| REG-05 | Token ausente o alterado | Tests `logoutRejectsMissingToken`, `logoutReturns400WithoutToken` y `tamperedTokenIsInvalid`. |
| REG-06 | El interno no devuelve credenciales y exige clave | Test `rejectsCallsWithoutInternalToken`, `doesNotExposeTheLegacyCredentialLookup` y `verifiesCredentialsWhenTheInternalTokenMatches`. |
| REG-07 | CVU de 22 dígitos y alias de tres palabras | Test de registro. La unicidad entre tres altas queda para la pasada manual. |
| REG-08 | Error 500 sin filtrar secretos | Solo manual, con el servicio caído. |
| REG-09 | La columna `password` es un hash BCrypt | Test de registro. La lectura directa en MySQL queda para la pasada manual. |

## Evidencia automática

Corrida del 2026-11-19 con `mvn test`. Resultado: 0 fallos.

`users-service`: 16 tests.

- `UserServiceTest`: 4
- `AccountIdentifierGeneratorTest`: 3
- `UserControllerTest`: 6
- `InternalCredentialControllerTest`: 3

`auth-service`: 17 tests.

- `AuthServiceTest`: 8
- `JwtServiceTest`: 2
- `AuthControllerTest`: 6
- `FeignUserDirectoryTest`: 1

`eureka-server`, `api-gateway` y `account-service` compilaron en la misma corrida. No tienen tests de negocio.

Los casos marcados como manuales (Swagger en los tres puertos, tres altas seguidas para unicidad de CVU, error 500 con el servicio caído y la lectura de la columna `password` en MySQL) no forman parte de esta evidencia.
