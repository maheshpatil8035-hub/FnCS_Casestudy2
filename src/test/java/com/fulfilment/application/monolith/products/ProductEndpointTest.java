package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  @BeforeEach
  @Transactional
  void setUpProducts() {
    Product.deleteAll();

    Product tonstad = new Product("TONSTAD");
    tonstad.stock = 10;
    tonstad.persist();

    Product kallax = new Product("KALLAX");
    kallax.stock = 5;
    kallax.persist();

    Product besta = new Product("BESTÅ");
    besta.stock = 3;
    besta.persist();
  }

  @Test
  public void testCrudProduct() {
    final String path = "product";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    Product tonstad = Product.find("name", "TONSTAD").firstResult();
    Long tonstadId = tonstad.id;

    // Delete the TONSTAD:
    given().when().delete(path + "/" + tonstadId).then().statusCode(204);

    // List all, TONSTAD should be missing now:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }
}
