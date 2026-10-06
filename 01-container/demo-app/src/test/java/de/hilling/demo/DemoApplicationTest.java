package de.hilling.demo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class DemoApplicationTest {

    @Test
    void servesIndex() {
        given().get("/").then().statusCode(200).body(containsString("Hallo aus dem Test!"));
        given().get("/index.html").then().statusCode(200).contentType(containsString("text/html"));
    }

    @Test
    void missingFileIsNotFound() {
        given().get("/does-not-exist.html").then().statusCode(404);
    }

    @Test
    void pathTraversalIsRejected() {
        given().urlEncodingEnabled(false).get("/..%2Fpom.xml").then().statusCode(400);
    }

    @Test
    void healthCanBeToggled() {
        given().get("/health").then().statusCode(200).body("status", equalTo("UP"));
        given().post("/admin/health/toggle").then().statusCode(200).body("status", equalTo("DOWN"));
        given().get("/health").then().statusCode(503).body("status", equalTo("DOWN"));
        given().post("/admin/health/toggle").then().statusCode(200).body("status", equalTo("UP"));
    }

    @Test
    void infoShowsVersionAndHostname() {
        given().get("/info").then().statusCode(200)
                .body("version", equalTo("dev"))
                .body("hostname", notNullValue())
                .body("message", equalTo("Hallo aus der Demo-App"));
    }

    @Test
    void quarkusHealthFollowsToggle() {
        given().get("/q/health/live").then().statusCode(200).body("status", equalTo("UP"));
        given().get("/q/health/ready").then().statusCode(200).body("status", equalTo("UP"));
        given().post("/admin/health/toggle").then().statusCode(200);
        given().get("/q/health/ready").then().statusCode(503).body("status", equalTo("DOWN"));
        given().get("/q/health/live").then().statusCode(200).body("status", equalTo("UP"));
        given().post("/admin/health/toggle").then().statusCode(200);
        given().get("/q/health/ready").then().statusCode(200);
    }
}
