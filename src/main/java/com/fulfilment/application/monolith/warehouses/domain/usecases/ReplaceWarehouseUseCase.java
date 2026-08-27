package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse == null) {
      throw new IllegalArgumentException("Replacement warehouse is required.");
    }

    if (newWarehouse.businessUnitCode == null || newWarehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Replacement warehouse business unit code is required.");
    }

    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new IllegalArgumentException("Warehouse with business unit code " + newWarehouse.businessUnitCode + " was not found.");
    }

    if (newWarehouse.stock == null || newWarehouse.capacity == null) {
      throw new IllegalArgumentException("Replacement warehouse stock and capacity are required.");
    }

    if (newWarehouse.capacity < existing.stock) {
      throw new IllegalArgumentException("Replacement warehouse capacity cannot accommodate the previous stock.");
    }

    if (!newWarehouse.stock.equals(existing.stock)) {
      throw new IllegalArgumentException("Replacement warehouse stock must match the current warehouse stock.");
    }

    Location location = locationResolver.resolveByIdentifier(newWarehouse.location == null ? existing.location : newWarehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Warehouse location is not valid: " + (newWarehouse.location == null ? existing.location : newWarehouse.location));
    }

    int totalCapacityAtLocation = warehouseStore.getAll().stream()
        .filter(w -> w.location != null && w.location.equalsIgnoreCase(newWarehouse.location == null ? existing.location : newWarehouse.location))
        .filter(w -> w.archivedAt == null || w.businessUnitCode.equals(existing.businessUnitCode))
        .mapToInt(w -> w.capacity == null ? 0 : w.capacity)
        .sum();

    if (newWarehouse.capacity > location.maxCapacity || totalCapacityAtLocation + newWarehouse.capacity - existing.capacity > location.maxCapacity) {
      throw new IllegalArgumentException("Replacement warehouse exceeds the location capacity limits.");
    }

    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);

    newWarehouse.createdAt = existing.createdAt;
    newWarehouse.archivedAt = null;
    warehouseStore.update(newWarehouse);
  }
}
