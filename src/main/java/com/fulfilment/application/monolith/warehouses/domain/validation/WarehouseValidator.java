package com.fulfilment.application.monolith.warehouses.domain.validation;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseValidator {

  public void validateCreateRequirements(Warehouse warehouse, WarehouseStore warehouseStore, LocationResolver locationResolver) {
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

    int totalCapacityAtLocation = warehousesInLocation.stream()
        .mapToInt(existing -> existing.capacity == null ? 0 : existing.capacity)
        .sum();
    if (totalCapacityAtLocation + warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException("Warehouse capacity exceeds the maximum allowed capacity for location " + warehouse.location);
    }

    warehouse.createdAt = warehouse.createdAt == null ? LocalDateTime.now() : warehouse.createdAt;
    warehouse.archivedAt = null;
  }

  public void validateReplaceRequirements(Warehouse replacement, Warehouse existing, WarehouseStore warehouseStore, LocationResolver locationResolver) {
    if (replacement == null) {
      throw new IllegalArgumentException("Replacement warehouse is required.");
    }

    if (replacement.businessUnitCode == null || replacement.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Replacement warehouse business unit code is required.");
    }

    if (existing == null) {
      throw new IllegalArgumentException("Warehouse with business unit code " + replacement.businessUnitCode + " was not found.");
    }

    if (replacement.stock == null || replacement.capacity == null) {
      throw new IllegalArgumentException("Replacement warehouse stock and capacity are required.");
    }

    if (replacement.capacity < existing.stock) {
      throw new IllegalArgumentException("Replacement warehouse capacity cannot accommodate the previous stock.");
    }

    if (!replacement.stock.equals(existing.stock)) {
      throw new IllegalArgumentException("Replacement warehouse stock must match the current warehouse stock.");
    }

    String targetLocation = replacement.location == null ? existing.location : replacement.location;
    Location location = locationResolver.resolveByIdentifier(targetLocation);
    if (location == null) {
      throw new IllegalArgumentException("Warehouse location is not valid: " + targetLocation);
    }

    int totalCapacityAtLocation = warehouseStore.getAll().stream()
        .filter(candidate -> candidate.location != null && candidate.location.equalsIgnoreCase(targetLocation))
        .filter(candidate -> candidate.archivedAt == null || candidate.businessUnitCode.equals(existing.businessUnitCode))
        .mapToInt(candidate -> candidate.capacity == null ? 0 : candidate.capacity)
        .sum();

    if (replacement.capacity > location.maxCapacity || totalCapacityAtLocation + replacement.capacity - existing.capacity > location.maxCapacity) {
      throw new IllegalArgumentException("Replacement warehouse exceeds the location capacity limits.");
    }
  }

  public void validateArchiveRequirements(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse is required.");
    }

    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Warehouse business unit code is required.");
    }

    if (warehouse.archivedAt != null) {
      return;
    }
  }
}
