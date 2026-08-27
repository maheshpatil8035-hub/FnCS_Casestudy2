package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse is required.");
    }

    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Warehouse business unit code is required.");
    }

    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new IllegalArgumentException("Warehouse location is required.");
    }

    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new IllegalArgumentException("Warehouse capacity must be greater than zero.");
    }

    if (warehouse.stock == null || warehouse.stock < 0 || warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Warehouse stock must be between zero and capacity.");
    }

    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new IllegalArgumentException("Business unit code already exists: " + warehouse.businessUnitCode);
    }

    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Warehouse location is not valid: " + warehouse.location);
    }

    List<Warehouse> warehousesInLocation = warehouseStore.getAll().stream()
        .filter(existing -> existing.location != null && existing.location.equalsIgnoreCase(warehouse.location))
        .filter(existing -> existing.archivedAt == null)
        .toList();

    if (warehousesInLocation.size() >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException("Location has reached the maximum warehouse count: " + warehouse.location);
    }

    int totalCapacityAtLocation = warehousesInLocation.stream().mapToInt(wh -> wh.capacity).sum();
    if (totalCapacityAtLocation + warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException("Warehouse capacity exceeds the maximum allowed capacity for location " + warehouse.location);
    }

    warehouse.createdAt = warehouse.createdAt == null ? LocalDateTime.now() : warehouse.createdAt;
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);
  }
}
