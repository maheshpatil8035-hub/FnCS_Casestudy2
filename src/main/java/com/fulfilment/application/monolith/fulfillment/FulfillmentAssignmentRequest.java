package com.fulfilment.application.monolith.fulfillment;

public class FulfillmentAssignmentRequest {
  public Long storeId;
  public Long productId;
  public String warehouseBusinessUnitCode;

  public FulfillmentAssignmentRequest() {
    // Intentionally empty: this DTO is created by the JSON binding layer and does
    // not need constructor initialization before the request fields are set.
  }

  public FulfillmentAssignmentRequest(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    this.storeId = storeId;
    this.productId = productId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
  }
}
