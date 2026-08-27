package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreEndpointTest {

  @Test
  public void testPatchStoreAllowsZeroQuantityUpdate() {
    String path = "store";
    String name = "PATCH-STORE-" + System.currentTimeMillis();

    int id =
        given()
            .contentType("application/json")
            .body(Map.of("name", name, "quantityProductsInStock", 10))
            .when()
            .post(path)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given()
        .contentType("application/json")
        .body(Map.of("name", name + "-UPDATED", "quantityProductsInStock", 0))
        .when()
        .patch(path + "/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo(name + "-UPDATED"))
        .body("quantityProductsInStock", equalTo(0));

    given().when().get(path).then().statusCode(200).body(containsString(name + "-UPDATED"));
  }
}
