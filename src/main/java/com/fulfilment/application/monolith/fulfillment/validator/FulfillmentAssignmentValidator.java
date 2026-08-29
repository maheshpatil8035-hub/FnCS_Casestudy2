package com.fulfilment.application.monolith.fulfillment.validator;

import com.fulfilment.application.monolith.fulfillment.FulfillmentAssignment;
import com.fulfilment.application.monolith.fulfillment.FulfillmentAssignmentRepository;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class FulfillmentAssignmentValidator {

  public void validateRequest(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    if (storeId == null || productId == null || warehouseBusinessUnitCode == null || warehouseBusinessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Store ID, product ID, and warehouse business unit code are required.");
    }
  }

  public void validateStoreAndProductExist(Long storeId, Long productId) {
    Store store = Store.findById(storeId);
    if (store == null) {
      throw new IllegalArgumentException("Store with id of " + storeId + " does not exist.");
    }

    Product product = Product.findById(productId);
    if (product == null) {
      throw new IllegalArgumentException("Product with id of " + productId + " does not exist.");
    }
  }

  public void validateWarehouseExists(String warehouseBusinessUnitCode, WarehouseRepository warehouseRepository) {
    if (warehouseRepository.findByBusinessUnitCode(warehouseBusinessUnitCode) == null) {
      throw new IllegalArgumentException("Warehouse with business unit code " + warehouseBusinessUnitCode + " does not exist.");
    }
  }

  public void validateAssignmentLimits(Long storeId, Long productId, String warehouseBusinessUnitCode,
      FulfillmentAssignmentRepository fulfillmentAssignmentRepository) {
    long uniqueWarehousesForStore = countUniqueWarehouses(fulfillmentAssignmentRepository.findByStoreId(storeId));
    if (uniqueWarehousesForStore >= 3) {
      throw new IllegalArgumentException("Each Store can be fulfilled by a maximum of 3 different Warehouses.");
    }

    long uniqueWarehousesForProduct = countUniqueWarehouses(fulfillmentAssignmentRepository.findByStoreAndProduct(storeId, productId));
    if (uniqueWarehousesForProduct >= 2) {
      throw new IllegalArgumentException("Each Product can be fulfilled by a maximum of 2 different Warehouses per Store.");
    }

    long warehouseProductTypes = fulfillmentAssignmentRepository.findByWarehouseBusinessUnitCode(warehouseBusinessUnitCode).stream()
        .map(assignment -> assignment.productId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet())
        .size();
    if (warehouseProductTypes >= 5) {
      throw new IllegalArgumentException("Each Warehouse can store maximally 5 types of Products.");
    }
  }

  private long countUniqueWarehouses(List<FulfillmentAssignment> assignments) {
    return assignments.stream()
        .map(assignment -> assignment.warehouseBusinessUnitCode)
        .filter(Objects::nonNull)
        .distinct()
        .count();
  }
}
