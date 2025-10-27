package me.nobugsme.tests;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import static io.restassured.RestAssured.given;

public class DepositTests {

    @BeforeAll
    public static void setup() {
        RestAssured.filters(List.of(
                new RequestLoggingFilter(),
                new ResponseLoggingFilter()
        ));
    }

    // ✅ 1. user кладет деньги на свой счет
    @Test
    public void userCanDepositToOwnAccount() {
        String base = "http://localhost:4111";
        String admin = "Basic YWRtaW46YWRtaW4=";

        // создать юзера
        given().contentType(ContentType.JSON)
                .header("Authorization", admin)
                .body("{\"username\":\"kate_deposit\",\"password\":\"Kate2000#\",\"role\":\"USER\"}")
                .post(base + "/api/v1/admin/users")
                .then().statusCode(201);

        // логин
        String u1 = given().contentType(ContentType.JSON)
                .body("{\"username\":\"kate_deposit\",\"password\":\"Kate2000#\"}")
                .post(base + "/api/v1/auth/login")
                .then().statusCode(200)
                .extract().header("Authorization");

        // создать счет
        int a1 = given().header("Authorization", u1)
                .contentType(ContentType.JSON)
                .post(base + "/api/v1/accounts")
                .then().statusCode(201)
                .extract().path("id");

        // положить деньги на свой счет
        given().header("Authorization", u1)
                .contentType(ContentType.JSON)
                .body("{\"id\":" + a1 + ",\"balance\":1000}")
                .post(base + "/api/v1/accounts/deposit")
                .then().statusCode(200);
    }

    // ✅ 2. user1 не может положить деньги на чужой счет (403)
    @Test
    public void userCannotDepositToOthersAccount_403() {
        String base = "http://localhost:4111";
        String admin = "Basic YWRtaW46YWRtaW4=";

        // user1
        given().contentType(ContentType.JSON)
                .header("Authorization", admin)
                .body("{\"username\":\"kate_forbidden\",\"password\":\"Kate2000#\",\"role\":\"USER\"}")
                .post(base + "/api/v1/admin/users")
                .then().statusCode(201);

        String u1 = given().contentType(ContentType.JSON)
                .body("{\"username\":\"kate_forbidden\",\"password\":\"Kate2000#\"}")
                .post(base + "/api/v1/auth/login")
                .then().statusCode(200)
                .extract().header("Authorization");

        // user2
        given().contentType(ContentType.JSON)
                .header("Authorization", admin)
                .body("{\"username\":\"john_forbidden\",\"password\":\"John2000#\",\"role\":\"USER\"}")
                .post(base + "/api/v1/admin/users")
                .then().statusCode(201);

        String u2 = given().contentType(ContentType.JSON)
                .body("{\"username\":\"john_forbidden\",\"password\":\"John2000#\"}")
                .post(base + "/api/v1/auth/login")
                .then().statusCode(200)
                .extract().header("Authorization");

        int a2 = given().header("Authorization", u2)
                .contentType(ContentType.JSON)
                .post(base + "/api/v1/accounts")
                .then().statusCode(201)
                .extract().path("id");

        // user1 кладет на счет user2 → 403
        given().header("Authorization", u1)
                .contentType(ContentType.JSON)
                .body("{\"id\":" + a2 + ",\"balance\":100}")
                .post(base + "/api/v1/accounts/deposit")
                .then().statusCode(403);
    }

    // ✅ 3. проверки transfer: >, =, <, 0, отрицательное
    @Test
    public void transfer_More_Equal_Less_Zero_Negative() {
        String base = "http://localhost:4111";
        String admin = "Basic YWRtaW46YWRtaW4=";

        // user1
        given().contentType(ContentType.JSON)
                .header("Authorization", admin)
                .body("{\"username\":\"kate_transfer\",\"password\":\"Kate2000#\",\"role\":\"USER\"}")
                .post(base + "/api/v1/admin/users")
                .then().statusCode(201);

        String u1 = given().contentType(ContentType.JSON)
                .body("{\"username\":\"kate_transfer\",\"password\":\"Kate2000#\"}")
                .post(base + "/api/v1/auth/login")
                .then().statusCode(200)
                .extract().header("Authorization");

        int a1 = given().header("Authorization", u1)
                .contentType(ContentType.JSON)
                .post(base + "/api/v1/accounts")
                .then().statusCode(201)
                .extract().path("id");

        // user2
        given().contentType(ContentType.JSON)
                .header("Authorization", admin)
                .body("{\"username\":\"john_transfer\",\"password\":\"John2000#\",\"role\":\"USER\"}")
                .post(base + "/api/v1/admin/users")
                .then().statusCode(201);

        String u2 = given().contentType(ContentType.JSON)
                .body("{\"username\":\"john_transfer\",\"password\":\"John2000#\"}")
                .post(base + "/api/v1/auth/login")
                .then().statusCode(200)
                .extract().header("Authorization");

        int a2 = given().header("Authorization", u2)
                .contentType(ContentType.JSON)
                .post(base + "/api/v1/accounts")
                .then().statusCode(201)
                .extract().path("id");

        // user1 кладет себе 1000
        given().header("Authorization", u1)
                .contentType(ContentType.JSON)
                .body("{\"id\":" + a1 + ",\"balance\":1000}")
                .post(base + "/api/v1/accounts/deposit")
                .then().statusCode(200);

        // > balance → 400
        given().header("Authorization", u1)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\":" + a1 + ",\"receiverAccountId\":" + a2 + ",\"amount\":1500}")
                .post(base + "/api/v1/accounts/transfer")
                .then().statusCode(400);

        // = balance → 200
        given().header("Authorization", u1)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\":" + a1 + ",\"receiverAccountId\":" + a2 + ",\"amount\":1000}")
                .post(base + "/api/v1/accounts/transfer")
                .then().statusCode(200);

        // теперь баланс 0 → < balance → 400
        given().header("Authorization", u1)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\":" + a1 + ",\"receiverAccountId\":" + a2 + ",\"amount\":1}")
                .post(base + "/api/v1/accounts/transfer")
                .then().statusCode(400);

        // 0 → 400
        given().header("Authorization", u1)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\":" + a1 + ",\"receiverAccountId\":" + a2 + ",\"amount\":0}")
                .post(base + "/api/v1/accounts/transfer")
                .then().statusCode(400);

        // отрицательная → 400
        given().header("Authorization", u1)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\":" + a1 + ",\"receiverAccountId\":" + a2 + ",\"amount\":-10}")
                .post(base + "/api/v1/accounts/transfer")
                .then().statusCode(400);
    }
}