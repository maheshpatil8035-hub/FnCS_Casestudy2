package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfillmentAssignmentServiceTest {

  @Inject FulfillmentAssignmentService fulfillmentAssignmentService;

  @Inject FulfillmentAssignmentRepository fulfillmentAssignmentRepository;

  @Inject WarehouseRepository warehouseRepository;

  @BeforeEach
  @Transactional
  void setUp() {
    fulfillmentAssignmentRepository.deleteAll();
    Store.deleteAll();
    Product.deleteAll();
    warehouseRepository.deleteAll();
  }

  @Test
  @Transactional
  void shouldAssignAValidWarehouseForStoreAndProduct() {
    Store store = new Store("F-STORE-1");
    store.persist();

    Product product = new Product("F-PRODUCT-1");
    product.persist();

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.900";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;
    warehouseRepository.create(warehouse);

    FulfillmentAssignment assignment = fulfillmentAssignmentService.assign(store.id, product.id, warehouse.businessUnitCode);

    assertNotNull(assignment);
    assertEquals(store.id, assignment.storeId);
    assertEquals(product.id, assignment.productId);
    assertEquals("MWH.900", assignment.warehouseBusinessUnitCode);
  }

  @Test
  @Transactional
  void shouldRejectWhenStoreReachMaxWarehouses() {
    Store store = new Store("F-STORE-2");
    store.persist();

    Product productA = new Product("F-PRODUCT-A");
    productA.persist();

    Product productB = new Product("F-PRODUCT-B");
    productB.persist();

    Product productC = new Product("F-PRODUCT-C");
    productC.persist();

    createWarehouse("MWH.901", "ZWOLLE-001");
    createWarehouse("MWH.902", "ZWOLLE-002");
    createWarehouse("MWH.903", "AMSTERDAM-001");

    fulfillmentAssignmentService.assign(store.id, productA.id, "MWH.901");
    fulfillmentAssignmentService.assign(store.id, productB.id, "MWH.902");
    fulfillmentAssignmentService.assign(store.id, productC.id, "MWH.903");

    Product productD = new Product("F-PRODUCT-D");
    productD.persist();

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> fulfillmentAssignmentService.assign(store.id, productD.id, "MWH.901"));

    assertEquals("Each Store can be fulfilled by a maximum of 3 different Warehouses.", exception.getMessage());
  }

  private void createWarehouse(String businessUnitCode, String location) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = 20;
    warehouse.stock = 10;
    warehouseRepository.create(warehouse);
  }
}
