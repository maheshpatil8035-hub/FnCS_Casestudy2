package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class FulfillmentAssignmentService {

  @Inject FulfillmentAssignmentRepository fulfillmentAssignmentRepository;

  @Inject WarehouseRepository warehouseRepository;

  @Transactional
  public FulfillmentAssignment assign(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    validateRequest(storeId, productId, warehouseBusinessUnitCode);
    validateStoreAndProductExist(storeId, productId);
    validateWarehouseExists(warehouseBusinessUnitCode);

    var existing = fulfillmentAssignmentRepository.findByStoreProductAndWarehouse(storeId, productId, warehouseBusinessUnitCode);
    if (existing != null) {
      return existing;
    }

    validateStoreWarehouseLimit(storeId);
    validateProductWarehouseLimit(storeId, productId);
    validateWarehouseProductTypeLimit(warehouseBusinessUnitCode);

    var assignment = new FulfillmentAssignment(storeId, productId, warehouseBusinessUnitCode);
    fulfillmentAssignmentRepository.persist(assignment);
    return assignment;
  }

  private void validateRequest(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    if (storeId == null || productId == null || warehouseBusinessUnitCode == null || warehouseBusinessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Store ID, product ID, and warehouse business unit code are required.");
    }
  }

  private void validateStoreAndProductExist(Long storeId, Long productId) {
    Store store = Store.findById(storeId);
    if (store == null) {
      throw new IllegalArgumentException("Store with id of " + storeId + " does not exist.");
    }

    Product product = Product.findById(productId);
    if (product == null) {
      throw new IllegalArgumentException("Product with id of " + productId + " does not exist.");
    }
  }

  private void validateWarehouseExists(String warehouseBusinessUnitCode) {
    if (warehouseRepository.findByBusinessUnitCode(warehouseBusinessUnitCode) == null) {
      throw new IllegalArgumentException("Warehouse with business unit code " + warehouseBusinessUnitCode + " does not exist.");
    }
  }

  private void validateStoreWarehouseLimit(Long storeId) {
    long uniqueWarehousesForStore = countUniqueWarehouses(fulfillmentAssignmentRepository.findByStoreId(storeId));
    if (uniqueWarehousesForStore >= 3) {
      throw new IllegalArgumentException("Each Store can be fulfilled by a maximum of 3 different Warehouses.");
    }
  }

  private void validateProductWarehouseLimit(Long storeId, Long productId) {
    long uniqueWarehousesForProduct = countUniqueWarehouses(fulfillmentAssignmentRepository.findByStoreAndProduct(storeId, productId));
    if (uniqueWarehousesForProduct >= 2) {
      throw new IllegalArgumentException("Each Product can be fulfilled by a maximum of 2 different Warehouses per Store.");
    }
  }

  private void validateWarehouseProductTypeLimit(String warehouseBusinessUnitCode) {
    long warehouseProductTypes = fulfillmentAssignmentRepository.findByWarehouseBusinessUnitCode(warehouseBusinessUnitCode).stream()
        .map(assignment -> assignment.productId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet())
        .size();
    if (warehouseProductTypes >= 5) {
      throw new IllegalArgumentException("Each Warehouse can store maximally 5 types of Products.");
    }
  }

  private long countUniqueWarehouses(java.util.List<FulfillmentAssignment> assignments) {
    return assignments.stream()
        .map(assignment -> assignment.warehouseBusinessUnitCode)
        .filter(Objects::nonNull)
        .distinct()
        .count();
  }
}
