package com.digitalmoneyhouse.smoke;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.net.InetSocketAddress;
import java.net.Socket;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@EnabledIfSystemProperty(named = "smoke", matches = "true")
class SprintSmokeTest {

    @BeforeAll
    static void requireGateway() {
        RestAssured.baseURI = "http://localhost:8080";
        Assumptions.assumeTrue(portOpen(8080), "El gateway no está en el puerto 8080");
    }

    @Test
    void registerLoginAccountProfileCardsAndLogout() {
        String suffix = Long.toHexString(System.nanoTime()).replaceAll("[0-9]", "x");
        String email = "smoke" + System.nanoTime() + "@example.com";
        String dni = String.format("%08d", Math.floorMod(System.nanoTime(), 100_000_000L));
        String alias = suffix.substring(0, 3) + "." + suffix.substring(3, 6) + "." + suffix.substring(6, 9);
        String cardNumber = String.format("%016d", Math.floorMod(System.nanoTime(), 10_000_000_000_000_000L));

        var registered = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "firstName": "Ana",
                          "lastName": "Perez",
                          "email": "%s",
                          "dni": "%s",
                          "phoneNumber": "1122334455",
                          "password": "secreta12"
                        }
                        """.formatted(email, dni))
                .when()
                .post("/users/register")
                .then()
                .statusCode(201)
                .body("password", equalTo(null))
                .body("account.cvu", matchesPattern("\\d{22}"))
                .body("account.alias", matchesPattern("[a-z]+\\.[a-z]+\\.[a-z]+"))
                .body("account.balance", anyOf(equalTo(0), equalTo(0.0f), equalTo(0.0)))
                .extract();

        int userId = registered.path("id");
        int accountId = registered.path("account.id");

        String token = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"secreta12"}
                        """.formatted(email))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/accounts/{id}", accountId)
                .then()
                .statusCode(200)
                .body("balance", anyOf(equalTo(0), equalTo(0.0f), equalTo(0.0)))
                .body("cvu", matchesPattern("\\d{22}"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/accounts/{id}/transactions", accountId)
                .then()
                .statusCode(200)
                .body("", empty());

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("limit", 5)
                .when()
                .get("/accounts/{id}/transactions", accountId)
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/users/{id}", userId)
                .then()
                .statusCode(200)
                .body("email", equalTo(email))
                .body("cvu", matchesPattern("\\d{22}"))
                .body("alias", matchesPattern("[a-z]+\\.[a-z]+\\.[a-z]+"))
                .body("password", equalTo(null));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {"firstName":"Ana Maria","lastName":"Perez","phoneNumber":"1199887766"}
                        """)
                .when()
                .put("/users/{id}", userId)
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Ana Maria"));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {"alias":"%s"}
                        """.formatted(alias))
                .when()
                .patch("/accounts/{id}", accountId)
                .then()
                .statusCode(200)
                .body("alias", equalTo(alias));

        int cardId = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "number": "%s",
                          "holderName": "Ana Perez",
                          "expirationDate": "12/28",
                          "cvv": "123",
                          "type": "DEBIT"
                        }
                        """.formatted(cardNumber))
                .when()
                .post("/cards")
                .then()
                .statusCode(201)
                .body("cvv", equalTo(null))
                .extract()
                .path("id");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {"cardId": %d}
                        """.formatted(cardId))
                .when()
                .post("/accounts/{id}/cards", accountId)
                .then()
                .statusCode(201)
                .body("accountId", equalTo(accountId));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/cards/accounts/{accountId}/cards", accountId)
                .then()
                .statusCode(200)
                .body("", hasSize(1));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/cards/accounts/{accountId}/cards/{cardId}", accountId, cardId)
                .then()
                .statusCode(200)
                .body("number", equalTo(cardNumber));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/cards/accounts/{accountId}/cards/{cardId}", accountId, cardId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Tarjeta eliminada"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/user/logout")
                .then()
                .statusCode(200)
                .body("message", equalTo("Sesión cerrada"));
    }

    @Test
    void swaggerRespondsOnTheThreeServices() {
        Assumptions.assumeTrue(portOpen(8081) && portOpen(8082) && portOpen(8083));
        given().when().get("http://localhost:8081/swagger-ui/index.html").then().statusCode(200);
        given().when().get("http://localhost:8082/swagger-ui/index.html").then().statusCode(200);
        given().when().get("http://localhost:8083/swagger-ui/index.html").then().statusCode(200);
    }

    @Test
    void accountWithoutTokenIsForbidden() {
        given()
                .when()
                .get("/accounts/1")
                .then()
                .statusCode(not(200));
    }

    private static boolean portOpen(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 400);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}
