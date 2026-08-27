package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "fulfillment_assignment",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"store_id", "product_id", "warehouse_business_unit_code"})
    })
public class FulfillmentAssignment extends PanacheEntityBase {

  @Id @GeneratedValue public Long id;

  @Column(name = "store_id", nullable = false)
  public Long storeId;

  @Column(name = "product_id", nullable = false)
  public Long productId;

  @Column(name = "warehouse_business_unit_code", nullable = false, length = 50)
  public String warehouseBusinessUnitCode;

  public FulfillmentAssignment() {
    // Intentionally empty: JPA requires a no-arg constructor, and the assignment
    // is fully populated through the explicit constructor used by the service.
  }

  public FulfillmentAssignment(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    this.storeId = storeId;
    this.productId = productId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
  }
}
