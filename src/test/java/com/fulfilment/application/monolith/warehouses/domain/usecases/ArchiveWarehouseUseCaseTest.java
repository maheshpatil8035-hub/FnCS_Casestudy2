package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  @Test
  public void shouldArchiveWarehouse() {
    var store = new InMemoryWarehouseStore();
    var useCase = new ArchiveWarehouseUseCase(store);
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.800";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 20;
    warehouse.stock = 5;
    store.create(warehouse);

    assertDoesNotThrow(() -> useCase.archive(warehouse));
    assertNotNull(warehouse.archivedAt);
  }

  @Test
  public void shouldIgnoreAlreadyArchivedWarehouse() {
    var store = new InMemoryWarehouseStore();
    var useCase = new ArchiveWarehouseUseCase(store);
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.801";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 30;
    warehouse.stock = 8;
    warehouse.archivedAt = null;
    store.create(warehouse);

    useCase.archive(warehouse);
    assertNotNull(warehouse.archivedAt);

    var previousArchive = warehouse.archivedAt;
    useCase.archive(warehouse);
    assertEquals(previousArchive, warehouse.archivedAt);
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
