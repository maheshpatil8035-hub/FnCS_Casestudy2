package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  @Test
  public void shouldCreateWarehouseWhenLocationAndCapacityAreValid() {
    var store = new InMemoryWarehouseStore();
    var useCase = new CreateWarehouseUseCase(store, new LocationGateway());
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.900";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 10;
    warehouse.stock = 5;

    assertDoesNotThrow(() -> useCase.create(warehouse));
    assertEquals(1, store.getAll().size());
  }

  @Test
  public void shouldRejectDuplicateBusinessUnitCode() {
    var store = new InMemoryWarehouseStore();
    var useCase = new CreateWarehouseUseCase(store, new LocationGateway());
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.900";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 10;
    warehouse.stock = 5;
    store.create(warehouse);

    var duplicate = new Warehouse();
    duplicate.businessUnitCode = "MWH.900";
    duplicate.location = "AMSTERDAM-001";
    duplicate.capacity = 20;
    duplicate.stock = 10;

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(duplicate));
    assertEquals("Business unit code already exists: MWH.900", ex.getMessage());
  }

  @Test
  public void shouldRejectWarehouseWhenLocationDoesNotExist() {
    var store = new InMemoryWarehouseStore();
    var useCase = new CreateWarehouseUseCase(store, new LocationGateway());
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.901";
    warehouse.location = "UNKNOWN-LOCATION";
    warehouse.capacity = 10;
    warehouse.stock = 5;

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Warehouse location is not valid: UNKNOWN-LOCATION", ex.getMessage());
  }

  @Test
  public void shouldRejectWarehouseWhenLocationCapacityIsExceeded() {
    var store = new InMemoryWarehouseStore();
    var useCase = new CreateWarehouseUseCase(store, new LocationGateway());
    var existing = new Warehouse();
    existing.businessUnitCode = "MWH.500";
    existing.location = "AMSTERDAM-001";
    existing.capacity = 60;
    existing.stock = 20;
    existing.archivedAt = null;
    store.create(existing);

    var warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.501";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Warehouse capacity exceeds the maximum allowed capacity for location AMSTERDAM-001", ex.getMessage());
  }

  private static class InMemoryWarehouseStore implements WarehouseStore {
    private final List<Warehouse> warehouses = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return new ArrayList<>(warehouses);
    }

    @Override
    public void create(Warehouse warehouse) {
      warehouses.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      warehouses.removeIf(current -> current.businessUnitCode.equals(warehouse.businessUnitCode));
      warehouses.add(warehouse);
    }

    @Override
    public void remove(Warehouse warehouse) {
      warehouses.removeIf(current -> current.businessUnitCode.equals(warehouse.businessUnitCode));
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouses.stream()
          .filter(warehouse -> warehouse.businessUnitCode != null && warehouse.businessUnitCode.equalsIgnoreCase(buCode))
          .findFirst()
          .orElse(null);
    }
  }
}
