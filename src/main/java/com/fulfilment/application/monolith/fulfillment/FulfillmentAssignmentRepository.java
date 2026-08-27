package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FulfillmentAssignmentRepository implements PanacheRepositoryBase<FulfillmentAssignment, Long> {

  public List<FulfillmentAssignment> findByStoreId(Long storeId) {
    return list("storeId = ?1", storeId);
  }

  public List<FulfillmentAssignment> findByStoreAndProduct(Long storeId, Long productId) {
    return list("storeId = ?1 and productId = ?2", storeId, productId);
  }

  public List<FulfillmentAssignment> findByWarehouseBusinessUnitCode(String warehouseBusinessUnitCode) {
    return list("warehouseBusinessUnitCode = ?1", warehouseBusinessUnitCode);
  }

  public FulfillmentAssignment findByStoreProductAndWarehouse(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    return find("storeId = ?1 and productId = ?2 and warehouseBusinessUnitCode = ?3",
            storeId, productId, warehouseBusinessUnitCode).firstResult();
  }
}
