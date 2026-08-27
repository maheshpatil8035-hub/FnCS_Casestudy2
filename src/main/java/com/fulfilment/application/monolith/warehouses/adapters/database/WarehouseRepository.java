package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final String BUSINESS_UNIT_CODE_FIELD = "businessUnitCode";

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream()
        .map(DbWarehouse::toWarehouse)
        .filter(warehouse -> warehouse.archivedAt == null)
        .toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    var entity = new DbWarehouse();
    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt == null ? LocalDateTime.now() : warehouse.createdAt;
    entity.archivedAt = warehouse.archivedAt;
    persist(entity);
  }

  @Override
  public void update(Warehouse warehouse) {
    var entity = find(BUSINESS_UNIT_CODE_FIELD, warehouse.businessUnitCode).firstResult();
    if (entity == null) {
      throw new IllegalArgumentException("Warehouse with business unit code " + warehouse.businessUnitCode + " does not exist.");
    }

    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt == null ? entity.createdAt : warehouse.createdAt;
    entity.archivedAt = warehouse.archivedAt;
    persist(entity);
  }

  @Override
  public void remove(Warehouse warehouse) {
    var entity = find(BUSINESS_UNIT_CODE_FIELD, warehouse.businessUnitCode).firstResult();
    if (entity != null) {
      delete(entity);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    if (buCode == null || buCode.isBlank()) {
      return null;
    }

    var entity = find(BUSINESS_UNIT_CODE_FIELD, buCode).firstResult();
    return entity == null ? null : entity.toWarehouse();
  }
}
