package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.fulfillment.validator.FulfillmentAssignmentValidator;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class FulfillmentAssignmentService {

  @Inject FulfillmentAssignmentRepository fulfillmentAssignmentRepository;

  @Inject WarehouseRepository warehouseRepository;

  @Inject FulfillmentAssignmentValidator fulfillmentAssignmentValidator;

  @Transactional
  public FulfillmentAssignment assign(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    fulfillmentAssignmentValidator.validateRequest(storeId, productId, warehouseBusinessUnitCode);
    fulfillmentAssignmentValidator.validateStoreAndProductExist(storeId, productId);
    fulfillmentAssignmentValidator.validateWarehouseExists(warehouseBusinessUnitCode, warehouseRepository);

    var existing = fulfillmentAssignmentRepository.findByStoreProductAndWarehouse(storeId, productId, warehouseBusinessUnitCode);
    if (existing != null) {
      return existing;
    }

    fulfillmentAssignmentValidator.validateAssignmentLimits(storeId, productId, warehouseBusinessUnitCode, fulfillmentAssignmentRepository);

    var assignment = new FulfillmentAssignment(storeId, productId, warehouseBusinessUnitCode);
    fulfillmentAssignmentRepository.persist(assignment);
    return assignment;
  }
}
