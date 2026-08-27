package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  @Test
  public void shouldReplaceWarehouseWhenNewStockAndCapacityAreValid() {
    var store = new InMemoryWarehouseStore();
    var resolver = new StubLocationResolver(new Location("WH-001", 2, 50));
    var useCase = new ReplaceWarehouseUseCase(store, resolver);

    var current = new Warehouse();
    current.businessUnitCode = "MWH.200";
    current.location = "WH-001";
    current.capacity = 20;
    current.stock = 10;
    store.create(current);

    var replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.200";
    replacement.location = "WH-001";
    replacement.capacity = 30;
    replacement.stock = 10;

    assertDoesNotThrow(() -> useCase.replace(replacement));
    assertEquals(1, store.getAll().size());
    assertEquals(30, store.findByBusinessUnitCode("MWH.200").capacity);
  }

  @Test
  public void shouldRejectReplacementWhenCapacityCannotSustainExistingStock() {
    var store = new InMemoryWarehouseStore();
    var resolver = new StubLocationResolver(new Location("WH-001", 2, 50));
    var useCase = new ReplaceWarehouseUseCase(store, resolver);

    var current = new Warehouse();
    current.businessUnitCode = "MWH.210";
    current.location = "WH-001";
    current.capacity = 20;
    current.stock = 10;
    store.create(current);

    var replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.210";
    replacement.location = "WH-001";
    replacement.capacity = 8;
    replacement.stock = 10;

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    assertEquals("Replacement warehouse capacity cannot accommodate the previous stock.", ex.getMessage());
  }

  @Test
  public void shouldRejectReplacementWhenStockDoesNotMatchCurrentWarehouse() {
    var store = new InMemoryWarehouseStore();
    var resolver = new StubLocationResolver(new Location("WH-001", 2, 50));
    var useCase = new ReplaceWarehouseUseCase(store, resolver);

    var current = new Warehouse();
    current.businessUnitCode = "MWH.220";
    current.location = "WH-001";
    current.capacity = 20;
    current.stock = 10;
    store.create(current);

    var replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.220";
    replacement.location = "WH-001";
    replacement.capacity = 25;
    replacement.stock = 9;

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    assertEquals("Replacement warehouse stock must match the current warehouse stock.", ex.getMessage());
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

  private static class StubLocationResolver implements LocationResolver {
    private final Location location;

    private StubLocationResolver(Location location) {
      this.location = location;
    }

    @Override
    public Location resolveByIdentifier(String identifier) {
      return identifier != null && identifier.equalsIgnoreCase(location.identification) ? location : null;
    }
  }
}
