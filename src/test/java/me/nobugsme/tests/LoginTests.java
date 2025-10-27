package me.nobugsme.tests;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

public class LoginTests {

    @BeforeAll
    public static void setup() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }

    @Test
    public void adminCanGetToken() {
        String base = "http://localhost:4111";

        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body("{\"username\":\"admin\",\"password\":\"admin\"}")
                .post(base + "/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", org.hamcrest.Matchers.notNullValue());
    }

    @Test
    public void userCanGetToken() {
        String base = "http://localhost:4111";
        String admin = "Basic YWRtaW46YWRtaW4=";

        given().contentType(ContentType.JSON).header("Authorization", admin)
                .body("{\"username\":\"kate_login\",\"password\":\"Kate2000#\",\"role\":\"USER\"}")
                .post(base + "/api/v1/admin/users")
                .then().statusCode(HttpStatus.SC_CREATED);

        given().contentType(ContentType.JSON)
                .body("{\"username\":\"kate_login\",\"password\":\"Kate2000#\"}")
                .post(base + "/api/v1/auth/login")
                .then().statusCode(HttpStatus.SC_OK)
                .header("Authorization", org.hamcrest.Matchers.notNullValue());
    }
}