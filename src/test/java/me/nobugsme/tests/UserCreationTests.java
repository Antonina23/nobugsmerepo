package me.nobugsme.tests;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

public class UserCreationTests {

    @BeforeAll
    public static void setup() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }

    @Test
    public void adminCanCreateUser_Positive() {
        String base = "http://localhost:4111";
        String admin = "Basic YWRtaW46YWRtaW4=";

        given().contentType(ContentType.JSON).header("Authorization", admin)
                .body("{\"username\":\"kate_user_ok\",\"password\":\"Kate2000#!\",\"role\":\"USER\"}")
                .post(base + "/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo("kate_user_ok"))
                .body("role", Matchers.equalTo("USER"));
    }

    @Test
    public void cannotCreateUser_BlankUsername() {
        String base = "http://localhost:4111";
        String admin = "Basic YWRtaW46YWRtaW4=";

        given().contentType(ContentType.JSON).header("Authorization", admin)
                .body("{\"username\":\"   \",\"password\":\"Password33$\",\"role\":\"USER\"}")
                .post(base + "/api/v1/admin/users")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void cannotCreateUser_ShortUsername() {
        String base = "http://localhost:4111";
        String admin = "Basic YWRtaW46YWRtaW4=";

        given().contentType(ContentType.JSON).header("Authorization", admin)
                .body("{\"username\":\"ab\",\"password\":\"Password33$\",\"role\":\"USER\"}")
                .post(base + "/api/v1/admin/users")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void cannotCreateUser_WrongCharsInUsername() {
        String base = "http://localhost:4111";
        String admin = "Basic YWRtaW46YWRtaW4=";

        given().contentType(ContentType.JSON).header("Authorization", admin)
                .body("{\"username\":\"abc$%^\",\"password\":\"Password33$\",\"role\":\"USER\"}")
                .post(base + "/api/v1/admin/users")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void cannotCreateUser_InvalidRole() {
        String base = "http://localhost:4111";
        String admin = "Basic YWRtaW46YWRtaW4=";

        given().contentType(ContentType.JSON).header("Authorization", admin)
                .body("{\"username\":\"kate_bad_role\",\"password\":\"Password33$\",\"role\":\"EDITOR\"}")
                .post(base + "/api/v1/admin/users")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void cannotCreateUser_BlankPassword() {
        String base = "http://localhost:4111";
        String admin = "Basic YWRtaW46YWRtaW4=";

        given().contentType(ContentType.JSON).header("Authorization", admin)
                .body("{\"username\":\"kate_blank_pwd\",\"password\":\" \",\"role\":\"USER\"}")
                .post(base + "/api/v1/admin/users")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void cannotCreateUser_ShortPassword() {
        String base = "http://localhost:4111";
        String admin = "Basic YWRtaW46YWRtaW4=";

        given().contentType(ContentType.JSON).header("Authorization", admin)
                .body("{\"username\":\"kate_short_pwd\",\"password\":\"Abc12$\",\"role\":\"USER\"}")
                .post(base + "/api/v1/admin/users")
                .then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}